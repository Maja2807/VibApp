package com.example.vib;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class InterpretationModeActivity extends AppCompatActivity {
    private Vibrator vibrator;
    private Handler handler = new Handler();
    private Random random = new Random();
    private long vibrationStartTime;
    private boolean waitingForReaction = false;
    private List<Long> reactionTimes = new ArrayList<>();
    private int remainingVibrations = 10;  // Anzahl der Vibrationen im Test
    private boolean isHighPoint = false;  // Wird der Hochpunkt oder Tiefpunkt erkannt?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interpretation_mode);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        TextView statusText = findViewById(R.id.statusText);

        Button startButton = findViewById(R.id.startTestButton);
        startButton.setOnClickListener(v -> startTest());

        findViewById(R.id.backButton).setOnClickListener(v -> finish());  // Zurück-Button

    }

    private void startTest() {
        reactionTimes.clear();
        remainingVibrations = 10;
        isHighPoint = false;
        scheduleNextVibration();
        findViewById(R.id.startTestButton).setEnabled(false); // Disable start button
    }

    private void scheduleNextVibration() {
        if (remainingVibrations <= 0) {
            finishTest();
            return;
        }

        long delay = random.nextInt(5000) + 1000; // zufällige Verzögerung für nächste Vibration (1–5s)

        handler.postDelayed(() -> {
            if (vibrator != null) {
                // Vibration abhängig vom aktuellen Punkt (hoch oder tief)
                VibrationEffect effect = getVibrationEffect();
                vibrator.vibrate(effect);
                vibrationStartTime = SystemClock.elapsedRealtime();
                waitingForReaction = true;
            }
        }, delay);
    }

    private VibrationEffect getVibrationEffect() {
        if (isHighPoint) {
            // Schnelle Impulse für Hochpunkt (kurze Zeitabstände)
            long[] timings = {50, 50};  // kurze Abstände für "Blinken"
            int[] amplitudes = {128, 128};  // gleiche Amplitude für "Blinken"
            isHighPoint = false;  // Hochpunkt nach dieser Vibration als erkannt markieren
            return VibrationEffect.createWaveform(timings, amplitudes, -1);
        } else {
            // Langsame Impulse für Anstieg/Abfall (große Zeitabstände)
            long[] timings = {1000, 500, 1000};  // große Zeitabstände für Anstieg/Abfall
            int[] amplitudes = {128, 128, 128};  // gleiche Amplitude
            isHighPoint = true;  // Nächste Vibration ist ein Hochpunkt
            return VibrationEffect.createWaveform(timings, amplitudes, -1);
        }
    }

    private void recordReactionTime() {
        if (waitingForReaction) {
            long reactionTime = SystemClock.elapsedRealtime() - vibrationStartTime;
            reactionTimes.add(reactionTime);
            waitingForReaction = false;
            remainingVibrations--;
            scheduleNextVibration();  // Nächste Vibration planen
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            recordReactionTime();  // Reaktion registrieren
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void finishTest() {
        // Berechnung der durchschnittlichen Reaktionszeit
        if (!reactionTimes.isEmpty()) {
            long sum = 0;
            for (long time : reactionTimes) {
                sum += time;
            }
            long averageReactionTime = sum / reactionTimes.size();
            Log.d("InterpretationMode", "Durchschnittliche Reaktionszeit: " + averageReactionTime + " ms");
        } else {
            Log.d("InterpretationMode", "Keine gültigen Reaktionszeiten erfasst.");
        }

        // Zeige den "Zurück-Button" an, wenn der Test abgeschlossen ist
        findViewById(R.id.startTestButton).setEnabled(true);
    }
}
