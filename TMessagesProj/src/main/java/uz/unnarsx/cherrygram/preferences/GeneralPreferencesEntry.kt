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
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.LaunchActivity
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper
import uz.unnarsx.cherrygram.core.helpers.FirebaseAnalyticsHelper
import uz.unnarsx.cherrygram.preferences.helpers.AlertDialogSwitchers
import uz.unnarsx.cherrygram.preferences.tgkit.preference.category
import uz.unnarsx.cherrygram.preferences.tgkit.preference.contract
import uz.unnarsx.cherrygram.preferences.tgkit.preference.hint
import uz.unnarsx.cherrygram.preferences.tgkit.preference.list
import uz.unnarsx.cherrygram.preferences.tgkit.preference.switch
import uz.unnarsx.cherrygram.preferences.tgkit.preference.textIcon
import uz.unnarsx.cherrygram.preferences.tgkit.preference.tgKitScreen
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitTextIconRow

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
            list {
                title = getString(R.string.AP_Tablet_Mode)

                contract({
                    return@contract listOf(
                        Pair(CherrygramCoreConfig.TABLET_MODE_AUTO, getString(R.string.QualityAuto)),
                        Pair(CherrygramCoreConfig.TABLET_MODE_ENABLE, getString(R.string.EP_DownloadSpeedBoostAverage)),
                        Pair(CherrygramCoreConfig.TABLET_MODE_DISABLE, getString(R.string.EP_DownloadSpeedBoostNone))
                    )
                }, {
                    return@contract when (CherrygramCoreConfig.tabletMode) {
                        CherrygramCoreConfig.TABLET_MODE_ENABLE -> getString(R.string.EP_DownloadSpeedBoostAverage)
                        CherrygramCoreConfig.TABLET_MODE_DISABLE -> getString(R.string.EP_DownloadSpeedBoostNone)
                        else -> getString(R.string.QualityAuto)
                    }
                }) {
                    CherrygramCoreConfig.tabletMode = it
                    AndroidUtilities.resetTabletFlag()
                    if (bf.parentActivity is LaunchActivity) {
                        (bf.parentActivity as LaunchActivity).invalidateTabletMode()
                    }
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
            textIcon {
                title = getString(R.string.CP_ArchiveStories)
                icon = R.drawable.msg_archive
                listener = TGKitTextIconRow.TGTIListener {
                    AlertDialogSwitchers.showArchiveStoriesAlert(bf)
                }
                divider = true
            }
            hint(getString(R.string.CP_ArchiveStories_Desc))
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

        FirebaseAnalyticsHelper.trackEventWithEmptyBundle("general_preferences_screen")
    }
}
