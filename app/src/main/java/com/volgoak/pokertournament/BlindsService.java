package com.volgoak.pokertournament;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.volgoak.pokertournament.utils.NotificationUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Volgoak on 14.01.2017.
 */

public class BlindsService extends Service {

    public static final String TAG = "BlindsService";

    public static final String START_GAME_ACTION = "start_game";
    public static final String GET_INFO_ACTION = "get_info";
    public static final String START_ACTION = "start_tournament";
    public static final String PAUSE_ACTION = "pause_tournament";
    public static final String RESUME_ACTION = "resume_tournament";
    public static final String STOP_ACTION = "stop_tournament";

    public static final String EXTRA_ROUND_TIME = "round_time";

    private static ExecutorService mExecutor = Executors.newFixedThreadPool(1);
    private static Thread gameThread;

    private static volatile boolean tournamentInProgress;
    //only for test version
    private static String blinds = "5/10";

    private static int round;

    private static long roundTime ;

    private long increaseTime;

    private static long pauseLeftTime;
    private static boolean paused;

    // TODO: 19.01.2017 remove to onstart
    /*protected void onHandleIntent(Intent intent) {
        String taskAction = intent.getAction();
        if (START_GAME_ACTION.equals(taskAction)) {
            Log.d(TAG, "onHandleIntent: " + taskAction);
            runAGame(intent);
        } else if (GET_INFO_ACTION.equals(taskAction)) {
            sendInfo();
        }
    }*/

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String taskAction = intent.getAction();
        Log.d(TAG, "onStartCommand: action " + taskAction);
        if (!tournamentInProgress && START_GAME_ACTION.equals(taskAction)) {
            roundTime = intent.getLongExtra(EXTRA_ROUND_TIME, 0);
            String a = intent.getStringExtra("a");
            Log.d(TAG, "onStartCommand: message is " + a);
            startNewGame(intent);
            return START_REDELIVER_INTENT;
        }else if(PAUSE_ACTION.equals(taskAction)){
            pause();
        }else if(RESUME_ACTION.equals(taskAction)){
            resume();
        }else if(STOP_ACTION.equals(taskAction)){
            stopTournament();
        }
        return  START_STICKY_COMPATIBILITY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        tournamentInProgress = false;
        //super.onDestroy();
    }

    private void stopTournament(){
        tournamentInProgress = false;
        gameThread.interrupt();
        round = 0;
        stopSelf();
    }

    private void pause(){
        pauseLeftTime = increaseTime - System.currentTimeMillis();
        tournamentInProgress = false;
        gameThread.interrupt();
        paused = true;
    }

    private void resume(){
        increaseTime = System.currentTimeMillis() + pauseLeftTime;
        tournamentInProgress = true;
        //mExecutor.execute(new BlindTimer());
        gameThread = new Thread(new BlindTimer());
        gameThread.start();
        paused = false;
    }

    private void startNewGame(Intent intent){
        //test time make changeable
        Log.d(TAG, "startGame: ");
        increaseTime = System.currentTimeMillis() + roundTime;
        // TODO: 19.01.2017 how to save time in case service was destroyed
        tournamentInProgress = true;
        //mExecutor.execute(new BlindTimer());
        gameThread = new Thread(new BlindTimer());
        gameThread.start();
    }

    private void startNextRound(){
        //mExecutor.shutdown();
        round++;
        tournamentInProgress = true;
        Log.d(TAG, "startNextRound: round is " + round);
        blinds = getResources().getStringArray(R.array.blinds_mid)[round];
        increaseTime = System.currentTimeMillis() + roundTime;
        gameThread = new Thread(new BlindTimer());
        gameThread.start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void sendNotification(String notificationType){
        Bundle notBundle = new Bundle();
        //add action - show timer
        String action = NotificationUtil.ROUND_ENDED.equals(notificationType)
                ? NotificationUtil.ROUND_ENDED : NotificationUtil.SHOW_TIMER;
        notBundle.putString(NotificationUtil.EXTRA_ACTION, action);
        //add time to increase
        long timeToIncrease = increaseTime - System.currentTimeMillis();
        notBundle.putLong(NotificationUtil.EXTRA_TIME, timeToIncrease);
        //add info about blinds
        notBundle.putString(NotificationUtil.EXTRA_BLINDS, blinds);

        NotificationUtil.showNotification(this, notBundle);
    }

    private class BlindTimer implements Runnable{
        @Override
        public void run() {
            Log.d(TAG, "run: ");
            long timeToRound = increaseTime - System.currentTimeMillis();
            while (timeToRound > 0){
                if(timeToRound > 1000){
                    try{
                        Thread.sleep(1000);
                    }catch (InterruptedException ex){
                        ex.printStackTrace();
                    }
                    sendNotification(NotificationUtil.SHOW_TIMER);
                    timeToRound = increaseTime - System.currentTimeMillis();
                }else{
                    try{
                        Thread.sleep(timeToRound);
                    }catch (InterruptedException ex){
                        ex.printStackTrace();
                    }
                    sendNotification(NotificationUtil.ROUND_ENDED);
                    tournamentInProgress = false;
                }
            }
            startNextRound();
        }
    }

    private class BlindTimerTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            long timeToRound = increaseTime - System.currentTimeMillis();
            while (timeToRound > 0){
                if(timeToRound > 1000){
                    try{
                        Thread.sleep(1000);
                    }catch (InterruptedException ex){
                        ex.printStackTrace();
                    }
                    publishProgress();
                    timeToRound = increaseTime - System.currentTimeMillis();
                }else{
                    try{
                        Thread.sleep(timeToRound);
                    }catch (InterruptedException ex){
                        ex.printStackTrace();
                    }
                    sendNotification(NotificationUtil.ROUND_ENDED);
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            sendNotification(NotificationUtil.SHOW_TIMER);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            startNextRound();
        }
    }
}

