package org.j_keepass.util;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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

    static public boolean isUsable(String str)
    {
        if( str == null || str.length() <= 0)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

}
