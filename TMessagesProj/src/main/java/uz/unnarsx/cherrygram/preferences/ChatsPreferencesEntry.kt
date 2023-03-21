package uz.unnarsx.cherrygram.preferences

import android.app.Activity
import android.content.SharedPreferences
import androidx.core.util.Pair
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.Components.BulletinFactory
import uz.unnarsx.cherrygram.CherrygramConfig
import uz.unnarsx.cherrygram.tgkit.preference.*
import uz.unnarsx.cherrygram.tgkit.preference.types.TGKitSliderPreference.TGSLContract

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
                    BulletinFactory.of(bf).createRestartBulletin(
                        R.raw.chats_infotip,
                        LocaleController.getString("CG_RestartToApply", R.string.CG_RestartToApply),
                        LocaleController.getString("BotUnblock", R.string.BotUnblock)
                    ) {
                    }.show()
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

        category(LocaleController.getString("DirectShare", R.string.DirectShare)) {
            switch {
                title = LocaleController.getString("FilterChats", R.string.FilterChats)

                contract({
                    return@contract CherrygramConfig.usersDrawShareButton
                }) {
                    CherrygramConfig.usersDrawShareButton = it
                    bf.parentActivity.recreate()
                }
            }
            /*switch {
                title = "Groups"

                contract({
                    return@contract CherrygramConfig.groupsDrawShareButton
                }) {
                    CherrygramConfig.groupsDrawShareButton = it
                    bf.parentActivity.recreate()
                }
            }*/
            switch {
                title = LocaleController.getString("FilterGroups", R.string.FilterGroups)

                contract({
                    return@contract CherrygramConfig.supergroupsDrawShareButton
                }) {
                    CherrygramConfig.supergroupsDrawShareButton = it
                    bf.parentActivity.recreate()
                }
            }
            switch {
                title = LocaleController.getString("FilterChannels", R.string.FilterChannels)

                contract({
                    return@contract CherrygramConfig.channelsDrawShareButton
                }) {
                    CherrygramConfig.channelsDrawShareButton = it
                    bf.parentActivity.recreate()
                }
            }
            switch {
                title = LocaleController.getString("FilterBots", R.string.FilterBots)

                contract({
                    return@contract CherrygramConfig.botsDrawShareButton
                }) {
                    CherrygramConfig.botsDrawShareButton = it
                    bf.parentActivity.recreate()
                }
            }
            switch {
                title = LocaleController.getString("StickersName", R.string.StickersName)

                contract({
                    return@contract CherrygramConfig.stickersDrawShareButton
                }) {
                    CherrygramConfig.stickersDrawShareButton = it
                    bf.parentActivity.recreate()
                }
            }
        }

        category(LocaleController.getString("AS_Header_Chats", R.string.CP_Header_Chats)) {
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
                title = LocaleController.getString("AS_NoRounding", R.string.CP_NoRounding)
                summary = LocaleController.getString("AS_NoRoundingSummary", R.string.CP_NoRoundingSummary)

                contract({
                    return@contract CherrygramConfig.noRounding
                }) {
                    CherrygramConfig.noRounding = it
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
                title = LocaleController.getString("CP_ConfirmCalls", R.string.CP_ConfirmCalls)

                contract({
                    return@contract CherrygramConfig.confirmCalls
                }) {
                    CherrygramConfig.confirmCalls = it
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
                title = LocaleController.getString("CP_ShowSeconds", R.string.CP_ShowSeconds)
                summary = LocaleController.getString("CP_ShowSeconds_Desc", R.string.CP_ShowSeconds_Desc)

                contract({
                    return@contract CherrygramConfig.showSeconds
                }) {
                    CherrygramConfig.showSeconds = it
                    BulletinFactory.of(bf).createRestartBulletin(
                        R.raw.chats_infotip,
                        LocaleController.getString("CG_RestartToApply", R.string.CG_RestartToApply),
                        LocaleController.getString("BotUnblock", R.string.BotUnblock)
                    ) {
                    }.show()
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
            switch {
                title = LocaleController.getString("CP_HideKbdOnScroll", R.string.CP_HideKbdOnScroll)

                contract({
                    return@contract CherrygramConfig.hideKeyboardOnScroll
                }) {
                    CherrygramConfig.hideKeyboardOnScroll = it
                    BulletinFactory.of(bf).createRestartBulletin(
                        R.raw.chats_infotip,
                        LocaleController.getString("CG_RestartToApply", R.string.CG_RestartToApply),
                        LocaleController.getString("BotUnblock", R.string.BotUnblock)
                    ) {
                    }.show()
                }
            }
            switch {
                title = LocaleController.getString("CP_HideSendAsChannel", R.string.CP_HideSendAsChannel)

                contract({
                    return@contract CherrygramConfig.hideSendAsChannel
                }) {
                    CherrygramConfig.hideSendAsChannel = it
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
                title = LocaleController.getString("CP_AudioFocus", R.string.CP_AudioFocus)
                summary = LocaleController.getString("CP_AudioFocus_Desc", R.string.CP_AudioFocus_Desc)

                contract({
                    return@contract CherrygramConfig.audioFocus
                }) {
                    CherrygramConfig.audioFocus = it
                }
            }
            switch {
                title = LocaleController.getString("CP_DisableVibration", R.string.CP_DisableVibration)

                contract({
                    return@contract CherrygramConfig.disableVibration
                }) {
                    CherrygramConfig.disableVibration = it
                }
            }
            switch {
                title = LocaleController.getString("CP_DisablePhotoTapAction", R.string.CP_DisablePhotoTapAction)
                summary = LocaleController.getString("CP_DisablePhotoTapAction_Desc", R.string.CP_DisablePhotoTapAction_Desc)

                contract({
                    return@contract CherrygramConfig.disablePhotoTapAction
                }) {
                    CherrygramConfig.disablePhotoTapAction = it
                }
            }
            switch {
                title = LocaleController.getString("CP_Proximity", R.string.CP_Proximity)
                summary = LocaleController.getString("CP_Proximity_Desc", R.string.CP_Proximity_Desc)

                contract({
                    return@contract CherrygramConfig.enableProximity
                }) {
                    CherrygramConfig.enableProximity = it
                    BulletinFactory.of(bf).createRestartBulletin(
                        R.raw.chats_infotip,
                        LocaleController.getString("CG_RestartToApply", R.string.CG_RestartToApply),
                        LocaleController.getString("BotUnblock", R.string.BotUnblock)
                    ) {
                    }.show()
                }
            }
        }

        category(LocaleController.getString("AS_Header_Notification", R.string.CP_Header_Notification)) {
            switch {
                title = LocaleController.getString("CP_IOSSound", R.string.CP_IOSSound)
                summary = LocaleController.getString("CP_IOSSound_Desc", R.string.CP_IOSSound_Desc)
                contract({
                    return@contract CherrygramConfig.iosSound
                }) {
                    CherrygramConfig.iosSound = it
                    BulletinFactory.of(bf).createRestartBulletin(
                        R.raw.chats_infotip,
                        LocaleController.getString("CG_RestartToApply", R.string.CG_RestartToApply),
                        LocaleController.getString("BotUnblock", R.string.BotUnblock)
                    ) {
                    }.show()
                }
            }
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
