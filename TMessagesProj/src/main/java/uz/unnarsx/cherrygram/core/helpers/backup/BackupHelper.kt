/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.helpers.backup

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.edit
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.json.JSONException
import org.json.JSONObject
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.R
import org.telegram.messenger.SendMessagesHelper
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.LaunchActivity
import uz.unnarsx.cherrygram.core.PermissionsUtils
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupHelper {

    const val FILE_TYPE_CG_BACKUP = 1390

    fun backupSettings(fragment: BaseFragment) {
        if (!PermissionsUtils.isStoragePermissionGranted()) {
            PermissionsUtils.requestStoragePermission(fragment.parentActivity)
            return
        }

        try {
            val formattedDate = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "$formattedDate-settings.cherry"
            val file = File(fragment.context.getExternalFilesDir(null), fileName)
            writeUtf8String(backupSettingsJson(fragment.context), file)
            shareFile(fragment.context, file)
        } catch (e: JSONException) {
            handleError(fragment.context, e)
        }
    }

    fun importSettings(fragment: BaseFragment) {
        if (!PermissionsUtils.isStoragePermissionGranted()) {
            PermissionsUtils.requestStoragePermission(fragment.parentActivity)
            return
        }

        val importActivity = BackupFileImportActivity().apply {
            setMaxSelectedFiles(1)
            setDelegate(object : BackupFileImportActivity.DocumentSelectActivityDelegate {
                override fun didSelectFiles(
                    activity: BackupFileImportActivity,
                    files: ArrayList<String>,
                    caption: String,
                    notify: Boolean,
                    scheduleDate: Int
                ) {
                    activity.finishFragment()
                    importSettings(File(files.first()), fragment.context)
                }

                override fun didSelectPhotos(
                    photos: ArrayList<SendMessagesHelper.SendingMediaInfo>,
                    notify: Boolean,
                    scheduleDate: Int
                ) {}

                override fun startDocumentSelectActivity() {}
            })
        }

        fragment.presentFragment(importActivity)
    }

    fun importSettings(file: File, context: Context) {
        AlertDialog.Builder(context).apply {
            setTitle(getString(R.string.CG_ImportSettings))
            setMessage(getString(R.string.CG_ImportSettingsAlert))
            setNegativeButton(getString(R.string.Cancel), null)
            setPositiveButton(getString(R.string.OK)) { _, _ ->
                importSettingsConfirmed(file, context)
            }
            val dialog = show()
            val button = dialog.getButton(DialogInterface.BUTTON_POSITIVE) as TextView
            button.setTextColor(Theme.getColor(Theme.key_text_RedBold))
        }
    }

    private fun importSettingsConfirmed(file: File, context: Context) {
        try {
            val json = readJsonObjectWithGson(file)
            restoreSharedPreferences(json, context)

            val dialog = AlertDialog(context, 0)
            dialog.setTitle(getString(R.string.CG_AppName))
            dialog.setMessage(getString(R.string.CG_RestartToApply))
            dialog.setPositiveButton(getString(R.string.BotUnblock)) { _, _ ->
                AppRestartHelper.restartApp(context)
            }
            dialog.show()
        } catch (e: Exception) {
            handleError(context, e)
        }
    }

    private fun shareFile(context: Context, fileToShare: File, caption: String = "") {
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, "${ApplicationLoader.getApplicationId()}.provider", fileToShare)
        } else {
            Uri.fromFile(fileToShare)
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, "")
            putExtra(Intent.EXTRA_STREAM, uri)
            if (caption.isNotBlank()) {
                putExtra(Intent.EXTRA_SUBJECT, caption)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            setClass(context, LaunchActivity::class.java)
        }

        context.startActivity(intent)
    }

    private fun writeUtf8String(text: String, file: File) {
        file.parentFile?.let { initDir(it) }
        file.writeText(text)
    }

    private fun readJsonObjectWithGson(file: File): JsonObject {
        file.inputStream().buffered().use { inputStream ->
            InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
                return JsonParser.parseReader(reader).asJsonObject
            }
        }
    }

    private fun restoreSharedPreferences(json: JsonObject, context: Context) {
        for ((spName, data) in json.entrySet()) {
            val prefs = context.getSharedPreferences(spName, Activity.MODE_PRIVATE)
            prefs.edit {
                for ((keyRaw, valueElement) in (data.asJsonObject.entrySet())) {
                    var key = keyRaw
                    val value = valueElement.asJsonPrimitive
                    when {
                        value.isBoolean -> putBoolean(key, value.asBoolean)
                        value.isNumber -> {
                            when {
                                key.endsWith("_long") -> {
                                    key = key.removeSuffix("_long")
                                    putLong(key, value.asLong)
                                }

                                key.endsWith("_float") -> {
                                    key = key.removeSuffix("_float")
                                    putFloat(key, value.asFloat)
                                }

                                else -> putInt(key, value.asInt)
                            }
                        }

                        else -> putString(key, value.asString)
                    }
                }
            }
        }
    }

    private fun initDir(dir: File) {
        if (dir.exists() && dir.isFile) {
            dir.delete()
        }
        dir.mkdirs()
    }

    private fun handleError(context: Context, e: Exception) {
        AndroidUtilities.addToClipboard(e.toString())
        Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show()
    }

    private fun backupSettingsJson(context: Context): String {
        val json = JSONObject()
        val keys = setOf(
            // General
            "CP_NoRounding", "AP_SystemEmoji", "AP_SystemFonts", "AP_Tablet_Mode", "AP_Old_Notification_Icon", "CG_ResidentNotification",
            "CP_HideStories", "CP_ArchiveStoriesFromUsers", "CP_ArchiveStoriesFromChannels", "CP_CustomWallpapers",
            "CP_DisableAnimAvatars", "CP_DisableReactionsOverlay", "CP_DisableReactionAnim", "CP_DisablePremStickAnim",
            "CP_DisablePremStickAutoPlay", "CP_HideSendAsChannel",

            // Appearance
            "AP_Icon_Replacements1", "AP_OneUI_SwitchStyle", "AP_DisableDividers", "AP_CenterTitle",
            "CP_ShowSeconds", "CP_DisablePremiumStatuses",
            "CP_ReplyBackground", "CP_ReplyCustomColors", "CP_ReplyBackgroundEmoji", "CP_ProfileChannelPreview",
            "AP_ShowID_DC_new", "CP_ProfileBirthDatePreview", "CP_ProfileBusinessPreview", "CP_ProfileBackgroundColor",
            "CP_ProfileBackgroundEmoji", "AP_FoldersAtBottom", "AP_FolderNameInHeader", "CP_NewTabs_RemoveAllChats", "CP_NewTabs_NoCounter",
            "AP_TabMode", "AP_TabStyleAddStroke", "AP_DrawSnowInActionBar", "AP_DrawSnowInChat",

            // Chats
            "CP_Shortcut_JumpToBegin", "CP_Shortcut_DeleteAll", "CP_Shortcut_SavedMessages",
            "CP_Shortcut_Browser", "CP_Admins_Reactions", "CP_Admins_Permissions", "CP_Admins_Administrators",
            "CP_Admins_Members", "CP_Admins_Statistics", "CP_Admins_RecentActions", "CP_UnreadBadgeOnBackButton",
            "AP_CenterChatTitle", "CP_Slider_RecentEmojisAmplifier", "CP_Slider_RecentStickersAmplifier",
            "CP_DisableSwipeToNext", "CP_HideMuteUnmuteButton", "CP_HideKeyboardOnScrollIntensity",
            "CP_GeminiApiKey", "CP_GeminiModelName", "CP_ShareDrawStoryButton", "CP_UsersDrawShareButton",
            "CP_SupergroupsDrawShareButton", "CP_ChannelsDrawShareButton", "CP_BotsDrawShareButton",
            "CP_StickersDrawShareButton", "CP_ShowSaveForNotifications", "CP_ShowGemini", "CP_ShowReply",
            "CP_ShowSaveToGallery", "CP_ShowCopyPhoto", "CP_ShowCopyPhotoAsSticker", "CP_ShowSaveToDownloads", "CP_ShowShare",
            "CP_ShowClearFromCache", "CP_ShowForward", "CP_ShowForward_WO_Authorship", "CP_ShowViewHistory", "CP_ShowSaveMessage",
            "CP_ShowReport", "CP_ShowJSON", "CP_JacksonJSON_Provider",
            "CP_LargerVoiceMessagesLayout", "CP_Slider_MediaAmplifier", "CP_Slider_StickerAmplifier", "CP_Slider_GifsAmplifier",
            "CP_EnableMsgFilter", "CP_MsgFiltersElements", "CP_MsgFiltersDetectTranslit",
            "CP_MsgFiltersMatchExactWord", "CP_MsgFiltersDetectEntities", "CP_MsgFiltersHideFromBlocked1",
            "CP_MsgFiltersHideAll", "CP_MsgFiltersCollapseAutomatically", "CP_MsgFilterTransparentMsg",
            "CP_AutoQuoteReplies", "CP_TimeOnStick", "CP_ForwardMsgDate", "AP_PencilIcon",
            "CP_LeftBottomButtonAction", "CP_DoubleTapAction", "CP_MessageSlideAction", "CP_DeleteForAll",
            "CP_LargePhotos", "CP_PlayVideo", "CP_AutoPauseVideo", "CP_DisableVibration",
            "CP_VideoSeekDuration", "CP_Notification_Sound", "CP_VibrationInChats", "CP_SilenceNonContacts", "CG_UnarchiveOnSwipe",

            // Camera
            "CP_CameraType", "CP_DisableAttachCam", "CP_UseDualCamera", "CP_CameraAspectRatio",
            "CP_StartFromUltraWideCam", /* "CP_CameraXFpsRange", */ "CP_CameraStabilisation",
            "CP_CenterCameraControlButtons", "CP_ExposureSlider", "CP_RearCam",

            // Privacy
            "SP_NoProxyPromo", /* "SP_GoogleAnalytics", */ "SP_HideArchiveFromChatsList",
            // "SP_AskBiometricsToOpenArchive", "SP_AskBiometricsToOpenChat", "SP_AskPinBeforeDelete", "SP_AllowSystemPasscode",

            // Experimental
            "EP_SpringAnimation", "EP_ActionbarCrossfade", "CP_CustomChatForSavedMessages",
            "CP_CustomChatIDSM", "EP_DownloadSpeedBoost", "EP_UploadSpeedBoost", "EP_SlowNetworkMode"
        )

        spToJSON("mainconfig", json, keys, context)

        return json.toString(4)
    }

    private fun spToJSON(name: String, target: JSONObject, keys: Set<String>, context: Context) {
        val prefs = context.getSharedPreferences(name, Activity.MODE_PRIVATE)
        val jsonPrefs = JSONObject()

        for ((keyRaw, value) in prefs.all) {
            var key = keyRaw
            if (key !in keys) continue
            when (value) {
                is Long -> key += "_long"
                is Float -> key += "_float"
            }
            jsonPrefs.put(key, value)
        }

        target.put(name, jsonPrefs)
    }

}
