/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.preferences

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import androidx.core.util.Pair
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ChatThemeController
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.MessagesController
import org.telegram.messenger.NotificationCenter
import org.telegram.messenger.R
import org.telegram.messenger.SharedConfig
import org.telegram.messenger.UserConfig
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.Components.Paint.PersistColorPalette
import org.telegram.ui.RestrictedLanguagesSelectActivity
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramDebugConfig
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper
import uz.unnarsx.cherrygram.core.helpers.FirebaseAnalyticsHelper
import uz.unnarsx.cherrygram.preferences.tgkit.preference.category
import uz.unnarsx.cherrygram.preferences.tgkit.preference.contract
import uz.unnarsx.cherrygram.preferences.tgkit.preference.hint
import uz.unnarsx.cherrygram.preferences.tgkit.preference.list
import uz.unnarsx.cherrygram.preferences.tgkit.preference.switch
import uz.unnarsx.cherrygram.preferences.tgkit.preference.textIcon
import uz.unnarsx.cherrygram.preferences.tgkit.preference.tgKitScreen
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitTextIconRow
import androidx.core.content.edit
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Components.AlertsCreator
import androidx.core.net.toUri

class DebugPreferencesEntry : BasePreferencesEntry {
    override fun getPreferences(bf: BaseFragment) = tgKitScreen("Debug // WIP") {
        category("Misc") {
            switch {
                isAvailable = !CherrygramCoreConfig.isStandaloneStableBuild() && !CherrygramCoreConfig.isPlayStoreBuild()
                title = "Toast all RPC errors *"
                description = "you'll see RPC errors from Telegram's backend as toast messages."

                contract({
                    return@contract CherrygramDebugConfig.showRPCErrors
                }) {
                    CherrygramDebugConfig.showRPCErrors = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
            switch {
                title = "Default time style in chats *"
                description = "unlike IOS and TDesktop"

                contract({
                    return@contract CherrygramDebugConfig.oldTimeStyle
                }) {
                    CherrygramDebugConfig.oldTimeStyle = it
                }
            }
            textIcon {
                title = "Force performance class:"
                value = SharedConfig.performanceClassName(SharedConfig.getDevicePerformanceClass())

                listener = TGKitTextIconRow.TGTIListener {
                    val builder = AlertDialog.Builder(bf.parentActivity, bf.resourceProvider)
                    builder.setTitle("Force performance class")
                    val currentClass = SharedConfig.getDevicePerformanceClass()
                    val trueClass = SharedConfig.measureDevicePerformanceClass()
                    builder.setItems(
                        arrayOf<CharSequence>(
                            AndroidUtilities.replaceTags(
                                (if (currentClass == SharedConfig.PERFORMANCE_CLASS_HIGH) "**HIGH**" else "HIGH") + (if (trueClass == SharedConfig.PERFORMANCE_CLASS_HIGH) " (measured)" else "")
                            ),
                            AndroidUtilities.replaceTags((if (currentClass == SharedConfig.PERFORMANCE_CLASS_AVERAGE) "**AVERAGE**" else "AVERAGE") + (if (trueClass == SharedConfig.PERFORMANCE_CLASS_AVERAGE) " (measured)" else "")),
                            AndroidUtilities.replaceTags((if (currentClass == SharedConfig.PERFORMANCE_CLASS_LOW) "**LOW**" else "LOW") + (if (trueClass == SharedConfig.PERFORMANCE_CLASS_LOW) " (measured)" else ""))
                        )
                    ) { _: DialogInterface?, which: Int ->
                        val newClass = 2 - which
                        if (newClass == trueClass) {
                            SharedConfig.overrideDevicePerformanceClass(-1)
                        } else {
                            SharedConfig.overrideDevicePerformanceClass(newClass)
                        }
                        bf.parentLayout.rebuildAllFragmentViews(true, true)
                        value = SharedConfig.performanceClassName(newClass)
                        AppRestartHelper.createRestartBulletin(bf)
                    }
                    builder.setNegativeButton(
                        getString(R.string.Cancel),
                        null
                    )
                    builder.show()
                }
            }
            textIcon {
                isAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
                title = "Fix calls notification"

                listener = TGKitTextIconRow.TGTIListener {
                    bf.showDialog(
                        AlertDialog.Builder(bf.parentActivity)
                            .setTopAnimation(
                                R.raw.permission_request_apk,
                                AlertsCreator.PERMISSIONS_REQUEST_TOP_ICON_SIZE,
                                false,
                                Theme.getColor(Theme.key_dialogTopBackground)
                            )
                            .setMessage(getString(R.string.PermissionFSILockscreen))
                            .setPositiveButton(getString(R.string.PermissionOpenSettings)) { _, _ ->
                                val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                                    data = ("package:" + bf.context.packageName).toUri()
                                }
                                try {
                                    bf.parentActivity.startActivity(intent)
                                } catch (ignored: Exception) { }
                            }
                            .setNegativeButton(getString(R.string.ContactsPermissionAlertNotNow), null)
                            .create()
                    )

                }
            }
        }
        category("Redesign") {
            switch {
                isAvailable = Build.VERSION.SDK_INT >= 31
                title = "New blur (GPU)"

                contract({
                    return@contract SharedConfig.useNewBlur
                }) {
                    SharedConfig.toggleUseNewBlur()
                }
            }
            switch {
                title = "MD3 Containers *"
                description = "Material Design 3 containers inside settings"

                contract({
                    return@contract CherrygramDebugConfig.mdContainers
                }) {
                    CherrygramDebugConfig.mdContainers = it
                }
            }
        }
        category("Chats") {
            switch {
                title = "Force Forum Tabs"

                contract({
                    return@contract SharedConfig.forceForumTabs
                }) {
                    SharedConfig.toggleForceForumTabs()
                }
            }
            switch {
                title = "Replace punctuation marks"
                description = "Replace quotation marks and dashes like on TDesktop"

                contract({
                    return@contract CherrygramDebugConfig.replacePunctuationMarks
                }) {
                    CherrygramDebugConfig.replacePunctuationMarks = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
            switch {
                title = "EditTextSugestionsFix *"
                description = "Emojis/formatting disappear when samsung puts suggestions in edit"

                contract({
                    return@contract CherrygramDebugConfig.editTextSuggestionsFix
                }) {
                    CherrygramDebugConfig.editTextSuggestionsFix = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
            list {
                isAvailable = Build.VERSION.SDK_INT >= 29
                title = "Microphone Audio Source *"

                contract({
                    return@contract listOf(
                        Pair(CherrygramDebugConfig.AUDIO_SOURCE_DEFAULT, "DEFAULT"),
                        Pair(CherrygramDebugConfig.AUDIO_SOURCE_CAMCORDER, "CAMCORDER"),
                        Pair(CherrygramDebugConfig.AUDIO_SOURCE_MIC, "MIC"),
                        Pair(CherrygramDebugConfig.AUDIO_SOURCE_REMOTE_SUBMIX, "REMOTE_SUBMIX"),
                        Pair(CherrygramDebugConfig.AUDIO_SOURCE_UNPROCESSED, "UNPROCESSED"),
                        Pair(CherrygramDebugConfig.AUDIO_SOURCE_VOICE_CALL, "VOICE_CALL"),
                        Pair(CherrygramDebugConfig.AUDIO_SOURCE_VOICE_COMMUNICATION, "VOICE_COMMUNICATION"),
                        Pair(CherrygramDebugConfig.AUDIO_SOURCE_VOICE_DOWNLINK, "VOICE_DOWNLINK"),
                        Pair(CherrygramDebugConfig.AUDIO_SOURCE_VOICE_PERFORMANCE, "VOICE_PERFORMANCE"),
                        Pair(CherrygramDebugConfig.AUDIO_SOURCE_VOICE_RECOGNITION, "VOICE_RECOGNITION"),
                        Pair(CherrygramDebugConfig.AUDIO_SOURCE_VOICE_UPLINK, "VOICE_UPLINK")
                    )
                }, {
                    return@contract when (CherrygramDebugConfig.audioSource) {
                        CherrygramDebugConfig.AUDIO_SOURCE_CAMCORDER -> "CAMCORDER"
                        CherrygramDebugConfig.AUDIO_SOURCE_MIC -> "MIC"
                        CherrygramDebugConfig.AUDIO_SOURCE_REMOTE_SUBMIX -> "REMOTE_SUBMIX"
                        CherrygramDebugConfig.AUDIO_SOURCE_UNPROCESSED -> "UNPROCESSED"
                        CherrygramDebugConfig.AUDIO_SOURCE_VOICE_CALL -> "VOICE_CALL"
                        CherrygramDebugConfig.AUDIO_SOURCE_VOICE_COMMUNICATION -> "VOICE_COMMUNICATION"
                        CherrygramDebugConfig.AUDIO_SOURCE_VOICE_DOWNLINK -> "VOICE_DOWNLINK"
                        CherrygramDebugConfig.AUDIO_SOURCE_VOICE_PERFORMANCE -> "VOICE_PERFORMANCE"
                        CherrygramDebugConfig.AUDIO_SOURCE_VOICE_RECOGNITION -> "VOICE_RECOGNITION"
                        CherrygramDebugConfig.AUDIO_SOURCE_VOICE_UPLINK -> "VOICE_UPLINK"
                        else -> "DEFAULT"
                    }
                }) {
                    CherrygramDebugConfig.audioSource = it
                }
            }
            switch {
                title = "Send videos at max quality *"
                description = "Max quality will be automatically selected when you send a video"

                contract({
                    return@contract CherrygramDebugConfig.sendVideosAtMaxQuality
                }) {
                    CherrygramDebugConfig.sendVideosAtMaxQuality = it
                }
            }
            switch {
                title = "Play GIFs as Videos *"

                contract({
                    return@contract CherrygramDebugConfig.playGIFsAsVideos
                }) {
                    CherrygramDebugConfig.playGIFsAsVideos = it
                }
            }
            switch {
                title = "Hide video timestamp *"
                description = "Saved Progress for Videos. Videos you watch now automatically save your progress – so you can return exactly where you left off."

                contract({
                    return@contract CherrygramDebugConfig.hideVideoTimestamp
                }) {
                    CherrygramDebugConfig.hideVideoTimestamp = it
                }
            }
            textIcon {
                title = getString(R.string.DebugMenuResetDialogs)

                listener = TGKitTextIconRow.TGTIListener {
                    bf.messagesController.forceResetDialogs()
                    AppRestartHelper.createDebugSuccessBulletin(bf)
                }
            }
            textIcon {
                title = getString(R.string.DebugMenuClearMediaCache)

                listener = TGKitTextIconRow.TGTIListener {
                    bf.messagesStorage.clearSentMedia()

                    SharedConfig.setNoSoundHintShowed(false)

                    MessagesController.getGlobalMainSettings().edit {
                        remove("archivehint").remove("proximityhint").remove("archivehint_l")
                            .remove("speedhint").remove("gifhint").remove("reminderhint")
                            .remove("soundHint").remove("themehint").remove("bganimationhint")
                            .remove("filterhint").remove("n_0").remove("storyprvhint")
                            .remove("storyhint").remove("storyhint2").remove("storydualhint")
                            .remove("storysvddualhint").remove("stories_camera").remove("dualcam")
                            .remove("dualmatrix").remove("dual_available").remove("archivehint")
                            .remove("askNotificationsAfter").remove("askNotificationsDuration")
                            .remove("viewoncehint").remove("taptostorysoundhint").remove("nothanos")
                            .remove("voiceoncehint").remove("savedhint").remove("savedsearchhint")
                            .remove("savedsearchtaghint").remove("groupEmojiPackHintShown")
                            .remove("newppsms").remove("monetizationadshint")
                    }

                    MessagesController.getEmojiSettings(UserConfig.selectedAccount).edit {
                        remove("featured_hidden").remove("emoji_featured_hidden")
                    }

                    SharedConfig.textSelectionHintShows = 0
                    SharedConfig.lockRecordAudioVideoHint = 0
                    SharedConfig.stickersReorderingHintUsed = false
                    SharedConfig.forwardingOptionsHintShown = false
                    SharedConfig.replyingOptionsHintShown = false
                    SharedConfig.messageSeenHintCount = 3
                    SharedConfig.emojiInteractionsHintCount = 3
                    SharedConfig.dayNightThemeSwitchHintCount = 3
                    SharedConfig.fastScrollHintCount = 3
                    SharedConfig.stealthModeSendMessageConfirm = 2
                    SharedConfig.updateStealthModeSendMessageConfirm(2)
                    SharedConfig.setStoriesReactionsLongPressHintUsed(false)
                    SharedConfig.setStoriesIntroShown(false)
                    SharedConfig.setMultipleReactionsPromoShowed(false)

                    ChatThemeController.getInstance(UserConfig.selectedAccount).clearCache()

                    bf.notificationCenter.postNotificationName(NotificationCenter.newSuggestionsAvailable)

                    RestrictedLanguagesSelectActivity.cleanup()

                    PersistColorPalette.getInstance(UserConfig.selectedAccount).cleanup()

                    val prefs: SharedPreferences = bf.messagesController.mainSettings
                    prefs.edit {
                        remove("peerColors")
                        remove("profilePeerColors")
                        remove("boostingappearance")
                        remove("bizbothint")
                        for (key in prefs.all.keys) {
                            if (key.contains("show_gift_for_") || key.contains("bdayhint_") || key.contains("bdayanim_")) {
                                remove(key)
                            }
                        }
                    }
                    AppRestartHelper.createDebugSuccessBulletin(bf)
                }
            }
            textIcon {
                title = getString(R.string.DebugMenuReadAllDialogs)

                listener = TGKitTextIconRow.TGTIListener {
                    bf.messagesStorage.readAllDialogs(-1)
                    AppRestartHelper.createDebugSuccessBulletin(bf)
                }
            }
        }
        category("Contacts") {
            textIcon {
                title = getString(R.string.DebugMenuImportContacts)

                listener = TGKitTextIconRow.TGTIListener {
                    bf.userConfig.syncContacts = true
                    bf.userConfig.saveConfig(false)
                    bf.contactsController.forceImportContacts()
                    AppRestartHelper.createDebugSuccessBulletin(bf)
                }
            }
            textIcon {
                title = getString(R.string.DebugMenuReloadContacts)

                listener = TGKitTextIconRow.TGTIListener {
                    bf.contactsController.loadContacts(false, 0)
                    AppRestartHelper.createDebugSuccessBulletin(bf)
                }
            }
            textIcon {
                title = getString(R.string.DebugMenuResetContacts)

                listener = TGKitTextIconRow.TGTIListener {
                    bf.contactsController.resetImportedContacts()
                    AppRestartHelper.createDebugSuccessBulletin(bf)
                }
            }
            hint("* Cherrygram's feature.")
        }

        FirebaseAnalyticsHelper.trackEventWithEmptyBundle("debug_preferences_screen")
    }
}