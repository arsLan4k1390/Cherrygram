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
import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramDebugConfig
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.exp
import kotlin.math.roundToInt
import androidx.core.content.edit
import uz.unnarsx.cherrygram.core.CherrygramLogger

object AudioEnhance {

    private const val PREFS_NAME = "audio_enhance"

    private const val KEY_MUTED = "recording_audio_muted"
    private const val KEY_MODE_CHANGED = "recording_mode_changed"
    private const val KEY_VOLUME_CHANGED = "recording_volume_changed"

    private const val KEY_RINGER_MODE = "ringer_mode"
    private const val KEY_RING_VOLUME = "ring_volume"
    private const val KEY_NOTIFICATION_VOLUME = "notification_volume"

    private var muted = false

    @JvmStatic
    fun muteSystemSounds(context: Context) {
        if (muted) {
            return
        }

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        if (prefs.getBoolean(KEY_MUTED, false)) {
            recoverSystemSounds(context)
        }

        val currentMode = audioManager.ringerMode

        if (currentMode != AudioManager.RINGER_MODE_NORMAL) {
            return
        }

        val ringVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING)

        val notificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)

        prefs.edit(commit = true) {
            putInt(KEY_RINGER_MODE, currentMode)
                .putInt(KEY_RING_VOLUME, ringVolume)
                .putInt(KEY_NOTIFICATION_VOLUME, notificationVolume)
                .putBoolean(KEY_MUTED, true)
        }

        try {
            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT

            if (audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT) {
                prefs.edit(commit = true) {
                    putBoolean(KEY_MODE_CHANGED, true)
                        .putBoolean(KEY_VOLUME_CHANGED, false)
                }

                muted = true
                return
            }
        } catch (e: SecurityException) {
            CherrygramLogger.e(e)
        } catch (e: Exception) {
            CherrygramLogger.e(e)
        }

        var ringChanged = false
        var notificationChanged = false

        try {
            audioManager.setStreamVolume(
                AudioManager.STREAM_RING,
                0,
                0
            )
            ringChanged = true
        } catch (e: SecurityException) {
            CherrygramLogger.e(e)
        } catch (e: Exception) {
            CherrygramLogger.e(e)
        }

        try {
            audioManager.setStreamVolume(
                AudioManager.STREAM_NOTIFICATION,
                0,
                0
            )
            notificationChanged = true
        } catch (e: SecurityException) {
            CherrygramLogger.e(e)
        } catch (e: Exception) {
            CherrygramLogger.e(e)
        }

        if (ringChanged || notificationChanged) {
            prefs.edit(commit = true) {
                putBoolean(KEY_MODE_CHANGED, false)
                    .putBoolean(KEY_VOLUME_CHANGED, true)
            }

            muted = true
        } else {
            prefs.edit(commit = true) {
                clear()
            }

            muted = false
        }
    }

    @JvmStatic
    fun restoreSystemSounds(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        if (!prefs.getBoolean(KEY_MUTED, false)) {
            muted = false
            return
        }

        val modeChanged = prefs.getBoolean(KEY_MODE_CHANGED, false)

        val volumeChanged = prefs.getBoolean(KEY_VOLUME_CHANGED, false)

        val originalMode =
            prefs.getInt(
                KEY_RINGER_MODE,
                AudioManager.RINGER_MODE_NORMAL
            )

        val originalRingVolume =
            prefs.getInt(
                KEY_RING_VOLUME,
                audioManager.getStreamVolume(AudioManager.STREAM_RING)
            )

        val originalNotificationVolume =
            prefs.getInt(
                KEY_NOTIFICATION_VOLUME,
                audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
            )

        try {
            if (modeChanged) {
                try {
                    audioManager.ringerMode = originalMode
                } catch (e: SecurityException) {
                    CherrygramLogger.e(e)
                }
            }

            if (volumeChanged) {
                try {
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_RING,
                        originalRingVolume.coerceIn(
                            0,
                            audioManager.getStreamMaxVolume(
                                AudioManager.STREAM_RING
                            )
                        ),
                        0
                    )
                } catch (e: SecurityException) {
                    CherrygramLogger.e(e)
                } catch (e: Exception) {
                    CherrygramLogger.e(e)
                }

                try {
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_NOTIFICATION,
                        originalNotificationVolume.coerceIn(
                            0,
                            audioManager.getStreamMaxVolume(
                                AudioManager.STREAM_NOTIFICATION
                            )
                        ),
                        0
                    )
                } catch (e: SecurityException) {
                    CherrygramLogger.e(e)
                } catch (e: Exception) {
                    CherrygramLogger.e(e)
                }
            }
        } finally {
            prefs.edit(commit = true) {
                remove(KEY_MUTED)
                    .remove(KEY_MODE_CHANGED)
                    .remove(KEY_VOLUME_CHANGED)
                    .remove(KEY_RINGER_MODE)
                    .remove(KEY_RING_VOLUME)
                    .remove(KEY_NOTIFICATION_VOLUME)
            }

            muted = false
        }
    }

    @JvmStatic
    fun recoverSystemSounds(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        if (!prefs.getBoolean(KEY_MUTED, false)) {
            return
        }

        restoreSystemSounds(context)
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
                CherrygramLogger.e(e)
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