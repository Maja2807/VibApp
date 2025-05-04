package com.example.vib;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
public class KombiActivity extends AppCompatActivity {

    private TextView statusText;
    private Button startTestButton, backButton;
    private Handler handler = new Handler();
    private boolean testRunning = false;
    private long lastCriticalTime = 0;
    private ArrayList<Long> reactionTimes = new ArrayList<>();
    private static final int TEST_DURATION = 180000; // 3 Minuten (in ms)
    private long testStartTime;
    private long lastSentTime;
    private int currentIndex = 0;
    private Vibrator vibrator;
    private long lastReceivedTime; // Zeitstempel für Wi-Fi Empfang
    private ArrayList<Long> latencyMeasurements = new ArrayList<>();

    // Herzfrequenzwerte
    private static final int[] HEART_RATE_VALUES = { //6x zu niedrig, 6x zu hoch
            94, 62, 75, 77, 60, 87, 91, 79, 38, 69, 93, 85, 79, 82, 99, 64, 74, 81, 82, 64,
            74, 94, 53, 79, 60, 82, 67, 79, 84, 82, 51, 80, 94, 94, 90, 94, 60, 85, 174, 86,
            64, 78, 79, 67, 80, 90, 58, 77, 83, 67, 99, 70, 77, 78, 80, 95, 85, 81, 83, 79,
            69, 98, 83, 86, 69, 89, 97, 78, 66, 72, 77, 66, 73, 164, 73, 97, 99, 136, 97, 96,
            98, 79, 88, 76, 84, 73, 128, 80, 75, 65, 74, 66, 84, 95, 86, 62, 91, 84, 96, 61,
            80, 98, 70, 99, 77, 64, 89, 119, 90, 93, 97, 89, 72, 98, 81, 92, 94, 70, 87, 83,
            99, 61, 64, 68, 78, 81, 94, 72, 66, 70, 88, 75, 75, 69, 73, 63, 90, 80, 76, 80,
            91, 168, 63, 73, 84, 73, 67, 70, 85, 86, 61, 76, 92, 89, 63, 84, 62, 76, 97, 61,
            96, 89, 97, 86, 70, 69, 59, 41, 90, 64, 75, 67, 99, 100, 96, 92, 98, 87, 70, 73
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kombi);

        statusText = findViewById(R.id.statusText);
        startTestButton = findViewById(R.id.startTestButton);
        backButton = findViewById(R.id.backButton);

        startTestButton.setOnClickListener(v -> startTest());
        backButton.setOnClickListener(v -> finish());

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        findViewById(android.R.id.content).setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && testRunning) {
                recordReaction();
                return true;
            }
            return false;
        });
    }

    private void startTest() {
        testRunning = true;
        startTestButton.setVisibility(View.GONE);
        backButton.setVisibility(View.GONE);
        statusText.setText("Test läuft...");
        testStartTime = SystemClock.elapsedRealtime();
        currentIndex = 0;
        scheduleNextStep();
    }

    private void scheduleNextStep() {
        if (SystemClock.elapsedRealtime() - testStartTime >= TEST_DURATION || currentIndex >= HEART_RATE_VALUES.length) {
            endTest();
            return;
        }

        handler.postDelayed(this::generateNextVibration, 1000); // 1 Sekunde
    }

    private void generateNextVibration() {
        if (!testRunning) return;

        int heartRate = HEART_RATE_VALUES[currentIndex];
        long currentTime = SystemClock.elapsedRealtime();
        lastCriticalTime = currentTime;

        Log.d("PulseVibrationTest", "Aktueller Puls: " + heartRate);
        lastSentTime = SystemClock.elapsedRealtime();

        // Vibrationsmuster je nach Pulswert
        if (heartRate < 60) {
            // Puls zu niedrig: lange starke Vibrationen bis normaler Bereich erreicht
            triggerStrongVibration(2000);
            Log.d("PulseVibrationTest", "Puls zu niedrig: lange starke Vibration gesendet.");
        } else if (heartRate > 100) {
            // Puls zu hoch: schnelle starke Impulse
            triggerRapidVibrations();
            Log.d("PulseVibrationTest", "Puls zu hoch: schnelle starke Impulse gesendet.");
        }

        currentIndex++;
        scheduleNextStep();
    }

    private void triggerStrongVibration(long duration) {
        // Lange starke Vibration
        vibrator.vibrate(duration);
        lastReceivedTime = SystemClock.elapsedRealtime();
        long latency = lastReceivedTime - lastSentTime;
        latencyMeasurements.add(latency);
        Log.d("InterpretationTest", "Wi-Fi Latenz gemessen: " + latency + " ms");
    }

    private void triggerRapidVibrations() {
        // Schnelle starke Vibrationen (mehrere Impulse)
        vibrator.vibrate(new long[]{0, 100, 50, 100, 50, 100}, -1);
        lastReceivedTime = SystemClock.elapsedRealtime();
        long latency = lastReceivedTime - lastSentTime;
        latencyMeasurements.add(latency);
        Log.d("InterpretationTest", "Wi-Fi Latenz gemessen: " + latency + " ms");
    }

    private void recordReaction() {
        if (testRunning) {
            // Überprüfen, ob die Reaktion auf den richtigen Pulswert war
            int heartRate = HEART_RATE_VALUES[currentIndex - 1]; // Der Wert, auf den reagiert werden soll
            if (heartRate < 60 || heartRate > 100) {
                long reactionTime = SystemClock.elapsedRealtime() - lastCriticalTime;
                reactionTimes.add(reactionTime);
                Log.d("PulseVibrationTest", "Korrekte Reaktion erfasst: " + reactionTime + " ms");
            } else {
                // Falsche Reaktion oder zu spät
                long reactionTime = SystemClock.elapsedRealtime() - lastCriticalTime;
                reactionTimes.add(reactionTime);
                Log.d("PulseVibrationTest", "Falsche Reaktion oder verspätet: " + reactionTime + " ms");
            }
        }
    }

    private void endTest() {
        testRunning = false;
        startTestButton.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.VISIBLE);
        statusText.setText("Test beendet");

        // durchschnittliche Reaktionszeit berechnen
        long sum = 0;
        for (long time : reactionTimes) {
            sum += time;
        }
        long avgReactionTime = reactionTimes.isEmpty() ? 0 : sum / reactionTimes.size();
        Log.d("PulseVibrationTest", "Durchschnittliche Reaktionszeit: " + avgReactionTime + " ms");

        // durchschnittliche WiFi-Latenz berechnen
        long latencySum = 0;
        for (long time : latencyMeasurements) {
            latencySum += time;
        }
        long avgLatency = latencyMeasurements.isEmpty() ? 0 : latencySum / latencyMeasurements.size();
        Log.d("InterpretationTest", "Durchschnittliche Wi-Fi Latenz: " + avgLatency + " ms");
    }
}