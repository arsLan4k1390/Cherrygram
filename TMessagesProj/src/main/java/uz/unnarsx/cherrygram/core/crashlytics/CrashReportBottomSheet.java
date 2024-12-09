package uz.unnarsx.cherrygram.core.crashlytics;

import static org.telegram.messenger.LocaleController.getString;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.content.FileProvider;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.StickerImageView;
import org.telegram.ui.LaunchActivity;

import java.io.File;
import java.io.IOException;

import uz.unnarsx.cherrygram.core.helpers.FirebaseAnalyticsHelper;
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

        TextView buttonTextView = new TextView(activity);
        buttonTextView.setPadding(AndroidUtilities.dp(34), 0, AndroidUtilities.dp(34), 0);
        buttonTextView.setGravity(Gravity.CENTER);
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        buttonTextView.setTypeface(AndroidUtilities.bold());
        buttonTextView.setText(getString(R.string.DebugSendLogs));
        buttonTextView.setOnClickListener(view -> {
            try {
                File cacheFile = Crashlytics.shareLogs();
                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(activity, ApplicationLoader.getApplicationId() + ".provider", cacheFile);
                } else {
                    uri = Uri.fromFile(cacheFile);
                }
                Intent i = new Intent(Intent.ACTION_SEND);
                if (Build.VERSION.SDK_INT >= 24) {
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_SUBJECT, Crashlytics.getCrashReportMessage());
                i.putExtra(Intent.EXTRA_STREAM, uri);
                i.setClass(activity, LaunchActivity.class);
                activity.startActivity(i);
                dismiss();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        buttonTextView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(6), Theme.getColor(Theme.key_featuredStickers_addButton), ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_windowBackgroundWhite), 120)));
        linearLayout.addView(buttonTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, 0, 16, 15, 16, 8));

        TextView textView = new TextView(activity);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setTypeface(AndroidUtilities.bold());
        textView.setText(getString(R.string.Cancel));
        textView.setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton));

        linearLayout.addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, 0, 16, 0, 16, 0));

        textView.setOnClickListener(view -> {
            dismiss();
            Crashlytics.deleteCrashLogs();
        });

        ScrollView scrollView = new ScrollView(activity);
        scrollView.addView(frameLayout);
        setCustomView(scrollView);

        FirebaseAnalyticsHelper.trackEventWithEmptyBundle("crash_screen");
    }

    public static void checkBottomSheet(BaseFragment fragment) {
        try {
            CrashReportBottomSheet dialog = new CrashReportBottomSheet(fragment);
            dialog.setCancelable(false);
            dialog.show();
            dialog.setOnDismissListener(v -> Crashlytics.deleteCrashLogs());
        } catch (Exception ignored) {
        }
    }
}
