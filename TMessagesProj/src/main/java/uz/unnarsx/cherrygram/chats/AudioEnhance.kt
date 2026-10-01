/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.chats

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import org.telegram.messenger.FileLog
import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramDebugConfig
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.exp
import kotlin.math.roundToInt

object AudioEnhance {

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

    private fun useStereoMode(): Boolean = CherrygramChatsConfig.recordInStereo
    private fun useMultiMic(): Boolean = CherrygramChatsConfig.recordMultiMic

    @JvmStatic
    fun outputChannels(recorder: AudioRecord): Int =
        if (useMultiMic() && useStereoMode() && recorder.channelCount >= 2) 2 else 1

    @JvmStatic
    @JvmOverloads
    @SuppressLint("MissingPermission")
    fun createRecorder(sampleRate: Int, monoBufferSize: Int, preferStereo: Boolean = useMultiMic()): AudioRecord {
        val source = getAudioSource()
        if (preferStereo) {
            val stereo = try {
                AudioRecord(source, sampleRate, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, monoBufferSize * 2)
            } catch (e: Exception) {
                FileLog.e(e)
                null
            }
            if (stereo != null) {
                if (stereo.state == AudioRecord.STATE_INITIALIZED) return stereo
                stereo.release()
            }
        }
        return AudioRecord(source, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, monoBufferSize)
    }

    class MonoMixer {
        private var tmp: ByteBuffer? = null
        private var lastRecorder: AudioRecord? = null
        private var gainA = 0.5f
        private var gainB = 0.5f
        private var energyA = 0f
        private var energyB = 0f

        @JvmOverloads
        fun read(recorder: AudioRecord, out: ByteBuffer, maxBytes: Int, keepStereo: Boolean = false): Int {
            if (recorder.channelCount < 2 || keepStereo) {
                return recorder.read(out, maxBytes)
            }
            if (recorder !== lastRecorder) {
                lastRecorder = recorder
                gainA = 0.5f; gainB = 0.5f
                energyA = 0f; energyB = 0f
            }

            val need = maxBytes * 2
            var t = tmp
            if (t == null || t.capacity() < need) {
                t = ByteBuffer.allocateDirect(need).order(ByteOrder.nativeOrder())
                tmp = t
            }
            t!!.clear()
            val r = recorder.read(t, need)
            if (r <= 0) return r
            val frames = r / 4
            if (frames == 0) return 0
            t.position(0)
            t.limit(r)
            val inp = t.asShortBuffer()

            var dA = 0L
            var dB = 0L
            var prevA = inp.get(0).toInt()
            var prevB = inp.get(1).toInt()
            for (i in 0 until frames) {
                val a = inp.get(i * 2).toInt()
                val b = inp.get(i * 2 + 1).toInt()
                val da = a - prevA
                val db = b - prevB
                dA += da.toLong() * da
                dB += db.toLong() * db
                prevA = a
                prevB = b
            }

            val rate = recorder.sampleRate.toFloat()
            val e = 1f - exp(-(frames / rate) / 0.2f)
            val g = 1f - exp(-1f / (rate * 0.01f))
            energyA += (dA.toFloat() / frames - energyA) * e
            energyB += (dB.toFloat() / frames - energyB) * e

            var targetA = 0.5f
            var targetB = 0.5f
            if (energyA + energyB > SILENCE) {
                if (energyA < energyB * RATIO) {
                    targetA = 0f; targetB = 1f
                } else if (energyB < energyA * RATIO) {
                    targetA = 1f; targetB = 0f
                }
            }

            val o = out.duplicate()
            o.clear()
            for (i in 0 until frames) {
                gainA += (targetA - gainA) * g
                gainB += (targetB - gainB) * g
                val s = (inp.get(i * 2) * gainA + inp.get(i * 2 + 1) * gainB)
                    .roundToInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                o.put(i * 2, (s and 0xFF).toByte())
                o.put(i * 2 + 1, ((s shr 8) and 0xFF).toByte())
            }
            return frames * 2
        }

        private companion object {
            const val RATIO = 0.1f
            const val SILENCE = 2000f
        }
    }

}