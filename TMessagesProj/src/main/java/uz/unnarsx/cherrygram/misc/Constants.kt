/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.misc

import org.telegram.messenger.BuildConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig

object Constants {

    fun getCherryVersion() : String {
        return BuildConfig.BUILD_VERSION_STRING_CHERRY
    }
    var CG_AUTHOR = "Updates: @CherrygramAPKs"

    /** CG Chats Links start**/
    @JvmField
    var CG_CHANNEL_USERNAME = "cherrygram"
    @JvmField
    var CG_CHANNEL_URL = "https://t.me/cherrygram"

    @JvmField
    var CG_APKS_CHANNEL_USERNAME = "CherrygramAPKs"
    @JvmField
    var CG_APKS_CHANNEL_URL = "https://t.me/CherrygramAPKs"

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
    const val CHERRY_EMOJI_ID = 5220045200780458122L // Cherrygram logo
    const val CHERRY_EMOJI_ID_BRA = 5222458839256825177L // Cherrygram logo (bra)
    const val CHERRY_EMOJI_ID_VERIFIED = 5449476181864779205L // Cherrygram Verified adaptive logo
    const val CHERRY_EMOJI_ID_VERIFIED_BRA = 5451850156318181341L // Cherrygram Verified Bra adaptive logo
    const val CHERRY_EMOJI_ID_DONATE = 5411229175971322671L // Cherry emoji with eyeglasses
    const val CHERRY_EMOJI_ID_PREMIUM = 5393391313502609448L // Cherry emoji with stars
    const val CHERRY_EMOJI_ID_PREMIUM_MOON = 5370777017904011118L // Evil moon emoji
    const val PROFILE_BACKGROUND_COLOR_ID_GREEN_BLUE = 12 // Blue-Green gradient
    const val PROFILE_BACKGROUND_COLOR_ID_RED = 14 // Red-Pink gradient
    const val REPLY_BACKGROUND_COLOR_ID = 13 // Red-Pink gradient
    /** Misc finish**/

    /** Firebase remote Config start */
    const val Videomessages_Resolution = "videomessages_resolution"
    const val Is_Donate_Screen_Available = "is_donate_screen_available"
    const val Re_Tg_Check = "re_tg_check"
    const val is_new_updates_ui_available = "is_new_updates_ui_available"
    /** Firebase remote Config finish */

}