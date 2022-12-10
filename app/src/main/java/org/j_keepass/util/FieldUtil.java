package org.j_keepass.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.j_keepass.R;

public class FieldUtil {

    public static View getTextField(LayoutInflater inflater, String hint, String value) {
        View viewToLoad = inflater.inflate(R.layout.field_layout, null);
        ImageButton copy = viewToLoad.findViewById(R.id.fieldCopy);
        LinearLayout copyLayout = viewToLoad.findViewById(R.id.copyLayout);
        copyLayout.setVisibility(View.GONE);
        TextInputLayout fieldText = viewToLoad.findViewById(R.id.fieldText);
        fieldText.setEndIconMode(TextInputLayout.END_ICON_NONE);
        fieldText.setHint(hint);
        TextInputEditText field = viewToLoad.findViewById(R.id.field);
        field.setEnabled(false);
        field.setInputType(InputType.TYPE_NULL);
        field.setTransformationMethod(null);
        field.setHint(hint);
        field.setText(value);
        return  viewToLoad;
    }

    public static View getTextFieldWithCopy(LayoutInflater inflater,  String hint, String value, ClipboardManager clipboard, String copiedToClipboardString) {
        View viewToLoad = inflater.inflate(R.layout.field_layout, null);
        ImageButton copy = viewToLoad.findViewById(R.id.fieldCopy);
        LinearLayout copyLayout = viewToLoad.findViewById(R.id.copyLayout);
        copyLayout.setVisibility(View.VISIBLE);
        TextInputLayout fieldText = viewToLoad.findViewById(R.id.fieldText);
        fieldText.setEndIconMode(TextInputLayout.END_ICON_NONE);
        fieldText.setHint(hint);
        TextInputEditText field = viewToLoad.findViewById(R.id.field);
        field.setEnabled(false);
        field.setInputType(InputType.TYPE_NULL);
        field.setTransformationMethod(null);
        //field.setHint(hint);
        field.setText(value);

        copy.setOnClickListener(v -> {
            if (value != null) {
                ClipData clip = ClipData.newPlainText(hint, value);
                clipboard.setPrimaryClip(clip);
                ToastUtil.showToast(inflater, v, hint+" "+copiedToClipboardString);
            }
        });

        return  viewToLoad;
    }

    public static View getPasswordFieldWithCopy(LayoutInflater inflater,  String hint, String value, ClipboardManager clipboard, String copiedToClipboardString) {
        View viewToLoad = inflater.inflate(R.layout.field_layout, null);
        ImageButton copy = viewToLoad.findViewById(R.id.fieldCopy);
        LinearLayout copyLayout = viewToLoad.findViewById(R.id.copyLayout);
        copyLayout.setVisibility(View.VISIBLE);
        TextInputLayout fieldText = viewToLoad.findViewById(R.id.fieldText);
        fieldText.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        fieldText.setHint(hint);
        TextInputEditText field = viewToLoad.findViewById(R.id.field);
        field.setEnabled(false);
        field.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        //field.setHint(hint);
        field.setText(value);

        copy.setOnClickListener(v -> {
            if (value != null) {
                ClipData clip = ClipData.newPlainText(hint, value);
                clipboard.setPrimaryClip(clip);
                ToastUtil.showToast(inflater, v, hint+" "+copiedToClipboardString);
            }
        });

        return  viewToLoad;
    }

    public static View getMultiLineTextFieldWithCopy(LayoutInflater inflater,  String hint, String value, ClipboardManager clipboard, String copiedToClipboardString) {
        View viewToLoad = inflater.inflate(R.layout.field_layout, null);
        ImageButton copy = viewToLoad.findViewById(R.id.fieldCopy);
        LinearLayout copyLayout = viewToLoad.findViewById(R.id.copyLayout);
        copyLayout.setVisibility(View.VISIBLE);
        TextInputLayout fieldText = viewToLoad.findViewById(R.id.fieldText);
        fieldText.setEndIconMode(TextInputLayout.END_ICON_NONE);
        fieldText.setHint(hint);
        TextInputEditText field = viewToLoad.findViewById(R.id.field);
        field.setEnabled(false);
        field.setLines(10);
        field.setInputType(InputType.TYPE_NULL);
        field.setTransformationMethod(null);
        //field.setHint(hint);
        field.setText(value);

        copy.setOnClickListener(v -> {
            if (value != null) {
                ClipData clip = ClipData.newPlainText(hint, value);
                clipboard.setPrimaryClip(clip);
                ToastUtil.showToast(inflater, v, hint+" "+copiedToClipboardString);
            }
        });

        return  viewToLoad;
    }

    public static Pair<View, TextInputEditText> getEditTextField(LayoutInflater inflater, String hint, String value) {
        Pair<View, TextInputEditText> pair = new Pair<View, TextInputEditText>();
        View viewToLoad = inflater.inflate(R.layout.field_layout, null);
        ImageButton copy = viewToLoad.findViewById(R.id.fieldCopy);
        LinearLayout copyLayout = viewToLoad.findViewById(R.id.copyLayout);
        copyLayout.setVisibility(View.GONE);
        TextInputLayout fieldText = viewToLoad.findViewById(R.id.fieldText);
        fieldText.setEndIconMode(TextInputLayout.END_ICON_NONE);
        fieldText.setHint(hint);
        TextInputEditText field = viewToLoad.findViewById(R.id.field);
        field.setInputType(InputType.TYPE_CLASS_TEXT);
        field.setTransformationMethod(null);
        //field.setHint(hint);
        field.setText(value);
        pair.first = viewToLoad;
        pair.second = field;
        return  pair;
    }
}
