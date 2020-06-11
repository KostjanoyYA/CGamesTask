package ru.kostyanoy.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class Connection {
    private PrintWriter writer;
    private BufferedReader reader;
    private Socket socket;
    private Thread pinger;
    public static final int PING_TIMEOUT = 5000;
    private static final int PING_ATTEMPTS_LIMIT = 12;
    private int attempts;
    private AtomicBoolean isConnected;
    private static final Logger log = LoggerFactory.getLogger(Connection.class);

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
                    log.debug("Ping {} for port: {}. IsConnected: {}", attempts, socket.getPort(), isConnected);
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
