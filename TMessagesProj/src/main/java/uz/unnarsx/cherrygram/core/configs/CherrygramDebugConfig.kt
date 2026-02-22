/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.configs

import android.app.Activity
import android.content.SharedPreferences
import org.telegram.messenger.ApplicationLoader
import uz.unnarsx.cherrygram.preferences.boolean
import uz.unnarsx.cherrygram.preferences.int

object CherrygramDebugConfig {

    private val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)

    /** Misc start */
    var showRPCErrors by sharedPreferences.boolean("EP_ShowRPCErrors", false)
    var oldTimeStyle by sharedPreferences.boolean("CP_OldTimeStyle", false)
    /** Misc finish */

    /** Redesign start */
    /** Redesign finish */

    /** Chats start */
    var chatPreviewFix by sharedPreferences.boolean("chatPreviewFix", true)
    var replacePunctuationMarks by sharedPreferences.boolean("replacePunctuationMarks", true)
    var editTextSuggestionsFix by sharedPreferences.boolean("editTextSuggestionsFix", false)
    /** Microphone Audio Source start */
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
    /** Microphone Audio Source finish */

    var sendVideosAtMaxQuality by sharedPreferences.boolean("sendVideosMaxQuality", true)
    var playGIFsAsVideos by sharedPreferences.boolean("CP_PlayGIFsAsVideos", true)
    var hideVideoTimestamp by sharedPreferences.boolean("CP_HideVideoTimestamp", true)
    /** Chats finish */

}