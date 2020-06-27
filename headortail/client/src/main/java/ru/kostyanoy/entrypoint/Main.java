package ru.kostyanoy.entrypoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kostyanoy.dataexchange.ClientExchanger;
import ru.kostyanoy.ui.UserGUIFormer;
import ru.kostyanoy.ui.VisualPresenter;

import javax.swing.*;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        final int defaultPort = 1234;
        ClientExchanger client = new ClientExchanger();
        VisualPresenter gui = createGUI(client);

        while (!client.getConnection().connect(gui.askServerIP(), defaultPort)) {
            gui.showMessage("The specified IP address is not available. Try again!");
        }
        gui.showMessage("Successful connection to the server");

        client.startExchange();

        String userStringNickName;
        boolean isCheckedNickName = false;
        while (true) {
            userStringNickName = gui.askNickName();
            log.debug("userStringNickName = {}", userStringNickName);
            isCheckedNickName = client.hasCheckedNickName(userStringNickName);
            if (isCheckedNickName) {
                break;
            }
            if (gui.askExit("Nickname '" + userStringNickName + "' is busy. Try again?") == 1) {
                client.stopExchange();
                log.debug("client.stopExchange() from Main");
                System.exit(0);
            }
        }


        gui.showMessage("Successful user name checking");

        try {
            gui.createMainWindow();
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | InstantiationException | IllegalAccessException e) {
            log.warn(e.getMessage(), e);
        }
    }

    private static VisualPresenter createGUI(ClientExchanger exchanger) {
        VisualPresenter gui = new UserGUIFormer(exchanger);
        javax.swing.SwingUtilities.invokeLater(() -> {
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);
        });
        return gui;
    }
}
