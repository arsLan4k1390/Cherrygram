/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.misc

import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig

object Constants {

    @JvmField
    var CG_AUTHOR = "Updates: @CherrygramAPKs"

    /** CG Links start**/
    @JvmField
    var CG_CHANNEL_USERNAME = "cherrygram"
    @JvmField
    var CG_CHANNEL_URL = "https://t.me/cherrygram"

    @JvmField
    var CG_APKS_CHANNEL_USERNAME = "CherrygramAPKs"
    @JvmField
    var CG_APKS_CHANNEL_URL = "https://t.me/CherrygramAPKs"

    @JvmField
    var CG_BETA_APKS_CHANNEL_USERNAME = "CherrygramBetaAPKs"
    @JvmField
    var CG_BETA_APKS_CHANNEL_URL = "https://t.me/CherrygramBetaAPKs"

    @JvmField
    var CG_CHAT_USERNAME = "CherrygramSupport"
    @JvmField
    var CG_CHAT_URL = "https://t.me/CherrygramSupport"

    @JvmField
    var UPDATE_APP_URL = if (CherrygramCoreConfig.isPlayStoreBuild()) "https://play.google.com/store/apps/details?id=uz.unnarsx.cherrygram" else CG_CHANNEL_URL

    @JvmField
    var CG_PRIVACY_URL = "https://arslan4k1390.github.io/cherrygram/privacy"
    @JvmField
    var CG_DONATIONS_AND_TERMS_URL = "https://arslan4k1390.github.io/cherrygram/donation-terms"

    @JvmField
    var CG_CROWDIN_URL = "https://crowdin.com/project/cherrygram"
    @JvmField
    var CG_GITHUB_URL = "https://github.com/arsLan4k1390/Cherrygram"
    /** CG Links finish**/

    const val PACKAGE_NAME = "uz.unnarsx.cherrygram"

    /** CG Chats IDs start**/
    const val Cherrygram_Owner = 282287840L // Cherrygram Owner (Arslan)
    const val Cherrygram_Channel = 1776033848L // Cherrygram Channel
    const val Cherrygram_Support = 1554776538L // Cherrygram Support Group
    const val Cherrygram_APKs = 1557718915L // Cherrygram APKs
    const val Cherrygram_Beta = 1544768810L // Cherrygram Beta APKs
    const val Cherrygram_Archive = 1719103382L // Cherrygram Archive
    /** CG Chats IDs finish**/

    /** OWNer's friends start */
    const val Yuki = 706402791L
    const val Alina = 553511970L
    const val Samir = 5710829964L
    /** OWNer's friends finish */

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
    const val show_ads_screen_in_settings = "show_ads_screen_in_settings"
    const val videomessages_resolution = "videomessages_resolution"

    const val humo_card_number = "humo_card_number"
    const val tbank_card_number = "tbank_card_number"

    const val allow_use_safestars = "allow_use_safestars"
    const val allow_use_safesurf = "allow_use_safesurf"
    const val safe_stars_URL = "safe_stars_URL"
    const val safe_stars_URL_RU = "safe_stars_URL_RU"
    const val safe_surf_URL = "safe_surf_URL"

    const val show_proxy_in_settings = "show_proxy_in_settings"
    const val proxy_link_in_settings = "proxy_link_in_settings"
    /** Firebase remote Config finish */

}