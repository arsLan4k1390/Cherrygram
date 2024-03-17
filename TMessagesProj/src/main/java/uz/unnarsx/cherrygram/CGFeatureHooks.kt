package uz.unnarsx.cherrygram

import android.os.Build
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.widget.FrameLayout
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.ActionBarPopupWindow
import org.telegram.ui.Components.ShareAlert
import uz.unnarsx.cherrygram.extras.CherrygramExtras
import uz.unnarsx.cherrygram.ui.dialogs.ShareAlertExtraUI

// I've created this so CG features can be injected in a source file with 1 line only (maybe)
// Because manual editing of drklo's sources harms your mental health.
object CGFeatureHooks {

    @JvmStatic
    fun setFlashLight(b: Boolean) {
        // ...
        CherrygramConfig.whiteBackground = b
    }

    @JvmStatic
    fun switchNoAuthor(b: Boolean) {
        // ...
        CherrygramConfig.noAuthorship = b
    }

    private var currentPopup: ActionBarPopupWindow? = null
    @JvmStatic
    fun showForwardMenu(sa: ShareAlert, field: FrameLayout) {
        currentPopup = ShareAlertExtraUI.createPopupWindow(sa.container, field, sa.context, listOf(
            ShareAlertExtraUI.PopupItem(
                    if (CherrygramConfig.forwardNoAuthorship)
                        LocaleController.getString("CG_FwdMenu_DisableNoForward", R.string.CG_FwdMenu_DisableNoForward)
                    else LocaleController.getString("CG_FwdMenu_EnableNoForward", R.string.CG_FwdMenu_EnableNoForward),
                    R.drawable.msg_forward
            ) {
                // Toggle!
                CherrygramConfig.forwardNoAuthorship = !CherrygramConfig.forwardNoAuthorship
                currentPopup?.dismiss()
                currentPopup = null
            },
            ShareAlertExtraUI.PopupItem(
                if (CherrygramConfig.forwardWithoutCaptions)
                    LocaleController.getString("CG_FwdMenu_EnableCaptions", R.string.CG_FwdMenu_EnableCaptions)
                else LocaleController.getString("CG_FwdMenu_DisableCaptions", R.string.CG_FwdMenu_DisableCaptions),
                R.drawable.msg_edit
            ) {
                // Toggle!
                CherrygramConfig.forwardWithoutCaptions = !CherrygramConfig.forwardWithoutCaptions
                currentPopup?.dismiss()
                currentPopup = null
            },
            ShareAlertExtraUI.PopupItem(
                if (CherrygramConfig.forwardNotify)
                    LocaleController.getString("CG_FwdMenu_NoNotify", R.string.CG_FwdMenu_NoNotify)
                else LocaleController.getString("CG_FwdMenu_Notify", R.string.CG_FwdMenu_Notify),
                R.drawable.input_notify_on
            ) {
                // Toggle!
                CherrygramConfig.forwardNotify = !CherrygramConfig.forwardNotify
                currentPopup?.dismiss()
                currentPopup = null
            },
        ))
    }

    @JvmStatic
    fun getProperNotificationIcon(): Int { //App notification icon
        return if (CherrygramConfig.oldNotificationIcon) R.drawable.notification else R.drawable.cg_notification
    }

    @JvmStatic
    fun getLeftButtonText(): String { //ChatActivity.java:\Left button action
        return when (CherrygramConfig.leftBottomButton) {
            CherrygramConfig.LEFT_BUTTON_FORWARD_WO_AUTHORSHIP -> LocaleController.getString("CG_Without_Authorship", R.string.CG_Without_Authorship)
            CherrygramConfig.LEFT_BUTTON_DIRECT_SHARE -> LocaleController.getString("DirectShare", R.string.DirectShare)
            else -> LocaleController.getString("Reply", R.string.Reply)
        }
    }

    @JvmStatic
    fun getCameraAdvise(): CharSequence { //CameraPreferences.java:\CameraX advise
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
        return CherrygramExtras.getUrlNoUnderlineText(htmlParsed)
    }

    @JvmStatic
    fun getCameraAspectRatio(): String { //CameraPreferences.java:\Camera aspect ratio
        return when (CherrygramConfig.cameraAspectRatio) {
            CherrygramConfig.Camera1to1 -> "1:1"
            CherrygramConfig.Camera4to3 -> "4:3"
            CherrygramConfig.Camera16to9 -> "16:9"
            else -> LocaleController.getString("Default", R.string.Default)
        }
    }

    @JvmStatic
    fun getCameraName(): String { //Crashlytics.java
        return when (CherrygramConfig.cameraType) {
            CherrygramConfig.TELEGRAM_CAMERA -> LocaleController.getString("CP_CameraTypeDefault", R.string.CP_CameraTypeDefault)
            CherrygramConfig.CAMERA_X -> "CameraX"
            else -> LocaleController.getString("CP_CameraTypeSystem", R.string.CP_CameraTypeSystem)
        }
    }

    @JvmStatic
    fun getDownloadSpeedBoostText(): String { //ExperimentalPreferences.java:\Download speed boost
        return when (CherrygramConfig.downloadSpeedBoost) {
            CherrygramConfig.BOOST_NONE -> LocaleController.getString("EP_DownloadSpeedBoostNone", R.string.EP_DownloadSpeedBoostNone)
            CherrygramConfig.BOOST_AVERAGE -> LocaleController.getString("EP_DownloadSpeedBoostAverage", R.string.EP_DownloadSpeedBoostAverage)
            else -> LocaleController.getString("EP_DownloadSpeedBoostExtreme", R.string.EP_DownloadSpeedBoostExtreme)
        }
    }

    @JvmStatic
    fun getShowDcIdText(): String { //MessagesAndProfilesPreferencesEntry.java:\Show dc id
        return when (CherrygramConfig.showIDDC) {
            CherrygramConfig.ID_ONLY -> "ID"
            CherrygramConfig.ID_DC -> "ID + DC"
            else -> LocaleController.getString("Disable", R.string.Disable)
        }
    }

}