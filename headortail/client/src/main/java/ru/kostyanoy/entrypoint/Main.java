package ru.kostyanoy.entrypoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kostyanoy.connection.Exchanger;
import ru.kostyanoy.dataexchange.ClientExchanger;
import ru.kostyanoy.ui.GUIFormer;
import ru.kostyanoy.ui.VisualPresenter;

import javax.swing.*;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        final int defaultPort = 1234;
        Exchanger client = new ClientExchanger();
        VisualPresenter gui = createGUI(client);

        while (!((ClientExchanger) client).getConnection().connect(gui.askServerIP(), defaultPort)) {
            gui.showMessage("The specified IP address is not available. Try again!");
        }
        gui.showMessage("Successful connection to the server");

        client.startExchange();

        while (!client.hasCheckedNickName(gui.askNickName())) {
            gui.askExit("This nickname is busy. Try again?");
        }
        gui.showMessage("Successful user name checking");

        try {
            gui.createMainWindow();
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | InstantiationException | IllegalAccessException e) {
            log.warn(e.getMessage(), e);
        }

        ((ClientExchanger) client).askGamePermission();
        while (((ClientExchanger) client).isGameAllowed()) {
            //TODO игру здесь помести. Сервер отвечает за правила (начало и конец игры)
            ((ClientExchanger) client).getPlayerState(); //TODO Перенести в класс игры или view
        }
    }

    private static VisualPresenter createGUI(Exchanger exchanger) {
        VisualPresenter gui = new GUIFormer(exchanger);
        javax.swing.SwingUtilities.invokeLater(() -> {
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);
        });
        return gui;
    }
}
