package uz.unnarsx.cherrygram.prefviews

import android.content.Context
import org.telegram.ui.ActionBar.BottomSheet

open class OnceBottomSheet(context: Context?, needFocus: Boolean) :
    BottomSheet(context, needFocus) {
    override fun show() {
        if (shown) {
            return
        }
        shown = true
        super.show()
    }

    companion object {
        private var shown = false
    }
}