package org.j_keepass.listgroupentry.eventinterface;

import android.content.Context;

import org.j_keepass.util.Util;

import java.util.ArrayList;

public class MoreOptionEventSource {
    private static final MoreOptionEventSource SOURCE = new MoreOptionEventSource();

    private MoreOptionEventSource() {

    }
    public static MoreOptionEventSource getInstance() {
        return SOURCE;
    }

    private ArrayList<MoreOptionsEvent> listeners = new ArrayList<>();

    public void addListener(MoreOptionsEvent listener) {
        listeners.add(listener);
    }

    public void removeListener(MoreOptionsEvent listener) {
        listeners.remove(listener);
    }

    public void showMenu(Context context) {
        Util.log("In listener got show menu " + listeners.size());
        for (MoreOptionsEvent listener : listeners) {
            listener.showMenu(context);
        }
    }
}
