package uz.unnarsx.cherrygram.preferences.drawer.cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.DrawerLayoutContainer;
import org.telegram.ui.Cells.DrawerProfileCell;

public class DrawerProfilePreviewCell extends DrawerProfileCell {
    public DrawerProfilePreviewCell(Context context) {
        super(context, new DrawerLayoutContainer(context) {
            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                super.onLayout(changed, l, t, r, b);
                setDrawerPosition(getDrawerPosition());
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), AndroidUtilities.dp(148));
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    protected void dispatchSetPressed(boolean pressed) {

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
