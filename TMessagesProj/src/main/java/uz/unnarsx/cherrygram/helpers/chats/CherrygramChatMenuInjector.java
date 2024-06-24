package uz.unnarsx.cherrygram.helpers.chats;

import android.content.Context;
import android.text.TextUtils;

import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.ChatActivityEnterView;
import org.telegram.ui.Components.ChatAttachAlert;

import uz.unnarsx.cherrygram.CherrygramConfig;
import uz.unnarsx.cherrygram.extras.CherrygramExtras;

// I've created this so CG features can be injected in a source file with 1 line only (maybe)
// Because manual editing of drklo's sources harms your mental health.
public class CherrygramChatMenuInjector {

    public static void injectAttachItem(ActionBarMenuItem headerItem, ActionBarMenu.LazyItem attachItem, ChatActivityEnterView chatActivityEnterView, ChatAttachAlert chatAttachAlert, Context context, Theme.ResourcesProvider resourcesProvider) {
        if (chatActivityEnterView != null && chatActivityEnterView.hasText() && TextUtils.isEmpty(chatActivityEnterView.getSlowModeTimer())) {
            ActionBarMenuSubItem attach = new ActionBarMenuSubItem(context, false, true, true, resourcesProvider);
            attach.setTextAndIcon(LocaleController.getString(R.string.AttachMenu), R.drawable.input_attach);
            attach.setOnClickListener(view -> {
                headerItem.closeSubMenu();
                if (chatAttachAlert != null) {
                    chatAttachAlert.setEditingMessageObject(null);
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
            headerItem.lazilyAddSubItem(ChatActivity.call, R.drawable.msg_callback, LocaleController.getString("Call", R.string.Call));
            if (userFull.video_calls_available) {
                headerItem.lazilyAddSubItem(ChatActivity.video_call, R.drawable.msg_videocall, LocaleController.getString("VideoCall", R.string.VideoCall));
            }
        }
    }

    public static void injectCherrygramShortcuts(ActionBarMenuItem headerItem, TLRPC.Chat currentChat, TLRPC.User currentUser) {
        boolean isAnyButtonEnabled = CherrygramConfig.INSTANCE.getShortcut_JumpToBegin() || CherrygramConfig.INSTANCE.getShortcut_DeleteAll()
                || CherrygramConfig.INSTANCE.getShortcut_SavedMessages() || CherrygramConfig.INSTANCE.getShortcut_Blur();

        if (isAnyButtonEnabled) headerItem.lazilyAddColoredGap();

        if (CherrygramConfig.INSTANCE.getShortcut_JumpToBegin())
            headerItem.lazilyAddSubItem(ChatActivity.OPTION_JUMP_TO_BEGINNING, R.drawable.ic_upward, LocaleController.getString("CG_JumpToBeginning", R.string.CG_JumpToBeginning));

        if ((ChatObject.isMegagroup(currentChat) || currentChat != null && !ChatObject.isChannel(currentChat)) && !CherrygramConfig.INSTANCE.isDeleteAllHidden(currentChat)) {
            if (CherrygramConfig.INSTANCE.getShortcut_DeleteAll())
                headerItem.lazilyAddSubItem(ChatActivity.OPTION_DELETE_ALL_FROM_SELF, R.drawable.msg_delete, LocaleController.getString("CG_DeleteAllFromSelf", R.string.CG_DeleteAllFromSelf));
        }

        if (currentChat != null && !ChatObject.isChannel(currentChat) && currentChat.creator) {
            headerItem.lazilyAddSubItem(ChatActivity.OPTION_UPGRADE_GROUP, R.drawable.ic_upward, LocaleController.getString("UpgradeGroup", R.string.UpgradeGroup));
        }

        if (currentChat != null && currentChat.id != Math.abs(CherrygramExtras.getCustomChatID())
                || currentUser != null && currentUser.id != Math.abs(CherrygramExtras.getCustomChatID())
        ) {
            if (CherrygramConfig.INSTANCE.getShortcut_SavedMessages())
                headerItem.lazilyAddSubItem(ChatActivity.OPTION_GO_TO_SAVED, R.drawable.msg_saved, LocaleController.getString("SavedMessages", R.string.SavedMessages));
        }

        if (CherrygramConfig.INSTANCE.getShortcut_Blur())
            headerItem.lazilyAddSubItem(ChatActivity.OPTION_BLUR_SETTINGS, R.drawable.msg_theme, LocaleController.getString("BlurInChat", R.string.BlurInChat));
    }

    public static void injectAdminShortcuts(ActionBarMenuItem headerItem, TLRPC.Chat currentChat) {

        boolean isAnyButtonEnabled = CherrygramConfig.INSTANCE.getAdmins_Reactions() || CherrygramConfig.INSTANCE.getAdmins_Permissions() || CherrygramConfig.INSTANCE.getAdmins_Administrators()
                || CherrygramConfig.INSTANCE.getAdmins_Members() || CherrygramConfig.INSTANCE.getAdmins_Statistics() || CherrygramConfig.INSTANCE.getAdmins_RecentActions();

        if (isAnyButtonEnabled) headerItem.lazilyAddColoredGap();

        if (CherrygramConfig.INSTANCE.getAdmins_Reactions() && ChatObject.canChangeChatInfo(currentChat)) {
            headerItem.lazilyAddSubItem(ChatActivity.OPTION_FOR_ADMINS_REACTIONS, R.drawable.msg_reactions2, LocaleController.getString("Reactions", R.string.Reactions));
        }

        if (CherrygramConfig.INSTANCE.getAdmins_Permissions() && !(ChatObject.isChannel(currentChat) && !currentChat.megagroup) && !currentChat.gigagroup) {
            headerItem.lazilyAddSubItem(ChatActivity.OPTION_FOR_ADMINS_PERMISSIONS, R.drawable.msg_permissions, LocaleController.getString("ChannelPermissions", R.string.ChannelPermissions));
        }

        if (CherrygramConfig.INSTANCE.getAdmins_Administrators()) {
            headerItem.lazilyAddSubItem(ChatActivity.OPTION_FOR_ADMINS_ADMINISTRATORS, R.drawable.msg_admins, LocaleController.getString("ChannelAdministrators", R.string.ChannelAdministrators));
        }

        if (CherrygramConfig.INSTANCE.getAdmins_Members()) {
            headerItem.lazilyAddSubItem(ChatActivity.OPTION_FOR_ADMINS_MEMBERS, R.drawable.msg_groups, LocaleController.getString("ChannelMembers", R.string.ChannelMembers));
        }

        if (CherrygramConfig.INSTANCE.getAdmins_Permissions() && (ChatObject.isChannel(currentChat) && !currentChat.megagroup || currentChat.gigagroup)) {
            headerItem.lazilyAddSubItem(ChatActivity.OPTION_FOR_ADMINS_PERMISSIONS, R.drawable.msg_user_remove, LocaleController.getString("ChannelBlacklist", R.string.ChannelBlacklist));
        }

        if (CherrygramConfig.INSTANCE.getAdmins_Statistics() && ChatObject.isBoostSupported(currentChat)) {
            headerItem.lazilyAddSubItem(ChatActivity.OPTION_FOR_ADMINS_STATISTICS, R.drawable.msg_stats, LocaleController.getString("StatisticsAndBoosts", R.string.StatisticsAndBoosts));
        }

        if (CherrygramConfig.INSTANCE.getAdmins_RecentActions()) {
            headerItem.lazilyAddSubItem(ChatActivity.OPTION_FOR_ADMINS_RECENT_ACTIONS, R.drawable.msg_log, LocaleController.getString("EventLog", R.string.EventLog));
        }
    }

}
