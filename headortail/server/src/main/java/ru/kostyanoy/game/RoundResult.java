package ru.kostyanoy.game;

import ru.kostyanoy.entity.PlayerState;
import ru.kostyanoy.entity.Stake;
import ru.kostyanoy.history.HistoryEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RoundResult implements HistoryEvent {
    private PlayerState stateAfterRound;
    private String roundResultMovement;
    private LocalDateTime roundResultTime;

    private PlayerState stateBeforeRound;
    private Stake stake;
    private LocalDateTime stakeTime;
    private boolean isPlayerAllowed;
    private boolean isStakeApproved;

    private List<String> possibleMovies;

    public RoundResult() {
        this.stateBeforeRound = new PlayerState();
        this.stateAfterRound = new PlayerState();
        this.possibleMovies = new ArrayList<>();
    }

    private RoundResult(RoundResult result) {
        this();
        this.stateAfterRound = (PlayerState) result.getStateAfterRound().clone();
        this.stateBeforeRound = (PlayerState) result.getStateBeforeRound().clone();
        this.roundResultMovement = result.roundResultMovement;
        this.stake = (Stake) result.getStake().clone();
        this.isPlayerAllowed = result.isPlayerAllowed;
        this.isStakeApproved = result.isStakeApproved;
        this.roundResultTime = cloneLocalDateTime(result.roundResultTime);
        this.stakeTime = cloneLocalDateTime(result.stakeTime);
        if (!(result.possibleMovies instanceof ArrayList
                && (result.possibleMovies.isEmpty() || result.possibleMovies.get(0) != null) )) {
            throw new ClassCastException("Possible movies List is not an ArrayList of Strings");
        }
        this.possibleMovies = (List<String>) ((ArrayList) result.possibleMovies).clone();
    }

    private LocalDateTime cloneLocalDateTime(LocalDateTime original) {
        return LocalDateTime.of(
                original.getYear(),
                original.getMonth(),
                original.getDayOfMonth(),
                original.getHour(),
                original.getMinute(),
                original.getSecond());
    }

    public Stake getStake() {
        return stake;
    }

    public PlayerState getStateAfterRound() {
        return stateAfterRound;
    }

    public PlayerState getStateBeforeRound() {
        return stateBeforeRound;
    }

    public String getRoundResultMovement() {
        return roundResultMovement;
    }

    public boolean isStakeApproved() {
        return isStakeApproved;
    }

    public boolean isPlayerAllowed() {
        return isPlayerAllowed;
    }

    public List<String> getPossibleMovies() {
        return possibleMovies;
    }

    public void setStateBeforeRound(Player player) {
        this.stateBeforeRound.setTokenCount(player.getAccount());
    }

    public void setStateAfterRound(Player player) {
        this.stateAfterRound.setTokenCount(player.getAccount());
    }

    public void setRoundResultMovement(String roundResultMovement) {
        this.roundResultMovement = roundResultMovement;
    }

    public void setStakeTime() {
        this.stakeTime = LocalDateTime.now();
    }

    public void setStake(long tokens, String option) {
        this.stake = new Stake(tokens, option);
    }

    public void setRoundResultTime() {
        this.roundResultTime = LocalDateTime.now();
    }

    public void setPossibleMovies(List<String> possibleMovies) {
        this.possibleMovies = possibleMovies;
    }

    public void setStakeApproved(boolean stakeApproved) {
        isStakeApproved = stakeApproved;
    }

    public void setPlayerAllowed(boolean playerAllowed) {
        this.isPlayerAllowed = playerAllowed;
    }

    @Override
    public RoundResult clone() {
        return new RoundResult(this);
    }

    @Override
    public String toString() {
        return "RoundResult{" +
                "stateBeforeRound=" + stateBeforeRound +
                ", stateAfterRound=" + stateAfterRound +
                ", stake=" + stake +
                ", stakeTime=" + stakeTime +
                ", isPlayerAllowed=" + isPlayerAllowed +
                ", isStakeApproved=" + isStakeApproved +
                ", roundResultMovement='" + roundResultMovement + '\'' +
                ", roundResultTime=" + roundResultTime +
                ", possibleMovies=" + possibleMovies +
                '}';
    }
}
