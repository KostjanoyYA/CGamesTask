package kostyanoy.game.history;

import kostyanoy.game.HeadOrTailMoves;
import kostyanoy.game.Player;
import ru.kostyanoy.entity.PlayerState;
import ru.kostyanoy.entity.Stake;

import java.time.LocalDateTime;

public class RoundHistory extends History {
    private PlayerState stateBeforeRound;
    private PlayerState stateAfterRound;
    private HeadOrTailMoves roundResultMovement;
    private LocalDateTime stakeTime;
    private Stake stake;
    private boolean isStakeApproved;
    private LocalDateTime roundResultTime;

    public PlayerState getStateBeforeRound() {
        return stateBeforeRound;
    }

    public void setStateBeforeRound(Player player) {
        this.stateBeforeRound.setTokenCount(player.getAccount());
    }

    public PlayerState getStateAfterRound() {
        return stateAfterRound;
    }

    public void setStateAfterRound(Player player) {
        this.stateAfterRound.setTokenCount(player.getAccount());
    }

    public HeadOrTailMoves getRoundResultMovement() {
        return roundResultMovement;
    }

    public void setRoundResultMovement(HeadOrTailMoves roundResultMovement) {
        this.roundResultMovement = roundResultMovement;
    }

    public LocalDateTime getStakeTime() {
        return stakeTime;
    }

    public void setStakeTime() {
        this.stakeTime = LocalDateTime.now();
    }

    public Stake getStake() {
        return stake;
    }

    public void setStake(long tokens, String option) {
        this.stake = new Stake(tokens, option);
    }

    public LocalDateTime getRoundResultTime() {
        return roundResultTime;
    }

    public void setRoundResultTime() {
        this.roundResultTime = LocalDateTime.now();
    }

    public boolean isStakeApproved() {
        return isStakeApproved;
    }

    public void setStakeApproved(boolean stakeApproved) {
        isStakeApproved = stakeApproved;
    }

    @Override
    public void addEvent(String nickName, History event) {

    }
}
