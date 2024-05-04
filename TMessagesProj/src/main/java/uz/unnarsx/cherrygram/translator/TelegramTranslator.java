package uz.unnarsx.cherrygram.translator;

import android.text.TextUtils;

import org.telegram.messenger.LanguageDetector;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.TranslateAlert2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import kotlin.NotImplementedError;
import uz.unnarsx.cherrygram.helpers.chats.MessageHelper;

public class TelegramTranslator extends BaseTranslator {

    private final HashMap<String, HashMap<String, Integer>> translatingProcess = new HashMap<>();

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
    protected Result singleTranslate(Object query, String tl) {
        throw new NotImplementedError();
    }

    private ArrayList<Result> internalTranslate(ArrayList<Object> query, String tl, String subToken) throws Exception {
        int count = query.size();
        final CountDownLatch waitDetect = new CountDownLatch(count);
        final ArrayList<String> languages = new ArrayList<>(Collections.nCopies(count, null));
        final CountDownLatch waitTranslate = new CountDownLatch(1);
        ArrayList<Result> results = new ArrayList<>();
        final AtomicReference<Exception> exception = new AtomicReference<>();
        for (int i = 0; i < count; i++) {
            final int index = i;
            final Object q = query.get(i);
            LanguageDetector.detectLanguage(stringFromTranslation(q), lng -> {
                if (!Objects.equals(lng, "und")) {
                    languages.set(index, lng);
                }
                waitDetect.countDown();
            }, e -> {
                exception.set(e);
                waitDetect.countDown();
            });
        }
        waitDetect.await();
        if (exception.get() != null) {
            throw exception.get();
        }
        TLRPC.TL_messages_translateText req = new TLRPC.TL_messages_translateText();
        req.flags |= 2;
        req.to_lang = tl;
        for (int i = 0; i < count; i++) {
            Object q = query.get(i);
            TLRPC.TL_textWithEntities textWithEntities;
            if (q instanceof TLRPC.TL_textWithEntities) {
                textWithEntities = (TLRPC.TL_textWithEntities) q;
            } else {
                textWithEntities = new TLRPC.TL_textWithEntities();
                textWithEntities.text = stringFromTranslation(q);
            }
            req.text.add(textWithEntities);
        }
        int reqId = ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(req, (res, err) -> {
            if (res instanceof TLRPC.TL_messages_translateResult &&
                    !((TLRPC.TL_messages_translateResult) res).result.isEmpty()) {
                TLRPC.TL_messages_translateResult result = (TLRPC.TL_messages_translateResult) res;
                for (int i = 0; i < count; i++) {
                    results.add(new Result(TranslateAlert2.preprocess(req.text.get(i), result.result.get(i)), languages.get(i)));
                }
            } else if (err != null) {
                exception.set(new Exception(err.text));
            } else {
                exception.set(new Exception("Unknown error"));
            }
            waitTranslate.countDown();
            removeTokenTranslation(subToken);
        });
        addSubTokenTranslation(subToken, reqId);
        waitTranslate.await();
        if (exception.get() != null) {
            throw exception.get();
        }
        return results;
    }

    private void removeTokenTranslation(String subToken) {
        HashMap<String, Integer> map = translatingProcess.get(token);
        if (map != null) {
            map.remove(subToken);
        }
        if (map != null && map.isEmpty()) {
            translatingProcess.remove(token);
        }
    }

    private void addSubTokenTranslation(String subToken, int reqId) {
        HashMap<String, Integer> map = translatingProcess.get(token);
        if (map == null) {
            map = new HashMap<>();
            translatingProcess.put(token, map);
        }
        map.put(subToken, reqId);
    }

    @Override
    protected ArrayList<Result> multiTranslate(ArrayList<Object> translations, String tl) throws Exception {
        int count = translations.size();
        ArrayList<ArrayList<Object>> chunks = new ArrayList<>();
        final HashMap<Integer, Result> rawResults = new HashMap<>();
        final ArrayList<Result> results = new ArrayList<>();
        AtomicReference<Exception> exception = new AtomicReference<>();
        ArrayList<Object> textMessagesQuery = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Object q = translations.get(i);
            if (q instanceof TLRPC.TL_textWithEntities || q instanceof CharSequence) {
                textMessagesQuery.add(q);
            } else if (q instanceof AdditionalObjectTranslation) {
                AdditionalObjectTranslation res = (AdditionalObjectTranslation) q;
                Object translationData = res.translation;
                if (translationData instanceof String || translationData instanceof TLRPC.TL_textWithEntities) {
                    textMessagesQuery.add(translationData);
                    if (res.additionalInfo instanceof MessageHelper.ReplyMarkupButtonsTexts) {
                        MessageHelper.ReplyMarkupButtonsTexts buttonRows = (MessageHelper.ReplyMarkupButtonsTexts) res.additionalInfo;
                        for (int x = 0; x < buttonRows.getTexts().size(); x++) {
                            textMessagesQuery.addAll(buttonRows.getTexts().get(x));
                        }
                    }
                } else if (translationData instanceof MessageHelper.PollTexts) {
                    textMessagesQuery.addAll(new ArrayList<>(((MessageHelper.PollTexts) translationData).getTexts()));
                }
            }
        }
        int maxSize = UserConfig.getInstance(UserConfig.selectedAccount).isPremium() ? 20:1;
        for (int i = 0; i < textMessagesQuery.size(); i += maxSize) {
            chunks.add(new ArrayList<>(textMessagesQuery.subList(i, Math.min(i + maxSize, textMessagesQuery.size()))));
        }
        final CountDownLatch semaphore = new CountDownLatch(chunks.size());
        for (ArrayList<Object> chunk : chunks) {
            new Thread(() -> {
                if (exception.get() == null) {
                    try {
                        ArrayList<Result> chunkResults = internalTranslate(chunk, tl, Utilities.generateRandomString());
                        for (int i = 0; i < chunkResults.size(); i++) {
                            rawResults.put(textMessagesQuery.indexOf(chunk.get(i)), chunkResults.get(i));
                        }
                    } catch (Exception e) {
                        exception.set(e);
                    }
                }
                semaphore.countDown();
            }).start();
        }
        semaphore.await();
        if (exception.get() != null) {
            throw exception.get();
        }
        for (int i = 0; i < translations.size(); i++) {
            Object q = translations.get(i);
            String sourceLanguage = null;
            if (q instanceof TLRPC.TL_textWithEntities || q instanceof CharSequence) {
                results.add(rawResults.get(i));
            } else if (q instanceof AdditionalObjectTranslation) {
                AdditionalObjectTranslation res = (AdditionalObjectTranslation) q;
                Object translationData = res.translation;
                if (translationData instanceof String || translationData instanceof TLRPC.TL_textWithEntities) {
                    res.translation = Objects.requireNonNull(rawResults.get(i)).translation;
                    sourceLanguage = Objects.requireNonNull(rawResults.get(i)).sourceLanguage;
                    if (res.additionalInfo instanceof MessageHelper.ReplyMarkupButtonsTexts) {
                        MessageHelper.ReplyMarkupButtonsTexts buttonRows = (MessageHelper.ReplyMarkupButtonsTexts) res.additionalInfo;
                        for (int x = 0; x < buttonRows.getTexts().size(); x++) {
                            for (int y = 0; y < buttonRows.getTexts().get(x).size(); y++) {
                                buttonRows.getTexts().get(x).set(y, stringFromTranslation(Objects.requireNonNull(rawResults.get(++i)).translation));
                            }
                        }
                    }
                } else if (translationData instanceof MessageHelper.PollTexts) {
                    MessageHelper.PollTexts pollTexts = (MessageHelper.PollTexts) translationData;
                    ArrayList<Result> totalPollResults = new ArrayList<>();
                    for (int x = 0; x < pollTexts.getTexts().size(); x++) {
                        totalPollResults.add(Objects.requireNonNull(rawResults.get(i++)));
                        pollTexts.getTexts().set(x, stringFromTranslation(totalPollResults.get(x).translation));
                    }
                    res.translation = pollTexts;
                    sourceLanguage = getTopLanguage(totalPollResults);
                }
                results.add(new Result(res.translation, res.additionalInfo, sourceLanguage));
            }
        }
        return results;
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
