package uz.unnarsx.cherrygram

import android.content.Context
import android.content.DialogInterface
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RemoteViews
import org.telegram.messenger.*
import org.telegram.ui.ActionBar.ActionBarPopupWindow
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Components.EditTextBoldCursor
import org.telegram.ui.Components.LayoutHelper
import org.telegram.ui.Components.ShareAlert

// I've created this so CG features can be injected in a source file with 1 line only (maybe)
// Because manual editing of drklo's sources harms your mental health.
object CGFeatureHooks {
    @JvmStatic
    fun colorFeedWidgetItem(rv: RemoteViews) {
        rv.setTextColor(R.id.feed_widget_item_text, Theme.getColor(Theme.key_windowBackgroundWhiteBlackText))
    }

    @JvmStatic
    fun getProperNotificationIcon(): Int {
        return if (CherrygramConfig.oldNotificationIcon) R.drawable.notification else R.drawable.cg_notification
    }

    @JvmStatic
    fun switchNoAuthor(b: Boolean) {
        // ...
        CherrygramConfig.noAuthorship = b
    }

    private var currentPopup: ActionBarPopupWindow? = null

    @JvmStatic
    fun showForwardMenu(sa: ShareAlert, field: FrameLayout) {
        currentPopup = CGFeatureJavaHooks.createPopupWindow(sa.container, field, sa.context, listOf(
                CGFeatureJavaHooks.PopupItem(
                        if (CherrygramConfig.forwardNoAuthorship) {
                            LocaleController.getString("CG_FwdMenu_DisableNoForward", R.string.CG_FwdMenu_DisableNoForward)
                        } else {
                            LocaleController.getString("CG_FwdMenu_EnableNoForward", R.string.CG_FwdMenu_EnableNoForward)
                        },
                        R.drawable.msg_forward
                ) {
                    // Toggle!
                    CherrygramConfig.forwardNoAuthorship = !CherrygramConfig.forwardNoAuthorship
                    currentPopup?.dismiss()
                    currentPopup = null
                },
            CGFeatureJavaHooks.PopupItem(
                if (CherrygramConfig.forwardWithoutCaptions) {
                    LocaleController.getString("CG_FwdMenu_EnableCaptions", R.string.CG_FwdMenu_EnableCaptions)
                } else {
                    LocaleController.getString("CG_FwdMenu_DisableCaptions", R.string.CG_FwdMenu_DisableCaptions)
                },
                R.drawable.msg_edit
            ) {
                // Toggle!
                CherrygramConfig.forwardWithoutCaptions = !CherrygramConfig.forwardWithoutCaptions
                currentPopup?.dismiss()
                currentPopup = null
            },
            CGFeatureJavaHooks.PopupItem(
                if (CherrygramConfig.forwardNotify) {
                    LocaleController.getString("CG_FwdMenu_NoNotify", R.string.CG_FwdMenu_NoNotify)
                } else {
                    LocaleController.getString("CG_FwdMenu_Notify", R.string.CG_FwdMenu_Notify)
                },
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
    fun createFieldAlert(
        context: Context,
        title: String,
        defaultValue: String,
        finish: (String) -> Unit) {
        val builder = AlertDialog.Builder(context);
        builder.setTitle(title);

        val textLayout = LinearLayout(context)
        textLayout.orientation = LinearLayout.HORIZONTAL

        val editText = EditTextBoldCursor(context);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f);
        editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        editText.background = Theme.createEditTextDrawable(context, true);
        editText.isSingleLine = true;
        editText.isFocusable = true;
        editText.imeOptions = EditorInfo.IME_ACTION_DONE;
        editText.setText(defaultValue);
        editText.requestFocus();

        val padding = AndroidUtilities.dp(0f);
        editText.setPadding(padding, 0, padding, 0);

        textLayout.addView(editText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 36))
        builder.setView(textLayout);
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK)) { _: DialogInterface?, _: Int ->
            finish(editText.text.toString().trim());
        }
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        builder.show().setOnShowListener {
            editText.requestFocus();
            AndroidUtilities.showKeyboard(editText);
        }

        val layoutParams = editText.layoutParams as ViewGroup.MarginLayoutParams;
        if (layoutParams is FrameLayout.LayoutParams) {
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        }
        layoutParams.leftMargin = AndroidUtilities.dp(24f);
        layoutParams.rightMargin = layoutParams.leftMargin;
        editText.layoutParams = layoutParams;
    }

}