package org.j_keepass.list_db.util.themes;

import androidx.appcompat.app.AppCompatDelegate;

import org.j_keepass.R;

public class Theme {

    private String id = "wbs";
    private int resId = R.style.Theme_J_KeePass_wbs, mode = AppCompatDelegate.MODE_NIGHT_NO;
    private boolean isLightTheme = true;
    private int pos = 1;

    private int color1 = R.color.kp_wbs_light_white;
    private int color2 = R.color.kp_wbs_blue;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public boolean isLightTheme() {
        return isLightTheme;
    }

    public void setLightTheme(boolean lightTheme) {
        isLightTheme = lightTheme;
    }

    public int getColor1() {
        return color1;
    }

    public void setColor1(int color1) {
        this.color1 = color1;
    }

    public int getColor2() {
        return color2;
    }

    public void setColor2(int color2) {
        this.color2 = color2;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public String asString() {
        return "Theme{" + "id='" + id + '\'' + ", resId=" + resId + ", mode=" + mode + ", isLightTheme=" + isLightTheme + ", pos=" + pos + ", color1=" + color1 + ", color2=" + color2 + '}';
    }
}
