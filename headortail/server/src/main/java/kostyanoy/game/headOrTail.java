package kostyanoy.game;

import java.util.Random;

public class headOrTail implements Game {

    private static Random rnd = new Random();
    private static final double WINRATE = 1.9d;


    @Override
    public Player changePlayerStateByGame(long bet, Player player) {
        if (!isAllowed(player) || !isBetAccepted(player, bet)) {
            return player;
        }

        for (int i = 0; i < System.nanoTime() % 10; i++) {
            rnd.nextBoolean();
        }
        player.setAccount(player.getAccount() + (rnd.nextBoolean() ? Math.round(WINRATE * bet) : -bet));
        return player;
    }

    @Override
    public boolean isAllowed(Player player) {
        if (player == null || player.getAccount() <= 0) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isBetAccepted(Player player, long bet) {
        if (bet <= 0 || bet > player.getAccount()) {
            return false;
        }
        return true;
    }
}
