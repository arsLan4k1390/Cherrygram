/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.core.helpers;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.Premium.LimitReachedBottomSheet;
import org.telegram.ui.LaunchActivity;

import java.util.Locale;

import uz.unnarsx.cherrygram.preferences.GeminiPreferencesBottomSheet;
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.misc.Constants;
import uz.unnarsx.cherrygram.preferences.CameraPreferencesEntry;
import uz.unnarsx.cherrygram.preferences.ExperimentalPreferencesEntry;
import uz.unnarsx.cherrygram.preferences.drawer.DrawerPreferencesEntry;
import uz.unnarsx.cherrygram.preferences.folders.FoldersPreferencesEntry;
import uz.unnarsx.cherrygram.preferences.tgkit.CherrygramPreferencesNavigator;

public class DeeplinkHelper {

    public static void processDeepLink(Uri uri, BaseFragment fragment, Callback callback, Runnable unknown, Browser.Progress progress) {
        if (uri == null) {
            unknown.run();
            return;
        }
        var segments = uri.getPathSegments();
        if (segments.isEmpty() || segments.size() > 2) {
            unknown.run();
            return;
        }

        if (segments.size() == 1) {
            var segment = segments.get(0).toLowerCase(Locale.US);
            switch (segment) {
                case "cg_settings", "cg_main" -> fragment = CherrygramPreferencesNavigator.INSTANCE.createMainMenu();
                case "cg_about" -> fragment = CherrygramPreferencesNavigator.INSTANCE.createAbout();
                case "cg_appearance" -> fragment = CherrygramPreferencesNavigator.INSTANCE.createAppearance();
                case "cg_camera", "cg_cam" -> fragment = new CameraPreferencesEntry();
                case "cg_chats" -> fragment = CherrygramPreferencesNavigator.INSTANCE.createChats();
                case "cg_debug" -> fragment = CherrygramPreferencesNavigator.INSTANCE.createDebug();
                case "cg_donate", "cg_donates", "cg_support", "cg_badge" -> fragment = CherrygramPreferencesNavigator.INSTANCE.createDonate();
                case "cg_drawer" -> fragment = new DrawerPreferencesEntry();
                case "cg_experimental" -> fragment = new ExperimentalPreferencesEntry();
                case "cg_folders", "cg_tabs" -> fragment = new FoldersPreferencesEntry();
                case "cg_general" -> fragment = CherrygramPreferencesNavigator.INSTANCE.createGeneral();
                case "cg_premium" -> {
                    // Fuckoff :)
                    unknown.run();
                    return;
                }
                case "cg_privacy", "cg_security" -> fragment = CherrygramPreferencesNavigator.INSTANCE.createPrivacyAndSecurity();
                case "cg_restart", "cg_reboot", "restart", "reboot" -> {
                    AppRestartHelper.triggerRebirth(fragment.getContext(), new Intent(fragment.getContext(), LaunchActivity.class));
                    return;
                }
                case "cg_update", "cg_upgrade", "update", "upgrade" -> {
                    if (CherrygramCoreConfig.INSTANCE.isPlayStoreBuild()) {
                        Browser.openUrl(fragment.getContext(), Constants.UPDATE_APP_URL);
                        return;
                    } else if (CherrygramCoreConfig.INSTANCE.isStandalonePremiumBuild()) {
                        // Fuckoff :)
                        unknown.run();
                        return;
                    } else {
                        LaunchActivity.instance.checkCgUpdates(fragment, progress, true);
                        return;
                    }
                }
                case "cg_updates", "updates" -> {
                    if (CherrygramCoreConfig.INSTANCE.isPlayStoreBuild()) {
                        Browser.openUrl(fragment.getContext(), Constants.UPDATE_APP_URL);
                    } else if (CherrygramCoreConfig.INSTANCE.isStandalonePremiumBuild()) {
                        // Fuckoff :)
                        unknown.run();
                        return;
                    } else if (!CherrygramCoreConfig.INSTANCE.isStandalonePremiumBuild()) {
                        LaunchActivity.instance.showCgUpdaterSettings(fragment);
                    }
                    return;
                }
                case "cg_username_limits" -> {
                    fragment.showDialog(new LimitReachedBottomSheet(fragment, fragment.getContext(), LimitReachedBottomSheet.TYPE_PUBLIC_LINKS, fragment.getCurrentAccount(), fragment.getResourceProvider()));
                    return;
                }
                case "cg_gemini" -> {
                    GeminiPreferencesBottomSheet.showAlert(fragment);
                    return;
                }
                default -> {
                    unknown.run();
                    return;
                }
            }
        }
        callback.presentFragment(fragment);
    }

    public interface Callback {
        void presentFragment(BaseFragment fragment);
    }

}
