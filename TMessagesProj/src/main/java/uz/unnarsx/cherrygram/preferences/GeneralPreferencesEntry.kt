package uz.unnarsx.cherrygram.preferences

import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.BaseFragment
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper
import uz.unnarsx.cherrygram.preferences.tgkit.preference.category
import uz.unnarsx.cherrygram.preferences.tgkit.preference.contract
import uz.unnarsx.cherrygram.preferences.tgkit.preference.switch
import uz.unnarsx.cherrygram.preferences.tgkit.preference.tgKitScreen

class GeneralPreferencesEntry : BasePreferencesEntry {
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(getString(R.string.AP_Header_General)) {
        category(getString(R.string.AP_Header_General)) {
            switch {
                title = getString(R.string.CP_NoRounding)
                description = getString(R.string.CP_NoRoundingSummary)

                contract({
                    return@contract CherrygramCoreConfig.noRounding
                }) {
                    CherrygramCoreConfig.noRounding = it
                }
            }
            switch {
                title = getString(R.string.AP_SystemEmoji)
                contract({
                    return@contract CherrygramCoreConfig.systemEmoji
                }) {
                    CherrygramCoreConfig.systemEmoji = it
                }
            }
            switch {
                title = getString(R.string.AP_SystemFonts)

                contract({
                    return@contract CherrygramCoreConfig.systemFonts
                }) {
                    CherrygramCoreConfig.systemFonts = it
                    AndroidUtilities.clearTypefaceCache()
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
            switch {
                title = getString(R.string.AP_Old_Notification_Icon)
                contract({
                    return@contract CherrygramCoreConfig.oldNotificationIcon
                }) {
                    CherrygramCoreConfig.oldNotificationIcon = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
        }

        category(getString(R.string.CP_PremAndAnim_Header)) {
            switch {
                title = getString(R.string.CP_HideStories)

                contract({
                    return@contract CherrygramCoreConfig.hideStories
                }) {
                    CherrygramCoreConfig.hideStories = it
                }
            }
            switch {
                title = getString(R.string.CP_CustomWallpapers)
                description = getString(R.string.CP_CustomWallpapers_Desc)

                contract({
                    return@contract CherrygramCoreConfig.customWallpapers
                }) {
                    CherrygramCoreConfig.customWallpapers = it
                }
            }
            switch {
                title = getString(R.string.CP_DisableAnimAvatars)

                contract({
                    return@contract CherrygramCoreConfig.disableAnimatedAvatars
                }) {
                    CherrygramCoreConfig.disableAnimatedAvatars = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
            switch {
                title = getString(R.string.CP_DisableReactionsOverlay)
                description = getString(R.string.CP_DisableReactionsOverlay_Desc)

                contract({
                    return@contract CherrygramCoreConfig.disableReactionsOverlay
                }) {
                    CherrygramCoreConfig.disableReactionsOverlay = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
            switch {
                title = getString(R.string.CP_DisableReactionAnim)
                description = getString(R.string.CP_DisableReactionAnim_Desc)

                contract({
                    return@contract CherrygramCoreConfig.disableReactionAnim
                }) {
                    CherrygramCoreConfig.disableReactionAnim = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
            switch {
                title = getString(R.string.CP_DisablePremStickAnim)
                description = getString(R.string.CP_DisablePremStickAnim_Desc)

                contract({
                    return@contract CherrygramCoreConfig.disablePremStickAnim
                }) {
                    CherrygramCoreConfig.disablePremStickAnim = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
            switch {
                title = getString(R.string.CP_DisablePremStickAutoPlay)
                description = getString(R.string.CP_DisablePremStickAutoPlay_Desc)

                contract({
                    return@contract CherrygramCoreConfig.disablePremStickAutoPlay
                }) {
                    CherrygramCoreConfig.disablePremStickAutoPlay = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
            switch {
                title = getString(R.string.CP_HideSendAsChannel)
                description = getString(R.string.CP_HideSendAsChannelDesc)

                contract({
                    return@contract CherrygramCoreConfig.hideSendAsChannel
                }) {
                    CherrygramCoreConfig.hideSendAsChannel = it
                }
            }

        }
    }
}
