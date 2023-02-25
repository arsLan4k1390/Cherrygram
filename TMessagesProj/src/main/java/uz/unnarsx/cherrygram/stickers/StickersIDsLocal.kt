package uz.unnarsx.cherrygram.stickers

import org.telegram.messenger.MessageObject
import org.telegram.tgnet.TLRPC

object StickersIDsLocal {
    val iDs = ArrayList<Long>()

    @JvmStatic
    fun isLocalSetId(document: TLRPC.Document?): Boolean = iDs.stream().anyMatch { setID: Long ->
        setID == MessageObject.getStickerSetId(document)
    }

    init {
        iDs.add(683462835916767409L)
        iDs.add(1510769529645432834L)
        iDs.add(8106175868352593928L)
        iDs.add(5835129661968875533L)
        iDs.add(5149354467191160831L)
        iDs.add(5091996690789957635L)
        iDs.add(7131267980628852733L)
    }
}