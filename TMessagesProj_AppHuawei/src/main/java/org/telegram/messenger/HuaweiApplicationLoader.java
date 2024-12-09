package org.telegram.messenger;

import android.content.Context;

import com.google.firebase.messaging.FirebaseMessaging;
import com.huawei.hms.push.HmsMessaging;

import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.huawei.BuildConfig;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.LaunchActivity;

import uz.unnarsx.cherrygram.updater.UpdaterBottomSheet;
import uz.unnarsx.cherrygram.updater.UpdaterUtils;

public class HuaweiApplicationLoader extends ApplicationLoader {
    @Override
    protected boolean isHuaweiBuild() {
        return true;
    }

    @Override
    protected PushListenerController.IPushListenerServiceProvider onCreatePushProvider() {
        if (PushListenerController.GooglePushListenerServiceProvider.INSTANCE.hasServices()) {
            HmsMessaging.getInstance(this).setAutoInitEnabled(false);
            FirebaseMessaging.getInstance().setAutoInitEnabled(true);
            return PushListenerController.GooglePushListenerServiceProvider.INSTANCE;
        }
        HmsMessaging.getInstance(this).setAutoInitEnabled(true);
        FirebaseMessaging.getInstance().setAutoInitEnabled(false);
        return HuaweiPushListenerProvider.INSTANCE;
    }

    @Override
    protected ILocationServiceProvider onCreateLocationServiceProvider() {
        if (PushListenerController.GooglePushListenerServiceProvider.INSTANCE.hasServices()) {
            return new GoogleLocationProvider();
        }
        return new HuaweiLocationProvider();
    }

    @Override
    protected IMapsProvider onCreateMapsProvider() {
        if (PushListenerController.GooglePushListenerServiceProvider.INSTANCE.hasServices()) {
            return new GoogleMapsProvider();
        }
        return new HuaweiMapsProvider();
    }

    @Override
    protected String onGetApplicationId() {
        return BuildConfig.APPLICATION_ID;
    }

    @Override
    protected boolean isStandalone() {
        return true;
    }

    @Override
    public boolean checkCgUpdates(BaseFragment fragment) {
        try {
            UpdaterUtils.checkUpdates(fragment, false);
        } catch (Exception e) {
            FileLog.e(e);
        }
        return true;
    }

    @Override
    public boolean checkCgUpdatesManually(BaseFragment fragment, LaunchActivity launchActivity, Browser.Progress progress) {
        UpdaterUtils.checkUpdates(fragment, true, () -> launchActivity.showBulletin(factory -> factory.createErrorBulletin(LocaleController.getString(R.string.UP_Not_Found))), null, progress);
        return true;
    }

    @Override
    public boolean showUpdaterSettings(BaseFragment fragment) {
        try {
            UpdaterBottomSheet.showAlert(fragment, false, null);
        } catch (Exception e) {
            FileLog.e(e);
        }
        return true;
    }

}
