package org.j_keepass.util.theme;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import org.j_keepass.theme.event.ThemeEvent;
import org.j_keepass.util.Util;

public class SetTheme {
    Activity activity;
    boolean loadFromShare;
    public static String sThemeId = "wbs";


    public SetTheme(Activity activity, boolean loadFromShare) {
        this.activity = activity;
        this.loadFromShare = loadFromShare;
    }


    public void run() {
        if (loadFromShare) {
            sThemeId = loadFromShare();
        }
        Util.log("sTheme is: " + sThemeId);
        changeToTheme(activity, sThemeId);
    }

    private String loadFromShare() {
        String sThemeId = "wbs";
        try {
            SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            String savedSTheme = sharedPref.getString("sTheme", null);
            Util.log("sTheme from shared: " + savedSTheme);
            if (savedSTheme != null) {
                sThemeId = savedSTheme;
            }
        } catch (Exception e) {
            Util.log("sTheme error is: " + e.getMessage());
        }
        return sThemeId;
    }

    private void changeToTheme(Activity activity, String sThemeId) {
        Util.log("Change theme is called with : " + sThemeId);
        Theme themeToApply = ThemeUtil.getThemes().get(sThemeId);
        ((ThemeEvent) activity).applyTheme(themeToApply, false);
        Util.log("Done Changing theme is called with : " + sThemeId);
    }
}
