package uz.unnarsx.cherrygram.tgkit

import uz.unnarsx.cherrygram.preferences.*

object CherrygramPreferencesNavigator {
    @JvmStatic
    fun createMainMenu() = TGKitSettingsFragment(MainPreferencesEntry())
    fun createGeneral() = TGKitSettingsFragment(GeneralPreferencesEntry())
    fun createAppearance() = TGKitSettingsFragment(AppearancePreferencesEntry())
    fun createChats() = TGKitSettingsFragment(ChatsPreferencesEntry())
    fun createSecurity() = TGKitSettingsFragment(SecurityPreferencesEntry())
    fun createDonate() = TGKitSettingsFragment(DonatePreferenceEntry())
}