package org.telegram.ui.Components;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.IntentSender;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.IUpdateLayout;

import uz.unnarsx.cherrygram.core.CherrygramLogger;
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper;

public class UpdateLayout extends IUpdateLayout {

    private static final int PLAY_UPDATE_REQUEST_CODE = 1001;

    private FrameLayout updateLayout;
    private RadialProgress2 updateLayoutIcon;
    private AnimatedTextView updateTextView;
    private AnimatedTextView.AnimatedTextDrawable updateSizeTextView;

    private final Activity activity;
    private final ViewGroup sideMenuContainer;

    private int currentAccount;

    private AppUpdateManager appUpdateManager;
    private InstallStateUpdatedListener installStateUpdatedListener;
    private boolean gpUpdateDownloaded = false;
    private boolean gpUpdateDownloading = false;
    private int gpDownloadProgress = 0;

    public UpdateLayout(Activity activity, ViewGroup sideMenuContainer) {
        super(activity, sideMenuContainer);
        this.activity = activity;
        this.sideMenuContainer = sideMenuContainer;
    }

    public void createUpdateUI(int currentAccount) {
        if (sideMenuContainer == null || updateLayout != null) {
            return;
        }
        this.currentAccount = currentAccount;

        updateLayout = new FrameLayout(activity);
        updateLayout.setVisibility(View.INVISIBLE);
        updateLayout.setTranslationY(dp(44));
        updateLayout.setBackground(Theme.getSelectorDrawable(0x40ffffff, false));
        sideMenuContainer.addView(updateLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 44, Gravity.LEFT | Gravity.BOTTOM));
        updateLayout.setOnClickListener(v -> {
            if (gpUpdateDownloaded) {
                completeGooglePlayUpdate();
            } else if (!gpUpdateDownloading) {
                startGooglePlayUpdate();
            }
        });

        updateTextView = new AnimatedTextView(activity, true, true, true) {
            @Override
            protected void onDraw(Canvas canvas) {
                updateSizeTextView.setBounds(0, 0, getMeasuredWidth() - dp(20), getMeasuredHeight());
                updateSizeTextView.draw(canvas);

                canvas.save();
                canvas.translate(dp(15), 0);
                super.onDraw(canvas);
                canvas.translate((getMeasuredWidth() - width()) / 2f - dp(30), dp(11));
                updateLayoutIcon.draw(canvas);
                canvas.restore();
            }

            @Override
            protected boolean verifyDrawable(@NonNull Drawable who) {
                return super.verifyDrawable(who) || who == updateSizeTextView;
            }
        };
        updateTextView.setTextSize(dp(15));
        updateTextView.setTypeface(AndroidUtilities.bold());
        updateTextView.setTextColor(0xffffffff);
        updateTextView.setGravity(Gravity.CENTER);
        updateLayout.addView(updateTextView, LayoutHelper.createFrameMatchParent());
        updateTextView.setText(LocaleController.getString(R.string.AppUpdateBeta), false);

        updateLayoutIcon = new RadialProgress2(updateTextView);
        updateLayoutIcon.setColors(0xffffffff, 0xffffffff, Theme.getColor(Theme.key_featuredStickers_addButton), Theme.getColor(Theme.key_featuredStickers_addButton));
        updateLayoutIcon.setProgressRect(0, 0, dp(22), dp(22));
        updateLayoutIcon.setCircleRadius(dp(11));
        updateLayoutIcon.setAsMini();

        updateSizeTextView = new AnimatedTextView.AnimatedTextDrawable(true, true, true);
        updateSizeTextView.setCallback(updateTextView);
        updateSizeTextView.setTextSize(dp(14));
        updateSizeTextView.setTypeface(AndroidUtilities.bold());
        updateSizeTextView.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        updateSizeTextView.setTextColor(0xccffffff);

        registerGooglePlayUpdateListener();
    }

    public void updateAppUpdateViews(int currentAccount, boolean animated) {
        if (sideMenuContainer == null) {
            return;
        }
        this.currentAccount = currentAccount;

        if (SharedConfig.pendingAppUpdate != null) {
            createUpdateUI(currentAccount);

            if (gpUpdateDownloaded) {
                updateLayoutIcon.setIcon(MediaActionDrawable.ICON_UPDATE, true, animated);
                setUpdateText(LocaleController.getString(R.string.AppUpdateNow), animated);
            } else if (gpUpdateDownloading) {
                updateLayoutIcon.setIcon(MediaActionDrawable.ICON_CANCEL, true, animated);
                updateLayoutIcon.setProgress(gpDownloadProgress / 100f, true);
                setUpdateText(LocaleController.formatString(R.string.AppUpdateDownloading, gpDownloadProgress), animated);
            } else {
                updateLayoutIcon.setIcon(MediaActionDrawable.ICON_DOWNLOAD, true, animated);
                setUpdateText(LocaleController.getString(R.string.AppUpdate).replace("Telegram", LocaleController.getString(R.string.CG_AppName)), animated);
            }
            updateSizeTextView.setText(null, animated);

            if (updateLayout.getTag() != null) {
                return;
            }
            updateLayout.setVisibility(View.VISIBLE);
            updateLayout.setTag(1);
            if (animated) {
                updateLayout.animate().translationY(0).setInterpolator(CubicBezierInterpolator.EASE_OUT).setListener(null).setDuration(180).start();
            } else {
                updateLayout.setTranslationY(0);
            }
        } else {
            if (updateLayout == null || updateLayout.getTag() == null) {
                return;
            }
            updateLayout.setTag(null);
            if (animated) {
                updateLayout.animate().translationY(dp(44)).setInterpolator(CubicBezierInterpolator.EASE_OUT).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (updateLayout.getTag() == null) {
                            updateLayout.setVisibility(View.GONE);
                        }
                    }
                }).setDuration(180).start();
            } else {
                updateLayout.setTranslationY(dp(44));
                updateLayout.setVisibility(View.GONE);
            }
        }
    }

    // ----------------------------------------------------------------------------------------
    // Google Play In-App Update
    // ----------------------------------------------------------------------------------------

    private void registerGooglePlayUpdateListener() {
        if (appUpdateManager != null) {
            return;
        }
        appUpdateManager = AppUpdateManagerFactory.create(activity);

        installStateUpdatedListener = state -> {
            switch (state.installStatus()) {
                case InstallStatus.DOWNLOADING:
                    gpUpdateDownloading = true;
                    gpUpdateDownloaded = false;
                    gpDownloadProgress = state.totalBytesToDownload() > 0
                            ? (int) (state.bytesDownloaded() * 100L / state.totalBytesToDownload())
                            : 0;
                    updateAppUpdateViews(currentAccount, true);
                    break;

                case InstallStatus.DOWNLOADED:
                    gpUpdateDownloading = false;
                    gpUpdateDownloaded = true;
                    updateAppUpdateViews(currentAccount, true);
                    break;

                case InstallStatus.INSTALLED:
                    gpUpdateDownloading = false;
                    gpUpdateDownloaded = false;
                    unregisterGooglePlayUpdateListener();
                    break;

                case InstallStatus.FAILED:
                case InstallStatus.CANCELED:
                    gpUpdateDownloading = false;
                    gpUpdateDownloaded = false;
                    updateAppUpdateViews(currentAccount, true);
                    break;

                case InstallStatus.PENDING:
                case InstallStatus.INSTALLING:
                case InstallStatus.REQUIRES_UI_INTENT:
                case InstallStatus.UNKNOWN:
                default:
                    break;
            }
        };
        appUpdateManager.registerListener(installStateUpdatedListener);

        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
            gpUpdateDownloaded = appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED;
            gpUpdateDownloading = appUpdateInfo.installStatus() == InstallStatus.DOWNLOADING;
            updateAppUpdateViews(currentAccount, true);
        });
    }

    private void startGooglePlayUpdate() {
        if (appUpdateManager == null) {
            return;
        }
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() != UpdateAvailability.UPDATE_AVAILABLE) {
                return;
            }
            if (!appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                return;
            }
            try {
                appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        activity,
                        AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
                        PLAY_UPDATE_REQUEST_CODE
                );
            } catch (IntentSender.SendIntentException e) {
                CherrygramLogger.e(e);
            }
        }).addOnFailureListener(CherrygramLogger::e);
    }

    private void completeGooglePlayUpdate() {
        if (appUpdateManager != null) {
            appUpdateManager.completeUpdate();
        } else {
            AppRestartHelper.restartApp(ApplicationLoader.applicationContext);
        }
    }

    private void unregisterGooglePlayUpdateListener() {
        if (appUpdateManager != null && installStateUpdatedListener != null) {
            appUpdateManager.unregisterListener(installStateUpdatedListener);
            installStateUpdatedListener = null;
        }
    }

    /** Вызвать при уничтожении drawer'а/side-меню, чтобы не держать listener живым дольше нужного. */
    public void destroy() {
        unregisterGooglePlayUpdateListener();
        appUpdateManager = null;
    }

    private void setUpdateText(String text, boolean animate) {
        updateTextView.setText(text, animate);
    }

}
