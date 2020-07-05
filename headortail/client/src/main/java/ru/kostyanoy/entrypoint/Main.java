package ru.kostyanoy.entrypoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kostyanoy.data.exchange.ClientExchanger;
import ru.kostyanoy.ui.StartGUIFormer;

import javax.swing.*;

public class Main {

    final static private int DEFAULT_PORT = 1234;
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        ClientExchanger serviceExchanger = new ClientExchanger();

        StartGUIFormer gui = new StartGUIFormer();
        javax.swing.SwingUtilities.invokeLater(() -> {
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);
        });

        while (!serviceExchanger.getConnection().connect(gui.askServerIP(), DEFAULT_PORT)) {
            gui.showMessage("The specified IP address is not available. Try again!");
        }
        gui.showMessage("Successful connection to the server");

        serviceExchanger.startExchange();

        try {
            gui.createMainWindow(serviceExchanger);
        } catch (ClassNotFoundException
                | UnsupportedLookAndFeelException
                | InstantiationException
                | IllegalAccessException e) {
            log.warn(e.getMessage(), e);
        }
    }
}
