package com.volgoak.pokertournament;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.volgoak.pokertournament.data.Blind;
import com.volgoak.pokertournament.data.Structure;
import com.volgoak.pokertournament.utils.BlindEvent;
import com.volgoak.pokertournament.utils.ControlEvent;
import com.volgoak.pokertournament.utils.NotificationUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

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
    public static final String EXTRA_START_ROUND = "start_round";
    public static final String EXTRA_STRUCTURE = "structure_extra";

    //Thread for countdown clock
    private Thread mGameThread;

    //Timer is ticking while true
    private volatile boolean mTournamentInProgress;

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
    private List<Blind> mBlindsList;

    //Current blinds
    private String mBlinds;

    private Structure mStructure;

    //Service uses a WakeLock for don't allow system
    //to sleep
    private PowerManager.WakeLock mWakeLock;

    @Override
    public void onCreate() {
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My log");
        EventBus.getDefault().register(this);
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
        EventBus.getDefault().unregister(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startNewGame(Intent intent){
        //doesn't allow system go to sleep mode
        mWakeLock.acquire();

        //data from intent
        mRoundTime = intent.getLongExtra(EXTRA_ROUND_TIME, 0);
        mStructure = (Structure) intent.getSerializableExtra(EXTRA_STRUCTURE);
        mRoundNum = intent.getIntExtra(EXTRA_START_ROUND, -1);

        mBlindsList = mStructure.getBlinds();

        //set increase time and blinds
        startNextRound();
        //start game thread
        mTournamentInProgress = true;
        mGameThread = new Thread(new BlindTimerRunnable());
        mGameThread.start();
    }

    @Subscribe
    public void onStateEvent(ControlEvent event) {
        if(event.type == ControlEvent.Type.STOP) {
            stop();
        } else if(event.type == ControlEvent.Type.CHANGE_STATE) {
            changeState();
        }
    }

    /**
     * Increases blinds and renew time
     */
    private void startNextRound(){
        // TODO: 21.03.2017 create new blinds smartly
        mRoundNum++;
        Log.d(TAG, "startNextRound: mRoundNum is " + mRoundNum);
        mBlinds = mBlindsList.get(mRoundNum).toString();

        if(mRoundNum == mBlindsList.size() - 1) produceBlinds();

        mIncreaseTime = SystemClock.elapsedRealtime() + mRoundTime;
    }

    /**
     * Creates new blinds when current is last in a list.
     * It produses kind of useless blinds and need only
     * for avoid IndexOfBoundException
     */
    private void produceBlinds(){
        Blind currentBlind = mBlindsList.get(mBlindsList.size() - 1);
        int smallBlind = currentBlind.getSb() * 2;
        int bigBlind = smallBlind * 2;
        Blind newBlind = new Blind();
        newBlind.setSb(smallBlind);
        newBlind.setBb(bigBlind);
        mBlindsList.add(newBlind);
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
            notifyTimer();
            return true;
        }else{
            mIncreaseTime = SystemClock.elapsedRealtime() + mPauseLeftTime;
            mTournamentInProgress = true;
            //mExecutor.execute(new BlindTimerRunnable());
            mGameThread = new Thread(new BlindTimerRunnable());
            mGameThread.start();
            mWakeLock.acquire();
            notifyTimer();
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

        String channel = timeToIncrease < 0
                ? NotificationUtil.CHANNEL_ID_IMPORTANT : NotificationUtil.CHANNEL_ID_SILENT;

        Notification notification = NotificationUtil.createNotification(this, notBundle, channel);
        startForeground(NotificationUtil.NOTIFICATION_COD, notification);

        Blind currentBlind = mBlindsList.get(mRoundNum);
        Blind nextBlind = mBlindsList.get(mRoundNum + 1);
        BlindEvent event = new BlindEvent(currentBlind, nextBlind, timeToIncrease, mTournamentInProgress);
        EventBus.getDefault().postSticky(event);
    }

    /**
     * Simple runnable class which call notifyTimer()
     * every one second
     */
    private class BlindTimerRunnable implements Runnable{
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
}

