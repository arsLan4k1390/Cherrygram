/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.chats.translator;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.text.TextUtils;

import androidx.core.text.HtmlCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig;
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper;
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
                names.add(HtmlCompat.fromHtml(String.format("%s - %s", CGResourcesHelper.INSTANCE.capitalize(locale.getDisplayScript()), CGResourcesHelper.INSTANCE.capitalize(locale.getDisplayScript(locale))), HtmlCompat.FROM_HTML_MODE_LEGACY).toString());
            } else {
                names.add(String.format("%s - %s", CGResourcesHelper.INSTANCE.capitalize(locale.getDisplayName()), CGResourcesHelper.INSTANCE.capitalize(locale.getDisplayName(locale))));
            }
        }
        AndroidUtilities.selectionSort(names, targetLanguages);

        targetLanguages.add(0, "app");
        names.add(0, getString(R.string.Default));

        PopupHelper.show(names, getString(R.string.CG_TranslationLanguage), targetLanguages.indexOf(isKeyboard ? CherrygramChatsConfig.INSTANCE.getTranslationKeyboardTarget() : CherrygramChatsConfig.INSTANCE.getTranslationTarget()), context, i -> {
            if (isKeyboard) {
                CherrygramChatsConfig.INSTANCE.setTranslationKeyboardTarget(targetLanguages.get(i));
            } else {
                CherrygramChatsConfig.INSTANCE.setTranslationTarget(targetLanguages.get(i));
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

