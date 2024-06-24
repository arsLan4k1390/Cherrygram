package uz.unnarsx.cherrygram

import android.os.Build
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
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
import org.telegram.ui.LauncherIconController
import org.telegram.ui.LauncherIconController.LauncherIcon
import uz.unnarsx.cherrygram.extras.CherrygramExtras
import uz.unnarsx.cherrygram.helpers.JsonBottomSheet
import uz.unnarsx.cherrygram.ui.dialogs.ShareAlertExtraUI

// I've created this so CG features can be injected in a source file with 1 line only (maybe)
// Because manual editing of drklo's sources harms your mental health.
object CGFeatureHooks {

    @JvmStatic
    fun setFlashLight(b: Boolean) {
        // ...
        CherrygramConfig.whiteBackground = b
    }

    @JvmStatic
    fun switchNoAuthor(b: Boolean) {
        // ...
        CherrygramConfig.noAuthorship = b
    }

    @JvmStatic
    fun switchGifSpoilers(b: Boolean) {
        // ...
        CherrygramConfig.gifSpoilers = b
    }

    private var currentPopup: ActionBarPopupWindow? = null
    @JvmStatic
    fun showForwardMenu(sa: ShareAlert, field: FrameLayout) {
        currentPopup = ShareAlertExtraUI.createPopupWindow(sa.container, field, sa.context, listOf(
            ShareAlertExtraUI.PopupItem(
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
            ShareAlertExtraUI.PopupItem(
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
            ShareAlertExtraUI.PopupItem(
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

    @JvmStatic
    fun injectChatActivityMsgSlideAction(cf: ChatActivity, msg: MessageObject, isChannel: Boolean, classGuid: Int) {
        when (CherrygramConfig.messageSlideAction) {
            CherrygramConfig.MESSAGE_SLIDE_ACTION_REPLY -> {
                // Reply (default)
                cf.showFieldPanelForReply(msg)
            }
            CherrygramConfig.MESSAGE_SLIDE_ACTION_SAVE -> {
                // Save message
                val id = CherrygramExtras.getCustomChatID()

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

    @JvmStatic
    fun getReplyIconDrawable(): Int {
        return when (CherrygramConfig.messageSlideAction) {
            CherrygramConfig.MESSAGE_SLIDE_ACTION_SAVE -> R.drawable.msg_saved_filled_solar
            CherrygramConfig.MESSAGE_SLIDE_ACTION_DIRECT_SHARE -> R.drawable.msg_share_filled
            CherrygramConfig.MESSAGE_SLIDE_ACTION_TRANSLATE -> R.drawable.msg_translate_filled_solar
            else -> R.drawable.filled_button_reply
        }
    }

    @JvmStatic
    fun showJsonMenu(sa: JsonBottomSheet, field: FrameLayout, messageObject: MessageObject) {
        currentPopup = ShareAlertExtraUI.createPopupWindow(sa.container, field, sa.context, listOf(
            ShareAlertExtraUI.PopupItem(
                "Date: " + CherrygramExtras.createDateAndTimeForJSON(messageObject.messageOwner.date.toLong()),
                R.drawable.msg_calendar2
            ) {
                currentPopup?.dismiss()
                currentPopup = null
            },
        ))
    }

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

    @JvmStatic
    fun getProperNotificationIcon(): Int { //App notification icon
        return if (CherrygramConfig.oldNotificationIcon) {
            R.drawable.notification
        } else {
            return if (LauncherIconController.isEnabled(LauncherIcon.DARK_CHERRY_BRA)
                || LauncherIconController.isEnabled(LauncherIcon.WHITE_CHERRY_BRA)
                || LauncherIconController.isEnabled(LauncherIcon.VIOLET_SUNSET_CHERRY_BRA)
                )
                R.drawable.cg_notification_bra else R.drawable.cg_notification
        }
    }

    @JvmStatic
    fun getLeftButtonText(): String { //ChatActivity.java:\Left button action
        return when (CherrygramConfig.leftBottomButton) {
            CherrygramConfig.LEFT_BUTTON_REPLY -> LocaleController.getString("Reply", R.string.Reply)
            CherrygramConfig.LEFT_BUTTON_SAVE_MESSAGE -> LocaleController.getString("CG_ToSaved", R.string.CG_ToSaved)
            CherrygramConfig.LEFT_BUTTON_DIRECT_SHARE -> LocaleController.getString("DirectShare", R.string.DirectShare)
            else -> AndroidUtilities.capitalize(LocaleController.getString("CG_Without_Authorship", R.string.CG_Without_Authorship))
        }
    }
    @JvmStatic
    fun getLeftButtonDrawable(): Int { //ChatActivity.java:\Left button action
        return when (CherrygramConfig.leftBottomButton) {
            CherrygramConfig.LEFT_BUTTON_REPLY -> R.drawable.input_reply
            CherrygramConfig.LEFT_BUTTON_SAVE_MESSAGE -> R.drawable.msg_saved
            CherrygramConfig.LEFT_BUTTON_DIRECT_SHARE -> R.drawable.msg_share
            else -> R.drawable.input_reply
        }
    }

    @JvmStatic
    fun getCameraAdvise(): CharSequence { //CameraPreferences.java:\CameraX advise
        val advise: String = when (CherrygramConfig.cameraType) {
            CherrygramConfig.TELEGRAM_CAMERA -> LocaleController.getString("CP_DefaultCameraDesc", R.string.CP_DefaultCameraDesc)
            CherrygramConfig.CAMERA_X -> LocaleController.getString("CP_CameraXDesc", R.string.CP_CameraXDesc)
            CherrygramConfig.CAMERA_2 -> LocaleController.getString("CP_Camera2Desc", R.string.CP_Camera2Desc)
            else -> LocaleController.getString("CP_SystemCameraDesc", R.string.CP_SystemCameraDesc)
        }

        val htmlParsed: Spannable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SpannableString(Html.fromHtml(advise, Html.FROM_HTML_MODE_LEGACY))
        } else {
            SpannableString(Html.fromHtml(advise))
        }
        return CherrygramExtras.getUrlNoUnderlineText(htmlParsed)
    }

    @JvmStatic
    fun getCameraAspectRatio(): String { //CameraPreferences.java:\Camera aspect ratio
        return when (CherrygramConfig.cameraAspectRatio) {
            CherrygramConfig.Camera1to1 -> "1:1"
            CherrygramConfig.Camera4to3 -> "4:3"
            CherrygramConfig.Camera16to9 -> "16:9"
            else -> LocaleController.getString("Default", R.string.Default)
        }
    }

    @JvmStatic
    fun getCameraName(): String { //Crashlytics.java
        return when (CherrygramConfig.cameraType) {
            CherrygramConfig.TELEGRAM_CAMERA -> "Telegram"
            CherrygramConfig.CAMERA_X -> "CameraX"
            CherrygramConfig.CAMERA_2 -> "Camera 2"
            else -> LocaleController.getString("CP_CameraTypeSystem", R.string.CP_CameraTypeSystem)
        }
    }

    @JvmStatic
    fun getDownloadSpeedBoostText(): String { //ExperimentalPreferences.java:\Download speed boost
        return when (CherrygramConfig.downloadSpeedBoost) {
            CherrygramConfig.BOOST_NONE -> LocaleController.getString("EP_DownloadSpeedBoostNone", R.string.EP_DownloadSpeedBoostNone)
            CherrygramConfig.BOOST_AVERAGE -> LocaleController.getString("EP_DownloadSpeedBoostAverage", R.string.EP_DownloadSpeedBoostAverage)
            else -> LocaleController.getString("EP_DownloadSpeedBoostExtreme", R.string.EP_DownloadSpeedBoostExtreme)
        }
    }

    @JvmStatic
    fun getShowDcIdText(): String { //MessagesAndProfilesPreferencesEntry.java:\Show dc id
        return when (CherrygramConfig.showIDDC) {
            CherrygramConfig.ID_ONLY -> "ID"
            CherrygramConfig.ID_DC -> "ID + DC"
            else -> LocaleController.getString("Disable", R.string.Disable)
        }
    }

}