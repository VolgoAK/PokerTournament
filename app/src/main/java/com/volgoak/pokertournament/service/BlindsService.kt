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

    enum class ServiceAction(val action: String) {
        RUN_SERVICE("run_service"),
        STOP_SERVICE("stop_service")
    }

    companion object {
        private const val CODE_NOTIFICATION_PROGRESS = 155
        private const val CODE_NOTIFICATION_LEVEL_UP = 156

        fun getStartGameIntent(context: Context)
                = createIntent(context, ServiceAction.RUN_SERVICE)

        fun getStopIntent(context: Context)
                = createIntent(context, ServiceAction.STOP_SERVICE)

        private fun createIntent(context: Context, action: ServiceAction): Intent {
            return Intent(context, BlindsService::class.java)
                    .setAction(action.action)
        }
    }

    private val stateRepository: ServiceStateRepository by inject()

    private val timerDisposable = SerialDisposable()

    //Service uses a WakeLock for don't allow system
    //to sleep
    private lateinit var wakeLock: PowerManager.WakeLock

    private val tournamentRepository by inject<TournamentRepository>()
    private val notificationCreator by inject<NotificationsCreator>()

    override fun onCreate() {
        super.onCreate()
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "pokerTimer:wakelock")
        stateRepository.serviceRunning = true
        initSubscriptions()
    }

    private fun initSubscriptions() {
        tournamentRepository.tournamentInProgressLD
                .observeSafe(this, ::onTournamentStateChanged)

        tournamentRepository.tournamentInfoLD
                .observeSafe(this) { showTimerNotification(it)}

        tournamentRepository.nextRoundLiveEvent
                .observeSafe(this, ::showLevelUpNotification)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return when(intent.action) {
            ServiceAction.RUN_SERVICE.action -> {
                showTimerNotification(TournamentInfo(), true)
                Service.START_STICKY
            }
            ServiceAction.STOP_SERVICE.action -> {
                stopForeground(true)
                stopSelf()
                Service.START_NOT_STICKY
            }
            else -> {
                throw RuntimeException("Unknown intent action ${intent.action}")
            }
        }
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
            timerDisposable.get()?.dispose()
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

    private fun showTimerNotification(info: TournamentInfo, startForeground: Boolean = false) {
        val notification = notificationCreator.createNotification(
                this,
                getString(R.string.blinds_is_f, info.currentBlinds.toReadableText()),
                getString(R.string.increase_in_f, info.timeToIncrease.parseTime()),
                NotificationsCreator.Channels.SILENT,
                NotificationsCreator.PendingScreen.TOURNAMENT
        )

        if(startForeground) {
            startForeground(CODE_NOTIFICATION_PROGRESS, notification)
        } else {
            NotificationManagerCompat.from(this)
                    .notify(CODE_NOTIFICATION_PROGRESS, notification)
        }
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

