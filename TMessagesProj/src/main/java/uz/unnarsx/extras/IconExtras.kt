package uz.unnarsx.extras

import android.content.ComponentName
import android.content.pm.PackageManager
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.BuildConfig

object IconExtras {
    enum class Icon(val mf: String) {
        DEFAULT("CG_Icon_Default"),
        ALT_WHITE("CG_Icon_White"),
        ALT_MONET_SAMSUNG("CG_Icon_Monet_Samsung"),
        ALT_MONET_PIXEL("CG_Icon_Monet_Pixel"),
    }

    fun setIcon(variant: Int) {
        setIcon(Icon.values()[variant])
    }

    private fun setIcon(icon: Icon) {
        Icon.values().forEach {
            if (it == icon) {
                enableComponent(it.mf)
            } else {
                disableComponent(it.mf)
            }
        }
    }

    private fun enableComponent(name: String) {
        ApplicationLoader.applicationContext.packageManager.setComponentEnabledSetting(
            ComponentName(BuildConfig.APPLICATION_ID, "org.telegram.messenger.$name"),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
        )
    }

    private fun disableComponent(name: String) {
        ApplicationLoader.applicationContext.packageManager.setComponentEnabledSetting(
            ComponentName(BuildConfig.APPLICATION_ID, "org.telegram.messenger.$name"),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        )
    }
}