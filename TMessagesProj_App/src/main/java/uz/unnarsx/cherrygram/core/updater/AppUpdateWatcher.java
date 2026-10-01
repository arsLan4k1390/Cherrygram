/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.updater;

import android.content.Context;

import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.InstallStatus;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.LaunchActivity;

import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper;
import uz.unnarsx.cherrygram.core.ui.CGBulletinCreator;

public final class AppUpdateWatcher {

    private static volatile AppUpdateWatcher instance;

    public static AppUpdateWatcher getInstance() {
        if (instance == null) {
            synchronized (AppUpdateWatcher.class) {
                if (instance == null) {
                    instance = new AppUpdateWatcher();
                }
            }
        }
        return instance;
    }

    private AppUpdateManager appUpdateManager;
    private final InstallStateUpdatedListener listener = state -> {
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            notifyDownloaded();
        }
    };
    private boolean started = false;

    private AppUpdateWatcher() {}

    /** Вызвать один раз за жизнь процесса — например, из ApplicationLoader.postInitApplication(). */
    public void start(Context appContext) {
        if (started) {
            return;
        }
        started = true;
        appUpdateManager = AppUpdateManagerFactory.create(appContext);
        appUpdateManager.registerListener(listener);
        checkPendingDownload();
    }

    /** Вызывать из LaunchActivity.onResume() — ловит случай, когда процесс убили посреди загрузки. */
    public void checkPendingDownload() {
        if (appUpdateManager == null) {
            return;
        }
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(info -> {
            if (info.installStatus() == InstallStatus.DOWNLOADED) {
                notifyDownloaded();
            }
        });
    }

    public void completeUpdate() {
        if (appUpdateManager != null) {
            appUpdateManager.completeUpdate();
        } else {
            AppRestartHelper.restartApp(ApplicationLoader.applicationContext);
        }
    }

    public void notifyDownloaded() {
        BaseFragment fragment = LaunchActivity.getSafeLastFragment();
        if (fragment == null || fragment.getParentActivity() == null) {
            return;
        }
        if (fragment.getParentActivity().isFinishing()) {
            return;
        }

        CGBulletinCreator.INSTANCE.createAppUpdateRestartDialog(fragment.getContext(), this::completeUpdate);
    }

}
