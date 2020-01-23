package stacktex.mobin.search.countdowntimer

import android.os.Handler
import android.os.HandlerThread
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * A simple count-town timer identical to android.os.CountDownTimer but with simplified usage and additional functionality to pause and resume.
 * @param fromMinutes minutes to countdown.
 * @param fromSeconds seconds to countdown.
 * @param onCountDownListener A listener for countdown ticks.
 * @param delayInSeconds optional delay in seconds for a tick to execute default is 1 second.
 * Note: This timer runs on UI thread by default but that can be changed by calling runOnBackgroundThread at any time.
 * @author Mobin Munir
 */
class SimpleCountDownTimerKotlin(
    private var fromMinutes: Long,
    private var fromSeconds: Long,
    private val onCountDownListener: OnCountDownListener,
    private var delayInSeconds: Long = 1
) {

    private val calendar = Calendar.getInstance()
    private var seconds = 0L
    private var minutes = 0L
    private var finished = false
    private var handler = Handler()
    private var handlerThread: HandlerThread? = null
    private var isBackgroundThreadRunning = false
    private val simpleDateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
    private val runnable = Runnable { decrementMinutes() }

    init {
        check(!(fromMinutes <= 0 && fromSeconds <= 0)) { javaClass.simpleName + " can't work in state 0:00" }

        if (delayInSeconds <= 0)
            delayInSeconds = 1

        setCountDownValues()
    }

    private fun setCountDownValues(
        fromMinutes: Long = this.fromMinutes,
        fromSeconds: Long = this.fromSeconds
    ) {
        this.fromMinutes = fromMinutes
        this.fromSeconds = fromSeconds
        minutes = this.fromMinutes

        if (fromMinutes > 0 && fromSeconds <= 0) {
            seconds = 0
            return
        }

        if (fromSeconds <= 0 || fromSeconds > 59) {
            seconds = 59
            return
        }
        seconds = this.fromSeconds
    }

    /**
     * @return This method returns seconds till countdown.
     */
    fun getSecondsTillCountDown() = seconds


    /**
     * @return This method returns minutes till countdown.
     */
    fun getMinutesTillCountDown() = minutes


    /**
     * Sets a new pattern for SimpleDateFormat for time returned on each tick.
     * @param pattern only acceptable "mm:ss","m:s","mm","ss","m","s".
     */
    fun setTimerPattern(pattern: String) {
        if (pattern.equals("mm:ss", ignoreCase = true) || pattern.equals(
                "m:s",
                ignoreCase = true
            ) || pattern.equals("mm", ignoreCase = true) ||
            pattern.equals("ss", ignoreCase = true) || pattern.equals(
                "m",
                ignoreCase = true
            ) || pattern.equals("s", ignoreCase = true)
        ) simpleDateFormat.applyPattern(pattern)
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
            finish()
        }

        if (seconds < 0L) {
            if (minutes > 0) {
                seconds = 59
                minutes--
            }
        }


        runCountdown()
    }

    private fun finish() {
        onCountDownListener.onCountDownFinished()
        finished = true
        pause()
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
        if (!resume) {
            setCountDownValues()
            finished = false
        }
        runCountdown()
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