package org.j_keepass.fields.adapters;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.j_keepass.R;
import org.j_keepass.databinding.FieldItemViewBinding;
import org.j_keepass.db.operation.Db;
import org.j_keepass.events.loading.LoadingEventSource;
import org.j_keepass.events.newpwd.GenerateNewPasswordEventSource;
import org.j_keepass.events.reload.ReloadEvent;
import org.j_keepass.events.reload.ReloadEventSource;
import org.j_keepass.fields.dtos.FieldData;
import org.j_keepass.fields.enums.FieldNameType;
import org.j_keepass.fields.enums.FieldValueType;
import org.j_keepass.util.ByteArrayOpener;
import org.j_keepass.util.CopyUtil;
import org.j_keepass.util.DateAndTimePickerUtil;
import org.j_keepass.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ListFieldAdapter extends RecyclerView.Adapter<ListFieldAdapter.ViewHolder> {

    final List<FieldData> mValues = new ArrayList<>();
    private boolean isEditable = false;

    public void setEditable(boolean editable) {
        isEditable = editable;
    }

    public void addValue(FieldData fieldData) {
        mValues.add(fieldData);
    }

    public ListFieldAdapterAnimator getItemAnimator() {
        return new ListFieldAdapterAnimator();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ListFieldAdapter.ViewHolder(FieldItemViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        if (holder.mItem.value != null && !holder.mItem.value.isEmpty()) {
            holder.editText.setHint(holder.mItem.name);
            holder.editText.setText(holder.mItem.value);
        }
        holder.editTextLayout.setHint(holder.mItem.name);

        boolean isDummy = holder.mItem.fieldValueType == FieldValueType.DUMMY;
        boolean isCreated = holder.mItem.fieldNameType == FieldNameType.CREATED_DATE;
        boolean isExpiryDate = holder.mItem.fieldNameType == FieldNameType.EXPIRY_DATE;
        boolean isCreatedOrExpiryDate = isCreated || isExpiryDate;
        boolean isAttachment = holder.mItem.fieldValueType == FieldValueType.ATTACHMENT;
        boolean isPassword = holder.mItem.fieldValueType == FieldValueType.PASSWORD;
        boolean isLargeText = holder.mItem.fieldValueType == FieldValueType.LARGE_TEXT;
        boolean isDateOtherThenCreateAndExpire = holder.mItem.fieldNameType == FieldNameType.DATE;
        boolean isAdditionalProp = holder.mItem.fieldNameType == FieldNameType.ADDITIONAL;
        if (isDummy) {
            setDummyView(holder);
        } else {
            configureEditText(holder, isEditable);
            holder.fieldCopy.setVisibility(isEditable && !isCreatedOrExpiryDate ? View.VISIBLE : View.GONE);

            Utils.log("Got " + holder.mItem.name);

            if (isCreatedOrExpiryDate || isDateOtherThenCreateAndExpire) {
                configureForDate(holder, isExpiryDate, isEditable);
            } else if (isAttachment) {
                configureForAttachment(holder, isEditable);
            } else if (isAdditionalProp && isEditable) {
                configureForAdditionalProp(holder);
            } else {
                configureForOtherTypes(holder, isPassword, isEditable);
            }

            if (isLargeText) {
                configureLargeText(holder);
            }

            if (!isEditable && !isCreatedOrExpiryDate && !isDateOtherThenCreateAndExpire) {
                Utils.log("Got !editable - " + holder.mItem.name);
                holder.fieldCopy.setVisibility(View.VISIBLE);
                if (!isAttachment) {
                    holder.fieldCopy.setOnClickListener(view -> CopyUtil.copyToClipboard(view.getContext(), holder.mItem.name, holder.mItem.value));
                }
            }

            if (isEditable) {
                setupEditTextFocusListener(holder);
            }
        }
    }

    private void configureForAdditionalProp(ViewHolder holder) {
        holder.editText.setEnabled(false);
        holder.editTextLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
        holder.editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        holder.fieldCopy.setImageDrawable(AppCompatResources.getDrawable(holder.fieldCopy.getContext(), R.drawable.ic_delete_fill0_wght300_grad_25_opsz24));
        holder.fieldCopy.setOnClickListener(view -> {
            String addingStr = view.getContext().getString(R.string.deleting);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                LoadingEventSource.getInstance().updateLoadingText(addingStr);
                LoadingEventSource.getInstance().showLoading();
                Db.getInstance().addEntryDeleteAdditionalProperty(Db.getInstance().getCurrentEntryId(), holder.mItem.name);
                ReloadEventSource.getInstance().reload(ReloadEvent.ReloadAction.ENTRY_PROP_UPDATE);
            });
        });
    }

    private void setDummyView(ViewHolder holder) {
        holder.editText.setEnabled(false);
        holder.fieldCardView.setVisibility(View.INVISIBLE);
    }

    private void configureEditText(ViewHolder holder, boolean isEditable) {
        holder.editText.setEnabled(isEditable);
    }

    private void configureForDate(ViewHolder holder, boolean isExpiryDate, boolean isEditable) {
        holder.editText.setEnabled(false);
        holder.editText.setFocusable(false);
        holder.editText.setClickable(false);
        holder.editTextLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
        holder.editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        if (isExpiryDate) {
            Utils.setExpiryText(holder.expiryStatus, Db.getInstance().getStatus(holder.mItem.expiryDate));
            if (isEditable) {
                setupFieldCopyForExpiry(holder);
            }
        } else {
            Utils.log("Got and hiding copy for " + holder.mItem.name);
            holder.fieldCopy.setVisibility(View.GONE);
        }
    }

    private void setupFieldCopyForExpiry(ViewHolder holder) {
        holder.fieldCopy.setVisibility(View.VISIBLE);
        holder.fieldCopy.setImageDrawable(AppCompatResources.getDrawable(holder.fieldCopy.getContext(), R.drawable.ic_calendar_month_fill0_wght300_grad_25_opsz24));
        holder.fieldCopy.setOnClickListener(view -> new DateAndTimePickerUtil().showDateAndTimePicker(holder.editText, holder.mItem.expiryDate, holder.mItem));
    }

    private void configureForAttachment(ViewHolder holder, boolean isEditable) {
        holder.editText.setEnabled(false);
        holder.editTextLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
        holder.editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        if (isEditable) {
            holder.fieldCopy.setImageDrawable(AppCompatResources.getDrawable(holder.fieldCopy.getContext(), R.drawable.ic_delete_fill0_wght300_grad_25_opsz24));
            holder.fieldCopy.setOnClickListener(view -> {
                String deletingStr = view.getContext().getString(R.string.deleting);
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    LoadingEventSource.getInstance().updateLoadingText(deletingStr);
                    LoadingEventSource.getInstance().showLoading();
                    Db.getInstance().deleteEntryBinaryProperty(Db.getInstance().getCurrentEntryId(), holder.mItem.value);
                    ReloadEventSource.getInstance().reload(ReloadEvent.ReloadAction.ENTRY_PROP_UPDATE);
                });
            });
        } else {
            holder.fieldCopy.setImageDrawable(AppCompatResources.getDrawable(holder.fieldCopy.getContext(), R.drawable.ic_open_with_fill0_wght300_grad_25_opsz24));
            holder.fieldCopy.setOnClickListener(view -> {
                String openingStr = view.getContext().getString(R.string.opening);
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    LoadingEventSource.getInstance().updateLoadingText(openingStr);
                    LoadingEventSource.getInstance().showLoading();
                    ByteArrayOpener.openByteArrayWithOtherApp(view.getContext(), holder.mItem.fileInBytes, holder.mItem.value);
                    LoadingEventSource.getInstance().dismissLoading();
                });
            });
        }
    }

    private void configureForOtherTypes(ViewHolder holder, boolean isPassword, boolean isEditable) {
        if (isPassword && isEditable) {
            Utils.log("Got other pwd as " + holder.mItem.name);
            setupFieldCopyForPassword(holder);
        } else {
            Utils.log("Got other than pwd as " + holder.mItem.name);
            holder.editTextLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
            holder.editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            holder.fieldCopy.setVisibility(View.GONE);
        }
    }

    private void setupFieldCopyForPassword(ViewHolder holder) {
        holder.fieldCopy.setImageDrawable(AppCompatResources.getDrawable(holder.fieldCopy.getContext(), R.drawable.ic_password_fill0_wght300_grad_25_opsz24));

        // Use a single thread executor for loading operations
        ExecutorService executor = Executors.newSingleThreadExecutor();

        holder.fieldCopy.setOnClickListener(view -> executor.execute(() -> {
            LoadingEventSource.getInstance().updateLoadingText(view.getContext().getString(R.string.generatingNewPwd));
            LoadingEventSource.getInstance().showLoading();
            GenerateNewPasswordEventSource.getInstance().generateNewPwd();
        }));
    }

    private void configureLargeText(ViewHolder holder) {
        holder.editText.setLines(10);
        holder.editText.setEms(10);
        holder.editText.setSingleLine(false);
        holder.editText.setInputType(InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
        holder.editText.setHorizontallyScrolling(false);
        holder.editText.setMaxLines(Integer.MAX_VALUE);
    }

    private void setupEditTextFocusListener(ViewHolder holder) {
        Utils.log("Got editable last - " + holder.mItem.name);
        holder.editText.setOnKeyListener((view, i, keyEvent) -> keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
        holder.editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (holder.editText.getText() != null) {
                    Utils.log("Calling update field Value for " + holder.mItem.asString());
                    holder.mItem.value = holder.editText.getText().toString();
                    Db.getInstance().updateEntryField(holder.mItem.eId, holder.mItem);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

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

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        public FieldData mItem;
        final TextInputEditText editText;
        final TextInputLayout editTextLayout;
        final ImageButton fieldCopy;
        final CardView fieldCardView;
        final TextView expiryStatus;

        public ViewHolder(@NonNull FieldItemViewBinding binding) {
            super(binding.getRoot());
            editText = binding.fieldValue;
            editTextLayout = binding.fieldNameValue;
            fieldCopy = binding.fieldCopy;
            fieldCardView = binding.fieldCardView;
            expiryStatus = binding.expiryStatus;
        }
    }

    protected static class ListFieldAdapterAnimator extends DefaultItemAnimator {

        @Override
        public boolean animateAdd(RecyclerView.ViewHolder holder) {
            setAnimation(((ListFieldAdapter.ViewHolder) holder).fieldCardView);
            return false;
        }

        private void setAnimation(CardView view) {
            LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(view.getContext(), R.anim.anim_bottom), 0.5f); //0.5f == time between appearance of listview items.
            view.setLayoutAnimation(lac);
        }
    }
}
