package stacktex.mobin.search.countdowntimer.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import stacktex.mobin.search.countdowntimer.R
import stacktex.mobin.search.countdowntimer.SimpleCountDownTimerKotlin

class MainActivity : AppCompatActivity(),
    SimpleCountDownTimerKotlin.OnCountDownListener {

    private val countDownTimer =
        SimpleCountDownTimerKotlin(
            0,
            30,
            this
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tv.setOnClickListener {
            countDownTimer.start()
            countDownTimer.setTimerPattern("ss:mm")
        }

        resumeBtn.setOnClickListener {
            countDownTimer.start(true)
        }

        pauseBtn.setOnClickListener {
            countDownTimer.pause()
        }
    }

    override fun onCountDownActive(time: String) {
        tv.text = time
        Toast.makeText(this, time, Toast.LENGTH_SHORT).show()
    }

    override fun onCountDownFinished() {
        tv.text = "Finished"
    }
}