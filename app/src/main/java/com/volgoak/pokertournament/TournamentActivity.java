package com.volgoak.pokertournament;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.volgoak.pokertournament.databinding.ActivityTournamentBinding;

public class TournamentActivity extends AppCompatActivity implements ServiceConnection{

    public static final String TAG = "TournamentActivity";
    //BroadcastReceiver fields
    public static final String RECEIVER_CODE = "com.volgoak.pokertournament.BlindsBroadCastReceiver";
    public static final String TIME_TO_INCREASE = "time_to_increase";
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;

    private ActivityTournamentBinding mBinder;

    private BlindTimer mBlindTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tournament);

        mBinder = DataBindingUtil.setContentView(this, R.layout.activity_tournament);

        mReceiver = new MyTimeReceiver();
        mIntentFilter = new IntentFilter(RECEIVER_CODE);

        //set listener for pause/resume button
        mBinder.btPauseTournament.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String buttonText =((Button) v).getText().toString();
                if(buttonText.equals(getString(R.string.pause))) ((Button) v).setText(getString(R.string.resume));
                else if (buttonText.equals(getString(R.string.resume))) ((Button) v).setText(R.string.pause);
                mBlindTimer.changeState();
            }
        });
        mBinder.btPauseTournament.setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        Intent intent = new Intent(this, BlindsService.class);
        boolean binded = bindService(intent, this, 0);
        Log.d(TAG, "onResume: binded is " + binded);

        mReceiver = new MyTimeReceiver();
        mIntentFilter = new IntentFilter(RECEIVER_CODE);
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause(){
        super.onPause();
        unbindService(this);
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "onServiceConnected: " + service);
        mBlindTimer = (BlindTimer) service;
        mBinder.btPauseTournament.setEnabled(true);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "onServiceDisconnected: ");
        mBlindTimer = null;
    }

    private void disconnect(){
        mBlindTimer = null;
    }

    private class MyTimeReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String time = intent.getStringExtra(TIME_TO_INCREASE);
            Log.d(TAG, "onReceive: time " + time);
            mBinder.tvTimeToNextTournament.setText(time);
        }
    }
}
