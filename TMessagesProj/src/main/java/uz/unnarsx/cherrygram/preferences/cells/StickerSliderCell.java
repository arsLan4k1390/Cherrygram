/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.preferences.cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.view.Gravity;
import android.widget.FrameLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.SeekBarView;

import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitSliderPreference;

public class StickerSliderCell extends FrameLayout {
    private final SeekBarView sizeBar;
    private final TextPaint textPaint;
    private TGKitSliderPreference.TGSLContract contract;
    private int startRadius;
    private int endRadius;

    public StickerSliderCell(Context context) {
        super(context);

        setWillNotDraw(false);

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(AndroidUtilities.dp(16));

        sizeBar = new SeekBarView(context);
        sizeBar.setReportChanges(true);
        sizeBar.setDelegate(new SeekBarView.SeekBarViewDelegate() {
            @Override
            public void onSeekBarDrag(boolean stop, float progress) {
                contract.setValue(Math.round(startRadius + (endRadius - startRadius) * progress));
                requestLayout();
            }

            @Override
            public void onSeekBarPressed(boolean pressed) {

            }
        });
        addView(sizeBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 38, Gravity.START | Gravity.TOP, 5, 5, 39, 0));
    }

    public StickerSliderCell setContract(TGKitSliderPreference.TGSLContract contract) {
        this.contract = contract;
        this.startRadius = contract.getMin();
        this.endRadius = contract.getMax();
        return this;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        textPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteValueText));
        canvas.drawText("" + contract.getPreferenceValue(), getMeasuredWidth() - AndroidUtilities.dp(39), AndroidUtilities.dp(28), textPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), heightMeasureSpec);
        sizeBar.setProgress((contract.getPreferenceValue() - startRadius) / (float) (endRadius - startRadius));
    }

    @Override
    public void invalidate() {
        super.invalidate();
        sizeBar.invalidate();
    }
}