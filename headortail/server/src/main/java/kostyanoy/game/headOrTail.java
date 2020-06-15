package kostyanoy.game;

import java.util.Random;

public class headOrTail implements Game {

    private static Random rnd = new Random();
    private static final double WINRATE = 1.9d;
    private static final long LIMIT = 0L;


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
        return player != null && player.getAccount() > LIMIT;
    }

    @Override
    public boolean isBetAccepted(Player player, long bet) {
        return bet > 0 && bet <= player.getAccount();
    }

    @Override
    public long getInputLimit() {
        return LIMIT;
    }
}
