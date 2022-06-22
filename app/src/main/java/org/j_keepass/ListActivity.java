package org.j_keepass;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.j_keepass.adapter.ListGroupAdapter;
import org.j_keepass.databinding.ActivityListBinding;
import org.j_keepass.util.Common;
import org.j_keepass.util.Pair;
import org.j_keepass.util.PasswordGenerator;
import org.j_keepass.util.ProgressDialogUtil;
import org.j_keepass.util.ToastUtil;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {

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
            finish();
        } else {

            final AlertDialog alertDialog = ProgressDialogUtil.getLoading(getLayoutInflater(), ListActivity.this);
            ProgressDialogUtil.showLoadingDialog(alertDialog);
            ProgressDialogUtil.setLoadingProgress(alertDialog, 10);
            new Thread(() -> {
                String groupName = "NA";
                Database<?, ?, ?, ?> database = Common.database;
                Group<?, ?, ?, ?> group = null;
                Bundle bundle = getIntent().getExtras();
                if (bundle != null) {
                    String click = bundle.getString("click");
                    if (click != null && click.equalsIgnoreCase("group")) {
                        group = Common.group;
                    }
                }
                ProgressDialogUtil.setLoadingProgress(alertDialog, 20);
                if (group == null) {
                    group = database.getRootGroup();
                    Common.group = group;
                }

                LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left), 0.5f); //0.5f == time between appearance of listview items.
                binding.groupListView.setLayoutAnimation(lac);
                binding.groupListView.startLayoutAnimation();

                if (group != null) {
                    groupName = group.getName();
                    ArrayList<Pair<Object, Boolean>> pairs = new ArrayList<>();
                    {

                        //groups
                        List<?> groups = group.getGroups();
                        for (int gCount = 0; gCount < groups.size(); gCount++) {
                            Group<?, ?, ?, ?> localGroup = (Group<?, ?, ?, ?>) groups.get(gCount);
                            Pair<Object, Boolean> pair = new Pair<>();
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
                            Pair<Object, Boolean> pair = new Pair<>();
                            pair.first = localEntry;
                            pair.second = false;
                            pairs.add(pair);
                        }
                    }
                    ProgressDialogUtil.setLoadingProgress(alertDialog, 50);
                    ListGroupAdapter listGroupAdapter = new ListGroupAdapter(pairs, ListActivity.this);
                    binding.groupListView.setAdapter(listGroupAdapter);
                    binding.groupListView.setFooterDividersEnabled(false);
                    binding.groupListView.setHeaderDividersEnabled(false);
                    binding.groupListView.setDivider(null);
                    binding.groupListView.setDividerHeight(0);

                }
                ProgressDialogUtil.dismissLoadingDialog(alertDialog);
                binding.groupName.setText(groupName);
            }).start();

            binding.floatAdd.setOnClickListener(v -> {
                if (!binding.floatAdd.isExtended()) {
                    binding.floatAdd.extend();
                } else {
                    binding.floatAdd.shrink();
                }
                AlertDialog alertDialog1 = getDialog();
                alertDialog1.show();
            });

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
        alertDialog.getWindow().setGravity(Gravity.BOTTOM);

        ScrollView newFloatScrollView = mView.findViewById(R.id.newFloatScrollView);
        newFloatScrollView.setAnimation(AnimationUtils.makeInAnimation(this, true));

        FloatingActionButton closeInfoBtn = mView.findViewById(R.id.addNewfloatCloseInfoBtn);
        closeInfoBtn.setOnClickListener(v -> {
            alertDialog.dismiss();
            if (!binding.floatAdd.isExtended()) {
                binding.floatAdd.extend();
            } else {
                binding.floatAdd.shrink();
            }
        });
        MaterialButton addGroup = mView.findViewById(R.id.addNewGroupfloatBtn);
        addGroup.setOnClickListener(v -> {
            Intent intent = new Intent(ListActivity.this, AddGroupActivity.class);
            startActivity(intent);
            finish();
        });

        MaterialButton addEntry = mView.findViewById(R.id.addNewEntryfloatBtn);
        addEntry.setOnClickListener(v -> {
            Intent intent = new Intent(ListActivity.this, AddEntryActivity.class);
            startActivity(intent);
            finish();
        });

        LinearLayout randomPasswordLayout = mView.findViewById(R.id.randomPasswordLayout);
        TextInputEditText randomPassword = mView.findViewById(R.id.randomPassword);
        ImageButton randomPasswordCopy = mView.findViewById(R.id.randomPasswordCopy);
        MaterialButton newRandonPasswordFloatBtn = mView.findViewById(R.id.newRandonPasswordFloatBtn);
        newRandonPasswordFloatBtn.setOnClickListener(v -> {
            if (randomPasswordLayout.getVisibility() == View.GONE) {
                randomPassword.setText(new PasswordGenerator().generate(20));
                randomPasswordLayout.setVisibility(View.VISIBLE);
                randomPasswordLayout.startAnimation(AnimationUtils.makeInAnimation(mView.getContext(), true));
            } else {
                randomPasswordLayout.setVisibility(View.GONE);
            }
        });
        randomPasswordCopy.setOnClickListener(v -> {
            if (randomPassword.getText() != null) {
                ClipboardManager clipboard = (ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("password", randomPassword.getText().toString());
                clipboard.setPrimaryClip(clip);
                ToastUtil.showToast(getLayoutInflater(), v, R.string.copiedToClipboard);
            }
        });
        return alertDialog;
    }

    @Override
    public void onBackPressed() {
        if (Common.group != null) {
            Common.group = Common.group.getParent();
            Intent intent = new Intent(this, ListActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("click", "group");
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        } else {
            super.onBackPressed();
            Intent intent = new Intent(ListActivity.this, LoadActivity.class);
            startActivity(intent);
            finish();
        }
    }
}