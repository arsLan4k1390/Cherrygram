/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.chats;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.text.TextUtils;

import org.telegram.messenger.ChatObject;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.ChatActivityEnterView;
import org.telegram.ui.Components.ChatAttachAlert;

import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig;
import uz.unnarsx.cherrygram.chats.helpers.ChatsHelper2;
import uz.unnarsx.cherrygram.chats.helpers.ChatsPasswordHelper;
import uz.unnarsx.cherrygram.core.configs.CherrygramPrivacyConfig;

// I've created this so CG features can be injected in a source file with 1 line only (maybe)
// Because manual editing of drklo's sources harms your mental health.
public class CherrygramChatMenuInjector {

    public static void injectAttachItem(ActionBarMenuItem headerItem, ActionBarMenu.LazyItem attachItem, ChatActivityEnterView chatActivityEnterView, ChatAttachAlert chatAttachAlert, Context context, Theme.ResourcesProvider resourcesProvider) {
        if (chatActivityEnterView != null && chatActivityEnterView.hasText() && TextUtils.isEmpty(chatActivityEnterView.getSlowModeTimer())) {
            ActionBarMenuSubItem attach = new ActionBarMenuSubItem(context, false, true, true, resourcesProvider);
            attach.setTextAndIcon(getString(R.string.AttachMenu), R.drawable.input_attach);
            attach.setOnClickListener(view -> {
                headerItem.closeSubMenu();
                if (chatAttachAlert != null) {
                    chatAttachAlert.setEditingMessageObject(0, null);
                }
                chatActivityEnterView.getAttachButton().performClick();
            });
            headerItem.setOnClickListener(v-> headerItem.toggleSubMenu(attach, attachItem.createView()));
        } else {
            headerItem.setOnClickListener(v-> headerItem.toggleSubMenu(null, null));
        }
    }

    public static void injectCallShortcuts(ActionBarMenuItem headerItem, TLRPC.UserFull userFull) {
        if (userFull != null && userFull.phone_calls_available) {
            headerItem.lazilyAddSubItem(ChatActivity.call, R.drawable.msg_callback, getString(R.string.Call));
            if (userFull.video_calls_available) {
                headerItem.lazilyAddSubItem(ChatActivity.video_call, R.drawable.msg_videocall, getString(R.string.VideoCall));
            }
        }
    }

    public static void injectCherrygramShortcuts(ActionBarMenuItem headerItem, TLRPC.Chat currentChat, TLRPC.User currentUser) {
        boolean isAnyButtonEnabled = CherrygramPrivacyConfig.INSTANCE.getAskBiometricsToOpenChat() || CherrygramChatsConfig.INSTANCE.getShortcut_JumpToBegin()
                || CherrygramChatsConfig.INSTANCE.getShortcut_DeleteAll() || CherrygramChatsConfig.INSTANCE.getShortcut_SavedMessages()
                || CherrygramChatsConfig.INSTANCE.getShortcut_Blur() || CherrygramChatsConfig.INSTANCE.getShortcut_Browser();

        if (isAnyButtonEnabled) headerItem.lazilyAddColoredGap();

        if (CherrygramPrivacyConfig.INSTANCE.getAskBiometricsToOpenChat() && ChatsPasswordHelper.INSTANCE.getShouldRequireBiometricsToOpenChats()) {
            if (
                    currentUser != null && currentUser.id != 0 && ChatsPasswordHelper.INSTANCE.getArrayList(ChatsPasswordHelper.Passcode_Array).contains(String.valueOf(currentUser.id))
                    || currentChat != null && currentChat.id != 0 && ChatsPasswordHelper.INSTANCE.getArrayList(ChatsPasswordHelper.Passcode_Array).contains(String.valueOf(-currentChat.id))
            ) {
                headerItem.lazilyAddSubItem(ChatActivity.OPTION_DO_NOT_ASK_PASSCODE, R.drawable.msg_secret, getString(R.string.SP_DoNotAskPin));
            } else {
                headerItem.lazilyAddSubItem(ChatActivity.OPTION_ASK_PASSCODE, R.drawable.msg_secret, getString(R.string.SP_AskPin));
            }
        }

        if (CherrygramChatsConfig.INSTANCE.getShortcut_JumpToBegin())
            headerItem.lazilyAddSubItem(ChatActivity.OPTION_JUMP_TO_BEGINNING, R.drawable.ic_upward, getString(R.string.CG_JumpToBeginning));

        if ((ChatObject.isMegagroup(currentChat) || currentChat != null && !ChatObject.isChannel(currentChat)) && !ChatsHelper2.INSTANCE.isDeleteAllHidden(currentChat)) {
            if (CherrygramChatsConfig.INSTANCE.getShortcut_DeleteAll())
                headerItem.lazilyAddSubItem(ChatActivity.OPTION_DELETE_ALL_FROM_SELF, R.drawable.msg_delete, getString(R.string.CG_DeleteAllFromSelf));
        }

        if (currentChat != null && !ChatObject.isChannel(currentChat) && currentChat.creator) {
            headerItem.lazilyAddSubItem(ChatActivity.OPTION_UPGRADE_GROUP, R.drawable.ic_upward, getString(R.string.UpgradeGroup));
        }

        if (currentChat != null && currentChat.id != Math.abs(ChatsHelper2.getCustomChatID())
                || currentUser != null && currentUser.id != Math.abs(ChatsHelper2.getCustomChatID())
        ) {
            if (CherrygramChatsConfig.INSTANCE.getShortcut_SavedMessages())
                headerItem.lazilyAddSubItem(ChatActivity.OPTION_GO_TO_SAVED, R.drawable.msg_saved, getString(R.string.SavedMessages));
        }

        if (CherrygramChatsConfig.INSTANCE.getShortcut_Blur())
            headerItem.lazilyAddSubItem(ChatActivity.OPTION_BLUR_SETTINGS, R.drawable.msg_theme, getString(R.string.BlurInChat));

        if (CherrygramChatsConfig.INSTANCE.getShortcut_Browser() )
            headerItem.lazilyAddSubItem(ChatActivity.OPTION_OPEN_TELEGRAM_BROWSER, R.drawable.msg_language, "Telegram Browser");

    }

    public static void injectAdminShortcuts(ActionBarMenuItem headerItem, TLRPC.Chat currentChat) {

        boolean isAnyButtonEnabled = CherrygramChatsConfig.INSTANCE.getAdmins_Reactions() || CherrygramChatsConfig.INSTANCE.getAdmins_Permissions() || CherrygramChatsConfig.INSTANCE.getAdmins_Administrators()
                || CherrygramChatsConfig.INSTANCE.getAdmins_Members() || CherrygramChatsConfig.INSTANCE.getAdmins_Statistics() || CherrygramChatsConfig.INSTANCE.getAdmins_RecentActions();

        if (isAnyButtonEnabled) headerItem.lazilyAddColoredGap();

        if (CherrygramChatsConfig.INSTANCE.getAdmins_Reactions() && ChatObject.canChangeChatInfo(currentChat)) {
            headerItem.lazilyAddSubItem(ChatActivity.OPTION_FOR_ADMINS_REACTIONS, R.drawable.msg_reactions2, getString(R.string.Reactions));
        }

        if (CherrygramChatsConfig.INSTANCE.getAdmins_Permissions() && !(ChatObject.isChannel(currentChat) && !currentChat.megagroup) && !currentChat.gigagroup) {
            headerItem.lazilyAddSubItem(ChatActivity.OPTION_FOR_ADMINS_PERMISSIONS, R.drawable.msg_permissions, getString(R.string.ChannelPermissions));
        }

        if (CherrygramChatsConfig.INSTANCE.getAdmins_Administrators()) {
            headerItem.lazilyAddSubItem(ChatActivity.OPTION_FOR_ADMINS_ADMINISTRATORS, R.drawable.msg_admins, getString(R.string.ChannelAdministrators));
        }

        if (CherrygramChatsConfig.INSTANCE.getAdmins_Members()) {
            headerItem.lazilyAddSubItem(ChatActivity.OPTION_FOR_ADMINS_MEMBERS, R.drawable.msg_groups, getString(R.string.ChannelMembers));
        }

        if (CherrygramChatsConfig.INSTANCE.getAdmins_Permissions() && (ChatObject.isChannel(currentChat) && !currentChat.megagroup || currentChat.gigagroup)) {
            headerItem.lazilyAddSubItem(ChatActivity.OPTION_FOR_ADMINS_PERMISSIONS, R.drawable.msg_user_remove, getString(R.string.ChannelBlacklist));
        }

        if (CherrygramChatsConfig.INSTANCE.getAdmins_Statistics() && ChatObject.isBoostSupported(currentChat)) {
            headerItem.lazilyAddSubItem(ChatActivity.OPTION_FOR_ADMINS_STATISTICS, R.drawable.msg_stats, getString(R.string.StatisticsAndBoosts));
        }

        if (CherrygramChatsConfig.INSTANCE.getAdmins_RecentActions()) {
            headerItem.lazilyAddSubItem(ChatActivity.OPTION_FOR_ADMINS_RECENT_ACTIONS, R.drawable.msg_log, getString(R.string.EventLog));
        }
    }

}
