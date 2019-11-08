package com.volgoak.pokertournament.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.volgoak.pokertournament.R
import com.volgoak.pokertournament.screens.tournament.TournamentActivity

class NotificationsCreator {

    fun createNotification(context: Context,
                           title: String,
                           body: String,
                           channel: Channels,
                           pendingScreen: PendingScreen?): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createChannel(context, channel)

        val builder = NotificationCompat.Builder(context, channel.id)
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_stat_timer)
                .setLargeIcon(largeIcon(context))
                .setContentTitle(title)
                .setContentText(body)

        if (channel.enableSound) {
            builder.setSound(soundUri(context, -1))
        }

        if (pendingScreen != null) {
            builder.setContentIntent(createPendingIntent(context, pendingScreen))
        }

        return builder.build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(context: Context, channel: Channels) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (notificationManager.getNotificationChannel(channel.id) == null) {
            val importance = if (channel.hightImportance) NotificationManager.IMPORTANCE_HIGH
            else NotificationManager.IMPORTANCE_LOW

            with(NotificationChannel(channel.id, channel.channelName, importance)) {
                description = channel.description
                enableVibration(channel.enableVibration)
                enableLights(channel.enableVibration)
                if(!channel.enableSound) {
                    setSound(null, null)
                }
                this.importance = importance
                notificationManager.createNotificationChannel(this)
            }
        }
    }

    private fun soundUri(context: Context, sound: Int): Uri? {
        var alarmSound: Uri? = null
        if (sound == -1) {
            alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }
        //Uri alarmSound = Uri.parse("android.resource://" + context.getPackageName() + "/" + sound);
        return alarmSound
    }

    private fun largeIcon(context: Context): Bitmap {
        val resources = context.resources
        return BitmapFactory.decodeResource(resources, R.drawable.ic_shortcut_notif)
    }

    private fun createPendingIntent(context: Context, pendingScreen: PendingScreen): PendingIntent {
        val intent = when (pendingScreen) {
            PendingScreen.TOURNAMENT -> Intent(context, TournamentActivity::class.java)
        }
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    enum class PendingScreen {
        TOURNAMENT
    }

    enum class Channels(
            val id: String,
            val channelName: String,
            val description: String,
            val enableSound: Boolean,
            val enableVibration: Boolean,
            val hightImportance: Boolean

    ) {
        SILENT(
                "Notifications_silent",
                "Silent notifications",
                "Timer notifications",
                false,
                false,
                false
        ),
        IMPORTANT(
                "Notifications_important",
                "Next round notifications",
                "notifications with a sound",
                true,
                true,
                true
        )
    }
}