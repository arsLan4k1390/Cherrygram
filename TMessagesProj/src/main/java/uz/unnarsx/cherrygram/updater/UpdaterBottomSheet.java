package uz.unnarsx.cherrygram.updater;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
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

import java.util.Objects;

import uz.unnarsx.cherrygram.CherrygramConfig;
import uz.unnarsx.cherrygram.extras.CherrygramExtras;
import uz.unnarsx.cherrygram.helpers.ui.MonetHelper;

public class UpdaterBottomSheet extends BottomSheet {

    public UpdaterBottomSheet(Context context, BaseFragment fragment, boolean available, UpdaterUtils.Update update) {
        super(context, false);
        setOpenNoDelay(true);
        fixNavigationBar();

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        FrameLayout header = new FrameLayout(context);
        linearLayout.addView(header, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 21, 10, 0, 10));

        if (available) {
            Drawable cherry = ContextCompat.getDrawable(context, R.drawable.about_cherry_icon).mutate();
            Theme.ThemeInfo theme = Theme.getActiveTheme();
            int color = ContextCompat.getColor(context, R.color.ic_background);

            if (theme.isMonet() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                color = MonetHelper.getColor(theme.isDark() ? "n1_800" : "a1_100");
                cherry.setColorFilter(new PorterDuffColorFilter(MonetHelper.getColor(theme.isDark() ? "a1_100" : "n2_700"), PorterDuff.Mode.MULTIPLY));
            } /*else {
            cherry.setAlpha((int) (70 * 2.55f));
            }*/

            ImageView logo = new ImageView(context);
            logo.setScaleType(ImageView.ScaleType.CENTER);
            logo.setBackground(Theme.createCircleDrawable(AndroidUtilities.dp(95), color));
            logo.setImageDrawable(cherry);
            header.addView(logo, LayoutHelper.createFrame(95, 95, Gravity.CENTER | Gravity.TOP, 0, 5, 10, 0));

            SimpleTextView nameView = new SimpleTextView(context);
            nameView.setTextSize(20);
            nameView.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
            nameView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            nameView.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
            nameView.setText(LocaleController.getString("CG_AppName", R.string.CG_AppName));
            header.addView(nameView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 30, Gravity.CENTER | Gravity.TOP, 0, 110, 10, 0));
        }

        AnimatedTextView timeView = new AnimatedTextView(context, true, true, false);
        timeView.setAnimationProperties(0.7f, 0, 450, CubicBezierInterpolator.EASE_OUT_QUINT);
        timeView.setIgnoreRTL(!LocaleController.isRTL);
        timeView.adaptWidth = false;
        timeView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        timeView.setTextSize(AndroidUtilities.dp(13));
        timeView.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
        timeView.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
        timeView.setText(available ? update.uploadDate : LocaleController.getString("UP_LastCheck", R.string.UP_LastCheck) + ": " + LocaleController.formatDateTime(CherrygramConfig.INSTANCE.getLastUpdateCheckTime() / 1000));
        if (available) header.addView(timeView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 20, Gravity.CENTER | Gravity.TOP, 0, 140, 10, 15));

        TextCell version = new TextCell(context);
        version.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), 100, 0));
        if (available) {
            version.setTextAndValueAndIcon(LocaleController.getString("UP_Version", R.string.UP_Version), update.version.replaceAll("v|-beta|-force", ""), R.drawable.msg_info, true);
        } else {
            version.setTextAndValueAndIcon(LocaleController.getString("UP_CurrentVersion", R.string.UP_CurrentVersion), CherrygramExtras.INSTANCE.getCG_VERSION(), R.drawable.msg_info, false);
        }
        version.setOnClickListener(v -> copyText(version.getTextView().getText() + ": " + version.getValueTextView().getText()));
        linearLayout.addView(version);

        View divider = new View(context) {
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                if (!CherrygramConfig.INSTANCE.getDisableDividers()) canvas.drawLine(0, AndroidUtilities.dp(1), getMeasuredWidth(), AndroidUtilities.dp(1), Theme.dividerPaint);
            }
        };

        if (available) {
            TextCell size = new TextCell(context);
            size.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), 100, 0));
            size.setTextAndValueAndIcon(LocaleController.getString("UP_UpdateSize", R.string.UP_UpdateSize), update.size, R.drawable.msg_sendfile, true);
            size.setOnClickListener(v -> copyText(size.getTextView().getText() + ": " + size.getValueTextView().getText()));
            linearLayout.addView(size);

            TextCell changelog = new TextCell(context);
            changelog.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), 100, 0));
            changelog.setTextAndIcon(LocaleController.getString("UP_Changelog", R.string.UP_Changelog), R.drawable.msg_log, false);
            changelog.setOnClickListener(v -> copyText(changelog.getTextView().getText() + "\n"));
            linearLayout.addView(changelog);

            TextInfoPrivacyCell changelogTextView = new TextInfoPrivacyCell(context);
            changelogTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
            changelogTextView.setText(UpdaterUtils.replaceTags(update.changelog));
            linearLayout.addView(changelogTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            linearLayout.addView(divider, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(1)));

            TextView doneButton = new TextView(context);
            doneButton.setLines(1);
            doneButton.setSingleLine(true);
            doneButton.setEllipsize(TextUtils.TruncateAt.END);
            doneButton.setGravity(Gravity.CENTER);
            doneButton.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
            doneButton.setBackground(Theme.AdaptiveRipple.filledRect(Theme.getColor(Theme.key_featuredStickers_addButton), 6));
            doneButton.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
            doneButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            doneButton.setText(LocaleController.getString("AppUpdateDownloadNow", R.string.AppUpdateDownloadNow));
            doneButton.setOnClickListener(v -> {
                UpdaterUtils.downloadApk(fragment.getContext(), update.downloadURL, "Cherrygram " + update.version);
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
            scheduleButton.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
            scheduleButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            scheduleButton.setText(LocaleController.getString("AppUpdateRemindMeLater", R.string.AppUpdateRemindMeLater));
            scheduleButton.setOnClickListener(v -> {
                CherrygramConfig.INSTANCE.setUpdateScheduleTimestamp(System.currentTimeMillis());
                dismiss();
            });
            linearLayout.addView(scheduleButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, 0, 16, 1, 16, 0));
        } else {
            final String btype = BuildVars.isBetaApp() ? LocaleController.getString("UP_BTBeta", R.string.UP_BTBeta) + " | " + CherrygramExtras.INSTANCE.getAbiCode() : LocaleController.getString("UP_BTRelease", R.string.UP_BTRelease) + " | " + CherrygramExtras.INSTANCE.getAbiCode();
            TextCell buildType = new TextCell(context);
            buildType.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), 100, 0));
            buildType.setTextAndValueAndIcon(LocaleController.getString("UP_BuildType", R.string.UP_BuildType), btype, R.drawable.msg_customize, true);
            buildType.setOnClickListener(v -> copyText(buildType.getTextView().getText() + ": " + buildType.getValueTextView().getText()));
            linearLayout.addView(buildType);

            TextCell installBetas = new TextCell(context, 23, false, true, resourcesProvider);
            installBetas.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), 100, 0));
            installBetas.setTextAndCheckAndIcon(LocaleController.getString("UP_InstallBetas", R.string.UP_InstallBetas), CherrygramConfig.INSTANCE.getInstallBetas(), R.drawable.test_tube_solar, false);
            installBetas.setOnClickListener(v -> {
                CherrygramConfig.INSTANCE.toggleInstallBetas();
                installBetas.setChecked(!installBetas.isChecked());
            });
            linearLayout.addView(installBetas);

            TextCell checkOnLaunch = new TextCell(context, 23, false, true, resourcesProvider);
            checkOnLaunch.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), 100, 0));
            checkOnLaunch.setTextAndCheckAndIcon(LocaleController.getString("UP_Auto_OTA", R.string.UP_Auto_OTA), CherrygramConfig.INSTANCE.getAutoOTA(), R.drawable.msg_retry, false);
            checkOnLaunch.setOnClickListener(v -> {
                CherrygramConfig.INSTANCE.toggleAutoOTA();
                checkOnLaunch.setChecked(!checkOnLaunch.isChecked());
            });
            linearLayout.addView(checkOnLaunch);

            TextCell clearUpdates = new TextCell(context);
            clearUpdates.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), 100, 0));
            clearUpdates.setTextAndIcon(LocaleController.getString("UP_ClearUpdatesCache", R.string.UP_ClearUpdatesCache), R.drawable.msg_clear, false);
            clearUpdates.setOnClickListener(v -> {
                if (UpdaterUtils.getOtaDirSize().replaceAll("\\D+", "").equals("0")) {
                    BulletinFactory.of(getContainer(), null).createErrorBulletin(LocaleController.getString("UP_NothingToClear", R.string.UP_NothingToClear)).show();
                } else {
                    BulletinFactory.of(getContainer(), null).createErrorBulletin(LocaleController.formatString("UP_ClearedUpdatesCache", R.string.UP_ClearedUpdatesCache, UpdaterUtils.getOtaDirSize())).show();
                    UpdaterUtils.cleanOtaDir();
                }
            });
            linearLayout.addView(clearUpdates);

            linearLayout.addView(divider, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(1)));

            FrameLayout checkUpdatesBackground = new FrameLayout(context);
            checkUpdatesBackground.setBackground(Theme.AdaptiveRipple.filledRect(Theme.getColor(Theme.key_featuredStickers_addButton), 6));
            linearLayout.addView(checkUpdatesBackground, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, 0, 16, 15, 16, 16));

            AnimatedTextView checkUpdates = new AnimatedTextView(context, true, true, false);
            checkUpdates.setAnimationProperties(.7f, 0, 500, CubicBezierInterpolator.EASE_OUT_QUINT);
            checkUpdates.setGravity(Gravity.CENTER);
            checkUpdates.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
            checkUpdates.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
            checkUpdates.setTextSize(AndroidUtilities.dp(14));
            checkUpdates.setIgnoreRTL(!LocaleController.isRTL);
            checkUpdates.adaptWidth = false;
            checkUpdates.setText(LocaleController.getString("UP_CheckForUpdates", R.string.UP_CheckForUpdates));
            checkUpdates.setOnClickListener(v -> {

                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                spannableStringBuilder.append(".  ");
                spannableStringBuilder.setSpan(new ColoredImageSpan(Objects.requireNonNull(ContextCompat.getDrawable(getContext(), R.drawable.sync_outline_28))), 0, 1, 0);
                checkUpdates.setText(spannableStringBuilder);

                UpdaterUtils.checkUpdates(fragment, true, () -> {
                    timeView.setText(LocaleController.getString("UP_LastCheck", R.string.UP_LastCheck) + ": " + LocaleController.formatDateTime(CherrygramConfig.INSTANCE.getLastUpdateCheckTime() / 1000));
                    checkUpdates.setText(LocaleController.getString("UP_CheckForUpdates", R.string.UP_CheckForUpdates));
                    BulletinFactory.of(getContainer(), null).createErrorBulletin(LocaleController.getString("UP_Not_Found", R.string.UP_Not_Found)).show();
                }, this::dismiss);
            });
            checkUpdatesBackground.addView(checkUpdates, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER));
        }

        ScrollView scrollView = new ScrollView(context);
        scrollView.addView(linearLayout);
        setCustomView(scrollView);
    }

    private void copyText(CharSequence text) {
        AndroidUtilities.addToClipboard(text);
        BulletinFactory.of(getContainer(), null).createCopyBulletin(LocaleController.getString("TextCopied", R.string.TextCopied)).show();
    }
}
