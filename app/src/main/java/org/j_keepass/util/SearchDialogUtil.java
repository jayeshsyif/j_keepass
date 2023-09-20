package org.j_keepass.util;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.j_keepass.R;

public class SearchDialogUtil {

    static public Triplet<BottomSheetDialog, MaterialButton, TextInputEditText> getSearchDialog(LayoutInflater layoutInflater, Context
            context) {
        BottomSheetDialog bsd = new BottomSheetDialog(context);
        Triplet<BottomSheetDialog, MaterialButton, TextInputEditText> triplet = new Triplet<BottomSheetDialog, MaterialButton, TextInputEditText>();
        try {

            bsd.setContentView(R.layout.search_layout);
            MaterialButton yes = bsd.findViewById(R.id.confirmSearch);
            TextInputEditText searchText = bsd.findViewById(R.id.searchText);
            triplet.second = yes;
            triplet.third = searchText;
        } catch (Exception e) {

        }
        triplet.first = bsd;
        return triplet;
    }

    static public void showDialog(AlertDialog alertDialog) {
        try {
            alertDialog.show();
        } catch (Exception e) {
            // do nothing
        }
    }
}
