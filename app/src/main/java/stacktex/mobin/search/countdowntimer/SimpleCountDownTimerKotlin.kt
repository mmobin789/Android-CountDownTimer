package stacktex.mobin.search.countdowntimer

import android.os.Handler
import android.os.HandlerThread
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * A simple count-town timer identical to android.os.CountDownTimer but with simplified usage and additional functionality to pause and resume.
 * @param fromMinutes Minutes to countdown.
 * @param fromSeconds Seconds to countdown.
 * @param onCountDownListener A listener for countdown ticks.
 * @param delayInSeconds optional delay in seconds for a tick to execute default is 1 second.
 * Note: This timer runs on UI thread by default but that can be changed by calling runOnBackgroundThread at any time.
 */
class SimpleCountDownTimerKotlin(
    private val fromMinutes: Long,
    private val fromSeconds: Long,
    private val onCountDownListener: OnCountDownListener,
    private var delayInSeconds: Long = 1
) {

    private val calendar = Calendar.getInstance()
    private var seconds = 0L
    private var minutes = 0L
    private var finished = false
    private var isStarted = false
    private var handler = Handler()
    private var handlerThread: HandlerThread? = null
    private var isBackgroundThreadRunning = false
    private val simpleDateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
    private val runnable = Runnable { decrementMinutes() }

    init {
        if (delayInSeconds <= 0)
            delayInSeconds = 1

        minutes = fromMinutes
        seconds = fromSeconds
    }

    /**
     * Sets a new pattern for SimpleDateFormat for time returned on each tick.
     *
     * @param pattern a pattern e.g. "mm:ss","hh:mm:ss" or "ss" etc.
     */
    fun setTimerPattern(pattern: String) {
        if (pattern.isNotBlank())
            simpleDateFormat.applyPattern(pattern)
    }

    /**
     * This method call will permanently move the timer to run in background thread for this instance.
     * A new thread is created which is then bound to timer's handler of main thread's message queue therefore overwriting it.
     * This method can be invoked at any time.
     * Note: onCountDownListener callbacks will not be invoked on main thread.
     */
    fun runOnBackgroundThread() {
        if (isBackgroundThreadRunning) return
        handlerThread = HandlerThread(javaClass.simpleName)
        startBackgroundThreadIfNotRunningAndEnabled()
        handler = Handler(handlerThread!!.looper)
    }

    private fun startBackgroundThreadIfNotRunningAndEnabled() {

        handlerThread!!.run {
            start()
            isBackgroundThreadRunning = true
        }

    }

    private fun getCountDownTime(): String {
        calendar[Calendar.MINUTE] = minutes.toInt()
        calendar[Calendar.SECOND] = seconds.toInt()
        return simpleDateFormat.format(calendar.time)
    }

    private fun decrementMinutes() {
        seconds--
        if (minutes == 0L && seconds == 0L) {
            onCountDownListener.onCountDownFinished()
            finished = true
            isStarted = false
            pause()
        }
        if (seconds == 0L) {
            minutes--
            seconds = fromSeconds
        }
        runCountdown()
    }

    private fun decrementSeconds() {
        handler.postDelayed(
            runnable,
            TimeUnit.SECONDS.toMillis(delayInSeconds)
        )
    }

    /**
     * A method to start/resume countdown.
     *
     * @param resume if true it will resume from where its paused else from start.
     */
    fun start(resume: Boolean = false) {
        if (!resume && isStarted) return
        if (!resume) {
            minutes = fromMinutes
            seconds = fromSeconds
            finished = false
        }
        runCountdown()
        isStarted = true
    }

    private fun runCountdown() {
        if (!finished) {
            updateUI()
            decrementSeconds()
        }
    }

    private fun updateUI() {
        onCountDownListener.onCountDownActive(getCountDownTime())
    }

    /**
     * A method to pause/stop countdown.
     */
    fun pause() {
        handler.removeCallbacks(runnable)
    }


    interface OnCountDownListener {
        fun onCountDownActive(time: String)
        fun onCountDownFinished()
    }
}