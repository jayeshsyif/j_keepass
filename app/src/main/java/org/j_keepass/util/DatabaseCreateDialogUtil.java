package org.j_keepass.util;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.j_keepass.R;

public class DatabaseCreateDialogUtil {

    static public Quadruple<BottomSheetDialog, MaterialButton, TextInputEditText, TextInputEditText> getConfirmDialog(LayoutInflater layoutInflater, Context context) {
        BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.database_name_password_dialog_layout);
        Quadruple<BottomSheetDialog, MaterialButton, TextInputEditText, TextInputEditText> quadruple = new Quadruple<>();
        try {

            MaterialButton save = bsd.findViewById(R.id.saveDatabase);

            quadruple.second = save;
            quadruple.third = bsd.findViewById(R.id.databaseName);
            quadruple.fourth = bsd.findViewById(R.id.databasePassword);
        } catch (Exception e) {
        }
        quadruple.first = bsd;
        return quadruple;
    }

    static public void showDialog(AlertDialog alertDialog) {
        try {
            alertDialog.show();
        } catch (Exception e) {
            // do nothing
        }
    }

    static public Triplet<BottomSheetDialog, MaterialButton, TextInputEditText> getConfirmDialogEditName(Context context, String value) {
        BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.database_name_password_dialog_layout);
        Triplet<BottomSheetDialog, MaterialButton, TextInputEditText> triplet = new Triplet<>();
        try {

            LinearLayout databasePasswordLayout = bsd.findViewById(R.id.databasePasswordLayout);
            databasePasswordLayout.setVisibility(View.GONE);
            MaterialButton save = bsd.findViewById(R.id.saveDatabase);
            triplet.second = save;
            triplet.third = bsd.findViewById(R.id.databaseName);
            triplet.third.setText(value);
        } catch (Exception e) {
        }
        triplet.first = bsd;
        return triplet;
    }

    static public Quadruple<BottomSheetDialog, MaterialButton, TextInputEditText, TextInputEditText> getConfirmDialogChangePassword(String name, Context context) {
        BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.database_change_password_dialog_layout);
        Quadruple<BottomSheetDialog, MaterialButton, TextInputEditText, TextInputEditText> quadruple = new Quadruple();
        try {

            MaterialButton save = bsd.findViewById(R.id.saveDatabase);
            quadruple.second = save;
            quadruple.third = bsd.findViewById(R.id.databaseOldPassword);
            quadruple.fourth = bsd.findViewById(R.id.databaseNewPassword);
            ((TextView) bsd.findViewById(R.id.nameMenuText)).setText(name);
        } catch (Exception e) {
        }
        quadruple.first = bsd;
        return quadruple;
    }
}
