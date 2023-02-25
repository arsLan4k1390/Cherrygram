package uz.unnarsx.cherrygram.helpers;

import android.graphics.Typeface;

import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmojiHelper {

    private static Typeface systemEmojiTypeface;
    private static boolean loadSystemEmojiFailed = false;

    public static Typeface getSystemEmojiTypeface() {
        if (!loadSystemEmojiFailed && systemEmojiTypeface == null) {
            try {
                Pattern p = Pattern.compile(">(.*emoji.*)</font>", Pattern.CASE_INSENSITIVE);
                BufferedReader br = new BufferedReader(new FileReader("/system/etc/fonts.xml"));
                String line;
                while ((line = br.readLine()) != null) {
                    Matcher m = p.matcher(line);
                    if (m.find()) {
                        systemEmojiTypeface = Typeface.createFromFile("/system/fonts/" + m.group(1));
                        if (BuildVars.DEBUG_VERSION) {
                            FileLog.d("emoji font file fonts.xml = " + m.group(1));
                        }
                        break;
                    }
                }
                br.close();
            } catch (Exception e) {
                FileLog.e(e);
            }
            if (systemEmojiTypeface == null) {
                try {
                    systemEmojiTypeface = Typeface.createFromFile("/system/fonts/" + "NotoColorEmoji.ttf");
                    if (BuildVars.DEBUG_VERSION) {
                        FileLog.d("emoji font file = " + "NotoColorEmoji.ttf");
                    }
                } catch (Exception e) {
                    FileLog.e(e);
                    loadSystemEmojiFailed = true;
                }
            }
        }
        return systemEmojiTypeface;
    }

}
