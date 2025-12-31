/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig

object VibrateUtil {

    lateinit var vibrator: Vibrator

    fun disableHapticFeedback(view: View) {
        view.isHapticFeedbackEnabled = false
        (view as? ViewGroup)?.children?.forEach(VibrateUtil::disableHapticFeedback)
    }

    @JvmOverloads
    fun vibrate(time: Long = 200L) {

        if (CherrygramChatsConfig.disableVibration) return

        if (!VibrateUtil::vibrator.isInitialized) {
            // Use new VibratorManager service for API >= 31
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    ApplicationLoader.applicationContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                // Backward compatibility for API < 31
                @Suppress("DEPRECATION")
                ApplicationLoader.applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
        }

        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runCatching {
                val effect = VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(effect, null)
            }
        } else {
            runCatching {
                // Backward compatibility for API < 26
                @Suppress("DEPRECATION")
                vibrator.vibrate(time)
            }
        }
    }

    fun makeClickVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val vibrator = AndroidUtilities.getVibrator()
                val vibrationEffect = VibrationEffect.createPredefined(
                    VibrationEffect.EFFECT_CLICK
                )
                vibrator.cancel()
                vibrator.vibrate(vibrationEffect)
            }
        } catch (ignore: Exception) { }
    }

    fun makeWaveVibration() { //MIUI moment
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val vibrator = AndroidUtilities.getVibrator()
                val vibrationEffect = VibrationEffect.createWaveform(
                    longArrayOf(75, 10, 5, 10),
                    intArrayOf(5, 20, 90, 20),
                    -1
                )
                vibrator.cancel()
                vibrator.vibrate(vibrationEffect)
            }
        } catch (ignore: Exception) { }
    }

}