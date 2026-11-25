/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.preferences;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.Premium.PremiumFeatureBottomSheet;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.LauncherIconController;
import org.telegram.ui.LauncherIconController.LauncherIcon;
import org.telegram.ui.PremiumPreviewFragment;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;

import uz.unnarsx.cherrygram.core.CherrygramLogger;
import uz.unnarsx.cherrygram.core.firebase.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.core.helpers.DeeplinkHelper;
import uz.unnarsx.cherrygram.helpers.ui.FontHelper;
import uz.unnarsx.cherrygram.preferences.cells.LauncherIconDrawable;
import uz.unnarsx.cherrygram.preferences.cells.LauncherIconTint;

public class AppIconsPreferencesEntry extends BaseCGPreferencesEntry implements NotificationCenter.NotificationCenterDelegate {

    private static final int BIG_ICON = 148;
    private static final int CAROUSEL_ITEM = BIG_ICON + 16;
    private static final int SMALL_ICON = 40;
    private static final int STRIP_ITEM = SMALL_ICON + 14;
    private static final float SIDE_SCALE = 0.62f;
    private static final float SIDE_ALPHA = 0.45f;
    private static final int JUMP_MIN_DURATION = 320;
    private static final int JUMP_PER_ICON_DURATION = 45;
    private static final int JUMP_MAX_DURATION = 900;
    private static final CubicBezierInterpolator JUMP_INTERPOLATOR = new CubicBezierInterpolator(0.4, 0, 0.1, 1);
    private final Theme.ResourcesProvider darkResourcesProvider = new DefaultDarkThemeProvider();

    private final ArrayList<LauncherIcon> icons = new ArrayList<>();
    private final EnumMap<LauncherIcon, LauncherIconDrawable> bigDrawables = new EnumMap<>(LauncherIcon.class);
    private final EnumMap<LauncherIcon, LauncherIconDrawable> smallDrawables = new EnumMap<>(LauncherIcon.class);

    private TintedLayout layout;
    private RecyclerView carouselView;
    private LinearLayoutManager carouselLayoutManager;
    private RecyclerView stripView;
    private LinearLayoutManager stripLayoutManager;
    private AnimatedTextView titleView;
    private TextView subtitleView;
    private CharSequence subtitleText;
    private ButtonWithCounterView applyButton;

    private LauncherIcon currentIcon;
    private int selectedPosition = -1;
    private int targetPosition = -1;
    private float carouselPosition = -1;
    private float jumpFromCarousel, jumpFromStrip;

    @Override
    public boolean onFragmentCreate() {
        setResourceProvider(darkResourcesProvider);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.premiumStatusChangedGlobal);
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.premiumStatusChangedGlobal);
        super.onFragmentDestroy();
    }

    @Override
    public Theme.ResourcesProvider getResourceProvider() {
        return darkResourcesProvider;
    }

    @Override
    public boolean isLightStatusBar() {
        return false;
    }

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("app_icon_preferences_screen");
        return getString(R.string.AppIcon);
    }

    @Override
    protected String getKey() {
        return DeeplinkHelper.DeepLinksRepo.CG_App_Icon;
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {

    }

    @Override
    public View createView(Context context) {
        View view = super.createView(context);
        listView.setVisibility(View.GONE);

        icons.clear();
        icons.addAll(getAvailableIcons());
        currentIcon = LauncherIconController.getCurrentIcon();
        selectedPosition = -1;
        targetPosition = -1;
        carouselPosition = -1;
        subtitleText = null;
        int startPosition = Math.max(0, icons.indexOf(currentIcon));

        layout = new TintedLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(0, Math.max(mSystemInsets.top, AndroidUtilities.statusBarHeight) + ActionBar.getCurrentActionBarHeight(), 0, mSystemInsets.bottom);
        contentView.addView(layout, 1, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        layout.addView(new View(context), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 0, 1f));

        carouselView = new HorizontalList(context) {
            @Override
            protected void onMeasure(int widthSpec, int heightSpec) {
                int side = (MeasureSpec.getSize(widthSpec) - dp(CAROUSEL_ITEM)) / 2;
                if (getPaddingLeft() != side) {
                    setPadding(side, 0, side, 0);
                }
                super.onMeasure(widthSpec, heightSpec);
            }

            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                super.onLayout(changed, l, t, r, b);
                updateCarousel();
            }
        };
        carouselLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        carouselView.setLayoutManager(carouselLayoutManager);
        carouselView.setClipToPadding(false);
        carouselView.setClipChildren(false);
        carouselView.setAdapter(new CarouselAdapter());
        new PagerSnapHelper().attachToRecyclerView(carouselView);
        carouselView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING || newState == RecyclerView.SCROLL_STATE_IDLE) {
                    targetPosition = -1;
                    updateCarousel();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                updateCarousel();
            }
        });
        carouselLayoutManager.scrollToPositionWithOffset(startPosition, 0);
        layout.addView(carouselView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, BIG_ICON + 48));

        titleView = new AnimatedTextView(context);
        titleView.setAllowCancel(true);
        titleView.getDrawable().updateAll = true;
        titleView.setAnimationProperties(0.45f, 0, 420, -1f, CubicBezierInterpolator.EASE_OUT_QUINT);
        titleView.setTextSize(dp(24));
        titleView.setTypeface(FontHelper.createTypeface2(FontHelper.TYPEFACE_GILROY_EXTRABOLD));
        titleView.setGravity(Gravity.CENTER_HORIZONTAL);
        titleView.setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
        layout.addView(titleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 34, 24, 14, 24, 0));

        subtitleView = new TextView(context);
        subtitleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        subtitleView.setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteGrayText));
        subtitleView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        subtitleView.setLines(2);
        subtitleView.setEllipsize(TextUtils.TruncateAt.END);
        subtitleView.setLineSpacing(dp(2), 1f);
        layout.addView(subtitleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 32, 6, 32, 0));

        layout.addView(new View(context), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 0, 1f));

        stripView = new HorizontalList(context);
        stripLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        stripView.setLayoutManager(stripLayoutManager);
        stripView.setPadding(dp(12), 0, dp(12), 0);
        stripView.setClipToPadding(false);
        stripView.setAdapter(new StripAdapter());
        layout.addView(stripView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, STRIP_ITEM));

        applyButton = new ButtonWithCounterView(context, getResourceProvider()).setRound();
        applyButton.getTextPaint().setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        applyButton.setOnClickListener(v -> applySelectedIcon());
        layout.addView(applyButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, 16, 16, 16, 16));

        return view;
    }

    @Override
    public void onInsets(int left, int top, int right, int bottom) {
        super.onInsets(left, top, right, bottom);
        if (layout != null) {
            layout.setPadding(0, Math.max(top, AndroidUtilities.statusBarHeight) + ActionBar.getCurrentActionBarHeight(), 0, bottom);
        }
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.premiumStatusChangedGlobal && selectedPosition != -1) {
            updateSelectedInfo(true);
            for (int i = 0; i < carouselView.getChildCount(); i++) {
                CarouselItemView child = (CarouselItemView) carouselView.getChildAt(i);
                child.bind(child.icon);
            }
        }
    }

    private ArrayList<LauncherIcon> getAvailableIcons() {
        ArrayList<LauncherIcon> icons = new ArrayList<>(Arrays.asList(LauncherIcon.values()));

        if (Build.VERSION.SDK_INT < 27) {
            icons.removeAll(EnumSet.of(
                    LauncherIcon.DARK_CHERRY, LauncherIcon.DARK_CHERRY_BRA,
                    LauncherIcon.WHITE_CHERRY, LauncherIcon.WHITE_CHERRY_BRA,
                    LauncherIcon.LAGUNA_CHERRY, LauncherIcon.AQUA_CHERRY,
                    LauncherIcon.GREEN_CHERRY, LauncherIcon.LAVANDA_CHERRY,
                    LauncherIcon.VIOLET_SUNSET_CHERRY, LauncherIcon.VIOLET_SUNSET_CHERRY_BRA,
                    LauncherIcon.SUNSET_CHERRY, LauncherIcon.SUNRISE_CHERRY,
                    LauncherIcon.TURBO_CHERRY, LauncherIcon.NOX_CHERRY, LauncherIcon.DARK_NY
            ));
        }
        if (getMessagesController().premiumFeaturesBlocked()) {
            icons.removeIf(icon -> icon.premium);
        }
        return icons;
    }

    private void updateCarousel() {
        if (carouselView == null || icons.isEmpty()) return;

        float center = carouselView.getWidth() / 2f;
        float itemWidth = dp(CAROUSEL_ITEM);
        float r = 0, g = 0, b = 0, total = 0;
        int nearest = -1;
        float nearestDistance = Float.MAX_VALUE;
        float nearestOffset = 0;

        for (int i = 0; i < carouselView.getChildCount(); i++) {
            View child = carouselView.getChildAt(i);
            int position = carouselView.getChildAdapterPosition(child);
            if (position == RecyclerView.NO_POSITION) continue;

            float offset = center - (child.getLeft() + child.getRight()) / 2f;
            float distance = Math.abs(offset);
            float fraction = Math.min(1f, distance / itemWidth);
            float eased = CubicBezierInterpolator.EASE_OUT.getInterpolation(fraction);
            float scale = AndroidUtilities.lerp(1f, SIDE_SCALE, eased);
            child.setScaleX(scale);
            child.setScaleY(scale);
            child.setAlpha(AndroidUtilities.lerp(1f, SIDE_ALPHA, eased));

            float weight = 1f - fraction;
            if (weight > 0) {
                int color = LauncherIconTint.getColor(icons.get(position));
                r += Color.red(color) * weight;
                g += Color.green(color) * weight;
                b += Color.blue(color) * weight;
                total += weight;
            }
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = position;
                nearestOffset = offset;
            }
        }
        if (total > 0) {
            layout.setTint(Color.rgb((int) (r / total), (int) (g / total), (int) (b / total)));
        }
        if (targetPosition == -1 && nearest != -1 && nearest != selectedPosition) {
            boolean animated = selectedPosition != -1;
            select(nearest, animated);
            if (animated) {
                try {
                    carouselView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                } catch (Exception ignore) {}
            }
        }
        if (nearest != -1) {
            carouselPosition = nearest + nearestOffset / itemWidth;
            syncStrip(carouselPosition);
        }
    }

    private void syncStrip(float position) {
        if (stripView.getWidth() == 0 || stripView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE) return;

        if (targetPosition != -1 && targetPosition != jumpFromCarousel) {
            float progress = Math.max(0f, Math.min(1f, (position - jumpFromCarousel) / (targetPosition - jumpFromCarousel)));
            position = AndroidUtilities.lerp(jumpFromStrip, targetPosition, progress);
        }

        int base = Math.max(0, (int) Math.floor(position));
        float fraction = position - base;
        int itemWidth = dp(STRIP_ITEM);
        int offset = (int) ((stripView.getWidth() - itemWidth) / 2f - fraction * itemWidth) - stripView.getPaddingLeft();
        stripLayoutManager.scrollToPositionWithOffset(base, offset);
    }

    private float getStripPosition() {
        float center = stripView.getWidth() / 2f;
        float itemWidth = dp(STRIP_ITEM);
        float result = carouselPosition;
        float nearestDistance = Float.MAX_VALUE;
        for (int i = 0; i < stripView.getChildCount(); i++) {
            View child = stripView.getChildAt(i);
            int position = stripView.getChildAdapterPosition(child);
            if (position == RecyclerView.NO_POSITION) continue;
            float offset = center - (child.getLeft() + child.getRight()) / 2f;
            if (Math.abs(offset) < nearestDistance) {
                nearestDistance = Math.abs(offset);
                result = position + offset / itemWidth;
            }
        }
        return result;
    }

    private void scrollCarouselTo(int position) {
        if (position == RecyclerView.NO_POSITION || position == selectedPosition || carouselPosition < 0) return;

        stripView.stopScroll();
        jumpFromStrip = getStripPosition();
        carouselView.stopScroll();
        jumpFromCarousel = carouselPosition;
        targetPosition = position;
        select(position, true);

        float distance = position - carouselPosition;
        int duration = Math.min(JUMP_MAX_DURATION, JUMP_MIN_DURATION + (int) (Math.abs(distance) * JUMP_PER_ICON_DURATION));
        carouselView.smoothScrollBy(Math.round(distance * dp(CAROUSEL_ITEM)), 0, duration, JUMP_INTERPOLATOR);
    }

    private void select(int position, boolean animated) {
        int previous = selectedPosition;
        selectedPosition = position;

        titleView.setText(getString(icons.get(position).title), animated, previous > position);
        updateSelectedInfo(animated);

        for (int i = 0; i < stripView.getChildCount(); i++) {
            View child = stripView.getChildAt(i);
            ((StripItemView) child).setChecked(stripView.getChildAdapterPosition(child) == position, animated);
        }
        if (!animated) {
            stripLayoutManager.scrollToPositionWithOffset(position, (stripView.getMeasuredWidth() - dp(STRIP_ITEM)) / 2 - stripView.getPaddingLeft());
        }
    }

    private void updateSelectedInfo(boolean animated) {
        LauncherIcon icon = icons.get(selectedPosition);
        boolean isCurrent = icon == currentIcon;
        boolean isLocked = icon.premium && !UserConfig.hasPremiumOnAccounts();

        if (isCurrent) {
            setSubtitle(getString(R.string.AP_AppIcon_Current_Desc), animated);
            applyButton.setText(getString(R.string.ApplyTheme), animated);
        } else if (isLocked) {
            setSubtitle(getString(R.string.PremiumPreviewAppIcon), animated);
            applyButton.setText(getString(R.string.UnlockPremiumIcons), animated);
        } else {
            setSubtitle(getString(R.string.AP_AppIcon_Hint), animated);
            applyButton.setText(getString(R.string.ApplyTheme), animated);
        }
        applyButton.setEnabled(!isCurrent);
    }

    private void setSubtitle(CharSequence text, boolean animated) {
        if (TextUtils.equals(subtitleText, text)) return;
        subtitleText = text;

        subtitleView.animate().cancel();
        if (!animated) {
            subtitleView.setText(text);
            subtitleView.setAlpha(1f);
            subtitleView.setTranslationY(0);
            return;
        }
        subtitleView.animate()
                .alpha(0f)
                .translationY(-dp(4))
                .setDuration(120)
                .setInterpolator(CubicBezierInterpolator.EASE_IN)
                .withEndAction(() -> {
                    subtitleView.setText(subtitleText);
                    subtitleView.setTranslationY(dp(4));
                    subtitleView.animate()
                            .alpha(1f)
                            .translationY(0)
                            .setDuration(260)
                            .setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT)
                            .start();
                })
                .start();
    }

    private void applySelectedIcon() {
        if (selectedPosition == -1) return;
        LauncherIcon icon = icons.get(selectedPosition);
        if (icon == currentIcon) return;
        if (icon.premium && !UserConfig.hasPremiumOnAccounts()) {
            showDialog(new PremiumFeatureBottomSheet(this, PremiumPreviewFragment.PREMIUM_FEATURE_APPLICATION_ICONS, true));
            return;
        }

        LauncherIconController.setIcon(icon);
        currentIcon = icon;
        for (int i = 0; i < carouselView.getChildCount(); i++) {
            CarouselItemView child = (CarouselItemView) carouselView.getChildAt(i);
            child.setApplied(child.icon == currentIcon, true);
        }
        updateSelectedInfo(true);
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.showBulletin, Bulletin.TYPE_APP_ICON, icon);
    }

    private LauncherIconDrawable getBigDrawable(LauncherIcon icon) {
        LauncherIconDrawable drawable = bigDrawables.get(icon);
        if (drawable == null) {
            drawable = new LauncherIconDrawable(getContext(), icon, dp(BIG_ICON));
            bigDrawables.put(icon, drawable);
        }
        return drawable;
    }

    private LauncherIconDrawable getSmallDrawable(LauncherIcon icon) {
        LauncherIconDrawable drawable = smallDrawables.get(icon);
        if (drawable == null) {
            drawable = new LauncherIconDrawable(getContext(), icon, dp(SMALL_ICON));
            smallDrawables.put(icon, drawable);
        }
        return drawable;
    }

    static class DefaultDarkThemeProvider implements Theme.ResourcesProvider {
        private final SparseIntArray colors;

        DefaultDarkThemeProvider() {
            SparseIntArray result = new SparseIntArray();
            try {
                Theme.ThemeInfo theme = Theme.getTheme("Night");
                if (theme != null) {
                    SparseIntArray colorsNoAccent = Theme.getThemeFileValues(null, theme.assetName, null);
                    result = colorsNoAccent.clone();
                    Theme.ThemeAccent accent = theme.themeAccentsMap != null ? theme.themeAccentsMap.get(0) : null;
                    if (accent != null) {
                        accent.fillAccentColors(colorsNoAccent, result);
                    }
                }
            } catch (Exception e) {
                CherrygramLogger.e(e);
            }
            colors = result;
        }

        @Override
        public int getColor(int key) {
            int index = colors.indexOfKey(key);
            if (index < 0) {
                int fallbackKey = Theme.getFallbackKeys().get(key, -1);
                if (fallbackKey >= 0) {
                    index = colors.indexOfKey(fallbackKey);
                }
            }
            return index >= 0 ? colors.valueAt(index) : Theme.getDefaultColor(key);
        }

        @Override
        public boolean isDark() {
            return true;
        }
    }

    private static class HorizontalList extends RecyclerView {
        HorizontalList(Context context) {
            super(context);
            setItemAnimator(null);
            setOverScrollMode(View.OVER_SCROLL_NEVER);
            setHorizontalScrollBarEnabled(false);
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent e) {
            getParent().requestDisallowInterceptTouchEvent(canScrollHorizontally(-1));
            return super.onInterceptTouchEvent(e);
        }
    }

    private class CarouselAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            CarouselItemView view = new CarouselItemView(parent.getContext());
            RecyclerView.ViewHolder holder = new RecyclerView.ViewHolder(view) {};
            view.setOnClickListener(v -> scrollCarouselTo(holder.getAdapterPosition()));
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((CarouselItemView) holder.itemView).bind(icons.get(position));
        }

        @Override
        public int getItemCount() {
            return icons.size();
        }
    }

    private class StripAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            StripItemView view = new StripItemView(parent.getContext());
            RecyclerView.ViewHolder holder = new RecyclerView.ViewHolder(view) {};
            view.setOnClickListener(v -> scrollCarouselTo(holder.getAdapterPosition()));
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            StripItemView view = (StripItemView) holder.itemView;
            view.bind(icons.get(position));
            view.setChecked(position == selectedPosition, false);
        }

        @Override
        public int getItemCount() {
            return icons.size();
        }
    }

    private class CarouselItemView extends View {
        private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint badgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint badgeBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint checkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path checkPath = new Path();

        private final Drawable lockDrawable;
        private int lockColor;

        private LauncherIcon icon;
        private LauncherIconDrawable drawable;
        private boolean locked;
        private float appliedProgress;
        private ValueAnimator animator;

        CarouselItemView(Context context) {
            super(context);
            Drawable lock = ContextCompat.getDrawable(context, R.drawable.msg_mini_premiumlock);
            lockDrawable = lock != null ? lock.mutate() : null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                shadowPaint.setShadowLayer(dp(14), 0, dp(6), 0x30000000);
            }
            badgeBorderPaint.setStyle(Paint.Style.STROKE);
            badgeBorderPaint.setStrokeWidth(dp(3));
            checkPaint.setStyle(Paint.Style.STROKE);
            checkPaint.setStrokeWidth(dp(2.6f));
            checkPaint.setStrokeCap(Paint.Cap.ROUND);
            checkPaint.setStrokeJoin(Paint.Join.ROUND);
        }

        void bind(LauncherIcon icon) {
            this.icon = icon;
            drawable = getBigDrawable(icon);
            setContentDescription(getString(icon.title));
            locked = icon.premium && !UserConfig.hasPremiumOnAccounts();
            setApplied(icon == currentIcon, false);
        }

        void setApplied(boolean applied, boolean animated) {
            float to = applied ? 1f : 0f;
            if (animator != null) {
                animator.cancel();
                animator = null;
            }
            if (!animated) {
                appliedProgress = to;
                invalidate();
                return;
            }
            if (to == appliedProgress) return;
            animator = ValueAnimator.ofFloat(appliedProgress, to).setDuration(420);
            animator.setInterpolator(CubicBezierInterpolator.EASE_OUT_BACK);
            animator.addUpdateListener(a -> {
                appliedProgress = (float) a.getAnimatedValue();
                invalidate();
            });
            animator.start();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(dp(CAROUSEL_ITEM), dp(BIG_ICON + 48));
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            if (drawable == null) return;
            int size = dp(BIG_ICON);
            int left = (getWidth() - size) / 2;
            int top = (getHeight() - size) / 2;
            drawable.setBounds(left, top, left + size, top + size);

            shadowPaint.setColor(getThemedColor(Theme.key_windowBackgroundWhite));
            canvas.drawPath(drawable.getShape(), shadowPaint);
            drawable.draw(canvas);

            float lockProgress = locked ? 1f - appliedProgress : 0f;
            float badgeProgress = Math.max(appliedProgress, lockProgress);
            if (badgeProgress <= 0f) return;
            float bx = left + size * 0.87f;
            float by = top + size * 0.87f;
            float radius = dp(17) * badgeProgress;
            int badgeColor = layout.tint != 0 ? layout.tint : getThemedColor(Theme.key_featuredStickers_addButton);
            int contentColor = LauncherIconTint.contentColor(badgeColor);
            badgePaint.setColor(badgeColor);
            badgeBorderPaint.setColor(getThemedColor(Theme.key_windowBackgroundGray));
            canvas.drawCircle(bx, by, radius, badgePaint);
            canvas.drawCircle(bx, by, radius, badgeBorderPaint);

            if (lockProgress > 0f && lockDrawable != null) {
                int half = (int) (dp(10) * lockProgress);
                if (lockColor != contentColor) {
                    lockColor = contentColor;
                    lockDrawable.setColorFilter(new PorterDuffColorFilter(contentColor, PorterDuff.Mode.SRC_IN));
                }
                lockDrawable.setBounds((int) bx - half, (int) by - half, (int) bx + half, (int) by + half);
                lockDrawable.draw(canvas);
            }
            if (appliedProgress <= 0f) return;
            checkPaint.setColor(contentColor);
            canvas.save();
            canvas.scale(appliedProgress, appliedProgress, bx, by);
            checkPath.rewind();
            checkPath.moveTo(bx - dp(7), by);
            checkPath.lineTo(bx - dp(2), by + dp(5));
            checkPath.lineTo(bx + dp(7.5f), by - dp(5));
            canvas.drawPath(checkPath, checkPaint);
            canvas.restore();
        }
    }

    private class StripItemView extends View {
        private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path ringPath = new Path();
        private final Matrix matrix = new Matrix();

        private LauncherIconDrawable drawable;
        private float progress;
        private ValueAnimator animator;

        StripItemView(Context context) {
            super(context);
            ringPaint.setStyle(Paint.Style.STROKE);
            ringPaint.setStrokeWidth(dp(2));
        }

        void bind(LauncherIcon icon) {
            drawable = getSmallDrawable(icon);
            setContentDescription(getString(icon.title));
            invalidate();
        }

        void setChecked(boolean checked, boolean animated) {
            float to = checked ? 1f : 0f;
            if (animator != null) {
                animator.cancel();
                animator = null;
            }
            if (!animated) {
                progress = to;
                invalidate();
                return;
            }
            if (to == progress) return;
            animator = ValueAnimator.ofFloat(progress, to).setDuration(320);
            animator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
            animator.addUpdateListener(a -> {
                progress = (float) a.getAnimatedValue();
                invalidate();
            });
            animator.start();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(dp(STRIP_ITEM), dp(STRIP_ITEM));
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            if (drawable == null) return;
            int size = dp(SMALL_ICON);
            int left = (getWidth() - size) / 2;
            int top = (getHeight() - size) / 2;

            float iconScale = AndroidUtilities.lerp(1f, 0.82f, progress);
            canvas.save();
            canvas.scale(iconScale, iconScale, getWidth() / 2f, getHeight() / 2f);
            drawable.setBounds(left, top, left + size, top + size);
            drawable.draw(canvas);
            canvas.restore();

            if (progress <= 0f) return;
            float ringScale = (size + dp(4)) / (float) size;
            matrix.setScale(ringScale, ringScale, getWidth() / 2f, getHeight() / 2f);
            drawable.getShape().transform(matrix, ringPath);
            ringPaint.setColor(layout.tint != 0 ? layout.tint : getThemedColor(Theme.key_featuredStickers_addButton));
            ringPaint.setAlpha((int) (255 * progress));
            canvas.drawPath(ringPath, ringPaint);
        }
    }

    @SuppressLint("ViewConstructor")
    private class TintedLayout extends LinearLayout {
        private static final float GLOW_RADIUS = 260;

        private final Paint gradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        private int tint;
        private float glowY;

        TintedLayout(Context context) {
            super(context);
            setWillNotDraw(false);
        }

        void setTint(int color) {
            if (tint == color) return;
            tint = color;
            PorterDuffColorFilter filter = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN);
            gradientPaint.setColorFilter(filter);
            glowPaint.setColorFilter(filter);
            invalidate();

            applyButton.setColor(color);
            applyButton.setTextColor(LauncherIconTint.contentColor(color));
            for (int i = 0; i < stripView.getChildCount(); i++) {
                stripView.getChildAt(i).invalidate();
            }
            for (int i = 0; i < carouselView.getChildCount(); i++) {
                carouselView.getChildAt(i).invalidate();
            }
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            gradientPaint.setShader(new LinearGradient(0, 0, 0, h * 0.85f, LauncherIconTint.FADE_COLORS, LauncherIconTint.FADE_POSITIONS, Shader.TileMode.CLAMP));
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            float y = carouselView.getTop() + carouselView.getHeight() / 2f;
            if (y != glowY) {
                glowY = y;
                glowPaint.setShader(new RadialGradient(getWidth() / 2f, glowY, dp(GLOW_RADIUS), LauncherIconTint.FADE_COLORS, LauncherIconTint.FADE_POSITIONS, Shader.TileMode.CLAMP));
            }
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            if (tint == 0) return;
            gradientPaint.setAlpha(140);
            canvas.drawRect(0, 0, getWidth(), getHeight(), gradientPaint);
            glowPaint.setAlpha(150);
            canvas.drawRect(0, 0, getWidth(), getHeight(), glowPaint);
        }
    }

}