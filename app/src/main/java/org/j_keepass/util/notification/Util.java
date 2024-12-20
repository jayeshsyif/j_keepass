package org.j_keepass.util.notification;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Util {
    public void startAlarmBroadcastReceiver(Context context) {
        try {
            log("Start Alarm Broadcast Receiver");
            if (checkIsPermissionAvailable(context)) {
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
                    log(" Cancelling ");
                    alarmManager.cancel(pendingIntent);
                    log(" Cancelled ");
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
                        log(" Not Available and Notification set. " + formattedDate);
                        //ToastUtil.showToast(getLayoutInflater(), binding.getRoot().getRootView(), "" + (isCancelled ? " Cancelled and" : "") + " Notification set. " + formattedDate, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
                    }
                } else {
                    log("Notification is available and set to " + alarmManager);
                }
            }
        } catch (Exception e) {
            log("Notification set error ." + e.getMessage());
        }
    }

    public boolean checkIsPermissionAvailable(Context context) {
        log("checkIsPermissionAvailable inside broadcast receiver");
        boolean isOK = false;
        try {
            if (!isOK) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    log("Tri checkIsPermissionAvailable inside broadcast receiver");
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.USE_EXACT_ALARM) == PackageManager.PERMISSION_GRANTED) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                            isOK = true;
                        } else {
                            isOK = false;
                        }
                    }
                } else {
                    log("Non Tri checkIsPermissionAvailable inside broadcast receiver");
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.SCHEDULE_EXACT_ALARM) == PackageManager.PERMISSION_GRANTED) {
                        isOK = true;
                    } else {
                        isOK = false;
                    }
                }
            }
        } catch (Exception e) {
        }
        log("received check permission return is: " + isOK);
        return isOK;
    }
    private void log(String msg) {
        org.j_keepass.util.Util.log(msg);
    }
}
