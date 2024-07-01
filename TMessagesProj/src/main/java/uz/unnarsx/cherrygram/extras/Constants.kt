package uz.unnarsx.cherrygram.extras

import org.telegram.messenger.BuildConfig
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import uz.unnarsx.cherrygram.CherrygramConfig

object Constants {

    var CG_VERSION = BuildConfig.BUILD_VERSION_STRING_CHERRY
    var CG_AUTHOR = "Updates: @CherrygramAPKs"

    fun getAppName(): String {
        if (CherrygramConfig.isStableBuild() || CherrygramConfig.isPlayStoreBuild()) {
            return "Cherrygram"
        } else if (CherrygramConfig.isBetaBuild()) {
            return "Cherrygram Beta"
        } else if (CherrygramConfig.isPremiumBuild()) {
            return "Cherrygram Premium"
        } else if (CherrygramConfig.isDevBuild()) {
            return "Cherrygram Dev"
        }
        return LocaleController.getString("CG_AppName", R.string.CG_AppName)
    }

    fun getBuildType(): String {
        if (CherrygramConfig.isStableBuild()) {
            return LocaleController.getString("UP_BTRelease", R.string.UP_BTRelease)
        } else if (CherrygramConfig.isPlayStoreBuild()) {
            return "Play Store"
        } else if (CherrygramConfig.isBetaBuild()) {
            return LocaleController.getString("UP_BTBeta", R.string.UP_BTBeta)
        } else if (CherrygramConfig.isPremiumBuild()) {
            return "Premium"
        } else if (CherrygramConfig.isDevBuild()) {
            return "Dev"
        }
        return "Unknown"
    }

    const val Cherrygram_Owner = 282287840L // Cherrygram Owner (Arslan)
    const val Cherrygram_Channel = 1776033848L // Cherrygram Channel
    const val Cherrygram_Support = 1554776538L // Cherrygram Support Group
    const val Cherrygram_APKs = 1557718915L // Cherrygram APKs
    const val Cherrygram_Beta = 1544768810L // Cherrygram Beta APKs
    const val Cherrygram_Archive = 1719103382L // Cherrygram Archive

    const val CHERRY_EMOJI_ID = 5203934494985307875L // Cherrygram logo
    const val CHERRY_EMOJI_ID_14 = 5203934494985307875L // Cherrygram logo (bra)
    const val PROFILE_BACKGROUND_COLOR_ID_GREEN_BLUE = 12 // Blue-Green gradient
    const val PROFILE_BACKGROUND_COLOR_ID_RED = 14 // Red-Pink gradient
    const val REPLY_BACKGROUND_COLOR_ID = 13 // Red-Pink gradient

}