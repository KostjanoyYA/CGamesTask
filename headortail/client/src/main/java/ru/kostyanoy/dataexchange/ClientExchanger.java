package ru.kostyanoy.dataexchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kostyanoy.connection.*;
import timer.TimeMeter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientExchanger implements Exchanger {
    private Connection connection;
    private String nickName;
    private long tokenCount;
    private String[] participants;
    TimeMeter timer;
    private static final Logger log = LoggerFactory.getLogger(ClientExchanger.class);

    public ClientExchanger() {
        this.connection = new Connection();
        tokenCount = new ArrayList<>();
        participants = new String[0];
        nickName = "";
        timer = new TimeMeter(Connection.PING_TIMEOUT*2);
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public String[] getParticipants() {
        return participants;
    }

    @Override
    public String[] getIncomingMessages() {
        return tokenCount.toArray(new String[0]);
    }

    @Override
    public boolean sendMessage(Message message) {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        try {
            connection.getWriter().println(mapper.writeValueAsString(message));
            connection.getWriter().flush();
        } catch (JsonProcessingException e) {
            log.warn(e.getMessage(), e);
            return false;
        }
        return true;
    }

    public boolean sendMessage(String messageText) {
        return sendMessage(new MessageSentByUser(nickName, messageText));
    }

    @Override
    public void resetMessages() {
        tokenCount.clear();
    }

    @Override
    public boolean hasCheckedNickName(String nickName) {
        sendMessage(new MessageGreeting(nickName));
        try {
            parseMessage();
        } catch (IOException | InterruptedException e) {
            log.warn(e.getMessage(), e);
            return false;
        }
        if (this.nickName.isEmpty()) {
            return false;
        }
        startExchange();
        return true;
    }

    @Override
    public void startExchange() {
        Thread messageListenerThread = new Thread(() -> {
            while (!Thread.interrupted() && isServerReachable) {
                try {
                    parseMessage();

                } catch (IOException | InterruptedException e) {
                    log.warn(e.getMessage(), e);
                }
            }
            connection.disconnect();
        });
        messageListenerThread.start();
        sendMessage(new MessageService(nickName));

        Thread connectionCheckerThread = new Thread(() -> { //TODO Сделать поток отправки сервисных сообщений
            while (!Thread.interrupted() && isServerReachable) {
                try {
                    parseMessage();

                } catch (IOException | InterruptedException e) {
                    log.warn(e.getMessage(), e);
                }
            }
            connection.disconnect();
        });


    }

    public void stopExchange() {
        sendMessage(new MessageGoodbye(nickName));

        try {
            Thread.sleep(Connection.PING_TIMEOUT * 2);
        } catch (InterruptedException e) {
            log.warn(e.getMessage(), e);
        }
        connection.disconnect();
    }

    @Override
    public boolean checkConnection() { //TODO Логика проверки соединения, для сервера тоже
        if (!connection.isConnected()) { return false; }

        if (!timer.hasTimesUp()) {
            return true;
        }
        sendMessage(new MessageService(nickName));
        timer.restartTimer();
        return false;
    }

    private void parseMessage() throws IOException, InterruptedException {
        while (!connection.getReader().ready()) {
            Thread.sleep(Connection.PING_TIMEOUT);
        }


        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String jsonMessage = connection.getReader().readLine();
        Message incomingMessage = mapper.readValue(jsonMessage, Message.class);
        if ((incomingMessage == null) || (incomingMessage.getMessages() == null)) {
            return;
        }

        log.info("{}: Have got {}", nickName, incomingMessage.getClass().getSimpleName());

        if (incomingMessage instanceof MessageGreeting) {
            if (incomingMessage.getMessages()[0].equals(MessageGreeting.HELLO)) {
                nickName = incomingMessage.getMessages()[1];
            } else {
                nickName = "";
            }
            log.info("{}: {} said {}", nickName, incomingMessage.getSenderName(), incomingMessage.getMessages()[0]);
            return;
        }
        if (incomingMessage instanceof MessageGoodbye) {
            stopExchange();
            return;
        }
        if (incomingMessage instanceof MessageParticipants) {
            participants = incomingMessage.getMessages().clone();
            log.info("{}: participants are updated:\n{}", nickName, Arrays.toString(participants));
            return;
        }
        if (incomingMessage instanceof MessageSentByUser) {
            tokenCount.addAll(Arrays.asList(incomingMessage.getMessages()));
            log.info("{}: messages are updated:\n{}", nickName, tokenCount.toString());
        }
        if (incomingMessage instanceof MessageService) {
            log.info("{}: Have got service message:\n{}", nickName, incomingMessage.getMessages());
            checkConnection();
        }
    }
}
