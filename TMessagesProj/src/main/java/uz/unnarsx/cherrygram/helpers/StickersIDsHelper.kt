package uz.unnarsx.cherrygram.helpers

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.telegram.messenger.MessageObject
import org.telegram.tgnet.TLRPC
import java.net.URL

object StickersIDsHelper: CoroutineScope by MainScope() {

    private var SET_IDS = listOf<String>()

    fun getStickerSetIDs() {
        launch(Dispatchers.IO) {
            try {
                SET_IDS = URL("https://raw.githubusercontent.com/arsLan4k1390/Cherrygram/main/stickers.txt").readText().lines()
                Log.d("SetsDownloader", SET_IDS.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @JvmStatic
    fun isProperSet(document: Long): Boolean {
        return SET_IDS.contains(document.toString())
    }

    fun isProperSetID(document: TLRPC.Document): Boolean {
        return isProperSet(MessageObject.getStickerSetId(document))
    }

    // Locally stored IDs
    private val iDs = ArrayList<Long>()

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
        iDs.add(7131267980628852734L)
        iDs.add(7131267980628852733L)
        iDs.add(3346563080237613068L)
        iDs.add(6055278067666911223L)
        iDs.add(5062008833983905790L)
        iDs.add(1169953291908415506L)
        iDs.add(6055278067666911216L)
        iDs.add(4331929539736240157L)
        iDs.add(5091996690789957649L)
    }
}