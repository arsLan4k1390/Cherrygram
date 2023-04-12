package uz.unnarsx.cherrygram.extras

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
import uz.unnarsx.cherrygram.CherrygramConfig

object VibrateUtil {

    lateinit var vibrator: Vibrator

    @JvmStatic
    fun disableHapticFeedback(view: View) {
        view.isHapticFeedbackEnabled = false
        (view as? ViewGroup)?.children?.forEach(VibrateUtil::disableHapticFeedback)
    }

    @JvmStatic
    @JvmOverloads
    fun vibrate(time: Long = 200L) {

        if (CherrygramConfig.disableVibration) return

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
        } catch (ignore: Exception) {
        }
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
        } catch (ignore: Exception) {
        }
    }

}