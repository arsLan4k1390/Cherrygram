package uz.unnarsx.cherrygram

import android.app.Activity
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.ComponentName
import android.content.pm.PackageManager
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.BuildConfig
import org.telegram.messenger.SharedConfig
import org.telegram.tgnet.TLRPC
import uz.unnarsx.cherrygram.helpers.AnalyticsHelper
import uz.unnarsx.cherrygram.helpers.CherrygramToasts
import uz.unnarsx.cherrygram.preferences.boolean
import uz.unnarsx.cherrygram.preferences.int
import uz.unnarsx.cherrygram.preferences.long
import uz.unnarsx.cherrygram.vkui.icon_replaces.BaseIconReplace
import uz.unnarsx.cherrygram.vkui.icon_replaces.NoIconReplace
import uz.unnarsx.cherrygram.vkui.icon_replaces.VkIconReplace
import uz.unnarsx.extras.LocalVerifications
import kotlin.system.exitProcess

object CherrygramConfig {

    private val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)

    val listener = OnSharedPreferenceChangeListener { preferences: SharedPreferences?, key: String ->
            val map = HashMap<String, String>(1)
            map["key"] = key
            AnalyticsHelper.trackEvent("Cherry config changed", map)
        }

    // Appearance Settings
    //Redesign
    var iconReplacement by sharedPreferences.int("AP_IconReplacements", CGFeatureJavaHooks.getDefaultVKUI())
    fun getIconReplacement(): BaseIconReplace {
        return when (iconReplacement) {
            1 -> NoIconReplace()
            else -> VkIconReplace()
        }
    }

    var flatActionbar by sharedPreferences.boolean("AP_ToolBarShadow", false)
    var flatNavbar by sharedPreferences.boolean("AP_FlatNB", false)
    var systemFonts by sharedPreferences.boolean("AP_SystemFonts", true)

    var oldNotificationIcon by sharedPreferences.boolean("AP_Old_Notification_Icon", false)
    fun toggleOldNotificationIcon() { // Telegram chats settings
        oldNotificationIcon = !oldNotificationIcon
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putBoolean("AP_Old_Notification_Icon", oldNotificationIcon)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var filterLauncherIcon by sharedPreferences.boolean("AP_Filter_Launcher_Icon", false)
    fun toggleAppIconFilter() { // Telegram chats settings
        filterLauncherIcon = !filterLauncherIcon
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
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
    var tabsOnForward by sharedPreferences.boolean("CP_TabsOnForward", true)

    var newTabs_noUnread by sharedPreferences.boolean("CP_NewTabs_NoCounter", false)
    fun toggleNewTabs_noUnread() { // Telegram folders settings
        newTabs_noUnread = !newTabs_noUnread
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putBoolean("CP_NewTabs_NoCounter", newTabs_noUnread)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    const val TAB_TYPE_TEXT = 0 // Telegram folders settings
    const val TAB_TYPE_MIX = 1 // Telegram folders settings
    const val TAB_TYPE_ICON = 2 // Telegram folders settings
    var tabMode by sharedPreferences.int("CG_FoldersType", 0)
    @JvmName("TabMode")
    fun setTabMode(mode: Int) { // Telegram folders settings
        tabMode = mode
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putInt("CG_FoldersType", tabMode)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }
    //Drawer
    var drawerAvatar by sharedPreferences.boolean("AP_DrawerAvatar", true)
    fun toggleDrawerAvatar() {
        drawerAvatar = !drawerAvatar
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putBoolean("AP_DrawerAvatar", drawerAvatar)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var drawerSmallAvatar by sharedPreferences.boolean("AP_DrawerSmallAvatar", true)
    fun toggleDrawerSmallAvatar() {
        drawerSmallAvatar = !drawerSmallAvatar
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putBoolean("AP_DrawerSmallAvatar", drawerSmallAvatar)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var drawerDarken by sharedPreferences.boolean("AP_DrawerDarken", true)
    fun toggleDrawerDarken() {
        drawerDarken = !drawerDarken
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putBoolean("AP_DrawerDarken", drawerDarken)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var drawerGradient by sharedPreferences.boolean("AP_DrawerGradient", true)
    fun toggleDrawerGradient() {
        drawerGradient = !drawerGradient
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putBoolean("AP_DrawerGradient", drawerGradient)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var drawerBlur by sharedPreferences.boolean("AP_DrawerBlur", true)
    fun toggleDrawerBlur() {
        drawerBlur = !drawerBlur
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putBoolean("AP_DrawerBlur", drawerBlur)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var drawerBlurIntensity by sharedPreferences.int("AP_DrawerBlurIntensity", 75)
    fun saveDrawerBlurIntensity(intensity: Int) {
        drawerBlurIntensity = intensity
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putInt("AP_DrawerBlurIntensity", drawerBlurIntensity)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var eventType by sharedPreferences.int("AP_DrawerEventType", 0)
    fun saveEventType(type: Int) {
        eventType = type
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putInt("AP_DrawerEventType", eventType)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    //Drawer buttons
    var CreateGroupDrawerButton by sharedPreferences.boolean("AP_CreateGroupDrawerButton", false)
    fun toggleCreateGroupDrawerButton() {
        CreateGroupDrawerButton = !CreateGroupDrawerButton
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putBoolean("AP_CreateGroupDrawerButton", CreateGroupDrawerButton)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var SecretChatDrawerButton by sharedPreferences.boolean("AP_SecretChatDrawerButton", false)
    fun toggleSecretChatDrawerButton() {
        SecretChatDrawerButton = !SecretChatDrawerButton
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putBoolean("AP_SecretChatDrawerButton", SecretChatDrawerButton)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var CreateChannelDrawerButton by sharedPreferences.boolean("AP_CreateChannelDrawerButton", false)
    fun toggleCreateChannelDrawerButton() {
        CreateChannelDrawerButton = !CreateChannelDrawerButton
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putBoolean("AP_CreateChannelDrawerButton", CreateChannelDrawerButton)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var ContactsDrawerButton by sharedPreferences.boolean("AP_ContactsDrawerButton", false)
    fun toggleContactsDrawerButton() {
        ContactsDrawerButton = !ContactsDrawerButton
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putBoolean("AP_ContactsDrawerButton", ContactsDrawerButton)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var CallsDrawerButton by sharedPreferences.boolean("AP_CallsDrawerButton", true)
    fun toggleCallsDrawerButton() {
        CallsDrawerButton = !CallsDrawerButton
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putBoolean("AP_CallsDrawerButton", CallsDrawerButton)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var SavedMessagesDrawerButton by sharedPreferences.boolean("AP_SavedMessagesDrawerButton", true)
    fun toggleSavedMessagesDrawerButton() {
        SavedMessagesDrawerButton = !SavedMessagesDrawerButton
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putBoolean("AP_SavedMessagesDrawerButton", SavedMessagesDrawerButton)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var ArchivedChatsDrawerButton by sharedPreferences.boolean("AP_ArchivedChatsDrawerButton", true)
    fun toggleArchivedChatsDrawerButton() {
        ArchivedChatsDrawerButton = !ArchivedChatsDrawerButton
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putBoolean("AP_ArchivedChatsDrawerButton", ArchivedChatsDrawerButton)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var PeopleNearbyDrawerButton by sharedPreferences.boolean("AP_PeopleNearbyDrawerButton", false)
    fun togglePeopleNearbyDrawerButton() {
        PeopleNearbyDrawerButton = !PeopleNearbyDrawerButton
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putBoolean("AP_PeopleNearbyDrawerButton", PeopleNearbyDrawerButton)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var ScanQRDrawerButton by sharedPreferences.boolean("AP_ScanQRDrawerButton", true)
    fun toggleScanQRDrawerButton() {
        ScanQRDrawerButton = !ScanQRDrawerButton
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putBoolean("AP_ScanQRDrawerButton", ScanQRDrawerButton)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var CGPreferencesDrawerButton by sharedPreferences.boolean("AP_CGPreferencesDrawerButton", true)
    fun toggleCGPreferencesDrawerButton() {
        CGPreferencesDrawerButton = !CGPreferencesDrawerButton
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putBoolean("AP_CGPreferencesDrawerButton", CGPreferencesDrawerButton)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }
    //Profile and Contacts
    var hidePhoneNumber by sharedPreferences.boolean("AP_HideUserPhone", false)
    var showId by sharedPreferences.boolean("AP_ShowID", false)
    var showDc by sharedPreferences.boolean("AP_ShowDC", false)

    // Chats Settings
    //Stickers
    var slider_stickerAmplifier by sharedPreferences.int("CP_Slider_StickerAmplifier", 100)
    var hideStickerTime by sharedPreferences.boolean("CP_TimeOnStick", false)
    //Chats
    var unreadBadgeOnBackButton by sharedPreferences.boolean("CP_UnreadBadgeOnBackButton", false)
    var noRounding by sharedPreferences.boolean("CP_NoRounding", false)
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
    var cameraType by sharedPreferences.int("CP_CameraType", 0) /*CameraXUtilities.getDefault());*/
    fun saveCameraType(type: Int) {
        cameraType = type
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt("CP_CameraType", cameraType)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var useCameraXOptimizedMode by sharedPreferences.boolean("CP_CameraXOptimizedMode", false)/*SharedConfig.getDevicePerformanceClass() != SharedConfig.PERFORMANCE_CLASS_HIGH)*/;
    fun toggleCameraXOptimizedMode() {
        useCameraXOptimizedMode = !useCameraXOptimizedMode
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean("CP_CameraXOptimizedMode", useCameraXOptimizedMode)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    const val CameraX30 = 30
    const val CameraX60 = 60
    var cameraXFps by sharedPreferences.int("CP_CameraXFps",
        if (SharedConfig.getDevicePerformanceClass() == SharedConfig.PERFORMANCE_CLASS_HIGH) CameraX60 else CameraX30
    )
    fun saveCameraXFps(fps: Int) {
        cameraXFps = fps
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt("CP_CameraXFps", cameraXFps)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }
    //Camera
    var disableAttachCamera by sharedPreferences.boolean("CP_DisableCam", false)
    fun toggleDisableAttachCamera() {
        disableAttachCamera = !disableAttachCamera
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putBoolean("CP_DisableCam", disableAttachCamera)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var rearCam by sharedPreferences.boolean("CP_RearCam", false)
    fun toggleRearCam() {
        rearCam = !rearCam
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putBoolean("CP_RearCam", rearCam)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    const val Camera16to9 = 0
    const val Camera4to3 = 1
    const val Camera1to1 = 2
    var cameraAspectRatio by sharedPreferences.int("CP_CameraAspectRatio", Camera16to9)
    @JvmName("setCameraAspectRatio1")
    fun setCameraAspectRatio(aspectRatio: Int) {
        cameraAspectRatio = aspectRatio
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putInt("CP_CameraAspectRatio", aspectRatio)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    // Privacy
    var hideProxySponsor by sharedPreferences.boolean("SP_NoProxyPromo", true)

    // Experimental
    const val BOOST_NONE = 0
    const val BOOST_AVERAGE = 1
    const val BOOST_EXTREME = 2
    var downloadSpeedBoost by sharedPreferences.int("EP_DownloadSpeedBoost", BOOST_NONE)
    @JvmName("setDownloadSpeedBoost1")
    fun setDownloadSpeedBoost(boost: Int) {
        downloadSpeedBoost = boost
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putInt("EP_DownloadSpeedBoost", boost)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var uploadSpeedBoost by sharedPreferences.boolean("EP_UploadSpeedBoost", false)
    fun toggleUploadSpeedBoost() {
        uploadSpeedBoost = !uploadSpeedBoost
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putBoolean("EP_UploadSpeedBoost", uploadSpeedBoost)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var slowNetworkMode by sharedPreferences.boolean("EP_SlowNetworkMode", false)
    fun toggleSlowNetworkMode() {
        slowNetworkMode = !slowNetworkMode
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putBoolean("EP_SlowNetworkMode", slowNetworkMode)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    // OTA
    var autoOTA by sharedPreferences.boolean("CG_Auto_OTA", true)
    fun toggleAutoOTA() {
        autoOTA = !autoOTA
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putBoolean("CG_Auto_OTA", autoOTA)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    var lastUpdateCheckTime by sharedPreferences.long("CG_LastUpdateCheckTime", 0)
    @JvmName("setLastUpdateCheckTime1")
    fun setLastUpdateCheckTime(time: Long) {
        lastUpdateCheckTime = time
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putLong("CG_LastUpdateCheckTime", lastUpdateCheckTime)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }
    var updateScheduleTimestamp by sharedPreferences.long("CG_UpdateScheduleTimestamp", 0)
    @JvmName("updateScheduleTimestamp1")
    fun setUpdateScheduleTimestamp(timestamp: Long) {
        updateScheduleTimestamp = timestamp
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putLong("CG_UpdateScheduleTimestamp", updateScheduleTimestamp)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    // Misc
    var residentNotification by sharedPreferences.boolean("CG_ResidentNotification", false)
    fun toggleResidentNotification() {
        residentNotification = !residentNotification
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putBoolean("CG_ResidentNotification", residentNotification)
        editor.apply()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }
    var forwardNoAuthorship by sharedPreferences.boolean("CG_ForwardNoAuthorship", false)
    var forwardWithoutCaptions by sharedPreferences.boolean("CG_ForwardWithoutCaptions", false)
    var forwardNotify by sharedPreferences.boolean("CG_ForwardNotify", true)
    var noAuthorship by sharedPreferences.boolean("CG_NoAuthorship", false)

    init {
        CherrygramToasts.init(sharedPreferences)
        fuckOff()
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
