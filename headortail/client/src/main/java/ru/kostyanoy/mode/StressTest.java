package ru.kostyanoy.mode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kostyanoy.data.exchange.ClientExchanger;
import ru.kostyanoy.data.exchange.TestExchanger;
import ru.kostyanoy.ui.StatisticsGUIFormer;
import ru.kostyanoy.ui.TestGUIFormer;

import javax.swing.*;

/**
 * Performs stress test of the game
 */
public class StressTest implements GameMode {
    private TestExchanger exchanger;
    private static final Logger log = LoggerFactory.getLogger(StressTest.class);


    @Override
    public void playGame(ClientExchanger client) {
        if (client == null) {
            throw new IllegalArgumentException("ClientExchanger is null");
        }
        exchanger = new TestExchanger(client);
        TestGUIFormer gui = new TestGUIFormer(this);

        try {
            gui.createMainWindow();
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | InstantiationException | IllegalAccessException e) {
            log.warn(e.getMessage(), e);
        }
    }

    /**
     * Interrupts stress test and all of the responding data exchanges.
     */
    public void stopTest() {
        exchanger.stopExchange();
    }

    /**
     * Begins stress test and all of the responding data exchanges, creates gui element with result of the data exchange
     * @param clientCount required number of clients to create defined by UI
     * @param requestInterval time interval between requests from a client defined by UI
     * @param requestCount count of requests from a client defined by UI
     */
    public void startTest(int clientCount, int requestInterval, int requestCount) {
        StatisticsGUIFormer tableGui = new StatisticsGUIFormer();
        Thread longTermOperation = new Thread(() ->
                exchanger.startExchange(clientCount, requestInterval, requestCount)
                        .ifPresentOrElse(tableGui::createTableFrame,
                                () -> tableGui.showMessage("Statistics is empty")));
        longTermOperation.start();
    }
}
