package ru.kostyanoy.mode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kostyanoy.dataexchange.ClientExchanger;
import ru.kostyanoy.dataexchange.TestExchanger;
import ru.kostyanoy.ui.TestGUIFormer;

import javax.swing.*;

public class StressTest implements GameMode {
    private static final Logger log = LoggerFactory.getLogger(StressTest.class);
    final static int DEFAULT_PORT = 1234;

    @Override
    public int playGame(ClientExchanger client) {
        if (client == null) {
            throw new IllegalArgumentException("ClientExchanger is null");
        }
        TestExchanger exchanger = new TestExchanger(client);
        TestGUIFormer gui = new TestGUIFormer(exchanger);


        try {
            gui.createMainWindow();
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | InstantiationException | IllegalAccessException e) {
            log.warn(e.getMessage(), e);
            return -1;
        }
        return 0;
    }
}
