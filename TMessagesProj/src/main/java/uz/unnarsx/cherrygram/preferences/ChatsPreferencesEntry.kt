package uz.unnarsx.cherrygram.preferences

import androidx.core.util.Pair
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ActionBar.Theme
//import uz.unnarsx.cherrygram.CGFeatureHooks
import uz.unnarsx.cherrygram.CherrygramConfig
import uz.unnarsx.cherrygram.preferences.ktx.*
import uz.unnarsx.tgkit.preference.types.TGKitSliderPreference.TGSLContract

class ChatsPreferencesEntry : BasePreferencesEntry {
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(LocaleController.getString("AS_Header_Chats", R.string.CP_Header_Chats)) {
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
            switch {
                title = LocaleController.getString("CP_TimeOnStick", R.string.CP_TimeOnStick)

                contract({
                    return@contract CherrygramConfig.hideStickerTime
                }) {
                    CherrygramConfig.hideStickerTime = it
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
                }
            }
            switch {
                title = LocaleController.getString("CP_DoubleTapReact", R.string.CP_DoubleTapReact)

                contract({
                    return@contract CherrygramConfig.disableDoubleTabReact
                }) {
                    CherrygramConfig.disableDoubleTabReact = it
                }
            }
            switch {
                title = LocaleController.getString("CP_DisableReactionAnim", R.string.CP_DisableReactionAnim)
                summary = LocaleController.getString("CP_DisableReactionAnim_Desc", R.string.CP_DisableReactionAnim_Desc)

                contract({
                    return@contract CherrygramConfig.disableReactionAnim
                }) {
                    CherrygramConfig.disableReactionAnim = it
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
                title = LocaleController.getString("CP_HideKbdOnScroll", R.string.CP_HideKbdOnScroll)

                contract({
                    return@contract CherrygramConfig.hideKeyboardOnScroll
                }) {
                    CherrygramConfig.hideKeyboardOnScroll = it
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

        category(LocaleController.getString("AS_Filters_Header", R.string.CP_Filters_Header)) {
            switch {
                title = LocaleController.getString("CP_NewTabs_RemoveAllChats", R.string.CP_NewTabs_RemoveAllChats)
                /*summary = LocaleController.getString("CP_NewTabs_RemoveAllChats_Desc", R.string.CP_NewTabs_RemoveAllChats_Desc)*/

                contract({
                    return@contract CherrygramConfig.newTabs_hideAllChats
                }) {
                    CherrygramConfig.newTabs_hideAllChats = it
                }
            }
            switch {
                title = LocaleController.getString("CP_NewTabs_NoCounter", R.string.CP_NewTabs_NoCounter)
                summary = LocaleController.getString("CP_NewTabs_NoCounter_Desc", R.string.CP_NewTabs_NoCounter_Desc)

                contract({
                    return@contract CherrygramConfig.newTabs_noUnread
                }) {
                    CherrygramConfig.newTabs_noUnread = it
                }
            }
            switch {
                title = LocaleController.getString("CP_ShowTabsOnForward", R.string.CP_ShowTabsOnForward)

                contract({
                    return@contract CherrygramConfig.showTabsOnForward
                }) {
                    CherrygramConfig.showTabsOnForward = it
                }
            }

            list {
                title = LocaleController.getString("CP_TabIconMode_Title", R.string.CP_TabIconMode_Title)

                contract({
                    return@contract listOf(
                            Pair(0, LocaleController.getString("CP_TabIconMode_Disabled", R.string.CP_TabIconMode_Disabled)),
                            Pair(1, LocaleController.getString("CP_TabIconMode_Append", R.string.CP_TabIconMode_Append)),
                            Pair(2, LocaleController.getString("CP_TabIconMode_Replace", R.string.CP_TabIconMode_Replace))
                    )
                }, {
                    return@contract when (CherrygramConfig.newTabs_iconsV2_mode) {
                        1 -> LocaleController.getString("CP_TabIconMode_Append", R.string.CP_TabIconMode_Append)
                        2 -> LocaleController.getString("CP_TabIconMode_Replace", R.string.CP_TabIconMode_Replace)
                        else -> LocaleController.getString("CP_TabIconMode_Disabled", R.string.CP_TabIconMode_Disabled)
                    }
                }) {
                    CherrygramConfig.newTabs_iconsV2_mode = it
                    bf.messagesController.loadRemoteFilters(true)
                }
            }
        }

        category(LocaleController.getString("AS_Header_Record", R.string.CP_Header_Record)) {
            switch {
                title = LocaleController.getString("CP_GIFasVideo", R.string.CP_GIFasVideo)

                contract({
                    return@contract CherrygramConfig.playGIFasVideo
                }) {
                    CherrygramConfig.playGIFasVideo = it
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
                title = LocaleController.getString("CP_DisableCam", R.string.CP_DisableCam)
                summary = LocaleController.getString("CP_DisableCam_Desc", R.string.CP_DisableCam_Desc)

                contract({
                    return@contract CherrygramConfig.disableAttachCamera
                }) {
                    CherrygramConfig.disableAttachCamera = it
                }
            }
            switch {
                title = LocaleController.getString("CP_RearCam", R.string.CP_RearCam)
                summary = LocaleController.getString("CP_RearCam_Desc", R.string.CP_RearCam_Desc)

                contract({
                    return@contract CherrygramConfig.rearCam
                }) {
                    CherrygramConfig.rearCam = it
                }
            }
            switch {
                title = LocaleController.getString("CP_Proximity", R.string.CP_Proximity)
                summary = LocaleController.getString("CP_Proximity_Desc", R.string.CP_Proximity_Desc)

                contract({
                    return@contract CherrygramConfig.enableProximity
                }) {
                    CherrygramConfig.enableProximity = it
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
