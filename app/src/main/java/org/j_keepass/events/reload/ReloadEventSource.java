package org.j_keepass.events.reload;

import org.j_keepass.util.Utils;

import java.util.ArrayList;

public class ReloadEventSource {
    private static final ReloadEventSource SOURCE = new ReloadEventSource();

    private ReloadEventSource() {

    }

    public static ReloadEventSource getInstance() {
        return SOURCE;
    }

    private ArrayList<ReloadEvent> listeners = new ArrayList<>();

    public void addListener(ReloadEvent listener) {
        Utils.log("In listener reload, adding listener "+ listener.getClass().getName());
        listeners.add(listener);
        Utils.log("In listener reload, count "+ listeners.size());
    }

    public void removeListener(ReloadEvent listener) {
        listeners.remove(listener);
    }

    public void reload(ReloadEvent.ReloadAction reloadAction) {
        Utils.log("In listener reload, count "+ listeners.size());
        for (ReloadEvent listener : listeners) {
            Utils.log("In listener reload, listener is  "+ listener.getClass().getName());
            listener.reload(reloadAction);
        }
    }
}
