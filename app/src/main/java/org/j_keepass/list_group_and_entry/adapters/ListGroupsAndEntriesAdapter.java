package org.j_keepass.list_group_and_entry.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import org.j_keepass.R;
import org.j_keepass.databinding.ListGroupAndEntriesItemViewBinding;
import org.j_keepass.db.operation.Db;
import org.j_keepass.events.changeactivity.ChangeActivityEvent;
import org.j_keepass.events.changeactivity.ChangeActivityEventSource;
import org.j_keepass.events.reload.ReloadEvent;
import org.j_keepass.events.reload.ReloadEventSource;
import org.j_keepass.list_db.dtos.GroupEntryData;
import org.j_keepass.list_db.dtos.GroupEntryType;
import org.j_keepass.util.Pair;
import org.j_keepass.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class ListGroupsAndEntriesAdapter extends RecyclerView.Adapter<ListGroupsAndEntriesAdapter.ViewHolder> {

    final List<GroupEntryData> mValues = new ArrayList<>();
    private static final String SUB_DIRECTORY_ARROW_SYMBOL_CODE = " ↳ ";
    private static final String DOT_SYMBOL_CODE = " → ";
    boolean showPath = false;

    public void addValue(GroupEntryData groupEntryData) {
        mValues.add(groupEntryData);
    }

    public void setShowPath(boolean showPath) {
        this.showPath = showPath;
    }

    public ListGroupsAndEntriesAdapterAnimator getItemAnimator() {
        return new ListGroupsAndEntriesAdapterAnimator();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ListGroupsAndEntriesAdapter.ViewHolder(ListGroupAndEntriesItemViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        configure(holder);

    }

    private void configure(ViewHolder holder) {
        holder.name.setText(holder.mItem.name);
        String itemTypeName = holder.mItem.type.name();
        Context context = holder.groupEntryImage.getContext();
        boolean isEntryUpdated = !Db.getInstance().isEntryNotUpdatedInDb(holder.mItem.id);

        // Handle visibility for DUMMY type
        if (itemTypeName.equals(GroupEntryType.DUMMY.name())) {
            holder.groupEntryNameCardView.setVisibility(View.INVISIBLE);
            return; // Exit early to avoid unnecessary checks
        }

        holder.groupEntryNameCardView.setOnClickListener(view -> {
            if (holder.mItem.type.name().equals(GroupEntryType.GROUP.name())) {
                Db.getInstance().setCurrentGroupId(holder.mItem.id);
                ReloadEventSource.getInstance().reload(ReloadEvent.ReloadAction.GROUP_UPDATE);
            } else if (holder.mItem.type.name().equals(GroupEntryType.ENTRY.name())) {
                Db.getInstance().setCurrentEntryId(holder.mItem.id);
                ChangeActivityEventSource.getInstance().changeActivity(ChangeActivityEvent.ChangeActivityAction.ENTRY_SELECTED);
            }
        });

        // Handle ENTRY type
        if (itemTypeName.equals(GroupEntryType.ENTRY.name())) {
            handleEntryType(holder, showPath);

            holder.groupEntryImage.setImageResource(R.drawable.ic_key_fill1_wght300_grad_25_opsz24);
            holder.groupEntryImage.setColorFilter(ContextCompat.getColor(context, R.color.kp_green_2));

            // Update status text based on entry update status
            if (isEntryUpdated) {
                Utils.setExpiryText(holder.groupEntryCountOrStatus, new Pair<>(holder.mItem.status, holder.mItem.daysToExpire));
            } else {
                holder.groupEntryCountOrStatus.setText(context.getString(R.string.notSaved));
                holder.groupEntryCountOrStatus.setTextColor(ContextCompat.getColor(context, R.color.kp_red));
            }
        }
        // Handle GROUP type
        else if (itemTypeName.equals(GroupEntryType.GROUP.name())) {
            String text = holder.mItem.subCount + SUB_DIRECTORY_ARROW_SYMBOL_CODE;
            holder.groupEntryCountOrStatus.setText(text);
        }
    }

    // Method to handle ENTRY type logic
    private void handleEntryType(ViewHolder holder, boolean showPath) {
        if (showPath && holder.mItem.path != null) {
            String path = holder.mItem.path.substring(1).replace("/", DOT_SYMBOL_CODE);
            holder.path.setVisibility(View.VISIBLE);
            holder.path.setText(path);
            holder.path.setOnClickListener(view -> {
                UUID fId = Db.getInstance().getParentGroupName(holder.mItem.id);
                Db.getInstance().setCurrentGroupId(fId);
                ReloadEventSource.getInstance().reload(ReloadEvent.ReloadAction.NAV_GROUP);
            });
        } else {
            holder.path.setVisibility(View.GONE);
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public GroupEntryData mItem;
        final TextView name;
        final TextView groupEntryCountOrStatus;
        final TextView path;
        final CardView groupEntryNameCardView;
        final ShapeableImageView groupEntryImage;

        public ViewHolder(@NonNull ListGroupAndEntriesItemViewBinding binding) {
            super(binding.getRoot());
            name = binding.groupName;
            groupEntryCountOrStatus = binding.groupEntryCountOrStatus;
            groupEntryNameCardView = binding.groupEntryNameCardView;
            groupEntryImage = binding.groupEntryImage;
            path = binding.path;
        }
    }

    static class ListGroupsAndEntriesAdapterAnimator extends DefaultItemAnimator {

        @Override
        public boolean animateAdd(RecyclerView.ViewHolder holder) {
            setAnimation(((ListGroupsAndEntriesAdapter.ViewHolder) holder).groupEntryNameCardView);
            return false;
        }

        private void setAnimation(CardView view) {
            LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(view.getContext(), R.anim.anim_bottom), 0.5f); //0.5f == time between appearance of listview items.
            view.setLayoutAnimation(lac);
        }
    }
}
