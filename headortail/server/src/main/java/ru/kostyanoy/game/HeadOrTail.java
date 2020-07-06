package ru.kostyanoy.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class HeadOrTail implements Game {

    private static final Random rnd = new Random();
    private static final double WINRATE = 1.9d;
    private static final long LIMIT = 0L;
    private static final List<String> allMovies;
    private List<String> possibleMovies;
    private final RoundResult roundResult;

    static {
        Player.customizePlayer("defaultplayer.properties");
        allMovies = Arrays.stream(HeadOrTailMoves.values())
                .map(Enum::toString)
                .collect(Collectors.toList());
    }

    public HeadOrTail() {
        roundResult = new RoundResult();
    }

    @Override
    public List<String> getPossibleMovies() {
        return possibleMovies;
    }

    @Override
    public RoundResult changePlayerStateByGame(long bet, Player player, String choice) {
        roundResult.setStateAfterRound(player);
        roundResult.setRoundResultMovement("");
        roundResult.setRoundResultTime();
        roundResult.setPossibleMovies(new ArrayList<>());

        roundResult.setStakeApproved(false);
        roundResult.setStateBeforeRound(player);
        roundResult.setStake(bet, choice);
        roundResult.setStakeTime();

        roundResult.setPlayerAllowed(isAllowed(player));
        if (!roundResult.isPlayerAllowed()) {
            roundResult.setRoundResultMovement(HeadOrTailMessages.PLAYER_NOT_ALLOWED.getMessage());
            return roundResult;
        }

        roundResult.setStakeApproved(isStakeAccepted(player, bet, choice));
        if (!roundResult.isStakeApproved()) {
            roundResult.setRoundResultMovement(HeadOrTailMessages.BET_REJECTED.getMessage());
            return roundResult;
        }

        player.setAccount(player.getAccount()
                + (HeadOrTailMoves.valueOf(choice) == coinDrop()
                ? Math.round(WINRATE * bet) - bet
                : -bet));
        roundResult.setRoundResultTime();
        roundResult.setStateAfterRound(player);
        roundResult.setPossibleMovies(this.possibleMovies);
        return roundResult;
    }

    private HeadOrTailMoves coinDrop() {
        for (int i = 0; i < System.nanoTime() % 10; i++) {
            rnd.nextBoolean();
        }
        HeadOrTailMoves roundResultMove = rnd.nextBoolean() ? HeadOrTailMoves.HEAD : HeadOrTailMoves.TAIL;
        roundResult.setRoundResultMovement(roundResultMove.toString());
        return roundResultMove;
    }

    private boolean isAllowed(Player player) {return player != null && player.getAccount() > LIMIT; }

    private boolean isStakeAccepted(Player player, long bet, String choice) {
        return bet > 0 && bet <= player.getAccount() && possibleMovies.contains(choice);
    }

    @Override
    public long getInputLimit() {
        return LIMIT;
    }

    @Override
    public Player createNewPlayer() {
        possibleMovies = allMovies;
        return Player.createDefaultPlayer();
    }
}
