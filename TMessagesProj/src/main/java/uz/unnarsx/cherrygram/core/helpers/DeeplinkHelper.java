/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.helpers;

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
import uz.unnarsx.cherrygram.core.ui.CGBulletinCreator;
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
                case DeepLinksRepo.CG_About-> {
                    CherrygramPreferencesNavigator.INSTANCE.createAbout(fragment);
                    return;
                }
                case DeepLinksRepo.CG_Appearance -> {
                    CherrygramPreferencesNavigator.INSTANCE.createAppearance(fragment);
                    return;
                }
                case DeepLinksRepo.CG_Camera -> {
                    CherrygramPreferencesNavigator.INSTANCE.createCamera(fragment);
                    return;
                }
                case DeepLinksRepo.CG_Chats -> fragment = CherrygramPreferencesNavigator.INSTANCE.createChats();
                case DeepLinksRepo.CG_Message_Menu, "cg_messages_menu", "cg_ios_menu" -> {
                    CherrygramPreferencesNavigator.INSTANCE.createMessageMenu(fragment);
                    return;
                }
                case DeepLinksRepo.CG_Debug -> {
                    CherrygramPreferencesNavigator.INSTANCE.createDebug(fragment);
                    return;
                }
                case DeepLinksRepo.CG_Support, "cg_donate", "cg_donates", "cg_badge" -> {
                    CherrygramPreferencesNavigator.INSTANCE.createDonate(fragment);
                    return;
                }
                case DeepLinksRepo.CG_Support_Force, "cg_donate_force", "cg_donates_force", "cg_support_f", "cg_badge_force" -> {
                    CherrygramPreferencesNavigator.INSTANCE.createDonateForce(fragment);
                    return;
                }
                case DeepLinksRepo.CG_Stars -> {
                    if (CherrygramCoreConfig.INSTANCE.getAllowSafeStars()) {
                        CherrygramPreferencesNavigator.INSTANCE.createStars(fragment, null, null, -1);
                    } else {
                        new StarsIntroActivity.StarsOptionsSheet(fragment.getContext(), fragment.getResourceProvider()).show();
                    }
                    return;
                }
                case DeepLinksRepo.CG_Experimental -> {
                    CherrygramPreferencesNavigator.INSTANCE.createExperimental(fragment);
                    return;
                }
                case DeepLinksRepo.CG_Message_Filters, "cg_filter" -> {
                    CherrygramPreferencesNavigator.INSTANCE.createMessageFilter(fragment);
                    return;
                }
                case DeepLinksRepo.CG_Folders -> {
                    CherrygramPreferencesNavigator.INSTANCE.createFoldersPrefs(fragment);
                    return;
                }
                /*case DeepLinksRepo.CG_Luck, "luck" -> {
                    unknown.run();
                    return;
                }*/
                case DeepLinksRepo.CG_Gemini -> {
                    CherrygramPreferencesNavigator.INSTANCE.createGemini(fragment);
                    return;
                }
                case DeepLinksRepo.CG_General -> fragment = CherrygramPreferencesNavigator.INSTANCE.createGeneral();
                case DeepLinksRepo.CG_Messages_And_Profiles -> {
                    CherrygramPreferencesNavigator.INSTANCE.createMessagesAndProfiles(fragment);
                    return;
                }
                case DeepLinksRepo.CG_Premium -> {
                    // Fuckoff :)
                    unknown.run();
                    return;
                }
                case DeepLinksRepo.CG_Privacy, "cg_security" -> fragment = CherrygramPreferencesNavigator.INSTANCE.createPrivacyAndSecurity();
                case DeepLinksRepo.CG_Restart, "cg_reboot", "restart", "reboot" -> {
                    CGBulletinCreator.INSTANCE.createRestartBulletin(fragment);
                    return;
                }
                case DeepLinksRepo.CG_Settings, "cg_main" -> {
                    CherrygramPreferencesNavigator.INSTANCE.createCherrySettings(fragment);
                    return;
                }
                case DeepLinksRepo.CG_Tabs -> {
                    CherrygramPreferencesNavigator.INSTANCE.createTabs(fragment);
                    return;
                }
                case DeepLinksRepo.CG_Update, "cg_upgrade", "update", "upgrade" -> {
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
                case DeepLinksRepo.CG_Updater_Bottom_Sheet, "updates" -> {
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
                case DeepLinksRepo.CG_Username_Limits -> {
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

    public static class DeepLinksRepo {

        public static final String CG_Settings = "cg_settings";

        public static final String CG_General = "cg_general";

        public static final String CG_Appearance = "cg_appearance";
        public static final String CG_Folders = "cg_folders";
        public static final String CG_Luck = "cg_luck";
        public static final String CG_Tabs = "cg_tabs";
        public static final String CG_Messages_And_Profiles = "cg_messages_profiles";

        public static final String CG_Chats = "cg_chats";
        public static final String CG_Gemini = "cg_gemini";
        public static final String CG_Message_Menu = "cg_message_menu";
        public static final String CG_Message_Filters = "cg_filters";

        public static final String CG_Camera = "cg_camera";

        public static final String CG_Experimental = "cg_experimental";

        public static final String CG_Privacy = "cg_privacy";

        public static final String CG_Support = "cg_support";
        public static final String CG_Support_Force = "cg_support_force";
        public static final String CG_Stars = "cg_stars";

        public static final String CG_Restart = "cg_restart";

        public static final String CG_About = "cg_about";
        public static final String CG_Debug = "cg_debug";
        public static final String CG_Update = "cg_update";
        public static final String CG_Updater_Bottom_Sheet = "cg_updates";

        public static final String CG_Username_Limits = "cg_username_limits";

        public static final String CG_Premium = "cg_premium";
    }

}
