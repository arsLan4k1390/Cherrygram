/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.chats.helpers

import org.telegram.messenger.MessageObject
import org.telegram.messenger.MessagesController
import org.telegram.tgnet.TLRPC
import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig
import uz.unnarsx.cherrygram.helpers.network.DonatesManager

// Dear Nagram / Nagram X / Octogram and related fork developers:
// Please respect this work and do not copy or reuse this feature in your forks.
// It required a significant amount of time and effort to implement,
// and it is provided exclusively for my users, who also support this project financially.

object MessagesFilterHelper {

    fun isFiltered(messageObject: MessageObject?): Boolean {
        return getFilteredRanges(messageObject).isNotEmpty()
    }

    private fun normalizeText(text: String): String {
        val translitMap = mapOf(
            'a' to 'а', 'A' to 'А', '@' to 'а',
            'b' to 'б', 'B' to 'В',
            'c' to 'с', 'C' to 'С', 'ç' to 'с',
            'd' to 'д', 'D' to 'Д',
            'e' to 'е', 'E' to 'Е', 'ё' to 'е', 'É' to 'Е',
            'f' to 'ф', 'F' to 'Ф',
            'g' to 'г', 'G' to 'Г',
            'h' to 'х', 'H' to 'Н',
            'i' to 'и', 'I' to 'И', '1' to 'и', '!' to 'и',
            'j' to 'ж', 'J' to 'Ж',
            'k' to 'к', 'K' to 'К',
            'l' to 'л', 'L' to 'Л',
            'm' to 'м', 'M' to 'М',
            'n' to 'н', 'N' to 'Н',
            'o' to 'о', 'O' to 'О', '0' to 'о',
            'p' to 'р', 'P' to 'Р',
            'q' to 'к', 'Q' to 'К',
            'r' to 'р', 'R' to 'Р',
            's' to 'с', 'S' to 'С', '$' to 'с',
            't' to 'т', 'T' to 'Т', '7' to 'т',
            'u' to 'у', 'U' to 'У',
            'v' to 'в', 'V' to 'В',
            'w' to 'в', 'W' to 'В',
            'x' to 'х', 'X' to 'Х',
            'y' to 'у', 'Y' to 'У',
            'z' to 'з', 'Z' to 'З', '3' to 'з',

            'а' to 'а', 'б' to 'б', 'в' to 'в', 'г' to 'г', 'д' to 'д',
            'е' to 'е', 'ж' to 'ж', 'з' to 'з', 'и' to 'и', 'й' to 'й',
            'к' to 'к', 'л' to 'л', 'м' to 'м', 'н' to 'н', 'о' to 'о',
            'п' to 'п', 'р' to 'р', 'с' to 'с', 'т' to 'т', 'у' to 'у',
            'ф' to 'ф', 'х' to 'х', 'ц' to 'ц', 'ч' to 'ч', 'ш' to 'ш',
            'щ' to 'щ', 'ъ' to 'ъ', 'ы' to 'ы', 'ь' to 'ь', 'э' to 'э',
            'ю' to 'ю', 'я' to 'я'
        )
        val builder = StringBuilder(text.length)
        for (ch in text) {
            val lower = ch.lowercaseChar()
            builder.append(translitMap[lower] ?: ch)
        }
        return builder.toString()
    }

    private fun getFilteredRanges(messageObject: MessageObject?): List<Pair<Int, Int>> {
        val matchExact = CherrygramChatsConfig.msgFiltersMatchExactWord
        val filterWords = getFilterWords()

        if (messageObject?.messageOwner?.message.isNullOrEmpty() || (!CherrygramChatsConfig.msgFiltersHideFromBlocked && filterWords.isEmpty())) {
            return emptyList()
        }

        val text = messageObject.messageOwner.message

        val entities = messageObject.messageOwner.entities
        if (!entities.isNullOrEmpty()) {
            val filterWordsLower = filterWords.map { it.lowercase() }
            for (entity in entities) {
                val url = entity.url?.lowercase()
                if (!url.isNullOrEmpty()) {
                    for (word in filterWordsLower) {
                        if (url.contains(word)) {
                            return listOf(0 to text.length)
                        }
                    }
                }
            }
        }

        val lowerText = if (CherrygramChatsConfig.msgFiltersDetectTranslit) {
            normalizeText(text).lowercase()
        } else {
            text.lowercase()
        }
        val ranges = mutableListOf<Pair<Int, Int>>()

        for (word in filterWords) {
            val lowerWord = if (CherrygramChatsConfig.msgFiltersDetectTranslit) {
                normalizeText(word).lowercase()
            } else {
                word.lowercase()
            }
            if (lowerWord.isEmpty()) continue

            var index = lowerText.indexOf(lowerWord)
            while (index >= 0) {
                if (matchExact) {
                    if (isExactWordMatch(lowerText, index, lowerWord.length)) {
                        ranges.add(index to (index + lowerWord.length))
                    }
                } else {
                    val (start, end) = getWordBounds(lowerText, index, lowerWord.length)
                    ranges.add(start to end)
                }
                index = lowerText.indexOf(lowerWord, index + 1)
            }
        }
        return ranges
    }

    fun addSpoilerEntities(messageObject: MessageObject): ArrayList<TLRPC.MessageEntity>? {
        return addSpoilerEntities(messageObject, messageObject.messageOwner.entities)
    }

    fun addSpoilerEntities(
        messageObject: MessageObject,
        original: ArrayList<TLRPC.MessageEntity>?
    ): ArrayList<TLRPC.MessageEntity>? {
        val ranges = getFilteredRanges(messageObject)

        if (!messageObject.shouldBlockMessage() || (ranges.isEmpty() && !CherrygramChatsConfig.msgFiltersHideFromBlocked)) {
            return original
        }

        val merged = ranges
            .sortedBy { it.first }
            .fold(mutableListOf<Pair<Int, Int>>()) { acc, r ->
                if (acc.isEmpty()) {
                    acc.add(r)
                } else {
                    val last = acc.last()
                    if (r.first <= last.second) {
                        acc[acc.size - 1] = last.first to maxOf(last.second, r.second)
                    } else {
                        acc.add(r)
                    }
                }
                acc
            }

        val owner = messageObject.messageOwner ?: return original
        if (owner.entities == null) owner.entities = ArrayList()

        if (isUserBlocked(messageObject) || CherrygramChatsConfig.msgFiltersHideAll) {
            val spoiler = TLRPC.TL_messageEntitySpoiler()
            spoiler.offset = 0
            spoiler.length = messageObject.messageOwner.message.length
            owner.entities.add(spoiler)

            if (CherrygramChatsConfig.msgFiltersCollapseAutomatically) {
                val quote = TLRPC.TL_messageEntityBlockquote()
                quote.offset = 0
                quote.length = messageObject.messageOwner.message.length
                quote.collapsed = true
                owner.entities.add(quote)
            }
        } else {
            merged.forEach { (start, end) ->
                val spoiler = TLRPC.TL_messageEntitySpoiler()
                spoiler.offset = start
                spoiler.length = end - start
                owner.entities.add(spoiler)
            }
        }

        return owner.entities
    }

    fun addSpoilerEntities(originalText: String?): String? {
        if (originalText == null) return null

        val filterWords = getFilterWords()
        if (filterWords.isEmpty()) return originalText

        val lowerText = originalText.lowercase()
        val stringBuilder = StringBuilder(originalText)

        for (word in filterWords) {
            var index = lowerText.indexOf(word.lowercase())
            while (index >= 0) {
                if (isExactWordMatch(lowerText, index, word.length)) {
                    val (start, end) = getWordBounds(lowerText, index, word.length)
                    for (i in start until end) {
                        stringBuilder.setCharAt(i, ChatsPasswordHelper.spoilerChars[i % ChatsPasswordHelper.spoilerChars.size])
                    }
                }
                index = lowerText.indexOf(word.lowercase(), index + word.length)
            }
        }

        return stringBuilder.toString()
    }

    private fun getFilterWords(): List<String> {
        val filterString = CherrygramChatsConfig.msgFiltersElements
        return filterString
            .split(Regex("[,;\\s]+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    private fun isExactWordMatch(text: String, index: Int, length: Int): Boolean {
        val beforeChar = if (index > 0) text[index - 1] else ' '
        val afterChar = if (index + length < text.length) text[index + length] else ' '
        return !Character.isLetterOrDigit(beforeChar) && !Character.isLetterOrDigit(afterChar)
    }

    private fun getWordBounds(text: String, matchStart: Int, matchLength: Int): Pair<Int, Int> {
        var start = matchStart
        var end = matchStart + matchLength

        while (start > 0 && Character.isLetterOrDigit(text[start - 1])) start--
        while (end < text.length && Character.isLetterOrDigit(text[end])) end++

        return start to end
    }

    fun shouldBlockMessage(messageObject: MessageObject): Boolean {
        if (messageObject.messageOwner == null || messageObject.storyItem != null) {
            return false
        }
        if (!CherrygramChatsConfig.enableMsgFilters/* || !DonatesManager.checkAllDonatedAccountsForMarketplace()*/) {
            return false
        }
        if (messageObject.isOut || messageObject.isOutOwner) {
            return false
        }
        return isUserBlocked(messageObject) || isFiltered(messageObject)
    }

    private fun isUserBlocked(message: MessageObject): Boolean {
        if (!CherrygramChatsConfig.msgFiltersHideFromBlocked) return false

        if (isUserBlocked(message.currentAccount, message.fromChatId)) {
            return true
        }

        if (message.isForwarded && message.messageOwner.fwd_from != null && message.messageOwner.fwd_from.from_id != null) {
            return isUserBlocked(
                message.currentAccount,
                MessageObject.getPeerId(message.messageOwner.fwd_from.from_id)
            )
        }
        return false
    }

    private fun isUserBlocked(currentAccount: Int, id: Long): Boolean { // Thanks to Nekogram for this function ❤️
        val messagesController = MessagesController.getInstance(currentAccount)
        val userFull = messagesController.getUserFull(id)
        return (userFull != null && userFull.blocked) || messagesController.blockePeers.indexOfKey(id) >= 0
    }

}


