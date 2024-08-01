package uz.unnarsx.cherrygram.chats.helpers

import android.view.View
import android.widget.FrameLayout
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ChatObject
import org.telegram.messenger.LocaleController
import org.telegram.messenger.MessageObject
import org.telegram.messenger.MessagesController
import org.telegram.messenger.R
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.TLRPC
import org.telegram.ui.ActionBar.ActionBarPopupWindow
import org.telegram.ui.AvatarPreviewer
import org.telegram.ui.ChatActivity
import org.telegram.ui.ChatRightsEditActivity
import org.telegram.ui.Components.BulletinFactory
import org.telegram.ui.Components.ShareAlert
import org.telegram.ui.Components.TranslateAlert2
import org.telegram.ui.Components.UndoView
import uz.unnarsx.cherrygram.CherrygramConfig
import uz.unnarsx.cherrygram.chats.JsonBottomSheet
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper
import uz.unnarsx.cherrygram.helpers.ui.ActionBarPopupWindowHelper

object ChatsHelper2 {

    /**Avatar admin actions start**/
    @JvmStatic
    fun injectChatActivityAvatarArraySize(cf: ChatActivity): Int {
        var objs = 0

        if (ChatObject.canBlockUsers(cf.currentChat)) objs++
        if (ChatObject.hasAdminRights(cf.currentChat)) objs++
        if (ChatObject.canAddAdmins(cf.currentChat)) objs++

        return objs
    }

    @JvmStatic
    fun injectChatActivityAvatarArrayItems(cf: ChatActivity, arr: Array<AvatarPreviewer.MenuItem>, enableMention: Boolean, enableSearchMessages: Boolean) {
        var startPos = if (enableMention || enableSearchMessages) 3 else 2

        if (ChatObject.canBlockUsers(cf.currentChat)) {
            arr[startPos] = AvatarPreviewer.MenuItem.CG_KICK
            startPos++
        }

        if (ChatObject.hasAdminRights(cf.currentChat)) {
            arr[startPos] = AvatarPreviewer.MenuItem.CG_CHANGE_PERMS
            startPos++
        }

        if (ChatObject.canAddAdmins(cf.currentChat)) {
            arr[startPos] = AvatarPreviewer.MenuItem.CG_CHANGE_ADMIN_PERMS
            startPos++
        }
    }

    @JvmStatic
    fun injectChatActivityAvatarOnClick(cf: ChatActivity, item: AvatarPreviewer.MenuItem, user: TLRPC.User) {
        when (item) {
            AvatarPreviewer.MenuItem.CG_KICK -> {
                cf.messagesController.deleteParticipantFromChat(cf.currentChat.id, cf.messagesController.getUser(user.id), cf.currentChatInfo)
            }
            AvatarPreviewer.MenuItem.CG_CHANGE_PERMS, AvatarPreviewer.MenuItem.CG_CHANGE_ADMIN_PERMS -> {
                val action = if (item == AvatarPreviewer.MenuItem.CG_CHANGE_PERMS) 1 else 0 // 0 - change admin rights

                val chatParticipant = cf.currentChatInfo.participants.participants.filter {
                    it.user_id == user.id
                }[0]

                var channelParticipant: TLRPC.ChannelParticipant? = null

                if (ChatObject.isChannel(cf.currentChat)) {
                    channelParticipant = (chatParticipant as TLRPC.TL_chatChannelParticipant).channelParticipant
                } else {
                    chatParticipant is TLRPC.TL_chatParticipantAdmin
                }

                val frag = ChatRightsEditActivity(
                    user.id,
                    cf.currentChatInfo.id,
                    channelParticipant?.admin_rights,
                    cf.currentChat.default_banned_rights,
                    channelParticipant?.banned_rights,
                    channelParticipant?.rank,
                    action,
                    true,
                    false,
                    null
                )

                cf.presentFragment(frag)
            }

            else -> {}
        }
    }
    /**Avatar admin actions finish**/

    /**Chat search filter start**/
    @JvmStatic
    fun getSearchFilterType(): TLRPC.MessagesFilter {
        val filter: TLRPC.MessagesFilter = when (CherrygramConfig.messagesSearchFilter) {
            CherrygramConfig.FILTER_PHOTOS -> {
                TLRPC.TL_inputMessagesFilterPhotos()
            }
            CherrygramConfig.FILTER_VIDEOS -> {
                TLRPC.TL_inputMessagesFilterVideo()
            }
            CherrygramConfig.FILTER_VOICE_MESSAGES -> {
                TLRPC.TL_inputMessagesFilterVoice()
            }
            CherrygramConfig.FILTER_VIDEO_MESSAGES -> {
                TLRPC.TL_inputMessagesFilterRoundVideo()
            }
            CherrygramConfig.FILTER_FILES -> {
                TLRPC.TL_inputMessagesFilterDocument()
            }
            CherrygramConfig.FILTER_MUSIC -> {
                TLRPC.TL_inputMessagesFilterMusic()
            }
            CherrygramConfig.FILTER_GIFS -> {
                TLRPC.TL_inputMessagesFilterGif()
            }
            CherrygramConfig.FILTER_GEO -> {
                TLRPC.TL_inputMessagesFilterGeo()
            }
            CherrygramConfig.FILTER_CONTACTS -> {
                TLRPC.TL_inputMessagesFilterContacts()
            }
            CherrygramConfig.FILTER_MENTIONS -> {
                TLRPC.TL_inputMessagesFilterMyMentions()
            }
            else -> {
                TLRPC.TL_inputMessagesFilterEmpty()
            }
        }
        return filter
    }
    /**Chat search filter finish**/

    /**Custom chat id for Saved Messages start**/
    @JvmStatic
    fun checkCustomChatID(currentAccount: Int) {
        val preferences = MessagesController.getMainSettings(currentAccount)
        val empty = preferences.getString("CP_CustomChatIDSM", "CP_CustomChatIDSM").equals("")
        if (empty) {
            CherrygramConfig.putStringForUserPrefs("CP_CustomChatIDSM",
                UserConfig.getInstance(currentAccount).getClientUserId().toString()
            )
//            FileLog.e("changed the id")
        }
    }

    @JvmStatic
    fun getCustomChatID(): Long {
        val id: Long
        val preferences = MessagesController.getMainSettings(UserConfig.selectedAccount)
        val savedMessagesChatID =
            preferences.getString("CP_CustomChatIDSM", UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId().toString())
        val chatID = savedMessagesChatID!!.replace("-100", "-").toLong()

        id = if (CherrygramConfig.customChatForSavedMessages) {
            chatID
        } else {
            UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId()
        }
        return id
    }
    /**Custom chat id for Saved Messages finish**/

    /**Direct share menu start**/
    private var currentPopup: ActionBarPopupWindow? = null
    @JvmStatic
    fun showForwardMenu(sa: ShareAlert, field: FrameLayout) {
        currentPopup = ActionBarPopupWindowHelper.createPopupWindow(sa.container, field, sa.context, listOf(
            ActionBarPopupWindowHelper.PopupItem(
                if (CherrygramConfig.forwardNoAuthorship)
                    LocaleController.getString("CG_FwdMenu_DisableNoForward", R.string.CG_FwdMenu_DisableNoForward)
                else LocaleController.getString("CG_FwdMenu_EnableNoForward", R.string.CG_FwdMenu_EnableNoForward),
                R.drawable.msg_forward
            ) {
                // Toggle!
                CherrygramConfig.forwardNoAuthorship = !CherrygramConfig.forwardNoAuthorship
                currentPopup?.dismiss()
                currentPopup = null
            },
            ActionBarPopupWindowHelper.PopupItem(
                if (CherrygramConfig.forwardWithoutCaptions)
                    LocaleController.getString("CG_FwdMenu_EnableCaptions", R.string.CG_FwdMenu_EnableCaptions)
                else LocaleController.getString("CG_FwdMenu_DisableCaptions", R.string.CG_FwdMenu_DisableCaptions),
                R.drawable.msg_edit
            ) {
                // Toggle!
                CherrygramConfig.forwardWithoutCaptions = !CherrygramConfig.forwardWithoutCaptions
                currentPopup?.dismiss()
                currentPopup = null
            },
            ActionBarPopupWindowHelper.PopupItem(
                if (CherrygramConfig.forwardNotify)
                    LocaleController.getString("CG_FwdMenu_NoNotify", R.string.CG_FwdMenu_NoNotify)
                else LocaleController.getString("CG_FwdMenu_Notify", R.string.CG_FwdMenu_Notify),
                R.drawable.input_notify_on
            ) {
                // Toggle!
                CherrygramConfig.forwardNotify = !CherrygramConfig.forwardNotify
                currentPopup?.dismiss()
                currentPopup = null
            },
        ))
    }
    /**Direct share menu finish**/

    /**JSON menu start**/
    @JvmStatic
    fun showJsonMenu(sa: JsonBottomSheet, field: FrameLayout, messageObject: MessageObject) {
        currentPopup = ActionBarPopupWindowHelper.createPopupWindow(sa.container, field, sa.context, listOf(
            ActionBarPopupWindowHelper.PopupItem(
                "Date: " + CGResourcesHelper.createDateAndTimeForJSON(messageObject.messageOwner.date.toLong()),
                R.drawable.msg_calendar2
            ) {
                currentPopup?.dismiss()
                currentPopup = null
            }
        ))
    }
    /**JSON menu finish**/

    /**Message slide action start**/
    @JvmStatic
    fun injectChatActivityMsgSlideAction(cf: ChatActivity, msg: MessageObject, isChannel: Boolean, classGuid: Int) {
        when (CherrygramConfig.messageSlideAction) {
            CherrygramConfig.MESSAGE_SLIDE_ACTION_REPLY -> {
                // Reply (default)
                cf.showFieldPanelForReply(msg)
            }
            CherrygramConfig.MESSAGE_SLIDE_ACTION_SAVE -> {
                // Save message
                val id = ChatsHelper2.getCustomChatID()

                cf.sendMessagesHelper.sendMessage(arrayListOf(msg), id, false, false, true, 0)

                cf.createUndoView()
                if (cf.undoView == null) {
                    return
                }
                if (!CherrygramConfig.customChatForSavedMessages) {
                    if (!BulletinFactory.of(cf).showForwardedBulletinWithTag(id, arrayListOf(msg).size)) {
                        cf.undoView!!.showWithAction(id, UndoView.ACTION_FWD_MESSAGES, arrayListOf(msg).size)
                    }
                } else {
                    cf.undoView!!.showWithAction(id, UndoView.ACTION_FWD_MESSAGES, arrayListOf(msg).size)
                }
            }
            CherrygramConfig.MESSAGE_SLIDE_ACTION_TRANSLATE -> {
                // Translate
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
            CherrygramConfig.MESSAGE_SLIDE_ACTION_DIRECT_SHARE -> {
                // Direct Share
                cf.showDialog(object : ShareAlert(cf.parentActivity, arrayListOf(msg), null, isChannel, null, false) {
                    override fun dismissInternal() {
                        super.dismissInternal()
                        AndroidUtilities.requestAdjustResize(cf.parentActivity, classGuid)
                        if (cf.chatActivityEnterView.visibility == View.VISIBLE) {
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
    /**Message slide action finish**/

}