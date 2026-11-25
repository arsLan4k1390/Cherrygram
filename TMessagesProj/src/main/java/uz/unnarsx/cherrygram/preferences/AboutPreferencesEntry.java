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
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ScaleStateListAnimator;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.LauncherIconController;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.community.CommunitySheet;
import org.telegram.ui.community.cells.CommunityLinkView;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.core.firebase.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.core.helpers.DeeplinkHelper;
import uz.unnarsx.cherrygram.misc.Constants;
import uz.unnarsx.cherrygram.preferences.cells.AboutHeaderView;
import uz.unnarsx.cherrygram.preferences.cells.LauncherIconTint;

public class AboutPreferencesEntry extends BaseCGPreferencesEntry {

    private final int channelRow = 1;
    private final int betaChannelRow = 2;
    private final int chatRow = 3;
    private final int offtopicChatRow = 4;

    private final int tikTokRow = 5;

    private final int githubRow = 6;
    private final int crowdinRow = 7;

    private final int headerItem = 8;
    private final int chatsItem = 9;
    private final int communityItem = 10;
    private final int projectItem = 11;
    private final int footerItem = 12;
    private final int tikTokShadowItem = 13;
    private final int tikTokItem = 14;

    private final Theme.ResourcesProvider darkResourcesProvider = new AppIconsPreferencesEntry.DefaultDarkThemeProvider();

    private TLRPC.Chat cherrygramSupportChat;
    private boolean cherrygramSupportChatLoaded;

    private AboutHeaderView headerView;
    private LinearLayout communityGroup;
    private int communityGroupItem;
    private LinearLayout tikTokGroup;
    private LinearLayout projectGroup;
    private LinearLayout footerView;
    private TextView websiteView;
    private TextView policyView;
    private TextView donateView;

    @Override
    public boolean onFragmentCreate() {
        setResourceProvider(darkResourcesProvider);
        return super.onFragmentCreate();
    }

    @Override
    public Theme.ResourcesProvider getResourceProvider() {
        return darkResourcesProvider;
    }

    @Override
    public int getThemedColor(int key) {
        if (key == Theme.key_windowBackgroundGray) {
            int tint = LauncherIconTint.getColor(LauncherIconController.getCurrentIcon());
            int color = ColorUtils.compositeColors(ColorUtils.setAlphaComponent(tint, 140), super.getThemedColor(key));
            return ColorUtils.compositeColors(ColorUtils.setAlphaComponent(tint, 50), color);
        }
        return super.getThemedColor(key);
    }

    @Override
    public boolean isLightStatusBar() {
        return false;
    }

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("about_preferences_screen");
        return getString(R.string.CGP_Header_About);
    }

    @Override
    protected String getKey() {
        return DeeplinkHelper.DeepLinksRepo.CG_About;
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        if (headerView == null) {
            return;
        }
        items.add(asFullyCustom(headerItem, headerView));
        if (communityGroup != null) {
            items.add(asFullyCustom(communityGroupItem, communityGroup));
        }
        UItem tikTokShadow = UItem.asShadow(null);
        tikTokShadow.id = tikTokShadowItem;
        items.add(tikTokShadow);
        items.add(asFullyCustom(tikTokItem, tikTokGroup));
        if (projectGroup != null) {
            items.add(asFullyCustom(projectItem, projectGroup));
        }
        items.add(asFullyCustom(footerItem, footerView));
    }

    private static UItem asFullyCustom(int id, View view) {
        UItem item = UItem.asFullyCustom(view);
        item.id = id;
        return item;
    }

    @Override
    public View createView(Context context) {
        headerView = null;
        View view = super.createView(context);
        contentView.setBackgroundColor(darkResourcesProvider.getColor(Theme.key_windowBackgroundGray));
        listView.setSectionsDrawBackground(false);

        headerView = new AboutHeaderView(context, getResourceProvider(), new AboutHeaderView.Delegate() {
            @Override
            public void onUpdatesClick() {
                if (LaunchActivity.instance == null) return;
                LaunchActivity.instance.showUpdaterBottomSheet(AboutPreferencesEntry.this, false, null);
            }

            @Override
            public void onUpdatesLongClick() {
                AndroidUtilities.addToClipboard("https://" + getMessagesController().linkPrefix + "/cgSettings/" + DeeplinkHelper.DeepLinksRepo.CG_Updater_Bottom_Sheet);
                BulletinFactory.of(AboutPreferencesEntry.this).createCopyLinkBulletin().show();
            }

            @Override
            public void onLinkClick(int id) {
                if (id == channelRow) {
                    getMessagesController().openByUserName(Constants.CG_CHANNEL_USERNAME, AboutPreferencesEntry.this, 1);
                } else if (id == betaChannelRow) {
                    getMessagesController().openByUserName(Constants.CG_BETA_APKS_CHANNEL_USERNAME, AboutPreferencesEntry.this, 1);
                } else if (id == chatRow) {
                    getMessagesController().openByUserName(Constants.CG_CHAT_USERNAME, AboutPreferencesEntry.this, 1);
                } else if (id == offtopicChatRow) {
                    getMessagesController().openByUserName(Constants.CG_OFFTOPIC_CHAT_USERNAME, AboutPreferencesEntry.this, 1);
                } else if (id == tikTokRow) {
                    Browser.openUrl(getContext(), Constants.TIKTOK_URL);
                } else if (id == githubRow) {
                    if (CherrygramCoreConfig.isStandaloneBetaBuild() || CherrygramCoreConfig.isDevBuild()) {
                        Browser.openInTelegramBrowser(getContext(), Constants.CG_GITHUB_URL, null);
                    } else {
                        Browser.openInTelegramBrowser(getContext(), Constants.CG_GITHUB_URL + "/commit/" + BuildConfig.GIT_COMMIT_HASH, null);
                    }
                } else if (id == crowdinRow) {
                    Browser.openInTelegramBrowser(getContext(), Constants.CG_CROWDIN_URL, null);
                }
            }

            @Override
            public boolean onLinkLongClick(int id) {
                if (id == channelRow) {
                    AndroidUtilities.addToClipboard("@" + Constants.CG_CHANNEL_USERNAME);
                } else if (id == betaChannelRow) {
                    AndroidUtilities.addToClipboard("@" + Constants.CG_BETA_APKS_CHANNEL_USERNAME);
                } else if (id == chatRow) {
                    AndroidUtilities.addToClipboard("@" + Constants.CG_CHAT_USERNAME);
                } else if (id == offtopicChatRow) {
                    AndroidUtilities.addToClipboard("@" + Constants.CG_OFFTOPIC_CHAT_USERNAME);
                } else if (id == tikTokRow) {
                    AndroidUtilities.addToClipboard(Constants.TIKTOK_URL);
                } else if (id == githubRow) {
                    AndroidUtilities.addToClipboard(BuildConfig.GIT_COMMIT_HASH);
                } else if (id == crowdinRow) {
                    AndroidUtilities.addToClipboard(Constants.CG_CROWDIN_URL);
                } else {
                    return false;
                }
                return true;
            }
        });

        listView.addItemDecoration(headerView.new Background(), 0);

        ArrayList<AboutHeaderView.Link> tikTok = new ArrayList<>();
        tikTok.add(new AboutHeaderView.Link(tikTokRow, R.drawable.tiktok_logo_outline, "TikTok", "@cherrygram_official"));
        tikTokGroup = headerView.createGroup(null, null, tikTok, 0);

        ArrayList<AboutHeaderView.Link> project = new ArrayList<>();
        if (!CherrygramCoreConfig.isStandalonePremiumBuild()) {
            String source = "commit " + BuildConfig.GIT_COMMIT_HASH.substring(0, 8);
            if (CherrygramCoreConfig.isStandaloneBetaBuild() || CherrygramCoreConfig.isDevBuild()) {
                source = "GitHub";
            }
            project.add(new AboutHeaderView.Link(githubRow, R.drawable.github_cat, getString(R.string.CGP_Source), source));
        }
        project.add(new AboutHeaderView.Link(crowdinRow, R.drawable.msg_translate_solar, getString(R.string.CGP_Crowdin), "Crowdin"));
        projectGroup = headerView.createGroup(getString(R.string.CGP_About_Project), null, project, 11);

        footerView = new AboutHeaderView.FullWidthLayout(context);
        footerView.setPadding(0, 0, 0, dp(16));

        TextView creditsView = new TextView(context);
        creditsView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        creditsView.setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteGrayText));
        creditsView.setGravity(Gravity.CENTER_HORIZONTAL);
        creditsView.setLineSpacing(dp(2), 1f);
        creditsView.setText(getString(R.string.CGP_About_Credits));
        footerView.addView(creditsView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 32, 14, 32, 0));

        LinearLayout footerLinks = new LinearLayout(context);
        footerLinks.setOrientation(LinearLayout.HORIZONTAL);
        footerLinks.setGravity(Gravity.CENTER_HORIZONTAL);

        websiteView = createFooterLink(context, R.drawable.msg_language, getString(R.string.CGP_About_Website),
                () -> Browser.openInTelegramBrowser(getContext(), Constants.CG_WEB_SITE_URL, null), Constants.CG_WEB_SITE_URL);
        footerLinks.addView(websiteView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 36));

        policyView = createFooterLink(context, R.drawable.msg_policy_solar, getString(R.string.PrivacyPolicy),
                () -> Browser.openInTelegramBrowser(getContext(), Constants.CG_PRIVACY_URL, null), Constants.CG_PRIVACY_URL);
        footerLinks.addView(policyView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 36, 8, 0, 0, 0));

        footerView.addView(footerLinks, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 16, 14, 16, 0));

        donateView = createFooterLink(context, R.drawable.msg2_reactions2, getString(R.string.DP_Support),
                () -> CherrygramPreferencesNavigator.INSTANCE.createDonate(this, true), null);
        if (getConnectionsManager().isTestBackend()) {
            donateView.setVisibility(View.GONE);
        }
        footerView.addView(donateView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 36, Gravity.CENTER_HORIZONTAL, 16, 8, 16, 0));

        updateCommunity(false);
        getChatWithFallback(Constants.Cherrygram_Support, Constants.CG_CHAT_USERNAME,
                cgCommunity -> {
                    if (getParentActivity() == null || headerView == null) return;

                    cherrygramSupportChatLoaded = true;
                    cherrygramSupportChat = cgCommunity;
                    updateCommunity(true);
                }
        );

        return view;
    }

    private TextView createFooterLink(Context context, int icon, CharSequence text, Runnable onClick, String copyText) {
        TextView link = new TextView(context);
        link.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        link.setTypeface(AndroidUtilities.bold());
        link.setGravity(Gravity.CENTER_VERTICAL);
        link.setSingleLine(true);
        link.setPadding(dp(12), 0, dp(14), 0);
        link.setCompoundDrawablePadding(dp(6));
        link.setBackground(new LayerDrawable(new Drawable[]{
                Theme.createRoundRectDrawable(dp(18), ColorUtils.setAlphaComponent(getThemedColor(Theme.key_windowBackgroundWhiteBlackText), 0x14)),
                Theme.createRadSelectorDrawable(getThemedColor(Theme.key_listSelector), 18, 18)
        }));
        Drawable drawable = ContextCompat.getDrawable(context, icon);
        if (drawable != null) {
            drawable = drawable.mutate();
            drawable.setBounds(0, 0, dp(18), dp(18));
        }
        link.setCompoundDrawables(drawable, null, null, null);
        setFooterLinkColor(link, headerView.getAccentTextColor());
        link.setText(text);
        link.setOnClickListener(v -> onClick.run());
        if (copyText != null) {
            link.setOnLongClickListener(v -> {
                AndroidUtilities.addToClipboard(copyText);
                return true;
            });
        }
        ScaleStateListAnimator.apply(link, .02f, 1.2f);
        return link;
    }

    private void setFooterLinkColor(TextView link, int color) {
        link.setTextColor(color);
        Drawable drawable = link.getCompoundDrawables()[0];
        if (drawable != null) {
            drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (headerView != null) {
            headerView.update();
            setFooterLinkColor(websiteView, headerView.getAccentTextColor());
            setFooterLinkColor(policyView, headerView.getAccentTextColor());
            setFooterLinkColor(donateView, headerView.getAccentTextColor());
        }
    }

    private void updateCommunity(boolean animated) {
        TLRPC.Chat linkedCommunity = null;
        if (cherrygramSupportChatLoaded && cherrygramSupportChat != null && cherrygramSupportChat.linked_community_id != 0) {
            linkedCommunity = getMessagesController().getChat(cherrygramSupportChat.linked_community_id);
        }

        ArrayList<AboutHeaderView.Link> chats = new ArrayList<>();
        CommunityLinkView communityLinkView = null;
        if (linkedCommunity != null && getParentActivity() != null) {
            communityLinkView = new CommunityLinkView(getParentActivity(), getResourceProvider());
            communityLinkView.setChat(currentAccount, linkedCommunity);
            long communityId = cherrygramSupportChat.linked_community_id;
            communityLinkView.setOnClickListener(v -> showDialog(new CommunitySheet(this, communityId)));
            communityGroupItem = communityItem;
        } else {
            chats.add(new AboutHeaderView.Link(channelRow, R.drawable.msg_channel_solar, getString(R.string.CGP_ToChannel), "@" + Constants.CG_CHANNEL_USERNAME));
            chats.add(new AboutHeaderView.Link(betaChannelRow, R.drawable.msg_channel_solar, getString(R.string.CGP_ToBetaChannel), "@" + Constants.CG_BETA_APKS_CHANNEL_USERNAME));
            chats.add(new AboutHeaderView.Link(chatRow, R.drawable.msg_discuss_solar, getString(R.string.CGP_ToChat), "@" + Constants.CG_CHAT_USERNAME));
            chats.add(new AboutHeaderView.Link(offtopicChatRow, R.drawable.msg_discuss_solar, getString(R.string.CGP_ToOfftopicChat), "@" + Constants.CG_OFFTOPIC_CHAT_USERNAME));
            communityGroupItem = chatsItem;
        }
        communityGroup = headerView.createGroup(getString(R.string.CGP_About_Community), communityLinkView, chats, 4);
        updateRows(animated);
    }

    public static String getLastCheckUpdateTime() {
        return getString(R.string.UP_LastCheck) + ": " + LocaleController.formatDateTime(SharedConfig.lastUpdateCheckTime / 1000, true);
    }

    protected void getChatWithFallback(
            long chatID,
            String username,
            Utilities.Callback<TLRPC.Chat> callback
    ) {
        TLRPC.Chat chat = getMessagesController().getChat(-chatID);

        if (chat != null) {
            callback.run(chat);
            return;
        }

        TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
        req.username = username;

        getConnectionsManager().sendRequest(req, (res, err) -> {
            if (err != null || !(res instanceof TLRPC.TL_contacts_resolvedPeer resolvedPeer)) {
                AndroidUtilities.runOnUIThread(() -> callback.run(null));
                return;
            }

            if (resolvedPeer.chats == null || resolvedPeer.chats.isEmpty()) {
                AndroidUtilities.runOnUIThread(() -> callback.run(null));
                return;
            }

            getMessagesController().putUsers(resolvedPeer.users, false);
            getMessagesController().putChats(resolvedPeer.chats, false);
            getMessagesStorage().putUsersAndChats(resolvedPeer.users, resolvedPeer.chats, false, true);

            TLRPC.Chat loadedChat = resolvedPeer.chats.get(0);
            AndroidUtilities.runOnUIThread(() -> callback.run(loadedChat));
        });
    }

}