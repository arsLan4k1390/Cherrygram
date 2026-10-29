/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.misc;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import org.telegram.messenger.NotchInfoUtils;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

import java.util.Arrays;

@RequiresApi(api = Build.VERSION_CODES.P)
public class LogoOverlayView extends FrameLayout {

    private static final String TAG = "cg_logo_overlay";

    private static final boolean DEBUG = false;

    private static final int LOGO_SAMPLE_SIDE = 64;
    private static final int ALPHA_THRESHOLD = 8;
    private static final float EDGE_MARGIN_PX = 1f;
    private static final float LOGO_SHRINK = 0.90f;
    private static final boolean ANCHOR_INNER_EDGE = true;

    private static final float CAMERA_DIAMETER_RATIO = 0.80f;
    private static final float CAMERA_CENTER_FROM_INNER = 0.75f;

    private static final float LOGO_NUDGE_TO_EDGE_DP = 1f;

    private ImageView logoView;

    private float[] logoPts = new float[0];
    private float logoAr = 1f;
    private float logoCu = .5f;
    private float logoCv = .5f;

    private final RectF fitRect = new RectF();
    private boolean hasFit;

    private boolean shapeIsAccurate;

    private final Path dbgShape = new Path();
    private final RectF dbgBounds = new RectF();
    private final Paint dbgPaint = new Paint();
    private final Paint dbgTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private String dbgSrc = "";
    private String dbgReport = "";
    private float dbgStatusBarPx;
    @Nullable private NotchInfoUtils.NotchInfo dbgNotch;

    public static void apply(Activity activity, boolean enabled) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P || activity == null) return;

        Window window = activity.getWindow();
        ViewGroup decor = (ViewGroup) window.getDecorView();
        View existing = decor.findViewWithTag(TAG);

        if (enabled) {
            if (existing != null) return;

            decor.setSystemUiVisibility(decor.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            WindowManager.LayoutParams lp = window.getAttributes();
            if (lp.layoutInDisplayCutoutMode != WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES) {
                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                window.setAttributes(lp);
            }

            LogoOverlayView view = new LogoOverlayView(activity);
            view.setTag(TAG);
            decor.addView(view);
            decor.requestApplyInsets();
        } else if (existing != null) {
            decor.removeView(existing);
        }
    }

    public LogoOverlayView(Context context) {
        super(context);
        init();
    }

    public LogoOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setWillNotDraw(!DEBUG);

        logoView = new ImageView(getContext());
        logoView.setScaleType(ImageView.ScaleType.FIT_XY);
        logoView.setVisibility(View.INVISIBLE);
        addView(logoView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        loadLogo(!Theme.isCurrentThemeDay() ? R.drawable.cg_logo_notch_white : R.drawable.cg_logo_notch);
    }

    public void loadLogo(@DrawableRes int res) {
        logoView.setImageResource(res);

        Drawable d = ContextCompat.getDrawable(getContext(), res);
        if (d == null) return;
        d = d.mutate();

        int iw = d.getIntrinsicWidth() > 0 ? d.getIntrinsicWidth() : LOGO_SAMPLE_SIDE;
        int ih = d.getIntrinsicHeight() > 0 ? d.getIntrinsicHeight() : LOGO_SAMPLE_SIDE;
        logoAr = iw / (float) ih;

        int mw, mh;
        if (logoAr >= 1f) {
            mw = LOGO_SAMPLE_SIDE;
            mh = Math.max(1, Math.round(LOGO_SAMPLE_SIDE / logoAr));
        } else {
            mh = LOGO_SAMPLE_SIDE;
            mw = Math.max(1, Math.round(LOGO_SAMPLE_SIDE * logoAr));
        }

        Bitmap bmp = Bitmap.createBitmap(mw, mh, Bitmap.Config.ARGB_8888);
        d.setBounds(0, 0, mw, mh);
        d.draw(new Canvas(bmp));
        int[] px = new int[mw * mh];
        bmp.getPixels(px, 0, mw, 0, 0, mw, mh);
        bmp.recycle();

        float[] tmp = new float[mw * mh * 8];
        int n = 0;
        float minU = 1f, maxU = 0f, minV = 1f, maxV = 0f;
        for (int y = 0; y < mh; y++) {
            for (int x = 0; x < mw; x++) {
                if (!opaque(px, mw, mh, x, y)) continue;
                if (opaque(px, mw, mh, x - 1, y) && opaque(px, mw, mh, x + 1, y)
                        && opaque(px, mw, mh, x, y - 1) && opaque(px, mw, mh, x, y + 1)
                ) continue;

                for (int c = 0; c < 4; c++) {
                    float u = (x + (c & 1)) / (float) mw;
                    float v = (y + (c >> 1)) / (float) mh;
                    tmp[n++] = u;
                    tmp[n++] = v;
                    minU = Math.min(minU, u);
                    maxU = Math.max(maxU, u);
                    minV = Math.min(minV, v);
                    maxV = Math.max(maxV, v);
                }
            }
        }
        if (n == 0) {
            logoPts = new float[]{0f, 0f, 1f, 0f, 0f, 1f, 1f, 1f};
            logoCu = logoCv = .5f;
        } else {
            logoPts = Arrays.copyOf(tmp, n);
            logoCu = (minU + maxU) / 2f;
            logoCv = (minV + maxV) / 2f;
        }
    }

    private static boolean opaque(int[] px, int w, int h, int x, int y) {
        return x >= 0 && y >= 0 && x < w && y < h && (px[y * w + x] >>> 24) >= ALPHA_THRESHOLD;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        requestApplyInsets();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        requestApplyInsets();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        requestApplyInsets();
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        updateFit(insets);
        if (DEBUG) buildDebugReport();
        requestLayout();
        return super.onApplyWindowInsets(insets);
    }

    @Nullable
    private static Path shapeFromCutout(@Nullable DisplayCutout dc) {
        if (dc == null) return null;

        Rect best = null;
        for (Rect r : dc.getBoundingRects()) {
            if (r.isEmpty()) continue;
            if (best == null || r.width() * r.height() > best.width() * best.height()) best = r;
        }
        if (best == null) return null;

        RectF r = new RectF(best);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Path cp = dc.getCutoutPath();
            if (cp != null && !cp.isEmpty()) {
                Path clip = new Path();
                clip.addRect(r, Path.Direction.CW);
                Path res = new Path(cp);
                if (res.op(clip, Path.Op.INTERSECT) && !res.isEmpty()) return res;
            }
        }
        return syntheticShape(r);
    }

    private static Path syntheticShape(RectF r) {
        Path p = new Path();
        if (r.width() <= r.height() * 1.2f) {
            p.addOval(r, Path.Direction.CW);
        } else {
            float rad = Math.min(r.width(), r.height()) / 2f;
            p.addRoundRect(r, rad, rad, Path.Direction.CW);
        }
        return p;
    }

    private Path refineWithNotchInfo(Path shape) {
        Display display = getDisplay();
        if (display == null || display.getRotation() != Surface.ROTATION_0) return shape;

        NotchInfoUtils.NotchInfo info = NotchInfoUtils.getInfo(getContext());
        if (DEBUG) dbgNotch = info;
        if (info == null || info.path == null || info.bounds == null || info.bounds.isEmpty()
                || !info.isAccurate
        ) {
            return shape;
        }

        RectF sb = new RectF();
        shape.computeBounds(sb, true);

        float tol = 3f * getResources().getDisplayMetrics().density;
        RectF grown = new RectF(sb);
        grown.inset(-tol, -tol);

        if (!grown.contains(info.bounds) || Math.abs(info.bounds.centerX() - sb.centerX()) > tol) {
            return shape;
        }

        shapeIsAccurate = true;
        return new Path(info.path);
    }

    @Nullable
    private Path resolveCutoutShape(@Nullable WindowInsets insets) {
        Path shape;

        WindowInsets root = getRootWindowInsets();
        shape = shapeFromCutout(root != null ? root.getDisplayCutout() : null);
        if (shape != null) {
            if (DEBUG) dbgSrc = "root";
            return refineWithNotchInfo(shape);
        }

        shape = shapeFromCutout(insets != null ? insets.getDisplayCutout() : null);
        if (shape != null) {
            if (DEBUG) dbgSrc = "dispatched";
            return refineWithNotchInfo(shape);
        }

        Display display = getDisplay();
        if (display != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            shape = shapeFromCutout(display.getCutout());
            if (shape != null) {
                if (DEBUG) dbgSrc = "display";
                return refineWithNotchInfo(shape);
            }
        }

        if (display != null && display.getRotation() == Surface.ROTATION_0) {
            NotchInfoUtils.NotchInfo info = NotchInfoUtils.getInfo(getContext());
            if (DEBUG) dbgNotch = info;
            if (info != null && info.bounds != null && !info.bounds.isEmpty()) {
                if (DEBUG) dbgSrc = "resource";
                if (info.isAccurate && info.path != null) {
                    shapeIsAccurate = true;
                    return new Path(info.path);
                }
                return syntheticShape(info.bounds);
            }
        }

        if (DEBUG) dbgSrc = "none";
        return null;
    }

    private Path anchorToInnerEdge(Path shape) {
        RectF b = new RectF();
        shape.computeBounds(b, true);
        float w = b.width(), h = b.height();
        if (Math.abs(w - h) < 1f) return shape;

        DisplayMetrics dm = getResources().getDisplayMetrics();
        float vw = getWidth() > 0 ? getWidth() : dm.widthPixels;
        float vh = getHeight() > 0 ? getHeight() : dm.heightPixels;
        final float tol = 2f;

        boolean top = b.top <= tol;
        boolean bottom = b.bottom >= vh - tol;
        boolean left = b.left <= tol;
        boolean right = b.right >= vw - tol;

        boolean tallRect = Math.max(w, h) > Math.min(w, h) * 1.5f;
        RectF out = new RectF(b);

        if (h > w && (top ^ bottom)) {
            if (tallRect) {
                float d = w * CAMERA_DIAMETER_RATIO;
                float c = top ? b.bottom - w * CAMERA_CENTER_FROM_INNER : b.top + w * CAMERA_CENTER_FROM_INNER;
                return ovalPath(new RectF(b.centerX() - d / 2f, c - d / 2f, b.centerX() + d / 2f, c + d / 2f));
            }
            if (top) out.top = b.bottom - w;
            else out.bottom = b.top + w;
            return ovalPath(out);
        }

        if (w > h && (left ^ right)) {
            if (tallRect) {
                float d = h * CAMERA_DIAMETER_RATIO;
                float c = left ? b.right - h * CAMERA_CENTER_FROM_INNER : b.left + h * CAMERA_CENTER_FROM_INNER;
                return ovalPath(new RectF(c - d / 2f, b.centerY() - d / 2f, c + d / 2f, b.centerY() + d / 2f));
            }
            if (left) out.left = b.right - h;
            else out.right = b.left + h;
            return ovalPath(out);
        }

        if (tallRect) return shape;
        if (h > w) {
            if (b.top <= vh - b.bottom) out.top = b.bottom - w;
            else out.bottom = b.top + w;
        } else {
            if (b.left <= vw - b.right) out.left = b.right - h;
            else out.right = b.left + h;
        }
        return ovalPath(out);
    }

    private void nudgeTowardEdge(Path shape, float px) {
        if (px == 0f) return;

        RectF b = new RectF();
        shape.computeBounds(b, true);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        float vw = getWidth() > 0 ? getWidth() : dm.widthPixels;
        float vh = getHeight() > 0 ? getHeight() : dm.heightPixels;

        float dTop = b.top, dBottom = vh - b.bottom, dLeft = b.left, dRight = vw - b.right;
        float min = Math.min(Math.min(dTop, dBottom), Math.min(dLeft, dRight));

        if (min == dTop) shape.offset(0, -px);
        else if (min == dBottom) shape.offset(0, px);
        else if (min == dLeft) shape.offset(-px, 0);
        else shape.offset(px, 0);
    }

    private static Path ovalPath(RectF r) {
        Path p = new Path();
        p.addOval(r, Path.Direction.CW);
        return p;
    }

    private void updateFit(@Nullable WindowInsets insets) {
        hasFit = false;
        shapeIsAccurate = false;
        if (DEBUG) {
            dbgBounds.setEmpty();
            dbgShape.reset();
            dbgNotch = null;
        }

        Path shape = resolveCutoutShape(insets);
        if (shape == null) return;

        if (DEBUG) shape.computeBounds(dbgBounds, true);
        if (ANCHOR_INNER_EDGE && !shapeIsAccurate) {
            shape = anchorToInnerEdge(shape);
            nudgeTowardEdge(shape, LOGO_NUDGE_TO_EDGE_DP * getResources().getDisplayMetrics().density);
        }
        if (DEBUG) dbgShape.set(shape);

        RectF b = new RectF();
        shape.computeBounds(b, true);
        if (b.isEmpty()) return;

        int mw = Math.max(1, (int) Math.ceil(b.width()));
        int mh = Math.max(1, (int) Math.ceil(b.height()));

        Path local = new Path(shape);
        local.offset(-b.left, -b.top);

        Bitmap mask = Bitmap.createBitmap(mw, mh, Bitmap.Config.ARGB_8888);
        Paint p = new Paint();
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.BLACK);
        new Canvas(mask).drawPath(local, p);
        int[] px = new int[mw * mh];
        mask.getPixels(px, 0, mw, 0, 0, mw, mh);
        mask.recycle();

        float cx = b.centerX();
        float cy = b.centerY();

        int lo = 0;
        int hi = (int) Math.ceil(Math.max(mw, mh * logoAr) * 2f);
        while (hi - lo > 1) {
            int mid = (lo + hi) >>> 1;
            if (fits(px, mw, mh, b, cx, cy, mid)) lo = mid; else hi = mid;
        }
        if (lo < 2) return;

        int w = Math.max(2, Math.round(lo * LOGO_SHRINK));
        int h = Math.max(1, Math.round(w / logoAr));
        int left = Math.round(cx - logoCu * w);
        int top = Math.round(cy - logoCv * h);
        fitRect.set(left, top, left + w, top + h);
        hasFit = true;
    }

    private boolean fits(int[] mask, int mw, int mh, RectF b, float cx, float cy, int w) {
        int h = Math.max(1, Math.round(w / logoAr));
        int left = Math.round(cx - logoCu * w);
        int top = Math.round(cy - logoCv * h);

        for (int i = 0; i < logoPts.length; i += 2) {
            float x = left + logoPts[i] * w - b.left;
            float y = top + logoPts[i + 1] * h - b.top;
            if (!solid(mask, mw, mh, x, y)
                    || !solid(mask, mw, mh, x - EDGE_MARGIN_PX, y)
                    || !solid(mask, mw, mh, x + EDGE_MARGIN_PX, y)
                    || !solid(mask, mw, mh, x, y - EDGE_MARGIN_PX)
                    || !solid(mask, mw, mh, x, y + EDGE_MARGIN_PX)
            ) {
                return false;
            }
        }
        return true;
    }

    private static boolean solid(int[] mask, int mw, int mh, float fx, float fy) {
        int x = (int) Math.floor(fx);
        int y = (int) Math.floor(fy);
        return x >= 0 && y >= 0 && x < mw && y < mh && (mask[y * mw + x] >>> 24) != 0;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!hasFit) {
            logoView.setVisibility(View.INVISIBLE);
            if (DEBUG) invalidate();
            return;
        }

        int[] loc = new int[2];
        getLocationInWindow(loc);

        int l = Math.round(fitRect.left - loc[0]);
        int t = Math.round(fitRect.top - loc[1]);
        int w = Math.max(1, Math.round(fitRect.width()));
        int h = Math.max(1, Math.round(fitRect.height()));

        logoView.measure(
                MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY)
        );
        logoView.layout(l, t, l + w, t + h);
        logoView.setVisibility(View.VISIBLE);

        if (DEBUG) invalidate();
    }

    private void buildDebugReport() {
        float d = getResources().getDisplayMetrics().density;
        RectF s = new RectF();
        dbgShape.computeBounds(s, true);

        WindowInsets root = getRootWindowInsets();
        int sbInset = root != null ? root.getSystemWindowInsetTop() : 0;
        int sbRes = 0;
        int id = getResources().getIdentifier("status_bar_height_portrait", "dimen", "android");
        if (id == 0) id = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (id != 0) sbRes = getResources().getDimensionPixelSize(id);
        dbgStatusBarPx = sbRes > 0 ? sbRes : sbInset;

        dbgReport = "src=" + dbgSrc + " hasFit=" + hasFit
                + "\nraw=" + dbgBounds + "  " + dp1(dbgBounds.width(), d) + "x" + dp1(dbgBounds.height(), d) + "dp"
                + "\nshape=" + s + "  " + dp1(s.width(), d) + "x" + dp1(s.height(), d) + "dp"
                + "\nfit=" + fitRect + "  " + dp1(fitRect.width(), d) + "dp"
                + "\nstatusBar inset=" + sbInset + " res=" + sbRes + " (half=" + (dbgStatusBarPx / 2f) + ")"
                + "\ncu/cv=" + logoCu + "/" + logoCv + " ar=" + logoAr
                + "\nnotch=" + (dbgNotch == null ? "null"
                : dbgNotch.bounds + " acc=" + dbgNotch.isAccurate + " usedAccurate=" + shapeIsAccurate)
                + "\nspec=" + (dbgNotch == null || dbgNotch.rawPath == null ? ""
                : dbgNotch.rawPath.substring(0, Math.min(90, dbgNotch.rawPath.length())))
                + "\nview=" + getWidth() + "x" + getHeight() + " density=" + d
                + " rot=" + (getDisplay() != null ? getDisplay().getRotation() : -1);

        Log.w(TAG, dbgReport.replace('\n', ' '));
    }

    private static float dp1(float px, float density) {
        return Math.round(px / density * 10f) / 10f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!DEBUG) return;

        int[] loc = new int[2];
        getLocationInWindow(loc);
        float d = getResources().getDisplayMetrics().density;

        canvas.save();
        canvas.translate(-loc[0], -loc[1]);

        dbgPaint.setStyle(Paint.Style.STROKE);
        dbgPaint.setStrokeWidth(1f);

        dbgPaint.setColor(Color.CYAN);
        float mid = dbgStatusBarPx / 2f;
        canvas.drawLine(0, mid, getWidth(), mid, dbgPaint);
        canvas.drawLine(mid, 0, mid, getHeight(), dbgPaint);

        if (hasFit) {
            dbgPaint.setColor(Color.RED);
            canvas.drawPath(dbgShape, dbgPaint);

            dbgPaint.setColor(Color.GREEN);
            canvas.drawRect(fitRect, dbgPaint);
        }
        canvas.restore();

        dbgTextPaint.setColor(Color.YELLOW);
        dbgTextPaint.setTextSize(11f * d);
        dbgTextPaint.setShadowLayer(3f, 0f, 0f, Color.BLACK);
        float y = 120f * d;
        for (String line : dbgReport.split("\n")) {
            canvas.drawText(line, 12f * d, y, dbgTextPaint);
            y += 14f * d;
        }
    }

}