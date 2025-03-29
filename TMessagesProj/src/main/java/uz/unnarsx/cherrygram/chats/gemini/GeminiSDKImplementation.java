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

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.BlockThreshold;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.ai.client.generativeai.type.GoogleGenerativeAIException;
import com.google.ai.client.generativeai.type.HarmCategory;
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
import org.telegram.ui.Components.ChatActivityEnterView;
import org.telegram.ui.PhotoViewer;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executor;

import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig;
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;

public class GeminiSDKImplementation {

    public static void initGeminiConfig(
            Activity parentActivity,
            BaseFragment baseFragment,
            PhotoViewer.FrameLayoutDrawer containerView,
            ChatActivityEnterView chatActivityEnterView,
            ChatActivity chatActivity,
            CharSequence inputText,
            Bitmap inputBitmap,
            Boolean ocr,
            Boolean translateText
    ) {

        if (parentActivity == null && baseFragment == null && baseFragment.getContext() == null) return;

        GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
//        configBuilder.temperature = 0.15f;
//        configBuilder.topK = 32;
//        configBuilder.topP = 1f;
        configBuilder.maxOutputTokens = 4096;

        ArrayList<SafetySetting> safetySettings = new ArrayList<>();
        safetySettings.add(new SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE));
        safetySettings.add(new SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.NONE));
        safetySettings.add(new SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE));
        safetySettings.add(new SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE));

        createGenerativeModel(
                configBuilder,
                safetySettings,
                parentActivity,
                baseFragment,
                containerView,
                chatActivityEnterView,
                chatActivity,
                inputText,
                inputBitmap,
                ocr,
                translateText
        );

    }

    private static void createGenerativeModel(
            GenerationConfig.Builder configBuilder,
            ArrayList<SafetySetting> safetySettings,
            Activity parentActivity,
            BaseFragment baseFragment,
            PhotoViewer.FrameLayoutDrawer containerView,
            ChatActivityEnterView chatActivityEnterView,
            ChatActivity chatActivity,
            CharSequence inputText,
            Bitmap inputBitmap,
            Boolean ocr,
            Boolean translateText
    ) {

        GenerativeModel gm = new GenerativeModel(
                CherrygramChatsConfig.INSTANCE.getGeminiModelName(),
                CherrygramChatsConfig.INSTANCE.getGeminiApiKey(),
                configBuilder.build(),
                safetySettings
        );

        generateContent(
                gm,
                parentActivity,
                baseFragment,
                containerView,
                chatActivityEnterView,
                chatActivity,
                inputText,
                inputBitmap,
                ocr,
                translateText
        );

    }

    private static void generateContent(
            GenerativeModel gm,
            Activity parentActivity,
            BaseFragment baseFragment,
            PhotoViewer.FrameLayoutDrawer containerView,
            ChatActivityEnterView chatActivityEnterView,
            ChatActivity chatActivity,
            CharSequence inputText,
            Bitmap inputBitmap,
            Boolean ocr,
            Boolean translateText
    ) {

        AlertDialog progressDialog = new AlertDialog(
                parentActivity,
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

        if (inputBitmap != null && !ocr) { // Describing picture
            String lang = LocaleController.getInstance().getCurrentLocale().getLanguage();
            content.addText("What is the object in this picture? Answer in language: " + lang);
            content.addImage(inputBitmap);
        } else if (inputBitmap != null && ocr) { // OCR - Optical Character Recognition
            content.addText("What text is written in the picture? Answer without further ado.");
            content.addImage(inputBitmap);
        } else if (translateText) { // Message translation
            String lang = CherrygramChatsConfig.INSTANCE.getTranslationTargetGemini();
            String prompt = "Answer without further ado. Translate the text to " + lang + ": " + inputText;
            if (CherrygramCoreConfig.INSTANCE.isDevBuild()) FileLog.e("промпт: " + prompt);
            content.addText(prompt);
        } else { // Answer only to text
            content.addText(inputText.toString());
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

                    if (inputBitmap != null || ocr) {
                        AndroidUtilities.runOnUIThread(() -> {
                            dialog.setNeutralButton(getString(R.string.Copy), (_1, _2) -> {
                                AndroidUtilities.addToClipboard(resultText);
                                Toast.makeText(baseFragment.getContext(), LocaleController.getString(R.string.TextCopied), Toast.LENGTH_SHORT).show();
                            });
                            dialog.create();
                            dialog.show();
                        });
                    } else if (translateText && chatActivity != null) {
                        GeminiTranslatorBottomSheet.showAlert(baseFragment, chatActivity, resultText);
                    } else {
                        if (containerView != null) {
                            BulletinFactory.of(containerView, baseFragment.getResourceProvider())
                                    .createSuccessBulletin(getString(R.string.YourPasswordSuccess))
                                    .setDuration(Bulletin.DURATION_LONG)
                                    .show();
                        } else {
                            BulletinFactory.global()
                                    .createSuccessBulletin(getString(R.string.YourPasswordSuccess), baseFragment.getResourceProvider())
                                    .setDuration(Bulletin.DURATION_SHORT)
                                    .show();
                        }

                        if (chatActivityEnterView != null && chatActivityEnterView.messageEditText != null) {
                            chatActivityEnterView.messageEditText.setText(resultText);
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
                    if (containerView != null) {
                        BulletinFactory.of(containerView, baseFragment.getResourceProvider())
                                .createErrorBulletin(ex.getMessage())
                                .setDuration(Bulletin.DURATION_LONG)
                                .show();
                    } else {
                        BulletinFactory.global()
                                .createErrorBulletin(ex.getMessage(), baseFragment.getResourceProvider())
                                .setDuration(Bulletin.DURATION_SHORT)
                                .show();
                    }

                }

            }
        }, executor);

    }

    public static void injectGeminiForMedia(
            MessageObject messageObject,
            int currentAccount,
            Activity parentActivity,
            BaseFragment parentFragment,
            PhotoViewer.FrameLayoutDrawer containerView,
            boolean isOCR
    ) {
        String path = "";
        if (messageObject != null) {
            path = messageObject.messageOwner.attachPath;
            if (!TextUtils.isEmpty(path)) {
                File temp = new File(path);
                if (!temp.exists()) {
                    path = null;
                }
            }
            if (TextUtils.isEmpty(path)) {
                path = FileLoader.getInstance(currentAccount).getPathToMessage(messageObject.messageOwner).toString();
            }
            if (messageObject.qualityToSave != null) {
                File f = FileLoader.getInstance(currentAccount).getPathToAttach(messageObject.qualityToSave, null, false, true);
                if (f == null) return;
                path = f.getPath();
            }
        }

        initGeminiConfig(
                parentActivity,
                parentFragment,
                containerView,
                null,
                null,
                "",
                GeminiSDKImplementation.getBitmapFromFile(new File(path)),
                isOCR,
                false
        );
    }

    public static Bitmap getBitmapFromFile(File file) {
        if (file.exists()) {
            return BitmapFactory.decodeFile(file.getAbsolutePath());
        } else {
            return null;
        }
    }

    private static void shutdownGeminiSDK(ListenableFuture<GenerateContentResponse> response) {
        if (response != null && !response.isCancelled()) {
            response.cancel(true);
        }
    }

}
