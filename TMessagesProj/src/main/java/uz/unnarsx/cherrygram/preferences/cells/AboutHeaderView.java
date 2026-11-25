/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.preferences.cells;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ScaleStateListAnimator;
import org.telegram.ui.Components.blur3.capture.IBlur3Capture;
import org.telegram.ui.Components.blur3.capture.IBlur3Hash;
import org.telegram.ui.LauncherIconController;
import org.telegram.ui.LauncherIconController.LauncherIcon;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper;
import uz.unnarsx.cherrygram.helpers.ui.FontHelper;

@SuppressLint("ViewConstructor")
public class AboutHeaderView extends LinearLayout {

    private static final int ICON = 96;

    public interface Delegate {
        void onUpdatesClick();
        void onUpdatesLongClick();
        void onLinkClick(int id);
        boolean onLinkLongClick(int id);
    }

    public static class Link {
        final int id;
        final int icon;
        final CharSequence title;
        final CharSequence subtitle;

        public Link(int id, int icon, CharSequence title, CharSequence subtitle) {
            this.id = id;
            this.icon = icon;
            this.title = title;
            this.subtitle = subtitle;
        }
    }

    private final Theme.ResourcesProvider resourcesProvider;
    private final Delegate delegate;

    private final IconView iconView;
    private final TextView titleView;
    private final TextView versionChip;
    private final TextView telegramChip;
    private final TextView buildChip;
    private final TextView updateView;
    private final ArrayList<LinearLayout> groups = new ArrayList<>();
    private final Typeface gilroy = FontHelper.createTypeface2(FontHelper.TYPEFACE_GILROY_EXTRABOLD);
    private Background background;

    private LauncherIcon icon;
    private int tint;
    private int accentText;

    public AboutHeaderView(Context context, Theme.ResourcesProvider resourcesProvider, Delegate delegate) {
        super(context);
        this.resourcesProvider = resourcesProvider;
        this.delegate = delegate;
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setPadding(0, dp(8), 0, dp(6));

        iconView = new IconView(context);
        iconView.setContentDescription(getString(R.string.AppIcon));
        addView(iconView, LayoutHelper.createLinear(ICON + 64, ICON + 64, 0, -20, 0, -20));

        titleView = new TextView(context);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 28);
        titleView.setTypeface(gilroy);
        titleView.setText(getString(R.string.CG_AppName));
        addView(titleView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0, 4, 0, 0));

        ChipsLayout chipsLayout = new ChipsLayout(context);
        versionChip = createChip(context, CGResourcesHelper.getCherryVersion());
        telegramChip = createChip(context, "Telegram " + BuildVars.BUILD_VERSION_STRING + " (" + CGResourcesHelper.getSourceCodeVersion() + ")");
        buildChip = createChip(context, CGResourcesHelper.getBuildType());
        chipsLayout.addView(versionChip);
        chipsLayout.addView(telegramChip);
        chipsLayout.addView(buildChip);
        addView(chipsLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 16, 10, 16, 0));

        updateView = new TextView(context);
        updateView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        updateView.setTypeface(AndroidUtilities.bold());
        updateView.setGravity(Gravity.CENTER_VERTICAL);
        updateView.setSingleLine(true);
        updateView.setPadding(dp(12), 0, dp(14), 0);
        updateView.setCompoundDrawablePadding(dp(6));
        updateView.setText(getString(R.string.UP_Category_Updates));
        updateView.setOnClickListener(v -> delegate.onUpdatesClick());
        updateView.setOnLongClickListener(v -> {
            delegate.onUpdatesLongClick();
            return true;
        });
        ScaleStateListAnimator.apply(updateView, .02f, 1.2f);
        addView(updateView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 36, 16, 10, 16, 0));

        update();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(FullWidthLayout.exactly(widthMeasureSpec), heightMeasureSpec);
    }

    private static class ChipsLayout extends ViewGroup {
        private final int gap = dp(6);

        ChipsLayout(Context context) {
            super(context);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
            int rowWidth = 0;
            int rowHeight = 0;
            int height = 0;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child.getVisibility() == GONE) {
                    continue;
                }
                child.measure(MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST), MeasureSpec.UNSPECIFIED);
                int childWidth = child.getMeasuredWidth();
                if (rowWidth > 0 && rowWidth + gap + childWidth > maxWidth) {
                    height += rowHeight + gap;
                    rowWidth = 0;
                    rowHeight = 0;
                }
                if (rowWidth > 0) {
                    rowWidth += gap;
                }
                rowWidth += childWidth;
                rowHeight = Math.max(rowHeight, child.getMeasuredHeight());
            }
            height += rowHeight;
            setMeasuredDimension(maxWidth, height);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            int maxWidth = r - l;
            int y = 0;
            int rowStart = 0;
            while (rowStart < getChildCount()) {
                int rowEnd = rowStart;
                int rowWidth = 0;
                int rowHeight = 0;
                while (rowEnd < getChildCount()) {
                    View child = getChildAt(rowEnd);
                    if (child.getVisibility() == GONE) {
                        rowEnd++;
                        continue;
                    }
                    int childWidth = child.getMeasuredWidth();
                    int widthWithGap = childWidth;
                    if (rowWidth > 0) {
                        widthWithGap += gap;
                    }
                    if (rowWidth > 0 && rowWidth + widthWithGap > maxWidth) {
                        break;
                    }
                    rowWidth += widthWithGap;
                    rowHeight = Math.max(rowHeight, child.getMeasuredHeight());
                    rowEnd++;
                }

                int x = (maxWidth - rowWidth) / 2;
                for (int i = rowStart; i < rowEnd; i++) {
                    View child = getChildAt(i);
                    if (child.getVisibility() == GONE) {
                        continue;
                    }
                    int childY = y + (rowHeight - child.getMeasuredHeight()) / 2;
                    child.layout(x, childY, x + child.getMeasuredWidth(), childY + child.getMeasuredHeight());
                    x += child.getMeasuredWidth() + gap;
                }
                y += rowHeight + gap;
                rowStart = rowEnd;
            }
        }
    }

    private TextView createChip(Context context, CharSequence text) {
        TextView chip = new TextView(context);
        chip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        chip.setTypeface(gilroy);
        chip.setSingleLine(true);
        chip.setPadding(dp(10), dp(5), dp(10), dp(5));
        chip.setText(text);
        return chip;
    }

    public LinearLayout createGroup(CharSequence title, View customView, ArrayList<Link> links, int topPadding) {
        if (customView == null && links.isEmpty()) {
            return null;
        }

        LinearLayout group = new FullWidthLayout(getContext());
        group.setPadding(dp(12), dp(topPadding), dp(12), 0);

        TextView captionView = new TextView(getContext());
        captionView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        captionView.setTypeface(AndroidUtilities.bold());
        captionView.setAllCaps(true);
        captionView.setLetterSpacing(0.04f);
        captionView.setText(title);
        if (title == null) {
            captionView.setVisibility(GONE);
        }
        group.addView(captionView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 6, 0, 6, 8));

        LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(VERTICAL);
        card.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), dp(16));
            }
        });
        card.setClipToOutline(true);
        if (customView != null) {
            AndroidUtilities.removeFromParent(customView);
            card.addView(customView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }
        for (int i = 0; i < links.size(); i++) {
            Link link = links.get(i);
            LinkRow row = new LinkRow(getContext(), link, i < links.size() - 1);
            row.setOnClickListener(v -> delegate.onLinkClick(link.id));
            row.setOnLongClickListener(v -> delegate.onLinkLongClick(link.id));
            card.addView(row, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 52));
        }
        group.addView(card, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        groups.add(group);
        updateColors();
        return group;
    }

    public void update() {
        LauncherIcon current = LauncherIconController.getCurrentIcon();
        if (current != icon) {
            icon = current;
            iconView.drawable = new LauncherIconDrawable(getContext(), icon, dp(ICON));
            tint = LauncherIconTint.getColor(icon);
        }
        updateColors();
    }

    public int getAccentTextColor() {
        return accentText;
    }

    private void updateColors() {
        int text = getThemedColor(Theme.key_windowBackgroundWhiteBlackText);
        int gray = getThemedColor(Theme.key_windowBackgroundWhiteGrayText);
        accentText = ColorUtils.blendARGB(tint, 0xFFFFFFFF, 0.35f);

        titleView.setTextColor(text);
        int chip = ColorUtils.setAlphaComponent(text, 0x17);
        versionChip.setTextColor(text);
        versionChip.setBackground(Theme.createRoundRectDrawable(dp(12), chip));
        telegramChip.setTextColor(text);
        telegramChip.setBackground(Theme.createRoundRectDrawable(dp(12), chip));
        buildChip.setTextColor(0xFFFFFFFF);
        buildChip.setBackground(Theme.createRoundRectDrawable(dp(12), ColorUtils.setAlphaComponent(tint, 0x70)));

        int rowIcon = ColorUtils.setAlphaComponent(text, 0xA6);
        updateView.setTextColor(text);
        updateView.setBackground(new LayerDrawable(new Drawable[]{
                Theme.createRoundRectDrawable(dp(18), ColorUtils.setAlphaComponent(text, 0x14)),
                Theme.createRadSelectorDrawable(getThemedColor(Theme.key_listSelector), 18, 18)
        }));
        Drawable refresh = ContextCompat.getDrawable(getContext(), R.drawable.msg_retry);
        if (refresh != null) {
            refresh = refresh.mutate();
            refresh.setColorFilter(new PorterDuffColorFilter(rowIcon, PorterDuff.Mode.SRC_IN));
            refresh.setBounds(0, 0, dp(18), dp(18));
        }
        updateView.setCompoundDrawables(refresh, null, null, null);

        for (LinearLayout group : groups) {
            ((TextView) group.getChildAt(0)).setTextColor(gray);
            LinearLayout card = (LinearLayout) group.getChildAt(1);
            card.setBackground(Theme.createRoundRectDrawable(dp(16), ColorUtils.setAlphaComponent(text, 0x14)));
            for (int i = 0; i < card.getChildCount(); i++) {
                if (card.getChildAt(i) instanceof LinkRow row) {
                    row.updateColors(text, gray, rowIcon);
                }
            }
        }

        iconView.invalidate();
        if (background != null) {
            background.invalidate();
        }
    }

    private int getThemedColor(int key) {
        return Theme.getColor(key, resourcesProvider);
    }

    public static class FullWidthLayout extends LinearLayout {
        public FullWidthLayout(Context context) {
            super(context);
            setOrientation(VERTICAL);
        }

        static int exactly(int widthMeasureSpec) {
            return MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(exactly(widthMeasureSpec), heightMeasureSpec);
        }
    }

    public class Background extends RecyclerView.ItemDecoration implements IBlur3Capture {
        private static final int GLOW_RADIUS = 260;

        private final Paint backgroundPaint = new Paint();
        private final Paint gradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        private final int[] location = new int[2];
        private RecyclerView listView;
        private int filterColor;
        private int height;

        public Background() {
            background = this;
            glowPaint.setShader(new RadialGradient(0, 0, dp(GLOW_RADIUS), LauncherIconTint.FADE_COLORS, LauncherIconTint.FADE_POSITIONS, Shader.TileMode.CLAMP));
        }

        void invalidate() {
            if (listView != null) {
                listView.invalidate();
            }
        }

        @Override
        public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            listView = parent;
            drawTint(canvas);
        }

        @Override
        public void capture(Canvas canvas, RectF position) {
            if (listView == null) {
                return;
            }
            canvas.save();
            canvas.clipRect(position);
            drawTint(canvas);
            canvas.restore();
        }

        @Override
        public void captureCalculateHash(IBlur3Hash builder, RectF position) {
            builder.add(tint);
            builder.add(height);
            if (listView != null) {
                builder.add(listView.computeVerticalScrollOffset());
            }
        }

        private void drawTint(Canvas canvas) {
            if (listView == null) {
                return;
            }
            int width = listView.getWidth();

            backgroundPaint.setColor(getThemedColor(Theme.key_windowBackgroundGray));
            canvas.drawRect(0, -listView.getHeight(), width, listView.getHeight() * 2, backgroundPaint);
            if (tint == 0) {
                return;
            }

            if (height != listView.getHeight()) {
                height = listView.getHeight();
                gradientPaint.setShader(new LinearGradient(0, 0, 0, height * 1.3f, LauncherIconTint.FADE_COLORS, LauncherIconTint.FADE_POSITIONS, Shader.TileMode.CLAMP));
            }
            if (filterColor != tint) {
                filterColor = tint;
                PorterDuffColorFilter filter = new PorterDuffColorFilter(tint, PorterDuff.Mode.SRC_IN);
                gradientPaint.setColorFilter(filter);
                glowPaint.setColorFilter(filter);
            }
            gradientPaint.setAlpha(140);
            canvas.drawRect(0, 0, width, height, gradientPaint);

            if (iconView.isAttachedToWindow()) {
                listView.getLocationInWindow(location);
                int listTop = location[1];
                iconView.getLocationInWindow(location);
                canvas.save();
                canvas.translate(width / 2f, location[1] - listTop + iconView.getHeight() / 2f);
                glowPaint.setAlpha(150);
                canvas.drawCircle(0, 0, dp(GLOW_RADIUS), glowPaint);
                canvas.restore();
            }
        }
    }

    private static class LinkRow extends LinearLayout {
        private final ImageView iconView;
        private final TextView titleView;
        private final TextView valueView;
        private final boolean divider;
        private final Paint dividerPaint = new Paint();
        private final Paint arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path arrowPath = new Path();

        LinkRow(Context context, Link link, boolean divider) {
            super(context);
            this.divider = divider;
            setOrientation(HORIZONTAL);
            setGravity(Gravity.CENTER_VERTICAL);
            setWillNotDraw(false);
            setPadding(dp(18), 0, dp(34), 0);
            setContentDescription(link.title);
            arrowPaint.setStyle(Paint.Style.STROKE);
            arrowPaint.setStrokeWidth(dp(1.8f));
            arrowPaint.setStrokeCap(Paint.Cap.ROUND);
            arrowPaint.setStrokeJoin(Paint.Join.ROUND);

            iconView = new ImageView(context);
            iconView.setScaleType(ImageView.ScaleType.CENTER);
            iconView.setImageResource(link.icon);
            addView(iconView, LayoutHelper.createLinear(24, 24));

            titleView = new TextView(context);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            titleView.setSingleLine(true);
            titleView.setEllipsize(TextUtils.TruncateAt.END);
            titleView.setText(link.title);
            addView(titleView, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f, 19, 0, 0, 0));

            valueView = new TextView(context);
            valueView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            valueView.setSingleLine(true);
            valueView.setEllipsize(TextUtils.TruncateAt.END);
            valueView.setText(link.subtitle);
            if (TextUtils.isEmpty(link.subtitle)) {
                valueView.setVisibility(GONE);
            }
            addView(valueView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 12, 0, 0, 0));
        }

        void updateColors(int text, int gray, int iconColor) {
            iconView.setColorFilter(new PorterDuffColorFilter(iconColor, PorterDuff.Mode.SRC_IN));
            titleView.setTextColor(text);
            valueView.setTextColor(gray);
            arrowPaint.setColor(gray);
            dividerPaint.setColor(ColorUtils.setAlphaComponent(text, 0x14));
            setBackground(Theme.createSelectorDrawable(ColorUtils.setAlphaComponent(text, 0x14), Theme.RIPPLE_MASK_ALL));
            invalidate();
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            float cx = getWidth() - dp(20);
            float cy = getHeight() / 2f;
            arrowPath.rewind();
            arrowPath.moveTo(cx - dp(3), cy - dp(6));
            arrowPath.lineTo(cx + dp(3), cy);
            arrowPath.lineTo(cx - dp(3), cy + dp(6));
            canvas.drawPath(arrowPath, arrowPaint);
            if (divider) {
                canvas.drawRect(dp(18 + 24 + 19), getHeight() - Math.max(1, dp(0.5f)), getWidth(), getHeight(), dividerPaint);
            }
        }
    }

    private class IconView extends View {
        private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        LauncherIconDrawable drawable;

        IconView(Context context) {
            super(context);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                shadowPaint.setShadowLayer(dp(14), 0, dp(6), 0x30000000);
            }
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            if (drawable == null) {
                return;
            }
            int size = dp(ICON);
            int left = (getWidth() - size) / 2;
            int top = (getHeight() - size) / 2;
            drawable.setBounds(left, top, left + size, top + size);

            shadowPaint.setColor(tint);
            canvas.save();
            canvas.scale(0.97f, 0.97f, left + size / 2f, top + size / 2f);
            canvas.drawPath(drawable.getShape(), shadowPaint);
            canvas.restore();
            drawable.draw(canvas);
        }
    }

}