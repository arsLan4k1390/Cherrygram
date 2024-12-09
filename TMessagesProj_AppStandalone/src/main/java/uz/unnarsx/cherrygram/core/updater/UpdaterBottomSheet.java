package uz.unnarsx.cherrygram.core.updater;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.graphics.Canvas;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
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

import java.util.Objects;

import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.misc.Constants;
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper;

public class UpdaterBottomSheet extends BottomSheet {

    private BaseFragment fragment;

    public UpdaterBottomSheet(Context context, boolean available, UpdaterUtils.Update update) {
        super(context, false);
        setOpenNoDelay(true);
        fixNavigationBar();

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        FrameLayout header = new FrameLayout(context);
        linearLayout.addView(header, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 21, 10, 0, 10));

        if (available) {
            StickerImageView imageView = new StickerImageView(context, currentAccount);
            imageView.setStickerPackName("HotCherry");
            imageView.setStickerNum(33);
            imageView.getImageReceiver().setAutoRepeat(1);
            header.addView(imageView, LayoutHelper.createFrame(60, 60, Gravity.LEFT | Gravity.CENTER_VERTICAL));

            SimpleTextView nameView = new SimpleTextView(context);
            nameView.setTextSize(20);
            nameView.setTypeface(AndroidUtilities.bold());
            nameView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            nameView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            nameView.setText(getString(R.string.UP_UpdateAvailable));
            header.addView(nameView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 30, Gravity.LEFT, 75, 5, 0, 0));

            AnimatedTextView timeView = new AnimatedTextView(context, true, true, false);
            timeView.setAnimationProperties(0.7f, 0, 450, CubicBezierInterpolator.EASE_OUT_QUINT);
            timeView.setIgnoreRTL(!LocaleController.isRTL);
            timeView.adaptWidth = false;
            timeView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
            timeView.setTextSize(AndroidUtilities.dp(13));
            timeView.setTypeface(AndroidUtilities.bold());
            timeView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            timeView.setText(update.uploadDate + " UTC");
            header.addView(timeView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 20, Gravity.LEFT, 75, 35, 0, 0));
        }

        TextCell version = new TextCell(context);
        version.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), 100, 0));
        if (available) {
            version.setTextAndValueAndIcon(getString(R.string.UP_Version), update.version.replaceAll("v|-beta|-force", ""), R.drawable.msg_info, true);
        } else {
            version.setTextAndValueAndIcon(getString(R.string.UP_CurrentVersion), Constants.INSTANCE.getCG_VERSION(), R.drawable.msg_info, false);
        }
        version.setOnClickListener(v -> copyText(version.getTextView().getText() + ": " + version.getValueTextView().getText()));
        linearLayout.addView(version);

        View divider = new View(context) {
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                canvas.drawLine(0, AndroidUtilities.dp(1), getMeasuredWidth(), AndroidUtilities.dp(1), Theme.dividerPaint);
            }
        };

        if (available) {
            TextCell size = new TextCell(context);
            size.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), 100, 0));
            size.setTextAndValueAndIcon(getString(R.string.UP_UpdateSize), update.size, R.drawable.msg_sendfile, true);
            size.setOnClickListener(v -> copyText(size.getTextView().getText() + ": " + size.getValueTextView().getText()));
            linearLayout.addView(size);

            if (!TextUtils.isEmpty(update.changelog)) {
                TextCell changelog = new TextCell(context);
                changelog.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), 100, 0));
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
                    TextInfoPrivacyCell changelogTextView = new TextInfoPrivacyCell(context);
                    changelogTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
                    changelogTextView.setText(UpdaterUtils.replaceTags(update.changelog));
                    linearLayout.addView(changelogTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
                }
            }

            linearLayout.addView(divider, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(1)));

            TextView doneButton = new TextView(context);
            doneButton.setLines(1);
            doneButton.setSingleLine(true);
            doneButton.setEllipsize(TextUtils.TruncateAt.END);
            doneButton.setGravity(Gravity.CENTER);
            doneButton.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
            doneButton.setBackground(Theme.AdaptiveRipple.filledRect(Theme.getColor(Theme.key_featuredStickers_addButton), 6));
            doneButton.setTypeface(AndroidUtilities.bold());
            doneButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            doneButton.setText(getString(R.string.AppUpdateDownloadNow));
            doneButton.setOnClickListener(v -> {
                UpdaterUtils.downloadApk(getContext(), update.downloadURL, "Cherrygram " + update.version);
                dismiss();
            });
            linearLayout.addView(doneButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, 0, 16, 15, 16, 5));

            TextView scheduleButton = new TextView(context);
            scheduleButton.setLines(1);
            scheduleButton.setSingleLine(true);
            scheduleButton.setEllipsize(TextUtils.TruncateAt.END);
            scheduleButton.setGravity(Gravity.CENTER);
            scheduleButton.setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton));
            scheduleButton.setBackground(null);
            scheduleButton.setTypeface(AndroidUtilities.bold());
            scheduleButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            scheduleButton.setText(getString(R.string.AppUpdateRemindMeLater));
            scheduleButton.setOnClickListener(v -> {
                CherrygramCoreConfig.INSTANCE.setUpdateScheduleTimestamp(System.currentTimeMillis());
                dismiss();
            });
            linearLayout.addView(scheduleButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, 0, 16, 1, 16, 0));
        } else {
            final String bType = CGResourcesHelper.INSTANCE.getBuildType() + " | " + CGResourcesHelper.INSTANCE.getAbiCode();
            TextCell buildType = new TextCell(context);
            buildType.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), 100, 0));
            buildType.setTextAndValueAndIcon(getString(R.string.UP_BuildType), bType, R.drawable.msg_customize, true);
            buildType.setOnClickListener(v -> copyText(buildType.getTextView().getText() + ": " + buildType.getValueTextView().getText()));
            linearLayout.addView(buildType);

            AnimatedTextView checkUpdates = new AnimatedTextView(context, true, true, false);
            TextCell installBetas = new TextCell(context, 23, false, true, resourcesProvider);
            installBetas.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), 100, 0));
            installBetas.setTextAndCheckAndIcon(getString(R.string.UP_InstallBetas), CherrygramCoreConfig.INSTANCE.getInstallBetas(), R.drawable.test_tube_solar, false);
            installBetas.setOnClickListener(v -> {
                CherrygramCoreConfig.INSTANCE.toggleInstallBetas();
                installBetas.setChecked(!installBetas.isChecked());
                checkUpdates.callOnClick();
            });
            linearLayout.addView(installBetas);

            TextCell checkOnLaunch = new TextCell(context, 23, false, true, resourcesProvider);
            checkOnLaunch.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), 100, 0));
            checkOnLaunch.setTextAndCheckAndIcon(getString(R.string.UP_Auto_OTA), CherrygramCoreConfig.INSTANCE.getAutoOTA(), R.drawable.msg_retry, false);
            checkOnLaunch.setOnClickListener(v -> {
                CherrygramCoreConfig.INSTANCE.toggleAutoOTA();
                checkOnLaunch.setChecked(!checkOnLaunch.isChecked());
            });
            linearLayout.addView(checkOnLaunch);

            TextCell clearUpdates = new TextCell(context);
            clearUpdates.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), 100, 0));
            clearUpdates.setTextAndIcon(getString(R.string.UP_ClearUpdatesCache), R.drawable.msg_clear, false);
            clearUpdates.setOnClickListener(v -> {
                if (UpdaterUtils.getOtaDirSize().replaceAll("\\D+", "").equals("0")) {
                    BulletinFactory.of(getContainer(), null).createErrorBulletin(getString(R.string.UP_NothingToClear)).show();
                } else {
                    BulletinFactory.of(getContainer(), null).createErrorBulletin(LocaleController.formatString(R.string.UP_ClearedUpdatesCache, UpdaterUtils.getOtaDirSize())).show();
                    UpdaterUtils.cleanOtaDir();
                }
            });
            linearLayout.addView(clearUpdates);

            linearLayout.addView(divider, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(1)));

            FrameLayout checkUpdatesBackground = new FrameLayout(context);
            checkUpdatesBackground.setBackground(Theme.AdaptiveRipple.filledRect(Theme.getColor(Theme.key_featuredStickers_addButton), 6));
            linearLayout.addView(checkUpdatesBackground, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, 0, 16, 15, 16, 16));

//            AnimatedTextView checkUpdates = new AnimatedTextView(context, true, true, false);
            checkUpdates.setAnimationProperties(.7f, 0, 500, CubicBezierInterpolator.EASE_OUT_QUINT);
            checkUpdates.setGravity(Gravity.CENTER);
            checkUpdates.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
            checkUpdates.setTypeface(AndroidUtilities.bold());
            checkUpdates.setTextSize(AndroidUtilities.dp(14));
            checkUpdates.setIgnoreRTL(!LocaleController.isRTL);
            checkUpdates.adaptWidth = false;
            checkUpdates.setText(getString(R.string.UP_CheckForUpdates));
            checkUpdates.setOnClickListener(v -> {
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                spannableStringBuilder.append(".  ");
                spannableStringBuilder.setSpan(new ColoredImageSpan(Objects.requireNonNull(ContextCompat.getDrawable(getContext(), R.drawable.sync_outline_28))), 0, 1, 0);
                checkUpdates.setText(spannableStringBuilder);

                UpdaterUtils.checkUpdates(fragment, true, () -> {
                    checkUpdates.setText(getString(R.string.UP_CheckForUpdates));
                    BulletinFactory.of(getContainer(), null).createErrorBulletin(getString(R.string.UP_Not_Found)).show();
                }, this::dismiss, null);
            });
            checkUpdatesBackground.addView(checkUpdates, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER));
        }

        ScrollView scrollView = new ScrollView(context);
        scrollView.addView(linearLayout);
        setCustomView(scrollView);
    }

    public void setFragment(BaseFragment fragment) {
        this.fragment = fragment;
    }

    private void copyText(CharSequence text) {
        AndroidUtilities.addToClipboard(text);
        BulletinFactory.of(getContainer(), null).createCopyBulletin(getString(R.string.TextCopied)).show();
    }

    public static void showAlert(BaseFragment fragment, boolean available, UpdaterUtils.Update update) {
        UpdaterBottomSheet alert = new UpdaterBottomSheet(fragment.getContext(), available, update);
        alert.setFragment(fragment);
        if (fragment.getParentActivity() != null) {
            fragment.showDialog(alert);
        }
    }
}
