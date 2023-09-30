package uz.unnarsx.cherrygram.preferences;

import android.os.CountDownTimer;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;
import java.util.Locale;

public class DeleteAccountDialog extends BaseFragment {

    public static void showDeleteAccountDialog(BaseFragment fragment) {
        if (fragment.getParentActivity() == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getParentActivity());
        builder.setMessage(LocaleController.getString("TosDeclineDeleteAccount", R.string.TosDeclineDeleteAccount));
        builder.setTitle(LocaleController.getString("SP_DeleteAccount", R.string.SP_DeleteAccount));
        builder.setPositiveButton(LocaleController.getString("Deactivate", R.string.Deactivate), (dialog, which) -> {
            if (BuildConfig.DEBUG) return;

            final AlertDialog progressDialog = new AlertDialog(fragment.getParentActivity(), AlertDialog.ALERT_TYPE_SPINNER);
            progressDialog.setCanCancel(false);

            ArrayList<TLRPC.Dialog> dialogs = new ArrayList<>(fragment.getMessagesController().getAllDialogs());
            for (TLRPC.Dialog TLdialog : dialogs) {
                if (TLdialog instanceof TLRPC.TL_dialogFolder) {
                    continue;
                }
                TLRPC.Peer peer = fragment.getMessagesController().getPeer((int) TLdialog.id);
                if (peer.channel_id != 0) {
                    TLRPC.Chat chat = fragment.getMessagesController().getChat(peer.channel_id);
                    if (!chat.broadcast) {
                        fragment.getMessageHelper().deleteUserHistoryWithSearch(fragment, TLdialog.id);
                    }
                }
                if (peer.user_id != 0) {
                    fragment.getMessagesController().deleteDialog(TLdialog.id, 0, true);
                }
            }

            Utilities.globalQueue.postRunnable(() -> {
                TLRPC.TL_account_deleteAccount req = new TLRPC.TL_account_deleteAccount();
                req.reason = "Cherry";
                fragment.getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
                    try {
                        progressDialog.dismiss();
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                    if (response instanceof TLRPC.TL_boolTrue) {
                        fragment.getMessagesController().performLogout(0);
                    } else if (error == null || error.code != -1000) {
                        String errorText = LocaleController.getString("ErrorOccurred", R.string.ErrorOccurred);
                        if (error != null) {
                            errorText += "\n" + error.text;
                        }
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(fragment.getParentActivity());
                        builder1.setTitle(LocaleController.getString("CG_AppName", R.string.CG_AppName));
                        builder1.setMessage(errorText);
                        builder1.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                        builder1.show();
                    }
                }));
            }, 20000);
            progressDialog.show();
        });
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialog1 -> {
            var button = (TextView) dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setTextColor(fragment.getThemedColor(Theme.key_text_RedBold));
            button.setEnabled(false);
            var buttonText = button.getText();
            new CountDownTimer(20000, 100) {
                @Override
                public void onTick(long millisUntilFinished) {
                    button.setText(String.format(Locale.getDefault(), "%s (%d)", buttonText, millisUntilFinished / 1000 + 1));
                }

                @Override
                public void onFinish() {
                    button.setText(buttonText);
                    button.setEnabled(true);
                }
            }.start();
        });
        fragment.showDialog(dialog);

    }
}
