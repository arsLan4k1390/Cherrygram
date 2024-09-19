package uz.unnarsx.cherrygram.preferences

import android.media.MediaPlayer
import android.view.HapticFeedbackConstants
import androidx.core.util.Pair
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.BaseFragment
import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig
import uz.unnarsx.cherrygram.core.VibrateUtil
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper
import uz.unnarsx.cherrygram.preferences.helpers.AlertDialogSwitchers
import uz.unnarsx.cherrygram.preferences.tgkit.preference.category
import uz.unnarsx.cherrygram.preferences.tgkit.preference.contract
import uz.unnarsx.cherrygram.preferences.tgkit.preference.hint
import uz.unnarsx.cherrygram.preferences.tgkit.preference.list
import uz.unnarsx.cherrygram.preferences.tgkit.preference.slider
import uz.unnarsx.cherrygram.preferences.tgkit.preference.switch
import uz.unnarsx.cherrygram.preferences.tgkit.preference.textIcon
import uz.unnarsx.cherrygram.preferences.tgkit.preference.tgKitScreen
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitSliderPreference.TGSLContract
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitTextIconRow

class ChatsPreferencesEntry : BasePreferencesEntry {
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(getString(R.string.CP_Header_Chats)) {
        category(getString(R.string.AccDescrStickers)) {
            switch {
                title = getString(R.string.CP_TimeOnStick)

                contract({
                    return@contract CherrygramChatsConfig.hideStickerTime
                }) {
                    CherrygramChatsConfig.hideStickerTime = it
                }
            }
        }
        category(getString(R.string.CP_Slider_StickerAmplifier)) {
            slider {
                contract = object : TGSLContract {
                    override fun setValue(value: Int) {
                        CherrygramChatsConfig.slider_stickerAmplifier = value
                    }

                    override fun getPreferenceValue(): Int {
                        return CherrygramChatsConfig.slider_stickerAmplifier
                    }

                    override fun getMin(): Int {
                        return 50
                    }

                    override fun getMax(): Int {
                        return 100
                    }
                }
            }

        }

        category(getString(R.string.CP_Header_Chats)) {
            textIcon {
                title = getString(R.string.CP_ChatMenuShortcuts)
                icon = R.drawable.msg_list
                listener = TGKitTextIconRow.TGTIListener {
                    AlertDialogSwitchers.showChatActionsAlert(bf)
                }
                divider = true
            }
            switch {
                title = getString(R.string.AP_CenterChatsTitle)
                contract({
                    return@contract CherrygramChatsConfig.centerChatTitle
                }) {
                    CherrygramChatsConfig.centerChatTitle = it
                    bf.parentLayout.rebuildAllFragmentViews(false, false)
                }
            }
            switch {
                title = getString(R.string.CP_UnreadBadgeOnBackButton)
                description = getString(R.string.CP_UnreadBadgeOnBackButton_Desc)

                contract({
                    return@contract CherrygramChatsConfig.unreadBadgeOnBackButton
                }) {
                    CherrygramChatsConfig.unreadBadgeOnBackButton = it
                }
            }
            switch {
                title = getString(R.string.CP_ConfirmCalls)

                contract({
                    return@contract CherrygramChatsConfig.confirmCalls
                }) {
                    CherrygramChatsConfig.confirmCalls = it
                }
            }
            switch {
                title = getString(R.string.CP_HideKbdOnScroll)

                contract({
                    return@contract CherrygramChatsConfig.hideKeyboardOnScroll
                }) {
                    CherrygramChatsConfig.hideKeyboardOnScroll = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
            switch {
                title = getString(R.string.CP_DisableSwipeToNext)
                description = getString(R.string.CP_DisableSwipeToNext_Desc)

                contract({
                    return@contract CherrygramChatsConfig.disableSwipeToNext
                }) {
                    CherrygramChatsConfig.disableSwipeToNext = it
                }
            }
            switch {
                title = getString(R.string.CP_HideMuteUnmuteButton)

                contract({
                    return@contract CherrygramChatsConfig.hideMuteUnmuteButton
                }) {
                    CherrygramChatsConfig.hideMuteUnmuteButton = it
                }
            }
        }

        category(getString(R.string.CP_Slider_RecentEmojisAmplifier)) {
            slider {
                contract = object : TGSLContract {
                    override fun setValue(value: Int) {
                        CherrygramChatsConfig.slider_RecentEmojisAmplifier = value
                    }

                    override fun getPreferenceValue(): Int {
                        return CherrygramChatsConfig.slider_RecentEmojisAmplifier
                    }

                    override fun getMin(): Int {
                        return 45
                    }

                    override fun getMax(): Int {
                        return 90
                    }
                }
            }
        }

        category(getString(R.string.CP_Slider_RecentStickersAmplifier)) {
            slider {
                contract = object : TGSLContract {
                    override fun setValue(value: Int) {
                        CherrygramChatsConfig.slider_RecentStickersAmplifier = value
                    }

                    override fun getPreferenceValue(): Int {
                        return CherrygramChatsConfig.slider_RecentStickersAmplifier
                    }

                    override fun getMin(): Int {
                        return 20
                    }

                    override fun getMax(): Int {
                        return 120
                    }
                }
            }
        }

        category(getString(R.string.CP_Header_Messages)) {
            textIcon {
                title = getString(R.string.DirectShare)
                icon = R.drawable.msg_share
                listener = TGKitTextIconRow.TGTIListener {
                    AlertDialogSwitchers.showDirectShareAlert(bf)
                }
                divider = true
            }
            textIcon {
                title = getString(R.string.CP_MessageMenu)
                icon = R.drawable.msg_list
                listener = TGKitTextIconRow.TGTIListener {
                    AlertDialogSwitchers.showChatMenuIconsAlert(bf)
                }
                divider = true
            }
            switch {
                title = getString(R.string.CP_DeleteForAll)
                description = getString(R.string.CP_DeleteForAll_Desc)

                contract({
                    return@contract CherrygramChatsConfig.deleteForAll
                }) {
                    CherrygramChatsConfig.deleteForAll = it
                }
            }
            switch {
                title = getString(R.string.CP_ForwardMsgDate)

                contract({
                    return@contract CherrygramChatsConfig.msgForwardDate
                }) {
                    CherrygramChatsConfig.msgForwardDate = it
                }
            }
            switch {
                title = getString(R.string.AP_ShowPencilIcon)
                contract({
                    return@contract CherrygramChatsConfig.showPencilIcon
                }) {
                    CherrygramChatsConfig.showPencilIcon = it
                }
            }
            list {
                title = getString(R.string.CP_LeftBottomButtonAction)

                contract({
                    return@contract listOf(
                        Pair(CherrygramChatsConfig.LEFT_BUTTON_FORWARD_WO_AUTHORSHIP, getString(R.string.Forward) + " " + getString(R.string.CG_Without_Authorship)),
                        Pair(CherrygramChatsConfig.LEFT_BUTTON_REPLY, getString(R.string.Reply)),
                        Pair(CherrygramChatsConfig.LEFT_BUTTON_SAVE_MESSAGE, getString(R.string.CG_ToSaved)),
                        Pair(CherrygramChatsConfig.LEFT_BUTTON_DIRECT_SHARE, getString(R.string.DirectShare))
                    )
                }, {
                    return@contract when (CherrygramChatsConfig.leftBottomButton) {
                        CherrygramChatsConfig.LEFT_BUTTON_REPLY -> getString(R.string.Reply)
                        CherrygramChatsConfig.LEFT_BUTTON_SAVE_MESSAGE -> getString(R.string.CG_ToSaved)
                        CherrygramChatsConfig.LEFT_BUTTON_DIRECT_SHARE -> getString(R.string.DirectShare)
                        else -> getString(R.string.Forward) + " " + getString(R.string.CG_Without_Authorship)
                    }
                }) {
                    CherrygramChatsConfig.leftBottomButton = it
                }
            }
            list {
                title = getString(R.string.CP_DoubleTapAction)

                contract({
                    return@contract listOf(
                        Pair(CherrygramChatsConfig.DOUBLE_TAP_ACTION_NONE, getString(R.string.Disable)),
                        Pair(CherrygramChatsConfig.DOUBLE_TAP_ACTION_REACTION, getString(R.string.Reactions)),
                        Pair(CherrygramChatsConfig.DOUBLE_TAP_ACTION_REPLY, getString(R.string.Reply)),
                        Pair(CherrygramChatsConfig.DOUBLE_TAP_ACTION_SAVE, getString(R.string.CG_ToSaved)),
                        Pair(CherrygramChatsConfig.DOUBLE_TAP_ACTION_EDIT, getString(R.string.Edit)),
                        Pair(CherrygramChatsConfig.DOUBLE_TAP_ACTION_TRANSLATE, getString(R.string.TranslateMessage))
                    )
                }, {
                    return@contract when (CherrygramChatsConfig.doubleTapAction) {
                        CherrygramChatsConfig.DOUBLE_TAP_ACTION_REACTION -> getString(R.string.Reactions)
                        CherrygramChatsConfig.DOUBLE_TAP_ACTION_REPLY -> getString(R.string.Reply)
                        CherrygramChatsConfig.DOUBLE_TAP_ACTION_SAVE -> getString(R.string.CG_ToSaved)
                        CherrygramChatsConfig.DOUBLE_TAP_ACTION_EDIT -> getString(R.string.Edit)
                        CherrygramChatsConfig.DOUBLE_TAP_ACTION_TRANSLATE -> getString(R.string.TranslateMessage)
                        else -> getString(R.string.Disable)
                    }
                }) {
                    CherrygramChatsConfig.doubleTapAction = it
                }
            }
            list {
                title = getString(R.string.CG_MsgSlideAction)

                contract({
                    return@contract listOf(
                        Pair(CherrygramChatsConfig.MESSAGE_SLIDE_ACTION_REPLY, getString(R.string.Reply)),
                        Pair(CherrygramChatsConfig.MESSAGE_SLIDE_ACTION_SAVE, getString(R.string.CG_ToSaved)),
                        Pair(CherrygramChatsConfig.MESSAGE_SLIDE_ACTION_TRANSLATE, getString(R.string.TranslateMessage)),
                        Pair(CherrygramChatsConfig.MESSAGE_SLIDE_ACTION_DIRECT_SHARE, getString(R.string.DirectShare))
                    )
                }, {
                    return@contract when (CherrygramChatsConfig.messageSlideAction) {
                        CherrygramChatsConfig.MESSAGE_SLIDE_ACTION_SAVE -> getString(R.string.CG_ToSaved)
                        CherrygramChatsConfig.MESSAGE_SLIDE_ACTION_TRANSLATE -> getString(R.string.TranslateMessage)
                        CherrygramChatsConfig.MESSAGE_SLIDE_ACTION_DIRECT_SHARE -> getString(R.string.DirectShare)
                        else -> getString(R.string.Reply)
                    }
                }) {
                    CherrygramChatsConfig.messageSlideAction = it
                }
            }
        }

        category(getString(R.string.CP_Header_Record)) {
            switch {
                title = getString(R.string.EP_PhotosSize)

                contract({
                    return@contract CherrygramChatsConfig.largePhotos
                }) {
                    CherrygramChatsConfig.largePhotos = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
            switch {
                title = getString(R.string.CP_SpoilersOnMedia)

                contract({
                    return@contract CherrygramChatsConfig.spoilersOnMedia
                }) {
                    CherrygramChatsConfig.spoilersOnMedia = it
                }
            }
            switch {
                title = getString(R.string.CP_VoiceEnhancements)
                description = getString(R.string.CP_VoiceEnhancements_Desc)

                contract({
                    return@contract CherrygramChatsConfig.voicesAgc
                }) {
                    CherrygramChatsConfig.voicesAgc = it
                }
            }
            switch {
                title = getString(R.string.CP_PlayVideo)
                description = getString(R.string.CP_PlayVideo_Desc)

                contract({
                    return@contract CherrygramChatsConfig.playVideoOnVolume
                }) {
                    CherrygramChatsConfig.playVideoOnVolume = it
                }
            }
            switch {
                title = getString(R.string.CP_AutoPauseVideo)
                description = getString(R.string.CP_AutoPauseVideo_Desc)

                contract({
                    return@contract CherrygramChatsConfig.autoPauseVideo
                }) {
                    CherrygramChatsConfig.autoPauseVideo = it
                }
            }
            switch {
                title = getString(R.string.CP_DisableVibration)

                contract({
                    return@contract CherrygramChatsConfig.disableVibration
                }) {
                    CherrygramChatsConfig.disableVibration = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
        }

        category(getString(R.string.CP_VideoSeekDuration)) {
            slider {
                contract = object : TGSLContract {
                    override fun setValue(value: Int) {
                        CherrygramChatsConfig.videoSeekDuration = value
                    }

                    override fun getPreferenceValue(): Int {
                        return CherrygramChatsConfig.videoSeekDuration
                    }

                    override fun getMin(): Int {
                        return 0
                    }

                    override fun getMax(): Int {
                        return 25
                    }
                }
            }
        }

        category(getString(R.string.CP_Header_Notification)) {
            list {
                title = getString(R.string.CP_NotificationSound)

                contract({
                    return@contract listOf(
                        Pair(CherrygramChatsConfig.NOTIF_SOUND_DISABLE, getString(R.string.Disable)),
                        Pair(CherrygramChatsConfig.NOTIF_SOUND_DEFAULT, getString(R.string.Default)),
                        Pair(CherrygramChatsConfig.NOTIF_SOUND_IOS, "IOS")
                    )
                }, {
                    return@contract when (CherrygramChatsConfig.notificationSound) {
                        CherrygramChatsConfig.NOTIF_SOUND_DEFAULT -> getString(R.string.Default)
                        CherrygramChatsConfig.NOTIF_SOUND_IOS -> "IOS"
                        else -> getString(R.string.Disable)
                    }
                }) {
                    CherrygramChatsConfig.notificationSound = it

                    var tone = 0
                    try {
                        if (CherrygramChatsConfig.notificationSound == 1) {
                            tone = R.raw.sound_in
                        } else if (CherrygramChatsConfig.notificationSound == 2) {
                            tone = R.raw.sound_in_ios
                        }
                        val mp: MediaPlayer = MediaPlayer.create(bf.context, tone)
                        mp.start()
                    } catch (ignore: Exception) { }

                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
            list {
                title = getString(R.string.CP_VibrateInChats)

                contract({
                    return@contract listOf(
                        Pair(CherrygramChatsConfig.VIBRATION_DISABLE, getString(R.string.Disable)),
                        Pair(CherrygramChatsConfig.VIBRATION_CLICK, "1"),
                        Pair(CherrygramChatsConfig.VIBRATION_WAVE_FORM, "2"),
                        Pair(CherrygramChatsConfig.VIBRATION_KEYBOARD_TAP, "3"),
                        Pair(CherrygramChatsConfig.VIBRATION_LONG, "4")
                    )
                }, {
                    return@contract when (CherrygramChatsConfig.vibrateInChats) {
                        CherrygramChatsConfig.VIBRATION_CLICK -> "1"
                        CherrygramChatsConfig.VIBRATION_WAVE_FORM -> "2"
                        CherrygramChatsConfig.VIBRATION_KEYBOARD_TAP -> "3"
                        CherrygramChatsConfig.VIBRATION_LONG -> "4"
                        else -> getString(R.string.Disable)
                    }
                }) {
                    CherrygramChatsConfig.vibrateInChats = it

                    try {
                        when (CherrygramChatsConfig.vibrateInChats) {
                            CherrygramChatsConfig.VIBRATION_CLICK -> {
                                VibrateUtil.makeClickVibration()
                            }
                            CherrygramChatsConfig.VIBRATION_WAVE_FORM -> {
                                VibrateUtil.makeWaveVibration()
                            }
                            CherrygramChatsConfig.VIBRATION_KEYBOARD_TAP -> {
                                VibrateUtil.vibrate(HapticFeedbackConstants.KEYBOARD_TAP.toLong())
                            }
                            CherrygramChatsConfig.VIBRATION_LONG -> {
                                VibrateUtil.vibrate()
                            }
                        }
                    } catch (ignore: Exception) { }

                }
            }
            hint(getString(R.string.CP_VibrateInChats_Desc))

            switch {
                title = getString(R.string.CP_SilenceNonContacts)
                description = getString(R.string.CP_SilenceNonContacts_Desc)


                contract({
                    return@contract CherrygramChatsConfig.silenceNonContacts
                }) {
                    CherrygramChatsConfig.silenceNonContacts = it
                }
            }
        }
    }
}
