/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.preferences;

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
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.ActionBar;
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
import org.telegram.ui.Components.TableView;

import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper;
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper;
import uz.unnarsx.cherrygram.core.helpers.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.misc.Constants;

public class DonatesPreferencesEntry extends BaseFragment {

    private int rowCount;
    private ListAdapter listAdapter;
    private RecyclerListView listView;

    private int brandedScreenshotsHeaderRow;
    private int brandedScreenshotsInfoRow;
    private int brandedScreenshotsSwitchRow;
    private int brandedScreenshotsDivisorRow;
    
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

    private int binanceHeaderRow;
    private int binanceIDRow;
    private int binanceBitcoinRow;
    private int binanceEthereumRow;
    private int binanceUSDTRow;
    private int binanceDivisorRow;

    private int walletHeaderRow;
    private int walletBitcoinRow;
    private int walletTonRow;
    private int walletUSDTRow;
    private int tonKeeperTonRow;
    private int walletDivisorRow;

    protected Theme.ResourcesProvider resourcesProvider;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        updateRowsId(true);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
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

        actionBar.setOccupyStatusBar(!AndroidUtilities.isTablet());
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
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
            } else if (position == binanceIDRow) {
                copyNumberAndMakeToast("220943480", false);
            } else if (position == binanceBitcoinRow) {
                copyNumberAndMakeToast("1Pr6GqqWakgKWW1nDjVyHUYo1AcWbSN453", false);
            } else if (position == binanceEthereumRow) {
                copyNumberAndMakeToast("0xa8392346f426443ef7e3d98047bace6dbcc0e7d8", false);
            } else if (position == binanceUSDTRow) {
                copyNumberAndMakeToast("TKnPT5rojMf851ejov2Xu4pxKcMfSh4Ws9", false);
            } else if (position == walletBitcoinRow) {
                copyNumberAndMakeToast("158BXPmSGEcKXpYhVeKU11ETEgsSn4eMt7", false);
            } else if (position == walletTonRow) {
                copyNumberAndMakeToast("UQCK2zt2pHa9ag-lUFTCuvsxW4lqPmkX6eSYFhS5xCKBwKAN", false);
            } else if (position == walletUSDTRow) {
                copyNumberAndMakeToast("UQCK2zt2pHa9ag-lUFTCuvsxW4lqPmkX6eSYFhS5xCKBwKAN", false);
            } else if (position == tonKeeperTonRow) {
                copyNumberAndMakeToast("UQCVVayzZkpm4LoHi9yuJQFjxRqi2qM4o0dqMLvZnXOFFBJy", false);
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
                case VIEW_TYPE_SHADOW:
                    holder.itemView.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case VIEW_TYPE_HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    headerCell.setTopMargin(10);
                    headerCell.setHeight(35);
                    if (position == brandedScreenshotsHeaderRow) {
                        headerCell.setText(getString(R.string.DP_CameraCutoutHeader));
                    } else if (position == bonusesHeaderRow) {
                        headerCell.setText(getString(R.string.DP_DonateBadge));
                    } else if (position == intHeaderRow) {
                        headerCell.setText(getString(R.string.DP_Donate_Method_Int));
                    } else if (position == rusHeaderRow) {
                        headerCell.setText(getString(R.string.DP_Donate_Method_Rus));
                    } else if (position == uzbHeaderRow) {
                        headerCell.setText(getString(R.string.DP_Donate_Method_Uzb));
                    } else if (position == binanceHeaderRow) {
                        headerCell.setText("Binance // Crypto");
                    } else if (position == walletHeaderRow) {
                        headerCell.setText("Telegram Wallet // Crypto");
                    }
                    break;
                case VIEW_TYPE_TEXT_CELL: {
                    TextCell textCell = (TextCell) holder.itemView;
                    textCell.getTextView().setPadding(AndroidUtilities.dp(8), 0, 0, 0);
                    textCell.getImageView().clearColorFilter();

                    CharSequence title = "";
                    int icon = 0;
                    boolean divider = false;
                    boolean isDarkMode = !Theme.isCurrentThemeDay();

                    if (position == masterCardRow) {
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
                    } else if (position == walletBitcoinRow) {
                        title = "Bitcoin (BTC) // @wallet";
                        icon = isDarkMode ? R.drawable.card_btc_dark : R.drawable.card_btc_light;
                    } else if (position == walletTonRow) {
                        title = "TON Coin // @wallet";
                        icon = isDarkMode ? R.drawable.card_ton_dark : R.drawable.card_ton_light;
                    } else if (position == walletUSDTRow) {
                        title = "USDT (TON) // @wallet";
                        icon = isDarkMode ? R.drawable.card_usdt_dark : R.drawable.card_usdt_light;
                    } else if (position == tonKeeperTonRow) {
                        title = "TON Coin (v3R2) // Tonkeeper";
                        icon = isDarkMode ? R.drawable.card_ton_dark : R.drawable.card_ton_light;
                        divider = true;
                    }
                    textCell.setTextAndIcon(title, icon, divider);
                    break;
                }
                case VIEW_TYPE_TEXT_CHECK:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    textCheckCell.setEnabled(true, null);
                    if (position == brandedScreenshotsSwitchRow) {
                        textCheckCell.setTextAndCheck(getString(R.string.DP_CameraCutout), CherrygramCoreConfig.INSTANCE.getCgBrandedScreenshots(), false);
                    }
                    break;
                case VIEW_TYPE_TEXT_INFO_PRIVACY:
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
                case VIEW_TYPE_TABLE:
                    TableView tableView = (TableView) holder.itemView;
                    tableView.clear();
                    if (position == bonusesPriceRow) {
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
                                            .translate(-AndroidUtilities.dp(30), AndroidUtilities.dp(50))
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
                                            .translate(-AndroidUtilities.dp(30), AndroidUtilities.dp(100))
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
                                () -> CherrygramPreferencesNavigator.INSTANCE.createExperimental(DonatesPreferencesEntry.this)
                        );

                        SpannableStringBuilder sb = new SpannableStringBuilder();
                        sb.append(epicBadgeTitle);
                        sb.append("\n");
                        sb.append(marketPlaceText);
                        sb.append("\n");
                        sb.append(filterText);
                        sb.append("\n");
                        sb.append(messageMenuText);
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
                    view.setPadding(AndroidUtilities.dp(20), 0, AndroidUtilities.dp(20), AndroidUtilities.dp(10));
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + viewType);
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == brandedScreenshotsDivisorRow || position == bonusesDivisorRow || position == intDivisorRow || position == rusDivisorRow || position == uzbDivisorRow || position == binanceDivisorRow || position == walletDivisorRow) {
                return VIEW_TYPE_SHADOW;
            } else if (position == brandedScreenshotsHeaderRow || position == bonusesHeaderRow || position == intHeaderRow || position == rusHeaderRow || position == uzbHeaderRow || position == binanceHeaderRow || position == walletHeaderRow) {
                return VIEW_TYPE_HEADER;
            } else if (position == masterCardRow || position == visaRow
                    || position == alfaRow || position == vtbRow || position == sberRow || position == tinkoffRow || position == yooMoneyRow
                    || position == humoRow || position == uzCardRow || position == uzCardMirRow || position == tirikchilikRow
                    || position == binanceIDRow || position ==  binanceBitcoinRow || position == binanceEthereumRow || position == binanceUSDTRow
                    || position == walletBitcoinRow || position == walletTonRow || position == walletUSDTRow || position == tonKeeperTonRow
            ) {
                return VIEW_TYPE_TEXT_CELL;
            } else if (position == brandedScreenshotsSwitchRow) {
                return VIEW_TYPE_TEXT_CHECK;
            } else if (position == brandedScreenshotsInfoRow || position == bonusesInfoRow) {
                return VIEW_TYPE_TEXT_INFO_PRIVACY;
            } else if (position == bonusesPriceRow || position == intCredsRow || position == rusCredsRow) {
                return VIEW_TYPE_TABLE;
            }
            return VIEW_TYPE_SHADOW;
        }
    }

    private void updateRowsId(boolean notify) {
        rowCount = 0;

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

        walletHeaderRow = rowCount++;
        walletBitcoinRow = rowCount++;
        walletTonRow = rowCount++;
        walletUSDTRow = rowCount++;
        tonKeeperTonRow = rowCount++;
        walletDivisorRow = rowCount++;

        if (listAdapter != null && notify) {
            listAdapter.notifyDataSetChanged();
        }
    }

    private void copyNumberAndMakeToast(String cardNumber, boolean card) {
        AndroidUtilities.addToClipboard(cardNumber);
        Toast.makeText(getParentActivity(), getString(card ? R.string.CardNumberCopied : R.string.TextCopied), Toast.LENGTH_SHORT).show();
    }

}
