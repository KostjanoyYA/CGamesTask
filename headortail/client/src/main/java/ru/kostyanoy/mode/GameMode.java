package ru.kostyanoy.mode;

import ru.kostyanoy.data.exchange.ClientExchanger;

/**
 * Defines behavior of different variants of the game mode
 */
public interface GameMode {
    /**
     * Creates UI and starts game
     * @param client {@link ClientExchanger} that has successfully connected to the server
     */
    void playGame(ClientExchanger client);
}
