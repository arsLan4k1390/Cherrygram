/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.preferences;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.OutlineEditText;
import org.telegram.ui.Components.RecyclerListView;

import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig;
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.core.helpers.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.helpers.network.DonatesManager;
import uz.unnarsx.cherrygram.preferences.tgkit.CherrygramPreferencesNavigator;

public class FiltersPreferencesEntry extends BaseFragment {

    private int rowCount;
    private ListAdapter listAdapter;
    private RecyclerListView listView;

    private int filtersHeaderRow;
    private int enableFilterRow;
    private int filterWordsRow;
    private int filteredWordsAdviceRow;
    private int detectTranslitRow;
    private int exactWordMatchRow;
    private int filtersEndDivisor;

    private int miscellaneousHeaderRow;
    private int hideFromBlockedRow;
    private int hideAllRow;
    private int collapseAutomaticallyRow;
    private int miscellaneousEndDivisor;

    protected Theme.ResourcesProvider resourcesProvider;

    private OutlineEditText outlineEditText;

    private static final int done_button = 1;
    private ActionBarMenuItem doneButton;

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
        checkDone(true);
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

        actionBar.setTitle(getString(R.string.CP_Message_Filtering));
        actionBar.setAllowOverlayTitle(false);

        actionBar.setOccupyStatusBar(!AndroidUtilities.isTablet());
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == done_button) {
                    checkDone(true);
                }
            }
        });
        doneButton = actionBar.createMenu().addItemWithWidth(done_button, R.drawable.ic_ab_done, dp(56), getString(R.string.Done));

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
            boolean requireDonate = !DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace();

            var holder = listView.findViewHolderForAdapterPosition(position);
            if (holder == null || !listAdapter.isEnabled(holder)) {
                return;
            }
            if (requireDonate) {
                AndroidUtilities.shakeView(view);
                BulletinFactory.of(this).createSimpleBulletin(
                        R.raw.cg_star_reaction, // stars_topup // star_premium_2
                        getString(R.string.DP_Donate_Exclusive),
                        getString(R.string.DP_Donate_ExclusiveDesc),
                        getString(R.string.MoreInfo),
                        () -> presentFragment(CherrygramPreferencesNavigator.INSTANCE.createDonate())
                ).show();
                return;
            }
            if (position == enableFilterRow) {
                CherrygramChatsConfig.INSTANCE.setEnableMsgFilters(!CherrygramChatsConfig.INSTANCE.getEnableMsgFilters());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramChatsConfig.INSTANCE.getEnableMsgFilters());
                }

                if (!CherrygramChatsConfig.INSTANCE.getEnableMsgFilters()) {
                    AndroidUtilities.runOnUIThread(() -> AndroidUtilities.hideKeyboard(listView), 50);
                }

                listAdapter.notifyItemChanged(filterWordsRow, false);
                listAdapter.notifyItemChanged(detectTranslitRow, false);
                listAdapter.notifyItemChanged(exactWordMatchRow, false);
                listAdapter.notifyItemChanged(miscellaneousHeaderRow, false);
                listAdapter.notifyItemChanged(hideFromBlockedRow, false);
                listAdapter.notifyItemChanged(hideAllRow, false);
                listAdapter.notifyItemChanged(collapseAutomaticallyRow, false);
            } else if (position == detectTranslitRow) {
                CherrygramChatsConfig.INSTANCE.setMsgFiltersDetectTranslit(!CherrygramChatsConfig.INSTANCE.getMsgFiltersDetectTranslit());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramChatsConfig.INSTANCE.getMsgFiltersDetectTranslit());

                    if (CherrygramChatsConfig.INSTANCE.getMsgFiltersDetectTranslit() && !CherrygramChatsConfig.INSTANCE.getEnableMsgFilters()) {
                        CherrygramChatsConfig.INSTANCE.setEnableMsgFilters(true);
                        listAdapter.notifyItemChanged(enableFilterRow, false);
                    }
                }
            } else if (position == exactWordMatchRow) {
                CherrygramChatsConfig.INSTANCE.setMsgFiltersMatchExactWord(!CherrygramChatsConfig.INSTANCE.getMsgFiltersMatchExactWord());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramChatsConfig.INSTANCE.getMsgFiltersMatchExactWord());

                    if (CherrygramChatsConfig.INSTANCE.getMsgFiltersMatchExactWord() && !CherrygramChatsConfig.INSTANCE.getEnableMsgFilters()) {
                        CherrygramChatsConfig.INSTANCE.setEnableMsgFilters(true);
                        listAdapter.notifyItemChanged(enableFilterRow, false);
                    }
                }
            } else if (position == hideFromBlockedRow) {
                CherrygramChatsConfig.INSTANCE.setMsgFiltersHideFromBlocked(!CherrygramChatsConfig.INSTANCE.getMsgFiltersHideFromBlocked());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramChatsConfig.INSTANCE.getMsgFiltersHideFromBlocked());

                    if (CherrygramChatsConfig.INSTANCE.getMsgFiltersHideFromBlocked() && !CherrygramChatsConfig.INSTANCE.getEnableMsgFilters()) {
                        CherrygramChatsConfig.INSTANCE.setEnableMsgFilters(true);
                        listAdapter.notifyItemChanged(enableFilterRow, false);
                    }
                    listAdapter.notifyItemChanged(collapseAutomaticallyRow, false);
                }
            } else if (position == hideAllRow) {
                CherrygramChatsConfig.INSTANCE.setMsgFiltersHideAll(!CherrygramChatsConfig.INSTANCE.getMsgFiltersHideAll());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramChatsConfig.INSTANCE.getMsgFiltersHideAll());

                    if (CherrygramChatsConfig.INSTANCE.getMsgFiltersHideAll() && !CherrygramChatsConfig.INSTANCE.getEnableMsgFilters()) {
                        CherrygramChatsConfig.INSTANCE.setEnableMsgFilters(true);
                        listAdapter.notifyItemChanged(enableFilterRow, false);
                    }
                    listAdapter.notifyItemChanged(collapseAutomaticallyRow, false);
                }
            } else if (position == collapseAutomaticallyRow) {
                CherrygramChatsConfig.INSTANCE.setMsgFiltersCollapseAutomatically(!CherrygramChatsConfig.INSTANCE.getMsgFiltersCollapseAutomatically());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramChatsConfig.INSTANCE.getMsgFiltersCollapseAutomatically());

                    if (CherrygramChatsConfig.INSTANCE.getMsgFiltersCollapseAutomatically() && !CherrygramChatsConfig.INSTANCE.getEnableMsgFilters()) {
                        CherrygramChatsConfig.INSTANCE.setEnableMsgFilters(true);
                        listAdapter.notifyItemChanged(enableFilterRow, false);
                    }
                }
            }
        });

        FirebaseAnalyticsHelper.trackEventWithEmptyBundle("filters_preferences_screen");

        return fragmentView;
    }

    public class ListAdapter extends RecyclerListView.SelectionAdapter {

        private final Context mContext;

        private final int VIEW_TYPE_SHADOW = 0;
        private final int VIEW_TYPE_HEADER = 1;
//        private final int VIEW_TYPE_TEXT_CELL = 2;
        private final int VIEW_TYPE_TEXT_CHECK = 3;
//        private final int VIEW_TYPE_TEXT_SETTINGS = 4;
        private final int VIEW_TYPE_TEXT_INFO_PRIVACY = 5;
//        private final int VIEW_TYPE_TEXT_DETAIL_SETTINGS = 6;
        private final int VIEW_TYPE_EDIT_TEXT = 7;

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
                    holder.itemView.setEnabled(false);
                    holder.itemView.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case VIEW_TYPE_HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    headerCell.setEnabled(false);

                    if (position == filtersHeaderRow) {
                        headerCell.setEnabled(true, null);
                        headerCell.setText(getString(R.string.General));
                    } else if (position == miscellaneousHeaderRow) {
                        headerCell.setEnabled(CherrygramChatsConfig.INSTANCE.getEnableMsgFilters(), null);
                        headerCell.setText(getString(R.string.LocalMiscellaneousCache));
                    }
                    break;
                case VIEW_TYPE_TEXT_CHECK:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    textCheckCell.setEnabled(CherrygramChatsConfig.INSTANCE.getEnableMsgFilters(), null);

                    if (requireDonate) textCheckCell.setCheckBoxIcon(R.drawable.permission_locked);

                    if (position == enableFilterRow) {
                        textCheckCell.setEnabled(true, null);
                        textCheckCell.setTextAndValueAndCheck(
                                getString(R.string.CP_Message_Filtering_Filter),
                                getString(R.string.CP_Message_Filtering_Filter_Desc),
                                CherrygramChatsConfig.INSTANCE.getEnableMsgFilters(),
                                true,
                                true
                        );
                    } else if (position == detectTranslitRow) {
                        textCheckCell.setEnabled(CherrygramChatsConfig.INSTANCE.getEnableMsgFilters(), null);
                        textCheckCell.setTextAndValueAndCheck(
                                getString(R.string.CP_Message_Filtering_Translit),
                                getString(R.string.CP_Message_Filtering_Translit_Desc),
                                CherrygramChatsConfig.INSTANCE.getMsgFiltersDetectTranslit(),
                                true,
                                true
                        );
                    } else if (position == exactWordMatchRow) {
                        textCheckCell.setEnabled(CherrygramChatsConfig.INSTANCE.getEnableMsgFilters(), null);
                        textCheckCell.setTextAndValueAndCheck(
                                getString(R.string.CP_Message_Filtering_Exact_Words),
                                getString(R.string.CP_Message_Filtering_Exact_Words_Desc),
                                CherrygramChatsConfig.INSTANCE.getMsgFiltersMatchExactWord(),
                                true,
                                true
                        );
                    } else if (position == hideFromBlockedRow) {
                        textCheckCell.setEnabled(CherrygramChatsConfig.INSTANCE.getEnableMsgFilters(), null);
                        textCheckCell.setTextAndValueAndCheck(
                                getString(R.string.CP_Message_Filtering_HideBlocked),
                                getString(R.string.CP_Message_Filtering_HideBlockedDesc),
                                CherrygramChatsConfig.INSTANCE.getMsgFiltersHideFromBlocked(),
                                true,
                                true
                        );
                    } else if (position == hideAllRow) {
                        textCheckCell.setEnabled(CherrygramChatsConfig.INSTANCE.getEnableMsgFilters(), null);
                        textCheckCell.setTextAndValueAndCheck(
                                getString(R.string.CP_Message_Filtering_HideAll),
                                getString(R.string.CP_Message_Filtering_HideAllDesc),
                                CherrygramChatsConfig.INSTANCE.getMsgFiltersHideAll(),
                                true,
                                true
                        );
                    } else if (position == collapseAutomaticallyRow) {
                        textCheckCell.setEnabled(
                                CherrygramChatsConfig.INSTANCE.getEnableMsgFilters() && (CherrygramChatsConfig.INSTANCE.getMsgFiltersHideFromBlocked() || CherrygramChatsConfig.INSTANCE.getMsgFiltersHideAll()),
                                null
                        );
                        textCheckCell.setTextAndValueAndCheck(
                                getString(R.string.CP_Message_Filtering_Collapse),
                                getString(R.string.CP_Message_Filtering_Collapse_Desc),
                                CherrygramChatsConfig.INSTANCE.getMsgFiltersCollapseAutomatically(),
                                true,
                                false
                        );
                    }
                    break;
                case VIEW_TYPE_TEXT_INFO_PRIVACY:
                    TextInfoPrivacyCell textInfoPrivacyCell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == filteredWordsAdviceRow) {
                        textInfoPrivacyCell.setText(getString(R.string.CP_Message_Filtering_Field_Desc));
                        textInfoPrivacyCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                        textInfoPrivacyCell.getTextView().setPadding(0, -dp(4), 0, dp(8));
                    }
                    break;
                case VIEW_TYPE_EDIT_TEXT:
                    outlineEditText = (OutlineEditText) holder.itemView;
                    outlineEditText.setPadding(dp(16), dp(12), dp(16), dp(12));
                    if (position == filterWordsRow) {
                        outlineEditText.setEnabled(CherrygramChatsConfig.INSTANCE.getEnableMsgFilters(), null);
                        outlineEditText.getEditText().setEnabled(CherrygramChatsConfig.INSTANCE.getEnableMsgFilters());
                        outlineEditText.getEditText().addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {}

                            @Override
                            public void afterTextChanged(Editable s) {
                                checkDone(false);
                            }
                        });
                        outlineEditText.getEditText().setSingleLine(false);
                        outlineEditText.setHint(getString(R.string.CP_Message_Filtering_Field));
                        outlineEditText.getEditText().setText(CherrygramChatsConfig.INSTANCE.getMsgFiltersElements());
                        outlineEditText.setMinimumHeight(200);
                        outlineEditText.getEditText().setPadding(dp(16), dp(12), dp(16), dp(12));
                    }
                    break;
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return holder.itemView.isEnabled();
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
                case VIEW_TYPE_TEXT_INFO_PRIVACY:
                    view = new TextInfoPrivacyCell(mContext);
                    break;
                case VIEW_TYPE_EDIT_TEXT:
                    view = new OutlineEditText(mContext, resourcesProvider);
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
            if (position == filtersEndDivisor || position == miscellaneousEndDivisor) {
                return VIEW_TYPE_SHADOW;
            } else if (position == filtersHeaderRow || position == miscellaneousHeaderRow) {
                return VIEW_TYPE_HEADER;
            } else if (position == enableFilterRow || position == detectTranslitRow || position == exactWordMatchRow || position == hideFromBlockedRow || position == hideAllRow  || position == collapseAutomaticallyRow) {
                return VIEW_TYPE_TEXT_CHECK;
            } else if (position == filteredWordsAdviceRow) {
                return VIEW_TYPE_TEXT_INFO_PRIVACY;
            } else if (position == filterWordsRow) {
                return VIEW_TYPE_EDIT_TEXT;
            }
            return VIEW_TYPE_SHADOW;
        }
    }

    private void updateRowsId(boolean notify) {
        rowCount = 0;

        filtersHeaderRow = rowCount++;
        enableFilterRow = rowCount++;
        filterWordsRow = rowCount++;
        filteredWordsAdviceRow = rowCount++;
        detectTranslitRow = rowCount++;
        exactWordMatchRow = rowCount++;
        filtersEndDivisor = rowCount++;

        miscellaneousHeaderRow = rowCount++;
        if (CherrygramCoreConfig.INSTANCE.isDevBuild() || CherrygramCoreConfig.INSTANCE.isStandalonePremiumBuild()) hideFromBlockedRow = rowCount++;
        hideAllRow = rowCount++;
        collapseAutomaticallyRow = rowCount++;
        miscellaneousEndDivisor = rowCount++;

        if (listAdapter != null && notify) {
            listAdapter.notifyDataSetChanged();
        }
    }

    private boolean hasChanges() {
        return (
                !TextUtils.equals(CherrygramChatsConfig.INSTANCE.getMsgFiltersElements(), outlineEditText.getEditText().getText().toString())
        );
    }

    private void checkDone(boolean finish) {
        if (doneButton == null || outlineEditText == null) return;

        if (finish && hasChanges()) {
            doOnDone(this);
        }

        boolean changed = hasChanges();

        doneButton.setEnabled(changed);

        doneButton.animate()
                .alpha(changed ? 1.0f : 0.0f)
                .scaleX(changed ? 1.0f : 0.0f)
                .scaleY(changed ? 1.0f : 0.0f)
                .setDuration(180)
                .start();
    }

    private void doOnDone(BaseFragment fragment) {
        if (fragment == null || fragment.getParentActivity() == null) {
            return;
        }

        CherrygramChatsConfig.INSTANCE.setMsgFiltersElements(
                outlineEditText.getEditText().getText().toString()
        );

        AndroidUtilities.runOnUIThread(() -> AndroidUtilities.hideKeyboard(listView), 50);

        outlineEditText.getEditText().clearFocus();

        if (CherrygramChatsConfig.INSTANCE.getMsgFiltersHideFromBlocked()) {
            getMessagesController().getBlockedPeers(false);
        }
    }

}
