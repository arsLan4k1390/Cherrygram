/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.donates.adsgram;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import uz.unnarsx.cherrygram.core.CherrygramLogger;

@SuppressLint("ViewConstructor")
public class AdsGramCell extends FrameLayout {

    public enum ViewType {
        BANNER_COMPACT,
        BANNER_LARGE
    }

    private final ViewType viewType;
    private final BaseFragment fragment;

    private BackupImageView icon;
    private TextView title;
    private TextView description;
    private TextView adBadge;

    private BackupImageView bigBanner;
    private ButtonWithCounterView actionButton;

    private AdsGramResponse ad;
    private String clickUrl;

    private boolean renderSent;
    private boolean showSent;
    private boolean clicked;

    private Runnable showRunnable;

    public AdsGramCell(Context context, BaseFragment fragment, ViewType viewType) {
        super(context);
        this.fragment = fragment;
        this.viewType = viewType;

        setPadding(dp(12), dp(12), dp(12), dp(12));

        Drawable background = Theme.createSimpleSelectorRoundRectDrawable(
                dp(12),
                Theme.getColor(Theme.key_windowBackgroundWhite),
                ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_listSelector), 80)
        );
        setBackground(background);

        setClickable(true);
        setFocusable(true);

        if (viewType == ViewType.BANNER_LARGE) {
            initBannerLayout(context);
            setClickable(false);
            setOnClickListener(null);
        } else {
            initCompactLayout(context);
            setOnClickListener(v -> handleAdClick());
        }
    }

    private void initCompactLayout(Context context) {
        LinearLayout cell = new LinearLayout(context);
        cell.setOrientation(LinearLayout.HORIZONTAL);
        cell.setGravity(Gravity.CENTER_VERTICAL);
        addView(cell, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        icon = new BackupImageView(context);
        icon.setRoundRadius(dp(10));
        cell.addView(icon, LayoutHelper.createLinear(48, 48));

        LinearLayout textColumn = new LinearLayout(context);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        cell.addView(textColumn, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f, Gravity.CENTER_VERTICAL, 12, 0, 12, 0));

        title = new TextView(context);
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        title.setTypeface(AndroidUtilities.bold());
        title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        title.setMaxLines(2);
        title.setEllipsize(TextUtils.TruncateAt.END);
        textColumn.addView(title);

        description = new TextView(context);
        description.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        description.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        description.setMaxLines(20);
        description.setEllipsize(TextUtils.TruncateAt.END);
        textColumn.addView(description);

        adBadge = new TextView(context);
        adBadge.setText("AD • 18+");
        adBadge.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        adBadge.setTypeface(AndroidUtilities.bold());
        adBadge.setTextColor(Color.WHITE);
        adBadge.setPadding(dp(10), dp(5), dp(10), dp(5));

        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setColor(Theme.getColor(Theme.key_featuredStickers_addButton));
        badgeBg.setCornerRadius(dp(999));
        adBadge.setBackground(badgeBg);
        cell.addView(adBadge);
    }

    private void initBannerLayout(Context context) {
        LinearLayout cell = new LinearLayout(context);
        cell.setOrientation(LinearLayout.VERTICAL);
        addView(cell, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        LinearLayout headerRow = new LinearLayout(context);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);
        cell.addView(headerRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0, 0, 0, 10));

        icon = new BackupImageView(context);
        icon.setRoundRadius(dp(10));
        headerRow.addView(icon, LayoutHelper.createLinear(40, 40));

        LinearLayout headerTextColumn = new LinearLayout(context);
        headerTextColumn.setOrientation(LinearLayout.VERTICAL);
        headerRow.addView(headerTextColumn, LayoutHelper.createLinear(0, LayoutParams.WRAP_CONTENT, 1f, Gravity.CENTER_VERTICAL, 10, 0, 10, 0));

        title = new TextView(context);
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        title.setTypeface(AndroidUtilities.bold());
        title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        title.setMaxLines(2);
        title.setEllipsize(TextUtils.TruncateAt.END);
        headerTextColumn.addView(title);

        adBadge = new TextView(context);
        adBadge.setText("AD • 18+");
        adBadge.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        adBadge.setTypeface(AndroidUtilities.bold());
        adBadge.setTextColor(Color.WHITE);
        adBadge.setPadding(dp(10), dp(5), dp(10), dp(5));

        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setColor(Theme.getColor(Theme.key_featuredStickers_addButton));
        badgeBg.setCornerRadius(dp(999));
        adBadge.setBackground(badgeBg);
        headerRow.addView(adBadge);

        bigBanner = new BackupImageView(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int width = MeasureSpec.getSize(widthMeasureSpec);
                int height = width;
//                int height = (int) (width * 9f / 21f);
                super.onMeasure(
                        MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
                );
            }
        };
        bigBanner.setRoundRadius(dp(8));
        cell.addView(bigBanner, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0, 4, 0, 10));

        description = new TextView(context);
        description.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        description.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        description.setMaxLines(20);
        description.setEllipsize(TextUtils.TruncateAt.END);
        cell.addView(description, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0, 0, 0, 12));

        actionButton = new ButtonWithCounterView(context, null).setRound();
        actionButton.setOnClickListener(v -> handleAdClick());
        cell.addView(actionButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
    }

    public void setAd(AdsGramResponse ad) {
        this.ad = ad;
        renderSent = false;
        showSent = false;
        clicked = false;

        if (ad == null || ad.banner == null) {
            return;
        }

        title.setText(ad.banner.getAsset("title"));

        clickUrl = ad.banner.getAsset("url");

        String desc = ad.banner.getAsset("description");
        if (TextUtils.isEmpty(desc)) {
            description.setVisibility(GONE);
        } else {
            description.setVisibility(VISIBLE);
            description.setText(desc);
        }

        String iconUrl = ad.banner.getAsset("icon");
        String imageUrl = ad.banner.getAsset("image");

        if (viewType == ViewType.BANNER_LARGE) {
            if (icon != null) {
                icon.setVisibility(GONE);
            }

            if (!TextUtils.isEmpty(iconUrl) && bigBanner != null) {
                bigBanner.setVisibility(VISIBLE);
                if (!TextUtils.isEmpty(imageUrl)) {
                    bigBanner.setImage(ImageLocation.getForPath(imageUrl), "512_512", null, null, null, 0);
                } else {
                    bigBanner.setImage(ImageLocation.getForPath(iconUrl), "512_512", null, null, null, 0);
                }
            } else if (bigBanner != null) {
                bigBanner.setVisibility(GONE);
            }

            if (actionButton != null) {
                String buttonName = ad.banner.getAsset("buttonName");
                if (buttonName != null && !TextUtils.isEmpty(buttonName)) {
                    actionButton.setText(buttonName);
                } else {
                    actionButton.setText(getString(R.string.Open));
                }
            }
        } else {
            if (!TextUtils.isEmpty(iconUrl) && icon != null) {
                icon.setVisibility(VISIBLE);
                icon.setImage(ImageLocation.getForPath(iconUrl), "100_100", null, null, null, 0);
            }
        }
    }

    private void handleAdClick() {
        if (ad == null || ad.banner == null || clickUrl == null) {
            return;
        }

        /*String clickTracking = ad.banner.getTracking("clickPixel");
        openTracking(clickTracking);
        CherrygramLogger.d("ADSgram", () -> "clickPixel request sent");*/

        clicked = true;

        AlertDialog progressDialog = new AlertDialog(
                fragment.getParentActivity(),
                AlertDialog.ALERT_TYPE_SPINNER,
                fragment.getResourceProvider()
        );

        AndroidUtilities.runOnUIThread(() -> {
            try {
                if (!fragment.getParentActivity().isFinishing()) progressDialog.show();
            } catch (Exception e) {
                CherrygramLogger.e(e);
            }
        });

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .followRedirects(false)
                .followSslRedirects(false)
                .build();

        Request request = new Request.Builder().url(clickUrl).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                CherrygramLogger.e(e);
                dismissDialog(progressDialog);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                dismissDialog(progressDialog);
                try (response) {
                    String redirect = response.header("Location");
                    if (redirect == null) return;

                    AndroidUtilities.runOnUIThread(() -> {
                        try {
                            Browser.openAsInternalIntent(fragment.getContext(), redirect);
                        } catch (Exception e) {
                            CherrygramLogger.e(e);
                        }
                    });
                }
            }
        });
    }

    private void dismissDialog(AlertDialog dialog) {
        AndroidUtilities.runOnUIThread(() -> {
            try {
                if (!fragment.getParentActivity().isFinishing() && dialog.isShowing()) dialog.dismiss();
            } catch (Exception e) {
                CherrygramLogger.e(e);
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (ad == null || ad.banner == null) {
            return;
        }

        if (!renderSent) {
            renderSent = true;

            String renderTracking = ad.banner.getTracking("render");
            openTracking(renderTracking);
            CherrygramLogger.d("ADSgram", () -> "render request sent");
        }

        if (!showSent) {
            AndroidUtilities.cancelRunOnUIThread(showRunnable);

            showRunnable = () -> {
                if (!isAttachedToWindow()) {
                    return;
                }
                showSent = true;

                String showTracking = ad.banner.getTracking("show");
                openTracking(showTracking);
                CherrygramLogger.d("ADSgram", () -> "show request sent");
            };
            AndroidUtilities.runOnUIThread(showRunnable, 2000);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (showRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(showRunnable);
        }
    }

    public static void openTracking(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        try {
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            new OkHttpClient()
                    .newCall(request)
                    .enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            CherrygramLogger.e(e, true);
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) {
                            response.close();
                        }
                    });
        } catch (Exception e) {
            CherrygramLogger.e(e);
        }
    }

}