package uz.unnarsx.cherrygram.translator;

import android.content.Context;
import android.text.TextUtils;

import androidx.core.text.HtmlCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;
import java.util.Locale;

import uz.unnarsx.cherrygram.CherrygramConfig;
import uz.unnarsx.cherrygram.helpers.PopupHelper;

public class Translator {

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

    public static BaseTranslator getCurrentTranslator() {
        return TelegramTranslator.getInstance();
    }

    public static void translate(Object query, boolean isKeyboard, TranslateCallBack translateCallBack) {
        BaseTranslator translator = getCurrentTranslator();
        String language = isKeyboard ? translator.getCurrentTargetKeyboardLanguage() : translator.getCurrentTargetLanguage();
        if (!translator.supportLanguage(language)) {
            translateCallBack.onError(new UnsupportedTargetLanguageException());
        } else {
            translator.startTask(query, language, translateCallBack);
        }
    }

    public interface TranslateCallBack {
        void onSuccess(BaseTranslator.Result result);

        void onError(Exception e);
    }


    private static class UnsupportedTargetLanguageException extends IllegalArgumentException {
    }
}

