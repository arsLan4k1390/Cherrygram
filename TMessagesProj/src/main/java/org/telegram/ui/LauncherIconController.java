package org.telegram.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig;

public class LauncherIconController {
    public static void tryFixLauncherIconIfNeeded() {
        for (LauncherIcon icon : LauncherIcon.values()) {
            if (isEnabled(icon)) {
                return;
            }
        }

        setIcon(LauncherIcon.CHERRY);
    }

    public static boolean isEnabled(LauncherIcon icon) {
        Context ctx = ApplicationLoader.applicationContext;
        int i = ctx.getPackageManager().getComponentEnabledSetting(icon.getComponentName(ctx));
        return i == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || i == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT && icon == LauncherIcon.CHERRY;
    }

    public static void setIcon(LauncherIcon icon) {
        Context ctx = ApplicationLoader.applicationContext;
        PackageManager pm = ctx.getPackageManager();
        for (LauncherIcon i : LauncherIcon.values()) {
            pm.setComponentEnabledSetting(i.getComponentName(ctx), i == icon ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
        applySplashTheme(LaunchActivity.instance, icon);
    }

    public enum LauncherIcon {
        CHERRY("CG_Icon_Cherry", R.drawable.icon_background_default, R.drawable.icon_foreground_cherry, R.string.Default, R.style.Theme_CG_Splash_Cherry_Light, R.style.Theme_CG_Splash_Cherry_Night),
        DARK_CHERRY("CG_Icon_Dark", R.drawable.icon_background_dark, R.drawable.icon_foreground_cherry, R.string.AP_ChangeIcon_Dark, R.style.Theme_CG_Splash_Dark_Light, R.style.Theme_CG_Splash_Dark_Night),
        DARK_CHERRY_BRA("CG_Icon_Dark_Bra", R.drawable.icon_background_dark, R.drawable.icon_foreground_cherry_bra, R.string.AP_ChangeIcon_Bra, R.style.Theme_CG_Splash_Dark_Bra_Light, R.style.Theme_CG_Splash_Dark_Bra_Night),
        WHITE_CHERRY("CG_Icon_White_Cherry", R.drawable.icon_background_white, R.drawable.icon_foreground_cherry_white, R.string.AP_ChangeIcon_White, 0, R.style.Theme_CG_Splash_White_Night),
        WHITE_CHERRY_BRA("CG_Icon_White_Cherry_Bra", R.drawable.icon_background_white, R.drawable.icon_foreground_cherry_white_bra, R.string.AP_ChangeIcon_Bra, 0, R.style.Theme_CG_Splash_White_Bra_Night),
        LAGUNA_CHERRY("CG_Icon_Laguna", R.drawable.icon_background_laguna, R.drawable.icon_foreground_cherry, R.string.AP_ChangeIcon_Laguna, R.style.Theme_CG_Splash_Laguna_Light, R.style.Theme_CG_Splash_Laguna_Night),
        AQUA_CHERRY("CG_Icon_Aqua", R.drawable.icon_background_aqua, R.drawable.icon_foreground_cherry, R.string.AppIconAqua, R.style.Theme_CG_Splash_Aqua_Light, R.style.Theme_CG_Splash_Aqua_Night),
        GREEN_CHERRY("CG_Icon_Green", R.drawable.icon_background_green, R.drawable.icon_foreground_cherry, R.string.AP_ChangeIcon_Green, R.style.Theme_CG_Splash_Green_Light, R.style.Theme_CG_Splash_Green_Night),
        LAVANDA_CHERRY("CG_Icon_Lavanda", R.drawable.icon_background_lavanda, R.drawable.icon_foreground_cherry, R.string.AP_ChangeIcon_Lavanda, R.style.Theme_CG_Splash_Lavanda_Light, R.style.Theme_CG_Splash_Lavanda_Night),
        VIOLET_SUNSET_CHERRY("CG_Icon_Violet_Sunset", R.drawable.icon_background_violet_sunset, R.drawable.icon_foreground_cherry, R.string.AP_ChangeIcon_Violet_Sunset, R.style.Theme_CG_Splash_Violet_Sunset_Light, R.style.Theme_CG_Splash_Violet_Sunset_Night),
        VIOLET_SUNSET_CHERRY_BRA("CG_Icon_Violet_Sunset_Bra", R.drawable.icon_background_violet_sunset, R.drawable.icon_foreground_cherry_bra, R.string.AP_ChangeIcon_Bra, R.style.Theme_CG_Splash_Violet_Sunset_Bra_Light, R.style.Theme_CG_Splash_Violet_Sunset_Bra_Night),
        SUNSET_CHERRY("CG_Icon_Sunset", R.drawable.icon_background_sunset, R.drawable.icon_foreground_cherry, R.string.AP_ChangeIcon_Sunset, R.style.Theme_CG_Splash_Sunset_Light, R.style.Theme_CG_Splash_Sunset_Night),
        SUNRISE_CHERRY("CG_Icon_Sunrise", R.drawable.icon_background_sunrise, R.drawable.icon_foreground_cherry, R.string.AP_ChangeIcon_Sunrise, R.style.Theme_CG_Splash_Sunrise_Light, R.style.Theme_CG_Splash_Sunrise_Night),
        TURBO_CHERRY("CG_Icon_Turbo", R.drawable.icon_5_background_sa, R.drawable.icon_foreground_cherry, R.string.AppIconTurbo, 0, 0),
        NOX_CHERRY("CG_Icon_Night", R.mipmap.icon_2_background_sa, R.drawable.icon_foreground_cherry, R.string.AppIconNox, 0, 0),
        DARK_NY("CG_Icon_Dark_NY", R.drawable.icon_background_dark_ny, R.drawable.icon_foreground_cherry_bra, R.string.AP_ChangeIcon_Cherry_NY, R.style.Theme_CG_Splash_Dark_NY_Light, R.style.Theme_CG_Splash_Dark_NY_Night),

        PREMIUM("PremiumIcon", R.drawable.icon_3_background_sa, R.mipmap.icon_3_foreground, R.string.AppIconPremium, 0, 0, true),
        TURBO("TurboIcon", R.drawable.icon_5_background_sa, R.mipmap.icon_5_foreground, R.string.AppIconTurbo, 0, 0, true),
        NOX("NoxIcon", R.mipmap.icon_2_background_sa, R.drawable.icon_foreground, R.string.AppIconNox, 0, 0, true);

        public final String key;
        public final int background;
        public final int foreground;
        public final int title;
        public final boolean premium;

        private ComponentName componentName;

        public ComponentName getComponentName(Context ctx) {
            if (componentName == null) {
                componentName = new ComponentName(ctx.getPackageName(), "uz.unnarsx.cherrygram." + key);
            }
            return componentName;
        }

        LauncherIcon(String key, int background, int foreground, int title, int splashThemeLight, int splashThemeNight) {
            this(key, background, foreground, title, splashThemeLight, splashThemeNight, false);
        }

        LauncherIcon(String key, int background, int foreground, int title, int splashThemeLight, int splashThemeNight, boolean premium) {
            this.key = key;
            this.background = background;
            this.foreground = foreground;
            this.title = title;
            this.splashThemeLight = splashThemeLight;
            this.splashThemeNight = splashThemeNight;
            this.premium = premium;
        }

        /** Cherrygram start */
        public final int splashThemeLight;
        public final int splashThemeNight;
        /** Cherrygram finish */
    }

    /** Cherrygram start */
    private static int appliedSplashTheme;

    public static LauncherIcon getCurrentIcon() {
        for (LauncherIcon icon : LauncherIcon.values()) {
            if (isEnabled(icon)) {
                return icon;
            }
        }
        return LauncherIcon.CHERRY;
    }

    public static void applySplashTheme(Activity activity, LauncherIcon icon) {
        if (Build.VERSION.SDK_INT < 33 || activity == null) return;
        boolean dark = Theme.getActiveTheme() != null && Theme.getActiveTheme().isDark();
        int theme = CherrygramAppearanceConfig.INSTANCE.getIconBasedSplash() ? (dark ? icon.splashThemeNight : icon.splashThemeLight) : 0;
        if (theme == 0) {
            theme = dark ? R.style.Theme_CG_Splash_Base_Night : R.style.Theme_CG_Splash_Base_Light;
        }
        if (theme == appliedSplashTheme) return;
        try {
            activity.getSplashScreen().setSplashScreenTheme(theme);
            appliedSplashTheme = theme;
        } catch (Exception e) {
            FileLog.e(e);
        }
    }
    /** Cherrygram finish */

}
