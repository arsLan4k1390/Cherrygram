package uz.unnarsx.cherrygram.core.configs

import android.app.Activity
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.telegram.messenger.ApplicationLoader
import uz.unnarsx.cherrygram.chats.helpers.ChatsPasswordHelper
import uz.unnarsx.cherrygram.core.helpers.FirebaseAnalyticsHelper
import uz.unnarsx.cherrygram.preferences.boolean
import uz.unnarsx.cherrygram.preferences.long

object CherrygramPrivacyConfig: CoroutineScope by CoroutineScope(
    context = SupervisorJob() + Dispatchers.Main.immediate
) {

    private val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)

    /** Privacy start **/
    var hideProxySponsor by sharedPreferences.boolean("SP_NoProxyPromo", true)
    var googleAnalytics by sharedPreferences.boolean("SP_GoogleAnalytics", ApplicationLoader.checkPlayServices())
    /** Privacy finish **/

    /** Passcode lock start **/
    var hideArchiveFromChatsList by sharedPreferences.boolean("SP_HideArchiveFromChatsList", false)
    var askBiometricsToOpenArchive by sharedPreferences.boolean("SP_AskBiometricsToOpenArchive", false)
    var askBiometricsToOpenChat by sharedPreferences.boolean("SP_AskBiometricsToOpenChat", false)
    private var tweakPasscodeChatsArray by sharedPreferences.boolean("tweakPasscodeChatsArray", false)
    var askPasscodeBeforeDelete by sharedPreferences.boolean("SP_AskPinBeforeDelete", false)
    var allowSystemPasscode by sharedPreferences.boolean("SP_AllowSystemPasscode", false)
    /** Passcode lock finish **/

    /** Misc **/
    var reTgCheck by sharedPreferences.boolean("SP_ReTgCheck", true)
    /** Misc **/

    init {
        launch {
            if (googleAnalytics && ApplicationLoader.checkPlayServices()) {
                FirebaseAnalyticsHelper.start(ApplicationLoader.applicationContext)
            }
            FirebaseAnalyticsHelper.trackEventWithEmptyBundle("cg_start")

            if (!tweakPasscodeChatsArray) {
                val arr: ArrayList<String?> = ArrayList()
                arr.add("0")
                ChatsPasswordHelper.saveArrayList(arr, ChatsPasswordHelper.Passcode_Array)
                tweakPasscodeChatsArray = true
            }

            /*delay(5000)
            FirebaseAnalyticsHelper.trackAllCherrygramSettings()*/
        }
    }

}