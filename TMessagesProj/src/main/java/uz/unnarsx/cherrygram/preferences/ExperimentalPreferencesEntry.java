package uz.unnarsx.cherrygram.preferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.DialogsActivity;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.CGFeatureHooks;
import uz.unnarsx.cherrygram.CherrygramConfig;
import uz.unnarsx.cherrygram.helpers.AppRestartHelper;
import uz.unnarsx.cherrygram.helpers.ui.PopupHelper;
import uz.unnarsx.cherrygram.ui.dialogs.TextFieldAlert;

public class ExperimentalPreferencesEntry extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private int rowCount;
    private ListAdapter listAdapter;
    private RecyclerListView listView;

    private int experimentalHeaderRow;
    private int springAnimationRow;
    private int actionbarCrossfadeRow;
    private int residentNotificationRow;
    private int showRPCErrorRow;
    private int customChatRow;
    private int customChatIdRow;
    private int experimentalSettingsDivisor;

    private int networkHeaderRow;
    private int downloadSpeedBoostRow;
    private int uploadSpeedBoostRow;
    private int slowNetworkMode;

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

        actionBar.setTitle(LocaleController.getString("EP_Category_Experimental", R.string.EP_Category_Experimental));
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
                ArrayList<String> arrayList = new ArrayList<>();
                ArrayList<Integer> types = new ArrayList<>();
                arrayList.add(LocaleController.getString("EP_NavigationAnimationSpring", R.string.EP_NavigationAnimationSpring));
                types.add(CherrygramConfig.ANIMATION_SPRING);
                arrayList.add(LocaleController.getString("EP_NavigationAnimationBezier", R.string.EP_NavigationAnimationBezier));
                types.add(CherrygramConfig.ANIMATION_CLASSIC);

                PopupHelper.show(arrayList, LocaleController.getString("EP_NavigationAnimation", R.string.EP_NavigationAnimation), types.indexOf(CherrygramConfig.INSTANCE.getSpringAnimation()), context, i -> {
                    CherrygramConfig.INSTANCE.setSpringAnimation(types.get(i));
                    listAdapter.notifyItemChanged(springAnimationRow);

                    updateRowsId(false);
                    AppRestartHelper.createRestartBulletin(this);
                });
            } else if (position == actionbarCrossfadeRow) {
                CherrygramConfig.INSTANCE.toggleActionbarCrossfade();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramConfig.INSTANCE.getActionbarCrossfade());
                }
                AppRestartHelper.createRestartBulletin(this);
            } else if (position == residentNotificationRow) {
                CherrygramConfig.INSTANCE.toggleResidentNotification();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramConfig.INSTANCE.getResidentNotification());
                }
                AppRestartHelper.createRestartBulletin(this);
            } else if (position == showRPCErrorRow) {
                CherrygramConfig.INSTANCE.toggleShowRPCError();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramConfig.INSTANCE.getShowRPCError());
                }
                AppRestartHelper.createRestartBulletin(this);
            } else if (position == customChatRow) {
                CherrygramConfig.INSTANCE.toggleCustomChatForSavedMessages();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramConfig.INSTANCE.getCustomChatForSavedMessages());
                }
                updateRowsId(false);
            } else if (position == customChatIdRow) {
                String currentValue = MessagesController.getMainSettings(currentAccount).getString("CP_CustomChatIDSM",
                        String.valueOf(getUserConfig().getClientUserId())
                );
                TextFieldAlert.createFieldAlert(
                        context,
                        LocaleController.getString("EP_CustomChat", R.string.EP_CustomChat),
                        currentValue,
                        (result) -> {
                            if (result.isEmpty()) {
                                result = currentValue;
                            }
                            SharedPreferences.Editor editor = MessagesController.getMainSettings(currentAccount).edit();
                            editor.putString("CP_CustomChatIDSM", result).apply();
                            if (view instanceof TextSettingsCell) {
                                ((TextSettingsCell) view).getValueTextView().setText(result);
                            }
                            return null;
                        }
                );
            } else if (position == downloadSpeedBoostRow) {
                ArrayList<String> arrayList = new ArrayList<>();
                ArrayList<Integer> types = new ArrayList<>();
                arrayList.add(LocaleController.getString("EP_DownloadSpeedBoostNone", R.string.EP_DownloadSpeedBoostNone));
                types.add(CherrygramConfig.BOOST_NONE);
                arrayList.add(LocaleController.getString("EP_DownloadSpeedBoostAverage", R.string.EP_DownloadSpeedBoostAverage));
                types.add(CherrygramConfig.BOOST_AVERAGE);
                arrayList.add(LocaleController.getString("EP_DownloadSpeedBoostExtreme", R.string.EP_DownloadSpeedBoostExtreme));
                types.add(CherrygramConfig.BOOST_EXTREME);
                PopupHelper.show(arrayList, LocaleController.getString("EP_DownloadSpeedBoost", R.string.EP_DownloadSpeedBoost), types.indexOf(CherrygramConfig.INSTANCE.getDownloadSpeedBoost()), context, i -> {
                    CherrygramConfig.INSTANCE.setDownloadSpeedBoost(types.get(i));
                    listAdapter.notifyItemChanged(downloadSpeedBoostRow);
                    AppRestartHelper.createRestartBulletin(this);
                });
            } else if (position == uploadSpeedBoostRow) {
                CherrygramConfig.INSTANCE.toggleUploadSpeedBoost();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramConfig.INSTANCE.getUploadSpeedBoost());
                }
                AppRestartHelper.createRestartBulletin(this);
            } else if (position == slowNetworkMode) {
                CherrygramConfig.INSTANCE.toggleSlowNetworkMode();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramConfig.INSTANCE.getSlowNetworkMode());
                }
                AppRestartHelper.createRestartBulletin(this);
            }
        });

        return fragmentView;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateRowsId(boolean notify) {
        rowCount = 0;
        int prevCustomChatIdRow = customChatIdRow;
        int prevActionbarCrossfadeRow = actionbarCrossfadeRow;

        experimentalHeaderRow = rowCount++;
        springAnimationRow = rowCount++;
        actionbarCrossfadeRow = -1;
        if (CherrygramConfig.INSTANCE.getSpringAnimation() == CherrygramConfig.ANIMATION_SPRING) actionbarCrossfadeRow = rowCount++;
        if (listAdapter != null) {
            if (prevActionbarCrossfadeRow == -1 && actionbarCrossfadeRow != -1) {
                listAdapter.notifyItemInserted(actionbarCrossfadeRow);
            } else if (prevActionbarCrossfadeRow != -1 && actionbarCrossfadeRow == -1) {
                listAdapter.notifyItemRemoved(prevActionbarCrossfadeRow);
            }
        }

        residentNotificationRow = rowCount++;
        showRPCErrorRow = rowCount++;

        customChatRow = rowCount++;
        customChatIdRow = -1;
        if (CherrygramConfig.INSTANCE.getCustomChatForSavedMessages()) customChatIdRow = rowCount++;
        if (listAdapter != null) {
            if (prevCustomChatIdRow == -1 && customChatIdRow != -1) {
                listAdapter.notifyItemInserted(customChatIdRow);
            } else if (prevCustomChatIdRow != -1 && customChatIdRow == -1) {
                listAdapter.notifyItemRemoved(prevCustomChatIdRow);
            }
        }

        experimentalSettingsDivisor = rowCount++;

        networkHeaderRow = rowCount++;
        downloadSpeedBoostRow = rowCount++;
        uploadSpeedBoostRow = rowCount++;
        slowNetworkMode = rowCount++;

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

    private class ListAdapter extends RecyclerListView.SelectionAdapter {
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
                        headerCell.setText(LocaleController.getString("General", R.string.General));
                    } else if (position == networkHeaderRow) {
                        headerCell.setText(LocaleController.getString("EP_Network", R.string.EP_Network));
                    }
                    break;
                case 3:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    textCheckCell.setEnabled(true, null);
                    if (position == actionbarCrossfadeRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("EP_NavigationAnimationCrossfading", R.string.EP_NavigationAnimationCrossfading), CherrygramConfig.INSTANCE.getActionbarCrossfade(), true);
                    } else if (position == residentNotificationRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("CG_ResidentNotification", R.string.CG_ResidentNotification), CherrygramConfig.INSTANCE.getResidentNotification(), true);
                    } else if (position == showRPCErrorRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("EP_ShowRPCError", R.string.EP_ShowRPCError), CherrygramConfig.INSTANCE.getShowRPCError(), true);
                    } else if (position == customChatRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("EP_CustomChat", R.string.EP_CustomChat), LocaleController.getString("EP_CustomChat_Desc", R.string.EP_CustomChat_Desc), CherrygramConfig.INSTANCE.getCustomChatForSavedMessages(), true, true);

                    } else if (position == uploadSpeedBoostRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("EP_UploadloadSpeedBoost", R.string.EP_UploadloadSpeedBoost), CherrygramConfig.INSTANCE.getUploadSpeedBoost(), true);
                    } else if (position == slowNetworkMode) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("EP_SlowNetworkMode", R.string.EP_SlowNetworkMode), CherrygramConfig.INSTANCE.getSlowNetworkMode(), true);
                    }
                    break;
                case 4:
                    TextSettingsCell textCell = (TextSettingsCell) holder.itemView;
                    textCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    if (position == springAnimationRow) {
                        String value;
                        switch (CherrygramConfig.INSTANCE.getSpringAnimation()) {
                            case CherrygramConfig.ANIMATION_CLASSIC:
                                value = LocaleController.getString("EP_NavigationAnimationBezier", R.string.EP_NavigationAnimationBezier);
                                break;
                            default:
                            case CherrygramConfig.ANIMATION_SPRING:
                                value = LocaleController.getString("EP_NavigationAnimationSpring", R.string.EP_NavigationAnimationSpring);
                                break;
                        }
                        textCell.setTextAndValue(LocaleController.getString("EP_NavigationAnimation", R.string.EP_NavigationAnimation), value, true);
                    } else if (position == customChatIdRow) {
                        String t = "ID:";
                        SharedPreferences preferences = MessagesController.getMainSettings(currentAccount);
                        String v = preferences.getString("CP_CustomChatIDSM",
                                String.valueOf(getUserConfig().getClientUserId())
                        );
                        textCell.setTextAndValue(t, v, false);
                    } else if (position == downloadSpeedBoostRow) {
                        textCell.setTextAndValue(LocaleController.getString("EP_DownloadSpeedBoost", R.string.EP_DownloadSpeedBoost), CGFeatureHooks.getDownloadSpeedBoostText(), true);
                    }
                    break;
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == 3 || type == 7 || type == 8;
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
                default:
                    view = new ShadowSectionCell(mContext);
                    break;
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == experimentalSettingsDivisor) {
                return 1;
            } else if (position == experimentalHeaderRow || position == networkHeaderRow) {
                return 2;
            } else if (position == actionbarCrossfadeRow || position == residentNotificationRow || position == showRPCErrorRow || position == customChatRow || position == uploadSpeedBoostRow || position == slowNetworkMode) {
                return 3;
            } else if (position == springAnimationRow || position == customChatIdRow || position == downloadSpeedBoostRow) {
                return 4;
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
