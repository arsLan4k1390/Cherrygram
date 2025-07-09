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
    var disableDividers by sharedPreferences.boolean("AP_DisableDividers", true)
    var centerTitle by sharedPreferences.boolean("AP_CenterTitle", true)
    var iosSearchPanel by sharedPreferences.boolean("AP_iosSearchPanel", false)
    var disableToolBarShadow by sharedPreferences.boolean("AP_ToolBarShadow", true)
    var overrideHeaderColor by sharedPreferences.boolean("AP_OverrideHeaderColor", true)
    var flatNavbar by sharedPreferences.boolean("AP_FlatNavBar", true)
    /** Redesign finish **/

    /** Messages and profiles start **/
    var showSeconds by sharedPreferences.boolean("CP_ShowSeconds", false)
    var disablePremiumStatuses by sharedPreferences.boolean("CP_DisablePremiumStatuses", SharedConfig.getDevicePerformanceClass() == SharedConfig.PERFORMANCE_CLASS_LOW)
    var replyBackground by sharedPreferences.boolean("CP_ReplyBackground", SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_AVERAGE)
    var replyCustomColors by sharedPreferences.boolean("CP_ReplyCustomColors", SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_AVERAGE)
    var replyBackgroundEmoji by sharedPreferences.boolean("CP_ReplyBackgroundEmoji", SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_AVERAGE)
    var profileChannelPreview by sharedPreferences.boolean("CP_ProfileChannelPreview", true)

    const val ID_DC_NONE = 0
    const val ID_ONLY = 1
    const val ID_DC = 2
    var showIDDC by sharedPreferences.int("AP_ShowID_DC", ID_DC_NONE)

    var profileBirthDatePreview by sharedPreferences.boolean("CP_ProfileBirthDatePreview", true)
    var profileBusinessPreview by sharedPreferences.boolean("CP_ProfileBusinessPreview", true)
    var profileBackgroundColor by sharedPreferences.boolean("CP_ProfileBackgroundColor", SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_AVERAGE)
    var profileBackgroundEmoji by sharedPreferences.boolean("CP_ProfileBackgroundEmoji", SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_AVERAGE)
    /** Messages and profiles finish **/

    /** Folders start **/
    var folderNameInHeader by sharedPreferences.boolean("AP_FolderNameInHeader", false)
    var tabsHideAllChats by sharedPreferences.boolean("CP_NewTabs_RemoveAllChats", false)
    var tabsNoUnread by sharedPreferences.boolean("CP_NewTabs_NoCounter", false)

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
    /** Folders finish **/

    /** Drawer start **/
    var drawSnowInDrawer by sharedPreferences.boolean("AP_DrawSnowInDrawer", false)
    var drawerAvatar by sharedPreferences.boolean("AP_DrawerAvatar", true)
    var drawerSmallAvatar by sharedPreferences.boolean("AP_DrawerSmallAvatar", false)
    var drawerDarken by sharedPreferences.boolean("AP_DrawerDarken", true)
    var drawerGradient by sharedPreferences.boolean("AP_DrawerGradient", false)
    var drawerBlur by sharedPreferences.boolean("AP_DrawerBlur", SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_AVERAGE)
    var drawerBlurIntensity by sharedPreferences.int("AP_DrawerBlur_Intensity", 50)

    /** Drawer buttons start **/
    var changeStatusDrawerButton by sharedPreferences.boolean("AP_ChangeStatusDrawerButton", true)
    /*var myStoriesDrawerButton by sharedPreferences.boolean("AP_MyStoriesDrawerButton", true)*/
    var myProfileDrawerButton by sharedPreferences.boolean("AP_MyProfileDrawerButton", true)
    var createGroupDrawerButton by sharedPreferences.boolean("AP_CreateGroupDrawerButton", false)
    var createChannelDrawerButton by sharedPreferences.boolean("AP_CreateChannelDrawerButton", false)
    var contactsDrawerButton by sharedPreferences.boolean("AP_ContactsDrawerButton", false)
    var callsDrawerButton by sharedPreferences.boolean("AP_CallsDrawerButton", true)
    var savedMessagesDrawerButton by sharedPreferences.boolean("AP_SavedMessagesDrawerButton", true)
    var archivedChatsDrawerButton by sharedPreferences.boolean("AP_ArchivedChatsDrawerButton", true)
    var scanQRDrawerButton by sharedPreferences.boolean("AP_ScanQRDrawerButton", true)
    var cGPreferencesDrawerButton by sharedPreferences.boolean("AP_CGPreferencesDrawerButton", true)
    /** Drawer buttons finish **/

    var eventType by sharedPreferences.int("AP_DrawerEventType", 0)
    /** Drawer finish **/

    /** Snowflakes start **/
    var drawSnowInActionBar by sharedPreferences.boolean("AP_DrawSnowInActionBar", false)
    var drawSnowInChat by sharedPreferences.boolean("AP_DrawSnowInChat", false)
    /** Snowflakes finish **/

}