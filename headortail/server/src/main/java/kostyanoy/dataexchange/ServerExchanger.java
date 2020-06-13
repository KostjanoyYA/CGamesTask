package kostyanoy.dataexchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kostyanoy.game.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kostyanoy.connection.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerExchanger {
    private String senderName;
    private List<Client> unnamedClients;
    private ConcurrentHashMap<String, Client> clientsMap;
    private int serverPort;
    private ServerSocket serverSocket;
    ObjectMapper mapper;
    private static final Logger log = LoggerFactory.getLogger(ServerExchanger.class);

    private Thread messageListenerThread;
    private Thread socketListenerThread;

    static {
        Connection.customizeConnectionClass("connection.properties");
    }

    public ServerExchanger(String serverNickName, int serverPort) {
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        senderName = serverNickName;
        unnamedClients = new CopyOnWriteArrayList<>();
        clientsMap = new ConcurrentHashMap<>();
        this.serverPort = serverPort;
    }

    public boolean sendMessage(Client client, Message message) {
        try {
            client.getConnection().getWriter().println(mapper.writeValueAsString(message));
            client.getConnection().getWriter().flush();
        } catch (JsonProcessingException e) {
            log.warn(e.getMessage(), e);
            return false;
        }
        return true;
    }

    private boolean isNotNullAndEmpty(String string) {
        return (string != null && !string.isEmpty());
    }

    public boolean hasCheckedNickName(String nickName) {
        if (!isNotNullAndEmpty(nickName) || clientsMap.containsKey(nickName)) {
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

    public void startExchange() {
        socketListenerThread = new Thread(() -> {
            log.debug("socketListenerThread started");
            try {
                while (!Thread.interrupted()) {
                    unnamedClients.add(new Client(new Connection().connect(serverSocket.accept())));
                    log.info("{}: Added client socket {}", senderName, unnamedClients.get(unnamedClients.size() - 1).hashCode());
                    Thread.sleep(Connection.PING_TIMEOUT >> 2);
                }
            } catch (InterruptedException | IOException e) {
                log.warn(e.getMessage(), e);
            }
        });
        socketListenerThread.start();

        messageListenerThread = new Thread(() -> {
            log.debug("messageListenerThread started");
            while (!Thread.interrupted()) {
                for (int i = unnamedClients.size() - 1; i >= 0; i--) {
                    try {
                        if (unnamedClients.get(i) == null) {
                            unnamedClients.remove(i);
                        }

                        if (unnamedClients.get(i).getConnection().getReader().ready()) {
                            log.debug("{}: recieved message from the client {} (socket: {})", senderName,
                                    unnamedClients.get(i).getNickName(), unnamedClients.get(i).hashCode());
                            parseMessage(unnamedClients.get(i));
                            break;
                        }
                    } catch (IOException e) {
                        log.warn(e.getMessage(), e);
                    }
                }
                sleep(Connection.PING_TIMEOUT >> 1);
            }
        });
        messageListenerThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(this::stopExchange));

        try {
            serverSocket = new ServerSocket(serverPort);
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }

    }

    public void stopExchange() {
        socketListenerThread.interrupt();
        messageListenerThread.interrupt();

        for (Client client : unnamedClients) {
            client.getConnection().disconnect();
        }

        clientsMap.forEach((key, value) -> {
            value.getConnection().disconnect();
        });

        try {
            serverSocket.close();
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        } finally {
            socketListenerThread.interrupt();
        }
        log.info("{}: Exchange has stopped", senderName);
    }

    private void parseMessage(Client client) throws IOException {
        String jsonMessage;
        jsonMessage = client.getConnection().getReader().readLine();
        Message incomingMessage = mapper.readValue(jsonMessage, Message.class);

        log.info("{}: Have got {} from the client (socket: {})", senderName, incomingMessage.getClass().getSimpleName(),
                client.hashCode());

        if ((incomingMessage == null)) {
            return;
        }

        if (incomingMessage instanceof Response) {
            log.info("{} sent response. Responses from client are not supported", incomingMessage.getSenderName());
            return;
        }

        Request request;
        Response response;
        if (incomingMessage instanceof Request) {
            request = (Request) incomingMessage;
        } else {
            log.warn("{} sent unsupported type of message", incomingMessage.getSenderName());
            return;
        }

        switch (request.getType()) {
            case GREETING -> {
                if (!hasCheckedNickName(request.getSenderName())) {
                    response = new Response(senderName, request.getType(), Status.REJECTED, 0);
                    response.setMessage("Username is busy or empty");

                    log.info("Client (id: {}) asked for adding busy or illegal nickname '{}'",
                            client.hashCode(), request.getSenderName());
                } else {
                    client.setNickName(request.getSenderName());
                    client.setPlayer(Player.createDefaultPlayer());
                    response = new Response(senderName, request.getType(), Status.ACCEPTED, client.getPlayer().getAccount());
                    clientsMap.put(client.getNickName(), client);
                    unnamedClients.remove(client);
                    log.info("Client added '{}'", client);
                }
                sendMessage(client, response);
            }

            case STAKE -> {
                if (!clientsMap.containsKey(request.getSenderName())) {
                    log.info("Client named {} is not registered", client.getNickName());
                    response = new Response(senderName, request.getType(), Status.REJECTED, 0);
                    response.setMessage("Not registered client name '" + request.getSenderName() + "'");
                } else {
                    //TODO Здесь сделать игру и сохранение результатов в List
                    log.info();
                }
            }
            case GAMEPERMISSION -> {
                setPlayerState(response);
                if (!(isGameAllowed = response.getStatus() == Status.ACCEPTED)) {
                    log.info("{} rejected {}, message: {}",
                            response.getSenderName(),
                            response.getType(),
                            response.getMessage());
                }
            }
            case GOODBYE -> {
                isRemoteAnswering.set(false);
                requestDelayTimer.stopAndResetTimer(0);
            }
            case SERVICE -> setPlayerState(response);

            default -> log.warn("{} sent unexpected response type: {}", response.getSenderName(), response);
        }


        if (incomingMessage instanceof MessageGreeting) {
            Message nameMessage = new MessageGreeting(senderName);
            if (hasCheckedNickName(incomingMessage.getSenderName())) {
                client.setNickName(incomingMessage.getSenderName());
                nameMessage.setMessages(MessageGreeting.HELLO, client.getNickName());
                log.info("{}: Client {} (socket: {}) added to client list", senderName, client.getNickName(), client.hashCode());
                messages.add(String.format("%s: \n%s joined the chat", senderName, client.getNickName()));
                log.info("{}: Added message 'Client {} (socket: {}) joined the chat'", senderName, client.getNickName(), client.hashCode());
            } else {
                nameMessage.setMessages(MessageGreeting.BUSY_NAME);
                log.info("{}: Client (socket: {}) asked for adding busy nickname '{}'", senderName, client.hashCode(),
                        incomingMessage.getSenderName());
            }
            sendMessageDirectly(nameMessage, client);
            return;
        }
        if (incomingMessage instanceof MessageGoodbye) {
            if (client.getNickName() != null) {
                messages.add(String.format("%s: \n%s left the chat", senderName, client.getNickName()));
                log.info("{}: Added message {}", senderName, messages.get(messages.size() - 1));
            }
            client.getConnection().disconnect();
            unnamedClients.remove(client);
            log.info("{}: Closed the sockets and removed the client", senderName);
            return;
        }
        if (incomingMessage instanceof MessageSentByUser && client.getNickName() != null && !client.getNickName().isEmpty()) {
            String userMessage = client.getNickName() + " (" + incomingMessage.getSendTime() + "):\n"
                    + Arrays.toString(incomingMessage.getMessages());
            messages.add(userMessage);
            log.info("{}: Added massages {}", senderName, userMessage);
        }
    }
}
