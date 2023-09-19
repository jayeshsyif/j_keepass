package org.j_keepass.util;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.slider.Slider;

import org.j_keepass.R;

public class NewPasswordDialogUtil {

    static public void show(LayoutInflater layoutInflater, View mView, ClipboardManager clipboard) {
        BottomSheetDialog bsd = new BottomSheetDialog(mView.getContext());
        bsd.setContentView(R.layout.new_password_layout);
        bsd.show();
        String pwd = new PasswordGenerator().generate(20);
        TextView pwdF = bsd.findViewById(R.id.newPassword);
        pwdF.setText(pwd);

        ImageButton copyBtn = bsd.findViewById(R.id.newPasswordCopy);
        copyBtn.setOnClickListener(v -> {
            if (pwdF.getText() != null) {
                ClipData clip = ClipData.newPlainText("password", pwdF.getText().toString());
                clipboard.setPrimaryClip(clip);
                ToastUtil.showToast(layoutInflater, v, R.string.copiedToClipboard);
            }
        });
        MaterialCheckBox useDigit = bsd.findViewById(R.id.useDigit);
        MaterialCheckBox useLowerCase = bsd.findViewById(R.id.useLowerCase);
        MaterialCheckBox useUpperCase = bsd.findViewById(R.id.useUpperCase);
        MaterialCheckBox useSymbol = bsd.findViewById(R.id.useSymbol);

        Slider slider = bsd.findViewById(R.id.newPasswordSlider);
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
