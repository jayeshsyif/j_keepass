package org.j_keepass;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.appcompat.app.AppCompatActivity;

import org.j_keepass.adapter.ListGroupAdapter;
import org.j_keepass.databinding.ActivitySearchBinding;
import org.j_keepass.util.Common;
import org.j_keepass.util.Pair;
import org.j_keepass.util.ProgressDialogUtil;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        if (Common.database == null) {
            Intent intent = new Intent(SearchActivity.this, LoadActivity.class);
            startActivity(intent);
            finish();
        } else {
            binding.searchBtn.setOnClickListener( v -> {
                final AlertDialog alertDialog = ProgressDialogUtil.getLoading(getLayoutInflater(), SearchActivity.this);
                ProgressDialogUtil.showLoadingDialog(alertDialog);
                ProgressDialogUtil.setLoadingProgress(alertDialog, 10);
                {
                    String groupName = "NA";
                    Database<?, ?, ?, ?> database = Common.database;

                    List<?> searchedEntries = database.findEntries(binding.searchKey.getText().toString());

                    Group<?, ?, ?, ?> group = null;
                    group = Common.group;

                    ProgressDialogUtil.setLoadingProgress(alertDialog, 20);
                    if (group == null) {
                        group = database.getRootGroup();
                        Common.group = group;
                    }

                    LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left), 0.5f); //0.5f == time between appearance of listview items.
                    binding.searchListView.setLayoutAnimation(lac);
                    binding.searchListView.startLayoutAnimation();

                    if (group != null) {
                        groupName = group.getName();
                        ArrayList<Pair<Object, Boolean>> pairs = new ArrayList<>();

                        {
                            //entries
                            for (int eCount = 0; eCount < searchedEntries.size(); eCount++) {
                                Entry<?, ?, ?, ?> localEntry = (Entry<?, ?, ?, ?>) searchedEntries.get(eCount);
                                Pair<Object, Boolean> pair = new Pair<>();
                                pair.first = localEntry;
                                pair.second = false;
                                pairs.add(pair);
                            }
                        }
                        ProgressDialogUtil.setLoadingProgress(alertDialog, 50);
                        ListGroupAdapter listGroupAdapter = new ListGroupAdapter(pairs, this);
                        binding.searchListView.setAdapter(listGroupAdapter);
                        binding.searchListView.setFooterDividersEnabled(false);
                        binding.searchListView.setHeaderDividersEnabled(false);
                        binding.searchListView.setDivider(null);
                        binding.searchListView.setDividerHeight(0);

                    }
                    ProgressDialogUtil.dismissLoadingDialog(alertDialog);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (Common.group != null) {
            Intent intent = new Intent(this, ListActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("click", "group");
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        } else {
            super.onBackPressed();
            Intent intent = new Intent(SearchActivity.this, LoadActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return false;
    }
}
