package ru.kostyanoy.dataexchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kostyanoy.connection.*;
import ru.kostyanoy.entity.PlayerState;
import timer.TimeMeter;

import java.io.IOException;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientExchanger implements Exchanger {
    private final Connection connection;
    private final PlayerState playerState;
    private String senderName;
    private final TimeMeter requestDelayTimer;
    private final long responseDelay;
    private final AtomicBoolean isRemoteAnswering;
    private final AtomicBoolean isSenderNameAccepted;
    private boolean isGameAllowed;
    private final ObjectMapper mapper;
    private final ConcurrentHashMap<String, Request> sentRequestMap;
    private final ConcurrentHashMap<String, Request> expiredRequestMap;
    private final ConcurrentHashMap<String, Request> successfulRequestMap;
    Thread messageListenerThread;
    Thread serviceExchangeThread;

    private static final Logger log = LoggerFactory.getLogger(ClientExchanger.class);

    static {
        Connection.customizeConnectionClass("connection.properties");
    }

    public ClientExchanger() {
        isRemoteAnswering = new AtomicBoolean(false);
        isSenderNameAccepted = new AtomicBoolean(false);
        isGameAllowed = false;
        this.connection = new Connection();
        requestDelayTimer = new TimeMeter(Connection.PING_TIMEOUT * 4);
        responseDelay = Connection.PING_TIMEOUT * 2;
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        sentRequestMap = new ConcurrentHashMap<>();
        expiredRequestMap = new ConcurrentHashMap<>();
        successfulRequestMap = new ConcurrentHashMap<>();
        playerState = new PlayerState(0,"");
    }

    public boolean isGameAllowed() {
        return isGameAllowed;
    }

    @Override
    public void startExchange() {
        messageListenerThread = new Thread(() -> {
            while (!Thread.interrupted() && isRemoteAnswering.get()) {
                try {
                    parseMessage();
                } catch (IOException e) {
                    log.warn(e.getMessage(), e);
                }
            }
            connection.disconnect();
        });
        messageListenerThread.start();

        serviceExchangeThread = new Thread(this::serviceExchange);
        serviceExchangeThread.start();
    }

    public Connection getConnection() {
        return connection;
    }

    public PlayerState getPlayerState() {
        return playerState;
    }

    @Override
    public boolean sendMessage(Message message) {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        try {
            connection.getWriter().println(mapper.writeValueAsString(message));
            connection.getWriter().flush();
            sentRequestMap.put(message.getId(), (Request) message);
            requestDelayTimer.restartTimer();
        } catch (JsonProcessingException e) {
            log.warn(e.getMessage(), e);
            return false;
        }
        return true;
    }

    private void sleep(int timeout) {
        try {
            Thread.sleep(Math.abs(timeout));
        } catch (InterruptedException e) {
            log.warn(e.getMessage(), e);
        }
    }

    @Override
    public boolean hasCheckedNickName(String nickName) {
        while (isRemoteAnswering.get()) {
            if (!isSenderNameAccepted.get()) {
                sendMessage(new Request(nickName, MessageType.GREETING, 0));
            }
            sleep(Connection.PING_TIMEOUT * 2);
        }


        startExchange();
        return true;
    }

    public void stopExchange() {
        sendMessage(new Request(senderName, MessageType.GOODBYE, playerState.getTokenCount()));
        sleep(Connection.PING_TIMEOUT * 3);

        serviceExchangeThread.interrupt();
        messageListenerThread.interrupt();
        connection.disconnect();
    }

    @Override
    public void serviceExchange() { //TODO Логика проверки соединения, для сервера тоже
        if (!connection.isConnected()) {
            isRemoteAnswering.set(false);
            return;
        }

        sendMessage(new Request(senderName, MessageType.SERVICE, playerState.getTokenCount()));

        while (!requestDelayTimer.hasTimesUp()) { //Ждать, пока не пройдёт время ожидания. Время ожидания сбрасывается при получении сообщения
            sleep(Connection.PING_TIMEOUT >> 1);
            sentRequestMap.forEach((key, value) -> {
                if ((System.nanoTime()
                        - value.getSendTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        >= responseDelay) {
                    log.info("Request expired: {}", sentRequestMap.get(key));
                    expiredRequestMap.put(key, value);
                    sentRequestMap.remove(key);
                }
            });
        }
        stopExchange();
    }

    public void askGamePermission() {
        sendMessage(new Request(senderName, MessageType.GAMEPERMISSION, playerState.getTokenCount()));
    }

    private void parseMessage() throws IOException {
        while (!connection.getReader().ready()) {
            sleep(Connection.PING_TIMEOUT >> 2);
        }

        String jsonMessage = connection.getReader().readLine();
        Message incomingMessage = mapper.readValue(jsonMessage, Message.class);

        if ((incomingMessage == null)) {
            return;
        }

        log.info("{}: Have got {}", senderName, incomingMessage.getClass().getSimpleName());

        if (incomingMessage instanceof Request) {
            log.info("{} sent request. Requests from server are not supported", incomingMessage.getSenderName());
            return;
        }

        Response response;
        if (incomingMessage instanceof Response) {
            response = (Response) incomingMessage;
        } else {
            log.warn("{} sent unsupported type message", incomingMessage.getSenderName());
            return;
        }

        if (!sentRequestMap.containsKey(response.getId())) {
            log.warn("{} sent unrequested response: {}", incomingMessage.getSenderName(), response);
            return;
        }

        if (expiredRequestMap.containsKey(response.getId())) {
            log.warn("{} sent expired response: {}", incomingMessage.getSenderName(), response);
            return;
        }

        switch (response.getType()) {
            case GREETING -> {
                senderName = (response.getStatus() == Status.ACCEPTED)
                    ? sentRequestMap.get(response.getId()).getSenderName()
                    : null;
                isSenderNameAccepted.set(true);
                setPlayerState(response);
            }

            case STAKE -> {
                if (response.getStatus() == Status.ACCEPTED) {
                    setPlayerState(response);
                }
            }
            case GAMEPERMISSION -> {
                setPlayerState(response);
                isGameAllowed = response.getStatus() == Status.ACCEPTED;
            }
            case GOODBYE -> {
                isRemoteAnswering.set(false);
                requestDelayTimer.stopAndResetTimer(0);
            }
            case SERVICE -> setPlayerState(response);

            default -> log.warn("{} sent unexpected response type: {}", incomingMessage.getSenderName(), response);
        }
        successfulRequestMap.put(sentRequestMap.get(response.getId()).

                getId(), sentRequestMap.

                get(response.getId()));
        sentRequestMap.remove(response.getId());
        requestDelayTimer.restartTimer();
        //TODO Статистика подсчёта успешных ответов и времени выполнения запросов

    }

    private void setPlayerState(Response response) {
        playerState.setTokenCount(isSenderNameAccepted.get()
                ? response.getTokens()
                : playerState.getTokenCount());
    }

    //TODO Класс для подсчёта статистики внутри Exchanger'а: Пользователь | Успешные запросы | Неуспешные запросы | Среднее время запроса
    //Неуспешные ответы считать по expiredRequestMap
}
