package org.j_keepass.copy_or_move.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import org.j_keepass.R;
import org.j_keepass.databinding.CopyOrMoveItemViewBinding;
import org.j_keepass.events.reload.ReloadEvent;
import org.j_keepass.list_db.dtos.GroupEntryData;
import org.j_keepass.list_db.dtos.GroupEntryType;
import org.j_keepass.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class ListGroupsForCopyMoveAdapter extends RecyclerView.Adapter<ListGroupsForCopyMoveAdapter.ViewHolder> {

    List<GroupEntryData> mValues = new ArrayList<>();

    private UUID selectedGid;
    private String selectedGName;
    private ReloadEvent reloadEvent;

    public UUID getSelectedGid() {
        return selectedGid;
    }

    public ListGroupForCopyMoveAdapterAnimator getItemAnimator() {
        return new ListGroupForCopyMoveAdapterAnimator();
    }

    public void setSelectedGid(UUID gId) {
        selectedGid = gId;
    }

    public void addValue(GroupEntryData groupEntryData) {
        mValues.add(groupEntryData);
    }

    public void removeAll() {
        mValues = new ArrayList<>();
    }

    public String getSelectedGName() {
        return selectedGName;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ListGroupsForCopyMoveAdapter.ViewHolder(CopyOrMoveItemViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.name.setText(holder.mItem.name);
        String itemTypeName = holder.mItem.type.name();
        // Handle visibility for DUMMY type
        if (itemTypeName.equals(GroupEntryType.DUMMY.name())) {
            holder.groupEntryNameCardView.setVisibility(View.INVISIBLE);
            return; // Exit early to avoid unnecessary checks
        } else {
            holder.name.setText(holder.mItem.name);
        }
        holder.groupEntryNameCardView.setOnClickListener(view -> {
            if (holder.mItem.type.name().equals(GroupEntryType.GROUP.name())) {
                Utils.log("Copy or move folder is clicked");
                selectedGid = holder.mItem.id;
                selectedGName = holder.mItem.name;
                reloadEvent.reload(ReloadEvent.ReloadAction.COPY_MOVE_GROUP_UPDATE);
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

    public void setReloadEvent(ReloadEvent reloadEvent) {
        this.reloadEvent = reloadEvent;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public GroupEntryData mItem;
        TextView name;
        CardView groupEntryNameCardView;
        ShapeableImageView groupEntryImage;

        public ViewHolder(@NonNull CopyOrMoveItemViewBinding binding) {
            super(binding.getRoot());
            name = binding.groupName;
            groupEntryNameCardView = binding.groupEntryNameCardView;
            groupEntryImage = binding.groupEntryImage;
        }
    }

    class ListGroupForCopyMoveAdapterAnimator extends DefaultItemAnimator {

        @Override
        public boolean animateAdd(RecyclerView.ViewHolder holder) {
            setAnimation(((ListGroupsForCopyMoveAdapter.ViewHolder) holder).groupEntryNameCardView);
            return false;
        }

        private void setAnimation(CardView view) {
            LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(view.getContext(), R.anim.anim_bottom), 0.5f); //0.5f == time between appearance of listview items.
            view.setLayoutAnimation(lac);
        }
    }
}
