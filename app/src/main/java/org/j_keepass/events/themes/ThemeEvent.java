package org.j_keepass.events.themes;

import org.j_keepass.list_db.util.themes.Theme;

public interface ThemeEvent {
    void applyTheme(Theme theme, boolean updatePref);
}
