package uz.unnarsx.cherrygram.tgkit

import uz.unnarsx.cherrygram.preferences.AppearancePreferencesEntry
import uz.unnarsx.cherrygram.preferences.ChatsPreferencesEntry
import uz.unnarsx.cherrygram.preferences.DonatePreferenceEntry
import uz.unnarsx.cherrygram.preferences.GeneralPreferencesEntry
import uz.unnarsx.cherrygram.preferences.MainPreferencesEntry
import uz.unnarsx.cherrygram.preferences.SecurityPreferencesEntry

object CherrygramPreferencesNavigator {
    @JvmStatic
    fun createMainMenu() = TGKitSettingsFragment(MainPreferencesEntry())
    fun createGeneral() = TGKitSettingsFragment(GeneralPreferencesEntry())
    fun createAppearance() = TGKitSettingsFragment(AppearancePreferencesEntry())
    fun createChats() = TGKitSettingsFragment(ChatsPreferencesEntry())
    fun createSecurity() = TGKitSettingsFragment(SecurityPreferencesEntry())
    fun createDonate() = TGKitSettingsFragment(DonatePreferenceEntry())
}