package com.volgoak.pokertournament;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button buttonStart = (Button) findViewById(R.id.button);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BlindsService.class);
                intent.setAction(BlindsService.START_GAME_ACTION);
                intent.putExtra(BlindsService.EXTRA_ROUND_TIME, 10000L);
                intent.putExtra("a", "massage");
                startService(intent);

            }
        });

        Button buttonStop = (Button) findViewById(R.id.button_stop);
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BlindsService.class);
                stopService(intent);
            }
        });


        Button buttonPause = (Button) findViewById(R.id.bt_pause);
        buttonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BlindsService.class);
                intent.setAction(BlindsService.PAUSE_ACTION);
                startService(intent);
            }
        });

        Button buttonResume = (Button) findViewById(R.id.bt_resume);
        buttonResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BlindsService.class);
                intent.setAction(BlindsService.RESUME_ACTION);
                startService(intent);
            }
        });

    }
}
