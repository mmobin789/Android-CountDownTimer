package stacktex.mobin.search.countdowntimer;

import android.os.Handler;
import android.os.HandlerThread;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * A simple count-town timer identical to android.os.CountDownTimer but with simplified usage and additional functionality to pause and resume.
 * Note: This timer runs on UI thread by default but that can be changed by calling runOnBackgroundThread at any time.
 * @author Mobin Munir
 */
public final class SimpleCountDownTimer {
    private OnCountDownListener onCountDownListener;
    private long fromMinutes;
    private long fromSeconds;
    private long delayInSeconds = 1;
    private Calendar calendar = Calendar.getInstance();
    private long seconds, minutes;
    private boolean finished;
    private Handler handler = new Handler();
    private HandlerThread handlerThread;
    private boolean isBackgroundThreadRunning;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
    private Runnable runnable = this::decrementMinutes;

    /**
     * @param fromMinutes         minutes to countdown.
     * @param fromSeconds         seconds to countdown.
     * @param onCountDownListener A listener for countdown ticks.
     * @param delayInSeconds      optional delay in seconds for a tick to execute default is 1 second.
     */
    public SimpleCountDownTimer(long fromMinutes, long fromSeconds, long delayInSeconds, OnCountDownListener onCountDownListener) {

        if (fromMinutes <= 0 && fromSeconds <= 0)
            throw new IllegalStateException(getClass().getSimpleName() + " can't work in state 0:00");

        if (delayInSeconds > 1)
            this.delayInSeconds = delayInSeconds;

        this.onCountDownListener = onCountDownListener;


        setCountDownValues(fromMinutes, fromSeconds);


    }

    /**
     * This method sets business logic for countdown operation before it starts.
     */
    private void setCountDownValues(long fromMinutes, long fromSeconds) {
        this.fromMinutes = fromMinutes;
        this.fromSeconds = fromSeconds;
        minutes = this.fromMinutes;

        if (fromMinutes > 0 && fromSeconds <= 0) {
            seconds = 0;
            return;
        }

        if (fromSeconds <= 0 || fromSeconds > 59) {
            seconds = 59;
            return;
        }

        seconds = this.fromSeconds;

    }


    /**
     * @return This method returns seconds till countdown.
     */
    public long getSecondsTillCountDown() {
        return seconds;
    }

    /**
     * @return This method returns minutes till countdown.
     */
    public long getMinutesTillCountDown() {
        return minutes;
    }

    /**
     * Sets a new pattern for SimpleDateFormat for time returned on each tick.
     *
     * @param pattern only acceptable "mm:ss","m:s","mm","ss","m","s".
     */

    public void setTimerPattern(String pattern) {
        if (pattern.equalsIgnoreCase("mm:ss") || pattern.equalsIgnoreCase("m:s") || pattern.equalsIgnoreCase("mm") ||
                pattern.equalsIgnoreCase("ss") || pattern.equalsIgnoreCase("m") || pattern.equalsIgnoreCase("s"))
            simpleDateFormat.applyPattern(pattern);
    }

    /**
     * This method call will permanently move the timer to run in background thread for this instance.
     * A new thread is created which is then bound to timer's handler of main thread's message queue therefore overwriting it.
     * This method can be invoked at any time.
     * Note: onCountDownListener callbacks will not be invoked on main thread.
     */
    public final void runOnBackgroundThread() {

        if (isBackgroundThreadRunning)
            return;

        handlerThread = new HandlerThread(getClass().getSimpleName());

        startBackgroundThreadIfNotRunningAndEnabled();

        handler = new Handler(handlerThread.getLooper());


    }

    private void startBackgroundThreadIfNotRunningAndEnabled() {
        if (handlerThread != null && !handlerThread.isAlive()) {
            handlerThread.start();
            isBackgroundThreadRunning = true;
        }
    }

    /**
     * No need to quit background thread once started.
     * Quitting it kills it. Threads don't restart.
     * This is just left here if needed for any reason in future.
     */
  /*  private void quitBackgroundThreadSafelyIfRunning() {

        if (!isBackgroundThreadRunning)
            return;

        isBackgroundThreadRunning = !handlerThread.quitSafely();
    }*/
    @NotNull
    private String getCountDownTime() {
        calendar.set(Calendar.MINUTE, (int) minutes);
        calendar.set(Calendar.SECOND, (int) seconds);
        return simpleDateFormat.format(calendar.getTime());
    }

    private void decrementMinutes() {

        seconds--;

        if (minutes == 0 && seconds == 0) {
            finish();
        }

        if (seconds < 0) {
            if (minutes > 0) {
                seconds = 59;
                minutes--;
            }

        }


        runCountdown();


    }

    private void finish() {
        onCountDownListener.onCountDownFinished();
        finished = true;
        pause();
    }

    private void decrementSeconds() {
        handler.postDelayed(runnable, TimeUnit.SECONDS.toMillis(delayInSeconds));

    }

    /**
     * A method to start/resume countdown.
     *
     * @param resume if true it will resume from where its paused else from start.
     */
    public final void start(boolean resume) {

        if (!resume) {
            setCountDownValues(fromMinutes, fromSeconds);
            finished = false;
        }

        runCountdown();


    }

    private void runCountdown() {
        if (!finished) {
            updateUI();
            decrementSeconds();
        }
    }

    private void updateUI() {
        onCountDownListener.onCountDownActive(getCountDownTime());
    }

    /**
     * A method to pause/stop countdown.
     */
    public final void pause() {
        handler.removeCallbacks(runnable);
    }

    /**
     * A countdown listener to be used to listen for ticks and finish.
     */
    public interface OnCountDownListener {
        /**
         * A method continuously called on ticking.
         *
         * @param time The time at tick.
         */
        void onCountDownActive(String time);

        /**
         * A method called once when countdown is finished.
         */
        void onCountDownFinished();
    }
}
