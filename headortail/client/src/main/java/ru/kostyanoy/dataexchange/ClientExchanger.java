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
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientExchanger {
    private final Connection connection;
    private final PlayerState playerState;
    private String senderName;
    private final TimeMeter requestDelayTimer;
    private final long responseDelay;
    private final AtomicBoolean isRemoteAnswering;
    private final AtomicBoolean isSenderNameAccepted;

    private final ObjectMapper mapper;
    private final ConcurrentHashMap<String, Request> sentRequestMap;
    private final ConcurrentHashMap<String, Request> expiredRequestMap;
    private Thread messageListenerThread;
    private Thread serviceExchangeThread;
    private Optional<ClientStatistics> statistics;
    private final ClientStatistics internalStatistics;

    private String previousRoundResult;

    private String[] possibleOptions;

    private static final Logger log = LoggerFactory.getLogger(ClientExchanger.class);

    static {
        Connection.customizeConnectionClass("connection.properties");
    }

    public ClientExchanger() {
        isRemoteAnswering = new AtomicBoolean(false);
        isSenderNameAccepted = new AtomicBoolean(false);
        this.connection = new Connection();
        requestDelayTimer = new TimeMeter(Connection.PING_TIMEOUT * 5000);
        responseDelay = Connection.PING_TIMEOUT * 10;
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        sentRequestMap = new ConcurrentHashMap<>();
        expiredRequestMap = new ConcurrentHashMap<>();
        playerState = new PlayerState(0);
        statistics = Optional.empty();
        internalStatistics = new ClientStatistics();
        possibleOptions = new String[0];
        previousRoundResult = "";
    }

    public void startExchange() {
        messageListenerThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    parseMessage();
                } catch (IOException e) {
                    log.warn(e.getMessage(), e);
                }
            }
        });
        messageListenerThread.setName("messageListenerThread");
        messageListenerThread.start();
        log.debug("messageListenerThread started");

        serviceExchangeThread = new Thread(this::checkExchange);
        serviceExchangeThread.setName("serviceExchangeThread");
        serviceExchangeThread.start();
        log.debug("serviceExchangeThread started");
    }

    public Connection getConnection() {
        return connection;
    }

    public PlayerState getPlayerState() {
        return playerState;
    }

    public Optional<ClientStatistics> getStatistics() {
        return statistics;
    }


    public void sendMessage(Message message) {
        try {
            connection.getWriter().println(mapper.writeValueAsString(message));
            connection.getWriter().flush();
            sentRequestMap.put(message.getMessageID(), (Request) message);
            log.info("Sent message {}", message.toString());
        } catch (JsonProcessingException e) {
            log.warn(e.getMessage(), e);
        }
    }

    private void sleep(int timeout) {
        try {
            Thread.sleep(Math.abs(timeout));
        } catch (InterruptedException e) {
            log.warn(e.getMessage(), e);
        }
    }

    public boolean hasCheckedNickName(String nickName) {
        if (isRemoteAnswering.get() && !isSenderNameAccepted.get()) {
            sendMessage(new Request(nickName, MessageCategory.GREETING, 0));
        }
        sleep(Connection.PING_TIMEOUT * 2);
        return isSenderNameAccepted.get();
    }

    public void stopExchange() {
        sendMessage(new Request(senderName, MessageCategory.GOODBYE, playerState.getTokenCount()));
        sleep(Connection.PING_TIMEOUT * 3);

        serviceExchangeThread.interrupt();
        messageListenerThread.interrupt();
        isRemoteAnswering.set(false);
        connection.disconnect();
        if (internalStatistics != null) {
            statistics = Optional.of(internalStatistics);
        }
    }

    public void checkExchange() {
        requestDelayTimer.startTimer();
        while (!requestDelayTimer.hasTimesUp()) {
            if (!connection.isConnected()) {
                isRemoteAnswering.set(false);
                log.info("Server is disconnected");
                break;
            }

            sendMessage(new Request(senderName, MessageCategory.SERVICE, playerState.getTokenCount()));

            sleep(Connection.PING_TIMEOUT*2);

            sentRequestMap.forEach((key, value) -> {
                if ((System.currentTimeMillis()
                        - value.getSendTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        >= responseDelay) {
                    log.info("Request expired: {}", value);
                    expiredRequestMap.put(key, value);
                    sentRequestMap.remove(key);
                }
            });
        }
        stopExchange();
    }

    public void sendStake(long stake, String option) {
        if (option == null || option.isEmpty() || stake <= 0) return;

        if (isRemoteAnswering.get() && isSenderNameAccepted.get()) {
            sendMessage(new Request(senderName, MessageCategory.STAKE, stake, option));
        }
    }

    private void parseMessage() throws IOException {
        while (!connection.getReader().ready()) {
            sleep(Connection.PING_TIMEOUT >> 2);
        }

        String jsonMessage = connection.getReader().readLine();
        Message incomingMessage = mapper.readValue(jsonMessage, Message.class);

        if (incomingMessage == null) {
            return;
        }

        log.info("Have got {}", incomingMessage);

        if (incomingMessage instanceof Request) {
            log.info("{} sent request. Requests from server are not supported", incomingMessage.getSenderName());
            return;
        }

        Response response;
        if (incomingMessage instanceof Response) {
            response = (Response) incomingMessage;
        } else {
            log.warn("{} sent unsupported type of message", incomingMessage.getSenderName());
            return;
        }

        isRemoteAnswering.set(true);
        requestDelayTimer.restartTimer();

        if (!sentRequestMap.containsKey(response.getMessageID())) {
            log.warn("{} sent unrequested response: {}", response.getSenderName(), response);
            return;
        }

        if (expiredRequestMap.containsKey(response.getMessageID())) {
            log.warn("{} sent expired response: {}", response.getSenderName(), response);
            return;
        }

        switch (response.getCategory()) {
            case GREETING -> {
                senderName = (response.getStatus() == Status.ACCEPTED)
                        ? sentRequestMap.get(response.getMessageID()).getSenderName()
                        : null;
                isSenderNameAccepted.set(senderName != null);
                setPlayerState(response);
                possibleOptions = (response.getPossibleOptions() != null)
                        ? response.getPossibleOptions().toArray(new String[0])
                        : new String[0];
            }

            case STAKE -> {
                previousRoundResult = response.getMessageText();
                if (response.getStatus() == Status.ACCEPTED) {
                    setPlayerState(response);
                } else {
                    log.info("{} rejected {}, message: {}",
                            response.getSenderName(),
                            response.getCategory(),
                            response.getMessageText());
                }
            }
            case GOODBYE -> {
                isRemoteAnswering.set(false);
                requestDelayTimer.stopAndResetTimer(0);
                possibleOptions = new String[0];
            }
            case SERVICE -> {
                setPlayerState(response);
                possibleOptions = (response.getPossibleOptions() != null)
                        ? response.getPossibleOptions().toArray(new String[0])
                        : new String[0];
            }

            default -> log.warn("{} sent unexpected response category: {}", response.getSenderName(), response);
        }

        internalStatistics.addSuccessfulRequestsByResponse(response);
        sentRequestMap.remove(response.getMessageID());
    }

    private void setPlayerState(Response response) {
        playerState.setTokenCount(isSenderNameAccepted.get()
                ? response.getTokens()
                : playerState.getTokenCount());
    }

    public String[] getPossibleOptions() {
        return possibleOptions;
    }

    public String getPreviousRoundResult() {
        return previousRoundResult;
    }

    public String getSenderName() {
        return senderName;
    }

    private class ClientStatistics {
        private long successfulRequestCount;
        private long totalTime;

        private void addSuccessfulRequestsByResponse(Response response) {
            this.successfulRequestCount++;
            totalTime += System.currentTimeMillis()
                    - response.getSendTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }

        public long getSuccessfulRequestCount() {
            return this.successfulRequestCount;
        }

        public long getExpiredRequests() {
            return expiredRequestMap.size();
        }

        public long getAverageRequestTime() {
            return totalTime / successfulRequestCount;
        }

        public String getUsername() {
            return senderName;
        }
    }
}
