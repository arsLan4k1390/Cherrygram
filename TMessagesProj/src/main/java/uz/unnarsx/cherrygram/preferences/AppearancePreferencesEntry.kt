package uz.unnarsx.cherrygram.preferences

import android.graphics.Color
import android.os.Build
import android.os.Build.VERSION_CODES.S
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.messenger.SharedConfig
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.LaunchActivity
import uz.unnarsx.cherrygram.CherrygramConfig
import uz.unnarsx.cherrygram.preferences.ktx.*
import uz.unnarsx.extras.CherrygramExtras
import uz.unnarsx.extras.IconExtras

class AppearancePreferencesEntry : BasePreferencesEntry {
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(LocaleController.getString("AP_Header_Appearance", R.string.AP_Header_Appearance)) {
        category(LocaleController.getString("AP_RedesignCategory", R.string.AP_RedesignCategory)) {
            if (Build.VERSION.SDK_INT == 31 || Build.VERSION.SDK_INT== 32) {
                list {
                    title = LocaleController.getString("AP_ChangeIcon", R.string.AP_ChangeIcon)

                    contractIcons({
                        return@contractIcons listOf(
                            Triple(0, LocaleController.getString("AP_ChangeIcon_Default", R.string.AP_ChangeIcon_Default), R.mipmap.cg_launcher_default),
                            Triple(1, LocaleController.getString("AP_ChangeIcon_White", R.string.AP_ChangeIcon_White), R.mipmap.cg_launcher_white),
                            Triple(2, LocaleController.getString("AP_ChangeIcon_Monet_Samsung", R.string.AP_ChangeIcon_Monet_Samsung), R.mipmap.cg_launcher_monet_samsung),
                            Triple(3, LocaleController.getString("AP_ChangeIcon_Monet_Pixel", R.string.AP_ChangeIcon_Monet_Pixel), R.mipmap.cg_launcher_monet_samsung),
                        )
                    }, {
                        return@contractIcons when (CherrygramConfig.change_Icon) {
                            1 -> LocaleController.getString("AP_ChangeIcon_White", R.string.AP_ChangeIcon_White)
                            2 -> LocaleController.getString("AP_ChangeIcon_Monet_Samsung", R.string.AP_ChangeIcon_Monet_Samsung)
                            3 -> LocaleController.getString("AP_ChangeIcon_Monet_Pixel", R.string.AP_ChangeIcon_Monet_Pixel)
                            else -> LocaleController.getString("AP_ChangeIcon_Default", R.string.AP_ChangeIcon_Default)
                        }
                    }) {
                        CherrygramConfig.change_Icon = it
                        IconExtras.setIcon(it)
                    }
                }
            }
            if (Build.VERSION.SDK_INT == 26 || Build.VERSION.SDK_INT == 27 || Build.VERSION.SDK_INT == 28 || Build.VERSION.SDK_INT == 29 || Build.VERSION.SDK_INT == 30 || Build.VERSION.SDK_INT == 33) {
                list {
                    title = LocaleController.getString("AP_ChangeIcon", R.string.AP_ChangeIcon)

                    contractIcons({
                        return@contractIcons listOf(
                            Triple(0, LocaleController.getString("AP_ChangeIcon_Default", R.string.AP_ChangeIcon_Default), R.mipmap.cg_launcher_default),
                            Triple(1, LocaleController.getString("AP_ChangeIcon_White", R.string.AP_ChangeIcon_White), R.mipmap.cg_launcher_white),
                        )
                    }, {
                        return@contractIcons when (CherrygramConfig.change_Icon2) {
                            1 -> LocaleController.getString("AP_ChangeIcon_White", R.string.AP_ChangeIcon_White)
                            else -> LocaleController.getString("AP_ChangeIcon_Default", R.string.AP_ChangeIcon_Default)
                        }
                    }) {
                        CherrygramConfig.change_Icon2 = it
                        IconExtras.setIcon(it)
                    }
                }
            }
            list {
                title = LocaleController.getString("AP_IconReplacements", R.string.AP_IconReplacements)

                contractIcons({
                    return@contractIcons listOf(
                            Triple(0, LocaleController.getString("AP_IconReplacement_VKUI", R.string.AP_IconReplacement_VKUI), R.drawable.settings_outline_28),
                            Triple(1, LocaleController.getString("AP_IconReplacement_Default", R.string.AP_IconReplacement_Default), R.drawable.menu_settings)
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
            switch {
                title = LocaleController.getString("AP_BackButton", R.string.AP_BackButton)
                summary = LocaleController.getString("AP_BackButton_Desc", R.string.AP_BackButton_Desc)

                contract({
                    return@contract CherrygramConfig.BackButton
                }) {
                    CherrygramConfig.BackButton = it
                    (bf.parentActivity as? LaunchActivity)?.reloadResources()
                }
            }
        }

        category(LocaleController.getString("AP_General", R.string.AP_General)) {
            switch {
                title = LocaleController.getString("AP_HideUserPhone", R.string.AP_HideUserPhone)
                summary = LocaleController.getString("AP_HideUserPhoneSummary", R.string.AP_HideUserPhoneSummary)

                contract({
                    return@contract CherrygramConfig.hidePhoneNumber
                }) {
                    CherrygramConfig.hidePhoneNumber = it
                }
            }
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                switch {
                    title = LocaleController.getString("AP_FlatSB", R.string.AP_FlatSB)

                    contract({
                        return@contract SharedConfig.noStatusBar
                    }) {
                        SharedConfig.toggleNoStatusBar()
                        bf.parentActivity.window.statusBarColor = if (Theme.getColor(Theme.key_actionBarDefault, null, true) == Color.WHITE) CherrygramExtras.lightStatusbarColor else CherrygramExtras.darkStatusbarColor
                    }
                }
            }
            switch {
                title = LocaleController.getString("AP_MutualContacts", R.string.AP_MutualContacts)
                summary = LocaleController.getString("AP_MutualContacts_Desc", R.string.AP_MutualContacts_Desc)

                contract({
                    return@contract CherrygramConfig.mutualContacts
                }) {
                    CherrygramConfig.mutualContacts = it
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

        category(LocaleController.getString("AS_Filters_Header", R.string.CP_Filters_Header)) {
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
                title = LocaleController.getString("CP_NewTabs_NoCounter", R.string.CP_NewTabs_NoCounter)
                summary = LocaleController.getString("CP_NewTabs_NoCounter_Desc", R.string.CP_NewTabs_NoCounter_Desc)

                contract({
                    return@contract CherrygramConfig.newTabs_noUnread
                }) {
                    CherrygramConfig.newTabs_noUnread = it
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

        category(LocaleController.getString("AP_ProfileCategory", R.string.AP_ProfileCategory)) {
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

        category(LocaleController.getString("AP_DrawerCategory", R.string.AP_DrawerCategory)) {
            switch {
                title = LocaleController.getString("AP_DrawerAvatar", R.string.AP_DrawerAvatar)

                contract({
                    return@contract CherrygramConfig.drawerAvatar
                }) {
                    CherrygramConfig.drawerAvatar = it
                }
            }
            switch {
                title = LocaleController.getString("AP_DrawerBlur", R.string.AP_DrawerBlur)

                contract({
                    return@contract CherrygramConfig.drawerBlur
                }) {
                    CherrygramConfig.drawerBlur = it
                }
            }
            switch {
                title = LocaleController.getString("AP_DrawerDarken", R.string.AP_DrawerDarken)

                contract({
                    return@contract CherrygramConfig.drawerDarken
                }) {
                    CherrygramConfig.drawerDarken = it
                }
            }
        }

        category(LocaleController.getString("AP_DrawerButtonsCategory", R.string.AP_DrawerButtonsCategory)) {
            switch {
                title = LocaleController.getString("AP_DrawerButtonsSaved", R.string.AP_DrawerButtonsSaved)

                contract({
                    return@contract CherrygramConfig.savedMessagesDrawerButton
                }) {
                    CherrygramConfig.savedMessagesDrawerButton = it
                }
            }
            switch {
                title = LocaleController.getString("AP_DrawerButtonsArchived", R.string.AP_DrawerButtonsArchived)

                contract({
                    return@contract CherrygramConfig.archivedChatsDrawerButton
                }) {
                    CherrygramConfig.archivedChatsDrawerButton = it
                }
            }
            switch {
                title = LocaleController.getString("AP_DrawerButtonsPeopleNearby", R.string.AP_DrawerButtonsPeopleNearby)

                contract({
                    return@contract CherrygramConfig.peopleNearbyDrawerButton
                }) {
                    CherrygramConfig.peopleNearbyDrawerButton = it
                }
            }
        }
    }
}
