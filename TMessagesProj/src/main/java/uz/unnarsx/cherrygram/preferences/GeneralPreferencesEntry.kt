package uz.unnarsx.cherrygram.preferences

import android.app.Activity
import android.content.SharedPreferences
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.BaseFragment
import uz.unnarsx.cherrygram.CherrygramConfig
import uz.unnarsx.cherrygram.tgkit.preference.*

class GeneralPreferencesEntry : BasePreferencesEntry {
    val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(LocaleController.getString("AP_Header_General", R.string.AP_Header_General)) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(CherrygramConfig.listener)
        category(LocaleController.getString("AP_Header_General", R.string.AP_Header_General)) {
            switch {
                title = LocaleController.getString("AS_NoRounding", R.string.CP_NoRounding)
                summary = LocaleController.getString("AS_NoRoundingSummary", R.string.CP_NoRoundingSummary)

                contract({
                    return@contract CherrygramConfig.noRounding
                }) {
                    CherrygramConfig.noRounding = it
                }
            }
            switch {
                title = LocaleController.getString("CP_ShowSeconds", R.string.CP_ShowSeconds)
                summary = LocaleController.getString("CP_ShowSeconds_Desc", R.string.CP_ShowSeconds_Desc)

                contract({
                    return@contract CherrygramConfig.showSeconds
                }) {
                    CherrygramConfig.showSeconds = it
                    createRestartBulletin(bf)
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
                    createRestartBulletin(bf)
                }
            }
            switch {
                title = LocaleController.getString("AP_Old_Notification_Icon", R.string.AP_Old_Notification_Icon)
                contract({
                    return@contract CherrygramConfig.oldNotificationIcon
                }) {
                    CherrygramConfig.oldNotificationIcon = it
                    createRestartBulletin(bf)
                }
            }
        }

        category(LocaleController.getString("AP_ProfileCategory", R.string.AP_ProfileCategory)) {
            switch {
                title = LocaleController.getString("CP_ConfirmCalls", R.string.CP_ConfirmCalls)

                contract({
                    return@contract CherrygramConfig.confirmCalls
                }) {
                    CherrygramConfig.confirmCalls = it
                }
            }
            switch {
                title = LocaleController.getString("AP_HideUserPhone", R.string.AP_HideUserPhone)
                summary = LocaleController.getString("AP_HideUserPhoneSummary", R.string.AP_HideUserPhoneSummary)

                contract({
                    return@contract CherrygramConfig.hidePhoneNumber
                }) {
                    CherrygramConfig.hidePhoneNumber = it
                    createRestartBulletin(bf)
                }
            }
            switch {
                title = LocaleController.getString("AP_ShowID", R.string.AP_ShowID)
                contract({
                    return@contract CherrygramConfig.showId
                }) {
                    CherrygramConfig.showId = it
                    createRestartBulletin(bf)
                }
            }
            switch {
                title = LocaleController.getString("AP_ShowDC", R.string.AP_ShowDC)
                contract({
                    return@contract CherrygramConfig.showDc
                }) {
                    CherrygramConfig.showDc = it
                    createRestartBulletin(bf)
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
                    createRestartBulletin(bf)
                }
            }
            switch {
                title = LocaleController.getString("CP_DisableReactionsOverlay", R.string.CP_DisableReactionsOverlay)
                summary = LocaleController.getString("CP_DisableReactionsOverlay_Desc", R.string.CP_DisableReactionsOverlay_Desc)

                contract({
                    return@contract CherrygramConfig.disableReactionsOverlay
                }) {
                    CherrygramConfig.disableReactionsOverlay = it
                    createRestartBulletin(bf)
                }
            }
            switch {
                title = LocaleController.getString("CP_DrawSmallReactions", R.string.CP_DrawSmallReactions)
                summary = LocaleController.getString("CP_DrawSmallReactions_Desc", R.string.CP_DrawSmallReactions_Desc)

                contract({
                    return@contract CherrygramConfig.drawSmallReactions
                }) {
                    CherrygramConfig.drawSmallReactions = it
                    createRestartBulletin(bf)
                }
            }
            switch {
                title = LocaleController.getString("CP_DisableReactionAnim", R.string.CP_DisableReactionAnim)
                summary = LocaleController.getString("CP_DisableReactionAnim_Desc", R.string.CP_DisableReactionAnim_Desc)

                contract({
                    return@contract CherrygramConfig.disableReactionAnim
                }) {
                    CherrygramConfig.disableReactionAnim = it
                    createRestartBulletin(bf)
                }
            }
            switch {
                title = LocaleController.getString("CP_DisablePremiumStatuses", R.string.CP_DisablePremiumStatuses)
                summary = LocaleController.getString("CP_DisablePremiumStatuses_Desc", R.string.CP_DisablePremiumStatuses_Desc)

                contract({
                    return@contract CherrygramConfig.disablePremiumStatuses
                }) {
                    CherrygramConfig.disablePremiumStatuses = it
                    createRestartBulletin(bf)
                }
            }
            switch {
                title = LocaleController.getString("CP_DisablePremStickAnim", R.string.CP_DisablePremStickAnim)
                summary = LocaleController.getString("CP_DisablePremStickAnim_Desc", R.string.CP_DisablePremStickAnim_Desc)

                contract({
                    return@contract CherrygramConfig.disablePremStickAnim
                }) {
                    CherrygramConfig.disablePremStickAnim = it
                    createRestartBulletin(bf)
                }
            }
            switch {
                title = LocaleController.getString("CP_DisablePremStickAutoPlay", R.string.CP_DisablePremStickAutoPlay)
                summary = LocaleController.getString("CP_DisablePremStickAutoPlay_Desc", R.string.CP_DisablePremStickAutoPlay_Desc)

                contract({
                    return@contract CherrygramConfig.disablePremStickAutoPlay
                }) {
                    CherrygramConfig.disablePremStickAutoPlay = it
                    createRestartBulletin(bf)
                }
            }
            switch {
                title = LocaleController.getString("CP_HideSendAsChannel", R.string.CP_HideSendAsChannel)

                contract({
                    return@contract CherrygramConfig.hideSendAsChannel
                }) {
                    CherrygramConfig.hideSendAsChannel = it
                }
            }

        }
    }
}
