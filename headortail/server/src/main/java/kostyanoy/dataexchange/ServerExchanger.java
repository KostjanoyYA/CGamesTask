package kostyanoy.dataexchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kostyanoy.connection.Connection;
import ru.kostyanoy.connection.Exchanger;
import ru.kostyanoy.connection.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerExchanger implements Exchanger {
    private String nickName;
    private CopyOnWriteArrayList<String> messages;
    private List<ChatClient> clients;
    private int serverPort;
    private ServerSocket serverSocket;
    ObjectMapper mapper;
    private static final int TIMEOUT = Connection.PING_TIMEOUT >> 1;
    private static final Logger log = LoggerFactory.getLogger(ServerExchanger.class);

    private Thread messageListenerThread;
    private Thread socketListenerThread;

    public ServerExchanger(String serverNickName, int serverPort) {
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        messages = new CopyOnWriteArrayList<>();
        nickName = serverNickName;
        clients = new CopyOnWriteArrayList<>();
        this.serverPort = serverPort;
    }

    @Override
    public String[] getParticipants() {
        ArrayList<String> participants = new ArrayList<>();
        participants.add(nickName);
        checkConnections();
        for (ChatClient client : clients) {
            if (client.getNickName() == null || client.getNickName().isEmpty()) {
                continue;
            }
            participants.add(client.getNickName());
        }
        return participants.toArray(new String[0]);
    }

    private void checkConnections() {
        log.debug("{}: clients: {}", nickName, clients);
        for (int i = clients.size() - 1; i >= 0; i--) {
            log.debug("{}: checkConnections: in the for loop. i={}", nickName, i);
            if (clients.get(i) == null) {
                log.debug("{}: remove client {}", nickName, clients.get(i));
                clients.remove(i);
                continue;
            }
            log.debug("{}: checkConnections: for loop. IsConnected: {}", nickName, clients.get(i).getConnection().isConnected());
            if (!clients.get(i).getConnection().isConnected()) {
                log.debug("{}: checkConnections: in the if statement. IsConnected: {}", nickName, clients.get(i).getConnection().isConnected());
                messages.add(String.format("%s: \n%s left the chat", nickName, clients.get(i).getNickName()));
                clients.remove(i);
            }
        }
    }

    @Override
    public String[] getIncomingMessages() {
        return (String[]) messages.toArray();
    }

    @Override
    public boolean sendMessage(Message message) {
        checkConnections();
        for (ChatClient client : clients) {
            if (isNotNullAndEmpty(client.getNickName())) {
                try {
                    sendMessageDirectly(message, client);
                } catch (JsonProcessingException e) {
                    log.warn(e.getMessage(), e);
                }
            }
        }
        return true;
    }

    private boolean isNotNullAndEmpty(String string) {
        return (string != null && !string.isEmpty());
    }

    private void sendMessageDirectly(Message message, ChatClient client) throws JsonProcessingException {
        client.getConnection().getWriter().println(mapper.writeValueAsString(message));
        client.getConnection().getWriter().flush();
    }

    public boolean sendMessage(String messageText) {
        return sendMessage(new MessageSentByUser(nickName, messageText));
    }

    @Override
    public void clearUnansweredMessages() {
        messages.clear();
    }

    @Override
    public boolean hasCheckedNickName(String nickName) {
        if (!isNotNullAndEmpty(nickName)) {
            return false;
        }

        for (ChatClient client : clients) {
            if (client.getNickName() == null) {
                continue;
            }

            if ((client.getNickName().equals(nickName))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void startExchange() {
        messageListenerThread = new Thread(() -> {
            log.debug("messageListenerThread started");
            while (!Thread.interrupted()) {
                for (int i = clients.size() - 1; i >= 0; i--) {
                    try {
                        if (clients.get(i) == null) {
                            clients.remove(i);
                        }

                        if (clients.get(i).getConnection().getReader().ready()) {
                            log.debug("{}: recieved message from the client {} (socket: {})", nickName,
                                    clients.get(i).getNickName(), clients.get(i).hashCode());
                            parseMessage(clients.get(i));
                            break;
                        }
                    } catch (IOException e) {
                        log.warn(e.getMessage(), e);
                    }
                }

                try {
                    Thread.sleep(TIMEOUT);
                } catch (InterruptedException e) {
                    log.warn(e.getMessage(), e);
                }
            }
        });
        messageListenerThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(this::stopExchange));

        try {
            serverSocket = new ServerSocket(serverPort);
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
        log.debug("serverSocket created. serverSocket.isClosed() = {}", serverSocket.isClosed());

        socketListenerThread = new Thread(() -> {
            log.debug("socketListenerThread started");
            try {
                while (!Thread.interrupted()) {
                    clients.add(new ChatClient(new Connection().connect(serverSocket.accept())));
                    log.info("{}: Added client socket {}", nickName, clients.get(clients.size() - 1).hashCode());
                    Thread.sleep(TIMEOUT >> 2);
                }
            } catch (InterruptedException | IOException e) {
                log.warn(e.getMessage(), e);
            }
        });
        socketListenerThread.start();

        while (!serverSocket.isClosed()) {

            Message participants = new MessageParticipants(nickName);
            participants.setMessages(getParticipants());
            log.debug("{}: participants.setMessages({})", nickName, getParticipants());
            sendMessage(participants);
            log.info("{}: Sent participants: {}", nickName, Arrays.toString(getParticipants()));

            Message outgoing = new MessageSentByUser(nickName, messages.toArray(new String[0]));
            sendMessage(outgoing);
            log.info("{}: Sent messages {}", nickName, messages.toString());
            clearUnansweredMessages();

            try {
                Thread.sleep(TIMEOUT >> 2);
            } catch (InterruptedException e) {
                log.warn(e.getMessage(), e);
            }
        }
    }

    @Override
    public void stopExchange() {
        for (ChatClient client : clients) {
            client.getConnection().disconnect();
        }
        sendMessage(new MessageGoodbye(nickName));
        messageListenerThread.interrupt();
        try {
            serverSocket.close();
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
        finally {
            socketListenerThread.interrupt();
        }
        log.info("{}: Exchange has stopped", nickName);
    }

    private void parseMessage(ChatClient client) throws IOException {
        String jsonMessage;
        jsonMessage = client.getConnection().getReader().readLine();
        Message incomingMessage = mapper.readValue(jsonMessage, Message.class);
        log.info("{}: Have got {} from the client (socket: {})", nickName, incomingMessage.getClass().getSimpleName(),
                client.hashCode());
        if (incomingMessage instanceof MessageGreeting) {
            Message nameMessage = new MessageGreeting(nickName);
            if (hasCheckedNickName(incomingMessage.getSenderName())) {
                client.setNickName(incomingMessage.getSenderName());
                nameMessage.setMessages(MessageGreeting.HELLO, client.getNickName());
                log.info("{}: Client {} (socket: {}) added to client list", nickName, client.getNickName(), client.hashCode());
                messages.add(String.format("%s: \n%s joined the chat", nickName, client.getNickName()));
                log.info("{}: Added message 'Client {} (socket: {}) joined the chat'", nickName, client.getNickName(), client.hashCode());
            } else {
                nameMessage.setMessages(MessageGreeting.BUSY_NAME);
                log.info("{}: Client (socket: {}) asked for adding busy nickname '{}'", nickName, client.hashCode(),
                        incomingMessage.getSenderName());
            }
            sendMessageDirectly(nameMessage, client);
            return;
        }
        if (incomingMessage instanceof MessageGoodbye) {
            if (client.getNickName() != null) {
                messages.add(String.format("%s: \n%s left the chat", nickName, client.getNickName()));
                log.info("{}: Added message {}", nickName, messages.get(messages.size() - 1));
            }
            client.getConnection().disconnect();
            clients.remove(client);
            log.info("{}: Closed the sockets and removed the client", nickName);
            return;
        }
        if (incomingMessage instanceof MessageSentByUser && client.getNickName() != null && !client.getNickName().isEmpty()) {
            String userMessage = client.getNickName() + " (" + incomingMessage.getSendTime() + "):\n"
                    + Arrays.toString(incomingMessage.getMessages());
            messages.add(userMessage);
            log.info("{}: Added massages {}", nickName, userMessage);
        }
    }
}
