package org.j_keepass.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import org.j_keepass.ListActivity;
import org.j_keepass.LoadActivity;
import org.j_keepass.R;
import org.j_keepass.ViewEntryActivity;
import org.j_keepass.util.Common;
import org.j_keepass.util.Pair;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;

import java.util.ArrayList;

public class ListGroupAdapter extends BaseAdapter {
    private ArrayList<Pair<Object, Boolean>> pairs = new ArrayList<Pair<Object, Boolean>>();
    private Activity activity;

    public ListGroupAdapter(ArrayList<Pair<Object, Boolean>> pairs, Activity activity) {
        this.pairs = pairs;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return pairs.size();
    }

    @Override
    public Pair<Object, Boolean> getItem(int position) {
        if (pairs != null) {
            return pairs.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(activity).inflate(R.layout.activity_list_adapter_view, parent, false);
        TextView tx = convertView.findViewById(R.id.adapterText);
        //LinearLayout mainIL = convertView.findViewById(R.id.adapterMainLinearLayout);
        ImageView adapterIconImageView = convertView.findViewById(R.id.adapterIconImageView);
        ImageView edit = convertView.findViewById(R.id.editGroupBtn);
        ImageView delete = convertView.findViewById(R.id.deleteGroupBtn);
        Pair<Object, Boolean> pair = getItem(position);
        if (pair != null && tx != null) {
            if (pair.second) {
                //its group
                Group<?, ?, ?, ?> localGroup = (Group<?, ?, ?, ?>) pair.first;
                tx.setText(localGroup.getName());
                tx.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Common.group = localGroup;
                        Intent intent = new Intent(activity, ListActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("click", "group");
                        intent.putExtras(bundle);
                        activity.startActivity(intent);
                        activity.finish();
                    }
                });
            } else {
                Entry<?, ?, ?, ?> localEntry = (Entry<?, ?, ?, ?>) pair.first;
                tx.setText(localEntry.getTitle());
                //mainIL.setBackgroundColor(convertView.getResources().getColor(R.color.kp_green_2));
                adapterIconImageView.setImageResource(R.drawable.ic_key_fill0_wght300_grad_25_opsz24);
                tx.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Common.entry = localEntry;
                        Intent intent = new Intent(activity, ViewEntryActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("click", "entry");
                        intent.putExtras(bundle);
                        activity.startActivity(intent);
                        activity.finish();
                    }
                });
            }

        }
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, R.string.devInProgress, Snackbar.LENGTH_SHORT).show();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, R.string.devInProgress, Snackbar.LENGTH_SHORT).show();
            }
        });
        return convertView;
    }

}
