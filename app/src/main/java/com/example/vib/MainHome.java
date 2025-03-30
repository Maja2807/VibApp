package com.example.vib;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainHome extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainhome);

        // Training
        Button vibrationButton = findViewById(R.id.Test1);
        vibrationButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainHome.this, home.class);
            startActivity(intent);
        });

        // Reaktionszeittest
        Button reactionTestButton = findViewById(R.id.reactionTestButton);
        reactionTestButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainHome.this, ReactionTimeActivity.class);
            startActivity(intent);
        });

        // Interpretationstest
        Button interpretationButton = findViewById(R.id.interpretationTestButton);
        interpretationButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainHome.this, InterpretationModeActivity.class);
            startActivity(intent);
        });

        // Kombi
        Button kombiButton = findViewById(R.id.kombiButton);
        kombiButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainHome.this, KombiActivity.class);
            startActivity(intent);
        });

    }
}
