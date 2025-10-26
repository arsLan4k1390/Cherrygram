/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.preferences

import android.content.Intent
import android.os.Build
import androidx.core.util.Pair
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.NotificationCenter
import org.telegram.messenger.NotificationsService
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.LaunchActivity
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper
import uz.unnarsx.cherrygram.core.helpers.FirebaseAnalyticsHelper
import uz.unnarsx.cherrygram.helpers.ui.PopupHelper
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
            switch {
                title = getString(R.string.AP_Old_Notification_Icon)
                contract({
                    return@contract CherrygramCoreConfig.oldNotificationIcon
                }) {
                    CherrygramCoreConfig.oldNotificationIcon = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
            switch {
                isAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM
                title = getString(R.string.CG_ResidentNotification)
                description = getString(R.string.NotificationsService)

                contract({
                    return@contract CherrygramCoreConfig.residentNotification
                }) {
                    CherrygramCoreConfig.residentNotification = it
                    ApplicationLoader.applicationContext.stopService(
                        Intent(
                            ApplicationLoader.applicationContext,
                            NotificationsService::class.java
                        )
                    )
                    ApplicationLoader.startPushService()
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
        }

        category(getString(R.string.CP_PremAndAnim_Header)) {
            switch {
                title = getString(R.string.CP_HideStories)
                description = getString(R.string.CP_HideStories_Desc)

                contract({
                    return@contract CherrygramCoreConfig.hideStories
                }) {
                    CherrygramCoreConfig.hideStories = it
                    bf.notificationCenter.postNotificationName(NotificationCenter.storiesEnabledUpdate)
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
            textIcon {
                title = getString(R.string.CP_ArchiveStories)
                icon = R.drawable.msg_archive
                listener = TGKitTextIconRow.TGTIListener {
                    showStoriesArchiveConfigurator(bf)
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

    private fun showStoriesArchiveConfigurator(fragment: BaseFragment) {
        val menuItems = listOf(
            MenuItemConfig(
                getString(R.string.FilterContacts),
                R.drawable.msg_contacts,
                { CherrygramCoreConfig.archiveStoriesFromUsers },
                { CherrygramCoreConfig.archiveStoriesFromUsers = !CherrygramCoreConfig.archiveStoriesFromUsers }
            ),
            MenuItemConfig(
                getString(R.string.FilterChannels),
                R.drawable.msg_channel,
                { CherrygramCoreConfig.archiveStoriesFromChannels },
                { CherrygramCoreConfig.archiveStoriesFromChannels = !CherrygramCoreConfig.archiveStoriesFromChannels }
            ),
        )

        val prefTitle = ArrayList<String>()
        val prefIcon = ArrayList<Int>()
        val prefCheck = ArrayList<Boolean>()
        val prefDivider = ArrayList<Boolean>()
        val clickListener = ArrayList<Runnable>()

        for (item in menuItems) {
            prefTitle.add(item.titleRes)
            prefIcon.add(item.iconRes)
            prefCheck.add(item.isChecked())
            prefDivider.add(item.divider)
            clickListener.add(Runnable { item.toggle() })
        }

        PopupHelper.showSwitchAlert(
            getString(R.string.CP_ArchiveStories),
            fragment,
            prefTitle,
            prefIcon,
            prefCheck,
            null,
            null,
            prefDivider,
            clickListener,
            null
        )

    }

    data class MenuItemConfig(
        val titleRes: String,
        val iconRes: Int,
        val isChecked: () -> Boolean,
        val toggle: () -> Unit,
        val divider: Boolean = false
    )

}
