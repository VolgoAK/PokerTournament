package com.volgoak.pokertournament.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.volgoak.pokertournament.R;
import com.volgoak.pokertournament.TournamentActivity;


/**
 * Created by Volgoak on 14.01.2017.
 */

public class NotificationUtil {

    //Constants for extra
    public static final String TAG = "NotificationUtil";
    public static final String EXTRA_ACTION = "action";
    public static final String EXTRA_TIME = "time";
    public static final String SHOW_TIMER = "show_timer";
    public static final String ROUND_ENDED = "round_ended";
    public static final String EXTRA_BLINDS = "extra_blinds";

    //Code of notification for system
    public static final int NOTIFICATION_COD = 1237;
    public static final String CHANNEL_ID_SILENT = "Notifications_silent";
    public static final String CHANNEL_ID_IMPORTANT = "Notifications_important";

    private static boolean channelsCreated;

    /**
     * Creates notification with params in Bundle
     *
     * @param context
     * @param extra   Bundle must contain String with a tag EXTRA_BLINDS,
     *                long time with a tag EXTRA_TIME,
     *                String action - SHOW_TIMER or ROUND_ENDED with a tag EXTRA_ACTION
     * @return Notification with info about tournament
     */
    public static Notification createNotification(Context context, Bundle extra, String channelId) {

        if(!channelsCreated) createChannels(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_stat_timer)
                .setLargeIcon(largeIcon(context))
                .setContentIntent(pendingIntent(context));

        String title = "";
        String text = "";

        String blinds = extra.getString(EXTRA_BLINDS);
        long timeToIncrease = extra.getLong(EXTRA_TIME, 0);
        String time = parseTime(timeToIncrease);
        //// TODO: 19.01.2017 what about show next level?
        String action = extra.getString(EXTRA_ACTION);
        if (SHOW_TIMER.equals(action)) {
            title = context.getString(R.string.blinds_is) + " " + blinds;
            text = context.getString(R.string.increase_in) + time;
        } else if (ROUND_ENDED.equals(action)) {
            title = context.getString(R.string.blinds_increase);
            text = context.getString(R.string.new_blinds) + " " + blinds;
            // TODO: 18.03.2017 add sound
            builder.setSound(soundUri(context, -1));
        }

        builder.setContentTitle(title);
        builder.setContentText(text);

        return builder.build();
    }

    private static void createChannels(Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_SILENT, "My channel",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Timer notifications");
            channel.enableVibration(false);
            channel.setSound(null, null);
            channel.setImportance(NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);

            NotificationChannel importantChannel = new NotificationChannel(CHANNEL_ID_IMPORTANT, "Important channel",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Blind increase notifications");
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setLightColor(Color.GREEN);
            notificationManager.createNotificationChannel(importantChannel);

            channelsCreated = true;
        }

    }

    /**
     * Decodes icon bitmap for notification
     *
     * @param context
     * @return Bitmap for notification
     */
    private static Bitmap largeIcon(Context context) {
        Resources resources = context.getResources();
        Bitmap icon = BitmapFactory.decodeResource(resources, R.drawable.ic_shortcut_notif);
        return icon;
    }

    /**
     * Creates Uri of sound for sound notification
     *
     * @param context
     * @param sound   id of sound
     * @return Uri of sound
     */
    private static Uri soundUri(Context context, int sound) {
        Uri alarmSound = null;
        if (sound == -1) {
            alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        //Uri alarmSound = Uri.parse("android.resource://" + context.getPackageName() + "/" + sound);
        Log.d(TAG, "soundUri: " + alarmSound);
        return alarmSound;
    }

    /**
     * Creates pending intent with TournamentActivity
     */
    private static PendingIntent pendingIntent(Context context) {
        Intent intent = new Intent(context, TournamentActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    /**
     * Parse time in long to readable String
     *
     * @param time left time in millis
     * @return readable time in String
     */
    public static String parseTime(long time) {
        int sec = (int) (time / 1000 % 60);
        int min = (int) (time / 1000 / 60);

        String timeString = String.format("%02d:%02d", min, sec);
        return timeString;
    }
}
