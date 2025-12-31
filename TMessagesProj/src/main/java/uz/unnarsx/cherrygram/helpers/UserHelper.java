/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.helpers;

import static org.telegram.messenger.LocaleController.getString;

import android.app.Activity;
import android.content.Intent;

import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BaseController;
import org.telegram.messenger.ContactsController;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import uz.unnarsx.cherrygram.Extra;
import uz.unnarsx.cherrygram.helpers.network.NetworkHelper;

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

    private CharSequence formattedDate;

    public interface OnResponseNotReceived {
        void run();
    }

    public interface OnResponseReceived {
        void run();
    }

    public void getCreationDate(long userID, OnResponseNotReceived onResponseNotReceived, OnResponseReceived onResponseReceived) {
        regDateQueue.postRunnable(() -> {
            try {
                URL url = new URL(Extra.ENDPOINT_FOR_DATE);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("User-Agent", NetworkHelper.formatUserAgent());
                con.setRequestProperty("X-Api-Key", Extra.ENDPOINT_FOR_DATE_SECRET);

                // For POST only - START
                con.setDoOutput(true);

//                FileLog.d("id of user: " + userID);
                String requestBody = "{\"telegramId\":" + userID +"}";
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

                    String type, date;
                    type = objectInside.getString("type");
                    date = objectInside.getString("date");

                    long timestamp = new SimpleDateFormat("yyyy-MM", Locale.getDefault())
                            .parse(date)
                            .getTime();

                    switch (type) {
                        case "TYPE_APPROX" ->
                                formattedDate = LocaleController.formatString(R.string.CG_RegistrationDateApproximately, formatDateTime(timestamp, true));
                        case "TYPE_NEWER" ->
                                formattedDate = LocaleController.formatString(R.string.CG_RegistrationDateNewer, formatDateTime(timestamp, true));
                        case "TYPE_OLDER" ->
                                formattedDate = LocaleController.formatString(R.string.CG_RegistrationDateOlder, formatDateTime(timestamp, true));
                        default -> formattedDate = formatDateTime(timestamp, true);
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

    public CharSequence getCreationDate(long userID, boolean telegram, String telegramDate) {
        CharSequence dateInfo;
        TLRPC.User user = getMessagesController().getUser(userID);
        String name = ContactsController.formatName(user.first_name, user.last_name);

        if (!telegram && formattedDate == null) {
            dateInfo = getString(R.string.CG_RegistrationDateFailed);
        } else if (telegram) {
            dateInfo = AndroidUtilities.replaceTags(
                    LocaleController.formatString(
                            R.string.CG_RegistrationDate, name,
                            telegramDate
                    )
            );
        } else {
            dateInfo = AndroidUtilities.replaceTags(
                    LocaleController.formatString(
                            R.string.CG_RegistrationDate, name,
                            formattedDate
                    )
            );
        }
        return dateInfo;
    }

    public void addBirthdayEvent(Activity parentActivity, long userID) {
        TLRPC.UserFull userFull = getMessagesController().getUserFull(userID);
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

    private String formatDateTime(long timestamp, boolean useToday) {
        try {
            Calendar calNow = Calendar.getInstance();
            Calendar calDate = Calendar.getInstance();
            calDate.setTimeInMillis(timestamp);

            int dayNow = calNow.get(Calendar.DAY_OF_YEAR);
            int yearNow = calNow.get(Calendar.YEAR);

            int dayDate = calDate.get(Calendar.DAY_OF_YEAR);
            int yearDate = calDate.get(Calendar.YEAR);

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM, yyyy", Locale.getDefault());
            SimpleDateFormat fullFormat = new SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault());

            if (useToday && yearNow == yearDate) {
                if (dayNow == dayDate) {
                    return "Today at " + timeFormat.format(new Date(timestamp));
                } else if (dayNow - 1 == dayDate) {
                    return "Yesterday at " + timeFormat.format(new Date(timestamp));
                }
            }

            if (Math.abs(calNow.getTimeInMillis() - timestamp) < 31536000000L) {
                return monthYearFormat.format(new Date(timestamp));
            } else {
                return fullFormat.format(new Date(timestamp));
            }

        } catch (Exception e) {
            FileLog.e(e);
            return "LOC_ERR";
        }
    }

}
