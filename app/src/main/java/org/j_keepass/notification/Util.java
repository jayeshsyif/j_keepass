package org.j_keepass.notification;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

import org.j_keepass.util.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Util {
    private static final Locale locale = Locale.ENGLISH;
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
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, 10);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && alarmManager != null && pendingIntent != null) {
                    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
                    //alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",locale);
                    String formattedDate = simpleDateFormat.format(calendar.getTime());
                    log(" Not Available and Notification set. " + formattedDate);
                    //ToastUtil.showToast(getLayoutInflater(), binding.getRoot().getRootView(), "" + (isCancelled ? " Cancelled and" : "") + " Notification set. " + formattedDate, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
                } else {
                    log(" Notification is not set, check version or manager / intent is null ");
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                log("Tri checkIsPermissionAvailable inside broadcast receiver");
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.USE_EXACT_ALARM) == PackageManager.PERMISSION_GRANTED) {
                    isOK = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
                }
            } else {
                log("Non Tri checkIsPermissionAvailable inside broadcast receiver");
                isOK = ContextCompat.checkSelfPermission(context, Manifest.permission.SCHEDULE_EXACT_ALARM) == PackageManager.PERMISSION_GRANTED;
            }
        } catch (Exception e) {
            ignoreError(e);
        }
        log("received check permission return is: " + isOK);
        return isOK;
    }

    private void log(String msg) {
        Utils.log(msg);
    }

    private void ignoreError(Throwable t) {
        Utils.ignoreError(t);
    }
}
