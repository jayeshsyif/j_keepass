package org.j_keepass.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.j_keepass.databinding.ListDbItemViewBinding;
import org.j_keepass.util.Util;

import java.util.ArrayList;
import java.util.List;

public class ListDbsAdapter extends RecyclerView.Adapter<ListDbsAdapter.ViewHolder> {

    List<DbData> mValues = new ArrayList<>();

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
        return new ViewHolder(ListDbItemViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mDbName.setText(mValues.get(position).dbName);
        holder.mDbModifiedDate.setText("Modified: " + Util.convertDateToStringOnlyDate(mValues.get(position).lastModified) + " ");
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView mDbName;
        TextView mDbModifiedDate;
        public DbData mItem;

        public ViewHolder(@NonNull ListDbItemViewBinding binding) {
            super(binding.getRoot());
            mDbName = binding.databaseName;
            mDbModifiedDate = binding.dbModifiedDate;
        }
    }

    public DbData getDbDatObj() {
        return new DbData();
    }

    public class DbData {
        public String dbName;
        public long lastModified;
    }
}
