/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.configs

import android.app.Activity
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.SharedConfig
import uz.unnarsx.cherrygram.helpers.network.StickersManager
import uz.unnarsx.cherrygram.preferences.boolean
import uz.unnarsx.cherrygram.preferences.int
import uz.unnarsx.cherrygram.preferences.long

object CherrygramChatsConfig: CoroutineScope by CoroutineScope(
    context = SupervisorJob() + Dispatchers.Default
) {

    private val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)

    /** Appearance start */
    var centerChatTitle by sharedPreferences.boolean("AP_CenterChatTitle", true)
    var unreadBadgeOnBackButton by sharedPreferences.boolean("CP_UnreadBadgeOnBackButton", false)

    /** Chat shortcuts start */
    var shortcut_JumpToBegin by sharedPreferences.boolean("CP_Shortcut_JumpToBegin", true)
    var shortcut_DeleteAll by sharedPreferences.boolean("CP_Shortcut_DeleteAll", true)
    var shortcut_SavedMessages by sharedPreferences.boolean("CP_Shortcut_SavedMessages", false)
    var shortcut_Browser by sharedPreferences.boolean("CP_Shortcut_Browser", false)
    /** Chat shortcuts finish */

    /** Admin shortcuts start */
    var admins_Reactions by sharedPreferences.boolean("CP_Admins_Reactions", false)
    var admins_Permissions by sharedPreferences.boolean("CP_Admins_Permissions", false)
    var admins_Administrators by sharedPreferences.boolean("CP_Admins_Administrators", false)
    var admins_Members by sharedPreferences.boolean("CP_Admins_Members", false)
    var admins_Statistics by sharedPreferences.boolean("CP_Admins_Statistics", false)
    var admins_RecentActions by sharedPreferences.boolean("CP_Admins_RecentActions", false)
    /** Admin shortcuts finish */

    var customWallpapers by sharedPreferences.boolean("CP_CustomWallpapers", true)
    var drawSnowInChat by sharedPreferences.boolean("AP_DrawSnowInChat", false && SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_AVERAGE)
    var discussInsteadOfMute by sharedPreferences.boolean("CP_DiscussInsteadOfMute", true)
    var hideMuteUnmuteButton by sharedPreferences.boolean("CP_HideMuteUnmuteButton", false)
    var hideSendAsChannel by sharedPreferences.boolean("CP_HideSendAsChannel", false)
    var slider_RecentEmojisAmplifier by sharedPreferences.int("CP_Slider_RecentEmojisAmplifier", 45)
    var slider_RecentStickersAmplifier by sharedPreferences.int("CP_Slider_RecentStickersAmplifier", 20)
    /** Appearance finish */

    var customChatForSavedMessages by sharedPreferences.boolean("CP_CustomChatForSavedMessages", false)
    var hideKeyboardOnScrollIntensity by sharedPreferences.int("CP_HideKeyboardOnScrollIntensity", 5)

    /** Actions start */
    var autoQuoteReplies by sharedPreferences.boolean("CP_AutoQuoteReplies", false)
    var disableSwipeToNext by sharedPreferences.boolean("CP_DisableSwipeToNext", false)
    var disableVibration by sharedPreferences.boolean("CP_DisableVibration", false)
    /** Actions finish */

    /** Media start */
    var largePhotos by sharedPreferences.boolean("CP_LargePhotos", SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_AVERAGE)
    var playVideoOnVolume by sharedPreferences.boolean("CP_PlayVideo", false)
    var autoPauseVideo by sharedPreferences.boolean("CP_AutoPauseVideo", false)
    var videoSeekDuration by sharedPreferences.int("CP_VideoSeekDuration", 10)
    /** Media finish */

    /** Notifications start */
    const val NOTIF_SOUND_DISABLE = 0
    const val NOTIF_SOUND_DEFAULT = 1
    const val NOTIF_SOUND_IOS = 2
    var notificationSound by sharedPreferences.int("CP_Notification_Sound", NOTIF_SOUND_IOS)

    const val VIBRATION_DISABLE = 0
    const val VIBRATION_CLICK = 1
    const val VIBRATION_WAVE_FORM = 2
    const val VIBRATION_KEYBOARD_TAP = 3
    const val VIBRATION_LONG = 4
    var vibrateInChats by sharedPreferences.int("CP_VibrationInChats", VIBRATION_DISABLE)
    /** Notifications finish */

    /** Misc start */
    /** Direct share start */
    var forwardAuthorship by sharedPreferences.boolean("CG_ForwardAuthorship", true)
    var forwardCaptions by sharedPreferences.boolean("CG_ForwardCaptions", true)
    var forwardNotify by sharedPreferences.boolean("CG_ForwardNotify", true)
    /** Direct share finish */

    /** Bottom buttons forward start */
    var noAuthorship by sharedPreferences.boolean("CG_NoAuthorship", false)
    var noCaptions by sharedPreferences.boolean("CG_NoCaptions", false)
    /** Bottom buttons forward finish */

    /** Search Filter start */
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
    /** Search Filter finish */

    var unarchiveOnSwipe by sharedPreferences.boolean("CG_UnarchiveOnSwipe", false)
    var lastStickersCheckTime by sharedPreferences.long("CG_LastStickersCheckTime", 0)
    var sortByUnread by sharedPreferences.boolean("CG_SortByUnread", false)
    /** Misc finish */

    fun init() {
        launch(Dispatchers.IO) {
            StickersManager.startAutoRefresh(ApplicationLoader.applicationContext)
            StickersManager.copyStickerFromAssets()
        }
    }
    
}