/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

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
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.MessagesController
import org.telegram.messenger.R
import org.telegram.messenger.UserConfig
import org.telegram.messenger.UserObject
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Components.EditTextBoldCursor
import org.telegram.ui.Components.LayoutHelper

object TextFieldAlert {

    fun createFieldAlertForAppName(
        context: Context?,
        title: String,
        defaultValue: String,
        finish: (String) -> Unit
    ) {
        if (context == null) return

        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)

        val textLayout = LinearLayout(context)
        textLayout.orientation = LinearLayout.HORIZONTAL

        val editText = EditTextBoldCursor(context)
        editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
        editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack))
        editText.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader))
        editText.setCursorSize(AndroidUtilities.dp(20f))
        editText.background = Theme.createEditTextDrawable(context, true)
        editText.isSingleLine = true
        editText.isFocusable = true
        editText.imeOptions = EditorInfo.IME_ACTION_DONE
        editText.hint = getString(R.string.CG_AppName)
        editText.setText(defaultValue)
        editText.requestFocus()

        val padding = AndroidUtilities.dp(0f)
        editText.setPadding(padding, 0, padding, 0)

        val user = UserConfig.getInstance(UserConfig.selectedAccount).currentUser

        textLayout.addView(editText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 36))
        builder.setView(textLayout)
        builder.setNeutralButton(getString(R.string.FirstNameSmall)) { _: DialogInterface?, _: Int ->
            finish(UserObject.getFirstName(user).toString().trim())
        }
        builder.setPositiveButton(getString(R.string.Save)) { _: DialogInterface?, _: Int ->
            AndroidUtilities.hideKeyboard(editText)
            finish(editText.text.toString().trim())
        }
        builder.setNegativeButton(getString(R.string.Cancel)) { _: AlertDialog?, _: Int -> AndroidUtilities.hideKeyboard(editText) }
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
        layoutParams.height = AndroidUtilities.dp(36f)
        layoutParams.bottomMargin = AndroidUtilities.dp(15f)
        editText.layoutParams = layoutParams
    }

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
        editText.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader))
        editText.setCursorSize(AndroidUtilities.dp(20f))
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
        builder.setPositiveButton(getString(R.string.OK)) { _: DialogInterface?, _: Int ->
            AndroidUtilities.hideKeyboard(editText)
            finish(removeNonNumericChars(editText.text.toString().trim(), true))
        }
        builder.setNegativeButton(getString(R.string.Cancel)) { _: AlertDialog?, _: Int -> AndroidUtilities.hideKeyboard(editText) }
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
        layoutParams.height = AndroidUtilities.dp(36f)
        layoutParams.bottomMargin = AndroidUtilities.dp(15f)
        editText.layoutParams = layoutParams
    }

    /*fun createFieldAlertForGemini(
        context: Context,
        title: String,
        defaultValue: String,
        finish: (String) -> Unit
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)

        val textLayout = LinearLayout(context)
        textLayout.orientation = LinearLayout.HORIZONTAL

        val editText = EditTextBoldCursor(context)
        editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
        editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack))
        editText.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader))
        editText.setCursorSize(AndroidUtilities.dp(20f))
        editText.background = Theme.createEditTextDrawable(context, true)
        editText.isSingleLine = true
        editText.isFocusable = true
        editText.imeOptions = EditorInfo.IME_ACTION_DONE
        editText.setText(defaultValue)
        editText.requestFocus()

        val padding = AndroidUtilities.dp(0f)
        editText.setPadding(padding, 0, padding, 0)

        textLayout.addView(editText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 36))
        builder.setView(textLayout)

        builder.setPositiveButton(getString(R.string.OK)) { _: DialogInterface?, _: Int ->
            AndroidUtilities.hideKeyboard(editText)
            finish(editText.text.toString().trim())
        }
        builder.setNegativeButton(getString(R.string.Cancel)) { _: AlertDialog?, _: Int -> AndroidUtilities.hideKeyboard(editText) }
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
        layoutParams.height = AndroidUtilities.dp(36f)
        layoutParams.bottomMargin = AndroidUtilities.dp(15f)
        editText.layoutParams = layoutParams
    }*/

    fun removeNonNumericChars(input: String, allowMinus: Boolean): String {
        return if (allowMinus) {
            input.replace(Regex("[^0-9-]"), "")
        } else {
            input.replace(Regex("[^0-9]"), "")
        }
    }
}