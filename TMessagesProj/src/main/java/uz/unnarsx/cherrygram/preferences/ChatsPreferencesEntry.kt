/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.preferences

import android.media.MediaPlayer
import android.view.HapticFeedbackConstants
import androidx.core.util.Pair
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.BaseFragment
import uz.unnarsx.cherrygram.chats.CGMessageMenuInjector
import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig
import uz.unnarsx.cherrygram.core.VibrateUtil
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper
import uz.unnarsx.cherrygram.core.helpers.FirebaseAnalyticsHelper
import uz.unnarsx.cherrygram.helpers.ui.PopupHelper
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

object ChatsPreferencesEntry : BasePreferencesEntry {
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(getString(R.string.CP_Header_Chats)) {
        category(getString(R.string.CP_Header_Chats)) {
            textIcon {
                title = getString(R.string.CP_ChatMenuShortcuts)
                icon = R.drawable.msg_list
                listener = TGKitTextIconRow.TGTIListener {
                    showChatMenuItemsConfigurator(bf)
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

        category(getString(R.string.CP_HideKbdOnScroll)) {
            slider {
                contract = object : TGSLContract {
                    override fun setValue(value: Int) {
                        CherrygramChatsConfig.hideKeyboardOnScrollIntensity = value
                    }

                    override fun getPreferenceValue(): Int {
                        return CherrygramChatsConfig.hideKeyboardOnScrollIntensity
                    }

                    override fun getMin(): Int {
                        return 0
                    }

                    override fun getMax(): Int {
                        return 10
                    }
                }
            }
        }

        category(null) {
            textIcon {
                title = getString(R.string.CP_GeminiAI_Header)
                icon = R.drawable.magic_stick_solar
                listener = TGKitTextIconRow.TGTIListener {
                    GeminiPreferencesBottomSheet.showAlert(bf)
                }
            }
        }

        category(getString(R.string.CP_Header_Messages)) {
            textIcon {
                title = getString(R.string.DirectShare)
                icon = R.drawable.msg_share
                listener = TGKitTextIconRow.TGTIListener {
                    showDirectShareConfigurator(bf)
                }
                divider = true
            }
            textIcon {
                title = getString(R.string.CP_MessageMenu)
                icon = R.drawable.msg_list
                listener = TGKitTextIconRow.TGTIListener {
                    CGMessageMenuInjector.showMessageMenuItemsConfigurator(bf)
                }
                divider = true
            }
            textIcon {
                title = getString(R.string.CP_Messages_Size)
                icon = R.drawable.msg_photo_settings
                listener = TGKitTextIconRow.TGTIListener {
                    AlertDialogSwitchers.showMessageSize(bf)
                }
                divider = true
            }
            switch {
                title = getString(R.string.CP_TimeOnStick)

                contract({
                    return@contract CherrygramChatsConfig.hideStickerTime
                }) {
                    CherrygramChatsConfig.hideStickerTime = it
                }
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
                description = getString(R.string.CP_SpoilersOnMedia_Desc)

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

        FirebaseAnalyticsHelper.trackEventWithEmptyBundle("chats_preferences_screen")
    }

    private fun showDirectShareConfigurator(fragment: BaseFragment) {
        val menuItems = listOf(
            MenuItemConfig(
                getString(R.string.RepostToStory),
                R.drawable.large_repost_story,
                { CherrygramChatsConfig.shareDrawStoryButton },
                { CherrygramChatsConfig.shareDrawStoryButton = !CherrygramChatsConfig.shareDrawStoryButton },
                true
            ),
            MenuItemConfig(
                getString(R.string.FilterChats),
                0,
                { CherrygramChatsConfig.usersDrawShareButton },
                { CherrygramChatsConfig.usersDrawShareButton = !CherrygramChatsConfig.usersDrawShareButton }
            ),
            MenuItemConfig(
                getString(R.string.FilterGroups),
                0,
                { CherrygramChatsConfig.supergroupsDrawShareButton },
                { CherrygramChatsConfig.supergroupsDrawShareButton = !CherrygramChatsConfig.supergroupsDrawShareButton }
            ),
            MenuItemConfig(
                getString(R.string.FilterChannels),
                0,
                { CherrygramChatsConfig.channelsDrawShareButton },
                { CherrygramChatsConfig.channelsDrawShareButton = !CherrygramChatsConfig.channelsDrawShareButton }
            ),
            MenuItemConfig(
                getString(R.string.FilterBots),
                0,
                { CherrygramChatsConfig.botsDrawShareButton },
                { CherrygramChatsConfig.botsDrawShareButton = !CherrygramChatsConfig.botsDrawShareButton }
            ),
            MenuItemConfig(
                getString(R.string.StickersName),
                0,
                { CherrygramChatsConfig.stickersDrawShareButton },
                { CherrygramChatsConfig.stickersDrawShareButton = !CherrygramChatsConfig.stickersDrawShareButton }
            )
        )

        val prefTitle = ArrayList<String>()
        val prefIcon = ArrayList<Int>()
        val prefCheck = ArrayList<Boolean>()
        val prefDivider = ArrayList<Boolean>()
        val clickListener = ArrayList<Runnable>()

        for (item in menuItems) {
            prefTitle.add(item.titleRes)
            prefIcon.add(item.iconRes)
            prefCheck.add(item.isChecked())
            prefDivider.add(item.divider)
            clickListener.add(Runnable { item.toggle() })
        }

        PopupHelper.showSwitchAlert(
            getString(R.string.DirectShare),
            fragment,
            prefTitle,
            prefIcon,
            prefCheck,
            null,
            prefDivider,
            clickListener,
            null
        )

    }

    fun showChatMenuItemsConfigurator(fragment: BaseFragment) {
        val menuItems = listOf(
            MenuItemConfig(
                getString(R.string.CG_JumpToBeginning),
                R.drawable.ic_upward,
                { CherrygramChatsConfig.shortcut_JumpToBegin },
                { CherrygramChatsConfig.shortcut_JumpToBegin = !CherrygramChatsConfig.shortcut_JumpToBegin }
            ),
            MenuItemConfig(
                getString(R.string.CG_DeleteAllFromSelf),
                R.drawable.msg_delete,
                { CherrygramChatsConfig.shortcut_DeleteAll },
                { CherrygramChatsConfig.shortcut_DeleteAll = !CherrygramChatsConfig.shortcut_DeleteAll }
            ),
            MenuItemConfig(
                getString(R.string.SavedMessages),
                R.drawable.msg_saved,
                { CherrygramChatsConfig.shortcut_SavedMessages },
                { CherrygramChatsConfig.shortcut_SavedMessages = !CherrygramChatsConfig.shortcut_SavedMessages }
            ),
            MenuItemConfig(
                getString(R.string.BlurInChat),
                R.drawable.msg_theme,
                { CherrygramChatsConfig.shortcut_Blur },
                { CherrygramChatsConfig.shortcut_Blur = !CherrygramChatsConfig.shortcut_Blur }
            ),
            MenuItemConfig(
                "Telegram Browser",
                R.drawable.msg_language,
                { CherrygramChatsConfig.shortcut_Browser },
                { CherrygramChatsConfig.shortcut_Browser = !CherrygramChatsConfig.shortcut_Browser },
                true
            ),
            MenuItemConfig(
                getString(R.string.CP_AdminActions),
                R.drawable.msg_admins,
                { false },
                { showChatAdminItemsConfigurator(fragment) },
                isCheckInvisible = true
            ),
        )

        val prefTitle = ArrayList<String>()
        val prefIcon = ArrayList<Int>()
        val prefCheck = ArrayList<Boolean>()
        val prefCheckInvisible = ArrayList<Boolean>()
        val prefDivider = ArrayList<Boolean>()
        val clickListener = ArrayList<Runnable>()

        for (item in menuItems) {
            prefTitle.add(item.titleRes)
            prefIcon.add(item.iconRes)
            prefCheck.add(item.isChecked())
            prefCheckInvisible.add(item.isCheckInvisible)
            prefDivider.add(item.divider)
            clickListener.add(Runnable { item.toggle() })
        }

        PopupHelper.showSwitchAlert(
            getString(R.string.CP_ChatMenuShortcuts),
            fragment,
            prefTitle,
            prefIcon,
            prefCheck,
            prefCheckInvisible,
            prefDivider,
            clickListener,
            null
        )
    }

    private fun showChatAdminItemsConfigurator(fragment: BaseFragment) {
        val menuItems = listOf(
            MenuItemConfig(
                getString(R.string.Reactions),
                R.drawable.msg_reactions2,
                { CherrygramChatsConfig.admins_Reactions },
                { CherrygramChatsConfig.admins_Reactions = !CherrygramChatsConfig.admins_Reactions }
            ),
            MenuItemConfig(
                getString(R.string.ChannelPermissions),
                R.drawable.msg_permissions,
                { CherrygramChatsConfig.admins_Permissions },
                { CherrygramChatsConfig.admins_Permissions = !CherrygramChatsConfig.admins_Permissions }
            ),
            MenuItemConfig(
                getString(R.string.ChannelAdministrators),
                R.drawable.msg_admins,
                { CherrygramChatsConfig.admins_Administrators },
                { CherrygramChatsConfig.admins_Administrators = !CherrygramChatsConfig.admins_Administrators }
            ),
            MenuItemConfig(
                getString(R.string.ChannelMembers),
                R.drawable.msg_groups,
                { CherrygramChatsConfig.admins_Members },
                { CherrygramChatsConfig.admins_Members = !CherrygramChatsConfig.admins_Members }
            ),
            MenuItemConfig(
                getString(R.string.StatisticsAndBoosts),
                R.drawable.msg_stats,
                { CherrygramChatsConfig.admins_Statistics },
                { CherrygramChatsConfig.admins_Statistics = !CherrygramChatsConfig.admins_Statistics }
            ),
            MenuItemConfig(
                getString(R.string.EventLog),
                R.drawable.msg_log,
                { CherrygramChatsConfig.admins_RecentActions },
                { CherrygramChatsConfig.admins_RecentActions = !CherrygramChatsConfig.admins_RecentActions }
            ),
        )

        val prefTitle = ArrayList<String>()
        val prefIcon = ArrayList<Int>()
        val prefCheck = ArrayList<Boolean>()
        val prefCheckInvisible = ArrayList<Boolean>()
        val prefDivider = ArrayList<Boolean>()
        val clickListener = ArrayList<Runnable>()

        for (item in menuItems) {
            prefTitle.add(item.titleRes)
            prefIcon.add(item.iconRes)
            prefCheck.add(item.isChecked())
            prefCheckInvisible.add(item.isCheckInvisible)
            prefDivider.add(item.divider)
            clickListener.add(Runnable { item.toggle() })
        }

        PopupHelper.showSwitchAlert(
            getString(R.string.CP_AdminActions),
            fragment,
            prefTitle,
            prefIcon,
            prefCheck,
            prefCheckInvisible,
            prefDivider,
            clickListener,
            null
        )
    }

    data class MenuItemConfig(
        val titleRes: String,
        val iconRes: Int,
        val isChecked: () -> Boolean,
        val toggle: () -> Unit,
        val divider: Boolean = false,
        val isCheckInvisible: Boolean = false
    )

}
