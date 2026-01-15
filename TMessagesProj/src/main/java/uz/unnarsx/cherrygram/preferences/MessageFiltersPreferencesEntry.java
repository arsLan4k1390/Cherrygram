/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BotWebViewVibrationEffect;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.OutlineEditText;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.UsersSelectActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import uz.unnarsx.cherrygram.chats.helpers.MessagesFilterHelper;
import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig;
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.core.helpers.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.core.ui.MD3ListAdapter;
import uz.unnarsx.cherrygram.donates.DonatesManager;

public class MessageFiltersPreferencesEntry extends BaseFragment {

    private int rowCount;
    private ListAdapter listAdapter;
    private RecyclerListView listView;

    private int filtersHeaderRow;
    private int enableFilterRow;
    private int filterWordsRow;
    private int filteredWordsAdviceRow;
    private int detectTranslitRow;
    private int exactWordMatchRow;
    private int exclusionsRow;
    private int filtersEndDivisor;

    private int miscellaneousHeaderRow;
    private int detectEntitiesRow;
    private int hideFromBlockedRow;
    private int hideAllRow;
    private int collapseAutomaticallyRow;
    private int makeTransparentRow;
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

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonDrawable(new BackDrawable(false));

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
                AndroidUtilities.shakeViewSpring(view);
                BotWebViewVibrationEffect.APP_ERROR.vibrate();
                BulletinFactory.of(this).createSimpleBulletin(
                        R.raw.cg_star_reaction, // stars_topup // star_premium_2
                        getString(R.string.DP_Donate_Exclusive),
                        getString(R.string.DP_Donate_ExclusiveDesc),
                        getString(R.string.MoreInfo),
                        () -> {
                            if (getConnectionsManager().isTestBackend()) {
                                CherrygramPreferencesNavigator.INSTANCE.createDonate(this);
                            } else {
                                CherrygramPreferencesNavigator.INSTANCE.createDonateForce(this);
                            }
                        }
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
                listAdapter.notifyItemChanged(exclusionsRow, false);
                listAdapter.notifyItemChanged(miscellaneousHeaderRow, false);
                listAdapter.notifyItemChanged(detectEntitiesRow, false);
                if (CherrygramCoreConfig.isDevBuild() || CherrygramCoreConfig.isStandalonePremiumBuild()) listAdapter.notifyItemChanged(hideFromBlockedRow, false);
                listAdapter.notifyItemChanged(hideAllRow, false);
                listAdapter.notifyItemChanged(collapseAutomaticallyRow, false);
                listAdapter.notifyItemChanged(makeTransparentRow, false);
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
            } else if (position == exclusionsRow) {
                AndroidUtilities.runOnUIThread(() -> {
                    UsersSelectActivity activity = getUsersSelectActivity();
                    activity.setDelegate((ids, unused) -> {
                        MessagesFilterHelper messagesFilterHelper = MessagesFilterHelper.INSTANCE;

                        Set<Long> chatIds = new HashSet<>(ids);
                        Set<String> excludedChats = new HashSet<>(messagesFilterHelper.getArrayList(messagesFilterHelper.getExcludedList()));

                        if (CherrygramCoreConfig.isDevBuild()) FileLog.d("old excluded chats array: " + excludedChats);
                        excludedChats.clear();

                        if (!chatIds.isEmpty()) {
                            for (Long id : chatIds) {
                                if (DialogObject.isUserDialog(id) || DialogObject.isChatDialog(id)) {
                                    excludedChats.add(String.valueOf(id));
                                }
                            }
                        }

                        messagesFilterHelper.saveArrayList(new ArrayList<>(excludedChats), messagesFilterHelper.getExcludedList());
                        if (CherrygramCoreConfig.isDevBuild()) FileLog.d("new excluded chats array: " + excludedChats);

                        listAdapter.notifyItemChanged(exclusionsRow, false);
                    });
                    presentFragment(activity);
                }, 300);
            } else if (position == detectEntitiesRow) {
                CherrygramChatsConfig.INSTANCE.setMsgFiltersDetectEntities(!CherrygramChatsConfig.INSTANCE.getMsgFiltersDetectEntities());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramChatsConfig.INSTANCE.getMsgFiltersDetectEntities());

                    if (CherrygramChatsConfig.INSTANCE.getMsgFiltersDetectEntities() && !CherrygramChatsConfig.INSTANCE.getEnableMsgFilters()) {
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
            } else if (position == makeTransparentRow) {
                CherrygramChatsConfig.INSTANCE.setMsgFilterTransparentMsg(!CherrygramChatsConfig.INSTANCE.getMsgFilterTransparentMsg());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramChatsConfig.INSTANCE.getMsgFilterTransparentMsg());

                    if (CherrygramChatsConfig.INSTANCE.getMsgFilterTransparentMsg() && !CherrygramChatsConfig.INSTANCE.getEnableMsgFilters()) {
                        CherrygramChatsConfig.INSTANCE.setEnableMsgFilters(true);
                        listAdapter.notifyItemChanged(enableFilterRow, false);
                    }
                }
            }
        });

        FirebaseAnalyticsHelper.trackEventWithEmptyBundle("filters_preferences_screen");

        return fragmentView;
    }

    private class ListAdapter extends MD3ListAdapter {

        private final Context mContext;

        private final int VIEW_TYPE_SHADOW = 0;
        private final int VIEW_TYPE_HEADER = 1;
        private final int VIEW_TYPE_TEXT_CELL = 2;
        private final int VIEW_TYPE_TEXT_CHECK = 3;
//        private final int VIEW_TYPE_TEXT_SETTINGS = 4;
        private final int VIEW_TYPE_TEXT_INFO_PRIVACY = 5;
//        private final int VIEW_TYPE_TEXT_DETAIL_SETTINGS = 6;
        private final int VIEW_TYPE_EDIT_TEXT = 7;

        ListAdapter(Context context) {
            forceLearnRole(VIEW_TYPE_TEXT_INFO_PRIVACY, ROLE_CONTENT);
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
                case VIEW_TYPE_TEXT_CELL:
                    TextCell textCell = (TextCell) holder.itemView;
                    textCell.setEnabled(false);

                    if (position == exclusionsRow) {
                        textCell.setEnabled(CherrygramChatsConfig.INSTANCE.getEnableMsgFilters(), null);
                        textCell.setTextAndValueAndIcon(
                                getString(R.string.CP_Message_Filtering_Exclusions),
                                String.valueOf(MessagesFilterHelper.INSTANCE.getExcludedChatsCount()),
                                R.drawable._menu_stream_comments_off_24,
                                false
                        );
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
                    } else if (position == detectEntitiesRow) {
                        textCheckCell.setEnabled(CherrygramChatsConfig.INSTANCE.getEnableMsgFilters(), null);
                        textCheckCell.setTextAndValueAndCheck(
                                getString(R.string.CP_Message_Filtering_Entities),
                                getString(R.string.CP_Message_Filtering_EntitiesDesc),
                                CherrygramChatsConfig.INSTANCE.getMsgFiltersDetectEntities(),
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
                    } else if (position == makeTransparentRow) {
                        textCheckCell.setEnabled(CherrygramChatsConfig.INSTANCE.getEnableMsgFilters(), null);
                        textCheckCell.setTextAndValueAndCheck(
                                getString(R.string.CP_Message_Filtering_Transparent),
                                getString(R.string.CP_Message_Filtering_Transparent_Desc),
                                CherrygramChatsConfig.INSTANCE.getMsgFilterTransparentMsg(),
                                true,
                                true
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
                case VIEW_TYPE_TEXT_CELL:
                    view = new TextCell(mContext);
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
            } else if (position == exclusionsRow) {
                return VIEW_TYPE_TEXT_CELL;
            } else if (position == enableFilterRow || position == detectTranslitRow || position == exactWordMatchRow || position == detectEntitiesRow || position == hideFromBlockedRow || position == hideAllRow  || position == collapseAutomaticallyRow || position == makeTransparentRow) {
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
        exclusionsRow = rowCount++;
        filtersEndDivisor = rowCount++;

        miscellaneousHeaderRow = rowCount++;
        detectEntitiesRow = rowCount++;
        if (CherrygramCoreConfig.isDevBuild() || CherrygramCoreConfig.isStandalonePremiumBuild()) hideFromBlockedRow = rowCount++;
        hideAllRow = rowCount++;
        collapseAutomaticallyRow = rowCount++;
        makeTransparentRow = rowCount++;
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

    private UsersSelectActivity getUsersSelectActivity() {
        MessagesFilterHelper messagesFilterHelper = MessagesFilterHelper.INSTANCE;

        ArrayList<Long> chatsList = new ArrayList<>();
        ArrayList<String> savedChats = messagesFilterHelper.getArrayList(messagesFilterHelper.getExcludedList());

        for (String chatIdStr : savedChats) {
            long chatId = Long.parseLong(chatIdStr);

            TLRPC.User user = getMessagesController().getUser(chatId);
            TLRPC.Chat chat = getMessagesController().getChat(-chatId);

            if (user != null) {
                chatsList.add(user.id);
            } else if (chat != null) {
                chatsList.add(-chat.id);
            }
        }

        UsersSelectActivity activity = new UsersSelectActivity(true, chatsList, 0);
        activity.asFilterExcludedChats();
        return activity;
    }

}
