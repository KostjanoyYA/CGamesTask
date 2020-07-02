package ru.kostyanoy.dataexchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class TestExchanger {
    private final List<ClientExchanger> clients;
    private final Map<LocalDateTime, ClientExchanger> timedClients;
    private final String inetAddress;
    private final int port;
    private static final Logger log = LoggerFactory.getLogger(TestExchanger.class);

    public TestExchanger(ClientExchanger baseClient) {
        this.inetAddress = baseClient.getConnection().getInetAddress();
        this.port = baseClient.getConnection().getPort();
        this.clients = new CopyOnWriteArrayList<>();
        this.timedClients = new ConcurrentHashMap<>();
    }

    public int startTest(int clientCount, int requestInterval, int requestCount) {
        if (clientCount <= 0 || requestInterval <= 0 || requestCount <= 0) {
            throw new IllegalArgumentException("Arguments must be more than 0");
        }

        for (int i = 0; i < clientCount; i++) {
            clients.add(new ClientExchanger());
        }

        int number = 0;
        for (int i = clients.size() - 1; i >= 0; i--) {
            if (!clients.get(i).getConnection().connect(inetAddress, port)) {
                log.warn("Cannot connect to {}:{}", inetAddress, port);
                clients.remove(i);
                continue;
            }
            clients.get(i).startExchange();
            timedClients.put(LocalDateTime.now(), clients.get(i));
            if (!clients.get(i).hasCheckedNickName("Client_" + number++)) {
                log.warn("Cannot use a name 'Client_{}' to connect", number-1);
            }
        }

        if (clients.isEmpty()) {
            return -1;
        }

        timedClients.forEach((key, value) -> {
            Thread sender = new Thread(() -> {
                for (int i = 0; i < requestCount; i++) {
                    value.sendStake(getRandomBet(value), getRandomChoice(value));
                    try {
                        Thread.sleep(requestInterval);
                    } catch (InterruptedException e) {
                        log.warn(e.getMessage(), e);
                    }
                }
                value.stopExchange();
            });
            sender.setName(value.getSenderName());
            sender.start();
        });
        return 1;
    }


    private int getRandomBet(ClientExchanger client) {
        Random rndBet = new Random(client.getPlayerState().getTokenCount() >> 1);
        for (int i = 0; i < System.nanoTime() % 10; i++) {
            rndBet.nextInt();
        }
        return Math.abs(rndBet.nextInt());
    }

    private String getRandomChoice(ClientExchanger client) {
        if (client.getPossibleOptions() == null
                || client.getPossibleOptions().length == 0
                || client.getPossibleOptions()[0].isEmpty()) {
            return "";
        }

        int max = client.getPossibleOptions().length;
        if (max == 1) {
            return client.getPossibleOptions()[0];
        }

        return client.getPossibleOptions()[(int) (Math.random() * max)];
    }

    public void stopTest() {
        if (clients != null && !clients.isEmpty()) {
            clients.forEach(ClientExchanger::stopExchange);
        }
    }
}

