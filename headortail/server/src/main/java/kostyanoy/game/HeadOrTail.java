package kostyanoy.game;

import kostyanoy.game.history.History;
import kostyanoy.game.history.HistoryTaker;
import kostyanoy.game.history.RoundHistory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HeadOrTail implements Game, HistoryTaker {

    private static Random rnd = new Random();
    private static final double WINRATE = 1.9d;
    private static final long LIMIT = 0L;
    private static List<String> allMovies;
    private List<String> possibleMovies;
    private HeadOrTailMoves roundResult;
    private RoundHistory roundHistory;

    static {
        Player.customizePlayer("defaultplayer.properties");
        allMovies = HeadOrTailMoves.getAllMoves();
    }

    public HeadOrTail() {
        possibleMovies = allMovies;
        roundHistory = new RoundHistory();
    }

    @Override
    public Player changePlayerStateByGame(long bet, Player player, String choice) {
        roundHistory.setStateBeforeRound(player);
        roundHistory.setStake(bet, choice);
        roundHistory.setStakeTime();

        if (!isAllowed(player) || !isBetAccepted(player, bet, choice)) {
            roundHistory.setStakeApproved(false);
            return player;
        }

        roundHistory.setStakeApproved(true);
        player.setAccount(player.getAccount()
                + (HeadOrTailMoves.valueOf(choice) == coinDrop()
                ? Math.round(WINRATE * bet) - bet
                : -bet));
        roundHistory.setRoundResultTime();
        roundHistory.setStateAfterRound(player);
        return player;
    }

    private HeadOrTailMoves coinDrop() {
        for (int i = 0; i < System.nanoTime() % 10; i++) {
            rnd.nextBoolean();
        }
        roundResult = rnd.nextBoolean() ? HeadOrTailMoves.HEAD : HeadOrTailMoves.TAIL;
        roundHistory.setRoundResultMovement(roundResult);
        return roundResult;
    }

    @Override
    public boolean isAllowed(Player player) {
        return player != null && player.getAccount() > LIMIT;
    }

    @Override
    public boolean isBetAccepted(Player player, long bet, String choice) {
        return bet > 0 && bet <= player.getAccount() && possibleMovies.contains(choice);
    }

    @Override
    public long getInputLimit() {
        return LIMIT;
    }

    @Override
    public Player createNewPlayer() {
        return Player.createDefaultPlayer();
    }

    @Override
    public List<String> getPossibleMoves(Player player) {
        return player != null ? possibleMovies : new ArrayList<String>();
    }

    @Override
    public String getRoundResult() {
        return roundResult.toString();
    }

    @Override
    public History getHistory() {
        return roundHistory;
    }
}
