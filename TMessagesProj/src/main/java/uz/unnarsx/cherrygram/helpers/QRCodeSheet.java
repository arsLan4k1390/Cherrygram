/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.helpers;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.RectF;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LinkifyPort;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.ColoredImageSpan;
import org.telegram.ui.Components.HintView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ScaleStateListAnimator;
import org.telegram.ui.Components.StickerImageView;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import uz.unnarsx.cherrygram.chats.helpers.ChatsHelper;

public class QRCodeSheet extends BottomSheet {

    private static final String AUTH_TOKEN_PREFIX = "tg://login?token=";
    private static final String PHONE_PREFIX = "tel:";
    private static final String WIFI_PREFIX = "WIFI:";

    private final int TEXT_TYPE_LINK = 0;
    private final int TEXT_TYPE_TEXT = 1;
    private final int TEXT_TYPE_AUTH_TOKEN = 2;
    private final int TEXT_TYPE_PHONE = 3;
    private final int TEXT_TYPE_WIFI = 4;

    private final BaseFragment fragment;
    private String password;
    private String ssid;
    private String wifiAuthType = "WPA";

    public QRCodeSheet(BaseFragment fragment, String text) {
        super(fragment.getParentActivity(), false, fragment.getResourceProvider());
        this.fragment = fragment;

        fixNavigationBar();
        setCanDismissWithSwipe(false);

        int textType;
        CharSequence primaryButtonText;
        CharSequence secondaryButtonText;

        if (text.startsWith(AUTH_TOKEN_PREFIX)) {
            textType = TEXT_TYPE_AUTH_TOKEN;
            primaryButtonText = getString(R.string.Cancel);
            secondaryButtonText = getString(R.string.Allow);
        } else {
            Matcher matcher = org.telegram.messenger.LinkifyPort.WEB_URL.matcher(text);
            boolean isWebUrl = matcher.matches();
            if (!isWebUrl && !text.startsWith(PHONE_PREFIX)) {
                if (text.startsWith(WIFI_PREFIX)) {
                    parseWifiInfo(text);
                    textType = TEXT_TYPE_WIFI;
                    primaryButtonText = getString(R.string.CG_WifiConnect);
                } else {
                    textType = TEXT_TYPE_TEXT;
                    primaryButtonText = getTextWithIcon("copy");
                }
            } else {
                textType = text.startsWith(PHONE_PREFIX) ? TEXT_TYPE_PHONE : TEXT_TYPE_LINK;
                primaryButtonText = buildOpenButtonText(ensureUrlHasHttps(text));
            }
            secondaryButtonText = getTextWithIcon("share");
        }

        String actionText = text;
        CharSequence displayText = buildDisplayText(textType, text);
        if (textType == TEXT_TYPE_LINK) {
            actionText = ensureUrlHasHttps(text);
        }

        final String finalActionText = actionText;
        Activity activity = fragment.getParentActivity();
        fixNavigationBar();

        FrameLayout root = new FrameLayout(activity);

        LinearLayout contentLayout = new LinearLayout(activity);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        root.addView(contentLayout);

        contentLayout.addView(
                new SheetHandleView(activity),
                LayoutHelper.createLinear(36, 4, Gravity.CENTER_HORIZONTAL, 18, 2, 18, 0)
        );

        if (textType == TEXT_TYPE_AUTH_TOKEN) {
            StickerImageView authImageView = new StickerImageView(activity, currentAccount);
            authImageView.setStickerPackName("tg_placeholders_android");
            authImageView.setStickerNum(6);
            authImageView.getImageReceiver().setAutoRepeat(1);
            authImageView.getImageReceiver().setAutoRepeatCount(1);
            contentLayout.addView(
                    authImageView,
                    LayoutHelper.createLinear(144, 144, Gravity.CENTER_HORIZONTAL, 0, 20, 0, 10)
            );
        } else {
            TextView hintTextView = new TextView(activity);
            hintTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            hintTextView.setTextSize(1, 14);
            hintTextView.setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteGrayText));
            hintTextView.setText(getString(R.string.CG_QR_Hint));
            contentLayout.addView(
                    hintTextView,
                    LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 15, 15, 15, 5)
            );

            ImageView qrImageView = new ImageView(activity);
            ScaleStateListAnimator.apply(qrImageView, 0.03f, 1.2f);
            qrImageView.setScaleType(ImageView.ScaleType.FIT_XY);
            qrImageView.setOutlineProvider(new RoundedQrOutlineProvider());
            qrImageView.setClipToOutline(true);

            Bitmap qrBitmap = QrHelper.createQR(text);
            qrImageView.setImageBitmap(qrBitmap);
            qrImageView.setOnClickListener(view -> {
                if (qrBitmap != null) {
                    copyQR(qrBitmap, false, activity);
                }
            });
            qrImageView.setOnLongClickListener(view -> {
                if (qrBitmap != null) {
                    copyQR(qrBitmap, true, activity);
                }
                return true;
            });

            contentLayout.addView(
                    qrImageView,
                    LayoutHelper.createLinear(230, 230, Gravity.CENTER_HORIZONTAL, 18, 20, 18, 15)
            );
        }

        TextView textView = new TextView(activity);
        ScaleStateListAnimator.apply(textView, 0.02f, 1.5f);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setTextSize(1, 14);
        textView.setPadding(
                dp(8),
                dp(4),
                dp(8),
                dp(4)
        );
        textView.setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteGrayText));
        textView.setText(displayText);

        if (textType != TEXT_TYPE_AUTH_TOKEN) {
            int selectorColor = Theme.multAlpha(getThemedColor(Theme.key_windowBackgroundWhiteGrayText), Theme.isCurrentThemeDark() ? 0.2f : 0.15f);
            textView.setBackground(Theme.createSelectorDrawable(selectorColor, 7, dp(8)));
            textView.setOnClickListener(view -> {
                if (AndroidUtilities.addToClipboard(finalActionText)) {
                    showCopyBulletin(true);
                }
            });
        }

        ScrollView scrollView = new ScrollView(activity);
        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.addView(
                textView,
                LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT)
        );

        textView.measure(
                View.MeasureSpec.makeMeasureSpec(AndroidUtilities.displaySize.x - dp(42), View.MeasureSpec.AT_MOST),
                View.MeasureSpec.UNSPECIFIED
        );

        int textHeight = textView.getMeasuredHeight();
        int maxHeight = (int) (AndroidUtilities.displaySize.y * 0.4f);

        LinearLayout.LayoutParams scrollParams = LayoutHelper.createLinear(
                LayoutHelper.WRAP_CONTENT,
                LayoutHelper.WRAP_CONTENT,
                Gravity.CENTER_HORIZONTAL,
                21, 2, 21, 8
        );
        scrollParams.height = Math.min(textHeight, maxHeight);
        contentLayout.addView(scrollView, scrollParams);

        LinearLayout buttonsLayout = new LinearLayout(activity);
        buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
        contentLayout.addView(
                buttonsLayout,
                LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, 16, 15, 16, 4)
        );

        ButtonWithCounterView primaryButton = new ButtonWithCounterView(activity, fragment.getResourceProvider());
        primaryButton.setRound();
        primaryButton.setText(primaryButtonText, false);
        primaryButton.setOnClickListener(view -> handlePrimaryAction(textType, finalActionText));
        if (textType == TEXT_TYPE_AUTH_TOKEN) {
            primaryButton.setNeutral();
        }

        View spacer = new View(activity);

        ButtonWithCounterView secondaryButton = new ButtonWithCounterView(activity, fragment.getResourceProvider());
        secondaryButton.setRound();
        secondaryButton.setText(secondaryButtonText, false);
        secondaryButton.setFilled(true);
        secondaryButton.setOnClickListener(view -> handleSecondaryAction(textType, finalActionText, fragment));

        ButtonWithCounterView first = textType == TEXT_TYPE_AUTH_TOKEN ? primaryButton : secondaryButton;
        ButtonWithCounterView second = textType == TEXT_TYPE_AUTH_TOKEN ? secondaryButton : primaryButton;

        buttonsLayout.addView(first, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0F));
        buttonsLayout.addView(spacer, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 0.06f));
        buttonsLayout.addView(second, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0F));

        setCustomView(root);
    }

    private CharSequence buildOpenButtonText(String actionText) {
        SpannableStringBuilder builder = new SpannableStringBuilder(getString(R.string.Open));

        builder.append("..");

        int start = builder.length() - 2;
        int end = builder.length();

        builder.setSpan(new DialogCell.FixedWidthSpan(dp(4)), start, start + 1, 0);

        Uri uri = Uri.parse(actionText);
        String host = uri.getHost();
        boolean isTelegramLink = host != null && isTelegramLink(host.toLowerCase());

        ColoredImageSpan arrowSpan = new ColoredImageSpan(
                ContextCompat.getDrawable(
                        fragment.getParentActivity(),
                        isTelegramLink ? R.drawable.filter_all : R.drawable.settings_language
                )
        );
        builder.setSpan(arrowSpan, start + 1, end, 0);

        return builder;
    }

    private Spanned getTextWithIcon(String iconType) {
        boolean isCopy = "copy".equals(iconType);
        int iconRes = isCopy ? R.drawable.msg_copy_filled : R.drawable.msg_share_filled;
        int textRes = isCopy ? R.string.LinkActionCopy : R.string.LinkActionShare;

        SpannableStringBuilder builder;

        if (isCopy) {
            builder = new SpannableStringBuilder(getString(textRes));
            builder.append("..");

            int start = builder.length() - 2;

            builder.setSpan(new DialogCell.FixedWidthSpan(dp(4)), start, start + 1, 0);

            ColoredImageSpan span = new ColoredImageSpan(ContextCompat.getDrawable(fragment.getParentActivity(), iconRes));
            builder.setSpan(span, start + 1, start + 2, 0);
        } else {
            builder = new SpannableStringBuilder();
            builder.append("..");

            ColoredImageSpan span = new ColoredImageSpan(ContextCompat.getDrawable(fragment.getParentActivity(), iconRes));
            builder.setSpan(span, 0, 1, 0);
            builder.setSpan(new DialogCell.FixedWidthSpan(dp(4)), 1, 2, 0);

            builder.append(getString(textRes));
        }

        return builder;
    }

    private CharSequence buildDisplayText(int textType, String originalText) {
        if (textType == TEXT_TYPE_WIFI && !TextUtils.isEmpty(ssid)) {
            String passwordSuffix = TextUtils.isEmpty(password) ? "" : ", Password: " + password;
            return MessageFormat.format("SSID: {0}{1}", ssid, passwordSuffix);
        }
        if (textType == TEXT_TYPE_AUTH_TOKEN) {
            return getString(R.string.CG_AreYouSureToLogin);
        }
        return originalText;
    }

    private void handlePrimaryAction(int textType, String actionText) {
        if (textType == TEXT_TYPE_AUTH_TOKEN) {
            dismiss();
            return;
        }

        if (textType == TEXT_TYPE_LINK || textType == TEXT_TYPE_PHONE) {
            Uri uri = Uri.parse(actionText);
            String host = uri.getHost();

            if (host != null) {
                if (isTelegramLink(host.toLowerCase())) {
                    dismiss();
                    Browser.openAsInternalIntent(fragment.getParentActivity(), actionText);
                    return;
                }
            }
            Browser.openUrl(fragment.getParentActivity(), uri);
        } else if (textType == TEXT_TYPE_TEXT) {
            if (AndroidUtilities.addToClipboard(actionText)) {
                showCopyBulletin(false);
            }
        } else if (textType == TEXT_TYPE_WIFI) {
            AndroidUtilities.runOnUIThread(this::connectToWifi, 750L);
        }

        dismiss();
    }

    private void handleSecondaryAction(int textType, String actionText, BaseFragment fragment) {
        if (textType == TEXT_TYPE_AUTH_TOKEN) {
            AndroidUtilities.runOnUIThread(() -> acceptLoginToken(actionText, fragment), 750L);
            dismiss();
            return;
        }

        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, actionText);

            Intent chooser = Intent.createChooser(shareIntent, getString(R.string.QrCode));
            fragment.startActivityForResult(chooser, 500);
        } catch (Exception e) {
            FileLog.e(e);
        }

        dismiss();
    }

    /** Auth start */
    private void acceptLoginToken(String actionText, BaseFragment fragment) {
        try {
            String token = actionText.substring(AUTH_TOKEN_PREFIX.length());
            token = token.replaceAll("/", "_");
            token = token.replaceAll("\\+", "-");

            byte[] decodedToken = Base64.decode(token, Base64.URL_SAFE);

            TLRPC.TL_auth_acceptLoginToken request = new TLRPC.TL_auth_acceptLoginToken();
            request.token = decodedToken;

            ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(request, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
                if (error != null) {
                    dismiss();
                    AlertsCreator.showSimpleAlert(
                            fragment,
                            getString(R.string.AuthAnotherClient),
                            error.text
                    );
                } else {
                    dismiss();
                }
            }));
        } catch (Exception e) {
            FileLog.e("Failed to pass qr code auth", e);
            AndroidUtilities.runOnUIThread(() -> AlertsCreator.showSimpleAlert(
                    fragment,
                    getString(R.string.AuthAnotherClient),
                    getString(R.string.ErrorOccurred)
            ));
        }
    }
    /** Auth finish */

    /** Wi-Fi start */
    private void parseWifiInfo(String text) {
        String wifiData = text.substring(text.indexOf(':') + 1);
        String[] parts = wifiData.split("(?<!\\\\);");
        for (String part : parts) {
            if (part.startsWith("S:")) {
                ssid = unescapeWifiString(part.substring(2));
            } else if (part.startsWith("P:")) {
                password = unescapeWifiString(part.substring(2));
            } else if (part.startsWith("T:")) {
                wifiAuthType = part.substring(2);
            }
        }
    }

    private String unescapeWifiString(String value) {
        return value.replace("\\\\", "\\")
                .replace("\\;", ";")
                .replace("\\:", ":")
                .replace("\\,", ",")
                .replace("\\\"", "\"");
    }

    private void connectToWifi() {
        if (TextUtils.isEmpty(ssid)) {
            showErrorBulletin(getString(R.string.CG_WifiFailed));
            return;
        }

        WifiManager wifiManager = (WifiManager) ApplicationLoader.applicationContext.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            showErrorBulletin(getString(R.string.CG_WifiFailed));
            return;
        }

        if (!wifiManager.isWifiEnabled()) {
            showErrorBulletin(getString(R.string.CG_WifiDisabled));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent panelIntent = new Intent("android.settings.panel.action.WIFI");
                fragment.startActivityForResult(panelIntent, 501);
            }
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            connectWifiModern(wifiManager);
        } else {
            connectWifiLegacy(wifiManager);
        }
    }

    @SuppressWarnings("deprecation")
    private void connectWifiLegacy(WifiManager wifiManager) {
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.SSID = String.format("\"%s\"", ssid);

        boolean noPassword = TextUtils.isEmpty(wifiAuthType) || "nopass".equalsIgnoreCase(wifiAuthType);
        if (!TextUtils.isEmpty(password) && !noPassword) {
            if ("WPA".equalsIgnoreCase(wifiAuthType)) {
                configuration.preSharedKey = String.format("\"%s\"", password);
            } else if ("WEP".equalsIgnoreCase(wifiAuthType)) {
                configuration.wepKeys[0] = String.format("\"%s\"", password);
                configuration.wepTxKeyIndex = 0;
                configuration.allowedKeyManagement.set(0);
                configuration.allowedGroupCiphers.set(0);
            }
        } else {
            configuration.allowedKeyManagement.set(0);
        }

        int networkId = wifiManager.addNetwork(configuration);
        if (networkId != -1 && wifiManager.enableNetwork(networkId, true)) {
            wifiManager.reconnect();
            BulletinFactory.of(fragment)
                    .createSimpleBulletin(R.raw.contact_check, getString(R.string.CG_WifiSuccess))
                    .show();
            return;
        }

        showErrorBulletin(getString(R.string.CG_WifiFailed));
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void connectWifiModern(WifiManager wifiManager) {
        WifiNetworkSuggestion.Builder builder = new WifiNetworkSuggestion.Builder()
                .setSsid(ssid)
                .setIsAppInteractionRequired(true);

        if (!TextUtils.isEmpty(password)) {
            if ("WPA".equalsIgnoreCase(wifiAuthType)) {
                builder.setWpa2Passphrase(password);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && "SAE".equalsIgnoreCase(wifiAuthType)) {
                builder.setWpa3Passphrase(password);
            }
        }

        WifiNetworkSuggestion suggestion = builder.build();
        List<WifiNetworkSuggestion> suggestions = Collections.singletonList(suggestion);

        if (wifiManager.addNetworkSuggestions(suggestions) == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
            Intent wifiSettingsIntent = new Intent("android.settings.WIFI_SETTINGS");
            fragment.getParentActivity().startActivity(wifiSettingsIntent);
            return;
        }

        showErrorBulletin(getString(R.string.CG_WifiFailed));
    }
    /** Wi-Fi finish */

    /** Misc start */
    private String ensureUrlHasHttps(String str) {
        if (str == null) {
            return null;
        }
        if (!LinkifyPort.WEB_URL.matcher(str).matches() || str.startsWith("http://") || str.startsWith("https://") || str.contains("://")) {
            return str;
        }
        return "https://" + str;
    }

    private boolean isTelegramLink(String host) {
        return host.equals("t.me") || host.equals("telegram.me") || host.equals("telegram.dog");
    }

    private void copyQR(Bitmap qrBitmap, boolean save, Activity activity) {
        try {
            File dir = activity.getExternalFilesDir(null);
            if (dir == null) return;

            File qrFile = new File(dir, "qr_code.png");

            try (FileOutputStream outputStream = new FileOutputStream(qrFile)) {
                qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            }

            if (save) {
                MediaController.saveFile(qrFile.toString(), activity, 0, null, "image/png", uri -> {
                    BulletinFactory.of(getContainer(), resourcesProvider)
                            .createDownloadBulletin(BulletinFactory.FileType.PHOTO, resourcesProvider)
                            .setDuration(Bulletin.DURATION_SHORT)
                            .show();
                });
            } else {
                ChatsHelper.addFileToClipboard(qrFile, () -> BulletinFactory.of(getContainer(), resourcesProvider)
                        .createSuccessBulletin(getString(R.string.CG_PhotoCopied), resourcesProvider)
                        .setDuration(Bulletin.DURATION_SHORT)
                        .show());
            }
        } catch (IOException e) {
            FileLog.e(e);
        }
    }

    private void showCopyBulletin(boolean useContainer) {
        AndroidUtilities.runOnUIThread(() -> {
            BulletinFactory factory = useContainer
                    ? BulletinFactory.of(getContainer(), resourcesProvider)
                    : BulletinFactory.of(fragment);
            factory.createCopyBulletin(formatString(R.string.TextCopied)).show();
        });
    }

    private void showErrorBulletin(String message) {
        AndroidUtilities.runOnUIThread(() ->
                BulletinFactory.of(fragment).createErrorBulletin(message).show()
        );
    }
    /** Misc finish */

    private final class SheetHandleView extends View {

        private SheetHandleView(Context context) {
            super(context);
        }

        private final RectF rect = new RectF();

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);

            int handleWidth = dp(36);

            rect.set(
                    (getWidth() - handleWidth) / 2f,
                    0,
                    (getWidth() + handleWidth) / 2f,
                    dp(4)
            );

            Theme.dialogs_onlineCirclePaint.setColor(getThemedColor(Theme.key_sheet_scrollUp));

            canvas.drawRoundRect(
                    rect,
                    dp(2),
                    dp(2),
                    Theme.dialogs_onlineCirclePaint
            );
        }
    }

    private static final class RoundedQrOutlineProvider extends ViewOutlineProvider {
        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(
                    0,
                    0,
                    view.getMeasuredWidth(),
                    view.getMeasuredHeight(),
                    dp(12)
            );
        }
    }

}
