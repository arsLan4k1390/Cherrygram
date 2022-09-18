package uz.unnarsx.cherrygram

import uz.unnarsx.cherrygram.preferences.*
import uz.unnarsx.tgkit.TGKitSettingsFragment

object CherrygramPreferencesNavigator {
    @JvmStatic
    fun createMainMenu() = TGKitSettingsFragment(MainPreferencesEntry())

    fun createAppearance() = TGKitSettingsFragment(AppearancePreferencesEntry())
    fun createChats() = TGKitSettingsFragment(ChatsPreferencesEntry())
    fun createSecurity() = TGKitSettingsFragment(SecurityPreferencesEntry())
    fun createDonate() = TGKitSettingsFragment(DonatePreferenceEntry())
}