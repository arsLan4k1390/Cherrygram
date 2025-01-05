/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.core.helpers;

import android.widget.Toast;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.tgnet.TLObject;

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
