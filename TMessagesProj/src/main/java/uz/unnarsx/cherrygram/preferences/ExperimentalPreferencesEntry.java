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

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.Components.TableView;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.core.configs.CherrygramDebugConfig;
import uz.unnarsx.cherrygram.core.configs.CherrygramExperimentalConfig;
import uz.unnarsx.cherrygram.core.configs.CherrygramFirebaseConfig;
import uz.unnarsx.cherrygram.core.firebase.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.core.helpers.DeeplinkHelper;
import uz.unnarsx.cherrygram.core.memory.MemoryMonitor;
import uz.unnarsx.cherrygram.core.memory.MemoryStressTest;
import uz.unnarsx.cherrygram.helpers.ui.PopupHelper;
import uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper;

public class ExperimentalPreferencesEntry extends BaseCGPreferencesEntry {

    private final int oomSectionRow = 1;
    private final int cleanupHeapRow = 2;
    private final int restartPopupRow = 3;
    private final int testOOMNotificationRow = 4;
    private boolean expandedOomSectionRow = false;

    private final int toastRpcRow = 5;
    private final int oldTimeStyleRow = 6;
    private final int safeStarsRow = 7;
    private final int safeSurfRow = 8;
    private final int performanceClassRow = 9;
    private final int fixCallsNotifRow = 10;

    private final int replacePunctuationRow = 11;
    private final int editTextFixRow = 12;
    private final int fixChineseKeyboardsRow = 13;
    private final int audioSourceRow = 14;
    private final int sendMaxQualityRow = 15;
    private final int playGifAsVideoRow = 16;
    private final int hideTimestampRow = 17;

    private static final Set<String> OOM_SECTION_SLUG = Set.of(
            "cleanupHeap",
            "oomPopup",
            "oomThreshold",
            "testOOMNotification"
    );

    private static final long REFRESH_INTERVAL_MS = CherrygramExperimentalConfig.INSTANCE.getOomHandlerPopup() ? 1000L : 200L;

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (CherrygramExperimentalConfig.INSTANCE.getUse_CG_OOMHandler()) {
                if (expandedOomSectionRow && listView != null && listView.adapter != null) {
                    updateRows(false);
                }
                AndroidUtilities.runOnUIThread(this, REFRESH_INTERVAL_MS);
            }
        }
    };

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("experimental_preferences_screen");
        return getString(R.string.EP_Category_Experimental) + " // W.I.P";
    }

    @Override
    protected String getKey() {
        return DeeplinkHelper.DeepLinksRepo.CG_Experimental;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (CherrygramExperimentalConfig.INSTANCE.getUse_CG_OOMHandler()) {
            AndroidUtilities.runOnUIThread(refreshRunnable, REFRESH_INTERVAL_MS);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        AndroidUtilities.cancelRunOnUIThread(refreshRunnable);
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        AndroidUtilities.cancelRunOnUIThread(refreshRunnable);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        MemoryMonitor monitor = CherrygramExperimentalConfig.INSTANCE.getUse_CG_OOMHandler() ? MemoryMonitor.getInstance() : null;
        if (monitor != null) {
            items.add(
                    SettingsHelper.asExpandableSwitch(
                            oomSectionRow,
                            R.drawable.msg2_data,
                            "OOM Handler",
                            " "
                    )
                    .setCollapsed(!expandedOomSectionRow)
                    .setClickCallback(v -> {
                        expandedOomSectionRow = !expandedOomSectionRow;
                        updateRows(true);
                    })
                    .hideCheckbox(true)
                    .slug("oomHandler")
            );
            if (expandedOomSectionRow) {
                items.add(UItem.asShadow(null));
                final TableView tableView = new TableView(getContext(), getResourceProvider());
                tableView.setPadding(dp(15), dp(10), dp(15), dp(10));

                tableView.addFullRow("Heap info", true, true, 15, false);
                tableView.addRow("Max heap available", monitor.getMaxMemoryMb() + " MB");
                tableView.addRow("Used heap", monitor.getUsedMemoryMb() + " MB");
                tableView.addRow("Used heap (%)", String.format(Locale.US, "%.1f%%", monitor.getCurrentUsagePercent()));

                items.add(SettingsHelper.asCustomWithBackground(tableView));
                items.add(UItem.asButton(cleanupHeapRow, "Cleanup heap")
                        .slug("cleanupHeap")
                );
                items.add(SettingsHelper.asSwitchCG(restartPopupRow, getString(R.string.CG_LowMemoryWarning))
                        .setChecked(CherrygramExperimentalConfig.INSTANCE.getOomHandlerPopup())
                        .slug("oomPopup")
                );
                items.add(UItem.asShadow(null));

                if (CherrygramExperimentalConfig.INSTANCE.getOomHandlerPopup()) {
                    items.add(UItem.asHeader("Warn when app memory usage exceeds"));
                    items.add(
                            UItem.asIntSlideView(
                                    1,
                                    CherrygramCoreConfig.isDevBuild() ? 10 : 85,
                                    (int) CherrygramExperimentalConfig.INSTANCE.getOomHandlerPopupThreshold(),
                                    97,
                                    val -> val + "%",
                                    val -> {
                                        monitor.updateWarningThresholdRatio(val);
                                        CherrygramExperimentalConfig.INSTANCE.setOomHandlerPopupThreshold(val);
                                    }
                            )
                            .slug("oomThreshold")
                    );
                    items.add(UItem.asShadow(null));
                }

                if (CherrygramCoreConfig.isDevBuild()) {
                    items.add(UItem.asHeader("For dev"));
                    items.add(UItem.asButton(testOOMNotificationRow, "Test OOM notification")
                            .slug("testOOMNotification")
                    );
                    items.add(UItem.asShadow(null));
                }
            } else {
                items.add(UItem.asShadow(null));
            }
        }

        items.add(UItem.asHeader("Misc"));
        if (!CherrygramCoreConfig.isStandaloneStableBuild() && !CherrygramCoreConfig.isPlayStoreBuild()) {
            items.add(SettingsHelper.asSwitchCG(toastRpcRow, "Toast all RPC errors *", "You'll see RPC errors from Telegram's backend as toast messages.")
                    .setChecked(CherrygramDebugConfig.INSTANCE.getShowRPCErrors())
                    .slug("toastRpcErrors")
            );
        }
        items.add(SettingsHelper.asSwitchCG(oldTimeStyleRow, "Default time style in chats *", "Unlike iOS and TDesktop")
                .setChecked(CherrygramDebugConfig.INSTANCE.getOldTimeStyle())
                .slug("oldTimeStyle")
        );
        if (CherrygramCoreConfig.isDevBuild()) {
            items.add(SettingsHelper.asSwitchCG(safeStarsRow, "Use SafeStars *")
                    .setChecked(CherrygramFirebaseConfig.INSTANCE.getAllowSafeStars())
                    .slug("safeStars")
            );
            items.add(SettingsHelper.asSwitchCG(safeSurfRow, "Use SafeSurf *")
                    .setChecked(CherrygramFirebaseConfig.INSTANCE.getAllowSafeSurf())
                    .slug("safeSurf")
            );
        }
        items.add(UItem.asButton(performanceClassRow, "Force performance class", SharedConfig.performanceClassName(SharedConfig.getDevicePerformanceClass()))
                .slug("performanceClass")
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            items.add(UItem.asButton(fixCallsNotifRow, "Fix calls notification *")
                    .slug("fixCalls")
            );
        }
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.FilterChats)));
        items.add(SettingsHelper.asSwitchCG(replacePunctuationRow, "Replace punctuation marks *", "Replace quotation marks and dashes like on TDesktop")
                .setChecked(CherrygramDebugConfig.INSTANCE.getReplacePunctuationMarks())
                .slug("replacePunctuation")
        );
        items.add(SettingsHelper.asSwitchCG(editTextFixRow, "EditTextSugestionsFix *", "Emojis/formatting disappear when Samsung puts suggestions in edit")
                .setChecked(CherrygramDebugConfig.INSTANCE.getEditTextSuggestionsFix())
                .slug("editTextSuggestionsFix")
        );
        items.add(SettingsHelper.asSwitchCG(fixChineseKeyboardsRow, "Fix Chinese keyboards *", "Fix empty keyboard input")
                .setChecked(CherrygramDebugConfig.INSTANCE.getFixChineseKeyboards())
                .slug("fixChineseKeyboards")
        );
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.CP_Header_Record)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            items.add(UItem.asButton(audioSourceRow, "Microphone Audio Source *", getAudioSourceValue())
                    .slug("audioSource")
            );
        }
        items.add(SettingsHelper.asSwitchCG(sendMaxQualityRow, "Send videos at max quality *", "Max quality will be automatically selected when you send a video")
                .setChecked(CherrygramDebugConfig.INSTANCE.getSendVideosAtMaxQuality())
                .slug("sendVideosMaxQuality")
        );
        items.add(SettingsHelper.asSwitchCG(playGifAsVideoRow, "Play GIFs as Videos *")
                .setChecked(CherrygramDebugConfig.INSTANCE.getPlayGIFsAsVideos())
                .slug("playGifsAsVideos")
        );
        items.add(SettingsHelper.asSwitchCG(hideTimestampRow, "Hide video timestamp *", "Saved progress for videos. Return exactly where you left off.")
                .setChecked(CherrygramDebugConfig.INSTANCE.getHideVideoTimestamp())
                .slug("hideVideoTimestamp")
        );

        items.add(UItem.asShadow("* Cherrygram's feature."));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == oomSectionRow) {
            expandedOomSectionRow = !expandedOomSectionRow;
            item.collapsed = !item.collapsed;

            updateRows(true);
        } else if (item.id == cleanupHeapRow) {
            ImageLoader.getInstance().clearMemory();

            AndroidUtilities.runOnUIThread(() -> {
                if (LaunchActivity.instance != null) {
                    LaunchActivity.instance.onLowMemory();
                }
            });

            System.gc();
        } else if (item.id == restartPopupRow) {
            CherrygramExperimentalConfig.INSTANCE.setOomHandlerPopup(!CherrygramExperimentalConfig.INSTANCE.getOomHandlerPopup());
            SettingsHelper.updateCheckState(view, CherrygramExperimentalConfig.INSTANCE.getOomHandlerPopup());

            if (listView != null && listView.adapter != null) {
                updateRows(true);
            }
        } else if (item.id == testOOMNotificationRow) {
            MemoryStressTest.fillHeapToTriggerMonitor();
        } else if (item.id == toastRpcRow) {
            CherrygramDebugConfig.INSTANCE.setShowRPCErrors(!CherrygramDebugConfig.INSTANCE.getShowRPCErrors());
            SettingsHelper.updateCheckState(view, CherrygramDebugConfig.INSTANCE.getShowRPCErrors());

            showRestartBulletin();
        } else if (item.id == oldTimeStyleRow) {
            CherrygramDebugConfig.INSTANCE.setOldTimeStyle(!CherrygramDebugConfig.INSTANCE.getOldTimeStyle());
            SettingsHelper.updateCheckState(view, CherrygramDebugConfig.INSTANCE.getOldTimeStyle());
        } else if (item.id == safeStarsRow) {
            CherrygramFirebaseConfig.INSTANCE.setAllowSafeStars(!CherrygramFirebaseConfig.INSTANCE.getAllowSafeStars());
            SettingsHelper.updateCheckState(view, CherrygramFirebaseConfig.INSTANCE.getAllowSafeStars());
        } else if (item.id == safeSurfRow) {
            CherrygramFirebaseConfig.INSTANCE.setAllowSafeSurf(!CherrygramFirebaseConfig.INSTANCE.getAllowSafeSurf());
            SettingsHelper.updateCheckState(view, CherrygramFirebaseConfig.INSTANCE.getAllowSafeSurf());
        } else if (item.id == performanceClassRow) {
            showPerformanceClassDialog(view);
        } else if (item.id == fixCallsNotifRow) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT);
            intent.setData(Uri.parse("package:" + getContext().getPackageName()));
            getParentActivity().startActivity(intent);
        } else if (item.id == replacePunctuationRow) {
            CherrygramDebugConfig.INSTANCE.setReplacePunctuationMarks(!CherrygramDebugConfig.INSTANCE.getReplacePunctuationMarks());
            SettingsHelper.updateCheckState(view, CherrygramDebugConfig.INSTANCE.getReplacePunctuationMarks());

            showRestartBulletin();
        } else if (item.id == editTextFixRow) {
            CherrygramDebugConfig.INSTANCE.setEditTextSuggestionsFix(!CherrygramDebugConfig.INSTANCE.getEditTextSuggestionsFix());
            SettingsHelper.updateCheckState(view, CherrygramDebugConfig.INSTANCE.getEditTextSuggestionsFix());

            showRestartBulletin();
        } else if (item.id == fixChineseKeyboardsRow) {
            CherrygramDebugConfig.INSTANCE.setFixChineseKeyboards(!CherrygramDebugConfig.INSTANCE.getFixChineseKeyboards());
            SettingsHelper.updateCheckState(view, CherrygramDebugConfig.INSTANCE.getFixChineseKeyboards());

            showRestartBulletin();
        } else if (item.id == audioSourceRow) {
            showAudioSourceDialog(() -> SettingsHelper.updateButtonValue(view, getAudioSourceValue()));
        } else if (item.id == sendMaxQualityRow) {
            CherrygramDebugConfig.INSTANCE.setSendVideosAtMaxQuality(!CherrygramDebugConfig.INSTANCE.getSendVideosAtMaxQuality());
            SettingsHelper.updateCheckState(view, CherrygramDebugConfig.INSTANCE.getSendVideosAtMaxQuality());
        } else if (item.id == playGifAsVideoRow) {
            CherrygramDebugConfig.INSTANCE.setPlayGIFsAsVideos(!CherrygramDebugConfig.INSTANCE.getPlayGIFsAsVideos());
            SettingsHelper.updateCheckState(view, CherrygramDebugConfig.INSTANCE.getPlayGIFsAsVideos());
        } else if (item.id == hideTimestampRow) {
            CherrygramDebugConfig.INSTANCE.setHideVideoTimestamp(!CherrygramDebugConfig.INSTANCE.getHideVideoTimestamp());
            SettingsHelper.updateCheckState(view, CherrygramDebugConfig.INSTANCE.getHideVideoTimestamp());
        }
    }

    @Override
    public void scrollToRow(String slug, Runnable unknown) {
        if (OOM_SECTION_SLUG.contains(slug) && !expandedOomSectionRow && CherrygramExperimentalConfig.INSTANCE.getUse_CG_OOMHandler()) {
            expandedOomSectionRow = true;
            updateRows(true);
        }
        super.scrollToRow(slug, unknown);
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

            SettingsHelper.updateButtonValue(view, SharedConfig.performanceClassName(SharedConfig.getDevicePerformanceClass()));

            showRestartBulletin();
        });
        builder2.setNegativeButton(getString(R.string.Cancel), null);
        builder2.show();
    }

    private void showAudioSourceDialog(Runnable runnable) {
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

        PopupHelper.show(configStringKeys, "Microphone Audio Source *", configValues.indexOf(CherrygramDebugConfig.INSTANCE.getAudioSource()), getContext(), i -> {
            CherrygramDebugConfig.INSTANCE.setAudioSource(configValues.get(i));
            if (runnable != null) runnable.run();
        });
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

}