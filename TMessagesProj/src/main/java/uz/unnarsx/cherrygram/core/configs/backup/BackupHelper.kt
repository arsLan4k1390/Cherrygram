/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.configs.backup

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.edit
import com.google.gson.JsonParser
import org.json.JSONArray
import org.json.JSONObject
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.LaunchActivity
import uz.unnarsx.cherrygram.core.PermissionsUtils
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupHelper {

    const val FILE_TYPE_CG_BACKUP = 1390
    const val BACKUP_EXTENSION = ".cherry"

    private const val SP_NAME = "mainconfig"
    private const val SUFFIX_LONG = "_long"
    private const val SUFFIX_FLOAT = "_float"

    private val BACKUP_KEYS = setOf(
        // General
        "AP_SystemEmoji", "AP_SystemFonts", "AP_Tablet_Mode", "AP_Old_Notification_Icon", "CG_ResidentNotification",
        "CP_HideStories", "CP_ArchiveStoriesFromUsers", "CP_ArchiveStoriesFromChannels", "CP_CustomWallpapers",
        "CP_DisableReactionsOverlay", "CP_DisableReactionAnim", "CP_DisablePremStickAnim",
        "CP_DisablePremStickAutoPlay",

        // Appearance
        "AP_Icon_Replacements1", "AP_OneUI_SwitchStyle", "AP_DisableDividers", "AP_GlareEffect", "AP_CenterTitle",
        "AP_HideSearchField", "AP_IconBasedSplash",
        "CP_ShowSeconds", "CP_DisablePremiumStatuses",
        "CP_ReplyBackground", "CP_ReplyCustomColors", "CP_ReplyBackgroundEmoji", "CP_ProfileHidePhoneNumber", "CP_ProfileChannelPreview",
        "AP_ShowID_DC_new", "CP_ProfileBirthDatePreview", "CP_ProfileBusinessPreview", "CP_ProfileBackgroundColor",
        "CP_ProfileBackgroundEmoji", "AP_FoldersAtBottom", "AP_FolderNameInHeader", "CP_NewTabs_RemoveAllChats", "CP_NewTabs_NoCounter",
        "CP_TabsIncludeMutedInCounter", "AP_TabMode", "AP_TabStyleAddStroke", "AP_DrawSnowInActionBar", "AP_DrawSnowInChat",

        // Bottom tabs
        "AP_ShowMainTabs", "AP_OpenSettingsBySwipe", "AP_MainTabsPosition_New", "AP_ShowSearchInTabs_New",
        "AP_ShowMainTabsTitle", "AP_MainTabsForceOpenChats",

        // Chats
        "CP_Shortcut_JumpToBegin", "CP_Shortcut_DeleteAll", "CP_Shortcut_SavedMessages",
        "CP_Shortcut_Browser", "CP_Admins_Reactions", "CP_Admins_Permissions", "CP_Admins_Administrators",
        "CP_Admins_Members", "CP_Admins_Statistics", "CP_Admins_RecentActions", "CP_UnreadBadgeOnBackButton", "CP_UnreadBadgeOnBackButton_iOS",
        "AP_CenterChatTitle_v2", "AP_CenterChatTitle_AdaptiveWidth_v2", "CP_Slider_RecentEmojisAmplifier", "CP_Slider_RecentStickersAmplifier",
        "CP_DisableSwipeToNext", "CP_iOSMessageInputField", "CP_HideSendAsChannel", "CP_HideMuteUnmuteButton", "CP_HideKeyboardOnScrollIntensity",
        "CP_GeminiApiKey", "CP_GeminiModelName", "CP_GeminiSystemPrompt", "CP_GeminiTemperature", "CP_VoiceTranscriptionProvider",
        "translationKeyboardTarget", "translationTarget", "translationTargetGemini",
        "CP_ShareDrawStoryButton", "CP_UsersDrawShareButton",
        "CP_SupergroupsDrawShareButton", "CP_ChannelsDrawShareButton", "CP_BotsDrawShareButton",
        "CP_StickersDrawShareButton", "CP_ShowSaveForNotifications", "CP_ShowGemini", "CP_ShowReply",
        "CP_ShowSaveToGallery", "CP_ShowCopyPhoto", "CP_ShowCopyPhotoAsSticker", "CP_ShowSaveToDownloads", "CP_ShowShare",
        "CP_ShowClearFromCache", "CP_ShowForward", "CP_ShowForward_WO_Authorship", "CP_ShowViewHistory", "CP_ShowSaveMessage",
        "CP_ShowReport", "CP_OpenInExternalApp", "CP_ShowJSON", "CP_JacksonJSON_Provider",
        "CP_LargerVoiceMessagesLayout", "CP_Slider_MediaAmplifier", "CP_Slider_StickerAmplifier", "CP_Slider_GifsAmplifier",
        "CP_EnableMsgFilter", "CP_MsgFiltersElements", "CP_MsgFiltersDetectTranslit",
        "CP_MsgFiltersMatchExactWord", "CP_MsgFiltersDetectEntities", "CP_MsgFiltersFullyHideMessages", "CP_MsgFiltersHideFromBlocked1",
        "CP_MsgFiltersHideAll", "CP_MsgFiltersCollapseAutomatically", "CP_MsgFilterTransparentMsg",
        "CP_MsgFiltersExcludeUsers", "CP_MsgFiltersExcludeBots", "CP_MsgFiltersExcludeGroups", "CP_MsgFiltersExcludeChannels",
        "CP_AutoQuoteReplies", "CP_WideMessagesLayout", "CP_TimeOnStick", "CP_ForwardMsgDate", "AP_PencilIcon",
        "CP_LeftBottomButtonAction", "CP_DoubleTapAction", "CP_MessageSlideAction", "CP_DeleteForAll",
        "CP_MuteSystemSounds", "CP_VoiceMessagesAutoPlay", "CP_PlayVideo", "CP_AutoPauseVideo", "CP_DisableVibration", "CP_OpenLinksInIV",
        "CP_VideoSeekDuration", "CP_Notification_Sound", "CP_VibrationInChats", "CP_SilenceNonContacts", "CG_UnarchiveOnSwipe",
        "CP_RecordMultiMic", "CP_RecordInStereo", "CG_SortByUnread",

        // Camera
        "CP_CameraType1", "CP_DisableAttachCam", "CP_UseDualCamera", "CP_CameraAspectRatio",
        "CP_StartFromUltraWideCam", /* "CP_CameraFpsRange", */
        "CP_VideoStabilisation", "CP_OpticalStabilisation", "CP_ContinuousAutofocus", "CP_NoiceReduction", "CP_FaceDetection",
        "CP_CenterCameraControlButtons", "CP_ExposureSlider", "CP_RearCam",

        // Privacy
        /* "SP_GoogleAnalytics", */ "SP_HideArchiveFromChatsList",
        // "SP_AskBiometricsToOpenArchive", "SP_AskBiometricsToOpenChat", "SP_AskPinBeforeDelete", "SP_AllowSystemPasscode",

        // Experimental
        "CG_NavigationAnimation", "CG_ActionbarCrossfade", "CP_CustomChatForSavedMessages",
        "CP_CustomChatIDSM", "EP_DownloadSpeedBoost", "EP_UploadSpeedBoost", "EP_SlowNetworkMode"
    )

    fun backupSettings(fragment: BaseFragment?) {
        val context = fragment?.context ?: return

        try {
            val prefsJson = JSONObject()
            for ((key, value) in context.getSharedPreferences(SP_NAME, Activity.MODE_PRIVATE).all) {
                if (key !in BACKUP_KEYS) continue
                when (value) {
                    is Long -> prefsJson.put(key + SUFFIX_LONG, value)
                    is Float -> prefsJson.put(key + SUFFIX_FLOAT, value)
                    is Set<*> -> prefsJson.put(key, JSONArray(value))
                    else -> prefsJson.put(key, value)
                }
            }

            val formattedDate = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val dir = context.getExternalFilesDir(null) ?: context.filesDir
            val file = File(dir, "$formattedDate-settings$BACKUP_EXTENSION")
            file.writeText(JSONObject().put(SP_NAME, prefsJson).toString(4), Charsets.UTF_8)

            val uri = FileProvider.getUriForFile(context, "${ApplicationLoader.getApplicationId()}.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setClass(context, LaunchActivity::class.java)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            handleError(context, e)
        }
    }

    fun importSettings(fragment: BaseFragment?) {
        if (fragment == null || fragment.parentActivity == null) return
        val context = fragment.context ?: return

        if (!PermissionsUtils.isStoragePermissionGranted()) {
            PermissionsUtils.requestStoragePermission(fragment.parentActivity)
            return
        }

        fragment.presentFragment(BackupFileImportActivity().apply {
            setOnFileSelected { file -> importSettings(file, context) }
        })
    }

    fun importSettings(file: File, context: Context) {
        if (!file.exists() || !file.canRead()) {
            handleError(context, Exception("File not accessible"))
            return
        }

        AlertDialog.Builder(context).apply {
            setTitle(getString(R.string.CG_ImportSettings))
            setMessage(getString(R.string.CG_ImportSettingsAlert))
            setNegativeButton(getString(R.string.Cancel), null)
            setPositiveButton(getString(R.string.OK)) { _, _ ->
                importSettingsConfirmed(file, context)
            }
            val dialog = show()
            val button = dialog.getButton(DialogInterface.BUTTON_POSITIVE) as? TextView
            button?.setTextColor(Theme.getColor(Theme.key_text_RedBold))
        }
    }

    private fun importSettingsConfirmed(file: File, context: Context) {
        try {
            val prefsJson = file.bufferedReader(Charsets.UTF_8).use { JsonParser.parseReader(it) }
                .asJsonObject
                .getAsJsonObject(SP_NAME)
                ?: throw IllegalArgumentException("Not a Cherrygram backup")

            context.getSharedPreferences(SP_NAME, Activity.MODE_PRIVATE).edit {
                for ((rawKey, element) in prefsJson.entrySet()) {
                    val key = rawKey.removeSuffix(SUFFIX_LONG).removeSuffix(SUFFIX_FLOAT)
                    if (key !in BACKUP_KEYS || element.isJsonNull) continue
                    when {
                        element.isJsonArray -> putStringSet(key, element.asJsonArray.mapTo(HashSet()) { it.asString })
                        !element.isJsonPrimitive -> continue
                        rawKey.endsWith(SUFFIX_LONG) -> putLong(key, element.asLong)
                        rawKey.endsWith(SUFFIX_FLOAT) -> putFloat(key, element.asFloat)
                        element.asJsonPrimitive.isBoolean -> putBoolean(key, element.asBoolean)
                        element.asJsonPrimitive.isNumber -> putInt(key, element.asInt)
                        else -> putString(key, element.asString)
                    }
                }
            }

            showRestartDialog(context)
        } catch (e: Exception) {
            handleError(context, e)
        }
    }

    fun resetSettings(fragment: BaseFragment?) {
        val context = fragment?.context ?: return

        AlertDialog.Builder(context).apply {
            setTitle(getString(R.string.CG_ResetSettings))
            setMessage(getString(R.string.CG_ResetSettingsAlert))
            setNegativeButton(getString(R.string.Cancel), null)
            setPositiveButton(getString(R.string.Reset)) { _, _ ->
                try {
                    context.getSharedPreferences(SP_NAME, Activity.MODE_PRIVATE).edit(commit = true) {
                        for (key in CherrygramResetKeys.KEYS) remove(key)
                    }
                    showRestartDialog(context)
                } catch (e: Exception) {
                    handleError(context, e)
                }
            }
            val dialog = show()
            val button = dialog.getButton(DialogInterface.BUTTON_POSITIVE) as? TextView
            button?.setTextColor(Theme.getColor(Theme.key_text_RedBold))
        }
    }

    private fun showRestartDialog(context: Context) {
        val dialog = AlertDialog(context, 0)
        dialog.setTitle(getString(R.string.CG_AppName))
        dialog.setMessage(getString(R.string.CG_RestartToApply))
        dialog.setPositiveButton(getString(R.string.BotUnblock)) { _, _ ->
            AppRestartHelper.restartApp(context)
        }
        dialog.show()
    }

    private fun handleError(context: Context, e: Exception) {
        try {
            AndroidUtilities.addToClipboard(e.toString())
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {}
    }

}