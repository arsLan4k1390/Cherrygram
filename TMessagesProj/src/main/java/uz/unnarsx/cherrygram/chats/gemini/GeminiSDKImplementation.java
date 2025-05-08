/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.chats.gemini;

import static org.telegram.messenger.LocaleController.getString;
import static uz.unnarsx.cherrygram.chats.gemini.GeminiResultsBottomSheet.capitalFirst;
import static uz.unnarsx.cherrygram.chats.gemini.GeminiResultsBottomSheet.languageName;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.BlobPart;
import com.google.ai.client.generativeai.type.BlockThreshold;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.ai.client.generativeai.type.GoogleGenerativeAIException;
import com.google.ai.client.generativeai.type.HarmCategory;
import com.google.ai.client.generativeai.type.Part;
import com.google.ai.client.generativeai.type.SafetySetting;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executor;

import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig;
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;

public class GeminiSDKImplementation {

    public static void initGeminiConfig(
            BaseFragment baseFragment,
            ChatActivity chatActivity,
            CharSequence inputText, boolean translateText, boolean summarize,
            File mediaFile,
            boolean ocr, boolean transcribe
    ) {

        if (baseFragment == null && baseFragment.getParentActivity() == null && baseFragment.getContext() == null) return;

        GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
        configBuilder.temperature = (float) CherrygramChatsConfig.INSTANCE.getGeminiTemperatureValue() / 10;
        configBuilder.topK = 10;
        configBuilder.topP = 0.5f;
        configBuilder.maxOutputTokens = 4096;

        ArrayList<SafetySetting> safetySettings = new ArrayList<>();
        safetySettings.add(new SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE));
        safetySettings.add(new SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.NONE));
        safetySettings.add(new SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE));
        safetySettings.add(new SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE));

        createGenerativeModel(
                configBuilder,
                safetySettings,
                baseFragment,
                chatActivity,
                inputText, translateText, summarize,
                mediaFile,
                ocr, transcribe
        );

    }

    private static void createGenerativeModel(
            GenerationConfig.Builder configBuilder,
            ArrayList<SafetySetting> safetySettings,
            BaseFragment baseFragment,
            ChatActivity chatActivity,
            CharSequence inputText, boolean translateText, boolean summarize,
            File mediaFile,
            boolean ocr, boolean transcribe
    ) {

        GenerativeModel gm = new GenerativeModel(
                CherrygramChatsConfig.INSTANCE.getGeminiModelName(),
                CherrygramChatsConfig.INSTANCE.getGeminiApiKey(),
                configBuilder.build(),
                safetySettings
        );

        generateContent(
                gm,
                baseFragment,
                chatActivity,
                inputText, translateText, summarize,
                mediaFile,
                ocr, transcribe
        );

    }

    private static void generateContent(
            GenerativeModel gm,
            BaseFragment baseFragment,
            ChatActivity chatActivity,
            CharSequence inputText, boolean translateText, boolean summarize,
            File mediaFile,
            boolean ocr, boolean transcribe
    ) {

        AlertDialog progressDialog = new AlertDialog(
                baseFragment.getParentActivity(),
                AlertDialog.ALERT_TYPE_SPINNER,
                baseFragment.getResourceProvider()
        );

        AndroidUtilities.runOnUIThread(() -> {
            try {
                progressDialog.show();
            } catch (Exception e) {
                FileLog.e(e);
            }
        });

        Content.Builder content = new Content.Builder();

        Bitmap inputBitmap = getBitmapFromFile(mediaFile);

        if (inputBitmap != null && !ocr) { // Describing picture
            String lang = LocaleController.getInstance().getCurrentLocale().getLanguage();
            content.addText("What is the object in this picture? Answer in " + lang + " language.");
            content.addImage(inputBitmap);
        } else if (inputBitmap != null && ocr) { // OCR - Optical Character Recognition
            content.addText("What text is written in the picture? Answer without further ado.");
            content.addImage(inputBitmap);
        } else if (translateText) { // Message translation
            String lang = capitalFirst(languageName(CherrygramChatsConfig.INSTANCE.getTranslationTargetGemini()));

            String translationPrompt = "You are a professional translator. Translate all input text into " +
                    lang + " accurately and naturally, preserving the original meaning, tone, and context. " +
                    "Do not add explanations or comments. Just return the translated text without any introduction or closing phrases. " +
                    "Here is the text to translate into " + lang + ": " + inputText;

            if (CherrygramCoreConfig.INSTANCE.isDevBuild()) FileLog.e("промпт: " + translationPrompt);
            content.addText(translationPrompt);
        } else if (transcribe) { // Voice to text
            byte[] audioBytes = readBytesCompat(mediaFile);
            Part audioPart = new BlobPart("audio/ogg", audioBytes);

            String ocrPrompt = "You are a speech-to-text transcriber. Accurately transcribe the spoken content from the provided audio without translating it. " +
                    "Keep the original language of the speaker. Do not explain or comment on the content. " +
                    "Return only the plain transcribed text, without any headers, summaries, or formatting.";

            content.addText(ocrPrompt);
            content.addPart(audioPart);
        } else { // Answer only to text
            String summarizeString = summarize ? "Summarize the following message briefly in the language " +
                    "it is written in. Output only the summary, without any additional text or phrases in the start or the end of the message " +
                    "like \"Summary\" or \"Here is the summary\". The text to summarize: " : " ";

            String prompt = summarizeString + inputText.toString();
            content.addText(prompt);
            if (CherrygramCoreConfig.INSTANCE.isDevBuild()) FileLog.e("промпт: " + prompt);
        }
        content.build();

        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content.build());

        Executor executor = ContextCompat.getMainExecutor(baseFragment.getContext());

        progressDialog.setOnDismissListener((dialogInterface) -> shutdownGeminiSDK(response));

        Futures.addCallback(response, new FutureCallback<>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                if (result.getText() != null) {
                    String resultText = result.getText().strip(); // Remove spaces
                    if (CherrygramCoreConfig.INSTANCE.isDevBuild()) FileLog.e("успешный ответ: " + resultText);

                    AndroidUtilities.runOnUIThread(() -> {
                        try {
                            if (progressDialog.isShowing()) progressDialog.dismiss();
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                    });

                    AlertDialog dialog = new AlertDialog(baseFragment.getContext(), AlertDialog.ALERT_TYPE_MESSAGE, baseFragment.getResourceProvider());
                    dialog.setTitle(getString(R.string.CP_GeminiAI_Header));
                    dialog.setMessage(resultText);
                    dialog.setPositiveButton(getString(R.string.OK), null);

                    if (translateText || transcribe || summarize || inputBitmap != null || ocr) {
                        int subtitle = getBottomSheetSubtitle(translateText, transcribe, summarize, inputBitmap, ocr);
                        GeminiResultsBottomSheet.showAlert(baseFragment, chatActivity, resultText, subtitle);
                    } else {
                        BulletinFactory.global()
                                .createSuccessBulletin(getString(R.string.YourPasswordSuccess), baseFragment.getResourceProvider())
                                .setDuration(Bulletin.DURATION_SHORT)
                                .show();

                        if (chatActivity != null && chatActivity.getChatActivityEnterView() != null && chatActivity.getChatActivityEnterView().messageEditText != null) {
                            chatActivity.getChatActivityEnterView().messageEditText.setText(resultText);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                FileLog.e(t);

                GoogleGenerativeAIException ex = GoogleGenerativeAIException.Companion.from(t);

                AndroidUtilities.runOnUIThread(() -> {
                    try {
                        if (progressDialog.isShowing()) progressDialog.dismiss();
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                });

                if (ex.getMessage() != null && ex.getMessage().contains("Unexpected")) {
                    // Cause fucking Google
                    // throws an "com.google.ai.client.generativeai.type.ServerException: Unexpected Response"

                    AndroidUtilities.runOnUIThread(() -> new AlertDialog.Builder(baseFragment.getContext(), AlertDialog.ALERT_TYPE_MESSAGE, baseFragment.getResourceProvider())
                            .setTitle(getString(R.string.CP_GeminiAI_Header))
                            .setMessage(ex.getMessage())
                            .setPositiveButton(getString(R.string.OK), null)
                            .create()
                            .show());
                } else {
                    BulletinFactory.global()
                            .createErrorBulletin(ex.getMessage(), baseFragment.getResourceProvider())
                            .setDuration(Bulletin.DURATION_SHORT)
                            .show();

                }

            }
        }, executor);

    }

    public static void injectGeminiForMedia(
            BaseFragment baseFragment,
            ChatActivity chatActivity,
            MessageObject messageObject,
            boolean isOCR,
            boolean transcribe
    ) {
        if (getMediaFile(messageObject, baseFragment).exists()) {
            initGeminiConfig(
                    baseFragment,
                    chatActivity,
                    "", false, false,
                    getMediaFile(messageObject, baseFragment),
                    isOCR, transcribe
            );
            if (transcribe && !messageObject.isOut() && messageObject.isContentUnread()) baseFragment.getMessagesController().markMessageContentAsRead(messageObject);
        } else {
            showDownloadAlert(baseFragment);
        }
    }

    public static File getMediaFile(MessageObject messageObject, BaseFragment baseFragment) {
        File file = null;
        if (!TextUtils.isEmpty(messageObject.messageOwner.attachPath)) {
            file = new File(messageObject.messageOwner.attachPath);
            if (!file.exists()) {
                file = null;
            }
        }
        if (file == null) {
            file = FileLoader.getInstance(baseFragment.getCurrentAccount()).getPathToMessage(messageObject.messageOwner, false, true);
        }
        if (file != null && !file.exists()) {
            file = FileLoader.getInstance(baseFragment.getCurrentAccount()).getPathToMessage(messageObject.messageOwner, true, true);
        }
        return file;
    }

    private static int getBottomSheetSubtitle(boolean translateText, boolean transcribe, boolean summarize, Bitmap inputBitmap, boolean ocr) {
        int subtitle = 0;

        if (summarize) {
            subtitle = GeminiResultsBottomSheet.GEMINI_TYPE_SUMMARIZE;
        } else if (translateText) {
            subtitle = GeminiResultsBottomSheet.GEMINI_TYPE_TRANSLATE;
        } else if (transcribe) {
            subtitle = GeminiResultsBottomSheet.GEMINI_TYPE_TRANSCRIBE;
        }  else if (inputBitmap != null && !ocr) {
            subtitle = GeminiResultsBottomSheet.GEMINI_TYPE_EXPLANATION;
        } else if (inputBitmap != null && ocr) {
            subtitle = GeminiResultsBottomSheet.GEMINI_TYPE_OCR;
        }
        return subtitle;
    }

    private static Bitmap getBitmapFromFile(File file) {
        if (file != null && file.exists()) {
            return BitmapFactory.decodeFile(file.getAbsolutePath());
        } else {
            return null;
        }
    }

    private static byte[] readBytesCompat(File file) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[4096];
            int nRead;
            while ((nRead = fis.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
        } catch (IOException e) {
            FileLog.e(e);
            return null;
        }
        return buffer.toByteArray();
    }

    private static void shutdownGeminiSDK(ListenableFuture<GenerateContentResponse> response) {
        if (response != null && !response.isCancelled()) {
            response.cancel(true);
        }
    }

    private static void showDownloadAlert(BaseFragment baseFragment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(baseFragment.getParentActivity(), baseFragment.getResourceProvider());
        builder.setTitle(getString(R.string.CP_GeminiAI_Header));
        builder.setPositiveButton(getString(R.string.OK), null);
        builder.setMessage(getString(R.string.PleaseDownload));
        baseFragment.showDialog(builder.create());
    }

}
