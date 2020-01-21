package stacktex.mobin.search.countdowntimer;

import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

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

    public SimpleCountDownTimer(long fromMinutes, long fromSeconds, long delayInSeconds, OnCountDownListener onCountDownListener) {
        this.fromMinutes = fromMinutes;
        this.fromSeconds = fromSeconds;
        if (delayInSeconds > 0)
            this.delayInSeconds = delayInSeconds;
        this.onCountDownListener = onCountDownListener;
        minutes = fromMinutes;
        seconds = fromSeconds;
    }

    /**
     * Sets a new pattern for SimpleDateFormat for time returned on each tick.
     *
     * @param pattern a pattern e.g. "mm:ss","hh:mm:ss" or "ss" etc.
     */

    public void setTimerPattern(String pattern) {
        if (!TextUtils.isEmpty(pattern))
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
        if (seconds == 0) {
            minutes--;
            seconds = fromSeconds;
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
            minutes = fromMinutes;
            seconds = fromSeconds;
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


    public interface OnCountDownListener {
        void onCountDownActive(String time);

        void onCountDownFinished();
    }
}
