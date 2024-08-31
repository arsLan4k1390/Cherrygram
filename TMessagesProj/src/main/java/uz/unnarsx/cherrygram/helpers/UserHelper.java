package uz.unnarsx.cherrygram.helpers;

import static org.telegram.messenger.LocaleController.getString;

import android.app.Activity;
import android.content.Intent;

import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BaseController;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Objects;

import uz.unnarsx.cherrygram.Extra;
import uz.unnarsx.cherrygram.core.updater.UpdaterUtils;

public class UserHelper extends BaseController {

    private static final UserHelper[] Instance = new UserHelper[UserConfig.MAX_ACCOUNT_COUNT];

    public UserHelper(int num) {
        super(num);
    }

    public static UserHelper getInstance(int num) {
        UserHelper localInstance = Instance[num];
        if (localInstance == null) {
            synchronized (UserHelper.class) {
                localInstance = Instance[num];
                if (localInstance == null) {
                    Instance[num] = localInstance = new UserHelper(num);
                }
            }
        }
        return localInstance;
    }

    public static final DispatchQueue regDateQueue = new DispatchQueue("regDateQueue");

    private final String uri = Extra.ENDPOINT_FOR_DATE;
    private final String secret = Extra.ENDPOINT_FOR_DATE_SECRET;
    public String type, date;
    private StringBuilder formattedDate;

    public interface OnResponseNotReceived {
        void run();
    }

    public interface OnResponseReceived {
        void run();
    }

    public void getCreationDate(long userId, OnResponseNotReceived onResponseNotReceived, OnResponseReceived onResponseReceived) {
        regDateQueue.postRunnable(() -> {
            try {
                URL url = new URL(uri);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("User-Agent", UpdaterUtils.formatUserAgent());
                con.setRequestProperty("X-Api-Key", secret);

                // For POST only - START
                con.setDoOutput(true);

//                FileLog.d("id of user: " + userId);
                String requestBody = "{\"telegramId\":" + userId +"}";
                byte[] outputInBytes = requestBody.getBytes(StandardCharsets.UTF_8);

                OutputStream os = con.getOutputStream();
                os.write(outputInBytes);
                os.flush();
                os.close();
                // For POST only - END

                int responseCode = con.getResponseCode();
//                System.out.println("POST Response Code :: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject obj = new JSONObject(response.toString());
                    JSONObject objectInside = obj.getJSONObject("data");
                    type = objectInside.getString("type");
                    date = objectInside.getString("date");

                    StringBuilder stringBuilder = new StringBuilder();
                    if (Objects.equals(type, "TYPE_APPROX")) {
                        formattedDate = stringBuilder.append(LocaleController.formatString(R.string.CG_RegistrationDateApproximately, date));
                    } else if (Objects.equals(type, "TYPE_NEWER")) {
                        formattedDate = stringBuilder.append(LocaleController.formatString(R.string.CG_RegistrationDateNewer, date));
                    } else if (Objects.equals(type, "TYPE_OLDER")) {
                        formattedDate = stringBuilder.append(LocaleController.formatString(R.string.CG_RegistrationDateOlder, date));
                    } else {
                        formattedDate = stringBuilder.append(date);
                    }

                    AndroidUtilities.runOnUIThread(() -> {
                        if (onResponseReceived != null)
                            onResponseReceived.run();
                    });
                } else {
                    if (onResponseNotReceived != null)
                        AndroidUtilities.runOnUIThread(onResponseNotReceived::run);

                    FileLog.d("POST request did not work.");
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        }, 0);

//        FileLog.d ("Full reg date:" + formattedDate);
    }

    public StringBuilder getCreationDateSB() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getString(R.string.CG_RegistrationDate));
        stringBuilder.append('\n');
        if (formattedDate == null) {
            stringBuilder.append(getString(R.string.CG_RegistrationDateFailed));
        } else {
            stringBuilder.append(formattedDate);
        }
        return stringBuilder;
    }

    public void addBirthdayEvent(Activity parentActivity, long userId) {
        TLRPC.UserFull userFull = getMessagesController().getUserFull(userId);
        if (userFull != null && userFull.birthday != null) {
            try {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.MONTH, userFull.birthday.month - 1);
                cal.set(Calendar.DAY_OF_MONTH, userFull.birthday.day);

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("FREQ=YEARLY;WKST=MO;INTERVAL=1;BYMONTH=");
                stringBuilder.append(userFull.birthday.month);
                stringBuilder.append(";");
                stringBuilder.append("BYMONTHDAY=");
                stringBuilder.append(userFull.birthday.day);

                Intent intent = new Intent(Intent.ACTION_EDIT);
                intent.setType("vnd.android.cursor.item/event");
                intent.putExtra("beginTime", cal.getTimeInMillis());
                intent.putExtra("allDay", true);
                intent.putExtra("rrule", (CharSequence) stringBuilder);
                intent.putExtra("endTime", cal.getTimeInMillis() + 60 * 60 * 1000);
                intent.putExtra("title",  "Birthday of " + userFull.user.first_name);
                parentActivity.startActivity(intent);
            } catch (Exception ignored) {}
        }
    }

}
