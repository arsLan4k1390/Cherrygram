package uz.unnarsx.cherrygram.helpers;

import android.widget.Toast;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.tgnet.TLObject;

import java.util.Arrays;
import java.util.HashSet;

public class ErrorDatabaseHelper {
    public static String getMethodName(TLObject method) {
        var name = method.toString();
        var start = name.indexOf("$") + 4;
        var end = name.indexOf("@");
        return name.substring(start, end).replace("_", ".");
    }

    public static void showErrorToast(TLObject method, String text) {
        if (text.equals("FILE_REFERENCE_EXPIRED")) {
            return;
        }
        AndroidUtilities.runOnUIThread(() -> Toast.makeText(ApplicationLoader.applicationContext, getMethodName(method) + ": " + text, Toast.LENGTH_SHORT).show());
    }
}
