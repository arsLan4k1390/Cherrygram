/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.misc.widgets;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_account;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.DrawerLayoutContainer;
import org.telegram.ui.ActionBar.INavigationLayout;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.CacheControlActivity;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.LoginActivity;
import org.telegram.ui.PassportActivity;
import org.telegram.ui.PhotoViewer;
import java.util.ArrayList;

public class KaboomWidgetActivity extends Activity implements INavigationLayout.INavigationLayoutDelegate {

    private boolean finished;
    private static ArrayList<BaseFragment> mainFragmentsStack = new ArrayList<>();
    private static ArrayList<BaseFragment> layerFragmentsStack = new ArrayList<>();

    protected INavigationLayout actionBarLayout;
    protected INavigationLayout layersActionBarLayout;
    protected SizeNotifierFrameLayout backgroundTablet;
    protected DrawerLayoutContainer drawerLayoutContainer;

    private Runnable lockRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ApplicationLoader.postInitApplication();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTheme(R.style.Theme_TMessages);
        getWindow().setBackgroundDrawableResource(R.drawable.transparent);

        super.onCreate(savedInstanceState);

        if (SharedConfig.passcodeHash.length() != 0 && SharedConfig.appLocked) {
            SharedConfig.lastPauseTime = (int) (SystemClock.elapsedRealtime() / 1000);
        }

        AndroidUtilities.fillStatusBarHeight(this, false);
        Theme.createDialogsResources(this);
        Theme.createChatResources(this, false);

        actionBarLayout = INavigationLayout.newLayout(this, false);

        drawerLayoutContainer = new DrawerLayoutContainer(this);
        drawerLayoutContainer.setAllowOpenDrawer(false, false);
        setContentView(drawerLayoutContainer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        if (AndroidUtilities.isTablet()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

            RelativeLayout launchLayout = new RelativeLayout(this);
            drawerLayoutContainer.addView(launchLayout);
            FrameLayout.LayoutParams layoutParams1 = (FrameLayout.LayoutParams) launchLayout.getLayoutParams();
            layoutParams1.width = LayoutHelper.MATCH_PARENT;
            layoutParams1.height = LayoutHelper.MATCH_PARENT;
            launchLayout.setLayoutParams(layoutParams1);

            backgroundTablet = new SizeNotifierFrameLayout(this) {
                @Override
                protected boolean isActionBarVisible() {
                    return false;
                }
            };
            backgroundTablet.setOccupyStatusBar(false);
            backgroundTablet.setBackgroundImage(Theme.getCachedWallpaper(), Theme.isWallpaperMotion());
            launchLayout.addView(backgroundTablet, LayoutHelper.createRelative(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

            launchLayout.addView(actionBarLayout.getView(), LayoutHelper.createRelative(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

            FrameLayout shadowTablet = new FrameLayout(this);
            shadowTablet.setBackgroundColor(0x7F000000);
            launchLayout.addView(shadowTablet, LayoutHelper.createRelative(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
            shadowTablet.setOnTouchListener((v, event) -> {
                if (!actionBarLayout.getFragmentStack().isEmpty() && event.getAction() == MotionEvent.ACTION_UP) {
                    float x = event.getX();
                    float y = event.getY();
                    int[] location = new int[2];
                    layersActionBarLayout.getView().getLocationOnScreen(location);
                    int viewX = location[0];
                    int viewY = location[1];

                    if (layersActionBarLayout.checkTransitionAnimation() || x > viewX && x < viewX + layersActionBarLayout.getView().getWidth() && y > viewY && y < viewY + layersActionBarLayout.getView().getHeight()) {
                        return false;
                    } else {
                        if (!layersActionBarLayout.getFragmentStack().isEmpty()) {
                            for (int a = 0; a < layersActionBarLayout.getFragmentStack().size() - 1; a++) {
                                layersActionBarLayout.removeFragmentFromStack(layersActionBarLayout.getFragmentStack().get(0));
                                a--;
                            }
                            layersActionBarLayout.closeLastFragment(true);
                        }
                        return true;
                    }
                }
                return false;
            });

            shadowTablet.setOnClickListener(v -> {

            });

            layersActionBarLayout = INavigationLayout.newLayout(this, false);
            layersActionBarLayout.setRemoveActionBarExtraHeight(true);
            layersActionBarLayout.setBackgroundView(shadowTablet);
            layersActionBarLayout.setUseAlphaAnimations(true);
            layersActionBarLayout.getView().setBackgroundResource(R.drawable.boxshadow);
            launchLayout.addView(layersActionBarLayout.getView(), LayoutHelper.createRelative(530, (AndroidUtilities.isSmallTablet() ? 528 : 700)));
            layersActionBarLayout.setFragmentStack(layerFragmentsStack);
            layersActionBarLayout.setDelegate(this);
            layersActionBarLayout.setDrawerLayoutContainer(drawerLayoutContainer);
        } else {
            RelativeLayout launchLayout = new RelativeLayout(this);
            drawerLayoutContainer.addView(launchLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

            backgroundTablet = new SizeNotifierFrameLayout(this) {
                @Override
                protected boolean isActionBarVisible() {
                    return false;
                }
            };
            backgroundTablet.setOccupyStatusBar(false);
            backgroundTablet.setBackgroundImage(Theme.getCachedWallpaper(), Theme.isWallpaperMotion());
            launchLayout.addView(backgroundTablet, LayoutHelper.createRelative(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

            launchLayout.addView(actionBarLayout.getView(), LayoutHelper.createRelative(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        }

        drawerLayoutContainer.setParentActionBarLayout(actionBarLayout);
        actionBarLayout.setDrawerLayoutContainer(drawerLayoutContainer);
        actionBarLayout.setFragmentStack(mainFragmentsStack);
        actionBarLayout.setDelegate(this);

        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.closeOtherAppActivities, this);

        actionBarLayout.removeAllFragments();
        if (layersActionBarLayout != null) {
            layersActionBarLayout.removeAllFragments();
        }

        handleIntent(getIntent(), UserConfig.selectedAccount, 0);
        needLayout();
    }

    protected boolean handleIntent(final Intent intent, final int intentAccount, int state) {
        if ("org.telegram.passport.AUTHORIZE".equals(intent.getAction())) {
            if (state == 0) {
                int activatedAccountsCount = UserConfig.getActivatedAccountsCount();
                if (activatedAccountsCount == 0) {

                    LoginActivity fragment = new LoginActivity();
                    if (AndroidUtilities.isTablet()) {
                        layersActionBarLayout.addFragmentToStack(fragment);
                    } else {
                        actionBarLayout.addFragmentToStack(fragment);
                    }
                    if (!AndroidUtilities.isTablet()) {
                        backgroundTablet.setVisibility(View.GONE);
                    }
                    actionBarLayout.showLastFragment();
                    if (AndroidUtilities.isTablet()) {
                        layersActionBarLayout.showLastFragment();
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(KaboomWidgetActivity.this);
                    builder.setTitle(LocaleController.getString(R.string.CG_AppName));
                    builder.setMessage(LocaleController.getString(R.string.PleaseLoginPassport));
                    builder.setPositiveButton(LocaleController.getString(R.string.OK), null);
                    builder.show();

                    return true;
                } else if (activatedAccountsCount >= 2) {
                    AlertDialog alertDialog = AlertsCreator.createAccountSelectDialog(this, account -> {
                        if (account != intentAccount) {
                            switchToAccount(account);
                        }
                        handleIntent(intent, account, 1);
                    });
                    alertDialog.show();
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.setOnDismissListener(dialog -> {
                        setResult(RESULT_CANCELED);
                        finish();
                    });
                    return true;
                }
            }

            final long bot_id = intent.getLongExtra("bot_id", intent.getIntExtra("bot_id", 0));
            final String nonce = intent.getStringExtra("nonce");
            final String payload = intent.getStringExtra("payload");
            final TL_account.getAuthorizationForm req = new TL_account.getAuthorizationForm();
            req.bot_id = bot_id;
            req.scope = intent.getStringExtra("scope");
            req.public_key = intent.getStringExtra("public_key");

            if (bot_id == 0 || TextUtils.isEmpty(payload) && TextUtils.isEmpty(nonce) || TextUtils.isEmpty(req.scope) || TextUtils.isEmpty(req.public_key)) {
                finish();
                return false;
            }

            final int[] requestId = {0};

            final AlertDialog progressDialog = new AlertDialog(this, AlertDialog.ALERT_TYPE_SPINNER);
            progressDialog.setOnCancelListener(dialog -> ConnectionsManager.getInstance(intentAccount).cancelRequest(requestId[0], true));

            progressDialog.show();
            requestId[0] = ConnectionsManager.getInstance(intentAccount).sendRequest(req, (response, error) -> {
                final TL_account.authorizationForm authorizationForm = (TL_account.authorizationForm) response;
                if (authorizationForm != null) {
                    TL_account.getPassword req2 = new TL_account.getPassword();
                    requestId[0] = ConnectionsManager.getInstance(intentAccount).sendRequest(req2, (response1, error1) -> AndroidUtilities.runOnUIThread(() -> {
                        try {
                            progressDialog.dismiss();
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                        if (response1 != null) {
                            TL_account.Password accountPassword = (TL_account.Password) response1;
                            MessagesController.getInstance(intentAccount).putUsers(authorizationForm.users, false);
                            PassportActivity fragment = new PassportActivity(PassportActivity.TYPE_PASSWORD, req.bot_id, req.scope, req.public_key, payload, nonce, null, authorizationForm, accountPassword);
                            fragment.setNeedActivityResult(true);
                            if (AndroidUtilities.isTablet()) {
                                layersActionBarLayout.addFragmentToStack(fragment);
                            } else {
                                actionBarLayout.addFragmentToStack(fragment);
                            }
                            if (!AndroidUtilities.isTablet()) {
                                backgroundTablet.setVisibility(View.GONE);
                            }
                            actionBarLayout.showLastFragment();
                            if (AndroidUtilities.isTablet()) {
                                layersActionBarLayout.showLastFragment();
                            }
                        }
                    }));
                } else {
                    AndroidUtilities.runOnUIThread(() -> {
                        try {
                            progressDialog.dismiss();
                            if ("APP_VERSION_OUTDATED".equals(error.text)) {
                                AlertDialog dialog = AlertsCreator.showUpdateAppAlert(KaboomWidgetActivity.this, LocaleController.getString(R.string.UpdateAppAlert), true);
                                if (dialog != null) {
                                    dialog.setOnDismissListener(dialog1 -> {
                                        setResult(RESULT_FIRST_USER, new Intent().putExtra("error", error.text));
                                        finish();
                                    });
                                } else {
                                    setResult(RESULT_FIRST_USER, new Intent().putExtra("error", error.text));
                                    finish();
                                }
                            } else if (("BOT_INVALID".equals(error.text) ||
                                    "PUBLIC_KEY_REQUIRED".equals(error.text) ||
                                    "PUBLIC_KEY_INVALID".equals(error.text)
                                    || "SCOPE_EMPTY".equals(error.text) ||
                                    "PAYLOAD_EMPTY".equals(error.text))) {
                                setResult(RESULT_FIRST_USER, new Intent().putExtra("error", error.text));
                                finish();
                            } else {
                                setResult(RESULT_CANCELED);
                                finish();
                            }
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                    });
                }
            }, ConnectionsManager.RequestFlagFailOnServerErrors | ConnectionsManager.RequestFlagWithoutLogin);
        } else {
            if (AndroidUtilities.isTablet()) {
                if (layersActionBarLayout.getFragmentStack().isEmpty()) {
                    layersActionBarLayout.addFragmentToStack(new CacheControlActivity());
                }
            } else {
                if (actionBarLayout.getFragmentStack().isEmpty()) {
                    actionBarLayout.addFragmentToStack(new CacheControlActivity());
                }
            }
            if (!AndroidUtilities.isTablet()) {
                backgroundTablet.setVisibility(View.GONE);
            }
            actionBarLayout.showLastFragment();
            if (AndroidUtilities.isTablet()) {
                layersActionBarLayout.showLastFragment();
            }
            intent.setAction(null);
        }
        return false;
    }

    public void switchToAccount(int account) {
        if (account == UserConfig.selectedAccount) {
            return;
        }
        ConnectionsManager.getInstance(UserConfig.selectedAccount).setAppPaused(true, false);
        UserConfig.selectedAccount = account;
        UserConfig.getInstance(0).saveConfig(false);
        if (!ApplicationLoader.mainInterfacePaused) {
            ConnectionsManager.getInstance(UserConfig.selectedAccount).setAppPaused(false, false);
        }
    }

    @Override
    public boolean onPreIme() {
        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent, UserConfig.selectedAccount, 0);
    }

    private void onFinish() {
        if (finished) {
            return;
        }
        if (lockRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(lockRunnable);
            lockRunnable = null;
        }
        finished = true;
    }

    public void presentFragment(BaseFragment fragment) {
        actionBarLayout.presentFragment(fragment);
    }

    public boolean presentFragment(final BaseFragment fragment, final boolean removeLast, boolean forceWithoutAnimation) {
        return actionBarLayout.presentFragment(fragment, removeLast, forceWithoutAnimation, true, false);
    }

    public void needLayout() {
        if (AndroidUtilities.isTablet()) {
            RelativeLayout.LayoutParams relativeLayoutParams = (RelativeLayout.LayoutParams) layersActionBarLayout.getView().getLayoutParams();
            relativeLayoutParams.leftMargin = (AndroidUtilities.displaySize.x - relativeLayoutParams.width) / 2;
            int y = AndroidUtilities.statusBarHeight;
            relativeLayoutParams.topMargin = y + (AndroidUtilities.displaySize.y - relativeLayoutParams.height - y) / 2;
            layersActionBarLayout.getView().setLayoutParams(relativeLayoutParams);


            if (!AndroidUtilities.isSmallTablet() || getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                int leftWidth = AndroidUtilities.displaySize.x / 100 * 35;
                if (leftWidth < AndroidUtilities.dp(320)) {
                    leftWidth = AndroidUtilities.dp(320);
                }

                relativeLayoutParams = (RelativeLayout.LayoutParams) actionBarLayout.getView().getLayoutParams();
                relativeLayoutParams.width = leftWidth;
                relativeLayoutParams.height = LayoutHelper.MATCH_PARENT;
                actionBarLayout.getView().setLayoutParams(relativeLayoutParams);

                if (AndroidUtilities.isSmallTablet() && actionBarLayout.getFragmentStack().size() == 2) {
                    BaseFragment chatFragment = actionBarLayout.getFragmentStack().get(1);
                    chatFragment.onPause();
                    actionBarLayout.getFragmentStack().remove(1);
                    actionBarLayout.showLastFragment();
                }
            } else {
                relativeLayoutParams = (RelativeLayout.LayoutParams) actionBarLayout.getView().getLayoutParams();
                relativeLayoutParams.width = LayoutHelper.MATCH_PARENT;
                relativeLayoutParams.height = LayoutHelper.MATCH_PARENT;
                actionBarLayout.getView().setLayoutParams(relativeLayoutParams);
            }
        }
    }

    public void fixLayout() {
        if (!AndroidUtilities.isTablet()) {
            return;
        }
        if (actionBarLayout == null) {
            return;
        }
        actionBarLayout.getView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                needLayout();
                if (actionBarLayout != null) {
                    actionBarLayout.getView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        actionBarLayout.onPause();
        if (AndroidUtilities.isTablet()) {
            layersActionBarLayout.onPause();
        }
        ApplicationLoader.externalInterfacePaused = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onFinish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        actionBarLayout.onResume();
        if (AndroidUtilities.isTablet()) {
            layersActionBarLayout.onResume();
        }
        ApplicationLoader.externalInterfacePaused = false;
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        AndroidUtilities.checkDisplaySize(this, newConfig);
        super.onConfigurationChanged(newConfig);
        fixLayout();
    }

    @Override
    public void onBackPressed() {
        if (PhotoViewer.getInstance().isVisible()) {
            PhotoViewer.getInstance().closePhoto(true, false);
        } else if (drawerLayoutContainer.isDrawerOpened()) {
            drawerLayoutContainer.closeDrawer(false);
        } else if (AndroidUtilities.isTablet()) {
            if (layersActionBarLayout.getView().getVisibility() == View.VISIBLE) {
                layersActionBarLayout.onBackPressed();
            } else {
                actionBarLayout.onBackPressed();
            }
        } else {
            actionBarLayout.onBackPressed();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        actionBarLayout.onLowMemory();
        if (AndroidUtilities.isTablet()) {
            layersActionBarLayout.onLowMemory();
        }
    }

    @Override
    public boolean needPresentFragment(BaseFragment fragment, boolean removeLast, boolean forceWithoutAnimation, INavigationLayout layout) {
        return true;
    }

    @Override
    public boolean needAddFragmentToStack(BaseFragment fragment, INavigationLayout layout) {
        return true;
    }

    @Override
    public boolean needCloseLastFragment(INavigationLayout layout) {
        if (AndroidUtilities.isTablet()) {
            if (layout == actionBarLayout && layout.getFragmentStack().size() <= 1) {
                onFinish();
                finish();
                return false;
            } else if (layout == layersActionBarLayout && actionBarLayout.getFragmentStack().isEmpty() && layersActionBarLayout.getFragmentStack().size() == 1) {
                onFinish();
                finish();
                return false;
            }
        } else {
            if (layout.getFragmentStack().size() <= 1) {
                onFinish();
                finish();
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRebuildAllFragments(INavigationLayout layout, boolean last) {
        if (AndroidUtilities.isTablet()) {
            if (layout == layersActionBarLayout) {
                actionBarLayout.rebuildAllFragmentViews(last, last);
            }
        }
    }
}
