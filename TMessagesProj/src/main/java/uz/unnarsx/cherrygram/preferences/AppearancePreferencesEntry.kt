/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.preferences

import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.LaunchActivity
import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig
import uz.unnarsx.cherrygram.preferences.tgkit.preference.category
import uz.unnarsx.cherrygram.preferences.tgkit.preference.contract
import uz.unnarsx.cherrygram.preferences.tgkit.preference.list
import uz.unnarsx.cherrygram.preferences.tgkit.preference.switch
import uz.unnarsx.cherrygram.preferences.tgkit.preference.textIcon
import uz.unnarsx.cherrygram.preferences.tgkit.preference.tgKitScreen
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitTextIconRow
import org.telegram.ui.Components.RecyclerListView
import uz.unnarsx.cherrygram.core.crashlytics.FirebaseAnalyticsHelper
import uz.unnarsx.cherrygram.core.helpers.DeeplinkHelper
import uz.unnarsx.cherrygram.core.ui.CGBulletinCreator
import uz.unnarsx.cherrygram.preferences.tabs.MainTabsPreferencesEntry
import java.lang.ref.WeakReference

class AppearancePreferencesEntry : BasePreferencesEntry {

    private var listViewRef: WeakReference<RecyclerListView>? = null

    override fun setListView(rv: RecyclerListView) {
        listViewRef = WeakReference(rv)
    }

    fun getListView(): RecyclerListView? {
        return listViewRef?.get()
    }

    override fun getPreferences(bf: BaseFragment) = tgKitScreen(getString(R.string.AP_Header_Appearance)) {
        category(getString(R.string.AP_RedesignCategory)) {
            list {
                title = getString(R.string.AP_IconReplacements)

                contract({
                    return@contract listOf(
                        Pair(CherrygramAppearanceConfig.ICON_REPLACE_NONE, getString(R.string.AP_IconReplacement_Default)),
                        Pair(CherrygramAppearanceConfig.ICON_REPLACE_SOLAR, getString(R.string.AP_IconReplacement_Solar))
                    )
                }, {
                    return@contract when (CherrygramAppearanceConfig.iconReplacement) {
                        CherrygramAppearanceConfig.ICON_REPLACE_SOLAR -> getString(R.string.AP_IconReplacement_Solar)
                        else -> getString(R.string.AP_IconReplacement_Default)
                    }
                }) {
                    CherrygramAppearanceConfig.iconReplacement = it

                    (bf.parentActivity as? LaunchActivity)?.reloadResources()
                    Theme.reloadAllResources(bf.parentActivity)
                    bf.parentLayout.rebuildAllFragmentViews(false, false)
                }
            }
            switch {
                title = getString(R.string.AP_OneUI_Switch_Style)

                contract({
                    return@contract CherrygramAppearanceConfig.oneUI_SwitchStyle
                }) {
                    CherrygramAppearanceConfig.oneUI_SwitchStyle = it
                    getListView()?.post {
                        getListView()!!.adapter?.notifyDataSetChanged()
                    }
                }
            }
            switch {
                title = getString(R.string.AP_DisableDividers)
                contract({
                    return@contract CherrygramAppearanceConfig.disableDividers
                }) {
                    CherrygramAppearanceConfig.disableDividers = it
                    Theme.applyCommonTheme()
                    getListView()?.post {
                        getListView()!!.adapter?.notifyDataSetChanged()
                    }
                }
            }
        }

        category(getString(R.string.CP_Snowflakes_AH)) {
            switch {
                title = getString(R.string.AP_CenterTitle)

                contract({
                    return@contract CherrygramAppearanceConfig.centerTitle
                }) {
                    CherrygramAppearanceConfig.centerTitle = it
                    bf.parentLayout.rebuildAllFragmentViews(true, true)
                }
            }
            switch {
                title = getString(R.string.AP_ToolBarShadow)

                contract({
                    return@contract CherrygramAppearanceConfig.disableToolBarShadow
                }) {
                    CherrygramAppearanceConfig.disableToolBarShadow = it
                    bf.parentLayout.setHeaderShadow(
                        if (CherrygramAppearanceConfig.disableToolBarShadow) null else ContextCompat.getDrawable(bf.context, R.drawable.header_shadow)?.mutate()
                    )
                    bf.parentLayout.rebuildAllFragmentViews(false, false)
                }
            }
        }

        category(getString(R.string.AP_Header_Appearance)) {
            textIcon {
                title = getString(R.string.CP_Filters_Header)
                icon = R.drawable.msg_folders
                divider = true

                listener = object : TGKitTextIconRow.TGTIListener {
                    override fun onClick(bf: BaseFragment) {
                        CherrygramPreferencesNavigator.createFoldersPrefs(bf)
                    }

                    override fun onLongClick(bf: BaseFragment) {
                        AndroidUtilities.addToClipboard("tg://" + DeeplinkHelper.DeepLinksRepo.CG_Folders)
                    }
                }
            }

            textIcon {
                title = getString(R.string.CP_MainTabs_Header)
                icon = R.drawable.tabs_reorder
                divider = true

                listener = object : TGKitTextIconRow.TGTIListener {
                    override fun onClick(bf: BaseFragment) {
                        bf.presentFragment(MainTabsPreferencesEntry())
                    }

                    override fun onLongClick(bf: BaseFragment) {
                        AndroidUtilities.addToClipboard("tg://" + DeeplinkHelper.DeepLinksRepo.CG_Tabs)
                    }
                }
            }

            textIcon {
                title = getString(R.string.CP_ProfileReplyBackground)
                icon = R.drawable.msg_customize
                divider = true

                listener = object : TGKitTextIconRow.TGTIListener {
                    override fun onClick(bf: BaseFragment) {
                        CherrygramPreferencesNavigator.createMessagesAndProfiles(bf)
                    }

                    override fun onLongClick(bf: BaseFragment) {
                        AndroidUtilities.addToClipboard("tg://" + DeeplinkHelper.DeepLinksRepo.CG_Messages_And_Profiles)
                    }
                }
            }
        }

        category(getString(R.string.CP_Snowflakes_Header)) {
            switch {
                title = getString(R.string.CP_Snowflakes_AH)
                contract({
                    return@contract CherrygramAppearanceConfig.drawSnowInActionBar
                }) {
                    CherrygramAppearanceConfig.drawSnowInActionBar = it
                    CGBulletinCreator.createRestartBulletin(bf)
                }
            }
            switch {
                title = getString(R.string.CP_Header_Chats)
                contract({
                    return@contract CherrygramAppearanceConfig.drawSnowInChat
                }) {
                    CherrygramAppearanceConfig.drawSnowInChat = it
                    bf.parentLayout.rebuildAllFragmentViews(false, false)
                }
            }
        }

        FirebaseAnalyticsHelper.trackEventWithEmptyBundle("appearance_preferences_screen")
    }
}
