package uz.unnarsx.cherrygram

import uz.unnarsx.cherrygram.preferences.*
import uz.unnarsx.tgkit.TGKitSettingsFragment

object CherrygramPreferencesNavigator {
    @JvmStatic
    fun createMainMenu() = TGKitSettingsFragment(MainPreferencesEntry())

    fun createChats() = TGKitSettingsFragment(ChatsPreferencesEntry())
    fun createAppearance() = TGKitSettingsFragment(AppearancePreferencesEntry())
    fun createSecurity() = TGKitSettingsFragment(SecurityPreferencesEntry())
//    fun createDB() = TGKitSettingsFragment(DoubleBottomPreferencesEntry())
    fun createUpdates() = TGKitSettingsFragment(UpdatesPreferenceEntry())
    fun createDonate() = TGKitSettingsFragment(DonatePreferenceEntry())
}