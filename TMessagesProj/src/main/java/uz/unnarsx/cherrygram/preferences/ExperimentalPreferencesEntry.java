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
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.R;
import org.telegram.ui.Components.TableView;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;
import java.util.Locale;

import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.core.configs.CherrygramExperimentalConfig;
import uz.unnarsx.cherrygram.core.firebase.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.core.memory.MemoryMonitor;
import uz.unnarsx.cherrygram.core.memory.MemoryStressTest;
import uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper;

public class ExperimentalPreferencesEntry extends BaseCGPreferencesEntry {

    private final int cleanupHeapRow = 1;
    private final int restartPopupRow = 2;

    private final int testOOMNotificationRow = 3;

    private static final long REFRESH_INTERVAL_MS = CherrygramExperimentalConfig.INSTANCE.getOomHandlerPopup() ? 1000L : 200L;

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (CherrygramExperimentalConfig.INSTANCE.getUse_CG_OOMHandler()) {
                if (listView != null && listView.adapter != null) {
                    updateRows(false);
                }
                AndroidUtilities.runOnUIThread(this, REFRESH_INTERVAL_MS);
            }
        }
    };

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("experimental_preferences_screen");
        return getString(R.string.EP_Category_Experimental);
    }

    @Override
    public void onResume() {
        super.onResume();
        AndroidUtilities.runOnUIThread(refreshRunnable, REFRESH_INTERVAL_MS);
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
        if (CherrygramExperimentalConfig.INSTANCE.getUse_CG_OOMHandler()) {
            final TableView tableView = new TableView(getContext(), getResourceProvider());
            tableView.setPadding(dp(15), dp(10), dp(15), dp(10));

            MemoryMonitor monitor = MemoryMonitor.getInstance();
            if (monitor != null) {
                tableView.addFullRow("Heap info", true, true, 15, false);

                tableView.addRow("Max heap available", monitor.getMaxMemoryMb() + " MB");
                tableView.addRow("Used heap", monitor.getUsedMemoryMb() + " MB");
                tableView.addRow("Used heap (%)", String.format(Locale.US, "%.1f%%", monitor.getCurrentUsagePercent()));

                items.add(SettingsHelper.asCustomWithBackground(tableView));
                items.add(UItem.asShadow(null));

                items.add(UItem.asButton(cleanupHeapRow, "Cleanup heap"));
                items.add(UItem.asShadow(null));

                items.add(SettingsHelper.asSwitchCG(
                            restartPopupRow,
                            getString(R.string.CG_LowMemoryWarning)/*,
                            "Show restart popup when heap usage is > 95% to prevent crashes"*/
                        )
                        .setChecked(CherrygramExperimentalConfig.INSTANCE.getOomHandlerPopup())
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
                    );
                    items.add(UItem.asShadow(null));
                }
            }

            if (CherrygramCoreConfig.isDevBuild()) {
                items.add(UItem.asHeader("For dev"));
                items.add(UItem.asButton(testOOMNotificationRow, "Test OOM notification"));
            }
        }
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == cleanupHeapRow) {
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
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }

}
