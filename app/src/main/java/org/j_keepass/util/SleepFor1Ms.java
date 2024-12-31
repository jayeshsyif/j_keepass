package org.j_keepass.util;

public class SleepFor1Ms implements Runnable{

    @Override
    public void run() {
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            Utils.ignoreError(e);
        }
    }
}
