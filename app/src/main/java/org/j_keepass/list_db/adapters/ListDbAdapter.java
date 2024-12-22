package org.j_keepass.list_db.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import org.j_keepass.databinding.ListDbItemViewBinding;
import org.j_keepass.db.events.DbEventSource;
import org.j_keepass.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class ListDbAdapter extends RecyclerView.Adapter<ListDbAdapter.ViewHolder> {

    List<DbData> mValues = new ArrayList<>();
    boolean animationFlag = false;

    public void setValues() {
        DbData d = new DbData();
        d.dbName = "TEST - DB";
        mValues.add(d);
    }

    public void addValue(DbData dbData) {
        mValues.add(dbData);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder vh = new ViewHolder(ListDbItemViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        setAnimation(vh.itemView);
        return vh;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mDbName.setText(mValues.get(position).dbName);
        holder.mDbModifiedDate.setText("Modified: " + Utils.convertDateToStringOnlyDate(mValues.get(position).lastModified) + " ");
        if (holder.mItem.lastModified == -1) {
            holder.cardView.setVisibility(View.INVISIBLE);
            holder.mDbModifiedDate.setVisibility(View.GONE);
            holder.mImage.setVisibility(View.GONE);
            holder.mDatabaseMoreOption.setVisibility(View.GONE);
        }
    }

    private void setAnimation(View view) {
        if (animationFlag) {
            view.startAnimation(AnimationUtils.loadAnimation(view.getContext(), android.R.anim.fade_in));
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView mDbName;
        TextView mDbModifiedDate;
        CardView cardView;
        public DbData mItem;
        ShapeableImageView mImage;
        ImageButton mDatabaseMoreOption;

        public ViewHolder(@NonNull ListDbItemViewBinding binding) {
            super(binding.getRoot());
            mDbName = binding.databaseName;
            mDbModifiedDate = binding.dbModifiedDate;
            cardView = binding.databaseNameCardView;
            mImage = binding.image;
            mDatabaseMoreOption = binding.dbMoreOption;
            cardView.setOnClickListener(view -> {
                if (mItem.lastModified != -1) {
                    DbEventSource.getInstance().askPwdForDb(binding.getRoot().getContext(), mItem.dbName, mItem.fullPath);
                }
            });
        }
    }

    public DbData getDbDatObj() {
        return new DbData();
    }

    public class DbData {
        public String dbName;
        public long lastModified;
        public String fullPath;

        @Override
        public String toString() {
            return "DbData{" +
                    "dbName='" + dbName + '\'' +
                    ", lastModified=" + lastModified +
                    ", fullPath='" + fullPath + '\'' +
                    '}';
        }
    }
}
