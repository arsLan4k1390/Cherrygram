/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.preferences.cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import org.telegram.ui.LauncherIconController;

public class LauncherIconDrawable extends Drawable {

    private static final float LAYER_SCALE = 108f / 72f;
    private static final double SQUIRCLE_POWER = 3;

    private final Drawable adaptive;
    private final Drawable background;
    private final Drawable foreground;
    private final Path shape = new Path();
    private final int size;
    private boolean squareMask;
    private final Path outside = new Path();
    private Paint maskPaint;

    public LauncherIconDrawable(Context context, LauncherIconController.LauncherIcon icon, int size) {
        this.size = size;

        Drawable bg = ContextCompat.getDrawable(context, icon.background);
        Drawable fg = icon.foreground != 0 ? ContextCompat.getDrawable(context, icon.foreground) : null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bg instanceof AdaptiveIconDrawable) {
            adaptive = bg;
            background = null;
            foreground = null;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bg != null && fg != null) {
            adaptive = new AdaptiveIconDrawable(bg, fg);
            background = null;
            foreground = null;
        } else {
            adaptive = null;
            background = bg;
            foreground = fg;
        }
        setBounds(0, 0, size, size);
    }

    public Path getShape() {
        return shape;
    }

    @Override
    protected void onBoundsChange(@NonNull Rect bounds) {
        super.onBoundsChange(bounds);
        shape.rewind();
        if (adaptive != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            adaptive.setBounds(bounds);
            Path mask = ((AdaptiveIconDrawable) adaptive).getIconMask();
            squareMask = isSquare(mask, bounds);
            if (squareMask) {
                addSquircle(shape, bounds);
                outside.set(shape);
                outside.setFillType(Path.FillType.INVERSE_WINDING);
            } else {
                shape.set(mask);
            }
        } else {
            shape.addCircle(bounds.exactCenterX(), bounds.exactCenterY(), Math.min(bounds.width(), bounds.height()) / 2f, Path.Direction.CW);
        }
    }

    private static boolean isSquare(Path mask, Rect bounds) {
        Region region = new Region();
        region.setPath(mask, new Region(bounds));
        int inset = Math.max(1, Math.round(bounds.width() * 0.03f));
        return region.contains(bounds.left + inset, bounds.top + inset);
    }

    private static void addSquircle(Path path, Rect bounds) {
        float cx = bounds.exactCenterX();
        float cy = bounds.exactCenterY();
        float r = Math.min(bounds.width(), bounds.height()) / 2f;
        int points = 96;
        double exponent = 2 / SQUIRCLE_POWER;
        for (int i = 0; i < points; i++) {
            double t = 2 * Math.PI * i / points;
            double cos = Math.cos(t), sin = Math.sin(t);
            float x = cx + r * (float) (Math.signum(cos) * Math.pow(Math.abs(cos), exponent));
            float y = cy + r * (float) (Math.signum(sin) * Math.pow(Math.abs(sin), exponent));
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.close();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (adaptive != null) {
            if (!squareMask || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                adaptive.draw(canvas);
                return;
            }
            if (maskPaint == null) {
                maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
            }
            Rect bounds = getBounds();
            int cx = bounds.centerX();
            int cy = bounds.centerY();
            int half = (int) (Math.min(bounds.width(), bounds.height()) * LAYER_SCALE / 2f);
            int count = canvas.saveLayer(bounds.left, bounds.top, bounds.right, bounds.bottom, null);
            AdaptiveIconDrawable icon = (AdaptiveIconDrawable) adaptive;
            Drawable layerBackground = icon.getBackground();
            Drawable layerForeground = icon.getForeground();
            if (layerBackground != null) {
                layerBackground.setBounds(cx - half, cy - half, cx + half, cy + half);
                layerBackground.draw(canvas);
            }
            if (layerForeground != null) {
                layerForeground.setBounds(cx - half, cy - half, cx + half, cy + half);
                layerForeground.draw(canvas);
            }
            canvas.drawPath(outside, maskPaint);
            canvas.restoreToCount(count);
            return;
        }
        Rect bounds = getBounds();
        if (foreground == null) {
            if (background != null) {
                background.setBounds(bounds);
                background.draw(canvas);
            }
            return;
        }
        int cx = bounds.centerX();
        int cy = bounds.centerY();
        int half = (int) (Math.min(bounds.width(), bounds.height()) * LAYER_SCALE / 2f);

        canvas.save();
        canvas.clipPath(shape);
        if (background != null) {
            background.setBounds(cx - half, cy - half, cx + half, cy + half);
            background.draw(canvas);
        }
        foreground.setBounds(cx - half, cy - half, cx + half, cy + half);
        foreground.draw(canvas);
        canvas.restore();
    }

    @Override
    public int getIntrinsicWidth() {
        return size;
    }

    @Override
    public int getIntrinsicHeight() {
        return size;
    }

    @Override
    public void setAlpha(int alpha) {
        if (adaptive != null) adaptive.setAlpha(alpha);
        if (background != null) background.setAlpha(alpha);
        if (foreground != null) foreground.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        if (adaptive != null) adaptive.setColorFilter(colorFilter);
        if (background != null) background.setColorFilter(colorFilter);
        if (foreground != null) foreground.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

}