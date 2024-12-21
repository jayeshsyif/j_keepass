package org.j_keepass.list_group_and_entry.adapters;

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
import org.j_keepass.events.changeactivity.ChangeActivityEvent;
import org.j_keepass.events.changeactivity.ChangeActivityEventSource;
import org.j_keepass.databinding.ListGroupAndEntriesItemViewBinding;
import org.j_keepass.db.event.operations.Db;
import org.j_keepass.events.reload.ReloadEvent;
import org.j_keepass.events.reload.ReloadEventSource;
import org.j_keepass.list_db.dtos.GroupEntryData;
import org.j_keepass.list_db.dtos.GroupEntryStatus;
import org.j_keepass.list_db.dtos.GroupEntryType;
import org.j_keepass.util.Pair;
import org.j_keepass.util.Utils;

import java.util.ArrayList;
import java.util.List;


public class ListGroupsAndEntriesAdapter extends RecyclerView.Adapter<ListGroupsAndEntriesAdapter.ViewHolder> {

    List<GroupEntryData> mValues = new ArrayList<>();
    private static final String SUB_DIRECTORY_ARROW_SYMBOL_CODE = " \u21B3 ";
    private static final String DOT_SYMBOL_CODE = " \u2192 ";
    boolean showPath = false;

    public void addValue(GroupEntryData groupEntryData) {
        mValues.add(groupEntryData);
    }

    public void setValues(List<GroupEntryData> mValues) {
        this.mValues = mValues;
    }

    public boolean isShowPath() {
        return showPath;
    }

    public void setShowPath(boolean showPath) {
        this.showPath = showPath;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ListGroupsAndEntriesAdapter.ViewHolder(ListGroupAndEntriesItemViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.name.setText(holder.mItem.name);
        if (holder.mItem.type.name().toString().equals(GroupEntryType.DUMMY.name().toString())) {
            holder.groupEntryNameCardView.setVisibility(View.INVISIBLE);
        } else if (holder.mItem.type.name().toString().equals(GroupEntryType.ENTRY.name().toString())) {
            if (showPath && holder.mItem.path != null) {
                holder.path.setVisibility(View.VISIBLE);
                String path = holder.mItem.path.substring(1, holder.mItem.path.length());
                path = path.replace("/", DOT_SYMBOL_CODE);
                holder.path.setText(path);
            } else {
                holder.path.setVisibility(View.GONE);
            }
            holder.groupEntryImage.setImageResource(R.drawable.ic_key_fill1_wght300_grad_25_opsz24);
            holder.groupEntryImage.setColorFilter(ContextCompat.getColor(holder.groupEntryImage.getContext(), R.color.kp_green_2));
            if (Db.getInstance().isEntryNotUpdatedInDb(holder.mItem.id)) {
                holder.groupEntryCountOrStatus.setText(holder.groupEntryCountOrStatus.getContext().getString(R.string.notSaved));
            } else {
                Pair<GroupEntryStatus, Long> statusLongPair = new Pair<>();
                statusLongPair.first = holder.mItem.status;
                statusLongPair.second = holder.mItem.daysToExpire;
                Utils.setExpiryText(holder.groupEntryCountOrStatus, statusLongPair);
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
        TextView name, groupEntryCountOrStatus, path;
        CardView groupEntryNameCardView;
        ShapeableImageView groupEntryImage;

        public ViewHolder(@NonNull ListGroupAndEntriesItemViewBinding binding) {
            super(binding.getRoot());
            name = binding.groupName;
            groupEntryCountOrStatus = binding.groupEntryCountOrStatus;
            groupEntryNameCardView = binding.groupEntryNameCardView;
            groupEntryImage = binding.groupEntryImage;
            path = binding.path;
            groupEntryNameCardView.setOnClickListener(view -> {
                if (mItem.type.name().equals(GroupEntryType.GROUP.name())) {
                    Db.getInstance().setCurrentGroupId(mItem.id);
                    ReloadEventSource.getInstance().reload(ReloadEvent.ReloadAction.GROUP_UPDATE);
                } else if (mItem.type.name().equals(GroupEntryType.ENTRY.name())) {
                    Db.getInstance().setCurrentEntryId(mItem.id);
                    ChangeActivityEventSource.getInstance().changeActivity(ChangeActivityEvent.ChangeActivityAction.ENTRY_SELECTED);
                }
            });
        }
    }
}
