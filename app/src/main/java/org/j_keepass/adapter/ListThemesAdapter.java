package org.j_keepass.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.j_keepass.LandingAndListDatabaseActivity;
import org.j_keepass.R;
import org.j_keepass.databinding.ListDbItemViewBinding;
import org.j_keepass.databinding.ListThemesItemViewBinding;
import org.j_keepass.loading.eventinterface.LoadingEventSource;
import org.j_keepass.theme.eventinterface.ThemeEventSource;
import org.j_keepass.util.Util;
import org.j_keepass.util.theme.Theme;

import java.util.ArrayList;
import java.util.List;

public class ListThemesAdapter extends RecyclerView.Adapter<ListThemesAdapter.ViewHolder> {

    List<Theme> mValues = new ArrayList<>();
    BottomSheetDialog bsd;

    public void setBsd(BottomSheetDialog bsd) {
        this.bsd = bsd;
    }

    public void addValue(Theme theme) {
        mValues.add(theme);
    }

    public void setValues(List<Theme> mValues) {
        this.mValues = mValues;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ListThemesItemViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.themeColor1.setBackgroundResource(mValues.get(position).getColor1());
        holder.themeColor2.setBackgroundResource(mValues.get(position).getColor2());
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout themeColor1;
        ImageButton themeColor2;
        public Theme mItem;

        public ViewHolder(@NonNull ListThemesItemViewBinding binding) {
            super(binding.getRoot());
            themeColor1 = binding.themeColor1;
            themeColor2 = binding.themeColor2;
            themeColor1.setOnClickListener(view -> {
                bsd.dismiss();
                Util.log("Change theme is called with : " + mItem);
                LoadingEventSource.getInstance().updateLoadingText(binding.getRoot().getContext().getString(R.string.applyingTheme));
                LoadingEventSource.getInstance().showLoading();
                Util.sleepFor3MSec();
                ThemeEventSource.getInstance().applyTheme(mItem);
            });
            themeColor2.setOnClickListener(view -> {
                bsd.dismiss();
                Util.log("Change theme is called with : " + mItem);
                LoadingEventSource.getInstance().updateLoadingText(binding.getRoot().getContext().getString(R.string.applyingTheme));
                LoadingEventSource.getInstance().showLoading();
                ThemeEventSource.getInstance().applyTheme(mItem);
            });
        }
    }

}
