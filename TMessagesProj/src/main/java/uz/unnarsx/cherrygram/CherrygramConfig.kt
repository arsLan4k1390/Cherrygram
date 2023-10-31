package uz.unnarsx.cherrygram

import android.app.Activity
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import kotlinx.coroutines.*
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.BuildVars
import org.telegram.messenger.MessagesController
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.TLRPC
import uz.unnarsx.cherrygram.extras.CherrygramExtras
import uz.unnarsx.cherrygram.extras.LocalVerifications
import uz.unnarsx.cherrygram.helpers.AnalyticsHelper
import uz.unnarsx.cherrygram.helpers.CherrygramToasts
import uz.unnarsx.cherrygram.helpers.FirebaseAnalyticsHelper
import uz.unnarsx.cherrygram.preferences.boolean
import uz.unnarsx.cherrygram.preferences.int
import uz.unnarsx.cherrygram.preferences.long
import uz.unnarsx.cherrygram.preferences.string
import uz.unnarsx.cherrygram.stickers.StickersIDsDownloader
import uz.unnarsx.cherrygram.ui.icons.icon_replaces.BaseIconReplace
import uz.unnarsx.cherrygram.ui.icons.icon_replaces.NoIconReplace
import uz.unnarsx.cherrygram.ui.icons.icon_replaces.SolarIconReplace
import uz.unnarsx.cherrygram.ui.icons.icon_replaces.VkIconReplace
import kotlin.system.exitProcess

object CherrygramConfig: CoroutineScope by MainScope() {

    private val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)

    val listener = OnSharedPreferenceChangeListener { preferences: SharedPreferences?, key: String? ->
        val map = HashMap<String, String?>(1)
        map["key"] = key
        if (appcenterAnalytics) {
            AnalyticsHelper.trackEvent("Cherry config changed", map)
        }

        if (googleAnalytics) {
            try {
                val bundle = Bundle()
                bundle.putString("key", key)
                FirebaseAnalyticsHelper.trackEvent("cherry_config_changed", bundle)
//                Toast.makeText(ApplicationLoader.applicationContext, bundle.toString(), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
//                Toast.makeText(ApplicationLoader.applicationContext, "error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun putBoolean(key: String, value: Boolean) {
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun putStringForUserPrefs(key: String, value: String) {
        val preferences = MessagesController.getMainSettings(UserConfig.selectedAccount)
        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    // General Settings
    //General
    var noRounding by sharedPreferences.boolean("CP_NoRounding", false)
    var showSeconds by sharedPreferences.boolean("CP_ShowSeconds", false)
    var systemEmoji by sharedPreferences.boolean("AP_SystemEmoji", false)
    var systemFonts by sharedPreferences.boolean("AP_SystemFonts", true)
    var oldNotificationIcon by sharedPreferences.boolean("AP_Old_Notification_Icon", false)
    //Profile and Contacts
    var confirmCalls by sharedPreferences.boolean("CP_ConfirmCalls", false)
    var hidePhoneNumber by sharedPreferences.boolean("AP_HideUserPhone", false)
    var showId by sharedPreferences.boolean("AP_ShowID", false)
    var showDc by sharedPreferences.boolean("AP_ShowDC", false)
    //Animations and Premium Features
    var hideStories by sharedPreferences.boolean("CP_HideStories", false)
    var disableAnimatedAvatars by sharedPreferences.boolean("CP_DisableAnimAvatars", false)
    var disableReplyPanelBackground by sharedPreferences.boolean("CP_DisableReplyBackground", false)
    var disableReactionsOverlay by sharedPreferences.boolean("CP_DisableReactionsOverlay", false)
    var disableReactionAnim by sharedPreferences.boolean("CP_DisableReactionAnim", false)
    var disablePremiumStatuses by sharedPreferences.boolean("CP_DisablePremiumStatuses", false)
    var disablePremStickAnim by sharedPreferences.boolean("CP_DisablePremStickAnim", false)
    var disablePremStickAutoPlay by sharedPreferences.boolean("CP_DisablePremStickAutoPlay", false)
    var hideSendAsChannel by sharedPreferences.boolean("CP_HideSendAsChannel", false)

    // Appearance Settings
    //Redesign
    const val ICON_REPLACE_NONE = 0
    const val ICON_REPLACE_VKUI = 1
    const val ICON_REPLACE_SOLAR = 2

    var iconReplacement by sharedPreferences.int("AP_Icon_Replacements", ICON_REPLACE_SOLAR)
    fun getCurrentIconPack(): BaseIconReplace {
        return when (iconReplacement) {
            ICON_REPLACE_VKUI -> VkIconReplace()
            ICON_REPLACE_SOLAR -> SolarIconReplace()
            else -> NoIconReplace()
        }
    }

    var oneUI_SwitchStyle by sharedPreferences.boolean("AP_OneUI_SwitchStyle", true)
    var centerTitle by sharedPreferences.boolean("AP_CenterTitle", true)
    var disableToolBarShadow by sharedPreferences.boolean("AP_ToolBarShadow", true)
    var disableDividers by sharedPreferences.boolean("AP_DisableDividers", true)
    var overrideHeaderColor by sharedPreferences.boolean("AP_OverrideHeaderColor", true)
    var flatNavbar by sharedPreferences.boolean("AP_FlatNavBar", true)
    //Drawer
    var drawerAvatar by sharedPreferences.boolean("AP_DrawerAvatar", true)
    fun toggleDrawerAvatar() {
        drawerAvatar = !drawerAvatar
        putBoolean("AP_DrawerAvatar", drawerAvatar)
    }

    var drawerSmallAvatar by sharedPreferences.boolean("AP_DrawerSmallAvatar", false)
    fun toggleDrawerSmallAvatar() {
        drawerSmallAvatar = !drawerSmallAvatar
        putBoolean("AP_DrawerSmallAvatar", drawerSmallAvatar)
    }

    var drawerDarken by sharedPreferences.boolean("AP_DrawerDarken", true)
    fun toggleDrawerDarken() {
        drawerDarken = !drawerDarken
        putBoolean("AP_DrawerDarken", drawerDarken)
    }

    var drawerGradient by sharedPreferences.boolean("AP_DrawerGradient", false)
    fun toggleDrawerGradient() {
        drawerGradient = !drawerGradient
        putBoolean("AP_DrawerGradient", drawerGradient)
    }

    var drawerBlur by sharedPreferences.boolean("AP_DrawerBlur", true)
    fun toggleDrawerBlur() {
        drawerBlur = !drawerBlur
        putBoolean("AP_DrawerBlur", drawerBlur)
    }

    var drawerBlurIntensity by sharedPreferences.int("AP_DrawerBlur_Intensity", 50)

    var eventType by sharedPreferences.int("AP_DrawerEventType", 0)

    //Drawer buttons
    var createGroupDrawerButton by sharedPreferences.boolean("AP_CreateGroupDrawerButton", false)
    fun toggleCreateGroupDrawerButton() {
        createGroupDrawerButton = !createGroupDrawerButton
        putBoolean("AP_CreateGroupDrawerButton", createGroupDrawerButton)
    }

    var secretChatDrawerButton by sharedPreferences.boolean("AP_SecretChatDrawerButton", false)
    fun toggleSecretChatDrawerButton() {
        secretChatDrawerButton = !secretChatDrawerButton
        putBoolean("AP_SecretChatDrawerButton", secretChatDrawerButton)
    }

    var createChannelDrawerButton by sharedPreferences.boolean("AP_CreateChannelDrawerButton", false)
    fun toggleCreateChannelDrawerButton() {
        createChannelDrawerButton = !createChannelDrawerButton
        putBoolean("AP_CreateChannelDrawerButton", createChannelDrawerButton)
    }

    var contactsDrawerButton by sharedPreferences.boolean("AP_ContactsDrawerButton", false)
    fun toggleContactsDrawerButton() {
        contactsDrawerButton = !contactsDrawerButton
        putBoolean("AP_ContactsDrawerButton", contactsDrawerButton)
    }

    var callsDrawerButton by sharedPreferences.boolean("AP_CallsDrawerButton", true)
    fun toggleCallsDrawerButton() {
        callsDrawerButton = !callsDrawerButton
        putBoolean("AP_CallsDrawerButton", callsDrawerButton)
    }

    var savedMessagesDrawerButton by sharedPreferences.boolean("AP_SavedMessagesDrawerButton", true)
    fun toggleSavedMessagesDrawerButton() {
        savedMessagesDrawerButton = !savedMessagesDrawerButton
        putBoolean("AP_SavedMessagesDrawerButton", savedMessagesDrawerButton)
    }

    var archivedChatsDrawerButton by sharedPreferences.boolean("AP_ArchivedChatsDrawerButton", true)
    fun toggleArchivedChatsDrawerButton() {
        archivedChatsDrawerButton = !archivedChatsDrawerButton
        putBoolean("AP_ArchivedChatsDrawerButton", archivedChatsDrawerButton)
    }

    var peopleNearbyDrawerButton by sharedPreferences.boolean("AP_PeopleNearbyDrawerButton", false)
    fun togglePeopleNearbyDrawerButton() {
        peopleNearbyDrawerButton = !peopleNearbyDrawerButton
        putBoolean("AP_PeopleNearbyDrawerButton", peopleNearbyDrawerButton)
    }

    var scanQRDrawerButton by sharedPreferences.boolean("AP_ScanQRDrawerButton", true)
    fun toggleScanQRDrawerButton() {
        scanQRDrawerButton = !scanQRDrawerButton
        putBoolean("AP_ScanQRDrawerButton", scanQRDrawerButton)
    }

    var cGPreferencesDrawerButton by sharedPreferences.boolean("AP_CGPreferencesDrawerButton", true)
    fun toggleCGPreferencesDrawerButton() {
        cGPreferencesDrawerButton = !cGPreferencesDrawerButton
        putBoolean("AP_CGPreferencesDrawerButton", cGPreferencesDrawerButton)
    }
    //Folders
    var folderNameInHeader by sharedPreferences.boolean("AP_FolderNameInHeader", false)
    var newTabs_hideAllChats by sharedPreferences.boolean("CP_NewTabs_RemoveAllChats", false)
    var newTabs_noUnread by sharedPreferences.boolean("CP_NewTabs_NoCounter", false)

    const val TAB_TYPE_MIX = 0
    const val TAB_TYPE_TEXT = 1
    const val TAB_TYPE_ICON = 2
    var tabMode by sharedPreferences.int("AP_TabMode", 1)

    const val TAB_STYLE_DEFAULT = 0
    const val TAB_STYLE_ROUNDED = 1
    const val TAB_STYLE_TEXT = 2
    const val TAB_STYLE_VKUI = 3
    const val TAB_STYLE_PILLS = 4
    var tabStyle by sharedPreferences.int("AP_TabStyle", TAB_STYLE_ROUNDED)
    var tabStyleStroke by sharedPreferences.boolean("AP_TabStyleAddStroke", false)

    //Snowflakes
    var drawSnowInChat by sharedPreferences.boolean("AP_DrawSnowInChat", false)
    var drawSnowInDrawer by sharedPreferences.boolean("AP_DrawSnowInDrawer", false)

    // Chats Settings
    //Stickers
    var blockStickers by sharedPreferences.boolean("CP_BlockStickers", false)
    var slider_stickerAmplifier by sharedPreferences.int("CP_Slider_StickerAmplifier", 100)
    var hideStickerTime by sharedPreferences.boolean("CP_TimeOnStick", false)
    //Direct Share
    var usersDrawShareButton by sharedPreferences.boolean("CP_UsersDrawShareButton", false)
    fun toggleUsersDrawShareButton() {
        usersDrawShareButton = !usersDrawShareButton
        putBoolean("CP_UsersDrawShareButton", usersDrawShareButton)
    }

    //Message menu
    var showReply by sharedPreferences.boolean("CP_ShowReply", true)
    fun toggleShowReply() {
        showReply = !showReply
        putBoolean("CP_ShowReply", showReply)
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
        putBoolean("CP_ShowSaveMessage", showReport)
    }

    var showJSON by sharedPreferences.boolean("CP_ShowJSON", false)
    fun toggleShowJSON() {
        showJSON = !showJSON
        putBoolean("CP_ShowJSON", showJSON)
    }

    //Direct Share
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

    //Chats
    var unreadBadgeOnBackButton by sharedPreferences.boolean("CP_UnreadBadgeOnBackButton", false)
    var deleteForAll by sharedPreferences.boolean("CP_DeleteForAll", false)
    var msgForwardDate by sharedPreferences.boolean("CP_ForwardMsgDate", false)
    var showPencilIcon by sharedPreferences.boolean("AP_PencilIcon", false)

    const val LEFT_BUTTON_FORWARD_WO_AUTHORSHIP = 0
    const val LEFT_BUTTON_DIRECT_SHARE = 1
    const val LEFT_BUTTON_REPLY = 2
    var leftBottomButton by sharedPreferences.int("CP_LeftBottomButton", LEFT_BUTTON_FORWARD_WO_AUTHORSHIP)

    const val DOUBLE_TAP_ACTION_NONE = 0
    const val DOUBLE_TAP_ACTION_REACTION = 1
    const val DOUBLE_TAP_ACTION_REPLY = 2
    const val DOUBLE_TAP_ACTION_SAVE = 3
    const val DOUBLE_TAP_ACTION_EDIT = 4
    const val DOUBLE_TAP_ACTION_TRANSLATE = 5
    var doubleTapAction by sharedPreferences.int("CP_DoubleTapAction", DOUBLE_TAP_ACTION_REACTION)

    var hideKeyboardOnScroll by sharedPreferences.boolean("CP_HideKbdOnScroll", false)
    var disableSwipeToNext by sharedPreferences.boolean("CP_DisableSwipeToNext", false)
    var hideMuteUnmuteButton by sharedPreferences.boolean("CP_HideMuteUnmuteButton", false)
    var slider_RecentEmojisAmplifier by sharedPreferences.int("CP_Slider_RecentEmojisAmplifier", 45)
    var slider_RecentStickersAmplifier by sharedPreferences.int("CP_Slider_RecentStickersAmplifier", 20)
    //Media
    var voicesAgc by sharedPreferences.boolean("CP_VoicesAGC", false)
    var playGIFasVideo by sharedPreferences.boolean("CP_PlayGIFasVideo", true)
    var playVideoOnVolume by sharedPreferences.boolean("CP_PlayVideo", false)
    var autoPauseVideo by sharedPreferences.boolean("CP_AutoPauseVideo", false)
    var disableVibration by sharedPreferences.boolean("CP_DisableVibration", false)
    //Notifications
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

    // Camera Settings
    //Camera type
    const val TELEGRAM_CAMERA = 0
    const val CAMERA_X = 1
    const val SYSTEM_CAMERA = 2
    var cameraType by sharedPreferences.int("CP_CameraType", TELEGRAM_CAMERA)

    var useCameraXOptimizedMode by sharedPreferences.boolean("CP_CameraXOptimizedMode", false)
    fun toggleCameraXOptimizedMode() {
        useCameraXOptimizedMode = !useCameraXOptimizedMode
        putBoolean("CP_CameraXOptimizedMode", useCameraXOptimizedMode)
    }

    var reduceCameraXLatency by sharedPreferences.boolean("CP_ReduceCameraXLatency", false)
    fun toggleReduceCameraXLatency() {
        reduceCameraXLatency = !reduceCameraXLatency
        putBoolean("CP_ReduceCameraXLatency", reduceCameraXLatency)
    }

    var cameraResolution by sharedPreferences.int("CP_CameraResolution", -1)
    //Camera
    var disableAttachCamera by sharedPreferences.boolean("CP_DisableCam", false)
    fun toggleDisableAttachCamera() {
        disableAttachCamera = !disableAttachCamera
        putBoolean("CP_DisableCam", disableAttachCamera)
    }

    var rearCam by sharedPreferences.boolean("CP_RearCam", false)
    fun toggleRearCam() {
        rearCam = !rearCam
        putBoolean("CP_RearCam", rearCam)
    }

    const val Camera16to9 = 0
    const val Camera4to3 = 1
    const val Camera1to1 = 2
    var cameraAspectRatio by sharedPreferences.int("CP_CameraAspectRatio", Camera16to9)

    var whiteBackground by sharedPreferences.boolean("CG_WhiteBG", false)

    // Privacy
    var hideProxySponsor by sharedPreferences.boolean("SP_NoProxyPromo", true)
    var appcenterAnalytics by sharedPreferences.boolean("SP_AppCenterAnalytics", !ApplicationLoader.checkPlayServices())
    var googleAnalytics by sharedPreferences.boolean("SP_GoogleAnalytics", ApplicationLoader.checkPlayServices())

    // Experimental
    //General
    const val ANIMATION_SPRING = 0
    const val ANIMATION_CLASSIC = 1
    var springAnimation by sharedPreferences.int("EP_SpringAnimation", ANIMATION_SPRING)

    var actionbarCrossfade by sharedPreferences.boolean("EP_ActionbarCrossfade", true)
    fun toggleActionbarCrossfade() {
        actionbarCrossfade = !actionbarCrossfade
        putBoolean("EP_ActionbarCrossfade", actionbarCrossfade)
    }

    var largePhotos by sharedPreferences.boolean("CP_LargePhotos", true)

    var residentNotification by sharedPreferences.boolean("CG_ResidentNotification", !ApplicationLoader.checkPlayServices())
    fun toggleResidentNotification() {
        residentNotification = !residentNotification
        putBoolean("CG_ResidentNotification", residentNotification)
    }

    var showRPCError by sharedPreferences.boolean("EP_ShowRPCError", false)
    fun toggleShowRPCError() {
        showRPCError = !showRPCError
        putBoolean("EP_ShowRPCError", showRPCError)
    }

    var customChatForSavedMessages by sharedPreferences.boolean("CP_CustomChatForSavedMessages", false)
    fun toggleCustomChatForSavedMessages() {
        customChatForSavedMessages = !customChatForSavedMessages
        putBoolean("CP_CustomChatForSavedMessages", customChatForSavedMessages)
    }

    const val BOOST_NONE = 0
    const val BOOST_AVERAGE = 1
    const val BOOST_EXTREME = 2
    var downloadSpeedBoost by sharedPreferences.int("EP_DownloadSpeedBoost", BOOST_NONE)

    var uploadSpeedBoost by sharedPreferences.boolean("EP_UploadSpeedBoost", false)
    fun toggleUploadSpeedBoost() {
        uploadSpeedBoost = !uploadSpeedBoost
        putBoolean("EP_UploadSpeedBoost", uploadSpeedBoost)
    }

    var slowNetworkMode by sharedPreferences.boolean("EP_SlowNetworkMode", false)
    fun toggleSlowNetworkMode() {
        slowNetworkMode = !slowNetworkMode
        putBoolean("EP_SlowNetworkMode", slowNetworkMode)
    }

    // OTA
    var installBetas by sharedPreferences.boolean("CG_Install_Beta_Ver", BuildVars.isBetaApp())
    fun toggleInstallBetas() {
        installBetas = !installBetas
        putBoolean("CG_Install_Beta_Ver", installBetas)
    }

    var autoOTA by sharedPreferences.boolean("CG_Auto_OTA", true)
    fun toggleAutoOTA() {
        autoOTA = !autoOTA
        putBoolean("CG_Auto_OTA", autoOTA)
    }

    var lastUpdateCheckTime by sharedPreferences.long("CG_LastUpdateCheckTime", 0)
    var updateScheduleTimestamp by sharedPreferences.long("CG_UpdateScheduleTimestamp", 0)

    // Misc
    var forwardNoAuthorship by sharedPreferences.boolean("CG_ForwardNoAuthorship", false)
    var forwardWithoutCaptions by sharedPreferences.boolean("CG_ForwardWithoutCaptions", false)
    var forwardNotify by sharedPreferences.boolean("CG_ForwardNotify", true)
    var noAuthorship by sharedPreferences.boolean("CG_NoAuthorship", false)

    var filterLauncherIcon by sharedPreferences.boolean("AP_Filter_Launcher_Icon", false)
    fun toggleAppIconFilter() { // Telegram chats settings
        filterLauncherIcon = !filterLauncherIcon
        putBoolean("AP_Filter_Launcher_Icon", filterLauncherIcon)
    }

    //Translator
    var translationKeyboardTarget by sharedPreferences.string("translationKeyboardTarget", "app")
    var translationTarget by sharedPreferences.string("translationTarget", "app")
    //Telegram Debug Menu
    var openSearch by sharedPreferences.boolean("CP_OpenSearch", true)
    fun toggleOpenSearch() {
        openSearch = !openSearch
        putBoolean("CP_OpenSearch", openSearch)
    }

    init {
        CherrygramToasts.init(sharedPreferences)
//        fuckOff()
    }

    init {
        launch(Dispatchers.IO) {
            if (blockStickers) {
                StickersIDsDownloader.getStickerSetIDs()
                CherrygramExtras.downloadCherrygramLogo(ApplicationLoader.applicationContext)
            }
            delay(2000)
            if (googleAnalytics) {
                try {
                    FirebaseAnalyticsHelper.start(ApplicationLoader.applicationContext)
                    val bundle = Bundle()
                    FirebaseAnalyticsHelper.trackEvent("cg_start", bundle)
                    /*AndroidUtilities.runOnUIThread(Runnable {
                        Toast.makeText(ApplicationLoader.applicationContext, "cg_start", Toast.LENGTH_SHORT).show()
                    })*/
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    /*AndroidUtilities.runOnUIThread(Runnable {
                        Toast.makeText(ApplicationLoader.applicationContext, "error", Toast.LENGTH_SHORT).show()
                    })*/
                }
            }
        }
    }

    private fun fuckOff() {
        val good = Extra.PACKAGE_HASH
        val info = AndroidUtilities.getCertificateSHA256Fingerprint()
        if (info != good) {
            exitProcess(0)
        }
    }

    fun isCherryVerified(chat: TLRPC.Chat): Boolean {
        return LocalVerifications.getVerify().stream().anyMatch { id: Long -> id == chat.id }
    }

    fun isDeleteAllHidden(chat: TLRPC.Chat): Boolean {
        return LocalVerifications.hideDeleteAll().stream().anyMatch { id: Long -> id == chat.id }
    }

}
