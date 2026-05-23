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

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BotWebViewVibrationEffect;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.OutlineEditText;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import org.telegram.ui.UsersSelectActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import uz.unnarsx.cherrygram.chats.filters.MessagesFilterHelper;
import uz.unnarsx.cherrygram.core.CherrygramLogger;
import uz.unnarsx.cherrygram.core.configs.CherrygramMessagesConfig;
import uz.unnarsx.cherrygram.core.crashlytics.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.core.ui.CGBulletinCreator;
import uz.unnarsx.cherrygram.donates.DonatesManager;
import uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper;

public class MessageFiltersPreferencesEntry extends UniversalFragment {

    private final int enableFilterRow = 1;
    private final int filterWordsRow = 2;
    private final int detectTranslitRow = 3;
    private final int exactWordMatchRow = 4;
    private final int exclusionsRow = 5;

    private final int filterFromBlockedRow = 6;
    private final int detectEntitiesRow = 7;

    private final int hideAllRow = 9;
    private final int collapseAutomaticallyRow = 10;
    private final int makeTransparentRow = 11;

    private OutlineEditText outlineEditText;

    private static final int done_button = 1;
    private ActionBarMenuItem doneButton;

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("filters_preferences_screen");
        return getString(R.string.CP_Message_Filtering);
    }

    @Override
    public View createView(Context context) {
        setMD3(true);
        setGilroy(true);

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

        return super.createView(context);
    }

    @Override
    public void onFragmentDestroy() {
        checkDone(true);
        super.onFragmentDestroy();
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        boolean requireDonate = !DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace();

        items.add(UItem.asHeader(getString(R.string.General)));
        items.add(SettingsHelper.asSwitchCG(enableFilterRow, getString(R.string.CP_Message_Filtering_Filter), getString(R.string.CP_Message_Filtering_Filter_Desc))
                .setChecked(CherrygramMessagesConfig.INSTANCE.getEnableMsgFilters())
                .setLocked(requireDonate)
        );
        outlineEditText = new OutlineEditText(getContext(), getResourceProvider());
        outlineEditText.setPadding(dp(16), dp(12), dp(16), dp(12));
        outlineEditText.setEnabled(CherrygramMessagesConfig.INSTANCE.getEnableMsgFilters(), null);
        outlineEditText.getEditText().setEnabled(CherrygramMessagesConfig.INSTANCE.getEnableMsgFilters() && !requireDonate);
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
        outlineEditText.getEditText().setText(CherrygramMessagesConfig.INSTANCE.getMsgFiltersElements());
        outlineEditText.setMinimumHeight(200);
        outlineEditText.getEditText().setPadding(dp(16), dp(12), dp(16), dp(12));
        items.add(SettingsHelper.asCustomWithBackground(outlineEditText));
        items.add(UItem.asShadow(getString(R.string.CP_Message_Filtering_Field_Desc)));

        items.add(SettingsHelper.asSwitchCG(detectTranslitRow, getString(R.string.CP_Message_Filtering_Translit), getString(R.string.CP_Message_Filtering_Translit_Desc))
                .setChecked(CherrygramMessagesConfig.INSTANCE.getMsgFiltersDetectTranslit())
                .setEnabled(CherrygramMessagesConfig.INSTANCE.getEnableMsgFilters())
        );
        items.add(SettingsHelper.asSwitchCG(exactWordMatchRow, getString(R.string.CP_Message_Filtering_Exact_Words), getString(R.string.CP_Message_Filtering_Exact_Words_Desc))
                .setChecked(CherrygramMessagesConfig.INSTANCE.getMsgFiltersMatchExactWord())
                .setEnabled(CherrygramMessagesConfig.INSTANCE.getEnableMsgFilters())
        );
        items.add(UItem.asButton(exclusionsRow, R.drawable._menu_stream_comments_off_24, getString(R.string.CP_Message_Filtering_Exclusions), String.valueOf(MessagesFilterHelper.INSTANCE.getExcludedChatsCount()))
                .setEnabled(CherrygramMessagesConfig.INSTANCE.getEnableMsgFilters())
        );
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.LocalMiscellaneousCache)));
        items.add(SettingsHelper.asSwitchCG(filterFromBlockedRow, getString(R.string.CP_Message_Filtering_FilterBlocked), getString(R.string.CP_Message_Filtering_FilterBlockedDesc))
                .setChecked(CherrygramMessagesConfig.INSTANCE.getMsgFiltersHideFromBlocked())
                .setEnabled(CherrygramMessagesConfig.INSTANCE.getEnableMsgFilters())
        );
        items.add(SettingsHelper.asSwitchCG(detectEntitiesRow, getString(R.string.CP_Message_Filtering_Entities), getString(R.string.CP_Message_Filtering_EntitiesDesc))
                .setChecked(CherrygramMessagesConfig.INSTANCE.getMsgFiltersDetectEntities())
                .setEnabled(CherrygramMessagesConfig.INSTANCE.getEnableMsgFilters())
        );
        items.add(UItem.asShadow(null));

        items.add(SettingsHelper.asSwitchCG(hideAllRow, getString(R.string.CP_Message_Filtering_HideAll), getString(R.string.CP_Message_Filtering_HideAllDesc))
                .setChecked(CherrygramMessagesConfig.INSTANCE.getMsgFiltersHideAllUnderSpoiler())
                .setEnabled(CherrygramMessagesConfig.INSTANCE.getEnableMsgFilters())
        );
        items.add(SettingsHelper.asSwitchCG(collapseAutomaticallyRow, getString(R.string.CP_Message_Filtering_Collapse), getString(R.string.CP_Message_Filtering_Collapse_Desc))
                .setChecked(CherrygramMessagesConfig.INSTANCE.getMsgFiltersCollapseAutomatically())
                .setEnabled(
                        CherrygramMessagesConfig.INSTANCE.getEnableMsgFilters() && (CherrygramMessagesConfig.INSTANCE.getMsgFiltersHideFromBlocked() || CherrygramMessagesConfig.INSTANCE.getMsgFiltersHideAllUnderSpoiler())
                )
        );
        items.add(SettingsHelper.asSwitchCG(makeTransparentRow, getString(R.string.CP_Message_Filtering_Transparent), getString(R.string.CP_Message_Filtering_Transparent_Desc))
                .setChecked(CherrygramMessagesConfig.INSTANCE.getMsgFilterTransparentMsg())
                .setEnabled(CherrygramMessagesConfig.INSTANCE.getEnableMsgFilters())
        );
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        boolean requireDonate = !DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace();

        var holder = listView.findViewHolderForAdapterPosition(position);
        if (holder == null || !listView.adapter.isEnabled(holder)) {
            return;
        }
        if (requireDonate) {
            AndroidUtilities.shakeViewSpring(view);
            BotWebViewVibrationEffect.APP_ERROR.vibrate();
            CGBulletinCreator.INSTANCE.createRequireDonateBulletin(this);
            return;
        }

        if (item.id == enableFilterRow) {
            CherrygramMessagesConfig.INSTANCE.setEnableMsgFilters(!CherrygramMessagesConfig.INSTANCE.getEnableMsgFilters());
            SettingsHelper.updateCheckState(view, CherrygramMessagesConfig.INSTANCE.getEnableMsgFilters());

            listView.adapter.update(true);
        } else if (item.id == detectTranslitRow) {
            CherrygramMessagesConfig.INSTANCE.setMsgFiltersDetectTranslit(!CherrygramMessagesConfig.INSTANCE.getMsgFiltersDetectTranslit());
            SettingsHelper.updateCheckState(view, CherrygramMessagesConfig.INSTANCE.getMsgFiltersDetectTranslit());
        } else if (item.id == exactWordMatchRow) {
            CherrygramMessagesConfig.INSTANCE.setMsgFiltersMatchExactWord(!CherrygramMessagesConfig.INSTANCE.getMsgFiltersMatchExactWord());
            SettingsHelper.updateCheckState(view, CherrygramMessagesConfig.INSTANCE.getMsgFiltersMatchExactWord());
        } else if (item.id == exclusionsRow) {
            AndroidUtilities.runOnUIThread(() -> {
                UsersSelectActivity activity = getUsersSelectActivity();
                activity.setDelegate((ids, unused) -> {
                    MessagesFilterHelper messagesFilterHelper = MessagesFilterHelper.INSTANCE;

                    Set<Long> chatIds = new HashSet<>(ids);
                    Set<String> excludedChats = new HashSet<>(messagesFilterHelper.getArrayList(messagesFilterHelper.getExcludedList()));

                    CherrygramLogger.d(() -> "old excluded chats array: " + excludedChats);
                    excludedChats.clear();

                    if (!chatIds.isEmpty()) {
                        for (Long id : chatIds) {
                            if (DialogObject.isUserDialog(id) || DialogObject.isChatDialog(id)) {
                                excludedChats.add(String.valueOf(id));
                            }
                        }
                    }

                    messagesFilterHelper.saveArrayList(new ArrayList<>(excludedChats), messagesFilterHelper.getExcludedList());
                    CherrygramLogger.d(() -> "new excluded chats array: " + excludedChats);

                    SettingsHelper.updateButtonValue(view, String.valueOf(MessagesFilterHelper.INSTANCE.getExcludedChatsCount()));
                });
                presentFragment(activity);
            }, 300);
        } else if (item.id == filterFromBlockedRow) {
            CherrygramMessagesConfig.INSTANCE.setMsgFiltersHideFromBlocked(!CherrygramMessagesConfig.INSTANCE.getMsgFiltersHideFromBlocked());
            SettingsHelper.updateCheckState(view, CherrygramMessagesConfig.INSTANCE.getMsgFiltersHideFromBlocked());
        } else if (item.id == detectEntitiesRow) {
            CherrygramMessagesConfig.INSTANCE.setMsgFiltersDetectEntities(!CherrygramMessagesConfig.INSTANCE.getMsgFiltersDetectEntities());
            SettingsHelper.updateCheckState(view, CherrygramMessagesConfig.INSTANCE.getMsgFiltersDetectEntities());
        } else if (item.id == hideAllRow) {
            CherrygramMessagesConfig.INSTANCE.setMsgFiltersHideAllUnderSpoiler(!CherrygramMessagesConfig.INSTANCE.getMsgFiltersHideAllUnderSpoiler());
            SettingsHelper.updateCheckState(view, CherrygramMessagesConfig.INSTANCE.getMsgFiltersHideAllUnderSpoiler());
        } else if (item.id == collapseAutomaticallyRow) {
            CherrygramMessagesConfig.INSTANCE.setMsgFiltersCollapseAutomatically(!CherrygramMessagesConfig.INSTANCE.getMsgFiltersCollapseAutomatically());
            SettingsHelper.updateCheckState(view, CherrygramMessagesConfig.INSTANCE.getMsgFiltersCollapseAutomatically());
        } else if (item.id == makeTransparentRow) {
            CherrygramMessagesConfig.INSTANCE.setMsgFilterTransparentMsg(!CherrygramMessagesConfig.INSTANCE.getMsgFilterTransparentMsg());
            SettingsHelper.updateCheckState(view, CherrygramMessagesConfig.INSTANCE.getMsgFilterTransparentMsg());
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }

    private boolean hasChanges() {
        return (
                !TextUtils.equals(CherrygramMessagesConfig.INSTANCE.getMsgFiltersElements(), outlineEditText.getEditText().getText().toString())
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

        CherrygramMessagesConfig.INSTANCE.setMsgFiltersElements(
                outlineEditText.getEditText().getText().toString()
        );

        AndroidUtilities.runOnUIThread(() -> AndroidUtilities.hideKeyboard(listView), 50);

        outlineEditText.getEditText().clearFocus();

        if (CherrygramMessagesConfig.INSTANCE.getMsgFiltersHideFromBlocked()) {
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