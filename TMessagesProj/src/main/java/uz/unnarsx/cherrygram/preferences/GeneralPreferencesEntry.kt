package uz.unnarsx.cherrygram.preferences

import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.BaseFragment
import uz.unnarsx.cherrygram.CherrygramConfig
import uz.unnarsx.cherrygram.helpers.AppRestartHelper
import uz.unnarsx.cherrygram.ui.tgkit.preference.category
import uz.unnarsx.cherrygram.ui.tgkit.preference.contract
import uz.unnarsx.cherrygram.ui.tgkit.preference.switch
import uz.unnarsx.cherrygram.ui.tgkit.preference.tgKitScreen

class GeneralPreferencesEntry : BasePreferencesEntry {
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(LocaleController.getString("AP_Header_General", R.string.AP_Header_General)) {
        category(LocaleController.getString("AP_Header_General", R.string.AP_Header_General)) {
            switch {
                title = LocaleController.getString("AS_NoRounding", R.string.CP_NoRounding)
                description = LocaleController.getString("AS_NoRoundingSummary", R.string.CP_NoRoundingSummary)

                contract({
                    return@contract CherrygramConfig.noRounding
                }) {
                    CherrygramConfig.noRounding = it
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

                contract({
                    return@contract CherrygramConfig.systemFonts
                }) {
                    CherrygramConfig.systemFonts = it
                    AndroidUtilities.clearTypefaceCache()
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
            switch {
                title = LocaleController.getString("AP_Old_Notification_Icon", R.string.AP_Old_Notification_Icon)
                contract({
                    return@contract CherrygramConfig.oldNotificationIcon
                }) {
                    CherrygramConfig.oldNotificationIcon = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
        }

        category(LocaleController.getString("CP_PremAndAnim_Header", R.string.CP_PremAndAnim_Header)) {
            switch {
                title = LocaleController.getString("CP_HideStories", R.string.CP_HideStories)

                contract({
                    return@contract CherrygramConfig.hideStories
                }) {
                    CherrygramConfig.hideStories = it
                }
            }
            switch {
                title = LocaleController.getString("CP_CustomWallpapers", R.string.CP_CustomWallpapers)
                description = LocaleController.getString("CP_CustomWallpapers_Desc", R.string.CP_CustomWallpapers_Desc)

                contract({
                    return@contract CherrygramConfig.customWallpapers
                }) {
                    CherrygramConfig.customWallpapers = it
                }
            }
            switch {
                title = LocaleController.getString("CP_DisableAnimAvatars", R.string.CP_DisableAnimAvatars)

                contract({
                    return@contract CherrygramConfig.disableAnimatedAvatars
                }) {
                    CherrygramConfig.disableAnimatedAvatars = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
            switch {
                title = LocaleController.getString("CP_DisableReactionsOverlay", R.string.CP_DisableReactionsOverlay)
                description = LocaleController.getString("CP_DisableReactionsOverlay_Desc", R.string.CP_DisableReactionsOverlay_Desc)

                contract({
                    return@contract CherrygramConfig.disableReactionsOverlay
                }) {
                    CherrygramConfig.disableReactionsOverlay = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
            switch {
                title = LocaleController.getString("CP_DisableReactionAnim", R.string.CP_DisableReactionAnim)
                description = LocaleController.getString("CP_DisableReactionAnim_Desc", R.string.CP_DisableReactionAnim_Desc)

                contract({
                    return@contract CherrygramConfig.disableReactionAnim
                }) {
                    CherrygramConfig.disableReactionAnim = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
            switch {
                title = LocaleController.getString("CP_DisablePremStickAnim", R.string.CP_DisablePremStickAnim)
                description = LocaleController.getString("CP_DisablePremStickAnim_Desc", R.string.CP_DisablePremStickAnim_Desc)

                contract({
                    return@contract CherrygramConfig.disablePremStickAnim
                }) {
                    CherrygramConfig.disablePremStickAnim = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
            switch {
                title = LocaleController.getString("CP_DisablePremStickAutoPlay", R.string.CP_DisablePremStickAutoPlay)
                description = LocaleController.getString("CP_DisablePremStickAutoPlay_Desc", R.string.CP_DisablePremStickAutoPlay_Desc)

                contract({
                    return@contract CherrygramConfig.disablePremStickAutoPlay
                }) {
                    CherrygramConfig.disablePremStickAutoPlay = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
            switch {
                title = LocaleController.getString("CP_HideSendAsChannel", R.string.CP_HideSendAsChannel)
                description = LocaleController.getString("CP_HideSendAsChannelDesc", R.string.CP_HideSendAsChannelDesc)

                contract({
                    return@contract CherrygramConfig.hideSendAsChannel
                }) {
                    CherrygramConfig.hideSendAsChannel = it
                }
            }

        }
    }
}
