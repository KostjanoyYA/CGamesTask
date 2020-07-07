package ru.kostyanoy.game;

import java.util.List;

/**
 * Determines common behaviour for classes used by server as a game
 */
public interface Game {
    /**
     * Changes {@link Player} state by game according to offered stake
     * @param bet    offered count of tokens
     * @param player {@link Player} offered the stake
     * @param choice string representation of user choice of possible game moves
     */
    RoundResult changePlayerStateByGame(long bet, Player player, String choice);

    /**
     * Gets the lower input threshold
     * @return the lowest count of tokens which allowed user play the game
     */
    long getInputLimit();

    /**
     * Creates new {@link Player} according game conditions
     * @return new {@link Player} of the game
     */
    Player createNewPlayer();

    /**
     * Gets a {@link List} of string representations of possible player moves for tne next round
     */
    List<String> getPossibleMovies();
}
