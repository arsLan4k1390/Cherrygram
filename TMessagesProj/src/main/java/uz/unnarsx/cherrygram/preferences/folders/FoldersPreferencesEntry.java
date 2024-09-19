package uz.unnarsx.cherrygram.preferences.folders;

import static org.telegram.messenger.LocaleController.getString;

import android.annotation.SuppressLint;
import android.content.Context;
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
import uz.unnarsx.cherrygram.helpers.ui.PopupHelper;
import uz.unnarsx.cherrygram.preferences.folders.cells.FoldersPreviewCell;

public class FoldersPreferencesEntry extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

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

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_SWITCH = 1;
    private static final int VIEW_TYPE_TEXT_SETTING = 2;
    private static final int VIEW_TYPE_PREVIEW = 3;
    private static final int VIEW_TYPE_SHADOW = 4;

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
                CherrygramAppearanceConfig.INSTANCE.toggleFolderNameInHeader();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getFolderNameInHeader());
                }
                parentLayout.rebuildAllFragmentViews(false, false);
                getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
            } else if (position == hideAllChatsTabRow) {
                CherrygramAppearanceConfig.INSTANCE.toggleTabsHideAllChats();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getTabsHideAllChats());
                }
                foldersPreviewCell.updateAllChatsTabName(true);
                parentLayout.rebuildAllFragmentViews(false, false);
                getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
                getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
            } else if (position == hideCounterRow) {
                CherrygramAppearanceConfig.INSTANCE.toggleTabsNoUnread();
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
                CherrygramAppearanceConfig.INSTANCE.toggleTabStyleStroke();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getTabStyleStroke());
                }
                foldersPreviewCell.updateTabStroke(true);
                parentLayout.rebuildAllFragmentViews(false, false);
            }
        });

        return fragmentView;
    }

    @SuppressLint("NotifyDataSetChanged")
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
                case VIEW_TYPE_HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == foldersHeaderRow) {
                        headerCell.setText(getString(R.string.ProfileBotPreviewTab));
                    }
                    break;
                case VIEW_TYPE_SWITCH:
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
                    break;
                case VIEW_TYPE_TEXT_SETTING:
                    TextSettingsCell textCell = (TextSettingsCell) holder.itemView;
                    textCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    if (position == tabIconTypeRow) {
                        String value;
                        switch (CherrygramAppearanceConfig.INSTANCE.getTabMode()) {
                            case CherrygramAppearanceConfig.TAB_TYPE_MIX:
                                value = getString(R.string.CG_FoldersTypeIconsTitles);
                                break;
                            case CherrygramAppearanceConfig.TAB_TYPE_ICON:
                                value = getString(R.string.CG_FoldersTypeIcons);
                                break;
                            default:
                            case CherrygramAppearanceConfig.TAB_TYPE_TEXT:
                                value = getString(R.string.CG_FoldersTypeTitles);
                                break;
                        }
                        textCell.setTextAndValue(getString(R.string.CG_FoldersType_Header), value, true);
                    } else if (position == tabStyleRow) {
                        String value;
                        switch (CherrygramAppearanceConfig.INSTANCE.getTabStyle()) {
                            case CherrygramAppearanceConfig.TAB_STYLE_DEFAULT:
                                value = getString(R.string.AP_Tab_Style_Default);
                                break;
                            case CherrygramAppearanceConfig.TAB_STYLE_TEXT:
                                value = getString(R.string.AP_Tab_Style_Text);
                                break;
                            case CherrygramAppearanceConfig.TAB_STYLE_VKUI:
                                value = "VKUI";
                                break;
                            case CherrygramAppearanceConfig.TAB_STYLE_PILLS:
                                value = getString(R.string.AP_Tab_Style_Pills);
                                break;
                            default:
                            case CherrygramAppearanceConfig.TAB_STYLE_ROUNDED:
                                value = getString(R.string.AP_Tab_Style_Rounded);
                                break;
                        }
                        textCell.setTextAndValue(getString(R.string.AP_Tab_Style), value, true);
                    }
                    break;
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == VIEW_TYPE_SWITCH || type == VIEW_TYPE_TEXT_SETTING;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case VIEW_TYPE_HEADER:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_SWITCH:
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_TEXT_SETTING:
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_PREVIEW:
                    foldersPreviewCell = new FoldersPreviewCell(mContext);
                    foldersPreviewCell.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
                    return new RecyclerListView.Holder(foldersPreviewCell);
                case VIEW_TYPE_SHADOW:
                default:
                    view = new ShadowSectionCell(mContext);
                    break;
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == foldersHeaderRow) {
                return VIEW_TYPE_HEADER;
            } else if (position == folderNameAppHeaderRow || position == hideAllChatsTabRow || position == hideCounterRow || position == addStrokeRow) {
                return VIEW_TYPE_SWITCH;
            } else if (position == tabIconTypeRow || position == tabStyleRow) {
                return VIEW_TYPE_TEXT_SETTING;
            } else if (position == foldersPreviewRow) {
                return VIEW_TYPE_PREVIEW;
            }
            return VIEW_TYPE_SHADOW;
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
