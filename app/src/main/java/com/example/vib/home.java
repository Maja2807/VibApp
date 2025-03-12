package com.example.vib;

import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class home extends AppCompatActivity {
    private TextView text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);     // das sagt, welche xml Datei hier geladen werden soll
        text = findViewById(R.id.text);     // das nimmt den Text, den wir anzeigen wollen
        text.setText("Test2jhgodfdklrfndsjfjdsjkfn.kjdfdjkfgnjkdnjkvndjvjkdjkgvjkdfjkdbnjkgvbnjkdxbgv.jkdxjk.fgvjkdxfgdf"); //setText updated das, was in der xml Datei bei "android:text" steht, also das was angezeigt wird
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        long[] vibrationPattern = {0, 500, 50, 300};
        //-1 - don't repeat
        final int indexInPatternToRepeat = -1;
        vibrator.vibrate(vibrationPattern, indexInPatternToRepeat);
    }

}
