/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.chats.helpers

import android.os.Build
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.telegram.messenger.FileLog
import org.telegram.messenger.FingerprintController
import org.telegram.messenger.MessageObject
import org.telegram.messenger.MessagesController
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.TLRPC.MessageEntity
import org.telegram.tgnet.TLRPC.TL_messageEntitySpoiler
import uz.unnarsx.cherrygram.core.CGBiometricPrompt
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramPrivacyConfig

object ChatsPasswordHelper {

    const val PASSCODE_ARRAY = "locked_chats_list"
    private val currentAccount = UserConfig.selectedAccount
    private val userConfig: UserConfig = UserConfig.getInstance(currentAccount)

    fun saveArrayList(list: ArrayList<String>, key: String) {
        MessagesController.getMainSettings(currentAccount)
            .edit()
            .putString(key, Gson().toJson(list))
            .apply()
    }

    fun getArrayList(key: String): ArrayList<String> {
        val json = MessagesController.getMainSettings(currentAccount).getString(key, null)

        return Gson().fromJson(json, object : TypeToken<ArrayList<String>>() {}.type) ?: arrayListOf(userConfig.clientUserId.toString())
    }

    fun isChatLocked(chatId: Long): Boolean {
        val lockedChats = getArrayList(PASSCODE_ARRAY)

        return chatId != 0L && (lockedChats.contains(chatId.toString()) || lockedChats.contains("-$chatId"))
    }

    fun isChatLocked(messageObject: MessageObject): Boolean {
        return CherrygramPrivacyConfig.askBiometricsToOpenChat
                && messageObject.messageOwner.message != null
                && !messageObject.isStoryReactionPush && !messageObject.isStoryPush
                && !messageObject.isStoryMentionPush && !messageObject.isStoryPushHidden
                && isChatLocked(messageObject.chatId)
    }

    fun checkLockedChatsEntities(messageObject: MessageObject, original: java.util.ArrayList<MessageEntity>?): java.util.ArrayList<MessageEntity>? {
        return if (isChatLocked(messageObject)) {
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
        return getArrayList(PASSCODE_ARRAY).size
    }

    fun shouldRequireBiometricsToOpenChats(): Boolean {
        if (CherrygramCoreConfig.isDevBuild()) FileLog.d("запросил shouldRequireBiometricsToOpenChats")
        return CherrygramPrivacyConfig.askBiometricsToOpenChat &&
                /*&& getArrayList(Passcode_Array).isNotEmpty()*/
                Build.VERSION.SDK_INT >= 23 &&
                CGBiometricPrompt.hasBiometricEnrolled() &&
                FingerprintController.isKeyReady() &&
                !FingerprintController.checkDeviceFingerprintsChanged()
    }

    fun shouldRequireBiometricsToOpenArchive(): Boolean {
        if (CherrygramCoreConfig.isDevBuild()) FileLog.d("запросил shouldRequireBiometricsToOpenArchive")
        return CherrygramPrivacyConfig.askBiometricsToOpenArchive && Build.VERSION.SDK_INT >= 23
    }

    fun askPasscodeBeforeDelete(): Boolean {
        if (CherrygramCoreConfig.isDevBuild()) FileLog.d("запросил askPasscodeBeforeDelete")
        return CherrygramPrivacyConfig.askPasscodeBeforeDelete &&
                Build.VERSION.SDK_INT >= 23 &&
                CGBiometricPrompt.hasBiometricEnrolled()
                && FingerprintController.isKeyReady()
                && !FingerprintController.checkDeviceFingerprintsChanged()
    }

}