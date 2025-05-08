/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.chats

import android.content.Context
import android.text.TextUtils
import org.telegram.messenger.ChatObject
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.R
import org.telegram.tgnet.TLRPC
import org.telegram.ui.ActionBar.ActionBarMenu
import org.telegram.ui.ActionBar.ActionBarMenuItem
import org.telegram.ui.ActionBar.ActionBarMenuSubItem
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.ChatActivity
import org.telegram.ui.Components.ChatActivityEnterView
import org.telegram.ui.Components.ChatAttachAlert
import uz.unnarsx.cherrygram.chats.helpers.ChatsHelper2.getCustomChatID
import uz.unnarsx.cherrygram.chats.helpers.ChatsPasswordHelper
import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramPrivacyConfig
import uz.unnarsx.cherrygram.misc.Constants
import kotlin.math.abs

// I've created this so CG features can be injected in a source file with 1 line only (maybe)
// Because manual editing of drklo's sources harms your mental health.
object CGChatMenuInjector {

    fun injectAttachItem(
        headerItem: ActionBarMenuItem?,
        attachItem: ActionBarMenu.LazyItem?,
        chatActivityEnterView: ChatActivityEnterView?,
        chatAttachAlert: ChatAttachAlert?,
        context: Context,
        resourcesProvider: Theme.ResourcesProvider
    ) {
        if (headerItem == null) return
        if (chatActivityEnterView != null && chatActivityEnterView.hasText() && TextUtils.isEmpty(chatActivityEnterView.slowModeTimer)) {
            val attach = ActionBarMenuSubItem(context, false, true, true, resourcesProvider)
            attach.setTextAndIcon(getString(R.string.AttachMenu), R.drawable.input_attach)
            attach.setOnClickListener {
                headerItem.closeSubMenu()
                chatAttachAlert?.setEditingMessageObject(0, null)
                chatActivityEnterView.attachButton.performClick()
            }
            headerItem.setOnClickListener {
                headerItem.toggleSubMenu(attach, attachItem?.createView())
            }
        } else {
            headerItem.setOnClickListener {
                headerItem.toggleSubMenu(null, null)
            }
        }
    }

    fun injectCallShortcuts(headerItem: ActionBarMenuItem, userFull: TLRPC.UserFull?) {
        if (userFull != null && userFull.phone_calls_available) {
            headerItem.lazilyAddSubItem(
                ChatActivity.call,
                R.drawable.msg_callback,
                getString(R.string.Call)
            )
            if (userFull.video_calls_available) headerItem.lazilyAddSubItem(
                    ChatActivity.video_call,
                    R.drawable.msg_videocall,
                    getString(R.string.VideoCall)
            )
        }
    }

    fun injectCherrygramShortcuts(
        headerItem: ActionBarMenuItem,
        currentChat: TLRPC.Chat?,
        currentUser: TLRPC.User?
    ) {
        val isAnyButtonEnabled = CherrygramPrivacyConfig.askBiometricsToOpenChat || CherrygramChatsConfig.shortcut_JumpToBegin
                    || CherrygramChatsConfig.shortcut_DeleteAll || CherrygramChatsConfig.shortcut_SavedMessages
                    || CherrygramChatsConfig.shortcut_Blur || CherrygramChatsConfig.shortcut_Browser

        if (isAnyButtonEnabled) headerItem.lazilyAddColoredGap()

        if (ChatsPasswordHelper.shouldRequireBiometricsToOpenChats()) {
            if (
                currentUser != null && currentUser.id != 0L && ChatsPasswordHelper.isChatLocked(currentUser.id)
                || currentChat != null && currentChat.id != 0L && ChatsPasswordHelper.isChatLocked(currentChat.id)
            ) {
                headerItem.lazilyAddSubItem(
                    ChatActivity.OPTION_DO_NOT_ASK_PASSCODE,
                    R.drawable.msg_secret,
                    getString(R.string.SP_DoNotAskPin)
                )
            } else {
                headerItem.lazilyAddSubItem(
                    ChatActivity.OPTION_ASK_PASSCODE,
                    R.drawable.msg_secret,
                    getString(R.string.SP_AskPin)
                )
            }
        }

        if (CherrygramChatsConfig.shortcut_JumpToBegin) headerItem.lazilyAddSubItem(
            ChatActivity.OPTION_JUMP_TO_BEGINNING,
            R.drawable.ic_upward,
            getString(R.string.CG_JumpToBeginning)
        )

        if (currentChat != null && !isDeleteAllHidden(currentChat) && (ChatObject.isMegagroup(currentChat) || !ChatObject.isChannel(currentChat))) {
            if (CherrygramChatsConfig.shortcut_DeleteAll) headerItem.lazilyAddSubItem(
                ChatActivity.OPTION_DELETE_ALL_FROM_SELF,
                R.drawable.msg_delete,
                getString(R.string.CG_DeleteAllFromSelf)
            )
        }

        if (currentChat != null && !ChatObject.isChannel(currentChat) && currentChat.creator) headerItem.lazilyAddSubItem(
                ChatActivity.OPTION_UPGRADE_GROUP,
                R.drawable.ic_upward,
                getString(R.string.UpgradeGroup)
        )

        if (currentChat != null && currentChat.id != abs(getCustomChatID())
            || currentUser != null && currentUser.id != abs(getCustomChatID())
        ) {
            if (CherrygramChatsConfig.shortcut_SavedMessages) headerItem.lazilyAddSubItem(
                ChatActivity.OPTION_GO_TO_SAVED,
                R.drawable.msg_saved,
                getString(R.string.SavedMessages)
            )
        }

        if (CherrygramChatsConfig.shortcut_Blur) headerItem.lazilyAddSubItem(
            ChatActivity.OPTION_BLUR_SETTINGS,
            R.drawable.msg_theme,
            getString(R.string.BlurInChat)
        )

        if (CherrygramChatsConfig.shortcut_Browser) headerItem.lazilyAddSubItem(
            ChatActivity.OPTION_OPEN_TELEGRAM_BROWSER,
            R.drawable.msg_language,
            "Telegram Browser"
        )

    }

    fun injectAdminShortcuts(headerItem: ActionBarMenuItem, currentChat: TLRPC.Chat) {
        val isAnyButtonEnabled = CherrygramChatsConfig.admins_Reactions || CherrygramChatsConfig.admins_Permissions || CherrygramChatsConfig.admins_Administrators
                || CherrygramChatsConfig.admins_Members || CherrygramChatsConfig.admins_Statistics || CherrygramChatsConfig.admins_RecentActions

        if (isAnyButtonEnabled) headerItem.lazilyAddColoredGap()

        if (CherrygramChatsConfig.admins_Reactions && ChatObject.canChangeChatInfo(currentChat)) headerItem.lazilyAddSubItem(
                ChatActivity.OPTION_FOR_ADMINS_REACTIONS,
                R.drawable.msg_reactions2,
                getString(R.string.Reactions)
        )

        if (CherrygramChatsConfig.admins_Permissions && !(ChatObject.isChannel(currentChat) && !currentChat.megagroup) && !currentChat.gigagroup) headerItem.lazilyAddSubItem(
                ChatActivity.OPTION_FOR_ADMINS_PERMISSIONS,
                R.drawable.msg_permissions,
                getString(R.string.ChannelPermissions)
        )

        if (CherrygramChatsConfig.admins_Administrators) headerItem.lazilyAddSubItem(
                ChatActivity.OPTION_FOR_ADMINS_ADMINISTRATORS,
                R.drawable.msg_admins,
                getString(R.string.ChannelAdministrators)
        )

        if (CherrygramChatsConfig.admins_Members) headerItem.lazilyAddSubItem(
                ChatActivity.OPTION_FOR_ADMINS_MEMBERS,
                R.drawable.msg_groups,
                getString(R.string.ChannelMembers)
        )

        if (CherrygramChatsConfig.admins_Permissions && (ChatObject.isChannel(currentChat) && !currentChat.megagroup || currentChat.gigagroup)) headerItem.lazilyAddSubItem(
                ChatActivity.OPTION_FOR_ADMINS_PERMISSIONS,
                R.drawable.msg_user_remove,
                getString(R.string.ChannelBlacklist)
        )

        if (CherrygramChatsConfig.admins_Statistics && ChatObject.isBoostSupported(currentChat)) headerItem.lazilyAddSubItem(
                ChatActivity.OPTION_FOR_ADMINS_STATISTICS,
                R.drawable.msg_stats,
                getString(R.string.StatisticsAndBoosts)
        )

        if (CherrygramChatsConfig.admins_RecentActions) headerItem.lazilyAddSubItem(
                ChatActivity.OPTION_FOR_ADMINS_RECENT_ACTIONS,
                R.drawable.msg_log,
                getString(R.string.EventLog)
        )

    }

    private fun isDeleteAllHidden(chat: TLRPC.Chat): Boolean {
        return Constants.Cherrygram_Support == chat.id
    }

}