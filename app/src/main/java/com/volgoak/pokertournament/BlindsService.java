package com.volgoak.pokertournament;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.volgoak.pokertournament.utils.NotificationUtil;

/**
 * Created by Volgoak on 14.01.2017.
 */

public class BlindsService extends Service {

    public static final String TAG = "BlindsService";

    public static final String START_GAME_ACTION = "start_game";
    //public static final String GET_INFO_ACTION = "get_info";

    public static final String EXTRA_ROUND_TIME = "round_time";
    public static final String EXTRA_BLINDS_STRUCTURE = "blinds_structure";
    public static final String EXTRA_START_BLINDS = "start_blinds";
    public static final String EXTRA_START_TIME = "start_time";
    public static final String EXTRA_BLINDS_TYPE = "blinds_type";

    public static final String BLINDS_SLOW = "blinds_slow";
    public static final String BLINDS_MEDIUM = "blinds_medium";
    public static final String BLINDS_FAST = "blinds_fast";

    private static Thread sGameThread;

    private static volatile boolean sTournamentInProgress;

    private static String sBlinds;

    private static int sRoundNum;

    private static int sBlindsResource;

    private static long sRoundTime;
    private static long sIncreaseTime;
    private static long sPauseLeftTime;

    /**
     * For start timer intent param must have action START_GAME_ACTION and include
     * blinds type as a string,
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
            return START_REDELIVER_INTENT;
        }
        return  START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        sTournamentInProgress = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
       return new ITimer();
    }

    private void startNewGame(Intent intent){
        //test time make changeable
        Log.d(TAG, "startGame: ");
        sRoundTime = intent.getLongExtra(EXTRA_ROUND_TIME, 0);
        sIncreaseTime = System.currentTimeMillis() + sRoundTime;
        // TODO: 19.01.2017 how to save time in case service was destroyed
        // TODO: 21.01.2017 decide what is sRoundNum number by start sBlinds

        String blindsType = intent.getStringExtra(EXTRA_BLINDS_TYPE);
        if(BLINDS_SLOW.equals(blindsType))sBlindsResource = R.array.blinds_slow;
        else if(BLINDS_MEDIUM.equals(blindsType)) sBlindsResource = R.array.blinds_mid;
        else if(BLINDS_FAST.equals(blindsType)) sBlindsResource = R.array.blinds_fast;

        sBlinds = getResources().getStringArray(sBlindsResource)[0];

        sTournamentInProgress = true;
        sGameThread = new Thread(new BlindTimerThread());
        sGameThread.start();
    }

    private void startNextRound(){
        //mExecutor.shutdown();
        sRoundNum++;
        sTournamentInProgress = true;
        Log.d(TAG, "startNextRound: sRoundNum is " + sRoundNum);
        sBlinds = getResources().getStringArray(sBlindsResource)[sRoundNum];
        sIncreaseTime = System.currentTimeMillis() + sRoundTime;
        sGameThread = new Thread(new BlindTimerThread());
        sGameThread.start();
    }

    private void stopTournament(){
        sTournamentInProgress = false;
        sGameThread.interrupt();
        sRoundNum = 0;
        stopSelf();
    }

    private void pause(){
        sPauseLeftTime = sIncreaseTime - System.currentTimeMillis();
        sTournamentInProgress = false;
        sGameThread.interrupt();
    }

    private void resume(){
        sIncreaseTime = System.currentTimeMillis() + sPauseLeftTime;
        sTournamentInProgress = true;
        //mExecutor.execute(new BlindTimerThread());
        sGameThread = new Thread(new BlindTimerThread());
        sGameThread.start();
    }



    public void showNotification(String notificationType){
        Bundle notBundle = new Bundle();
        //add action - show timer
        String action = NotificationUtil.ROUND_ENDED.equals(notificationType)
                ? NotificationUtil.ROUND_ENDED : NotificationUtil.SHOW_TIMER;
        notBundle.putString(NotificationUtil.EXTRA_ACTION, action);
        //add time to increase
        long timeToIncrease = sIncreaseTime - System.currentTimeMillis();
        notBundle.putLong(NotificationUtil.EXTRA_TIME, timeToIncrease);
        //add info about sBlinds
        notBundle.putString(NotificationUtil.EXTRA_BLINDS, sBlinds);

        Notification notification = NotificationUtil.createNotification(this, notBundle);
        startForeground(NotificationUtil.NOTIFICATION_COD, notification);
    }

    private class BlindTimerThread implements Runnable{
        @Override
        public void run() {
            Log.d(TAG, "run: ");
            long timeToRound = sIncreaseTime - System.currentTimeMillis();
            while (sTournamentInProgress){
                if(timeToRound > 1000){
                    try{
                        Thread.sleep(1000);
                    }catch (InterruptedException ex){
                        ex.printStackTrace();
                    }
                    showNotification(NotificationUtil.SHOW_TIMER);
                    timeToRound = sIncreaseTime - System.currentTimeMillis();
                }else{
                    try{
                        Thread.sleep(timeToRound);
                    }catch (InterruptedException ex){
                        ex.printStackTrace();
                    }
                    showNotification(NotificationUtil.ROUND_ENDED);
                    sTournamentInProgress = false;
                    startNextRound();
                    break;
                }
            }
        }
    }

    //Binder class with callback methods
    private class ITimer extends Binder implements BlindTimer{
        @Override
        public void pause() {
            BlindsService.this.pause();
        }

        @Override
        public void resume() {
            BlindsService.this.resume();
        }
    }
}

