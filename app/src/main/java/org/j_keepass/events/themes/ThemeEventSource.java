package org.j_keepass.events.themes;


import org.j_keepass.util.Utils;
import org.j_keepass.list_db.util.themes.Theme;

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
        Utils.log("In listener got show loading");
        for (ThemeEvent listener : listeners) {
            listener.applyTheme(theme, true);
        }
    }
}
