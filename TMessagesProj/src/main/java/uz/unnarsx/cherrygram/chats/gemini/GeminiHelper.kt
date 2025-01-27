/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.chats.gemini

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.DispatchQueue
import org.telegram.messenger.FileLog
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.Theme.ResourcesProvider
import org.telegram.ui.Components.Bulletin
import org.telegram.ui.Components.BulletinFactory
import org.telegram.ui.Components.ChatActivityEnterView
import org.telegram.ui.LaunchActivity
import uz.unnarsx.cherrygram.core.configs.CherrygramExperimentalConfig
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper
import uz.unnarsx.cherrygram.helpers.NetworkHelper
import uz.unnarsx.cherrygram.helpers.ui.PopupHelper
import uz.unnarsx.cherrygram.preferences.ExperimentalPreferencesEntry
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

@Deprecated("Not used anymore, use GeminiSDKImplementation.Java")
object GeminiHelper: CoroutineScope by MainScope() {

    /*interface OnResponseNotReceived {
        fun run(geminiErrorAnswer: String)
    }

    interface OnResponseReceived {
        fun run(geminiAnswer: String)
    }

    private val geminiQueue = DispatchQueue("geminiQueue")
    private fun makeGeminiRequest(onResponseNotReceived: OnResponseNotReceived?, onResponseReceived: OnResponseReceived?, textToInput: String) {
        geminiQueue.postRunnable(
            {
                val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/"
                val modelUrl = CGResourcesHelper.getGeminiModel()
                val apiKey = CherrygramExperimentalConfig.geminiApiKey
                val uriGit = "$baseUrl$modelUrl:generateContent?key=$apiKey"
                val urlGit = URL(uriGit)

                val connection = urlGit.openConnection() as HttpURLConnection

                try {
                    connection.requestMethod = "POST"
                    connection.doOutput = true
                    connection.doInput = true
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.setRequestProperty("User-Agent", NetworkHelper.formatUserAgent())
                    connection.connectTimeout = 15000
                    connection.readTimeout = 15000

                    val requestText = textToInput
                    val requestBody = "{\"contents\": [{\"parts\":[{\"text\": \"$requestText\"}]}]}"

                    connection.outputStream.use { outputStream: OutputStream ->
                        outputStream.write(requestBody.toByteArray(Charsets.UTF_8))
                        outputStream.flush()
                    }

                    val responseCode = connection.responseCode

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val jsonString = connection.inputStream.bufferedReader().readText()

                        val geminiAnswer = parseJsonWithArray(jsonString)
                        if (onResponseReceived != null) AndroidUtilities.runOnUIThread { onResponseReceived.run(geminiAnswer) }
                    } else {

                        val errorJsonString = connection.errorStream.bufferedReader().readText()
                        val errorResponseCode: Int? = getErrorText(errorJsonString)?.error?.code
                        val errorResponseMessage: String? = getErrorText(errorJsonString)?.error?.message

                        if (onResponseNotReceived != null) {
                            AndroidUtilities.runOnUIThread {
                                if (errorResponseCode != null && errorResponseMessage != null) {
                                    onResponseNotReceived.run("Error $errorResponseCode: $errorResponseMessage")
                                } else {
                                    onResponseNotReceived.run(getString(R.string.VoipFailed))
                                }
                            }
                        }

                    }

                } catch (e: Exception) {

                    val errorJsonString = connection.errorStream.bufferedReader().readText()
                    val errorResponseCode: Int? = getErrorText(errorJsonString)?.error?.code
                    val errorResponseMessage: String? = getErrorText(errorJsonString)?.error?.message

                    if (onResponseNotReceived != null) {
                        AndroidUtilities.runOnUIThread {
                            if (errorResponseCode != null && errorResponseMessage != null) {
                                onResponseNotReceived.run("Error $errorResponseCode: $errorResponseMessage")
                            } else {
                                onResponseNotReceived.run(getString(R.string.VoipFailed))
                            }
                        }
                    }

                } finally {
                    connection.disconnect()
                }
            }, 0
        )
    }

    @Deprecated("Not used anymore, use GeminiSDKImplementation.Java")
    fun showLoading(activity: Activity, messageInput: ChatActivityEnterView, textToInput: String) {
        val progressDialog = AlertDialog(activity, AlertDialog.ALERT_TYPE_SPINNER)

        AndroidUtilities.runOnUIThread {
            try {
                progressDialog.show()
            } catch (e: Exception) {
                FileLog.e(e)
            }
        }

        makeGeminiRequest(
            object : OnResponseNotReceived {
                override fun run(geminiErrorAnswer: String) {
                    AndroidUtilities.runOnUIThread {
                        try {
                            if (progressDialog.isShowing) progressDialog.dismiss()
                        } catch (e: Exception) {
                            FileLog.e(e)
                        }
                    }

                    BulletinFactory.global()
                        .createErrorBulletin(geminiErrorAnswer)
                        .setDuration(Bulletin.DURATION_LONG)
                        .show()
                }

            },
            object : OnResponseReceived {
                override fun run(geminiAnswer: String) {
                    AndroidUtilities.runOnUIThread {
                        try {
                            if (progressDialog.isShowing) progressDialog.dismiss()
                        } catch (e: Exception) {
                            FileLog.e(e)
                        }
                    }

                    BulletinFactory.global()
                        .createSuccessBulletin(getString(R.string.OK))
                        .setDuration(Bulletin.DURATION_LONG)
                        .show()

                    messageInput.messageEditText?.setText(geminiAnswer.substring(0, geminiAnswer.length - 1))
                }
            },
            textToInput
        )
    }

    private fun parseJsonWithArray(jsonString: String): String {
        val gson = Gson()
        val response = gson.fromJson(jsonString, GeminiDTO.Response::class.java)

        FileLog.d("текст из json: " + response.candidates[0].content.parts[0].text)

        return response.candidates[0].content.parts[0].text
    }

    private fun getErrorText(jsonString: String): GeminiErrorDTO.ErrorResponse? {
        val gson = Gson()
        val response = gson.fromJson(jsonString, GeminiErrorDTO.ErrorResponse::class.java)
        return response
    }

    @JvmStatic
    fun showGeminiModelSelector(context: Context, resourcesProvider: ResourcesProvider?, listAdapter: ExperimentalPreferencesEntry.ListAdapter?, geminiModelRow: Int, view: View?) {
        val configStringKeys = ArrayList<String>()
        val configValues = ArrayList<Int>()

        configStringKeys.add("Gemini 1.5 Flash")
        configValues.add(CherrygramExperimentalConfig.GEMINI_MODEL_1_5_FLASH)

        configStringKeys.add("Gemini 1.5 Flash-8B")
        configValues.add(CherrygramExperimentalConfig.GEMINI_MODEL_1_5_FLASH_8B)

        configStringKeys.add("Gemini 1.5 Pro")
        configValues.add(CherrygramExperimentalConfig.GEMINI_MODEL_1_5_PRO)

        configStringKeys.add("Gemini 2.0 Flash")
        configValues.add(CherrygramExperimentalConfig.GEMINI_MODEL_2_0_EXP)

        configStringKeys.add("Gemini 2.0 Advanced")
        configValues.add(CherrygramExperimentalConfig.GEMINI_MODEL_2_0_ADVANCED)

        configStringKeys.add("Gemini 2.0 Flash Thinking")
        configValues.add(CherrygramExperimentalConfig.GEMINI_MODEL_2_0_FLASH_THINKING)

        PopupHelper.show(
            configStringKeys,
            getString(R.string.CP_GeminiAI_Model),
            configValues.indexOf(CherrygramExperimentalConfig.geminiModelName),
            context,
            { i: Int ->
                CherrygramExperimentalConfig.geminiModelName = configValues[i]

                listAdapter?.notifyItemChanged(geminiModelRow)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && view != null) {
                    LaunchActivity.makeRipple(view.right.toFloat(), view.y, 5f)
                }
            },
            resourcesProvider
        )
    }*/

}