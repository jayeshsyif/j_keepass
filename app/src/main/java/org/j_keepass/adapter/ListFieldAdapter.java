package org.j_keepass.adapter;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.j_keepass.databinding.FieldItemViewBinding;
import org.j_keepass.fragments.entry.dtos.FieldData;
import org.j_keepass.fragments.entry.dtos.FieldValueType;
import org.j_keepass.util.CopyUtil;

import java.util.ArrayList;
import java.util.List;


public class ListFieldAdapter extends RecyclerView.Adapter<ListFieldAdapter.ViewHolder> {

    List<FieldData> mValues = new ArrayList<>();
    private boolean isEditable = false;

    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
    }

    public void addValue(FieldData fieldData) {
        mValues.add(fieldData);
    }

    public void setValues(List<FieldData> mValues) {
        this.mValues = mValues;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ListFieldAdapter.ViewHolder(FieldItemViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        if (!isEditable) {
            holder.editText.setEnabled(false);
            holder.editText.setHint(holder.mItem.name);
            holder.editText.setText(holder.mItem.value);
            holder.editTextLayout.setHint(holder.mItem.name);
            if (!holder.mItem.fieldValueType.name().equals(FieldValueType.PASSWORD.name())) {
                holder.editTextLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
                holder.editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            if (holder.mItem.fieldValueType.name().equals(FieldValueType.ATTACHMENT.name())) {
                holder.fieldCopy.setVisibility(View.GONE);
            }
            holder.fieldCopy.setOnClickListener(view -> {
                CopyUtil.copyToClipboard(view.getContext(), holder.mItem.name, holder.mItem.value);
            });
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public FieldData mItem;
        TextInputEditText editText;
        TextInputLayout editTextLayout;
        ImageButton fieldCopy;

        public ViewHolder(@NonNull FieldItemViewBinding binding) {
            super(binding.getRoot());
            editText = binding.fieldValue;
            editTextLayout = binding.fieldNameValue;
            fieldCopy = binding.fieldCopy;
        }
    }
}
