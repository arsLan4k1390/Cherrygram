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
import uz.unnarsx.cherrygram.donates.adsgram.AdsScreen
import uz.unnarsx.cherrygram.preferences.folders.FoldersPreferencesEntry
import uz.unnarsx.cherrygram.preferences.tabs.MainTabsPreferencesEntry

object CherrygramPreferencesNavigator {

    fun createCherrySettings(fragment: BaseFragment): BaseFragment =
        CGPreferencesEntry().also(fragment::presentFragment)

    fun createGeneral(fragment: BaseFragment): BaseFragment =
        GeneralPreferencesEntry().also(fragment::presentFragment)

    fun createAppearance(fragment: BaseFragment): BaseFragment =
        AppearancePreferencesEntry().also(fragment::presentFragment)

    fun createFoldersPrefs(fragment: BaseFragment): BaseFragment =
        FoldersPreferencesEntry().also(fragment::presentFragment)

    fun createTabs(fragment: BaseFragment): BaseFragment =
        MainTabsPreferencesEntry().also(fragment::presentFragment)

    fun createMessagesAndProfiles(fragment: BaseFragment): BaseFragment =
        MessagesAndProfilesPreferencesEntry().also(fragment::presentFragment)

    fun createChats(fragment: BaseFragment): BaseFragment =
        ChatsPreferencesEntry().also(fragment::presentFragment)

    fun createMessages(fragment: BaseFragment): BaseFragment =
        MessagesPreferencesEntry().also(fragment::presentFragment)

    fun createGemini(fragment: BaseFragment): BaseFragment =
        GeminiPreferencesEntry().also(fragment::presentFragment)

    fun createMessageFilter(fragment: BaseFragment): BaseFragment =
        MessageFiltersPreferencesEntry().also(fragment::presentFragment)

    fun createMessageMenu(fragment: BaseFragment): BaseFragment =
        MessageMenuPreferencesEntry().also(fragment::presentFragment)

    fun createCamera(fragment: BaseFragment): BaseFragment =
        CameraPreferencesEntry().also(fragment::presentFragment)

    fun createExperimental(fragment: BaseFragment): BaseFragment =
        ExperimentalPreferencesEntry().also(fragment::presentFragment)

    fun createPrivacy(fragment: BaseFragment): BaseFragment =
        PrivacyPreferencesEntry().also(fragment::presentFragment)

    @JvmOverloads
    fun createDonate(fragment: BaseFragment, force: Boolean = false): BaseFragment =
        DonatesPreferencesEntry().forceShowDonates(force).also(fragment::presentFragment)

    fun createAlternativeSupport(fragment: BaseFragment): BaseFragment =
        AlternativeSupportScreen().also(fragment::presentFragment)

    fun createStars(fragment: BaseFragment, customTitle: String?, userName: String?, type: Int) = fragment.presentFragment(StarsIntroActivityCG(customTitle, userName, type))

    fun createADS(fragment: BaseFragment): BaseFragment =
        AdsScreen().also(fragment::presentFragment)

    fun createAbout(fragment: BaseFragment): BaseFragment =
        AboutPreferencesEntry().also(fragment::presentFragment)

    fun createDebug(fragment: BaseFragment): BaseFragment =
        DebugPreferencesEntry().also(fragment::presentFragment)

}