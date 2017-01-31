package com.volgoak.pokertournament;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.volgoak.pokertournament.utils.NotificationUtil;

/**
 * Created by Volgoak on 14.01.2017.
 */

public class BlindsService extends Service {

    public static final String TAG = "BlindsService";

    public static final String START_GAME_ACTION = "start_game";

    public static final String EXTRA_ROUND_TIME = "round_time";
    public static final String EXTRA_BLINDS_STRUCTURE = "blinds_structure";
    public static final String EXTRA_START_BLINDS = "start_blinds";
    public static final String EXTRA_BLINDS_ARRAY = "blinds_array";

    private static Thread sGameThread;

    private static volatile boolean sTournamentInProgress;
    private static boolean binded;

    private static String sBlinds;

    private static int sRoundNum;

    private static long sRoundTime;
    private static long sIncreaseTime;
    private static long sPauseLeftTime;

    private static String[] sBlindsArray;

    private PowerManager.WakeLock mWakeLock;

    @Override
    public void onCreate() {
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My log");
    }

    /**
     * For start timer intent param must have action START_GAME_ACTION and include
     * blinds as an array of strings,
     * round time as a long
     * start time as a long
     * start blind as a string
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String taskAction = intent.getAction();
        Log.d(TAG, "onStartCommand: action " + taskAction);
        if (!sTournamentInProgress && START_GAME_ACTION.equals(taskAction)) {
            startNewGame(intent);
            return START_STICKY;
        }
        return  START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        sTournamentInProgress = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        binded = true;
       return new ITimer();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: ");
        binded = false;
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind: ");
        binded = true;
    }

    private void startNewGame(Intent intent){
        mWakeLock.acquire();
        Log.d(TAG, "startGame: ");
        sRoundTime = intent.getLongExtra(EXTRA_ROUND_TIME, 0);
        sIncreaseTime = SystemClock.elapsedRealtime() + sRoundTime;
        // TODO: 19.01.2017 how to save time in case service was destroyed
        // TODO: 21.01.2017 decide what is sRoundNum number by start sBlinds

        sBlindsArray = intent.getStringArrayExtra(EXTRA_BLINDS_ARRAY);
        sBlinds = sBlindsArray[0];

        sTournamentInProgress = true;
        sGameThread = new Thread(new BlindTimerThread());
        sGameThread.start();
    }

    private void startNextRound(){
        //mExecutor.shutdown();
        sRoundNum++;
        Log.d(TAG, "startNextRound: sRoundNum is " + sRoundNum);
        sBlinds = sBlindsArray[sRoundNum];
        sIncreaseTime = SystemClock.elapsedRealtime() + sRoundTime;
    }

    // returns true if state was changed to pause,
    private boolean changeState(){
        if(sTournamentInProgress){
            sPauseLeftTime = sIncreaseTime - SystemClock.elapsedRealtime();
            sTournamentInProgress = false;
            sGameThread.interrupt();
            mWakeLock.release();
            return true;
        }else{
            sIncreaseTime = SystemClock.elapsedRealtime() + sPauseLeftTime;
            sTournamentInProgress = true;
            //mExecutor.execute(new BlindTimerThread());
            sGameThread = new Thread(new BlindTimerThread());
            sGameThread.start();
            mWakeLock.acquire();
            return false;
        }
    }

    private void stop(){
        sTournamentInProgress = false;
        sRoundNum = 0;
        mWakeLock.release();
        stopSelf();
    }

    private boolean isPaused(){
        return !sTournamentInProgress;
    }

    private void notifyTimer(){
        long timeToIncrease = sIncreaseTime - SystemClock.elapsedRealtime();
        showNotification();
        if(timeToIncrease < 0) startNextRound();
    }

    /**
     * Creates notification with timer info
     * Calls startForeground method which doesn't allow Android to
     * kill service
     */
    public void showNotification(){
        Bundle notBundle = new Bundle();
        //add action - show timer
        long timeToIncrease = sIncreaseTime - SystemClock.elapsedRealtime();
        String action = timeToIncrease < 0
                ? NotificationUtil.ROUND_ENDED : NotificationUtil.SHOW_TIMER;
        notBundle.putString(NotificationUtil.EXTRA_ACTION, action);
        //add time to increase

        notBundle.putLong(NotificationUtil.EXTRA_TIME, timeToIncrease);
        //add info about sBlinds
        notBundle.putString(NotificationUtil.EXTRA_BLINDS, sBlinds);

        Notification notification = NotificationUtil.createNotification(this, notBundle);
        startForeground(NotificationUtil.NOTIFICATION_COD, notification);

        //Send info to tournament activity if exists
        if(binded){
            String timeMessage = NotificationUtil.parseTime(timeToIncrease);
            Intent intent = new Intent(TournamentActivity.RECEIVER_CODE);
            intent.putExtra(TournamentActivity.TIME_TO_INCREASE, timeMessage);
            intent.putExtra(TournamentActivity.CURRENT_BLIND, sBlindsArray[sRoundNum]);
            intent.putExtra(TournamentActivity.NEXT_BLIND, sBlindsArray[sRoundNum + 1]);
            sendBroadcast(intent);
        }
    }

    private class BlindTimerThread implements Runnable{
        @Override
        public void run() {
            Log.d(TAG, "run: ");
            while (sTournamentInProgress){
                    try{
                        Thread.sleep(1000);
                    }catch (InterruptedException ex){
                        ex.printStackTrace();
                    }
                    notifyTimer();
            }
        }
    }

    //Binder class with callback methods
    private class ITimer extends Binder implements BlindTimer{
       public boolean changeState(){
          return BlindsService.this.changeState();
       }

       public void stop(){
           BlindsService.this.stop();
       }

    }
}

