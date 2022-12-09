package uz.unnarsx.cherrygram.translator;

import android.text.TextUtils;

import org.telegram.messenger.LanguageDetector;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class TelegramTranslator extends BaseTranslator {

    private final List<String> targetLanguages = Arrays.asList(
            "sq", "ar", "am", "az", "ga", "et", "or", "eu", "be", "bg", "is", "pl", "bs",
            "fa", "af", "tt", "da", "de", "ru", "fr", "tl", "fi", "fy", "km", "ka", "gu",
            "kk", "ht", "ko", "ha", "nl", "ky", "gl", "ca", "cs", "kn", "co", "hr", "ku",
            "la", "lv", "lo", "lt", "lb", "rw", "ro", "mg", "mt", "mr", "ml", "ms", "mk",
            "mi", "mn", "bn", "my", "hmn", "xh", "zu", "ne", "no", "pa", "pt", "ps", "ny",
            "ja", "sv", "sm", "sr", "st", "si", "eo", "sk", "sl", "sw", "gd", "ceb", "so",
            "tg", "te", "ta", "th", "tr", "tk", "cy", "ug", "ur", "uk", "uz", "es", "iw",
            "el", "haw", "sd", "hu", "sn", "hy", "ig", "it", "yi", "hi", "su", "id", "jw",
            "en", "yo", "vi", "zh-TW", "zh-CN", "zh");


    static TelegramTranslator getInstance() {
        return new TelegramTranslator();
    }

    @Override
    protected Result translate(String query, String tl) throws Exception {
        AtomicReference<Exception> exception = new AtomicReference<>();
        AtomicReference<String> detectedLanguage = new AtomicReference<>();
        final CountDownLatch waitDetect = new CountDownLatch(1);

        if (LanguageDetector.hasSupport()) {
            LanguageDetector.detectLanguage(query, lng -> {
                if (!Objects.equals(lng, "und")) {
                    detectedLanguage.set(lng);
                }
                waitDetect.countDown();
            }, e -> {
                exception.set(e);
                waitDetect.countDown();
            });
            waitDetect.await();
            if (exception.get() != null) {
                throw exception.get();
            }
        }

        ArrayList<String> blocks = getStringBlocks(query, 2500);
        StringBuilder resultString = new StringBuilder();

        for (String block : blocks) {
            final CountDownLatch waitTranslate = new CountDownLatch(1);
            AtomicReference<String> translated = new AtomicReference<>();

            TLRPC.TL_messages_translateText req = new TLRPC.TL_messages_translateText();
            req.flags |= 2;
            req.to_lang = tl;
            req.text = block.replace("\n", "<br>");
            ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(req, (res, err) -> {
                if (res instanceof TLRPC.TL_messages_translateResultText) {
                    TLRPC.TL_messages_translateResultText result = (TLRPC.TL_messages_translateResultText) res;
                    translated.set(result.text);
                } else if (err != null) {
                    exception.set(new Exception(err.text));
                } else {
                    exception.set(new Exception("Unknown error"));
                }
                waitTranslate.countDown();
            });
            waitTranslate.await();
            if (exception.get() != null) {
                throw exception.get();
            }
            resultString.append(buildTranslatedString(block, translated.get().replace("<br>", "\n")));
        }
        return new Result(resultString.toString(), detectedLanguage.get());
    }

    @Override
    public String convertLanguageCode(String language, String country) {
        String languageLowerCase = language.toLowerCase();
        String code;
        if (!TextUtils.isEmpty(country)) {
            String countryUpperCase = country.toUpperCase();
            if (targetLanguages.contains(languageLowerCase + "-" + countryUpperCase)) {
                code = languageLowerCase + "-" + countryUpperCase;
            } else if (languageLowerCase.equals("zh")) {
                if (countryUpperCase.equals("DG")) {
                    code = "zh-CN";
                } else if (countryUpperCase.equals("HK")) {
                    code = "zh-TW";
                } else {
                    code = languageLowerCase;
                }
            } else {
                code = languageLowerCase;
            }
        } else {
            code = languageLowerCase;
        }
        return code;
    }

    @Override
    public List<String> getTargetLanguages() {
        return targetLanguages;
    }
}
