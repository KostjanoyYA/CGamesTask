package kostyanoy.game;

public interface Game {
    Player changePlayerStateByGame(long bet, Player player);
    boolean isAllowed(Player player);
    boolean isBetAccepted(Player player, long bet);
    long getInputLimit();
    Player createNewPlayer();
}
