/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.core.configs

import android.app.Activity
import android.content.SharedPreferences
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.SharedConfig
import uz.unnarsx.cherrygram.chats.helpers.StickersHelper
import uz.unnarsx.cherrygram.preferences.boolean
import uz.unnarsx.cherrygram.preferences.int
import uz.unnarsx.cherrygram.preferences.string

object CherrygramChatsConfig: CoroutineScope by CoroutineScope(
    context = SupervisorJob() + Dispatchers.Main.immediate
) {

    private val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)

    fun putBoolean(key: String, value: Boolean) {
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    /** Stickers start **/
    /** Stickers finish **/

    /** Chats start **/
    /** Chat shortcuts start **/
    var shortcut_JumpToBegin by sharedPreferences.boolean("CP_Shortcut_JumpToBegin", true)
    fun toggleShortcutJumpToBegin() {
        shortcut_JumpToBegin = !shortcut_JumpToBegin
        putBoolean("CP_Shortcut_JumpToBegin", shortcut_JumpToBegin)
    }

    var shortcut_DeleteAll by sharedPreferences.boolean("CP_Shortcut_DeleteAll", true)
    fun toggleShortcutDeleteAll() {
        shortcut_DeleteAll = !shortcut_DeleteAll
        putBoolean("CP_Shortcut_DeleteAll", shortcut_DeleteAll)
    }

    var shortcut_SavedMessages by sharedPreferences.boolean("CP_Shortcut_SavedMessages", false)
    fun toggleShortcutSavedMessages() {
        shortcut_SavedMessages = !shortcut_SavedMessages
        putBoolean("CP_Shortcut_SavedMessages", shortcut_SavedMessages)
    }

    var shortcut_Blur by sharedPreferences.boolean("CP_Shortcut_Blur", false)
    fun toggleShortcutBlur() {
        shortcut_Blur = !shortcut_Blur
        putBoolean("CP_Shortcut_Blur", shortcut_Blur)
    }

    var shortcut_Browser by sharedPreferences.boolean("CP_Shortcut_Browser", false)
    fun toggleShortcutBrowser() {
        shortcut_Browser = !shortcut_Browser
        putBoolean("CP_Shortcut_Browser", shortcut_Browser)
    }
    /** Chat shortcuts finish **/

    /** Admin shortcuts start **/
    var admins_Reactions by sharedPreferences.boolean("CP_Admins_Reactions", false)
    fun toggleAdminsReactions() {
        admins_Reactions = !admins_Reactions
        putBoolean("CP_Admins_Reactions", admins_Reactions)
    }

    var admins_Permissions by sharedPreferences.boolean("CP_Admins_Permissions", false)
    fun toggleAdminsPermissions() {
        admins_Permissions = !admins_Permissions
        putBoolean("CP_Admins_Permissions", admins_Permissions)
    }

    var admins_Administrators by sharedPreferences.boolean("CP_Admins_Administrators", false)
    fun toggleAdminsAdministrators() {
        admins_Administrators = !admins_Administrators
        putBoolean("CP_Admins_Administrators", admins_Administrators)
    }

    var admins_Members by sharedPreferences.boolean("CP_Admins_Members", false)
    fun toggleAdminsMembers() {
        admins_Members = !admins_Members
        putBoolean("CP_Admins_Members", admins_Members)
    }

    var admins_Statistics by sharedPreferences.boolean("CP_Admins_Statistics", false)
    fun toggleAdminsStatistics() {
        admins_Statistics = !admins_Statistics
        putBoolean("CP_Admins_Statistics", admins_Statistics)
    }

    var admins_RecentActions by sharedPreferences.boolean("CP_Admins_RecentActions", false)
    fun toggleAdminsRecentActions() {
        admins_RecentActions = !admins_RecentActions
        putBoolean("CP_Admins_RecentActions", admins_RecentActions)
    }
    /** Admin shortcuts finish **/

    var centerChatTitle by sharedPreferences.boolean("AP_CenterChatTitle", true)
    var unreadBadgeOnBackButton by sharedPreferences.boolean("CP_UnreadBadgeOnBackButton", false)
    var confirmCalls by sharedPreferences.boolean("CP_ConfirmCalls", true)
    var hideKeyboardOnScroll by sharedPreferences.boolean("CP_HideKbdOnScroll", false)
    var disableSwipeToNext by sharedPreferences.boolean("CP_DisableSwipeToNext", false)
    var hideMuteUnmuteButton by sharedPreferences.boolean("CP_HideMuteUnmuteButton", false)

    /** Gemini AI start **/
    var geminiApiKey by sharedPreferences.string("CP_GeminiApiKey", "yourKey12345")
    var geminiModelName by sharedPreferences.string("CP_GeminiModelName", "gemini-1.5-flash")
    /** Gemini AI finish **/

    var slider_RecentEmojisAmplifier by sharedPreferences.int("CP_Slider_RecentEmojisAmplifier", 45)
    var slider_RecentStickersAmplifier by sharedPreferences.int("CP_Slider_RecentStickersAmplifier", 20)
    /** Chats finish **/

    /** Messages start **/
    /** Direct share start **/
    var shareDrawStoryButton by sharedPreferences.boolean("CP_ShareDrawStoryButton", true)
    fun toggleShareDrawStoryButton() {
        shareDrawStoryButton = !shareDrawStoryButton
        putBoolean("CP_ShareDrawStoryButton", shareDrawStoryButton)
    }

    var usersDrawShareButton by sharedPreferences.boolean("CP_UsersDrawShareButton", false)
    fun toggleUsersDrawShareButton() {
        usersDrawShareButton = !usersDrawShareButton
        putBoolean("CP_UsersDrawShareButton", usersDrawShareButton)
    }

    var supergroupsDrawShareButton by sharedPreferences.boolean("CP_SupergroupsDrawShareButton", false)
    fun toggleSupergroupsDrawShareButton() {
        supergroupsDrawShareButton = !supergroupsDrawShareButton
        putBoolean("CP_SupergroupsDrawShareButton", supergroupsDrawShareButton)
    }

    var channelsDrawShareButton by sharedPreferences.boolean("CP_ChannelsDrawShareButton", true)
    fun toggleChannelsDrawShareButton() {
        channelsDrawShareButton = !channelsDrawShareButton
        putBoolean("CP_ChannelsDrawShareButton", channelsDrawShareButton)
    }

    var botsDrawShareButton by sharedPreferences.boolean("CP_BotsDrawShareButton", true)
    fun toggleBotsDrawShareButton() {
        botsDrawShareButton = !botsDrawShareButton
        putBoolean("CP_BotsDrawShareButton", botsDrawShareButton)
    }

    var stickersDrawShareButton by sharedPreferences.boolean("CP_StickersDrawShareButton", false)
    fun toggleStickersDrawShareButton() {
        stickersDrawShareButton = !stickersDrawShareButton
        putBoolean("CP_StickersDrawShareButton", stickersDrawShareButton)
    }
    /** Direct share finish **/

    /** Message menu start **/
    var showSaveForNotifications by sharedPreferences.boolean("CP_ShowSaveForNotifications", false)
    fun toggleShowSaveForNotifications() {
        showSaveForNotifications = !showSaveForNotifications
        putBoolean("CP_ShowSaveForNotifications", showSaveForNotifications)
    }

    var showReply by sharedPreferences.boolean("CP_ShowReply", true)
    fun toggleShowReply() {
        showReply = !showReply
        putBoolean("CP_ShowReply", showReply)
    }

    var showGeminiReply by sharedPreferences.boolean("CP_ShowGeminiReply", true)
    fun toggleShowGeminiReply() {
        showGeminiReply = !showGeminiReply
        putBoolean("CP_ShowGeminiReply", showGeminiReply)
    }

    var showCopyPhoto by sharedPreferences.boolean("CP_ShowCopyPhoto", true)
    fun toggleShowCopyPhoto() {
        showCopyPhoto = !showCopyPhoto
        putBoolean("CP_ShowCopyPhoto", showCopyPhoto)
    }

    var showCopyPhotoAsSticker by sharedPreferences.boolean("CP_ShowCopyPhotoAsSticker", true)
    fun toggleShowCopyPhotoAsSticker() {
        showCopyPhotoAsSticker = !showCopyPhotoAsSticker
        putBoolean("CP_ShowCopyPhotoAsSticker", showCopyPhotoAsSticker)
    }

    var showClearFromCache by sharedPreferences.boolean("CP_ShowClearFromCache", true)
    fun toggleShowClearFromCache() {
        showClearFromCache = !showClearFromCache
        putBoolean("CP_ShowClearFromCache", showClearFromCache)
    }

    var showForward by sharedPreferences.boolean("CP_ShowForward", false)
    fun toggleShowForward() {
        showForward = !showForward
        putBoolean("CP_ShowForward", showForward)
    }

    var showForwardWoAuthorship by sharedPreferences.boolean("CP_ShowForward_WO_Authorship", false)
    fun toggleShowForwardWoAuthorship() {
        showForwardWoAuthorship = !showForwardWoAuthorship
        putBoolean("CP_ShowForward_WO_Authorship", showForwardWoAuthorship)
    }

    var showViewHistory by sharedPreferences.boolean("CP_ShowViewHistory", true)
    fun toggleShowViewHistory() {
        showViewHistory = !showViewHistory
        putBoolean("CP_ShowViewHistory", showViewHistory)
    }

    var showSaveMessage by sharedPreferences.boolean("CP_ShowSaveMessage", false)
    fun toggleShowSaveMessage() {
        showSaveMessage = !showSaveMessage
        putBoolean("CP_ShowSaveMessage", showSaveMessage)
    }

    var showReport by sharedPreferences.boolean("CP_ShowReport", true)
    fun toggleShowReport() {
        showReport = !showReport
        putBoolean("CP_ShowReport", showReport)
    }

    var showJSON by sharedPreferences.boolean("CP_ShowJSON", false)
    fun toggleShowJSON() {
        showJSON = !showJSON
        putBoolean("CP_ShowJSON", showJSON)
    }

    var jacksonJSON_Provider by sharedPreferences.boolean("CP_JacksonJSON_Provider", Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
    /** Message menu finish **/

    /** Messages size start **/
    var slider_mediaAmplifier by sharedPreferences.int("CP_Slider_MediaAmplifier", 100)
    var slider_stickerAmplifier by sharedPreferences.int("CP_Slider_StickerAmplifier", 100)
    var slider_gifsAmplifier by sharedPreferences.int("CP_Slider_GifsAmplifier", 100)
    /** Messages size finish **/

    var hideStickerTime by sharedPreferences.boolean("CP_TimeOnStick", false)
    var deleteForAll by sharedPreferences.boolean("CP_DeleteForAll", false)
    var msgForwardDate by sharedPreferences.boolean("CP_ForwardMsgDate", true)
    var showPencilIcon by sharedPreferences.boolean("AP_PencilIcon", true)

    const val LEFT_BUTTON_FORWARD_WO_AUTHORSHIP = 0
    const val LEFT_BUTTON_REPLY = 1
    const val LEFT_BUTTON_SAVE_MESSAGE= 2
    const val LEFT_BUTTON_DIRECT_SHARE = 3
    var leftBottomButton by sharedPreferences.int("CP_LeftBottomButtonAction", LEFT_BUTTON_FORWARD_WO_AUTHORSHIP)

    const val DOUBLE_TAP_ACTION_NONE = 0
    const val DOUBLE_TAP_ACTION_REACTION = 1
    const val DOUBLE_TAP_ACTION_REPLY = 2
    const val DOUBLE_TAP_ACTION_SAVE = 3
    const val DOUBLE_TAP_ACTION_EDIT = 4
    const val DOUBLE_TAP_ACTION_TRANSLATE = 5
    var doubleTapAction by sharedPreferences.int("CP_DoubleTapAction", DOUBLE_TAP_ACTION_REACTION)

    const val MESSAGE_SLIDE_ACTION_REPLY = 0
    const val MESSAGE_SLIDE_ACTION_SAVE = 1
    const val MESSAGE_SLIDE_ACTION_TRANSLATE = 2
    const val MESSAGE_SLIDE_ACTION_DIRECT_SHARE = 3
    var messageSlideAction by sharedPreferences.int("CP_MessageSlideAction", MESSAGE_SLIDE_ACTION_REPLY)
    /** Messages finish **/

    /** Media start **/
    var largePhotos by sharedPreferences.boolean("CP_LargePhotos", SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_AVERAGE)
    var spoilersOnMedia by sharedPreferences.boolean("CP_SpoilersOnMedia", true)
    var voicesAgc by sharedPreferences.boolean("CP_VoicesAGC", false)
    var playVideoOnVolume by sharedPreferences.boolean("CP_PlayVideo", false)
    var autoPauseVideo by sharedPreferences.boolean("CP_AutoPauseVideo", false)
    var disableVibration by sharedPreferences.boolean("CP_DisableVibration", false)
    var videoSeekDuration by sharedPreferences.int("CP_VideoSeekDuration", 10)
    /** Media finish **/

    /** Notifications start **/
    const val NOTIF_SOUND_DISABLE = 0
    const val NOTIF_SOUND_DEFAULT = 1
    const val NOTIF_SOUND_IOS = 2
    var notificationSound by sharedPreferences.int("CP_Notification_Sound", NOTIF_SOUND_DEFAULT)

    const val VIBRATION_DISABLE = 0
    const val VIBRATION_CLICK = 1
    const val VIBRATION_WAVE_FORM = 2
    const val VIBRATION_KEYBOARD_TAP = 3
    const val VIBRATION_LONG = 4
    var vibrateInChats by sharedPreferences.int("CP_VibrationInChats", VIBRATION_DISABLE)

    var silenceNonContacts by sharedPreferences.boolean("CP_SilenceNonContacts", false)
    /** Notifications finish **/

    /** Misc start **/
    var forwardNoAuthorship by sharedPreferences.boolean("CG_ForwardNoAuthorship", false)
    var forwardWithoutCaptions by sharedPreferences.boolean("CG_ForwardWithoutCaptions", false)
    var forwardNotify by sharedPreferences.boolean("CG_ForwardNotify", true)
    var noAuthorship by sharedPreferences.boolean("CG_NoAuthorship", false)
    var gifSpoilers by sharedPreferences.boolean("CG_GifSpoiler", false)

    /** Translator start **/
    var translationKeyboardTarget by sharedPreferences.string("translationKeyboardTarget", "app")
    var translationTarget by sharedPreferences.string("translationTarget", "app")
    /** Translator finish **/

    /** Search Filter start **/
    const val FILTER_NONE = 0
    const val FILTER_PHOTOS = 1
    const val FILTER_VIDEOS = 2
    const val FILTER_VOICE_MESSAGES = 3
    const val FILTER_VIDEO_MESSAGES = 4
    const val FILTER_FILES = 5
    const val FILTER_MUSIC = 6
    const val FILTER_GIFS = 7
    const val FILTER_GEO = 8
    const val FILTER_CONTACTS = 9
    const val FILTER_MENTIONS = 10
    var messagesSearchFilter by sharedPreferences.int("messagesSearchFilter", FILTER_NONE)
    /** Search Filter finish **/

    var unarchiveOnSwipe by sharedPreferences.boolean("CG_UnarchiveOnSwipe", false)
    fun toggleUnarchiveOnSwipe() {
        unarchiveOnSwipe = !unarchiveOnSwipe
        putBoolean("CG_UnarchiveOnSwipe", unarchiveOnSwipe)
    }
    /** Misc finish **/

    init {
        launch {
            if (!CherrygramCoreConfig.isPlayStoreBuild()) StickersHelper.getStickerSetIDs()
            StickersHelper.copyStickerFromAssets()
        }
    }
    
}