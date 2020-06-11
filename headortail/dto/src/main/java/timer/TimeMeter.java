package timer;

public class TimeMeter {
    private long startTime;
    private long duration;
    private long remainingTime;

    public TimeMeter(long duration)
    {
        this.duration = Math.abs(duration);
    }

    public void startTimer() {
        this.startTime = System.nanoTime();
    }

    public void stopAndResetTimer(long newDuration) {
        this.duration = newDuration;
        remainingTime = this.duration;
    }

    public void restartTimer() {
        stopAndResetTimer(duration);
        startTimer();
    }

    public boolean hasTimesUp() {
        return (System.nanoTime() - startTime) >= duration;
    }
}