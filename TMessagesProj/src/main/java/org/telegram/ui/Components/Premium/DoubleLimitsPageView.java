package org.telegram.ui.Components.Premium;

import android.content.Context;
import android.graphics.Canvas;

import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.Theme;

import uz.unnarsx.cherrygram.CherrygramConfig;

public class DoubleLimitsPageView extends BaseListPageView {

    DoubledLimitsBottomSheet.Adapter adapter;

    public DoubleLimitsPageView(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context, resourcesProvider);
    }

    @Override
    public RecyclerView.Adapter createAdapter() {
        adapter = new DoubledLimitsBottomSheet.Adapter(UserConfig.selectedAccount, true, resourcesProvider);
        adapter.containerView = this;
        return adapter;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        adapter.measureGradient(getContext(), getMeasuredWidth(), getMeasuredHeight());
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (!CherrygramConfig.INSTANCE.getDisableDividers()) canvas.drawLine(0, getMeasuredHeight() - 1, getMeasuredWidth(), getMeasuredHeight() - 1, Theme.dividerPaint);
    }

    @Override
    public void setOffset(float translationX) {
        float progress = Math.abs(translationX / getMeasuredWidth());
        if (progress == 1f) {
            if (recyclerListView.findViewHolderForAdapterPosition(0) == null || recyclerListView.findViewHolderForAdapterPosition(0).itemView.getTop() != recyclerListView.getPaddingTop()) {
                recyclerListView.scrollToPosition(0);
            }
        }

    }

    public void setTopOffset(int topOffset) {
        recyclerListView.setPadding(0, topOffset, 0, 0);
    }
}
