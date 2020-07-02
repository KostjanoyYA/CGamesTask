package ru.kostyanoy.dataexchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class TestExchanger {
    private List<ClientExchanger> clients;
    private Map<LocalDateTime, ClientExchanger> timedClients;
    private String inetAddress;
    private int port;
    private boolean hasDone;
    private static final Logger log = LoggerFactory.getLogger(TestExchanger.class);

    public TestExchanger(ClientExchanger baseClient) {
        this.inetAddress = baseClient.getConnection().getInetAddress();
        this.port = baseClient.getConnection().getPort();
        this.clients = new CopyOnWriteArrayList<>();
        this.timedClients = new CopyOnWriteArrayList<>();
        hasDone = false;
    }

    public int startTest(int clientCount, int requestInterval, int requestCount) { //TODO переделать, чтобы возвращал int
        if (clientCount <= 0 || requestInterval <= 0 || requestCount <= 0) {
            throw new IllegalArgumentException("Arguments must be more than 0");
        }

        for (int i = 0; i < clientCount; i++) {
            clients.add(new ClientExchanger());
        }

        int number = 0;
        for (int i = clients.size(); i >= 0; i--) {
            if (!clients.get(i).getConnection().connect(inetAddress, port)) {
                log.warn("Cannot connect to {}:{}", inetAddress, port);
                clients.remove(i);
                continue;
            }
            clients.get(i).startExchange();
            timedClients.put(LocalDateTime.now(), clients.get(i));
            if (!clients.get(i).hasCheckedNickName("Client_" + number++)) {
                continue;
            }
        }

        if (clients.isEmpty()) {
            return -1;
        }


        Random rnd = new Random(clients.get(0).getPlayerState().getTokenCount()/requestCount);
        //TODO SendStake в течении заданного времени с заданным интервалом в отдельных потоках
        for (ClientExchanger client : clients) {
            Thread(() -> {
                while ()
                client.sendStake();
            });


            return !clients.isEmpty();
        }
    }

        public void stopTest() {
            if (clients != null && !clients.isEmpty()) {
                clients.forEach(client -> client.stopExchange());
            }
        }
    }

