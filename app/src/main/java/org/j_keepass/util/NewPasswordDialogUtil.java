package org.j_keepass.util;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.textfield.TextInputEditText;

import org.j_keepass.R;

public class NewPasswordDialogUtil {
    static public AlertDialog getDialog(LayoutInflater layoutInflater, Context
            context, ClipboardManager clipboard) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        Pair<AlertDialog, TextInputEditText> pair = new Pair<AlertDialog, TextInputEditText>();
        AlertDialog alertDialog = null;
        try {

            View mView = layoutInflater.inflate(R.layout.new_password_layout, null);
            alert.setView(mView);
            alertDialog = alert.create();
            pair.first = alertDialog;
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.getWindow().getAttributes().windowAnimations = BaseTransientBottomBar.ANIMATION_MODE_SLIDE;
            alertDialog.getWindow().setGravity(Gravity.CENTER);
            //((ScrollView) mView.findViewById(R.id.confirmScrollView)).setAnimation(AnimationUtils.makeInAnimation(layoutInflater.getContext(), true));
            String pwd = new PasswordGenerator().generate(20);
            TextInputEditText pwdF = mView.findViewById(R.id.newPassword);
            pair.second = pwdF;
            pwdF.setText(pwd);

            mView.findViewById(R.id.newPasswordFloatCloseBtn).setOnClickListener(v -> {
                pair.first.dismiss();
            });

            ImageButton copyBtn = mView.findViewById(R.id.newPasswordCopy);
            copyBtn.setOnClickListener(v -> {
                if (pwdF.getText() != null) {
                    ClipData clip = ClipData.newPlainText("password", pwdF.getText().toString());
                    clipboard.setPrimaryClip(clip);
                    ToastUtil.showToast(layoutInflater, v, R.string.copiedToClipboard);
                }
            });
            MaterialCheckBox useDigit = mView.findViewById(R.id.useDigit);
            MaterialCheckBox useLowerCase = mView.findViewById(R.id.useLowerCase);
            MaterialCheckBox useUpperCase = mView.findViewById(R.id.useUpperCase);
            MaterialCheckBox useSymbol = mView.findViewById(R.id.useSymbol);

            Slider slider = mView.findViewById(R.id.newPasswordSlider);
            slider.addOnChangeListener(new Slider.OnChangeListener() {
                @SuppressLint("RestrictedApi")
                @Override
                public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                    if (isAllFalse(useDigit, useLowerCase, useUpperCase, useSymbol)) {
                        ToastUtil.showToast(layoutInflater, mView, R.string.newPasswordAllFalse);
                    } else {
                        pwdF.setText(getPassword(useDigit, useLowerCase, useUpperCase, useSymbol, slider));
                    }
                }
            });
            useDigit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isAllFalse(useDigit, useLowerCase, useUpperCase, useSymbol)) {
                        ToastUtil.showToast(layoutInflater, mView, R.string.newPasswordAllFalse);
                    } else {
                        pwdF.setText(getPassword(useDigit, useLowerCase, useUpperCase, useSymbol, slider));
                    }
                }
            });
            useLowerCase.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isAllFalse(useDigit, useLowerCase, useUpperCase, useSymbol)) {
                        ToastUtil.showToast(layoutInflater, mView, R.string.newPasswordAllFalse);
                    } else {
                        pwdF.setText(getPassword(useDigit, useLowerCase, useUpperCase, useSymbol, slider));
                    }
                }
            });
            useUpperCase.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isAllFalse(useDigit, useLowerCase, useUpperCase, useSymbol)) {
                        ToastUtil.showToast(layoutInflater, mView, R.string.newPasswordAllFalse);
                    } else {
                        pwdF.setText(getPassword(useDigit, useLowerCase, useUpperCase, useSymbol, slider));
                    }
                }
            });
            useSymbol.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isAllFalse(useDigit, useLowerCase, useUpperCase, useSymbol)) {
                        ToastUtil.showToast(layoutInflater, mView, R.string.newPasswordAllFalse);
                    } else {
                        pwdF.setText(getPassword(useDigit, useLowerCase, useUpperCase, useSymbol, slider));
                    }
                }
            });
        } catch (Exception e) {
            alertDialog = alert.create();
        }
        pair.first = alertDialog;
        return alertDialog;
    }

    static public void showDialog(AlertDialog alertDialog) {
        try {
            alertDialog.show();
        } catch (Exception e) {
            // do nothing
        }
    }

    private static String getPassword(MaterialCheckBox useDigit, MaterialCheckBox useLowerCase, MaterialCheckBox useUpperCase, MaterialCheckBox useSymbol, Slider slider) {
        String pwd = "";
        PasswordGenerator.PasswordGeneratorBuilder pgb = new PasswordGenerator.PasswordGeneratorBuilder();
        pgb.useLower(useLowerCase.isChecked());
        pgb.useUpper(useUpperCase.isChecked());
        pgb.useDigits(useDigit.isChecked());
        pgb.usePunctuation(useSymbol.isChecked());
        PasswordGenerator pg = new PasswordGenerator(pgb);
        return pg.generate(new Float(slider.getValue()).intValue());
    }

    private static boolean isAllFalse(MaterialCheckBox useDigit, MaterialCheckBox useLowerCase, MaterialCheckBox useUpperCase, MaterialCheckBox useSymbol) {
        if (!useDigit.isChecked() && !useLowerCase.isChecked() && !useUpperCase.isChecked() && !useSymbol.isChecked()) {
            return true;
        } else {
            return false;
        }
    }
}
