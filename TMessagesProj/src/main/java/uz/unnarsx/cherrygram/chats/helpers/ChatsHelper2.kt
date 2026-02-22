/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.chats.helpers

import android.os.Build
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ChatObject
import org.telegram.messenger.LocaleController.formatJoined
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.MessageObject
import org.telegram.messenger.MessagesController
import org.telegram.messenger.R
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.TLRPC
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.Cells.ChatMessageCell
import org.telegram.ui.ChatActivity
import org.telegram.ui.ChatRightsEditActivity
import org.telegram.ui.Components.BulletinFactory
import org.telegram.ui.Components.ItemOptions
import org.telegram.ui.Components.ShareAlert
import org.telegram.ui.Components.TranslateAlert2
import org.telegram.ui.Components.UndoView
import uz.unnarsx.cherrygram.chats.JsonBottomSheet
import uz.unnarsx.cherrygram.chats.gemini.GeminiResultsBottomSheet
import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramExperimentalConfig
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper
import androidx.core.view.isVisible
import org.telegram.messenger.AndroidUtilities.dp

object ChatsHelper2 {

    /** Avatar admin actions start */
    fun injectChatActivityAvatarOnClickNew(
        chatActivity: ChatActivity, chatMessageCellDelegate: ChatActivity.ChatMessageCellDelegate, cell: ChatMessageCell, user: TLRPC.User,
        enableMention: Boolean, enableSearchMessages: Boolean
    ) {
        if (chatActivity.context == null) return

        val participants = arrayListOf<TLRPC.ChatParticipant>().apply {
            val chatInfo = chatActivity.currentChatInfo
            val chatParticipants = chatInfo?.participants?.participants
            if (chatParticipants != null) {
                addAll(chatParticipants)
            }
        }

        val participant = participants.find { it.user_id == user.id }
        val isChatParticipant = participant != null

        ItemOptions.makeOptions(chatActivity, cell)
            .add(R.drawable.msg_discussion, getString(R.string.SendMessage)) {
                chatMessageCellDelegate.openDialog(cell, user)
            }
            .addIf(enableMention, R.drawable.msg_mention, getString(R.string.Mention)) {
                chatMessageCellDelegate.appendMention(user)
            }
            .addIf(enableSearchMessages, R.drawable.msg_search, getString(R.string.AvatarPreviewSearchMessages)) {
                chatActivity.openSearchWithUser(user)
            }
            .addIf(ChatObject.canBlockUsers(chatActivity.currentChat) && isChatParticipant, R.drawable.msg_remove, getString(R.string.KickFromGroup)) {
                chatActivity.messagesController.deleteParticipantFromChat(
                    chatActivity.currentChat.id,
                    chatActivity.messagesController.getUser(user.id),
                    chatActivity.currentChatInfo
                )
            }
            .addIf(ChatObject.hasAdminRights(chatActivity.currentChat) && isChatParticipant, R.drawable.msg_permissions, getString(R.string.ChangePermissions)) {
                val action = 1 // Change permissions

                val chatParticipant = chatActivity.currentChatInfo.participants.participants.filter {
                    it.user_id == user.id
                }[0]

                var channelParticipant: TLRPC.ChannelParticipant? = null

                if (ChatObject.isChannel(chatActivity.currentChat)) {
                    channelParticipant = (chatParticipant as TLRPC.TL_chatChannelParticipant).channelParticipant
                } else {
                    chatParticipant is TLRPC.TL_chatParticipantAdmin
                }

                val frag = ChatRightsEditActivity(
                    user.id,
                    chatActivity.currentChatInfo.id,
                    channelParticipant?.admin_rights,
                    chatActivity.currentChat.default_banned_rights,
                    channelParticipant?.banned_rights,
                    channelParticipant?.rank,
                    action,
                    true,
                    false,
                    null
                )
                chatActivity.presentFragment(frag)
            }
            .addIf(ChatObject.canAddAdmins(chatActivity.currentChat) && isChatParticipant, R.drawable.msg_admins, getString(R.string.EditAdminRights)) {
                val action = 0 // Change admin rights

                val chatParticipant = chatActivity.currentChatInfo.participants.participants.filter {
                    it.user_id == user.id
                }[0]

                var channelParticipant: TLRPC.ChannelParticipant? = null

                if (ChatObject.isChannel(chatActivity.currentChat)) {
                    channelParticipant = (chatParticipant as TLRPC.TL_chatChannelParticipant).channelParticipant
                } else {
                    chatParticipant is TLRPC.TL_chatParticipantAdmin
                }

                val frag = ChatRightsEditActivity(
                    user.id,
                    chatActivity.currentChatInfo.id,
                    channelParticipant?.admin_rights,
                    chatActivity.currentChat.default_banned_rights,
                    channelParticipant?.banned_rights,
                    channelParticipant?.rank,
                    action,
                    true,
                    false,
                    null
                )
                chatActivity.presentFragment(frag)
            }
            .addGapIf(participant?.date != null && participant.date != 0)
            .addTextIf(
                participant?.date != null && participant.date != 0,
                CGResourcesHelper.capitalize(formatJoined(participant?.date?.toLong() ?: 0)),
                13
            )
            .addGap()
            .addProfile(user, getString(R.string.ViewProfile)) {
                chatMessageCellDelegate.openProfile(user)
            }

            .setGravity(Gravity.LEFT)
            .forceBottom(true)
            .translate(0f, -AndroidUtilities.dp(48f).toFloat())
            .setDrawScrim(false)
            .setBlur(true)
            .show()
    }

    fun getActiveUsername(userId: Long): String {
        val user: TLRPC.User = MessagesController.getInstance(UserConfig.selectedAccount).getUser(userId)
        var username: String? = null
        var usernames = ArrayList<TLRPC.TL_username?>()
        usernames.addAll(user.usernames)
        if (!TextUtils.isEmpty(user.username)) {
            username = user.username
        }
        usernames = ArrayList(user.usernames)
        if (TextUtils.isEmpty(username)) {
            for (i in usernames.indices) {
                val u: TLRPC.TL_username? = usernames[i]
                if (u != null && u.active && !TextUtils.isEmpty(u.username)) {
                    username = u.username
                    break
                }
            }
        }
        return username ?: ""
    }
    /** Avatar admin actions finish */

    /** Chat search filter start */
    fun getSearchFilterType(): TLRPC.MessagesFilter {
        val filter: TLRPC.MessagesFilter = when (CherrygramChatsConfig.messagesSearchFilter) {
            CherrygramChatsConfig.FILTER_PHOTOS -> {
                TLRPC.TL_inputMessagesFilterPhotos()
            }
            CherrygramChatsConfig.FILTER_VIDEOS -> {
                TLRPC.TL_inputMessagesFilterVideo()
            }
            CherrygramChatsConfig.FILTER_VOICE_MESSAGES -> {
                TLRPC.TL_inputMessagesFilterVoice()
            }
            CherrygramChatsConfig.FILTER_VIDEO_MESSAGES -> {
                TLRPC.TL_inputMessagesFilterRoundVideo()
            }
            CherrygramChatsConfig.FILTER_FILES -> {
                TLRPC.TL_inputMessagesFilterDocument()
            }
            CherrygramChatsConfig.FILTER_MUSIC -> {
                TLRPC.TL_inputMessagesFilterMusic()
            }
            CherrygramChatsConfig.FILTER_GIFS -> {
                TLRPC.TL_inputMessagesFilterGif()
            }
            CherrygramChatsConfig.FILTER_GEO -> {
                TLRPC.TL_inputMessagesFilterGeo()
            }
            CherrygramChatsConfig.FILTER_CONTACTS -> {
                TLRPC.TL_inputMessagesFilterContacts()
            }
            CherrygramChatsConfig.FILTER_MENTIONS -> {
                TLRPC.TL_inputMessagesFilterMyMentions()
            }
            else -> {
                TLRPC.TL_inputMessagesFilterEmpty()
            }
        }
        return filter
    }
    /** Chat search filter finish */

    /** Custom chat id for Saved Messages start */
    fun getCustomChatID(): Long {
        val preferences = MessagesController.getMainSettings(UserConfig.selectedAccount)
        val savedMessagesChatID =
            preferences.getString("CP_CustomChatIDSM", UserConfig.getInstance(UserConfig.selectedAccount).clientUserId.toString())
        val chatID = savedMessagesChatID!!.replace("-100", "-").toLong()

        return if (CherrygramExperimentalConfig.customChatForSavedMessages) chatID
        else UserConfig.getInstance(UserConfig.selectedAccount).clientUserId
    }
    /** Custom chat id for Saved Messages finish */

    /** Direct share menu start */
    fun showForwardMenu(sa: ShareAlert, scrimView: View) {
        ItemOptions.makeOptions(sa.container, sa.resourcesProvider, scrimView)
            .addChecked(
                CherrygramChatsConfig.forwardAuthorship,
                getString(R.string.CG_FwdMenu_Authorship)
            ) {
                CherrygramChatsConfig.forwardAuthorship = !CherrygramChatsConfig.forwardAuthorship
            }
            .addChecked(
                CherrygramChatsConfig.forwardCaptions,
                getString(R.string.CG_FwdMenu_Captions)
            ) {
                CherrygramChatsConfig.forwardCaptions = !CherrygramChatsConfig.forwardCaptions
            }
            .addChecked(
                CherrygramChatsConfig.forwardNotify,
                getString(R.string.CG_FwdMenu_Notify)
            ) {
                CherrygramChatsConfig.forwardNotify = !CherrygramChatsConfig.forwardNotify
            }

            .setDimAlpha(100)
            .translate(-dp(10f).toFloat(), dp(5f).toFloat())
            .show()
    }
    /** Direct share menu finish */

    /** JSON menu start */
    fun showJsonMenu(sa: JsonBottomSheet, field: FrameLayout, messageObject: MessageObject) {
        ItemOptions.makeOptions(sa.container, sa.resourcesProvider, field)
            .addIf(
                messageObject.messageOwner !is TLRPC.TL_messageService,
                R.drawable.msg_info,
                if (sa.isJacksonSupportedAndEnabled) "Switch to GSON" else "Switch to Jackson"
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    CherrygramChatsConfig.jacksonJSON_Provider = !CherrygramChatsConfig.jacksonJSON_Provider
                    sa.dismiss()
                    JsonBottomSheet.showAlert(sa.context, sa.resourcesProvider, sa.fragment, messageObject, null)
                } else {
                    BulletinFactory.of(sa.container, sa.resourcesProvider)
                        .createSimpleBulletin(R.raw.error, "Jackson library is supported on Android 8 and newer.")
                        .show()
                }
            }
            .add(
                R.drawable.msg_calendar2,
                "Date: " + CGResourcesHelper.createDateAndTimeForJSON(messageObject.messageOwner.date.toLong()),
            ) {
                AndroidUtilities.addToClipboard(CGResourcesHelper.createDateAndTimeForJSON(messageObject.messageOwner.date.toLong()))
                BulletinFactory.of(sa.container, sa.resourcesProvider)
                    .createCopyBulletin(getString(R.string.TextCopied))
                    .show()
            }
            .addIf(messageObject.messageOwner != null,
                R.drawable.msg_calendar2,
                if (messageObject.messageOwner.media != null && messageObject.messageOwner.media.document != null) {
                    "Date: ➥ " + CGResourcesHelper.createDateAndTimeForJSON(messageObject.messageOwner.media.document.date.toLong())
                } else if (messageObject.messageOwner.media != null && messageObject.messageOwner.media.photo != null) {
                    "Date: ➥ " + CGResourcesHelper.createDateAndTimeForJSON(messageObject.messageOwner.media.photo.date.toLong())
                } else {
                    "Message is not forwarded."
                }
            ) {
                var textToCopy = ""
                if (messageObject.messageOwner.media != null && messageObject.messageOwner.media.document != null) {
                    textToCopy = CGResourcesHelper.createDateAndTimeForJSON(messageObject.messageOwner.media.document.date.toLong())
                } else if (messageObject.messageOwner.media != null && messageObject.messageOwner.media.photo != null) {
                    textToCopy = CGResourcesHelper.createDateAndTimeForJSON(messageObject.messageOwner.media.photo.date.toLong())
                }
                if (textToCopy != "") {
                    AndroidUtilities.addToClipboard(textToCopy)
                    BulletinFactory.of(sa.container, sa.resourcesProvider)
                        .createCopyBulletin(getString(R.string.TextCopied))
                        .show()
                }
            }
            .addIf(messageObject.messageOwner != null,
                R.drawable.msg_info,
                if (messageObject.messageOwner.media != null && messageObject.messageOwner.media.document != null) {
                    "DC: " + messageObject.messageOwner.media.document.dc_id
                } else if (messageObject.messageOwner.media != null && messageObject.messageOwner.media.photo != null) {
                    "DC: " + messageObject.messageOwner.media.photo.dc_id
                } else {
                    "DC: Available only for media."
                }
            ) {}

            .setDimAlpha(100)
            .translate(-AndroidUtilities.dp(15f).toFloat(), 0f)
            .show()
    }
    /** JSON menu finish */

    /** Message slide action start */
    fun injectChatActivityMsgSlideAction(cf: ChatActivity, msg: MessageObject, isChannel: Boolean, classGuid: Int) {
        when (CherrygramChatsConfig.messageSlideAction) {
            CherrygramChatsConfig.MESSAGE_SLIDE_ACTION_REPLY -> {
                cf.showFieldPanelForReply(msg)
            }
            CherrygramChatsConfig.MESSAGE_SLIDE_ACTION_SAVE -> {
                val chatID = getCustomChatID()

                cf.sendMessagesHelper.sendMessage(arrayListOf(msg), chatID, false, false, true, 0, 0)

                cf.createUndoView()
                if (cf.undoView == null) {
                    return
                }
                if (!BulletinFactory.of(cf).showForwardedBulletinWithTag(chatID, arrayListOf(msg).size)) {
                    cf.undoView!!.showWithAction(chatID, UndoView.ACTION_FWD_MESSAGES, arrayListOf(msg).size)
                }
            }
            CherrygramChatsConfig.MESSAGE_SLIDE_ACTION_TRANSLATE -> {
                val languageAndTextToTranslate: String = msg.messageOwner.message
                val toLang = TranslateAlert2.getToLanguage()
                val alert = TranslateAlert2.showAlert(
                    cf.context,
                    cf,
                    UserConfig.selectedAccount,
                    languageAndTextToTranslate,
                    toLang,
                    languageAndTextToTranslate,
                    null,
                    false,
                    null
                ) { cf.dimBehindView(false) }
                alert.setDimBehindAlpha(100)
                alert.setDimBehind(true)
            }
            CherrygramChatsConfig.MESSAGE_SLIDE_ACTION_TRANSLATE_GEMINI -> {
                if (msg == null && msg.messageOwner == null && msg.messageOwner.message == null) {
                    return
                }

                GeminiResultsBottomSheet.setMessageObject(msg)
                GeminiResultsBottomSheet.setCurrentChat(cf.currentChat)
                cf.processGeminiWithText(msg, null, true, false)
            }
            CherrygramChatsConfig.MESSAGE_SLIDE_ACTION_DIRECT_SHARE -> {
                cf.showDialog(object : ShareAlert(cf.parentActivity, arrayListOf(msg), null, isChannel, null, false) {
                    override fun dismissInternal() {
                        super.dismissInternal()
                        AndroidUtilities.requestAdjustResize(cf.parentActivity, classGuid)
                        if (cf.chatActivityEnterView.isVisible) {
                            cf.fragmentView.requestLayout()
                        }
                        cf.updatePinnedMessageView(true)
                    }
                })

                AndroidUtilities.setAdjustResizeToNothing(cf.parentActivity, classGuid)
                cf.fragmentView.requestLayout()
            }
        }
    }
    /** Message slide action finish */

    /** Misc start */
    fun updateStickerSetCache(fragment: BaseFragment, stickerSet: TLRPC.TL_messages_stickerSet, emoji: Boolean, isKeyboardVisible: Boolean) {
        val req = TLRPC.TL_messages_getStickerSet()
        val input = TLRPC.TL_inputStickerSetShortName().apply {
            short_name = stickerSet.set.short_name
        }
        req.stickerset = input

        fragment.connectionsManager.sendRequest(req) { res, err ->
            AndroidUtilities.runOnUIThread {
                if (res is TLRPC.TL_messages_stickerSet) {
                    fragment.mediaDataController.putStickerSet(res, true)

                    if (fragment.parentActivity == null || fragment.context == null) return@runOnUIThread

                } else {
                    BulletinFactory.of(fragment)
                        .createSimpleBulletin(
                            R.raw.error,
                            getString(if (emoji) R.string.AddEmojiNotFound else R.string.AddStickersNotFound)
                        )
                        .show(true)
                }
            }
        }
    }
    /** Misc finish */

}