package kostyanoy.dataexchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kostyanoy.game.Game;
import kostyanoy.game.history.GameHistory;
import kostyanoy.game.history.History;
import kostyanoy.game.history.HistoryTaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kostyanoy.connection.*;
import ru.kostyanoy.propertyloader.PropertyLoader;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerExchanger implements HistoryTaker {
    private String senderName;
    private final List<Client> unnamedClients;
    private final ConcurrentHashMap<String, Client> clientsMap;
    private int serverPort;
    private ServerSocket serverSocket;
    ObjectMapper mapper;
    private final Game game;

    private History gameHistory;
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
        this.gameHistory = new GameHistory(serverNickName);
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
            log.info("Sent message {}", message.toString());
        } catch (JsonProcessingException e) {
            log.warn(e.getMessage(), e);
        }
    }

    private boolean isNullOrEmpty(String string) {
        return (string == null || string.isEmpty());
    }

    public boolean hasCheckedNickName(String nickName) {
        return !isNullOrEmpty(nickName) || !clientsMap.containsKey(nickName);
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

                    log.debug("unnamedClients: {})", unnamedClients);

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
                            log.debug("received message from the client '{}' ({})",
                                    unnamedClients.get(i).getNickName(), unnamedClients.get(i).hashCode());
                            parseMessage(unnamedClients.get(i));
                        }

                    } catch (IOException e) {
                        log.warn(e.getMessage(), e);
                    }
                }

                clientsMap.forEach((key, value) -> {
                    try {
                        if (value.getConnection().getReader().ready()) {
                            log.debug("received message from the client '{}'", value.getNickName());
                            parseMessage(value);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

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

        log.debug("Have got {} from the client (id {})", incomingMessage.getClass().getSimpleName(),
                client.hashCode());

        if (incomingMessage instanceof Response) {
            log.info("{} sent response. Responses from client are not supported", incomingMessage.getSenderName());
            return;
        }

        Request request;
        Response response = new Response();
        if (incomingMessage instanceof Request) {
            request = (Request) incomingMessage;
            log.info("{} sent request: {}", incomingMessage.getSenderName(), request);
            response.setMessageID(request.getMessageID());
            response.setSenderName(senderName);
            response.setCategory(request.getCategory());
            response.setPossibleOptions(game.getPossibleMoves(client.getPlayer()));
        } else {
            log.warn("{} sent unsupported category of message", incomingMessage.getSenderName());
            return;
        }

        switch (request.getCategory()) {
            case GREETING -> {
                if (!hasCheckedNickName(request.getSenderName())) {
                    response.setStatus(Status.REJECTED);
                    response.setTokens(game.getInputLimit());
                    response.setMessageText("Username is busy, null or empty");
                    log.info("Client (id: {}) asked for adding busy or illegal nickname '{}'",
                            client.hashCode(), request.getSenderName());
                } else {
                    client.setNickName(request.getSenderName());
                    client.setPlayer(game.createNewPlayer());

                    response.setStatus(Status.ACCEPTED);
                    response.setTokens(client.getPlayer().getAccount());

                    clientsMap.put(client.getNickName(), client);
                    unnamedClients.remove(client);
                    log.info("Client added '{}'", client);
                }
            }

            case STAKE -> {
                response.setStatus(Status.REJECTED);
                response.setTokens(client.getPlayer().getAccount());

                if (isNullOrEmpty(client.getNickName()) || !clientsMap.containsKey(request.getSenderName())) {
                    log.info("Client named {} is not registered", client.getNickName());
                    response.setMessageText("Not registered client name '" + request.getSenderName() + "'");
                    break;
                }

                if (!game.isAllowed(client.getPlayer())) {
                    response.setMessageText("Client '" + client.getNickName() + "' is not allowed to the game");
                    log.info("Client {} is not allowed to the game", client.getNickName());
                    break;
                }

                if (!game.isBetAccepted(client.getPlayer(), request.getTokens(), request.getMessageText())) {
                    response.setMessageText("Illegal bet "
                            + request.getTokens()
                            + " in the game. Player account "
                            + client.getPlayer().getAccount());
                    log.info("Illegal bet {} in the game. Player account {}",
                            request.getTokens(),
                            client.getPlayer().getAccount());
                    gameHistory.addEvent(client.getNickName(), ((HistoryTaker)game).getHistory());
                    break;
                }
                response.setStatus(Status.ACCEPTED);
                response.setTokens(game
                        .changePlayerStateByGame(request.getTokens(), client.getPlayer(), request.getMessageText())
                        .getAccount());
                response.setMessageText("round result: " + request.getMessageText()); //TODO доделать вывод результата на клиенте
                gameHistory.addEvent(client.getNickName(), ((HistoryTaker)game).getHistory());
            }
            case GOODBYE -> {
                response.setStatus(Status.ACCEPTED);
                response.setTokens(game.getInputLimit());

                log.info("Client {} left the server", client.getNickName());
                if (clientsMap.contains(client)) {
                    clientsMap.remove(request.getSenderName());
                }
                sendMessage(client, response);
                client.getConnection().disconnect();
            }
            case SERVICE -> {
                long tokens = client.getPlayer() == null ? game.getInputLimit() : client.getPlayer().getAccount();
                response.setStatus(Status.ACCEPTED);
                response.setTokens(tokens);
            }
            default -> {
                response.setCategory(MessageCategory.SERVICE);
                response.setMessageText("Illegal request category '" + request.getCategory() + "' (message id =" + request.getMessageID() + ")");
                log.warn("{} sent unexpected request category: {}", request.getSenderName(), request);
            }
        }
        sendMessage(client, response);
    }

    @Override
    public History getHistory() {
        return gameHistory;
    }
}
