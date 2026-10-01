/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.camera;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.SparseArray;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.view.animation.PathInterpolator;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.blur3.BlurredBackgroundDrawableViewFactory;
import org.telegram.ui.Components.blur3.drawable.BlurredBackgroundDrawable;
import org.telegram.ui.Components.blur3.drawable.color.BlurredBackgroundColorProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig;
import uz.unnarsx.cherrygram.helpers.ui.FontHelper;

@SuppressLint("ViewConstructor")
public class CherryZoomSliderView extends View {

    private static final double LOG_2 = Math.log(2.0);
    private static final TimeInterpolator ZOOM_INTERPOLATOR = new PathInterpolator(0.25f, 0.1f, 0.25f, 1f);
    private static final TimeInterpolator MORPH_INTERPOLATOR = new CubicBezierInterpolator(0.4, 0, 0.2, 1);
    private static final PorterDuffXfermode DST_IN_XFERMODE = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);

    private static final int TOGGLE_SIZE_DP_DEFAULT = 44;

    private static final int EXPANDED_BACKGROUND_WIDTH_DP = 290;
    private static final int EXPANDED_RULER_WIDTH_DP = 260;

    private static final int ZOOM_BUTTON_SIZE_DP = 32;
    private static final int ZOOM_BUTTON_ICON_SIZE_DP = 14;
    private static final int ZOOM_BUTTON_GAP_DP = 4;
    private static final float ZOOM_BUTTON_STEP = 0.1f;
    private static final long ZOOM_REPEAT_INITIAL_DELAY = 400L;
    private static final long ZOOM_REPEAT_INTERVAL = 60L;

    private static final long AUTO_COLLAPSE_DELAY = 1500L;
    private static final long ANIMATION_DURATION = 280;

    private static final int MAX_BIND_RETRIES = 25;
    private static final long BIND_RETRY_DELAY = 100L;
    private static final long ZOOM_UPDATE_INTERVAL_MS = 33L;

    private static final float ZOOM_SMOOTH_TAU_MS = 150f;

    private static final FloatPropertyCompat<CherryZoomSliderView> CONTROL_WIDTH = new FloatPropertyCompat<>("controlWidth") {
        @Override
        public float getValue(CherryZoomSliderView view) {
            return view.animatedControlWidth;
        }

        @Override
        public void setValue(CherryZoomSliderView view, float value) {
            view.animatedControlWidth = Math.max(0f, value);
            view.invalidate();
        }
    };

    private static final FloatPropertyCompat<CherryZoomSliderView> SELECTOR_OFFSET = new FloatPropertyCompat<>("selectorOffset") {
        @Override
        public float getValue(CherryZoomSliderView view) {
            return view.animatedSelectorOffset;
        }

        @Override
        public void setValue(CherryZoomSliderView view, float value) {
            view.animatedSelectorOffset = value;
            view.invalidate();
        }
    };

    public interface OnPresetSelectedListener {
        void onPresetSelected(float zoom);
    }

    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint toggleTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Paint selectedToggleTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Paint tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rulerLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Paint edgeFadePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bubbleTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Paint zoomButtonBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Drawable protectionBackgroundDrawable;
    private Drawable minusButtonIcon;
    private Drawable plusButtonIcon;
    private BlurredBackgroundDrawable minusButtonBackgroundDrawable;
    private BlurredBackgroundDrawable plusButtonBackgroundDrawable;
    private BlurredBackgroundDrawableViewFactory liquidGlassFactory;
    private BlurredBackgroundColorProvider liquidGlassColorProvider;

    private final RectF controlBounds = new RectF();
    private final RectF compactBounds = new RectF();
    private final RectF rulerBounds = new RectF();
    private final RectF compactTouchBounds = new RectF();
    private final RectF rulerTouchBounds = new RectF();
    private final RectF selectorBounds = new RectF();
    private final RectF bubbleBounds = new RectF();
    private final RectF minusButtonBounds = new RectF();
    private final RectF plusButtonBounds = new RectF();

    private float toggleCellWidth = dp(TOGGLE_SIZE_DP_DEFAULT);

    private final SparseArray<String> primaryLabels = new SparseArray<>();
    private final int touchSlop;
    private final SpringAnimation widthSpring;
    private final SpringAnimation selectorSpring;

    private float minZoom = 0.5f;
    private float maxZoom = 30f;
    private float zoom = 1f;
    private float[] toggleStops = {0.5f, 1f, 2f, 5f};
    private float[] rulerStops = {0.5f, 1f, 2f, 5f, 10f, 30f};
    private int[] primaryTickIndices = new int[0];
    private int oneXTick = -1;
    private int intervalCount;
    private float tickSpacing;
    private float displayNormalizationFactor = 1f;

    private int protectionBackgroundColor;
    private int primaryColor;
    private int minorTickColor;
    private int secondaryFixedColor;
    private int onSecondaryFixedColor;
    private int unselectedToggleColor;

    private float animatedControlWidth;
    private float animatedSelectorOffset;
    private float expandedProgress;
    private ValueAnimator expandedAnimator;
    private ValueAnimator zoomAnimator;
    private boolean expanded;

    private int selectedToggleIndex;
    private boolean selectedShowsStopValue;

    private boolean dragging;
    private boolean compactGestureDown;
    private boolean dragStartedFromCompact;
    private boolean movedPastSlop;
    private int pressedToggleIndex = -1;
    private float downX;
    private float downY;
    private float lastTouchX;
    private float dragTick;
    private int dragPrimarySegment = Integer.MIN_VALUE;
    private int lastHapticTick = Integer.MIN_VALUE;
    private int stickyTick = -1;
    private float stickyDistance;
    private float stickyFactor;
    private VelocityTracker velocityTracker;

    private final Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            if (!compactGestureDown || movedPastSlop || expanded) return;
            performHapticSafe(HapticFeedbackConstants.CONTEXT_CLICK);
            dragStartedFromCompact = true;
            beginDrag(downX);
            setExpanded(true, true);
        }
    };

    private final Runnable autoCollapseRunnable = new Runnable() {
        @Override
        public void run() {
            if (expanded && !dragging) setExpanded(false, true);
        }
    };

    private long zoomRepeatStartTime;
    private int pressedZoomButton = 0;

    private boolean zoomRepeatFired;

    private final Runnable zoomRepeatRunnable = new Runnable() {
        @Override
        public void run() {
            zoomRepeatFired = true;
            if (pressedZoomButton == -1) {
                stepZoomByButton(-getDynamicZoomStep(), false);
                postDelayed(this, ZOOM_REPEAT_INTERVAL);
            } else if (pressedZoomButton == 1) {
                stepZoomByButton(getDynamicZoomStep(), false);
                postDelayed(this, ZOOM_REPEAT_INTERVAL);
            }
        }
    };

    private OnPresetSelectedListener presetSelectedListener;

    private final Runnable bindRunnable = this::tryBind;
    private final Runnable zoomFlushRunnable = this::flushPendingZoom;

    private CameraXController cameraXController;
    private int bindRetries;
    private float pendingZoom = Float.NaN;
    private float lastAppliedZoom = Float.NaN;
    private long lastZoomAppliedAt;
    private boolean zoomFlushScheduled;
    private boolean baselineSettled;
    private float smoothedZoom = Float.NaN;
    private long lastFrameTime;

    public CherryZoomSliderView(Context context, Theme.ResourcesProvider resourcesProvider, boolean greyColor) {
        super(context);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        tickSpacing = Math.max(1f, Math.round((float) dp(8)));
        widthSpring = createSpring(CONTROL_WIDTH, 1f, 3800f, dp(0.1f));
        selectorSpring = createSpring(SELECTOR_OFFSET, 0.8f, 800f, 1f);
        configurePaints();
        rebuildScale();
        selectedToggleIndex = findToggleSegment(zoom);
        animatedSelectorOffset = getSelectorOffset(selectedToggleIndex);
        animatedControlWidth = getCompactWidth();
        setClickable(true);
        setFocusable(true);
        setColors(resourcesProvider, greyColor);
        setZoomButtonIcons();
    }

    public void setOnPresetSelectedListener(OnPresetSelectedListener listener) {
        this.presetSelectedListener = listener;
    }

    public void setLiquidGlassBackground(
            BlurredBackgroundDrawableViewFactory factory,
            BlurredBackgroundColorProvider colorProvider
    ) {
        liquidGlassFactory = factory;
        liquidGlassColorProvider = colorProvider;

        if (protectionBackgroundDrawable != null) {
            protectionBackgroundDrawable.setCallback(null);
            protectionBackgroundDrawable = null;
        }

        if (factory != null && colorProvider != null) {
            BlurredBackgroundDrawable drawable = factory.create(this, colorProvider);
            if (drawable != null) {
                drawable.setRadius(dp(24f));
                drawable.setCallback(this);
                protectionBackgroundDrawable = drawable;
            }
        }

        if (minusButtonIcon != null || plusButtonIcon != null) {
            createZoomButtonBackgrounds();
        } else {
            invalidate();
        }
    }

    public void setZoomButtonIcons() {
        minusButtonIcon = ContextCompat.getDrawable(getContext(), R.drawable.zoom_minus);
        plusButtonIcon = ContextCompat.getDrawable(getContext(), R.drawable.zoom_plus);

        if (minusButtonIcon != null) {
            minusButtonIcon.setTint(onSecondaryFixedColor);
        }

        if (plusButtonIcon != null) {
            plusButtonIcon.setTint(onSecondaryFixedColor);
        }

        invalidate();
    }

    private void setColors(Theme.ResourcesProvider resourcesProvider, boolean greyColor) {
        if (greyColor) {
            protectionBackgroundColor = ColorUtils.setAlphaComponent(0xFF2C2C2E, 150);
            unselectedToggleColor = 0xFFFFFFFF;
            primaryColor = 0xFFFFFFFF;
            minorTickColor = 0xFF8E8E93;
            secondaryFixedColor = ColorUtils.setAlphaComponent(0xFF48484A, 200);
            onSecondaryFixedColor = 0xFFFFFFFF;
        } else {
            protectionBackgroundColor = Theme.getColor(Theme.key_chat_messagePanelBackground, resourcesProvider);
            unselectedToggleColor = Theme.getColor(Theme.key_chat_messagePanelText, resourcesProvider);
            primaryColor = Theme.getColor(Theme.key_chat_recordedVoiceBackground, resourcesProvider);
            minorTickColor = Theme.getColor(Theme.key_dialogTextGray, resourcesProvider);
            secondaryFixedColor = Theme.getColor(Theme.key_chat_recordedVoiceBackground, resourcesProvider);
            onSecondaryFixedColor = Theme.getColor(Theme.key_chats_actionIcon, resourcesProvider);
        }

        tickPaint.setColor(minorTickColor);
        markerPaint.setColor(primaryColor);
        rulerLabelPaint.setColor(primaryColor);
        selectorPaint.setColor(secondaryFixedColor);
        bubblePaint.setColor(secondaryFixedColor);
        bubbleTextPaint.setColor(onSecondaryFixedColor);
        zoomButtonBackgroundPaint.setColor(protectionBackgroundColor);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private float zoomToTick(float value) {
        final float clamped = clamp(value, minZoom, maxZoom);
        if (clamped <= minZoom) return 0f;
        if (clamped >= maxZoom) return intervalCount;
        if (oneXTick >= 0) {
            if (clamped == displayNormalizationFactor) return oneXTick;
            if (clamped > displayNormalizationFactor) {
                final float progress = (float) (Math.log(clamped / displayNormalizationFactor) / Math.log(maxZoom / displayNormalizationFactor));
                return oneXTick + progress * (intervalCount - oneXTick);
            }
            return (float) (Math.log(clamped / minZoom) / Math.log(displayNormalizationFactor / minZoom)) * oneXTick;
        }
        return (float) (Math.log(clamped / minZoom) / Math.log(maxZoom / minZoom)) * intervalCount;
    }

    private float tickToZoom(float tick) {
        final float clamped = clamp(tick, 0f, intervalCount);
        if (clamped <= 0f) return minZoom;
        if (clamped >= intervalCount) return maxZoom;
        if (oneXTick < 0) {
            return (float) (minZoom * Math.exp(Math.log(maxZoom / minZoom) * (clamped / intervalCount)));
        }
        if (clamped == oneXTick) return displayNormalizationFactor;
        if (clamped <= oneXTick) {
            return (float) (minZoom * Math.exp(Math.log(displayNormalizationFactor / minZoom) * (clamped / oneXTick)));
        }
        return (float) (displayNormalizationFactor * Math.exp(Math.log(maxZoom / displayNormalizationFactor) * ((clamped - oneXTick) / (float) (intervalCount - oneXTick))));
    }

    private void rebuildScale() {
        final float octaves = (float) (Math.log(maxZoom / minZoom) / LOG_2);
        if (minZoom >= displayNormalizationFactor || maxZoom < displayNormalizationFactor) {
            oneXTick = -1;
            intervalCount = Math.max(1, Math.round(octaves * 5));
        } else {
            oneXTick = Math.max(3, Math.round((float) (Math.log(displayNormalizationFactor / minZoom) / LOG_2) * 5));
            intervalCount = oneXTick + (maxZoom > displayNormalizationFactor
                    ? Math.max(1, Math.round((float) (Math.log(maxZoom / displayNormalizationFactor) / LOG_2) * 5))
                    : 0
            );
        }
        primaryLabels.clear();
        final int[] indices = new int[rulerStops.length];
        int count = 0;
        for (float stop : rulerStops) {
            if (stop < minZoom || stop > maxZoom) continue;
            final int tick = Math.round(zoomToTick(stop));
            primaryLabels.put(tick, formatRuler(stop));
            boolean known = false;
            for (int i = 0; i < count; i++) {
                if (indices[i] == tick) {
                    known = true;
                    break;
                }
            }
            if (!known) indices[count++] = tick;
        }
        primaryTickIndices = Arrays.copyOf(indices, count);
        Arrays.sort(primaryTickIndices);
    }

    private int findToggleSegment(float value) {
        if (toggleStops.length == 0) return -1;

        final float epsilon = 0.001f;

        for (int i = 0; i < toggleStops.length; i++) {
            if (Math.abs(value - toggleStops[i]) <= epsilon) {
                return i;
            }
        }

        int segment = 0;
        for (int i = 1; i < toggleStops.length && value > toggleStops[i]; i++) {
            segment = i;
        }

        return segment;
    }

    private int findToggleIndexAt(float x) {
        if (x < compactBounds.left || x > compactBounds.right || toggleStops.length == 0) return -1;
        final int index = (int) ((x - compactBounds.left) / toggleCellWidth);
        return Math.max(0, Math.min(toggleStops.length - 1, index));
    }

    private int findPrimarySegment(float tick, float delta) {
        if (primaryTickIndices.length == 0) return Integer.MIN_VALUE;
        final float first = primaryTickIndices[0];
        if (tick < first || (tick == first && delta >= 0f)) return -1;
        int i = 0;
        while (i < primaryTickIndices.length - 1) {
            final float next = primaryTickIndices[i + 1];
            if (delta < 0f ? tick < next : tick <= next) return primaryTickIndices[i];
            i++;
        }
        return primaryTickIndices[primaryTickIndices.length - 1];
    }

    private float normalizeDisplayZoom(float value) {
        final float normalized = value / displayNormalizationFactor;
        float tenths = normalized * 10f;

        if (normalized < 1f) {
            return (float) Math.floor(tenths) / 10f;
        }

        final float floor = (float) Math.floor(tenths);
        if (floor % 5f == 0f) {
            tenths = floor;
        } else {
            final float ceil = (float) Math.ceil(tenths);
            if (ceil % 5f == 0f) {
                tenths = ceil;
            }
        }

        return (float) Math.rint(tenths) / 10f;
    }

    private String formatZoomNumber(float value) {
        final float normalized = normalizeDisplayZoom(value);
        final Locale locale = Locale.getDefault();
        if (normalized % 1f == 0f) return String.format(locale, "%.0f", normalized);
        final String formatted = String.format(locale, "%.1f", normalized);
        return formatted.startsWith("0") ? formatted.substring(1) : formatted;
    }

    private String formatBubble(float value) {
        return formatZoomNumber(value) + "×";
    }

    private String formatRuler(float value) {
        return formatZoomNumber(value);
    }

    private String formatToggle(float value) {
        return formatZoomNumber(value);
    }

    private String getToggleLabel(int index) {
        if (index == selectedToggleIndex) return formatBubble(selectedShowsStopValue ? toggleStops[index] : zoom);
        return formatToggle(toggleStops[index]);
    }

    private float getCompactWidth() {
        return toggleCellWidth * toggleStops.length;
    }

    private float getExpandedBackgroundWidth() {
        return dp(EXPANDED_BACKGROUND_WIDTH_DP);
    }

    private float getExpandedRulerWidth() {
        return dp(EXPANDED_RULER_WIDTH_DP);
    }

    private float getSelectorOffset(int index) {
        return toggleCellWidth * Math.max(0, index);
    }

    private float getBubbleHeight() {
        final Paint.FontMetricsInt metrics = bubbleTextPaint.getFontMetricsInt();
        return (metrics.descent - metrics.ascent) + dp(6f) * 2f;
    }

    private float centeredChildLeft(float available, float width) {
        return getPaddingLeft() + (int) ((available - width) / 2f);
    }

    private float getZoomButtonSize() {
        return dp(ZOOM_BUTTON_SIZE_DP);
    }

    private float getZoomButtonIconSize() {
        return dp(ZOOM_BUTTON_ICON_SIZE_DP);
    }

    private float getZoomButtonGap() {
        return dp(ZOOM_BUTTON_GAP_DP);
    }

    private void updateLayoutBounds() {
        final float available = Math.max(0f, getWidth() - getPaddingLeft() - getPaddingRight());
        final float usable = Math.max(0f, available - dp(8f) * 2f);

        if (toggleStops.length > 0) {
            final float desiredWidth = dp(TOGGLE_SIZE_DP_DEFAULT) * toggleStops.length;
            toggleCellWidth = desiredWidth <= usable ? dp(TOGGLE_SIZE_DP_DEFAULT) : usable / toggleStops.length;
        }

        final float bottom = getHeight() - getPaddingBottom();
        final float top = bottom - dp(56);
        final float controlBottom = top + toggleCellWidth;

        final float expandedWidth = Math.min(usable, Math.round(getExpandedBackgroundWidth()));
        final float compactWidth = Math.min(usable, Math.round(getCompactWidth()));

        if (animatedControlWidth <= 0f) {
            animatedControlWidth = expanded ? expandedWidth : compactWidth;
        }

        final float currentWidth = Math.min(usable, Math.round(animatedControlWidth));
        final float currentLeft = centeredChildLeft(available, currentWidth);
        controlBounds.set(currentLeft, top, currentLeft + currentWidth, controlBottom);

        final float compactLeft = centeredChildLeft(available, compactWidth);
        compactBounds.set(compactLeft, top, compactLeft + compactWidth, controlBottom);

        final float rulerWidth = Math.min(usable, getExpandedRulerWidth());
        final float rulerLeft = centeredChildLeft(available, rulerWidth);
        rulerBounds.set(rulerLeft, top, rulerLeft + rulerWidth, bottom);

        compactTouchBounds.set(compactBounds);
        compactTouchBounds.bottom = bottom;
        rulerTouchBounds.set(rulerBounds);

        final float buttonSize = getZoomButtonSize();
        final float gap = getZoomButtonGap();
        final float buttonTop = top + (toggleCellWidth - buttonSize) / 2f;

        minusButtonBounds.set(
                compactBounds.left - gap - buttonSize,
                buttonTop,
                compactBounds.left - gap,
                buttonTop + buttonSize
        );

        plusButtonBounds.set(
                compactBounds.right + gap,
                buttonTop,
                compactBounds.right + gap + buttonSize,
                buttonTop + buttonSize
        );
    }

    private SpringAnimation createSpring(
            FloatPropertyCompat<CherryZoomSliderView> property,
            float damping, float stiffness, float minimumVisibleChange
    ) {
        final SpringAnimation animation = new SpringAnimation(this, property);
        animation.setSpring(new SpringForce().setDampingRatio(damping).setStiffness(stiffness));
        animation.setMinimumVisibleChange(minimumVisibleChange);
        return animation;
    }

    private void configurePaints() {
        backgroundPaint.setStyle(Paint.Style.FILL);
        selectorPaint.setStyle(Paint.Style.FILL);
        bubblePaint.setStyle(Paint.Style.FILL);
        zoomButtonBackgroundPaint.setStyle(Paint.Style.FILL);

        tickPaint.setStrokeCap(Paint.Cap.ROUND);
        markerPaint.setStrokeCap(Paint.Cap.ROUND);

        toggleTextPaint.setTextAlign(Paint.Align.CENTER);
        toggleTextPaint.setTextSize(dp(13));
        toggleTextPaint.setTypeface(FontHelper.createTypeface2(FontHelper.TYPEFACE_GILROY_EXTRABOLD)); // AndroidUtilities.bold()

        selectedToggleTextPaint.setTextAlign(Paint.Align.CENTER);
        selectedToggleTextPaint.setTextSize(dp(16));
        selectedToggleTextPaint.setTypeface(FontHelper.createTypeface2(FontHelper.TYPEFACE_GILROY_EXTRABOLD)); // AndroidUtilities.bold()

        rulerLabelPaint.setTextAlign(Paint.Align.CENTER);
        rulerLabelPaint.setTextSize(dp(11));
        rulerLabelPaint.setTypeface(FontHelper.createTypeface2(FontHelper.TYPEFACE_GILROY_EXTRABOLD)); // AndroidUtilities.bold()

        bubbleTextPaint.setTextAlign(Paint.Align.CENTER);
        bubbleTextPaint.setTextSize(dp(16));
        bubbleTextPaint.setTypeface(FontHelper.createTypeface2(FontHelper.TYPEFACE_GILROY_EXTRABOLD)); // AndroidUtilities.bold()
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        updateLayoutBounds();

        if (!controlBounds.isEmpty()) {
            drawProtectionBackground(canvas);
        }

        final float compactAlpha = clamp(1f - expandedProgress, 0f, 1f);
        final float expandedAlpha = clamp(expandedProgress, 0f, 1f);
        final int layer = canvas.saveLayer(0f, 0f, getWidth(), getHeight(), null);

        if (compactAlpha > 0.001f) {
            final int save = canvas.saveLayerAlpha(0f, 0f, getWidth(), getHeight(), Math.round(compactAlpha * 255f));
            drawToggleRow(canvas);
            drawZoomButtons(canvas);
            canvas.restoreToCount(save);
        }

        if (expandedAlpha > 0.001f) {
            final int save = canvas.saveLayerAlpha(0f, 0f, getWidth(), getHeight(), Math.round(expandedAlpha * 255f));
            drawBubble(canvas);
            drawRuler(canvas);
            canvas.restoreToCount(save);
        }
        canvas.restoreToCount(layer);
    }

    private void createZoomButtonBackgrounds() {
        if (minusButtonBackgroundDrawable != null) {
            minusButtonBackgroundDrawable.setCallback(null);
            minusButtonBackgroundDrawable = null;
        }

        if (plusButtonBackgroundDrawable != null) {
            plusButtonBackgroundDrawable.setCallback(null);
            plusButtonBackgroundDrawable = null;
        }

        if (liquidGlassFactory == null || liquidGlassColorProvider == null) {
            invalidate();
            return;
        }

        minusButtonBackgroundDrawable = liquidGlassFactory.create(this, liquidGlassColorProvider);
        if (minusButtonBackgroundDrawable != null) {
            minusButtonBackgroundDrawable.setRadius(dp(16f));
            minusButtonBackgroundDrawable.setCallback(this);
        }

        plusButtonBackgroundDrawable = liquidGlassFactory.create(this, liquidGlassColorProvider);
        if (plusButtonBackgroundDrawable != null) {
            plusButtonBackgroundDrawable.setRadius(dp(16f));
            plusButtonBackgroundDrawable.setCallback(this);
        }
        invalidate();
    }

    private void drawProtectionBackground(Canvas canvas) {
        if (protectionBackgroundDrawable != null) {
            protectionBackgroundDrawable.setBounds(
                    Math.round(controlBounds.left), Math.round(controlBounds.top),
                    Math.round(controlBounds.right), Math.round(controlBounds.bottom)
            );
            protectionBackgroundDrawable.draw(canvas);
        } else {
            backgroundPaint.setColor(protectionBackgroundColor);
            canvas.drawRoundRect(controlBounds, dp(24f), dp(24f), backgroundPaint);
        }
    }

    private void drawToggleRow(Canvas canvas) {
        if (toggleStops.length == 0 || compactBounds.isEmpty()) return;
        final float size = toggleCellWidth - dp(8f);
        final float inset = (toggleCellWidth - size) / 2f;
        final float left = compactBounds.left + Math.round(animatedSelectorOffset) + inset;
        selectorBounds.set(left, compactBounds.top + inset, left + size, compactBounds.top + inset + size);
        if (!expanded) {
            selectorPaint.setColor(secondaryFixedColor);
            canvas.drawOval(selectorBounds, selectorPaint);
        }
        drawToggleLabels(canvas);
    }

    private void drawZoomButtons(Canvas canvas) {
        drawZoomButtonBackground(canvas, minusButtonBackgroundDrawable, minusButtonBounds);
        if (minusButtonIcon != null) {
            drawZoomButtonIcon(canvas, minusButtonIcon, minusButtonBounds);
        }

        drawZoomButtonBackground(canvas, plusButtonBackgroundDrawable, plusButtonBounds);
        if (plusButtonIcon != null) {
            drawZoomButtonIcon(canvas, plusButtonIcon, plusButtonBounds);
        }
    }

    private void drawZoomButtonBackground(Canvas canvas, BlurredBackgroundDrawable drawable, RectF bounds) {
        if (bounds.isEmpty()) {
            return;
        }

        if (drawable != null) {
            drawable.setBounds(
                    Math.round(bounds.left),
                    Math.round(bounds.top),
                    Math.round(bounds.right),
                    Math.round(bounds.bottom)
            );
            drawable.draw(canvas);
            return;
        }

        zoomButtonBackgroundPaint.setColor(protectionBackgroundColor);
        canvas.drawRoundRect(bounds, dp(16f), dp(16f), zoomButtonBackgroundPaint);
    }

    private void drawZoomButtonIcon(Canvas canvas, Drawable icon, RectF bounds) {
        final float size = getZoomButtonIconSize();

        final int left = Math.round(bounds.centerX() - size / 2f);
        final int top = Math.round(bounds.centerY() - size / 2f);
        final int right = Math.round(bounds.centerX() + size / 2f);
        final int bottom = Math.round(bounds.centerY() + size / 2f);

        icon.setBounds(left, top, right, bottom);
        icon.draw(canvas);
    }

    private void drawToggleLabels(Canvas canvas) {
        for (int i = 0; i < toggleStops.length; i++) {
            final boolean selected = i == selectedToggleIndex && !expanded;
            final Paint paint = i == selectedToggleIndex ? selectedToggleTextPaint : toggleTextPaint;
            paint.setColor(selected ? onSecondaryFixedColor : unselectedToggleColor);
            final float cx = compactBounds.left + (i + 0.5f) * toggleCellWidth;
            final Paint.FontMetricsInt metrics = paint.getFontMetricsInt();
            final int textHeight = metrics.descent - metrics.ascent;
            final float baseline = compactBounds.top + (int) ((compactBounds.height() - textHeight) / 2f) - metrics.ascent;
            canvas.drawText(getToggleLabel(i), cx, baseline, paint);
        }
    }

    private void drawRuler(Canvas canvas) {
        if (rulerBounds.isEmpty()) return;
        final int layer = canvas.saveLayer(rulerBounds.left, controlBounds.top, rulerBounds.right, controlBounds.bottom, null);
        canvas.clipRect(rulerBounds.left, controlBounds.top, rulerBounds.right, controlBounds.bottom);

        final float centerX = rulerBounds.centerX();
        final float currentTick = zoomToTick(zoom);
        final float ticksBottom = controlBounds.bottom - dp(22f);
        final float markerBottom = controlBounds.bottom - dp(23f);
        final float halfTicks = (rulerBounds.width() / 2f) / tickSpacing;

        int from = Math.max(0, (int) Math.floor(currentTick - halfTicks) - 1);
        final int to = Math.min(intervalCount, (int) Math.ceil(currentTick + halfTicks) + 1);
        final Paint.FontMetricsInt metrics = rulerLabelPaint.getFontMetricsInt();
        final float labelBaseline = controlBounds.bottom - dp(4f) - (metrics.descent - metrics.ascent) - metrics.ascent;

        for (; from <= to; from++) {
            final float x = centerX + (from - currentTick) * tickSpacing;
            final String label = primaryLabels.get(from);
            final boolean primary = label != null;
            tickPaint.setColor(primary ? primaryColor : minorTickColor);
            tickPaint.setStrokeWidth(dp(1f));
            canvas.drawLine(x, ticksBottom - dp(primary ? 12f : 6f), x, ticksBottom, tickPaint);
            if (primary) {
                rulerLabelPaint.setColor(primaryColor);
                canvas.drawText(label, x, labelBaseline, rulerLabelPaint);
            }
        }

        markerPaint.setColor(primaryColor);
        markerPaint.setStrokeWidth(dp(4f));
        canvas.drawLine(centerX, markerBottom - dp(12f), centerX, markerBottom, markerPaint);

        edgeFadePaint.setShader(new LinearGradient(rulerBounds.left, 0f, rulerBounds.right, 0f,
                new int[]{0x33000000, 0xff000000, 0xff000000, 0x33000000},
                new float[]{0f, 1f / 3f, 2f / 3f, 1f}, Shader.TileMode.CLAMP)
        );
        edgeFadePaint.setXfermode(DST_IN_XFERMODE);
        canvas.drawRect(rulerBounds.left, controlBounds.top, rulerBounds.right, controlBounds.bottom, edgeFadePaint);
        edgeFadePaint.setShader(null);
        edgeFadePaint.setXfermode(null);
        canvas.restoreToCount(layer);
    }

    private void drawBubble(Canvas canvas) {
        final String text = formatBubble(zoom);
        if (text.isEmpty()) return;
        final Paint.FontMetricsInt metrics = bubbleTextPaint.getFontMetricsInt();
        final float width = Math.max(dp(40f), (float) Math.ceil(bubbleTextPaint.measureText(text))) + dp(4f) * 2f;
        final float height = getBubbleHeight();
        final float halfHeight = height / 2f;
        final float centerX = controlBounds.centerX();
        final float centerY = controlBounds.top - dp(8f) - halfHeight;
        bubbleBounds.set(centerX - width / 2f, centerY - halfHeight, centerX + width / 2f, centerY + halfHeight);

        bubblePaint.setColor(secondaryFixedColor);
        bubbleTextPaint.setColor(onSecondaryFixedColor);
        final float baseline = bubbleBounds.top + dp(6f) - metrics.ascent;
        canvas.drawRoundRect(bubbleBounds, halfHeight, halfHeight, bubblePaint);
        canvas.drawText(text, bubbleBounds.centerX(), baseline, bubbleTextPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int compactTotalWidth = Math.round(
                dp(TOGGLE_SIZE_DP_DEFAULT) * toggleStops.length + getZoomButtonSize() * 2f + getZoomButtonGap() * 2f
        );

        final int width = Math.round(Math.max(getExpandedBackgroundWidth(), compactTotalWidth) + dp(8f) * 2f)
                + getPaddingLeft() + getPaddingRight();
        final int height = Math.round(getBubbleHeight() + dp(8f) * 2f + dp(56))
                + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(height, heightMeasureSpec));
    }

    private void animateExpandedProgress(float target, boolean animated) {
        if (expandedAnimator != null) {
            expandedAnimator.cancel();
            expandedAnimator = null;
        }
        if (!animated || !isLaidOut()) {
            expandedProgress = target;
            return;
        }
        final ValueAnimator animator = ValueAnimator.ofFloat(expandedProgress, target);
        expandedAnimator = animator;
        animator.setDuration(ANIMATION_DURATION);
        animator.setInterpolator(MORPH_INTERPOLATOR);
        animator.addUpdateListener(a -> {
            expandedProgress = clamp((float) a.getAnimatedValue(), 0f, 1f);
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (expandedAnimator == animation) expandedAnimator = null;
            }
        });
        animator.start();
    }

    private void animateSelectorTo(int index, boolean animated) {
        final float offset = getSelectorOffset(index);
        if (animated && isLaidOut()) {
            selectorSpring.animateToFinalPosition(offset);
            return;
        }
        selectorSpring.cancel();
        animatedSelectorOffset = offset;
        invalidate();
    }

    private void animateZoomTo(float target, boolean notify) {
        animateZoomTo(target, notify, -1);
    }

    private void animateZoomTo(float target, boolean notify, int toggleIndex) {
        final boolean fromToggle = toggleIndex >= 0 && toggleIndex < toggleStops.length;
        if (fromToggle) {
            stopZoomAnimator();
        } else {
            cancelZoomAnimator(true);
        }
        final float clamped = clamp(target, minZoom, maxZoom);
        if (fromToggle) {
            selectedToggleIndex = toggleIndex;
            selectedShowsStopValue = true;
            animateSelectorTo(toggleIndex, true);
        }
        if (Math.abs(clamped - zoom) < 1.0E-4f) {
            setZoomInternal(clamped, notify, !fromToggle);
            return;
        }
        final float from = zoom;
        final long duration = Math.min(500L, (long) Math.rint((Math.max(from, clamped) / Math.min(from, clamped)) * 500f / 3f));
        final ValueAnimator animator = ValueAnimator.ofFloat(from, clamped);
        zoomAnimator = animator;
        animator.setDuration(duration);
        animator.setInterpolator(ZOOM_INTERPOLATOR);
        animator.addUpdateListener(a -> setZoomInternal((float) a.getAnimatedValue(), notify, !fromToggle));
        animator.addListener(new AnimatorListenerAdapter() {
            private boolean cancelled;

            @Override
            public void onAnimationCancel(Animator animation) {
                cancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!cancelled && Math.abs(zoom - clamped) > 1.0E-4f) setZoomInternal(clamped, notify, !fromToggle);
                if (zoomAnimator == animation) {
                    zoomAnimator = null;
                    syncSelectedToggle(true);
                }
            }
        });
        animator.start();
    }

    public void setZoom(float newZoom, boolean notify) {
        final float clamped = clamp(newZoom, minZoom, maxZoom);
        if (Math.abs(zoom - clamped) >= 1.0E-3f) {
            cancelZoomAnimator();
            performPinchHaptic(zoom, clamped);
            zoom = clamped;
            syncSelectedToggle(true);
            invalidate();
            if (notify) {
                applyZoomToCamera(zoom);
            }
        }
        if (!expanded) {
            setExpanded(true, true);
        } else {
            resetAutoCollapseTimeout();
        }
    }

    public void scaleZoom(float factor) {
        if (!Float.isFinite(factor) || factor <= 0f) return;
        setZoom(zoom * factor, true);
    }

    public void collapse() {
        setExpanded(false, true);
    }

    private boolean stopZoomAnimator() {
        final ValueAnimator animator = zoomAnimator;
        if (animator == null) return false;
        zoomAnimator = null;
        animator.cancel();
        return true;
    }

    private void cancelZoomAnimator() {
        cancelZoomAnimator(false);
    }

    private void cancelZoomAnimator(boolean animated) {
        if (stopZoomAnimator()) syncSelectedToggle(animated);
    }

    private void cancelTransientSprings() {
        if (expandedAnimator != null) {
            expandedAnimator.cancel();
            expandedAnimator = null;
        }
        widthSpring.cancel();
        selectorSpring.cancel();
    }

    private void settleTransientAnimationValues() {
        expandedProgress = expanded ? 1f : 0f;
        animatedControlWidth = expanded ? getExpandedBackgroundWidth() : getCompactWidth();
        animatedSelectorOffset = getSelectorOffset(selectedToggleIndex);
    }

    private void updateTargetControlWidth(boolean animated) {
        final float target = expanded ? getExpandedBackgroundWidth() : getCompactWidth();
        if (animated && isLaidOut()) {
            widthSpring.animateToFinalPosition(target);
            return;
        }
        widthSpring.cancel();
        animatedControlWidth = target;
        invalidate();
    }

    private void setZoomInternal(float value, boolean notify, boolean syncToggle) {
        zoom = clamp(value, minZoom, maxZoom);
        if (syncToggle) syncSelectedToggle(!expanded);
        invalidate();
        if (notify) applyZoomToCamera(zoom);
    }

    private void syncSelectedToggle(boolean animated) {
        final int segment = findToggleSegment(zoom);

        if (segment < 0) {
            selectedShowsStopValue = false;
            selectedToggleIndex = -1;
            animatedSelectorOffset = 0f;
            return;
        }

        final boolean isExactPreset = Math.abs(zoom - toggleStops[segment]) <= 0.001f;

        if (selectedToggleIndex != segment) {
            selectedToggleIndex = segment;
            selectedShowsStopValue = isExactPreset;
            animateSelectorTo(segment, animated);
        } else {
            selectedShowsStopValue = isExactPreset;

            if (!animated) {
                selectorSpring.cancel();
                animatedSelectorOffset = getSelectorOffset(segment);
            }
        }

        invalidate();
    }

    private void selectToggle(int index) {
        if (index < 0 || index >= toggleStops.length) return;
        if (index == selectedToggleIndex && selectedShowsStopValue) return;
        performHapticSafe(HapticFeedbackConstants.CONTEXT_CLICK);
        animateZoomTo(toggleStops[index], true, index);
        if (presetSelectedListener != null) presetSelectedListener.onPresetSelected(toggleStops[index]);
    }

    private float getDynamicZoomStep() {
        final long heldTime = Math.max(
                0L,
                SystemClock.uptimeMillis() - zoomRepeatStartTime - ZOOM_REPEAT_INITIAL_DELAY
        );

        final float progress = Math.min(1f, heldTime / 1500f);
        final float easedProgress = progress * progress;

        return 0.2f + easedProgress * 2f;
    }

    private void stepZoomByButton(float amount, boolean animated) {
        final float target = clamp(zoom + amount, minZoom, maxZoom);

        if (Math.abs(target - zoom) < 1.0E-4f) {
            removeCallbacks(zoomRepeatRunnable);
            return;
        }

        performHapticSafe(HapticFeedbackConstants.CLOCK_TICK);

        if (animated) {
            animateZoomTo(target, true);
        } else {
            setZoomInternal(target, true, true);
        }
    }

    private void decreaseZoom() {
        stepZoomByButton(-ZOOM_BUTTON_STEP, true);
    }

    private void increaseZoom() {
        stepZoomByButton(ZOOM_BUTTON_STEP, true);
    }

    private void resetAutoCollapseTimeout() {
        removeCallbacks(autoCollapseRunnable);
        if (!expanded || dragging) return;
        postDelayed(autoCollapseRunnable, AUTO_COLLAPSE_DELAY);
    }

    private void beginDrag(float x) {
        if (stopZoomAnimator()) syncSelectedToggle(true);
        dragging = true;
        lastTouchX = x;
        dragTick = zoomToTick(zoom);
        dragPrimarySegment = Integer.MIN_VALUE;
        lastHapticTick = (int) Math.floor(dragTick);
        stickyTick = -1;
        stickyDistance = 0f;
        stickyFactor = 0f;
        removeCallbacks(autoCollapseRunnable);
    }

    private void moveDrag(float x) {
        float delta = x - lastTouchX;
        lastTouchX = x;
        if (Math.abs(delta) < 1.0E-4f) return;
        float tick = dragTick;
        final float velocity = getCurrentXVelocity();
        boolean primaryHaptic = false;

        if (stickyTick >= 0) {
            final float budget = Math.max(0f, tickSpacing * stickyFactor - stickyDistance);
            final float travelled = Math.abs(delta);
            if (travelled <= budget) {
                stickyDistance += travelled;
                setDragTick(stickyTick);
                return;
            }
            delta = Math.copySign(travelled - budget, delta);
            tick = stickyTick;
            stickyTick = -1;
            stickyDistance = 0f;
            stickyFactor = 0f;
        }

        if (primaryTickIndices.length > 0) {
            final int segment = findPrimarySegment(tick, delta);
            if (dragPrimarySegment == Integer.MIN_VALUE) {
                dragPrimarySegment = segment;
            } else if (segment != dragPrimarySegment) {
                final int previous = dragPrimarySegment;
                dragPrimarySegment = segment;
                performHapticSafe(HapticFeedbackConstants.KEYBOARD_TAP);
                primaryHaptic = true;
                final float stickiness = calculateStickiness(velocity);
                if (stickiness > 0f) {
                    final int anchor = Math.max(previous, segment);
                    final float radius = tickSpacing * stickiness;
                    final float distance = Math.abs(tick - anchor) * tickSpacing;
                    if (distance < radius) {
                        final float budget = radius - distance;
                        final float travelled = Math.abs(delta);
                        if (travelled <= budget) {
                            stickyTick = anchor;
                            stickyDistance = distance + travelled;
                            stickyFactor = stickiness;
                            setDragTick(anchor);
                            return;
                        }
                        delta = Math.copySign(travelled - budget, delta);
                        tick = anchor;
                    }
                }
            }
        }

        final float target = clamp(tick - delta / tickSpacing, 0f, intervalCount);
        performTickHaptic((int) Math.floor(target), primaryHaptic);
        setDragTick(target);
    }

    private float calculateStickiness(float velocity) {
        final float speed = Math.abs(velocity);
        if (speed <= 100f) return 0.7f;
        if (speed >= 1000f) return 0f;
        return (1000f - speed) / 900f * 0.7f;
    }

    private void performHapticSafe(int feedbackConstant) {
        if (CherrygramChatsConfig.INSTANCE.getDisableVibration()) return;
        performHapticFeedback(feedbackConstant);
    }

    private void performTickHaptic(int tick, boolean skip) {
        if (lastHapticTick == tick) return;
        lastHapticTick = tick;
        if (skip) return;
        performHapticSafe(HapticFeedbackConstants.CLOCK_TICK);
    }

    private void performPinchHaptic(float oldZoom, float newZoom) {
        final int a = (int) Math.floor(zoomToTick(oldZoom));
        final int b = (int) Math.floor(zoomToTick(newZoom));
        if (a == b) return;
        final int lo = Math.min(a, b) + 1;
        final int hi = Math.max(a, b);
        boolean primary = false;
        for (int t = lo; t <= hi; t++) {
            if (primaryLabels.get(t) != null) {
                primary = true;
                break;
            }
        }
        performHapticSafe(primary ? HapticFeedbackConstants.KEYBOARD_TAP : HapticFeedbackConstants.CLOCK_TICK);
    }

    private void setDragTick(float tick) {
        dragTick = clamp(tick, 0f, intervalCount);
        setTickInternal(dragTick, true);
    }

    private void setTickInternal(float tick, boolean notify) {
        setZoomInternal(tickToZoom(tick), notify, true);
    }

    private void setZoomFromRulerTap(float x) {
        animateZoomTo(tickToZoom(zoomToTick(zoom) + (x - rulerBounds.centerX()) / tickSpacing), true);
    }

    private void finishDrag(boolean click, boolean haptic) {
        if (click && haptic) performHapticSafe(HapticFeedbackConstants.CLOCK_TICK);
        dragging = false;
        compactGestureDown = false;
        dragStartedFromCompact = false;
        stickyTick = -1;
        stickyDistance = 0f;
        stickyFactor = 0f;
        dragPrimarySegment = Integer.MIN_VALUE;
        pressedToggleIndex = -1;
        setPressed(false);
        requestParentIntercept(true);
        resetAutoCollapseTimeout();
        if (click) performClick();
    }

    private void clearTouchState() {
        dragging = false;
        compactGestureDown = false;
        dragStartedFromCompact = false;
        movedPastSlop = false;
        pressedToggleIndex = -1;
        stickyTick = -1;
        stickyDistance = 0f;
        stickyFactor = 0f;
        dragPrimarySegment = Integer.MIN_VALUE;
        setPressed(false);
        requestParentIntercept(true);
    }

    private void requestParentIntercept(boolean allow) {
        final ViewParent parent = getParent();
        if (parent != null) parent.requestDisallowInterceptTouchEvent(!allow);
    }

    private void obtainVelocityTracker(MotionEvent event) {
        recycleVelocityTracker();
        velocityTracker = VelocityTracker.obtain();
        velocityTracker.addMovement(event);
    }

    private void recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    private float getCurrentXVelocity() {
        if (velocityTracker == null) return 0f;
        velocityTracker.computeCurrentVelocity(1000);
        return velocityTracker.getXVelocity();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || toggleStops.length == 0) return false;
        final int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            updateLayoutBounds();

            if (!expanded && minusButtonBounds.contains(event.getX(), event.getY())) {
                pressedZoomButton = -1;
                setPressed(true);
                requestParentIntercept(false);

                zoomRepeatStartTime = SystemClock.uptimeMillis();

                removeCallbacks(zoomRepeatRunnable);
                zoomRepeatFired = false;
                postDelayed(zoomRepeatRunnable, ZOOM_REPEAT_INITIAL_DELAY);

                return true;
            }

            if (!expanded && plusButtonBounds.contains(event.getX(), event.getY())) {
                pressedZoomButton = 1;
                setPressed(true);
                requestParentIntercept(false);

                zoomRepeatStartTime = SystemClock.uptimeMillis();

                removeCallbacks(zoomRepeatRunnable);
                zoomRepeatFired = false;
                postDelayed(zoomRepeatRunnable, ZOOM_REPEAT_INITIAL_DELAY);

                return true;
            }

            final RectF bounds = expanded ? rulerTouchBounds : compactTouchBounds;
            if (!bounds.contains(event.getX(), event.getY())) return false;
            downX = event.getX();
            downY = event.getY();
            lastTouchX = downX;
            movedPastSlop = false;
            compactGestureDown = !expanded;
            dragStartedFromCompact = false;
            pressedToggleIndex = !expanded && compactBounds.contains(downX, downY)
                    ? findToggleIndexAt(downX) : -1;
            setPressed(true);
            requestParentIntercept(false);
            obtainVelocityTracker(event);
            if (expanded) {
                beginDrag(downX);
            } else {
                postDelayed(longPressRunnable, ViewConfiguration.getLongPressTimeout());
            }
            return true;
        }

        if (velocityTracker != null) velocityTracker.addMovement(event);

        if (action == MotionEvent.ACTION_MOVE) {
            if (dragging) {
                if (!movedPastSlop && Math.hypot(event.getX() - downX, event.getY() - downY) > touchSlop) {
                    movedPastSlop = true;
                }
                moveDrag(event.getX());
                return true;
            }
            if (!compactGestureDown) return true;
            final float dx = event.getX() - downX;
            final float dy = event.getY() - downY;
            if (!movedPastSlop && Math.hypot(dx, dy) > touchSlop) {
                movedPastSlop = true;
                removeCallbacks(longPressRunnable);
                if (Math.abs(dx) >= Math.abs(dy)) {
                    dragStartedFromCompact = true;
                    beginDrag(downX);
                    setExpanded(true, true);
                    moveDrag(event.getX());
                }
            }
            return true;
        }

        if (action == MotionEvent.ACTION_UP) {
            if (pressedZoomButton != 0) {
                final int button = pressedZoomButton;

                removeCallbacks(zoomRepeatRunnable);
                pressedZoomButton = 0;

                setPressed(false);
                requestParentIntercept(true);

                if (!expanded && !zoomRepeatFired) {
                    if (button < 0 && minusButtonBounds.contains(event.getX(), event.getY())) {
                        decreaseZoom();
                    } else if (button > 0 && plusButtonBounds.contains(event.getX(), event.getY())) {
                        increaseZoom();
                    }
                }

                performClick();
                return true;
            }

            removeCallbacks(longPressRunnable);
            if (dragging) {
                final boolean tap = !movedPastSlop && !dragStartedFromCompact;
                if (tap) setZoomFromRulerTap(event.getX());
                finishDrag(true, tap);
            } else if (movedPastSlop || pressedToggleIndex < 0) {
                clearTouchState();
            } else {
                final int index = findToggleIndexAt(event.getX());
                if (compactBounds.contains(event.getX(), event.getY()) && index == pressedToggleIndex) {
                    selectToggle(index);
                }
                clearTouchState();
                performClick();
            }
            recycleVelocityTracker();
            return true;
        }

        if (action == MotionEvent.ACTION_CANCEL) {
            if (pressedZoomButton != 0) {
                pressedZoomButton = 0;
                removeCallbacks(zoomRepeatRunnable);
                setPressed(false);
                requestParentIntercept(true);
                invalidate();
                return true;
            }

            removeCallbacks(longPressRunnable);
            if (dragging) {
                finishDrag(false, false);
            } else {
                clearTouchState();
            }
            recycleVelocityTracker();
            return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        cancelTransientSprings();
        settleTransientAnimationValues();
        if (expanded && !dragging) resetAutoCollapseTimeout();
        if (cameraXController != null) {
            bindRetries = 0;
            tryBind();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(longPressRunnable);
        removeCallbacks(autoCollapseRunnable);
        removeCallbacks(bindRunnable);
        removeCallbacks(zoomFlushRunnable);
        removeCallbacks(zoomRepeatRunnable);
        animate().cancel();
        cancelZoomAnimator();
        cancelTransientSprings();
        settleTransientAnimationValues();
        clearTouchState();
        recycleVelocityTracker();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        cancelTransientSprings();
        tickSpacing = Math.max(1f, Math.round((float) dp(8f)));
        configurePaints();
        rebuildScale();
        settleTransientAnimationValues();
        requestLayout();
        invalidate();
    }

    private static void addDistinctStop(ArrayList<Float> stops, float value) {
        for (int i = 0; i < stops.size(); i++) {
            final float existing = stops.get(i);
            if (Math.abs(existing - value) <= 1.0E-4f) return;
            if (existing > value) {
                stops.add(i, value);
                return;
            }
        }
        stops.add(value);
    }

    private static float[] boundStops(float[] values, float min, float max, boolean withEdges) {
        final ArrayList<Float> stops = new ArrayList<>(values.length + 2);
        if (withEdges) addDistinctStop(stops, min);
        for (float value : values) {
            if (value <= 0f || !Float.isFinite(value)) continue;
            if (value < min - 1.0E-4f) {
                if (!withEdges) addDistinctStop(stops, min);
            } else if (value <= max + 1.0E-4f) {
                addDistinctStop(stops, clamp(value, min, max));
            }
        }
        if (withEdges) addDistinctStop(stops, max);
        final float[] result = new float[stops.size()];
        for (int i = 0; i < stops.size(); i++) result[i] = stops.get(i);
        return result;
    }

    private static float[] buildRulerStops(float min, float max, float unit) {
        final float[] stops = boundStops(new float[]{min, unit, 2f * unit, 5f * unit, 10f * unit, 30f * unit}, min, max, false);
        if (stops.length == 0 || stops[stops.length - 1] * 1.15f < max) return boundStops(stops, min, max, true);
        return stops;
    }

    private static float[] buildToggleStops(boolean frontFace, float min, float max, float unit) {
        return boundStops(frontFace
                ? new float[]{min, unit, 2f * unit}
                : new float[]{min, unit, 2f * unit, 5f * unit}, min, max, false
        );
    }

    private void applyZoomToCamera(float value) {
        pendingZoom = value;
        if (Float.isNaN(smoothedZoom) && cameraXController != null) {
            smoothedZoom = cameraXController.getZoomRatio();
            lastFrameTime = SystemClock.uptimeMillis();
        }
        scheduleZoomFlush();
    }

    private void scheduleZoomFlush() {
        if (zoomFlushScheduled) return;
        zoomFlushScheduled = true;
        postOnAnimation(zoomFlushRunnable);
    }

    private void discardPendingZoom() {
        removeCallbacks(zoomFlushRunnable);
        zoomFlushScheduled = false;
        pendingZoom = Float.NaN;
    }

    private void resetZoomThrottle() {
        discardPendingZoom();
        lastAppliedZoom = Float.NaN;
        lastZoomAppliedAt = 0L;
        smoothedZoom = Float.NaN;
    }

    private void flushPendingZoom() {
        zoomFlushScheduled = false;
        final float target = pendingZoom;
        if (Float.isNaN(target) || cameraXController == null) return;
        if (Float.isNaN(smoothedZoom)) smoothedZoom = target;

        final long now = SystemClock.uptimeMillis();
        final float dt = Math.max(1L, Math.min(50L, now - lastFrameTime));
        lastFrameTime = now;

        final double logCur = Math.log(smoothedZoom);
        final double diff = Math.log(target) - logCur;
        final boolean arrived = Math.abs(diff) < 0.002;
        if (arrived) {
            smoothedZoom = target;
        } else {
            final double k = 1.0 - Math.exp(-dt / ZOOM_SMOOTH_TAU_MS);
            smoothedZoom = (float) Math.exp(logCur + diff * k);
        }

        final boolean intervalPassed = now - lastZoomAppliedAt >= ZOOM_UPDATE_INTERVAL_MS;
        if ((intervalPassed || arrived) && smoothedZoom != lastAppliedZoom) {
            lastAppliedZoom = smoothedZoom;
            lastZoomAppliedAt = now;
            cameraXController.setZoomRatio(smoothedZoom);
        }

        if (!arrived) scheduleZoomFlush();
    }

    public void bindCamera(CameraXController controller) {
        cameraXController = controller;
        bindRetries = 0;
        baselineSettled = false;
        removeCallbacks(bindRunnable);
        cancelZoomAnimator();
        resetZoomThrottle();
        if (controller == null) {
            hideImmediately();
            return;
        }
        controller.setOnZoomSettledListener(this::onZoomSettled);
        tryBind();
    }

    public void unbindCamera() {
        cameraXController = null;
        bindRetries = 0;
        removeCallbacks(bindRunnable);
        resetZoomThrottle();
        hideImmediately();
    }

    public void prepareForCameraChange() {
        removeCallbacks(bindRunnable);
        baselineSettled = false;
        cancelZoomAnimator();
        resetZoomThrottle();
        hideImmediately();
    }

    private void onZoomSettled() {
        if (cameraXController == null) return;
        baselineSettled = true;
        tryBind();
    }

    private void retryBinding() {
        if (cameraXController != null && bindRetries++ < MAX_BIND_RETRIES) {
            postDelayed(bindRunnable, BIND_RETRY_DELAY);
            return;
        }
        hideImmediately();
    }

    private void tryBind() {
        if (cameraXController == null || !cameraXController.isCameraReady()) {
            retryBinding();
            return;
        }
        if (!baselineSettled) {
            retryBinding();
            return;
        }

        final float minRatio = cameraXController.getMinZoomRatio();
        final float maxRatio = cameraXController.getMaxZoomRatio();
        if (!Float.isFinite(minRatio) || !Float.isFinite(maxRatio) || minRatio <= 0f || maxRatio <= minRatio) {
            retryBinding();
            return;
        }

        final boolean frontFace = cameraXController.isFrontface();
        final float unit = frontFace ? minRatio : 1f;

        minZoom = minRatio;
        maxZoom = maxRatio;
        final float[] lensBreakpoints = frontFace ? null : cameraXController.getLensZoomBreakpoints();
        if (lensBreakpoints != null && lensBreakpoints.length > 0) {
            toggleStops = boundStops(lensBreakpoints, minRatio, maxRatio, true);
            rulerStops = boundStops(lensBreakpoints, minRatio, maxRatio, true);
        } else {
            toggleStops = buildToggleStops(frontFace, minRatio, maxRatio, unit);
            rulerStops = buildRulerStops(minRatio, maxRatio, unit);
        }
        displayNormalizationFactor = unit;
        rebuildScale();

        zoom = clamp(cameraXController.getZoomRatio(), minRatio, maxRatio);
        syncSelectedToggle(false);
        updateTargetControlWidth(false);
        settleTransientAnimationValues();
        requestLayout();
        invalidate();

        resetZoomThrottle();
        showAnimated();
    }

    public void hideImmediately() {
        animate().cancel();
        setAlpha(1f);
        setScaleX(1f);
        setScaleY(1f);
        setVisibility(GONE);
    }

    private void showAnimated() {
        setEnabled(true);
        if (getVisibility() != VISIBLE) {
            setAlpha(0f);
            setScaleX(0.9f);
            setScaleY(0.9f);
            setVisibility(VISIBLE);
            animate().alpha(1f).scaleX(1f).scaleY(1f)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(CubicBezierInterpolator.DEFAULT)
                    .start();
        }
    }

    public void setExpanded(boolean value, boolean animated) {
        final float progress = value ? 1f : 0f;
        final float width = value ? getExpandedBackgroundWidth() : getCompactWidth();
        if (expanded == value
                && Math.abs(expandedProgress - progress) < 1.0E-4f
                && Math.abs(animatedControlWidth - width) < 0.1f
        ) {
            if (value) resetAutoCollapseTimeout();
            return;
        }
        expanded = value;
        removeCallbacks(longPressRunnable);
        removeCallbacks(autoCollapseRunnable);
        animateExpandedProgress(progress, animated);
        if (animated && isLaidOut()) {
            widthSpring.animateToFinalPosition(width);
        } else {
            widthSpring.cancel();
            animatedControlWidth = width;
            invalidate();
        }
        if (value && !dragging) resetAutoCollapseTimeout();
    }

}