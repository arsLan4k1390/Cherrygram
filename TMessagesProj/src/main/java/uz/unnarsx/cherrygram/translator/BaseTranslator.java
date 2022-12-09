package uz.unnarsx.cherrygram.translator;

import androidx.annotation.Nullable;
import androidx.collection.LruCache;
import androidx.core.util.Pair;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.tgnet.TLRPC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import uz.unnarsx.cherrygram.CherrygramConfig;
import uz.unnarsx.cherrygram.helpers.MessageHelper;


abstract public class BaseTranslator {

    private final LruCache<Pair<Object, String>, Result> cache = new LruCache<>(200);

    abstract protected Result translate(String query, String tl) throws Exception;

    abstract public List<String> getTargetLanguages();

    public String convertLanguageCode(String language, String country) {
        return language;
    }

    public ArrayList<String> getStringBlocks(String query, int maxBlockSize) throws IOException {
        ArrayList<String> blocks = new ArrayList<>();
        while (query.length() > maxBlockSize) {
            String maxBlockStr = query.subSequence(0, maxBlockSize).toString();
            int n = maxBlockStr.lastIndexOf("\n\n");
            if (n == -1) n = maxBlockStr.lastIndexOf("\n");
            if (n == -1) n = maxBlockStr.lastIndexOf(". ");
            if (n == -1) n = Math.min(maxBlockStr.length(), maxBlockSize);
            blocks.add(query.substring(0, n + 1));
            query = query.substring(n + 1);
        }
        if (query.length() > 0) {
            blocks.add(query);
        }
        if (blocks.size() == 100) throw new IOException("Too many blocks");
        return blocks;
    }

    public String buildTranslatedString(String original, String translated) {
        if (translated.length() > 2) {
            if (original.startsWith("\n\n") && !translated.startsWith("\n\n")) {
                translated = "\n\n" + translated;
            } else if (original.startsWith("\n") && !translated.startsWith("\n")) {
                translated = "\n" + translated;
            }
            if (original.endsWith("\n\n") && !translated.endsWith("\n\n")) {
                translated += "\n\n";
            } else if (original.endsWith("\n") && !translated.endsWith("\n")) {
                translated += "\n";
            }
        }
        return translated;
    }

    public void startTask(Object query, String toLang, Translator.TranslateCallBack translateCallBack) {
        Result result = cache.get(Pair.create(query, toLang));
        if (result != null) {
            translateCallBack.onSuccess(result);
            return;
        }
        new Thread() {
            @Override
            public void run() {
                try {
                    if (query instanceof CharSequence) {
                        Result result = translate(query.toString(), toLang);
                        if (result != null) {
                            cache.put(Pair.create(query, result.sourceLanguage), result);
                            AndroidUtilities.runOnUIThread(() -> translateCallBack.onSuccess(result));
                        } else {
                            AndroidUtilities.runOnUIThread(() -> translateCallBack.onError(null));
                        }
                    } else if (query instanceof AdditionalObjectTranslation) {
                        if (((AdditionalObjectTranslation) query).translation instanceof TLRPC.Poll) {
                            TLRPC.TL_poll poll = new TLRPC.TL_poll();
                            TLRPC.TL_poll original = (TLRPC.TL_poll) ((AdditionalObjectTranslation) query).translation;
                            Result questionResult = translate(original.question, toLang);
                            poll.question = (String) questionResult.translation;
                            for (int i = 0; i < original.answers.size(); i++) {
                                TLRPC.TL_pollAnswer answer = new TLRPC.TL_pollAnswer();
                                answer.text = (String) translate(original.answers.get(i).text, toLang).translation;
                                answer.option = original.answers.get(i).option;
                                poll.answers.add(answer);
                            }
                            poll.close_date = original.close_date;
                            poll.close_period = original.close_period;
                            poll.closed = original.closed;
                            poll.flags = original.flags;
                            poll.id = original.id;
                            poll.multiple_choice = original.multiple_choice;
                            poll.public_voters = original.public_voters;
                            poll.quiz = original.quiz;
                            AndroidUtilities.runOnUIThread(() -> translateCallBack.onSuccess(new Result(poll, questionResult.sourceLanguage)));
                        } else if (((AdditionalObjectTranslation) query).translation instanceof String) {
                            Result result = translate((String) ((AdditionalObjectTranslation) query).translation, toLang);
                            if (result != null) {
                                if (((AdditionalObjectTranslation) query).additionalInfo != null && ((AdditionalObjectTranslation) query).additionalInfo instanceof MessageHelper.ReplyMarkupButtonsTexts) {
                                    MessageHelper.ReplyMarkupButtonsTexts buttonRows = (MessageHelper.ReplyMarkupButtonsTexts) ((AdditionalObjectTranslation) query).additionalInfo;
                                    for (int i = 0; i < buttonRows.getTexts().size(); i++) {
                                        ArrayList<String> buttonsRow = buttonRows.getTexts().get(i);
                                        for (int j = 0; j < buttonsRow.size(); j++) {
                                            buttonsRow.set(j, (String) translate(buttonsRow.get(j), toLang).translation);
                                        }
                                    }
                                    result.additionalInfo = buttonRows;
                                }
                                cache.put(Pair.create(query, result.sourceLanguage), result);
                                AndroidUtilities.runOnUIThread(() -> translateCallBack.onSuccess(result));
                            } else {
                                AndroidUtilities.runOnUIThread(() -> translateCallBack.onError(null));
                            }
                        } else {
                            throw new UnsupportedOperationException("Unsupported translation query");
                        }
                    } else {
                        throw new UnsupportedOperationException("Unsupported translation query");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    FileLog.e(e, false);
                    AndroidUtilities.runOnUIThread(() -> translateCallBack.onError(e));
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
        return getTargetLanguage(CherrygramConfig.INSTANCE.getTranslationTarget());
    }

    public String getCurrentTargetKeyboardLanguage() {
        return getTargetLanguage(CherrygramConfig.INSTANCE.getTranslationKeyboardTarget());
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

        public Result(Object translation, @Nullable TLRPC.ReplyMarkup additionalInfo, @Nullable String sourceLanguage) {
            this.translation = translation;
            this.additionalInfo = additionalInfo;
            this.sourceLanguage = sourceLanguage;
        }
    }

    public static class AdditionalObjectTranslation {
        public Object translation;
        public Object additionalInfo;
    }
}
