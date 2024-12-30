package org.j_keepass.list_db.util.themes;

import androidx.appcompat.app.AppCompatDelegate;

import org.j_keepass.R;
import org.j_keepass.util.Utils;

import java.util.HashMap;

public class ThemeUtil {
    public static HashMap<String, Theme> getThemes() {
        HashMap<String, Theme> themesMap = new HashMap<>();
        int pos = 1;

        // Define themes using a helper method
        addTheme(themesMap, "wbs", pos++, AppCompatDelegate.MODE_NIGHT_NO, R.style.Theme_J_KeePass_wbs, true, R.color.kp_wbs_light_white, R.color.kp_wbs_blue);
        addTheme(themesMap, "wb", pos++, AppCompatDelegate.MODE_NIGHT_NO, R.style.Theme_J_KeePass_wb, true, R.color.kp_wb_light_white, R.color.kp_wb_blue);
        addTheme(themesMap, "wrs", pos++, AppCompatDelegate.MODE_NIGHT_NO, R.style.Theme_J_KeePass_wrs, true, R.color.kp_wrs_light_white, R.color.kp_wrs_blue);
        addTheme(themesMap, "wr", pos++, AppCompatDelegate.MODE_NIGHT_NO, R.style.Theme_J_KeePass_wr, true, R.color.kp_wr_light_white, R.color.kp_wr_red);
        addTheme(themesMap, "wl", pos++, AppCompatDelegate.MODE_NIGHT_NO, R.style.Theme_J_KeePass_wl, true, R.color.kp_wl_light_white, R.color.kp_wl_lemon);
        addTheme(themesMap, "bl", pos++, AppCompatDelegate.MODE_NIGHT_YES, R.style.Theme_J_KeePass_bl, false, R.color.kp_bl_black, R.color.kp_bl_lemon);
        addTheme(themesMap, "ws", pos++, AppCompatDelegate.MODE_NIGHT_NO, R.style.Theme_J_KeePass_ws, true, R.color.kp_ws_light_white, R.color.kp_ws_blue);
        addTheme(themesMap, "bs", pos++, AppCompatDelegate.MODE_NIGHT_YES, R.style.Theme_J_KeePass_bs, false, R.color.kp_bs_black, R.color.kp_bs_green);
        addTheme(themesMap, "br", pos++, AppCompatDelegate.MODE_NIGHT_YES, R.style.Theme_J_KeePass_br, false, R.color.kp_br_black, R.color.kp_br_red);
        addTheme(themesMap, "brs", pos++, AppCompatDelegate.MODE_NIGHT_YES, R.style.Theme_J_KeePass_brs, false, R.color.kp_brs_black, R.color.kp_brs_blue);
        addTheme(themesMap, "bb", pos++, AppCompatDelegate.MODE_NIGHT_YES, R.style.Theme_J_KeePass_br, false, R.color.kp_bb_dark_blue, R.color.kp_bb_blue);
        addTheme(themesMap, "bbs", pos++, AppCompatDelegate.MODE_NIGHT_YES, R.style.Theme_J_KeePass_bbs, false, R.color.kp_bbs_black, R.color.kp_bbs_blue);
        addTheme(themesMap, "bg", pos++, AppCompatDelegate.MODE_NIGHT_YES, R.style.Theme_J_KeePass_bg, false, R.color.kp_bg_black, R.color.kp_bg_green);
        addTheme(themesMap, "p", pos++, AppCompatDelegate.MODE_NIGHT_NO, R.style.Theme_J_KeePass_p, true, R.color.kp_p_light_pink, R.color.kp_p_pink);
        Utils.log("Max theme pos is " + pos);
        return themesMap;
    }

    private static void addTheme(HashMap<String, Theme> themesMap,
                                 String id,
                                 int pos,
                                 int mode,
                                 int resId,
                                 boolean isLightTheme,
                                 int color1,
                                 int color2) {
        Theme theme = new Theme();
        theme.setId(id);
        theme.setPos(pos);
        theme.setMode(mode);
        theme.setResId(resId);
        theme.setLightTheme(isLightTheme);
        theme.setColor1(color1);
        theme.setColor2(color2);
        themesMap.put(theme.getId(), theme);
    }
}
