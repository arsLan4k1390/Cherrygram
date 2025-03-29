/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.chats.gemini.network;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import uz.unnarsx.cherrygram.chats.gemini.GeminiErrorDTO;

public class ApiClient {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void fetchModels(Context context, Theme.ResourcesProvider resourcesProvider, String apiKey, ApiCallback callback) {
        AlertDialog progressDialog = new AlertDialog(context, AlertDialog.ALERT_TYPE_SPINNER, resourcesProvider);
        executor.execute(() -> {
            List<ModelInfo> modelList = new ArrayList<>();
            HttpURLConnection connection = null;
            try {
                AndroidUtilities.runOnUIThread(progressDialog::show);

                String urlString = "https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey;
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();
                    JsonArray modelsArray = jsonObject.getAsJsonArray("models");

                    for (JsonElement element : modelsArray) {
                        JsonObject obj = element.getAsJsonObject();
                        String name = obj.get("name").getAsString();
                        String displayName = obj.get("displayName").getAsString();
                        String description = obj.has("description") ? obj.get("description").getAsString() : null;
                        modelList.add(new ModelInfo(name, displayName, description));
                    }
                } else {
                    FileLog.e("Ошибка API_ERROR: " + responseCode);
                    showErrorAlert(connection, context, progressDialog, resourcesProvider);
                }
                connection.disconnect();
            } catch (Exception e) {
                FileLog.e("Ошибка NETWORK_ERROR: " + e.getMessage());
                showErrorAlert(connection, context, progressDialog, resourcesProvider);
            }
            dismissProgressDialog(progressDialog);

            callback.onResult(modelList);
        });
    }

    private static GeminiErrorDTO.ErrorResponse handleError(String jsonString) {
        Gson gson = new Gson();
        return gson.fromJson(jsonString, GeminiErrorDTO.ErrorResponse.class);
    }

    private static void showErrorAlert(HttpURLConnection connection, Context context, AlertDialog progressDialog, Theme.ResourcesProvider resourcesProvider) {
        dismissProgressDialog(progressDialog);

        int errorResponseCode = 0;
        String errorResponseMessage = " ";

        if (connection == null && connection.getErrorStream() == null) return;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            StringBuilder errorJsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                errorJsonString.append(line);
            }
            reader.close();
            errorResponseCode = handleError(errorJsonString.toString()).getError().getCode();
            errorResponseMessage = handleError(errorJsonString.toString()).getError().getMessage();
        } catch (Exception e) {
            FileLog.e(e);
            errorResponseMessage = e.toString();
        }

        String finalErrorMessage = "Error " + errorResponseCode + ": " + errorResponseMessage;

        AndroidUtilities.runOnUIThread(() -> new AlertDialog.Builder(context, AlertDialog.ALERT_TYPE_MESSAGE, resourcesProvider)
                .setTitle(getString(R.string.CP_GeminiAI_Header))
                .setMessage(finalErrorMessage)
                .setPositiveButton(getString(R.string.OK), null)
                .create()
                .show());

    }

    private static void dismissProgressDialog(AlertDialog progressDialog) {
        AndroidUtilities.runOnUIThread(() -> {
            try {
                if (progressDialog.isShowing()) progressDialog.dismiss();
            } catch (Exception e) {
                FileLog.e(e);
            }
        });
    }

}
