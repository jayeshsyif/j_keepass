package org.j_keepass;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.j_keepass.adapter.ListGroupAdapter;
import org.j_keepass.databinding.ActivityListBinding;
import org.j_keepass.util.Common;
import org.j_keepass.util.Pair;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {
    private AppBarConfiguration appBarConfiguration;
    private ActivityListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.floatAdd.shrink();

        if (Common.database == null) {
            Intent intent = new Intent(ListActivity.this, LoadActivity.class);
            startActivity(intent);
        } else {
            String groupName = "NA";
            int id = -1;
            Database<?, ?, ?, ?> database = Common.database;
            Group<?, ?, ?, ?> group = null;
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                String click = bundle.getString("click");
                if (click != null && click.equalsIgnoreCase("group")) {
                    group = Common.group;
                }
            }

            if (group == null) {
                group = database.getRootGroup();
            }
            if (group != null) {
                groupName = group.getName();
                ArrayList<Pair<Object, Boolean>> pairs = new ArrayList<Pair<Object, Boolean>>();
                {

                    //groups
                    List<?> groups = group.getGroups();
                    for (int gCount = 0; gCount < groups.size(); gCount++) {
                        Group<?, ?, ?, ?> localGroup = (Group<?, ?, ?, ?>) groups.get(gCount);
                        Pair<Object, Boolean> pair = new Pair<Object, Boolean>();
                        pair.first = localGroup;
                        pair.second = true;
                        pairs.add(pair);
                    }
                }
                {
                    //entries
                    List<?> entries = group.getEntries();
                    for (int eCount = 0; eCount < entries.size(); eCount++) {
                        Entry<?, ?, ?, ?> localEntry = (Entry<?, ?, ?, ?>) entries.get(eCount);
                        Pair<Object, Boolean> pair = new Pair<Object, Boolean>();
                        pair.first = localEntry;
                        pair.second = false;
                        pairs.add(pair);
                    }
                }

                ListGroupAdapter listGroupAdapter = new ListGroupAdapter(this, pairs);
                binding.groupListView.setAdapter(listGroupAdapter);
                binding.groupListView.setFooterDividersEnabled(false);
                binding.groupListView.setHeaderDividersEnabled(false);
                binding.groupListView.setDivider(null);
                binding.groupListView.setDividerHeight(0);

            }

            binding.floatAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!binding.floatAdd.isExtended()) {
                        binding.floatAdd.extend();
                    } else {
                        binding.floatAdd.shrink();
                    }
                    AlertDialog alertDialog = getDialog();
                    alertDialog.show();
                }
            });
            binding.groupName.setText(groupName);
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        return false;
    }

    private AlertDialog getDialog() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(ListActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.add_new_layout, null);
        alert.setView(mView);
        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        FloatingActionButton closeInfoBtn = mView.findViewById(R.id.addNewfloatCloseInfoBtn);
        closeInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                if (!binding.floatAdd.isExtended()) {
                    binding.floatAdd.extend();
                } else {
                    binding.floatAdd.shrink();
                }
            }
        });
        MaterialButton addGroup = mView.findViewById(R.id.addNewGroupfloatBtn);
        addGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListActivity.this, AddGroupActivity.class);
                startActivity(intent);
            }
        });
        return alertDialog;
    }

}