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
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientExchanger implements Exchanger {
    private Connection connection;
    private PlayerState playerState;
    private String senderName;
    private TimeMeter requestDelayTimer;
    private TimeMeter responseDelayTimer;
    private AtomicBoolean isRemoteAnswering;
    private ObjectMapper mapper;
    private ConcurrentHashMap<String, Request> sentRequestQueue;

    private static final Logger log = LoggerFactory.getLogger(ClientExchanger.class);

    static {
        Connection.customizeConnectionClass("connection.properties");
    }

    public ClientExchanger() {
        isRemoteAnswering.set(false);
        this.connection = new Connection();
        requestDelayTimer = new TimeMeter(Connection.PING_TIMEOUT*4);
        responseDelayTimer = new TimeMeter(Connection.PING_TIMEOUT*2);
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        sentRequestQueue = new ConcurrentHashMap<>();
    }

    @Override
    public void startExchange() {

        //playerState = new PlayerState();
        //nickName = "";



        Thread messageListenerThread = new Thread(() -> {
            while (!Thread.interrupted() && isRemoteAnswering.get()) {
                try {
                    parseMessage();
                } catch (IOException | InterruptedException e) {
                    log.warn(e.getMessage(), e);
                }
            }
            connection.disconnect();
        });
        messageListenerThread.start();

        Thread serviceExchangeThread = new Thread(() -> {
            serviceExchange();
        });
        serviceExchangeThread.start();


    }

    public Connection getConnection() {
        return connection;
    }

    public String[] getPlayerState() {


        return tokenCount.toArray(new String[0]);
    }

    @Override
    public boolean sendMessage(Message message) {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        try {
            connection.getWriter().println(mapper.writeValueAsString(message));
            connection.getWriter().flush();
            sentRequestQueue.put(message.getId(), (Request) message);
            requestDelayTimer.restartTimer();
        } catch (JsonProcessingException e) {
            log.warn(e.getMessage(), e);
            return false;
        }
        return true;
    }

    @Override
    public void clearUnansweredMessages() {
        tokenCount.clear();
    }

    @Override
    public boolean hasCheckedNickName(String nickName) { //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        while (isServerAnswering) {
            sendMessage(new Request(nickName, MessageType.GREETING));
        }

        try {
            parseMessage();
        } catch (IOException | InterruptedException e) {
            log.warn(e.getMessage(), e);
            return false;
        }
        if (this.senderName.isEmpty()) {
            return false;
        }
        startExchange();
        return true;
    }

    public void stopExchange() {
        sendMessage(new MessageGoodbye(senderName));

        try {
            Thread.sleep(Connection.PING_TIMEOUT * 2);
        } catch (InterruptedException e) {
            log.warn(e.getMessage(), e);
        }
        connection.disconnect();
    }

    @Override
    public void serviceExchange() { //TODO Логика проверки соединения, для сервера тоже
        if (!connection.isConnected()) {
            isRemoteAnswering.set(false);
            return;
        }

        sendMessage(new Request(senderName, MessageType.SERVICE, 0));

        while (!responseDelayTimer.hasTimesUp()) {

            // Не нужен response timer. Нужно только фиксированное время ожидания, т.к. время отправки есть в отправленном сообщении
            //TODO Пробежаться по мапе и проверить ниличие ждущих ответа запросов.
            //TODO Если таких нет, то проверить, не закончилось ли время

            while (!requestDelayTimer.hasTimesUp()) {


                try {
                    Thread.sleep(Connection.PING_TIMEOUT);
                    if (isRemoteAnswering.get()) {
                        return;
                    }
                } catch (InterruptedException e) {
                    log.warn(e.getMessage(), e);
                }
            }
        }
        isRemoteAnswering.set(false);
        return;
    }

    //TODO Проверка списка сообщений на "протухшие" сообщения, т.е. время жизни вышло, а ответа так и нет

    private void NNN() {

    }

    private void parseMessage() throws IOException, InterruptedException {
        while (!connection.getReader().ready()) {
            Thread.sleep(Connection.PING_TIMEOUT >> 2);
        }

        String jsonMessage = connection.getReader().readLine();
        Message incomingMessage = mapper.readValue(jsonMessage, Message.class);

        if ((incomingMessage == null)) { return; }

        log.info("{}: Have got {}", senderName, incomingMessage.getClass().getSimpleName());

        if (incomingMessage instanceof Request) {

            log.info("{}: sent request. Requests are not supported", senderName);
            return;
        }

        Response response;
        if (incomingMessage instanceof Response) {
            response = (Response)incomingMessage;
        }
        else {
            return;
        }

        //TODO сверить с id отправленного, после обработки ответа удалить его из списка направленных пакетов

        switch (response.getType()) {
            case SERVICE ->
        }


        stopExchange();
        return;

        if (incomingMessage instanceof MessageParticipants) {
            participants = incomingMessage.getMessages().clone();
            log.info("{}: participants are updated:\n{}", senderName, Arrays.toString(participants));
            return;
        }
        if (incomingMessage instanceof MessageSentByUser) {
            tokenCount.addAll(Arrays.asList(incomingMessage.getMessages()));
            log.info("{}: messages are updated:\n{}", senderName, tokenCount.toString());
        }
        if (incomingMessage instanceof MessageService) {
            log.info("{}: Have got service message:\n{}", senderName, incomingMessage.getMessages());
            serviceExchange();
        }
    }
}
