package uz.unnarsx.cherrygram

import android.app.Activity
import android.content.SharedPreferences
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.SharedConfig
import uz.unnarsx.cherrygram.camera.CameraXUtilities
import uz.unnarsx.cherrygram.preferences.boolean
import uz.unnarsx.cherrygram.preferences.int
import uz.unnarsx.cherrygram.vkui.icon_replaces.BaseIconReplace
import uz.unnarsx.cherrygram.vkui.icon_replaces.NoIconReplace
import uz.unnarsx.cherrygram.vkui.icon_replaces.VkIconReplace

object CherrygramConfig {

    private val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
    // Appearance Settings
    // Redesign
    /*var change_Icon by sharedPreferences.int("AP_ChangeIcon", 0)
    var change_Icon2 by sharedPreferences.int("AP_ChangeIcon2", 0)*/

    var iconReplacement by sharedPreferences.int("AP_IconReplacements", 0)
    fun getIconReplacement(): BaseIconReplace {
        return when (iconReplacement) {
            1 -> NoIconReplace()
            else -> VkIconReplace()
        }
    }

    var flatActionbar by sharedPreferences.boolean("AP_FlatSB", true)
    var BackButton by sharedPreferences.boolean("AP_BackButton", false)
    var systemFonts by sharedPreferences.boolean("AP_SystemFonts", true)
    //Folders
    var folderNameInHeader by sharedPreferences.boolean("AP_FolderNameInHeader", false)
    var newTabs_noUnread by sharedPreferences.boolean("CP_NewTabs_NoCounter", false)
    var showTabsOnForward by sharedPreferences.boolean("CP_ShowTabsOnForward", true)
    var filledIcons by sharedPreferences.boolean("AP_FilledIcons", false)

    const val TAB_TYPE_TEXT = 0
    const val TAB_TYPE_MIX = 1
    const val TAB_TYPE_ICON = 2

    var tabMode by sharedPreferences.int("CG_FoldersType", 0)
    @JvmName("setTabMode1")
    fun setTabMode(mode: Int) {
        tabMode = mode
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "owlconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putInt("tabMode", tabMode)
        editor.apply()
    }
    // Drawer
    var drawerAvatar by sharedPreferences.boolean("AP_DrawerAvatar", true)
    var drawerBlur by sharedPreferences.boolean("AP_DrawerBlur", true)
    var drawerDarken by sharedPreferences.boolean("AP_DrawerDarken", true)
    // Drawer buttons
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
    }

    var eventType by sharedPreferences.int("eventType", 0)
    @JvmName("eventType")
    fun setEventType(event: Int) {
        eventType = event
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "mainconfig",
            Activity.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.putInt("eventType", eventType)
        editor.apply()
    }
    // Profile and Contacts
    var hidePhoneNumber by sharedPreferences.boolean("AP_HideUserPhone", false)
    var mutualContacts by sharedPreferences.boolean("AP_MutualContacts", true)
    var showId by sharedPreferences.boolean("AP_ShowID", false)
    var showDc by sharedPreferences.boolean("AP_ShowDC", false)

    // Chats Settings
    // Stickers
    var slider_stickerAmplifier by sharedPreferences.int("CP_Slider_StickerAmplifier", 100)
    var hideStickerTime by sharedPreferences.boolean("CP_TimeOnStick", false)
    //Chats
    var unreadBadgeOnBackButton by sharedPreferences.boolean("CP_UnreadBadgeOnBackButton", false)
    var noRounding by sharedPreferences.boolean("CP_NoRounding", false)
    var confirmCalls by sharedPreferences.boolean("CP_ConfirmCalls", false)
    var msgForwardDate by sharedPreferences.boolean("CP_ForwardMsgDate", false)
    var showSeconds by sharedPreferences.boolean("CP_ShowSeconds", false)
    var disableDoubleTabReact by sharedPreferences.boolean("CP_DoubleTapReact", false)
    var disableReactionAnim by sharedPreferences.boolean("CP_DisableReactionAnim", false)
    var disableSwipeToNext by sharedPreferences.boolean("CP_DisableSwipeToNext", false)
    var hideKeyboardOnScroll by sharedPreferences.boolean("CP_HideKbdOnScroll", false)
    var hideSendAsChannel by sharedPreferences.boolean("CP_HideSendAsChannel", false)
    var slider_RecentEmojisAmplifier by sharedPreferences.int("CP_Slider_RecentEmojisAmplifier", 45)
    var slider_RecentStickersAmplifier by sharedPreferences.int("CP_Slider_RecentStickersAmplifier", 20)
    // Media
    var playGIFasVideo by sharedPreferences.boolean("CP_GIFasVideo", true)
    var playVideoOnVolume by sharedPreferences.boolean("CP_PlayVideo", false)
    var autoPauseVideo by sharedPreferences.boolean("CP_AutoPauseVideo", false)
    var audioFocus by sharedPreferences.boolean("CP_AudioFocus", false)
    var disableVibration by sharedPreferences.boolean("CP_DisableVibration", false)
    var disablePhotoTapAction by sharedPreferences.boolean("CP_DisablePhotoTapAction", false)
    var disableAttachCamera by sharedPreferences.boolean("CP_DisableCam", false)
    var rearCam by sharedPreferences.boolean("CP_RearCam", false)
    var enableProximity by sharedPreferences.boolean("CP_Proximity", true)
    //Notifications
    var iosSound by sharedPreferences.boolean("CP_IOSSound", false)
    var silenceNonContacts by sharedPreferences.boolean("CP_SilenceNonContacts", false)

    //Privacy
    var hideProxySponsor by sharedPreferences.boolean("SP_NoProxyPromo", true)
    var kaboom by sharedPreferences.boolean("SP_Kaboom", false)

    //OTA
    var autoOTA by sharedPreferences.boolean("CG_Auto_OTA", true)

    //Misc
    var forwardNoAuthorship by sharedPreferences.boolean("CG_ForwardNoAuthorship", false)
    var forwardWithoutCaptions by sharedPreferences.boolean("CG_ForwardWithoutCaptions", false)
    var forwardNotify by sharedPreferences.boolean("CG_ForwardNotify", true)
    var noAuthorship by sharedPreferences.boolean("CG_NoAuthorship", false)

    var cameraType by sharedPreferences.int("cameraType", CameraXUtilities.getDefault());
    fun saveCameraType(type: Int) {
        cameraType = type
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt("cameraType", cameraType)
        editor.apply()
    }

    var cameraXFps by sharedPreferences.int("cameraXFps",
        if (SharedConfig.getDevicePerformanceClass() == SharedConfig.PERFORMANCE_CLASS_HIGH) 60 else 30
    )
    fun saveCameraXFps(fps: Int) {
        cameraXFps = fps
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt("cameraXFps", cameraXFps)
        editor.apply()
    }

    var useCameraXOptimizedMode by sharedPreferences.boolean("useCameraXOptimizedMode", /*false*/SharedConfig.getDevicePerformanceClass() != SharedConfig.PERFORMANCE_CLASS_HIGH);
    fun toggleCameraXOptimizedMode() {
        useCameraXOptimizedMode = !useCameraXOptimizedMode
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean("useCameraXOptimizedMode", useCameraXOptimizedMode)
        editor.apply()
    }

    /*init {
        CherrygramToasts.init(sharedPreferences)
        fuckOff()
    }

    private fun fuckOff() {
        val good = "30820311308201f9a0030201020204019cc993300d06092a864886f70d01010b05003039311730150603550403130e4361746f6772616d2044656275673111300f060355040a13084361746f6772616d310b3009060355040613025553301e170d3231303130343139343631395a170d3438303532323139343631395a3039311730150603550403130e4361746f6772616d2044656275673111300f060355040a13084361746f6772616d310b300906035504061302555330820122300d06092a864886f70d01010105000382010f003082010a0282010100b7110aa72a8436c77137971b0dff973799637219f0cfc415a8956309dfd4ea153bd1f8867d981d25d29b7f9e7bc123af03f829520135cee3c90e11338dc9b06f08dac8db85c4232b9daacd76d9d08abecba93981065fbef6e3be979851a843305ae7454fa69c40d174ecc98b6cea0ec95ab83e9de4938c5eca1f689460944ccf13cff85c3db28d276c74a9972ffce529d769bfc39197d39896158fb2c75d536dbc66307c3a100994415685e27a1fa3b6078ba4ce72a689192f8f8433649c4b1ce5a64807d0a5974241b51ab3265d524de544f67fa5cf1c0f9569a041fa5eb64138467d68406cf982f63d0e7c22c22a25518347da1f157a6ba41f3c6e91420ad10203010001a321301f301d0603551d0e0416041491fd9440a4ddf35ebb4e5783baa30e80a4aa0753300d06092a864886f70d01010b050003820101006dbcc10cc3190c4c5f99fac9410dce10d598e494052bc894d4de09b1bf4fc186f53b8a31d3ef47003d65f01b127a0ab9e274ab5b577e2d4bcb9305f1dc0131640e3c0c83a5230df34fa18a693819966540ad80c2e96c66458a5ae4010aa5591a6eb16c96e28dc2ac23a41fd464aed31aa9ee62b0bc755908944f80dcd45f8f81f439cec6a20c1a21a35360ba8dc37b23b98b203716477aca09e9f48c071ba898fceaed278b9f128b2eca7d3172191438a873c84519d5312b71aa1557bba544dae1150928c3bb9152955de7dc7810ef31d1b4e198595a43596fb96c410a5c7604d35acde21c75bde970de79f44c55a9b84a5496539e9f53a83fc7059770929987"
        val info = ApplicationLoader.applicationContext.packageManager.getPackageInfo("uz.unnarsx.cherrygram", PackageManager.GET_SIGNATURES).signatures[0].toCharsString()
        if (info != good) {
            exitProcess(0)
        }
    }*/
}
