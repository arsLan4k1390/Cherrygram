package org.telegram.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;

public class LauncherIconController {
    public static void tryFixLauncherIconIfNeeded() {
        for (LauncherIcon icon : LauncherIcon.values()) {
            if (isEnabled(icon)) {
                return;
            }
        }

        setIcon(LauncherIcon.CHERRY);
    }

    public static void updateMonetIcon() {
        if (isEnabled(LauncherIcon.MONET_CHERRY_SAMSUNG)) {
            setIcon(LauncherIcon.CHERRY);
            setIcon(LauncherIcon.MONET_CHERRY_SAMSUNG);
        }
        if (isEnabled(LauncherIcon.MONET_CHERRY_PIXEL)) {
            setIcon(LauncherIcon.CHERRY);
            setIcon(LauncherIcon.MONET_CHERRY_PIXEL);
        }
        if (isEnabled(LauncherIcon.MONET_SAMSUNG)) {
            setIcon(LauncherIcon.CHERRY);
            setIcon(LauncherIcon.MONET_SAMSUNG);
        }
        if (isEnabled(LauncherIcon.MONET_PIXEL)) {
            setIcon(LauncherIcon.CHERRY);
            setIcon(LauncherIcon.MONET_PIXEL);
        }
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
    }

    public enum LauncherIcon {
        CHERRY("CG_Icon_Cherry", R.drawable.icon_background_default, R.drawable.icon_foreground_cherry, R.string.AP_ChangeIcon_Cherry),
        MONET_CHERRY_SAMSUNG("CG_Icon_Monet_Samsung", R.color.icon_background_cherry_samsung, R.drawable.icon_foreground_cherry_samsung, R.string.AP_ChangeIcon_Monet_Samsung),
        MONET_CHERRY_PIXEL("CG_Icon_Monet_Pixel", R.color.icon_background_cherry_pixel, R.drawable.icon_foreground_cherry_pixel, R.string.AP_ChangeIcon_Monet_Pixel),
        DARK_CHERRY("CG_Icon_Dark", R.drawable.icon_background_dark, R.drawable.icon_foreground_cherry, R.string.AP_ChangeIcon_Dark),
        WHITE_CHERRY("CG_Icon_White_Cherry", R.drawable.icon_background_white, R.drawable.ic_launcher_foreground_white, R.string.AP_ChangeIcon_White),
        LAGUNA_CHERRY("CG_Icon_Laguna", R.drawable.icon_background_laguna, R.drawable.icon_foreground_cherry, R.string.AP_ChangeIcon_Laguna),
        AQUA_CHERRY("CG_Icon_Aqua", R.drawable.icon_background_aqua, R.drawable.icon_foreground_cherry, R.string.AppIconAqua),
        GREEN_CHERRY("CG_Icon_Green", R.drawable.icon_background_green, R.drawable.icon_foreground_cherry, R.string.AP_ChangeIcon_Green),
        LAVANDA_CHERRY("CG_Icon_Lavanda", R.drawable.icon_background_lavanda, R.drawable.icon_foreground_cherry, R.string.AP_ChangeIcon_Lavanda),
        SUNSET_CHERRY("CG_Icon_Sunset", R.drawable.icon_background_sunset, R.drawable.icon_foreground_cherry, R.string.AP_ChangeIcon_Sunset),
        SUNRISE_CHERRY("CG_Icon_Sunrise", R.drawable.icon_background_sunrise, R.drawable.icon_foreground_cherry, R.string.AP_ChangeIcon_Sunrise),
        TURBO_CHERRY("CG_Icon_Turbo", R.drawable.icon_background_turbo, R.drawable.icon_foreground_cherry, R.string.AppIconTurbo),
        NOX_CHERRY("CG_Icon_Night", R.mipmap.icon_background_nox, R.drawable.icon_foreground_cherry, R.string.AppIconNox),
        DARK_CHERRY_BRA("CG_Icon_Dark_Bra", R.drawable.icon_background_dark, R.drawable.icon_foreground_cherry_bra, R.string.AP_ChangeIcon_Dark_Bra),
        CHERRY_NY("CG_Icon_Cherry_NY", R.drawable.icon_background_cherry_ny, R.drawable.icon_foreground_cherry_ny, R.string.AP_ChangeIcon_Cherry_NY),
        DARK_NY("CG_Icon_Dark_NY", R.drawable.icon_background_dark_ny, R.drawable.icon_foreground_cherry_ny, R.string.AP_ChangeIcon_Cherry_NY),

        OLD("Old_Icon", R.drawable.icon_background_default, R.drawable.icon_foreground, R.string.AP_ChangeIcon_Default),
        MONET_SAMSUNG("Monet_Icon_Samsung", R.color.icon_background_samsung, R.drawable.icon_foreground_samsung, R.string.AP_ChangeIcon_Monet_Samsung),
        MONET_PIXEL("Monet_Icon_Pixel", R.color.icon_background_pixel, R.drawable.icon_foreground_pixel, R.string.AP_ChangeIcon_Monet_Pixel),
        DARK("DarkIcon", R.drawable.icon_background_dark, R.drawable.icon_foreground, R.string.AP_ChangeIcon_Dark),
        WHITE("White_Icon", R.drawable.icon_background_white, R.drawable.icon_foreground_white, R.string.AP_ChangeIcon_White),
        LAGUNA("LagunaIcon", R.drawable.icon_background_laguna, R.drawable.icon_foreground, R.string.AP_ChangeIcon_Laguna),
        AQUA("AquaIcon", R.drawable.icon_background_aqua, R.drawable.icon_foreground, R.string.AppIconAqua),
        GREEN("GreenIcon", R.drawable.icon_background_green, R.drawable.icon_foreground, R.string.AP_ChangeIcon_Green),
        LAVANDA("LavandaIcon", R.drawable.icon_background_lavanda, R.drawable.icon_foreground, R.string.AP_ChangeIcon_Lavanda),
        SUNSET("SunsetIcon", R.drawable.icon_background_sunset, R.drawable.icon_foreground, R.string.AP_ChangeIcon_Sunset),
        SUNRISE("SunriseIcon", R.drawable.icon_background_sunrise, R.drawable.icon_foreground, R.string.AP_ChangeIcon_Sunrise),
        PREMIUM("PremiumIcon", R.drawable.icon_background_premium, R.mipmap.icon_foreground_premium, R.string.AppIconPremium, true),
        TURBO("TurboIcon", R.drawable.icon_background_turbo, R.mipmap.icon_foreground_turbo, R.string.AppIconTurbo, true),
        NOX("NoxIcon", R.mipmap.icon_background_nox, R.drawable.icon_foreground, R.string.AppIconNox, true);

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

        LauncherIcon(String key, int background, int foreground, int title) {
            this(key, background, foreground, title, false);
        }

        LauncherIcon(String key, int background, int foreground, int title, boolean premium) {
            this.key = key;
            this.background = background;
            this.foreground = foreground;
            this.title = title;
            this.premium = premium;
        }
    }
}
