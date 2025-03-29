/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.preferences.drawer;

import static org.telegram.messenger.LocaleController.getString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SnowflakesEffect;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig;
import uz.unnarsx.cherrygram.core.helpers.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.helpers.ui.PopupHelper;
import uz.unnarsx.cherrygram.preferences.drawer.cells.BlurIntensityCell;
import uz.unnarsx.cherrygram.preferences.drawer.cells.DrawerProfilePreviewCell;
import uz.unnarsx.cherrygram.preferences.drawer.cells.ThemeSelectorDrawerCell;

public class DrawerPreferencesEntry extends BaseFragment {
    private int rowCount;
    private ListAdapter listAdapter;
    private RecyclerListView listView;
    private DrawerProfilePreviewCell profilePreviewCell;

    private int drawerRow;
    private int drawerSnowRow;
    private int drawerAvatarAsBackgroundRow;
    private int showAvatarRow;
    private int drawerDarkenBackgroundRow;
    private int showGradientRow;
    private int drawerBlurBackgroundRow;
    private int drawerDividerRow;
    private int editBlurHeaderRow;
    private int editBlurRow;
    private int editBlurDividerRow;
    private int menuItemsRow;
    private int menuItemsDividerRow;
    private int themeDrawerHeader;
    private int themeDrawerRow;
    private int themeDrawerDividerRow;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        updateRowsId(true);
        return true;
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

        actionBar.setTitle(getString(R.string.AP_DrawerCategory));
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
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(listAdapter);
        if (listView.getItemAnimator() != null) {
            ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
        }
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setOnItemClickListener((view, position, x, y) -> {
            if (position == drawerSnowRow) {
                CherrygramAppearanceConfig.INSTANCE.setDrawSnowInDrawer(!CherrygramAppearanceConfig.INSTANCE.getDrawSnowInDrawer());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getDrawSnowInDrawer());
                }

                listAdapter.notifyItemChanged(drawerRow, new Object());

                if (CherrygramAppearanceConfig.INSTANCE.getDrawSnowInDrawer()) {
                    profilePreviewCell.snowflakesEffect = new SnowflakesEffect(0);
                    profilePreviewCell.snowflakesEffect.setColorKey(Theme.key_chats_menuName);
                    profilePreviewCell.snowflakesEffect.onDraw(profilePreviewCell, null);
                } else {
                    profilePreviewCell.snowflakesEffect = null;
                }
            } else if (position == drawerAvatarAsBackgroundRow) {
                CherrygramAppearanceConfig.INSTANCE.setDrawerAvatar(!CherrygramAppearanceConfig.INSTANCE.getDrawerAvatar());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getDrawerAvatar());
                }

                TransitionManager.beginDelayedTransition(profilePreviewCell);
                listAdapter.notifyItemChanged(drawerRow, new Object());

                if (CherrygramAppearanceConfig.INSTANCE.getDrawerAvatar()) {
                    updateRowsId(false);
                    listAdapter.notifyItemRangeInserted(showGradientRow, 4 + (CherrygramAppearanceConfig.INSTANCE.getDrawerBlur() ? 3:0));
                } else {
                    listAdapter.notifyItemRangeRemoved(showGradientRow, 4 + (CherrygramAppearanceConfig.INSTANCE.getDrawerBlur() ? 3:0));
                    updateRowsId(false);
                }
            } else if (position == showAvatarRow) {
                CherrygramAppearanceConfig.INSTANCE.setDrawerSmallAvatar(!CherrygramAppearanceConfig.INSTANCE.getDrawerSmallAvatar());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getDrawerSmallAvatar());
                }

                TransitionManager.beginDelayedTransition(profilePreviewCell);
                listAdapter.notifyItemChanged(drawerRow, new Object());
            } else if (position == drawerDarkenBackgroundRow) {
                CherrygramAppearanceConfig.INSTANCE.setDrawerDarken(!CherrygramAppearanceConfig.INSTANCE.getDrawerDarken());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getDrawerDarken());
                }

                TransitionManager.beginDelayedTransition(profilePreviewCell);
                listAdapter.notifyItemChanged(drawerRow, new Object());
            } else if (position == showGradientRow) {
                CherrygramAppearanceConfig.INSTANCE.setDrawerGradient(!CherrygramAppearanceConfig.INSTANCE.getDrawerGradient());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getDrawerGradient());
                }

                TransitionManager.beginDelayedTransition(profilePreviewCell);
                listAdapter.notifyItemChanged(drawerRow, new Object());
            } else if (position == drawerBlurBackgroundRow) {
                CherrygramAppearanceConfig.INSTANCE.setDrawerBlur(!CherrygramAppearanceConfig.INSTANCE.getDrawerBlur());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getDrawerBlur());
                }

                TransitionManager.beginDelayedTransition(profilePreviewCell);
                listAdapter.notifyItemChanged(drawerRow, new Object());

                if (CherrygramAppearanceConfig.INSTANCE.getDrawerBlur()) {
                    listAdapter.notifyItemRangeInserted(drawerDividerRow, 3);
                } else {
                    listAdapter.notifyItemRangeRemoved(drawerDividerRow, 3);
                }
                updateRowsId(false);
            } else if (position == menuItemsRow) {
                showDrawerItemsSelector();
            }
        });

        FirebaseAnalyticsHelper.trackEventWithEmptyBundle("drawer_preferences_screen");

        return fragmentView;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateRowsId(boolean notify) {
        rowCount = 0;
        showAvatarRow = -1;
        drawerDarkenBackgroundRow = -1;
        showGradientRow = -1;
        drawerBlurBackgroundRow = -1;
        editBlurHeaderRow = -1;
        editBlurRow = -1;
        editBlurDividerRow = -1;

        drawerRow = rowCount++;
        drawerSnowRow = rowCount++;
        drawerAvatarAsBackgroundRow = rowCount++;
        if (CherrygramAppearanceConfig.INSTANCE.getDrawerAvatar()) {
            showAvatarRow = rowCount++;
            drawerDarkenBackgroundRow = rowCount++;
            showGradientRow = rowCount++;
            drawerBlurBackgroundRow = rowCount++;
        }
        drawerDividerRow = rowCount++;
        if (CherrygramAppearanceConfig.INSTANCE.getDrawerBlur() && CherrygramAppearanceConfig.INSTANCE.getDrawerAvatar()) {
            editBlurHeaderRow = rowCount++;
            editBlurRow = rowCount++;
            editBlurDividerRow = rowCount++;
        }

        menuItemsRow = rowCount++;
        menuItemsDividerRow = rowCount++;

        themeDrawerHeader = rowCount++;
        themeDrawerRow = rowCount++;
        themeDrawerDividerRow = rowCount++;

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
                    if (position == editBlurHeaderRow) {
                        headerCell.setText(getString(R.string.AP_DrawerBlurIntensity));
                    } else if (position == themeDrawerHeader) {
                        headerCell.setText(getString(R.string.AP_DrawerIconPack_Header));
                    }
                    break;
                case 3:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    if (position == drawerSnowRow) {
                        textCheckCell.setTextAndCheck(getString(R.string.CP_Snowflakes_Header), CherrygramAppearanceConfig.INSTANCE.getDrawSnowInDrawer(), true);
                    } else if (position == drawerAvatarAsBackgroundRow) {
                        textCheckCell.setTextAndCheck(getString(R.string.AP_DrawerAvatar), CherrygramAppearanceConfig.INSTANCE.getDrawerAvatar(), CherrygramAppearanceConfig.INSTANCE.getDrawerAvatar());
                    } else if (position == showAvatarRow) {
                        textCheckCell.setTextAndCheck(getString(R.string.AP_DrawerShowAvatar), CherrygramAppearanceConfig.INSTANCE.getDrawerSmallAvatar(), drawerBlurBackgroundRow != -1);
                    } else if (position == drawerDarkenBackgroundRow) {
                        textCheckCell.setTextAndCheck(getString(R.string.AP_DrawerDarken), CherrygramAppearanceConfig.INSTANCE.getDrawerDarken(), true);
                    } else if (position == showGradientRow) {
                        textCheckCell.setTextAndCheck(getString(R.string.AP_ShadeBackground), CherrygramAppearanceConfig.INSTANCE.getDrawerGradient(), true);
                    } else if (position == drawerBlurBackgroundRow) {
                        textCheckCell.setTextAndCheck(getString(R.string.AP_DrawerBlur), CherrygramAppearanceConfig.INSTANCE.getDrawerBlur(), !CherrygramAppearanceConfig.INSTANCE.getDrawerBlur());
                    }
                    break;
                case 4:
                    DrawerProfilePreviewCell cell = (DrawerProfilePreviewCell) holder.itemView;
                    cell.setUser(getUserConfig().getCurrentUser(), false);
                    break;
                case 6:
                    TextCell textCell = (TextCell) holder.itemView;
                    textCell.setColors(Theme.key_windowBackgroundWhiteBlueText4, Theme.key_windowBackgroundWhiteBlueText4);
                    if (position == menuItemsRow) {
                        textCell.setTextAndIcon(getString(R.string.AP_DrawerButtonsCategory), R.drawable.msg_newfilter, false);
                    }
                    break;
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == 3 || type == 6;
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
                    view = profilePreviewCell = new DrawerProfilePreviewCell(mContext);
                    view.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case 5:
                    view = new BlurIntensityCell(mContext) {
                        @Override
                        protected void onBlurIntensityChange(int percentage, boolean layout) {
                            super.onBlurIntensityChange(percentage, layout);
                            CherrygramAppearanceConfig.INSTANCE.setDrawerBlurIntensity(percentage);
                            RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(editBlurRow);
                            if (holder != null && holder.itemView instanceof BlurIntensityCell cell) {
                                if (layout) {
                                    cell.requestLayout();
                                } else {
                                    cell.invalidate();
                                }
                            }

                            listAdapter.notifyItemChanged(drawerRow, new Object());
                        }
                    };
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 6:
                    view = new TextCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 7:
                    view = new ThemeSelectorDrawerCell(mContext, CherrygramAppearanceConfig.INSTANCE.getEventType()) {
                        @Override
                        protected void onSelectedEvent(int eventSelected) {
                            super.onSelectedEvent(eventSelected);
                            CherrygramAppearanceConfig.INSTANCE.setEventType(eventSelected);
                            listAdapter.notifyItemChanged(drawerRow, new Object());
                            Theme.lastHolidayCheckTime = 0;
                            Theme.dialogs_holidayDrawable = null;
                            getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                            updateRowsId(false);
                        }
                    };
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
            if (position == drawerDividerRow || position == editBlurDividerRow || position== menuItemsDividerRow ||position == themeDrawerDividerRow){
                return 1;
            } else if (position == editBlurHeaderRow || position == themeDrawerHeader) {
                return 2;
            } else if (position == drawerSnowRow || position == drawerAvatarAsBackgroundRow || position == showAvatarRow || position == drawerDarkenBackgroundRow || position == showGradientRow || position == drawerBlurBackgroundRow) {
                return 3;
            } else if (position == drawerRow) {
                return 4;
            } else if (position == editBlurRow) {
                return 5;
            } else if (position == menuItemsRow) {
                return 6;
            } else if (position == themeDrawerRow) {
                return 7;
            }
            return 1;
        }
    }

    private void showDrawerItemsSelector() {
        ArrayList<String> prefTitle = new ArrayList<>();
        ArrayList<Integer> prefIcon = new ArrayList<>();
        ArrayList<Boolean> prefCheck = new ArrayList<>();
        ArrayList<Boolean> prefDivider = new ArrayList<>();
        ArrayList<Runnable> clickListener = new ArrayList<>();

        prefTitle.add(getUserConfig().getEmojiStatus() != null ? getString(R.string.ChangeEmojiStatus) : getString(R.string.SetEmojiStatus));
        prefIcon.add(getUserConfig().getEmojiStatus() != null ? R.drawable.msg_status_edit : R.drawable.msg_status_set);
        prefCheck.add(CherrygramAppearanceConfig.INSTANCE.getChangeStatusDrawerButton());
        prefDivider.add(false);
        clickListener.add(() -> CherrygramAppearanceConfig.INSTANCE.setChangeStatusDrawerButton(!CherrygramAppearanceConfig.INSTANCE.getChangeStatusDrawerButton()));

        prefTitle.add(getString(R.string.MyProfile));
        prefIcon.add(R.drawable.left_status_profile);
        prefCheck.add(CherrygramAppearanceConfig.INSTANCE.getMyProfileDrawerButton());
        prefDivider.add(true);
        clickListener.add(() -> CherrygramAppearanceConfig.INSTANCE.setMyProfileDrawerButton(!CherrygramAppearanceConfig.INSTANCE.getMyProfileDrawerButton()));

        prefTitle.add(getString(R.string.NewGroup));
        prefIcon.add(R.drawable.msg_groups);
        prefCheck.add(CherrygramAppearanceConfig.INSTANCE.getCreateGroupDrawerButton());
        prefDivider.add(false);
        clickListener.add(() -> CherrygramAppearanceConfig.INSTANCE.setCreateGroupDrawerButton(!CherrygramAppearanceConfig.INSTANCE.getCreateGroupDrawerButton()));

        prefTitle.add(getString(R.string.NewChannel));
        prefIcon.add(R.drawable.msg_channel);
        prefCheck.add(CherrygramAppearanceConfig.INSTANCE.getCreateChannelDrawerButton());
        prefDivider.add(false);
        clickListener.add(() -> CherrygramAppearanceConfig.INSTANCE.setCreateChannelDrawerButton(!CherrygramAppearanceConfig.INSTANCE.getCreateChannelDrawerButton()));

        prefTitle.add(getString(R.string.Contacts));
        prefIcon.add(R.drawable.msg_contacts);
        prefCheck.add(CherrygramAppearanceConfig.INSTANCE.getContactsDrawerButton());
        prefDivider.add(false);
        clickListener.add(() -> CherrygramAppearanceConfig.INSTANCE.setContactsDrawerButton(!CherrygramAppearanceConfig.INSTANCE.getContactsDrawerButton()));

        prefTitle.add(getString(R.string.Calls));
        prefIcon.add(R.drawable.msg_calls);
        prefCheck.add(CherrygramAppearanceConfig.INSTANCE.getCallsDrawerButton());
        prefDivider.add(false);
        clickListener.add(() -> CherrygramAppearanceConfig.INSTANCE.setCallsDrawerButton(!CherrygramAppearanceConfig.INSTANCE.getCallsDrawerButton()));

        prefTitle.add(getString(R.string.SavedMessages));
        prefIcon.add(R.drawable.msg_saved);
        prefCheck.add(CherrygramAppearanceConfig.INSTANCE.getSavedMessagesDrawerButton());
        prefDivider.add(false);
        clickListener.add(() -> CherrygramAppearanceConfig.INSTANCE.setSavedMessagesDrawerButton(!CherrygramAppearanceConfig.INSTANCE.getSavedMessagesDrawerButton()));

        prefTitle.add(getString(R.string.ArchivedChats));
        prefIcon.add(R.drawable.msg_archive);
        prefCheck.add(CherrygramAppearanceConfig.INSTANCE.getArchivedChatsDrawerButton());
        prefDivider.add(false);
        clickListener.add(() -> CherrygramAppearanceConfig.INSTANCE.setArchivedChatsDrawerButton(!CherrygramAppearanceConfig.INSTANCE.getArchivedChatsDrawerButton()));

        prefTitle.add(getString(R.string.AuthAnotherClient));
        prefIcon.add(R.drawable.msg_qrcode);
        prefCheck.add(CherrygramAppearanceConfig.INSTANCE.getScanQRDrawerButton());
        prefDivider.add(true);
        clickListener.add(() -> CherrygramAppearanceConfig.INSTANCE.setScanQRDrawerButton(!CherrygramAppearanceConfig.INSTANCE.getScanQRDrawerButton()));

        prefTitle.add(getString(R.string.CGP_AdvancedSettings));
        prefIcon.add(R.drawable.msg_settings);
        prefCheck.add(CherrygramAppearanceConfig.INSTANCE.getCGPreferencesDrawerButton());
        prefDivider.add(false);
        clickListener.add(() -> CherrygramAppearanceConfig.INSTANCE.setCGPreferencesDrawerButton(!CherrygramAppearanceConfig.INSTANCE.getCGPreferencesDrawerButton()));

        PopupHelper.showSwitchAlert(
                getString(R.string.AP_DrawerButtonsCategory),
                this,
                prefTitle,
                prefIcon,
                prefCheck,
                null,
                prefDivider,
                clickListener,
                () -> getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged)
        );
    }

}
