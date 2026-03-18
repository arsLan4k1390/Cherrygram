/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.preferences

import org.telegram.ui.ActionBar.BaseFragment
import uz.unnarsx.cherrygram.preferences.folders.FoldersPreferencesEntry
import uz.unnarsx.cherrygram.preferences.tabs.MainTabsPreferencesEntry

object CherrygramPreferencesNavigator {

    fun createCherrySettings(fragment: BaseFragment) = fragment.presentFragment(CGPreferencesEntry())

    fun createGeneral(fragment: BaseFragment) = fragment.presentFragment(GeneralPreferencesEntry())

    fun createAppearance(fragment: BaseFragment) = fragment.presentFragment(AppearancePreferencesEntry())
    fun createFoldersPrefs(fragment: BaseFragment) = fragment.presentFragment(FoldersPreferencesEntry())
    fun createTabs(fragment: BaseFragment) = fragment.presentFragment(MainTabsPreferencesEntry())
    fun createMessagesAndProfiles(fragment: BaseFragment) = fragment.presentFragment(MessagesAndProfilesPreferencesEntry())

    fun createChats(fragment: BaseFragment) = fragment.presentFragment(ChatsPreferencesEntry())
    fun createMessages(fragment: BaseFragment) = fragment.presentFragment(MessagesPreferencesEntry())
    fun createGemini(fragment: BaseFragment) = fragment.presentFragment(GeminiPreferencesEntry())
    fun createMessageFilter(fragment: BaseFragment) = fragment.presentFragment(MessageFiltersPreferencesEntry())
    fun createMessageMenu(fragment: BaseFragment) = fragment.presentFragment(MessageMenuPreferencesEntry())

    fun createCamera(fragment: BaseFragment) = fragment.presentFragment(CameraPreferencesEntry())

    fun createExperimental(fragment: BaseFragment) = fragment.presentFragment(ExperimentalPreferencesEntry())

    fun createPrivacy(fragment: BaseFragment) = fragment.presentFragment(PrivacyPreferencesEntry())

    @JvmOverloads
    fun createDonate(fragment: BaseFragment, force: Boolean = false) = fragment.presentFragment(DonatesPreferencesEntry().forceShowDonates(force))
    fun createStars(fragment: BaseFragment, customTitle: String?, userName: String?, type: Int) = fragment.presentFragment(StarsIntroActivityCG(customTitle, userName, type))

    fun createAbout(fragment: BaseFragment) = fragment.presentFragment(AboutPreferencesEntry())
    fun createDebug(fragment: BaseFragment) = fragment.presentFragment(DebugPreferencesEntry())

}