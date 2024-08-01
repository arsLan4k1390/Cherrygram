package uz.unnarsx.cherrygram.core.helpers

import android.os.Build
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.URLSpan
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.FileLog
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.Components.URLSpanNoUnderline
import org.telegram.ui.LauncherIconController
import uz.unnarsx.cherrygram.CherrygramConfig
import java.util.Calendar
import java.util.Date

object CGResourcesHelper {

    /**About app start**/
    fun getAppName(): String {
        if (CherrygramConfig.isStableBuild() || CherrygramConfig.isPlayStoreBuild()) {
            return "Cherrygram"
        } else if (CherrygramConfig.isBetaBuild()) {
            return "Cherrygram Beta"
        } else if (CherrygramConfig.isPremiumBuild()) {
            return "Cherrygram Premium"
        } else if (CherrygramConfig.isDevBuild()) {
            return "Cherrygram Dev"
        }
        return LocaleController.getString("CG_AppName", R.string.CG_AppName)
    }

    fun getBuildType(): String {
        if (CherrygramConfig.isStableBuild()) {
            return LocaleController.getString("UP_BTRelease", R.string.UP_BTRelease)
        } else if (CherrygramConfig.isPlayStoreBuild()) {
            return "Play Store"
        } else if (CherrygramConfig.isBetaBuild()) {
            return LocaleController.getString("UP_BTBeta", R.string.UP_BTBeta)
        } else if (CherrygramConfig.isPremiumBuild()) {
            return "Premium"
        } else if (CherrygramConfig.isDevBuild()) {
            return "Dev"
        }
        return "Unknown"
    }

    @Suppress("DEPRECATION")
    fun getAbiCode(): String {
        var abi = ""
        try {
            when (ApplicationLoader.applicationContext.packageManager.getPackageInfo(
                ApplicationLoader.applicationContext.packageName, 0).versionCode % 10) {
                1, 3 -> abi = "armeabi-v7a"
                2, 4 -> abi = "x86"
                5, 7 -> abi = "arm64-v8a"
                6, 8 -> abi = "x86_64"
                0, 9 -> abi = "universal"
            }
        } catch (e: java.lang.Exception) {
            FileLog.e(e)
        }
        return abi
    }
    /**About app finish**/

    /**Camera start**/
    @JvmStatic
    fun getCameraName(): String { // Crashlytics.java:\ Camera type
        return when (CherrygramConfig.cameraType) {
            CherrygramConfig.TELEGRAM_CAMERA -> "Telegram"
            CherrygramConfig.CAMERA_X -> "CameraX"
            CherrygramConfig.CAMERA_2 -> "Camera 2"
            else -> LocaleController.getString("CP_CameraTypeSystem", R.string.CP_CameraTypeSystem)
        }
    }

    @JvmStatic
    fun getCameraAdvise(): CharSequence {
        val advise: String = when (CherrygramConfig.cameraType) {
            CherrygramConfig.TELEGRAM_CAMERA -> LocaleController.getString("CP_DefaultCameraDesc", R.string.CP_DefaultCameraDesc)
            CherrygramConfig.CAMERA_X -> LocaleController.getString("CP_CameraXDesc", R.string.CP_CameraXDesc)
            CherrygramConfig.CAMERA_2 -> LocaleController.getString("CP_Camera2Desc", R.string.CP_Camera2Desc)
            else -> LocaleController.getString("CP_SystemCameraDesc", R.string.CP_SystemCameraDesc)
        }

        val htmlParsed: Spannable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SpannableString(Html.fromHtml(advise, Html.FROM_HTML_MODE_LEGACY))
        } else {
            SpannableString(Html.fromHtml(advise))
        }
        return getUrlNoUnderlineText(htmlParsed)
    }

    @JvmStatic
    fun getCameraAspectRatio(): String { // CameraPreferences.java:\Camera aspect ratio
        return when (CherrygramConfig.cameraAspectRatio) {
            CherrygramConfig.Camera1to1 -> "1:1"
            CherrygramConfig.Camera4to3 -> "4:3"
            CherrygramConfig.Camera16to9 -> "16:9"
            else -> LocaleController.getString("Default", R.string.Default)
        }
    }

    @JvmStatic
    fun getZoomSliderPosition(): String { // CameraPreferences.java:\Zoom slider
        return when (CherrygramConfig.zoomSlider) {
            CherrygramConfig.ZOOM_SLIDER_BOTTOM -> LocaleController.getString("CP_ZoomSliderPosition_Bottom", R.string.CP_ZoomSliderPosition_Bottom)
//            CherrygramConfig.ZOOM_SLIDER_RIGHT -> LocaleController.getString("CP_ZoomSliderPosition_Right", R.string.CP_ZoomSliderPosition_Right)
            CherrygramConfig.ZOOM_SLIDER_LEFT -> LocaleController.getString("CP_ZoomSliderPosition_Left", R.string.CP_ZoomSliderPosition_Left)
            else -> LocaleController.getString("Disable", R.string.Disable)
        }
    }

    @JvmStatic
    fun getExposureSliderPosition(): String { // CameraPreferences.java:\Exposure slider
        return when (CherrygramConfig.exposureSlider) {
//            CherrygramConfig.EXPOSURE_SLIDER_BOTTOM -> LocaleController.getString("CP_ZoomSliderPosition_Bottom", R.string.CP_ZoomSliderPosition_Bottom)
            CherrygramConfig.EXPOSURE_SLIDER_RIGHT -> LocaleController.getString("CP_ZoomSliderPosition_Right", R.string.CP_ZoomSliderPosition_Right)
//            CherrygramConfig.EXPOSURE_SLIDER_LEFT -> LocaleController.getString("CP_ZoomSliderPosition_Left", R.string.CP_ZoomSliderPosition_Left)
            else -> LocaleController.getString("Disable", R.string.Disable)
        }
    }


    @JvmStatic
    fun getCameraCaptureTypeFront(): String { // CameraPreferences.java:\Camera capture type
        return when (CherrygramConfig.captureTypeFront) {
            CherrygramConfig.CaptureType_ImageCapture -> "ImageCapture"
            else -> "VideoCapture"
        }
    }

    @JvmStatic
    fun getCameraCaptureTypeBack(): String { // CameraPreferences.java:\Camera capture type
        return when (CherrygramConfig.captureTypeBack) {
            CherrygramConfig.CaptureType_ImageCapture -> "ImageCapture"
            else -> "VideoCapture"
        }
    }
    /**Camera finish**/

    /**Chats start**/
    @JvmStatic
    fun getLeftButtonText(): String {
        return when (CherrygramConfig.leftBottomButton) {
            CherrygramConfig.LEFT_BUTTON_REPLY -> LocaleController.getString("Reply", R.string.Reply)
            CherrygramConfig.LEFT_BUTTON_SAVE_MESSAGE -> LocaleController.getString("CG_ToSaved", R.string.CG_ToSaved)
            CherrygramConfig.LEFT_BUTTON_DIRECT_SHARE -> LocaleController.getString("DirectShare", R.string.DirectShare)
            else -> AndroidUtilities.capitalize(LocaleController.getString("CG_Without_Authorship", R.string.CG_Without_Authorship))
        }
    }

    @JvmStatic
    fun getLeftButtonDrawable(): Int {
        return when (CherrygramConfig.leftBottomButton) {
            CherrygramConfig.LEFT_BUTTON_REPLY -> R.drawable.input_reply
            CherrygramConfig.LEFT_BUTTON_SAVE_MESSAGE -> R.drawable.msg_saved
            CherrygramConfig.LEFT_BUTTON_DIRECT_SHARE -> R.drawable.msg_share
            else -> R.drawable.input_reply
        }
    }

    @JvmStatic
    fun getReplyIconDrawable(): Int {
        return when (CherrygramConfig.messageSlideAction) {
            CherrygramConfig.MESSAGE_SLIDE_ACTION_SAVE -> R.drawable.msg_saved_filled_solar
            CherrygramConfig.MESSAGE_SLIDE_ACTION_DIRECT_SHARE -> R.drawable.msg_share_filled
            CherrygramConfig.MESSAGE_SLIDE_ACTION_TRANSLATE -> R.drawable.msg_translate_filled_solar
            else -> R.drawable.filled_button_reply
        }
    }
    /**Chats finish**/

    /**Profile activity start**/
    fun getDCGeo(dcId: Int): String {
        return when (dcId) {
            1, 3 -> "USA (Miami)"
            2, 4 -> "NLD (Amsterdam)"
            5 -> "SGP (Singapore)"
            else -> "UNK (Unknown)"
        }
    }

    fun getDCName(dc: Int): String {
        return when (dc) {
            1 -> "Pluto"
            2 -> "Venus"
            3 -> "Aurora"
            4 -> "Vesta"
            5 -> "Flora"
            else -> LocaleController.getString("NumberUnknown", R.string.NumberUnknown)
        }
    }
    /**Profile activity finish**/

    /**Misc start**/
    @JvmStatic
    fun getProperNotificationIcon(): Int { // App notification icon
        return if (CherrygramConfig.oldNotificationIcon) {
            R.drawable.notification
        } else {
            return if (LauncherIconController.isEnabled(LauncherIconController.LauncherIcon.DARK_CHERRY_BRA)
                || LauncherIconController.isEnabled(LauncherIconController.LauncherIcon.WHITE_CHERRY_BRA)
                || LauncherIconController.isEnabled(LauncherIconController.LauncherIcon.VIOLET_SUNSET_CHERRY_BRA)
            )
                R.drawable.cg_notification_bra else R.drawable.cg_notification
        }
    }

    @JvmStatic
    fun getResidentNotificationIcon(): Int {
        return if (CherrygramConfig.oldNotificationIcon) R.drawable.cg_notification else R.drawable.notification
    }

    @JvmStatic
    fun getDownloadSpeedBoostText(): String { // ExperimentalPreferences.java:\Download speed boost
        return when (CherrygramConfig.downloadSpeedBoost) {
            CherrygramConfig.BOOST_NONE -> LocaleController.getString("EP_DownloadSpeedBoostNone", R.string.EP_DownloadSpeedBoostNone)
            CherrygramConfig.BOOST_AVERAGE -> LocaleController.getString("EP_DownloadSpeedBoostAverage", R.string.EP_DownloadSpeedBoostAverage)
            else -> LocaleController.getString("EP_DownloadSpeedBoostExtreme", R.string.EP_DownloadSpeedBoostExtreme)
        }
    }

    @JvmStatic
    fun getShowDcIdText(): String { // MessagesAndProfilesPreferencesEntry.java:\Show dc id
        return when (CherrygramConfig.showIDDC) {
            CherrygramConfig.ID_ONLY -> "ID"
            CherrygramConfig.ID_DC -> "ID + DC"
            else -> LocaleController.getString("Disable", R.string.Disable)
        }
    }

    fun createDateAndTime(date: Long): String {
        var dateAndTime = date
        try {
            dateAndTime *= 1000
            val rightNow = Calendar.getInstance()
            rightNow.timeInMillis = dateAndTime
            return String.format("%1\$s | %2\$s", LocaleController.getInstance().formatterYear.format(
                Date(dateAndTime)
            ),
                LocaleController.getInstance().formatterDay.format(Date(dateAndTime))
            )
        } catch (ignore: Exception) { }
        return "LOC_ERR"
    }

    fun createDateAndTimeForJSON(date: Long): String {
        var dateAndTime = date
        try {
            dateAndTime *= 1000
            val rightNow = Calendar.getInstance()
            rightNow.timeInMillis = dateAndTime
            return String.format("%1\$s | %2\$s", LocaleController.getInstance().formatterYear.format(
                Date(dateAndTime)
            ),
                LocaleController.getInstance().formatterDayWithSeconds.format(Date(dateAndTime))
            )
        } catch (ignore: Exception) { }
        return "LOC_ERR"
    }

    fun getUrlNoUnderlineText(charSequence: CharSequence): CharSequence {
        val spannable: Spannable = SpannableString(charSequence)
        val spans = spannable.getSpans(0, charSequence.length, URLSpan::class.java)
        for (urlSpan in spans) {
            var span = urlSpan
            val start = spannable.getSpanStart(span)
            val end = spannable.getSpanEnd(span)
            spannable.removeSpan(span)
            span = object : URLSpanNoUnderline(span.url) {
            }
            spannable.setSpan(span, start, end, 0)
        }
        return spannable
    }
    /**Misc finish**/
}