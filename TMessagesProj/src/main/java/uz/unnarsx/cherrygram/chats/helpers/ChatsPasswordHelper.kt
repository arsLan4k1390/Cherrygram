/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

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
        return if (shouldRequireBiometricsToOpenChats && messageObject.messageOwner.message != null
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

    fun isChatLocked(messageObject: MessageObject): Boolean {
        return shouldRequireBiometricsToOpenChats && messageObject.messageOwner.message != null
            && messageObject.chatId != 0L && (
                    getArrayList(Passcode_Array)!!.contains(messageObject.chatId.toString())
                            || getArrayList(Passcode_Array)!!.contains("-" + messageObject.chatId.toString())
                    )
            && !messageObject.isStoryReactionPush && !messageObject.isStoryPush && !messageObject.isStoryMentionPush && !messageObject.isStoryPushHidden
    }

    fun checkLockedChatsEntities(messageObject: MessageObject): java.util.ArrayList<MessageEntity>? {
        return checkLockedChatsEntities(messageObject, messageObject.messageOwner.entities)
    }

    private var spoilerChars: CharArray = charArrayOf(
        '⠌', '⡢', '⢑', '⠨', '⠥', '⠮', '⡑'
    )

    fun replaceStringToSpoilers(originalText: String?, force: Boolean): String? {
        if (originalText == null) {
            return null
        }
        return if (CherrygramPrivacyConfig.askBiometricsToOpenArchive || force) {
            val stringBuilder = StringBuilder(originalText)
            for (i in originalText.indices) {
                stringBuilder.setCharAt(i, spoilerChars[i % spoilerChars.size])
            }
            stringBuilder.toString()
        } else {
            originalText
        }
    }

    fun getLockedChatsCount(): Int {
        return getArrayList(Passcode_Array)!!.size
    }

    var shouldRequireBiometricsToOpenChats =
        CherrygramPrivacyConfig.askBiometricsToOpenChat && getArrayList(Passcode_Array) != null && !getArrayList(Passcode_Array)!!.isEmpty()
                && CGBiometricPrompt.hasBiometricEnrolled() && FingerprintController.isKeyReady() && !FingerprintController.checkDeviceFingerprintsChanged()

    var shouldRequireBiometricsToOpenArchive =
        CherrygramPrivacyConfig.askBiometricsToOpenArchive
                && CGBiometricPrompt.hasBiometricEnrolled() && FingerprintController.isKeyReady() && !FingerprintController.checkDeviceFingerprintsChanged()

}