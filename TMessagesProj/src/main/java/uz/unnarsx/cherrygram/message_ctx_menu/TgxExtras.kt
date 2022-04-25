package uz.unnarsx.cherrygram.message_ctx_menu

import android.app.Activity
import org.telegram.messenger.LocaleController
import org.telegram.messenger.MessageObject
import uz.unnarsx.cherrygram.CherrygramConfig
import uz.unnarsx.redesign.BaseActionedSwipeFragment
import uz.unnarsx.redesign.sheet.TgxMessageMenuSheetFragment
import uz.unnarsx.redesign.slides.TgxMessageMenuFragment

object TgxExtras {
//    @JvmStatic
//    fun createSlideMenu(options: ArrayList<Int>, items: ArrayList<CharSequence>, icons: ArrayList<Int>, parentActivity: Activity, processSelectedOption: (Int) -> Unit): TgxMessageMenuFragment {
//        val list = mutableListOf<BaseActionedSwipeFragment.Action>()
//
//        options.forEachIndexed { index, data ->
//            list.add(BaseActionedSwipeFragment.Action(
//                    id = "$data",
//                    title = "${items[index]}",
//                    icon = icons[index]
//            ))
//        }
//
//        return TgxMessageMenuFragment(list, processSelectedOption)
//    }
//
//    @JvmStatic
//    fun createSheetMenu(options: ArrayList<Int>, items: ArrayList<CharSequence>, icons: ArrayList<Int>, parentActivity: Activity, processSelectedOption: (Int) -> Unit): TgxMessageMenuSheetFragment {
//        val list = mutableListOf<BaseActionedSwipeFragment.Action>()
//
//        options.forEachIndexed { index, data ->
//            list.add(BaseActionedSwipeFragment.Action(
//                    id = "$data",
//                    title = "${items[index]}",
//                    icon = icons[index]
//            ))
//        }
//
//        return TgxMessageMenuSheetFragment(list, processSelectedOption)
//    }

    @JvmStatic
    fun createForwardTimeName(obj: MessageObject, orig: CharSequence): String {
        if (!CherrygramConfig.msgForwardDate) return orig.toString()
        //return orig.toString()
        return "$orig (${LocaleController.formatDate(obj.messageOwner.fwd_from.date.toLong())})"
    }
}