package org.j_keepass;

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
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class AlarmBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("@@ JKeePass","recieved alaram");
        showNotification(context);
        Log.i("@@ JKeePass","recieved alaram done");
    }

    void showNotification(Context context) {
        try {
            Log.i("@@ JKeePass","recieved alaram 1");
            String CHANNEL_ID = "" + context.getString(R.string.app_name) + "-channel";// The id of the channel.
            CharSequence name = context.getResources().getString(R.string.app_name)+" - Reminder";// The user-visible name of the channel.
            NotificationCompat.Builder mBuilder;
            Log.i("@@ JKeePass","recieved alaram 2");
            Intent notificationIntent = new Intent(context, LoadActivity.class);
            Bundle bundle = new Bundle();
            notificationIntent.putExtras(bundle);
            Log.i("@@ JKeePass","recieved alaram 3");
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            Log.i("@@ JKeePass","recieved alaram 4");
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
            Log.i("@@ JKeePass","recieved alaram 5");
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Log.i("@@ JKeePass","recieved alaram 6");
            if (Build.VERSION.SDK_INT >= 26) {
                Log.i("@@ JKeePass","recieved alaram 7");
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
                mNotificationManager.createNotificationChannel(mChannel);
                Log.i("@@ JKeePass","recieved alaram 8");
                mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLights(Color.RED, 300, 300)
                        .setColor(context.getColor(R.color.kp_changing_white))
                        .setChannelId(CHANNEL_ID)
                        .setContentTitle(name);
                Log.i("@@ JKeePass","recieved alaram 9");
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.i("@@ JKeePass","recieved alaram 10");
                    mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setPriority(Notification.PRIORITY_HIGH)
                            .setColor(context.getColor(R.color.kp_changing_white))
                            .setContentTitle(name);
                    Log.i("@@ JKeePass","recieved alaram 11");
                } else {
                    Log.i("@@ JKeePass","recieved alaram 12");
                    mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setPriority(Notification.PRIORITY_HIGH)
                            .setContentTitle(name);
                    Log.i("@@ JKeePass","recieved alaram 13");
                }
            }
            Log.i("@@ JKeePass","recieved alaram 14");
            mBuilder.setContentIntent(contentIntent);
            Log.i("@@ JKeePass","recieved alaram 15");
            mBuilder.setContentText("Keep your passwords update with JKeePass app.");
            mBuilder.setAutoCancel(true);
            Log.i("@@ JKeePass","recieved alaram 16");
            mNotificationManager.notify(1, mBuilder.build());
            Log.i("@@ JKeePass","recieved alaram 17");
        } catch (Exception e) {
            Log.i("@@ JKeePass","recieved alaram, exception is "+e.getMessage());
        }
    }
}