package uz.unnarsx.cherrygram.ui.drawer;

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
import org.telegram.messenger.LocaleController;
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

import uz.unnarsx.cherrygram.CherrygramConfig;
import uz.unnarsx.cherrygram.ui.dialogs.AlertDialogSwitchers;

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

        actionBar.setTitle(LocaleController.getString("AP_DrawerCategory", R.string.AP_DrawerCategory));
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
        if(listView.getItemAnimator() != null){
            ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
        }
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setOnItemClickListener((view, position, x, y) -> {
            if (position == drawerSnowRow) {
                CherrygramConfig.INSTANCE.toggleDrawerSnow();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramConfig.INSTANCE.getDrawSnowInDrawer());
                }
                parentLayout.rebuildAllFragmentViews(true, true);
            } else if (position == drawerAvatarAsBackgroundRow) {
                CherrygramConfig.INSTANCE.toggleDrawerAvatar();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramConfig.INSTANCE.getDrawerAvatar());
                }
                getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                TransitionManager.beginDelayedTransition(profilePreviewCell);
                listAdapter.notifyItemChanged(drawerRow, new Object());
                if (CherrygramConfig.INSTANCE.getDrawerAvatar()) {
                    updateRowsId(false);
                    listAdapter.notifyItemRangeInserted(showGradientRow, 4 + (CherrygramConfig.INSTANCE.getDrawerBlur() ? 3:0));
                } else {
                    listAdapter.notifyItemRangeRemoved(showGradientRow, 4 + (CherrygramConfig.INSTANCE.getDrawerBlur() ? 3:0));
                    updateRowsId(false);
                }
            } else if (position == showAvatarRow) {
                CherrygramConfig.INSTANCE.toggleDrawerSmallAvatar();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramConfig.INSTANCE.getDrawerSmallAvatar());
                }
                getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                listAdapter.notifyItemChanged(drawerRow, new Object());
            } else if (position == drawerDarkenBackgroundRow) {
                CherrygramConfig.INSTANCE.toggleDrawerDarken();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramConfig.INSTANCE.getDrawerDarken());
                }
                getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                listAdapter.notifyItemChanged(drawerRow, new Object());
            } else if (position == showGradientRow) {
                CherrygramConfig.INSTANCE.toggleDrawerGradient();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramConfig.INSTANCE.getDrawerGradient());
                }
                getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                listAdapter.notifyItemChanged(drawerRow, new Object());
            } else if (position == drawerBlurBackgroundRow) {
                CherrygramConfig.INSTANCE.toggleDrawerBlur();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramConfig.INSTANCE.getDrawerBlur());
                }
                getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                listAdapter.notifyItemChanged(drawerRow, new Object());
                if(CherrygramConfig.INSTANCE.getDrawerBlur()) {
                    listAdapter.notifyItemRangeInserted(drawerDividerRow, 3);
                } else {
                    listAdapter.notifyItemRangeRemoved(drawerDividerRow, 3);
                }
                updateRowsId(false);
            } else if (position == menuItemsRow) {
                AlertDialogSwitchers.showDrawerIconsAlert(this);
            }
        });
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
        if (CherrygramConfig.INSTANCE.getDrawerAvatar()) {
            showAvatarRow = rowCount++;
            drawerDarkenBackgroundRow = rowCount++;
            showGradientRow = rowCount++;
            drawerBlurBackgroundRow = rowCount++;
        }
        drawerDividerRow = rowCount++;
        if (CherrygramConfig.INSTANCE.getDrawerBlur() && CherrygramConfig.INSTANCE.getDrawerAvatar()) {
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
                        headerCell.setText(LocaleController.getString("AP_DrawerBlurIntensity", R.string.AP_DrawerBlurIntensity));
                    } else if (position == themeDrawerHeader) {
                        headerCell.setText(LocaleController.getString("AP_DrawerIconPack_Header", R.string.AP_DrawerIconPack_Header));
                    }
                    break;
                case 3:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    if (position == drawerSnowRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("CP_Snowflakes_Header", R.string.CP_Snowflakes_Header), CherrygramConfig.INSTANCE.getDrawSnowInDrawer(), !CherrygramConfig.INSTANCE.getDrawerBlur());
                    } else if (position == drawerAvatarAsBackgroundRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("AP_DrawerAvatar", R.string.AP_DrawerAvatar), CherrygramConfig.INSTANCE.getDrawerAvatar(), CherrygramConfig.INSTANCE.getDrawerAvatar());
                    } else if (position == showAvatarRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("AP_DrawerShowAvatar", R.string.AP_DrawerShowAvatar), CherrygramConfig.INSTANCE.getDrawerSmallAvatar(), drawerBlurBackgroundRow != -1);
                    } else if (position == drawerDarkenBackgroundRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("AP_DrawerDarken", R.string.AP_DrawerDarken), CherrygramConfig.INSTANCE.getDrawerDarken(), true);
                    } else if (position == showGradientRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("AP_ShadeBackground", R.string.AP_ShadeBackground), CherrygramConfig.INSTANCE.getDrawerGradient(), true);
                    } else if (position == drawerBlurBackgroundRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("AP_DrawerBlur", R.string.AP_DrawerBlur), CherrygramConfig.INSTANCE.getDrawerBlur(), !CherrygramConfig.INSTANCE.getDrawerBlur());
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
                        textCell.setTextAndIcon(LocaleController.getString("AP_DrawerButtonsCategory", R.string.AP_DrawerButtonsCategory), R.drawable.msg_newfilter, false);
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
                            CherrygramConfig.INSTANCE.setDrawerBlurIntensity(percentage);
                            RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(editBlurRow);
                            if (holder != null && holder.itemView instanceof BlurIntensityCell) {
                                BlurIntensityCell cell = (BlurIntensityCell) holder.itemView;
                                if (layout) {
                                    cell.requestLayout();
                                } else {
                                    cell.invalidate();
                                }
                            }
                            getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
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
                    view = new ThemeSelectorDrawerCell(mContext, CherrygramConfig.INSTANCE.getEventType()) {
                        @Override
                        protected void onSelectedEvent(int eventSelected) {
                            super.onSelectedEvent(eventSelected);
                            CherrygramConfig.INSTANCE.setEventType(eventSelected);
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

}
