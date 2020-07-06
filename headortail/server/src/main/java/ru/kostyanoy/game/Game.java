package ru.kostyanoy.game;

import java.util.List;

public interface Game {
    RoundResult changePlayerStateByGame(long bet, Player player, String choice);
    long getInputLimit();
    Player createNewPlayer();
    List<String> getPossibleMovies();
}
