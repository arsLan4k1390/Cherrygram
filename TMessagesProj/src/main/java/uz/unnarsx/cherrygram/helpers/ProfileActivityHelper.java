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
import static org.telegram.messenger.AndroidUtilities.lerp;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;

import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BaseController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_account;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.EmojiPacksAlert;
import org.telegram.ui.Components.ItemOptions;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.TypefaceSpan;
import org.telegram.ui.PeerColorActivity;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.Stories.ChannelBoostUtilities;
import org.telegram.ui.Stories.recorder.HintView2;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig;
import uz.unnarsx.cherrygram.donates.BadgeHelper;
import uz.unnarsx.cherrygram.donates.DonatesManager;
import uz.unnarsx.cherrygram.misc.Constants;
import uz.unnarsx.cherrygram.preferences.CherrygramPreferencesNavigator;

public class ProfileActivityHelper extends BaseController {

    private static final ProfileActivityHelper[] Instance = new ProfileActivityHelper[UserConfig.MAX_ACCOUNT_COUNT];

    public ProfileActivityHelper(int num) {
        super(num);
    }

    public static ProfileActivityHelper getInstance(int num) {
        ProfileActivityHelper localInstance = Instance[num];
        if (localInstance == null) {
            synchronized (ProfileActivityHelper.class) {
                localInstance = Instance[num];
                if (localInstance == null) {
                    Instance[num] = localInstance = new ProfileActivityHelper(num);
                }
            }
        }
        return localInstance;
    }

    /** Options start */
    public final static int OPTION_RESTART = 1000;
    public final static int OPTION_BOOST_CHANNEL = 1001;
    public final static int OPTION_GET_PROFILE_BACKGROUND = 1002;
    public final static int OPTION_APPLY_PROFILE_BACKGROUND = 1003;
    public final static int OPTION_USER_INFO = 1004;

    public void injectCherryFeats(ActionBarMenuItem otherItem, TLRPC.User user, TLRPC.EncryptedChat currentEncryptedChat, boolean isBot) {
        otherItem.addColoredGap();

        long emojiDocumentId = UserObject.getProfileEmojiId(user);
        if (!UserObject.isUserSelf(user) && currentEncryptedChat == null && !isBot) {
            if (emojiDocumentId != 0
                    && CherrygramAppearanceConfig.INSTANCE.getProfileBackgroundEmoji()
            ) {
                otherItem.addSubItem(ProfileActivityHelper.OPTION_GET_PROFILE_BACKGROUND, R.drawable.msg_emoji_stickers, getString(R.string.CG_GetEmojiPack));
            }
            if (getUserConfig().isPremium() &&
                    UserObject.getProfileEmojiId(getUserConfig().getCurrentUser()) != emojiDocumentId
                    && CherrygramAppearanceConfig.INSTANCE.getProfileBackgroundEmoji()
            ) {
                otherItem.addSubItem(ProfileActivityHelper.OPTION_APPLY_PROFILE_BACKGROUND, R.drawable.msg_emoji_stickers, getString(R.string.CG_ProfileBackground));
            }
        }

        otherItem.addSubItem(ProfileActivityHelper.OPTION_USER_INFO, R.drawable.icon_json_solar, getString(R.string.Info));
    }

    public void injectPhoneNumber(
            BaseFragment fragment,
            ItemOptions itemOptions,
            String phone
    ) {
        itemOptions.addGap();

        TextView phoneInfoView = new TextView(fragment.getContext());
        phoneInfoView.setPadding(AndroidUtilities.dp(13), AndroidUtilities.dp(8), AndroidUtilities.dp(13), AndroidUtilities.dp(8));
        phoneInfoView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        phoneInfoView.setTextColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuItem, fragment.getResourceProvider()));
        phoneInfoView.setLinkTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteValueText, fragment.getResourceProvider()));
        phoneInfoView.setBackground(Theme.createRadSelectorDrawable(Theme.getColor(Theme.key_dialogButtonSelector, fragment.getResourceProvider()), 0,6));

        boolean isFragmentPhoneNumber = phone != null && phone.matches("888\\d{8}");

        String phoneInfoString = LocaleController.getString(isFragmentPhoneNumber ? R.string.AnonymousNumber : R.string.PhoneMobile) +
                ": " +
                "*" +
                PhoneFormat.getInstance().format("+" + phone) +
                "*";

        SpannableStringBuilder spanned = new SpannableStringBuilder(AndroidUtilities.replaceTags(phoneInfoString));

        int startIndex = TextUtils.indexOf(spanned, '*');
        int lastIndex = TextUtils.lastIndexOf(spanned, '*');
        if (startIndex != -1 && lastIndex != -1 && startIndex != lastIndex) {
            spanned.replace(lastIndex, lastIndex + 1, "");
            spanned.replace(startIndex, startIndex + 1, "");
            spanned.setSpan(new TypefaceSpan(AndroidUtilities.bold()), startIndex, lastIndex - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanned.setSpan(new ForegroundColorSpan(phoneInfoView.getLinkTextColors().getDefaultColor()), startIndex, lastIndex - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        phoneInfoView.setText(spanned);
        phoneInfoView.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+" + phone));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                fragment.getParentActivity().startActivityForResult(intent, 500);
            } catch (Exception e) {
                FileLog.e(e);
            }
            itemOptions.dismiss();
        });

        itemOptions.addView(phoneInfoView);
    }

    public void boostChannel(Context context, long dialogID) {
        Browser.openUrl(context, ChannelBoostUtilities.createLink(currentAccount, dialogID));
    }

    public void getProfileBackground(BaseFragment fragment, long dialogID) {
        if (fragment == null || fragment.getContext() == null || fragment.getResourceProvider() == null) {
            return;
        }

        long emojiDocumentId = UserObject.getProfileEmojiId(getMessagesController().getUser(dialogID));

        AnimatedEmojiDrawable.getDocumentFetcher(currentAccount).fetchDocument(emojiDocumentId, document -> AndroidUtilities.runOnUIThread(() -> {
            ArrayList<TLRPC.InputStickerSet> inputSets = new ArrayList<>(1);
            inputSets.add(MessageObject.getInputStickerSet(document));
            EmojiPacksAlert alert = new EmojiPacksAlert(fragment, fragment.getContext(), fragment.getResourceProvider(), inputSets);
            alert.show();
        }));
    }

    public void applyProfileBackground(BaseFragment fragment, long dialogID) {
        long emojiDocumentId = UserObject.getProfileEmojiId(getMessagesController().getUser(dialogID));
        int colorId = UserObject.getProfileColorId(getMessagesController().getUser(dialogID));
        TLRPC.User me = getUserConfig().getCurrentUser();

        if (me.profile_color == null) {
            me.profile_color = new TLRPC.PeerColor();
        }
        TL_account.updateColor req = new TL_account.updateColor();
        req.for_profile = true;
        me.flags2 |= 512;

        if (colorId >= 0) {
            me.profile_color.flags |= 1;
            if (req.color == null) {
                req.flags |= 4;
                req.color = new TLRPC.TL_peerColor();
            }
            req.color.flags |= 1;
            req.color.color = me.profile_color.color = colorId;
        } else {
            me.profile_color.flags &= ~1;
        }

        if (emojiDocumentId != 0) {
            me.profile_color.flags |= 2;
            if (req.color == null) {
                req.flags |= 4;
                req.color = new TLRPC.TL_peerColor();
            }
            req.color.flags |= 2;
            req.color.background_emoji_id = me.profile_color.background_emoji_id = emojiDocumentId;
        } else {
            me.profile_color.flags &= ~2;
            me.profile_color.background_emoji_id = 0;
            if (req.color != null) {
                req.color.flags &= ~2;
                req.color.background_emoji_id = 0;
            }
        }

        getConnectionsManager().sendRequest(req, (res, err) -> {
            if (res != null) {
                AndroidUtilities.runOnUIThread(() -> fragment.presentFragment(new PeerColorActivity(0).startOnProfile().setOnApplied(fragment)), 300);
            }
        });
    }

    public void showCherryUserInfo(BaseFragment baseFragment, long userID) {
        boolean isPremium = false; // cgPremium
        boolean isDonated = DonatesManager.INSTANCE.didUserDonate2(userID);
        boolean isDonatedForMarketplace = DonatesManager.INSTANCE.didUserDonateForMarketplace(userID);
        boolean hasCustomEmojiColor = BadgeHelper.Companion.hasCustomUserColor(userID);
        String customColor = BadgeHelper.Companion.getUserColorString(userID);

        boolean isBlocked = DonatesManager.INSTANCE.isUserBlocked(userID);

        StringBuilder sb = new StringBuilder();
        sb.append("isCherryPremium: ").append(isPremium)
                .append("\nisDonated: ").append(isDonated)
                .append("\nisDonatedForMarketplace: ").append(isDonatedForMarketplace)
                .append("\n\nhasCustomEmojiColor: ").append(hasCustomEmojiColor);

        if (customColor != null && hasCustomEmojiColor) {
            sb.append("\ncustomColor: ").append(customColor);
        }

        sb.append("\n\nisBlocked: ").append(isBlocked);

        AlertDialog.Builder builder = new AlertDialog.Builder(baseFragment.getParentActivity(), baseFragment.getResourceProvider());
        builder.setTitle(getString(R.string.Info));
        builder.setMessage(sb);
        builder.setPositiveButton(getString(R.string.OK), null);
        baseFragment.showDialog(builder.create());
    }
    /** Options finish */

    /** Badges start */
    private HintView2 donatorHint;
    private int donatorHintBackgroundColor;
    private Boolean donatorHintVisible;

    public void checkCherrygramBadges(
            ProfileActivity profileActivity,
            SimpleTextView[] nameTextView,
            int a,
            MessagesController.PeerColor peerColor,
            FrameLayout avatarContainer2,
            float extraHeight,
            float currentExpandAnimatorValue,
            float currentExpanAnimatorFracture,
            float[] expandAnimatorValues,
            TLRPC.User user
    ) {
        if (user == null) return;

        long emojiDocumentId;
        boolean isPremium = false; // cgPremium
        boolean isDonated = DonatesManager.INSTANCE.didUserDonate(user.id);
        boolean forceBra = user.id == Constants.Cherrygram_Owner;
        boolean showParticles = isPremium || forceBra || DonatesManager.INSTANCE.didUserDonateForMarketplace(user.id);

        if (isPremium && isDonated) {
            emojiDocumentId = Constants.CHERRY_EMOJI_ID_VERIFIED_BRA;
        } else if (isPremium || isDonated || forceBra) {
            emojiDocumentId = isPremium || forceBra ? Constants.CHERRY_EMOJI_ID_VERIFIED_BRA : Constants.CHERRY_EMOJI_ID_VERIFIED;
        } else {
            emojiDocumentId = 0;
        }

        if (emojiDocumentId != 0) {
            nameTextView[a].setRightDrawable2(profileActivity.getEmojiStatusDrawable(emojiDocumentId, false, showParticles, 22, a));
            nameTextView[a].setRightDrawableInside(true);

            nameTextView[a].setRightDrawable2OnClick(v -> {
                TLRPC.Document document = AnimatedEmojiDrawable.findDocument(currentAccount, emojiDocumentId);
                SpannableStringBuilder stringBuilder = new SpannableStringBuilder(AndroidUtilities.replaceTags(formatString(R.string.DP_Donate_Bulletin, UserObject.getUserName(user))));

                BulletinFactory.of(profileActivity).createDonatesBulletin(
                        document,
                        stringBuilder,
                        getString(R.string.LearnMore),
                        Bulletin.DURATION_PROLONG,
                        () -> CherrygramPreferencesNavigator.INSTANCE.createDonate(profileActivity)
                ).show();
                showDonatorHint(
                        profileActivity,
                        nameTextView,
                        peerColor,
                        avatarContainer2,
                        extraHeight,
                        currentExpandAnimatorValue,
                        currentExpanAnimatorFracture,
                        expandAnimatorValues,
                        user
                );
            });
        }
    }

    private void showDonatorHint(
            BaseFragment fragment,
            SimpleTextView[] nameTextView,
            MessagesController.PeerColor peerColor,
            FrameLayout avatarContainer2,
            float extraHeight,
            float currentExpandAnimatorValue,
            float currentExpanAnimatorFracture,
            float[] expandAnimatorValues,
            TLRPC.User user
    ) {
        if (user == null) return;
        if (avatarContainer2 == null) return;
        if (donatorHint != null) donatorHint.hide();

        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(
                AndroidUtilities.replaceTags(
                        formatString(R.string.DP_Donate_Bulletin, UserObject.getUserName(user))
                )
        );

        donatorHintVisible = null;
        donatorHint = new HintView2(fragment.getContext(), HintView2.DIRECTION_BOTTOM);
        if (CherrygramAppearanceConfig.INSTANCE.getProfileBackgroundColor()) {
            donatorHintBackgroundColor = peerColor != null && peerColor.getBgColor1(Theme.isCurrentThemeDark()) != peerColor.getBgColor2(Theme.isCurrentThemeDark()) ? peerColor.getBgColor1(Theme.isCurrentThemeDark()) : fragment.getThemedColor(Theme.key_undo_background);
        } else {
            donatorHintBackgroundColor = fragment.getThemedColor(Theme.key_undo_background);
        }
        donatorHint.setPadding(dp(4), 0, dp(4), dp(2));
        donatorHint.setFlicker(.66f, Theme.multAlpha(11922687 | 0xFF000000, 0.5f));
        avatarContainer2.addView(donatorHint, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 24));
        donatorHint.setTextSize(10f);
        donatorHint.setText(stringBuilder);
        donatorHint.setDuration(-1);
        donatorHint.setInnerPadding(6, 3, 6, 3);
        donatorHint.setArrowSize(4, 2.66f);
        donatorHint.setRoundingWithCornerEffect(false);
        donatorHint.setRounding(16);
        donatorHint.show();
        donatorHint.setOnClickListener(v -> CherrygramPreferencesNavigator.INSTANCE.createDonate(fragment));
        if (extraHeight < dp(82)) {
            donatorHintVisible = false;
            donatorHint.setAlpha(0.0f);
        }
        updateDonatorHint(
                nameTextView,
                extraHeight,
                currentExpandAnimatorValue,
                currentExpanAnimatorFracture,
                expandAnimatorValues
        );
        AndroidUtilities.runOnUIThread(donatorHint::hide, 5500);
    }

    public void updateDonatorHint(
            SimpleTextView[] nameTextView,
            float extraHeight,
            float currentExpandAnimatorValue,
            float currentExpanAnimatorFracture,
            float[] expandAnimatorValues
    ) {
        if (donatorHint == null) return;

        float emojiRight;
        int extra;
        if (nameTextView[1].getRightDrawableWidth() != 0) {
            emojiRight = (nameTextView[1].getRightDrawableX()
                    - nameTextView[1].getRightDrawableWidth() * lerp(0.45f, 0.25f, currentExpandAnimatorValue))
                    * nameTextView[1].getScaleX();

            extra = dp(30);
        } else {
            float textWidth;

            if (nameTextView[1].getLayout() != null) {
                textWidth = nameTextView[1].getLayout().getLineWidth(0);
            } else {
                textWidth = nameTextView[1].getPaint().measureText(nameTextView[1].getText().toString());
            }

            emojiRight = textWidth * nameTextView[1].getScaleX();

            extra = dp(2);
        }

        donatorHint.setJointPx(
                0,
                -donatorHint.getPaddingLeft()
                        + nameTextView[1].getX()
                        + emojiRight
                        + extra
        );

        final float expanded = AndroidUtilities.lerp(expandAnimatorValues, currentExpanAnimatorFracture);
        donatorHint.setTranslationY(-donatorHint.getPaddingBottom() + nameTextView[1].getY() - dp(24) + lerp(dp(6), -dp(12), expanded));
        donatorHint.setBgColor(ColorUtils.blendARGB(donatorHintBackgroundColor, 0x50000000, expanded));
        final boolean visible = extraHeight >= dp(82);
        if (donatorHintVisible == null || donatorHintVisible != visible) {
            donatorHint.animate().alpha((donatorHintVisible = visible) ? 1.0f : 0.0f).setInterpolator(CubicBezierInterpolator.EASE_OUT).setDuration(200).start();
        }
    }
    /** Badges finish */

}
