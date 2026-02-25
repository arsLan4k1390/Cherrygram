/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.preferences;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatThemeController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.Paint.PersistColorPalette;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import org.telegram.ui.RestrictedLanguagesSelectActivity;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.core.configs.CherrygramDebugConfig;
import uz.unnarsx.cherrygram.core.crashlytics.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.core.ui.CGBulletinCreator;
import uz.unnarsx.cherrygram.helpers.ui.PopupHelper;

public class DebugPreferencesEntry extends UniversalFragment {

    private final int toastRpcRow = 1;
    private final int oldTimeStyleRow = 2;
    private final int safeStarsRow = 3;
    private final int performanceClassRow = 4;
    private final int fixCallsNotifRow = 5;

    private final int newBlurRow = 6;

    private final int chatPreviewFixRow = 21;
    private final int forceForumTabsRow = 7;
    private final int replacePunctuationRow = 8;
    private final int editTextFixRow = 9;
    private final int audioSourceRow = 10;
    private final int sendMaxQualityRow = 11;
    private final int playGifAsVideoRow = 12;
    private final int hideTimestampRow = 13;
    private final int resetDialogsRow = 15;
    private final int clearMediaCacheRow = 16;
    private final int readAllDialogsRow = 17;

    private final int importContactsRow = 18;
    private final int reloadContactsRow = 19;
    private final int resetContactsRow = 20;

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("debug_preferences_screen");
        return "Debug // WIP";
    }

    @Override
    public View createView(Context context) {
        setMD3(true);
        return super.createView(context);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader("Misc"));

        if (!CherrygramCoreConfig.isStandaloneStableBuild() && !CherrygramCoreConfig.isPlayStoreBuild()) {
            items.add(
                    UItem.asButtonCheck(
                            toastRpcRow,
                            "Toast all RPC errors *",
                            "You'll see RPC errors from Telegram's backend as toast messages."
                    ).setChecked(CherrygramDebugConfig.INSTANCE.getShowRPCErrors())
            );
        }

        items.add(
                UItem.asButtonCheck(
                        oldTimeStyleRow,
                        "Default time style in chats *",
                        "Unlike iOS and TDesktop"
                ).setChecked(CherrygramDebugConfig.INSTANCE.getOldTimeStyle())
        );

        items.add(
                UItem.asCheck(
                        safeStarsRow,
                        "Use SafeStars *"
                ).setChecked(CherrygramCoreConfig.INSTANCE.getAllowSafeStars())
        );

        items.add(
                UItem.asButton(
                        performanceClassRow,
                        "Force performance class",
                        SharedConfig.performanceClassName(
                                SharedConfig.getDevicePerformanceClass()
                        )
                )
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            items.add(
                    UItem.asButton(
                            fixCallsNotifRow,
                            "Fix calls notification *"
                    )
            );
        }

        items.add(UItem.asShadow(null));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            items.add(UItem.asHeader(getString(R.string.AP_Header_Appearance)));

            items.add(
                    UItem.asCheck(
                            newBlurRow,
                            "New blur (GPU)"
                    ).setChecked(SharedConfig.useNewBlur)
            );

            items.add(UItem.asShadow(null));
        }

        items.add(UItem.asHeader(getString(R.string.CP_Header_Chats)));

        items.add(
                UItem.asCheck(
                        chatPreviewFixRow,
                        "Chat preview fix *"
                ).setChecked(CherrygramDebugConfig.INSTANCE.getChatPreviewFix())
        );

        items.add(
                UItem.asCheck(
                        forceForumTabsRow,
                        "Force Forum Tabs"
                ).setChecked(SharedConfig.forceForumTabs)
        );

        items.add(
                UItem.asButtonCheck(
                        replacePunctuationRow,
                        "Replace punctuation marks *",
                        "Replace quotation marks and dashes like on TDesktop"
                ).setChecked(CherrygramDebugConfig.INSTANCE.getReplacePunctuationMarks())
        );

        items.add(
                UItem.asButtonCheck(
                        editTextFixRow,
                        "EditTextSugestionsFix *",
                        "Emojis/formatting disappear when Samsung puts suggestions in edit"
                ).setChecked(CherrygramDebugConfig.INSTANCE.getEditTextSuggestionsFix())
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            items.add(
                    UItem.asButton(
                            audioSourceRow,
                            "Microphone Audio Source *",
                            getAudioSourceValue()
                    )
            );
        }

        items.add(
                UItem.asButtonCheck(
                        sendMaxQualityRow,
                        "Send videos at max quality *",
                        "Max quality will be automatically selected when you send a video"
                ).setChecked(CherrygramDebugConfig.INSTANCE.getSendVideosAtMaxQuality())
        );

        items.add(
                UItem.asCheck(
                        playGifAsVideoRow,
                        "Play GIFs as Videos *"
                ).setChecked(CherrygramDebugConfig.INSTANCE.getPlayGIFsAsVideos())
        );

        items.add(
                UItem.asButtonCheck(
                        hideTimestampRow,
                        "Hide video timestamp *",
                        "Saved progress for videos. Return exactly where you left off."
                ).setChecked(CherrygramDebugConfig.INSTANCE.getHideVideoTimestamp())
        );

        items.add(UItem.asButton(resetDialogsRow, 0, getString(R.string.DebugMenuResetDialogs)));
        items.add(UItem.asButton(clearMediaCacheRow, 0, getString(R.string.DebugMenuClearMediaCache)));
        items.add(UItem.asButton(readAllDialogsRow, 0, getString(R.string.DebugMenuReadAllDialogs)));

        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.Contacts)));

        items.add(UItem.asButton(importContactsRow, 0, getString(R.string.DebugMenuImportContacts)));
        items.add(UItem.asButton(reloadContactsRow, 0, getString(R.string.DebugMenuReloadContacts)));
        items.add(UItem.asButton(resetContactsRow, 0, getString(R.string.DebugMenuResetContacts)));

        items.add(UItem.asShadow("* Cherrygram's feature."));
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == toastRpcRow) {
            CherrygramDebugConfig.INSTANCE.setShowRPCErrors(!CherrygramDebugConfig.INSTANCE.getShowRPCErrors());
            ((NotificationsCheckCell) view).setChecked(CherrygramDebugConfig.INSTANCE.getShowRPCErrors());

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        } else if (item.id == oldTimeStyleRow) {
            CherrygramDebugConfig.INSTANCE.setOldTimeStyle(!CherrygramDebugConfig.INSTANCE.getOldTimeStyle());
            ((NotificationsCheckCell) view).setChecked(CherrygramDebugConfig.INSTANCE.getOldTimeStyle());
        } else if (item.id == safeStarsRow) {
            CherrygramCoreConfig.INSTANCE.setAllowSafeStars(!CherrygramCoreConfig.INSTANCE.getAllowSafeStars());
            ((TextCheckCell) view).setChecked(CherrygramCoreConfig.INSTANCE.getAllowSafeStars());
        } else if (item.id == performanceClassRow) {
            showPerformanceClassDialog(view);
        } else if (item.id == fixCallsNotifRow) {
            openFullScreenIntentSettings();
        } else if (item.id == newBlurRow) {
            SharedConfig.toggleUseNewBlur();
            ((TextCheckCell) view).setChecked(SharedConfig.useNewBlur);
        } else if (item.id == chatPreviewFixRow) {
            CherrygramDebugConfig.INSTANCE.setChatPreviewFix(!CherrygramDebugConfig.INSTANCE.getChatPreviewFix());
            ((TextCheckCell) view).setChecked(CherrygramDebugConfig.INSTANCE.getChatPreviewFix());

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        } else if (item.id == forceForumTabsRow) {
            SharedConfig.toggleForceForumTabs();
            ((TextCheckCell) view).setChecked(SharedConfig.forceForumTabs);
        } else if (item.id == replacePunctuationRow) {
            CherrygramDebugConfig.INSTANCE.setReplacePunctuationMarks(!CherrygramDebugConfig.INSTANCE.getReplacePunctuationMarks());
            ((NotificationsCheckCell) view).setChecked(CherrygramDebugConfig.INSTANCE.getReplacePunctuationMarks());

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        } else if (item.id == editTextFixRow) {
            CherrygramDebugConfig.INSTANCE.setEditTextSuggestionsFix(!CherrygramDebugConfig.INSTANCE.getEditTextSuggestionsFix());
            ((NotificationsCheckCell) view).setChecked(CherrygramDebugConfig.INSTANCE.getEditTextSuggestionsFix());

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        } else if (item.id == audioSourceRow) {
            showAudioSourceDialog(view);
        } else if (item.id == sendMaxQualityRow) {
            CherrygramDebugConfig.INSTANCE.setSendVideosAtMaxQuality(!CherrygramDebugConfig.INSTANCE.getSendVideosAtMaxQuality());
            ((NotificationsCheckCell) view).setChecked(CherrygramDebugConfig.INSTANCE.getSendVideosAtMaxQuality());
        } else if (item.id == playGifAsVideoRow) {
            CherrygramDebugConfig.INSTANCE.setPlayGIFsAsVideos(!CherrygramDebugConfig.INSTANCE.getPlayGIFsAsVideos());
            ((TextCheckCell) view).setChecked(CherrygramDebugConfig.INSTANCE.getPlayGIFsAsVideos());
        } else if (item.id == hideTimestampRow) {
            CherrygramDebugConfig.INSTANCE.setHideVideoTimestamp(!CherrygramDebugConfig.INSTANCE.getHideVideoTimestamp());
            ((NotificationsCheckCell) view).setChecked(CherrygramDebugConfig.INSTANCE.getHideVideoTimestamp());
        } else if (item.id == resetDialogsRow) {
            getMessagesController().forceResetDialogs();

            CGBulletinCreator.INSTANCE.createDebugSuccessBulletin(this);
        } else if (item.id == clearMediaCacheRow) {
            clearMediaCache();
        } else if (item.id == readAllDialogsRow) {
            getMessagesStorage().readAllDialogs(-1);

            CGBulletinCreator.INSTANCE.createDebugSuccessBulletin(this);
        } else if (item.id == importContactsRow) {
            getUserConfig().syncContacts = true;
            getUserConfig().saveConfig(false);
            getContactsController().forceImportContacts();

            CGBulletinCreator.INSTANCE.createDebugSuccessBulletin(this);
        } else if (item.id == reloadContactsRow) {
            getContactsController().loadContacts(false, 0);

            CGBulletinCreator.INSTANCE.createDebugSuccessBulletin(this);
        } else if (item.id == resetContactsRow) {
            getContactsController().resetImportedContacts();

            CGBulletinCreator.INSTANCE.createDebugSuccessBulletin(this);
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }

    private void showPerformanceClassDialog(View view) {
        AlertDialog.Builder builder2 = new AlertDialog.Builder(getParentActivity(), getResourceProvider());
        builder2.setTitle("Force performance class");
        int currentClass = SharedConfig.getDevicePerformanceClass();
        int trueClass = SharedConfig.measureDevicePerformanceClass();
        builder2.setItems(new CharSequence[]{
                AndroidUtilities.replaceTags((currentClass == SharedConfig.PERFORMANCE_CLASS_HIGH ? "**HIGH**" : "HIGH") + (trueClass == SharedConfig.PERFORMANCE_CLASS_HIGH ? " (measured)" : "")),
                AndroidUtilities.replaceTags((currentClass == SharedConfig.PERFORMANCE_CLASS_AVERAGE ? "**AVERAGE**" : "AVERAGE") + (trueClass == SharedConfig.PERFORMANCE_CLASS_AVERAGE ? " (measured)" : "")),
                AndroidUtilities.replaceTags((currentClass == SharedConfig.PERFORMANCE_CLASS_LOW ? "**LOW**" : "LOW") + (trueClass == SharedConfig.PERFORMANCE_CLASS_LOW ? " (measured)" : ""))
        }, (dialog2, which2) -> {
            int newClass = 2 - which2;
            if (newClass == trueClass) {
                SharedConfig.overrideDevicePerformanceClass(-1);
            } else {
                SharedConfig.overrideDevicePerformanceClass(newClass);
            }

            ((TextCell) view).setValue(SharedConfig.performanceClassName(SharedConfig.getDevicePerformanceClass()), true);

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        });
        builder2.setNegativeButton(getString(R.string.Cancel), null);
        builder2.show();
    }

    private void openFullScreenIntentSettings() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT);
        intent.setData(Uri.parse("package:" + getContext().getPackageName()));
        getParentActivity().startActivity(intent);
    }

    private void showAudioSourceDialog(View view) {
        ArrayList<String> configStringKeys = new ArrayList<>();
        ArrayList<Integer> configValues = new ArrayList<>();

        configStringKeys.add("DEFAULT");
        configValues.add(CherrygramDebugConfig.AUDIO_SOURCE_DEFAULT);

        configStringKeys.add("CAMCORDER");
        configValues.add(CherrygramDebugConfig.AUDIO_SOURCE_CAMCORDER);

        configStringKeys.add("MIC");
        configValues.add(CherrygramDebugConfig.AUDIO_SOURCE_MIC);

        configStringKeys.add("REMOTE_SUBMIX");
        configValues.add(CherrygramDebugConfig.AUDIO_SOURCE_REMOTE_SUBMIX);

        configStringKeys.add("UNPROCESSED");
        configValues.add(CherrygramDebugConfig.AUDIO_SOURCE_UNPROCESSED);

        configStringKeys.add("VOICE_CALL");
        configValues.add(CherrygramDebugConfig.AUDIO_SOURCE_VOICE_CALL);

        configStringKeys.add("VOICE_COMMUNICATION");
        configValues.add(CherrygramDebugConfig.AUDIO_SOURCE_VOICE_COMMUNICATION);

        configStringKeys.add("VOICE_DOWNLINK");
        configValues.add(CherrygramDebugConfig.AUDIO_SOURCE_VOICE_DOWNLINK);

        configStringKeys.add("VOICE_PERFORMANCE");
        configValues.add(CherrygramDebugConfig.AUDIO_SOURCE_VOICE_PERFORMANCE);

        configStringKeys.add("VOICE_RECOGNITION");
        configValues.add(CherrygramDebugConfig.AUDIO_SOURCE_VOICE_RECOGNITION);

        configStringKeys.add("VOICE_UPLINK");
        configValues.add(CherrygramDebugConfig.AUDIO_SOURCE_VOICE_UPLINK);

        PopupHelper.show(configStringKeys, "Microphone Audio Source *", configValues.indexOf(CherrygramDebugConfig.INSTANCE.getAudioSource()), getContext(),
                i -> {
                    CherrygramDebugConfig.INSTANCE.setAudioSource(configValues.get(i));
                    ((TextCell) view).setValue(getAudioSourceValue(), true);
                }
        );
    }

    private String getAudioSourceValue() {
        return switch (CherrygramDebugConfig.INSTANCE.getAudioSource()) {
            case CherrygramDebugConfig.AUDIO_SOURCE_CAMCORDER -> "CAMCORDER";
            case CherrygramDebugConfig.AUDIO_SOURCE_MIC -> "MIC";
            case CherrygramDebugConfig.AUDIO_SOURCE_REMOTE_SUBMIX -> "REMOTE_SUBMIX";
            case CherrygramDebugConfig.AUDIO_SOURCE_UNPROCESSED -> "UNPROCESSED";
            case CherrygramDebugConfig.AUDIO_SOURCE_VOICE_CALL -> "VOICE_CALL";
            case CherrygramDebugConfig.AUDIO_SOURCE_VOICE_COMMUNICATION -> "VOICE_COMMUNICATION";
            case CherrygramDebugConfig.AUDIO_SOURCE_VOICE_DOWNLINK -> "VOICE_DOWNLINK";
            case CherrygramDebugConfig.AUDIO_SOURCE_VOICE_PERFORMANCE -> "VOICE_PERFORMANCE";
            case CherrygramDebugConfig.AUDIO_SOURCE_VOICE_RECOGNITION -> "VOICE_RECOGNITION";
            case CherrygramDebugConfig.AUDIO_SOURCE_VOICE_UPLINK -> "VOICE_UPLINK";
            default -> "DEFAULT";
        };
    }

    private void clearMediaCache() {
        getMessagesStorage().clearSentMedia();
        SharedConfig.setNoSoundHintShowed(false);
        SharedPreferences.Editor editor = MessagesController.getGlobalMainSettings().edit();
        editor.remove("archivehint").remove("proximityhint").remove("archivehint_l").remove("searchpostsnew").remove("speedhint").remove("gifhint").remove("reminderhint").remove("soundHint").remove("themehint").remove("bganimationhint").remove("filterhint").remove("n_0").remove("storyprvhint").remove("storyhint").remove("storyhint2").remove("storydualhint").remove("storysvddualhint").remove("stories_camera").remove("dualcam").remove("dualmatrix").remove("dual_available").remove("archivehint").remove("askNotificationsAfter").remove("askNotificationsDuration").remove("viewoncehint").remove("voicepausehint").remove("taptostorysoundhint").remove("nothanos").remove("voiceoncehint").remove("savedhint").remove("savedsearchhint").remove("savedsearchtaghint").remove("groupEmojiPackHintShown").remove("newppsms").remove("monetizationadshint").remove("seekSpeedHintShowed").remove("unsupport_video/av01").remove("channelgifthint").remove("statusgiftpage").remove("multistorieshint").remove("channelsuggesthint").remove("trimvoicehint").remove("taptostoryhighlighthint").apply();
        MessagesController.getEmojiSettings(currentAccount).edit().remove("featured_hidden").remove("emoji_featured_hidden").apply();
        SharedConfig.textSelectionHintShows = 0;
        SharedConfig.lockRecordAudioVideoHint = 0;
        SharedConfig.stickersReorderingHintUsed = false;
        SharedConfig.forwardingOptionsHintShown = false;
        SharedConfig.replyingOptionsHintShown = false;
        SharedConfig.messageSeenHintCount = 3;
        SharedConfig.emojiInteractionsHintCount = 3;
        SharedConfig.dayNightThemeSwitchHintCount = 3;
        SharedConfig.fastScrollHintCount = 3;
        SharedConfig.stealthModeSendMessageConfirm = 2;
        SharedConfig.updateStealthModeSendMessageConfirm(2);
        SharedConfig.setStoriesReactionsLongPressHintUsed(false);
        SharedConfig.setStoriesIntroShown(false);
        SharedConfig.setMultipleReactionsPromoShowed(false);
        ChatThemeController.getInstance(currentAccount).clearCache();
        getNotificationCenter().postNotificationName(NotificationCenter.newSuggestionsAvailable);
        RestrictedLanguagesSelectActivity.cleanup();
        PersistColorPalette.getInstance(currentAccount).cleanup();
        SharedPreferences prefs = getMessagesController().getMainSettings();
        editor = prefs.edit();
        editor.remove("peerColors").remove("profilePeerColors").remove("boostingappearance").remove("bizbothint").remove("movecaptionhint");
        for (String key : prefs.getAll().keySet()) {
            if (key.contains("show_gift_for_") || key.contains("bdayhint_") || key.contains("bdayanim_") || key.startsWith("ask_paid_message_") || key.startsWith("topicssidetabs")) {
                editor.remove(key);
            }
        }
        editor.apply();
        editor = MessagesController.getNotificationsSettings(currentAccount).edit();
        for (String key : MessagesController.getNotificationsSettings(currentAccount).getAll().keySet()) {
            if (key.startsWith("dialog_bar_botver")) {
                editor.remove(key);
            }
        }
        editor.apply();

        CGBulletinCreator.INSTANCE.createDebugSuccessBulletin(this);
    }

}
