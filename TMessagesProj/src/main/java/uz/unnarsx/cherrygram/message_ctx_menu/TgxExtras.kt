package uz.unnarsx.cherrygram.message_ctx_menu

import org.telegram.messenger.LocaleController
import org.telegram.messenger.MessageObject
import uz.unnarsx.cherrygram.CherrygramConfig

object TgxExtras {

    @JvmStatic
    fun createForwardTimeName(obj: MessageObject, orig: CharSequence): String {
        if (!CherrygramConfig.msgForwardDate) return orig.toString()
        //return orig.toString()
        return "$orig (${LocaleController.formatDate(obj.messageOwner.fwd_from.date.toLong())})"
    }
}