package org.telegram.messenger;

import android.app.Activity;
import android.view.ViewGroup;

import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.UpdateAvailability;

import org.telegram.messenger.regular.BuildConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.UpdateLayout;
import org.telegram.ui.IUpdateLayout;

import uz.unnarsx.cherrygram.core.CherrygramLogger;
import uz.unnarsx.cherrygram.core.updater.AppUpdateWatcher;
import uz.unnarsx.cherrygram.core.updater.UpdaterBottomSheet;

public class ApplicationLoaderImpl extends ApplicationLoader {
    @Override
    protected String onGetApplicationId() {
        return BuildConfig.APPLICATION_ID;
    }

    /** Cherrygram start */
    @Override
    public IUpdateLayout takeUpdateLayout(Activity activity, ViewGroup sideMenuContainer) {
        return new UpdateLayout(activity, sideMenuContainer);
    }

    @Override
    public boolean showUpdaterBottomSheet(BaseFragment fragment, boolean available, TLRPC.TL_help_appUpdate update) {
        try {
            UpdaterBottomSheet.showAlert(fragment, available, update);
        } catch (Exception e) {
            CherrygramLogger.e(e);
        }
        return true;
    }

    @Override
    public boolean playUpdaterRegisterWatcher() {
        try {
            AppUpdateWatcher.getInstance().start(ApplicationLoader.applicationContext);
        } catch (Exception e) {
            CherrygramLogger.e(e);
        }
        return true;
    }

    @Override
    public void playUpdaterFilterUpdate(TLRPC.TL_help_appUpdate update, Utilities.Callback<TLRPC.TL_help_appUpdate> callback) {
        if (update == null) {
            callback.run(null);
            return;
        }
        AppUpdateManagerFactory.create(applicationContext).getAppUpdateInfo()
                .addOnSuccessListener(info -> {
                    int availability = info.updateAvailability();
                    boolean offeredByPlay = availability == UpdateAvailability.UPDATE_AVAILABLE
                            || availability == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS;
                    callback.run(offeredByPlay ? update : null);
                })
                .addOnFailureListener(e -> callback.run(update));
    }

    @Override
    public boolean playUpdaterCheckPendingDownload() {
        try {
            AppUpdateWatcher.getInstance().checkPendingDownload();
//            AppUpdateWatcher.getInstance().notifyDownloaded();
        } catch (Exception e) {
            CherrygramLogger.e(e);
        }
        return true;
    }
    /** Cherrygram finish */

}
