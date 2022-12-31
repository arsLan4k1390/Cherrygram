package uz.unnarsx.cherrygram.preferences.drawer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.text.TextPaint
import android.view.Gravity
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.FrameLayout
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.LocaleController
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Components.LayoutHelper
import org.telegram.ui.Components.SeekBarView
import org.telegram.ui.Components.SeekBarView.SeekBarViewDelegate
import uz.unnarsx.cherrygram.CherrygramConfig
import kotlin.math.roundToInt

open class BlurIntensityCell(context: Context?) : FrameLayout(context!!) {

    private val sizeBar: SeekBarView
    private val startIntensity = 0
    private val endIntensity = 80
    private val textPaint: TextPaint

    override fun onDraw(canvas: Canvas) {
        textPaint.color = Theme.getColor(Theme.key_windowBackgroundWhiteValueText)
        canvas.drawText("" + CherrygramConfig.drawerBlurIntensity, (measuredWidth - AndroidUtilities.dp(39f)).toFloat(), AndroidUtilities.dp(28f).toFloat(), textPaint)
        canvas.drawLine(
            (if (LocaleController.isRTL) 0 else AndroidUtilities.dp(20f).toFloat()) as Float, (measuredHeight - 1).toFloat(),
            (measuredWidth - if (LocaleController.isRTL) AndroidUtilities.dp(20f) else 0).toFloat(),
            (measuredHeight - 1).toFloat(), Theme.dividerPaint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), heightMeasureSpec
        )
        sizeBar.progress = (CherrygramConfig.drawerBlurIntensity - startIntensity) / (endIntensity - startIntensity).toFloat()
    }

    override fun invalidate() {
        super.invalidate()
        sizeBar.invalidate()
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        sizeBar.seekBarAccessibilityDelegate.onInitializeAccessibilityNodeInfoInternal(this, info)
    }

    override fun performAccessibilityAction(action: Int, arguments: Bundle?): Boolean {
        return super.performAccessibilityAction(action, arguments) || sizeBar.seekBarAccessibilityDelegate.performAccessibilityActionInternal(this, action, arguments)
    }

    protected open fun onBlurIntensityChange(percentage: Int, layout: Boolean) {

    }

    init {
        this.setWillNotDraw(false)
        textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        textPaint.textSize = AndroidUtilities.dp(16f).toFloat()
        sizeBar = SeekBarView(context)
        sizeBar.setReportChanges(true)
        sizeBar.setDelegate(object : SeekBarViewDelegate {
            override fun onSeekBarDrag(stop: Boolean, progress: Float) {
                onBlurIntensityChange((startIntensity + (endIntensity - startIntensity) * progress).roundToInt(), false)
            }

            override fun onSeekBarPressed(pressed: Boolean) {

            }

            override fun getContentDescription(): CharSequence {
                return (startIntensity + (endIntensity - startIntensity) * sizeBar.progress).roundToInt().toString()
            }

            override fun getStepsCount(): Int {
                return endIntensity - startIntensity
            }
        })
        sizeBar.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        this.addView(sizeBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 38f, Gravity.LEFT or Gravity.TOP, 5f, 5f, 39f, 0f))
    }
}