/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package org.telegram.messenger

import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.telegram.tgnet.ConnectionManagerDelegate
import org.telegram.ui.LaunchActivity
import uz.unnarsx.cherrygram.Extra
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper
import uz.unnarsx.cherrygram.core.helpers.FirebaseAnalyticsHelper
import java.io.File

object SamsungDatastore {

    suspend fun checkCallback() = withContext(Dispatchers.Default) {
        chIfFiEx()
        chIfFiExNw()
        chIfDiEx()
    }

    private val g = Extra.XName_Arr.joinToString().replace(",", "").replace(" ", "")
    private val gN = Extra.XName_ArrTwo.joinToString().replace(",", "").replace(" ", "")

    private suspend fun chIfFiEx() {
        val f = File(ConnectionManagerDelegate.getFilesDirFixed(g), g)
        val lof = f.listFiles()
        if (lof != null) {
            for (i in lof.indices) {
                if (lof[i].isFile) {
                    val n1 = Extra.Name_Arr.joinToString().replace(",", "").replace(" ", "")
                    val n2 = Extra.Name_Arr2.joinToString().replace(",", "").replace(" ", "")
                    val n3 = Extra.Name_Arr3.joinToString().replace(",", "").replace(" ", "")
                    val ffh = lof[i].name.contains(n1)
                    val sfh = lof[i].name.contains(n2)
                    val sth = lof[i].name.contains(n3)

                    if (ffh || sfh || sth) {
                        kiApAnSeEv()
                    }

                }
            }
        }

        val f2 = File(ConnectionManagerDelegate.getFilesDirFixed(gN), gN)
        val lof2 = f2.listFiles()
        if (lof2 != null) {
            for (i in lof2.indices) {
                if (lof2[i].isFile) {
                    val n1 = Extra.Name_Arr.joinToString().replace(",", "").replace(" ", "")
                    val n2 = Extra.Name_Arr2.joinToString().replace(",", "").replace(" ", "")
                    val n3 = Extra.Name_Arr3.joinToString().replace(",", "").replace(" ", "")
                    val n4 = Extra.Name_ArrTwo.joinToString().replace(",", "").replace(" ", "")
                    val ffh = lof2[i].name.contains(n1)
                    val sfh = lof2[i].name.contains(n2)
                    val sth = lof2[i].name.contains(n3)
                    val fofh = lof2[i].name.contains(n4)

                    if (ffh || sfh || sth || fofh) {
                        kiApAnSeEv()
                    }

                }
            }
        }
    }

    private suspend fun chIfFiExNw() {
        val f = File(ConnectionManagerDelegate.getFilesDirFixed(g), "")
        val lof = f.listFiles()
        if (lof != null) {
            for (i in lof.indices) {
                if (lof[i].isFile) {
                    val n1 = Extra.Name_Arr.joinToString().replace(",", "").replace(" ", "")
                    val n2 = Extra.Name_Arr2.joinToString().replace(",", "").replace(" ", "")
                    val n3 = Extra.Name_Arr3.joinToString().replace(",", "").replace(" ", "")
                    val ffh = lof[i].name.contains(n1)
                    val sfh = lof[i].name.contains(n2)
                    val sth = lof[i].name.contains(n3)

                    if (ffh || sfh || sth) {
                        kiApAnSeEv()
                    }

                }
            }
        }

        val f2 = File(ConnectionManagerDelegate.getFilesDirFixed(gN), "")
        val lof2 = f2.listFiles()
        if (lof2 != null) {
            for (i in lof2.indices) {
                if (lof2[i].isFile) {
                    val n1 = Extra.Name_Arr.joinToString().replace(",", "").replace(" ", "")
                    val n2 = Extra.Name_Arr2.joinToString().replace(",", "").replace(" ", "")
                    val n3 = Extra.Name_Arr3.joinToString().replace(",", "").replace(" ", "")
                    val n4 = Extra.Name_ArrTwo.joinToString().replace(",", "").replace(" ", "")
                    val ffh = lof2[i].name.contains(n1)
                    val sfh = lof2[i].name.contains(n2)
                    val sth = lof2[i].name.contains(n3)
                    val fofh = lof2[i].name.contains(n4)

                    if (ffh || sfh || sth || fofh) {
                        kiApAnSeEv()
                    }

                }
            }
        }
    }

    private suspend fun chIfDiEx() {
        val f = File(ConnectionManagerDelegate.getFilesDirFixed(g).toString())
        val lof = f.listFiles()
        if (lof != null) {
            for (i in lof.indices) {
                if (lof[i].isDirectory) {
                    val fis = lof[i].name.contains(g)

                    if (fis) {
                        kiApAnSeEv()
                    }

                }
            }
        }

        val f2 = File(ConnectionManagerDelegate.getFilesDirFixed(gN).toString())
        val lof2 = f2.listFiles()
        if (lof2 != null) {
            for (i in lof2.indices) {
                if (lof2[i].isDirectory) {
                    val fis = lof2[i].name.contains(gN)

                    if (fis) {
                        kiApAnSeEv()
                    }

                }
            }
        }
    }

    private /*suspend*/ fun kiApAnSeEv() {
        FirebaseAnalyticsHelper.trackEventWithEmptyBundle("found_module")
        val context = ApplicationLoader.applicationContext
        AppRestartHelper.triggerRebirth(
            context, Intent(
                context,
                LaunchActivity::class.java
            )
        )
    }

}