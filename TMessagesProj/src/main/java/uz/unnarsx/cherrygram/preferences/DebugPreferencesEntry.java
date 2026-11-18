///**
// * This is the source code of Cherrygram for Android.
// * It is licensed under GNU GPL v. 2 or later.
// * You should have received a copy of the license in this archive (see LICENSE).
// * Please, be respectful and credit the original author if you use this code.
// *
// * Copyright github.com/arsLan4k1390, 2022-2026.
// */
//
//package uz.unnarsx.cherrygram.preferences;
//
//import static org.telegram.messenger.LocaleController.getString;
//
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Build;
//import android.provider.Settings;
//import android.view.View;
//
//import org.telegram.messenger.AndroidUtilities;
//import org.telegram.messenger.R;
//import org.telegram.messenger.SharedConfig;
//import org.telegram.ui.ActionBar.AlertDialog;
//import org.telegram.ui.Components.UItem;
//import org.telegram.ui.Components.UniversalAdapter;
//
//import java.util.ArrayList;
//
//import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
//import uz.unnarsx.cherrygram.core.configs.CherrygramDebugConfig;
//import uz.unnarsx.cherrygram.core.configs.CherrygramFirebaseConfig;
//import uz.unnarsx.cherrygram.core.firebase.FirebaseAnalyticsHelper;
//import uz.unnarsx.cherrygram.core.helpers.DeeplinkHelper;
//import uz.unnarsx.cherrygram.helpers.ui.PopupHelper;
//import uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper;
//
//public class DebugPreferencesEntry extends BaseCGPreferencesEntry {
//
//    private final int toastRpcRow = 1;
//    private final int oldTimeStyleRow = 2;
//    private final int safeStarsRow = 3;
//    private final int safeSurfRow = 4;
//    private final int performanceClassRow = 5;
//    private final int fixCallsNotifRow = 6;
//
//    private final int replacePunctuationRow = 7;
//    private final int editTextFixRow = 8;
//    private final int fixChineseKeyboardsRow = 9;
//    private final int audioSourceRow = 10;
//    private final int sendMaxQualityRow = 11;
//    private final int playGifAsVideoRow = 12;
//    private final int hideTimestampRow = 13;
//
//    @Override
//    protected CharSequence getTitle() {
//        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("debug_preferences_screen");
//        return "Debug // W.I.P";
//    }
//
//    @Override
//    protected String getKey() {
//        return DeeplinkHelper.DeepLinksRepo.CG_Debug;
//    }
//
//    @Override
//    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
//        items.add(UItem.asHeader("Misc"));
//        if (!CherrygramCoreConfig.isStandaloneStableBuild() && !CherrygramCoreConfig.isPlayStoreBuild()) {
//            items.add(SettingsHelper.asSwitchCG(toastRpcRow, "Toast all RPC errors *", "You'll see RPC errors from Telegram's backend as toast messages.")
//                    .setChecked(CherrygramDebugConfig.INSTANCE.getShowRPCErrors())
//            );
//        }
//        items.add(SettingsHelper.asSwitchCG(oldTimeStyleRow, "Default time style in chats *", "Unlike iOS and TDesktop")
//                .setChecked(CherrygramDebugConfig.INSTANCE.getOldTimeStyle())
//                .slug("oldTimeStyle")
//        );
//        if (CherrygramCoreConfig.isDevBuild()) {
//            items.add(SettingsHelper.asSwitchCG(safeStarsRow, "Use SafeStars *")
//                    .setChecked(CherrygramFirebaseConfig.INSTANCE.getAllowSafeStars())
//            );
//            items.add(SettingsHelper.asSwitchCG(safeSurfRow, "Use SafeSurf *")
//                    .setChecked(CherrygramFirebaseConfig.INSTANCE.getAllowSafeSurf())
//            );
//        }
//        items.add(UItem.asButton(performanceClassRow, "Force performance class", SharedConfig.performanceClassName(SharedConfig.getDevicePerformanceClass())));
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
//            items.add(UItem.asButton(fixCallsNotifRow, "Fix calls notification *")
//                    .slug("fixCalls")
//            );
//        }
//        items.add(UItem.asShadow(null));
//
//        items.add(UItem.asHeader(getString(R.string.FilterChats)));
//        items.add(SettingsHelper.asSwitchCG(replacePunctuationRow, "Replace punctuation marks *", "Replace quotation marks and dashes like on TDesktop")
//                .setChecked(CherrygramDebugConfig.INSTANCE.getReplacePunctuationMarks())
//        );
//        items.add(SettingsHelper.asSwitchCG(editTextFixRow, "EditTextSugestionsFix *", "Emojis/formatting disappear when Samsung puts suggestions in edit")
//                .setChecked(CherrygramDebugConfig.INSTANCE.getEditTextSuggestionsFix())
//        );
//        items.add(SettingsHelper.asSwitchCG(fixChineseKeyboardsRow, "Fix Chinese keyboards *", "Fix empty keyboard input")
//                .setChecked(CherrygramDebugConfig.INSTANCE.getEditTextSuggestionsFix())
//                .slug("fixChineseKeyboards")
//        );
//        items.add(UItem.asShadow(null));
//
//        items.add(UItem.asHeader(getString(R.string.CP_Header_Record)));
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            items.add(UItem.asButton(audioSourceRow, "Microphone Audio Source *", getAudioSourceValue())
//                    .slug("audioSource")
//            );
//        }
//        items.add(SettingsHelper.asSwitchCG(sendMaxQualityRow, "Send videos at max quality *", "Max quality will be automatically selected when you send a video")
//                .setChecked(CherrygramDebugConfig.INSTANCE.getSendVideosAtMaxQuality())
//        );
//        items.add(SettingsHelper.asSwitchCG(playGifAsVideoRow, "Play GIFs as Videos *")
//                .setChecked(CherrygramDebugConfig.INSTANCE.getPlayGIFsAsVideos())
//        );
//        items.add(SettingsHelper.asSwitchCG(hideTimestampRow, "Hide video timestamp *", "Saved progress for videos. Return exactly where you left off.")
//                .setChecked(CherrygramDebugConfig.INSTANCE.getHideVideoTimestamp())
//        );
//
//        items.add(UItem.asShadow("* Cherrygram's feature."));
//    }
//
//    @Override
//    protected void onClick(UItem item, View view, int position, float x, float y) {
//        if (item.id == toastRpcRow) {
//            CherrygramDebugConfig.INSTANCE.setShowRPCErrors(!CherrygramDebugConfig.INSTANCE.getShowRPCErrors());
//            SettingsHelper.updateCheckState(view, CherrygramDebugConfig.INSTANCE.getShowRPCErrors());
//
//            showRestartBulletin();
//        } else if (item.id == oldTimeStyleRow) {
//            CherrygramDebugConfig.INSTANCE.setOldTimeStyle(!CherrygramDebugConfig.INSTANCE.getOldTimeStyle());
//            SettingsHelper.updateCheckState(view, CherrygramDebugConfig.INSTANCE.getOldTimeStyle());
//        } else if (item.id == safeStarsRow) {
//            CherrygramFirebaseConfig.INSTANCE.setAllowSafeStars(!CherrygramFirebaseConfig.INSTANCE.getAllowSafeStars());
//            SettingsHelper.updateCheckState(view, CherrygramFirebaseConfig.INSTANCE.getAllowSafeStars());
//        } else if (item.id == safeSurfRow) {
//            CherrygramFirebaseConfig.INSTANCE.setAllowSafeSurf(!CherrygramFirebaseConfig.INSTANCE.getAllowSafeSurf());
//            SettingsHelper.updateCheckState(view, CherrygramFirebaseConfig.INSTANCE.getAllowSafeSurf());
//        } else if (item.id == performanceClassRow) {
//            showPerformanceClassDialog(view);
//        } else if (item.id == fixCallsNotifRow) {
//            openFullScreenIntentSettings();
//        } else if (item.id == replacePunctuationRow) {
//            CherrygramDebugConfig.INSTANCE.setReplacePunctuationMarks(!CherrygramDebugConfig.INSTANCE.getReplacePunctuationMarks());
//            SettingsHelper.updateCheckState(view, CherrygramDebugConfig.INSTANCE.getReplacePunctuationMarks());
//
//            showRestartBulletin();
//        } else if (item.id == editTextFixRow) {
//            CherrygramDebugConfig.INSTANCE.setEditTextSuggestionsFix(!CherrygramDebugConfig.INSTANCE.getEditTextSuggestionsFix());
//            SettingsHelper.updateCheckState(view, CherrygramDebugConfig.INSTANCE.getEditTextSuggestionsFix());
//
//            showRestartBulletin();
//        } else if (item.id == fixChineseKeyboardsRow) {
//            CherrygramDebugConfig.INSTANCE.setFixChineseKeyboards(!CherrygramDebugConfig.INSTANCE.getFixChineseKeyboards());
//            SettingsHelper.updateCheckState(view, CherrygramDebugConfig.INSTANCE.getFixChineseKeyboards());
//
//            showRestartBulletin();
//        } else if (item.id == audioSourceRow) {
//            showAudioSourceDialog(() -> SettingsHelper.updateButtonValue(view, getAudioSourceValue()));
//        } else if (item.id == sendMaxQualityRow) {
//            CherrygramDebugConfig.INSTANCE.setSendVideosAtMaxQuality(!CherrygramDebugConfig.INSTANCE.getSendVideosAtMaxQuality());
//            SettingsHelper.updateCheckState(view, CherrygramDebugConfig.INSTANCE.getSendVideosAtMaxQuality());
//        } else if (item.id == playGifAsVideoRow) {
//            CherrygramDebugConfig.INSTANCE.setPlayGIFsAsVideos(!CherrygramDebugConfig.INSTANCE.getPlayGIFsAsVideos());
//            SettingsHelper.updateCheckState(view, CherrygramDebugConfig.INSTANCE.getPlayGIFsAsVideos());
//        } else if (item.id == hideTimestampRow) {
//            CherrygramDebugConfig.INSTANCE.setHideVideoTimestamp(!CherrygramDebugConfig.INSTANCE.getHideVideoTimestamp());
//            SettingsHelper.updateCheckState(view, CherrygramDebugConfig.INSTANCE.getHideVideoTimestamp());
//        }
//    }
//
//    private void showPerformanceClassDialog(View view) {
//        AlertDialog.Builder builder2 = new AlertDialog.Builder(getParentActivity(), getResourceProvider());
//        builder2.setTitle("Force performance class");
//        int currentClass = SharedConfig.getDevicePerformanceClass();
//        int trueClass = SharedConfig.measureDevicePerformanceClass();
//        builder2.setItems(new CharSequence[]{
//                AndroidUtilities.replaceTags((currentClass == SharedConfig.PERFORMANCE_CLASS_HIGH ? "**HIGH**" : "HIGH") + (trueClass == SharedConfig.PERFORMANCE_CLASS_HIGH ? " (measured)" : "")),
//                AndroidUtilities.replaceTags((currentClass == SharedConfig.PERFORMANCE_CLASS_AVERAGE ? "**AVERAGE**" : "AVERAGE") + (trueClass == SharedConfig.PERFORMANCE_CLASS_AVERAGE ? " (measured)" : "")),
//                AndroidUtilities.replaceTags((currentClass == SharedConfig.PERFORMANCE_CLASS_LOW ? "**LOW**" : "LOW") + (trueClass == SharedConfig.PERFORMANCE_CLASS_LOW ? " (measured)" : ""))
//        }, (dialog2, which2) -> {
//            int newClass = 2 - which2;
//            if (newClass == trueClass) {
//                SharedConfig.overrideDevicePerformanceClass(-1);
//            } else {
//                SharedConfig.overrideDevicePerformanceClass(newClass);
//            }
//
//            SettingsHelper.updateButtonValue(view, SharedConfig.performanceClassName(SharedConfig.getDevicePerformanceClass()));
//
//            showRestartBulletin();
//        });
//        builder2.setNegativeButton(getString(R.string.Cancel), null);
//        builder2.show();
//    }
//
//    private void openFullScreenIntentSettings() {
//        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT);
//        intent.setData(Uri.parse("package:" + getContext().getPackageName()));
//        getParentActivity().startActivity(intent);
//    }
//
//    private void showAudioSourceDialog(Runnable runnable) {
//        ArrayList<String> configStringKeys = new ArrayList<>();
//        ArrayList<Integer> configValues = new ArrayList<>();
//
//        configStringKeys.add("DEFAULT");
//        configValues.add(CherrygramDebugConfig.AUDIO_SOURCE_DEFAULT);
//
//        configStringKeys.add("CAMCORDER");
//        configValues.add(CherrygramDebugConfig.AUDIO_SOURCE_CAMCORDER);
//
//        configStringKeys.add("MIC");
//        configValues.add(CherrygramDebugConfig.AUDIO_SOURCE_MIC);
//
//        configStringKeys.add("REMOTE_SUBMIX");
//        configValues.add(CherrygramDebugConfig.AUDIO_SOURCE_REMOTE_SUBMIX);
//
//        configStringKeys.add("UNPROCESSED");
//        configValues.add(CherrygramDebugConfig.AUDIO_SOURCE_UNPROCESSED);
//
//        configStringKeys.add("VOICE_CALL");
//        configValues.add(CherrygramDebugConfig.AUDIO_SOURCE_VOICE_CALL);
//
//        configStringKeys.add("VOICE_COMMUNICATION");
//        configValues.add(CherrygramDebugConfig.AUDIO_SOURCE_VOICE_COMMUNICATION);
//
//        configStringKeys.add("VOICE_DOWNLINK");
//        configValues.add(CherrygramDebugConfig.AUDIO_SOURCE_VOICE_DOWNLINK);
//
//        configStringKeys.add("VOICE_PERFORMANCE");
//        configValues.add(CherrygramDebugConfig.AUDIO_SOURCE_VOICE_PERFORMANCE);
//
//        configStringKeys.add("VOICE_RECOGNITION");
//        configValues.add(CherrygramDebugConfig.AUDIO_SOURCE_VOICE_RECOGNITION);
//
//        configStringKeys.add("VOICE_UPLINK");
//        configValues.add(CherrygramDebugConfig.AUDIO_SOURCE_VOICE_UPLINK);
//
//        PopupHelper.show(configStringKeys, "Microphone Audio Source *", configValues.indexOf(CherrygramDebugConfig.INSTANCE.getAudioSource()), getContext(), i -> {
//            CherrygramDebugConfig.INSTANCE.setAudioSource(configValues.get(i));
//            if (runnable != null) runnable.run();
//        });
//    }
//
//    private String getAudioSourceValue() {
//        return switch (CherrygramDebugConfig.INSTANCE.getAudioSource()) {
//            case CherrygramDebugConfig.AUDIO_SOURCE_CAMCORDER -> "CAMCORDER";
//            case CherrygramDebugConfig.AUDIO_SOURCE_MIC -> "MIC";
//            case CherrygramDebugConfig.AUDIO_SOURCE_REMOTE_SUBMIX -> "REMOTE_SUBMIX";
//            case CherrygramDebugConfig.AUDIO_SOURCE_UNPROCESSED -> "UNPROCESSED";
//            case CherrygramDebugConfig.AUDIO_SOURCE_VOICE_CALL -> "VOICE_CALL";
//            case CherrygramDebugConfig.AUDIO_SOURCE_VOICE_COMMUNICATION -> "VOICE_COMMUNICATION";
//            case CherrygramDebugConfig.AUDIO_SOURCE_VOICE_DOWNLINK -> "VOICE_DOWNLINK";
//            case CherrygramDebugConfig.AUDIO_SOURCE_VOICE_PERFORMANCE -> "VOICE_PERFORMANCE";
//            case CherrygramDebugConfig.AUDIO_SOURCE_VOICE_RECOGNITION -> "VOICE_RECOGNITION";
//            case CherrygramDebugConfig.AUDIO_SOURCE_VOICE_UPLINK -> "VOICE_UPLINK";
//            default -> "DEFAULT";
//        };
//    }
//
//}
