package uz.unnarsx.cherrygram.preferences

import android.app.Activity
import android.content.SharedPreferences
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.Components.BulletinFactory
import uz.unnarsx.cherrygram.CherrygramConfig
import uz.unnarsx.cherrygram.preferences.drawer.DrawerPreferencesEntry
import uz.unnarsx.cherrygram.tgkit.preference.*
import uz.unnarsx.cherrygram.tgkit.preference.types.TGKitTextIconRow

class GeneralPreferencesEntry : BasePreferencesEntry {
    val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(LocaleController.getString("AP_Header_General", R.string.AP_Header_General)) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(CherrygramConfig.listener)
        category(LocaleController.getString("CP_PremAndAnim_Header", R.string.CP_PremAndAnim_Header)) {
            switch {
                title = LocaleController.getString("CP_DisableAnimAvatars", R.string.CP_DisableAnimAvatars)

                contract({
                    return@contract CherrygramConfig.disableAnimatedAvatars
                }) {
                    CherrygramConfig.disableAnimatedAvatars = it
                    BulletinFactory.of(bf).createRestartBulletin(
                        R.raw.chats_infotip,
                        LocaleController.getString("CG_RestartToApply", R.string.CG_RestartToApply),
                        LocaleController.getString("BotUnblock", R.string.BotUnblock)
                    ) {
                    }.show()
                }
            }
            switch {
                title = LocaleController.getString("CP_DisableReactionsOverlay", R.string.CP_DisableReactionsOverlay)
                summary = LocaleController.getString("CP_DisableReactionsOverlay_Desc", R.string.CP_DisableReactionsOverlay_Desc)

                contract({
                    return@contract CherrygramConfig.disableReactionsOverlay
                }) {
                    CherrygramConfig.disableReactionsOverlay = it
                    BulletinFactory.of(bf).createRestartBulletin(
                        R.raw.chats_infotip,
                        LocaleController.getString("CG_RestartToApply", R.string.CG_RestartToApply),
                        LocaleController.getString("BotUnblock", R.string.BotUnblock)
                    ) {
                    }.show()
                }
            }
            switch {
                title = LocaleController.getString("CP_DrawSmallReactions", R.string.CP_DrawSmallReactions)
                summary = LocaleController.getString("CP_DrawSmallReactions_Desc", R.string.CP_DrawSmallReactions_Desc)

                contract({
                    return@contract CherrygramConfig.drawSmallReactions
                }) {
                    CherrygramConfig.drawSmallReactions = it
                    BulletinFactory.of(bf).createRestartBulletin(
                        R.raw.chats_infotip,
                        LocaleController.getString("CG_RestartToApply", R.string.CG_RestartToApply),
                        LocaleController.getString("BotUnblock", R.string.BotUnblock)
                    ) {
                    }.show()
                }
            }
            switch {
                title = LocaleController.getString("CP_DisableReactionAnim", R.string.CP_DisableReactionAnim)
                summary = LocaleController.getString("CP_DisableReactionAnim_Desc", R.string.CP_DisableReactionAnim_Desc)

                contract({
                    return@contract CherrygramConfig.disableReactionAnim
                }) {
                    CherrygramConfig.disableReactionAnim = it
                    BulletinFactory.of(bf).createRestartBulletin(
                        R.raw.chats_infotip,
                        LocaleController.getString("CG_RestartToApply", R.string.CG_RestartToApply),
                        LocaleController.getString("BotUnblock", R.string.BotUnblock)
                    ) {
                    }.show()
                }
            }
            switch {
                title = LocaleController.getString("CP_DisablePremiumStatuses", R.string.CP_DisablePremiumStatuses)
                summary = LocaleController.getString("CP_DisablePremiumStatuses_Desc", R.string.CP_DisablePremiumStatuses_Desc)

                contract({
                    return@contract CherrygramConfig.disablePremiumStatuses
                }) {
                    CherrygramConfig.disablePremiumStatuses = it
                    BulletinFactory.of(bf).createRestartBulletin(
                        R.raw.chats_infotip,
                        LocaleController.getString("CG_RestartToApply", R.string.CG_RestartToApply),
                        LocaleController.getString("BotUnblock", R.string.BotUnblock)
                    ) {
                    }.show()
                }
            }
            switch {
                title = LocaleController.getString("CP_DisablePremStickAnim", R.string.CP_DisablePremStickAnim)
                summary = LocaleController.getString("CP_DisablePremStickAnim_Desc", R.string.CP_DisablePremStickAnim_Desc)

                contract({
                    return@contract CherrygramConfig.disablePremStickAnim
                }) {
                    CherrygramConfig.disablePremStickAnim = it
                    BulletinFactory.of(bf).createRestartBulletin(
                        R.raw.chats_infotip,
                        LocaleController.getString("CG_RestartToApply", R.string.CG_RestartToApply),
                        LocaleController.getString("BotUnblock", R.string.BotUnblock)
                    ) {
                    }.show()
                }
            }
            switch {
                title = LocaleController.getString("CP_DisablePremStickAutoPlay", R.string.CP_DisablePremStickAutoPlay)
                summary = LocaleController.getString("CP_DisablePremStickAutoPlay_Desc", R.string.CP_DisablePremStickAutoPlay_Desc)

                contract({
                    return@contract CherrygramConfig.disablePremStickAutoPlay
                }) {
                    CherrygramConfig.disablePremStickAutoPlay = it
                    BulletinFactory.of(bf).createRestartBulletin(
                        R.raw.chats_infotip,
                        LocaleController.getString("CG_RestartToApply", R.string.CG_RestartToApply),
                        LocaleController.getString("BotUnblock", R.string.BotUnblock)
                    ) {
                    }.show()
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
                    BulletinFactory.of(bf).createRestartBulletin(
                        R.raw.chats_infotip,
                        LocaleController.getString("CG_RestartToApply", R.string.CG_RestartToApply),
                        LocaleController.getString("BotUnblock", R.string.BotUnblock)
                    ) {
                    }.show()
                }
            }
            switch {
                title = LocaleController.getString("AP_ShowID", R.string.AP_ShowID)
                contract({
                    return@contract CherrygramConfig.showId
                }) {
                    CherrygramConfig.showId = it
                    BulletinFactory.of(bf).createRestartBulletin(
                        R.raw.chats_infotip,
                        LocaleController.getString("CG_RestartToApply", R.string.CG_RestartToApply),
                        LocaleController.getString("BotUnblock", R.string.BotUnblock)
                    ) {
                    }.show()
                }
            }
            switch {
                title = LocaleController.getString("AP_ShowDC", R.string.AP_ShowDC)
                contract({
                    return@contract CherrygramConfig.showDc
                }) {
                    CherrygramConfig.showDc = it
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
