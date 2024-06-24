package uz.unnarsx.cherrygram.preferences

import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.BaseFragment
import uz.unnarsx.cherrygram.CherrygramConfig
import uz.unnarsx.cherrygram.helpers.AppRestartHelper
import uz.unnarsx.cherrygram.helpers.CGBiometricPrompt
import uz.unnarsx.cherrygram.ui.dialogs.DeleteAccountDialog
import uz.unnarsx.cherrygram.ui.tgkit.preference.category
import uz.unnarsx.cherrygram.ui.tgkit.preference.contract
import uz.unnarsx.cherrygram.ui.tgkit.preference.switch
import uz.unnarsx.cherrygram.ui.tgkit.preference.textIcon
import uz.unnarsx.cherrygram.ui.tgkit.preference.tgKitScreen
import uz.unnarsx.cherrygram.ui.tgkit.preference.types.TGKitTextIconRow
import java.io.File

class PrivacyAndSecurityPreferencesEntry : BasePreferencesEntry {
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(LocaleController.getString("SP_Category_PrivacyAndSecurity", R.string.SP_Category_PrivacyAndSecurity)) {
        category(LocaleController.getString("SP_Header_Privacy", R.string.SP_Header_Privacy)) {
            switch {
                title = LocaleController.getString("AS_NoProxyPromo", R.string.SP_NoProxyPromo)

                contract({
                    return@contract CherrygramConfig.hideProxySponsor
                }) {
                    CherrygramConfig.hideProxySponsor = it
                    bf.parentLayout.rebuildAllFragmentViews(false, false)
                }
            }
            switch {
                title = LocaleController.getString("SP_GoogleAnalytics", R.string.SP_GoogleAnalytics)
                description = LocaleController.getString("SP_GoogleAnalytics_Desc", R.string.SP_GoogleAnalytics_Desc)

                contract({
                    return@contract CherrygramConfig.googleAnalytics
                }) {
                    CherrygramConfig.googleAnalytics = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }

            textIcon {
                title = LocaleController.getString("SP_CleanOld", R.string.SP_CleanOld)

                listener = TGKitTextIconRow.TGTIListener {
                    val file = File(Environment.getExternalStorageDirectory(), "Telegram")
                    file.deleteRecursively()
                    Toast.makeText(bf.parentActivity, LocaleController.getString("SP_RemovedS", R.string.SP_RemovedS), Toast.LENGTH_SHORT).show()
                }
            }
        }

        val askPasscode = Build.VERSION.SDK_INT >= 23
        if (askPasscode) {
            category(LocaleController.getString("Passcode", R.string.Passcode)) {
                switch {
                    title = LocaleController.getString("SP_AskPinBeforeDelete", R.string.SP_AskPinBeforeDelete)
                    description = LocaleController.getString("SP_AskPinBeforeDelete_Desc", R.string.SP_AskPinBeforeDelete_Desc)

                    contract({
                        return@contract CherrygramConfig.askPasscodeBeforeDelete
                    }) {
                        CherrygramConfig.askPasscodeBeforeDelete = it
                        AppRestartHelper.createRestartBulletin(bf)
                    }
                }
                switch {
                    title = LocaleController.getString("SP_AllowUseSystemPasscode", R.string.SP_AllowUseSystemPasscode)
                    description = LocaleController.getString("SP_AllowUseSystemPasscode_Desc", R.string.SP_AllowUseSystemPasscode_Desc)

                    contract({
                        return@contract CherrygramConfig.allowSystemPasscode
                    }) {
                        CherrygramConfig.allowSystemPasscode = it
                    }
                }
                textIcon {
                    title = LocaleController.getString("SP_TestFingerprint", R.string.SP_TestFingerprint)
                    icon = R.drawable.fingerprint

                    listener = TGKitTextIconRow.TGTIListener {
                        CGBiometricPrompt.prompt(bf.parentActivity) { AppRestartHelper.createDebugSuccessBulletin(bf) }
                    }
                }
            }
        }

        category(LocaleController.getString("SP_Category_Account", R.string.SP_Category_Account)) {
            textIcon {
                title = LocaleController.getString("SP_DeleteAccount", R.string.SP_DeleteAccount)
                icon = R.drawable.msg_delete
                listener = TGKitTextIconRow.TGTIListener {
                    DeleteAccountDialog.showDeleteAccountDialog(bf)
                }
            }
        }

    }
}