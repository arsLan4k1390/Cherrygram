/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
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
import org.telegram.ui.Stars.StarsIntroActivity;

import java.util.Locale;

import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.misc.Constants;
import uz.unnarsx.cherrygram.preferences.CherrygramPreferencesNavigator;

public class DeeplinkHelper {

    public static void processDeepLink(Uri uri, BaseFragment fragment, Callback callback, Runnable unknown, Browser.Progress progress) {
        if (fragment == null) {
            fragment = LaunchActivity.getSafeLastFragment();
        }
        if (fragment == null) {
            return;
        }
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
                case "cg_about" -> fragment = CherrygramPreferencesNavigator.INSTANCE.createAbout();
                case "cg_appearance" -> fragment = CherrygramPreferencesNavigator.INSTANCE.createAppearance();
                case "cg_camera", "cg_cam" -> {
                    CherrygramPreferencesNavigator.INSTANCE.createCamera(fragment);
                    return;
                }
                case "cg_chats" -> fragment = CherrygramPreferencesNavigator.INSTANCE.createChats();
                case "cg_message_menu", "cg_messages_menu", "cg_ios_menu" -> {
                    CherrygramPreferencesNavigator.INSTANCE.createMessageMenu(fragment);
                    return;
                }
                case "cg_debug" -> fragment = CherrygramPreferencesNavigator.INSTANCE.createDebug();
                case "cg_donate", "cg_donates", "cg_support", "cg_badge" -> {
                    CherrygramPreferencesNavigator.INSTANCE.createDonate(fragment);
                    return;
                }
                case "cg_stars" -> {
                    if (CherrygramCoreConfig.INSTANCE.getAllowSafeStars()) {
                        CherrygramPreferencesNavigator.INSTANCE.createStars(fragment, null, null, -1);
                    } else {
                        new StarsIntroActivity.StarsOptionsSheet(fragment.getContext(), fragment.getResourceProvider()).show();
                    }
                    return;
                }
                case "cg_donate_force", "cg_donates_force", "cg_support_force", "cg_support_f", "cg_badge_force" -> {
                    CherrygramPreferencesNavigator.INSTANCE.createDonateForce(fragment);
                    return;
                }
                case "cg_drawer" -> {
                    CherrygramPreferencesNavigator.INSTANCE.createDrawerPrefs(fragment);
                    return;
                }
                case "cg_drawer_items" -> {
                    CherrygramPreferencesNavigator.INSTANCE.createDrawerItems(fragment);
                    return;
                }
                case "cg_experimental" -> {
                    CherrygramPreferencesNavigator.INSTANCE.createExperimental(fragment);
                    return;
                }
                case "cg_filter", "cg_filters" -> {
                    CherrygramPreferencesNavigator.INSTANCE.createMessageFilter(fragment);
                    return;
                }
                case "cg_folders", "cg_tabs" -> {
                    CherrygramPreferencesNavigator.INSTANCE.createFoldersPrefs(fragment);
                    return;
                }
                case "cg_gemini" -> {
                    CherrygramPreferencesNavigator.INSTANCE.createGemini(fragment);
                    return;
                }
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
                case "cg_settings", "cg_main" -> fragment = CherrygramPreferencesNavigator.INSTANCE.createMainMenu(false);
                case "cg_update", "cg_upgrade", "update", "upgrade" -> {
                    if (CherrygramCoreConfig.isPlayStoreBuild()) {
                        Browser.openUrl(fragment.getContext(), Constants.UPDATE_APP_URL);
                        return;
                    } else if (CherrygramCoreConfig.isStandalonePremiumBuild()) {
                        // Fuckoff :)
                        unknown.run();
                        return;
                    } else {
                        LaunchActivity.instance.checkCgUpdates(fragment, progress, true);
                        return;
                    }
                }
                case "cg_updates", "updates" -> {
                    if (CherrygramCoreConfig.isPlayStoreBuild()) {
                        Browser.openUrl(fragment.getContext(), Constants.UPDATE_APP_URL);
                    } else if (CherrygramCoreConfig.isStandalonePremiumBuild()) {
                        // Fuckoff :)
                        unknown.run();
                        return;
                    } else if (!CherrygramCoreConfig.isStandalonePremiumBuild()) {
                        LaunchActivity.instance.showCgUpdaterSettings(fragment);
                    }
                    return;
                }
                case "cg_username_limits" -> {
                    fragment.showDialog(new LimitReachedBottomSheet(fragment, fragment.getContext(), LimitReachedBottomSheet.TYPE_PUBLIC_LINKS, fragment.getCurrentAccount(), fragment.getResourceProvider()));
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
