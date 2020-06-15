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
import java.util.concurrent.atomic.AtomicBoolean;

public class Connection {
    public static int PING_TIMEOUT;
    private static int PING_ATTEMPTS_LIMIT;

    private PrintWriter writer;
    private BufferedReader reader;
    private Socket socket;
    private Thread pinger;
    private int attempts;
    private AtomicBoolean isConnected;
    private static final Logger log = LoggerFactory.getLogger(Connection.class);

    static {
        PING_TIMEOUT = 5000;
        PING_ATTEMPTS_LIMIT = 12;
    }

    public static boolean customizeConnectionClass(String propertyFileName) {
        if (propertyFileName == null
                || propertyFileName.isEmpty()
                || !PropertyLoader.load(propertyFileName, Connection.class)) {
            log.warn("Cannot load property file '{}' \nDefault configuration has been used", propertyFileName);
            return false;
        }
        else {
            log.info("Property file '{}' has been loaded successfully", propertyFileName);
            PING_TIMEOUT = Integer.parseInt(PropertyLoader.getPropertiesMap().get("ping.timeout"));
            PING_ATTEMPTS_LIMIT = Integer.parseInt(PropertyLoader.getPropertiesMap().get("ping.attempts.limit"));
        }
        return true;
    }

    public Connection() {
        this.isConnected = new AtomicBoolean(false);
        attempts = 0;
    }

    public boolean connect(String ipAddress, int port) {
        try {
            socket = new Socket(Inet4Address.getByName(ipAddress), port);
            connect(socket);
        } catch (IOException | InterruptedException e) {
            log.warn(e.getMessage(), e);
            return false;
        }
        return true;
    }

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
            } finally {
                isConnected.set(false);
            }
        });
        log.info("Ping started");
        pinger.start();
    }

    public boolean isConnected() {
        return isConnected.get();
    }

    public void disconnect() {
        try {
            pinger.interrupt();
            socket.close();
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
        isConnected.set(false);
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public BufferedReader getReader() {
        return reader;
    }
}
