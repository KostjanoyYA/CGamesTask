package ru.kostyanoy.mode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kostyanoy.dataexchange.ClientExchanger;
import ru.kostyanoy.ui.UserGUIFormer;

import javax.swing.*;

public class SinglePlayer implements GameMode {
    private static final Logger log = LoggerFactory.getLogger(SinglePlayer.class);

    @Override
    public int playGame(ClientExchanger client) {
        if (client == null) {
            throw new IllegalArgumentException("ClientExchanger is null");
        }
        UserGUIFormer gui = new UserGUIFormer(client);

        String userStringNickName;

        while (true) {
            userStringNickName = gui.askNickName();
            log.debug("userStringNickName = {}", userStringNickName);
            if (client.hasCheckedNickName(userStringNickName)) {
                break;
            }
            if (gui.askExit("Nickname '" + userStringNickName + "' is busy. Try again?") == 1) {
                client.stopExchange();
                log.debug("client.stopExchange() from Main");
                return 1;
            }
        }
        gui.showMessage("Successful user name checking");

        try {
            gui.createMainWindow();
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | InstantiationException | IllegalAccessException e) {
            log.warn(e.getMessage(), e);
            return -1;
        }
        return 0;
    }
}
