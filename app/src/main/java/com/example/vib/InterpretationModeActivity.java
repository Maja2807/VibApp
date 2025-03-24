package com.example.vib;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Random;

public class InterpretationModeActivity extends AppCompatActivity {

    private Vibrator vibrator;
    private TextView statusText;
    private Button startTestButton, backButton;
    private Handler handler = new Handler();
    private boolean testRunning = false;
    private long lastPeakTime = 0;
    private long lastValleyTime = 0;
    private ArrayList<Long> peakReactions = new ArrayList<>();
    private ArrayList<Long> valleyReactions = new ArrayList<>();
    private Random random = new Random();

    private final int TEST_DURATION = 180000; // 3 Minuten (in Millisekunden)
    private long testStartTime;

    private int currentLevel = 0; // Simulierter Kurvenverlauf (steigt/fällt)
    private boolean isGoingUp = true; // Richtung der Kurve

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interpretation_mode);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        statusText = findViewById(R.id.statusText);
        startTestButton = findViewById(R.id.startTestButton);
        backButton = findViewById(R.id.backButton);

        startTestButton.setOnClickListener(v -> startTest());
        backButton.setOnClickListener(v -> finish());

        // Nutzer kann irgendwo auf den Bildschirm tippen, um zu reagieren
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
        scheduleNextStep();
    }

    private void scheduleNextStep() {
        long elapsedTime = SystemClock.elapsedRealtime() - testStartTime;

        if (elapsedTime >= TEST_DURATION) {
            endTest();
            return;
        }

        int nextEventTime = random.nextInt(3000) + 1000; // Zufälliger Abstand: 1s - 4s

        handler.postDelayed(this::generateNextCurveStep, nextEventTime);
    }

    private void generateNextCurveStep() {
        if (!testRunning) return;

        if (isGoingUp) {
            currentLevel++;
        } else {
            currentLevel--;
        }

        // Hochpunkt erreicht
        if (currentLevel >= 5) {
            triggerPeak();
            isGoingUp = false;
        }
        // Tiefpunkt erreicht
        else if (currentLevel <= -5) {
            triggerValley();
            isGoingUp = true;
        }
        // Zwischenzustand (steigende oder fallende Kurve)
        else {
            triggerIntermediateState();
        }

        scheduleNextStep();
    }

    private void triggerPeak() {
        lastPeakTime = SystemClock.elapsedRealtime();
        vibratePattern(new long[]{130, 130, 130, 130, 130, 130}, 250); // Hochpunkt = schnelle Impulse
        runOnUiThread(() -> statusText.setText("🟢 Hochpunkt erreicht!"));
        System.out.println("🟢 Hochpunkt um " + lastPeakTime + " ms");
    }

    private void triggerValley() {
        lastValleyTime = SystemClock.elapsedRealtime();
        vibrate(2000, 128); // Tiefpunkt = lange Vibration
        runOnUiThread(() -> statusText.setText("🔵 Tiefpunkt erreicht!"));
        System.out.println("🔵 Tiefpunkt um " + lastValleyTime + " ms");
    }

    private void triggerIntermediateState() {
        if (isGoingUp) {
            long duration = 500 - (currentLevel * 20);
            if (duration < 100) duration = 100;
            vibrate(duration, 64);
            runOnUiThread(() -> statusText.setText("📈 Steigend..."));
            System.out.println("📈 Steigende Kurve");
        } else {
            long duration = 500 + (Math.abs(currentLevel) * 20);
            if (duration > 1000) duration = 1000;
            vibrate(duration, 64);
            runOnUiThread(() -> statusText.setText("📉 Fallend..."));
            System.out.println("📉 Fallende Kurve");
        }
    }

    private void vibrate(long duration, int amplitude) {
        if (vibrator != null) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude));
        }
    }

    private void vibratePattern(long[] pattern, int amplitude) {
        if (vibrator != null) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
        }
    }

    private void recordReaction() {
        long reactionTime = SystemClock.elapsedRealtime();

        if (lastPeakTime > 0 && reactionTime > lastPeakTime && reactionTime - lastPeakTime < 3000) {
            long reactionDelay = reactionTime - lastPeakTime;
            peakReactions.add(reactionDelay);
            runOnUiThread(() -> statusText.setText("Reaktion auf Hochpunkt: " + reactionDelay + " ms"));
            System.out.println("✅ Reaktion auf Hochpunkt: " + reactionDelay + " ms");
        } else if (lastValleyTime > 0 && reactionTime > lastValleyTime && reactionTime - lastValleyTime < 3000) {
            long reactionDelay = reactionTime - lastValleyTime;
            valleyReactions.add(reactionDelay);
            runOnUiThread(() -> statusText.setText("Reaktion auf Tiefpunkt: " + reactionDelay + " ms"));
            System.out.println("✅ Reaktion auf Tiefpunkt: " + reactionDelay + " ms");
        } else {
            runOnUiThread(() -> statusText.setText("⚠ Falsche Reaktion!"));
            System.out.println("⚠ Falsche Reaktion!");
        }
    }

    private void endTest() {
        testRunning = false;
        backButton.setVisibility(View.VISIBLE);
        startTestButton.setVisibility(View.VISIBLE);

        if (peakReactions.isEmpty() && valleyReactions.isEmpty()) {
            statusText.setText("❌ Keine korrekten Reaktionen erfasst.");
            return;
        }

        long avgPeakReaction = peakReactions.stream().mapToLong(Long::longValue).sum() / (peakReactions.isEmpty() ? 1 : peakReactions.size());
        long avgValleyReaction = valleyReactions.stream().mapToLong(Long::longValue).sum() / (valleyReactions.isEmpty() ? 1 : valleyReactions.size());

        String result = "Durchschnittliche Reaktionszeiten:\n" +
                "🟢 Hochpunkt: " + avgPeakReaction + " ms\n" +
                "🔵 Tiefpunkt: " + avgValleyReaction + " ms";

        statusText.setText(result);
        System.out.println(result);
    }
}
