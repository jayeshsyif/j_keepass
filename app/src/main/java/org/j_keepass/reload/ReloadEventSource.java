package org.j_keepass.reload;

import org.j_keepass.util.Util;

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
        listeners.add(listener);
    }

    public void removeListener(ReloadEvent listener) {
        listeners.remove(listener);
    }

    public void reload() {
        Util.log("In listener reload");
        for (ReloadEvent listener : listeners) {
            listener.reload();
        }
    }
}
