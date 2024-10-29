package org.j_keepass.util;


import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Util {

    static public byte[] object2Bytes(Object o) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        return baos.toByteArray();
    }

    static public Object bytes2Object(byte raw[])
            throws Exception, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(raw);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object o = ois.readObject();
        return o;
    }

    static public boolean isUsable(String str) {
        if (str == null || str.length() <= 0) {
            return false;
        } else {
            return true;
        }
    }

    static public String convertDateToString(Date d) {
        String dateStr = "";
        try {
            DateFormat df = new SimpleDateFormat("E, dd MMM yyyy, hh:mm:ss a");
            dateStr = df.format(d);
        } catch (Exception e) {
            dateStr = d.toString();
        }
        return dateStr;
    }

    static public String convertDateToStringOtherFormat(Date d) {
        String dateStr = "";
        try {
            DateFormat df = new SimpleDateFormat("E, dd/M/yy, hh:mm: a");
            dateStr = df.format(d);
        } catch (Exception e) {
            dateStr = d.toString();
        }
        return dateStr;
    }

    static public String convertDateToStringOnlyDate(Date d) {
        String dateStr = "";
        try {
            DateFormat df = new SimpleDateFormat("dd/M/yy");
            dateStr = df.format(d);
        } catch (Exception e) {
            dateStr = d.toString();
        }
        return dateStr;
    }

    static public Date convertStringToDate(String d) {
        Date date = null;
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
        }
    }

    static public void sleepFor1Sec() {
        sleepInMilliSec(1000);
    }

    static public void sleepForHalfSec() {
        sleepInMilliSec(500);
    }
    static public void sleepFor100Sec() {
        sleepInMilliSec(100);
    }

    public static void log(String msg)
    {
       Log.i("JKEEPASS", msg);
    }
}
