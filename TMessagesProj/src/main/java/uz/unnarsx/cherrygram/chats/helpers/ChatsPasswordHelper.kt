/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.chats.helpers

import android.os.Build
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.telegram.messenger.BaseController
import org.telegram.messenger.DialogObject
import org.telegram.messenger.FileLog
import org.telegram.messenger.FingerprintController
import org.telegram.messenger.MessageObject
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.TLRPC.MessageEntity
import org.telegram.tgnet.TLRPC.TL_messageEntitySpoiler
import uz.unnarsx.cherrygram.core.CGBiometricPrompt
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramPrivacyConfig

class ChatsPasswordHelper private constructor(num: Int) : BaseController(num) {

    companion object {
        private val instances = arrayOfNulls<ChatsPasswordHelper>(UserConfig.MAX_ACCOUNT_COUNT)

        @JvmStatic
        fun getInstance(num: Int): ChatsPasswordHelper {
            return instances[num] ?: synchronized(ChatsPasswordHelper::class.java) {
                instances[num] ?: ChatsPasswordHelper(num).also { instances[num] = it }
            }
        }
    }

    private var lockedChatsCache: HashSet<String>? = null

    fun getPasscodeArray(): String = "locked_chats_list"

    fun saveArrayList(list: ArrayList<String>, key: String) {
        if (CherrygramCoreConfig.isDevBuild()) FileLog.d("запросил saveArrayList")

        if (key == getPasscodeArray()) {
            lockedChatsCache = HashSet(list)
        }

        messagesController.mainSettings
            .edit {
                putString(key, Gson().toJson(list))
            }
    }

    fun getArrayList(key: String): ArrayList<String> {
        if (CherrygramCoreConfig.isDevBuild()) FileLog.d("запросил кешированный getArrayList для паролей")

        if (key == getPasscodeArray() && lockedChatsCache != null) {
            return ArrayList(lockedChatsCache!!)
        }

        if (CherrygramCoreConfig.isDevBuild()) FileLog.d("запросил getArrayList для паролей")

        val json = messagesController.mainSettings.getString(key, null)
        if (CherrygramCoreConfig.isDevBuild()) FileLog.d("getArrayList: $json")
        val list: ArrayList<String> = Gson().fromJson(json, object : TypeToken<ArrayList<String>>() {}.type)
            ?: arrayListOf(userConfig.clientUserId.toString())

        if (key == getPasscodeArray()) {
            lockedChatsCache = HashSet(list)
        }

        return list
    }

    fun isChatLocked(chatId: Long): Boolean {
        if (CherrygramCoreConfig.isDevBuild()) FileLog.d("запросил isChatLocked")
        if (chatId == 0L || !CherrygramPrivacyConfig.askBiometricsToOpenChat) return false

        if (lockedChatsCache == null) {
            getArrayList(getPasscodeArray())
        }

        val idStr = chatId.toString()
        return lockedChatsCache?.contains(idStr) == true || lockedChatsCache?.contains("-$idStr") == true
    }

    fun isChatLocked(messageObject: MessageObject): Boolean {
        if (CherrygramCoreConfig.isDevBuild()) FileLog.d("запросил isChatLocked2")
        return CherrygramPrivacyConfig.askBiometricsToOpenChat && messageObject.messageOwner.message != null
                && !messageObject.isStoryReactionPush && !messageObject.isStoryPush
                && !messageObject.isStoryMentionPush && !messageObject.isStoryPushHidden
                && isChatLocked(messageObject.chatId)
    }

    fun isEncryptedChat(chatId: Long): Boolean {
        if (CherrygramCoreConfig.isDevBuild()) FileLog.d("запросил isEncryptedChat")
        if (CherrygramPrivacyConfig.askBiometricsToOpenEncrypted) {
            val encID = DialogObject.getEncryptedChatId(chatId)
            val encryptedChat = messagesController.getEncryptedChat(encID)
            return encryptedChat != null
        } else {
            return false
        }
    }

    fun isEncryptedChat(messageObject: MessageObject): Boolean {
        if (CherrygramCoreConfig.isDevBuild()) FileLog.d("запросил isEncryptedChat2")
        if (CherrygramPrivacyConfig.askBiometricsToOpenEncrypted) {
            val encID = DialogObject.getEncryptedChatId(messageObject.dialogId)
            val encryptedChat = messagesController.getEncryptedChat(encID)
            return messageObject.messageOwner.message != null
                    && !messageObject.isStoryReactionPush && !messageObject.isStoryPush
                    && !messageObject.isStoryMentionPush && !messageObject.isStoryPushHidden
                    && encryptedChat != null
        } else {
            return false
        }
    }

    fun checkLockedChatsEntities(messageObject: MessageObject): ArrayList<MessageEntity>? {
        if (CherrygramCoreConfig.isDevBuild()) FileLog.d("запросил checkLockedChatsEntities")
        return checkLockedChatsEntities(messageObject, messageObject.messageOwner.entities)
    }

    fun checkLockedChatsEntities(messageObject: MessageObject, original: ArrayList<MessageEntity>?): ArrayList<MessageEntity>? {
        if (CherrygramCoreConfig.isDevBuild()) FileLog.d("запросил checkLockedChatsEntities2")
        return if (isChatLocked(messageObject) || isEncryptedChat(messageObject)) {
            val entities = original?.let { ArrayList(it) }
            val spoiler = TL_messageEntitySpoiler()
            spoiler.offset = 0
            spoiler.length = messageObject.messageOwner.message.length
            entities?.add(spoiler)
            entities
        } else {
            original
        }
    }

    private var spoilerChars: CharArray = charArrayOf(
        '⠌', '⡢', '⢑', '⠨', '⠥', '⠮', '⡑'
    )

    fun replaceStringToSpoilers(originalText: String?, force: Boolean): String? {
        if (CherrygramCoreConfig.isDevBuild()) FileLog.d("запросил replaceStringToSpoilers")
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
        if (CherrygramCoreConfig.isDevBuild()) FileLog.d("запросил getLockedChatsCount")
        return getArrayList(getPasscodeArray()).size
    }

    fun shouldRequireBiometrics(userID: Long, chatID: Long, encID: Long): Boolean {
        if (CherrygramCoreConfig.isDevBuild()) FileLog.d("запросил shouldRequireBiometrics")
        val lockedChat = (userID != 0L && isChatLocked(userID)) || (chatID != 0L && isChatLocked(chatID))

        val encryptedChat = encID != 0L && isEncryptedChat(encID)

        return (lockedChat && shouldRequireBiometricsToOpenChats()) || (encryptedChat && shouldRequireBiometricsToOpenEncryptedChats())
    }

    fun shouldRequireBiometricsToOpenChats(): Boolean {
        if (CherrygramCoreConfig.isDevBuild()) FileLog.d("запросил shouldRequireBiometricsToOpenChats")
        return CherrygramPrivacyConfig.askBiometricsToOpenChat && checkBiometricAvailable()
    }

    fun shouldRequireBiometricsToOpenEncryptedChats(): Boolean {
        if (CherrygramCoreConfig.isDevBuild()) FileLog.d("запросил shouldRequireBiometricsToOpenEncryptedChats")
        return CherrygramPrivacyConfig.askBiometricsToOpenEncrypted && checkBiometricAvailable()
    }

    fun askPasscodeBeforeDelete(): Boolean {
        if (CherrygramCoreConfig.isDevBuild()) FileLog.d("запросил askPasscodeBeforeDelete")
        return CherrygramPrivacyConfig.askPasscodeBeforeDelete && checkBiometricAvailable()
    }

    fun checkBiometricAvailable(): Boolean {
        if (CherrygramCoreConfig.isDevBuild()) FileLog.d("запросил checkBiometricAvailable")

        val hasBiometrics = CGBiometricPrompt.hasBiometricEnrolled()
        if (!hasBiometrics) return false

        val hasFingerprints = CGBiometricPrompt.hasEnrolledFingerprints()
        return if (hasFingerprints) {
            FingerprintController.isKeyReady() && !FingerprintController.checkDeviceFingerprintsChanged()
        } else {
            true
        }
    }

}