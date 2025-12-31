/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.preferences.drawer;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
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
import uz.unnarsx.cherrygram.core.ui.MD3ListAdapter;
import uz.unnarsx.cherrygram.helpers.ui.PopupHelper;
import uz.unnarsx.cherrygram.preferences.drawer.cells.BlurIntensityCell;
import uz.unnarsx.cherrygram.preferences.drawer.cells.DrawerProfilePreviewCell;
import uz.unnarsx.cherrygram.preferences.drawer.cells.ThemeSelectorDrawerCell;

public class DrawerPreferencesEntry extends BaseFragment {

    private int rowCount;
    private ListAdapter listAdapter;
    private RecyclerListView listView;
    private DrawerProfilePreviewCell profilePreviewCell;

    private int drawerProfilePreviewRow;
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

                listAdapter.notifyItemChanged(drawerProfilePreviewRow, new Object());

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
                listAdapter.notifyItemChanged(drawerProfilePreviewRow, new Object());

                if (CherrygramAppearanceConfig.INSTANCE.getDrawerAvatar()) {
                    updateRowsId(false);
                    listAdapter.notifyItemRangeInserted(showGradientRow, 4 + (CherrygramAppearanceConfig.INSTANCE.getDrawerBlur() ? 3:0));
                } else {
                    listAdapter.notifyItemRangeRemoved(showGradientRow, 4 + (CherrygramAppearanceConfig.INSTANCE.getDrawerBlur() ? 3:0));
                    updateRowsId(false);
                }
                getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
            } else if (position == showAvatarRow) {
                CherrygramAppearanceConfig.INSTANCE.setDrawerSmallAvatar(!CherrygramAppearanceConfig.INSTANCE.getDrawerSmallAvatar());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getDrawerSmallAvatar());
                }

                TransitionManager.beginDelayedTransition(profilePreviewCell);
                listAdapter.notifyItemChanged(drawerProfilePreviewRow, new Object());
                getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
            } else if (position == drawerDarkenBackgroundRow) {
                CherrygramAppearanceConfig.INSTANCE.setDrawerDarken(!CherrygramAppearanceConfig.INSTANCE.getDrawerDarken());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getDrawerDarken());
                }

                TransitionManager.beginDelayedTransition(profilePreviewCell);
                listAdapter.notifyItemChanged(drawerProfilePreviewRow, new Object());
                getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
            } else if (position == showGradientRow) {
                CherrygramAppearanceConfig.INSTANCE.setDrawerGradient(!CherrygramAppearanceConfig.INSTANCE.getDrawerGradient());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getDrawerGradient());
                }

                TransitionManager.beginDelayedTransition(profilePreviewCell);
                listAdapter.notifyItemChanged(drawerProfilePreviewRow, new Object());
            } else if (position == drawerBlurBackgroundRow) {
                CherrygramAppearanceConfig.INSTANCE.setDrawerBlur(!CherrygramAppearanceConfig.INSTANCE.getDrawerBlur());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getDrawerBlur());
                }

                TransitionManager.beginDelayedTransition(profilePreviewCell);
                listAdapter.notifyItemChanged(drawerProfilePreviewRow, new Object());

                if (CherrygramAppearanceConfig.INSTANCE.getDrawerBlur()) {
                    listAdapter.notifyItemRangeInserted(drawerDividerRow, 3);
                } else {
                    listAdapter.notifyItemRangeRemoved(drawerDividerRow, 3);
                }
                updateRowsId(false);
                getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
            } else if (position == menuItemsRow) {
                showDrawerItemsSelector(this);
            }
        });

        FirebaseAnalyticsHelper.trackEventWithEmptyBundle("drawer_preferences_screen");

        return fragmentView;
    }

    private class ListAdapter extends MD3ListAdapter {

        private final Context mContext;

        private final int VIEW_TYPE_SHADOW = 0;
        private final int VIEW_TYPE_HEADER = 1;
        private final int VIEW_TYPE_TEXT_CELL = 2;
        private final int VIEW_TYPE_TEXT_CHECK = 3;
//        private final int VIEW_TYPE_TEXT_SETTINGS = 4;
//        private final int VIEW_TYPE_TEXT_INFO_PRIVACY = 5;
//        private final int VIEW_TYPE_TEXT_DETAIL_SETTINGS = 6;
        private final int VIEW_TYPE_PROFILE_PREVIEW = 7;
        private final int VIEW_TYPE_SLIDER = 8;
        private final int VIEW_TYPE_THEMES_SELECTOR = 9;

        ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case VIEW_TYPE_SHADOW:
                    holder.itemView.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case VIEW_TYPE_HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == editBlurHeaderRow) {
                        headerCell.setText(getString(R.string.AP_DrawerBlurIntensity));
                    } else if (position == themeDrawerHeader) {
                        headerCell.setText(getString(R.string.AP_DrawerIconPack_Header));
                    }
                    break;
                case VIEW_TYPE_TEXT_CELL:
                    TextCell textCell = (TextCell) holder.itemView;
                    textCell.setColors(Theme.key_windowBackgroundWhiteBlueText4, Theme.key_windowBackgroundWhiteBlueText4);
                    if (position == menuItemsRow) {
                        textCell.setTextAndIcon(getString(R.string.AP_DrawerButtonsCategory), R.drawable.msg_list, false);
                    }
                    break;
                case VIEW_TYPE_TEXT_CHECK:
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
                case VIEW_TYPE_PROFILE_PREVIEW:
                    DrawerProfilePreviewCell cell = (DrawerProfilePreviewCell) holder.itemView;
                    if (position == drawerProfilePreviewRow) {
                        cell.setUser(getUserConfig().getCurrentUser(), false);
                    }
                    break;
                case VIEW_TYPE_SLIDER:
                    /*BlurIntensityCell blurIntensityCell = (BlurIntensityCell) holder.itemView;
                    if (position == editBlurRow) {

                    }*/
                    break;
                case VIEW_TYPE_THEMES_SELECTOR:
                    /*ThemeSelectorDrawerCell themeSelectorDrawerCell = (ThemeSelectorDrawerCell) holder.itemView;
                    if (position == themeDrawerRow) {

                    }*/
                    break;
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == VIEW_TYPE_TEXT_CHECK || type == VIEW_TYPE_TEXT_CELL;
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
                case VIEW_TYPE_TEXT_CELL:
                    view = new TextCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_TEXT_CHECK:
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_PROFILE_PREVIEW:
                    view = profilePreviewCell = new DrawerProfilePreviewCell(mContext);
                    view.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case VIEW_TYPE_SLIDER:
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

                            listAdapter.notifyItemChanged(drawerProfilePreviewRow, new Object());
                        }
                    };
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_THEMES_SELECTOR:
                    view = new ThemeSelectorDrawerCell(mContext, CherrygramAppearanceConfig.INSTANCE.getEventType()) {
                        @Override
                        protected void onSelectedEvent(int eventSelected) {
                            super.onSelectedEvent(eventSelected);
                            CherrygramAppearanceConfig.INSTANCE.setEventType(eventSelected);
                            listAdapter.notifyItemChanged(drawerProfilePreviewRow, new Object());
                            Theme.lastHolidayCheckTime = 0;
                            Theme.dialogs_holidayDrawable = null;
                            getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                            updateRowsId(false);
                        }
                    };
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
            if (position == drawerDividerRow || position == editBlurDividerRow || position== menuItemsDividerRow ||position == themeDrawerDividerRow){
                return VIEW_TYPE_SHADOW;
            } else if (position == editBlurHeaderRow || position == themeDrawerHeader) {
                return VIEW_TYPE_HEADER;
            } else if (position == menuItemsRow) {
                return VIEW_TYPE_TEXT_CELL;
            } else if (position == drawerSnowRow || position == drawerAvatarAsBackgroundRow || position == showAvatarRow || position == drawerDarkenBackgroundRow || position == showGradientRow || position == drawerBlurBackgroundRow) {
                return VIEW_TYPE_TEXT_CHECK;
            } else if (position == drawerProfilePreviewRow) {
                return VIEW_TYPE_PROFILE_PREVIEW;
            } else if (position == editBlurRow) {
                return VIEW_TYPE_SLIDER;
            } else if (position == themeDrawerRow) {
                return VIEW_TYPE_THEMES_SELECTOR;
            }
            return VIEW_TYPE_SHADOW;
        }
    }

    private void updateRowsId(boolean notify) {
        rowCount = 0;
        showAvatarRow = -1;
        drawerDarkenBackgroundRow = -1;
        showGradientRow = -1;
        drawerBlurBackgroundRow = -1;
        editBlurHeaderRow = -1;
        editBlurRow = -1;
        editBlurDividerRow = -1;

        drawerProfilePreviewRow = rowCount++;
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

    public static void showDrawerItemsSelector(BaseFragment fragment) {
        int eventType = Theme.getEventType();
        if (CherrygramAppearanceConfig.INSTANCE.getEventType() > 0) {
            eventType = CherrygramAppearanceConfig.INSTANCE.getEventType() - 1;
        }
        int newGroupIcon;
        int newChannelIcon;
        int contactsIcon;
        int callsIcon;
        int savedIcon;
        int settingsIcon;
        if (eventType == 0) {
            newGroupIcon = R.drawable.msg_groups_ny;
            newChannelIcon = R.drawable.msg_channel_ny;
            contactsIcon = R.drawable.msg_contacts_ny;
            callsIcon = R.drawable.msg_calls_ny;
            savedIcon = R.drawable.msg_saved_ny;
            settingsIcon = R.drawable.msg_settings_ny;
        } else if (eventType == 1) {
            newGroupIcon = R.drawable.msg_groups_14;
            newChannelIcon = R.drawable.msg_channel_14;
            contactsIcon = R.drawable.msg_contacts_14;
            callsIcon = R.drawable.msg_calls_14;
            savedIcon = R.drawable.msg_saved_14;
            settingsIcon = R.drawable.msg_settings_14;
        } else if (eventType == 2) {
            newGroupIcon = R.drawable.msg_groups_hw;
            newChannelIcon = R.drawable.msg_channel_hw;
            contactsIcon = R.drawable.msg_contacts_hw;
            callsIcon = R.drawable.msg_calls_hw;
            savedIcon = R.drawable.msg_saved_hw;
            settingsIcon = R.drawable.msg_settings_hw;
        } else if (eventType == 3) {
            newGroupIcon = R.drawable.menu_groups_cn;
            newChannelIcon = R.drawable.menu_broadcast_cn;
            contactsIcon = R.drawable.menu_contacts_cn;
            callsIcon = R.drawable.menu_calls_cn;
            savedIcon = R.drawable.menu_bookmarks_cn;
            settingsIcon = R.drawable.menu_settings_cn;
        } else {
            newGroupIcon = R.drawable.msg_groups;
            newChannelIcon = R.drawable.msg_channel;
            contactsIcon = R.drawable.msg_contacts;
            callsIcon = R.drawable.msg_calls;
            savedIcon = R.drawable.msg_saved;
            settingsIcon = R.drawable.msg_settings_old;
        }

        ArrayList<String> prefTitle = new ArrayList<>();
        ArrayList<Integer> prefIcon = new ArrayList<>();
        ArrayList<Boolean> prefCheck = new ArrayList<>();
        ArrayList<Boolean> prefDonate = new ArrayList<>();
        ArrayList<Boolean> prefDivider = new ArrayList<>();
        ArrayList<Runnable> clickListener = new ArrayList<>();

        prefTitle.add(fragment.getUserConfig().getEmojiStatus() != null ? getString(R.string.ChangeEmojiStatus) : getString(R.string.SetEmojiStatus));
        prefIcon.add(fragment.getUserConfig().getEmojiStatus() != null ? R.drawable.msg_status_edit : R.drawable.msg_status_set);
        prefCheck.add(CherrygramAppearanceConfig.INSTANCE.getChangeStatusDrawerButton());
        prefDonate.add(false);
        prefDivider.add(false);
        clickListener.add(() -> CherrygramAppearanceConfig.INSTANCE.setChangeStatusDrawerButton(!CherrygramAppearanceConfig.INSTANCE.getChangeStatusDrawerButton()));

        prefTitle.add(getString(R.string.Gift2TitleSelf1));
        prefIcon.add(R.drawable.menu_gift);
        prefCheck.add(CherrygramAppearanceConfig.INSTANCE.getMarketPlaceDrawerButton());
        prefDonate.add(true);
        prefDivider.add(false);
        clickListener.add(() -> CherrygramAppearanceConfig.INSTANCE.setMarketPlaceDrawerButton(!CherrygramAppearanceConfig.INSTANCE.getMarketPlaceDrawerButton()));

        prefTitle.add(getString(R.string.MyProfile));
        prefIcon.add(R.drawable.left_status_profile);
        prefCheck.add(CherrygramAppearanceConfig.INSTANCE.getMyProfileDrawerButton());
        prefDonate.add(false);
        prefDivider.add(true);
        clickListener.add(() -> CherrygramAppearanceConfig.INSTANCE.setMyProfileDrawerButton(!CherrygramAppearanceConfig.INSTANCE.getMyProfileDrawerButton()));

        prefTitle.add(getString(R.string.NewGroup));
        prefIcon.add(newGroupIcon);
        prefCheck.add(CherrygramAppearanceConfig.INSTANCE.getCreateGroupDrawerButton());
        prefDonate.add(false);
        prefDivider.add(false);
        clickListener.add(() -> CherrygramAppearanceConfig.INSTANCE.setCreateGroupDrawerButton(!CherrygramAppearanceConfig.INSTANCE.getCreateGroupDrawerButton()));

        prefTitle.add(getString(R.string.NewChannel));
        prefIcon.add(newChannelIcon);
        prefCheck.add(CherrygramAppearanceConfig.INSTANCE.getCreateChannelDrawerButton());
        prefDonate.add(false);
        prefDivider.add(false);
        clickListener.add(() -> CherrygramAppearanceConfig.INSTANCE.setCreateChannelDrawerButton(!CherrygramAppearanceConfig.INSTANCE.getCreateChannelDrawerButton()));

        prefTitle.add(getString(R.string.Contacts));
        prefIcon.add(contactsIcon);
        prefCheck.add(CherrygramAppearanceConfig.INSTANCE.getContactsDrawerButton());
        prefDonate.add(false);
        prefDivider.add(false);
        clickListener.add(() -> CherrygramAppearanceConfig.INSTANCE.setContactsDrawerButton(!CherrygramAppearanceConfig.INSTANCE.getContactsDrawerButton()));

        prefTitle.add(getString(R.string.Calls));
        prefIcon.add(callsIcon);
        prefCheck.add(CherrygramAppearanceConfig.INSTANCE.getCallsDrawerButton());
        prefDonate.add(false);
        prefDivider.add(false);
        clickListener.add(() -> CherrygramAppearanceConfig.INSTANCE.setCallsDrawerButton(!CherrygramAppearanceConfig.INSTANCE.getCallsDrawerButton()));

        prefTitle.add(getString(R.string.SavedMessages));
        prefIcon.add(savedIcon);
        prefCheck.add(CherrygramAppearanceConfig.INSTANCE.getSavedMessagesDrawerButton());
        prefDonate.add(false);
        prefDivider.add(false);
        clickListener.add(() -> CherrygramAppearanceConfig.INSTANCE.setSavedMessagesDrawerButton(!CherrygramAppearanceConfig.INSTANCE.getSavedMessagesDrawerButton()));

        prefTitle.add(getString(R.string.ArchivedChats));
        prefIcon.add(R.drawable.msg_archive);
        prefCheck.add(CherrygramAppearanceConfig.INSTANCE.getArchivedChatsDrawerButton());
        prefDonate.add(false);
        prefDivider.add(false);
        clickListener.add(() -> CherrygramAppearanceConfig.INSTANCE.setArchivedChatsDrawerButton(!CherrygramAppearanceConfig.INSTANCE.getArchivedChatsDrawerButton()));

        prefTitle.add(getString(R.string.AuthAnotherClient));
        prefIcon.add(R.drawable.msg_qrcode);
        prefCheck.add(CherrygramAppearanceConfig.INSTANCE.getScanQRDrawerButton());
        prefDonate.add(false);
        prefDivider.add(true);
        clickListener.add(() -> CherrygramAppearanceConfig.INSTANCE.setScanQRDrawerButton(!CherrygramAppearanceConfig.INSTANCE.getScanQRDrawerButton()));

        prefTitle.add(getString(R.string.CGP_AdvancedSettings));
        prefIcon.add(settingsIcon);
        prefCheck.add(CherrygramAppearanceConfig.INSTANCE.getCGPreferencesDrawerButton());
        prefDonate.add(false);
        prefDivider.add(false);
        clickListener.add(() -> CherrygramAppearanceConfig.INSTANCE.setCGPreferencesDrawerButton(!CherrygramAppearanceConfig.INSTANCE.getCGPreferencesDrawerButton()));

        PopupHelper.showSwitchAlert(
                getString(R.string.AP_DrawerButtonsCategory),
                fragment,
                prefTitle,
                prefIcon,
                prefCheck,
                null,
                prefDonate,
                prefDivider,
                clickListener,
                () -> fragment.getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged)
        );
    }

}
