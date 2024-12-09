package uz.unnarsx.cherrygram.core.configs

import android.app.Activity
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.telegram.messenger.ApplicationLoader
import uz.unnarsx.cherrygram.preferences.boolean
import uz.unnarsx.cherrygram.preferences.int

object CherrygramDebugConfig: CoroutineScope by CoroutineScope(
    context = SupervisorJob() + Dispatchers.Main.immediate
) {

    private val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)

    fun putBoolean(key: String, value: Boolean) {
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    /** Misc start **/
    var showRPCErrors by sharedPreferences.boolean("EP_ShowRPCErrors", false)
    var oldTimeStyle by sharedPreferences.boolean("CP_OldTimeStyle", false)
    /** Misc finish **/

    /** Blur start **/
    var forceChatBlurEffect by sharedPreferences.boolean("AP_ForceBlur", false)
    fun toggleForceChatBlurEffect() {
        forceChatBlurEffect = !forceChatBlurEffect
        putBoolean("AP_ForceBlur", forceChatBlurEffect)
    }
    var forceChatBlurEffectIntensity by sharedPreferences.int("AP_ForceBlur_Intensity", 155)
    /** Blur finish **/

    /** Chats start **/
    /** Microphone Audio Source start **/
    const val AUDIO_SOURCE_DEFAULT = 0
    const val AUDIO_SOURCE_CAMCORDER = 1
    const val AUDIO_SOURCE_MIC = 2
    const val AUDIO_SOURCE_REMOTE_SUBMIX = 3
    const val AUDIO_SOURCE_UNPROCESSED = 4
    const val AUDIO_SOURCE_VOICE_CALL = 5
    const val AUDIO_SOURCE_VOICE_COMMUNICATION = 6
    const val AUDIO_SOURCE_VOICE_DOWNLINK = 7
    const val AUDIO_SOURCE_VOICE_PERFORMANCE = 8
    const val AUDIO_SOURCE_VOICE_RECOGNITION = 9
    const val AUDIO_SOURCE_VOICE_UPLINK = 10
    var audioSource by sharedPreferences.int("audioSource", AUDIO_SOURCE_DEFAULT)
    /** Microphone Audio Source finish **/

    var sendVideosAtMaxQuality by sharedPreferences.boolean("sendVideosMaxQuality", false)
    var playGIFsAsVideos by sharedPreferences.boolean("CP_PlayGIFsAsVideos", true)
    /** Chats finish **/

}