/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.crashlytics;

import static org.telegram.messenger.LocaleController.getString;

import android.app.Activity;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.StickerImageView;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;

import uz.unnarsx.cherrygram.helpers.ui.OnceBottomSheetHelper;

public class CrashReportBottomSheet extends OnceBottomSheetHelper {

    public CrashReportBottomSheet(BaseFragment fragment) {
        super(fragment.getParentActivity(), false);

        Activity activity = fragment.getParentActivity();

        FrameLayout frameLayout = new FrameLayout(activity);
        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        frameLayout.addView(linearLayout);

        StickerImageView imageView = new StickerImageView(activity, currentAccount);
        imageView.setStickerPackName("HotCherry");
        imageView.setStickerNum(30);
        imageView.getImageReceiver().setAutoRepeat(1);
        linearLayout.addView(imageView, LayoutHelper.createLinear(200, 200, Gravity.CENTER_HORIZONTAL, 0, 16, 0, 0));

        TextView title = new TextView(activity);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        title.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        title.setTypeface(AndroidUtilities.bold());
        title.setText(getString(R.string.CG_AppCrashed));
        linearLayout.addView(title, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 21, 20, 21, 0));

        TextView description = new TextView(activity);
        description.setGravity(Gravity.CENTER_HORIZONTAL);
        description.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        description.setTextColor(Theme.getColor(Theme.key_dialogTextGray3));
        description.setText(getString(R.string.CG_AppCrashedDesc));
        linearLayout.addView(description, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 21, 15, 21, 16));

        ButtonWithCounterView sendLogsButton = new ButtonWithCounterView(getContext(), resourcesProvider);
        sendLogsButton.setRound();
        sendLogsButton.setText(getString(R.string.DebugSendLogs));
        sendLogsButton.setOnClickListener(view -> {
            Crashlytics.sendCrashLogs(activity, this);
        });
        linearLayout.addView(sendLogsButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, 0, 16, 8, 16, 16));

        ButtonWithCounterView cancelButton = new ButtonWithCounterView(getContext(), false, true, resourcesProvider);
        cancelButton.setRoundRadius(24);
        cancelButton.text.setTypeface(AndroidUtilities.bold());
        cancelButton.setText(getString(R.string.Cancel));
        cancelButton.setOnClickListener(view -> {
            dismiss();
            Crashlytics.deleteCrashLogs();
        });
        linearLayout.addView(cancelButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, 0, 16, 0, 16, 16));

        ScrollView scrollView = new ScrollView(activity);
        scrollView.addView(frameLayout);
        setCustomView(scrollView);

        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("crash_screen");
    }

    public static void checkBottomSheet(BaseFragment fragment) {
        try {
            CrashReportBottomSheet dialog = new CrashReportBottomSheet(fragment);
            dialog.setCancelable(false);
            dialog.show();
        } catch (Exception ignored) {
        }
    }

}
