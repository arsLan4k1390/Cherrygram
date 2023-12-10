package uz.unnarsx.cherrygram.ui.tgkit

import uz.unnarsx.cherrygram.preferences.AboutPreferencesEntry
import uz.unnarsx.cherrygram.preferences.AppearancePreferencesEntry
import uz.unnarsx.cherrygram.preferences.ChatsPreferencesEntry
import uz.unnarsx.cherrygram.preferences.DonatePreferenceEntry
import uz.unnarsx.cherrygram.preferences.GeneralPreferencesEntry
import uz.unnarsx.cherrygram.preferences.MainPreferencesEntry
import uz.unnarsx.cherrygram.preferences.PrivacyAndSecurityPreferencesEntry

object CherrygramPreferencesNavigator {
    @JvmStatic
    fun createMainMenu() = TGKitSettingsFragment(MainPreferencesEntry())
    fun createGeneral() = TGKitSettingsFragment(GeneralPreferencesEntry())
    fun createAppearance() = TGKitSettingsFragment(AppearancePreferencesEntry())
    fun createChats() = TGKitSettingsFragment(ChatsPreferencesEntry())
    fun createPrivacyAndSecurity() = TGKitSettingsFragment(PrivacyAndSecurityPreferencesEntry())
    fun createDonate() = TGKitSettingsFragment(DonatePreferenceEntry())
    fun createAbout() = TGKitSettingsFragment(AboutPreferencesEntry())
}