package org.j_keepass.events.loading;

import org.j_keepass.util.Utils;

import java.util.ArrayList;

public class LoadingEventSource {
    private static final LoadingEventSource SOURCE = new LoadingEventSource();

    private LoadingEventSource() {

    }

    public static LoadingEventSource getInstance() {
        return SOURCE;
    }

    private final ArrayList<LoadingEvent> listeners = new ArrayList<>();

    public void addListener(LoadingEvent listener) {
        listeners.add(listener);
    }

    public void removeListener(LoadingEvent listener) {
        listeners.remove(listener);
    }

    public void showLoading() {
        Utils.log("In listener got show loading");
        for (LoadingEvent listener : listeners) {
            listener.showLoading();
        }
    }

    public void dismissLoading() {
        Utils.log("In listener got show loading");
        for (LoadingEvent listener : listeners) {
            listener.dismissLoading();
        }
    }

    public void updateLoadingText(String text) {
        Utils.log("In listener got update loading with text");
        for (LoadingEvent listener : listeners) {
            listener.updateLoadingText(text);
        }
    }
}
