package com.volgoak.pokertournament

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log

import com.volgoak.pokertournament.data.Blind
import com.volgoak.pokertournament.data.Structure
import com.volgoak.pokertournament.utils.BlindEvent
import com.volgoak.pokertournament.utils.ControlEvent
import com.volgoak.pokertournament.utils.NotificationUtil

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * Service maintains lifecycle of tournament.
 * It shows notification with time to next round
 * and current blinds.
 * Also it runs sound notification when blinds increases
 */

class BlindsService : Service() {

    //Thread for countdown clock
    private var mGameThread: Thread? = null

    //Timer is ticking while true
    @Volatile
    private var mTournamentInProgress: Boolean = false

    //Blinds stored in array of Strings, so we need to know
    //num of round to pick correct string
    private var mRoundNum: Int = 0

    //Time for round in millis
    private var mRoundTime: Long = 0

    //Time to increase blinds
    private var mIncreaseTime: Long = 0

    //When game paused service save time
    private var mPauseLeftTime: Long = 0

    //All blinds are stored here
    private var mBlindsList: MutableList<Blind>? = null

    //Current blinds
    private var mBlinds: String? = null

    private var mStructure: Structure? = null

    //Service uses a WakeLock for don't allow system
    //to sleep
    private var mWakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My log")
        EventBus.getDefault().register(this)
    }

    /**
     * For start timer intent param must have action START_GAME_ACTION and include
     * blinds as an array of strings,
     * round time as a long
     * start time as a long
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // TODO: 21.03.2017 fix error with nullpointexception on intent
        val taskAction = intent.action
        Log.d(TAG, "onStartCommand: action " + taskAction!!)
        if (!mTournamentInProgress && START_GAME_ACTION == taskAction) {
            startNewGame(intent)
            return Service.START_STICKY
        }
        return Service.START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        mTournamentInProgress = false
        EventBus.getDefault().unregister(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun startNewGame(intent: Intent) {
        //doesn't allow system go to sleep mode
        mWakeLock!!.acquire()

        //data from intent
        mRoundTime = intent.getLongExtra(EXTRA_ROUND_TIME, 0)
        mStructure = intent.getSerializableExtra(EXTRA_STRUCTURE) as Structure
        mRoundNum = intent.getIntExtra(EXTRA_START_ROUND, -1)

        mBlindsList = mStructure!!.blinds

        //set increase time and blinds
        startNextRound()
        //start game thread
        mTournamentInProgress = true
        mGameThread = Thread(BlindTimerRunnable())
        mGameThread!!.start()
    }

    @Subscribe
    fun onStateEvent(event: ControlEvent) {
        if (event.type == ControlEvent.Type.STOP) {
            stop()
        } else if (event.type == ControlEvent.Type.CHANGE_STATE) {
            changeState()
        }
    }

    /**
     * Increases blinds and renew time
     */
    private fun startNextRound() {
        // TODO: 21.03.2017 create new blinds smartly
        mRoundNum++
        Log.d(TAG, "startNextRound: mRoundNum is $mRoundNum")
        mBlinds = mBlindsList!![mRoundNum].toString()

        if (mRoundNum == mBlindsList!!.size - 1) produceBlinds()

        mIncreaseTime = SystemClock.elapsedRealtime() + mRoundTime
    }

    /**
     * Creates new blinds when current is last in a list.
     * It produses kind of useless blinds and need only
     * for avoid IndexOfBoundException
     */
    private fun produceBlinds() {
        val currentBlind = mBlindsList!![mBlindsList!!.size - 1]
        val smallBlind = currentBlind.sb * 2
        val bigBlind = smallBlind * 2
        val newBlind = Blind()
        newBlind.sb = smallBlind
        newBlind.bb = bigBlind
        mBlindsList!!.add(newBlind)
    }

    /**
     * Change state to pause and to active
     * @return true if state changed to pause, false if to active
     */
    private fun changeState(): Boolean {
        if (mTournamentInProgress) {
            mPauseLeftTime = mIncreaseTime - SystemClock.elapsedRealtime()
            mTournamentInProgress = false
            mGameThread!!.interrupt()
            mWakeLock!!.release()
            notifyTimer()
            return true
        } else {
            mIncreaseTime = SystemClock.elapsedRealtime() + mPauseLeftTime
            mTournamentInProgress = true
            //mExecutor.execute(new BlindTimerRunnable());
            mGameThread = Thread(BlindTimerRunnable())
            mGameThread!!.start()
            mWakeLock!!.acquire()
            notifyTimer()
            return false
        }
    }

    /**
     * destroy service
     */
    private fun stop() {
        mTournamentInProgress = false
        mRoundNum = 0
        if (mWakeLock!!.isHeld) {
            mWakeLock!!.release()
        }
        stopSelf()
    }

    private fun notifyTimer() {
        val timeToIncrease = mIncreaseTime - SystemClock.elapsedRealtime()
        showNotification()
        if (timeToIncrease < 0) startNextRound()
    }

    /**
     * Creates notification with timer info
     * Calls startForeground method which doesn't allow Android to
     * kill service
     */
    fun showNotification() {
        val notBundle = Bundle()
        //add action - show timer
        val timeToIncrease = mIncreaseTime - SystemClock.elapsedRealtime()
        val action = if (timeToIncrease < 0)
            NotificationUtil.ROUND_ENDED
        else
            NotificationUtil.SHOW_TIMER
        notBundle.putString(NotificationUtil.EXTRA_ACTION, action)
        //add time to increase

        notBundle.putLong(NotificationUtil.EXTRA_TIME, timeToIncrease)
        //add info about mBlinds
        notBundle.putString(NotificationUtil.EXTRA_BLINDS, mBlinds)

        val channel = if (timeToIncrease < 0)
            NotificationUtil.CHANNEL_ID_IMPORTANT
        else
            NotificationUtil.CHANNEL_ID_SILENT

        val notification = NotificationUtil.createNotification(this, notBundle, channel)
        startForeground(NotificationUtil.NOTIFICATION_COD, notification)

        val currentBlind = mBlindsList!![mRoundNum]
        val nextBlind = mBlindsList!![mRoundNum + 1]
        val event = BlindEvent(currentBlind, nextBlind, timeToIncrease, mTournamentInProgress)
        EventBus.getDefault().postSticky(event)
    }

    /**
     * Simple runnable class which call notifyTimer()
     * every one second
     */
    private inner class BlindTimerRunnable : Runnable {
        override fun run() {
            Log.d(TAG, "run: ")
            while (mTournamentInProgress) {
                try {
                    Thread.sleep(1000)
                } catch (ex: InterruptedException) {
                    ex.printStackTrace()
                }

                notifyTimer()
            }
        }
    }

    companion object {

        val TAG = "BlindsService"

        val START_GAME_ACTION = "start_game"

        //Constants for Intent extra
        val EXTRA_ROUND_TIME = "round_time"
        val EXTRA_START_ROUND = "start_round"
        val EXTRA_STRUCTURE = "structure_extra"
    }
}

