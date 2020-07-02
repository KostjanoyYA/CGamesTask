package ru.kostyanoy.entrypoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kostyanoy.dataexchange.ClientExchanger;
import ru.kostyanoy.ui.StartGUIFormer;
import ru.kostyanoy.ui.VisualPresenter;

import javax.swing.*;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        final int defaultPort = 1234;
        ClientExchanger serviceExchanger = new ClientExchanger();
        VisualPresenter gui = createGUI();

        while (!serviceExchanger.getConnection().connect(gui.askServerIP(), defaultPort)) {
            gui.showMessage("The specified IP address is not available. Try again!");
        }
        gui.showMessage("Successful connection to the server");

        serviceExchanger.startExchange();

        try {
            gui.createMainWindow(serviceExchanger);
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | InstantiationException | IllegalAccessException e) {
            log.warn(e.getMessage(), e);
        }
    }

    private static VisualPresenter createGUI() {
        VisualPresenter gui = new StartGUIFormer();
        javax.swing.SwingUtilities.invokeLater(() -> {
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);
        });
        return gui;
    }
}
