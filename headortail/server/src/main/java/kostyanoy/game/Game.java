package kostyanoy.game;

import java.util.List;

public interface Game {
    Player changePlayerStateByGame(long bet, Player player, String choice);
    boolean isAllowed(Player player);
    boolean isBetAccepted(Player player, long bet, String choice);
    long getInputLimit();
    Player createNewPlayer();
    List<String> getPossibleMoves(Player player);
}
