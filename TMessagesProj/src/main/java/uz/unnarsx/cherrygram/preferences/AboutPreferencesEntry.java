/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.preferences;

import static org.telegram.messenger.LocaleController.getString;

import android.os.Bundle;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.IconBackgroundColors;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.SettingsActivity;
import org.telegram.ui.community.CommunitySheet;
import org.telegram.ui.community.cells.CommunityLinkView;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.core.firebase.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper;
import uz.unnarsx.cherrygram.core.helpers.DeeplinkHelper;
import uz.unnarsx.cherrygram.misc.Constants;
import uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper;

public class AboutPreferencesEntry extends BaseCGPreferencesEntry {

    private final int readmeRow = 1;
    private final int updatesRow = 2;
    private final int debugPrefsRow = 3;

    private final int channelRow = 4;
    private final int betaChannelRow = 5;
    private final int chatRow = 6;
    private final int offtopicChatRow = 7;
    private final int communityRow = 8;
    private final int githubRow = 9;
    private final int crowdinRow = 10;
    private final int policyRow = 11;

    private TLRPC.Chat cherrygramSupportChat;
    private boolean cherrygramSupportChatLoaded;

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
        items.add(UItem.asHeader(getString(R.string.Info)));
        items.add(
                SettingsHelper.asTextDetail(
                        readmeRow,
                        0,
                        CGResourcesHelper.getAppName() + " " + CGResourcesHelper.getCherryVersion() + " | " + "Telegram " + BuildVars.BUILD_VERSION_STRING,
                        getString(R.string.CGP_About_Desc)
                )
        );
        items.add(
                SettingsActivity.SettingCell.Factory.of(
                        updatesRow,
                        IconBackgroundColors.GREEN.top, IconBackgroundColors.GREEN.bottom,
                        R.drawable.settings_refresh_filled_solar,
                        getString(R.string.UP_Category_Updates),
                        getLastCheckUpdateTime()
                )
                .slug(DeeplinkHelper.DeepLinksRepo.CG_Updater_Bottom_Sheet)
        );
        items.add(
                SettingsActivity.SettingCell.Factory.of(
                        debugPrefsRow,
                        IconBackgroundColors.PURPLE.top, IconBackgroundColors.PURPLE.bottom,
                        R.drawable.settings_tube_filled_solar,
                        "Debug // W.I.P",
                        getString(R.string.CGP_Experimental_Desc),
                        true
                )
                .slug(DeeplinkHelper.DeepLinksRepo.CG_Debug)
        );
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.CGP_Links)));
        items.add(UItem.asButton(channelRow, R.drawable.msg_channel_solar, getString(R.string.CGP_ToChannel)));
        items.add(UItem.asButton(betaChannelRow, R.drawable.msg_channel_solar, getString(R.string.CGP_ToBetaChannel)));
        if (!cherrygramSupportChatLoaded) {
            items.add(UItem.asShadow(null));
            items.add(UItem.asButton(chatRow, R.drawable.msg_discuss_solar, getString(R.string.CGP_ToChat)));
            items.add(UItem.asButton(offtopicChatRow, R.drawable.msg_discuss_solar, getString(R.string.CGP_ToOfftopicChat)));
            items.add(UItem.asShadow(null));

            getChatWithFallback(Constants.Cherrygram_Support, Constants.CG_CHAT_USERNAME,
                    cgCommunity -> {
                        cherrygramSupportChatLoaded = true;
                        cherrygramSupportChat = cgCommunity;
                        updateRows(true);
                    }
            );
        } else if (cherrygramSupportChat != null && cherrygramSupportChat.linked_community_id != 0) {
            TLRPC.Chat linkedCommunity = getMessagesController().getChat(cherrygramSupportChat.linked_community_id);

            if (linkedCommunity != null) {
                CommunityLinkView communityLinkView = new CommunityLinkView(getParentActivity(), getResourceProvider());
                communityLinkView.setChat(currentAccount, linkedCommunity);

                items.add(UItem.asShadow(null));
                items.add(UItem.asCustom(communityRow, communityLinkView));
                items.add(UItem.asShadow(null));
            } else {
                items.add(UItem.asShadow(null));

                items.add(UItem.asButton(chatRow, R.drawable.msg_discuss_solar, getString(R.string.CGP_ToChat)));
                items.add(UItem.asButton(offtopicChatRow, R.drawable.msg_discuss_solar, getString(R.string.CGP_ToOfftopicChat)));
                items.add(UItem.asShadow(null));
            }
        } else {
            items.add(UItem.asShadow(null));
            items.add(UItem.asButton(chatRow, R.drawable.msg_discuss_solar, getString(R.string.CGP_ToChat)));
            items.add(UItem.asButton(offtopicChatRow, R.drawable.msg_discuss_solar, getString(R.string.CGP_ToOfftopicChat)));
            items.add(UItem.asShadow(null));
        }

        if (!CherrygramCoreConfig.isStandalonePremiumBuild()) {
            String value;
            if (CherrygramCoreConfig.isStandaloneBetaBuild() || CherrygramCoreConfig.isDevBuild()) {
                value = "GitHub";
            } else {
                value = "commit " + BuildConfig.GIT_COMMIT_HASH.substring(0, 8);
            }
            items.add(UItem.asButton(githubRow, R.drawable.github_cat, getString(R.string.CGP_Source), value)
                    .slug("source_code")
            );
        }

        items.add(UItem.asButton(crowdinRow, R.drawable.msg_translate_solar, getString(R.string.CGP_Crowdin), "Crowdin")
                .slug("crowdin")
        );
        items.add(UItem.asButton(policyRow, R.drawable.msg_policy_solar, getString(R.string.PrivacyPolicy))
                .slug("policy")
        );
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == readmeRow) {
            Browser.openUrl(getContext(), Constants.CG_GITHUB_URL + "#readme");
        } else if (item.id == updatesRow) {
            if (LaunchActivity.instance == null) return;
            LaunchActivity.instance.showUpdaterBottomSheet(this, false, null);
        } else if (item.id == debugPrefsRow) {
            CherrygramPreferencesNavigator.INSTANCE.createDebug(this);
        } else if (item.id == channelRow) {
            getMessagesController().openByUserName(Constants.CG_CHANNEL_USERNAME, this, 1);
        } else if (item.id == betaChannelRow) {
            getMessagesController().openByUserName(Constants.CG_BETA_APKS_CHANNEL_USERNAME, this, 1);
        } else if (item.id == chatRow) {
            getMessagesController().openByUserName(Constants.CG_CHAT_USERNAME, this, 1);
        } else if (item.id == offtopicChatRow) {
            getMessagesController().openByUserName(Constants.CG_OFFTOPIC_CHAT_USERNAME, this, 1);
        } else if (item.id == communityRow) {
            TLRPC.Chat cgCommunity = getMessagesController().getChat(Constants.Cherrygram_Support);
            if (cgCommunity != null) {
                showDialog(new CommunitySheet(this, cgCommunity.linked_community_id));
            }
        } else if (item.id == githubRow) {
            if (CherrygramCoreConfig.isStandaloneBetaBuild() || CherrygramCoreConfig.isDevBuild()) {
                Browser.openUrl(getContext(), Constants.CG_GITHUB_URL);
            } else {
                Browser.openUrl(getContext(), Constants.CG_GITHUB_URL + "/commit/" + BuildConfig.GIT_COMMIT_HASH);
            }
        } else if (item.id == crowdinRow) {
            Browser.openUrl(getContext(), Constants.CG_CROWDIN_URL);
        } else if (item.id == policyRow) {
            Browser.openUrl(getContext(), Constants.CG_PRIVACY_URL);
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        /*if (item.id == readmeRow) {
            AndroidUtilities.addToClipboard(Constants.CG_GITHUB_URL + "#readme");
            return true;
        } else if (item.id == updatesRow) {
            AndroidUtilities.addToClipboard("tg://" + DeeplinkHelper.DeepLinksRepo.CG_Updater_Bottom_Sheet);
            return true;
        } else if (item.id == debugPrefsRow) {
            AndroidUtilities.addToClipboard("tg://" + DeeplinkHelper.DeepLinksRepo.CG_Debug);
            return true;
        } else*/ if (item.id == channelRow) {
            AndroidUtilities.addToClipboard("@" + Constants.CG_CHANNEL_USERNAME);
            return true;
        } else if (item.id == betaChannelRow) {
            AndroidUtilities.addToClipboard("@" + Constants.CG_BETA_APKS_CHANNEL_USERNAME);
            return true;
        } else if (item.id == chatRow) {
            AndroidUtilities.addToClipboard("@" + Constants.CG_CHAT_USERNAME);
            return true;
        } else if (item.id == offtopicChatRow) {
            AndroidUtilities.addToClipboard("@" + Constants.CG_OFFTOPIC_CHAT_USERNAME);
            return true;
        } else if (item.id == crowdinRow) {
            AndroidUtilities.addToClipboard(Constants.CG_CROWDIN_URL);
            return true;
        } else if (item.id == policyRow) {
            AndroidUtilities.addToClipboard(Constants.CG_PRIVACY_URL);
            return true;
        }
        return false;
    }

    public static String getLastCheckUpdateTime() {
        return getString(R.string.UP_LastCheck) + ": " + LocaleController.formatDateTime(SharedConfig.lastUpdateCheckTime / 1000, true);
    }

    /*private boolean isInChannel(long chatID) {
        TLRPC.Chat chat = getMessagesController().getChat(chatID);
        return chat != null && ChatObject.isInChat(chat);
    }

    private void openChannel(long chatID) {
        if (isInChannel(chatID)) {
            Bundle args = new Bundle();
            args.putLong("chat_id", chatID);
            presentFragment(new ChatActivity(args));
        }
    }*/

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
