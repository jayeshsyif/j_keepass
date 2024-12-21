package org.j_keepass.list_db.util.themes;

import androidx.appcompat.app.AppCompatDelegate;

import org.j_keepass.R;

import java.util.HashMap;

public class ThemeUtil {
    public static HashMap<String, Theme> getThemes() {
        HashMap<String, Theme> themesMap = new HashMap<>();
        int pos = 1;
        {
            Theme theme = new Theme();
            theme.setId("wbs");
            pos++;
            theme.setPos(pos);
            theme.setMode(AppCompatDelegate.MODE_NIGHT_NO);
            theme.setResId(R.style.Theme_J_KeePass_wbs);
            theme.setLightTheme(true);
            theme.setColor1(R.color.kp_wbs_light_white);
            theme.setColor2(R.color.kp_wbs_blue);
            themesMap.put(theme.getId(), theme);
        }
        {
            Theme theme = new Theme();
            theme.setId("wb");
            pos++;
            theme.setPos(pos);
            theme.setMode(AppCompatDelegate.MODE_NIGHT_NO);
            theme.setResId(R.style.Theme_J_KeePass_wb);
            theme.setLightTheme(true);
            theme.setColor1(R.color.kp_wb_light_white);
            theme.setColor2(R.color.kp_wb_blue);
            themesMap.put(theme.getId(), theme);
        }
        {
            Theme theme = new Theme();
            theme.setId("wrs");
            pos++;
            theme.setPos(pos);
            theme.setMode(AppCompatDelegate.MODE_NIGHT_NO);
            theme.setResId(R.style.Theme_J_KeePass_wrs);
            theme.setLightTheme(true);
            theme.setColor1(R.color.kp_wrs_light_white);
            theme.setColor2(R.color.kp_wrs_blue);
            themesMap.put(theme.getId(), theme);
        }
        {
            Theme theme = new Theme();
            theme.setId("wr");
            pos++;
            theme.setPos(pos);
            theme.setMode(AppCompatDelegate.MODE_NIGHT_NO);
            theme.setResId(R.style.Theme_J_KeePass_wr);
            theme.setLightTheme(true);
            theme.setColor1(R.color.kp_wr_light_white);
            theme.setColor2(R.color.kp_wr_red);
            themesMap.put(theme.getId(), theme);
        }
        {
            Theme theme = new Theme();
            theme.setId("wl");
            pos++;
            theme.setPos(pos);
            theme.setMode(AppCompatDelegate.MODE_NIGHT_NO);
            theme.setResId(R.style.Theme_J_KeePass_wl);
            theme.setColor1(R.color.kp_wl_light_white);
            theme.setColor2(R.color.kp_wl_lemon);
            theme.setLightTheme(true);
            themesMap.put(theme.getId(), theme);
        }
        {
            Theme theme = new Theme();
            theme.setId("bl");
            pos++;
            theme.setPos(pos);
            theme.setMode(AppCompatDelegate.MODE_NIGHT_YES);
            theme.setResId(R.style.Theme_J_KeePass_bl);
            theme.setLightTheme(false);
            theme.setColor1(R.color.kp_bl_black);
            theme.setColor2(R.color.kp_bl_lemon);
            themesMap.put(theme.getId(), theme);
        }
        {
            Theme theme = new Theme();
            theme.setId("ws");
            pos++;
            theme.setPos(pos);
            theme.setMode(AppCompatDelegate.MODE_NIGHT_NO);
            theme.setResId(R.style.Theme_J_KeePass_ws);
            theme.setLightTheme(true);
            theme.setColor1(R.color.kp_ws_light_white);
            theme.setColor2(R.color.kp_ws_blue);
            themesMap.put(theme.getId(), theme);
        }
        {
            Theme theme = new Theme();
            theme.setId("bs");
            pos++;
            theme.setPos(pos);
            theme.setMode(AppCompatDelegate.MODE_NIGHT_YES);
            theme.setResId(R.style.Theme_J_KeePass_bs);
            theme.setLightTheme(false);
            theme.setColor1(R.color.kp_bs_black);
            theme.setColor2(R.color.kp_bs_green);
            themesMap.put(theme.getId(), theme);
        }
        {
            Theme theme = new Theme();
            theme.setId("br");
            pos++;
            theme.setPos(pos);
            theme.setMode(AppCompatDelegate.MODE_NIGHT_YES);
            theme.setResId(R.style.Theme_J_KeePass_br);
            theme.setLightTheme(false);
            theme.setColor1(R.color.kp_br_black);
            theme.setColor2(R.color.kp_br_red);
            themesMap.put(theme.getId(), theme);
        }
        {
            Theme theme = new Theme();
            theme.setId("brs");
            pos++;
            theme.setPos(pos);
            theme.setMode(AppCompatDelegate.MODE_NIGHT_YES);
            theme.setResId(R.style.Theme_J_KeePass_brs);
            theme.setLightTheme(false);
            theme.setColor1(R.color.kp_brs_black);
            theme.setColor2(R.color.kp_brs_blue);
            themesMap.put(theme.getId(), theme);
        }
        {
            Theme theme = new Theme();
            theme.setId("bb");
            pos++;
            theme.setPos(pos);
            theme.setMode(AppCompatDelegate.MODE_NIGHT_YES);
            theme.setResId(R.style.Theme_J_KeePass_br);
            theme.setLightTheme(false);
            theme.setColor1(R.color.kp_bb_dark_blue);
            theme.setColor2(R.color.kp_bb_blue);
            themesMap.put(theme.getId(), theme);
        }
        {
            Theme theme = new Theme();
            theme.setId("bbs");
            pos++;
            theme.setPos(pos);
            theme.setMode(AppCompatDelegate.MODE_NIGHT_YES);
            theme.setResId(R.style.Theme_J_KeePass_bbs);
            theme.setLightTheme(false);
            theme.setColor1(R.color.kp_bbs_black);
            theme.setColor2(R.color.kp_bbs_blue);
            themesMap.put(theme.getId(), theme);
        }

        {
            Theme theme = new Theme();
            theme.setId("bg");
            pos++;
            theme.setPos(pos);
            theme.setMode(AppCompatDelegate.MODE_NIGHT_YES);
            theme.setResId(R.style.Theme_J_KeePass_bg);
            theme.setLightTheme(false);
            theme.setColor1(R.color.kp_bg_black);
            theme.setColor2(R.color.kp_bg_green);
            themesMap.put(theme.getId(), theme);
        }
        {
            Theme theme = new Theme();
            theme.setId("p");
            pos++;
            theme.setPos(pos);
            theme.setMode(AppCompatDelegate.MODE_NIGHT_NO);
            theme.setResId(R.style.Theme_J_KeePass_p);
            theme.setLightTheme(true);
            theme.setColor1(R.color.kp_p_light_pink);
            theme.setColor2(R.color.kp_p_pink);
            themesMap.put(theme.getId(), theme);
        }
        return themesMap;
    }
}
