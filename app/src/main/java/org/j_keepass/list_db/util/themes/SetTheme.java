package org.j_keepass.list_db.util.themes;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import org.j_keepass.events.themes.ThemeEvent;
import org.j_keepass.util.Utils;

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
        Utils.log("sTheme is: " + sThemeId);
        changeToTheme(activity, sThemeId);
    }

    private String loadFromShare() {
        String sThemeId = "wbs";
        try {
            SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            String savedSTheme = sharedPref.getString("sTheme", null);
            Utils.log("sTheme from shared: " + savedSTheme);
            if (savedSTheme != null) {
                sThemeId = savedSTheme;
            }
        } catch (Exception e) {
            Utils.log("sTheme error is: " + e.getMessage());
        }
        return sThemeId;
    }

    private void changeToTheme(Activity activity, String sThemeId) {
        Utils.log("Change theme is called with : " + sThemeId);
        Theme themeToApply = ThemeUtil.getThemes().get(sThemeId);
        ((ThemeEvent) activity).applyTheme(themeToApply, false);
        Utils.log("Done Changing theme is called with : " + sThemeId);
    }
}
