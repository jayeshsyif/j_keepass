package org.j_keepass.landing.eventinterface;

import android.content.Context;

public interface MoreOptionsEvent {
    void showMenu(Context context);
    void changeThemeIsClickedShowThemes(Context context);
    void showCreateNewDb(Context context);
    void showInfo(Context context);
}
