package org.j_keepass.util;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.j_keepass.R;

import java.util.ArrayList;

public class BottomMenuUtil {

    public static Pair<BottomSheetDialog, ArrayList<LinearLayout>> getDbMenuOptions(String dbName, Context context) {
        Pair<BottomSheetDialog, ArrayList<LinearLayout>> res = new Pair<>();
        BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.bottom_diaglog_menu);
        ArrayList<LinearLayout> options = new ArrayList<>();
        options.add(bsd.findViewById(R.id.editMenuLinearLayout));
        options.add(bsd.findViewById(R.id.changePwdMenuLinearLayout));
        options.add(bsd.findViewById(R.id.deleteMenuLinearLayout));
        bsd.findViewById(R.id.addEntryMenuLinearLayout).setVisibility(View.GONE);
        bsd.findViewById(R.id.addGroupMenuLinearLayout).setVisibility(View.GONE);
        ((TextView) bsd.findViewById(R.id.nameMenuText)).setText(dbName);
        res.first = bsd;
        res.second = options;
        return res;
    }

    public static Pair<BottomSheetDialog, ArrayList<LinearLayout>> getEntryAndGroupMenuOptions(String name, Context context) {
        Pair<BottomSheetDialog, ArrayList<LinearLayout>> res = new Pair<>();
        BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.bottom_diaglog_menu);
        ArrayList<LinearLayout> options = new ArrayList<>();
        bsd.findViewById(R.id.changePwdMenuLinearLayout).setVisibility(View.GONE);
        bsd.findViewById(R.id.addEntryMenuLinearLayout).setVisibility(View.GONE);
        bsd.findViewById(R.id.addGroupMenuLinearLayout).setVisibility(View.GONE);
        options.add(bsd.findViewById(R.id.editMenuLinearLayout));
        options.add(bsd.findViewById(R.id.deleteMenuLinearLayout));
        bsd.findViewById(R.id.moveMenuLinearLayout).setVisibility(View.VISIBLE);
        options.add(bsd.findViewById(R.id.moveMenuLinearLayout));
        ((TextView) bsd.findViewById(R.id.nameMenuText)).setText(name);
        res.first = bsd;
        res.second = options;
        return res;
    }

    public static Pair<BottomSheetDialog, ArrayList<LinearLayout>> getAddNewEntryAndGroupMenuOptions(Context context) {
        Pair<BottomSheetDialog, ArrayList<LinearLayout>> res = new Pair<>();
        BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.bottom_diaglog_menu);
        res.first = bsd;
        ArrayList<LinearLayout> options = new ArrayList<>();
        options.add(bsd.findViewById(R.id.addGroupMenuLinearLayout));
        options.add(bsd.findViewById(R.id.addEntryMenuLinearLayout));
        bsd.findViewById(R.id.editMenuLinearLayout).setVisibility(View.GONE);
        bsd.findViewById(R.id.changePwdMenuLinearLayout).setVisibility(View.GONE);
        bsd.findViewById(R.id.deleteMenuLinearLayout).setVisibility(View.GONE);
        bsd.findViewById(R.id.nameMenuLinearLayout).setVisibility(View.GONE);
        res.second = options;
        return res;
    }
}
