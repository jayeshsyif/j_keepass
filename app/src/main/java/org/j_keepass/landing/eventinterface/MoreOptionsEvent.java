package org.j_keepass.landing.eventinterface;

import android.content.Context;

public interface MoreOptionsEvent {
    void showMenu(Context context);
    void changeThemeIsClickedShowThemes(Context context);
    void showCreateNewDb(Context context);
    void showNewPwd(String newPwd,boolean useDigit, boolean useLowerCase, boolean useUpperCase, boolean useSymbol, int length);
    void showFailedNewGenPwd(String errorMsg);
    void showInfo(Context context);
}
