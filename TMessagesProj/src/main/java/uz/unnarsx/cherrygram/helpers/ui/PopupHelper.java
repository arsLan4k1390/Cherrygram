/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.helpers.ui;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.Uri;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.StringRes;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BotWebViewVibrationEffect;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.RadioColorCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.Components.ScaleStateListAnimator;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;

import java.util.ArrayList;
import java.util.Set;

import uz.unnarsx.cherrygram.core.ui.CGBulletinCreator;
import uz.unnarsx.cherrygram.donates.DonatesManager;
import uz.unnarsx.cherrygram.misc.CherrygramExtras;
import uz.unnarsx.cherrygram.preferences.ChatsPreferencesEntry;

public class PopupHelper {

    public static void show(ArrayList<? extends CharSequence> entries, String title, int checkedIndex, Context context, OnItemClickListener listener) {
        show(entries, title, checkedIndex, context, listener, null);
    }

    public static void show(ArrayList<? extends CharSequence> entries, String title, int checkedIndex, Context context, OnItemClickListener listener, Theme.ResourcesProvider resourcesProvider) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, resourcesProvider);
        builder.setTitle(title);
        final LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        builder.setView(linearLayout);

        for (int a = 0; a < entries.size(); a++) {
            RadioColorCell cell = new RadioColorCell(context);
            cell.setPadding(dp(4), 0, dp(4), 0);
            cell.setTag(a);
            cell.setCheckColor(Theme.getColor(Theme.key_radioBackground, resourcesProvider), Theme.getColor(Theme.key_dialogRadioBackgroundChecked, resourcesProvider));
            cell.setTextAndValue(entries.get(a), checkedIndex == a);
            linearLayout.addView(cell);
            cell.setOnClickListener(v -> {
                Integer which = (Integer) v.getTag();
                builder.getDismissRunnable().run();
                listener.onClick(which);
            });
        }
        builder.setNegativeButton(getString(R.string.Cancel), null);
        builder.show();
    }

    public static void show(
            String title,
            ArrayList<String> prefTitle,
            ArrayList<String> prefDesc,
            int checkedIndex,
            Context context,
            OnItemClickListener listener,
            Theme.ResourcesProvider resourcesProvider
    ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, resourcesProvider);
        builder.setTitle(title);
        final LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        builder.setView(linearLayout);

        for (int a = 0; a < prefTitle.size(); a++) {
            RadioColorCell cell = new RadioColorCell(context);
            cell.setPadding(dp(4), 0, dp(4), 0);
            cell.setTag(a);
            cell.setCheckColor(Theme.getColor(Theme.key_radioBackground, resourcesProvider), Theme.getColor(Theme.key_dialogRadioBackgroundChecked, resourcesProvider));
            cell.setTextAndText2AndValue(prefTitle.get(a), prefDesc.get(a), checkedIndex == a);
            linearLayout.addView(cell);
            cell.setOnClickListener(v -> {
                Integer which = (Integer) v.getTag();
                builder.getDismissRunnable().run();
                listener.onClick(which);
            });
        }
        builder.setNegativeButton(getString(R.string.Cancel), null);
        builder.show();
    }

    public interface OnItemClickListener {
        void onClick(int i);
    }

    public static void showSwitchAlert(
            String title,
            BaseFragment fragment,
            ArrayList<String> prefTitle,
            ArrayList<Integer> prefIcon,
            ArrayList<Boolean> prefCheck,
            ArrayList<Boolean> prefCheckInvisible,
            ArrayList<Boolean> donateLock,
            ArrayList<Boolean> prefDivider,
            ArrayList<Runnable> clickListener,
            Runnable dismissRunnable
    ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getContext(), fragment.getResourceProvider());
        builder.setTitle(title);
        final LinearLayout linearLayout = new LinearLayout(fragment.getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        for (int a = 0; a < prefTitle.size(); a++) {
            TextCell textCell = new TextCell(fragment.getContext(), 23, false, true, fragment.getResourceProvider());
            textCell.setTextAndCheckAndIcon(prefTitle.get(a), prefCheck.get(a), prefIcon.get(a), prefDivider.get(a));

            textCell.setTag(a);
            textCell.setBackground(Theme.getSelectorDrawable(false));

            if (prefCheckInvisible != null && prefCheckInvisible.get(a)) {
                textCell.getCheckBox().setVisibility(View.INVISIBLE);
            }

            boolean requireDonate = donateLock != null && donateLock.get(a) && !DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace();
            if (requireDonate) textCell.setCheckBoxIcon(R.drawable.permission_locked);

            linearLayout.addView(textCell);
            int finalA = a;
            textCell.setOnClickListener(view -> {
                if (requireDonate) {
                    AndroidUtilities.shakeViewSpring(view);
                    BotWebViewVibrationEffect.APP_ERROR.vibrate();
                    Bulletin.BulletinWindow.BulletinWindowLayout window = Bulletin.BulletinWindow.make(fragment.getParentActivity());
                    window.setTouchable(true);

                    CGBulletinCreator.INSTANCE.createRequireDonateBulletin(fragment);
                } else {
                    boolean newValue = !prefCheck.get(finalA);
                    prefCheck.set(finalA, newValue);
                    textCell.setChecked(newValue);
                    clickListener.get(finalA).run();
                }
            });
        }

        builder.setView(linearLayout);

        if (title.equals(getString(R.string.CP_AdminActions))) {
            builder.setNegativeButton(getString(R.string.Back),
                    ((dialog, which) -> ChatsPreferencesEntry.showChatMenuItemsConfigurator(fragment))
            );
        }

        builder.setPositiveButton(getString(R.string.Close), dismissRunnable != null ? (dialogInterface, i) -> dismissRunnable.run() : null);
        if (dismissRunnable != null) builder.setOnDismissListener(v -> dismissRunnable.run());
        fragment.showDialog(builder.create());
    }

    private static final Set<String> TELEGRAM_PACKAGES = Set.of("org.telegram.messenger", "org.telegram.messenger.web", "org.telegram.messenger.beta");

    private static Intent findOfficialTelegram(Context context, String uri) {
        var pm = context.getPackageManager();
        var intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(uri));
        var activities = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (var info : activities) {
            if (TELEGRAM_PACKAGES.contains(info.activityInfo.packageName)) {
                intent.setPackage(info.activityInfo.packageName);
                return intent;
            }
        }
        return null;
    }

    public static void showBlameAlert(Context context, @StringRes int text, String uri) {
        var linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        var scrollView = new ScrollView(context);
        scrollView.addView(linearLayout);

        var title = new TextView(context);
        title.setGravity(Gravity.START);
        title.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        title.setTypeface(AndroidUtilities.bold());
        title.setText(AndroidUtilities.replaceTags(getString(R.string.SubscribeToPremiumOfficialAppNeeded)));
        linearLayout.addView(title, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 21, 16, 21, 0));

        var description = new TextView(context);
        description.setGravity(Gravity.START);
        description.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        description.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        description.setText(AndroidUtilities.replaceTags(getString(text)));
        linearLayout.addView(description, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 21, 15, 21, 16));

        var buttonTextView = new ButtonWithCounterView(context, true, null).setRound();
        var officialIntent = findOfficialTelegram(context, uri);
        if (officialIntent != null) {
            buttonTextView.setText(getString(R.string.CG_OpenOfficialApp));
            buttonTextView.setOnClickListener(v -> {
                try {
                    context.startActivity(officialIntent);
                } catch (ActivityNotFoundException e) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=org.telegram.messenger")));
                }
            });
        } else {
            buttonTextView.setText(getString(R.string.InstallOfficialApp));
            buttonTextView.setOnClickListener(v -> context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=org.telegram.messenger"))));
        }
        linearLayout.addView(buttonTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM, 21, 0, 21, 16));

        var sheet = new BottomSheet(context, false);
        sheet.setCustomView(scrollView);
        sheet.show();
    }

    public static void showLoginWarning(Context context, Runnable onConfirm) {
        showWarning(
                context,
                R.raw.ic_ban,
                getString(R.string.Warning),
                AndroidUtilities.replaceTags(getString(R.string.CG_AddAccountWarning)),
                getString(R.string.GotIt),
                5,
                false,
                onConfirm
        );
    }

    public static void showWarning(
            Context context,
            int resID,
            String title,
            CharSequence message,
            String buttonText,
            int timerInSeconds,
            boolean kaboom,
            Runnable onConfirm
    ) {
        if (!kaboom && CherrygramExtras.isUserAccountAdded(1714120111L) && onConfirm != null) {
            onConfirm.run();
            return;
        }
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        RLottieImageView imageView = new RLottieImageView(context);
        imageView.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN));
        imageView.setAnimation(resID, 50, 50);
        imageView.playAnimation();
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setBackground(Theme.createCircleDrawable(dp(80), Theme.getColor(Theme.key_windowBackgroundWhiteValueText)));
        if (kaboom) {
            imageView.setAnimation(resID, 70, 70);
            imageView.setBackground(Theme.createCircleDrawable(dp(80), Theme.getColor(Theme.key_fill_RedNormal)));
        }
        linearLayout.addView(imageView, LayoutHelper.createLinear(80, 80, Gravity.CENTER, 0, 14, 0, 0));

        TextView headerTextView = new TextView(context);
        headerTextView.setTypeface(AndroidUtilities.bold());
        headerTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        headerTextView.setGravity(Gravity.CENTER);
        headerTextView.setText(title);
        headerTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        linearLayout.addView(headerTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 28, 14, 28, 0));

        TextView messageTextView = new TextView(context);
        messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        messageTextView.setGravity(Gravity.CENTER);
        messageTextView.setText(message);
        messageTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        linearLayout.addView(messageTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 40, 9, 40, 0));

        ButtonWithCounterView button = new ButtonWithCounterView(context, null).setRound();
        ScaleStateListAnimator.apply(button, 0.02f, 1.5f);
        button.setText(buttonText, true);
        if (kaboom) {
            button.setColor(Theme.getColor(Theme.key_fill_RedNormal));
        }
        linearLayout.addView(button, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, 14, 20, 14, 4));

        final BottomSheet sheet = new BottomSheet.Builder(context)
                .setCustomView(linearLayout)
                .show();

        sheet.setCanDismissWithSwipe(false);
        sheet.setCanDismissWithTouchOutside(false);
        button.setTimer(timerInSeconds, () -> {
            sheet.setCanDismissWithSwipe(true);
            sheet.setCanDismissWithTouchOutside(true);
        });
        button.setOnClickListener(v -> {
            if (button.isTimerActive()) {
                AndroidUtilities.shakeViewSpring(button, 3);
                BotWebViewVibrationEffect.APP_ERROR.vibrate();
            } else {
                sheet.dismiss();
                if (onConfirm != null) {
                    onConfirm.run();
                }
            }
        });
    }

}

