package com.volgoak.pokertournament.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
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

    /**
     * Creates notification with params in Bundle
     * @param context
     * @param extra Bundle must contain String with a tag EXTRA_BLINDS,
     *              long time with a tag EXTRA_TIME,
     *              String action - SHOW_TIMER or ROUND_ENDED with a tag EXTRA_ACTION
     * @return Notification with info about tournament
     */
    public static Notification createNotification(Context context, Bundle extra){

        Notification.Builder builder = new Notification.Builder(context)
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.poker_24p)
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
        }else if(ROUND_ENDED.equals(action)){
            title = context.getString(R.string.blinds_increase);
            text = context.getString(R.string.new_blinds) + " " + blinds;
            // TODO: 18.03.2017 add sound
            builder.setSound(soundUri(context, -1));
        }

        builder.setContentTitle(title);
        builder.setContentText(text);

        return builder.build();
    }
    // TODO: 14.01.2017 add silluete icon for version 21

    /**
     * Decodes icon bitmap for notification
     * @param context
     * @return Bitmap for notification
     */
    private static Bitmap largeIcon(Context context){
        Resources resources = context.getResources();
        Bitmap icon = BitmapFactory.decodeResource(resources, R.drawable.poker_36p);
        return icon;
    }

    /**
     * Creates Uri of sound for sound notification
     * @param context
     * @param sound id of sound
     * @return Uri of sound
     */
    private static Uri soundUri(Context context, int sound){
        Uri alarmSound = null;
        if(sound == -1) {
            alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        //Uri alarmSound = Uri.parse("android.resource://" + context.getPackageName() + "/" + sound);
        Log.d(TAG, "soundUri: " + alarmSound);
        return alarmSound;
    }

    /**
     * Creates pending intent with TournamentActivity
     */
    private static PendingIntent pendingIntent(Context context){
        Intent intent = new Intent(context, TournamentActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    /**
     * Parse time in long to readable String
     * @param time left time in millis
     * @return readable time in String
     */
    public static String parseTime(long time){
        int sec = (int)(time/1000 % 60);
        int min = (int)(time/1000/60);

        String timeString = String.format("%02d:%02d", min, sec);
        return timeString;
    }
}
