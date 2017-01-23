package com.volgoak.pokertournament.utils;

import android.app.Notification;
import android.app.NotificationManager;
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

import java.text.DateFormat;


/**
 * Created by Volgoak on 14.01.2017.
 */

public class NotificationUtil {

    public static final String TAG = "NotificationUtil";
    public static final String EXTRA_ACTION = "action";
    public static final String EXTRA_TIME = "time";
    public static final String SHOW_TIMER = "show_timer";
    public static final String ROUND_ENDED = "round_ended";
    public static final String EXTRA_BLINDS = "extra_blinds";

    public static final int NOTIFICATION_COD = 1237;

    //first create simple notification
    public static Notification createNotification(Context context, Bundle extra){

        Notification.Builder builder = new Notification.Builder(context)
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.poker_24p)
                .setLargeIcon(largeIcon(context));

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
            builder.setSound(soundUri(context));
        }

        builder.setContentTitle(title);
        builder.setContentText(text);

        return builder.build();
    }
    // TODO: 14.01.2017 add silluete icon for version 21

    private static Bitmap largeIcon(Context context){
        Resources resources = context.getResources();
        Bitmap icon = BitmapFactory.decodeResource(resources, R.drawable.poker_36p);
        return icon;
    }

    private static Uri soundUri(Context context){
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Log.d(TAG, "soundUri: " + alarmSound);
        return alarmSound;
    }

    private static String parseTime(long time){
        int sec = (int)(time/1000 % 60);
        int min = (int)(time/1000/60);

        String timeString = String.format("%2d:%02d", min, sec);
        return timeString;
    }
}
