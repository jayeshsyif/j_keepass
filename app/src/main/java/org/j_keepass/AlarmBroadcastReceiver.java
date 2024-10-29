package org.j_keepass;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import org.j_keepass.util.Common;
import org.j_keepass.util.ToastUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AlarmBroadcastReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if (checkAndGetPermission(context)) {
            log("@@ JKeePass", "received alarm");
            showNotification(context);
            log("@@ JKeePass", "received alarm done");
            //if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))
            {
                startAlarmBroadcastReceiver(context);
            }
        }
    }

    void showNotification(Context context) {
        if (checkAndGetPermission(context)) {
            try {
                log("@@ JKeePass", "received alarm 1");
                String CHANNEL_ID = "" + context.getString(R.string.app_name) + "-channel";// The id of the channel.
                CharSequence name = context.getResources().getString(R.string.app_name) + " - Reminder";// The user-visible name of the channel.
                NotificationCompat.Builder mBuilder;
                log("@@ JKeePass", "received alarm 2");
                Intent notificationIntent = new Intent(context, LoadActivity.class);
                Bundle bundle = new Bundle();
                notificationIntent.putExtras(bundle);
                log("@@ JKeePass", "received alarm 3");
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                log("@@ JKeePass", "received alarm 4");
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
                log("@@ JKeePass", "received alarm 5");
                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                log("@@ JKeePass", "received alarm 6");
                if (Build.VERSION.SDK_INT >= 26) {
                    log("@@ JKeePass", "received alarm 7");
                    NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
                    mNotificationManager.createNotificationChannel(mChannel);
                    log("@@ JKeePass", "received alarm 8");
                    mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.mipmap.ic_launcher).setLights(Color.RED, 300, 300).setColor(context.getColor(R.color.kp_changing_white)).setChannelId(CHANNEL_ID).setContentTitle(name);
                    log("@@ JKeePass", "received alarm 9");
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        log("@@ JKeePass", "received alarm 10");
                        mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.mipmap.ic_launcher).setPriority(Notification.PRIORITY_HIGH).setColor(context.getColor(R.color.kp_changing_white)).setContentTitle(name);
                        log("@@ JKeePass", "received alarm 11");
                    } else {
                        log("@@ JKeePass", "received alarm 12");
                        mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.mipmap.ic_launcher).setPriority(Notification.PRIORITY_HIGH).setContentTitle(name);
                        log("@@ JKeePass", "received alarm 13");
                    }
                }
                log("@@ JKeePass", "received alarm 14");
                mBuilder.setContentIntent(contentIntent);
                log("@@ JKeePass", "received alarm 15");
                mBuilder.setContentText("Keep your passwords updated with JKeePass app.");
                mBuilder.setAutoCancel(true);
                log("@@ JKeePass", "received alarm 16");
                mNotificationManager.notify(1, mBuilder.build());
                log("@@ JKeePass", "received alarm 17");
            } catch (Exception e) {
                log("@@ JKeePass", "received alarm, exception is " + e.getMessage());
            }
        }
    }

    private void log(String tag, String msg) {
        //Log.i(tag, msg);
    }

    private boolean checkAndGetPermission(Context context) {
        log("@@ JKeePass", "checkAndGetPermission inside broadcast receiver");
        boolean isOK = false;
        try {
            if (!isOK) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.USE_EXACT_ALARM) == PackageManager.PERMISSION_GRANTED) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                            isOK = true;
                        } else {
                            isOK = false;
                        }
                    }
                } else {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.SCHEDULE_EXACT_ALARM) == PackageManager.PERMISSION_GRANTED) {
                        isOK = true;
                    } else {
                        isOK = false;
                    }
                }
            }
        } catch (Exception e) {
        }
        log("@@ JKeePass", "received check permission return is: " + isOK);
        return isOK;
    }

    public void startAlarmBroadcastReceiver(Context context) {
        try {
            if (checkAndGetPermission(context)) {
                boolean isAvailable = false;
                Intent _intent = new Intent(context, AlarmBroadcastReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, _intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                //PendingIntent pendingIntent = PendingIntent.getService(context, 1, _intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (pendingIntent != null && alarmManager != null) {
                    //alarmManager.cancel(pendingIntent);
                    isAvailable = true;
                }
                if (isAvailable) {
                    log("JKEEPASS", " Cancelling ");
                    alarmManager.cancel(pendingIntent);
                    log("JKEEPASS", " Cancelled ");
                    isAvailable = false;
                }
                if (!isAvailable) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.set(Calendar.HOUR_OF_DAY, 10);
                    calendar.set(Calendar.MINUTE, 00);
                    calendar.set(Calendar.SECOND, 00);
                    if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
                        //alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        String formattedDate = simpleDateFormat.format(calendar.getTime());
                        log("JKEEPASS", " Not Available and Notification set. " + formattedDate);
                        //ToastUtil.showToast(getLayoutInflater(), binding.getRoot().getRootView(), "" + (isCancelled ? " Cancelled and" : "") + " Notification set. " + formattedDate, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
                    }
                } else {
                    log("JKEEPASS", "Notification is available and set to " + alarmManager);
                }
            }
        } catch (Exception e) {
            log("JKEEPASS", "Notification set error ." + e.getMessage());
        }
    }
}