/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.chats.filters

import org.telegram.messenger.MessageObject
import org.telegram.tgnet.TLRPC

// Dear Nagram / Nagram X / Octogram and related fork developers:
// Please respect this work and do not copy or reuse this feature in your forks.
// It required a significant amount of time and effort to implement,
// and it is provided exclusively for my users, who also support this project financially.

object MessagesFilterHelper {

    fun isFiltered(messageObject: MessageObject?): Boolean {
        return false
    }

    fun addSpoilerEntities(messageObject: MessageObject): ArrayList<TLRPC.MessageEntity>? {
        return addSpoilerEntities(messageObject, messageObject.messageOwner.entities)
    }

    fun addSpoilerEntities(
        messageObject: MessageObject,
        original: ArrayList<TLRPC.MessageEntity>?
    ): ArrayList<TLRPC.MessageEntity>? {
        return original
    }

    fun addSpoilerEntities(originalText: String?): String? {
        return originalText
    }

    fun shouldBlockMessage(messageObject: MessageObject): Boolean {
        return false
    }

    /** Exclusions start */
    fun getExcludedList(): String {
        return "excluded_for_filters"
    }

    fun getExcludedChatsCount(): Int {
        return 0
    }

    fun saveArrayList(list: ArrayList<String>, key: String) {

    }

    fun getArrayList(key: String): ArrayList<String> {
        return arrayListOf()
    }
    /** Exclusions finish */

}