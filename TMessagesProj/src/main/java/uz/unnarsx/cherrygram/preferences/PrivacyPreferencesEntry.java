/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.preferences;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.view.View;

import androidx.biometric.BiometricPrompt;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import org.telegram.ui.UsersSelectActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import uz.unnarsx.cherrygram.core.CGBiometricPrompt;
import uz.unnarsx.cherrygram.core.CherrygramLogger;
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.core.configs.CherrygramPrivacyConfig;
import uz.unnarsx.cherrygram.core.firebase.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper;
import uz.unnarsx.cherrygram.core.ui.CGBulletinCreator;
import uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper;

public class PrivacyPreferencesEntry extends UniversalFragment {


    private final int hideArchiveFromChatsListRow = 1;
    private final int askBiometricsToOpenDialogsRow = 2;
    private final int askBiometricsToOpenChatsRow = 3;
    private final int askBiometricsToOpenSecretChatsRow = 4;
    private final int askBiometricsToOpenArchivedChatsRow = 5;

    private final int lockedChatsRow = 6;
    private final int requireBiometricsToDeleteChatsRow = 7;
    private final int allowSystemPinRow = 8;
    private final int testFingerprintRow = 9;

    private final int googleAnalyticsRow = 10;
    private final int deleteAccountRow = 11;

    private final int hideArchivedStoriesRow = 1390;

    private boolean expandedBiometricSection = false;

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("privacy_preferences_screen");
        return getString(R.string.SettingsPrivacySecurity);
    }

    @Override
    public View createView(Context context) {
        setMD3(true);
        setGilroy(true);
        return super.createView(context);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(getString(R.string.FilterChats)));
        if ((CherrygramCoreConfig.isStandalonePremiumBuild() || CherrygramCoreConfig.isDevBuild()) && (getUserConfig().clientUserId == 6578415824L || getUserConfig().clientUserId == 282287840L)) {
            items.add(SettingsHelper.asSwitchCG(hideArchivedStoriesRow, "Скрыть архивированные истории", "Скрывает раздел архивированных историй в профиле")
                    .setChecked(CherrygramPrivacyConfig.INSTANCE.getHideArchivedStories())
            );
        }
        items.add(SettingsHelper.asSwitchCG(hideArchiveFromChatsListRow, getString(R.string.SP_HideArchive), getString(R.string.SP_HideArchive_Desc))
                .setChecked(CherrygramPrivacyConfig.INSTANCE.getHideArchiveFromChatsList())
        );
        if (getChatsPasswordHelper().checkBiometricAvailable()) {
            items.add(UItem.asShadow(null));
            items.add(
                    SettingsHelper.asExpandableSwitch(
                            askBiometricsToOpenDialogsRow,
                            R.drawable.msg_pin_code,
                            getString(R.string.Passcode),
                            getBiometricCountText()
                    )
                    .setChecked(
                            CherrygramPrivacyConfig.INSTANCE.getAskBiometricsToOpenChat() ||
                            CherrygramPrivacyConfig.INSTANCE.getAskBiometricsToOpenEncrypted() ||
                            CherrygramPrivacyConfig.INSTANCE.getAskBiometricsToOpenArchive()
                    )
                    .setCollapsed(!expandedBiometricSection)
                    .setClickCallback(v -> CGBiometricPrompt.prompt(getParentActivity(), () -> {
                        boolean newValue = !(
                                CherrygramPrivacyConfig.INSTANCE.getAskBiometricsToOpenChat() ||
                                CherrygramPrivacyConfig.INSTANCE.getAskBiometricsToOpenEncrypted() ||
                                CherrygramPrivacyConfig.INSTANCE.getAskBiometricsToOpenArchive()
                        );

                        CherrygramPrivacyConfig.INSTANCE.setAskBiometricsToOpenChat(newValue);
                        CherrygramPrivacyConfig.INSTANCE.setAskBiometricsToOpenEncrypted(newValue);
                        CherrygramPrivacyConfig.INSTANCE.setAskBiometricsToOpenArchive(newValue);

                        expandedBiometricSection = !expandedBiometricSection;
                        listView.adapter.update(true);
                    }))
            );
            if (expandedBiometricSection) {
                items.add(UItem.asRoundCheckbox(askBiometricsToOpenChatsRow, getString(R.string.FilterChats))
                        .setChecked(CherrygramPrivacyConfig.INSTANCE.getAskBiometricsToOpenChat())
                        .setPad(1)
                );

                items.add(UItem.asRoundCheckbox(askBiometricsToOpenSecretChatsRow, getString(R.string.SecretChat))
                        .setChecked(CherrygramPrivacyConfig.INSTANCE.getAskBiometricsToOpenEncrypted())
                        .setPad(1)
                );

                items.add(UItem.asRoundCheckbox(askBiometricsToOpenArchivedChatsRow, getString(R.string.ArchivedChats))
                        .setChecked(CherrygramPrivacyConfig.INSTANCE.getAskBiometricsToOpenArchive())
                        .setPad(1)
                );
            }

            items.add(UItem.asShadow(getString(R.string.SP_AskBioToOpenChats_Desc)));

            if (CherrygramPrivacyConfig.INSTANCE.getAskBiometricsToOpenChat()) {
                items.add(UItem.asButton(lockedChatsRow, R.drawable.msg_discussion, getString(R.string.SP_LockedChats), String.valueOf(getChatsPasswordHelper().getLockedChatsCount())));
            }
            items.add(SettingsHelper.asSwitchCG(requireBiometricsToDeleteChatsRow, getString(R.string.SP_AskPinBeforeDelete), getString(R.string.SP_AskPinBeforeDelete_Desc))
                    .setChecked(CherrygramPrivacyConfig.INSTANCE.getAskPasscodeBeforeDelete())
            );
            items.add(SettingsHelper.asSwitchCG(allowSystemPinRow, getString(R.string.SP_AllowUseSystemPasscode), getString(R.string.SP_AllowUseSystemPasscode_Desc))
                    .setChecked(CherrygramPrivacyConfig.INSTANCE.getAllowSystemPasscode())
            );
        }
        items.add(UItem.asButton(testFingerprintRow, R.drawable.fingerprint, getString(R.string.SP_TestFingerprint)));
        items.add(UItem.asShadow(getString(R.string.SP_TestFingerprint_Desc)));

        items.add(UItem.asHeader(getString(R.string.LocalMiscellaneousCache)));
        /*items.add(SettingsHelper.asSwitchCG(googleAnalyticsRow, getString(R.string.SP_GoogleAnalytics), getString(R.string.SP_GoogleAnalytics_Desc))
                .setChecked(CherrygramPrivacyConfig.INSTANCE.getGoogleAnalytics())
        );*/

        UItem deleteAccountButton = UItem.asButton(
                deleteAccountRow,
                R.drawable.msg_delete,
                getString(R.string.SP_DeleteAccount)
        );
        deleteAccountButton.red = true;
        items.add(deleteAccountButton);
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == hideArchivedStoriesRow) {
            CherrygramPrivacyConfig.INSTANCE.setHideArchivedStories(!CherrygramPrivacyConfig.INSTANCE.getHideArchivedStories());
            SettingsHelper.updateCheckState(view, CherrygramPrivacyConfig.INSTANCE.getHideArchivedStories());

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        } else if (item.id == hideArchiveFromChatsListRow) {
            CherrygramPrivacyConfig.INSTANCE.setHideArchiveFromChatsList(!CherrygramPrivacyConfig.INSTANCE.getHideArchiveFromChatsList());
            SettingsHelper.updateCheckState(view, CherrygramPrivacyConfig.INSTANCE.getHideArchiveFromChatsList());
        } else if (item.id == askBiometricsToOpenDialogsRow) {
            expandedBiometricSection = !expandedBiometricSection;
            item.collapsed = !item.collapsed;

            listView.adapter.update(true);
        } else if (item.id == askBiometricsToOpenChatsRow) {
            CGBiometricPrompt.prompt(getParentActivity(), () -> {
                CherrygramPrivacyConfig.INSTANCE.setAskBiometricsToOpenChat(!CherrygramPrivacyConfig.INSTANCE.getAskBiometricsToOpenChat());
                SettingsHelper.updateCheckState(view, CherrygramPrivacyConfig.INSTANCE.getAskBiometricsToOpenChat());

                listView.adapter.update(true);
            });
        } else if (item.id == askBiometricsToOpenSecretChatsRow) {
            CGBiometricPrompt.prompt(getParentActivity(), () -> {
                CherrygramPrivacyConfig.INSTANCE.setAskBiometricsToOpenEncrypted(!CherrygramPrivacyConfig.INSTANCE.getAskBiometricsToOpenEncrypted());
                SettingsHelper.updateCheckState(view, CherrygramPrivacyConfig.INSTANCE.getAskBiometricsToOpenEncrypted());

                listView.adapter.update(true);
            });
        } else if (item.id == askBiometricsToOpenArchivedChatsRow) {
            CGBiometricPrompt.prompt(getParentActivity(), () -> {
                CherrygramPrivacyConfig.INSTANCE.setAskBiometricsToOpenArchive(!CherrygramPrivacyConfig.INSTANCE.getAskBiometricsToOpenArchive());
                SettingsHelper.updateCheckState(view, CherrygramPrivacyConfig.INSTANCE.getAskBiometricsToOpenArchive());

                listView.adapter.update(true);
            });
        } else if (item.id == lockedChatsRow) {
            CGBiometricPrompt.prompt(getParentActivity(), () -> createUsersSelectActivity(view));
        } else if (item.id == requireBiometricsToDeleteChatsRow) {
            CherrygramPrivacyConfig.INSTANCE.setAskPasscodeBeforeDelete(!CherrygramPrivacyConfig.INSTANCE.getAskPasscodeBeforeDelete());
            SettingsHelper.updateCheckState(view, CherrygramPrivacyConfig.INSTANCE.getAskPasscodeBeforeDelete());
        } else if (item.id == allowSystemPinRow) {
            CherrygramPrivacyConfig.INSTANCE.setAllowSystemPasscode(!CherrygramPrivacyConfig.INSTANCE.getAllowSystemPasscode());
            SettingsHelper.updateCheckState(view, CherrygramPrivacyConfig.INSTANCE.getAllowSystemPasscode());
        } else if (item.id == testFingerprintRow) {
            testFingerprint();
        } else if (item.id == googleAnalyticsRow) {
            CherrygramPrivacyConfig.INSTANCE.setGoogleAnalytics(!CherrygramPrivacyConfig.INSTANCE.getGoogleAnalytics());
            SettingsHelper.updateCheckState(view, CherrygramPrivacyConfig.INSTANCE.getGoogleAnalytics());

            FirebaseAnalyticsHelper.INSTANCE.onPrivacyConfigChanged(CherrygramPrivacyConfig.INSTANCE.getGoogleAnalytics());
        } else if (item.id == deleteAccountRow) {
            if (getChatsPasswordHelper().checkBiometricAvailable()) {
                CGBiometricPrompt.prompt(getParentActivity(), () -> DeleteAccountDialog.showDeleteAccountDialog(this));
            } else {
                DeleteAccountDialog.showDeleteAccountDialog(this);
            }
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }

    private String getBiometricCountText() {
        int count = 0;

        if (CherrygramPrivacyConfig.INSTANCE.getAskBiometricsToOpenChat()) count++;
        if (CherrygramPrivacyConfig.INSTANCE.getAskBiometricsToOpenEncrypted()) count++;
        if (CherrygramPrivacyConfig.INSTANCE.getAskBiometricsToOpenArchive()) count++;

        return count + "/3";
    }

    private void createUsersSelectActivity(View view) {
        AndroidUtilities.runOnUIThread(() -> {
            UsersSelectActivity activity = getUsersSelectActivity();
            activity.setDelegate((ids, type) -> {
                Set<Long> chatIds = new HashSet<>(ids);

                Set<String> lockedChats = new HashSet<>(getChatsPasswordHelper().getArrayList(getChatsPasswordHelper().getPasscodeArray()));

                CherrygramLogger.d(() -> "old locked chats array: " + lockedChats);

                lockedChats.clear();

                if (!chatIds.isEmpty()) {
                    for (Long id : chatIds) {
                        if (DialogObject.isUserDialog(id) || DialogObject.isChatDialog(id)) {
                            lockedChats.add(String.valueOf(id));
                        }
                    }
                }

                getChatsPasswordHelper().saveArrayList(
                        new ArrayList<>(lockedChats),
                        getChatsPasswordHelper().getPasscodeArray()
                );

                CherrygramLogger.d(() -> "new locked chats array: " + lockedChats);

                SettingsHelper.updateButtonValue(view, String.valueOf(getChatsPasswordHelper().getLockedChatsCount()));
            });

            presentFragment(activity);
        }, 300);
    }

    private UsersSelectActivity getUsersSelectActivity() {
        ArrayList<Long> chatsList = new ArrayList<>();
        ArrayList<String> lockedChatIds = getChatsPasswordHelper().getArrayList(getChatsPasswordHelper().getPasscodeArray());

        for (String chatIdStr : lockedChatIds) {
            long chatId = Long.parseLong(chatIdStr);

            TLRPC.User user = getMessagesController().getUser(chatId);
            TLRPC.Chat chat = getMessagesController().getChat(-chatId);

            if (user != null) {
                chatsList.add(user.id);
            } else if (chat != null) {
                chatsList.add(-chat.id);
            }
        }

        UsersSelectActivity activity = new UsersSelectActivity(true, chatsList, 0);
        activity.asLockedChats();
        return activity;
    }

    private void testFingerprint() {
        CGBiometricPrompt.fixFingerprint(getParentActivity(), new CGBiometricPrompt.CGBiometricListener() {
            @Override
            public void onSuccess(BiometricPrompt.AuthenticationResult result) {
                handle();
            }

            @Override
            public void onFailed() {
                // showError(0);
            }

            @Override
            public void onError(int error, CharSequence msg) {
                showError(error);
            }

            private void handle() {
                CGBiometricPrompt.cancelPendingAuthentications();
                CGBiometricPrompt.reloadFingerprintState();

                if (listView != null && listView.adapter != null) listView.adapter.update(true);

                if (CGBiometricPrompt.hasFingerprintCached()) {
                    AndroidUtilities.runOnUIThread(() ->
                            BulletinFactory.of(PrivacyPreferencesEntry.this)
                                    .createSimpleBulletin(
                                            R.raw.chats_infotip,
                                            getString(R.string.SP_BiometricUnavailable_Test_Fixed),
                                            getString(R.string.CG_RestartToApply),
                                            getString(R.string.OK),
                                            () -> AppRestartHelper.restartApp(getContext())
                                    ).show(),
                            300
                    );
                } else {
                    showError(0);
                }
            }

            private void showError(int error) {
                String title = getString(R.string.CG_AppCrashed) + (error == 0 ? "" : " (e" + error + ")");

                BulletinFactory.of(PrivacyPreferencesEntry.this).createSimpleBulletin(
                        R.raw.chats_infotip,
                        title,
                        getString(R.string.SP_BiometricUnavailable_Test_Wrong_Desc),
                        getString(R.string.Settings),
                        () -> openFingerprintSettings(getContext())
                ).show();
            }

            private void openFingerprintSettings(Context context) {
                Intent fallbackIntent = new Intent(Settings.ACTION_SECURITY_SETTINGS);

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        Intent fingerprintIntent = new Intent(Settings.ACTION_FINGERPRINT_ENROLL);
                        fingerprintIntent.setPackage("com.android.settings");

                        if (fingerprintIntent.resolveActivity(context.getPackageManager()) != null) {
                            context.startActivity(fingerprintIntent);
                            return;
                        }
                    }
                    context.startActivity(fallbackIntent);
                } catch (SecurityException e) {
                    CherrygramLogger.e(e);
                    context.startActivity(fallbackIntent);
                } catch (Exception e) {
                    CherrygramLogger.e(e);
                }
            }
        });
    }

}
