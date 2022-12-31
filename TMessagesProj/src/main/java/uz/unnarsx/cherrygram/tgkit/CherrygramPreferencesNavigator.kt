package uz.unnarsx.cherrygram.tgkit

import uz.unnarsx.cherrygram.preferences.*
import uz.unnarsx.cherrygram.preferences.drawer.DrawerIconsPreferencesEntry

object CherrygramPreferencesNavigator {
    @JvmStatic
    fun createMainMenu() = TGKitSettingsFragment(MainPreferencesEntry())
    fun createAppearance() = TGKitSettingsFragment(AppearancePreferencesEntry())
    fun createDrawerIcons() = TGKitSettingsFragment(DrawerIconsPreferencesEntry())
    fun createChats() = TGKitSettingsFragment(ChatsPreferencesEntry())
    fun createSecurity() = TGKitSettingsFragment(SecurityPreferencesEntry())
    fun createDonate() = TGKitSettingsFragment(DonatePreferenceEntry())
}