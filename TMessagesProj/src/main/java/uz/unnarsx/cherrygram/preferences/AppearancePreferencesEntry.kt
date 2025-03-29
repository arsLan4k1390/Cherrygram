/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.preferences

import androidx.core.util.Pair
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.MessagesController
import org.telegram.messenger.R
import org.telegram.messenger.UserConfig
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.DialogsActivity
import org.telegram.ui.LaunchActivity
import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper
import uz.unnarsx.cherrygram.core.helpers.FirebaseAnalyticsHelper
import uz.unnarsx.cherrygram.preferences.helpers.TextFieldAlert
import uz.unnarsx.cherrygram.preferences.drawer.DrawerPreferencesEntry
import uz.unnarsx.cherrygram.preferences.folders.FoldersPreferencesEntry
import uz.unnarsx.cherrygram.preferences.tgkit.preference.category
import uz.unnarsx.cherrygram.preferences.tgkit.preference.contract
import uz.unnarsx.cherrygram.preferences.tgkit.preference.list
import uz.unnarsx.cherrygram.preferences.tgkit.preference.switch
import uz.unnarsx.cherrygram.preferences.tgkit.preference.textIcon
import uz.unnarsx.cherrygram.preferences.tgkit.preference.tgKitScreen
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitTextIconRow

class AppearancePreferencesEntry : BasePreferencesEntry {
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(getString(R.string.AP_Header_Appearance)) {
        category(getString(R.string.AP_RedesignCategory)) {
            list {
                title = getString(R.string.AP_IconReplacements)

                contract({
                    return@contract listOf(
                        Pair(CherrygramAppearanceConfig.ICON_REPLACE_NONE, getString(R.string.AP_IconReplacement_Default)),
                        Pair(CherrygramAppearanceConfig.ICON_REPLACE_VKUI, getString(R.string.AP_IconReplacement_VKUI)),
                        Pair(CherrygramAppearanceConfig.ICON_REPLACE_SOLAR, getString(R.string.AP_IconReplacement_Solar))
                    )
                }, {
                    return@contract when (CherrygramAppearanceConfig.iconReplacement) {
                        CherrygramAppearanceConfig.ICON_REPLACE_VKUI -> getString(R.string.AP_IconReplacement_VKUI)
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
                }
            }
            switch {
                title = getString(R.string.AP_DisableDividers)
                contract({
                    return@contract CherrygramAppearanceConfig.disableDividers
                }) {
                    CherrygramAppearanceConfig.disableDividers = it
                    Theme.applyCommonTheme();
                    bf.parentLayout.rebuildAllFragmentViews(true, true)
                }
            }
        }

        category(getString(R.string.CP_Snowflakes_AH)) {
            textIcon {
                title = getString(R.string.EP_CustomAppTitle)
                value = MessagesController.getMainSettings(UserConfig.selectedAccount).getString("CG_AppName", getString(R.string.CG_AppName))
                divider = true

                listener = TGKitTextIconRow.TGTIListener {
                    val defaultValue = getString(R.string.CG_AppName)
                    TextFieldAlert.createFieldAlertForAppName(
                        bf.context,
                        getString(R.string.EP_CustomAppTitle),
                        MessagesController.getMainSettings(UserConfig.selectedAccount).getString("CG_AppName", defaultValue)!!
                    ) { result: String ->
                        var result = result
                        if (result.isEmpty()) {
                            result = defaultValue
                        }
                        val editor =
                            MessagesController.getMainSettings(UserConfig.selectedAccount).edit()
                        editor.putString("CG_AppName", result)
                        editor.apply()
                        bf.parentLayout.rebuildAllFragmentViews(true, true)
                        val previousFragment: BaseFragment? =
                            if (bf.parentLayout.fragmentStack.size > 2) bf.parentLayout.fragmentStack[bf.parentLayout.fragmentStack.size - 3] else null
                        (previousFragment as? DialogsActivity)?.actionBar?.setTitle(result)

                        value = result
                    }
                }
            }

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
                        if (CherrygramAppearanceConfig.disableToolBarShadow) null else bf.parentLayout.parentActivity.getDrawable(R.drawable.header_shadow)?.mutate()
                    )
                    bf.parentLayout.rebuildAllFragmentViews(false, false)
                }
            }
            switch {
                title = getString(R.string.AP_OverrideHeader)
                description = getString(R.string.AP_OverrideHeader_Desc)

                contract({
                    return@contract CherrygramAppearanceConfig.overrideHeaderColor
                }) {
                    CherrygramAppearanceConfig.overrideHeaderColor = it
                    bf.parentLayout.rebuildAllFragmentViews(false, false)
                }
            }
        }

        category(getString(R.string.AP_Header_Appearance)) {
            textIcon {
                title = getString(R.string.CP_ProfileReplyBackground)
                icon = R.drawable.msg_customize
                divider = true

                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(MessagesAndProfilesPreferencesEntry())
                }
            }

            textIcon {
                title = getString(R.string.CP_Filters_Header)
                icon = R.drawable.msg_folders
                divider = true

                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(FoldersPreferencesEntry())
                }
            }

            textIcon {
                title = getString(R.string.AP_DrawerCategory)
                icon = R.drawable.msg_list
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(DrawerPreferencesEntry())
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
                    AppRestartHelper.createRestartBulletin(bf)
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
