/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.helpers.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

public class FontHelper {

    private static final String TEST_TEXT;
    private static final int CANVAS_SIZE = 40;
    private static final Paint PAINT = new Paint() {{
        setTextSize(20);
        setAntiAlias(false);
        setSubpixelText(false);
        setFakeBoldText(false);
    }};

    private static Boolean mediumWeightSupported = null;
    private static Boolean italicSupported = null;

    public static boolean loadSystemEmojiFailed = false;
    private static Typeface systemEmojiTypeface;

    static {
        var lang = LocaleController.getInstance().getCurrentLocale().getLanguage();
        if (List.of("zh", "ja", "ko").contains(lang)) {
            TEST_TEXT = "你好";
        } else if (List.of("ar", "fa").contains(lang)) {
            TEST_TEXT = "مرحبا";
        } else if ("iw".equals(lang)) {
            TEST_TEXT = "שלום";
        } else if ("th".equals(lang)) {
            TEST_TEXT = "สวัสดี";
        } else if ("hi".equals(lang)) {
            TEST_TEXT = "नमस्ते";
        } else if (List.of("ru", "uk", "ky", "be", "sr").contains(lang)) {
            TEST_TEXT = "Привет";
        } else {
            TEST_TEXT = "R";
        }
    }

    public static Typeface createTypeface(String assetPath) {
        return switch (assetPath) {
            case AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM ->
                    isMediumWeightSupported() ? Typeface.create("sans-serif-medium", Typeface.NORMAL) : Typeface.create("sans-serif", Typeface.BOLD);
            case AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM_ITALIC ->
                    isMediumWeightSupported() ? Typeface.create("sans-serif-medium", Typeface.ITALIC) : Typeface.create("sans-serif", Typeface.BOLD_ITALIC);
            case AndroidUtilities.TYPEFACE_ROBOTO_CONDENSED_BOLD ->
                    Typeface.create("sans-serif-condensed", Typeface.BOLD);
            case AndroidUtilities.TYPEFACE_ROBOTO_ITALIC ->
                    Build.VERSION.SDK_INT >= 28 ? Typeface.create(Typeface.SANS_SERIF, 400, true) : Typeface.create("sans-serif", Typeface.ITALIC);
            case AndroidUtilities.TYPEFACE_ROBOTO_MONO ->
                    Typeface.MONOSPACE;
            default -> Build.VERSION.SDK_INT >= 28 ? Typeface.create(Typeface.SANS_SERIF, 400, false) : Typeface.create("sans-serif", Typeface.NORMAL);
        };
    }

    public static Typeface createTypefaceFromAsset(String assetPath) {
        if (Build.VERSION.SDK_INT >= 26) {
            Typeface.Builder builder = new Typeface.Builder(ApplicationLoader.applicationContext.getAssets(), assetPath);
            if (assetPath.contains("medium")) {
                builder.setWeight(700);
            }
            if (assetPath.contains("italic")) {
                builder.setItalic(true);
            }
            return builder.build();
        } else {
            return Typeface.createFromAsset(ApplicationLoader.applicationContext.getAssets(), assetPath);
        }
    }

    public static boolean isMediumWeightSupported() {
        if (mediumWeightSupported == null) {
            mediumWeightSupported = testTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            FileLog.d("mediumWeightSupported = " + mediumWeightSupported);
        }
        return mediumWeightSupported;
    }

    public static boolean isItalicSupported() {
        if (italicSupported == null) {
            italicSupported = testTypeface(Typeface.create("sans-serif", Typeface.ITALIC));
            FileLog.d("italicSupported = " + italicSupported);
        }
        return italicSupported;
    }

    private static boolean testTypeface(Typeface typeface) {
        Canvas canvas = new Canvas();

        Bitmap bitmap1 = Bitmap.createBitmap(CANVAS_SIZE * 2, CANVAS_SIZE, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap1);
        PAINT.setTypeface(null);
        canvas.drawText(TEST_TEXT, 0, CANVAS_SIZE, PAINT);

        Bitmap bitmap2 = Bitmap.createBitmap(CANVAS_SIZE * 2, CANVAS_SIZE, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap2);
        PAINT.setTypeface(typeface);
        canvas.drawText(TEST_TEXT, 0, CANVAS_SIZE, PAINT);

        boolean supported = !bitmap1.sameAs(bitmap2);
        AndroidUtilities.recycleBitmaps(List.of(bitmap1, bitmap2));
        return supported;
    }

    public static File getSystemEmojiFontPath() {
        try (var br = new BufferedReader(new FileReader("/system/etc/fonts.xml"))) {
            String line;
            var ignored = false;
            while ((line = br.readLine()) != null) {
                var trimmed = line.trim();
                if (trimmed.startsWith("<family") && trimmed.contains("ignore=\"true\"")) {
                    ignored = true;
                } else if (trimmed.startsWith("</family>")) {
                    ignored = false;
                } else if (trimmed.startsWith("<font") && !ignored) {
                    var start = trimmed.indexOf(">");
                    var end = trimmed.indexOf("<", 1);
                    if (start > 0 && end > 0) {
                        var font = trimmed.substring(start + 1, end);
                        if (font.toLowerCase().contains("emoji")) {
                            File file = new File("/system/fonts/" + font);
                            if (file.exists()) {
                                FileLog.d("emoji font file fonts.xml = " + font);
                                return file;
                            }
                        }
                    }
                }
            }
            br.close();

            var fileAOSP = new File("/system/fonts/NotoColorEmoji.ttf");
            if (fileAOSP.exists()) {
                return fileAOSP;
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        return null;
    }

    public static Typeface getSystemEmojiTypeface() {
        if (!loadSystemEmojiFailed && systemEmojiTypeface == null) {
            var font = getSystemEmojiFontPath();
            if (font != null) {
                systemEmojiTypeface = Typeface.createFromFile(font);
            }
            if (systemEmojiTypeface == null) {
                loadSystemEmojiFailed = true;
            }
        }
        return systemEmojiTypeface;
    }
}

