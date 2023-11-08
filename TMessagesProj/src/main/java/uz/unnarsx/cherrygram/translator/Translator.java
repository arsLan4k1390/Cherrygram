package uz.unnarsx.cherrygram.translator;

import android.content.Context;
import android.text.TextUtils;

import androidx.core.text.HtmlCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import uz.unnarsx.cherrygram.CherrygramConfig;
import uz.unnarsx.cherrygram.helpers.ui.PopupHelper;

public class Translator {

    public static BaseTranslator getCurrentTranslator() {
        return TelegramTranslator.getInstance();
    }

    public static void showTranslationTargetSelector(Context context, boolean isKeyboard, Runnable callback, Theme.ResourcesProvider resourcesProvider) {
        BaseTranslator translator = Translator.getCurrentTranslator();
        ArrayList<String> targetLanguages = new ArrayList<>(translator.getTargetLanguages());
        ArrayList<CharSequence> names = new ArrayList<>();
        for (String language : targetLanguages) {
            Locale locale = Locale.forLanguageTag(language);
            if (!TextUtils.isEmpty(locale.getScript())) {
                names.add(HtmlCompat.fromHtml(String.format("%s - %s", AndroidUtilities.capitalize(locale.getDisplayScript()), AndroidUtilities.capitalize(locale.getDisplayScript(locale))), HtmlCompat.FROM_HTML_MODE_LEGACY).toString());
            } else {
                names.add(String.format("%s - %s", AndroidUtilities.capitalize(locale.getDisplayName()), AndroidUtilities.capitalize(locale.getDisplayName(locale))));
            }
        }
        AndroidUtilities.selectionSort(names, targetLanguages);

        targetLanguages.add(0, "app");
        names.add(0, LocaleController.getString("Default", R.string.Default));

        PopupHelper.show(names, LocaleController.getString("CG_TranslationLanguage", R.string.CG_TranslationLanguage), targetLanguages.indexOf(isKeyboard ? CherrygramConfig.INSTANCE.getTranslationKeyboardTarget() : CherrygramConfig.INSTANCE.getTranslationTarget()), context, i -> {
            if (isKeyboard) {
                CherrygramConfig.INSTANCE.setTranslationKeyboardTarget(targetLanguages.get(i));
            } else {
                CherrygramConfig.INSTANCE.setTranslationTarget(targetLanguages.get(i));
            }
            callback.run();
        }, resourcesProvider);
    }

    public static String translate(String query, boolean isKeyboard, TranslateCallBack translateCallBack) {
        return translate(new ArrayList<>(Collections.singletonList(query)), isKeyboard, singleTranslateCallback(translateCallBack));
    }

    private static MultiTranslateCallBack singleTranslateCallback(TranslateCallBack callBack) {
        return (e, result) -> {
            if (result != null && !result.isEmpty()) {
                callBack.onSuccess(e, result.get(0));
            } else {
                callBack.onSuccess(e, null);
            }
        };
    }

    public static String translate(ArrayList<Object> translations, boolean isKeyboard, MultiTranslateCallBack translateCallBack) {
        BaseTranslator translator = getCurrentTranslator();
        String language = isKeyboard ? translator.getCurrentTargetKeyboardLanguage() : translator.getCurrentTargetLanguage();
        String token = Utilities.generateRandomString();
        if (!translator.supportLanguage(language)) {
            translateCallBack.onSuccess(new UnsupportedTargetLanguageException(), null);
        } else {
            translator.startTask(translations, language, translateCallBack, token);
        }
        return token;
    }

    public interface MultiTranslateCallBack {
        void onSuccess(Exception e, ArrayList<BaseTranslator.Result> result);
    }

    public interface TranslateCallBack {
        void onSuccess(Exception e, BaseTranslator.Result result);
    }


    private static class UnsupportedTargetLanguageException extends IllegalArgumentException {
    }
}

