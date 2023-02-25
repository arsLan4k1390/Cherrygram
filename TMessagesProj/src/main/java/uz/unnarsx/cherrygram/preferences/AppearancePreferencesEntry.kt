package uz.unnarsx.cherrygram.preferences

import android.app.Activity
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import androidx.core.util.Pair
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.messenger.SharedConfig
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Components.BulletinFactory
import org.telegram.ui.LaunchActivity
import uz.unnarsx.cherrygram.CherrygramConfig
import uz.unnarsx.cherrygram.extras.CherrygramExtras
import uz.unnarsx.cherrygram.tgkit.preference.*

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
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                switch {
                    title = LocaleController.getString("AP_TransparentSB", R.string.AP_TransparentSB)

                    contract({
                        return@contract SharedConfig.noStatusBar
                    }) {
                        SharedConfig.toggleNoStatusBar()
                        bf.parentActivity.window.statusBarColor = if (Theme.getColor(Theme.key_actionBarDefault, null, true) == Color.WHITE) CherrygramExtras.lightStatusbarColor else CherrygramExtras.darkStatusbarColor
                    }
                }
            }
            switch {
                title = LocaleController.getString("AP_FlatNB", R.string.AP_FlatNB)
                summary = LocaleController.getString("AP_FlatNB_Desc", R.string.AP_FlatNB_Desc)

                contract({
                    return@contract CherrygramConfig.flatNavbar
                }) {
                    CherrygramConfig.flatNavbar = it
                    bf.parentActivity.recreate()
                }
            }
            switch {
                title = LocaleController.getString("AP_SystemEmoji", R.string.AP_SystemEmoji)
                contract({
                    return@contract CherrygramConfig.systemEmoji
                }) {
                    CherrygramConfig.systemEmoji = it
                }
            }
            switch {
                title = LocaleController.getString("AP_SystemFonts", R.string.AP_SystemFonts)
                summary = LocaleController.getString("AP_SystemFonts_Desc", R.string.AP_SystemFonts_Desc)

                contract({
                    return@contract CherrygramConfig.systemFonts
                }) {
                    CherrygramConfig.systemFonts = it
                    BulletinFactory.of(bf).createRestartBulletin(
                        R.raw.chats_infotip,
                        LocaleController.getString("CG_RestartToApply", R.string.CG_RestartToApply),
                        LocaleController.getString("BotUnblock", R.string.BotUnblock)
                    ) {
                    }.show()
                }
            }
        }

        category(LocaleController.getString("AS_Filters_Header", R.string.CP_Filters_Header)) {
            switch {
                title = LocaleController.getString("AP_FolderNameInHeader", R.string.AP_FolderNameInHeader)
                summary = LocaleController.getString("AP_FolderNameInHeader_Desc", R.string.AP_FolderNameInHeader_Desc)

                contract({
                    return@contract CherrygramConfig.folderNameInHeader
                }) {
                    CherrygramConfig.folderNameInHeader = it
                    BulletinFactory.of(bf).createRestartBulletin(
                        R.raw.chats_infotip,
                        LocaleController.getString("CG_RestartToApply", R.string.CG_RestartToApply),
                        LocaleController.getString("BotUnblock", R.string.BotUnblock)
                    ) {
                    }.show()
                }
            }
            switch {
                title = LocaleController.getString("CP_NewTabs_RemoveAllChats", R.string.CP_NewTabs_RemoveAllChats)

                contract({
                    return@contract CherrygramConfig.newTabs_hideAllChats
                }) {
                    CherrygramConfig.newTabs_hideAllChats = it
                    BulletinFactory.of(bf).createRestartBulletin(
                        R.raw.chats_infotip,
                        LocaleController.getString("CG_RestartToApply", R.string.CG_RestartToApply),
                        LocaleController.getString("BotUnblock", R.string.BotUnblock)
                    ) {
                    }.show()
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
                    return@contract when (CherrygramConfig.tab_style) {
                        CherrygramConfig.TAB_STYLE_ROUNDED -> LocaleController.getString("AP_Tab_Style_Rounded", R.string.AP_Tab_Style_Rounded)
                        CherrygramConfig.TAB_STYLE_TEXT -> LocaleController.getString("AP_Tab_Style_Text", R.string.AP_Tab_Style_Text)
                        CherrygramConfig.TAB_STYLE_VKUI -> "VKUI"
                        CherrygramConfig.TAB_STYLE_PILLS -> LocaleController.getString("AP_Tab_Style_Pills", R.string.AP_Tab_Style_Pills)
                        else -> LocaleController.getString("AP_Tab_Style_Default", R.string.AP_Tab_Style_Default)
                    }
                }) {
                    CherrygramConfig.tab_style = it
                    BulletinFactory.of(bf).createRestartBulletin(
                        R.raw.chats_infotip,
                        LocaleController.getString("CG_RestartToApply", R.string.CG_RestartToApply),
                        LocaleController.getString("BotUnblock", R.string.BotUnblock)
                    ) {
                    }.show()
                }
            }
        }

        category(LocaleController.getString("CP_Snowflakes_Header", R.string.CP_Snowflakes_Header)) {
            switch {
                title = LocaleController.getString("AP_DrawerCategory", R.string.AP_DrawerCategory)
                contract({
                    return@contract CherrygramConfig.drawSnowInDrawer
                }) {
                    CherrygramConfig.drawSnowInDrawer = it
                    BulletinFactory.of(bf).createRestartBulletin(
                        R.raw.chats_infotip,
                        LocaleController.getString("CG_RestartToApply", R.string.CG_RestartToApply),
                        LocaleController.getString("BotUnblock", R.string.BotUnblock)
                    ) {
                    }.show()
                }
            }
            switch {
                title = LocaleController.getString("CP_Snowflakes_AH", R.string.CP_Snowflakes_AH)
                contract({
                    return@contract CherrygramConfig.drawSnowInActionBar
                }) {
                    CherrygramConfig.drawSnowInActionBar = it
                    BulletinFactory.of(bf).createRestartBulletin(
                        R.raw.chats_infotip,
                        LocaleController.getString("CG_RestartToApply", R.string.CG_RestartToApply),
                        LocaleController.getString("BotUnblock", R.string.BotUnblock)
                    ) {
                    }.show()
                }
            }
            switch {
                title = LocaleController.getString("CP_Header_Chats", R.string.CP_Header_Chats)
                contract({
                    return@contract CherrygramConfig.drawSnowInChat
                }) {
                    CherrygramConfig.drawSnowInChat = it
                    BulletinFactory.of(bf).createRestartBulletin(
                        R.raw.chats_infotip,
                        LocaleController.getString("CG_RestartToApply", R.string.CG_RestartToApply),
                        LocaleController.getString("BotUnblock", R.string.BotUnblock)
                    ) {
                    }.show()
                }
            }
        }
    }
}
