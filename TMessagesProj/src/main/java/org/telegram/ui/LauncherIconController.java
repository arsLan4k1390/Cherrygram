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

        setIcon(LauncherIcon.DEFAULT);
    }

    public static void updateMonetIcon() {
        if (isEnabled(LauncherIcon.ALT_MONET_SAMSUNG)) {
            setIcon(LauncherIcon.DEFAULT);
            setIcon(LauncherIcon.ALT_MONET_SAMSUNG);
        }
        if (isEnabled(LauncherIcon.ALT_MONET_PIXEL)) {
            setIcon(LauncherIcon.DEFAULT);
            setIcon(LauncherIcon.ALT_MONET_PIXEL);
        }
    }

    public static boolean isEnabled(LauncherIcon icon) {
        Context ctx = ApplicationLoader.applicationContext;
        int i = ctx.getPackageManager().getComponentEnabledSetting(icon.getComponentName(ctx));
        return i == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || i == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT && icon == LauncherIcon.DEFAULT;
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
        DEFAULT("DefaultIcon", R.drawable.ic_launcher_background, R.mipmap.ic_launcher_sa_foreground, R.string.AP_ChangeIcon_Default),
        ALT_WHITE("CG_Icon_White", R.drawable.ic_launcher_white_background, R.mipmap.ic_launcher_white_foreground, R.string.AP_ChangeIcon_White),
        ALT_MONET_SAMSUNG("CG_Icon_Monet_Samsung", R.color.ic_launcher_background_samsung, R.drawable.ic_launcher_foreground_samsung, R.string.AP_ChangeIcon_Monet_Samsung),
        ALT_MONET_PIXEL("CG_Icon_Monet_Pixel", R.color.ic_launcher_background_pixel, R.drawable.ic_launcher_foreground_pixel, R.string.AP_ChangeIcon_Monet_Pixel),
        VINTAGE("VintageIcon", R.drawable.icon_6_background_sa, R.mipmap.icon_6_foreground_sa, R.string.AppIconVintage),
        AQUA("AquaIcon", R.drawable.icon_4_background_sa, R.mipmap.ic_launcher_sa_foreground, R.string.AppIconAqua),
        PREMIUM("PremiumIcon", R.drawable.icon_3_background_sa, R.mipmap.icon_3_foreground_sa, R.string.AppIconPremium/*, true*/),
        TURBO("TurboIcon", R.drawable.icon_5_background_sa, R.mipmap.icon_5_foreground_sa, R.string.AppIconTurbo/*, true*/),
        NOX("NoxIcon", R.drawable.icon_2_background_sa, R.mipmap.ic_launcher_sa_foreground, R.string.AppIconNox/*, true*/);

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
