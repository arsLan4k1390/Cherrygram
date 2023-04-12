/*

 This is the source code of exteraGram for Android.

 We do not and cannot prevent the use of our code,
 but be respectful and credit the original author.

 Copyright @immat0x1, 2022.

*/

package uz.unnarsx.cherrygram.updater;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.ColoredImageSpan;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;

import java.util.Objects;

import uz.unnarsx.cherrygram.CherrygramConfig;
import uz.unnarsx.cherrygram.extras.CherrygramExtras;

public class UpdaterBottomSheet extends BottomSheet {

    AnimatorSet animatorSet;
    private TextView changelogTextView;
    private boolean isTranslated = false;
    private CharSequence translatedC;

    public UpdaterBottomSheet(Context context, boolean available, String... args) {
        super(context, false);
        setOpenNoDelay(true);
        fixNavigationBar();

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        FrameLayout header = new FrameLayout(context);
        linearLayout.addView(header, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 21, 10, 0, 10));

        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setImageResource(R.drawable.about_cherry_icon);
        imageView.setColorFilter(Theme.getColor(Theme.key_featuredStickers_buttonText));
        imageView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(100), Theme.getColor(Theme.key_featuredStickers_addButton), Color.BLACK));
        header.addView(imageView, LayoutHelper.createFrame(85, 85, Gravity.CENTER | Gravity.TOP, 0, 5, 10, 0));

        SimpleTextView nameView = new SimpleTextView(context);
        nameView.setTextSize(20);
        nameView.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
        nameView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        nameView.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
        nameView.setText(LocaleController.getString("CG_AppName", R.string.CG_AppName));
        header.addView(nameView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER | Gravity.TOP, 0, 95, 10, 0));

        SimpleTextView timeView = new SimpleTextView(context);
        timeView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        timeView.setTextSize(13);
        timeView.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
        timeView.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
        timeView.setText(available ? args[4] : LocaleController.getString("UP_LastCheck", R.string.UP_LastCheck) + ": " + LocaleController.formatDateTime(CherrygramConfig.INSTANCE.getLastUpdateCheckTime() / 1000));
        header.addView(timeView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER | Gravity.TOP, 0, 125, 10, 0));

        TextCell version = new TextCell(context);
        version.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), 100, 0));
        if (available) {
            version.setTextAndValueAndIcon(LocaleController.getString("UP_Version", R.string.UP_Version), args[0].replaceAll("v|-beta", ""), R.drawable.msg_info, true);
        } else {
            version.setTextAndValueAndIcon(LocaleController.getString("UP_CurrentVersion", R.string.UP_CurrentVersion), CherrygramExtras.INSTANCE.getCG_VERSION(), R.drawable.msg_info, false);
        }
        version.setOnClickListener(v -> copyText(version.getTextView().getText() + ": " + version.getValueTextView().getText()));
        linearLayout.addView(version);


        if (available) {
            TextCell size = new TextCell(context);
            size.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), 100, 0));
            size.setTextAndValueAndIcon(LocaleController.getString("UP_UpdateSize", R.string.UP_UpdateSize), args[2], R.drawable.msg_sendfile, true);
            size.setOnClickListener(v -> copyText(size.getTextView().getText() + ": " + size.getValueTextView().getText()));
            linearLayout.addView(size);

            TextCell changelog = new TextCell(context);
            changelog.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), 100, 0));
            changelog.setTextAndIcon(LocaleController.getString("UP_Changelog", R.string.UP_Changelog), R.drawable.msg_log, false);
            changelog.setOnClickListener(v -> copyText(changelog.getTextView().getText() + "\n" + (isTranslated ? translatedC : UpdaterUtils.replaceTags(args[1]))));
            linearLayout.addView(changelog);

            changelogTextView = new TextView(context) {
                @Override
                protected void onDraw(Canvas canvas) {
                    super.onDraw(canvas);
                    if (!CherrygramConfig.INSTANCE.getDisableDividers()) canvas.drawLine(0, getMeasuredHeight() - 1, getMeasuredWidth(), getMeasuredHeight() - 1, Theme.dividerPaint);
                }
            };
            changelogTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
            changelogTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            changelogTextView.setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());
            changelogTextView.setLinkTextColor(Theme.getColor(Theme.key_dialogTextLink));
            changelogTextView.setText(UpdaterUtils.replaceTags(args[1]));
            changelogTextView.setPadding(AndroidUtilities.dp(21), 0, AndroidUtilities.dp(21), AndroidUtilities.dp(10));
            changelogTextView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            changelogTextView.setOnClickListener(v -> UpdaterUtils.translate(args[1], (String translated) -> {
                translatedC = translated;
                animateText(changelogTextView, UpdaterUtils.replaceTags(isTranslated ? args[1] : (String) translatedC));
                isTranslated ^= true;
            }, () -> {}));
            UpdaterUtils.translate(args[1], (String translated)  -> {
                translatedC = translated;
                animateText(changelogTextView, UpdaterUtils.replaceTags(isTranslated ? args[1] : (String) translatedC));
                isTranslated ^= true;
            }, () -> {});

            linearLayout.addView(changelogTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

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
                UpdaterUtils.downloadApk(context, args[3], "Cherrygram " + args[0]);
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

            TextCell clearUpdates = new TextCell(context) {
                @Override
                protected void onDraw(Canvas canvas) {
                    super.onDraw(canvas);
                    if (!CherrygramConfig.INSTANCE.getDisableDividers()) canvas.drawLine(0, getMeasuredHeight() - 1, getMeasuredWidth(), getMeasuredHeight() - 1, Theme.dividerPaint);
                }
            };
            clearUpdates.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), 100, 0));
            clearUpdates.setTextAndIcon(LocaleController.getString("UP_ClearUpdatesCache", R.string.UP_ClearUpdatesCache), R.drawable.msg_clear, false);
            clearUpdates.setOnClickListener(v -> {
                if (UpdaterUtils.getOtaDirSize().replaceAll("[^0-9]+", "").equals("0")) {
                    BulletinFactory.of(getContainer(), null).createErrorBulletin(LocaleController.getString("UP_NothingToClear", R.string.UP_NothingToClear)).show();
                } else {
                    BulletinFactory.of(getContainer(), null).createErrorBulletin(LocaleController.formatString("UP_ClearedUpdatesCache", R.string.UP_ClearedUpdatesCache, UpdaterUtils.getOtaDirSize())).show();
                    UpdaterUtils.cleanOtaDir();
                }
            });
            linearLayout.addView(clearUpdates);

            FrameLayout frameLayout2 = new FrameLayout(context);
            frameLayout2.setBackground(Theme.AdaptiveRipple.filledRect(Theme.getColor(Theme.key_featuredStickers_addButton), 6));
            linearLayout.addView(frameLayout2, LayoutHelper.createFrame(-1, 48, 0, 16, 15, 16, 16));

            TextView checkUpdatesButton = new TextView(context);
            checkUpdatesButton.setLines(1);
            checkUpdatesButton.setSingleLine(true);
            checkUpdatesButton.setEllipsize(TextUtils.TruncateAt.END);
            checkUpdatesButton.setGravity(Gravity.CENTER);
            checkUpdatesButton.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
            checkUpdatesButton.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
            checkUpdatesButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            checkUpdatesButton.setText(LocaleController.getString("UP_CheckForUpdates", R.string.UP_CheckForUpdates));
            checkUpdatesButton.setOnClickListener(v -> {
                animateView(checkUpdatesButton, context, timeView);
            });

            frameLayout2.addView(checkUpdatesButton, LayoutHelper.createFrame(-1, -1, 17));
        }

        ScrollView scrollView = new ScrollView(context);
        scrollView.addView(linearLayout);
        setCustomView(scrollView);
    }

    private void animateText(View view, CharSequence charSequence) {
        AnimatorSet animatorSet2 = animatorSet;
        if (animatorSet2 != null) {
            animatorSet2.cancel();
        }
        if (view instanceof TextView) {
            ((TextView) view).setText(charSequence);
        }
        AnimatorSet animateButton = new AnimatorSet();
        animatorSet = animateButton;
        animateButton.setDuration(500);
        animatorSet.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        animatorSet.playTogether(ObjectAnimator.ofFloat(view, View.ALPHA, 0, 1), ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, (float) AndroidUtilities.dp(12), 0));
        animatorSet.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationCancel(Animator animator) {
                @SuppressLint("Recycle") AnimatorSet animatorSet = new AnimatorSet();
                if (animatorSet.equals(animator)) {
                    animatorSet = null;
                }
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                @SuppressLint("Recycle") AnimatorSet animatorSet = new AnimatorSet();
                if (animatorSet.equals(animator)) {
                    animatorSet = null;
                }
            }

        });
        animatorSet.start();
    }

    void animateView(TextView textView, Context context, SimpleTextView simpleTextView) {
        animateText(textView,"");

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(".  ");
        spannableStringBuilder.setSpan(new ColoredImageSpan(Objects.requireNonNull(ContextCompat.getDrawable(getContext(), R.drawable.sync_outline_28))), 0, 1, 0);
        textView.setText(spannableStringBuilder);

        UpdaterUtils.checkUpdates(context, true, () -> {
            BulletinFactory.of(getContainer(), null).createErrorBulletin(LocaleController.getString("UP_Not_Found", R.string.UP_Not_Found)).show();
            simpleTextView.setText(LocaleController.getString("UP_LastCheck", R.string.UP_LastCheck) + ": " + LocaleController.formatDateTime(CherrygramConfig.INSTANCE.getLastUpdateCheckTime() / 1000));
            animateText(textView, LocaleController.getString("UP_CheckForUpdates", R.string.UP_CheckForUpdates));
        }, this::dismiss);
    }

    private void copyText(CharSequence text) {
        AndroidUtilities.addToClipboard(text);
        BulletinFactory.of(getContainer(), null).createCopyBulletin(LocaleController.getString("TextCopied", R.string.TextCopied)).show();
    }
    
}
