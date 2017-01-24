package com.volgoak.pokertournament;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.volgoak.pokertournament.databinding.ActivityTournamentBinding;

public class TournamentActivity extends AppCompatActivity implements ServiceConnection{

    public static final String TAG = "TournamentActivity";
    //BroadcastReceiver fields
    public static final String RECEIVER_CODE = "com.volgoak.pokertournament.BlindsBroadCastReceiver";
    public static final String TIME_TO_INCREASE = "time_to_increase";
    public static final String CURRENT_BLIND = "current_blind";
    public static final String NEXT_BLIND = "next_blind";
    public static final String CHANGE_STATE_TEXT = "change_state";
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;

    private ActivityTournamentBinding mBinder;

    private BlindTimer mBlindTimer;

    private boolean mStopWasClicked;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tournament);

        mBinder = DataBindingUtil.setContentView(this, R.layout.activity_tournament);
        mReceiver = new MyTimeReceiver();
        mIntentFilter = new IntentFilter(RECEIVER_CODE);

        if(savedInstanceState != null){
            String time = savedInstanceState.getString(TIME_TO_INCREASE);
            mBinder.tvTimeToNextTournament.setText(time);
            String blinds = savedInstanceState.getString(CURRENT_BLIND);
            mBinder.tvCurrentBlindsTourn.setText(blinds);
            String nextBlinds = savedInstanceState.getString(NEXT_BLIND);
            mBinder.tvNextBlindsTour.setText(nextBlinds);
            String stateButton = savedInstanceState.getString(CHANGE_STATE_TEXT);
            mBinder.btPauseTournament.setText(stateButton);
        }

        //set listener for pause/resume button
        mBinder.btPauseTournament.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isPaused = mBlindTimer.changeState();
                if(isPaused)((Button) v).setText(getString(R.string.resume));
                else ((Button) v).setText(R.string.pause);
            }
        });
        mBinder.btPauseTournament.setEnabled(false);
        //set listener for stop button
        mBinder.btEndTournament.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mStopWasClicked){
                    Toast.makeText(TournamentActivity.this, R.string.tap_one_more, Toast.LENGTH_SHORT).show();
                    mStopWasClicked = true;
                }else{
                    mBlindTimer.stop();
                    finish();
                }
            }
        });

        //set font for clock
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/digits_bold.ttf");
        mBinder.tvTimeToNextTournament.setTypeface(font);
        mBinder.tvCurrentBlindsTourn.setTypeface(font);
        mBinder.tvNextBlindsTour.setTypeface(font);
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TIME_TO_INCREASE, mBinder.tvTimeToNextTournament.getText().toString());
        outState.putString(CURRENT_BLIND, mBinder.tvCurrentBlindsTourn.getText().toString());
        outState.putString(NEXT_BLIND, mBinder.tvNextBlindsTour.getText().toString());
        outState.putString(CHANGE_STATE_TEXT, mBinder.btPauseTournament.getText().toString());
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

            String blinds = intent.getStringExtra(CURRENT_BLIND);
            mBinder.tvCurrentBlindsTourn.setText(blinds);

            String nextBlinds = intent.getStringExtra(NEXT_BLIND);
            mBinder.tvNextBlindsTour.setText(nextBlinds);
        }
    }
}
