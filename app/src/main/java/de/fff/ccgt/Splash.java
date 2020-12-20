package de.fff.ccgt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import static android.util.Log.d;

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

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1234);
        }

        textView = findViewById(R.id.introTextView);
        appendText();
    }

    private void appendText() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                currentString = currentString + intro[currentStringNum] + "\n";
                textView.setText(currentString);;
                currentStringNum++;
                if(currentStringNum < intro.length) {
                    appendText();
                } else {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }

            }
        }, 150);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1234: {
                //If request is cancelled, result arrays ar,e empty
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //TODO: it never happens :-(
                    //tuner.run();
                    d("Splash: " , "thx a lot");
                } else {
                    d("Splash: " , "permission denied by user ....");
                }
                return;
            }
        }
    }

}
