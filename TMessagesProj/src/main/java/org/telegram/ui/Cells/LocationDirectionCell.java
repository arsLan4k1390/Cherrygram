package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.BadWayToMakeButtonRound;
import org.telegram.ui.Components.ColoredImageSpan;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ScaleStateListAnimator;

@SuppressWarnings("FieldCanBeLocal")
public class LocationDirectionCell extends FrameLayout {

    private final Theme.ResourcesProvider resourcesProvider;
    public TextView buttonTextView, buttonTextView2;

    public ButtonsBox buttonsBox;
    SpannableStringBuilder spannableStringBuilder;

    static class ButtonsBox extends FrameLayout {

        private Paint paint = new Paint();
        private float[] radii = new float[8];
        private Path path = new Path();

        public ButtonsBox(Context context) {
            super(context);
            setWillNotDraw(false);
            paint.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        }

        private float t;
        public void setT(float t) {
            this.t = t;
            invalidate();
        }

        private void setRadii(float left, float right) {
            radii[0] = radii[1] = radii[6] = radii[7] = left;
            radii[2] = radii[3] = radii[4] = radii[5] = right;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            final float cx = getMeasuredWidth() / 2f;

            path.rewind();
            AndroidUtilities.rectTmp.set(0, 0, cx - AndroidUtilities.lerp(0, AndroidUtilities.dp(4), t), getMeasuredHeight());
            setRadii(AndroidUtilities.dp(8), AndroidUtilities.lerp(0, AndroidUtilities.dp(8), t));
            path.addRoundRect(AndroidUtilities.rectTmp, radii, Path.Direction.CW);
            canvas.drawPath(path, paint);

            path.rewind();
            AndroidUtilities.rectTmp.set(cx + AndroidUtilities.lerp(0, AndroidUtilities.dp(4), t), 0, getMeasuredWidth(), getMeasuredHeight());
            setRadii(AndroidUtilities.lerp(0, AndroidUtilities.dp(8), t), AndroidUtilities.dp(8));
            path.addRoundRect(AndroidUtilities.rectTmp, radii, Path.Direction.CW);
            canvas.drawPath(path, paint);
        }
    }

    public LocationDirectionCell(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.resourcesProvider = resourcesProvider;

        buttonsBox = new ButtonsBox(context);
        addView(buttonsBox, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.LEFT | Gravity.TOP, 16, 10, 16, 0));

        buttonTextView = new TextView(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(
                        MeasureSpec.makeMeasureSpec((MeasureSpec.getSize(widthMeasureSpec) - AndroidUtilities.dp(8)) / 2, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(48), MeasureSpec.EXACTLY)
                );
            }
        };
        buttonTextView.setPadding(AndroidUtilities.dp(34), 0, AndroidUtilities.dp(34), 0);
        buttonTextView.setGravity(Gravity.CENTER);
        buttonTextView.setBackground(Theme.AdaptiveRipple.filledRect(getThemedColor(Theme.key_featuredStickers_addButton), 8));
        buttonTextView.setTextColor(getThemedColor(Theme.key_featuredStickers_buttonText));
        buttonTextView.setTextSize(14);
        spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append("..").setSpan(new ColoredImageSpan(ContextCompat.getDrawable(context, R.drawable.filled_directions)), 0, 1, 0);
        spannableStringBuilder.setSpan(new DialogCell.FixedWidthSpan(AndroidUtilities.dp(8)), 1, 2, 0);
        spannableStringBuilder.append(LocaleController.getString(R.string.Directions));
        spannableStringBuilder.append(".").setSpan(new DialogCell.FixedWidthSpan(AndroidUtilities.dp(5)), spannableStringBuilder.length() - 1, spannableStringBuilder.length(), 0);
        buttonTextView.setText(spannableStringBuilder);
        buttonTextView.setTypeface(AndroidUtilities.bold());
        buttonsBox.addView(buttonTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT));

        buttonTextView2 = new TextView(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(
                        MeasureSpec.makeMeasureSpec((MeasureSpec.getSize(widthMeasureSpec) - AndroidUtilities.dp(8)) / 2, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(48), MeasureSpec.EXACTLY)
                );
            }
        };
        buttonTextView2.setPadding(AndroidUtilities.dp(34), 0, AndroidUtilities.dp(34), 0);
        buttonTextView2.setGravity(Gravity.CENTER);
        buttonTextView2.setBackground(Theme.AdaptiveRipple.filledRect(getThemedColor(Theme.key_featuredStickers_addButton), 8));
        buttonTextView2.setTextColor(getThemedColor(Theme.key_featuredStickers_buttonText));
        buttonTextView2.setTextSize(14);
        spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append("..").setSpan(new ColoredImageSpan(ContextCompat.getDrawable(context, R.drawable.msg_copy_filled)), 0, 1, 0);
        spannableStringBuilder.setSpan(new DialogCell.FixedWidthSpan(AndroidUtilities.dp(8)), 1, 2, 0);
        spannableStringBuilder.append(LocaleController.getString(R.string.CG_Copy_Coordinates));
        spannableStringBuilder.append(".").setSpan(new DialogCell.FixedWidthSpan(AndroidUtilities.dp(5)), spannableStringBuilder.length() - 1, spannableStringBuilder.length(), 0);
        buttonTextView2.setText(spannableStringBuilder);
        buttonTextView2.setTypeface(AndroidUtilities.bold());
        buttonsBox.addView(buttonTextView2, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.RIGHT));

        BadWayToMakeButtonRound.round(buttonsBox);
        ScaleStateListAnimator.apply(buttonsBox, 0.02f, 1.2f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(73), MeasureSpec.EXACTLY));
    }

    public void setOnButtonClick(OnClickListener onButtonClick) {
        buttonTextView.setOnClickListener(onButtonClick);
    }

    public void setOnCoordinatesButtonClick(View.OnClickListener onButtonClick) {
        buttonTextView2.setOnClickListener(onButtonClick);
    }

    private int getThemedColor(int key) {
        return Theme.getColor(key, resourcesProvider);
    }
}
