/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.preferences;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
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
import uz.unnarsx.cherrygram.core.ui.MD3ListAdapter;
import uz.unnarsx.cherrygram.donates.DonatesManager;
import uz.unnarsx.cherrygram.helpers.ui.PopupHelper;

public class ExperimentalPreferencesEntry extends BaseFragment {

    private int rowCount;
    private ListAdapter listAdapter;
    private RecyclerListView listView;

    private int generalHeaderRow;
    private int springAnimationRow;
    private int actionbarCrossfadeRow;
    private int generalDivisorRow;
    
    private int chatsHeaderRow;
    private int customChatRow;
    private int customChatPreviewRow;
    private int chatsDivisorRow;

    private int networkHeaderRow;
    private int downloadSpeedBoostRow;
    private int uploadSpeedBoostRow;
    private int slowNetworkMode;
    private int networkDivisorRow;

    protected Theme.ResourcesProvider resourcesProvider;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        updateRowsId(true);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonDrawable(new BackDrawable(false));

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
            var holder = listView.findViewHolderForAdapterPosition(position);
            if (holder == null || !listAdapter.isEnabled(holder)) {
                return;
            }
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

    private class ListAdapter extends MD3ListAdapter {

        private final Context mContext;

        private final int VIEW_TYPE_SHADOW = 0;
        private final int VIEW_TYPE_HEADER = 1;
//        private final int VIEW_TYPE_TEXT_CELL = 2;
        private final int VIEW_TYPE_TEXT_CHECK = 3;
        private final int VIEW_TYPE_TEXT_SETTINGS = 4;
//        private final int VIEW_TYPE_TEXT_INFO_PRIVACY = 5;
//        private final int VIEW_TYPE_TEXT_DETAIL_SETTINGS = 6;
        private final int VIEW_TYPE_USER = 7;

        ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            boolean requireDonate = !DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace();
            switch (holder.getItemViewType()) {
                case VIEW_TYPE_SHADOW:
                    holder.itemView.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case VIEW_TYPE_HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == generalHeaderRow) {
                        headerCell.setText(getString(R.string.General));
                    } else if (position == chatsHeaderRow) {
                        headerCell.setText(getString(R.string.FilterChats));
                    } else if (position == networkHeaderRow) {
                        headerCell.setText(getString(R.string.EP_Network));
                    }
                    break;
                case VIEW_TYPE_TEXT_CHECK:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    textCheckCell.setEnabled(true, null);
                    if (position == actionbarCrossfadeRow) {
                        textCheckCell.setTextAndCheck(getString(R.string.EP_NavigationAnimationCrossfading), CherrygramExperimentalConfig.INSTANCE.getActionbarCrossfade(), true);
                    } else if (position == customChatRow) {
                        textCheckCell.setTextAndValueAndCheck(getString(R.string.EP_CustomChat), getString(R.string.EP_CustomChat_Desc), CherrygramExperimentalConfig.INSTANCE.getCustomChatForSavedMessages(), true, true);
                    } else if (position == uploadSpeedBoostRow) {
                        textCheckCell.setTextAndCheck(getString(R.string.EP_UploadloadSpeedBoost), CherrygramExperimentalConfig.INSTANCE.getUploadSpeedBoost(), true);
                    } else if (position == slowNetworkMode) {
                        textCheckCell.setTextAndCheck(getString(R.string.EP_SlowNetworkMode), CherrygramExperimentalConfig.INSTANCE.getSlowNetworkMode(), true);
                    }
                    break;
                case VIEW_TYPE_TEXT_SETTINGS:
                    TextSettingsCell textSettingsCell = (TextSettingsCell) holder.itemView;
                    textSettingsCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
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
                        textSettingsCell.setTextAndValue(getString(R.string.EP_NavigationAnimation), value, true);
                    } else if (position == downloadSpeedBoostRow) {
                        textSettingsCell.setTextAndValue(getString(R.string.EP_DownloadSpeedBoost), CGResourcesHelper.INSTANCE.getDownloadSpeedBoostText(), true);
                    }
                    break;
                case VIEW_TYPE_USER:
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
            return type == VIEW_TYPE_TEXT_CHECK || type == VIEW_TYPE_TEXT_SETTINGS /*|| type == VIEW_TYPE_USER*/;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case VIEW_TYPE_SHADOW:
                    view = new ShadowSectionCell(mContext);
                    break;
                case VIEW_TYPE_HEADER:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_TEXT_CHECK:
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_TEXT_SETTINGS:
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_USER:
                    view = new UserCell(getContext(), 14, 0, false, true, getResourceProvider(), false, false);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + viewType);
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == generalDivisorRow || position == chatsDivisorRow || position == networkDivisorRow) {
                return VIEW_TYPE_SHADOW;
            } else if (position == generalHeaderRow || position == chatsHeaderRow || position == networkHeaderRow) {
                return VIEW_TYPE_HEADER;
            } else if (position == actionbarCrossfadeRow || position == customChatRow || position == uploadSpeedBoostRow || position == slowNetworkMode) {
                return VIEW_TYPE_TEXT_CHECK;
            } else if (position == springAnimationRow || position == downloadSpeedBoostRow) {
                return VIEW_TYPE_TEXT_SETTINGS;
            } else if (position == customChatPreviewRow) {
                return VIEW_TYPE_USER;
            }
            return VIEW_TYPE_SHADOW;
        }
    }

    private void updateRowsId(boolean notify) {
        rowCount = 0;
        int prevCustomChatPreviewRow = customChatPreviewRow;
        int prevActionbarCrossfadeRow = actionbarCrossfadeRow;

        generalHeaderRow = rowCount++;
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
        generalDivisorRow = rowCount++;

        chatsHeaderRow = rowCount++;
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
        chatsDivisorRow = rowCount++;

        networkHeaderRow = rowCount++;
        downloadSpeedBoostRow = rowCount++;
        uploadSpeedBoostRow = rowCount++;
        slowNetworkMode = rowCount++;
        networkDivisorRow = rowCount++;

        if (listAdapter != null && notify) {
            listAdapter.notifyDataSetChanged();
        }

        if (listView != null) {
            listView.post(() -> {
                RecyclerView.Adapter adapter = listView.getAdapter();
                if (adapter instanceof MD3ListAdapter md3) {
                    md3.reapplyVisible();
                }
            });
        }
    }

}
