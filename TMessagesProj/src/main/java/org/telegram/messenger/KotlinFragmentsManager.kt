/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package org.telegram.messenger

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.withContext
import org.telegram.tgnet.ConnectionManagerDelegate
import uz.unnarsx.cherrygram.Extra
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper
import uz.unnarsx.cherrygram.misc.Constants
import java.util.Locale

object KotlinFragmentsManager: CoroutineScope by MainScope() {

    suspend fun checkConnection() = withContext(Dispatchers.Default) {
//        etrioaei43()
//        kutyferw()
//        li9yhtyr6()
//        bfvjkqtbse74()
//        yyey5436tw3rq3q()
//        thb34y3ye5()
//        nerw4278c2()
    }

    private fun etrioaei43() {
        val good = Extra.HASH_ARRAY.joinToString().replace(",", "").replace(" ", "")
        val info = AndroidUtilities.getCertificateSHA256Fingerprint()
        if (info != good) {
            nfweioufwehr117()
        }
    }

    private fun kutyferw() {
        val good = Extra.APP_ARRAY.joinToString().replace(",", "").replace(" ", "")
        val info = jhfkugrrgg()
        if (info != good) {
            nfweioufwehr117()
        }
    }

    private fun li9yhtyr6() {
        val good = Constants.PACKAGE_NAME
        val info = ApplicationLoader.applicationContext.packageName
        if (info != good) {
            nfweioufwehr117()
        }
    }

    private fun bfvjkqtbse74() {
        val connectionManagerDelegate = ConnectionManagerDelegate(ApplicationLoader.applicationContext)
        val good = Extra.SMS_ARRAY.joinToString().replace(",", "").replace(" ", "")
        val info = connectionManagerDelegate.appSignature
        if (info != good) {
            nfweioufwehr117()
        }
    }

    /*private fun fuckOff() {
        Log.d("FuckOff", ApplicationLoader.applicationContext.packageManager.getPackageInfo(Constants.PACKAGE_NAME, PackageManager.GET_SIGNATURES).signatures[0].toCharsString())
    }*/

    private fun jhfkugrrgg(): CharSequence {
        var applicationInfo: ApplicationInfo? = null
        try {
            applicationInfo = ApplicationLoader.applicationContext.packageManager.getApplicationInfo(ApplicationLoader.applicationContext.applicationInfo.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
        }
        return (if (applicationInfo != null) ApplicationLoader.applicationContext.packageManager.getApplicationLabel(applicationInfo) else "Unknown")
    }

    private fun yyey5436tw3rq3q() {
        val good = BuildConfig.BUILD_VERSION_STRING
        val info = BuildVars.BUILD_VERSION_STRING
        if (info != good) {
            nfweioufwehr117()
        }
    }

    fun vnwpoih23nkjhqj(text: CharSequence) {
        val allDrawerItems = ArrayList<CharSequence>()
        allDrawerItems.add(text)

        val notAllowedItems = ArrayList<CharSequence>()
        notAllowedItems.add(Extra.Name_ArrTwo1.joinToString().replace(",", "").replace(" ", ""))
        notAllowedItems.add(Extra.Name_ArrTwo2.joinToString().replace(",", "").replace(" ", ""))
        notAllowedItems.add(Extra.Name_ArrTwo3.joinToString().replace(",", "").replace(" ", ""))
        notAllowedItems.add(Extra.Name_ArrTwo4.joinToString().replace(",", "").replace(" ", ""))
        notAllowedItems.add(Extra.Name_ArrTwo5.joinToString().replace(",", "").replace(" ", ""))
        notAllowedItems.add(
            Extra.Name_ArrTwo1.joinToString().replace(",", "").replace(" ", "") +
            " " +
            Extra.Name_ArrTwo2.joinToString().replace(",", "").replace(" ", "")+
            " " +
            Extra.Name_ArrTwo3.joinToString().replace(",", "").replace(" ", "")
        )
        notAllowedItems.add(
            Extra.Name_ArrTwo1.joinToString().replace(",", "").replace(" ", "") +
            " " +
            Extra.Name_ArrTwo2.joinToString().replace(",", "").replace(" ", "")+
            Extra.Name_ArrTwo3.joinToString().replace(",", "").replace(" ", "")
        )

        for (id in notAllowedItems) {
            if (allDrawerItems.contains(id)) {
                nfweioufwehr117()
            }
        }
    }

    fun vreg42r2r2r1r3q1rq3(input: String): Boolean {
        var normalized = input.lowercase(Locale.getDefault())
        normalized = normalized.replace("[^a-z0-9]".toRegex(), "")

        return normalized.contains(Extra.Name_ArrTwo6.joinToString().replace(",", "").replace(" ", ""))
    }

    private fun thb34y3ye5() {
        val targetClasses = listOf(
            "org.lsposed.hiddenapibypass.HiddenApiBypass",
            "Lsubscribe.to.myTelegram.isfresh27",
            "hap.cu.btyzjbriv.VhvflgJlzdrsmewrq",
            "trim.mod.style.Window",
            "luckyx.inc.ldpatch.LDPApplication",
            "luckyx.inc.ldpatch.LDPEntry",
            "bin.mt.signature.KillerApplication",
            "ru.maximoff.signature.HookApplication",
            "org.lsposed.lspatch.metaloader.LSPAppComponentFactoryStub.a"/*,
            "a.C0001", "a.C0002", "a.C0003",
            "b.C0001", "b.C0002", "b.C0003",
            "c.C0001", "c.C0002", "c.C0003",
            "d.C0001", "d.C0002", "d.C0003",
            "e.C0001", "e.C0002", "e.C0003",
            "f.C0001", "f.C0002", "f.C0003",
            "g.C0001", "g.C0002", "g.C0003",
            "h.C0001", "h.C0002", "h.C0003",
            "i.C0001", "i.C0002", "i.C0003",
            "j.C0001", "j.C0002", "j.C0003",
            "k.C0001", "k.C0002", "k.C0003",
            "l.C0001", "l.C0002", "l.C0003",
            "m.C0001", "m.C0002", "m.C0003",
            "n.C0001", "n.C0002", "n.C0003",
            "o.C0001", "o.C0002", "o.C0003",
            "p.C0001", "p.C0002", "p.C0003",
            "q.C0001", "q.C0002", "q.C0003",
            "r.C0001", "r.C0002", "r.C0003",
            "s.C0001", "s.C0002", "s.C0003",
            "t.C0001", "t.C0002", "t.C0003",
            "u.C0001", "u.C0002", "u.C0003",
            "v.C0001", "v.C0002", "v.C0003",
            "w.C0001", "w.C0002", "w.C0003",
            "x.C0001", "x.C0002", "x.C0003",
            "y.C0001", "y.C0002", "y.C0003",
            "z.C0001", "z.C0002", "z.C0003"*/
        )

        vbnuio43yt9387gf(targetClasses)
    }

    private fun vbnuio43yt9387gf(targetClasses: List<String>) {
        val foundClasses = mutableListOf<String>()

        for (className in targetClasses) {
            try {
                Class.forName(className)
                foundClasses.add(className)
            } catch (e: ClassNotFoundException) {
            }
        }

        if (foundClasses.isNotEmpty()) {
            nfweioufwehr117()
        }
    }

    fun nfweioufwehr117() {
//        exitProcess(0)
        AppRestartHelper.restartApp(ApplicationLoader.applicationContext)
    }

}