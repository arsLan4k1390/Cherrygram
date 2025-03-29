/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.preferences;

import static org.telegram.messenger.LocaleController.getString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.ItemOptions;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.ProfileActivity;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.chats.helpers.ChatsHelper2;
import uz.unnarsx.cherrygram.core.configs.CherrygramExperimentalConfig;
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper;
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper;
import uz.unnarsx.cherrygram.core.helpers.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.helpers.ui.PopupHelper;

public class ExperimentalPreferencesEntry extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private int rowCount;
    private ListAdapter listAdapter;
    private RecyclerListView listView;

    private int experimentalHeaderRow;
    private int springAnimationRow;
    private int actionbarCrossfadeRow;
    private int residentNotificationRow;
    private int customChatRow;
    private int customChatPreviewRow;
    private int experimentalSettingsDivisor;

    private int networkHeaderRow;
    private int downloadSpeedBoostRow;
    private int uploadSpeedBoostRow;
    private int slowNetworkMode;
    private int networkDivisorRow;

    protected Theme.ResourcesProvider resourcesProvider;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiLoaded);
        updateRowsId(true);
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiLoaded);
    }

    protected boolean hasWhiteActionBar() {
        return true;
    }

    @Override
    public boolean isLightStatusBar() {
        if (!hasWhiteActionBar()) return super.isLightStatusBar();
        int color = getThemedColor(Theme.key_windowBackgroundWhite);
        return ColorUtils.calculateLuminance(color) > 0.7f;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonDrawable(new BackDrawable(false));

        actionBar.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
        actionBar.setItemsColor(getThemedColor(Theme.key_windowBackgroundWhiteBlackText), false);
        actionBar.setItemsBackgroundColor(getThemedColor(Theme.key_actionBarActionModeDefaultSelector), true);
        actionBar.setItemsBackgroundColor(getThemedColor(Theme.key_actionBarWhiteSelector), false);
        actionBar.setItemsColor(getThemedColor(Theme.key_actionBarActionModeDefaultIcon), true);
        actionBar.setTitleColor(getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
        actionBar.setCastShadows(false);

        actionBar.setTitle(getString(R.string.EP_Category_Experimental));
        actionBar.setAllowOverlayTitle(false);

        actionBar.setOccupyStatusBar(!AndroidUtilities.isTablet());
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        listAdapter = new ListAdapter(context);

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(false);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setAdapter(listAdapter);
        if (listView.getItemAnimator() != null) {
            ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
        }
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setOnItemClickListener((view, position, x, y) -> {
            if (position == springAnimationRow) {
                ArrayList<String> configStringKeys = new ArrayList<>();
                ArrayList<Integer> configValues = new ArrayList<>();

                configStringKeys.add(getString(R.string.EP_NavigationAnimationSpring));
                configValues.add(CherrygramExperimentalConfig.ANIMATION_SPRING);

                configStringKeys.add(getString(R.string.EP_NavigationAnimationBezier));
                configValues.add(CherrygramExperimentalConfig.ANIMATION_CLASSIC);

                PopupHelper.show(configStringKeys, getString(R.string.EP_NavigationAnimation), configValues.indexOf(CherrygramExperimentalConfig.INSTANCE.getSpringAnimation()), context, i -> {
                    CherrygramExperimentalConfig.INSTANCE.setSpringAnimation(configValues.get(i));

                    listAdapter.notifyItemChanged(springAnimationRow);
                    updateRowsId(false);
                    AppRestartHelper.createRestartBulletin(this);
                });
            } else if (position == actionbarCrossfadeRow) {
                CherrygramExperimentalConfig.INSTANCE.setActionbarCrossfade(!CherrygramExperimentalConfig.INSTANCE.getActionbarCrossfade());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramExperimentalConfig.INSTANCE.getActionbarCrossfade());
                }
                AppRestartHelper.createRestartBulletin(this);
            } else if (position == residentNotificationRow) {
                CherrygramExperimentalConfig.INSTANCE.setResidentNotification(!CherrygramExperimentalConfig.INSTANCE.getResidentNotification());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramExperimentalConfig.INSTANCE.getResidentNotification());
                }
                AppRestartHelper.createRestartBulletin(this);
            } else if (position == customChatRow) {
                CherrygramExperimentalConfig.INSTANCE.setCustomChatForSavedMessages(!CherrygramExperimentalConfig.INSTANCE.getCustomChatForSavedMessages());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramExperimentalConfig.INSTANCE.getCustomChatForSavedMessages());
                }
                updateRowsId(false);
            } else if (position == customChatPreviewRow) {
                if (view instanceof UserCell) {
                    ItemOptions options = ItemOptions.makeOptions(this, view);
                    options.add(R.drawable.msg_openprofile, getString(R.string.OpenProfile),
                            () -> presentFragment(ProfileActivity.of(ChatsHelper2.INSTANCE.getCustomChatID())
                    ));
                    options.add(R.drawable.msg_discussion, getString(R.string.AccDescrOpenChat),
                            () -> presentFragment(ChatActivity.of(ChatsHelper2.INSTANCE.getCustomChatID())
                    ));
                    options.show();
                }
            } else if (position == downloadSpeedBoostRow) {
                ArrayList<String> configStringKeys = new ArrayList<>();
                ArrayList<Integer> configValues = new ArrayList<>();

                configStringKeys.add(getString(R.string.EP_DownloadSpeedBoostNone));
                configValues.add(CherrygramExperimentalConfig.BOOST_NONE);

                configStringKeys.add(getString(R.string.EP_DownloadSpeedBoostAverage));
                configValues.add(CherrygramExperimentalConfig.BOOST_AVERAGE);

                configStringKeys.add(getString(R.string.EP_DownloadSpeedBoostExtreme));
                configValues.add(CherrygramExperimentalConfig.BOOST_EXTREME);

                PopupHelper.show(configStringKeys, getString(R.string.EP_DownloadSpeedBoost), configValues.indexOf(CherrygramExperimentalConfig.INSTANCE.getDownloadSpeedBoost()), context, i -> {
                    CherrygramExperimentalConfig.INSTANCE.setDownloadSpeedBoost(configValues.get(i));

                    listAdapter.notifyItemChanged(downloadSpeedBoostRow);
                    AppRestartHelper.createRestartBulletin(this);
                });
            } else if (position == uploadSpeedBoostRow) {
                CherrygramExperimentalConfig.INSTANCE.setUploadSpeedBoost(!CherrygramExperimentalConfig.INSTANCE.getUploadSpeedBoost());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramExperimentalConfig.INSTANCE.getUploadSpeedBoost());
                }
                AppRestartHelper.createRestartBulletin(this);
            } else if (position == slowNetworkMode) {
                CherrygramExperimentalConfig.INSTANCE.setSlowNetworkMode(!CherrygramExperimentalConfig.INSTANCE.getSlowNetworkMode());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramExperimentalConfig.INSTANCE.getSlowNetworkMode());
                }
                AppRestartHelper.createRestartBulletin(this);
            }
        });

        FirebaseAnalyticsHelper.trackEventWithEmptyBundle("experimental_preferences_screen");

        return fragmentView;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateRowsId(boolean notify) {
        rowCount = 0;
        int prevCustomChatPreviewRow = customChatPreviewRow;
        int prevActionbarCrossfadeRow = actionbarCrossfadeRow;

        experimentalHeaderRow = rowCount++;
        springAnimationRow = rowCount++;
        actionbarCrossfadeRow = -1;
        if (CherrygramExperimentalConfig.INSTANCE.getSpringAnimation() == CherrygramExperimentalConfig.ANIMATION_SPRING) actionbarCrossfadeRow = rowCount++;
        if (listAdapter != null) {
            if (prevActionbarCrossfadeRow == -1 && actionbarCrossfadeRow != -1) {
                listAdapter.notifyItemInserted(actionbarCrossfadeRow);
            } else if (prevActionbarCrossfadeRow != -1 && actionbarCrossfadeRow == -1) {
                listAdapter.notifyItemRemoved(prevActionbarCrossfadeRow);
            }
        }

        residentNotificationRow = rowCount++;

        customChatRow = rowCount++;
        customChatPreviewRow = -1;
        if (CherrygramExperimentalConfig.INSTANCE.getCustomChatForSavedMessages()) customChatPreviewRow = rowCount++;
        if (listAdapter != null) {
            if (prevCustomChatPreviewRow == -1 && customChatPreviewRow != -1) {
                listAdapter.notifyItemInserted(customChatPreviewRow);
            } else if (prevCustomChatPreviewRow != -1 && customChatPreviewRow == -1) {
                listAdapter.notifyItemRemoved(prevCustomChatPreviewRow);
            }
        }

        experimentalSettingsDivisor = rowCount++;

        networkHeaderRow = rowCount++;
        downloadSpeedBoostRow = rowCount++;
        uploadSpeedBoostRow = rowCount++;
        slowNetworkMode = rowCount++;
        networkDivisorRow = rowCount++;

        if (listAdapter != null && notify) {
            listAdapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    public class ListAdapter extends RecyclerListView.SelectionAdapter {
        private final Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 1:
                    holder.itemView.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case 2:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == experimentalHeaderRow) {
                        headerCell.setText(getString(R.string.General));
                    } else if (position == networkHeaderRow) {
                        headerCell.setText(getString(R.string.EP_Network));
                    }
                    break;
                case 3:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    textCheckCell.setEnabled(true, null);
                    if (position == actionbarCrossfadeRow) {
                        textCheckCell.setTextAndCheck(getString(R.string.EP_NavigationAnimationCrossfading), CherrygramExperimentalConfig.INSTANCE.getActionbarCrossfade(), true);
                    } else if (position == residentNotificationRow) {
                        textCheckCell.setTextAndCheck(getString(R.string.CG_ResidentNotification), CherrygramExperimentalConfig.INSTANCE.getResidentNotification(), true);
                    } else if (position == customChatRow) {
                        textCheckCell.setTextAndValueAndCheck(getString(R.string.EP_CustomChat), getString(R.string.EP_CustomChat_Desc), CherrygramExperimentalConfig.INSTANCE.getCustomChatForSavedMessages(), true, true);
                    } else if (position == uploadSpeedBoostRow) {
                        textCheckCell.setTextAndCheck(getString(R.string.EP_UploadloadSpeedBoost), CherrygramExperimentalConfig.INSTANCE.getUploadSpeedBoost(), true);
                    } else if (position == slowNetworkMode) {
                        textCheckCell.setTextAndCheck(getString(R.string.EP_SlowNetworkMode), CherrygramExperimentalConfig.INSTANCE.getSlowNetworkMode(), true);
                    }
                    break;
                case 4:
                    TextSettingsCell textCell = (TextSettingsCell) holder.itemView;
                    textCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    if (position == springAnimationRow) {
                        String value;
                        switch (CherrygramExperimentalConfig.INSTANCE.getSpringAnimation()) {
                            case CherrygramExperimentalConfig.ANIMATION_CLASSIC:
                                value = getString(R.string.EP_NavigationAnimationBezier);
                                break;
                            default:
                            case CherrygramExperimentalConfig.ANIMATION_SPRING:
                                value = getString(R.string.EP_NavigationAnimationSpring);
                                break;
                        }
                        textCell.setTextAndValue(getString(R.string.EP_NavigationAnimation), value, true);
                    } else if (position == downloadSpeedBoostRow) {
                        textCell.setTextAndValue(getString(R.string.EP_DownloadSpeedBoost), CGResourcesHelper.getDownloadSpeedBoostText(), true);
                    }
                    break;
                case 5:
                    UserCell userCell = (UserCell) holder.itemView;
                    userCell.addButton.setText(getString(R.string.Edit));
                    userCell.addButton.setOnClickListener(view1 -> {
                        if (getUserConfig().getCurrentUser() == null) {
                            return;
                        }
                        Bundle args = new Bundle();
                        args.putBoolean("onlySelect", true);
                        args.putBoolean("cgPrefs", true);
                        args.putBoolean("allowGlobalSearch", false);
                        args.putInt("dialogsType", DialogsActivity.DIALOGS_TYPE_FORWARD);
                        args.putBoolean("resetDelegate", false);
                        args.putBoolean("closeFragment", true);
                        DialogsActivity fragment = new DialogsActivity(args);
                        fragment.setDelegate((fragment1, dids, message, param, notify, scheduleDate, topicsFragment) -> {
                            long did = dids.get(0).dialogId;

                            String selectedChatId = String.valueOf(did);

                            SharedPreferences.Editor editor = MessagesController.getMainSettings(currentAccount).edit();
                            editor.putString("CP_CustomChatIDSM", selectedChatId).apply();

                            fragment.finishFragment(true);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                LaunchActivity.makeRipple(userCell.getLeft(), userCell.getY(), 5f);
                            }
                            return true;
                        });
                        presentFragment(fragment);
                    });
                    if (position == customChatPreviewRow) {
                        long chatId = ChatsHelper2.INSTANCE.getCustomChatID();

                        TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(-chatId);
                        TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(chatId);

                        StringBuilder status = new StringBuilder();
                        status.append(getString(R.string.EP_CustomChat_Selected_Title));
                        status.append(' ');
                        status.append("\"");
                        status.append(getString(R.string.SavedMessages));
                        status.append("\".");

                        if (chatId == getUserConfig().clientUserId) {
                            userCell.setData("saved_cg", getString(R.string.SavedMessages), "", 0);
                        } else if (chat != null) {
                            userCell.setData(chat, chat.title, status, 0);
                        } else {
                            userCell.setData(user, UserObject.getUserName(user), status, 0);
                        }
                    }
                    break;
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == 3 || type == 4 /*|| type == 5*/;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 2:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 3:
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 4:
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 5:
                    view = new UserCell(getContext(), 14, 0, false, true, getResourceProvider(), false, false);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                default:
                    view = new ShadowSectionCell(mContext);
                    break;
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == experimentalSettingsDivisor || position == networkDivisorRow) {
                return 1;
            } else if (position == experimentalHeaderRow || position == networkHeaderRow) {
                return 2;
            } else if (position == actionbarCrossfadeRow || position == residentNotificationRow || position == customChatRow || position == uploadSpeedBoostRow || position == slowNetworkMode) {
                return 3;
            } else if (position == springAnimationRow || position == downloadSpeedBoostRow) {
                return 4;
            } else if (position == customChatPreviewRow) {
                return 5;
            }
            return 1;
        }
    }

    @Override
    public void didReceivedNotification(int id, int account, final Object... args) {
        if (id == NotificationCenter.emojiLoaded) {
            if (listView != null) {
                listView.invalidateViews();
            }
        }
    }
}
