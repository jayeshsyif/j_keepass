package org.j_keepass.util;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.j_keepass.R;

import java.util.Random;

public class FieldUtil {

    public View getTextField(LayoutInflater inflater, String hint, String value) {
        final View viewToLoad = inflater.inflate(R.layout.field_layout, null);
        final ImageButton copy = viewToLoad.findViewById(R.id.fieldCopy);
        final LinearLayout copyLayout = viewToLoad.findViewById(R.id.copyLayout);
        copyLayout.setVisibility(View.GONE);
        final TextInputLayout fieldText = viewToLoad.findViewById(R.id.fieldText);
        fieldText.setEndIconMode(TextInputLayout.END_ICON_NONE);
        fieldText.setHint(hint);
        final TextInputEditText field = viewToLoad.findViewById(R.id.field);
        field.setEnabled(false);
        field.setInputType(InputType.TYPE_NULL);
        field.setTransformationMethod(null);
        field.setHint(hint);
        field.setText(value);
        LinearLayout wholeFieldLayout = viewToLoad.findViewById(R.id.wholeFieldLayout);
        @SuppressLint("ResourceType") LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(inflater.getContext(), R.animator.anim_bottom), Common.ANIMATION_TIME); //0.5f == time between appearance of listview items.
        wholeFieldLayout.setLayoutAnimation(lac);
        wholeFieldLayout.startLayoutAnimation();
        return viewToLoad;
    }

    public View getTextFieldWithCopy(LayoutInflater inflater, String hint, String value, ClipboardManager clipboard, String copiedToClipboardString) {
        final View viewToLoad = inflater.inflate(R.layout.field_layout, null);
        final ImageButton copy = viewToLoad.findViewById(R.id.fieldCopy);
        final LinearLayout copyLayout = viewToLoad.findViewById(R.id.copyLayout);
        copyLayout.setVisibility(View.VISIBLE);
        final TextInputLayout fieldText = viewToLoad.findViewById(R.id.fieldText);
        fieldText.setId(new Random().nextInt());
        fieldText.setEndIconMode(TextInputLayout.END_ICON_NONE);
        fieldText.setHint(hint);
        final TextInputEditText field = viewToLoad.findViewById(R.id.field);
        field.setId(new Random().nextInt());
        field.setEnabled(false);
        field.setText(value);
        field.setInputType(InputType.TYPE_NULL);
        field.setTransformationMethod(null);
        //field.setHint(hint);

        copy.setOnClickListener(v -> {
            if (value != null) {
                ClipData clip = ClipData.newPlainText(hint, value);
                clipboard.setPrimaryClip(clip);
                ToastUtil.showToast(inflater, v, hint + " " + copiedToClipboardString);
            }
        });
        LinearLayout wholeFieldLayout = viewToLoad.findViewById(R.id.wholeFieldLayout);
        @SuppressLint("ResourceType") LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(inflater.getContext(), R.animator.anim_bottom), Common.ANIMATION_TIME); //0.5f == time between appearance of listview items.
        wholeFieldLayout.setLayoutAnimation(lac);
        wholeFieldLayout.startLayoutAnimation();
        return viewToLoad;
    }

    public View getPasswordFieldWithCopy(LayoutInflater inflater, String hint, String value, ClipboardManager clipboard, String copiedToClipboardString) {
        final View viewToLoad = inflater.inflate(R.layout.field_layout, null);
        final ImageButton copy = viewToLoad.findViewById(R.id.fieldCopy);
        final LinearLayout copyLayout = viewToLoad.findViewById(R.id.copyLayout);
        copyLayout.setVisibility(View.VISIBLE);
        final TextInputLayout fieldText = viewToLoad.findViewById(R.id.fieldText);
        fieldText.setId(new Random().nextInt());
        fieldText.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        fieldText.setHint(hint);
        final TextInputEditText field = viewToLoad.findViewById(R.id.field);
        field.setId(new Random().nextInt());
        field.setEnabled(false);
        field.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        //field.setHint(hint);
        field.setText(value);

        copy.setOnClickListener(v -> {
            if (value != null) {
                ClipData clip = ClipData.newPlainText(hint, value);
                clipboard.setPrimaryClip(clip);
                ToastUtil.showToast(inflater, v, hint + " " + copiedToClipboardString);
            }
        });
        LinearLayout wholeFieldLayout = viewToLoad.findViewById(R.id.wholeFieldLayout);
        @SuppressLint("ResourceType") LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(inflater.getContext(), R.animator.anim_bottom), Common.ANIMATION_TIME); //0.5f == time between appearance of listview items.
        wholeFieldLayout.setLayoutAnimation(lac);
        wholeFieldLayout.startLayoutAnimation();
        return viewToLoad;
    }

    public View getMultiLineTextFieldWithCopy(LayoutInflater inflater, String hint, String value, ClipboardManager clipboard, String copiedToClipboardString) {
        final View viewToLoad = inflater.inflate(R.layout.field_layout, null);
        final ImageButton copy = viewToLoad.findViewById(R.id.fieldCopy);
        final LinearLayout copyLayout = viewToLoad.findViewById(R.id.copyLayout);
        copyLayout.setVisibility(View.VISIBLE);
        final TextInputLayout fieldText = viewToLoad.findViewById(R.id.fieldText);
        fieldText.setId(new Random().nextInt());
        fieldText.setEndIconMode(TextInputLayout.END_ICON_NONE);
        fieldText.setHint(hint);
        final TextInputEditText field = viewToLoad.findViewById(R.id.field);
        field.setId(new Random().nextInt());
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
                ToastUtil.showToast(inflater, v, hint + " " + copiedToClipboardString);
            }
        });
        LinearLayout wholeFieldLayout = viewToLoad.findViewById(R.id.wholeFieldLayout);
        @SuppressLint("ResourceType") LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(inflater.getContext(), R.animator.anim_bottom), Common.ANIMATION_TIME); //0.5f == time between appearance of listview items.
        wholeFieldLayout.setLayoutAnimation(lac);
        wholeFieldLayout.startLayoutAnimation();
        return viewToLoad;
    }

    public Pair<View, TextInputEditText> getEditTextField(LayoutInflater inflater, String hint, String value) {
        final Pair<View, TextInputEditText> pair = new Pair<View, TextInputEditText>();
        final View viewToLoad = inflater.inflate(R.layout.field_layout, null);
        final ImageButton copy = viewToLoad.findViewById(R.id.fieldCopy);
        final LinearLayout copyLayout = viewToLoad.findViewById(R.id.copyLayout);
        copyLayout.setVisibility(View.GONE);
        final TextInputLayout fieldText = viewToLoad.findViewById(R.id.fieldText);
        fieldText.setId(new Random().nextInt());
        fieldText.setEndIconMode(TextInputLayout.END_ICON_NONE);
        fieldText.setHint(hint);
        final TextInputEditText field = viewToLoad.findViewById(R.id.field);
        field.setTag(hint);
        field.setId(new Random().nextInt());
        field.setInputType(InputType.TYPE_CLASS_TEXT);
        field.setTransformationMethod(null);
        //field.setHint(hint);
        field.setText(value);
        LinearLayout wholeFieldLayout = viewToLoad.findViewById(R.id.wholeFieldLayout);
        @SuppressLint("ResourceType") LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(inflater.getContext(), R.animator.anim_bottom), Common.ANIMATION_TIME); //0.5f == time between appearance of listview items.
        wholeFieldLayout.setLayoutAnimation(lac);
        wholeFieldLayout.startLayoutAnimation();
        pair.first = viewToLoad;
        pair.second = field;
        return pair;
    }

    public Pair<View, TextInputEditText> getEditPasswordField(LayoutInflater inflater, String hint, String value) {
        final Pair<View, TextInputEditText> pair = new Pair<View, TextInputEditText>();
        final View viewToLoad = inflater.inflate(R.layout.field_layout, null);
        final ImageButton copy = viewToLoad.findViewById(R.id.fieldCopy);
        final LinearLayout copyLayout = viewToLoad.findViewById(R.id.copyLayout);
        copyLayout.setVisibility(View.GONE);
        final TextInputLayout fieldText = viewToLoad.findViewById(R.id.fieldText);
        fieldText.setId(new Random().nextInt());
        fieldText.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        fieldText.setHint(hint);
        final TextInputEditText field = viewToLoad.findViewById(R.id.field);
        field.setTag(hint);
        field.setId(new Random().nextInt());
        field.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        field.setTransformationMethod(null);
        //field.setHint(hint);
        field.setText(value);
        LinearLayout wholeFieldLayout = viewToLoad.findViewById(R.id.wholeFieldLayout);
        @SuppressLint("ResourceType") LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(inflater.getContext(), R.animator.anim_bottom), Common.ANIMATION_TIME); //0.5f == time between appearance of listview items.
        wholeFieldLayout.setLayoutAnimation(lac);
        wholeFieldLayout.startLayoutAnimation();
        pair.first = viewToLoad;
        pair.second = field;
        return pair;
    }

    public Pair<View, TextInputEditText> getMultiEditTextField(LayoutInflater inflater, String hint, String value) {
        final Pair<View, TextInputEditText> pair = new Pair<View, TextInputEditText>();
        final View viewToLoad = inflater.inflate(R.layout.field_layout, null);
        final ImageButton copy = viewToLoad.findViewById(R.id.fieldCopy);
        final LinearLayout copyLayout = viewToLoad.findViewById(R.id.copyLayout);
        copyLayout.setVisibility(View.GONE);
        final TextInputLayout fieldText = viewToLoad.findViewById(R.id.fieldText);
        fieldText.setId(new Random().nextInt());
        fieldText.setEndIconMode(TextInputLayout.END_ICON_NONE);
        fieldText.setHint(hint);
        final TextInputEditText field = viewToLoad.findViewById(R.id.field);
        field.setTag(hint);
        field.setId(new Random().nextInt());
        field.setLines(10);
        field.setSingleLine(false);
        field.setInputType(InputType.TYPE_CLASS_TEXT);
        field.setTransformationMethod(null);
        //field.setHint(hint);
        field.setText(value);
        LinearLayout wholeFieldLayout = viewToLoad.findViewById(R.id.wholeFieldLayout);
        @SuppressLint("ResourceType") LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(inflater.getContext(), R.animator.anim_bottom), Common.ANIMATION_TIME); //0.5f == time between appearance of listview items.
        wholeFieldLayout.setLayoutAnimation(lac);
        wholeFieldLayout.startLayoutAnimation();
        pair.first = viewToLoad;
        pair.second = field;
        return pair;
    }

    public Triplet<View, TextInputEditText, ImageButton> getAdditionalEditTextField(LayoutInflater inflater, String hint, String value) {
        final Triplet<View, TextInputEditText, ImageButton> triplet = new Triplet<View, TextInputEditText, ImageButton>();
        final View viewToLoad = inflater.inflate(R.layout.additional_field_layout, null);
        final TextInputLayout additionalFieldNameTextLayout = viewToLoad.findViewById(R.id.additionalFieldNameTextInputLayout);
        additionalFieldNameTextLayout.setId(new Random().nextInt());
        additionalFieldNameTextLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
        final TextInputEditText additionalFieldName = viewToLoad.findViewById(R.id.additionalFieldNameEditText);
        additionalFieldName.setId(new Random().nextInt());
        additionalFieldName.setInputType(InputType.TYPE_CLASS_TEXT);
        additionalFieldName.setTransformationMethod(null);
        //additionalFieldName.setHint(hint);
        additionalFieldName.setText(value);

        final TextInputLayout additionalFieldValueTextLayout = viewToLoad.findViewById(R.id.additionalFieldValueTextInputLayout);
        additionalFieldValueTextLayout.setId(new Random().nextInt());
        additionalFieldValueTextLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
        additionalFieldValueTextLayout.setHint(hint);
        final TextInputEditText additionalFieldValue = viewToLoad.findViewById(R.id.additionalFieldValueEditText);
        additionalFieldValue.setTag(hint);
        additionalFieldValue.setId(new Random().nextInt());
        additionalFieldValue.setInputType(InputType.TYPE_CLASS_TEXT);
        additionalFieldValue.setTransformationMethod(null);
        //field.setHint(hint);
        additionalFieldValue.setText(value);

        additionalFieldName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                additionalFieldValueTextLayout.setHint("Enter value for: " + additionalFieldName.getText().toString());
                additionalFieldValue.setTag(additionalFieldName.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        LinearLayout wholeAdditionalFieldLayout = viewToLoad.findViewById(R.id.wholeAdditionalFieldLayout);
        @SuppressLint("ResourceType") LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(inflater.getContext(), R.animator.anim_bottom), Common.ANIMATION_TIME); //0.5f == time between appearance of listview items.
        wholeAdditionalFieldLayout.setLayoutAnimation(lac);
        wholeAdditionalFieldLayout.startLayoutAnimation();

        triplet.first = viewToLoad;
        triplet.second = additionalFieldValue;
        triplet.third = viewToLoad.findViewById(R.id.fieldDelete);

        return triplet;
    }
}
