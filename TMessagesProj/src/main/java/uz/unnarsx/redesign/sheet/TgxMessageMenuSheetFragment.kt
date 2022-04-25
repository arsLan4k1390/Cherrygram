package uz.unnarsx.redesign.sheet

import uz.unnarsx.redesign.BaseActionedSwipeFragment

class TgxMessageMenuSheetFragment(val act: List<BaseActionedSwipeFragment.Action>, val onClick: (Int) -> Unit) : BaseActionedSheetFragment() {
    override fun getActions(): List<BaseActionedSwipeFragment.Action> {
        return act
    }

    override fun processActionClick(id: String) = onClick.invoke(id.toInt())
}