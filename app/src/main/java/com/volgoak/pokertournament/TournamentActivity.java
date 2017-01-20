package com.volgoak.pokertournament;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.volgoak.pokertournament.databinding.ActivityTournamentBinding;

public class TournamentActivity extends AppCompatActivity {

    //BroadcastReceiver fields
    public static final String RECEIVER_CODE = "com.volgoak.pokertournament.BlindsBroadCastReceiver";
    public static final String TIME_TO_INCREASE = "time_to_increase";
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;

    private ActivityTournamentBinding mBinder;

    private AsyncTask<Long, Integer, Void> mAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tournament);

        mBinder = DataBindingUtil.setContentView(this, R.layout.activity_tournament);

        mReceiver = new BlindsBroadCastReceiver();
        mIntentFilter = new IntentFilter(RECEIVER_CODE);

        Intent intent = new Intent(TournamentActivity.this, BlindsService.class);
        intent.setAction(BlindsService.GET_INFO_ACTION);
        startService(intent);
    }

    @Override
    public void onResume(){
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
        mAsyncTask = new AsyncTask<Long, Integer, Void>() {
            long aTime;
            @Override
            protected Void doInBackground(Long... params) {
                aTime = params[0];
                Log.d("AsyncTask", "do in background with time " + aTime);
                while (aTime > 0){
                    aTime--;
                    int newTime = (int) aTime;
                    publishProgress(newTime);
                    try{
                        Thread.sleep(1000);
                    }catch (InterruptedException ex){
                        ex.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                Log.d("AsyncTask", "onProgressUpdate " + values[0]);
                showTime(values[0]);
            }
        };
    }

    @Override
    public void onPause(){
        super.onPause();
        unregisterReceiver(mReceiver);
        mAsyncTask.cancel(true);
    }

    private void showTime(int time){
        int minute = time/60;
        int seconds = time % 60;


        String timeToIncrease = String.format("%02d:%02d", minute, seconds);
        Log.d("ShowTime", "time " + timeToIncrease);

        mBinder.tvTimeTournament.setText(timeToIncrease);
    }

    private void startTimer(long time){
        mAsyncTask.execute(time);
    }

    class BlindsBroadCastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {

            long increaseTime = intent.getLongExtra(TIME_TO_INCREASE, 0);
            long timeToincrease = (increaseTime - System.currentTimeMillis())/1000;
            Log.d("onReceive", "increase time " + increaseTime);
            Log.d("onReceive", "time to increase " + timeToincrease);
            startTimer(timeToincrease / 1000);
        }
    }
}
