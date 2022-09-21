package uz.unnarsx.cherrygram.preferences

import android.graphics.Color
import android.os.Build
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.messenger.SharedConfig
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.LaunchActivity
import uz.unnarsx.cherrygram.CherrygramConfig
import uz.unnarsx.cherrygram.preferences.ktx.*
import uz.unnarsx.extras.CherrygramExtras
import uz.unnarsx.tgkit.preference.types.TGKitTextIconRow

class AppearancePreferencesEntry : BasePreferencesEntry {
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(LocaleController.getString("AP_Header_Appearance", R.string.AP_Header_Appearance)) {
        category(LocaleController.getString("AP_RedesignCategory", R.string.AP_RedesignCategory)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                list {
                    title = LocaleController.getString("AP_IconReplacements", R.string.AP_IconReplacements)

                    contractIcons({
                        return@contractIcons listOf(
                            Triple(0, LocaleController.getString("AP_IconReplacement_VKUI", R.string.AP_IconReplacement_VKUI), R.drawable.settings_outline_28),
                            Triple(1, LocaleController.getString("AP_IconReplacement_Default", R.string.AP_IconReplacement_Default), R.drawable.msg_settings)
                        )
                    }, {
                        return@contractIcons when (CherrygramConfig.iconReplacement) {
                            1 -> LocaleController.getString("AP_IconReplacement_Default", R.string.AP_IconReplacement_Default)
                            else -> LocaleController.getString("AP_IconReplacement_VKUI", R.string.AP_IconReplacement_VKUI)
                        }
                    }) {
                        CherrygramConfig.iconReplacement = it
                        (bf.parentActivity as? LaunchActivity)?.reloadResources()
                    }
                }
            }
            switch {
                title = LocaleController.getString("AP_ToolBarShadow", R.string.AP_ToolBarShadow)

                contract({
                    return@contract CherrygramConfig.flatActionbar
                }) {
                    CherrygramConfig.flatActionbar = it
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
                }
            }
            switch {
                title = LocaleController.getString("AP_SystemFonts", R.string.AP_SystemFonts)
                summary = LocaleController.getString("AP_SystemFonts_Desc", R.string.AP_SystemFonts_Desc)

                contract({
                    return@contract CherrygramConfig.systemFonts
                }) {
                    CherrygramConfig.systemFonts = it
                }
            }
        }

        category(LocaleController.getString("CP_PremAndAnim_Header", R.string.CP_PremAndAnim_Header)) {
            switch {
                title = LocaleController.getString("CP_DisableAnimAvatars", R.string.CP_DisableAnimAvatars)

                contract({
                    return@contract CherrygramConfig.disableAnimatedAvatars
                }) {
                    CherrygramConfig.disableAnimatedAvatars = it
                }
            }
            switch {
                title = LocaleController.getString("CP_DisableReactionAnim", R.string.CP_DisableReactionAnim)
                summary = LocaleController.getString("CP_DisableReactionAnim_Desc", R.string.CP_DisableReactionAnim_Desc)

                contract({
                    return@contract CherrygramConfig.disableReactionAnim
                }) {
                    CherrygramConfig.disableReactionAnim = it
                }
            }
            switch {
                title = LocaleController.getString("CP_DisablePremiumStatuses", R.string.CP_DisablePremiumStatuses)
                summary = LocaleController.getString("CP_DisablePremiumStatuses_Desc", R.string.CP_DisablePremiumStatuses_Desc)

                contract({
                    return@contract CherrygramConfig.disablePremiumStatuses
                }) {
                    CherrygramConfig.disablePremiumStatuses = it
                }
            }
            switch {
                title = LocaleController.getString("CP_DisablePremStickAnim", R.string.CP_DisablePremStickAnim)
                summary = LocaleController.getString("CP_DisablePremStickAnim_Desc", R.string.CP_DisablePremStickAnim_Desc)

                contract({
                    return@contract CherrygramConfig.disablePremStickAnim
                }) {
                    CherrygramConfig.disablePremStickAnim = it
                }
            }
            switch {
                title = LocaleController.getString("CP_DisablePremStickAutoPlay", R.string.CP_DisablePremStickAutoPlay)
                summary = LocaleController.getString("CP_DisablePremStickAutoPlay_Desc", R.string.CP_DisablePremStickAutoPlay_Desc)

                contract({
                    return@contract CherrygramConfig.disablePremStickAutoPlay
                }) {
                    CherrygramConfig.disablePremStickAutoPlay = it
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
                }
            }
            switch {
                title = LocaleController.getString("CP_NewTabs_RemoveAllChats", R.string.CP_NewTabs_RemoveAllChats)
                /*summary = LocaleController.getString("CP_NewTabs_RemoveAllChats_Desc", R.string.CP_NewTabs_RemoveAllChats_Desc)*/

                contract({
                    return@contract CherrygramConfig.newTabs_hideAllChats
                }) {
                    CherrygramConfig.newTabs_hideAllChats = it
                }
            }
            switch {
                title = LocaleController.getString("CP_ShowTabsOnForward", R.string.CP_ShowTabsOnForward)

                contract({
                    return@contract CherrygramConfig.showTabsOnForward
                }) {
                    CherrygramConfig.showTabsOnForward = it
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

        category(LocaleController.getString("AP_ProfileCategory", R.string.AP_ProfileCategory)) {
            switch {
                title = LocaleController.getString("AP_HideUserPhone", R.string.AP_HideUserPhone)
                summary = LocaleController.getString("AP_HideUserPhoneSummary", R.string.AP_HideUserPhoneSummary)

                contract({
                    return@contract CherrygramConfig.hidePhoneNumber
                }) {
                    CherrygramConfig.hidePhoneNumber = it
                }
            }
            switch {
                title = LocaleController.getString("AP_ShowID", R.string.AP_ShowID)
                contract({
                    return@contract CherrygramConfig.showId
                }) {
                    CherrygramConfig.showId = it
                }
            }
            switch {
                title = LocaleController.getString("AP_ShowDC", R.string.AP_ShowDC)
                contract({
                    return@contract CherrygramConfig.showDc
                }) {
                    CherrygramConfig.showDc = it
                }
            }
        }
    }
}
