package uz.unnarsx.cherrygram.preferences.helpers

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
import org.telegram.messenger.MessagesController
import org.telegram.messenger.R
import org.telegram.messenger.UserConfig
import org.telegram.messenger.UserObject
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Components.EditTextBoldCursor
import org.telegram.ui.Components.LayoutHelper

object TextFieldAlert {

    @JvmStatic
    fun createFieldAlertForAppName(
        context: Context,
        title: String,
        defaultValue: String,
        finish: (String) -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)

        val textLayout = LinearLayout(context)
        textLayout.orientation = LinearLayout.HORIZONTAL

        val editText = EditTextBoldCursor(context)
        editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
        editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack))
        editText.background = Theme.createEditTextDrawable(context, true)
        editText.isSingleLine = true
        editText.isFocusable = true
        editText.imeOptions = EditorInfo.IME_ACTION_DONE
        editText.setText(defaultValue)
        editText.requestFocus()

        val padding = AndroidUtilities.dp(0f)
        editText.setPadding(padding, 0, padding, 0)

        val user = UserConfig.getInstance(UserConfig.selectedAccount).currentUser

        textLayout.addView(editText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 36))
        builder.setView(textLayout)
        builder.setNeutralButton(LocaleController.getString("FirstNameSmall", R.string.FirstNameSmall)) { _: DialogInterface?, _: Int ->
            finish(UserObject.getFirstName(user).toString().trim())
        }
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK)) { _: DialogInterface?, _: Int ->
            finish(editText.text.toString().trim())
        }
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null)
        builder.show().setOnShowListener {
            editText.requestFocus()
            AndroidUtilities.showKeyboard(editText)
        }

        val layoutParams = editText.layoutParams as ViewGroup.MarginLayoutParams
        if (layoutParams is FrameLayout.LayoutParams) {
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL
        }
        layoutParams.leftMargin = AndroidUtilities.dp(24f)
        layoutParams.rightMargin = layoutParams.leftMargin
        editText.layoutParams = layoutParams
    }

    @JvmStatic
    fun createFieldAlert(
        context: Context,
        title: String,
        currentValue: String,
        finish: (String) -> Unit) {

        val preferences = MessagesController.getMainSettings(UserConfig.selectedAccount)

        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)

        val textLayout = LinearLayout(context)
        textLayout.orientation = LinearLayout.HORIZONTAL

        val editText = EditTextBoldCursor(context)
        editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
        editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack))
        editText.background = Theme.createEditTextDrawable(context, true)
        editText.isSingleLine = true
        editText.isFocusable = true
        editText.imeOptions = EditorInfo.IME_ACTION_DONE
        editText.setText(preferences.getString(currentValue, currentValue))
        editText.requestFocus()

        val padding = AndroidUtilities.dp(0f)
        editText.setPadding(padding, 0, padding, 0)

        textLayout.addView(editText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 36))
        builder.setView(textLayout)
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK)) { _: DialogInterface?, _: Int ->
            finish(removeNonNumericChars(editText.text.toString().trim()))
        }
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null)
        builder.show().setOnShowListener {
            editText.requestFocus()
            AndroidUtilities.showKeyboard(editText)
        }

        val layoutParams = editText.layoutParams as ViewGroup.MarginLayoutParams
        if (layoutParams is FrameLayout.LayoutParams) {
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL
        }
        layoutParams.leftMargin = AndroidUtilities.dp(24f)
        layoutParams.rightMargin = layoutParams.leftMargin
        editText.layoutParams = layoutParams
    }

    private fun removeNonNumericChars(input: String): String {
        return input.replace(Regex("[^0-9-]"), "")
    }
}