package org.j_keepass;

import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import org.j_keepass.databinding.ListGroupEntryActivityLayoutBinding;
import org.j_keepass.theme.eventinterface.ThemeEvent;
import org.j_keepass.util.Util;
import org.j_keepass.util.db.Db;
import org.j_keepass.util.theme.SetTheme;
import org.j_keepass.util.theme.Theme;

public class ListGroupEntriesActivity extends AppCompatActivity implements ThemeEvent {

    private ListGroupEntryActivityLayoutBinding binding;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new SetTheme(this, true).run();
        binding = ListGroupEntryActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.groupName.setText(Db.getInstance().getRootGroupName());
    }

    @Override
    public void applyTheme(Theme theme, boolean updatePref) {
        runOnUiThread(() -> {
            Util.log("Applying theme " + theme.asString());
            setTheme(theme.getResId());
            AppCompatDelegate.setDefaultNightMode(theme.getMode());
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (theme.isLightTheme()) {
                            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                        }
                    }
                }
            } catch (Exception e) {
                Util.log("Error setting light status " + e.getMessage());
            }
        });
    }
}
