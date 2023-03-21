package uz.unnarsx.cherrygram.stickers

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.telegram.messenger.MessageObject
import org.telegram.tgnet.TLRPC
import java.net.URL

object StickersIDsDownloader: CoroutineScope by MainScope() {

    var SET_IDS = listOf<String>()

    init {
        launch(Dispatchers.IO) {
            try {
                SET_IDS = URL("https://raw.githubusercontent.com/arsLan4k1390/Cherrygram/main/stickers.txt").readText().lines()
//                Log.d("SetsDownloader", SET_IDS.toString())
            }
            catch (e: Exception) {
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
}