package org.telegram.ui.Cells;

import org.telegram.messenger.FileLog;
import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeBind;
import org.telegram.ui.BadWayToMakeButtonRound;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ScaleStateListAnimator;

@SuppressWarnings("FieldCanBeLocal")
public class LocationDirectionCell extends FrameLayout implements Theme.Colorable {

    private final Theme.ResourcesProvider resourcesProvider;
    private SimpleTextView buttonTextView;
    private FrameLayout frameLayout;

    public LocationDirectionCell(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.resourcesProvider = resourcesProvider;

        frameLayout = new FrameLayout(context);
        frameLayout.setBackground(Theme.AdaptiveRipple.filledRect(getThemedColor(Theme.key_featuredStickers_addButton), 8));
        addView(frameLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.LEFT | Gravity.TOP, 16, 10, 16, 0));

        buttonTextView = new SimpleTextView(context);
        buttonTextView.setPadding(AndroidUtilities.dp(34), 0, AndroidUtilities.dp(34), 0);
        buttonTextView.setGravity(Gravity.CENTER);
        buttonTextView.setDrawablePadding(AndroidUtilities.dp(8));
        ThemeBind.textColor(buttonTextView, Theme.key_featuredStickers_buttonText, resourcesProvider);
        buttonTextView.setTextSize(14);
        buttonTextView.setText(LocaleController.getString(R.string.Directions));
        buttonTextView.setLeftDrawable(R.drawable.filled_directions);
        buttonTextView.setTypeface(AndroidUtilities.bold());
        frameLayout.addView(buttonTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        BadWayToMakeButtonRound.round(frameLayout);
        ScaleStateListAnimator.apply(frameLayout, 0.02f, 1.2f);

        cgAddCoordinatesButton(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(73), MeasureSpec.EXACTLY));
    }

    public void setOnButtonClick(OnClickListener onButtonClick) {
        frameLayout.setOnClickListener(onButtonClick);
    }

    private int getThemedColor(int key) {
        return Theme.getColor(key, resourcesProvider);
    }

    private int lastButtonColor;
    private boolean lastButtonColorDark;

    @Override
    public void updateColors() {
        try {
            final int color = getThemedColor(Theme.key_featuredStickers_addButton);
            if (frameLayout != null && (color != lastButtonColor || lastButtonColorDark != Theme.isCurrentThemeDark())) {
                lastButtonColorDark = Theme.isCurrentThemeDark(); // the ripple shade depends on dark / light
                lastButtonColor = color;
                // recolor in place (content + adaptive ripple) instead of a new drawable per frame
                Theme.setSelectorDrawableColor(frameLayout.getBackground(), color, false);
                Theme.setSelectorDrawableColor(frameLayout.getBackground(), Theme.AdaptiveRipple.calcRippleColor(color), true);
                frameLayout.invalidate();
                cgUpdateColors(color);
            }
            invalidate();
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    /** Cherrygram start */
    public ButtonsBox buttonsBox;
    private SimpleTextView buttonTextView2;
    private FrameLayout frameLayout2;

    static class ButtonsBox extends FrameLayout {
        public ButtonsBox(Context context) {
            super(context);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            // two buttons side by side with a gap of 8dp between them
            final int childWidth = (MeasureSpec.getSize(widthMeasureSpec) - AndroidUtilities.dp(8)) / 2;
            for (int i = 0; i < getChildCount(); i++) {
                getChildAt(i).measure(
                        MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(48), MeasureSpec.EXACTLY)
                );
            }
        }
    }

    private void cgAddCoordinatesButton(Context context) {
        removeView(frameLayout);
        buttonsBox = new ButtonsBox(context);
        addView(buttonsBox, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.LEFT | Gravity.TOP, 16, 10, 16, 0));
        buttonsBox.addView(frameLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT));

        frameLayout2 = new FrameLayout(context);
        frameLayout2.setBackground(Theme.AdaptiveRipple.filledRect(getThemedColor(Theme.key_featuredStickers_addButton), 8));
        buttonsBox.addView(frameLayout2, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.RIGHT));

        buttonTextView2 = new SimpleTextView(context);
        buttonTextView2.setPadding(AndroidUtilities.dp(34), 0, AndroidUtilities.dp(34), 0);
        buttonTextView2.setGravity(Gravity.CENTER);
        buttonTextView2.setDrawablePadding(AndroidUtilities.dp(8));
        ThemeBind.textColor(buttonTextView2, Theme.key_featuredStickers_buttonText, resourcesProvider);
        buttonTextView2.setTextSize(14);
        buttonTextView2.setText(LocaleController.getString(R.string.CG_Copy_Coordinates));
        buttonTextView2.setLeftDrawable(R.drawable.msg_copy_filled);
        buttonTextView2.setTypeface(AndroidUtilities.bold());
        frameLayout2.addView(buttonTextView2, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        BadWayToMakeButtonRound.round(frameLayout2);
        ScaleStateListAnimator.apply(frameLayout2, 0.02f, 1.2f);
    }

    public void setOnCoordinatesButtonClick(OnClickListener onButtonClick) {
        frameLayout2.setOnClickListener(onButtonClick);
    }

    private void cgUpdateColors(int color) {
        Theme.setSelectorDrawableColor(frameLayout2.getBackground(), color, false);
        Theme.setSelectorDrawableColor(frameLayout2.getBackground(), Theme.AdaptiveRipple.calcRippleColor(color), true);
        frameLayout2.invalidate();
    }
    /** Cherrygram finish */

}