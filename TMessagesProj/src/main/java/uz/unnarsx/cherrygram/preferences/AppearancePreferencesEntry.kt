package uz.unnarsx.cherrygram.preferences

import android.app.Activity
import android.content.SharedPreferences
import androidx.core.util.Pair
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.LocaleController
import org.telegram.messenger.NotificationCenter
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.LaunchActivity
import uz.unnarsx.cherrygram.CherrygramConfig
import uz.unnarsx.cherrygram.helpers.AppRestartHelper
import uz.unnarsx.cherrygram.ui.drawer.DrawerPreferencesEntry
import uz.unnarsx.cherrygram.ui.tgkit.preference.category
import uz.unnarsx.cherrygram.ui.tgkit.preference.contract
import uz.unnarsx.cherrygram.ui.tgkit.preference.contractIcons
import uz.unnarsx.cherrygram.ui.tgkit.preference.list
import uz.unnarsx.cherrygram.ui.tgkit.preference.switch
import uz.unnarsx.cherrygram.ui.tgkit.preference.textIcon
import uz.unnarsx.cherrygram.ui.tgkit.preference.tgKitScreen
import uz.unnarsx.cherrygram.ui.tgkit.preference.types.TGKitTextIconRow

class AppearancePreferencesEntry : BasePreferencesEntry {
    val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(LocaleController.getString("AP_Header_Appearance", R.string.AP_Header_Appearance)) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(CherrygramConfig.listener)
        category(LocaleController.getString("AP_RedesignCategory", R.string.AP_RedesignCategory)) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                list {
                    title = LocaleController.getString("AP_IconReplacements", R.string.AP_IconReplacements)

                    contractIcons({
                        return@contractIcons listOf(
                            Triple(0, LocaleController.getString("AP_IconReplacement_VKUI", R.string.AP_IconReplacement_VKUI), R.drawable.settings_outline_28),
                            Triple(1, LocaleController.getString("AP_IconReplacement_Solar", R.string.AP_IconReplacement_Solar), R.drawable.msg_settings_solar),
                            Triple(2, LocaleController.getString("AP_IconReplacement_Default", R.string.AP_IconReplacement_Default), R.drawable.msg_settings)
                        )
                    }, {
                        return@contractIcons when (CherrygramConfig.iconReplacement) {
                            0 -> LocaleController.getString("AP_IconReplacement_VKUI", R.string.AP_IconReplacement_VKUI)
                            1 -> LocaleController.getString("AP_IconReplacement_Solar", R.string.AP_IconReplacement_Solar)
                            else -> LocaleController.getString("AP_IconReplacement_Default", R.string.AP_IconReplacement_Default)
                        }
                    }) {
                        CherrygramConfig.iconReplacement = it
                        bf.parentActivity.recreate()
                        (bf.parentActivity as? LaunchActivity)?.reloadResources()
                    }
                }
//            }
            switch {
                title = LocaleController.getString("AP_OneUI_Switch_Style", R.string.AP_OneUI_Switch_Style)

                contract({
                    return@contract CherrygramConfig.oneUI_SwitchStyle
                }) {
                    CherrygramConfig.oneUI_SwitchStyle = it
                }
            }
            switch {
                title = LocaleController.getString("AP_CenterTitle", R.string.AP_CenterTitle)
                contract({
                    return@contract CherrygramConfig.centerTitle
                }) {
                    CherrygramConfig.centerTitle = it
                    bf.parentActivity.recreate()
                }
            }
            switch {
                title = LocaleController.getString("AP_ToolBarShadow", R.string.AP_ToolBarShadow)

                contract({
                    return@contract CherrygramConfig.disableToolBarShadow
                }) {
                    CherrygramConfig.disableToolBarShadow = it
                    bf.parentActivity.recreate()
                }
            }
            switch {
                title = LocaleController.getString("AP_DisableDividers", R.string.AP_DisableDividers)
                contract({
                    return@contract CherrygramConfig.disableDividers
                }) {
                    CherrygramConfig.disableDividers = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
            switch {
                title = LocaleController.getString("AP_OverrideHeader", R.string.AP_OverrideHeader)
                description = LocaleController.getString("AP_OverrideHeader_Desc", R.string.AP_OverrideHeader_Desc)

                contract({
                    return@contract CherrygramConfig.overrideHeaderColor
                }) {
                    CherrygramConfig.overrideHeaderColor = it
                    bf.parentActivity.recreate()
                }
            }
        }

        category(LocaleController.getString("AP_DrawerCategory", R.string.AP_DrawerCategory)) {
            textIcon {
                title = LocaleController.getString("AP_DrawerPreferences", R.string.AP_DrawerPreferences)
                icon = R.drawable.msg_list
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(DrawerPreferencesEntry())
                }
            }
        }

        category(LocaleController.getString("AS_Filters_Header", R.string.CP_Filters_Header)) {
            switch {
                title = LocaleController.getString("AP_FolderNameInHeader", R.string.AP_FolderNameInHeader)
                description = LocaleController.getString("AP_FolderNameInHeader_Desc", R.string.AP_FolderNameInHeader_Desc)

                contract({
                    return@contract CherrygramConfig.folderNameInHeader
                }) {
                    CherrygramConfig.folderNameInHeader = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
            switch {
                title = LocaleController.getString("CP_NewTabs_RemoveAllChats", R.string.CP_NewTabs_RemoveAllChats)

                contract({
                    return@contract CherrygramConfig.newTabs_hideAllChats
                }) {
                    CherrygramConfig.newTabs_hideAllChats = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }

            switch {
                title = LocaleController.getString("CP_NewTabs_NoCounter", R.string.CP_NewTabs_NoCounter)

                contract({
                    return@contract CherrygramConfig.newTabs_noUnread
                }) {
                    CherrygramConfig.newTabs_noUnread = it
                    bf.notificationCenter.postNotificationName(NotificationCenter.dialogFiltersUpdated);
                }
            }

            list {
                title = LocaleController.getString("CG_FoldersType_Header", R.string.CG_FoldersType_Header)

                contract({
                    return@contract listOf(
                        Pair(CherrygramConfig.TAB_TYPE_MIX, LocaleController.getString("CG_FoldersTypeIconsTitles", R.string.CG_FoldersTypeIconsTitles)),
                        Pair(CherrygramConfig.TAB_TYPE_TEXT, LocaleController.getString("CG_FoldersTypeTitles", R.string.CG_FoldersTypeTitles)),
                        Pair(CherrygramConfig.TAB_TYPE_ICON, LocaleController.getString("CG_FoldersTypeIcons", R.string.CG_FoldersTypeIcons))
                    )
                }, {
                    return@contract when (CherrygramConfig.tabMode) {
                        CherrygramConfig.TAB_TYPE_MIX -> LocaleController.getString("CG_FoldersTypeIconsTitles", R.string.CG_FoldersTypeIconsTitles)
                        CherrygramConfig.TAB_TYPE_TEXT -> LocaleController.getString("CG_FoldersTypeTitles", R.string.CG_FoldersTypeTitles)
                        else -> LocaleController.getString("CG_FoldersTypeIcons", R.string.CG_FoldersTypeIcons)
                    }
                }) {
                    CherrygramConfig.tabMode = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }

            list {
                title = LocaleController.getString("AP_Tab_Style", R.string.AP_Tab_Style)

                contract({
                    return@contract listOf(
                        Pair(CherrygramConfig.TAB_STYLE_DEFAULT, LocaleController.getString("AP_Tab_Style_Default", R.string.AP_Tab_Style_Default)),
                        Pair(CherrygramConfig.TAB_STYLE_ROUNDED, LocaleController.getString("AP_Tab_Style_Rounded", R.string.AP_Tab_Style_Rounded)),
                        Pair(CherrygramConfig.TAB_STYLE_TEXT, LocaleController.getString("AP_Tab_Style_Text", R.string.AP_Tab_Style_Text)),
                        Pair(CherrygramConfig.TAB_STYLE_VKUI, "VKUI"),
                        Pair(CherrygramConfig.TAB_STYLE_PILLS, LocaleController.getString("AP_Tab_Style_Pills", R.string.AP_Tab_Style_Pills))
                    )
                }, {
                    return@contract when (CherrygramConfig.tabStyle) {
                        CherrygramConfig.TAB_STYLE_ROUNDED -> LocaleController.getString("AP_Tab_Style_Rounded", R.string.AP_Tab_Style_Rounded)
                        CherrygramConfig.TAB_STYLE_TEXT -> LocaleController.getString("AP_Tab_Style_Text", R.string.AP_Tab_Style_Text)
                        CherrygramConfig.TAB_STYLE_VKUI -> "VKUI"
                        CherrygramConfig.TAB_STYLE_PILLS -> LocaleController.getString("AP_Tab_Style_Pills", R.string.AP_Tab_Style_Pills)
                        else -> LocaleController.getString("AP_Tab_Style_Default", R.string.AP_Tab_Style_Default)
                    }
                }) {
                    CherrygramConfig.tabStyle = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }

            switch {
                title = LocaleController.getString("AP_Tab_Style_Stroke", R.string.AP_Tab_Style_Stroke)
                contract({
                    return@contract CherrygramConfig.tabStyleStroke
                }) {
                    CherrygramConfig.tabStyleStroke = it
                }
                AppRestartHelper.createRestartBulletin(bf)
            }
        }

        category(LocaleController.getString("CP_Snowflakes_Header", R.string.CP_Snowflakes_Header)) {
            switch {
                title = LocaleController.getString("AP_DrawerCategory", R.string.AP_DrawerCategory)
                contract({
                    return@contract CherrygramConfig.drawSnowInDrawer
                }) {
                    CherrygramConfig.drawSnowInDrawer = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
            switch {
                title = LocaleController.getString("CP_Header_Chats", R.string.CP_Header_Chats)
                contract({
                    return@contract CherrygramConfig.drawSnowInChat
                }) {
                    CherrygramConfig.drawSnowInChat = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
        }
    }
}
