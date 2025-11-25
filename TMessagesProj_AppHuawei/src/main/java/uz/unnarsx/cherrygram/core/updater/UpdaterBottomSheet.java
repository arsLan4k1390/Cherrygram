/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.core.updater;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.graphics.Canvas;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.ColoredImageSpan;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.StickerImageView;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;

import java.util.Objects;

import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.misc.Constants;
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper;

public class UpdaterBottomSheet extends BottomSheet {

    private BaseFragment fragment;
    private Theme.ResourcesProvider resourcesProvider;
    private ButtonWithCounterView checkUpdatesButton;
    private FrameLayout buttonsView;

    private boolean isForce = false;
    private boolean downloadButtonClicked = false;

    public UpdaterBottomSheet(Context context, Theme.ResourcesProvider resourcesProvider, boolean available, UpdaterUtils.Update update) {
        super(context, false, resourcesProvider);
        setOpenNoDelay(true);

        fixNavigationBar();

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        FrameLayout header = new FrameLayout(context);
        linearLayout.addView(header, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 21, 10, 0, 10));

        if (available) {
            setCanDismissWithSwipe(false);
            setCanDismissWithTouchOutside(false);

            StickerImageView imageView = new StickerImageView(context, currentAccount);
            imageView.setStickerPackName("HotCherry");
            imageView.setStickerNum(33);
            imageView.getImageReceiver().setAutoRepeat(1);
            header.addView(imageView, LayoutHelper.createFrame(60, 60, Gravity.LEFT | Gravity.CENTER_VERTICAL));

            SimpleTextView nameView = new SimpleTextView(context);
            nameView.setTextSize(20);
            nameView.setTypeface(AndroidUtilities.bold());
            nameView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider));
            nameView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            nameView.setText(getString(R.string.UP_UpdateAvailable));
            header.addView(nameView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 30, Gravity.LEFT, 75, 5, 0, 0));

            AnimatedTextView timeView = new AnimatedTextView(context, true, true, false);
            timeView.setAnimationProperties(0.7f, 0, 450, CubicBezierInterpolator.EASE_OUT_QUINT);
            timeView.setIgnoreRTL(!LocaleController.isRTL);
            timeView.adaptWidth = false;
            timeView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
            timeView.setTextSize(AndroidUtilities.dp(13));
            timeView.setTypeface(AndroidUtilities.bold());
            timeView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            timeView.setText(update.uploadDate + " UTC");
            header.addView(timeView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 20, Gravity.LEFT, 75, 35, 0, 0));
        }

        TextCell version = new TextCell(context, resourcesProvider);
        version.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector, resourcesProvider), 100, 0));
        if (available) {
            version.setTextAndValueAndIcon(getString(R.string.UP_Version), update.version.replaceAll("v|-beta|-force", ""), R.drawable.msg_info, true);
        } else {
            version.setTextAndValueAndIcon(getString(R.string.UP_CurrentVersion), Constants.INSTANCE.getCherryVersion(), R.drawable.msg_info, false);
        }
        version.setOnClickListener(v -> copyText(version.getTextView().getText() + ": " + version.getValueTextView().getText()));
        linearLayout.addView(version);

        View divider = new View(context) {
            @Override
            protected void onDraw(@NonNull Canvas canvas) {
                super.onDraw(canvas);
                canvas.drawLine(0, AndroidUtilities.dp(1), getMeasuredWidth(), AndroidUtilities.dp(1), Theme.dividerPaint);
            }
        };

        buttonsView = new FrameLayout(context);
        buttonsView.setBackgroundColor(getThemedColor(Theme.key_dialogBackground));

        if (available) {
            if (!TextUtils.isEmpty(update.changelog)) {
                TextCell changelog = new TextCell(context, resourcesProvider);
                changelog.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector, resourcesProvider), 100, 0));
                if (update.changelog.contains("Changelog")) {
                    changelog.setTextAndIcon(getString(R.string.UP_Changelog), R.drawable.msg_log, false);
                } else {
                    changelog.setTextAndValueAndIcon(getString(R.string.UP_Changelog), getString(R.string.UP_ChangelogRead), R.drawable.msg_log, false);
                    changelog.setOnClickListener(v -> {
                        Browser.openUrl(getContext(), update.changelog);
                        dismiss();
                    });
                }
                linearLayout.addView(changelog);

                if (update.changelog.contains("Changelog")) {
                    TextInfoPrivacyCell changelogTextView = new TextInfoPrivacyCell(context, resourcesProvider);
                    changelogTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
                    changelogTextView.setText(UpdaterUtils.replaceTags(update.changelog));
                    linearLayout.addView(changelogTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
                }
            }

            linearLayout.addView(divider, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(1)));

            ButtonWithCounterView downloadButton = new ButtonWithCounterView(context, resourcesProvider);
            downloadButton.setFilled(true);
            downloadButton.setText(getUpdateSizeString(update), false);
            downloadButton.setOnClickListener(v -> {
                if (!downloadButtonClicked) {
                    downloadButtonClicked = true;
                    downloadButton.setClickable(false);
                    UpdaterUtils.downloadApk(getContext(), update.downloadURL, "Cherrygram " + update.version, downloadButton);
                    dismiss();
                }
            });
            buttonsView.addView(downloadButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 16, 16, 72, 16));

            if (update.isForce()) {
                setCancelable(false);
                isForce = true;
                CherrygramCoreConfig.INSTANCE.setAutoOTA(true);
            }
            CherrygramCoreConfig.INSTANCE.setForceFound(update.isForce());
        } else {
            final String bType = CGResourcesHelper.INSTANCE.getBuildType() + " | " + CGResourcesHelper.INSTANCE.getAbiCode();

            TextCell buildType = new TextCell(context, resourcesProvider);
            buildType.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector, resourcesProvider), 100, 0));
            buildType.setTextAndValueAndIcon(getString(R.string.UP_BuildType), bType, R.drawable.msg_customize, true);
            buildType.setOnClickListener(v -> copyText(buildType.getTextView().getText() + ": " + buildType.getValueTextView().getText()));
            linearLayout.addView(buildType);

            TextCell installBetas = new TextCell(context, 23, false, true, resourcesProvider);
            installBetas.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector, resourcesProvider), 100, 0));
            installBetas.setTextAndCheckAndIcon(getString(R.string.UP_InstallBetas), CherrygramCoreConfig.INSTANCE.getInstallBetas(), R.drawable.test_tube_solar, false);
            installBetas.setOnClickListener(v -> {
                CherrygramCoreConfig.INSTANCE.setInstallBetas(!CherrygramCoreConfig.INSTANCE.getInstallBetas());
                installBetas.setChecked(!installBetas.isChecked());
                checkUpdatesButton.callOnClick();
            });
            linearLayout.addView(installBetas);

            TextCell checkOnLaunch = new TextCell(context, 23, false, true, resourcesProvider);
            checkOnLaunch.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector, resourcesProvider), 100, 0));
            checkOnLaunch.setTextAndCheckAndIcon(getString(R.string.UP_Auto_OTA), CherrygramCoreConfig.INSTANCE.getAutoOTA(), R.drawable.msg_retry, false);
            checkOnLaunch.setOnClickListener(v -> {
                CherrygramCoreConfig.INSTANCE.setAutoOTA(!CherrygramCoreConfig.INSTANCE.getAutoOTA());
                checkOnLaunch.setChecked(!checkOnLaunch.isChecked());
            });
            if (!CherrygramCoreConfig.INSTANCE.getForceFound()) linearLayout.addView(checkOnLaunch);

            TextCell clearUpdates = new TextCell(context, resourcesProvider);
            clearUpdates.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector, resourcesProvider), 100, 0));
            clearUpdates.setTextAndIcon(getString(R.string.UP_ClearUpdatesCache), R.drawable.msg_clear, false);
            clearUpdates.setOnClickListener(v -> {
                if (UpdaterUtils.getOtaDirSize().replaceAll("\\D+", "").equals("0")) {
                    BulletinFactory.of(getContainer(), null).createErrorBulletin(getString(R.string.UP_NothingToClear)).show();
                } else {
                    BulletinFactory.of(getContainer(), null).createErrorBulletin(LocaleController.formatString(R.string.UP_ClearedUpdatesCache, UpdaterUtils.getOtaDirSize())).show();
                    UpdaterUtils.cleanOtaDir();
                }

                UpdaterUtils.cancelDownload(getContext(), UpdaterUtils.id);
                CherrygramCoreConfig.INSTANCE.setUpdateAvailable(false);
            });
            linearLayout.addView(clearUpdates);

            linearLayout.addView(divider, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(1)));

            checkUpdatesButton = new ButtonWithCounterView(context, resourcesProvider);
            checkUpdatesButton.text.setAnimationProperties(.7f, 0, 500, CubicBezierInterpolator.EASE_OUT_QUINT);
            checkUpdatesButton.setText(getString(R.string.UP_CheckForUpdates), true);
            checkUpdatesButton.setOnClickListener(v -> {
                SpannableStringBuilder sb = new SpannableStringBuilder();
                sb.append("+ ");
                sb.setSpan(new ColoredImageSpan(Objects.requireNonNull(ContextCompat.getDrawable(getContext(), R.drawable.sync_outline_28))), 0, 1, 0);
                checkUpdatesButton.setText(sb, true);

                UpdaterUtils.checkUpdates(fragment, true, () -> {
                    checkUpdatesButton.setText(getString(R.string.UP_CheckForUpdates), true);
                    BulletinFactory.of(getContainer(), resourcesProvider).createErrorBulletin(getString(R.string.UP_Not_Found)).show();
                }, this::dismiss, null);
            });
            buttonsView.addView(checkUpdatesButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 16, 16, 72, 16));
        }

        ButtonWithCounterView apkButton = new ButtonWithCounterView(context, resourcesProvider);
        SpannableStringBuilder sb = new SpannableStringBuilder();
        sb.append("+");
        sb.setSpan(new ColoredImageSpan(ContextCompat.getDrawable(getContext(), isForce ? R.drawable.github_logo_white : R.drawable.msg_folders_channels_solar)), 0, 1, 0);
        apkButton.setText(sb, false);
        apkButton.setOnClickListener(v -> {
            if (isForce) {
                openGithubReleases();
            } else {
                openApkChannel();
            }
        });
        buttonsView.addView(apkButton, LayoutHelper.createFrame(48, 48, Gravity.BOTTOM | Gravity.RIGHT, 0, 16, 16, 16));
        linearLayout.addView(buttonsView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL));

        if (available && !isForce) {
            ButtonWithCounterView scheduleButton = new ButtonWithCounterView(context, resourcesProvider);
            scheduleButton.setFilled(true);
            scheduleButton.setText(getString(R.string.AppUpdateRemindMeLater), false);
            scheduleButton.setOnClickListener(v -> {
                CherrygramCoreConfig.INSTANCE.setUpdateScheduleTimestamp(System.currentTimeMillis());
                dismiss();
            });
            linearLayout.addView(scheduleButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 16, 0, 16, 16));
        }

        ScrollView scrollView = new ScrollView(context);
        scrollView.addView(linearLayout);
        setCustomView(scrollView);
    }

    private void openApkChannel() {
        dismiss();
        fragment.getMessagesController().openByUserName(Constants.CG_APKS_CHANNEL_USERNAME, fragment, 1);
    }

    private void openGithubReleases() {
        String githubLink;
        if (CherrygramCoreConfig.INSTANCE.isStandaloneBetaBuild()) {
            githubLink = "https://github.com/arsLan4k1390/CherrygramBeta-APKs/releases/latest";
        } else {
            githubLink = "https://github.com/arsLan4k1390/Cherrygram/releases/latest";
        }
        Browser.openUrl(fragment.getContext(), githubLink);
    }

    private StringBuilder getUpdateSizeString(UpdaterUtils.Update update) {
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.AppUpdateDownloadNow));
        sb.append(" (");
        sb.append(update.size);
        sb.append(")");
        return sb;
    }

    private void copyText(CharSequence text) {
        AndroidUtilities.addToClipboard(text);
        BulletinFactory.of(getContainer(), resourcesProvider).createCopyBulletin(getString(R.string.TextCopied)).show();
    }

    public void setFragmentParams(BaseFragment fragment) {
        this.fragment = fragment;
        this.resourcesProvider = fragment.getResourceProvider();
    }

    public static void showAlert(BaseFragment fragment, boolean available, UpdaterUtils.Update update) {
        UpdaterBottomSheet alert = new UpdaterBottomSheet(fragment.getContext(), fragment.getResourceProvider(), available, update);
        alert.setFragmentParams(fragment);
        if (fragment.getParentActivity() != null) {
            fragment.showDialog(alert);
        }
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
    }

    @Override
    public void onBackPressed() {
        if (!isForce) {
            if (attachedFragment == null) {
                super.onBackPressed();
            } else {
                dismiss();
            }
        }
    }

    @Override
    public void dismiss() {
        if (!isForce) {
            if (attachedFragment == null) {
                super.dismiss();
            } else {
                dismiss();
            }
        }
    }

}
