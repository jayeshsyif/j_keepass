package org.j_keepass.permission.eventinterface;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.j_keepass.util.Util;

public class PermissionCheckerAndGetter implements PermissionEvent {
    private static final int READ_EXTERNAL_STORAGE = 100;
    private boolean IS_READ_EXTERNAL_STORAGE_RECEIVED = false;

    private PermissionCheckerAndGetter() {
    }

    private static final PermissionCheckerAndGetter SOURCE = new PermissionCheckerAndGetter();

    public static PermissionCheckerAndGetter getInstance() {
        return SOURCE;
    }

    public static void register() {
        PermissionEventSource.getInstance().addListener(SOURCE);
    }

    @Override
    public void checkAndGetPermission(View v, Activity activity, Action action) {
        Util.log("Inside check and get permission");
        boolean isOK = IS_READ_EXTERNAL_STORAGE_RECEIVED;
        Util.log("Inside check and get permission, First flag is "+isOK);
        if (!isOK) {
            Util.log("Show permission popup");
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                if (ContextCompat.checkSelfPermission(v.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(v.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
                }
                if (ContextCompat.checkSelfPermission(v.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    isOK = true;
                }
            } else {
                if (ContextCompat.checkSelfPermission(v.getContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(v.getContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_IMAGES}, READ_EXTERNAL_STORAGE);
                }
                if (ContextCompat.checkSelfPermission(v.getContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                    isOK = true;
                }
            }
        }
        IS_READ_EXTERNAL_STORAGE_RECEIVED = isOK;
        Util.log("Inside check and get permission, return flag is "+IS_READ_EXTERNAL_STORAGE_RECEIVED);
        if (IS_READ_EXTERNAL_STORAGE_RECEIVED) {
            PermissionEventSource.getInstance().permissionGranted(action);
        } else {
            PermissionEventSource.getInstance().permissionDenied(action);
        }
    }

    @Override
    public void permissionDenied(Action action) {

    }

    @Override
    public void permissionGranted(Action action) {

    }
}
