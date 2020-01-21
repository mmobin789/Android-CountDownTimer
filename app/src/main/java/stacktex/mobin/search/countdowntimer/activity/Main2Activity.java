package stacktex.mobin.search.countdowntimer.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import stacktex.mobin.search.countdowntimer.R;
import stacktex.mobin.search.countdowntimer.SimpleCountDownTimer;

public class Main2Activity extends AppCompatActivity implements SimpleCountDownTimer.OnCountDownListener {
    private TextView textView;
    private Button start;
    private Button resume;

    private SimpleCountDownTimer simpleCountDownTimer = new SimpleCountDownTimer(0, 10, 1, this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.tv);
        start = findViewById(R.id.startBtn);
        resume = findViewById(R.id.resumeBtn);
        Button pause = findViewById(R.id.pauseBtn);

        resume.setEnabled(false);

        start.setOnClickListener(view -> {
            simpleCountDownTimer.start(false);

            start.setEnabled(false);

        });

        resume.setOnClickListener(view -> {
            simpleCountDownTimer.start(true);
            simpleCountDownTimer.runOnBackgroundThread();
        });
        pause.setOnClickListener(view -> {
            simpleCountDownTimer.pause();
            simpleCountDownTimer.setTimerPattern("s");
            resume.setEnabled(true);
        });


    }


    @Override
    public void onCountDownActive(String time) {
        textView.post(() -> textView.setText(time));

    }

    @Override
    public void onCountDownFinished() {
        textView.post(() -> {
            textView.setText("Finished");
            start.setEnabled(true);
            resume.setEnabled(false);
        });


    }
}
