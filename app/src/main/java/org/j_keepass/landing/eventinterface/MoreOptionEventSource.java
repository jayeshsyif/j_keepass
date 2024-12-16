package org.j_keepass.landing.eventinterface;

import android.content.Context;

import org.j_keepass.util.Util;

import java.util.ArrayList;

public class MoreOptionEventSource {
    private static final MoreOptionEventSource SOURCE = new MoreOptionEventSource();

    private MoreOptionEventSource() {

    }

    public static MoreOptionEventSource getInstance() {
        return SOURCE;
    }

    private ArrayList<MoreOptionsEvent> listeners = new ArrayList<>();

    public void addListener(MoreOptionsEvent listener) {
        listeners.add(listener);
    }

    public void removeListener(MoreOptionsEvent listener) {
        listeners.remove(listener);
    }

    public void showMenu(Context context) {
        Util.log("In listener got show menu " + listeners.size());
        for (MoreOptionsEvent listener : listeners) {
            listener.showMenu(context);
        }
    }

    public void changeThemeIsClickedShowThemes(Context context) {
        Util.log("In listener got show menu");
        for (MoreOptionsEvent listener : listeners) {
            listener.changeThemeIsClickedShowThemes(context);
        }
    }

    public void showCreateNewDb(Context context) {
        Util.log("In listener got show Create New Db with context");
        for (MoreOptionsEvent listener : listeners) {
            listener.showCreateNewDb(context);
        }
    }

    public void showNewPwd(String newPwd, boolean useDigit, boolean useLowerCase, boolean useUpperCase, boolean useSymbol, int length) {
        Util.log("In listener show new pwd");
        for (MoreOptionsEvent listener : listeners) {
            listener.showNewPwd(newPwd, useDigit, useLowerCase, useUpperCase, useSymbol, length);
        }
    }

    public void showFailedNewGenPwd(String errorMsg) {
        Util.log("In listener show failed gen pwd");
        for (MoreOptionsEvent listener : listeners) {
            listener.showFailedNewGenPwd(errorMsg);
        }
    }

    public void showInfo(Context context) {
        Util.log("In listener got show Create New Db with context");
        for (MoreOptionsEvent listener : listeners) {
            listener.showInfo(context);
        }
    }
}
