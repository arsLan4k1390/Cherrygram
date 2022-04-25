package uz.unnarsx.cherrygram.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.telegram.messenger.ApplicationLoader
import org.telegram.ui.ActionBar.AlertDialog

object UIUtil {

    @JvmStatic
    fun runOnUIThread(runnable: Runnable) = ApplicationLoader.applicationHandler.post(runnable)

    fun runOnUIThread(runnable: () -> Unit) = ApplicationLoader.applicationHandler.post(runnable)

    @JvmStatic
    @JvmOverloads
    fun runOnIoDispatcher(runnable: Runnable, delay: Long = 0) {

        GlobalScope.launch(Dispatchers.IO) {

            delay(delay)

            runnable.run()

        }

    }

    fun runOnIoDispatcher(runnable: suspend () -> Unit) {

        GlobalScope.launch(Dispatchers.IO) {

            runnable()

        }

    }
}
fun AlertDialog.uUpdate(message: String) = UIUtil.runOnUIThread { setMessage(message) }
fun AlertDialog.uDismiss() = UIUtil.runOnUIThread { dismiss() }