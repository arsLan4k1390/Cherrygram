package uz.unnarsx.cherrygram.core.configs

import android.app.Activity
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.SharedConfig
import uz.unnarsx.cherrygram.core.icons.icon_replaces.BaseIconReplace
import uz.unnarsx.cherrygram.core.icons.icon_replaces.NoIconReplace
import uz.unnarsx.cherrygram.core.icons.icon_replaces.SolarIconReplace
import uz.unnarsx.cherrygram.core.icons.icon_replaces.VkIconReplace
import uz.unnarsx.cherrygram.preferences.boolean
import uz.unnarsx.cherrygram.preferences.int

object CherrygramAppearanceConfig: CoroutineScope by CoroutineScope(
    context = SupervisorJob() + Dispatchers.Main.immediate
) {

    private val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)

    fun putBoolean(key: String, value: Boolean) {
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    /** Redesign start **/
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
    /** Redesign finish **/

    /** Messages and profiles start **/
    var showSeconds by sharedPreferences.boolean("CP_ShowSeconds", false)
    fun toggleShowSeconds() {
        showSeconds = !showSeconds
        putBoolean("CP_ShowSeconds", showSeconds)
    }

    var disablePremiumStatuses by sharedPreferences.boolean("CP_DisablePremiumStatuses", SharedConfig.getDevicePerformanceClass() == SharedConfig.PERFORMANCE_CLASS_LOW)
    fun toggleDisablePremiumStatuses() {
        disablePremiumStatuses = !disablePremiumStatuses
        putBoolean("CP_DisablePremiumStatuses", disablePremiumStatuses)
    }

    var replyBackground by sharedPreferences.boolean("CP_ReplyBackground", SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_AVERAGE)
    fun toggleReplyBackground() {
        replyBackground = !replyBackground
        putBoolean("CP_ReplyBackground", replyBackground)
    }

    var replyCustomColors by sharedPreferences.boolean("CP_ReplyCustomColors", SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_AVERAGE)
    fun toggleReplyCustomColors() {
        replyCustomColors = !replyCustomColors
        putBoolean("CP_ReplyCustomColors", replyCustomColors)
    }

    var replyBackgroundEmoji by sharedPreferences.boolean("CP_ReplyBackgroundEmoji", SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_AVERAGE)
    fun toggleReplyBackgroundEmoji() {
        replyBackgroundEmoji = !replyBackgroundEmoji
        putBoolean("CP_ReplyBackgroundEmoji", replyBackgroundEmoji)
    }

    var profileChannelPreview by sharedPreferences.boolean("CP_ProfileChannelPreview", true)
    fun toggleProfileChannelPreview() {
        profileChannelPreview = !profileChannelPreview
        putBoolean("CP_ProfileChannelPreview", profileChannelPreview)
    }

    const val ID_DC_NONE = 0
    const val ID_ONLY = 1
    const val ID_DC = 2
    var showIDDC by sharedPreferences.int("AP_ShowID_DC", ID_DC_NONE)

    var profileBirthDatePreview by sharedPreferences.boolean("CP_ProfileBirthDatePreview", true)
    fun toggleProfileBirthDatePreview() {
        profileBirthDatePreview = !profileBirthDatePreview
        putBoolean("CP_ProfileBirthDatePreview", profileBirthDatePreview)
    }

    var profileBusinessPreview by sharedPreferences.boolean("CP_ProfileBusinessPreview", true)
    fun toggleProfileBusinessPreview() {
        profileBusinessPreview = !profileBusinessPreview
        putBoolean("CP_ProfileBusinessPreview", profileBusinessPreview)
    }

    var profileBackgroundColor by sharedPreferences.boolean("CP_ProfileBackgroundColor", SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_AVERAGE)
    fun toggleProfileBackgroundColor() {
        profileBackgroundColor = !profileBackgroundColor
        putBoolean("CP_ProfileBackgroundColor", profileBackgroundColor)
    }

    var profileBackgroundEmoji by sharedPreferences.boolean("CP_ProfileBackgroundEmoji", SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_AVERAGE)
    fun toggleProfileBackgroundEmoji() {
        profileBackgroundEmoji = !profileBackgroundEmoji
        putBoolean("CP_ProfileBackgroundEmoji", profileBackgroundEmoji)
    }
    /** Messages and profiles finish **/

    /** Folders start **/
    var folderNameInHeader by sharedPreferences.boolean("AP_FolderNameInHeader", false)
    fun toggleFolderNameInHeader() {
        folderNameInHeader = !folderNameInHeader
        putBoolean("AP_FolderNameInHeader", folderNameInHeader)
    }

    var tabsHideAllChats by sharedPreferences.boolean("CP_NewTabs_RemoveAllChats", false)
    fun toggleTabsHideAllChats() {
        tabsHideAllChats = !tabsHideAllChats
        putBoolean("CP_NewTabs_RemoveAllChats", tabsHideAllChats)
    }

    var tabsNoUnread by sharedPreferences.boolean("CP_NewTabs_NoCounter", false)
    fun toggleTabsNoUnread() {
        tabsNoUnread = !tabsNoUnread
        putBoolean("CP_NewTabs_NoCounter", tabsNoUnread)
    }

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
    fun toggleTabStyleStroke() {
        tabStyleStroke = !tabStyleStroke
        putBoolean("AP_TabStyleAddStroke", tabStyleStroke)
    }
    /** Folders finish **/

    /** Drawer start **/
    var drawSnowInDrawer by sharedPreferences.boolean("AP_DrawSnowInDrawer", false)
    fun toggleDrawerSnow() {
        drawSnowInDrawer = !drawSnowInDrawer
        putBoolean("AP_DrawSnowInDrawer", drawSnowInDrawer)
    }

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

    var drawerBlur by sharedPreferences.boolean("AP_DrawerBlur", SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_AVERAGE)
    fun toggleDrawerBlur() {
        drawerBlur = !drawerBlur
        putBoolean("AP_DrawerBlur", drawerBlur)
    }

    var drawerBlurIntensity by sharedPreferences.int("AP_DrawerBlur_Intensity", 50)
    var eventType by sharedPreferences.int("AP_DrawerEventType", 0)

    /** Drawer buttons start **/
    var changeStatusDrawerButton by sharedPreferences.boolean("AP_ChangeStatusDrawerButton", true)
    fun toggleChangeStatusDrawerButton() {
        changeStatusDrawerButton = !changeStatusDrawerButton
        putBoolean("AP_ChangeStatusDrawerButton", changeStatusDrawerButton)
    }

    /*var myStoriesDrawerButton by sharedPreferences.boolean("AP_MyStoriesDrawerButton", true)
    fun toggleMyStoriesDrawerButton() {
        myStoriesDrawerButton = !myStoriesDrawerButton
        putBoolean("AP_MyStoriesDrawerButton", myStoriesDrawerButton)
    }*/

    var myProfileDrawerButton by sharedPreferences.boolean("AP_MyProfileDrawerButton", true)
    fun toggleMyProfileDrawerButton() {
        myProfileDrawerButton = !myProfileDrawerButton
        putBoolean("AP_MyProfileDrawerButton", myProfileDrawerButton)
    }

    var createGroupDrawerButton by sharedPreferences.boolean("AP_CreateGroupDrawerButton", false)
    fun toggleCreateGroupDrawerButton() {
        createGroupDrawerButton = !createGroupDrawerButton
        putBoolean("AP_CreateGroupDrawerButton", createGroupDrawerButton)
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
    /** Drawer buttons finish **/
    /** Drawer finish **/

    /** Snowflakes start **/
    var drawSnowInActionBar by sharedPreferences.boolean("AP_DrawSnowInActionBar", false)
    var drawSnowInChat by sharedPreferences.boolean("AP_DrawSnowInChat", false)
    /** Snowflakes finish **/

}