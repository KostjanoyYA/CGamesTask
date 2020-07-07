package ru.kostyanoy.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kostyanoy.propertyloader.PropertyLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Socket;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Works with sockets to establish connection and periodically check it.
 */
public class Connection {
    public static int PING_TIMEOUT;
    private static int PING_ATTEMPTS_LIMIT;

    private PrintWriter writer;
    private BufferedReader reader;
    private Socket socket;
    private Thread pinger;
    private int attempts;
    private final AtomicBoolean isConnected;
    private static final Logger log = LoggerFactory.getLogger(Connection.class);

    static {
        PING_TIMEOUT = 1000;
        PING_ATTEMPTS_LIMIT = 12;
    }

    /**
     * Gets the host name of the socket connection 
     * @return String of the host name
     */
    public String getHostName() {
        return socket.getInetAddress().getHostName();
    }

    /**
     * Gets the port of the socket connection 
     * @return port number int
     */
    public int getPort() {
        return socket.getPort();
    }

    /**
     * Loads static properties from the specified property file 
     * @param propertyFileName string file name included ".property".
     */
    public static void customizeConnectionClass(String propertyFileName) {
        if (propertyFileName == null
                || propertyFileName.isEmpty()) {
            log.warn("Cannot load property file '{}' \nDefault configuration has been used", propertyFileName);
            return;
        }
        Optional<Map<String, String>> optionalMap = PropertyLoader.load(propertyFileName, Connection.class);
        if (optionalMap.isEmpty()) {
            log.warn("Cannot load property file '{}' \nDefault configuration has been used", propertyFileName);
            return;
        }
        Map<String, String> propertyMap = optionalMap.get();
        log.info("Property file '{}' has been loaded successfully", propertyFileName);
        PING_TIMEOUT = Integer.parseInt(propertyMap.get("ping.timeout"));
        PING_ATTEMPTS_LIMIT = Integer.parseInt(propertyMap.get("ping.attempts.limit"));
    }

    public Connection() {
        this.isConnected = new AtomicBoolean(false);
        attempts = 0;
    }

    /**
     * Establish socket connection 
     * @param hostName the specified host name
     * @param port the port number
     * @return Optional. Optional is empty for unsuccessful property file loading. Otherwise Optional contains a Map of properties.
     */
    public boolean connect(String hostName, int port) {
        try {
            socket = new Socket(Inet4Address.getByName(hostName), port);
            connect(socket);
        } catch (IOException | InterruptedException e) {
            log.warn(e.getMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * Establish socket connection by socket received from the ServerSocket.
     * Starts ping thread to update {@link Connection#isConnected()}
     * @param socket socket received from the ServerSocket
     * @return Connection that is ready for data exchange.
     */
    public Connection connect(Socket socket) throws IOException, InterruptedException {
        this.socket = socket;
        setSocketIO();
        startPing();
        Thread.sleep(PING_TIMEOUT);
        return this;
    }

    private void setSocketIO() throws IOException {
        writer = new PrintWriter(socket.getOutputStream());
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    private void startPing() {
        pinger = new Thread(() -> {
            try {
                while (!Thread.interrupted() && attempts <= PING_ATTEMPTS_LIMIT) {
                    isConnected.set(socket.getInetAddress().isReachable(PING_TIMEOUT));
                    attempts = isConnected.get() ? 0 : attempts++;
                    Thread.sleep(PING_TIMEOUT);
                }
            } catch (IOException | InterruptedException e) {
                log.warn(e.getMessage(), e);
                isConnected.set(false);
                pinger.interrupt();
            } finally {
                isConnected.set(false);
            }
        });
        pinger.setName("Pinger (" + pinger.getId() + ")");
        pinger.start();
        log.info("Ping started");
    }

    /**
     * Method for getting the connection status
     * @return Connection status: <b>true</b> if the socket's host is reachable, otherwise, <b>false</b>
     */
    public boolean isConnected() {
        return isConnected.get();
    }

    /**
     * Interrupts pinger thread and close the socket.
     */
    public void disconnect() {
        try {
            pinger.interrupt();
            socket.close();
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
        isConnected.set(false);
    }

    /**
     * Method for getting the input network stream
     * @return network PrintWriter
     */
    public PrintWriter getWriter() {
        return writer;
    }

    /**
     * Method for getting the output network stream
     * @return network BufferedReader
     */
    public BufferedReader getReader() {
        return reader;
    }
}
