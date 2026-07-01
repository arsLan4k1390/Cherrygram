/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.ui

import android.os.Build
import androidx.annotation.RequiresApi
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ContactsController
import org.telegram.messenger.LocaleController.formatString
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.PasskeysController
import org.telegram.messenger.R
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.TLObject
import org.telegram.tgnet.TLRPC
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.Components.Bulletin
import org.telegram.ui.Components.BulletinFactory
import org.telegram.ui.LaunchActivity
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper
import uz.unnarsx.cherrygram.preferences.CherrygramPreferencesNavigator

object CGBulletinCreator {

    fun createRestartBulletin(fragment: BaseFragment) {
        BulletinFactory.of(fragment).createSimpleBulletin(
            R.raw.chats_infotip,
            getString(R.string.CG_RestartToApply),
            getString(R.string.BotUnblock)
        ) {
            AppRestartHelper.restartApp(fragment.context)
        }.show()
    }

    fun createDebugSuccessBulletin(fragment: BaseFragment) {
        BulletinFactory.of(fragment)
            .createSuccessBulletin(getString(R.string.YourPasswordSuccess))
            .setDuration(Bulletin.DURATION_LONG)
            .show()
    }

    fun createRequireDonateBulletin(fragment: BaseFragment) {
        BulletinFactory.of(fragment).createSimpleBulletin(
            R.raw.cg_star_reaction,  // stars_topup // star_premium_2
            getString(R.string.DP_Donate_Exclusive),
            getString(R.string.DP_Donate_ExclusiveDesc),
            getString(R.string.MoreInfo)
        ) {
            CherrygramPreferencesNavigator.createDonate(fragment, !fragment.connectionsManager.isTestBackend)
        }.show()
    }

    fun createSwitchAccountBulletin(account: Int) {
        val nextAcc = UserConfig.getInstance(account).currentUser

        if (nextAcc is TLRPC.User) {
            AndroidUtilities.runOnUIThread({

                val activity = LaunchActivity.instance
                if (activity == null || activity.isFinishing || activity.isDestroyed) {
                    return@runOnUIThread
                }

                val accs = ArrayList<TLObject?>()
                accs.add(nextAcc)

                val text = AndroidUtilities.replaceTags(
                    formatString(
                        R.string.CG_SwitchedToAccount,
                        ContactsController.formatName(nextAcc.first_name, nextAcc.last_name)
                    )
                )

                BulletinFactory.global()
                    .createChatsBulletin(accs, text, null)
                    .setDuration(Bulletin.DURATION_SHORT)
                    .show()

                accs.clear()
            }, 200)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun createPasskeyBulletin(fragment: BaseFragment) {
        BulletinFactory.of(fragment).createSimpleBulletin(
            R.raw.passkey,
            getString(R.string.CG_PasskeyNoCredentialAvailable),
            getString(R.string.Settings)
        ) {
            PasskeysController.openSettings(fragment.getParentActivity())
        }.show()
    }

}
