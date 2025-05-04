package com.example.vib;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ReactionTimeActivity extends AppCompatActivity {
    private Vibrator vibrator;
    private Handler handler = new Handler();
    private Random random = new Random();
    private long vibrationStartTime;
    private long wifiSendTime;
    private boolean waitingForReaction = false;
    private int remainingVibrations = 30; // Anzahl der Vibrationen pro Testlauf
    private List<Long> reactionTimes = new ArrayList<>(); // Speicherung der Reaktionszeiten
    private List<Long> latencyTimes = new ArrayList<>(); // Speicherung der Wi-Fi-Latenzen
    private Button backButton;
    private Runnable reactionTimeoutRunnable;
    private static final long MAX_REACTION_TIME = 5000; // 5 Sekunden

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reaction_time);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        backButton = findViewById(R.id.backButton); // Speichere den Zurück-Button

        backButton.setOnClickListener(v -> finish()); // Standard-Zurück-Funktion
        findViewById(R.id.startTestButton).setOnClickListener(v -> startTest());
    }

    private void startTest() {
        reactionTimes.clear();
        latencyTimes.clear();
        remainingVibrations = 30;
        backButton.setVisibility(View.GONE); // Zurück-Button ausblenden
        scheduleNextVibration();
        findViewById(R.id.startTestButton).setEnabled(false);
    }

    private void scheduleNextVibration() {
        if (remainingVibrations <= 0) {
            finishTest();
            return;
        }

        long delay = random.nextInt(9990) + 15; // Zufällige Pause (10 ms – 15 s)

        handler.postDelayed(() -> {
            if (vibrator != null) {
                wifiSendTime = SystemClock.elapsedRealtime();
                VibrationEffect effect = VibrationEffect.createOneShot(200, 250);
                vibrator.vibrate(effect);

                // WiFi-Latenz messen
                vibrationStartTime = SystemClock.elapsedRealtime();
                long latency = vibrationStartTime - wifiSendTime;
                latencyTimes.add(latency);
                Log.d("ReactionTimeTest", "Wi-Fi Latenz: " + latency + " ms");
                waitingForReaction = true;

                // Timeout für maximale Reaktionszeit setzen
                reactionTimeoutRunnable = () -> {
                    if (waitingForReaction) {
                        Log.d("ReactionTimeTest", "Keine Reaktion innerhalb von 5 Sekunden");
                        reactionTimes.add(MAX_REACTION_TIME);
                        waitingForReaction = false;
                        remainingVibrations--;
                        scheduleNextVibration();
                    }
                };
                handler.postDelayed(reactionTimeoutRunnable, MAX_REACTION_TIME);
            }
        }, delay);
    }

    private void recordReactionTime() {
        if (waitingForReaction) {
            long reactionTime = SystemClock.elapsedRealtime() - vibrationStartTime;
            reactionTimes.add(reactionTime);
            Log.d("ReactionTimeTest", "Reaktion: " + reactionTime + " ms");

            waitingForReaction = false;
            remainingVibrations--;
            handler.removeCallbacks(reactionTimeoutRunnable); // Timeout verhindern, wenn rechtzeitig reagiert wurde
            scheduleNextVibration();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            recordReactionTime(); // Reaktion auf beliebige Berührung registrieren
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void finishTest() {
        Log.d("ReactionTimeTest", "Alle Reaktionszeiten: " + reactionTimes.toString());
        Log.d("ReactionTimeTest", "Alle Wi-Fi Latenzen: " + latencyTimes.toString());

        // durchschnittliche Reaktionszeit berechnen
        if (!reactionTimes.isEmpty()) {
            long sum = 0;
            for (long time : reactionTimes) {
                sum += time;
            }
            long averageReactionTime = sum / reactionTimes.size();
            Log.d("ReactionTimeTest", "Durchschnittliche Reaktionszeit: " + averageReactionTime + " ms");
        } else {
            Log.d("ReactionTimeTest", "Keine gültigen Reaktionszeiten erfasst.");
        }

        // durchschnittliche WiFi-Latenz berechnen
        if (!latencyTimes.isEmpty()) {
            long sum = 0;
            for (long time : latencyTimes) {
                sum += time;
            }
            long averageLatency = sum / latencyTimes.size();
            Log.d("ReactionTimeTest", "Durchschnittliche Wi-Fi Latenz: " + averageLatency + " ms");
        } else {
            Log.d("ReactionTimeTest", "Keine gültigen Wi-Fi Latenzen erfasst.");
        }

        backButton.setVisibility(View.VISIBLE); // Zurück-Button wieder anzeigen
        findViewById(R.id.startTestButton).setEnabled(true);
    }

}

