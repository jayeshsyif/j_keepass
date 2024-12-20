package org.j_keepass.theme.event;


import org.j_keepass.util.Util;
import org.j_keepass.util.theme.Theme;

import java.util.ArrayList;

public class ThemeEventSource {
    private static final ThemeEventSource SOURCE = new ThemeEventSource();

    private ThemeEventSource() {

    }

    public static ThemeEventSource getInstance() {
        return SOURCE;
    }

    private ArrayList<ThemeEvent> listeners = new ArrayList<>();

    public void addListener(ThemeEvent listener) {
        listeners.add(listener);
    }

    public void removeListener(ThemeEvent listener) {
        listeners.remove(listener);
    }
    public void applyTheme(Theme theme){
        Util.log("In listener got show loading");
        for (ThemeEvent listener : listeners) {
            listener.applyTheme(theme, true);
        }
    }
}
