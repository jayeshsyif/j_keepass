package org.j_keepass.util.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;

import org.j_keepass.LandingAndListDatabaseActivity;
import org.j_keepass.R;
import org.j_keepass.util.Util;

public class AlarmBroadcastReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        org.j_keepass.util.notification.Util util = new org.j_keepass.util.notification.Util();
        if (util.checkIsPermissionAvailable(context)) {
            log("received alarm");
            showNotification(context, util);
            log("received alarm done");
            //if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))
            {
                util.startAlarmBroadcastReceiver(context);
            }
        }
    }

    void showNotification(Context context, org.j_keepass.util.notification.Util util) {
        if (util.checkIsPermissionAvailable(context)) {
            try {
                log("received alarm 1");
                String CHANNEL_ID = "" + context.getString(R.string.app_name) + "-channel";// The id of the channel.
                CharSequence name = context.getResources().getString(R.string.app_name) + " - Reminder";// The user-visible name of the channel.
                NotificationCompat.Builder mBuilder;
                log("received alarm 2");
                Intent notificationIntent = new Intent(context, LandingAndListDatabaseActivity.class);
                Bundle bundle = new Bundle();
                notificationIntent.putExtras(bundle);
                log("received alarm 3");
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                log("received alarm 4");
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
                log("received alarm 5");
                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                log("received alarm 6");
                if (Build.VERSION.SDK_INT >= 26) {
                    log("received alarm 7");
                    NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
                    mNotificationManager.createNotificationChannel(mChannel);
                    log("received alarm 8");
                    mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.mipmap.ic_launcher).setLights(Color.RED, 300, 300).setColor(context.getColor(R.color.kp_changing_white)).setChannelId(CHANNEL_ID).setContentTitle(name);
                    log("received alarm 9");
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        log("received alarm 10");
                        mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.mipmap.ic_launcher).setPriority(Notification.PRIORITY_HIGH).setColor(context.getColor(R.color.kp_changing_white)).setContentTitle(name);
                        log("received alarm 11");
                    } else {
                        log("received alarm 12");
                        mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.mipmap.ic_launcher).setPriority(Notification.PRIORITY_HIGH).setContentTitle(name);
                        log("received alarm 13");
                    }
                }
                log("received alarm 14");
                mBuilder.setContentIntent(contentIntent);
                log("received alarm 15");
                mBuilder.setContentText("Keep your passwords updated with JKeePass app.");
                mBuilder.setAutoCancel(true);
                log("received alarm 16");
                mNotificationManager.notify(1, mBuilder.build());
                log("received alarm 17");
            } catch (Exception e) {
                log("received alarm, exception is " + e.getMessage());
            }
        }
    }

    private void log(String msg) {
        Util.log(msg);
    }
}