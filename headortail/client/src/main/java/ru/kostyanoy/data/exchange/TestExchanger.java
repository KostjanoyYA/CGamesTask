package ru.kostyanoy.data.exchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kostyanoy.connection.Connection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class TestExchanger {
    private final List<ClientExchanger> clients;
    private final Map<LocalDateTime, ClientExchanger> timedClients;
    private final String hostName;
    private final int port;
    private final List<ClientExchanger.ClientStatistics> statistics;
    private static final int WAIT_FOR_ANSWER_TIMEOUT_MILLS = Connection.PING_TIMEOUT;
    private static final Logger log = LoggerFactory.getLogger(TestExchanger.class);

    public TestExchanger(ClientExchanger baseClient) {
        this.hostName = baseClient.getConnection().getHostName();
        this.port = baseClient.getConnection().getPort();
        this.clients = new CopyOnWriteArrayList<>();
        this.timedClients = new ConcurrentHashMap<>();
        this.statistics = new CopyOnWriteArrayList<>();
        baseClient.stopExchange();
    }

    public Optional<List<ClientExchanger.ClientStatistics>> startExchange(int clientCount, int requestInterval, int requestCount) {
        if (clientCount <= 0 || requestInterval <= 0 || requestCount <= 0) {
            throw new IllegalArgumentException("Arguments must be more than 0");
        }

        for (int i = 0; i < clientCount; i++) {
            clients.add(new ClientExchanger());
        }

        int number = 0;
        for (int i = clients.size() - 1; i >= 0; i--) {
            if (!clients.get(i).getConnection().connect(hostName, port)) {
                log.warn("Cannot connect to {}:{}", hostName, port);
                clients.remove(i);
                continue;
            }
            clients.get(i).startExchange();
            sleep(WAIT_FOR_ANSWER_TIMEOUT_MILLS);
            timedClients.put(LocalDateTime.now(), clients.get(i));
            while (!clients.get(i).hasCheckedNickName("Client_" + number++)) {
                log.warn("Cannot use a name 'Client_{}' to connect", number - 1);
            }
        }

        if (clients.isEmpty()) {
            return Optional.empty();
        }

        timedClients.forEach((key, value) -> {
            Thread sender = new Thread(() -> {
                for (int i = 0; i < requestCount; i++) {
                    value.sendStake(getRandomBet(value.getPlayerState().getTokenCount()), getRandomChoice(value));
                    sleep(requestInterval);
                }
                sleep(WAIT_FOR_ANSWER_TIMEOUT_MILLS*2);
                value.stopExchange();

                value.getStatistics().ifPresent(statistics::add);
            });
            sender.setName(value.getSenderName());
            sender.start();
            try {
                sender.join();
            } catch (InterruptedException e) {
                log.warn(e.getMessage(), e);
            }
        });
        return Optional.of(statistics);
    }

    private int getRandomBet(long max) {
        return (int) (Math.random() * (Math.abs(max+2)));
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

    public void stopExchange() {
        if (clients != null && !clients.isEmpty()) {
            clients.forEach(ClientExchanger::stopExchange);
        }
    }

    private void sleep(int timeout) {
        try {
            Thread.sleep(Math.abs(timeout));
        } catch (InterruptedException e) {
            log.warn(e.getMessage(), e);
        }
    }
}

