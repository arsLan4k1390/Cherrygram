package uz.unnarsx.cherrygram.chats.helpers

import android.app.Activity
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.FingerprintController
import org.telegram.messenger.MessageObject
import org.telegram.tgnet.TLRPC.MessageEntity
import org.telegram.tgnet.TLRPC.TL_messageEntitySpoiler
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

    fun checkLockedChatsEntities(messageObject: MessageObject, original: java.util.ArrayList<MessageEntity>?): java.util.ArrayList<MessageEntity>? {
        return if (askPasscodeForChats && messageObject.messageOwner.message != null
            && messageObject.chatId != 0L && (
                    getArrayList(Passcode_Array)!!.contains(messageObject.chatId.toString())
                    || getArrayList(Passcode_Array)!!.contains("-" + messageObject.chatId.toString())
            )
            && !messageObject.isStoryReactionPush && !messageObject.isStoryPush && !messageObject.isStoryMentionPush && !messageObject.isStoryPushHidden
        ) {
            val entities = original?.let { java.util.ArrayList(it) }
            val spoiler = TL_messageEntitySpoiler()
            spoiler.offset = 0
            spoiler.length = messageObject.messageOwner.message.length
            entities?.add(spoiler)
            entities
        } else {
            original
        }
    }

    fun checkLockedChatsEntities(messageObject: MessageObject): java.util.ArrayList<MessageEntity>? {
        return checkLockedChatsEntities(messageObject, messageObject.messageOwner.entities)
    }

    var askPasscodeForChats =
        CherrygramPrivacyConfig.askForPasscodeBeforeOpenChat && getArrayList(Passcode_Array) != null && !getArrayList(Passcode_Array)!!.isEmpty()
                && CGBiometricPrompt.hasBiometricEnrolled() && FingerprintController.isKeyReady() && !FingerprintController.checkDeviceFingerprintsChanged()
}