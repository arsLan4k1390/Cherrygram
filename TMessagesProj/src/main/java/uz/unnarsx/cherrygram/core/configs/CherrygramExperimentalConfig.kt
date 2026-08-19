/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.configs

import android.app.Activity
import android.content.SharedPreferences
import org.telegram.messenger.ApplicationLoader
import uz.unnarsx.cherrygram.preferences.boolean
import uz.unnarsx.cherrygram.preferences.float

object CherrygramExperimentalConfig {

    private val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)

    var recordOOMasNonFatal by sharedPreferences.boolean("CG_Record_OOM_AS_NF", true)
    var use_CG_OOMHandler by sharedPreferences.boolean("CG_Use_CG_OOMHandler", true)
    var oomHandlerPopup by sharedPreferences.boolean("CG_OOMHandlerPopup", true)
    var oomHandlerPopupThreshold by sharedPreferences.float("CG_OOMHandlerPopupThreshold", 95f)

}