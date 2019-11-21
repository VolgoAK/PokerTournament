package com.volgoak.pokertournament.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import com.volgoak.pokertournament.R
import com.volgoak.pokertournament.data.TournamentRepository
import com.volgoak.pokertournament.data.model.*
import com.volgoak.pokertournament.data.toReadableText
import com.volgoak.pokertournament.extensions.into
import com.volgoak.pokertournament.extensions.observeSafe
import com.volgoak.pokertournament.extensions.parseTime
import com.volgoak.pokertournament.utils.NotificationsCreator
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.SerialDisposable
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Service maintains lifecycle of tournament.
 * It shows notification with time to next round
 * and current blinds.
 * Also it runs sound notification when blinds increases
 */

class BlindsService : LifecycleService() {

    companion object {
        private const val EXTRA_ROUND_TIME = "round_time"
        private const val EXTRA_START_ROUND = "start_round"
        private const val EXTRA_STRUCTURE = "structure_extra"

        private const val CODE_NOTIFICATION_PROGRESS = 155
        private const val CODE_NOTIFICATION_LEVEL_UP = 156

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

    /*private var roundNum: Int = 0
    private var roundTime: Long = 0
    private var increaseTime: Long = 0
    private var pauseLeftTime: Long = 0
    private var blindsList: MutableList<Blind>? = null
    private var currentBlinds: String? = null
    private var structure: Structure? = null*/

    private val timerDisposable = SerialDisposable()

    //Service uses a WakeLock for don't allow system
    //to sleep
    private lateinit var wakeLock: PowerManager.WakeLock

    private val tournamentRepository by inject<TournamentRepository>()
    private val notificationCreator by inject<NotificationsCreator>()

    override fun onCreate() {
        super.onCreate()
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My log")
        stateRepository.serviceRunning = true
        initSubscriptions()
    }

    private fun initSubscriptions() {
        tournamentRepository.tournamentInProgressLD
                .observeSafe(this, ::onTournamentStateChanged)

        tournamentRepository.blindsLD
                .observeSafe(this, ::showTimerNotification)

        tournamentRepository.nextRoundLiveEvent
                .observeSafe(this, ::showLevelUpNotification)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return Service.START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stateRepository.serviceRunning = false
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    private fun onTournamentStateChanged(inProgress: Boolean) {
        if(inProgress) {
            if(!wakeLock.isHeld) wakeLock.acquire()
            runTimer()
            tournamentRepository.notifyTimer()
        } else {
            if(wakeLock.isHeld) wakeLock.release()
            timerDisposable.dispose()
        }
    }

    private fun runTimer() {
        Observable.interval(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    tournamentRepository.notifyTimer()
                }, {
                    Timber.e(it)
                }) into timerDisposable
    }

    private fun showTimerNotification(info: BlindInfo) {
        val notification = notificationCreator.createNotification(
                this,
                getString(R.string.blinds_is_f, info.currentBlinds),
                getString(R.string.increase_in_f, info.timeToIncrease.parseTime()),
                NotificationsCreator.Channels.SILENT,
                NotificationsCreator.PendingScreen.TOURNAMENT
        )

        startForeground(CODE_NOTIFICATION_PROGRESS, notification)
    }

    private fun showLevelUpNotification(blind: Blind) {
        val notification = notificationCreator.createNotification(
                this,
                getString(R.string.blinds_increase),
                getString(R.string.new_blinds_f, blind.toReadableText()),
                NotificationsCreator.Channels.IMPORTANT,
                NotificationsCreator.PendingScreen.TOURNAMENT
        )

        NotificationManagerCompat.from(this)
                .notify(CODE_NOTIFICATION_LEVEL_UP, notification)
    }
}

