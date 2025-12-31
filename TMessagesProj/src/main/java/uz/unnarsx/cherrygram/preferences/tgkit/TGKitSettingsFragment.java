/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.preferences.tgkit;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import uz.unnarsx.cherrygram.core.configs.CherrygramDebugConfig;
import uz.unnarsx.cherrygram.preferences.tgkit.preference.TGKitCategory;
import uz.unnarsx.cherrygram.preferences.tgkit.preference.TGKitPreference;
import uz.unnarsx.cherrygram.preferences.tgkit.preference.TGKitSettings;
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitHeaderRow;
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitListPreference;
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitSectionRow;
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitSettingsCellRow;
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitSliderPreference;
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitSwitchPreference;
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitTextDetailRow;
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitTextIconRow;
import uz.unnarsx.cherrygram.preferences.BasePreferencesEntry;
import uz.unnarsx.cherrygram.preferences.cells.StickerSliderCell;

public class TGKitSettingsFragment extends BaseFragment {

    private final TGKitSettings settings;
    private final SparseArray<TGKitPreference> positions = new SparseArray<>();

    private int rowCount;
    private ListAdapter listAdapter;
    private RecyclerListView listView;

    private boolean openDelay = false;

    public TGKitSettingsFragment(BasePreferencesEntry entry) {
        super();
        this.settings = entry.getProcessedPrefs(this);
    }

    public TGKitSettingsFragment withOpenDelay(boolean openDelay) {
        this.openDelay = openDelay;
        return this;
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        rowCount = 0;
        initSettings();

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
    public boolean needDelayOpenAnimation() {
        return openDelay;
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

        actionBar.setTitle(settings.name);
        actionBar.setAllowOverlayTitle(true);
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
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener((view, position, x, y) -> {
            TGKitPreference pref = positions.get(position);
            if (pref instanceof TGKitSwitchPreference) {
                ((TGKitSwitchPreference) pref).contract.toggleValue();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(((TGKitSwitchPreference) pref).contract.getPreferenceValue());
                }
            } else if (pref instanceof TGKitTextIconRow preference) {
                if (preference.listener != null) preference.listener.onClick(this);
            } else if (pref instanceof TGKitTextDetailRow preference) {
                if (preference.listener != null) preference.listener.onClick(this);
            } else if (pref instanceof TGKitSettingsCellRow preference) {
                if (preference.listener != null) preference.listener.onClick(this);
            } else if (pref instanceof TGKitListPreference preference) {
                preference.callActionHueta(this, getParentActivity(), () -> {
                    if (view instanceof TextSettingsCell) {
                        ((TextSettingsCell) view).setTextAndValue(preference.title, preference.getContract().getValue(), true, preference.getDivider());
                    }
                });
            }
        });

        return fragmentView;
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private final Context mContext;

        private final int VIEW_TYPE_SHADOW = 0;
        private final int VIEW_TYPE_HEADER = 1;
        private final int VIEW_TYPE_TEXT_CELL = 2;
        private final int VIEW_TYPE_TEXT_CHECK = 3;
        private final int VIEW_TYPE_TEXT_SETTINGS = 4;
        private final int VIEW_TYPE_TEXT_INFO_PRIVACY = 5;
        private final int VIEW_TYPE_TEXT_DETAIL_SETTINGS = 6;
        private final int VIEW_TYPE_SLIDER = 7;

        ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case VIEW_TYPE_SHADOW: {
                    holder.itemView.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    applyMD3Background(holder, position);
                    break;
                }
                case VIEW_TYPE_HEADER: {
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    headerCell.setText(positions.get(position).title);
                    applyMD3Background(holder, position);
                    break;
                }
                case VIEW_TYPE_TEXT_CELL: {
                    TextCell textCell = (TextCell) holder.itemView;
                    ((TGKitTextIconRow) positions.get(position)).bindCell(textCell);

                    if (positions.get(position).title.toString().contains(getString(R.string.SP_DeleteAccount))) {
                        textCell.getImageView().setColorFilter(Theme.getColor(Theme.key_text_RedBold));
                        textCell.getTextView().setTextColor(Theme.getColor(Theme.key_text_RedBold));
                    }
                    applyMD3Background(holder, position);
                    break;
                }
                case VIEW_TYPE_TEXT_CHECK: {
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    TGKitSwitchPreference pref = (TGKitSwitchPreference) positions.get(position);
                    if (pref.description != null) {
                        textCheckCell.setTextAndValueAndCheck(pref.title.toString(), pref.description, pref.contract.getPreferenceValue(), true, pref.divider);
                    } else {
                        textCheckCell.setTextAndCheck(pref.title, pref.contract.getPreferenceValue(), pref.divider);
                    }
                    applyMD3Background(holder, position);
                    break;
                }
                case VIEW_TYPE_TEXT_SETTINGS: {
                    TextSettingsCell textSettingsCell = (TextSettingsCell) holder.itemView;
                    Object pref = positions.get(position);

                    if (pref instanceof TGKitSettingsCellRow rowPref) {
                        textSettingsCell.setCanDisable(false);
                        textSettingsCell.setTextColor(rowPref.textColor);
                        textSettingsCell.setText(rowPref.title, rowPref.divider);
                    } else if (pref instanceof TGKitListPreference listPref) {
                        textSettingsCell.setTextAndValue(listPref.title, listPref.getContract().getValue(), true, listPref.getDivider());
                    }
                    applyMD3Background(holder, position);
                    break;
                }
                case VIEW_TYPE_TEXT_INFO_PRIVACY: {
                    TextInfoPrivacyCell textInfoPrivacyCell = (TextInfoPrivacyCell) holder.itemView;
                    textInfoPrivacyCell.setText(positions.get(position).title);
                    applyMD3Background(holder, position);
                    break;
                }
                case VIEW_TYPE_TEXT_DETAIL_SETTINGS: {
                    TextDetailSettingsCell textDetailSettingsCell = (TextDetailSettingsCell) holder.itemView;
                    textDetailSettingsCell.setMultilineDetail(true);
                    ((TGKitTextDetailRow) positions.get(position)).bindCell(textDetailSettingsCell);
                    applyMD3Background(holder, position);
                    break;
                }
                case VIEW_TYPE_SLIDER: {
                    ((StickerSliderCell) holder.itemView).setContract(((TGKitSliderPreference) positions.get(position)).contract);
                    applyMD3Background(holder, position);
                    break;
                }
            }
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            int viewType = holder.getItemViewType();
            int position = holder.getAdapterPosition();

            if (viewType == VIEW_TYPE_TEXT_CHECK) {
                TextCheckCell checkCell = (TextCheckCell) holder.itemView;
                checkCell.setChecked(((TGKitSwitchPreference) positions.get(position)).contract.getPreferenceValue());
            } else if (viewType == VIEW_TYPE_TEXT_SETTINGS) {
                TextSettingsCell checkCell = (TextSettingsCell) holder.itemView;
                TGKitListPreference pref = ((TGKitListPreference) positions.get(position));
                checkCell.setTextAndValue(pref.title, pref.getContract().getValue(), true, pref.getDivider());
            }

            if (!CherrygramDebugConfig.INSTANCE.getMdContainers()) return;

            // Ignore roundings
            if (viewType == VIEW_TYPE_SHADOW || viewType == VIEW_TYPE_TEXT_INFO_PRIVACY)
                return;

            int side = AndroidUtilities.dp(16); // Outside margin - dp(16) in docs
            int top = 0;
            int bottom = 0;

            boolean prevIsHeader = position > 0 && getItemViewType(position - 1) == VIEW_TYPE_HEADER;
            boolean nextIsHeader = position < getItemCount() - 1 && getItemViewType(position + 1) == VIEW_TYPE_HEADER;

            // top margin
            if (position == 0 || getItemViewType(position - 1) == VIEW_TYPE_SHADOW
                    || getItemViewType(position - 1) == VIEW_TYPE_TEXT_INFO_PRIVACY
            ) {
                top = AndroidUtilities.dp(2); // margin between groups. Use 2 because of shadow height
            }

            // Margin between the action bar and the first element inside the list
            if (position == 0 /*|| viewType == VIEW_TYPE_HEADER*/) {
                top = AndroidUtilities.dp(16);
            }

            // remove gap between header and the next element
            if (prevIsHeader) {
                top = 0;
            }

            // bottom margin
            if (position == getItemCount() - 1
                    || nextIsHeader
                    || getItemViewType(position + 1) == VIEW_TYPE_SHADOW
                    || getItemViewType(position + 1) == VIEW_TYPE_TEXT_INFO_PRIVACY
            ) {
                bottom = AndroidUtilities.dp(2); // margin between groups. Use 2 because of shadow height
            }

            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            lp.leftMargin = side;
            lp.rightMargin = side;
            lp.topMargin = top;
            lp.bottomMargin = bottom;
            holder.itemView.setLayoutParams(lp);
        }

        public boolean isRowEnabled(int position) {
            return positions.get(position).getType().enabled;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return isRowEnabled(holder.getAdapterPosition());
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
                    view = new TextCell(mContext, 21, false);
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
                case VIEW_TYPE_TEXT_INFO_PRIVACY:
                    view = new TextInfoPrivacyCell(mContext);
                    break;
                case VIEW_TYPE_TEXT_DETAIL_SETTINGS:
                    view = new TextDetailSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_SLIDER:
                    view = new StickerSliderCell(mContext, resourceProvider);
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
            return positions.get(position).getType().adapterType;
        }

        private void applyMD3Background(RecyclerView.ViewHolder holder, int position) {
            if (!CherrygramDebugConfig.INSTANCE.getMdContainers()) return;

            int viewType = holder.getItemViewType();

            // Ignore roundings
            if (viewType == VIEW_TYPE_SHADOW || viewType == VIEW_TYPE_TEXT_INFO_PRIVACY) {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
                return;
            }

            int prevType = position > 0 ? getItemViewType(position - 1) : -1;
            int nextType = position < getItemCount() - 1 ? getItemViewType(position + 1) : -1;

            boolean isHeader = viewType == VIEW_TYPE_HEADER;

            // group start = any first element or any element after SHADOW / INFO_PRIVACY
            boolean isGroupStart = position == 0
                    || prevType == VIEW_TYPE_SHADOW
                    || prevType == VIEW_TYPE_TEXT_INFO_PRIVACY;

            // group end = any last element or any element before SHADOW / INFO_PRIVACY
            boolean isGroupEnd = position == getItemCount() - 1
                    || nextType == VIEW_TYPE_SHADOW
                    || nextType == VIEW_TYPE_TEXT_INFO_PRIVACY;

            int r = AndroidUtilities.dp(14);

            // default 0 for roundings
            int topLeft = 0, topRight = 0, bottomLeft = 0, bottomRight = 0;

            if (isHeader) {
                // apply only top rounding for headers
                topLeft = topRight = r;
            } else if (isGroupStart && isGroupEnd) {
                // buttons - apply 4-corner rounding
                topLeft = topRight = bottomLeft = bottomRight = r;
            } else if (isGroupStart) {
                // do not round corner if the previous element was a header
                topLeft = topRight = r;
            } else if (isGroupEnd) {
                bottomLeft = bottomRight = r;
            }

            Drawable bg = Theme.createRoundRectDrawable(
                    topLeft, topRight, bottomRight, bottomLeft,
                    Theme.getColor(Theme.key_windowBackgroundWhite)
            );
            holder.itemView.setBackground(bg);

            // margins inside a view
            final int side = 0; // already handled via LayoutParams (dp(16) in docs)
            holder.itemView.setPadding(side, holder.itemView.getPaddingTop(), side, holder.itemView.getPaddingBottom());
        }

    }

    private void initSettings() {
        for (TGKitCategory category : settings.categories) {
            if (!category.isAvailable) continue;

            if (category.name != null) positions.put(rowCount++, new TGKitHeaderRow(category.name));

            for (TGKitPreference preference : category.preferences) {
                if (preference.isAvailable) positions.put(rowCount++, preference);
            }

            positions.put(rowCount++, new TGKitSectionRow());
        }
    }

}