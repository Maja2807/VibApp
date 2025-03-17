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
    private boolean waitingForReaction = false;
    private int remainingVibrations = 10; // Anzahl der Vibrationen pro Testlauf
    private List<Long> reactionTimes = new ArrayList<>(); // Speicherung der Reaktionszeiten
    private Button backButton;

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
        remainingVibrations = 10;
        backButton.setVisibility(View.GONE); // Zurück-Button ausblenden
        scheduleNextVibration();
        findViewById(R.id.startTestButton).setEnabled(false);
    }

    private void scheduleNextVibration() {
        if (remainingVibrations <= 0) {
            finishTest();
            return;
        }

        long delay = random.nextInt(9990) + 10; // Zufällige Pause (10 ms – 10 s)

        handler.postDelayed(() -> {
            if (vibrator != null) {
                VibrationEffect effect = VibrationEffect.createOneShot(50, 128);
                vibrator.vibrate(effect);
                vibrationStartTime = SystemClock.elapsedRealtime();
                waitingForReaction = true;
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

        backButton.setVisibility(View.VISIBLE); // Zurück-Button wieder anzeigen
        findViewById(R.id.startTestButton).setEnabled(true);
    }

}
