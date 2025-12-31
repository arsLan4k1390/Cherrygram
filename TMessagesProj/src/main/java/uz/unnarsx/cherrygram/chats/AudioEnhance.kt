/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.chats

import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramDebugConfig

object AudioEnhance {

    private var agc: AutomaticGainControl? = null
    private var ns: NoiseSuppressor? = null
    private var aec: AcousticEchoCanceler? = null

    fun initVoiceEnhancements(audioRecord: AudioRecord) {
        if (!CherrygramChatsConfig.voicesAgc) return

        agc = if (AutomaticGainControl.isAvailable()) {
            AutomaticGainControl.create(audioRecord.audioSessionId)?.apply { enabled = true }
        } else null

        ns = if (NoiseSuppressor.isAvailable()) {
            NoiseSuppressor.create(audioRecord.audioSessionId)?.apply { enabled = true }
        } else null

        aec = if (AcousticEchoCanceler.isAvailable()) {
            AcousticEchoCanceler.create(audioRecord.audioSessionId)?.apply { enabled = true }
        } else null
    }

    fun releaseVoiceEnhancements() {
        agc?.let {
            it.release()
            agc = null
        }
        ns?.let {
            it.release()
            ns = null
        }
        aec?.let {
            it.release()
            aec = null
        }
    }

    fun getAudioSource(): Int = when (CherrygramDebugConfig.audioSource) {
        CherrygramDebugConfig.AUDIO_SOURCE_CAMCORDER -> MediaRecorder.AudioSource.CAMCORDER
        CherrygramDebugConfig.AUDIO_SOURCE_MIC -> MediaRecorder.AudioSource.MIC
        CherrygramDebugConfig.AUDIO_SOURCE_REMOTE_SUBMIX -> MediaRecorder.AudioSource.REMOTE_SUBMIX
        CherrygramDebugConfig.AUDIO_SOURCE_UNPROCESSED -> MediaRecorder.AudioSource.UNPROCESSED // Api 24
        CherrygramDebugConfig.AUDIO_SOURCE_VOICE_CALL -> MediaRecorder.AudioSource.VOICE_CALL
        CherrygramDebugConfig.AUDIO_SOURCE_VOICE_COMMUNICATION -> MediaRecorder.AudioSource.VOICE_COMMUNICATION
        CherrygramDebugConfig.AUDIO_SOURCE_VOICE_DOWNLINK -> MediaRecorder.AudioSource.VOICE_DOWNLINK
        CherrygramDebugConfig.AUDIO_SOURCE_VOICE_PERFORMANCE -> MediaRecorder.AudioSource.VOICE_PERFORMANCE // Api 29
        CherrygramDebugConfig.AUDIO_SOURCE_VOICE_RECOGNITION -> MediaRecorder.AudioSource.VOICE_RECOGNITION
        CherrygramDebugConfig.AUDIO_SOURCE_VOICE_UPLINK -> MediaRecorder.AudioSource.VOICE_UPLINK
        else -> MediaRecorder.AudioSource.DEFAULT
    }

}