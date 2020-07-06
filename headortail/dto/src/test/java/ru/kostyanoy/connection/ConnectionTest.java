package ru.kostyanoy.connection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;

class ConnectionTest {


    void initializeListenerSocket(int port) throws IOException {
        ServerSocket serverSocket;
        serverSocket = new ServerSocket(port);

        Thread socketListenerThread = new Thread(() -> {
            try {
                serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        socketListenerThread.start();
    }

    @Test
    void connect_whenGetAchievable_thenReturnTrue() throws IOException {
        String host = "localhost";
        int port = 1234;
        initializeListenerSocket(port);

        Connection connection = new Connection();
        Assertions.assertTrue(connection.connect(host, port));
    }

    @Test
    void connect_whenGetUnachievable_thenReturnFalse() {
        String host = "Unachievable@#&^<*>-"; //rfc952, rfc1123
        int port = 1234;
        Connection connection = new Connection();
        Assertions.assertFalse(connection.connect(host, port));
    }
}