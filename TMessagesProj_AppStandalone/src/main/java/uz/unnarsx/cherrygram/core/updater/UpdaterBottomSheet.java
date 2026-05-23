/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.updater;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.DocumentObject;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.SvgHelper;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.ColoredImageSpan;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.StickerImageView;
import org.telegram.ui.Components.TranslateAlert2;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;

import java.io.File;
import java.util.Objects;
import java.util.Random;

import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.helpers.ui.FontHelper;
import uz.unnarsx.cherrygram.misc.Constants;
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper;

public class UpdaterBottomSheet extends BottomSheet implements NotificationCenter.NotificationCenterDelegate {

    private BaseFragment fragment;
    private Theme.ResourcesProvider resourcesProvider;
    private ButtonWithCounterView checkUpdatesButton;
    private ButtonWithCounterView downloadButton;

    private boolean isForce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.fileLoadProgressChanged);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.appUpdateLoading);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.fileLoaded);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.fileLoadFailed);
    }

    public UpdaterBottomSheet(Context context, Theme.ResourcesProvider resourcesProvider, boolean available, TLRPC.TL_help_appUpdate update) {
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

            if (update.sticker != null) {
                BackupImageView imageView = new BackupImageView(context);
                SvgHelper.SvgDrawable svgThumb = DocumentObject.getSvgThumb(update.sticker.thumbs, Theme.key_windowBackgroundGray, 1.0f);
                TLRPC.PhotoSize thumb = FileLoader.getClosestPhotoSizeWithSize(update.sticker.thumbs, 90);
                ImageLocation imageLocation = ImageLocation.getForDocument(thumb, update.sticker);

                if (svgThumb != null) {
                    imageView.setImage(ImageLocation.getForDocument(update.sticker), "250_250", svgThumb, 0, "update");
                } else {
                    imageView.setImage(ImageLocation.getForDocument(update.sticker), "250_250", imageLocation, null, 0, "update");
                }
                header.addView(imageView, LayoutHelper.createFrame(dp(20), dp(20), Gravity.LEFT | Gravity.CENTER_VERTICAL));
            } else {
                int[] stickers = {5, 7, 8, 15, 16, 23, 24, 25, 26, 31, 33};
                int randomIndex = new Random().nextInt(stickers.length);

                StickerImageView imageView = new StickerImageView(context, currentAccount);
                imageView.setStickerPackName("HotCherry");
                imageView.setStickerNum(stickers[randomIndex]);
                imageView.getImageReceiver().setAutoRepeat(1);
                header.addView(imageView, LayoutHelper.createFrame(dp(20), dp(20), Gravity.LEFT | Gravity.CENTER_VERTICAL));
            }

            SimpleTextView nameView = new SimpleTextView(context);
            nameView.setTextSize(20);
            nameView.setTypeface(FontHelper.createTypeface2(FontHelper.TYPEFACE_GILROY_EXTRABOLD));
            nameView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider));
            nameView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            nameView.setText(getString(R.string.UP_UpdateAvailable));
            header.addView(nameView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, dp(10), Gravity.LEFT, dp(20 + 2), dp(3), 0, 0));

            AnimatedTextView timeView = new AnimatedTextView(context, true, true, false);
            timeView.setAnimationProperties(0.7f, 0, 450, CubicBezierInterpolator.EASE_OUT_QUINT);
            timeView.setIgnoreRTL(!LocaleController.isRTL);
            timeView.adaptWidth = false;
            timeView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
            timeView.setTextSize(dp(13));
            timeView.setTypeface(AndroidUtilities.bold());
            timeView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            timeView.setText(
                    getString(R.string.CG_AppName) + " " + update.build_flavor + " " + update.version +
                    " | " + update.release_date
            );
            header.addView(timeView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, dp(5), Gravity.LEFT, dp(20 + 2), dp(13), 0, 0));
        }

        if (!available) {
            TextCell version = new TextCell(context, resourcesProvider);
            version.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector, resourcesProvider), 100, 0));
            version.setTextAndValueAndIcon(getString(R.string.UP_CurrentVersion), CGResourcesHelper.getCherryVersion(), R.drawable.msg_info, false);
            version.setOnClickListener(v -> copyText(version.getTextView().getText() + ": " + version.getValueTextView().getText()));
            linearLayout.addView(version);
        }

        View divider = new View(context) {
            @Override
            protected void onDraw(@NonNull Canvas canvas) {
                super.onDraw(canvas);
                canvas.drawLine(0, dp(1), getMeasuredWidth(), dp(1), Theme.dividerPaint);
            }
        };

        LinearLayout buttonsView = new LinearLayout(context);
        buttonsView.setOrientation(LinearLayout.HORIZONTAL);
        buttonsView.setBackgroundColor(getThemedColor(Theme.key_dialogBackground));
        buttonsView.setPadding(16, 16, 16, 16);
        buttonsView.setGravity(Gravity.CENTER_VERTICAL);

        if (available) {
            TextView changelogTextView = new TextViewEffects(getContext());
            changelogTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider)); // key_windowBackgroundWhiteBlackText
            changelogTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            changelogTextView.setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());
            changelogTextView.setLinkTextColor(Theme.getColor(Theme.key_dialogTextLink));
            changelogTextView.setLineSpacing(AndroidUtilities.dp(2), 1.0f);

            if (TextUtils.isEmpty(update.text)) {
                changelogTextView.setText(AndroidUtilities.replaceTags(LocaleController.getString(R.string.AppUpdateChangelogEmpty)));
            } else {
                SpannableStringBuilder builder = new SpannableStringBuilder(update.text);
                MessageObject.addEntitiesToText(builder, update.entities, false, true, false, false);
                MessageObject.replaceAnimatedEmoji(builder, update.entities, changelogTextView.getPaint().getFontMetricsInt());
                changelogTextView.setText(builder);
            }

            ScrollView changelogScrollView = new ScrollView(context);
            changelogScrollView.setVerticalScrollBarEnabled(false);

            changelogTextView.measure(
                    View.MeasureSpec.makeMeasureSpec(AndroidUtilities.displaySize.x - AndroidUtilities.dp(46), View.MeasureSpec.AT_MOST),
                    View.MeasureSpec.UNSPECIFIED
            );
            int textHeight = changelogTextView.getMeasuredHeight();
            int maxHeight = (int) (AndroidUtilities.displaySize.y * 0.65F);

            LinearLayout.LayoutParams scrollParams = LayoutHelper.createLinear(
                    LayoutHelper.MATCH_PARENT,
                    LayoutHelper.WRAP_CONTENT,
                    0,
                    23, 0, 23, 8
            );
            scrollParams.height = Math.min(textHeight, maxHeight);
            changelogScrollView.setLayoutParams(scrollParams);
            changelogScrollView.addView(changelogTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            linearLayout.addView(changelogScrollView);

            linearLayout.addView(divider, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, dp(1)));

            downloadButton = new ButtonWithCounterView(context, resourcesProvider).setRound();
            downloadButton.setFilled(true);
            downloadButton.setText(getDownloadButtonText(), false);
            downloadButton.setOnClickListener(v -> {
                if (SharedConfig.pendingAppUpdate != null && SharedConfig.pendingAppUpdate.document != null) {
                    File path = FileLoader.getInstance(currentAccount).getPathToAttach(SharedConfig.pendingAppUpdate.document, true);
                    if (path != null && path.exists() && fragment != null && fragment.getParentActivity() != null) {
                        AndroidUtilities.openForView(SharedConfig.pendingAppUpdate.document, true, fragment.getParentActivity());
                    } else {
                        downloadButton.setClickable(false);
                        FileLoader.getInstance(fragment.getCurrentAccount()).loadFile(update.document, "update", FileLoader.PRIORITY_NORMAL, 1);
                        dismiss();
                    }
                } else {
                    downloadButton.setClickable(false);
                    FileLoader.getInstance(fragment.getCurrentAccount()).loadFile(update.document, "update", FileLoader.PRIORITY_NORMAL, 1);
                    dismiss();
                }
            });
            LinearLayout.LayoutParams downloadButtonParams = new LinearLayout.LayoutParams(0, dp(48), 1f);
            downloadButtonParams.rightMargin = dp(8);
            downloadButtonParams.leftMargin = dp(8);
            buttonsView.addView(downloadButton, downloadButtonParams);

            if (update.can_not_skip) {
                setCancelable(false);
                isForce = true;
                CherrygramCoreConfig.INSTANCE.setAutoOTA(true);
            }
            CherrygramCoreConfig.INSTANCE.setForceFound(update.can_not_skip);
        } else {
            final String bType = CGResourcesHelper.getBuildType() + " | " + CGResourcesHelper.getAbiCode();

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

            linearLayout.addView(divider, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, dp(1)));

            checkUpdatesButton = new ButtonWithCounterView(context, resourcesProvider).setRound();
            checkUpdatesButton.text.setAnimationProperties(.7f, 0, 500, CubicBezierInterpolator.EASE_OUT_QUINT);
            checkUpdatesButton.setText(getString(R.string.UP_CheckForUpdates), true);
            checkUpdatesButton.setOnClickListener(v -> {
                SpannableStringBuilder sb = new SpannableStringBuilder();
                sb.append("+ ");
                sb.setSpan(new ColoredImageSpan(Objects.requireNonNull(ContextCompat.getDrawable(getContext(), R.drawable.msg_retry_solar))), 0, 1, 0);
                checkUpdatesButton.setText(sb, true);
                SharedConfig.lastUpdateCheckTime = System.currentTimeMillis();

                if (fragment.getParentActivity() instanceof LaunchActivity launchActivity) {
                    launchActivity.checkAppUpdate(true, new Browser.Progress() {
                        @Override
                        public void end() {
                            checkUpdatesButton.setText(getString(R.string.UP_CheckForUpdates), true);
                            if (SharedConfig.isAppUpdateAvailable()) {
                                dismiss();
                            } else {
                                BulletinFactory.of(getContainer(), resourcesProvider).createErrorBulletin(getString(R.string.YourVersionIsLatest)).show();
                            }
                        }
                    });
                }
            });
            LinearLayout.LayoutParams downloadButtonParams = new LinearLayout.LayoutParams(0, dp(48), 1f);
            downloadButtonParams.rightMargin = dp(8);
            downloadButtonParams.leftMargin = dp(8);
            buttonsView.addView(checkUpdatesButton, downloadButtonParams);
        }

        LinearLayout.LayoutParams secondaryButtonParams = new LinearLayout.LayoutParams(dp(48), dp(48));
        secondaryButtonParams.rightMargin = dp(8);

        if (available) {
            ButtonWithCounterView translateButton = new ButtonWithCounterView(context, resourcesProvider).setRound();
            SpannableStringBuilder sb = new SpannableStringBuilder();
            sb.append("+");
            sb.setSpan(new ColoredImageSpan(ContextCompat.getDrawable(getContext(), R.drawable.msg_translate_filled_solar)), 0, 1, 0);
            translateButton.setText(sb, false);
            translateButton.setOnClickListener(v -> {
                String fromLang = "en";
                String toLang = TranslateAlert2.getToLanguage();

                TranslateAlert2.showAlert(getContext(), fragment, currentAccount, fromLang, toLang, update.text, update.entities, false, span -> {
                    if (span != null) {
                        Browser.openUrl(getContext(), span.getURL());
                        return true;
                    }
                    return false;
                }, () -> {
                    if (isForce) return;
                    AndroidUtilities.runOnUIThread(() -> UpdaterBottomSheet.showAlert(fragment, true, SharedConfig.pendingAppUpdate), 200);
                });
            });
            buttonsView.addView(translateButton, secondaryButtonParams);
        }

        ButtonWithCounterView apkButton = new ButtonWithCounterView(context, resourcesProvider).setRound();
        SpannableStringBuilder sb2 = new SpannableStringBuilder();
        sb2.append("+");
        sb2.setSpan(new ColoredImageSpan(ContextCompat.getDrawable(getContext(), isForce ? R.drawable.github_cat_filled : R.drawable.msg_folders_channels_solar)), 0, 1, 0);
        apkButton.setText(sb2, false);
        apkButton.setOnClickListener(v -> {
            if (isForce) {
                openGithubReleases();
            } else {
                openApkChannel();
            }
        });
        buttonsView.addView(apkButton, secondaryButtonParams);
        linearLayout.addView(buttonsView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL));

        if (available && !isForce) {
            ButtonWithCounterView scheduleButton = new ButtonWithCounterView(context, resourcesProvider);
            scheduleButton.setFilled(false);
            scheduleButton.setText(getString(R.string.AppUpdateRemindMeLater), false);
            scheduleButton.setOnClickListener(v -> {
                dismiss();

                SharedConfig.lastUpdateCheckTime = System.currentTimeMillis();
                SharedConfig.pendingAppUpdate = null;
                SharedConfig.saveConfig();
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
            });
            linearLayout.addView(scheduleButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 16, 0, 16, 0));
        }

        setCustomView(linearLayout);
    }

    private void openApkChannel() {
        dismiss();

        String username;
        if (CherrygramCoreConfig.INSTANCE.getInstallBetas()) {
            username = Constants.CG_BETA_APKS_CHANNEL_USERNAME;
        } else {
            username = Constants.CG_APKS_CHANNEL_USERNAME;
        }
        fragment.getMessagesController().openByUserName(username, fragment, 1);
    }

    private void openGithubReleases() {
        String githubLink;
        if (CherrygramCoreConfig.isStandaloneBetaBuild() || CherrygramCoreConfig.INSTANCE.getInstallBetas()) {
            githubLink = Constants.CG_GITHUB_URL + "Beta-APKs/releases/latest";
        } else {
            githubLink = Constants.CG_GITHUB_URL + "/releases/latest";
        }
        Browser.openInExternalBrowser(fragment.getContext(), githubLink, true);
    }

    private StringBuilder getDownloadButtonText() {
        StringBuilder sb = new StringBuilder();

        File path = FileLoader.getInstance(currentAccount).getPathToAttach(SharedConfig.pendingAppUpdate.document, true);
        if (path.exists()) {
            sb.append(getString(R.string.AppUpdateNow));
        } else {
            sb.append(getString(R.string.AppUpdateDownloadNow));
            sb.append(" (");
            sb.append(getUpdateSizeString());
            sb.append(")");
        }
        return sb;
    }

    private String getUpdateSizeString() {
        if (SharedConfig.pendingAppUpdate != null && SharedConfig.pendingAppUpdate.document != null) {
            String size = AndroidUtilities.formatFileSize(SharedConfig.pendingAppUpdate.document.size, true, false);
            if (!TextUtils.isEmpty(size) && !size.equals("0")) {
                return size;
            }
        }
        return "";
    }

    private void copyText(CharSequence text) {
        AndroidUtilities.addToClipboard(text);
        BulletinFactory.of(getContainer(), resourcesProvider).createCopyBulletin(getString(R.string.TextCopied)).show();
    }

    public void setFragmentParams(BaseFragment fragment) {
        this.fragment = fragment;
        this.resourcesProvider = fragment.getResourceProvider();
    }

    public static void showAlert(BaseFragment fragment, boolean available, TLRPC.TL_help_appUpdate update) {
        UpdaterBottomSheet alert = new UpdaterBottomSheet(fragment.getContext(), fragment.getResourceProvider(), available, update);
        alert.setFragmentParams(fragment);
        if (fragment.getParentActivity() != null) {
            fragment.showDialog(alert);
        }
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
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.fileLoadProgressChanged);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.appUpdateLoading);
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.fileLoaded);
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.fileLoadFailed);
            if (attachedFragment == null) {
                super.dismiss();
            } else {
                dismiss();
            }
        }
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.fileLoadProgressChanged || id == NotificationCenter.appUpdateLoading) {
            updateFileProgress(args);
        } else if (id == NotificationCenter.fileLoaded || id == NotificationCenter.fileLoadFailed) {
            String path = (String) args[0];
            if (SharedConfig.isAppUpdateAvailable()) {
                String name = FileLoader.getAttachFileName(SharedConfig.pendingAppUpdate.document);
                if (name.equals(path) && downloadButton != null) {
                    downloadButton.setClickable(true);
                    downloadButton.setText(getDownloadButtonText(), true);
                }
            }
        }
    }

    public void updateFileProgress(Object[] args) {
        if (downloadButton == null || args == null) return;
        if (SharedConfig.isAppUpdateAvailable()) {
            String location = (String) args[0];
            String fileName = FileLoader.getAttachFileName(SharedConfig.pendingAppUpdate.document);
            if (fileName != null && fileName.equals(location)) {
                Long loadedSize = (Long) args[1];
                Long totalSize = (Long) args[2];
                float loadProgress = loadedSize / (float) totalSize;
                downloadButton.setText(LocaleController.formatString(R.string.AppUpdateDownloading, (int) (loadProgress * 100)), true);
            }
        }
    }

}
