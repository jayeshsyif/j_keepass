package org.j_keepass;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import org.j_keepass.databinding.ActivityListBinding;
import org.j_keepass.util.BottomMenuUtil;
import org.j_keepass.util.Common;
import org.j_keepass.util.ConfirmDialogUtil;
import org.j_keepass.util.NewPasswordDialogUtil;
import org.j_keepass.util.Pair;
import org.j_keepass.util.ProgressDialogUtil;
import org.j_keepass.util.SearchDialogUtil;
import org.j_keepass.util.ThemeSettingDialogUtil;
import org.j_keepass.util.ToastUtil;
import org.j_keepass.util.Triplet;
import org.j_keepass.util.Util;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.kdbx.KdbxCreds;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ListActivity extends AppCompatActivity {

    private ActivityListBinding binding;
    private boolean isSearchView = false;
    public static final int PICK_FOLDER_OPEN_RESULT_CODE = 2;
    private static final int READ_EXTERNAL_STORAGE = 100;

    private static Date currentDate = null;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeSettingDialogUtil.onActivityCreateSetTheme(this, false);
        binding = ActivityListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        currentDate = Calendar.getInstance().getTime();
        binding.floatAdd.shrink();

        if (Common.database == null) {
            Intent intent = new Intent(ListActivity.this, LoadActivity.class);
            startActivity(intent);
            finish();
        } else {
            final AlertDialog alertDialog = ProgressDialogUtil.getLoading(getLayoutInflater(), ListActivity.this);
            ProgressDialogUtil.showLoadingDialog(alertDialog);
            new Thread(() -> {
                {
                    updateProgress(alertDialog, 10);
                    Database<?, ?, ?, ?> database = Common.database;
                    Group<?, ?, ?, ?> group = Common.group;

                    updateProgress(alertDialog, 20);
                    if (group == null) {
                        group = database.getRootGroup();
                        Common.group = group;
                    }
                    if (group != null) {
                        updateProgress(alertDialog, 50);
                        listAndShowGroupsAndEntries(group, false, alertDialog);
                        updateProgress(alertDialog, 70);
                    }

                    Group<?, ?, ?, ?> finalGroup = group;
                    new Thread(() -> {
                        if (finalGroup.isRootGroup()) {
                            runOnUiThread(() -> {
                                binding.entriesStatistics.setVisibility(View.VISIBLE);
                            });
                            List<?> allEntriesList = database.findEntries(new Entry.Matcher() {
                                @Override
                                public boolean matches(Entry entry) {
                                    return entry.getTitle().length() > 0;
                                }
                            });
                            if (allEntriesList != null) {
                                Util.sleepFor100Sec();
                                runOnUiThread(() -> {
                                    ((Chip) binding.totalCountDisplay).setText("" + allEntriesList.size());
                                    binding.totalCountTextDisplay.setOnClickListener(v -> {
                                        binding.totalCountDisplayLayout.performClick();
                                    });
                                    binding.totalCountDisplay.setOnClickListener(v -> {
                                        binding.totalCountDisplayLayout.performClick();
                                    });
                                    binding.totalCountDisplayLayout.setOnClickListener(v -> {
                                        final AlertDialog alertDialogL = ProgressDialogUtil.getLoading(LayoutInflater.from(v.getContext()), v.getContext());
                                        ProgressDialogUtil.showLoadingDialog(alertDialogL);
                                        ProgressDialogUtil.setLoadingProgress(alertDialogL, 10);
                                        new Thread(() -> {
                                            {
                                                Common.group = Common.database.getRootGroup();
                                                listAndShowGroupsAndEntries(Common.group, false, alertDialogL);
                                                updateProgress(alertDialogL, 100);
                                                dismissLoadingDialog(alertDialogL);
                                            }
                                        }).start();
                                    });
                                    LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(this, R.animator.anim_bottom), Common.ANIMATION_TIME);
                                    binding.totalCountDisplayLayout.setVisibility(View.VISIBLE);
                                    binding.totalCountDisplayLayout.setLayoutAnimation(lac);
                                });
                            }
                        }
                    }).start();
                    updateProgress(alertDialog, 80);
                    new Thread(() -> {
                        if (finalGroup.isRootGroup()) {
                            List<?> expiredList = database.findEntries(new Entry.Matcher() {
                                @Override
                                public boolean matches(Entry entry) {
                                    long diff = entry.getExpiryTime().getTime() - currentDate.getTime();
                                    long daysToExpire = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                                    return (daysToExpire <= 0);
                                }
                            });
                            if (expiredList != null) {
                                Util.sleepFor100Sec();
                                runOnUiThread(() -> {
                                    ((Chip) binding.totalExpiredCountDisplay).setText("" + expiredList.size());
                                    binding.totalExpiredCountDisplay.setOnClickListener(v -> {
                                        binding.totalExpiredCountDisplayLayout.performClick();
                                    });
                                    binding.totalExpiredCountTextDisplay.setOnClickListener(v -> {
                                        binding.totalExpiredCountDisplayLayout.performClick();
                                    });
                                    binding.totalExpiredCountDisplayLayout.setOnClickListener(v -> {
                                        final AlertDialog alertDialogL = ProgressDialogUtil.getLoading(LayoutInflater.from(v.getContext()), v.getContext());
                                        ProgressDialogUtil.showLoadingDialog(alertDialogL);
                                        ProgressDialogUtil.setLoadingProgress(alertDialogL, 10);
                                        new Thread(() -> {
                                            runOnUiThread(() -> {
                                                binding.groupsLinearLayout.removeAllViews();
                                                binding.entriesLinearLayout.removeAllViews();
                                                binding.groupScrollView.fullScroll(View.FOCUS_UP);
                                                binding.groupName.setText(getString(R.string.expired));
                                                //binding.groupName.startAnimation(AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.animator.anim_bottom));
                                                binding.justGroupsTextView.setVisibility(View.GONE);
                                                if (expiredList != null && expiredList.size() > 0) {
                                                    binding.justEntriesTextView.setVisibility(View.VISIBLE);
                                                    binding.justNothingTextView.setVisibility(View.GONE);
                                                } else {
                                                    binding.justEntriesTextView.setVisibility(View.GONE);
                                                    binding.justNothingTextView.setVisibility(View.VISIBLE);
                                                    binding.justNothingTextView.startAnimation(AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.animator.anim_bottom));
                                                }
                                            });
                                            ProgressDialogUtil.setLoadingProgress(alertDialogL, 50);
                                            for (int eCount = 0; eCount < expiredList.size(); eCount++) {
                                                Util.sleepFor100Sec();
                                                Entry<?, ?, ?, ?> localEntry = (Entry<?, ?, ?, ?>) expiredList.get(eCount);
                                                addEntryOnUi(localEntry, false, true);
                                            }
                                            updateProgress(alertDialogL, 100);
                                            dismissLoadingDialog(alertDialogL);

                                        }).start();
                                    });
                                    LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(this, R.animator.anim_bottom), Common.ANIMATION_TIME);
                                    binding.totalExpiredCountDisplayLayout.setVisibility(View.VISIBLE);
                                    binding.totalExpiredCountDisplayLayout.setLayoutAnimation(lac);
                                });
                            }
                        }
                    }).start();
                    updateProgress(alertDialog, 85);
                    new Thread(() -> {
                        if (finalGroup.isRootGroup()) {
                            List<?> expiringSoonList = database.findEntries(new Entry.Matcher() {
                                @Override
                                public boolean matches(Entry entry) {
                                    long diff = entry.getExpiryTime().getTime() - currentDate.getTime();
                                    long daysToExpire = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                                    return (daysToExpire > 0 && daysToExpire <= 10);
                                }
                            });
                            if (expiringSoonList != null) {
                                Util.sleepFor100Sec();
                                runOnUiThread(() -> {
                                    ((Chip) binding.totalExpiringSoonCountDisplay).setText("" + expiringSoonList.size());
                                    binding.totalExpiringSoonCountDisplay.setOnClickListener(v -> {
                                        binding.totalExpiringSoonCountDisplayLayout.performClick();
                                    });
                                    binding.totalExpiringSoonCountTextDisplay.setOnClickListener(v -> {
                                        binding.totalExpiringSoonCountDisplayLayout.performClick();
                                    });
                                    binding.totalExpiringSoonCountDisplayLayout.setOnClickListener(v -> {
                                        final AlertDialog alertDialogL = ProgressDialogUtil.getLoading(LayoutInflater.from(v.getContext()), v.getContext());
                                        ProgressDialogUtil.showLoadingDialog(alertDialogL);
                                        ProgressDialogUtil.setLoadingProgress(alertDialogL, 10);
                                        new Thread(() -> {
                                            runOnUiThread(() -> {
                                                binding.groupsLinearLayout.removeAllViews();
                                                binding.entriesLinearLayout.removeAllViews();
                                                binding.groupScrollView.fullScroll(View.FOCUS_UP);
                                                binding.groupName.setText(getString(R.string.expiringSoon));
                                                //binding.groupName.startAnimation(AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.animator.anim_bottom));
                                                binding.justGroupsTextView.setVisibility(View.GONE);
                                                if (expiringSoonList != null && expiringSoonList.size() > 0) {
                                                    binding.justEntriesTextView.setVisibility(View.VISIBLE);
                                                    binding.justNothingTextView.setVisibility(View.GONE);
                                                } else {
                                                    binding.justEntriesTextView.setVisibility(View.GONE);
                                                    binding.justNothingTextView.setVisibility(View.VISIBLE);
                                                    binding.justNothingTextView.startAnimation(AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.animator.anim_bottom));
                                                }
                                            });
                                            ProgressDialogUtil.setLoadingProgress(alertDialogL, 50);
                                            for (int eCount = 0; eCount < expiringSoonList.size(); eCount++) {
                                                Util.sleepFor100Sec();
                                                Entry<?, ?, ?, ?> localEntry = (Entry<?, ?, ?, ?>) expiringSoonList.get(eCount);
                                                addEntryOnUi(localEntry, false, true);
                                            }
                                            updateProgress(alertDialogL, 100);
                                            dismissLoadingDialog(alertDialogL);

                                        }).start();
                                    });
                                    LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(this, R.animator.anim_bottom), Common.ANIMATION_TIME);
                                    binding.totalExpiringSoonCountDisplayLayout.setVisibility(View.VISIBLE);
                                    binding.totalExpiringSoonCountDisplayLayout.setLayoutAnimation(lac);
                                });
                            }
                        }
                    }).start();
                    updateProgress(alertDialog, 90);
                    dismissLoadingDialog(alertDialog);
                }
            }).start();

            binding.floatAdd.setOnClickListener(v -> {
                if (!binding.floatAdd.isExtended()) {
                    binding.floatAdd.extend();
                } else {
                    binding.floatAdd.shrink();
                }
                showMenu(v);
            });

        }

        binding.searchBtn.setOnClickListener(v -> {
            search(v, this);
        });
        binding.generateNewPassword.setOnClickListener(v -> {
            NewPasswordDialogUtil.show(getLayoutInflater(), v, (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE), binding.getRoot().findViewById(R.id.floatAdd));
        });

        binding.exportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isOk = checkAndGetPermission(v, ListActivity.this);

                if (isOk) {
                    if(Common.kdbxFileUri != null) {
                        Intent chooseFile = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
                        chooseFile.setType("*/*");
                        chooseFile.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                        chooseFile.putExtra(Intent.EXTRA_TITLE, new File(Common.kdbxFileUri.getPath()).getName());

                        chooseFile = Intent.createChooser(chooseFile, "Choose a folder");
                        startActivityForResult(chooseFile, PICK_FOLDER_OPEN_RESULT_CODE);
                    }else {
                        ToastUtil.showToast(getLayoutInflater(), v, R.string.emptyFileError, binding.getRoot().findViewById(R.id.floatAdd));
                    }
                } else {
                    ToastUtil.showToast(getLayoutInflater(), v, R.string.permissionNotGranted, binding.getRoot().findViewById(R.id.floatAdd));
                }
            }
        });

        binding.lockBtn.setOnClickListener(v -> {
            Common.database = null;
            Common.group = null;
            Common.entry = null;
            Common.creds = null;
            Common.kdbxFileUri = null;
            Intent intent = new Intent(ListActivity.this, LoadActivity.class);
            startActivity(intent);
            finish();
        });

    }

    private void dismissLoadingDialog(AlertDialog alertDialog) {
        runOnUiThread(() -> {
            ProgressDialogUtil.dismissLoadingDialog(alertDialog);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return false;
    }

    @SuppressLint("ResourceType")
    private void listAndShowGroupsAndEntries(Group<?, ?, ?, ?> group, boolean isFromBack, AlertDialog alertDialog) {
        runOnUiThread(() -> {
            binding.groupName.setText(group.getName());
            if (!isFromBack) {
                //binding.groupName.startAnimation(AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.animator.anim_bottom));
            } else {
                //binding.groupName.startAnimation(AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.animator.anim_bottom));
            }
            binding.groupsLinearLayout.removeAllViews();
            binding.entriesLinearLayout.removeAllViews();
            if (group.getGroups() != null && group.getGroups().size() > 0) {
                binding.justGroupsTextView.setVisibility(View.VISIBLE);
            } else {
                binding.justGroupsTextView.setVisibility(View.GONE);
            }
            if (group.getEntries() != null && group.getEntries().size() > 0) {
                binding.justEntriesTextView.setVisibility(View.VISIBLE);
            } else {
                binding.justEntriesTextView.setVisibility(View.GONE);
            }
            if (binding.justEntriesTextView.getVisibility() == View.GONE && binding.justGroupsTextView.getVisibility() == View.GONE) {
                binding.justNothingTextView.setVisibility(View.VISIBLE);
                binding.justNothingTextView.startAnimation(AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.animator.anim_bottom));
            } else {
                binding.justNothingTextView.setVisibility(View.GONE);
            }
            binding.entriesStatistics.setVisibility(View.GONE);
            binding.totalCountDisplayLayout.setVisibility(View.GONE);
            binding.totalExpiredCountDisplayLayout.setVisibility(View.GONE);
            binding.totalExpiringSoonCountDisplayLayout.setVisibility(View.GONE);
            binding.groupScrollView.fullScroll(View.FOCUS_UP);
        });
        updateProgress(alertDialog, 60);
        ArrayList<Group<?, ?, ?, ?>> gList = (ArrayList<Group<?, ?, ?, ?>>) group.getGroups();
        Collections.sort(gList, new Comparator<Group<?, ?, ?, ?>>() {
            @Override
            public int compare(Group<?, ?, ?, ?> o1, Group<?, ?, ?, ?> o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (Group<?, ?, ?, ?> g : gList) {
            Util.sleepFor100Sec();
            addGroupOnUi(g, isFromBack);
        }
        ArrayList<Entry<?, ?, ?, ?>> eList = (ArrayList<Entry<?, ?, ?, ?>>) group.getEntries();
        Collections.sort(eList, new Comparator<Entry<?, ?, ?, ?>>() {
            @Override
            public int compare(Entry<?, ?, ?, ?> o1, Entry<?, ?, ?, ?> o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });
        ProgressDialogUtil.setLoadingProgress(alertDialog, 80);
        for (Entry<?, ?, ?, ?> e : eList) {
            Util.sleepFor100Sec();
            addEntryOnUi(e, isFromBack, false);
        }
    }

    private void showMenu(View v) {
        Pair<BottomSheetDialog, ArrayList<LinearLayout>> bsd = BottomMenuUtil.getAddNewEntryAndGroupMenuOptions(v.getContext());
        bsd.first.show();
        bsd.second.get(0).setOnClickListener(v1 -> {
            bsd.first.dismiss();
            Intent intent = new Intent(ListActivity.this, AddGroupActivity.class);
            startActivity(intent);
            finish();
        });

        bsd.second.get(1).setOnClickListener(v1 -> {
            bsd.first.dismiss();
            Intent intent = new Intent(ListActivity.this, AddEntryActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        if (Common.group != null && !Common.group.isRootGroup()) {
            Common.group = Common.group.getParent();
            final AlertDialog alertDialog = ProgressDialogUtil.getLoading(getLayoutInflater(), ListActivity.this);
            ProgressDialogUtil.showLoadingDialog(alertDialog);
            updateProgress(alertDialog, 20);
            new Thread(() -> {
                listAndShowGroupsAndEntries(Common.group, false, alertDialog);
                updateProgress(alertDialog, 99);
                dismissLoadingDialog(alertDialog);
            }).start();
        } else if (Common.group != null && isSearchView) {
            final AlertDialog alertDialog = ProgressDialogUtil.getLoading(getLayoutInflater(), ListActivity.this);
            ProgressDialogUtil.showLoadingDialog(alertDialog);
            updateProgress(alertDialog, 20);
            new Thread(() -> {
                listAndShowGroupsAndEntries(Common.group, false, alertDialog);
                updateProgress(alertDialog, 99);
                dismissLoadingDialog(alertDialog);
            }).start();
        } else {
            super.onBackPressed();
            Intent intent = new Intent(ListActivity.this, LoadActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @SuppressLint("ResourceType")
    private void addGroupOnUi(Group<?, ?, ?, ?> g, boolean isFromBack) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewToLoad = inflater.inflate(R.layout.activity_list_adapter_view, null);
        ((TextView) viewToLoad.findViewById(R.id.adapterText)).setText(g.getName());
        viewToLoad.setOnClickListener(v -> {
            Common.group = g;
            isSearchView = false;
            final AlertDialog alertDialog = ProgressDialogUtil.getLoading(getLayoutInflater(), ListActivity.this);
            ProgressDialogUtil.showLoadingDialog(alertDialog);
            updateProgress(alertDialog, 20);
            new Thread(() -> {
                updateProgress(alertDialog, 50);
                listAndShowGroupsAndEntries(g, false, alertDialog);
                updateProgress(alertDialog, 99);
                dismissLoadingDialog(alertDialog);
            }).start();

        });
        viewToLoad.findViewById(R.id.moreGroupBtn).setOnClickListener(v -> {
            Pair<BottomSheetDialog, ArrayList<LinearLayout>> bsd = BottomMenuUtil.getEntryAndGroupMenuOptions(g.getName(), v.getContext());
            bsd.second.get(0).setOnClickListener(view -> {
                bsd.first.dismiss();
                Common.group = g;
                Intent intent = new Intent(ListActivity.this, EditGroupActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("click", "group");
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            });
            bsd.second.get(1).setOnClickListener(view -> {
                bsd.first.dismiss();
                deleteGroup(v, ListActivity.this, g);
            });
            bsd.second.get(2).setOnClickListener(view -> {
                bsd.first.dismiss();
                Pair<Snackbar, LinearLayout> pairToast = ToastUtil.showMoveToast(getLayoutInflater(), v, binding.floatAdd);
                pairToast.second.setOnClickListener(view1 -> {
                    pairToast.first.dismiss();
                    moveCopyGroup(v, this, g, 1);
                });
            });
            bsd.second.get(3).setOnClickListener(view -> {
                bsd.first.dismiss();
                Pair<Snackbar, LinearLayout> pairToast = ToastUtil.showMoveToast(getLayoutInflater(), v, binding.floatAdd);
                pairToast.second.setOnClickListener(view1 -> {
                    pairToast.first.dismiss();
                    moveCopyGroup(v, this, g, 2);
                });
            });
            bsd.first.show();
        });
        TextView subCountArrowBtn = viewToLoad.findViewById(R.id.subCountArrow);
        subCountArrowBtn.setText("" + (g.getGroupsCount() + g.getEntriesCount()) + " " + Common.SUB_DIRECTORY_ARROW_SYMBOL_CODE);
        runOnUiThread(() -> {
            CardView cardView = viewToLoad.findViewById(R.id.adapterCardView);
            if (!isFromBack) {
                LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(this, R.animator.anim_bottom), Common.ANIMATION_TIME);
                cardView.setLayoutAnimation(lac);
            } else {
                LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(this, R.animator.anim_bottom), Common.ANIMATION_TIME);
                cardView.setLayoutAnimation(lac);
            }
            binding.groupsLinearLayout.addView(viewToLoad);
        });
    }

    @SuppressLint("ResourceType")
    private void addEntryOnUi(Entry<?, ?, ?, ?> e, boolean isFromBack, boolean showGroupInfo) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewToLoad = inflater.inflate(R.layout.activity_list_adapter_view, null);
        ((TextView) viewToLoad.findViewById(R.id.adapterText)).setText(e.getTitle());
        ShapeableImageView adapterIconImageView = viewToLoad.findViewById(R.id.adapterIconImageView);
        adapterIconImageView.setImageResource(R.drawable.ic_key_fill1_wght300_grad_25_opsz24);
        adapterIconImageView.setColorFilter(ContextCompat.getColor(viewToLoad.getContext(), R.color.kp_green_2));
        viewToLoad.setOnClickListener(v -> {
            Common.entry = e;
            Intent intent = new Intent(this, ViewEntryActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("click", "entry");
            intent.putExtras(bundle);
            this.startActivity(intent);
            this.finish();
        });
        TextView dateAndSubInfo = viewToLoad.findViewById(R.id.dateAndSubInfo);
        String info = Util.convertDateToString(e.getExpiryTime());
        dateAndSubInfo.setText(info);
        if (showGroupInfo) {
            dateAndSubInfo.setVisibility(View.INVISIBLE);
        } else {
            dateAndSubInfo.setVisibility(View.INVISIBLE);
        }
        TextView subCountArrowBtn = viewToLoad.findViewById(R.id.subCountArrow);
        //subCountArrowBtn.setVisibility(View.INVISIBLE);
        long diff = e.getExpiryTime().getTime() - currentDate.getTime();
        long daysToExpire = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        if (daysToExpire <= 0) {
            subCountArrowBtn.setText("Expired ! ");
            subCountArrowBtn.setTextColor(ContextCompat.getColor(viewToLoad.getContext(), R.color.kp_red));
        } else if (daysToExpire > 0 && daysToExpire <= 10) {
            subCountArrowBtn.setText("Expires soon, in " + daysToExpire + " days ! ");
            subCountArrowBtn.setTextColor(ContextCompat.getColor(viewToLoad.getContext(), R.color.kp_coral));
        } else {
            subCountArrowBtn.setText("Expires in " + daysToExpire + " days.");
        }
        subCountArrowBtn.setTextSize(TypedValue.COMPLEX_UNIT_PT, 4);
        viewToLoad.findViewById(R.id.moreGroupBtn).setOnClickListener(v -> {

            Pair<BottomSheetDialog, ArrayList<LinearLayout>> bsd = BottomMenuUtil.getEntryAndGroupMenuOptions(e.getTitle(), v.getContext());
            bsd.second.get(0).setOnClickListener(view -> {
                bsd.first.dismiss();
                Common.entry = e;
                Intent intent = new Intent(ListActivity.this, EditEntryActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("click", "entry");
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            });
            bsd.second.get(1).setOnClickListener(view -> {
                bsd.first.dismiss();
                deleteEntry(v, ListActivity.this, e);
            });
            bsd.second.get(2).setOnClickListener(view -> {
                bsd.first.dismiss();
                Pair<Snackbar, LinearLayout> pairToast = ToastUtil.showMoveToast(getLayoutInflater(), v, binding.floatAdd);
                pairToast.second.setOnClickListener(view1 -> {
                    pairToast.first.dismiss();
                    moveCopyEntry(v, this, e, 1);
                });
            });
            bsd.second.get(3).setOnClickListener(view -> {
                bsd.first.dismiss();
                Pair<Snackbar, LinearLayout> pairToast = ToastUtil.showMoveToast(getLayoutInflater(), v, binding.floatAdd);
                pairToast.second.setOnClickListener(view1 -> {
                    pairToast.first.dismiss();
                    moveCopyEntry(v, this, e, 2);
                });
            });
            bsd.first.show();
        });

        if (showGroupInfo) {
            TextView adapterGroupInfo = viewToLoad.findViewById(R.id.adapterGroupInfo);
            String path = e.getPath();
            if (path != null) {
                path = path.substring(1, path.length());
                path = path.replace("/", Common.DOT_SYMBOL_CODE);
                adapterGroupInfo.setVisibility(View.VISIBLE);
                adapterGroupInfo.setText(path);
            }
        }
        runOnUiThread(() -> {
            CardView cardView = viewToLoad.findViewById(R.id.adapterCardView);
            if (!isFromBack) {
                LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(this, R.animator.anim_bottom), Common.ANIMATION_TIME);
                cardView.setLayoutAnimation(lac);
            } else {
                LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(this, R.animator.anim_bottom), Common.ANIMATION_TIME);
                cardView.setLayoutAnimation(lac);
            }
            binding.entriesLinearLayout.addView(viewToLoad);
        });
    }

    private void deleteGroup(View v, Activity activity, Group group) {

        Triplet<AlertDialog, MaterialButton, MaterialButton> confirmDialog = ConfirmDialogUtil.getConfirmDialog(activity.getLayoutInflater(), activity);
        confirmDialog.second.setOnClickListener(viewObj -> {
            final AlertDialog alertDialog = ProgressDialogUtil.getSaving(activity.getLayoutInflater(), activity);
            ProgressDialogUtil.showSavingDialog(alertDialog);
            new Thread(() -> {
                String groupName = null;
                Group parent = Common.group;

                if (parent == null) {
                    ProgressDialogUtil.dismissSavingDialog(alertDialog);
                    ToastUtil.showToast(activity.getLayoutInflater(), v, "Parent is null", binding.getRoot().findViewById(R.id.floatAdd));
                } else {
                    parent.removeGroup(group);
                    Common.group = parent;
                    boolean isOk = checkAndGetPermission(v, ListActivity.this);
                    if (isOk && Common.isCodecAvailable) {
                        ProgressDialogUtil.setSavingProgress(alertDialog, 30);
                        OutputStream fileOutputStream = null;
                        try {
                            //activity.getContentResolver().takePersistableUriPermission(Common.kdbxFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            ProgressDialogUtil.setSavingProgress(alertDialog, 40);
                            fileOutputStream = activity.getContentResolver().openOutputStream(Common.kdbxFileUri, "wt");
                            ProgressDialogUtil.setSavingProgress(alertDialog, 50);
                            Common.database.save(Common.creds, fileOutputStream);
                            ProgressDialogUtil.setSavingProgress(alertDialog, 100);
                            Intent intent = new Intent(activity, ListActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("click", "group");
                            intent.putExtras(bundle);
                            activity.startActivity(intent);
                            activity.finish();
                        } catch (NoSuchMethodError e) {
                            ProgressDialogUtil.dismissSavingDialog(alertDialog);
                            ToastUtil.showToast(activity.getLayoutInflater(), v, e.getMessage(), binding.getRoot().findViewById(R.id.floatAdd));
                        } catch (Exception e) {
                            ProgressDialogUtil.dismissSavingDialog(alertDialog);
                            ToastUtil.showToast(activity.getLayoutInflater(), v, e.getMessage(), binding.getRoot().findViewById(R.id.floatAdd));
                        } finally {
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (Exception e) {
                                    //do nothing
                                }
                            }
                        }
                    } else {
                        ProgressDialogUtil.dismissSavingDialog(alertDialog);
                        ToastUtil.showToast(activity.getLayoutInflater(), v, R.string.permissionNotGranted, binding.getRoot().findViewById(R.id.floatAdd));
                    }
                }
            }).start();
        });
        ConfirmDialogUtil.showDialog(confirmDialog.first);
    }

    private void deleteEntry(View v, Activity activity, Entry entry) {

        Triplet<AlertDialog, MaterialButton, MaterialButton> confirmDialog = ConfirmDialogUtil.getConfirmDialog(activity.getLayoutInflater(), activity);
        confirmDialog.second.setOnClickListener(viewObj -> {
            final AlertDialog alertDialog = ProgressDialogUtil.getSaving(activity.getLayoutInflater(), activity);
            ProgressDialogUtil.showSavingDialog(alertDialog);
            new Thread(() -> {
                String groupName = null;
                Group parent = Common.group;

                if (parent == null) {
                    ProgressDialogUtil.dismissSavingDialog(alertDialog);
                    ToastUtil.showToast(activity.getLayoutInflater(), v, "Group is null", binding.getRoot().findViewById(R.id.floatAdd));
                } else {
                    parent.removeEntry(entry);
                    boolean isOk = checkAndGetPermission(v, ListActivity.this);
                    if (isOk && Common.isCodecAvailable) {
                        ProgressDialogUtil.setSavingProgress(alertDialog, 30);
                        OutputStream fileOutputStream = null;
                        try {
                            //activity.getContentResolver().takePersistableUriPermission(Common.kdbxFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            ProgressDialogUtil.setSavingProgress(alertDialog, 40);
                            fileOutputStream = activity.getContentResolver().openOutputStream(Common.kdbxFileUri, "wt");
                            ProgressDialogUtil.setSavingProgress(alertDialog, 50);
                            Common.database.save(Common.creds, fileOutputStream);
                            ProgressDialogUtil.setSavingProgress(alertDialog, 100);
                            Intent intent = new Intent(activity, ListActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("click", "group");
                            intent.putExtras(bundle);
                            activity.startActivity(intent);
                            activity.finish();
                        } catch (NoSuchMethodError e) {
                            ProgressDialogUtil.dismissSavingDialog(alertDialog);
                            ToastUtil.showToast(activity.getLayoutInflater(), v, e.getMessage(), binding.getRoot().findViewById(R.id.floatAdd));
                        } catch (Exception e) {
                            ProgressDialogUtil.dismissSavingDialog(alertDialog);
                            ToastUtil.showToast(activity.getLayoutInflater(), v, e.getMessage(), binding.getRoot().findViewById(R.id.floatAdd));
                        } finally {
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (Exception e) {
                                    //do nothing
                                }
                            }
                        }
                    } else {
                        ProgressDialogUtil.dismissSavingDialog(alertDialog);
                        ToastUtil.showToast(activity.getLayoutInflater(), v, R.string.permissionNotGranted, binding.getRoot().findViewById(R.id.floatAdd));
                    }
                }
            }).start();
        });
        confirmDialog.first.show();
    }

    private void moveCopyEntry(View v, Activity activity, Entry entry, int type) {
        Triplet<AlertDialog, MaterialButton, MaterialButton> confirmDialog = ConfirmDialogUtil.getConfirmDialog(activity.getLayoutInflater(), activity);
        confirmDialog.second.setOnClickListener(viewObj -> {
            final AlertDialog alertDialog = ProgressDialogUtil.getSaving(activity.getLayoutInflater(), activity);
            ProgressDialogUtil.showSavingDialog(alertDialog);
            new Thread(() -> {
                Group group = null;
                if (type == 1) {
                    // move
                    entry.getParent().removeEntry(entry);
                    group = Common.group;
                    group.addEntry(entry);
                    Common.group = group;
                } else {
                    // copy
                    group = Common.group;
                    Entry copied = (Entry) copyEntry(entry);
                    group.addEntry(copied);
                    Common.group = group;
                }
                if (group == null) {
                    ProgressDialogUtil.dismissSavingDialog(alertDialog);
                    ToastUtil.showToast(activity.getLayoutInflater(), v, "Group is null", binding.getRoot().findViewById(R.id.floatAdd));
                } else {
                    boolean isOk = checkAndGetPermission(v, ListActivity.this);
                    if (isOk && Common.isCodecAvailable) {
                        ProgressDialogUtil.setSavingProgress(alertDialog, 30);
                        OutputStream fileOutputStream = null;
                        try {
                            //activity.getContentResolver().takePersistableUriPermission(Common.kdbxFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            ProgressDialogUtil.setSavingProgress(alertDialog, 40);
                            fileOutputStream = activity.getContentResolver().openOutputStream(Common.kdbxFileUri, "wt");
                            ProgressDialogUtil.setSavingProgress(alertDialog, 50);
                            Common.database.save(Common.creds, fileOutputStream);
                            ProgressDialogUtil.setSavingProgress(alertDialog, 100);
                            Intent intent = new Intent(activity, ListActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("click", "group");
                            intent.putExtras(bundle);
                            activity.startActivity(intent);
                            activity.finish();
                        } catch (NoSuchMethodError e) {
                            ProgressDialogUtil.dismissSavingDialog(alertDialog);
                            ToastUtil.showToast(activity.getLayoutInflater(), v, e.getMessage(), binding.getRoot().findViewById(R.id.floatAdd));
                        } catch (Exception e) {
                            ProgressDialogUtil.dismissSavingDialog(alertDialog);
                            ToastUtil.showToast(activity.getLayoutInflater(), v, e.getMessage(), binding.getRoot().findViewById(R.id.floatAdd));
                        } finally {
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (Exception e) {
                                    //do nothing
                                }
                            }
                        }
                    } else {
                        ProgressDialogUtil.dismissSavingDialog(alertDialog);
                        ToastUtil.showToast(activity.getLayoutInflater(), v, R.string.permissionNotGranted, binding.getRoot().findViewById(R.id.floatAdd));
                    }
                }
            }).start();
        });
        confirmDialog.first.show();
    }

    private void moveCopyGroup(View v, Activity activity, Group group, int type) {
        Triplet<AlertDialog, MaterialButton, MaterialButton> confirmDialog = ConfirmDialogUtil.getConfirmDialog(activity.getLayoutInflater(), activity);
        confirmDialog.second.setOnClickListener(viewObj -> {
            final AlertDialog alertDialog = ProgressDialogUtil.getSaving(activity.getLayoutInflater(), activity);
            ProgressDialogUtil.showSavingDialog(alertDialog);
            new Thread(() -> {
                Group groupToMove = null;
                if (type == 1) {
                    groupToMove = Common.group;
                    group.getParent().removeGroup(group);
                    groupToMove.addGroup(group);
                    Common.group = groupToMove;
                } else {
                    groupToMove = Common.group;
                    groupToMove.addGroup(copyGroup(group));
                    Common.group = groupToMove;
                }
                if (groupToMove == null) {
                    ProgressDialogUtil.dismissSavingDialog(alertDialog);
                    ToastUtil.showToast(activity.getLayoutInflater(), v, "Group is null", binding.getRoot().findViewById(R.id.floatAdd));
                } else {
                    boolean isOk = checkAndGetPermission(v, ListActivity.this);
                    if (isOk && Common.isCodecAvailable) {
                        ProgressDialogUtil.setSavingProgress(alertDialog, 30);
                        OutputStream fileOutputStream = null;
                        try {
                            //activity.getContentResolver().takePersistableUriPermission(Common.kdbxFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            ProgressDialogUtil.setSavingProgress(alertDialog, 40);
                            fileOutputStream = activity.getContentResolver().openOutputStream(Common.kdbxFileUri, "wt");
                            ProgressDialogUtil.setSavingProgress(alertDialog, 50);
                            Common.database.save(Common.creds, fileOutputStream);
                            ProgressDialogUtil.setSavingProgress(alertDialog, 100);
                            ProgressDialogUtil.dismissSavingDialog(alertDialog);
                            Intent intent = new Intent(activity, ListActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("click", "group");
                            intent.putExtras(bundle);
                            activity.startActivity(intent);
                            activity.finish();
                        } catch (NoSuchMethodError e) {
                            ProgressDialogUtil.dismissSavingDialog(alertDialog);
                            ToastUtil.showToast(activity.getLayoutInflater(), v, e.getMessage(), binding.getRoot().findViewById(R.id.floatAdd));
                        } catch (Exception e) {
                            ProgressDialogUtil.dismissSavingDialog(alertDialog);
                            ToastUtil.showToast(activity.getLayoutInflater(), v, e.getMessage(), binding.getRoot().findViewById(R.id.floatAdd));
                        } finally {
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (Exception e) {
                                    //do nothing
                                }
                            }
                        }
                    } else {
                        ProgressDialogUtil.dismissSavingDialog(alertDialog);
                        ToastUtil.showToast(activity.getLayoutInflater(), v, R.string.permissionNotGranted, binding.getRoot().findViewById(R.id.floatAdd));
                    }
                }
            }).start();
        });
        confirmDialog.first.show();
    }

    @SuppressLint("ResourceType")
    private void search(View v, Activity activity) {
        Triplet<BottomSheetDialog, MaterialButton, TextInputEditText> searchDialog = SearchDialogUtil.getSearchDialog(activity.getLayoutInflater(), activity);
        searchDialog.second.setOnClickListener(viewObj -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            final AlertDialog alertDialog = ProgressDialogUtil.getSearch(activity.getLayoutInflater(), activity);
            ProgressDialogUtil.showSearchDialog(alertDialog);
            isSearchView = true;
            updateSearchProgress(alertDialog, 20);
            binding.groupsLinearLayout.removeAllViews();
            binding.entriesLinearLayout.removeAllViews();
            binding.groupScrollView.fullScroll(View.FOCUS_UP);
            binding.groupName.setText(getString(R.string.search) + ": " + searchDialog.third.getText().toString());
            //binding.groupName.startAnimation(AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.animator.anim_bottom));
            binding.justGroupsTextView.setVisibility(View.GONE);
            binding.totalCountDisplayLayout.setVisibility(View.GONE);
            binding.totalExpiredCountDisplayLayout.setVisibility(View.GONE);
            binding.totalExpiringSoonCountDisplayLayout.setVisibility(View.GONE);
            TextView justEntriesTextView = binding.justEntriesTextView;
            TextView justNothingTextView = binding.justNothingTextView;
            updateSearchProgress(alertDialog, 30);
            new Thread(() -> {
                List<?> searchedEntries = Common.database.findEntries(searchDialog.third.getText().toString());
                activity.runOnUiThread(() -> {
                    if (searchedEntries != null && searchedEntries.size() > 0) {
                        justEntriesTextView.setVisibility(View.VISIBLE);
                        justNothingTextView.setVisibility(View.GONE);
                    } else {
                        justEntriesTextView.setVisibility(View.GONE);
                        justNothingTextView.setVisibility(View.VISIBLE);
                        justNothingTextView.startAnimation(AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.animator.anim_bottom));
                    }
                });
                updateSearchProgress(alertDialog, 50);
                for (int eCount = 0; eCount < searchedEntries.size(); eCount++) {
                    Util.sleepFor100Sec();
                    Entry<?, ?, ?, ?> localEntry = (Entry<?, ?, ?, ?>) searchedEntries.get(eCount);
                    addEntryOnUi(localEntry, false, true);
                }
                dismissSearchDialog(alertDialog);
                runOnUiThread(() -> {
                    searchDialog.first.dismiss();
                });

            }).start();
        });
        searchDialog.first.show();
    }

    private void dismissSearchDialog(AlertDialog alertDialog) {
        runOnUiThread(() -> {
            ProgressDialogUtil.dismissSearchDialog(alertDialog);
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICK_FOLDER_OPEN_RESULT_CODE:
                if (resultCode == -1) {
                    Uri newFile = data.getData();
                    if (newFile != null) {
                        export(newFile);
                    }
                }
                break;
        }
    }

    private void export(Uri newFile) {
        AlertDialog d = ProgressDialogUtil.getSaving(getLayoutInflater(), binding.getRoot().getContext());
        ProgressDialogUtil.showSavingDialog(d);
        runOnUiThread(() -> {
            String fileName = "";
            try {
                getContentResolver().takePersistableUriPermission(newFile, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                this.grantUriPermission(this.getPackageName(), newFile, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } catch (Exception e) {
                ToastUtil.showToast(getLayoutInflater(), binding.getRoot(), R.string.writePermissionNotGotError, binding.getRoot().findViewById(R.id.floatAdd));
            }
            Database<?, ?, ?, ?> database = Common.database;
            OutputStream fileOutputStream = null;
            try {
                ProgressDialogUtil.setSavingProgress(d, 50);
                KdbxCreds creds = Common.creds;
                fileOutputStream = getContentResolver().openOutputStream(newFile, "wt");
                database.save(creds, fileOutputStream);
            } catch (NoSuchMethodError e) {
                ToastUtil.showToast(getLayoutInflater(), binding.getRoot(), e.getMessage(), binding.getRoot().findViewById(R.id.floatAdd));
            } catch (Exception e) {
                ToastUtil.showToast(getLayoutInflater(), binding.getRoot(), e.getMessage(), binding.getRoot().findViewById(R.id.floatAdd));
                Log.e("KP", "KP error ", e);
            } finally {
                ProgressDialogUtil.dismissSavingDialog(d);
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (Exception e) {
                        //do nothing
                    }
                }
            }
        });
    }

    private boolean checkAndGetPermission(View v, Activity activity) {
        boolean isOK = false;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
            }

            if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                isOK = true;
            } else {
                ToastUtil.showToast(getLayoutInflater(), v, R.string.permissionNotGranted, binding.getRoot().findViewById(R.id.floatAdd));
            }
        } else {
            if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_IMAGES}, READ_EXTERNAL_STORAGE);
            }

            if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                isOK = true;
            } else {
                ToastUtil.showToast(getLayoutInflater(), v, R.string.permissionNotGranted, binding.getRoot().findViewById(R.id.floatAdd));
            }
        }
        return isOK;
    }

    private Entry copyEntry(Entry e) {
        Entry c = Common.database.newEntry();
        c.setExpiryTime(e.getExpiryTime());
        c.setTitle(e.getTitle());
        c.setNotes(e.getNotes());
        c.setPassword(e.getPassword());
        c.setExpires(e.getExpires());
        c.setIcon(e.getIcon());
        c.setUrl(e.getUrl());
        c.setUsername(e.getUsername());
        try {
            ArrayList<String> props = (ArrayList<String>) e.getPropertyNames();
            if (props != null) {
                for (String p : props) {
                    c.setProperty(p, e.getProperty(p));
                }
            }
        } catch (ClassCastException ce) {

        }
        try {
            ArrayList<String> bprops = (ArrayList<String>) e.getBinaryPropertyNames();
            if (bprops != null) {
                for (String bp : bprops) {
                    c.setProperty(bp, e.getProperty(bp));
                }
            }
        } catch (ClassCastException ce) {

        }
        return c;

    }

    private Group copyGroup(Group g) {
        Group c = Common.database.newGroup();
        c.setName(g.getName());
        c.setIcon(g.getIcon());
        try {
            ArrayList<Group> subG = (ArrayList<Group>) g.getGroups();
            if (subG != null) {
                for (Group sg : subG) {
                    c.addGroup(copyGroup(sg));
                }
            }
        } catch (ClassCastException ce) {

        }
        try {
            ArrayList<Entry> subE = (ArrayList<Entry>) g.getEntries();
            if (subE != null) {
                for (Entry se : subE) {
                    c.addEntry(copyEntry(se));
                }
            }
        } catch (ClassCastException ce) {

        }

        return c;
    }

    private void updateProgress(AlertDialog alertDialog, int progress) {
        runOnUiThread(() -> {
            ProgressDialogUtil.setLoadingProgress(alertDialog, progress);
        });
    }

    private void updateSearchProgress(AlertDialog alertDialog, int progress) {
        runOnUiThread(() -> {
            ProgressDialogUtil.setSearchProgress(alertDialog, progress);
        });
    }
}