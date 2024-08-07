
package de.fff.ccgt.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.util.Log.d;

import de.fff.ccgt.R;
import de.fff.ccgt.activity.MainActivity;

public class Splash extends AppCompatActivity {

    TextView textView;
    int currentStringNum = 0;
    String currentString = "";

    String[] intro = {
            "               _   ",
            "  ___ ___ __ _| |_ ",
            " / __/ __/ _` | __|",
            "| (_| (_| (_| | |_ ",
            " \\___\\___\\__, |\\__|",
            "         |___/     ",
            "                   ",
            "                   ",
            "                   ",
            "                   ",
            "                   "
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getSupportActionBar().setTitle(R.string.title_action_bar);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1234);
        } else {
            textView = findViewById(R.id.introTextView);
            appendText();
        }


    }

    private void appendText() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                currentString = currentString + intro[currentStringNum] + "\n";
                textView.setText(currentString);
                currentStringNum++;
                if(currentStringNum < intro.length) {
                    appendText();
                } else {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }

            }
        }, 50);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1234: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    textView = findViewById(R.id.introTextView);
                    appendText();
                } else {
                    d("Splash: ", "permission denied by user ....");
                }
                return;
            }
        }
    }

}
