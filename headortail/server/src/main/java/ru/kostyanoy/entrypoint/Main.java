package ru.kostyanoy.entrypoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kostyanoy.data.exchange.ServerExchanger;
import ru.kostyanoy.game.Game;
import ru.kostyanoy.game.HeadOrTail;
import ru.kostyanoy.history.History;

import java.util.Scanner;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(ServerExchanger.class);

    public static void main(String[] args) {

        Game game = new HeadOrTail();
        ServerExchanger server = new ServerExchanger("server.properties", game);
        server.startExchange();

        log.info("{}: Started", server.getSenderName());
        log.info("{}: The port {} is used", server.getSenderName(), server.getServerPort());

        try (Scanner reader = new Scanner(System.in)) {
            while (!reader.nextLine().equals("stop server")) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    log.warn(e.getMessage(), e);
                }
            }
        }
        server.stopExchange();
        log.info("{}: Stopped", server.getSenderName());
        printHistory(server.getHistory());
        System.exit(0);
    }

    private static void printHistory(History history) {
        if (history == null) {
            log.info("\n\n\n****Game history is empty*****\n");
            return;
        }
        log.info("\n\n\n****Game history*****\n{}", history.toString());
    }
}
