package uz.unnarsx.cherrygram.core.configs

import android.app.Activity
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.SharedConfig
import uz.unnarsx.cherrygram.camera.CameraXUtils
import uz.unnarsx.cherrygram.helpers.CherrygramToasts
import uz.unnarsx.cherrygram.preferences.boolean
import uz.unnarsx.cherrygram.preferences.int

object CherrygramCameraConfig: CoroutineScope by CoroutineScope(
    context = SupervisorJob() + Dispatchers.Main.immediate
) {

    private val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)

    fun putBoolean(key: String, value: Boolean) {
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    /** Camera type start **/
    const val TELEGRAM_CAMERA = 0
    const val CAMERA_X = 1
    const val CAMERA_2 = 2
    const val SYSTEM_CAMERA = 3
    var cameraType by sharedPreferences.int("CP_CameraType", if (CameraXUtils.isCameraXSupported()) CAMERA_X else TELEGRAM_CAMERA)
    /** Camera type finish **/

    /** Camera start **/
    var disableAttachCamera by sharedPreferences.boolean("CP_DisableCam", SharedConfig.getDevicePerformanceClass() == SharedConfig.PERFORMANCE_CLASS_LOW)
    fun toggleDisableAttachCamera() {
        disableAttachCamera = !disableAttachCamera
        putBoolean("CP_DisableCam", disableAttachCamera)
    }

    var useDualCamera by sharedPreferences.boolean("CP_UseDualCamera", false)
    fun toggleUseDualCamera() {
        useDualCamera = !useDualCamera
        putBoolean("CP_UseDualCamera", useDualCamera)
    }

    const val Camera16to9 = 0
    const val Camera4to3 = 1
    const val Camera1to1 = 2
    const val CameraAspectDefault = 3
    var cameraAspectRatio by sharedPreferences.int("CP_CameraAspectRatio", CameraAspectDefault)
    /** Camera finish **/

    /** Videomessages start **/
    var cameraResolution by sharedPreferences.int("CP_CameraResolution", -1)

    var startFromUltraWideCam by sharedPreferences.boolean("CP_StartFromUltraWideCam", true)
    fun toggleStartFromUltraWideCam() {
        startFromUltraWideCam = !startFromUltraWideCam
        putBoolean("CP_StartFromUltraWideCam", startFromUltraWideCam)
    }

    var cameraStabilisation by sharedPreferences.boolean("CP_CameraStabilisation", false)
    fun toggleCameraStabilisation() {
        cameraStabilisation = !cameraStabilisation
        putBoolean("CP_CameraStabilisation", cameraStabilisation)
    }

    const val EXPOSURE_SLIDER_NONE = 0
//    const val EXPOSURE_SLIDER_BOTTOM = 1
    const val EXPOSURE_SLIDER_RIGHT = 2
//    const val EXPOSURE_SLIDER_LEFT = 3
    var exposureSlider by sharedPreferences.int("CP_ExposureSlider", EXPOSURE_SLIDER_RIGHT)

    var rearCam by sharedPreferences.boolean("CP_RearCam", false)
    fun toggleRearCam() {
        rearCam = !rearCam
        putBoolean("CP_RearCam", rearCam)
    }

    const val CaptureType_VideoCapture = 0
    const val CaptureType_ImageCapture = 1
    var captureTypeFront by sharedPreferences.int("CP_CaptureTypeFront", CaptureType_VideoCapture)
    var captureTypeBack by sharedPreferences.int("CP_CaptureTypeBack", CaptureType_VideoCapture)

    var whiteBackground by sharedPreferences.boolean("CG_WhiteBG", false)
    var videoMessagesResolution by sharedPreferences.int("CG_Round_Video_Resolution", 512)
    /** Videomessages finish **/

    init {
        CherrygramToasts.init(sharedPreferences)
    }

}