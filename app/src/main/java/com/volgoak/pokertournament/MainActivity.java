package com.volgoak.pokertournament;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements ServiceConnection{

    private BlindTimer mTimer;

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
                intent.putExtra(BlindsService.EXTRA_BLINDS_TYPE, BlindsService.BLINDS_SLOW);
                startService(intent);

                bindService(new Intent(MainActivity.this, BlindsService.class), MainActivity.this, Context.BIND_AUTO_CREATE);

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
        buttonPause.setEnabled(false);
        buttonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimer.pause();
            }
        });

        Button buttonResume = (Button) findViewById(R.id.bt_resume);
        buttonResume.setEnabled(false);
        buttonResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimer.resume();
            }
        });
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mTimer = (BlindTimer) service;
        findViewById(R.id.bt_pause).setEnabled(true);
        findViewById(R.id.bt_resume).setEnabled(true);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mTimer = null;
    }


}
