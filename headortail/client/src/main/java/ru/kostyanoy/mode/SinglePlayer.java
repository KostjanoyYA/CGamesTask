package ru.kostyanoy.mode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kostyanoy.dataexchange.ClientExchanger;
import ru.kostyanoy.ui.UserGUIFormer;

import javax.swing.*;
import java.util.Arrays;

public class SinglePlayer implements GameMode {
    private static final int REFRESH_TIMEOUT = 1000;
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
                return -1;
            }
        }
        gui.showMessage("Successful user name checking");
        gui.createMainWindow();
        Thread refresher = new Thread(() -> {
            while (!Thread.interrupted()) {
                SwingUtilities.invokeLater(() -> {
                    gui.getAccountLabel().setText(String.valueOf(client.getPlayerState().getTokenCount()));
                    refreshComboBox(gui.getComboBox(), client);
                    gui.getRoundResultValue().setText(client.getPreviousRoundResult());
                });
                try {
                    Thread.sleep(REFRESH_TIMEOUT);
                } catch (InterruptedException e) {
                    log.warn(e.getMessage(), e);
                    break;
                }
            }
        });
        refresher.setDaemon(true);
        refresher.start();
        return 0;
    }

    private void refreshComboBox(JComboBox comboBox, ClientExchanger exchanger) {
        int itemCount = comboBox.getItemCount();
        for (int i = itemCount - 1; i >= 0; i--) {
            if (!Arrays.asList(exchanger.getPossibleOptions()).contains(comboBox.getItemAt(i))) {
                comboBox.remove(i);
            }
        }

        boolean hasFound = false;
        for (int i = 0; i < exchanger.getPossibleOptions().length; i++) {
            for (int j = 0; j < comboBox.getItemCount(); j++) {
                if (comboBox.getItemAt(j).equals(exchanger.getPossibleOptions()[i])) {
                    hasFound = true;
                    break;
                }
            }
            if (!hasFound) {
                comboBox.addItem(exchanger.getPossibleOptions()[i]);
            }
            hasFound = false;
        }
    }
}
