package org.j_keepass.util;


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
}
