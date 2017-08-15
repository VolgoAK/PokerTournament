package com.volgoak.pokertournament;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.volgoak.pokertournament.databinding.ActivityTournamentBinding;

public class TournamentActivity extends AppCompatActivity implements ServiceConnection {

    public static final String TAG = "TournamentActivity";
    //BroadcastReceiver fields
    public static final String RECEIVER_CODE = "com.volgoak.pokertournament.BlindsBroadCastReceiver";
    public static final String TIME_TO_INCREASE = "time_to_increase";
    public static final String CURRENT_BLIND = "current_blind";
    public static final String NEXT_BLIND = "next_blind";

    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;

    private ActivityTournamentBinding mBinder;

    private BlindTimer mBlindTimer;

    private boolean mStopWasClicked;

    private AdView mAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tournament);

        mBinder = DataBindingUtil.setContentView(this, R.layout.activity_tournament);
        mReceiver = new MyTimeReceiver();
        mIntentFilter = new IntentFilter(RECEIVER_CODE);

        if (savedInstanceState != null) {
            String time = savedInstanceState.getString(TIME_TO_INCREASE);
            mBinder.tvTimeToNextTournament.setText(time);
            String blinds = savedInstanceState.getString(CURRENT_BLIND);
            mBinder.tvCurrentBlindsTourn.setText(blinds);
            String nextBlinds = savedInstanceState.getString(NEXT_BLIND);
            mBinder.tvNextBlindsTour.setText(nextBlinds);
//            String stateButton = savedInstanceState.getString(CHANGE_STATE_TEXT);
//            mBinder.btPauseTournament.setText(stateButton);
        }

        //set listener for pause/resume button
        mBinder.btPauseTournament.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isPaused = mBlindTimer.changeState();
                if (isPaused) ((FloatingActionButton) v).setImageResource(R.drawable.ic_play_arrow_black_24dp);
                else ((FloatingActionButton) v).setImageResource(R.drawable.ic_pause_black_24dp);
            }
        });
        mBinder.btPauseTournament.setEnabled(false);
        //set listener for stop button
        mBinder.btEndTournament.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryToStopService();
            }
        });
        mBinder.btEndTournament.setEnabled(false);

        //set font for clock
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/digits_bold.ttf");
        mBinder.tvTimeToNextTournament.setTypeface(font);
        mBinder.tvCurrentBlindsTourn.setTypeface(font);
        mBinder.tvNextBlindsTour.setTypeface(font);

        // TODO: 07.06.2017 replace with my ads id before publish
        /*MobileAds.initialize(this, "ca-app-pub-3940256099942544/6300978111");
        AdRequest request = new  AdRequest.Builder().build();
        mAdView = (AdView) findViewById(R.id.adView);
        mAdView.loadAd(request);*/

    }

    @Override
    protected void onResume() {
        super.onResume();
        //bind to BlindsService
        Intent intent = new Intent(this, BlindsService.class);
        boolean binded = bindService(intent, this, 0);

        //register receiver for obtain data from service
        mReceiver = new MyTimeReceiver();
        mIntentFilter = new IntentFilter(RECEIVER_CODE);
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onBackPressed() {
        tryToStopService();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TIME_TO_INCREASE, mBinder.tvTimeToNextTournament.getText().toString());
        outState.putString(CURRENT_BLIND, mBinder.tvCurrentBlindsTourn.getText().toString());
        outState.putString(NEXT_BLIND, mBinder.tvNextBlindsTour.getText().toString());
        // TODO: 29.07.2017 save image for pause button
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "onServiceConnected: " + service);
        mBlindTimer = (BlindTimer) service;
        mBinder.btPauseTournament.setEnabled(true);
        mBinder.btEndTournament.setEnabled(true);

        //get info from service and show it.
        //When app is resumed and timer service in a pause state
        //it won't send data. So we need to fetch it manually.
//        TournamentData td = mBlindTimer.get
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "onServiceDisconnected: ");
        mBlindTimer = null;
        mBinder.btPauseTournament.setEnabled(false);
        mBinder.btEndTournament.setEnabled(false);
    }

    //call when stop button clicked. If clicked second time in five seconds it stops service
    //else run timer
    private void tryToStopService(){
        if (!mStopWasClicked) {
            Toast.makeText(TournamentActivity.this, R.string.tap_one_more, Toast.LENGTH_SHORT).show();
            mStopWasClicked = true;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        Thread.sleep(5000);
                    }catch(InterruptedException ex){
                        ex.printStackTrace();
                    }
                    mStopWasClicked = false;
                }
            });
            thread.start();
        } else {
            mBlindTimer.stop();
            Intent intent = new Intent(TournamentActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    private class MyTimeReceiver extends BroadcastReceiver {
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
