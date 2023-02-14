package uz.unnarsx.cherrygram

import android.app.Activity
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.os.Build
import org.telegram.messenger.*
import org.telegram.tgnet.TLRPC
import uz.unnarsx.cherrygram.extras.CherrygramExtras
import uz.unnarsx.cherrygram.extras.LocalVerifications
import uz.unnarsx.cherrygram.helpers.AnalyticsHelper
import uz.unnarsx.cherrygram.helpers.CherrygramToasts
import uz.unnarsx.cherrygram.preferences.boolean
import uz.unnarsx.cherrygram.preferences.int
import uz.unnarsx.cherrygram.preferences.long
import uz.unnarsx.cherrygram.preferences.string
//import uz.unnarsx.cherrygram.stickers.StickersIDsDownloader
import uz.unnarsx.cherrygram.icons.icon_replaces.BaseIconReplace
import uz.unnarsx.cherrygram.icons.icon_replaces.NoIconReplace
import uz.unnarsx.cherrygram.icons.icon_replaces.SolarIconReplace
import uz.unnarsx.cherrygram.icons.icon_replaces.VkIconReplace
import java.net.URL
import kotlin.system.exitProcess

object CherrygramConfig {

    private val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)

    val listener = OnSharedPreferenceChangeListener { preferences: SharedPreferences?, key: String ->
        val map = HashMap<String, String>(1)
        map["key"] = key
        if (appcenterAnalytics) {
            AnalyticsHelper.trackEvent("Cherry config changed", map)
        }
    }

    // Appearance Settings
    //Redesign
    var iconReplacement by sharedPreferences.int("AP_Icon_Replacements", getDefaultVKUI())
    fun getIconReplacement(): BaseIconReplace {
        return when (iconReplacement) {
            0 -> VkIconReplace()
            1 -> SolarIconReplace()
            else -> NoIconReplace()
        }
    }
    fun getDefaultVKUI(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            0
        } else 2
    }

    var centerTitle by sharedPreferences.boolean("AP_CenterTitle", true)
    var disableToolBarShadow by sharedPreferences.boolean("AP_ToolBarShadow", false)
    var flatNavbar by sharedPreferences.boolean("AP_FlatNB", false)
    var systemFonts by sharedPreferences.boolean("AP_SystemFonts", true)

    var drawSnowInChat by sharedPreferences.boolean("AP_DrawSnowInChat", false)
    var drawSnowInDrawer by sharedPreferences.boolean("AP_DrawSnowInDrawer", true)
    var drawSnowInActionBar by sharedPreferences.boolean("AP_DrawSnowInActionBar", true)

    var oldNotificationIcon by sharedPreferences.boolean("AP_Old_Notification_Icon", false)
    fun toggleOldNotificationIcon() { // Telegram chats settings
        oldNotificationIcon = !oldNotificationIcon
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean("AP_Old_Notification_Icon", oldNotificationIcon)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var defaultNotificationName by sharedPreferences.boolean("AP_Default_Notification_Name", false)
    fun toggleDefaultNotificationName() { // Telegram chats settings
        defaultNotificationName = !defaultNotificationName
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean("AP_Default_Notification_Name", defaultNotificationName)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var filterLauncherIcon by sharedPreferences.boolean("AP_Filter_Launcher_Icon", false)
    fun toggleAppIconFilter() { // Telegram chats settings
        filterLauncherIcon = !filterLauncherIcon
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean("AP_Filter_Launcher_Icon", filterLauncherIcon)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }
    //Animations and Premium Features
    var disableAnimatedAvatars by sharedPreferences.boolean("CP_DisableAnimAvatars", false)
    var disableReactionsOverlay by sharedPreferences.boolean("CP_DisableReactionsOverlay", false)
    var drawSmallReactions by sharedPreferences.boolean("CP_DrawSmallReactions", false)
    var disableReactionAnim by sharedPreferences.boolean("CP_DisableReactionAnim", false)
    var disablePremiumStatuses by sharedPreferences.boolean("CP_DisablePremiumStatuses", false)
    var disablePremStickAnim by sharedPreferences.boolean("CP_DisablePremStickAnim", false)
    var disablePremStickAutoPlay by sharedPreferences.boolean("CP_DisablePremStickAutoPlay", false)
    //Folders
    var folderNameInHeader by sharedPreferences.boolean("AP_FolderNameInHeader", false)
    var newTabs_hideAllChats by sharedPreferences.boolean("CP_NewTabs_RemoveAllChats", false)
    fun toggleNewTabs_hideAllChats() { // Telegram folders settings
        newTabs_hideAllChats = !newTabs_hideAllChats
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean("CP_NewTabs_RemoveAllChats", newTabs_hideAllChats)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }
    var tabsOnForward by sharedPreferences.boolean("CP_TabsOnForward", true)

    var vkuiFoldersStyle by sharedPreferences.boolean("CP_VKUIFoldersStyle", false)
    fun toggleVKUIFoldersStyle() { // Telegram folders settings
        vkuiFoldersStyle = !vkuiFoldersStyle
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean("CP_VKUIFoldersStyle", vkuiFoldersStyle)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var frameCorrection by sharedPreferences.int("frameCorrection", 20)

    var newTabs_noUnread by sharedPreferences.boolean("CP_NewTabs_NoCounter", false)
    fun toggleNewTabs_noUnread() { // Telegram folders settings
        newTabs_noUnread = !newTabs_noUnread
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean("CP_NewTabs_NoCounter", newTabs_noUnread)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    const val TAB_TYPE_TEXT = 0 // Telegram folders settings
    const val TAB_TYPE_MIX = 1 // Telegram folders settings
    const val TAB_TYPE_ICON = 2 // Telegram folders settings
    var tabMode by sharedPreferences.int("CG_FoldersType", 0)
    //Drawer
    var drawerAvatar by sharedPreferences.boolean("AP_DrawerAvatar", true)
    fun toggleDrawerAvatar() {
        drawerAvatar = !drawerAvatar
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean("AP_DrawerAvatar", drawerAvatar)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var drawerSmallAvatar by sharedPreferences.boolean("AP_DrawerSmallAvatar", true)
    fun toggleDrawerSmallAvatar() {
        drawerSmallAvatar = !drawerSmallAvatar
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean("AP_DrawerSmallAvatar", drawerSmallAvatar)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var drawerDarken by sharedPreferences.boolean("AP_DrawerDarken", true)
    fun toggleDrawerDarken() {
        drawerDarken = !drawerDarken
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean("AP_DrawerDarken", drawerDarken)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var drawerGradient by sharedPreferences.boolean("AP_DrawerGradient", true)
    fun toggleDrawerGradient() {
        drawerGradient = !drawerGradient
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean("AP_DrawerGradient", drawerGradient)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var drawerBlur by sharedPreferences.boolean("AP_DrawerBlur", true)
    fun toggleDrawerBlur() {
        drawerBlur = !drawerBlur
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean("AP_DrawerBlur", drawerBlur)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var drawerBlurIntensity by sharedPreferences.int("AP_DrawerBlur_Intensity", 40)

    var eventType by sharedPreferences.int("AP_DrawerEventType", 0)

    //Drawer buttons
    var createGroupDrawerButton by sharedPreferences.boolean("AP_CreateGroupDrawerButton", false)
    var secretChatDrawerButton by sharedPreferences.boolean("AP_SecretChatDrawerButton", false)
    var createChannelDrawerButton by sharedPreferences.boolean("AP_CreateChannelDrawerButton", false)
    var contactsDrawerButton by sharedPreferences.boolean("AP_ContactsDrawerButton", false)
    var callsDrawerButton by sharedPreferences.boolean("AP_CallsDrawerButton", true)
    var savedMessagesDrawerButton by sharedPreferences.boolean("AP_SavedMessagesDrawerButton", true)
    var archivedChatsDrawerButton by sharedPreferences.boolean("AP_ArchivedChatsDrawerButton", true)
    var peopleNearbyDrawerButton by sharedPreferences.boolean("AP_PeopleNearbyDrawerButton", false)
    var scanQRDrawerButton by sharedPreferences.boolean("AP_ScanQRDrawerButton", true)
    var cGPreferencesDrawerButton by sharedPreferences.boolean("AP_CGPreferencesDrawerButton", true)

    //Profile and Contacts
    var hidePhoneNumber by sharedPreferences.boolean("AP_HideUserPhone", false)
    var showId by sharedPreferences.boolean("AP_ShowID", false)
    var showDc by sharedPreferences.boolean("AP_ShowDC", false)

    // Chats Settings
    //Stickers
    var blockStickers by sharedPreferences.boolean("CP_BlockStickers", false)
    var slider_stickerAmplifier by sharedPreferences.int("CP_Slider_StickerAmplifier", 100)
    var hideStickerTime by sharedPreferences.boolean("CP_TimeOnStick", false)
    //Direct Share
    var usersDrawShareButton by sharedPreferences.boolean("CP_UsersDrawShareButton", false)
    //    var groupsDrawShareButton by sharedPreferences.boolean("CP_GroupsDrawShareButton", false)
    var supergroupsDrawShareButton by sharedPreferences.boolean("CP_SupergroupsDrawShareButton", false)
    var channelsDrawShareButton by sharedPreferences.boolean("CP_ChannelsDrawShareButton", true)
    var botsDrawShareButton by sharedPreferences.boolean("CP_BotsDrawShareButton", true)
    var stickersDrawShareButton by sharedPreferences.boolean("CP_StickersDrawShareButton", false)
    //Chats
    var unreadBadgeOnBackButton by sharedPreferences.boolean("CP_UnreadBadgeOnBackButton", false)
    var noRounding by sharedPreferences.boolean("CP_NoRounding", false)
    var deleteForAll by sharedPreferences.boolean("CP_DeleteForAll", false)
    var confirmCalls by sharedPreferences.boolean("CP_ConfirmCalls", false)
    var msgForwardDate by sharedPreferences.boolean("CP_ForwardMsgDate", false)
    var showSeconds by sharedPreferences.boolean("CP_ShowSeconds", false)

    const val DOUBLE_TAP_ACTION_NONE = 0
    const val DOUBLE_TAP_ACTION_REACTION = 1
    const val DOUBLE_TAP_ACTION_REPLY = 2
    const val DOUBLE_TAP_ACTION_SAVE = 3
    const val DOUBLE_TAP_ACTION_EDIT = 4
    var doubleTapAction by sharedPreferences.int("CP_DoubleTapAction", DOUBLE_TAP_ACTION_REACTION)

    var disableSwipeToNext by sharedPreferences.boolean("CP_DisableSwipeToNext", false)
    var hideKeyboardOnScroll by sharedPreferences.boolean("CP_HideKbdOnScroll", false)
    var hideSendAsChannel by sharedPreferences.boolean("CP_HideSendAsChannel", false)
    var slider_RecentEmojisAmplifier by sharedPreferences.int("CP_Slider_RecentEmojisAmplifier", 45)
    var slider_RecentStickersAmplifier by sharedPreferences.int("CP_Slider_RecentStickersAmplifier", 20)
    //Media
    var voicesAgc by sharedPreferences.boolean("CP_VoicesAGC", false)
    var playGIFasVideo by sharedPreferences.boolean("CP_PlayGIFasVideo", true)
    var playVideoOnVolume by sharedPreferences.boolean("CP_PlayVideo", false)
    var autoPauseVideo by sharedPreferences.boolean("CP_AutoPauseVideo", false)
    var audioFocus by sharedPreferences.boolean("CP_AudioFocus", false)
    var disableVibration by sharedPreferences.boolean("CP_DisableVibration", false)
    var disablePhotoTapAction by sharedPreferences.boolean("CP_DisablePhotoTapAction", false)
    var enableProximity by sharedPreferences.boolean("CP_Proximity", true)
    //Notifications
    var iosSound by sharedPreferences.boolean("CP_IOSSound", false)
    var silenceNonContacts by sharedPreferences.boolean("CP_SilenceNonContacts", false)

    // Camera Settings
    //Camera type
    var cameraType by sharedPreferences.int("CP_CameraType", 0)

    var useCameraXOptimizedMode by sharedPreferences.boolean("CP_CameraXOptimizedMode", false)
    fun toggleCameraXOptimizedMode() {
        useCameraXOptimizedMode = !useCameraXOptimizedMode
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean("CP_CameraXOptimizedMode", useCameraXOptimizedMode)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var cameraResolution by sharedPreferences.int("CP_CameraResolution", -1)
    //Camera
    var disableAttachCamera by sharedPreferences.boolean("CP_DisableCam", false)
    fun toggleDisableAttachCamera() {
        disableAttachCamera = !disableAttachCamera
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean("CP_DisableCam", disableAttachCamera)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var rearCam by sharedPreferences.boolean("CP_RearCam", false)
    fun toggleRearCam() {
        rearCam = !rearCam
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean("CP_RearCam", rearCam)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    const val Camera16to9 = 0
    const val Camera4to3 = 1
    const val Camera1to1 = 2
    var cameraAspectRatio by sharedPreferences.int("CP_CameraAspectRatio", Camera16to9)

    // Privacy
    var hideProxySponsor by sharedPreferences.boolean("SP_NoProxyPromo", true)
    var appcenterAnalytics by sharedPreferences.boolean("SP_AppCenterAnalytics", true)

    // Experimental
    const val BOOST_NONE = 0
    const val BOOST_AVERAGE = 1
    const val BOOST_EXTREME = 2
    var downloadSpeedBoost by sharedPreferences.int("EP_DownloadSpeedBoost", BOOST_NONE)

    const val DEFAULT_SIZE = 0
    const val HQ_SIZE = 1
    var photoSize by sharedPreferences.int("CP_PhotoSize", HQ_SIZE)

    var uploadSpeedBoost by sharedPreferences.boolean("EP_UploadSpeedBoost", false)
    fun toggleUploadSpeedBoost() {
        uploadSpeedBoost = !uploadSpeedBoost
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean("EP_UploadSpeedBoost", uploadSpeedBoost)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var slowNetworkMode by sharedPreferences.boolean("EP_SlowNetworkMode", false)
    fun toggleSlowNetworkMode() {
        slowNetworkMode = !slowNetworkMode
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean("EP_SlowNetworkMode", slowNetworkMode)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var showRPCError by sharedPreferences.boolean("EP_ShowRPCError", false)
    fun toggleShowRPCError() {
        showRPCError = !showRPCError
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean("EP_ShowRPCError", showRPCError)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    // OTA
    var installBetas by sharedPreferences.boolean("CG_Install_Beta_Ver", BuildVars.isBetaApp())
    fun toggleInstallBetas() {
        installBetas = !installBetas
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean("CG_Install_Beta_Ver", installBetas)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var autoOTA by sharedPreferences.boolean("CG_Auto_OTA", true)
    fun toggleAutoOTA() {
        autoOTA = !autoOTA
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean("CG_Auto_OTA", autoOTA)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var lastUpdateCheckTime by sharedPreferences.long("CG_LastUpdateCheckTime", 0)
    var updateScheduleTimestamp by sharedPreferences.long("CG_UpdateScheduleTimestamp", 0)

    // Misc
    var openProfile by sharedPreferences.boolean("CG_OpenProfile", false)
    fun toggleOpenProfile() {
        openProfile = !openProfile
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean("CG_OpenProfile", openProfile)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var residentNotification by sharedPreferences.boolean("CG_ResidentNotification", !ApplicationLoader.checkPlayServices())
    fun toggleResidentNotification() {
        residentNotification = !residentNotification
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean("CG_ResidentNotification", residentNotification)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }
    var forwardNoAuthorship by sharedPreferences.boolean("CG_ForwardNoAuthorship", false)
    var forwardWithoutCaptions by sharedPreferences.boolean("CG_ForwardWithoutCaptions", false)
    var forwardNotify by sharedPreferences.boolean("CG_ForwardNotify", true)
    var noAuthorship by sharedPreferences.boolean("CG_NoAuthorship", false)

    //Translator
    var translationKeyboardTarget by sharedPreferences.string("translationKeyboardTarget", "app")
    var translationTarget by sharedPreferences.string("translationTarget", "app")

    var hideMuteUnmuteButton by sharedPreferences.boolean("hideMuteUnmuteButton", false)

    init {
        CherrygramToasts.init(sharedPreferences)
        fuckOff()
        if (blockStickers) {
            CherrygramExtras.downloadCherrygramLogo(ApplicationLoader.applicationContext)
        }
        /*try {
            StickersIDsDownloader.SET_IDS = URL("https://raw.githubusercontent.com/arsLan4k1390/Cherrygram/main/stickers.txt").readText().lines()
//            Log.d("SetsDownloader", StickersIDsDownloader.SET_IDS.toString())
        }
        catch (e: Exception) { }*/
    }

    private fun fuckOff() {
        val good = "308203953082027da00302010202045c6a100c300d06092a864886f70d01010b0500307a310b300906035504061302555a311230100603550408130953616d61726b616e64311230100603550407130953616d61726b616e643111300f060355040a130854656c656772616d3111300f060355040b130854656c656772616d311d301b060355040313144172736c616e204b6861646a69627564696e6f763020170d3232303130333138343733315a180f32303731313232323138343733315a307a310b300906035504061302555a311230100603550408130953616d61726b616e64311230100603550407130953616d61726b616e643111300f060355040a130854656c656772616d3111300f060355040b130854656c656772616d311d301b060355040313144172736c616e204b6861646a69627564696e6f7630820122300d06092a864886f70d01010105000382010f003082010a0282010100905ab76f19c6fc8d50d50c4e6ae5fa32b72f2b3426bef7098f922a64e75b43af4c065bcc5f70e8d2a5517c7c089c3caddad964e876869c76662811f32e1b0e8ea46eb07375d0e563c4a440646be4b2a1947a83935a20039f1f0a19051561aaae714e9e5fe15494668f950303ab79176b432b2eadde75b9ac5ef61ef7c40db6711bd69b1912adb58802e74ff7cace591fde0b126788bded838303a82a5be479ce69a664745e8c9150d4510e0491608461be3598dfa9ff62e852aa544c7c17b00456dea5ecbb61c3cdb1bee00c5350f274ddd3c3579f812c1dd81b9825c0aa017ac2028c79c2b9a2c25ba29ea7d2a20da48188914b119dec1946280610073309e70203010001a321301f301d0603551d0e04160414282e1a317aa0a6133e8293a4b060ad1160c077c1300d06092a864886f70d01010b050003820101001f34812c430de1aa4644976c4df8686359283f381ab950fc9adc55a7b78a6fa762ad170c5df35baffe08689f1d64c396d610d6265df7fb9384520a97bf0665efabf4014ecd259c86ea4e5a384df3854e7a9e12b9388439f50bd9a70ef4a1b5a204ac717f5e431469ebc7cb4a4494e5681f369a56c11d4a912b15db76191029414749289b703c5739862d0e0bd89921b11a1bda953d529f22cd0596f41161b707f73f5a7e4df7e782bd73346001cb395127ab1a5901ec13518cd733d7b3c7a2e52b7e927e7f52a6e53efb4fde745829a4534f0f9cd04949e8240060ed5ac7373f2f04f5489211702e71fec86ba340ea87aa6e3f221e89a99ece1c33e710bb4efd"
        val info = ApplicationLoader.applicationContext.packageManager.getPackageInfo("uz.unnarsx.cherrygram", PackageManager.GET_SIGNATURES).signatures[0].toCharsString()
        if (info != good) {
            exitProcess(0)
        }
    }

    fun isCherryVerified(chat: TLRPC.Chat): Boolean {
        return LocalVerifications.getVerify().stream().anyMatch { id: Long -> id == chat.id }
    }

    fun isDirectApp(): Boolean {
        return "release" == BuildConfig.BUILD_TYPE || "debug" == BuildConfig.BUILD_TYPE
    }

}
