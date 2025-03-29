/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.preferences.tgkit

import uz.unnarsx.cherrygram.preferences.AboutPreferencesEntry
import uz.unnarsx.cherrygram.preferences.AppearancePreferencesEntry
import uz.unnarsx.cherrygram.preferences.ChatsPreferencesEntry
import uz.unnarsx.cherrygram.preferences.DebugPreferencesEntry
import uz.unnarsx.cherrygram.preferences.DonatePreferenceEntry
import uz.unnarsx.cherrygram.preferences.GeneralPreferencesEntry
import uz.unnarsx.cherrygram.preferences.MainPreferencesEntry
import uz.unnarsx.cherrygram.preferences.PrivacyAndSecurityPreferencesEntry

object CherrygramPreferencesNavigator {
    @JvmStatic
    fun createMainMenu() = TGKitSettingsFragment(MainPreferencesEntry())
    fun createGeneral() = TGKitSettingsFragment(GeneralPreferencesEntry())
    fun createAppearance() = TGKitSettingsFragment(AppearancePreferencesEntry())
    fun createChats() = TGKitSettingsFragment(ChatsPreferencesEntry)
    fun createPrivacyAndSecurity() = TGKitSettingsFragment(PrivacyAndSecurityPreferencesEntry())
    fun createDonate() = TGKitSettingsFragment(DonatePreferenceEntry())
    fun createDebug() = TGKitSettingsFragment(DebugPreferencesEntry())
    fun createAbout() = TGKitSettingsFragment(AboutPreferencesEntry())
}