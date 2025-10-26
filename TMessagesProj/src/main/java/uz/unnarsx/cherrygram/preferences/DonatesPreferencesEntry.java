/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.preferences;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.ItemOptions;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.StickerImageView;
import org.telegram.ui.Components.TableView;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper;
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper;
import uz.unnarsx.cherrygram.core.helpers.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.helpers.network.DonatesManager;
import uz.unnarsx.cherrygram.misc.CherrygramExtras;
import uz.unnarsx.cherrygram.misc.Constants;

public class DonatesPreferencesEntry extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private int rowCount;
    private ListAdapter listAdapter;
    private RecyclerListView listView;

    private int stickerViewRow;
    private int stickerTableInfoRow;

    private int brandedScreenshotsHeaderRow;
    private int brandedScreenshotsInfoRow;
    private int brandedScreenshotsSwitchRow;
    private int brandedScreenshotsDivisorRow;

    private int enjoyingHeaderRow;
    private int rateUsRow;
    private int enjoyingEndDivisor;
    
    private int bonusesHeaderRow;
    private int bonusesInfoRow;
    private int bonusesPriceRow;
    private int bonusesDivisorRow;

    private int intHeaderRow;
    private int masterCardRow;
    private int visaRow;
    private int intCredsRow;
    private int intDivisorRow;

    private int rusHeaderRow;
    private int alfaRow;
    private int vtbRow;
    private int sberRow;
    private int tinkoffRow;
    private int yooMoneyRow;
    private int rusCredsRow;
    private int rusDivisorRow;

    private int uzbHeaderRow;
    private int humoRow;
    private int uzCardRow;
    private int uzCardMirRow;
    private int tirikchilikRow;
    private int uzbDivisorRow;

    private int walletHeaderRow;
    private int walletBitcoinRow;
    private int walletTonRow;
    private int walletUSDTRow;
    private int tonKeeperTonRow;
    private int tonKeeperUSDTRow;
    private int walletDivisorRow;

    private int binanceHeaderRow;
    private int binanceIDRow;
    private int binanceBitcoinRow;
    private int binanceEthereumRow;
    private int binanceUSDTRow;
    private int binanceDivisorRow;

    protected Theme.ResourcesProvider resourcesProvider;

    private final static int update_donates_list = 1;

    private final boolean isTestBackend = ConnectionsManager.getInstance(getCurrentAccount()).isTestBackend();
    private boolean didDonate = DonatesManager.INSTANCE.checkAllDonatedAccounts() || DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace();
    private boolean showDonates = !isTestBackend && (ApplicationLoader.isStandaloneBuild() || didDonate);
    public DonatesPreferencesEntry forceShowDonates() {
        this.showDonates = !isTestBackend;
        return this;
    }

    @Override
    public boolean onFragmentCreate() {
        getNotificationCenter().addObserver(this, NotificationCenter.cgDonatesLoaded);
        updateRowsId(true);
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();

        getNotificationCenter().removeObserver(this, NotificationCenter.cgDonatesLoaded);
    }

    protected boolean hasWhiteActionBar() {
        return true;
    }

    @Override
    public boolean isLightStatusBar() {
        if (!hasWhiteActionBar()) return super.isLightStatusBar();
        int color = getThemedColor(Theme.key_windowBackgroundWhite);
        return ColorUtils.calculateLuminance(color) > 0.7f;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonDrawable(new BackDrawable(false));

        actionBar.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
        actionBar.setItemsColor(getThemedColor(Theme.key_windowBackgroundWhiteBlackText), false);
        actionBar.setItemsBackgroundColor(getThemedColor(Theme.key_actionBarActionModeDefaultSelector), true);
        actionBar.setItemsBackgroundColor(getThemedColor(Theme.key_actionBarWhiteSelector), false);
        actionBar.setItemsColor(getThemedColor(Theme.key_actionBarActionModeDefaultIcon), true);
        actionBar.setTitleColor(getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
        actionBar.setCastShadows(false);

        actionBar.setTitle(getString(R.string.DP_SupportOptions));
        actionBar.setAllowOverlayTitle(false);

        if (!isTestBackend) {
            ActionBarMenu menu = actionBar.createMenu();
            ActionBarMenuItem menuItem = menu.addItem(0, R.drawable.ic_ab_other);
            menuItem.setContentDescription(getString(R.string.AccDescrMoreOptions));
            menuItem.addSubItem(update_donates_list, R.drawable.msg_retry, getString(R.string.Refresh));
        }

        actionBar.setOccupyStatusBar(!AndroidUtilities.isTablet());
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == update_donates_list) {
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
                    DonatesManager.INSTANCE.startAutoRefresh(getContext(), true, suspendResult);
                }
            }
        });

        listAdapter = new ListAdapter(context);

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(false);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setAdapter(listAdapter);
        if (listView.getItemAnimator() != null) {
            ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
        }
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setOnItemClickListener((view, position, x, y) -> {
            var holder = listView.findViewHolderForAdapterPosition(position);
            if (holder == null || !listAdapter.isEnabled(holder)) {
                return;
            }
            if (position == brandedScreenshotsSwitchRow) {
                CherrygramCoreConfig.INSTANCE.setCgBrandedScreenshots(!CherrygramCoreConfig.INSTANCE.getCgBrandedScreenshots());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramCoreConfig.INSTANCE.getCgBrandedScreenshots());
                }
                AppRestartHelper.createRestartBulletin(this);
            } else if (position == rateUsRow) {
                CherrygramExtras.INSTANCE.requestReviewFlow(this, getContext(), getParentActivity());
            } else if (position == masterCardRow) {
                copyNumberAndMakeToast("5181000156329583", true);
            } else if (position == visaRow) {
                copyNumberAndMakeToast("4278310028377794", true);
            } else if (position == alfaRow) {
                copyNumberAndMakeToast("4278310028377794", true);
            } else if (position == vtbRow) {
                copyNumberAndMakeToast("9860100124370345", true);
            } else if (position == sberRow) {
                copyNumberAndMakeToast("5614683516520707", true);
            } else if (position == tinkoffRow) {
                copyNumberAndMakeToast("5614683588301333", true);
            } else if (position == yooMoneyRow) {
                copyNumberAndMakeToast("4100116983696293", false);
            } else if (position == humoRow) {
                copyNumberAndMakeToast("9860100124370345", true);
            } else if (position == uzCardRow) {
                copyNumberAndMakeToast("5614683588301333", true);
            } else if (position == uzCardMirRow) {
                copyNumberAndMakeToast("5614683516520707", true);
            } else if (position == tirikchilikRow) {
                Intent openURL = new Intent(Intent.ACTION_VIEW);
                openURL.setData(Uri.parse("https://tirikchilik.uz/arslan4k1390"));
                getParentActivity().startActivity(openURL);
            } else if (position == walletBitcoinRow) {
                copyNumberAndMakeToast("158BXPmSGEcKXpYhVeKU11ETEgsSn4eMt7", false);
            } else if (position == walletTonRow) {
                copyNumberAndMakeToast("UQCK2zt2pHa9ag-lUFTCuvsxW4lqPmkX6eSYFhS5xCKBwKAN", false);
            } else if (position == walletUSDTRow) {
                copyNumberAndMakeToast("UQCK2zt2pHa9ag-lUFTCuvsxW4lqPmkX6eSYFhS5xCKBwKAN", false);
            } else if (position == tonKeeperTonRow) {
                copyNumberAndMakeToast("UQBG--q68OfxTQNE_-wcWIWTWxSSL1GxYGUW75CyrTrTUUCT", false);
            } else if (position == tonKeeperUSDTRow) {
                copyNumberAndMakeToast("UQBG--q68OfxTQNE_-wcWIWTWxSSL1GxYGUW75CyrTrTUUCT", false);
            } else if (position == binanceIDRow) {
                copyNumberAndMakeToast("220943480", false);
            } else if (position == binanceBitcoinRow) {
                copyNumberAndMakeToast("1Pr6GqqWakgKWW1nDjVyHUYo1AcWbSN453", false);
            } else if (position == binanceEthereumRow) {
                copyNumberAndMakeToast("0xa8392346f426443ef7e3d98047bace6dbcc0e7d8", false);
            } else if (position == binanceUSDTRow) {
                copyNumberAndMakeToast("TKnPT5rojMf851ejov2Xu4pxKcMfSh4Ws9", false);
            }
        });

        FirebaseAnalyticsHelper.trackEventWithEmptyBundle("donates_screen");

        return fragmentView;
    }

    public class ListAdapter extends RecyclerListView.SelectionAdapter {

        private final Context mContext;

        private final int VIEW_TYPE_SHADOW = 0;
        private final int VIEW_TYPE_HEADER = 1;
        private final int VIEW_TYPE_TEXT_CELL = 2;
        private final int VIEW_TYPE_TEXT_CHECK = 3;
//        private final int VIEW_TYPE_TEXT_SETTINGS = 4;
        private final int VIEW_TYPE_TEXT_INFO_PRIVACY = 5;
//        private final int VIEW_TYPE_TEXT_DETAIL_SETTINGS = 6;
        private final int VIEW_TYPE_TABLE = 8;
        private final int VIEW_TYPE_STICKER = 9;

        ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case VIEW_TYPE_SHADOW: {
                    holder.itemView.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                }
                case VIEW_TYPE_HEADER: {
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    headerCell.setTopMargin(10);
                    headerCell.setHeight(35);
                    if (position == brandedScreenshotsHeaderRow) {
                        headerCell.setText(getString(R.string.DP_CameraCutoutHeader));
                    } else if (position == enjoyingHeaderRow) {
                        headerCell.setText(getString(R.string.DP_EnjoyingUs));
                    } else if (position == bonusesHeaderRow) {
                        headerCell.setText(getString(R.string.DP_DonateBadge));
                    } else if (position == intHeaderRow) {
                        headerCell.setText(getString(R.string.DP_Donate_Method_Int));
                    } else if (position == rusHeaderRow) {
                        headerCell.setText(getString(R.string.DP_Donate_Method_Rus));
                    } else if (position == uzbHeaderRow) {
                        headerCell.setText(getString(R.string.DP_Donate_Method_Uzb));
                    } else if (position == walletHeaderRow) {
                        headerCell.setText("Crypto");
                    } else if (position == binanceHeaderRow) {
                        headerCell.setText("Crypto // Binance");
                    }
                    break;
                }
                case VIEW_TYPE_TEXT_CELL: {
                    TextCell textCell = (TextCell) holder.itemView;
                    textCell.getTextView().setPadding(dp(8), 0, 0, 0);
                    textCell.getImageView().clearColorFilter();

                    CharSequence title = "";
                    int icon = 0;
                    boolean divider = false;
                    boolean isDarkMode = !Theme.isCurrentThemeDay();

                    if (position == rateUsRow) {
                        title = getString(R.string.DP_RateUs);

                        textCell.getTextView().setPadding(-dp(1), 0, 0, 0);
                        textCell.setText(title, divider);
                        break;
                    } else if (position == masterCardRow) {
                        title = "MasterCard (MoneySend)";
                        icon = isDarkMode ? R.drawable.card_master_dark : R.drawable.card_master_light;
                    } else if (position == visaRow) {
                        title = "VISA USD (Visa Direct)";
                        icon = isDarkMode ? R.drawable.card_visa_dark : R.drawable.card_visa_light;
                    } else if (position == alfaRow) {
                        title = "Альфа-Банк";
                        icon = isDarkMode ? R.drawable.card_alfa_dark : R.drawable.card_alfa_light;
                    } else if (position == vtbRow) {
                        title = "ВТБ";
                        icon = isDarkMode ? R.drawable.card_vtb_dark : R.drawable.card_vtb_light;
                    } else if (position == sberRow) {
                        title = "СберБанк";
                        icon = isDarkMode ? R.drawable.card_sber_dark : R.drawable.card_sber_light;
                    } else if (position == tinkoffRow) {
                        title = "Т-Банк";
                        icon = isDarkMode ? R.drawable.card_tinkoff_dark : R.drawable.card_tinkoff_light;
                    } else if (position == yooMoneyRow) {
                        title = "YooMoney";
                        icon = isDarkMode ? R.drawable.card_ym_dark : R.drawable.card_ym_light;
                    } else if (position == humoRow) {
                        title = "HUMO";
                        icon = isDarkMode ? R.drawable.card_humo_dark : R.drawable.card_humo_light;
                    } else if (position == uzCardRow) {
                        title = "UzCard";
                        icon = isDarkMode ? R.drawable.card_uzcard_dark : R.drawable.card_uzcard_light;
                    } else if (position == uzCardMirRow) {
                        title = "UzCard-MIR Co-Badge";
                        icon = isDarkMode ? R.drawable.card_uzcard_mir_dark : R.drawable.card_uzcard_mir_light;
                    } else if (position == tirikchilikRow) {
                        title = "Tirikchilik";
                        icon = isDarkMode ? R.drawable.card_tirikchilik_dark : R.drawable.card_tirikchilik_light;
                    } else if (position == walletBitcoinRow) {
                        title = "Bitcoin (BTC) // @wallet";
                        icon = isDarkMode ? R.drawable.card_btc_dark : R.drawable.card_btc_light;
                    } else if (position == walletTonRow) {
                        title = "TON Coin // @wallet";
                        icon = isDarkMode ? R.drawable.card_ton_dark : R.drawable.card_ton_light;
                    } else if (position == walletUSDTRow) {
                        title = "USDT (TON) // @wallet";
                        icon = isDarkMode ? R.drawable.card_usdt_dark : R.drawable.card_usdt_light;
                        divider = true;
                    } else if (position == tonKeeperTonRow) {
                        title = "TON Coin (w5)";
                        icon = isDarkMode ? R.drawable.card_ton_dark : R.drawable.card_ton_light;
                    } else if (position == tonKeeperUSDTRow) {
                        title = "USDT (TON // w5)";
                        icon = isDarkMode ? R.drawable.card_usdt_dark : R.drawable.card_usdt_light;
                    } else if (position == binanceIDRow) {
                        title = "Binance ID";
                        icon = isDarkMode ? R.drawable.card_binance_dark : R.drawable.card_binance_light;
                    } else if (position == binanceBitcoinRow) {
                        title = "Bitcoin (BTC)";
                        icon = isDarkMode ? R.drawable.card_btc_dark : R.drawable.card_btc_light;
                    } else if (position == binanceEthereumRow) {
                        title = "Ethereum (ERC20)";
                        icon = isDarkMode ? R.drawable.card_eth_dark : R.drawable.card_eth_light;
                    } else if (position == binanceUSDTRow) {
                        title = "TetherUS - USDT (TRC20)";
                        icon = isDarkMode ? R.drawable.card_usdt_dark : R.drawable.card_usdt_light;
                    }
                    textCell.setTextAndIcon(title, icon, divider);
                    break;
                }
                case VIEW_TYPE_TEXT_CHECK: {
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    textCheckCell.setEnabled(true, null);
                    if (position == brandedScreenshotsSwitchRow) {
                        textCheckCell.setTextAndCheck(getString(R.string.DP_CameraCutout), CherrygramCoreConfig.INSTANCE.getCgBrandedScreenshots(), false);
                    }
                    break;
                }
                case VIEW_TYPE_TEXT_INFO_PRIVACY: {
                    TextInfoPrivacyCell textInfoPrivacyCell = (TextInfoPrivacyCell) holder.itemView;
                    textInfoPrivacyCell.setTopPadding(5);
                    textInfoPrivacyCell.setBottomPadding(8);
                    textInfoPrivacyCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

                    if (position == brandedScreenshotsInfoRow) {
                        textInfoPrivacyCell.setText(AndroidUtilities.replaceTags(getString(R.string.DP_CameraCutoutDesc)));
                    } else if (position == bonusesInfoRow) {
                        textInfoPrivacyCell.setText(CGResourcesHelper.INSTANCE.getDonatesAdvice());
                    }
                    break;
                }
                case VIEW_TYPE_TABLE: {
                    TableView tableView = (TableView) holder.itemView;
                    tableView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

                    tableView.clear();
                    if (position == stickerTableInfoRow) {
                        tableView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
                        CharSequence moreInfoText = AndroidUtilities.replaceSingleTag(getString(R.string.DP_DonatesMoreInfo),
                                Theme.key_windowBackgroundWhiteLinkText,
                                AndroidUtilities.REPLACING_TAG_TYPE_LINKBOLD,
                                () -> Browser.openUrl(getContext(), "https://t.me/CherrygramSupport")
                        );
                        tableView.addFullRow(
                                didDonate && !isTestBackend ? getString(R.string.DP_DonatesThanks) : moreInfoText,
                                false, true, 15, true
                        );
                    } else if (position == bonusesPriceRow) {
                        tableView.addRow(getString(R.string.GiftValue2), getString(R.string.Gift2UniqueTitle2), true);

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
                        tableView.addRow("$2 / €2 / 200₽ \n\n1.5 TON", badgeTitle);

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
                                () -> CherrygramPreferencesNavigator.INSTANCE.createDrawerItems(DonatesPreferencesEntry.this)
                        );
                        CharSequence filterText = AndroidUtilities.replaceSingleTag(getString(R.string.DP_Donate_MessageFilter_Desc),
                                Theme.key_windowBackgroundWhiteLinkText,
                                AndroidUtilities.REPLACING_TAG_TYPE_LINKBOLD,
                                () -> CherrygramPreferencesNavigator.INSTANCE.createMessageFilter(DonatesPreferencesEntry.this)
                        );
                        CharSequence messageMenuText = AndroidUtilities.replaceSingleTag(getString(R.string.DP_Donate_MessageMenu_Desc),
                                Theme.key_windowBackgroundWhiteLinkText,
                                AndroidUtilities.REPLACING_TAG_TYPE_LINKBOLD,
                                () -> CherrygramPreferencesNavigator.INSTANCE.createMessageMenu(DonatesPreferencesEntry.this)
                        );

                        SpannableStringBuilder sb = new SpannableStringBuilder();
                        sb.append(epicBadgeTitle);
                        sb.append("\n");
                        sb.append(marketPlaceText);
                        sb.append("\n");
                        sb.append(filterText);
                        if (Build.VERSION.SDK_INT >= 31) {
                            sb.append("\n");
                            sb.append(messageMenuText);
                        }
                        tableView.addRow("$5 / €5 / 500₽ \n\n3 TON", sb);

                        CharSequence chequeText = AndroidUtilities.replaceSingleTag(getString(R.string.DP_Donate_Cheque),
                                Theme.key_windowBackgroundWhiteLinkText,
                                AndroidUtilities.REPLACING_TAG_TYPE_LINKBOLD,
                                () -> Browser.openUrl(getContext(), "https://t.me/arsLan")
                        );
                        tableView.addFullRow(chequeText);
                    } else if (position == intCredsRow || position == rusCredsRow) {
                        CharSequence personalDataText = AndroidUtilities.replaceSingleTag(getString(R.string.DP_Donate_Method_Surname),
                                Theme.key_windowBackgroundWhiteLinkText,
                                AndroidUtilities.REPLACING_TAG_TYPE_LINKBOLD,
                                () -> Browser.openUrl(getContext(), "https://t.me/arsLan")
                        );
                        tableView.addFullRow(personalDataText);
                    }
                    break;
                }
                case VIEW_TYPE_STICKER: {
                    StickerImageView stickerImageView = (StickerImageView) ((FrameLayout) holder.itemView).getChildAt(0);
                    bindStickerCell(stickerImageView);
                    break;
                }
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == VIEW_TYPE_TEXT_CELL || type == VIEW_TYPE_TEXT_CHECK /*|| type == VIEW_TYPE_USER*/;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case VIEW_TYPE_SHADOW:
                    view = new ShadowSectionCell(mContext);
                    break;
                case VIEW_TYPE_HEADER:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_TEXT_CELL:
                    view = new TextCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_TEXT_CHECK:
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_TEXT_INFO_PRIVACY:
                    view = new TextInfoPrivacyCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_TABLE:
                    view = new TableView(getContext(), getResourceProvider());
                    view.setPadding(dp(20), 0, dp(20), dp(10));
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_STICKER: {
                    FrameLayout stickerFrame = new FrameLayout(parent.getContext());

                    StickerImageView stickerImageView = new StickerImageView(parent.getContext(), getCurrentAccount());
                    stickerFrame.addView(stickerImageView, LayoutHelper.createFrame(
                            RecyclerView.LayoutParams.WRAP_CONTENT,
                            RecyclerView.LayoutParams.WRAP_CONTENT,
                            Gravity.CENTER
                    ));

                    bindStickerCell(stickerImageView);
                    view = stickerFrame;
                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected value: " + viewType);
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == brandedScreenshotsDivisorRow || position == enjoyingEndDivisor || position == bonusesDivisorRow || position == intDivisorRow || position == rusDivisorRow || position == uzbDivisorRow || position == binanceDivisorRow || position == walletDivisorRow) {
                return VIEW_TYPE_SHADOW;
            } else if (position == brandedScreenshotsHeaderRow || position == enjoyingHeaderRow || position == bonusesHeaderRow || position == intHeaderRow || position == rusHeaderRow || position == uzbHeaderRow || position == walletHeaderRow || position == binanceHeaderRow) {
                return VIEW_TYPE_HEADER;
            } else if (position == rateUsRow
                    || position == masterCardRow || position == visaRow
                    || position == alfaRow || position == vtbRow || position == sberRow || position == tinkoffRow || position == yooMoneyRow
                    || position == humoRow || position == uzCardRow || position == uzCardMirRow || position == tirikchilikRow
                    || position == walletBitcoinRow || position == walletTonRow || position == walletUSDTRow || position == tonKeeperTonRow || position == tonKeeperUSDTRow
                    || position == binanceIDRow || position ==  binanceBitcoinRow || position == binanceEthereumRow || position == binanceUSDTRow
            ) {
                return VIEW_TYPE_TEXT_CELL;
            } else if (position == brandedScreenshotsSwitchRow) {
                return VIEW_TYPE_TEXT_CHECK;
            } else if (position == brandedScreenshotsInfoRow || position == bonusesInfoRow) {
                return VIEW_TYPE_TEXT_INFO_PRIVACY;
            } else if (position == stickerTableInfoRow || position == bonusesPriceRow || position == intCredsRow || position == rusCredsRow) {
                return VIEW_TYPE_TABLE;
            } else if (position == stickerViewRow) {
                return VIEW_TYPE_STICKER;
            }
            return VIEW_TYPE_SHADOW;
        }

        private void bindStickerCell(StickerImageView stickerImageView) {
            if (isTestBackend) {
                stickerImageView.setStickerPackName(AndroidUtilities.STICKERS_PLACEHOLDER_PACK_NAME);
            } else {
                stickerImageView.setStickerPackName("HotCherry");
            }

            int width = 180, height = 180, top = dp(4), bottom = dp(4);

            if (showDonates) {
                if (DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace()) {
                    stickerImageView.setStickerNum(33);
                    width = dp(65);
                    height = dp(55);
                    top = 0;
                    bottom = dp(2);
                } else if (DonatesManager.INSTANCE.checkAllDonatedAccounts()) {
                    stickerImageView.setStickerNum(24);
                } else {
                    stickerImageView.setStickerNum(7);
                }
            } else {
                stickerImageView.setStickerNum(7);
            }
            stickerImageView.getImageReceiver().setAutoRepeat(1);

            FrameLayout.LayoutParams params = LayoutHelper.createFrame(
                    width, height, Gravity.CENTER, 0, top, 0, bottom
            );
            stickerImageView.setLayoutParams(params);

            stickerImageView.invalidate();
        }
    }

    private void updateRowsId(boolean notify) {
        rowCount = 0;

        stickerViewRow = rowCount++;
        stickerTableInfoRow = rowCount++;

        if (ApplicationLoader.isStandaloneBuild()) {
            enjoyingHeaderRow = -1;
            rateUsRow = -1;
            enjoyingEndDivisor = -1;
        } else {
            enjoyingHeaderRow = rowCount++;
            rateUsRow = rowCount++;
            enjoyingEndDivisor = rowCount++;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            brandedScreenshotsHeaderRow = rowCount++;
            brandedScreenshotsInfoRow = rowCount++;
            brandedScreenshotsSwitchRow = rowCount++;
            brandedScreenshotsDivisorRow = rowCount++;
        } else {
            brandedScreenshotsHeaderRow = -1;
            brandedScreenshotsInfoRow = -1;
            brandedScreenshotsSwitchRow = -1;
            brandedScreenshotsDivisorRow = -1;
        }

        if (showDonates) {
            bonusesHeaderRow = rowCount++;
            bonusesInfoRow = rowCount++;
            bonusesPriceRow = rowCount++;
            bonusesDivisorRow = rowCount++;

            intHeaderRow = rowCount++;
            masterCardRow = rowCount++;
            visaRow = rowCount++;
            intCredsRow = rowCount++;
            intDivisorRow = rowCount++;

            rusHeaderRow = rowCount++;
            alfaRow = rowCount++;
            vtbRow = rowCount++;
            sberRow = rowCount++;
            tinkoffRow = rowCount++;
            if (CherrygramCoreConfig.INSTANCE.isDevBuild()) {
                yooMoneyRow = rowCount++;
            } else {
                yooMoneyRow = -1;
            }
            rusCredsRow = rowCount++;
            rusDivisorRow = rowCount++;

            uzbHeaderRow = rowCount++;
            humoRow = rowCount++;
            uzCardRow = rowCount++;
            uzCardMirRow = rowCount++;
            tirikchilikRow = rowCount++;
            uzbDivisorRow = rowCount++;

            walletHeaderRow = rowCount++;
            walletBitcoinRow = rowCount++;
            if (CherrygramCoreConfig.INSTANCE.isDevBuild()) {
                walletTonRow = rowCount++;
            } else {
                walletTonRow = -1;
            }
            walletUSDTRow = rowCount++;
            tonKeeperTonRow = rowCount++;
            tonKeeperUSDTRow = rowCount++;
            walletDivisorRow = rowCount++;

            binanceHeaderRow = rowCount++;
            binanceIDRow = rowCount++;
            binanceBitcoinRow = rowCount++;
            if (CherrygramCoreConfig.INSTANCE.isDevBuild()) {
                binanceEthereumRow = rowCount++;
            } else {
                binanceEthereumRow = -1;
            }
            binanceUSDTRow = rowCount++;
            binanceDivisorRow = rowCount++;
        } else {
            bonusesHeaderRow = -1;
            bonusesInfoRow = -1;
            bonusesPriceRow = -1;
            bonusesDivisorRow = -1;

            intHeaderRow = -1;
            masterCardRow = -1;
            visaRow = -1;
            intCredsRow = -1;
            intDivisorRow = -1;

            rusHeaderRow = -1;
            alfaRow = -1;
            vtbRow = -1;
            sberRow = -1;
            tinkoffRow = -1;
            yooMoneyRow = -1;
            rusCredsRow = -1;
            rusDivisorRow = -1;

            uzbHeaderRow = -1;
            humoRow = -1;
            uzCardRow = -1;
            uzCardMirRow = -1;
            tirikchilikRow = -1;
            uzbDivisorRow = -1;

            walletHeaderRow = -1;
            walletBitcoinRow = -1;
            walletTonRow = -1;
            walletUSDTRow = -1;
            tonKeeperTonRow = -1;
            tonKeeperUSDTRow = -1;
            walletDivisorRow = -1;

            binanceHeaderRow = -1;
            binanceIDRow = -1;
            binanceBitcoinRow = -1;
            binanceEthereumRow = -1;
            binanceUSDTRow = -1;
            binanceDivisorRow = -1;
        }

        if (listAdapter != null && notify) {
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.cgDonatesLoaded) {
            didDonate = DonatesManager.INSTANCE.checkAllDonatedAccounts() || DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace();

            if (listAdapter != null) {
                try {
                    updateRowsId(false);

                    if (stickerViewRow >= 0 && stickerViewRow < rowCount) {
                        listAdapter.notifyItemChanged(stickerViewRow);
                    }

                    if (stickerTableInfoRow >= 0 && stickerTableInfoRow < rowCount) {
                        listAdapter.notifyItemChanged(stickerTableInfoRow);
                    }

                } catch (Exception e) {
                    FileLog.e(e);
                    updateRowsId(true);
                }
            }
        }
    }

    private void copyNumberAndMakeToast(String cardNumber, boolean card) {
        AndroidUtilities.addToClipboard(cardNumber);
        Toast.makeText(getParentActivity(), getString(card ? R.string.CardNumberCopied : R.string.TextCopied), Toast.LENGTH_SHORT).show();
    }

}
