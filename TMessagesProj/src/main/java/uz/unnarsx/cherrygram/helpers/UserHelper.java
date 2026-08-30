/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.helpers;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import uz.unnarsx.cherrygram.core.CherrygramLogger;

public class UserHelper {

    /** Registration date start */
    private static final String JSON_FILE = "id_date.json";
    private static final ArrayList<ProfileDateData> profileDateDataList = new ArrayList<>();

    private static void loadData() {
        try {
            InputStream in = ApplicationLoader.applicationContext.getAssets().open(JSON_FILE);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[10240];
            int c;
            while ((c = in.read(buffer)) != -1) {
                bos.write(buffer, 0, c);
            }
            bos.close();
            in.close();
            String json = bos.toString("UTF-8");
            JSONObject object = new JSONObject(json);
            JSONArray data = object.getJSONArray("data");
            profileDateDataList.clear();
            for (int i = 0; i < data.length(); i++) {
                JSONObject o = data.getJSONObject(i);
                profileDateDataList.add(new ProfileDateData(o.getLong("id"), o.getLong("date")));
            }
        } catch (Exception e) {
            CherrygramLogger.e(e);
        }
    }

    private static String formatCreationDate(String prefix, long timestamp) {
        String formattedDate = formatDateTime(timestamp, true);

        return switch (prefix) {
            case "~" -> LocaleController.formatString(R.string.CG_RegistrationDateApproximately, formattedDate);
            case ">" -> LocaleController.formatString(R.string.CG_RegistrationDateNewer, formattedDate);
            case "<" -> LocaleController.formatString(R.string.CG_RegistrationDateOlder, formattedDate);
            default -> formattedDate;
        };
    }

    public static CharSequence getUserTime(long userId, String regDateFromTelegram) {
        TLRPC.User user = MessagesController.getInstance(UserConfig.selectedAccount).getUser(userId);
        String name = user != null ? ContactsController.formatName(user.first_name, user.last_name) : "";

        String formattedDateValue;

        if (regDateFromTelegram != null && !TextUtils.isEmpty(regDateFromTelegram)) {
            formattedDateValue = regDateFromTelegram;
        } else {
            formattedDateValue = calculateDateFromJson(userId);
        }

        return AndroidUtilities.replaceTags(
                LocaleController.formatString(
                        R.string.CG_RegistrationDate, name,
                        formattedDateValue
                )
        );
    }

    private static String calculateDateFromJson(long userId) {
        if (profileDateDataList.isEmpty()) {
            loadData();
        }

        if (profileDateDataList.isEmpty()) {
            return LocaleController.getString(R.string.CG_RegistrationDateFailed);
        }

        for (int i = 1; i < profileDateDataList.size(); i++) {
            ProfileDateData data1 = profileDateDataList.get(i - 1);
            ProfileDateData data2 = profileDateDataList.get(i);
            if (userId >= data1.id() && userId <= data2.id()) {
                long idx = userId - data1.id();
                long idxRange = data2.id() - data1.id();
                double t = (double) idx / idxRange;
                long date1 = data1.date();
                long date2 = data2.date();
                double date = (date1 + t * (date2 - date1)) * 1000.0;

                return formatCreationDate("~", Math.round(date));
            }
        }

        if (userId <= 1000000) {
            return formatCreationDate("=", 1380326400000L);
        }
        return formatCreationDate(">", 1711889200000L);
    }

    public record ProfileDateData(long id, long date) {
    }

    private static String formatDateTime(long timestamp, boolean useToday) {
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
            SimpleDateFormat fullFormat = new SimpleDateFormat("d MMMM, yyyy", Locale.getDefault());

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
            CherrygramLogger.e(e);
            return "LOC_ERR";
        }
    }
    /** Registration date finish */

    public static void addBirthdayEvent(Activity parentActivity, long userID) {
        TLRPC.UserFull userFull = MessagesController.getInstance(UserConfig.selectedAccount).getUserFull(userID);
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