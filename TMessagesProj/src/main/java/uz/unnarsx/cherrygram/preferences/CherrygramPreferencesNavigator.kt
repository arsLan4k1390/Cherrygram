/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.preferences

import org.telegram.ui.ActionBar.BaseFragment
import uz.unnarsx.cherrygram.preferences.drawer.DrawerPreferencesEntry
import uz.unnarsx.cherrygram.preferences.folders.FoldersPreferencesEntry
import uz.unnarsx.cherrygram.preferences.tgkit.TGKitSettingsFragment

object CherrygramPreferencesNavigator {

    fun createMainMenu() = TGKitSettingsFragment(MainPreferencesEntry())
    fun createGeneral() = TGKitSettingsFragment(GeneralPreferencesEntry())
    fun createAppearance() = TGKitSettingsFragment(AppearancePreferencesEntry())
    fun createMessagesAndProfiles(fragment: BaseFragment) = fragment.presentFragment(MessagesAndProfilesPreferencesEntry())
    fun createFoldersPrefs(fragment: BaseFragment) = fragment.presentFragment(FoldersPreferencesEntry())
    fun createDrawerPrefs(fragment: BaseFragment) = fragment.presentFragment(DrawerPreferencesEntry())
    fun createDrawerItems(fragment: BaseFragment) = DrawerPreferencesEntry.showDrawerItemsSelector(fragment)
    fun createChats() = TGKitSettingsFragment(ChatsPreferencesEntry)
    fun createGemini(fragment: BaseFragment): GeminiPreferencesBottomSheet = GeminiPreferencesBottomSheet.showAlert(fragment)
    fun createMessageFilter(fragment: BaseFragment) = fragment.presentFragment(MessageFiltersPreferencesEntry())
    fun createMessageMenu(fragment: BaseFragment) = fragment.presentFragment(MessageMenuPreferencesEntry())
    fun createCamera(fragment: BaseFragment) = fragment.presentFragment(CameraPreferencesEntry())
    fun createExperimental(fragment: BaseFragment) = fragment.presentFragment(ExperimentalPreferencesEntry())
    fun createPrivacyAndSecurity() = TGKitSettingsFragment(PrivacyAndSecurityPreferencesEntry())
    fun createDonate(fragment: BaseFragment) = fragment.presentFragment(DonatesPreferencesEntry())
    fun createDonateForce(fragment: BaseFragment) = fragment.presentFragment(DonatesPreferencesEntry().forceShowDonates())
    fun createAbout() = TGKitSettingsFragment(AboutPreferencesEntry())
    fun createDebug() = TGKitSettingsFragment(DebugPreferencesEntry())
    fun createBlur(fragment: BaseFragment) = BlurPreferencesBottomSheet.show(fragment)

}