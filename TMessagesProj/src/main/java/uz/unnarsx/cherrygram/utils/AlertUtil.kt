package uz.unnarsx.cherrygram.utils

import android.content.Context
import android.widget.Toast
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.tgnet.TLRPC
import org.telegram.ui.ActionBar.AlertDialog

object AlertUtil {

    @JvmStatic
    fun showToast(e: Throwable) = showToast(e.message ?: e.javaClass.simpleName)

    @JvmStatic
    fun showToast(e: TLRPC.TL_error?) {
        if (e == null) return
        showToast("${e.code}: ${e.text}")
    }

    @JvmStatic
    fun showToast(text: String) = UIUtil.runOnUIThread(Runnable {
        Toast.makeText(
            ApplicationLoader.applicationContext,
            text.takeIf { it.isNotBlank() }
                ?: "喵 !",
            Toast.LENGTH_LONG
        ).show()
    })

    @JvmOverloads
    @JvmStatic
    fun showProgress(ctx: Context, text: String = LocaleController.getString("Loading", R.string.Loading)): AlertDialog {

        return AlertDialog.Builder(ctx, 1).apply {

            setMessage(text)

        }.create()

    }


}