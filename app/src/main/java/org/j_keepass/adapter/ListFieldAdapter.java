package org.j_keepass.adapter;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.j_keepass.R;
import org.j_keepass.databinding.FieldItemViewBinding;
import org.j_keepass.fragments.entry.dtos.FieldData;
import org.j_keepass.fragments.entry.dtos.FieldNameType;
import org.j_keepass.fragments.entry.dtos.FieldValueType;
import org.j_keepass.fragments.listdatabase.dtos.GroupEntryStatus;
import org.j_keepass.util.CopyUtil;
import org.j_keepass.util.DateAndTimePickerUtil;
import org.j_keepass.util.Pair;
import org.j_keepass.util.Util;
import org.j_keepass.util.db.Db;

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
        holder.editText.setHint(holder.mItem.name);
        holder.editText.setText(holder.mItem.value);
        holder.editTextLayout.setHint(holder.mItem.name);

        boolean isDummy = holder.mItem.fieldValueType == FieldValueType.DUMMY;
        boolean isCreated = holder.mItem.fieldNameType == FieldNameType.CREATED_DATE;
        boolean isExpiryDate = holder.mItem.fieldNameType == FieldNameType.EXPIRY_DATE;
        boolean isCreatedOrExpiryDate = isCreated || isExpiryDate;
        boolean isAttachment = holder.mItem.fieldValueType == FieldValueType.ATTACHMENT;
        boolean isPassword = holder.mItem.fieldValueType == FieldValueType.PASSWORD;
        boolean isLargeText = holder.mItem.fieldValueType == FieldValueType.LARGE_TEXT;
        boolean isDateOtherThenCreateAndExpire = holder.mItem.fieldNameType == FieldNameType.DATE;

        if (isDummy) {
            holder.editText.setEnabled(false);
            holder.fieldCardView.setVisibility(View.INVISIBLE);
        } else {
            holder.editText.setEnabled(isEditable);
            holder.fieldCopy.setVisibility(isEditable && !isAttachment ? View.VISIBLE : View.GONE);

            if (isCreatedOrExpiryDate || isDateOtherThenCreateAndExpire) {
                holder.editText.setEnabled(false);
                holder.editTextLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
                holder.editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                if (isExpiryDate) {
                    Util.setExpiryText(holder.expiryStatus, Db.getInstance().getStatus(holder.mItem.expiryDate));
                    if (isEditable) {
                        holder.fieldCopy.setImageDrawable(holder.fieldCopy.getResources().getDrawable(R.drawable.ic_calendar_month_fill0_wght300_grad_25_opsz24));
                        holder.fieldCopy.setOnClickListener(view -> new DateAndTimePickerUtil().showDateAndTimePicker(holder.editText, holder.mItem.expiryDate));
                    }
                } else {
                    holder.fieldCopy.setVisibility(View.GONE);
                }
            } else {
                if (!isPassword) {
                    holder.editTextLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
                    holder.editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }
            }

            if (isLargeText) {
                holder.editText.setLines(10);
                holder.editText.setEms(10);
                holder.editText.setSingleLine(false);
                holder.editText.setInputType(InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
                holder.editText.setHorizontallyScrolling(false);
                holder.editText.setMaxLines(Integer.MAX_VALUE);
            }

            if (!isEditable && !isAttachment && !isCreatedOrExpiryDate && !isDateOtherThenCreateAndExpire) {
                holder.fieldCopy.setVisibility(View.VISIBLE);
                holder.fieldCopy.setOnClickListener(view -> CopyUtil.copyToClipboard(view.getContext(), holder.mItem.name, holder.mItem.value));
            }
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
        CardView fieldCardView;
        TextView expiryStatus;

        public ViewHolder(@NonNull FieldItemViewBinding binding) {
            super(binding.getRoot());
            editText = binding.fieldValue;
            editTextLayout = binding.fieldNameValue;
            fieldCopy = binding.fieldCopy;
            fieldCardView = binding.fieldCardView;
            expiryStatus = binding.expiryStatus;
        }
    }
}
