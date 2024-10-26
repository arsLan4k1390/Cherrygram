package uz.unnarsx.cherrygram.misc

import org.telegram.messenger.BuildConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig

object Constants {

    var CG_VERSION = BuildConfig.BUILD_VERSION_STRING_CHERRY
    var CG_AUTHOR = "Updates: @CherrygramAPKs"

    /** CG Chats Links start**/
    @JvmField
    var CG_CHANNEL_USERNAME = "Cherry_gram"
    @JvmField
    var CG_CHANNEL_URL = "https://t.me/cherry_gram"
    @JvmField
    var CG_CHAT_USERNAME = "CherrygramSupport"
    @JvmField
    var CG_CHAT_URL = "https://t.me/CherrygramSupport"
    @JvmField
    var UPDATE_APP_URL = if (CherrygramCoreConfig.isPlayStoreBuild()) "https://play.google.com/store/apps/details?id=uz.unnarsx.cherrygram" else CG_CHANNEL_URL
    /** CG Chats Links finish**/

    const val PACKAGE_NAME = "uz.unnarsx.cherrygram"

    /** CG Chats IDs start**/
    const val Cherrygram_Owner = 282287840L // Cherrygram Owner (Arslan)
    const val Cherrygram_Channel = 1776033848L // Cherrygram Channel
    const val Cherrygram_Support = 1554776538L // Cherrygram Support Group
    const val Cherrygram_APKs = 1557718915L // Cherrygram APKs
    const val Cherrygram_Beta = 1544768810L // Cherrygram Beta APKs
    const val Cherrygram_Archive = 1719103382L // Cherrygram Archive
    /** CG Chats IDs finish**/

    /** Misc start**/
    const val CHERRY_EMOJI_ID = 5203934494985307875L // Cherrygram logo
    const val CHERRY_EMOJI_ID_14 = 5203934494985307875L // Cherrygram logo (bra)
    const val PROFILE_BACKGROUND_COLOR_ID_GREEN_BLUE = 12 // Blue-Green gradient
    const val PROFILE_BACKGROUND_COLOR_ID_RED = 14 // Red-Pink gradient
    const val REPLY_BACKGROUND_COLOR_ID = 13 // Red-Pink gradient
    /** Misc finish**/

    /** Firebase remote Config start **/
    const val Videomessages_Resolution = "videomessages_resolution"
    const val Is_Donate_Screen_Available = "is_donate_screen_available"
    /** Firebase remote Config finish **/

}