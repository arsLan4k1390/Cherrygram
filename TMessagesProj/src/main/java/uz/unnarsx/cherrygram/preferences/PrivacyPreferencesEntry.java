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
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import org.telegram.ui.UsersSelectActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import uz.unnarsx.cherrygram.core.CGBiometricPrompt;
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.core.configs.CherrygramPrivacyConfig;
import uz.unnarsx.cherrygram.core.crashlytics.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper;
import uz.unnarsx.cherrygram.core.ui.CGBulletinCreator;
import uz.unnarsx.cherrygram.helpers.ui.PopupHelper;
import uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper;

public class PrivacyPreferencesEntry extends UniversalFragment {

    private final int proxySponsorRow = 1;
    private final int googleAnalyticsRow = 2;
    private final int deleteAccountRow = 3;

    private final int hideArchiveFromChatsListRow = 4;
    private final int askBiometricsToOpenChatsRow = 5;

    private final int lockedChatsRow = 6;
    private final int requireBiometricsToDeleteChatsRow = 7;
    private final int allowSystemPinRow = 8;
    private final int testFingerprintRow = 9;

    private final int hideArchivedStoriesRow = 1390;

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("privacy_preferences_screen");
        return getString(R.string.SettingsPrivacySecurity);
    }

    @Override
    public View createView(Context context) {
        setMD3(true);
        return super.createView(context);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(getString(R.string.SP_Header_Privacy)));
        items.add(SettingsHelper.asSwitchCG(proxySponsorRow, getString(R.string.SP_NoProxyPromo))
                .setChecked(CherrygramPrivacyConfig.INSTANCE.getHideProxySponsor())
        );
        items.add(SettingsHelper.asSwitchCG(googleAnalyticsRow, getString(R.string.SP_GoogleAnalytics), getString(R.string.SP_GoogleAnalytics_Desc))
                .setChecked(CherrygramPrivacyConfig.INSTANCE.getGoogleAnalytics())
        );

        UItem deleteAccountButton = UItem.asButton(
                deleteAccountRow,
                R.drawable.msg_delete,
                getString(R.string.SP_DeleteAccount)
        );
        deleteAccountButton.red = true;
        items.add(deleteAccountButton);
        items.add(UItem.asShadow(null));

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
            items.add(UItem.asButton(askBiometricsToOpenChatsRow, R.drawable.msg_pin_code, getString(R.string.SP_AskBioToOpenChats)));
            items.add(UItem.asShadow(getString(R.string.SP_AskBioToOpenChats_Desc)));
            if (CherrygramPrivacyConfig.INSTANCE.getAskBiometricsToOpenChat()) {
                items.add(UItem.asButton(lockedChatsRow, R.drawable.msg_discussion, getString(R.string.SP_LockedChats), String.valueOf(getChatsPasswordHelper().getLockedChatsCount())));
            }
            items.add(SettingsHelper.asSwitchCG(requireBiometricsToDeleteChatsRow, getString(R.string.SP_AskPinBeforeDelete), getString(R.string.SP_AskPinBeforeDelete_Desc)));
            items.add(SettingsHelper.asSwitchCG(allowSystemPinRow, getString(R.string.SP_AllowUseSystemPasscode), getString(R.string.SP_AllowUseSystemPasscode_Desc)));
        }
        items.add(UItem.asButton(testFingerprintRow, R.drawable.fingerprint, getString(R.string.SP_TestFingerprint)));
        items.add(UItem.asShadow(getString(R.string.SP_TestFingerprint_Desc)));
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == proxySponsorRow) {
            CherrygramPrivacyConfig.INSTANCE.setHideProxySponsor(!CherrygramPrivacyConfig.INSTANCE.getHideProxySponsor());
            SettingsHelper.updateCheckState(view, CherrygramPrivacyConfig.INSTANCE.getHideProxySponsor());

            getMessagesController().checkPromoInfo(true);
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
        } else if (item.id == hideArchivedStoriesRow) {
            CherrygramPrivacyConfig.INSTANCE.setHideArchivedStories(!CherrygramPrivacyConfig.INSTANCE.getHideArchivedStories());
            SettingsHelper.updateCheckState(view, CherrygramPrivacyConfig.INSTANCE.getHideArchivedStories());

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        } else if (item.id == hideArchiveFromChatsListRow) {
            CherrygramPrivacyConfig.INSTANCE.setHideArchiveFromChatsList(!CherrygramPrivacyConfig.INSTANCE.getHideArchiveFromChatsList());
            SettingsHelper.updateCheckState(view, CherrygramPrivacyConfig.INSTANCE.getHideArchiveFromChatsList());
        } else if (item.id == askBiometricsToOpenChatsRow) {
            CGBiometricPrompt.prompt(getParentActivity(), this::showPasscodeItemsSelector);
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
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }

    private void createUsersSelectActivity(View view) {
        AndroidUtilities.runOnUIThread(() -> {
            UsersSelectActivity activity = getUsersSelectActivity();
            activity.setDelegate((ids, type) -> {
                // Убираем дубликаты через HashSet
                Set<Long> chatIds = new HashSet<>(ids);

                // Получаем текущий список заблокированных чатов
                Set<String> lockedChats = new HashSet<>(getChatsPasswordHelper().getArrayList(getChatsPasswordHelper().getPasscodeArray()));

                if (CherrygramCoreConfig.isDevBuild()) {
                    FileLog.d("old locked chats array: " + lockedChats);
                }

                lockedChats.clear();

                if (!chatIds.isEmpty()) {
                    for (Long id : chatIds) {
                        if (DialogObject.isUserDialog(id) || DialogObject.isChatDialog(id)) {
                            lockedChats.add(String.valueOf(id));
                        }
                    }
                }

                // Сохраняем, преобразуя Set обратно в ArrayList
                getChatsPasswordHelper().saveArrayList(
                        new ArrayList<>(lockedChats),
                        getChatsPasswordHelper().getPasscodeArray()
                );

                if (CherrygramCoreConfig.isDevBuild()) {
                    FileLog.d("new locked chats array: " + lockedChats);
                }

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
                    FileLog.e(e);
                    context.startActivity(fallbackIntent);
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
        });
    }

    private void showPasscodeItemsSelector() {
        List<MenuItemConfig> menuItems = Arrays.asList(
                new MenuItemConfig(
                        getString(R.string.FilterChats),
                        0,
                        CherrygramPrivacyConfig.INSTANCE::getAskBiometricsToOpenChat,
                        () -> {
                            CherrygramPrivacyConfig.INSTANCE.setAskBiometricsToOpenChat(!CherrygramPrivacyConfig.INSTANCE.getAskBiometricsToOpenChat());
                            if (listView != null && listView.adapter != null) listView.adapter.update(true);
                        }
                ),
                new MenuItemConfig(
                        getString(R.string.SecretChat),
                        0,
                        CherrygramPrivacyConfig.INSTANCE::getAskBiometricsToOpenEncrypted,
                        () -> CherrygramPrivacyConfig.INSTANCE.setAskBiometricsToOpenEncrypted(!CherrygramPrivacyConfig.INSTANCE.getAskBiometricsToOpenEncrypted())
                ),
                new MenuItemConfig(
                        getString(R.string.ArchivedChats),
                        0,
                        CherrygramPrivacyConfig.INSTANCE::getAskBiometricsToOpenArchive,
                        () -> CherrygramPrivacyConfig.INSTANCE.setAskBiometricsToOpenArchive(!CherrygramPrivacyConfig.INSTANCE.getAskBiometricsToOpenArchive()),
                        false
                )
        );

        ArrayList<String> prefTitle = new ArrayList<>();
        ArrayList<Integer> prefIcon = new ArrayList<>();
        ArrayList<Boolean> prefCheck = new ArrayList<>();
        ArrayList<Boolean> prefDivider = new ArrayList<>();
        ArrayList<Runnable> clickListener = new ArrayList<>();

        for (MenuItemConfig item : menuItems) {
            prefTitle.add(item.title());
            prefIcon.add(item.icon());
            prefCheck.add(item.isChecked().get());
            prefDivider.add(item.divider());
            clickListener.add(item.toggle());
        }

        PopupHelper.showSwitchAlert(
                getString(R.string.SelectChats),
                this,
                prefTitle,
                prefIcon,
                prefCheck,
                null,
                null,
                prefDivider,
                clickListener,
                null
        );
    }

    public record MenuItemConfig(
            String title,
            int icon,
            Supplier<Boolean> isChecked,
            Runnable toggle,
            boolean divider
    ) {
        public MenuItemConfig(String title, int icon, Supplier<Boolean> isChecked, Runnable toggle) {
            this(title, icon, isChecked, toggle, true);
        }
    }

}
