/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.helpers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.LaunchActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import uz.unnarsx.cherrygram.core.CherrygramLogger;

public class ContactsVcfExporter {

    public static void export(Context context, ArrayList<TLRPC.User> contacts) {
        Utilities.globalQueue.postRunnable(() -> {
            try {
                String vcf = buildVcf(contacts);

                File dir = new File(ApplicationLoader.applicationContext.getCacheDir(), "vcf");
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(dir, "contacts_" + System.currentTimeMillis() + ".vcf");

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(vcf.getBytes(StandardCharsets.UTF_8));
                }

                AndroidUtilities.runOnUIThread(() -> shareFile(context, file));
            } catch (Exception e) {
                FileLog.e(e);
            }
        });
    }

    private static String buildVcf(ArrayList<TLRPC.User> contacts) {
        contacts.sort((a, b) -> {
            String nameA = ContactsController.formatName(a);
            String nameB = ContactsController.formatName(b);
            return nameA.compareToIgnoreCase(nameB);
        });
        StringBuilder sb = new StringBuilder();
        for (TLRPC.User user : contacts) {
            if (user == null) continue;

            String phone = user.phone != null ? user.phone : "";
            if (phone.isEmpty()) continue;

            String fullName = ContactsController.formatName(user);
            if (fullName.trim().isEmpty()) {
                fullName = phone;
            }

            String firstName = user.first_name != null ? user.first_name : "";
            String lastName = user.last_name != null ? user.last_name : "";

            sb.append("BEGIN:VCARD\r\n");
            sb.append("VERSION:3.0\r\n");
            sb.append("N:").append(escape(lastName)).append(";").append(escape(firstName)).append(";;;\r\n");
            sb.append("FN:").append(escape(fullName)).append("\r\n");
            sb.append("TEL;TYPE=CELL:+").append(phone).append("\r\n");
            sb.append("END:VCARD\r\n");
        }
        return sb.toString();
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\n", "\\n");
    }

    private static void shareFile(Context context, File file) {
        try {
            if (!file.exists()) return;

            Uri uri = FileProvider.getUriForFile(context, ApplicationLoader.getApplicationId() + ".provider", file);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/octet-stream");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setClass(context, LaunchActivity.class);

            context.startActivity(intent);
        } catch (Exception e) {
            CherrygramLogger.e(e);
        }
    }

}