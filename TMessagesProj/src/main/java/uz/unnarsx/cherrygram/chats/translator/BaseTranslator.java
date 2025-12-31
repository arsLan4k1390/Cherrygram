/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.chats.translator;

import android.os.SystemClock;
import android.util.LruCache;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.tgnet.TLRPC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig;

abstract public class BaseTranslator {

    protected final LruCache<Pair<Object, String>, Result> cache = new LruCache<>(200);

    protected String token;

    abstract protected Result singleTranslate(Object query, String tl) throws Exception;

    protected ArrayList<Result> multiTranslate(ArrayList<Object> translations, String tl) throws Exception {
        int count = translations.size();
        final CountDownLatch semaphore = new CountDownLatch(count);
        final ArrayList<Result> results = new ArrayList<>(Collections.nCopies(count, null));
        AtomicReference<Exception> exception = new AtomicReference<>();
        for (int i = 0; i < count; i++) {
            final int j = i;
            new Thread() {
                @Override
                public void run() {
                    if (exception.get() == null) {
                        // retry 250 * 4 * 60 = 60 seconds
                        for (int k = 0; k < 250 * 4 * 60; k++) {
                            try {
                                results.set(j, preProcessTranslation(translations.get(j), tl));
                            } catch (Http429Exception e) {
                                exception.set(e);
                            } catch (Exception e) {
                                SystemClock.sleep(250);
                                continue;
                            }
                            break;
                        }
                    }
                    semaphore.countDown();
                }
            }.start();
        }
        semaphore.await();
        if (exception.get() != null) {
            throw exception.get();
        }
        return results;
    }

    abstract public List<String> getTargetLanguages();

    public String convertLanguageCode(String language, String country) {
        return language;
    }

    public static String stringFromTranslation(Object translation) throws UnsupportedOperationException {
        if (translation instanceof TLRPC.TL_textWithEntities) {
            return ((TLRPC.TL_textWithEntities) translation).text;
        } else if (translation instanceof CharSequence) {
            return (String) translation;
        }
        throw new UnsupportedOperationException("Unsupported translation result type");
    }

    protected String getTopLanguage(ArrayList<Result> results) {
        Map<String, Long> topLanguages = results.stream()
                .map(result -> result.sourceLanguage)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        Map.Entry<String, Long> mostUsedLanguage = topLanguages.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
        if (mostUsedLanguage != null) {
            return mostUsedLanguage.getKey();
        } else {
            return null;
        }
    }

    protected Result preProcessTranslation(Object query, String toLang) throws Exception {
        Pair<Object, String> key = new Pair<>(query, toLang);
        Result cacheRes = cache.get(key);
        if (cacheRes != null) {
            return cacheRes;
        }
        Result resTranslation = null;
        if (query instanceof CharSequence || query instanceof TLRPC.TL_textWithEntities) {
            resTranslation = singleTranslate(query, toLang);
        }
        cache.put(key, resTranslation);
        if (resTranslation != null) {
            return resTranslation;
        }
        throw new UnsupportedOperationException("Unsupported translation query: " + query.getClass().getSimpleName());
    }

    public void startTask(ArrayList<Object> translations, String toLang, Translator.MultiTranslateCallBack translateCallBack, String token) {
        new Thread() {
            @Override
            public void run() {
                try {
                    BaseTranslator.this.token = token;
                    ArrayList<Result> results = multiTranslate(translations, toLang);
                    AndroidUtilities.runOnUIThread(() -> translateCallBack.onSuccess(null, results));
                } catch (Exception e) {
                    e.printStackTrace();
                    FileLog.e(e, false);
                    AndroidUtilities.runOnUIThread(() -> translateCallBack.onSuccess(e, null));
                }
            }
        }.start();
    }

    public boolean supportLanguage(String language) {
        return getTargetLanguages().contains(language);
    }

    public String getCurrentAppLanguage() {
        String toLang;
        Locale locale = LocaleController.getInstance().getCurrentLocale();
        toLang = convertLanguageCode(locale.getLanguage(), locale.getCountry());
        if (!supportLanguage(toLang)) {
            toLang = convertLanguageCode("en", null);
        }
        return toLang;
    }

    public String getTargetLanguage(String language) {
        String toLang;
        if (language.equals("app")) {
            toLang = getCurrentAppLanguage();
        } else {
            toLang = language;
        }
        return toLang;
    }

    public String getCurrentTargetLanguage() {
        return getTargetLanguage(CherrygramChatsConfig.INSTANCE.getTranslationTarget());
    }

    public String getCurrentTargetKeyboardLanguage() {
        return getTargetLanguage(CherrygramChatsConfig.INSTANCE.getTranslationKeyboardTarget());
    }

    public static class Http429Exception extends IOException {
    }

    public static class Result {
        public Object translation;
        @Nullable
        public Object additionalInfo;
        @Nullable
        public String sourceLanguage;

        public Result(Object translation, @Nullable String sourceLanguage) {
            this(translation, null, sourceLanguage);
        }

        public Result(Object translation, @Nullable Object additionalInfo, @Nullable String sourceLanguage) {
            this.translation = translation;
            this.additionalInfo = additionalInfo;
            this.sourceLanguage = sourceLanguage;
        }
    }

    public static class AdditionalObjectTranslation {
        public Object translation;
        public Object additionalInfo;
        public int messagesCount;
    }
}
