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
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.AccountFrozenAlert;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.FireworksOverlay;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LinkSpanDrawable;
import org.telegram.ui.Components.Premium.GLIcon.GLIconRenderer;
import org.telegram.ui.Components.Premium.GLIcon.GLIconTextureView;
import org.telegram.ui.Components.Premium.GLIcon.Icon3D;
import org.telegram.ui.Components.Premium.StarParticlesView;
import org.telegram.ui.Components.RLottieDrawable;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.TypefaceSpan;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.GradientHeaderActivity;
import org.telegram.ui.ImageReceiverSpan;
import org.telegram.ui.Stars.BotStarsController;
import org.telegram.ui.Stars.ExplainStarsSheet;
import org.telegram.ui.Stars.StarsController;
import org.telegram.ui.Stars.StarsIntroActivity;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;
import org.telegram.ui.Stories.recorder.HintView2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import uz.unnarsx.cherrygram.core.crashlytics.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.misc.Constants;

public class StarsIntroActivityCG extends GradientHeaderActivity implements NotificationCenter.NotificationCenterDelegate {

    private GLIconTextureView iconTextureView;

    private LinearLayout balanceLayout;
    private SpannableStringBuilder starBalanceIcon;
    private AnimatedTextView starBalanceTextView;

    private String customTitle;
    private String userName;
    private int type;

    private FrameLayout termsView;

    public StarsIntroActivityCG(@Nullable String customTitle, @Nullable String userName, int type) {
        this.customTitle = customTitle;
        this.userName = userName;
        this.type = type;
        setWhiteBackground(true);
    }

    @Override
    public boolean onFragmentCreate() {
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.starBalanceUpdated);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.botStarsUpdated);
        StarsController.getInstance(currentAccount).getOptions();
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.starBalanceUpdated);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.botStarsUpdated);
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.starBalanceUpdated) {
            updateBalance();
        } else if (id == NotificationCenter.botStarsUpdated) {
            if (getUserConfig().getClientUserId() == (long) args[0]) {
                updateBalance();
            }
        }
    }

    @Override
    public View createView(Context context) {
        useFillLastLayoutManager = false;
        particlesViewHeight = dp(32 + 190 + 16);

        super.createView(context);

        FrameLayout aboveTitleView = new FrameLayout(context);
        aboveTitleView.setClickable(true);
        iconTextureView = new GLIconTextureView(context, GLIconRenderer.DIALOG_STYLE, Icon3D.TYPE_GOLDEN_STAR);
        iconTextureView.mRenderer.colorKey1 = Theme.key_starsGradient1;
        iconTextureView.mRenderer.colorKey2 = Theme.key_starsGradient2;
        iconTextureView.mRenderer.updateColors();
        iconTextureView.setStarParticlesView(particlesView);
        aboveTitleView.addView(iconTextureView, LayoutHelper.createFrame(190, 190, Gravity.CENTER, 0, 12, 0, 24));

        if (customTitle != null && !customTitle.isEmpty()) {
            configureHeader(
                    customTitle,
                    AndroidUtilities.replaceArrows(
                            AndroidUtilities.replaceTags(
                                    getSubtitle(type, userName)
                            ), true),
                    aboveTitleView,
                    null
            );
        } else {
            configureHeader(
                    getString(R.string.TelegramStars),
                    AndroidUtilities.replaceArrows(
                            AndroidUtilities.replaceSingleTag(
                                    getString(R.string.TelegramStarsInfo2),
                                    () -> new ExplainStarsSheet(context).show()
                            ), true),
                    aboveTitleView,
                    null
            );
        }

        listView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setSupportsChangeAnimations(false);
        itemAnimator.setDelayAnimations(false);
        itemAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        itemAnimator.setDurations(350);
        listView.setItemAnimator(itemAnimator);
        listView.setOnItemClickListener((view, position) -> {
            if (adapter == null) return;
        });

        FireworksOverlay fireworksOverlay = new FireworksOverlay(getContext());
        contentView.addView(fireworksOverlay, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        createBalanceView();
        createButtonsView(context);
        createTermsView(context);

        updateBalance();

        if (adapter != null) {
            adapter.update(false);
        }

        BotStarsController.getInstance(currentAccount).preloadStarsStats(getUserConfig().getClientUserId());

        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("safestars_screen");

        return fragmentView;
    }

    private void createBalanceView() {
        balanceLayout = new LinearLayout(getContext());
        balanceLayout.setOrientation(LinearLayout.VERTICAL);
        balanceLayout.setPadding(0, dp(24), 0, dp(10));

        starBalanceTextView = new AnimatedTextView(getContext(), false, true, false);
        starBalanceTextView.setTypeface(AndroidUtilities.bold());
        starBalanceTextView.setTextSize(dp(32));
        starBalanceTextView.setGravity(Gravity.CENTER);
        starBalanceTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourceProvider));

        starBalanceIcon = new SpannableStringBuilder("S");
        final ImageReceiverSpan starBalanceIconSpan = new ImageReceiverSpan(starBalanceTextView, currentAccount, 42);
        starBalanceIconSpan.imageReceiver.setImageBitmap(new RLottieDrawable(R.raw.star_reaction, "s" + R.raw.star_reaction, dp(42), dp(42)));
        starBalanceIconSpan.imageReceiver.setAutoRepeat(2);
        starBalanceIconSpan.enableShadow(false);
        starBalanceIconSpan.translate(-dp(3), 0);
        starBalanceIcon.setSpan(starBalanceIconSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        balanceLayout.addView(starBalanceTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 40, Gravity.CENTER, 24, 0, 24, 0));

        TextView starBalanceTitleView = new TextView(getContext());
        starBalanceTitleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        starBalanceTitleView.setGravity(Gravity.CENTER);
        starBalanceTitleView.setText(LocaleController.getString(R.string.YourStarsBalance));
        starBalanceTitleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2, resourceProvider));
        balanceLayout.addView(starBalanceTitleView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 24, 0, 24, 0));
    }

    private void createButtonsView(Context context) {
        LinearLayout oneButtonsLayout = new LinearLayout(getContext());
        oneButtonsLayout.setOrientation(LinearLayout.VERTICAL);
        oneButtonsLayout.setBackgroundColor(Color.TRANSPARENT);

        FrameLayout buttonsLayout = new FrameLayout(getContext());
        buttonsLayout.addView(oneButtonsLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        ButtonWithCounterView buyViaSafeStarsButton = new ButtonWithCounterView(getContext(), resourceProvider);
        buyViaSafeStarsButton.setRound();
        buyViaSafeStarsButton.setText(getString(R.string.CG_SafeStars_buy), false);
        buyViaSafeStarsButton.setOnClickListener(v -> {
            if (MessagesController.getInstance(currentAccount).isFrozen()) {
                AccountFrozenAlert.show(currentAccount);
                return;
            }
            openSafeStars(context);
        });
        oneButtonsLayout.addView(buyViaSafeStarsButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, 0, 0, 0, 8));

        ButtonWithCounterView buyViaTelegramButton = new ButtonWithCounterView(getContext(), false, true, resourceProvider);
        buyViaTelegramButton.setRoundRadius(24);
        buyViaTelegramButton.text.setTypeface(AndroidUtilities.bold());
        buyViaTelegramButton.setText(getString(R.string.CG_SafeStars_buy_Telegram), false);
        buyViaTelegramButton.setOnClickListener(v -> {
            if (MessagesController.getInstance(currentAccount).isFrozen()) {
                AccountFrozenAlert.show(currentAccount);
                return;
            }
            StarsIntroActivity.StarsOptionsSheet.fromSafeStars = true;
            new StarsIntroActivity.StarsOptionsSheet(context, resourceProvider).show();
        });
        oneButtonsLayout.addView(buyViaTelegramButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));

        balanceLayout.addView(buttonsLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 20, 17, 20, 0));
    }

    private void openSafeStars(Context context) {
        Set<String> cyrillicLangs = new HashSet<>(Arrays.asList(
                "ru", // Русский
                "uk", // Украинский
                "be", // Белорусский
                "bg", // Болгарский
                "sr", // Сербский
                "kk", // Казахский
                "ky", // Киргизский
                "tg", // Таджикский
                "uz"  // Узбекский
        ));

        String lang = LocaleController.getInstance().getCurrentLocaleInfo().shortName;

        Browser.openUrl(context, cyrillicLangs.contains(lang) ? Constants.CG_SAFESTARS_RU : Constants.CG_SAFESTARS);
    }

    private void createTermsView(Context context) {
        termsView = new FrameLayout(context);
        LinkSpanDrawable.LinksTextView footerTextView = new LinkSpanDrawable.LinksTextView(context, resourceProvider);

        termsView.setPadding(0, dp(11), 0, dp(11));
        footerTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        footerTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText4, resourceProvider));
        footerTextView.setLinkTextColor(Theme.getColor(Theme.key_chat_messageLinkIn, resourceProvider));

        SpannableStringBuilder text = AndroidUtilities.replaceTags(getString(R.string.CG_Stats_TOS), AndroidUtilities.FLAG_TAG_BOLD);

        Object[] spans = text.getSpans(0, text.length(), TypefaceSpan.class);
        if (spans != null && spans.length >= 2) {
            int start1 = text.getSpanStart(spans[0]);
            int end1 = text.getSpanEnd(spans[0]);
            text.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    Browser.openUrl(context, getString(R.string.StarsTOSLink));
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            }, start1, end1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            int start2 = text.getSpanStart(spans[1]);
            int end2 = text.getSpanEnd(spans[1]);
            text.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    Browser.openUrl(context, Constants.CG_DONATIONS_AND_TERMS_URL);
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            }, start2, end2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        footerTextView.setText(text);
        footerTextView.setMovementMethod(LinkMovementMethod.getInstance());
        footerTextView.setHighlightColor(Color.TRANSPARENT);
        footerTextView.setGravity(Gravity.CENTER);
        footerTextView.setMaxWidth(HintView2.cutInFancyHalf(footerTextView.getText(), footerTextView.getPaint()));

        termsView.addView(footerTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 20, 0, 20, 0));
//        termsView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite, resourceProvider)); // Breaks monet
    }

    private void updateBalance() {
        final StarsController s = StarsController.getInstance(currentAccount);

        final SpannableStringBuilder sb = new SpannableStringBuilder();
        sb.append(starBalanceIcon);
        sb.append(StarsIntroActivity.formatStarsAmount(s.getBalance(), 0.66f, ' '));
        starBalanceTextView.setText(sb);
    }

    @Override
    public StarParticlesView createParticlesView() {
        return StarsIntroActivity.makeParticlesView(getContext(), 75, 1);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (iconTextureView != null) {
            iconTextureView.setPaused(false);
            iconTextureView.setDialogVisible(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (iconTextureView != null) {
            iconTextureView.setPaused(true);
            iconTextureView.setDialogVisible(true);
        }
    }

    @Override
    protected View getHeader(Context context) {
        return super.getHeader(context);
    }

    private UniversalAdapter adapter;
    @Override
    protected RecyclerView.Adapter<?> createAdapter() {
        adapter = new UniversalAdapter(listView, getContext(), currentAccount, classGuid, true, this::fillItems, getResourceProvider()) {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                if (viewType == UniversalAdapter.VIEW_TYPE_ANIMATED_HEADER) {
                    HeaderCell headerCell = new HeaderCell(getContext(), Theme.key_windowBackgroundWhiteBlueHeader, 21, 0, false, resourceProvider);
                    headerCell.setHeight(40 - 15);
                    return new RecyclerListView.Holder(headerCell);
                }
                return super.onCreateViewHolder(parent, viewType);
            }
        };
        adapter.setApplyBackground(false);
        return adapter;
    }

    public void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        if (getContext() == null) {
            return;
        }

        items.add(UItem.asFullyCustom(getHeader(getContext())));
        items.add(UItem.asCustom(balanceLayout));
        items.add(UItem.asCustom(termsView));
    }

    private String getSubtitle(int type, String botName) {
        String stringRes;
        if (type == StarsIntroActivity.StarsNeededSheet.TYPE_SUBSCRIPTION_BUY) {
            stringRes = "StarsNeededTextBuySubscription";
        } else if (type == StarsIntroActivity.StarsNeededSheet.TYPE_SUBSCRIPTION_KEEP) {
            stringRes = "StarsNeededTextKeepSubscription";
        } else if (type == StarsIntroActivity.StarsNeededSheet.TYPE_BOT_SUBSCRIPTION_KEEP) {
            stringRes = "StarsNeededTextKeepBotSubscription";
        } else if (type == StarsIntroActivity.StarsNeededSheet.TYPE_BIZ_SUBSCRIPTION_KEEP) {
            stringRes = "StarsNeededTextKeepBizSubscription";
        } else if (type == StarsIntroActivity.StarsNeededSheet.TYPE_SUBSCRIPTION_REFULFILL) {
            stringRes = "StarsNeededTextKeepSubscription";
        } else if (type == StarsIntroActivity.StarsNeededSheet.TYPE_LINK) {
            stringRes = botName == null ? "StarsNeededTextLink" : "StarsNeededTextLink_" + botName.toLowerCase();
            if (LocaleController.nullable(getString(stringRes)) == null) {
                stringRes = "StarsNeededTextLink";
            }
        } else if (type == StarsIntroActivity.StarsNeededSheet.TYPE_REACTIONS) {
            stringRes = "StarsNeededTextReactions";
        } else if (type == StarsIntroActivity.StarsNeededSheet.TYPE_STAR_GIFT_BUY) {
            stringRes = "StarsNeededTextGift";
        } else if (type == StarsIntroActivity.StarsNeededSheet.TYPE_STAR_GIFT_BUY_CHANNEL) {
            stringRes = "StarsNeededTextGiftChannel";
        } else if (type == StarsIntroActivity.StarsNeededSheet.TYPE_PRIVATE_MESSAGE) {
            stringRes = "StarsNeededTextPrivateMessage";
        } else if (type == StarsIntroActivity.StarsNeededSheet.TYPE_STAR_GIFT_UPGRADE) {
            stringRes = "StarsNeededTextGiftUpgrade";
        } else if (type == StarsIntroActivity.StarsNeededSheet.TYPE_STAR_GIFT_TRANSFER) {
            stringRes = "StarsNeededTextGiftTransfer";
        } else if (type == StarsIntroActivity.StarsNeededSheet.TYPE_BIZ) {
            stringRes = "StarsNeededBizText";
        } else if (type == StarsIntroActivity.StarsNeededSheet.TYPE_STAR_GIFT_BUY_RESALE) {
            stringRes = "StarsNeededTextGiftBuyResale";
        } else if (type == StarsIntroActivity.StarsNeededSheet.TYPE_SEARCH) {
            stringRes = "StarsNeededTextSearch";
        } else if (type == StarsIntroActivity.StarsNeededSheet.TYPE_REMOVE_GIFT_DESCRIPTION) {
            stringRes = "StarsNeededRemoveGiftDescription";
        } else if (type == StarsIntroActivity.StarsNeededSheet.TYPE_LIVE_COMMENTS) {
            stringRes = "StarsNeededLiveComments";
        } else {
            stringRes = "StarsNeededText";
        }

        String str = LocaleController.nullable(formatString(stringRes, LocaleController.getStringResId(stringRes), botName));
        if (str == null) {
            str = getString(stringRes);
        }

        return str;
    }

}
