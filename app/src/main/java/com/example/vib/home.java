package com.example.vib;

import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

// Trainingsmodus
public class home extends AppCompatActivity {
    private TextView text;
    private Button startButton;
    private Button modeButton;
    private boolean isVibrating = false;
    private boolean isMaxMode = true;
    private Vibrator vibrator;
    private Handler handler = new Handler();
    private Runnable vibrationRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        startButton = findViewById(R.id.startButton);
        modeButton = findViewById(R.id.modeButton);
        Button backButton = findViewById(R.id.backButton);

        startButton.setOnClickListener(v -> {
            if (!isVibrating) {
                if (isMaxMode) {
                    startMaxVibration();
                } else {
                    startMinVibration();
                }
                startButton.setText("Stop");
            } else {
                stopVibration();
                startButton.setText("Start");
            }
            isVibrating = !isVibrating;
        });

        modeButton.setOnClickListener(v -> {
            isMaxMode = !isMaxMode;
            modeButton.setText(isMaxMode ? "Wechsel zu A-Modus" : "Wechsel zu B-Modus"); //A = Min, B = Max
        });

        backButton.setOnClickListener(v -> finish()); // Zurück zum Hauptmenü
    }

    private void startMaxVibration() { //B
        if (vibrator == null) return;
        stopVibration(); // Vorherige Vibrationen stoppen

        long[] timings = {1400, 1200, 1000, 800, 600, 400, 200, 100, 50, 50, 50, 50, 50}; // Immer schneller
        int[] amplitudes = {120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120};
        //int[] amplitudes = {100, 120, 140, 160, 180, 200};
        //long[] timings = {130, 130, 130, 130, 130, 130}; //hoher Wert = mehrere kurze Vibrationen
        //int[] amplitudes = {200, 200, 200, 200, 200, 200};

        runVibrationPattern(timings, amplitudes);
    }

    private void startMinVibration() { //A
        if (vibrator == null) return;
        stopVibration(); // Vorherige Vibrationen stoppen!

        long[] timings = {50, 50, 50, 50, 50, 100, 200, 400, 600, 800, 1000, 1200, 1400}; // Immer langsamer
        int[] amplitudes = {120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120};
        //int[] amplitudes = {200, 180, 160, 140, 120, 100};
        //long[] timings = {2000}; // niedriger Wert = eine lange Vibration
        //int[] amplitudes = {200};

        runVibrationPattern(timings, amplitudes);
    }

    private void runVibrationPattern(long[] timings, int[] amplitudes) {
        vibrationRunnable = new Runnable() {
            int index = 0;

            @Override
            public void run() {
                if (!isVibrating || index >= timings.length) {
                    isVibrating = false;
                    startButton.setText("Start"); // Button zurücksetzen
                    return;
                }

                vibrator.vibrate(VibrationEffect.createOneShot(timings[index], amplitudes[index]));
                handler.postDelayed(this, timings[index]);
                index++;
            }
        };

        handler.post(vibrationRunnable);
    }

    private void stopVibration() {
        isVibrating = false;
        if (vibrator != null) {
            vibrator.cancel();
        }
        handler.removeCallbacksAndMessages(null);
    }
}
