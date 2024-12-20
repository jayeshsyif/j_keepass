package org.j_keepass.theme.event;

import org.j_keepass.util.theme.Theme;

public interface ThemeEvent {
    void applyTheme(Theme theme, boolean updatePref);
}
