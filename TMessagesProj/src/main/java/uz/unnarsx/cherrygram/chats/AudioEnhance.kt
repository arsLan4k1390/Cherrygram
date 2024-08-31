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

    @JvmStatic
    fun initVoiceEnhancements(audioRecord: AudioRecord) {
        if (!CherrygramChatsConfig.voicesAgc) return

        if (AutomaticGainControl.isAvailable()) {
            agc = AutomaticGainControl.create(audioRecord.audioSessionId)
            agc!!.enabled = true
        }
        if (NoiseSuppressor.isAvailable()) {
            ns = NoiseSuppressor.create(audioRecord.audioSessionId)
            ns!!.enabled = true
        }
        if (AcousticEchoCanceler.isAvailable()) {
            aec = AcousticEchoCanceler.create(audioRecord.audioSessionId)
            aec!!.enabled = true
        }
    }

    @JvmStatic
    fun releaseVoiceEnhancements() {
        if (agc != null) {
            agc!!.release()
            agc = null
        }
        if (ns != null) {
            ns!!.release()
            ns = null
        }
        if (aec != null) {
            aec!!.release()
            aec = null
        }
    }

    @JvmStatic
    fun getAudioSource(): Int {
        val source: Int = when (CherrygramDebugConfig.audioSource) {
            CherrygramDebugConfig.AUDIO_SOURCE_CAMCORDER -> {
                MediaRecorder.AudioSource.CAMCORDER
            }
            CherrygramDebugConfig.AUDIO_SOURCE_MIC -> {
                MediaRecorder.AudioSource.MIC
            }
            CherrygramDebugConfig.AUDIO_SOURCE_REMOTE_SUBMIX -> {
                MediaRecorder.AudioSource.REMOTE_SUBMIX
            }
            CherrygramDebugConfig.AUDIO_SOURCE_UNPROCESSED -> {
                MediaRecorder.AudioSource.UNPROCESSED //Api 24
            }
            CherrygramDebugConfig.AUDIO_SOURCE_VOICE_CALL -> {
                MediaRecorder.AudioSource.VOICE_CALL
            }
            CherrygramDebugConfig.AUDIO_SOURCE_VOICE_COMMUNICATION -> {
                MediaRecorder.AudioSource.VOICE_COMMUNICATION
            }
            CherrygramDebugConfig.AUDIO_SOURCE_VOICE_DOWNLINK -> {
                MediaRecorder.AudioSource.VOICE_DOWNLINK
            }
            CherrygramDebugConfig.AUDIO_SOURCE_VOICE_PERFORMANCE -> {
                MediaRecorder.AudioSource.VOICE_PERFORMANCE //Api 29
            }
            CherrygramDebugConfig.AUDIO_SOURCE_VOICE_RECOGNITION -> {
                MediaRecorder.AudioSource.VOICE_RECOGNITION
            }
            CherrygramDebugConfig.AUDIO_SOURCE_VOICE_UPLINK -> {
                MediaRecorder.AudioSource.VOICE_UPLINK
            }
            else -> {
                MediaRecorder.AudioSource.DEFAULT
            }
        }
        return source
    }

}