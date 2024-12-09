package uz.unnarsx.cherrygram.preferences;

import static org.telegram.messenger.LocaleController.getString;

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

import java.util.ArrayList;

import uz.unnarsx.cherrygram.core.configs.CherrygramExperimentalConfig;
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper;
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper;
import uz.unnarsx.cherrygram.core.helpers.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.helpers.ui.PopupHelper;
import uz.unnarsx.cherrygram.preferences.helpers.TextFieldAlert;

public class ExperimentalPreferencesEntry extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private int rowCount;
    private ListAdapter listAdapter;
    private RecyclerListView listView;

    private int experimentalHeaderRow;
    private int springAnimationRow;
    private int actionbarCrossfadeRow;
    private int residentNotificationRow;
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
                CherrygramExperimentalConfig.INSTANCE.toggleActionbarCrossfade();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramExperimentalConfig.INSTANCE.getActionbarCrossfade());
                }
                AppRestartHelper.createRestartBulletin(this);
            } else if (position == residentNotificationRow) {
                CherrygramExperimentalConfig.INSTANCE.toggleResidentNotification();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramExperimentalConfig.INSTANCE.getResidentNotification());
                }
                AppRestartHelper.createRestartBulletin(this);
            } else if (position == customChatRow) {
                CherrygramExperimentalConfig.INSTANCE.toggleCustomChatForSavedMessages();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramExperimentalConfig.INSTANCE.getCustomChatForSavedMessages());
                }
                updateRowsId(false);
            } else if (position == customChatIdRow) {
                String currentValue = MessagesController.getMainSettings(currentAccount).getString("CP_CustomChatIDSM",
                        String.valueOf(getUserConfig().getClientUserId())
                );
                TextFieldAlert.createFieldAlert(
                        context,
                        getString(R.string.EP_CustomChat),
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
                CherrygramExperimentalConfig.INSTANCE.toggleUploadSpeedBoost();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramExperimentalConfig.INSTANCE.getUploadSpeedBoost());
                }
                AppRestartHelper.createRestartBulletin(this);
            } else if (position == slowNetworkMode) {
                CherrygramExperimentalConfig.INSTANCE.toggleSlowNetworkMode();
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
        int prevCustomChatIdRow = customChatIdRow;
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
        customChatIdRow = -1;
        if (CherrygramExperimentalConfig.INSTANCE.getCustomChatForSavedMessages()) customChatIdRow = rowCount++;
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
                    } else if (position == customChatIdRow) {
                        String t = "ID:";
                        SharedPreferences preferences = MessagesController.getMainSettings(currentAccount);
                        String v = preferences.getString("CP_CustomChatIDSM",
                                String.valueOf(getUserConfig().getClientUserId())
                        );
                        textCell.setTextAndValue(t, v, false);
                    } else if (position == downloadSpeedBoostRow) {
                        textCell.setTextAndValue(getString(R.string.EP_DownloadSpeedBoost), CGResourcesHelper.getDownloadSpeedBoostText(), true);
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
            } else if (position == actionbarCrossfadeRow || position == residentNotificationRow || position == customChatRow || position == uploadSpeedBoostRow || position == slowNetworkMode) {
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
