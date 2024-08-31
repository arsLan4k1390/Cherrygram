package uz.unnarsx.cherrygram.chats.helpers

import android.app.Activity
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.FingerprintController
import uz.unnarsx.cherrygram.core.CGBiometricPrompt
import uz.unnarsx.cherrygram.core.configs.CherrygramPrivacyConfig
import java.lang.reflect.Type

object ChatsPasswordHelper {

    const val Passcode_Array = "passcode_array12"

    fun saveArrayList(list: ArrayList<String?>?, key: String?) {
        val prefs = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = prefs.edit()
        val gson = Gson()
        val json: String = gson.toJson(list)
        editor.putString(key, json)
        editor.apply()
    }

    fun getArrayList(key: String?): ArrayList<String?>? {
        val prefs = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val gson = Gson()
        val json: String? = prefs.getString(key, null)
        val type: Type = object : TypeToken<ArrayList<String?>?>() {}.type
        return gson.fromJson(json, type)
    }

    var askPasscodeForChats =
        CherrygramPrivacyConfig.askForPasscodeBeforeOpenChat && getArrayList(Passcode_Array) != null && !getArrayList(Passcode_Array)!!.isEmpty()
                && CGBiometricPrompt.hasBiometricEnrolled() && FingerprintController.isKeyReady() && !FingerprintController.checkDeviceFingerprintsChanged()
}