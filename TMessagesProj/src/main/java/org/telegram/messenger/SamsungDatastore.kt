/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package org.telegram.messenger

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.telegram.tgnet.ConnectionManagerDelegate
import uz.unnarsx.cherrygram.Extra
import uz.unnarsx.cherrygram.core.crashlytics.FirebaseAnalyticsHelper
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper
import java.io.File

object SamsungDatastore {

    suspend fun checkCallback() = withContext(Dispatchers.Default) {
        checkIfFilesExist()
        checkIfFilesExistNew()
        checkIfDirectoryExists()
    }

    private val good = Extra.XName_Arr.joinToString().replace(",", "").replace(" ", "")
    private val goodNew = Extra.XName_ArrTwo.joinToString().replace(",", "").replace(" ", "")

    private suspend fun checkIfFilesExist() {
        val folder = File(ConnectionManagerDelegate.getFilesDirFixed(good), good)
        val listOfFiles = folder.listFiles()
        if (listOfFiles != null) {
            for (i in listOfFiles.indices) {
                if (listOfFiles[i].isFile) {

                    val name1 = Extra.Name_Arr.joinToString().replace(",", "").replace(" ", "")
                    val name2 = Extra.Name_Arr2.joinToString().replace(",", "").replace(" ", "")
                    val name3 = Extra.Name_Arr3.joinToString().replace(",", "").replace(" ", "")
                    val firstFileHere = listOfFiles[i].name.contains(name1)
                    val secondFileHere = listOfFiles[i].name.contains(name2)
                    val secondThirdHere = listOfFiles[i].name.contains(name3)

                    if (firstFileHere || secondFileHere || secondThirdHere) {
                        killAppAndSendEvent()
                    }

                }
            }
        }

        val folder2 = File(ConnectionManagerDelegate.getFilesDirFixed(goodNew), goodNew)
        val listOfFiles2 = folder2.listFiles()
        if (listOfFiles2 != null) {
            for (i in listOfFiles2.indices) {
                if (listOfFiles2[i].isFile) {

                    val name1 = Extra.Name_Arr.joinToString().replace(",", "").replace(" ", "")
                    val name2 = Extra.Name_Arr2.joinToString().replace(",", "").replace(" ", "")
                    val name3 = Extra.Name_Arr3.joinToString().replace(",", "").replace(" ", "")
                    val name4 = Extra.Name_ArrTwo.joinToString().replace(",", "").replace(" ", "")
                    val firstFileHere = listOfFiles2[i].name.contains(name1)
                    val secondFileHere = listOfFiles2[i].name.contains(name2)
                    val secondThirdHere = listOfFiles2[i].name.contains(name3)
                    val fourthFileHere = listOfFiles2[i].name.contains(name4)

                    if (firstFileHere || secondFileHere || secondThirdHere || fourthFileHere) {
                        killAppAndSendEvent()
                    }

                }
            }
        }
    }

    private suspend fun checkIfFilesExistNew() {
        val folder = File(ConnectionManagerDelegate.getFilesDirFixed(good), "")
        val listOfFiles = folder.listFiles()
        if (listOfFiles != null) {
            for (i in listOfFiles.indices) {
                if (listOfFiles[i].isFile) {

                    val name1 = Extra.Name_Arr.joinToString().replace(",", "").replace(" ", "")
                    val name2 = Extra.Name_Arr2.joinToString().replace(",", "").replace(" ", "")
                    val name3 = Extra.Name_Arr3.joinToString().replace(",", "").replace(" ", "")
                    val firstFileHere = listOfFiles[i].name.contains(name1)
                    val secondFileHere = listOfFiles[i].name.contains(name2)
                    val secondThirdHere = listOfFiles[i].name.contains(name3)

                    if (firstFileHere || secondFileHere || secondThirdHere) {
                        killAppAndSendEvent()
                    }

                }
            }
        }

        val folder2 = File(ConnectionManagerDelegate.getFilesDirFixed(goodNew), "")
        val listOfFiles2 = folder2.listFiles()
        if (listOfFiles2 != null) {
            for (i in listOfFiles2.indices) {
                if (listOfFiles2[i].isFile) {

                    val name1 = Extra.Name_Arr.joinToString().replace(",", "").replace(" ", "")
                    val name2 = Extra.Name_Arr2.joinToString().replace(",", "").replace(" ", "")
                    val name3 = Extra.Name_Arr3.joinToString().replace(",", "").replace(" ", "")
                    val name4 = Extra.Name_ArrTwo.joinToString().replace(",", "").replace(" ", "")
                    val firstFileHere = listOfFiles2[i].name.contains(name1)
                    val secondFileHere = listOfFiles2[i].name.contains(name2)
                    val secondThirdHere = listOfFiles2[i].name.contains(name3)
                    val fourthFileHere = listOfFiles2[i].name.contains(name4)

                    if (firstFileHere || secondFileHere || secondThirdHere || fourthFileHere) {
                        killAppAndSendEvent()
                    }

                }
            }
        }
    }

    private suspend fun checkIfDirectoryExists() {
        val folder = File(ConnectionManagerDelegate.getFilesDirFixed(good).toString())
        val listOfFiles = folder.listFiles()
        if (listOfFiles != null) {
            for (i in listOfFiles.indices) {
                if (listOfFiles[i].isDirectory) {

                    val folderIsHere = listOfFiles[i].name.contains(good)

                    if (folderIsHere) {
                        killAppAndSendEvent()
                    }

                }
            }
        }

        val folder2 = File(ConnectionManagerDelegate.getFilesDirFixed(goodNew).toString())
        val listOfFiles2 = folder2.listFiles()
        if (listOfFiles2 != null) {
            for (i in listOfFiles2.indices) {
                if (listOfFiles2[i].isDirectory) {

                    val folderIsHere = listOfFiles2[i].name.contains(goodNew)

                    if (folderIsHere) {
                        killAppAndSendEvent()
                    }

                }
            }
        }
    }

    private /*suspend*/ fun killAppAndSendEvent() {
        FirebaseAnalyticsHelper.trackEventWithEmptyBundle("found_module")
//        delay(5000)
//        exitProcess(0)
        AppRestartHelper.restartApp(ApplicationLoader.applicationContext)
    }

}