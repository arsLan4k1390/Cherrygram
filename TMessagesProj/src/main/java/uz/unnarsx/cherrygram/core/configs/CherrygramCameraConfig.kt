/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.core.configs

import android.app.Activity
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.SharedConfig
import uz.unnarsx.cherrygram.camera.CameraXUtils
import uz.unnarsx.cherrygram.preferences.boolean
import uz.unnarsx.cherrygram.preferences.int

object CherrygramCameraConfig: CoroutineScope by CoroutineScope(
    context = SupervisorJob() + Dispatchers.Main.immediate
) {

    private val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)

    /** Camera type start **/
    const val TELEGRAM_CAMERA = 0
    const val CAMERA_X = 1
    const val CAMERA_2 = 2
    const val SYSTEM_CAMERA = 3
    var cameraType by sharedPreferences.int("CP_CameraType", if (CameraXUtils.isCameraXSupported()) CAMERA_X else TELEGRAM_CAMERA)
    /** Camera type finish **/

    /** Camera start **/
    var disableAttachCamera by sharedPreferences.boolean("CP_DisableCam", true)
    var useDualCamera by sharedPreferences.boolean("CP_UseDualCamera", false)

    const val Camera16to9 = 0
    const val Camera4to3 = 1
    const val Camera1to1 = 2
    const val CameraAspectDefault = 3
    var cameraAspectRatio by sharedPreferences.int("CP_CameraAspectRatio", CameraAspectDefault)
    /** Camera finish **/

    /** Videomessages start **/
    var cameraResolution by sharedPreferences.int("CP_CameraResolution", -1)
    var startFromUltraWideCam by sharedPreferences.boolean("CP_StartFromUltraWideCam", true)

    /** CameraX FPS start **/
    const val CameraXFpsRangeDefault = 0
    const val CameraXFpsRange25to30 = 1
    const val CameraXFpsRange30to30 = 2
    const val CameraXFpsRange30to60 = 3
    const val CameraXFpsRange60to60 = 4
    var cameraXFpsRange by sharedPreferences.int("CP_CameraXFpsRangeNew",
        if (SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_AVERAGE) CameraXFpsRange25to30 else CameraXFpsRangeDefault)
    /** CameraX FPS finish **/

    var cameraStabilisation by sharedPreferences.boolean("CP_CameraStabilisation", false)
    var centerCameraControlButtons by sharedPreferences.boolean("CP_CenterCameraControlButtons", true)

    const val EXPOSURE_SLIDER_NONE = 0
//    const val EXPOSURE_SLIDER_BOTTOM = 1
    const val EXPOSURE_SLIDER_RIGHT = 2
//    const val EXPOSURE_SLIDER_LEFT = 3
    var exposureSlider by sharedPreferences.int("CP_ExposureSlider", EXPOSURE_SLIDER_RIGHT)

    var rearCam by sharedPreferences.boolean("CP_RearCam", false)

    var whiteBackground by sharedPreferences.boolean("CG_WhiteBG", false)
    var videoMessagesResolution by sharedPreferences.int("CG_Round_Video_Resolution", 512)
    /** Videomessages finish **/

}