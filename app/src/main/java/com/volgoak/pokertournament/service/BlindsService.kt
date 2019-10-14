package com.volgoak.pokertournament.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import com.volgoak.pokertournament.data.Blind
import com.volgoak.pokertournament.data.Structure
import com.volgoak.pokertournament.extensions.into
import com.volgoak.pokertournament.utils.BlindEvent
import com.volgoak.pokertournament.utils.ControlEvent
import com.volgoak.pokertournament.utils.NotificationUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.SerialDisposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Service maintains lifecycle of tournament.
 * It shows notification with time to next round
 * and current blinds.
 * Also it runs sound notification when blinds increases
 */

class BlindsService : Service() {

    companion object {
        private const val EXTRA_ROUND_TIME = "round_time"
        private const val EXTRA_START_ROUND = "start_round"
        private const val EXTRA_STRUCTURE = "structure_extra"

        fun getStartGameIntent(
                context: Context,
                structure: Structure,
                roundTime: Long,
                startRound: Int): Intent {
            return Intent(context, BlindsService::class.java)
                    .putExtra(EXTRA_STRUCTURE, structure)
                    .putExtra(EXTRA_ROUND_TIME, roundTime)
                    .putExtra(EXTRA_START_ROUND, startRound)
        }
    }

    private val stateRepository: ServiceStateRepository by inject()

    @Volatile
    private var tournamentInProgress: Boolean = false

    private var roundNum: Int = 0
    private var roundTime: Long = 0
    private var increaseTime: Long = 0
    private var pauseLeftTime: Long = 0
    private var blindsList: MutableList<Blind>? = null
    private var currentBlinds: String? = null
    private var structure: Structure? = null

    private val timerDisposable = SerialDisposable()

    //Service uses a WakeLock for don't allow system
    //to sleep
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My log")
        EventBus.getDefault().register(this)
        timerDisposable.isDisposed
        stateRepository.serviceRunning = true
    }

    /**
     * For start timer intent param must have action START_GAME_ACTION and include
     * blinds as an array of strings,
     * round time as a long
     * start time as a long
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (!tournamentInProgress) {
            startNewGame(intent)
            return Service.START_STICKY
        }
        return Service.START_STICKY
    }

    override fun onDestroy() {
        Timber.d("TESTING service onDestroy called")
        tournamentInProgress = false
        EventBus.getDefault().unregister(this)
        stateRepository.serviceRunning = false
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun startNewGame(intent: Intent) {
        //doesn't allow system go to sleep mode
        wakeLock!!.acquire()

        //data from intent
        roundTime = intent.getLongExtra(EXTRA_ROUND_TIME, 0)
        structure = intent.getSerializableExtra(EXTRA_STRUCTURE) as Structure
        roundNum = intent.getIntExtra(EXTRA_START_ROUND, -1)

        blindsList = structure!!.blinds

        //set increase time and blinds
        startNextRound()
        runTimer()
        tournamentInProgress = true
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
        roundNum++
        Timber.d("startNextRound: roundNum is $roundNum")
        currentBlinds = blindsList!![roundNum].toString()

        if (roundNum == blindsList!!.size - 1) produceBlinds()

        increaseTime = SystemClock.elapsedRealtime() + roundTime
    }

    /**
     * Creates new blinds when current is last in a list.
     * It produses kind of useless blinds and need only
     * for avoid IndexOfBoundException
     */
    private fun produceBlinds() {
        val currentBlind = blindsList!![blindsList!!.size - 1]
        val smallBlind = currentBlind.sb * 2
        val bigBlind = smallBlind * 2
        val newBlind = Blind()
        newBlind.sb = smallBlind
        newBlind.bb = bigBlind
        blindsList!!.add(newBlind)
    }

    /**
     * Change state to pause and to active
     * @return true if state changed to pause, false if to active
     */
    private fun changeState(): Boolean {
        if (tournamentInProgress) {
            pauseLeftTime = increaseTime - SystemClock.elapsedRealtime()
            tournamentInProgress = false
            wakeLock!!.release()
            timerDisposable.dispose()
            notifyTimer()
            return true
        } else {
            increaseTime = SystemClock.elapsedRealtime() + pauseLeftTime
            tournamentInProgress = true
            runTimer()
            wakeLock!!.acquire()
            notifyTimer()
            return false
        }
    }

    private fun runTimer() {
        Observable.interval(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    notifyTimer()
                }, {
                    Timber.e(it)
                }) into timerDisposable
    }

    /**
     * destroy service
     */
    private fun stop() {
        tournamentInProgress = false
        roundNum = 0
        if (wakeLock!!.isHeld) {
            wakeLock!!.release()
        }
        timerDisposable.dispose()
        stopSelf()
    }

    private fun notifyTimer() {
        val timeToIncrease = increaseTime - SystemClock.elapsedRealtime()
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
        val timeToIncrease = increaseTime - SystemClock.elapsedRealtime()
        val action = if (timeToIncrease < 0)
            NotificationUtil.ROUND_ENDED
        else
            NotificationUtil.SHOW_TIMER
        notBundle.putString(NotificationUtil.EXTRA_ACTION, action)
        //add time to increase

        notBundle.putLong(NotificationUtil.EXTRA_TIME, timeToIncrease)
        //add info about currentBlinds
        notBundle.putString(NotificationUtil.EXTRA_BLINDS, currentBlinds)

        val channel = if (timeToIncrease < 0)
            NotificationUtil.CHANNEL_ID_IMPORTANT
        else
            NotificationUtil.CHANNEL_ID_SILENT

        val notification = NotificationUtil.createNotification(this, notBundle, channel)
        startForeground(NotificationUtil.NOTIFICATION_COD, notification)

        blindsList?.let { list ->
            val event = BlindEvent(list[roundNum], list[roundNum + 1], timeToIncrease, tournamentInProgress)
            EventBus.getDefault().postSticky(event)
        }
    }
}

