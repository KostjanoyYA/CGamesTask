package kostyanoy.dataexchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kostyanoy.game.Game;
import kostyanoy.game.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kostyanoy.connection.*;
import ru.kostyanoy.propertyloader.PropertyLoader;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerExchanger {
    private String senderName;
    private final List<Client> unnamedClients;
    private final ConcurrentHashMap<String, Client> clientsMap;
    private int serverPort;
    private ServerSocket serverSocket;
    ObjectMapper mapper;
    private final Game game;
    private static final Logger log = LoggerFactory.getLogger(ServerExchanger.class);

    private Thread messageListenerThread;
    private Thread socketListenerThread;

    static {
        Connection.customizeConnectionClass("connection.properties");
    }

    public ServerExchanger(String propertyFileName, Game game) {
        this("1", 0, game);
        if (isNullOrEmpty(propertyFileName) || !PropertyLoader.load(propertyFileName, ServerExchanger.class)) {
            log.warn("Cannot load property file '{}'", propertyFileName);
            throw new IllegalArgumentException("Cannot load property file");
        } else {
            log.info("Property file '{}' has been loaded successfully", propertyFileName);
            senderName = PropertyLoader.getPropertiesMap().get("server.nickname");
            serverPort = Integer.parseInt(PropertyLoader.getPropertiesMap().get("server.port"));
        }
    }

    public ServerExchanger(String serverNickName, int serverPort, Game game) {
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        unnamedClients = new CopyOnWriteArrayList<>();
        clientsMap = new ConcurrentHashMap<>();
        this.serverPort = serverPort;

        if (isNullOrEmpty(serverNickName)) {
            log.warn("Server nickname is null or empty");
            throw new IllegalArgumentException("Server nickname is null or empty");
        }
        senderName = serverNickName;

        if (game == null) {
            log.warn("Game is null");
            throw new IllegalArgumentException("Game is null");
        }
        this.game = game;
    }

    public String getSenderName() {
        return senderName;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void sendMessage(Client client, Message message) {
        try {
            client.getConnection().getWriter().println(mapper.writeValueAsString(message));
            client.getConnection().getWriter().flush();
        } catch (JsonProcessingException e) {
            log.warn(e.getMessage(), e);
        }
    }

    private boolean isNullOrEmpty(String string) {
        return (string == null || string.isEmpty());
    }

    public boolean hasCheckedNickName(String nickName) {
        return !(isNullOrEmpty(nickName) || clientsMap.containsKey(nickName));
    }

    private void sleep(int timeout) {
        try {
            Thread.sleep(Math.abs(timeout));
        } catch (InterruptedException e) {
            log.warn(e.getMessage(), e);
        }
    }

    public void startExchange() {
        try {
            serverSocket = new ServerSocket(serverPort);
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }

        socketListenerThread = new Thread(() -> {
            log.debug("socketListenerThread started");
            try {
                while (!Thread.interrupted()) {
                    unnamedClients.add(new Client(new Connection().connect(serverSocket.accept())));
                    log.info("Added new client (temp id {})", unnamedClients.get(unnamedClients.size() - 1).hashCode());
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
    }

    public void stopExchange() {
        socketListenerThread.interrupt();
        messageListenerThread.interrupt();

        for (Client client : unnamedClients) {
            client.getConnection().disconnect();
        }

        clientsMap.forEach((key, value) -> value.getConnection().disconnect());

        try {
            serverSocket.close();
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        } finally {
            log.info("{}: Exchange has stopped", senderName);
        }
    }

    private void parseMessage(Client client) throws IOException {
        String jsonMessage;
        jsonMessage = client.getConnection().getReader().readLine();
        Message incomingMessage = mapper.readValue(jsonMessage, Message.class);

        log.info("{}: Have got {} from the client (socket: {})", senderName, incomingMessage.getClass().getSimpleName(),
                client.hashCode());

        if (incomingMessage instanceof Response) {
            log.info("{} sent response. Responses from client are not supported", incomingMessage.getSenderName());
            return;
        }

        Request request;
        Response response = null;
        if (incomingMessage instanceof Request) {
            request = (Request) incomingMessage;
        } else {
            log.warn("{} sent unsupported type of message", incomingMessage.getSenderName());
            return;
        }

        switch (request.getType()) {
            case GREETING -> {
                if (!hasCheckedNickName(request.getSenderName())) {
                    response = new Response(senderName, request.getType(), Status.REJECTED, game.getInputLimit());
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
                if (sendMessageIfNotRegisteredName(client, request)) {
                    return;
                }
                if (sendMessageIfNotAllowedPlayer(client, request)) {
                    return;
                }

                if (!game.isBetAccepted(client.getPlayer(), request.getTokens())) {
                    response = new Response(senderName, request.getType(), Status.REJECTED, client.getPlayer().getAccount());
                    response.setMessage("Illegal bet "
                            + request.getTokens()
                            + " in the game. Player account "
                            + client.getPlayer().getAccount());
                    log.info("Illegal bet {} in the game. Player account {}",
                            request.getTokens(),
                            client.getPlayer().getAccount());
                }
                response = new Response(senderName, request.getType(),
                        Status.ACCEPTED,
                        game.changePlayerStateByGame(request.getTokens(),
                                client.getPlayer()).getAccount());
            }
            case GAMEPERMISSION -> {
                if (sendMessageIfNotRegisteredName(client, request)) {
                    return;
                }
                if (game.isAllowed(client.getPlayer())) {
                    response = new Response(senderName, request.getType(), Status.ACCEPTED, client.getPlayer().getAccount());
                    log.info("Player {} was allowed to the game", client.getNickName());
                }
            }
            case GOODBYE -> {
                if (sendMessageIfNotRegisteredName(client, request)) {
                    return;
                }
                response = new Response(senderName, request.getType(), Status.ACCEPTED, client.getPlayer().getAccount());
                clientsMap.remove(request.getSenderName());
                log.info("Client {} left the server", client.getNickName());
            }
            case SERVICE -> {
                long tokens = client.getPlayer() == null ? game.getInputLimit() : client.getPlayer().getAccount();
                response = new Response(senderName, request.getType(), Status.ACCEPTED, tokens);
            }

            default -> {
                response = new Response(senderName, MessageType.SERVICE, Status.REJECTED, game.getInputLimit());
                response.setMessage("Illegal request type '" + request.getType() + "' (message id =" + request.getId() + ")");
                log.warn("{} sent unexpected request type: {}", request.getSenderName(), request);
            }
        }

        sendMessage(client, response);
    }

    private boolean sendMessageIfNotRegisteredName(Client client, Request request) { // returns true if message was sent
        boolean isNotRegisteredName = !clientsMap.containsKey(request.getSenderName());
        if (isNotRegisteredName) {
            log.info("Client named {} is not registered", client.getNickName());
            Response response = new Response(senderName, request.getType(), Status.REJECTED, game.getInputLimit());
            response.setMessage("Not registered client name '" + request.getSenderName() + "'");
            sendMessage(client, response);
        }
        return isNotRegisteredName;
    }

    private boolean sendMessageIfNotAllowedPlayer(Client client, Request request) { // returns true if message was sent
        boolean isNotAllowedPlayer = !game.isAllowed(client.getPlayer());
        if (isNotAllowedPlayer) {
            Response response = new Response(senderName, request.getType(), Status.REJECTED, client.getPlayer().getAccount());
            response.setMessage("Client '" + client.getNickName() + "' is not allowed to the game");
            log.info("Client {} is not allowed to the game", client.getNickName());
            sendMessage(client, response);
        }
        return isNotAllowedPlayer;
    }
}
