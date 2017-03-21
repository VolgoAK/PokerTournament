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

import java.util.ArrayList;

/**
 * Service maintains lifecycle of tournament.
 * It shows notification with time to next round
 * and current blinds.
 * Also it runs sound notification when blinds increases
 */

public class BlindsService extends Service {

    public static final String TAG = "BlindsService";

    public static final String START_GAME_ACTION = "start_game";

    //Constants for Intent extra
    public static final String EXTRA_ROUND_TIME = "round_time";
    //public static final String EXTRA_START_BLINDS = "start_blinds";
    public static final String EXTRA_BLINDS_ARRAY = "blinds_array";
    public static final String EXTRA_START_ROUND = "start_round";

    //Thread for countdown clock
    private Thread mGameThread;

    //Timer is ticking while true
    private volatile boolean mTournamentInProgress;

    //Is service bound to an activity
    private boolean mIsBound;

    //Blinds stored in array of Strings, so we need to know
    //num of round to pick correct string
    private int mRoundNum;

    //Time for round in millis
    private long mRoundTime;

    //Time to increase blinds
    private long mIncreaseTime;

    //When game paused service save time
    private long mPauseLeftTime;

    //All blinds are stored here
    private ArrayList<String> mBlindsList;

    //Current blinds
    private String mBlinds;

    //Service use WakeLock for don't allow system
    //to sleep
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
        // TODO: 21.03.2017 fix error with nullpointexception on intent
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

    /**
    * Returns binder when Activity call BindService()
     */
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
        //doesn't allow system go to sleep mode
        mWakeLock.acquire();
        Log.d(TAG, "startGame: ");

        //data from intent
        mRoundTime = intent.getLongExtra(EXTRA_ROUND_TIME, 0);
        mBlindsList = intent.getStringArrayListExtra(EXTRA_BLINDS_ARRAY);
        mRoundNum = intent.getIntExtra(EXTRA_START_ROUND, -1);

        //set increase time and blinds
        startNextRound();
        //start game thread
        mTournamentInProgress = true;
        mGameThread = new Thread(new BlindTimerThread());
        mGameThread.start();
    }

    /**
     * Increases blinds and renew time
     */
    private void startNextRound(){
        // TODO: 21.03.2017 fix error when no more blinds in an array
        mRoundNum++;
        Log.d(TAG, "startNextRound: mRoundNum is " + mRoundNum);
        mBlinds = mBlindsList.get(mRoundNum);

        if(mRoundNum == mBlindsList.size() - 1) produceBlinds();

        mIncreaseTime = SystemClock.elapsedRealtime() + mRoundTime;
    }

    /**
     * Creates new blinds when current is last in a list.
     * It produses kind of useless blinds and need only
     * for avoid IndexOfBoundException
     */
    private void produceBlinds(){
        String[] blinds = mBlindsList.get(mBlindsList.size() - 1).split("/");
        int smallBlind = Integer.parseInt(blinds[0]) * 2;
        int bigBlind = smallBlind * 2;
        String newBlinds = smallBlind + "/" + bigBlind;
        mBlindsList.add(newBlinds);
    }

    /**
     * Change state to pause and to active
     * @return true if state changed to pause, false if to active
    */
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

    /**
     * destroy service
     */
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

        //Send info to tournament activity if service is bound
        if(mIsBound){
            String timeMessage = NotificationUtil.parseTime(timeToIncrease);
            Intent intent = new Intent(TournamentActivity.RECEIVER_CODE);
            intent.putExtra(TournamentActivity.TIME_TO_INCREASE, timeMessage);
            intent.putExtra(TournamentActivity.CURRENT_BLIND, mBlinds);
            intent.putExtra(TournamentActivity.NEXT_BLIND, mBlindsList.get(mRoundNum + 1));
            sendBroadcast(intent);
        }
    }

    /**
     * Simple runnable class which call notifyTimer()
     * every one second
     */
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

