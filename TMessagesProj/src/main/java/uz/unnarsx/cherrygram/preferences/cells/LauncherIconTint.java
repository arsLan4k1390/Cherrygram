/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.preferences.cells;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.ui.LauncherIconController.LauncherIcon;

import java.util.EnumMap;

import uz.unnarsx.cherrygram.core.CherrygramLogger;

public final class LauncherIconTint {

    private static final int FADE_STOPS = 24;
    public static final int[] FADE_COLORS = new int[FADE_STOPS];
    public static final float[] FADE_POSITIONS = new float[FADE_STOPS];

    static {
        for (int i = 0; i < FADE_STOPS; i++) {
            float t = i / (float) (FADE_STOPS - 1);
            float alpha = 1f - t * t * (3f - 2f * t);
            FADE_POSITIONS[i] = t;
            FADE_COLORS[i] = Color.argb(Math.round(255 * alpha), 255, 255, 255);
        }
    }

    private static final EnumMap<LauncherIcon, Integer> colors = new EnumMap<>(LauncherIcon.class);

    private LauncherIconTint() {}

    public static int getColor(LauncherIcon icon) {
        Integer cached = colors.get(icon);
        if (cached != null) return cached;

        int color = 0xFFFFFFFF;
        try {
            int size = 24;
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            new LauncherIconDrawable(ApplicationLoader.applicationContext, icon, size).draw(new Canvas(bitmap));
            int[] pixels = new int[size * size];
            bitmap.getPixels(pixels, 0, size, 0, 0, size, size);
            bitmap.recycle();

            long r = 0, g = 0, b = 0, count = 0;
            float ar = 0, ag = 0, ab = 0, artTotal = 0;
            float[] hsv = new float[3];
            for (int pixel : pixels) {
                if (Color.alpha(pixel) < 200) continue;
                r += Color.red(pixel);
                g += Color.green(pixel);
                b += Color.blue(pixel);
                count++;

                Color.colorToHSV(pixel, hsv);
                if (hsv[1] > 0.25f && hsv[2] > 0.15f) {
                    float weight = hsv[1] * hsv[2];
                    ar += Color.red(pixel) * weight;
                    ag += Color.green(pixel) * weight;
                    ab += Color.blue(pixel) * weight;
                    artTotal += weight;
                }
            }
            if (count > 0) {
                int average = Color.rgb((int) (r / count), (int) (g / count), (int) (b / count));
                float[] hsl = new float[3];
                ColorUtils.colorToHSL(average, hsl);
                if (isGray(average) && hsl[2] > 0.7f && artTotal > 0) {
                    average = Color.rgb((int) (ar / artTotal), (int) (ag / artTotal), (int) (ab / artTotal));
                    ColorUtils.colorToHSL(average, hsl);
                }
                if (!isGray(average)) {
                    hsl[1] = Math.max(hsl[1], 0.55f);
                    hsl[2] = Math.min(Math.max(hsl[2], 0.38f), 0.55f);
                    color = ColorUtils.HSLToColor(hsl);
                }
            }
        } catch (Exception e) {
            CherrygramLogger.e(e);
        }
        colors.put(icon, color);
        return color;
    }

    public static int contentColor(int background) {
        return ColorUtils.calculateLuminance(background) > 0.55 ? 0xFF1C1C1E : 0xFFFFFFFF;
    }

    private static boolean isGray(int color) {
        int max = Math.max(Color.red(color), Math.max(Color.green(color), Color.blue(color)));
        int min = Math.min(Color.red(color), Math.min(Color.green(color), Color.blue(color)));
        return max - min < 16;
    }

}