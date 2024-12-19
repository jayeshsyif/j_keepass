package org.j_keepass.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import org.j_keepass.R;
import org.j_keepass.databinding.ListGroupEntriyItemViewBinding;
import org.j_keepass.fragments.listdatabase.dtos.GroupEntryData;
import org.j_keepass.fragments.listdatabase.dtos.GroupEntryStatus;
import org.j_keepass.fragments.listdatabase.dtos.GroupEntryType;
import org.j_keepass.groupentry.eventinterface.GroupEntryEventSource;

import java.util.ArrayList;
import java.util.List;


public class ListGroupEntryAdapter extends RecyclerView.Adapter<ListGroupEntryAdapter.ViewHolder> {

    List<GroupEntryData> mValues = new ArrayList<>();
    static final String SUB_DIRECTORY_ARROW_SYMBOL_CODE = " \u21B3 ";

    public void addValue(GroupEntryData groupEntryData) {
        mValues.add(groupEntryData);
    }

    public void setValues(List<GroupEntryData> mValues) {
        this.mValues = mValues;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ListGroupEntryAdapter.ViewHolder(ListGroupEntriyItemViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.name.setText(holder.mItem.name);
        if (holder.mItem.type.name().toString().equals(GroupEntryType.DUMMY.name().toString())) {
            holder.groupEntryNameCardView.setVisibility(View.INVISIBLE);
        } else if (holder.mItem.type.name().toString().equals(GroupEntryType.ENTRY.name().toString())) {
            holder.groupEntryImage.setImageResource(R.drawable.ic_key_fill1_wght300_grad_25_opsz24);
            holder.groupEntryImage.setColorFilter(ContextCompat.getColor(holder.groupEntryImage.getContext(), R.color.kp_green_2));
            if (holder.mItem.status.name().equals(GroupEntryStatus.EXPIRED.name())) {
                String text = holder.groupEntryCountOrStatus.getContext().getString(R.string.expiredInDays);
                text = text.replace("{0}", "" + holder.mItem.daysToExpire);
                holder.groupEntryCountOrStatus.setText(text);
                holder.groupEntryCountOrStatus.setTextColor(holder.groupEntryCountOrStatus.getContext().getResources().getColor(R.color.kp_red));
            } else if (holder.mItem.status.name().equals(GroupEntryStatus.EXPIRNG_SOON.name())) {
                String text = holder.groupEntryCountOrStatus.getContext().getString(R.string.expiringSoonInDays);
                text = text.replace("{0}", "" + holder.mItem.daysToExpire);
                holder.groupEntryCountOrStatus.setText(text);
                holder.groupEntryCountOrStatus.setTextColor(holder.groupEntryCountOrStatus.getContext().getResources().getColor(R.color.kp_coral));
            } else if (holder.mItem.status.name().equals(GroupEntryStatus.OK.name())) {
                String text = holder.groupEntryCountOrStatus.getContext().getString(R.string.expiringInDays);
                text = text.replace("{0}", "" + holder.mItem.daysToExpire);
                holder.groupEntryCountOrStatus.setText(text);
            }
        } else if (holder.mItem.type.name().toString().equals(GroupEntryType.GROUP.name().toString())) {
            holder.groupEntryCountOrStatus.setText("" + holder.mItem.subCount + SUB_DIRECTORY_ARROW_SYMBOL_CODE);
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
        public GroupEntryData mItem;
        TextView name, groupEntryCountOrStatus;
        CardView groupEntryNameCardView;
        ShapeableImageView groupEntryImage;

        public ViewHolder(@NonNull ListGroupEntriyItemViewBinding binding) {
            super(binding.getRoot());
            name = binding.groupName;
            groupEntryCountOrStatus = binding.groupEntryCountOrStatus;
            groupEntryNameCardView = binding.groupEntryNameCardView;
            groupEntryImage = binding.groupEntryImage;
            groupEntryNameCardView.setOnClickListener(view -> {
                if (mItem.type.name().equals(GroupEntryType.GROUP.name())) {
                    GroupEntryEventSource.getInstance().setGroup(mItem.id);
                } else if (mItem.type.name().equals(GroupEntryType.ENTRY.name())) {
                    GroupEntryEventSource.getInstance().setEntry(mItem.id);
                }
            });
        }
    }
}
