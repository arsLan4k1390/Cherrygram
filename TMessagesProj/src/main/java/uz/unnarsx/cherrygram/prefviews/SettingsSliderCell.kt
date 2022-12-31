package uz.unnarsx.cherrygram.prefviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextPaint
import android.view.Gravity
import android.widget.FrameLayout
import org.telegram.messenger.AndroidUtilities
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Components.LayoutHelper
import org.telegram.ui.Components.SeekBarView
import org.telegram.ui.Components.SeekBarView.SeekBarViewDelegate
import uz.unnarsx.cherrygram.tgkit.preference.types.TGKitSliderPreference.TGSLContract
import kotlin.math.roundToInt

class SettingsSliderCell(context: Context?) : FrameLayout(context!!) {

    private val sizeBar: SeekBarView
    private val textPaint: TextPaint
    private var contract: TGSLContract? = null
    private var startRadius = 0
    private var endRadius = 0

    fun setContract(contract: TGSLContract): SettingsSliderCell {
        this.contract = contract
        startRadius = contract.min
        endRadius = contract.max
        return this
    }

    override fun onDraw(canvas: Canvas) {
        textPaint.color = Theme.getColor(Theme.key_windowBackgroundWhiteValueText)
        canvas.drawText(
            "" + contract!!.preferenceValue,
            (measuredWidth - AndroidUtilities.dp(39f)).toFloat(),
            AndroidUtilities.dp(28f).toFloat(),
            textPaint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), heightMeasureSpec
        )
        sizeBar.progress = (contract!!.preferenceValue - startRadius) / (endRadius - startRadius).toFloat()
    }

    override fun invalidate() {
        super.invalidate()
        sizeBar.invalidate()
    }

    init {
        setWillNotDraw(false)
        textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        textPaint.textSize = AndroidUtilities.dp(16f).toFloat()
        sizeBar = SeekBarView(context)
        sizeBar.setReportChanges(true)
        sizeBar.setDelegate(object : SeekBarViewDelegate {
            override fun onSeekBarDrag(stop: Boolean, progress: Float) {
                contract!!.setValue((startRadius + (endRadius - startRadius) * progress).roundToInt())
                requestLayout()
            }

            override fun onSeekBarPressed(pressed: Boolean) {}
        })
        addView(sizeBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 38f, Gravity.START or Gravity.TOP, 5f, 5f, 39f, 0f))
    }
}