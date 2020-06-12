package timer;

public class TimeMeter {
    private long startTime;
    private long duration;

    public TimeMeter(long duration)
    {
        this.duration = Math.abs(duration);
    }

    public void startTimer() {
        this.startTime = System.nanoTime();
    }

    public void stopAndResetTimer(long newDuration) {
        this.duration = Math.abs(newDuration);
    }

    public void restartTimer() {
        stopAndResetTimer(duration);
        startTimer();
    }

    public boolean hasTimesUp() {
        return (System.nanoTime() - startTime) >= duration;
    }
}