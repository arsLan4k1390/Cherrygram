/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.preferences.folders;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig;
import uz.unnarsx.cherrygram.core.configs.CherrygramDebugConfig;
import uz.unnarsx.cherrygram.core.helpers.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.helpers.ui.PopupHelper;
import uz.unnarsx.cherrygram.preferences.folders.cells.FoldersPreviewCell;

public class FoldersPreferencesEntry extends BaseFragment {

    private int rowCount;
    private ListAdapter listAdapter;
    private RecyclerListView listView;

    private int foldersHeaderRow;

    private int foldersPreviewRow;
    private int folderNameAppHeaderRow;
    private int hideAllChatsTabRow;
    private int hideCounterRow;

    private int tabIconTypeRow;
    private int tabStyleRow;
    private int addStrokeRow;

    private int divisorRow;

    protected Theme.ResourcesProvider resourcesProvider;
    protected FoldersPreviewCell foldersPreviewCell;

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

        actionBar.setTitle(getString(R.string.CP_Filters_Header));
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
            if (position == folderNameAppHeaderRow) {
                CherrygramAppearanceConfig.INSTANCE.setFolderNameInHeader(!CherrygramAppearanceConfig.INSTANCE.getFolderNameInHeader());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getFolderNameInHeader());
                }
                parentLayout.rebuildAllFragmentViews(false, false);
                getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
            } else if (position == hideAllChatsTabRow) {
                CherrygramAppearanceConfig.INSTANCE.setTabsHideAllChats(!CherrygramAppearanceConfig.INSTANCE.getTabsHideAllChats());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getTabsHideAllChats());
                }
                foldersPreviewCell.updateAllChatsTabName(true);
                parentLayout.rebuildAllFragmentViews(false, false);
                getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
                getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
            } else if (position == hideCounterRow) {
                CherrygramAppearanceConfig.INSTANCE.setTabsNoUnread(!CherrygramAppearanceConfig.INSTANCE.getTabsNoUnread());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getTabsNoUnread());
                }
                foldersPreviewCell.updateTabCounter(true);
                parentLayout.rebuildAllFragmentViews(false, false);
                getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
            } else if (position == tabIconTypeRow) {
                ArrayList<String> arrayList = new ArrayList<>();
                ArrayList<Integer> types = new ArrayList<>();

                arrayList.add(getString(R.string.CG_FoldersTypeIconsTitles));
                types.add(CherrygramAppearanceConfig.TAB_TYPE_MIX);
                arrayList.add(getString(R.string.CG_FoldersTypeTitles));
                types.add(CherrygramAppearanceConfig.TAB_TYPE_TEXT);
                arrayList.add(getString(R.string.CG_FoldersTypeIcons));
                types.add(CherrygramAppearanceConfig.TAB_TYPE_ICON);

                PopupHelper.show(arrayList, getString(R.string.CG_FoldersType_Header), types.indexOf(CherrygramAppearanceConfig.INSTANCE.getTabMode()), context, i -> {
                    CherrygramAppearanceConfig.INSTANCE.setTabMode(types.get(i));

                    foldersPreviewCell.updateTabIcons(true);
                    foldersPreviewCell.updateTabTitle(true);
                    listAdapter.notifyItemChanged(tabIconTypeRow);
                    parentLayout.rebuildAllFragmentViews(false, false);
                    getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
                });
            } else if (position == tabStyleRow) {
                ArrayList<String> configStringKeys = new ArrayList<>();
                ArrayList<Integer> configValues = new ArrayList<>();

                configStringKeys.add(getString(R.string.AP_Tab_Style_Default));
                configValues.add(CherrygramAppearanceConfig.TAB_STYLE_DEFAULT);

                configStringKeys.add(getString(R.string.AP_Tab_Style_Rounded));
                configValues.add(CherrygramAppearanceConfig.TAB_STYLE_ROUNDED);

                configStringKeys.add(getString(R.string.AP_Tab_Style_Text));
                configValues.add(CherrygramAppearanceConfig.TAB_STYLE_TEXT);

                configStringKeys.add("VKUI");
                configValues.add(CherrygramAppearanceConfig.TAB_STYLE_VKUI);

                configStringKeys.add(getString(R.string.AP_Tab_Style_Pills));
                configValues.add(CherrygramAppearanceConfig.TAB_STYLE_PILLS);

                PopupHelper.show(configStringKeys, getString(R.string.AP_Tab_Style), configValues.indexOf(CherrygramAppearanceConfig.INSTANCE.getTabStyle()), context, i -> {
                    CherrygramAppearanceConfig.INSTANCE.setTabStyle(configValues.get(i));

                    foldersPreviewCell.updateTabStyle(true);
                    listAdapter.notifyItemChanged(tabStyleRow);
                    parentLayout.rebuildAllFragmentViews(false, false);
                    getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
                    updateRowsId(false);
                });
            } else if (position == addStrokeRow) {
                CherrygramAppearanceConfig.INSTANCE.setTabStyleStroke(!CherrygramAppearanceConfig.INSTANCE.getTabStyleStroke());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getTabStyleStroke());
                }
                foldersPreviewCell.updateTabStroke(true);
                parentLayout.rebuildAllFragmentViews(false, false);
            }
        });

        FirebaseAnalyticsHelper.trackEventWithEmptyBundle("folders_preferences_screen");

        return fragmentView;
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private final Context mContext;

        private final int VIEW_TYPE_SHADOW = 0;
        private final int VIEW_TYPE_HEADER = 1;
//        private final int VIEW_TYPE_TEXT_CELL = 2;
        private final int VIEW_TYPE_TEXT_CHECK = 3;
        private final int VIEW_TYPE_TEXT_SETTINGS = 4;
//        private final int VIEW_TYPE_TEXT_INFO_PRIVACY = 5;
//        private final int VIEW_TYPE_TEXT_DETAIL_SETTINGS = 6;
        private final int VIEW_TYPE_PREVIEW = 7;

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
                    applyMD3Background(holder, position);
                    break;
                case VIEW_TYPE_HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == foldersHeaderRow) {
                        headerCell.setText(getString(R.string.ProfileBotPreviewTab));
                    }
                    applyMD3Background(holder, position);
                    break;
                case VIEW_TYPE_TEXT_CHECK:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    textCheckCell.setEnabled(true, null);
                    if (position == folderNameAppHeaderRow) {
                        textCheckCell.setTextAndValueAndCheck(getString(R.string.AP_FolderNameInHeader), getString(R.string.AP_FolderNameInHeader_Desc), CherrygramAppearanceConfig.INSTANCE.getFolderNameInHeader(), true, true);
                    } else if (position == hideAllChatsTabRow) {
                        textCheckCell.setTextAndCheck(getString(R.string.CP_NewTabs_RemoveAllChats), CherrygramAppearanceConfig.INSTANCE.getTabsHideAllChats(), true);
                    } else if (position == hideCounterRow) {
                        textCheckCell.setTextAndCheck(getString(R.string.CP_NewTabs_NoCounter), CherrygramAppearanceConfig.INSTANCE.getTabsNoUnread(), true);
                    } else if (position == addStrokeRow) {
                        textCheckCell.setTextAndCheck(getString(R.string.AP_Tab_Style_Stroke), CherrygramAppearanceConfig.INSTANCE.getTabStyleStroke(), true);
                    }
                    applyMD3Background(holder, position);
                    break;
                case VIEW_TYPE_TEXT_SETTINGS:
                    TextSettingsCell textSettingsCell = (TextSettingsCell) holder.itemView;
                    textSettingsCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    if (position == tabIconTypeRow) {
                        String value = switch (CherrygramAppearanceConfig.INSTANCE.getTabMode()) {
                            case CherrygramAppearanceConfig.TAB_TYPE_MIX ->
                                    getString(R.string.CG_FoldersTypeIconsTitles);
                            case CherrygramAppearanceConfig.TAB_TYPE_ICON ->
                                    getString(R.string.CG_FoldersTypeIcons);
                            default -> getString(R.string.CG_FoldersTypeTitles);
                        };
                        textSettingsCell.setTextAndValue(getString(R.string.CG_FoldersType_Header), value, true);
                    } else if (position == tabStyleRow) {
                        String value = switch (CherrygramAppearanceConfig.INSTANCE.getTabStyle()) {
                            case CherrygramAppearanceConfig.TAB_STYLE_DEFAULT ->
                                    getString(R.string.AP_Tab_Style_Default);
                            case CherrygramAppearanceConfig.TAB_STYLE_TEXT ->
                                    getString(R.string.AP_Tab_Style_Text);
                            case CherrygramAppearanceConfig.TAB_STYLE_VKUI -> "VKUI";
                            case CherrygramAppearanceConfig.TAB_STYLE_PILLS ->
                                    getString(R.string.AP_Tab_Style_Pills);
                            default -> getString(R.string.AP_Tab_Style_Rounded);
                        };
                        textSettingsCell.setTextAndValue(getString(R.string.AP_Tab_Style), value, true);
                    }
                    applyMD3Background(holder, position);
                    break;
                case VIEW_TYPE_PREVIEW:
                    applyMD3Background(holder, position);
                    break;
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == VIEW_TYPE_TEXT_CHECK || type == VIEW_TYPE_TEXT_SETTINGS;
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
                case VIEW_TYPE_PREVIEW:
                    foldersPreviewCell = new FoldersPreviewCell(mContext);
                    foldersPreviewCell.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
                    return new RecyclerListView.Holder(foldersPreviewCell);
                default:
                    throw new IllegalStateException("Unexpected value: " + viewType);
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            if (!CherrygramDebugConfig.INSTANCE.getMdContainers()) return;

            int viewType = holder.getItemViewType();
            int position = holder.getAdapterPosition();

            if (viewType == VIEW_TYPE_SHADOW /*|| viewType == VIEW_TYPE_TEXT_INFO_PRIVACY*/)
                return;

            int side = AndroidUtilities.dp(16);
            int top = 0;
            int bottom = 0;

            boolean prevIsHeader = position > 0 && getItemViewType(position - 1) == VIEW_TYPE_HEADER;
            boolean nextIsHeader = position < getItemCount() - 1 && getItemViewType(position + 1) == VIEW_TYPE_HEADER;

            if (position == 0 || getItemViewType(position - 1) == VIEW_TYPE_SHADOW /*|| getItemViewType(position - 1) == VIEW_TYPE_TEXT_INFO_PRIVACY*/) {
                top = AndroidUtilities.dp(2);
            }

            if (position == 0 /*|| viewType == VIEW_TYPE_HEADER*/) {
                top = AndroidUtilities.dp(16);
            }

            if (prevIsHeader) {
                top = 0;
            }

            if (position == getItemCount() - 1
                    || nextIsHeader
                    || getItemViewType(position + 1) == VIEW_TYPE_SHADOW
                /*|| getItemViewType(position + 1) == VIEW_TYPE_TEXT_INFO_PRIVACY*/
            ) {
                bottom = AndroidUtilities.dp(2);
            }

            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            lp.leftMargin = side;
            lp.rightMargin = side;
            lp.topMargin = top;
            lp.bottomMargin = bottom;
            holder.itemView.setLayoutParams(lp);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == divisorRow) {
                return VIEW_TYPE_SHADOW;
            } else if (position == foldersHeaderRow) {
                return VIEW_TYPE_HEADER;
            } else if (position == folderNameAppHeaderRow || position == hideAllChatsTabRow || position == hideCounterRow || position == addStrokeRow) {
                return VIEW_TYPE_TEXT_CHECK;
            } else if (position == tabIconTypeRow || position == tabStyleRow) {
                return VIEW_TYPE_TEXT_SETTINGS;
            } else if (position == foldersPreviewRow) {
                return VIEW_TYPE_PREVIEW;
            }
            return VIEW_TYPE_SHADOW;
        }

        private void applyMD3Background(RecyclerView.ViewHolder holder, int position) {
            if (!CherrygramDebugConfig.INSTANCE.getMdContainers()) return;

            int viewType = holder.getItemViewType();

            if (viewType == VIEW_TYPE_SHADOW/* || viewType == VIEW_TYPE_TEXT_INFO_PRIVACY*/) {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
                return;
            }

            int prevType = position > 0 ? getItemViewType(position - 1) : -1;
            int nextType = position < getItemCount() - 1 ? getItemViewType(position + 1) : -1;

            boolean isHeader = viewType == VIEW_TYPE_HEADER;

            boolean isGroupStart = position == 0
                    || prevType == VIEW_TYPE_SHADOW
                    /*|| prevType == VIEW_TYPE_TEXT_INFO_PRIVACY*/;

            boolean isGroupEnd = position == getItemCount() - 1
                    || nextType == VIEW_TYPE_SHADOW
                    /*|| nextType == VIEW_TYPE_TEXT_INFO_PRIVACY*/;

            int r = AndroidUtilities.dp(14);

            int topLeft = 0, topRight = 0, bottomLeft = 0, bottomRight = 0;

            if (isHeader) {
                topLeft = topRight = r;
            } else if (isGroupStart && isGroupEnd) {
                topLeft = topRight = bottomLeft = bottomRight = r;
            } else if (isGroupStart) {
                topLeft = topRight = r;
            } else if (isGroupEnd) {
                bottomLeft = bottomRight = r;
            }

            Drawable bg = Theme.createRoundRectDrawable(
                    topLeft, topRight, bottomRight, bottomLeft,
                    Theme.getColor(Theme.key_windowBackgroundWhite)
            );
            holder.itemView.setBackground(bg);

            final int side = 0;
            holder.itemView.setPadding(side, holder.itemView.getPaddingTop(), side, holder.itemView.getPaddingBottom());
        }

    }

    private void updateRowsId(boolean notify) {
        rowCount = 0;

        foldersHeaderRow = rowCount++;

        foldersPreviewRow = rowCount++;
        divisorRow = rowCount++;

        folderNameAppHeaderRow = rowCount++;
        hideAllChatsTabRow = rowCount++;
        hideCounterRow = rowCount++;
        tabIconTypeRow = rowCount++;
        tabStyleRow = rowCount++;

        int prevAddStrokeRow = addStrokeRow;
        addStrokeRow = -1;
        if (CherrygramAppearanceConfig.INSTANCE.getTabStyle() >= CherrygramAppearanceConfig.TAB_STYLE_VKUI) addStrokeRow = rowCount++;
        if (listAdapter != null) {
            if (prevAddStrokeRow == -1 && addStrokeRow != -1) {
                listAdapter.notifyItemInserted(addStrokeRow);
            } else if (prevAddStrokeRow != -1 && addStrokeRow == -1) {
                listAdapter.notifyItemRemoved(prevAddStrokeRow);
            }
        }

        divisorRow = rowCount++;

        if (listAdapter != null && notify) {
            listAdapter.notifyDataSetChanged();
        }
    }

}
