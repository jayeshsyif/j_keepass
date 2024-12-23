package org.j_keepass.util;


import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.j_keepass.R;
import org.j_keepass.list_db.dtos.GroupEntryStatus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utils {

    private static final String TAG = "JKEEPASS";
    private static final boolean LOG_FLAG = false;

    static public byte[] object2Bytes(Object o) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        return baos.toByteArray();
    }

    static public Object bytes2Object(byte raw[]) throws Exception, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(raw);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object o = ois.readObject();
        return o;
    }

    static public boolean isUsable(String str) {
        if (str == null || str.length() == 0) {
            return false;
        } else {
            return true;
        }
    }

    static public String convertDateToString(Date d) {
        String dateStr;
        try {
            DateFormat df = new SimpleDateFormat("E, dd MMM yyyy, hh:mm:ss a");
            dateStr = df.format(d);
        } catch (Exception e) {
            dateStr = d.toString();
        }
        return dateStr;
    }

    static public String convertDateToStringOtherFormat(Date d) {
        String dateStr;
        try {
            DateFormat df = new SimpleDateFormat("E, dd/M/yy, hh:mm: a");
            dateStr = df.format(d);
        } catch (Exception e) {
            dateStr = d.toString();
        }
        return dateStr;
    }

    static public String convertDateToStringOnlyDate(Date d) {
        String dateStr;
        try {
            DateFormat df = new SimpleDateFormat("dd/M/yy");
            dateStr = df.format(d);
        } catch (Exception e) {
            dateStr = d.toString();
        }
        return dateStr;
    }

    static public Date convertStringToDate(String d) {
        Date date;
        try {
            DateFormat df = new SimpleDateFormat("E, dd MMM yyyy, hh:mm:ss a");
            date = df.parse(d);
        } catch (Exception e) {
            date = Calendar.getInstance().getTime();
        }
        return date;
    }

    static public String convertDateToString(Long dateInLong) {
        return convertDateToString(new Date(dateInLong));
    }

    static public String convertDateToStringOtherFormat(Long dateInLong) {
        return convertDateToStringOtherFormat(new Date(dateInLong));
    }

    static public String convertDateToStringOnlyDate(Long dateInLong) {
        return convertDateToStringOnlyDate(new Date(dateInLong));
    }

    static public void sleepInMilliSec(long millisec) {
        try {
            Thread.sleep(millisec);
        } catch (Exception e) {
            //ignore
        }
    }

    static public void sleepFor1Sec() {
        sleepInMilliSec(1000);
    }

    static public void sleepForHalfSec() {
        sleepInMilliSec(500);
    }

    static public void sleepFor1MSec() {
        sleepInMilliSec(100);
    }

    static public void sleepFor3MSec() {
        sleepInMilliSec(300);
    }

    static public void sleepFor5Sec() {
        sleepInMilliSec(5000);
    }

    public static void log(String msg) {
        if (LOG_FLAG) {
            Log.i(TAG, msg);
        }
    }

    public static boolean checkCodecAvailable() {
        boolean isCodecAvailable = false;
        try {
            org.apache.commons.codec.binary.Base64.encodeBase64String("".getBytes());
            isCodecAvailable = true;
        } catch (Throwable e) {
            //ignore
        }
        return isCodecAvailable;
    }

    public static void setExpiryText(TextView tv, Pair<GroupEntryStatus, Long> statusLongPair) {
        if (statusLongPair.first != null) {
            Context context = tv.getContext();
            String text;
            int color;

            switch (statusLongPair.first) {
                case EXPIRED:
                    text = context.getString(R.string.expiredInDays).replace("{0}", String.valueOf(statusLongPair.second));
                    color = R.color.kp_red;
                    break;
                case EXPIRING_SOON:
                    text = context.getString(R.string.expiringSoonInDays).replace("{0}", String.valueOf(statusLongPair.second));
                    color = R.color.kp_coral;
                    break;
                case OK:
                    text = context.getString(R.string.expiringInDays).replace("{0}", String.valueOf(statusLongPair.second));
                    color = 0; // No specific color needed for OK status
                    break;
                default:
                    tv.setVisibility(View.GONE);
                    return; // Exit early if no relevant status
            }

            tv.setVisibility(View.VISIBLE);
            tv.setText(text);

            if (color != 0) {
                tv.setTextColor(context.getResources().getColor(color));
            }
        }

    }
}
