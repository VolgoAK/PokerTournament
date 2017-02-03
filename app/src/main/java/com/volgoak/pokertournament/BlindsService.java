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

    private Thread mGameThread;

    private volatile boolean mTournamentInProgress;
    private boolean mIsBound;

    private String mBlinds;

    private int mRoundNum;

    private long mRoundTime;
    private long mIncreaseTime;
    private long mPauseLeftTime;

    private String[] mBlindsArray;

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
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String taskAction = intent.getAction();
        Log.d(TAG, "onStartCommand: action " + taskAction);
        if (!mTournamentInProgress && START_GAME_ACTION.equals(taskAction)) {
            startNewGame(intent);
            return START_STICKY;
        }
        return  START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mTournamentInProgress = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        mIsBound = true;
       return new ITimer();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: ");
        mIsBound = false;
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind: ");
        mIsBound = true;
    }

    private void startNewGame(Intent intent){
        mWakeLock.acquire();
        Log.d(TAG, "startGame: ");
        mRoundTime = intent.getLongExtra(EXTRA_ROUND_TIME, 0);
        mIncreaseTime = SystemClock.elapsedRealtime() + mRoundTime;

        mBlindsArray = intent.getStringArrayExtra(EXTRA_BLINDS_ARRAY);
        mBlinds = mBlindsArray[0];

        mTournamentInProgress = true;
        mGameThread = new Thread(new BlindTimerThread());
        mGameThread.start();
    }

    private void startNextRound(){
        mRoundNum++;
        Log.d(TAG, "startNextRound: mRoundNum is " + mRoundNum);
        mBlinds = mBlindsArray[mRoundNum];
        mIncreaseTime = SystemClock.elapsedRealtime() + mRoundTime;
    }

    // returns true if state was changed to pause,
    private boolean changeState(){
        if(mTournamentInProgress){
            mPauseLeftTime = mIncreaseTime - SystemClock.elapsedRealtime();
            mTournamentInProgress = false;
            mGameThread.interrupt();
            mWakeLock.release();
            return true;
        }else{
            mIncreaseTime = SystemClock.elapsedRealtime() + mPauseLeftTime;
            mTournamentInProgress = true;
            //mExecutor.execute(new BlindTimerThread());
            mGameThread = new Thread(new BlindTimerThread());
            mGameThread.start();
            mWakeLock.acquire();
            return false;
        }
    }

    private void stop(){
        mTournamentInProgress = false;
        mRoundNum = 0;
        if(mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        stopSelf();
    }

    private boolean isPaused(){
        return !mTournamentInProgress;
    }

    private void notifyTimer(){
        long timeToIncrease = mIncreaseTime - SystemClock.elapsedRealtime();
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
        long timeToIncrease = mIncreaseTime - SystemClock.elapsedRealtime();
        String action = timeToIncrease < 0
                ? NotificationUtil.ROUND_ENDED : NotificationUtil.SHOW_TIMER;
        notBundle.putString(NotificationUtil.EXTRA_ACTION, action);
        //add time to increase

        notBundle.putLong(NotificationUtil.EXTRA_TIME, timeToIncrease);
        //add info about mBlinds
        notBundle.putString(NotificationUtil.EXTRA_BLINDS, mBlinds);

        Notification notification = NotificationUtil.createNotification(this, notBundle);
        startForeground(NotificationUtil.NOTIFICATION_COD, notification);

        //Send info to tournament activity if exists
        if(mIsBound){
            String timeMessage = NotificationUtil.parseTime(timeToIncrease);
            Intent intent = new Intent(TournamentActivity.RECEIVER_CODE);
            intent.putExtra(TournamentActivity.TIME_TO_INCREASE, timeMessage);
            intent.putExtra(TournamentActivity.CURRENT_BLIND, mBlindsArray[mRoundNum]);
            intent.putExtra(TournamentActivity.NEXT_BLIND, mBlindsArray[mRoundNum + 1]);
            sendBroadcast(intent);
        }
    }

    private class BlindTimerThread implements Runnable{
        @Override
        public void run() {
            Log.d(TAG, "run: ");
            while (mTournamentInProgress){
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

