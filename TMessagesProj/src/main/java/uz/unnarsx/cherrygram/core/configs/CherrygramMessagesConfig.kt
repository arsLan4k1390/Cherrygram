/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.configs

import android.app.Activity
import android.content.SharedPreferences
import android.os.Build
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.LocaleController
import uz.unnarsx.cherrygram.chats.gemini.GeminiButtonsLayout
import uz.unnarsx.cherrygram.preferences.boolean
import uz.unnarsx.cherrygram.preferences.int
import uz.unnarsx.cherrygram.preferences.string

object CherrygramMessagesConfig {

    private val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)

    /** Gemini AI start */
    var geminiApiKey by sharedPreferences.string("CP_GeminiApiKey", " ")
    var geminiModelName by sharedPreferences.string("CP_GeminiModelName", " ")
    var geminiSystemPrompt by sharedPreferences.string("CP_GeminiSystemPrompt", " ")
    var geminiTemperatureValue by sharedPreferences.int("CP_GeminiTemperature", 5)

    const val TRANSCRIPTION_PROVIDER_TELEGRAM = 0
    const val TRANSCRIPTION_PROVIDER_GEMINI = 1
    var voiceTranscriptionProvider by sharedPreferences.int("CP_VoiceTranscriptionProvider", TRANSCRIPTION_PROVIDER_TELEGRAM)
    /** Gemini AI finish */

    /** Appearance start */
    /** Message menu start */
    var blurMessageMenuBackground by sharedPreferences.boolean("CP_BlurMessageMenuBackground", false)
    var msgMenuUnifiedScroll by sharedPreferences.boolean("CP_MsgMenuUnifiedScrollForce", true)
    var msgMenuAutoScroll by sharedPreferences.boolean("CP_MsgMenuAutoScroll", true)
    var msgMenuFixedHeight by sharedPreferences.boolean("CP_MsgMenuFixedHeightForce", true)
    var blurMessageMenuItems by sharedPreferences.boolean("CP_BlurMessageMenuItems", false)
    var msgMenuNativeBlur by sharedPreferences.boolean("CP_MsgMenuNativeBlur", Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
    /** Message menu items start */
    var showSaveForNotifications by sharedPreferences.boolean("CP_ShowSaveForNotifications", true)
    var showGemini by sharedPreferences.boolean("CP_ShowGemini", GeminiButtonsLayout.geminiButtonsVisible())
    var showReply by sharedPreferences.boolean("CP_ShowReply", true)
    var showSaveToGallery by sharedPreferences.boolean("CP_ShowSaveToGallery", true)
    var showCopyPhoto by sharedPreferences.boolean("CP_ShowCopyPhoto", true)
    var showCopyPhotoAsSticker by sharedPreferences.boolean("CP_ShowCopyPhotoAsSticker", true)
    var showSaveToDownloads by sharedPreferences.boolean("CP_ShowSaveToDownloads", true)
    var showShare by sharedPreferences.boolean("CP_ShowShare", true)
    var showClearFromCache by sharedPreferences.boolean("CP_ShowClearFromCache", true)
    var showForward by sharedPreferences.boolean("CP_ShowForward", false)
    var showForwardWoAuthorship by sharedPreferences.boolean("CP_ShowForward_WO_Authorship", false)
    var showViewHistory by sharedPreferences.boolean("CP_ShowViewHistory", true)
    var showSaveMessage by sharedPreferences.boolean("CP_ShowSaveMessage", false)
    var showReport by sharedPreferences.boolean("CP_ShowReport", true)

    var showJSON by sharedPreferences.boolean("CP_ShowJSON", false)
    var jacksonJSON_Provider by sharedPreferences.boolean("CP_JacksonJSON_Provider", Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)

    var msgMenuItemsCompactView by sharedPreferences.boolean("CP_MsgMenuItemsCompactView", false)
    /** Message menu items finish */
    /** Message menu finish */

    /** Messages size start */
    var largerVoiceMessagesLayout by sharedPreferences.boolean("CP_LargerVoiceMessagesLayout", true)
    var slider_mediaAmplifier by sharedPreferences.int("CP_Slider_MediaAmplifier", 100)
    var slider_stickerAmplifier by sharedPreferences.int("CP_Slider_StickerAmplifier", 100)
    var slider_gifsAmplifier by sharedPreferences.int("CP_Slider_GifsAmplifier", 100)
    /** Messages size finish */

    /** Direct share start */
    var shareDrawStoryButton by sharedPreferences.boolean("CP_ShareDrawStoryButton", true)
    var usersDrawShareButton by sharedPreferences.boolean("CP_UsersDrawShareButton", false)
    var supergroupsDrawShareButton by sharedPreferences.boolean("CP_SupergroupsDrawShareButton", false)
    var channelsDrawShareButton by sharedPreferences.boolean("CP_ChannelsDrawShareButton", true)
    var botsDrawShareButton by sharedPreferences.boolean("CP_BotsDrawShareButton", true)
    var stickersDrawShareButton by sharedPreferences.boolean("CP_StickersDrawShareButton", false)
    /** Direct share finish */

    var hideStickerTime by sharedPreferences.boolean("CP_TimeOnStick", false)
    var msgForwardDate by sharedPreferences.boolean("CP_ForwardMsgDate", true)
    var showPencilIcon by sharedPreferences.boolean("AP_PencilIcon", true)
    /** Appearance finish */

    /** Actions start */
    /** Messages filter start */
    var enableMsgFilters by sharedPreferences.boolean("CP_EnableMsgFilter", false)
    var msgFiltersElements by sharedPreferences.string("CP_MsgFiltersElements", "")
    var msgFiltersDetectTranslit by sharedPreferences.boolean("CP_MsgFiltersDetectTranslit", false)
    var msgFiltersMatchExactWord by sharedPreferences.boolean("CP_MsgFiltersMatchExactWord", false)
    var msgFiltersDetectEntities by sharedPreferences.boolean("CP_MsgFiltersDetectEntities", false)
    var msgFiltersHideFromBlocked by sharedPreferences.boolean("CP_MsgFiltersHideFromBlocked1", false)
    var msgFiltersHideAll by sharedPreferences.boolean("CP_MsgFiltersHideAll", false)
    var msgFiltersCollapseAutomatically by sharedPreferences.boolean("CP_MsgFiltersCollapseAutomatically", false)
    var msgFilterTransparentMsg by sharedPreferences.boolean("CP_MsgFilterTransparentMsg", false)
    /** Messages filter finish */

    const val LEFT_BUTTON_FORWARD_WO_AUTHORSHIP = 0
    const val LEFT_BUTTON_REPLY = 1
    const val LEFT_BUTTON_SAVE_MESSAGE= 2
    const val LEFT_BUTTON_DIRECT_SHARE = 3
    const val LEFT_BUTTON_FORWARD_WO_CAPTION = 4
    var leftBottomButton by sharedPreferences.int("CP_LeftBottomButtonAction", LEFT_BUTTON_FORWARD_WO_AUTHORSHIP)

    const val DOUBLE_TAP_ACTION_NONE = 0
    const val DOUBLE_TAP_ACTION_REACTION = 1
    const val DOUBLE_TAP_ACTION_REPLY = 2
    const val DOUBLE_TAP_ACTION_SAVE = 3
    const val DOUBLE_TAP_ACTION_EDIT = 4
    const val DOUBLE_TAP_ACTION_TRANSLATE = 5
    const val DOUBLE_TAP_ACTION_TRANSLATE_GEMINI = 6
    var doubleTapAction by sharedPreferences.int("CP_DoubleTapAction", DOUBLE_TAP_ACTION_REACTION)

    const val MESSAGE_SLIDE_ACTION_REPLY = 0
    const val MESSAGE_SLIDE_ACTION_SAVE = 1
    const val MESSAGE_SLIDE_ACTION_TRANSLATE = 2
    const val MESSAGE_SLIDE_ACTION_DIRECT_SHARE = 3
    const val MESSAGE_SLIDE_ACTION_TRANSLATE_GEMINI = 4
    var messageSlideAction by sharedPreferences.int("CP_MessageSlideAction", MESSAGE_SLIDE_ACTION_REPLY)

    var deleteForAll by sharedPreferences.boolean("CP_DeleteForAll", false)
    /** Actions finish */

    /** Telegram Premium start */
    var disableReactionsOverlay by sharedPreferences.boolean("CP_DisableReactionsOverlay", false)
    var disableReactionAnim by sharedPreferences.boolean("CP_DisableReactionAnim", false)
    var disablePremStickAnim by sharedPreferences.boolean("CP_DisablePremStickAnim", false)
    var disablePremStickAutoPlay by sharedPreferences.boolean("CP_DisablePremStickAutoPlay", false)
    /** Telegram Premium finish */

    /** Misc start */
    var gifSpoilers by sharedPreferences.boolean("CG_GifSpoiler", false)
    var photoAsSticker by sharedPreferences.boolean("CG_PhotoAsSticker", false)

    /** Translator start */
    var translationKeyboardTarget by sharedPreferences.string("translationKeyboardTarget", "app")
    var translationTarget by sharedPreferences.string("translationTarget", "app")
    var translationTargetGemini by sharedPreferences.string("translationTargetGemini", LocaleController.getInstance().currentLocale.language)
    /** Translator finish */
    /** Misc finish */

}