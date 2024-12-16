package org.j_keepass.loading.eventinterface;

import android.content.Context;

import org.j_keepass.util.Util;

import java.util.ArrayList;

public class LoadingEventSource {
    private static final LoadingEventSource SOURCE = new LoadingEventSource();

    private LoadingEventSource() {

    }

    public static LoadingEventSource getInstance() {
        return SOURCE;
    }

    private ArrayList<LoadingEvent> listeners = new ArrayList<>();

    public void addListener(LoadingEvent listener) {
        listeners.add(listener);
    }

    public void removeListener(LoadingEvent listener) {
        listeners.remove(listener);
    }

    public void showLoading() {
        Util.log("In listener got show loading");
        for (LoadingEvent listener : listeners) {
            listener.showLoading();
        }
    }

    public void dismissLoading() {
        Util.log("In listener got show loading");
        for (LoadingEvent listener : listeners) {
            listener.dismissLoading();
        }
    }

    public void updateLoadingText(String text) {
        Util.log("In listener got show loading");
        for (LoadingEvent listener : listeners) {
            listener.updateLoadingText(text);
        }
    }
}
