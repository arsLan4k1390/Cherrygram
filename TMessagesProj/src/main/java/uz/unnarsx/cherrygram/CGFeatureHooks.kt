package uz.unnarsx.cherrygram

import android.content.Context
import android.content.DialogInterface
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.LinearLayout
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.ActionBarPopupWindow
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Components.EditTextBoldCursor
import org.telegram.ui.Components.LayoutHelper
import org.telegram.ui.Components.ShareAlert
import uz.unnarsx.cherrygram.ui.dialogs.ShareAlertExtraUI

// I've created this so CG features can be injected in a source file with 1 line only (maybe)
// Because manual editing of drklo's sources harms your mental health.
object CGFeatureHooks {
    @JvmStatic
    fun getProperNotificationIcon(): Int {
        return if (CherrygramConfig.oldNotificationIcon) R.drawable.notification else R.drawable.cg_notification
    }

    @JvmStatic
    fun switchNoAuthor(b: Boolean) {
        // ...
        CherrygramConfig.noAuthorship = b
    }

    @JvmStatic
    fun setFlashLight(b: Boolean) {
        // ...
        CherrygramConfig.whiteBackground = b
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

}