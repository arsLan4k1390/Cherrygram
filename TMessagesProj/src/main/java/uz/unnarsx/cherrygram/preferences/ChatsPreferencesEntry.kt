package uz.unnarsx.cherrygram.preferences

import android.app.Activity
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.view.HapticFeedbackConstants
import androidx.core.util.Pair
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.BaseFragment
import uz.unnarsx.cherrygram.CherrygramConfig
import uz.unnarsx.cherrygram.extras.VibrateUtil
import uz.unnarsx.cherrygram.helpers.AppRestartHelper
import uz.unnarsx.cherrygram.tgkit.preference.*
import uz.unnarsx.cherrygram.tgkit.preference.types.TGKitSliderPreference.TGSLContract
import uz.unnarsx.cherrygram.tgkit.preference.types.TGKitTextIconRow

class ChatsPreferencesEntry : BasePreferencesEntry {
    val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(LocaleController.getString("AS_Header_Chats", R.string.CP_Header_Chats)) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(CherrygramConfig.listener)
        category(LocaleController.getString("AccDescrStickers", R.string.AccDescrStickers)) {
            switch {
                title = LocaleController.getString("CP_BlockSomeSets", R.string.CP_BlockSomeSets)
                summary = LocaleController.getString("CP_BlockSomeSets_Desc", R.string.CP_BlockSomeSets_Desc)

                contract({
                    return@contract CherrygramConfig.blockStickers
                }) {
                    CherrygramConfig.blockStickers = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
            switch {
                title = LocaleController.getString("CP_TimeOnStick", R.string.CP_TimeOnStick)

                contract({
                    return@contract CherrygramConfig.hideStickerTime
                }) {
                    CherrygramConfig.hideStickerTime = it
                }
            }
        }
        category(LocaleController.getString("CP_Slider_StickerAmplifier", R.string.CP_Slider_StickerAmplifier)) {
            slider {
                contract = object : TGSLContract {
                    override fun setValue(value: Int) {
                        CherrygramConfig.slider_stickerAmplifier = value
                    }

                    override fun getPreferenceValue(): Int {
                        return CherrygramConfig.slider_stickerAmplifier
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

        category(LocaleController.getString("AS_Header_Chats", R.string.CP_Header_Chats)) {
            textIcon {
                title = LocaleController.getString("DirectShare", R.string.DirectShare)
                icon = R.drawable.msg_share
                listener = TGKitTextIconRow.TGTIListener {
                    AlertDialogSwitchHelper.showDirectShareAlert(bf)
                }
                divider = true
            }
            textIcon {
                title = LocaleController.getString("CP_MessageMenu", R.string.CP_MessageMenu)
                icon = R.drawable.msg_list
                listener = TGKitTextIconRow.TGTIListener {
                    AlertDialogSwitchHelper.showChatMenuIconsAlert(bf)
                }
                divider = true
            }
            switch {
                title = LocaleController.getString("CP_UnreadBadgeOnBackButton", R.string.CP_UnreadBadgeOnBackButton)
                summary = LocaleController.getString("CP_UnreadBadgeOnBackButton_Desc", R.string.CP_UnreadBadgeOnBackButton_Desc)

                contract({
                    return@contract CherrygramConfig.unreadBadgeOnBackButton
                }) {
                    CherrygramConfig.unreadBadgeOnBackButton = it
                }
            }
            switch {
                title = LocaleController.getString("CP_DeleteForAll", R.string.CP_DeleteForAll)
                summary = LocaleController.getString("CP_DeleteForAll_Desc", R.string.CP_DeleteForAll_Desc)

                contract({
                    return@contract CherrygramConfig.deleteForAll
                }) {
                    CherrygramConfig.deleteForAll = it
                }
            }
            switch {
                title = LocaleController.getString("CP_ForwardMsgDate", R.string.CP_ForwardMsgDate)

                contract({
                    return@contract CherrygramConfig.msgForwardDate
                }) {
                    CherrygramConfig.msgForwardDate = it
                }
            }
            switch {
                title = LocaleController.getString("AP_ShowPencilIcon", R.string.AP_ShowPencilIcon)
                contract({
                    return@contract CherrygramConfig.showPencilIcon
                }) {
                    CherrygramConfig.showPencilIcon = it
                }
            }
            list {
                title = LocaleController.getString("CP_LeftBottomButtonAction", R.string.CP_LeftBottomButtonAction)

                contract({
                    return@contract listOf(
                        Pair(CherrygramConfig.LEFT_BUTTON_FORWARD_WO_AUTHORSHIP, LocaleController.getString("Forward", R.string.Forward) + " " + LocaleController.getString("CG_Without_Authorship", R.string.CG_Without_Authorship)),
                        Pair(CherrygramConfig.LEFT_BUTTON_DIRECT_SHARE, LocaleController.getString("DirectShare", R.string.DirectShare)),
                        Pair(CherrygramConfig.LEFT_BUTTON_REPLY, LocaleController.getString("Reply", R.string.Reply))
                    )
                }, {
                    return@contract when (CherrygramConfig.leftBottomButton) {
                        CherrygramConfig.LEFT_BUTTON_DIRECT_SHARE -> LocaleController.getString("DirectShare", R.string.DirectShare)
                        CherrygramConfig.LEFT_BUTTON_REPLY -> LocaleController.getString("Reply", R.string.Reply)
                        else -> LocaleController.getString("Forward", R.string.Forward) + " " + LocaleController.getString("CG_Without_Authorship", R.string.CG_Without_Authorship)
                    }
                }) {
                    CherrygramConfig.leftBottomButton = it
                }
            }
            list {
                title = LocaleController.getString("CP_DoubleTapAction", R.string.CP_DoubleTapAction)

                contract({
                    return@contract listOf(
                        Pair(CherrygramConfig.DOUBLE_TAP_ACTION_NONE, LocaleController.getString("Disable", R.string.Disable)),
                        Pair(CherrygramConfig.DOUBLE_TAP_ACTION_REACTION, LocaleController.getString("Reactions", R.string.Reactions)),
                        Pair(CherrygramConfig.DOUBLE_TAP_ACTION_REPLY, LocaleController.getString("Reply", R.string.Reply)),
                        Pair(CherrygramConfig.DOUBLE_TAP_ACTION_SAVE, LocaleController.getString("CG_ToSaved", R.string.CG_ToSaved)),
                        Pair(CherrygramConfig.DOUBLE_TAP_ACTION_EDIT, LocaleController.getString("Edit", R.string.Edit))
                    )
                }, {
                    return@contract when (CherrygramConfig.doubleTapAction) {
                        CherrygramConfig.DOUBLE_TAP_ACTION_REACTION -> LocaleController.getString("Reactions", R.string.Reactions)
                        CherrygramConfig.DOUBLE_TAP_ACTION_REPLY -> LocaleController.getString("Reply", R.string.Reply)
                        CherrygramConfig.DOUBLE_TAP_ACTION_SAVE -> LocaleController.getString("CG_ToSaved", R.string.CG_ToSaved)
                        CherrygramConfig.DOUBLE_TAP_ACTION_EDIT -> LocaleController.getString("Edit", R.string.Edit)
                        else -> LocaleController.getString("Disable", R.string.Disable)
                    }
                }) {
                    CherrygramConfig.doubleTapAction = it
                }
            }
            switch {
                title = LocaleController.getString("CP_HideKbdOnScroll", R.string.CP_HideKbdOnScroll)

                contract({
                    return@contract CherrygramConfig.hideKeyboardOnScroll
                }) {
                    CherrygramConfig.hideKeyboardOnScroll = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
            switch {
                title = LocaleController.getString("CP_DisableSwipeToNext", R.string.CP_DisableSwipeToNext)
                summary = LocaleController.getString("CP_DisableSwipeToNext_Desc", R.string.CP_DisableSwipeToNext_Desc)

                contract({
                    return@contract CherrygramConfig.disableSwipeToNext
                }) {
                    CherrygramConfig.disableSwipeToNext = it
                }
            }
            switch {
                title = LocaleController.getString("CP_HideMuteUnmuteButton", R.string.CP_HideMuteUnmuteButton)

                contract({
                    return@contract CherrygramConfig.hideMuteUnmuteButton
                }) {
                    CherrygramConfig.hideMuteUnmuteButton = it
                }
            }
        }

        category(LocaleController.getString("CP_Slider_RecentEmojisAmplifier", R.string.CP_Slider_RecentEmojisAmplifier)) {
            slider {
                contract = object : TGSLContract {
                    override fun setValue(value: Int) {
                        CherrygramConfig.slider_RecentEmojisAmplifier = value
                    }

                    override fun getPreferenceValue(): Int {
                        return CherrygramConfig.slider_RecentEmojisAmplifier
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

        category(LocaleController.getString("CP_Slider_RecentStickersAmplifier", R.string.CP_Slider_RecentStickersAmplifier)) {
            slider {
                contract = object : TGSLContract {
                    override fun setValue(value: Int) {
                        CherrygramConfig.slider_RecentStickersAmplifier = value
                    }

                    override fun getPreferenceValue(): Int {
                        return CherrygramConfig.slider_RecentStickersAmplifier
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

        category(LocaleController.getString("AS_Header_Record", R.string.CP_Header_Record)) {
            switch {
                title = LocaleController.getString("CP_VoiceEnhancements", R.string.CP_VoiceEnhancements)
                summary = LocaleController.getString("CP_VoiceEnhancements_Desc", R.string.CP_VoiceEnhancements_Desc)

                contract({
                    return@contract CherrygramConfig.voicesAgc
                }) {
                    CherrygramConfig.voicesAgc = it
                }
            }
            switch {
                title = LocaleController.getString("CP_PlayVideo", R.string.CP_PlayVideo)
                summary = LocaleController.getString("CP_PlayVideo_Desc", R.string.CP_PlayVideo_Desc)

                contract({
                    return@contract CherrygramConfig.playVideoOnVolume
                }) {
                    CherrygramConfig.playVideoOnVolume = it
                }
            }
            switch {
                title = LocaleController.getString("CP_AutoPauseVideo", R.string.CP_AutoPauseVideo)
                summary = LocaleController.getString("CP_AutoPauseVideo_Desc", R.string.CP_AutoPauseVideo_Desc)

                contract({
                    return@contract CherrygramConfig.autoPauseVideo
                }) {
                    CherrygramConfig.autoPauseVideo = it
                }
            }
            switch {
                title = LocaleController.getString("CP_DisableVibration", R.string.CP_DisableVibration)

                contract({
                    return@contract CherrygramConfig.disableVibration
                }) {
                    CherrygramConfig.disableVibration = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
            switch {
                title = LocaleController.getString("CP_Proximity", R.string.CP_Proximity)
                summary = LocaleController.getString("CP_Proximity_Desc", R.string.CP_Proximity_Desc)

                contract({
                    return@contract CherrygramConfig.enableProximity
                }) {
                    CherrygramConfig.enableProximity = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
        }

        category(LocaleController.getString("AS_Header_Notification", R.string.CP_Header_Notification)) {
            list {
                title = LocaleController.getString("CP_NotificationSound", R.string.CP_NotificationSound)

                contract({
                    return@contract listOf(
                        Pair(CherrygramConfig.NOTIF_SOUND_DISABLE, LocaleController.getString("Disable", R.string.Disable)),
                        Pair(CherrygramConfig.NOTIF_SOUND_DEFAULT, LocaleController.getString("Default", R.string.Default)),
                        Pair(CherrygramConfig.NOTIF_SOUND_IOS, LocaleController.getString("CP_IOSSound", R.string.CP_IOSSound))
                    )
                }, {
                    return@contract when (CherrygramConfig.notificationSound) {
                        CherrygramConfig.NOTIF_SOUND_DEFAULT -> LocaleController.getString("Default", R.string.Default)
                        CherrygramConfig.NOTIF_SOUND_IOS -> LocaleController.getString("CP_IOSSound", R.string.CP_IOSSound)
                        else -> LocaleController.getString("Disable", R.string.Disable)
                    }
                }) {
                    CherrygramConfig.notificationSound = it

                    var tone = 0
                    try {
                        if (CherrygramConfig.notificationSound == 1) {
                            tone = R.raw.sound_in
                        } else if (CherrygramConfig.notificationSound == 2) {
                            tone = R.raw.sound_in_ios
                        }
                        val mp: MediaPlayer = MediaPlayer.create(bf.context, tone)
                        mp.start()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
            list {
                title = LocaleController.getString("CP_VibrateInChats", R.string.CP_VibrateInChats)

                contract({
                    return@contract listOf(
                        Pair(CherrygramConfig.VIBRATION_DISABLE, LocaleController.getString("Disable", R.string.Disable)),
                        Pair(CherrygramConfig.VIBRATION_CLICK, "1"),
                        Pair(CherrygramConfig.VIBRATION_WAVE_FORM, "2"),
                        Pair(CherrygramConfig.VIBRATION_KEYBOARD_TAP, "3"),
                        Pair(CherrygramConfig.VIBRATION_LONG, "4")
                    )
                }, {
                    return@contract when (CherrygramConfig.vibrateInChats) {
                        CherrygramConfig.VIBRATION_CLICK -> "1"
                        CherrygramConfig.VIBRATION_WAVE_FORM -> "2"
                        CherrygramConfig.VIBRATION_KEYBOARD_TAP -> "3"
                        CherrygramConfig.VIBRATION_LONG -> "4"
                        else -> LocaleController.getString("Disable", R.string.Disable)
                    }
                }) {
                    CherrygramConfig.vibrateInChats = it

                    try {
                        when (CherrygramConfig.vibrateInChats) {
                            CherrygramConfig.VIBRATION_CLICK -> {
                                VibrateUtil.makeClickVibration()
                            }
                            CherrygramConfig.VIBRATION_WAVE_FORM -> {
                                VibrateUtil.makeWaveVibration()
                            }
                            CherrygramConfig.VIBRATION_KEYBOARD_TAP -> {
                                VibrateUtil.vibrate(HapticFeedbackConstants.KEYBOARD_TAP.toLong())
                            }
                            CherrygramConfig.VIBRATION_LONG -> {
                                VibrateUtil.vibrate()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            }
            hint(LocaleController.getString("CP_VibrateInChats_Desc", R.string.CP_VibrateInChats_Desc))

            switch {
                title = LocaleController.getString("CP_SilenceNonContacts", R.string.CP_SilenceNonContacts)
                summary = LocaleController.getString("CP_SilenceNonContacts_Desc", R.string.CP_SilenceNonContacts_Desc)


                contract({
                    return@contract CherrygramConfig.silenceNonContacts
                }) {
                    CherrygramConfig.silenceNonContacts = it
                }
            }
        }
    }
}
