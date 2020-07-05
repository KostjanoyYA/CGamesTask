package ru.kostyanoy.mode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kostyanoy.dataexchange.ClientExchanger;
import ru.kostyanoy.dataexchange.TestExchanger;
import ru.kostyanoy.ui.StatisticsGUIFormer;
import ru.kostyanoy.ui.TestGUIFormer;

import javax.swing.*;

public class StressTest implements GameMode {
    TestExchanger exchanger;
    TestGUIFormer gui;
    private static final int REFRESH_TIMEOUT = 1000;
    private static final Logger log = LoggerFactory.getLogger(StressTest.class);


    @Override
    public int playGame(ClientExchanger client) {
        if (client == null) {
            throw new IllegalArgumentException("ClientExchanger is null");
        }
        exchanger = new TestExchanger(client);
        gui = new TestGUIFormer(this);

        try {
            gui.createMainWindow();
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | InstantiationException | IllegalAccessException e) {
            log.warn(e.getMessage(), e);
            return -1;
        }
        return 0;
    }

    public void stopTest() {
        exchanger.stopExchange();
    }

    public void startTest(int clientCount, int requestInterval, int requestCount) {
        StatisticsGUIFormer tableGui = new StatisticsGUIFormer();
        Thread longTermOperation = new Thread(() ->
                exchanger.startExchange(clientCount, requestInterval, requestCount)
                        .ifPresentOrElse(tableGui::createTableFrame,
                                () -> tableGui.showMessage("Statistics is empty")));
        longTermOperation.start();
    }
}
