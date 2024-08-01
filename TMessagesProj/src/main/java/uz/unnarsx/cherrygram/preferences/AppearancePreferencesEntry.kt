package uz.unnarsx.cherrygram.preferences

import androidx.core.util.Pair
import org.telegram.messenger.LocaleController
import org.telegram.messenger.MessagesController
import org.telegram.messenger.R
import org.telegram.messenger.UserConfig
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.DialogsActivity
import org.telegram.ui.LaunchActivity
import uz.unnarsx.cherrygram.CherrygramConfig
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
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(LocaleController.getString("AP_Header_Appearance", R.string.AP_Header_Appearance)) {
        category(LocaleController.getString("AP_RedesignCategory", R.string.AP_RedesignCategory)) {
            list {
                title = LocaleController.getString("AP_IconReplacements", R.string.AP_IconReplacements)

                contract({
                    return@contract listOf(
                        Pair(CherrygramConfig.ICON_REPLACE_NONE, LocaleController.getString("AP_IconReplacement_Default", R.string.AP_IconReplacement_Default)),
                        Pair(CherrygramConfig.ICON_REPLACE_VKUI, LocaleController.getString("AP_IconReplacement_VKUI", R.string.AP_IconReplacement_VKUI)),
                        Pair(CherrygramConfig.ICON_REPLACE_SOLAR, LocaleController.getString("AP_IconReplacement_Solar", R.string.AP_IconReplacement_Solar))
                    )
                }, {
                    return@contract when (CherrygramConfig.iconReplacement) {
                        CherrygramConfig.ICON_REPLACE_VKUI -> LocaleController.getString("AP_IconReplacement_VKUI", R.string.AP_IconReplacement_VKUI)
                        CherrygramConfig.ICON_REPLACE_SOLAR -> LocaleController.getString("AP_IconReplacement_Solar", R.string.AP_IconReplacement_Solar)
                        else -> LocaleController.getString("AP_IconReplacement_Default", R.string.AP_IconReplacement_Default)
                    }
                }) {
                    CherrygramConfig.iconReplacement = it
                    (bf.parentActivity as? LaunchActivity)?.reloadResources()
                    bf.parentLayout.rebuildAllFragmentViews(true, true)
                }
            }
            switch {
                title = LocaleController.getString("AP_OneUI_Switch_Style", R.string.AP_OneUI_Switch_Style)

                contract({
                    return@contract CherrygramConfig.oneUI_SwitchStyle
                }) {
                    CherrygramConfig.oneUI_SwitchStyle = it
                    bf.parentLayout.rebuildAllFragmentViews(true, true)
                }
            }
            switch {
                title = LocaleController.getString("AP_DisableDividers", R.string.AP_DisableDividers)
                contract({
                    return@contract CherrygramConfig.disableDividers
                }) {
                    CherrygramConfig.disableDividers = it
                    bf.parentLayout.rebuildAllFragmentViews(true, true)
                }
            }
        }

        category(LocaleController.getString("CP_Snowflakes_AH", R.string.CP_Snowflakes_AH)) {
            textIcon {
                title = LocaleController.getString("EP_CustomAppTitle", R.string.EP_CustomAppTitle)
                value = MessagesController.getMainSettings(UserConfig.selectedAccount).getString("CG_AppName", LocaleController.getString("CG_AppName", R.string.CG_AppName))

                listener = TGKitTextIconRow.TGTIListener {
                    val defaultValue = LocaleController.getString("CG_AppName", R.string.CG_AppName)
                    TextFieldAlert.createFieldAlertForAppName(
                        bf.context,
                        LocaleController.getString("EP_CustomAppTitle", R.string.EP_CustomAppTitle),
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
                title = LocaleController.getString("AP_CenterTitle", R.string.AP_CenterTitle)
                contract({
                    return@contract CherrygramConfig.centerTitle
                }) {
                    CherrygramConfig.centerTitle = it
                    bf.parentLayout.rebuildAllFragmentViews(true, true)
                }
            }
            switch {
                title = LocaleController.getString("AP_ToolBarShadow", R.string.AP_ToolBarShadow)

                contract({
                    return@contract CherrygramConfig.disableToolBarShadow
                }) {
                    CherrygramConfig.disableToolBarShadow = it
                    bf.parentLayout.setHeaderShadow(
                        if (CherrygramConfig.disableToolBarShadow) null else bf.parentLayout.parentActivity.getDrawable(R.drawable.header_shadow)?.mutate()
                    )
                    bf.parentLayout.rebuildAllFragmentViews(false, false)
                }
            }
            switch {
                title = LocaleController.getString("AP_OverrideHeader", R.string.AP_OverrideHeader)
                description = LocaleController.getString("AP_OverrideHeader_Desc", R.string.AP_OverrideHeader_Desc)

                contract({
                    return@contract CherrygramConfig.overrideHeaderColor
                }) {
                    CherrygramConfig.overrideHeaderColor = it
                    bf.parentLayout.rebuildAllFragmentViews(false, false)
                }
            }
        }

        category(LocaleController.getString("AP_Header_Appearance", R.string.AP_Header_Appearance)) {
            textIcon {
                title = LocaleController.getString("CP_ProfileReplyBackground", R.string.CP_ProfileReplyBackground)
                icon = R.drawable.msg_customize
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(MessagesAndProfilesPreferencesEntry())
                }
            }

            textIcon {
                title = LocaleController.getString("CP_Filters_Header", R.string.CP_Filters_Header)
                icon = R.drawable.msg_folders
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(FoldersPreferencesEntry())
                }
            }

            textIcon {
                title = LocaleController.getString("AP_DrawerCategory", R.string.AP_DrawerCategory)
                icon = R.drawable.msg_list
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(DrawerPreferencesEntry())
                }
            }
        }

        category(LocaleController.getString("CP_Snowflakes_Header", R.string.CP_Snowflakes_Header)) {
            switch {
                title = LocaleController.getString("CP_Snowflakes_AH", R.string.CP_Snowflakes_AH)
                contract({
                    return@contract CherrygramConfig.drawSnowInActionBar
                }) {
                    CherrygramConfig.drawSnowInActionBar = it
                    bf.parentLayout.rebuildAllFragmentViews(false, false)
                }
            }
            switch {
                title = LocaleController.getString("CP_Header_Chats", R.string.CP_Header_Chats)
                contract({
                    return@contract CherrygramConfig.drawSnowInChat
                }) {
                    CherrygramConfig.drawSnowInChat = it
                    bf.parentLayout.rebuildAllFragmentViews(false, false)
                }
            }
        }
    }
}
