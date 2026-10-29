/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.preferences;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.formatPluralString;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.ColoredImageSpan;
import org.telegram.ui.Components.ItemOptions;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.StickerImageView;
import org.telegram.ui.Components.TableView;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.SettingsActivity;
import org.telegram.ui.Wallet.WalletRecipientView;

import java.util.ArrayList;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.core.configs.CherrygramFirebaseConfig;
import uz.unnarsx.cherrygram.core.firebase.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.core.firebase.remoteConfig.RemoteConfigHelper;
import uz.unnarsx.cherrygram.core.helpers.DeeplinkHelper;
import uz.unnarsx.cherrygram.donates.DonatesManager;
import uz.unnarsx.cherrygram.misc.Constants;
import uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper;

public class DonatesPreferencesEntry extends BaseCGPreferencesEntry implements NotificationCenter.NotificationCenterDelegate {

    private final boolean isTestBackend = getConnectionsManager().isTestBackend();
    private boolean didDonate = DonatesManager.INSTANCE.checkAllDonatedAccounts() || DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace();
    private boolean showDonates = !isTestBackend && (ApplicationLoader.isStandaloneBuild() || didDonate);
    public DonatesPreferencesEntry forceShowDonates(boolean force) {
        if (force) this.showDonates = !isTestBackend;
        return this;
    }

    private FrameLayout stickerFrame;
    private StickerImageView stickerImageView;

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("donates_screen");
        return getString(R.string.DP_SupportOptions);
    }

    @Override
    protected String getKey() {
        return DeeplinkHelper.DeepLinksRepo.CG_Support;
    }

    @Override
    public boolean onFragmentCreate() {
        getNotificationCenter().addObserver(this, NotificationCenter.cgDonatesLoaded);
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        getNotificationCenter().removeObserver(this, NotificationCenter.cgDonatesLoaded);
    }

    @Override
    public View createView(Context context) {
        if (!isTestBackend) {
            ActionBarMenu menu = actionBar.createMenu();
            ActionBarMenuItem otherItem = menu.addItem(1, R.drawable.ic_ab_other);
            otherItem.setContentDescription(getString(R.string.AccDescrMoreOptions));
            otherItem.setOnClickListener(v -> ItemOptions.makeOptions(this, otherItem)
                .add(R.drawable.msg_retry, getString(R.string.Refresh), () -> {
                    Continuation<Object> suspendResult = new Continuation<>() {
                        @NonNull
                        @Override
                        public CoroutineContext getContext() {
                            return EmptyCoroutineContext.INSTANCE;
                        }

                        @Override
                        public void resumeWith(@NonNull Object o) {

                        }
                    };
                    DonatesManager.INSTANCE.startAutoRefresh(getContext(), true, false, suspendResult);
                    RemoteConfigHelper.INSTANCE.fetchAndActivate(true, suspendResult);
                })
                .setDrawScrim(false)
                .translate(0F, -dp(48F))
                .show()
            );
        }

        return super.createView(context);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        if (stickerFrame == null) {
            stickerFrame = new FrameLayout(getContext()) {
                @Override
                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), heightMeasureSpec);
                }
            };
            stickerImageView = new StickerImageView(getContext(), getCurrentAccount());
            stickerImageView.setStickerPackName(isTestBackend ? AndroidUtilities.STICKERS_PLACEHOLDER_PACK_NAME : "HotCherry");
            stickerImageView.getImageReceiver().setAutoRepeat(1);
            stickerFrame.addView(stickerImageView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
        }
        int width = 180, height = 180, top = dp(4), bottom = dp(4);
        if (showDonates && DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace()) {
            stickerImageView.setStickerNum(33);
            width = dp(65);
            height = dp(55);
            top = 0;
            bottom = dp(2);
        } else if (showDonates && DonatesManager.INSTANCE.checkAllDonatedAccounts()) {
            stickerImageView.setStickerNum(24);
        } else {
            stickerImageView.setStickerNum(7);
        }
        stickerImageView.setLayoutParams(LayoutHelper.createFrame(width, height, Gravity.CENTER, 0, top, 0, bottom));
        stickerImageView.invalidate();
        items.add(UItem.asFullyCustom(stickerFrame));

        TableView stickerInfoTable = new TableView(getContext(), getResourceProvider());
        stickerInfoTable.setPadding(dp(15), 0, dp(15), dp(10));
        CharSequence moreInfoText = AndroidUtilities.replaceSingleTag(getString(R.string.DP_DonatesMoreInfo),
                Theme.key_windowBackgroundWhiteLinkText,
                AndroidUtilities.REPLACING_TAG_TYPE_LINKBOLD,
                () -> Browser.openUrl(getContext(), "https://t.me/CherrygramSupport")
        );
        stickerInfoTable.addFullRow(
                didDonate && !isTestBackend ? getString(R.string.DP_DonatesThanks) : moreInfoText,
                false, true, 15, true
        );
        items.add(UItem.asFullyCustom(stickerInfoTable));

        if (!showDonates) return;

        items.add(UItem.asHeader(getString(R.string.DP_DonateBadge)));
        TextInfoPrivacyCell bonusesInfoCell = new TextInfoPrivacyCell(getContext(), 18);
        bonusesInfoCell.setTopPadding(dp(1));
        bonusesInfoCell.setBottomPadding(dp(2));
        bonusesInfoCell.setText(getString(R.string.DP_Donate_Desc));
        items.add(SettingsHelper.asCustomWithBackground(bonusesInfoCell));

        TableView bonusesPriceTable = new TableView(getContext(), getResourceProvider());
        bonusesPriceTable.setPadding(dp(15), 0, dp(15), dp(10));
        bonusesPriceTable.addRow(getString(R.string.GiftValue2), getString(R.string.Gift2UniqueTitle2), null);
        String amount2 = DonatesManager.INSTANCE.getTonAmountForUsd(getContext(), 2.0, false) + " TON (GRAM)";
        bonusesPriceTable.addRow(getTonSpanned("$2 / €2 / 200₽ \n\n" + amount2), generateDescForDonates(bonusesPriceTable));
        String amount5 = DonatesManager.INSTANCE.getTonAmountForUsd(getContext(), 5.0, true) + " TON (GRAM)";
        bonusesPriceTable.addRow(getTonSpanned("$5 / €5 / 500₽ \n\n" + amount5), generateDescForMarketplace(bonusesPriceTable));
        bonusesPriceTable.addFullRow(AndroidUtilities.replaceSingleTag(getString(R.string.DP_Donate_Cheque),
                Theme.key_windowBackgroundWhiteLinkText,
                AndroidUtilities.REPLACING_TAG_TYPE_LINKBOLD,
                () -> Browser.openUrl(getContext(), "https://t.me/arsLan")
        ));
        int counter = DonatesManager.INSTANCE.donatesCounter(2);
        if (counter > 1) {
            CharSequence countText = AndroidUtilities.replaceSingleTag(formatPluralString("DP_Donate_Counter", counter) + " \uD83C\uDF52",
                    Theme.key_windowBackgroundWhiteLinkText,
                    AndroidUtilities.REPLACING_TAG_TYPE_LINKBOLD,
                    null
            );
            bonusesPriceTable.addFullRow(countText, false, true, 15, false);
        }
        items.add(SettingsHelper.asCustomWithBackground(bonusesPriceTable));
        items.add(UItem.asShadow(AndroidUtilities.replaceSingleTag(getString(R.string.DP_Donate_Terms),
                Theme.key_windowBackgroundWhiteLinkText,
                AndroidUtilities.REPLACING_TAG_TYPE_LINKBOLD,
                () -> Browser.openUrl(getContext(), Constants.CG_DONATIONS_AND_TERMS_URL)
        )));

        CharSequence personalDataText = AndroidUtilities.replaceSingleTag(getString(R.string.DP_Donate_Method_Surname),
                Theme.key_windowBackgroundWhiteLinkText,
                AndroidUtilities.REPLACING_TAG_TYPE_LINKBOLD,
                () -> Browser.openUrl(getContext(), "https://t.me/arsLan")
        );

        items.add(UItem.asHeader(getString(R.string.DP_Donate_Method_Int)));
        items.add(asPaymentCell("MasterCard (MoneySend)", R.drawable.card_master_light, R.drawable.card_master_dark, CardsRepo.Card_Kapital_Master_UZS, true));
        items.add(asPaymentCell("VISA USD (Visa Direct)", R.drawable.card_visa_light, R.drawable.card_visa_dark, CardsRepo.Card_Kapital_Visa_USD, true));
        items.add(UItem.asShadow(personalDataText));

        items.add(UItem.asHeader(getString(R.string.DP_Donate_Method_Rus)));
        items.add(asPaymentCell("Альфа-Банк", R.drawable.card_alfa_light, R.drawable.card_alfa_dark, CardsRepo.Card_Kapital_Visa_USD, true));
        items.add(asPaymentCell("ВТБ", R.drawable.card_vtb_light, R.drawable.card_vtb_dark, CardsRepo.Card_T_Bank, true)); // Card_Humo
        items.add(asPaymentCell("СберБанк", R.drawable.card_sber_light, R.drawable.card_sber_dark, CardsRepo.Card_T_Bank, true)); // Card_Anor_UzCard
        items.add(asPaymentCell("МИР", R.drawable.card_mir_light, R.drawable.card_mir_dark, CardsRepo.Card_Yoomoney_MIR, true));
        items.add(asPaymentCell("Т-Банк", R.drawable.card_tinkoff_light, R.drawable.card_tinkoff_dark, CardsRepo.Card_T_Bank, true)); // Card_Anor_UzCard_Kids
        items.add(asPaymentCell("YooMoney", R.drawable.card_ym_light, R.drawable.card_ym_dark, CardsRepo.Card_Yoomoney_Account, false));
        items.add(UItem.asShadow(personalDataText));

        items.add(UItem.asHeader(getString(R.string.DP_Donate_Method_Uzb)));
        items.add(asPaymentCell("HUMO", R.drawable.card_humo_light, R.drawable.card_humo_dark, CardsRepo.Card_Humo, true));
        items.add(asPaymentCell("UzCard", R.drawable.card_uzcard_light, R.drawable.card_uzcard_dark, CardsRepo.Card_Anor_UzCard_Kids, true));
        items.add(asPaymentCell("UzCard-MIR Co-Badge", R.drawable.card_uzcard_mir_light, R.drawable.card_uzcard_mir_dark, CardsRepo.Card_Anor_UzCard, true));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader("Crypto // w5"));
        items.add(asPaymentCell("TON Coin", R.drawable.card_ton_light, R.drawable.card_ton_dark, CryptoWalletsRepo.Tonkeeper_TON, false));
        items.add(asPaymentCell("USDT (TON Network)", R.drawable.card_usdt_light, R.drawable.card_usdt_dark, CryptoWalletsRepo.Tonkeeper_USDT, false));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader("Crypto // @walt"));
        items.add(asPaymentCell("TON Coin", R.drawable.card_ton_light, R.drawable.card_ton_dark, CryptoWalletsRepo.Wallet_TON, false));
        items.add(asPaymentCell("USDT (TON Network)", R.drawable.card_usdt_light, R.drawable.card_usdt_dark, CryptoWalletsRepo.Wallet_USDT, false));
        items.add(asPaymentCell("Bitcoin (BTC)", R.drawable.card_btc_light, R.drawable.card_btc_dark, CryptoWalletsRepo.Wallet_Bitcoin, false));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader("Crypto // Binance"));
        items.add(asPaymentCell("Binance ID", R.drawable.card_binance_light, R.drawable.card_binance_dark, CryptoWalletsRepo.Binance_Account, false));
        items.add(asPaymentCell("Bitcoin (BTC)", R.drawable.card_btc_light, R.drawable.card_btc_dark, CryptoWalletsRepo.Binance_Bitcoin, false));
        if (CherrygramCoreConfig.isDevBuild()) {
            items.add(asPaymentCell("Ethereum (ERC20)", R.drawable.card_eth_light, R.drawable.card_eth_dark, CryptoWalletsRepo.Binance_Ethereum, false));
        }
        items.add(asPaymentCell("TetherUS - USDT (TRC20)", R.drawable.card_usdt_light, R.drawable.card_usdt_dark, CryptoWalletsRepo.Binance_USDT, false));
        items.add(UItem.asShadow(null));
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.cgDonatesLoaded) {
            didDonate = DonatesManager.INSTANCE.checkAllDonatedAccounts() || DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace();
            updateRows(true);
        }
    }

    private UItem asPaymentCell(CharSequence title, int iconLight, int iconDark, String address, boolean isCard) {
        WalletRecipientView recipientView = new WalletRecipientView(getContext(), getResourceProvider(), null, null, true);
        recipientView.setCustom(title, Theme.isCurrentThemeDay() ? iconLight : iconDark, address);
        recipientView.setOnClickListener(v -> {
            AndroidUtilities.addToClipboard(address);
            Toast.makeText(getParentActivity(), getString(isCard ? R.string.CardNumberCopied : R.string.TextCopied), Toast.LENGTH_SHORT).show();
        });

        FrameLayout frame = new FrameLayout(getContext()) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), heightMeasureSpec);
            }
        };
        frame.setPadding(dp(12), dp(4), dp(12), dp(4));
        frame.addView(recipientView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        return UItem.asCustom(frame);
    }

    private SpannableStringBuilder generateDescForDonates(TableView tableView) {
        CharSequence badgeTitle = AndroidUtilities.replaceSingleTag(getString(R.string.DP_Donate_Badge_desc),
                Theme.key_windowBackgroundWhiteLinkText,
                AndroidUtilities.REPLACING_TAG_TYPE_LINKBOLD,
                () -> {
                    getMessageMenuHelper().checkBlur(getParentActivity(), true, true, 50f);
                    ItemOptions.makeOptions(DonatesPreferencesEntry.this, tableView)
                            .addEmojiStatus(getUserConfig().getCurrentUser(), Constants.CHERRY_EMOJI_ID_VERIFIED, false)
                            .forceTop(true)
                            .setDrawScrim(false)
                            .setGravity(Gravity.RIGHT)
                            .translate(-dp(30), dp(50))
                            .setOnDismiss(() -> getMessageMenuHelper().checkBlur(getParentActivity(), false, false, 0f))
                            .show();
                }
        );

        CharSequence compactMessageMenuText = AndroidUtilities.replaceSingleTag(getString(R.string.DP_Donate_MsgMenuCompactView_Desc),
                Theme.key_windowBackgroundWhiteLinkText,
                AndroidUtilities.REPLACING_TAG_TYPE_LINKBOLD,
                () -> Browser.openUrl(getContext(), "https://t.me/cgSettings/message_menu?r=compactMsgMenu")
        );

        CharSequence foldersAtTheBottomText = AndroidUtilities.replaceSingleTag(getString(R.string.DP_Donate_FoldersAtBottom_Desc),
                Theme.key_windowBackgroundWhiteLinkText,
                AndroidUtilities.REPLACING_TAG_TYPE_LINKBOLD,
                () -> Browser.openUrl(getContext(), "https://t.me/cgSettings/folders?r=foldersAtBottom")
        );

        SpannableStringBuilder sb = new SpannableStringBuilder();
        sb.append(badgeTitle);
        sb.append("\n");
        sb.append(compactMessageMenuText);
        sb.append("\n");
        sb.append(foldersAtTheBottomText);

        return sb;
    }

    private SpannableStringBuilder generateDescForMarketplace(TableView tableView) {
        CharSequence epicBadgeTitle = AndroidUtilities.replaceSingleTag(getString(R.string.DP_Donate_EpicBadge_desc),
                Theme.key_windowBackgroundWhiteLinkText,
                AndroidUtilities.REPLACING_TAG_TYPE_LINKBOLD,
                () -> {
                    getMessageMenuHelper().checkBlur(getParentActivity(), true, true, 50f);
                    ItemOptions.makeOptions(DonatesPreferencesEntry.this, tableView)
                            .addEmojiStatus(getUserConfig().getCurrentUser(), Constants.CHERRY_EMOJI_ID_VERIFIED, true)
                            .forceTop(true)
                            .setDrawScrim(false)
                            .setGravity(Gravity.RIGHT)
                            .translate(-dp(30), dp(100))
                            .setOnDismiss(() -> getMessageMenuHelper().checkBlur(getParentActivity(), false, false, 0f))
                            .show();
                }
        );
        CharSequence marketPlaceText = AndroidUtilities.replaceSingleTag(getString(R.string.DP_Donate_GiftMarket_Desc),
                Theme.key_windowBackgroundWhiteLinkText,
                AndroidUtilities.REPLACING_TAG_TYPE_LINKBOLD,
                () -> presentFragment(new SettingsActivity())
        );
        CharSequence filterText = AndroidUtilities.replaceSingleTag(getString(R.string.DP_Donate_MessageFilter_Desc),
                Theme.key_windowBackgroundWhiteLinkText,
                AndroidUtilities.REPLACING_TAG_TYPE_LINKBOLD,
                () -> CherrygramPreferencesNavigator.INSTANCE.createMessageFilter(DonatesPreferencesEntry.this)
        );
        CharSequence messageMenuText = AndroidUtilities.replaceSingleTag(getString(R.string.DP_Donate_MessageMenu_Desc),
                Theme.key_windowBackgroundWhiteLinkText,
                AndroidUtilities.REPLACING_TAG_TYPE_LINKBOLD,
                () -> Browser.openUrl(getContext(), "https://t.me/cgSettings/message_menu?r=iosMsgMenu")
        );

        SpannableStringBuilder sb = new SpannableStringBuilder();
        sb.append(epicBadgeTitle);
        sb.append("\n");
        sb.append(marketPlaceText);
        sb.append("\n");
        sb.append(filterText);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            sb.append("\n");
            sb.append(messageMenuText);
        }

        return sb;
    }

    private CharSequence getTonSpanned(String textWithTon) {
        SpannableString tonIcon = new SpannableString("TON");
        ColoredImageSpan span = new ColoredImageSpan(R.drawable.mini_gram_16);
        span.setWidth(dp(13));
        tonIcon.setSpan(span, 0, tonIcon.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return AndroidUtilities.replaceCharSequence("TON", textWithTon, tonIcon);
    }

    private static class CardsRepo {

        public static String Card_Anor_Humo = "9860600408892476";
        public static String Card_Anor_UzCard = "5614683516520707";
        public static String Card_Anor_UzCard_Kids = "5614683588301333";

        public static String Card_Humo = CherrygramFirebaseConfig.INSTANCE.getHumoCardNumber();

        public static String Card_Kapital_Humo = "9860100128256904";
        public static String Card_Kapital_Visa_USD = "4278310028377794";
        public static String Card_Kapital_Master_UZS = "5397170005511325";

        public static String Card_T_Bank = CherrygramFirebaseConfig.INSTANCE.getTbankCardNumber();

        public static String Card_TBC_Humo = "9860350143344678";

        public static String Card_Yoomoney_MIR = "2204120134366927";
        public static String Card_Yoomoney_Account = "4100116983696293";

    }

    private static class CryptoWalletsRepo {

        public static String Binance_Account = "220943480";
        public static String Binance_Bitcoin = "1Pr6GqqWakgKWW1nDjVyHUYo1AcWbSN453";
        public static String Binance_Ethereum = "0xa8392346f426443ef7e3d98047bace6dbcc0e7d8";
        public static String Binance_USDT = "TKnPT5rojMf851ejov2Xu4pxKcMfSh4Ws9";

        public static String Tonkeeper_TON = "UQBG--q68OfxTQNE_-wcWIWTWxSSL1GxYGUW75CyrTrTUUCT";
        public static String Tonkeeper_USDT = "UQBG--q68OfxTQNE_-wcWIWTWxSSL1GxYGUW75CyrTrTUUCT";

        public static String Wallet_Bitcoin = "bc1ql4f7nuxv44y6tc72zy5z4h2sucqzk93nnmpyrl";
        public static String Wallet_TON = "UQCK2zt2pHa9ag-lUFTCuvsxW4lqPmkX6eSYFhS5xCKBwKAN";
        public static String Wallet_USDT = "UQCK2zt2pHa9ag-lUFTCuvsxW4lqPmkX6eSYFhS5xCKBwKAN";

    }

}