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
import android.content.Context;

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
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.ChatActivityEnterView;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import uz.unnarsx.cherrygram.core.configs.CherrygramExperimentalConfig;
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper;

public class GeminiSDKImplementation {

    public static void initGeminiConfig(
            Activity parentActivity,
            Context context,
            ChatActivityEnterView chatActivityEnterView,
            String inputText
    ) {

        if (parentActivity == null && context == null && chatActivityEnterView == null && chatActivityEnterView.messageEditText == null) return;

        GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
        configBuilder.temperature = 0.15f;
        configBuilder.topK = 32;
        configBuilder.topP = 1f;
        configBuilder.maxOutputTokens = 4096;

        ArrayList<SafetySetting> safetySettings = new ArrayList<>();
        safetySettings.add(new SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE));
        safetySettings.add(new SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE));
        safetySettings.add(new SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE));
        safetySettings.add(new SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE));

        createGenerativeModel(
                configBuilder,
                safetySettings,
                parentActivity,
                context,
                chatActivityEnterView,
                inputText
        );

    }

    private static void createGenerativeModel(
            GenerationConfig.Builder configBuilder,
            ArrayList<SafetySetting> safetySettings,
            Activity parentActivity,
            Context context,
            ChatActivityEnterView chatActivityEnterView,
            String inputText
    ) {

        GenerativeModel gm = new GenerativeModel(
                CGResourcesHelper.INSTANCE.getGeminiModel(),
                CherrygramExperimentalConfig.INSTANCE.getGeminiApiKey(),
                configBuilder.build(),
                safetySettings
        );

        generateContent(
                gm,
                parentActivity,
                context,
                chatActivityEnterView,
                inputText
        );

    }

    private static void generateContent(
            GenerativeModel gm,
            Activity parentActivity,
            Context context,
            ChatActivityEnterView chatActivityEnterView,
            String inputText
    ) {

        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content = new Content.Builder()
                .addText(inputText)
                .build();
        Executor executor = ContextCompat.getMainExecutor(context);

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        AlertDialog progressDialog = new AlertDialog(
                parentActivity,
                AlertDialog.ALERT_TYPE_SPINNER,
                chatActivityEnterView.getParentFragment().getResourceProvider()
        );

        AndroidUtilities.runOnUIThread(() -> {
            try {
                progressDialog.show();
            } catch (Exception e) {
                FileLog.e(e);
            }
        });

        Futures.addCallback(response, new FutureCallback<>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                if (resultText != null) {

                    AndroidUtilities.runOnUIThread(() -> {
                        try {
                            if (progressDialog.isShowing()) progressDialog.dismiss();
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                    });

                    BulletinFactory.global()
                            .createSuccessBulletin(getString(R.string.OK), chatActivityEnterView.getParentFragment().getResourceProvider())
                            .setDuration(Bulletin.DURATION_SHORT)
                            .show();


                    chatActivityEnterView.messageEditText.setText(resultText.substring(0, resultText.length() - 1));
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

                    new AlertDialog.Builder(context, AlertDialog.ALERT_TYPE_MESSAGE, chatActivityEnterView.getParentFragment().getResourceProvider())
                            .setTitle(getString(R.string.EP_GeminiAI_Header))
                            .setMessage(ex.getMessage())
                            .setPositiveButton(getString(R.string.OK), null)
                            .create()
                            .show();
                } else {
                    BulletinFactory.global()
                            .createErrorBulletin(ex.getMessage(), chatActivityEnterView.getParentFragment().getResourceProvider())
                            .setDuration(Bulletin.DURATION_SHORT)
                            .show();
                }

            }
        }, executor);

    }

}
