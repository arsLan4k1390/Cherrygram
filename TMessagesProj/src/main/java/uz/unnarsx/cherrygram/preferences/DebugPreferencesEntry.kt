package uz.unnarsx.cherrygram.preferences

import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Build
import androidx.core.util.Pair
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ChatThemeController
import org.telegram.messenger.LocaleController
import org.telegram.messenger.MessagesController
import org.telegram.messenger.NotificationCenter
import org.telegram.messenger.R
import org.telegram.messenger.SharedConfig
import org.telegram.messenger.UserConfig
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.Components.Paint.PersistColorPalette
import org.telegram.ui.RestrictedLanguagesSelectActivity
import uz.unnarsx.cherrygram.CherrygramConfig
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper
import uz.unnarsx.cherrygram.preferences.tgkit.preference.category
import uz.unnarsx.cherrygram.preferences.tgkit.preference.contract
import uz.unnarsx.cherrygram.preferences.tgkit.preference.hint
import uz.unnarsx.cherrygram.preferences.tgkit.preference.list
import uz.unnarsx.cherrygram.preferences.tgkit.preference.switch
import uz.unnarsx.cherrygram.preferences.tgkit.preference.textIcon
import uz.unnarsx.cherrygram.preferences.tgkit.preference.tgKitScreen
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitTextIconRow

class DebugPreferencesEntry : BasePreferencesEntry {
    override fun getPreferences(bf: BaseFragment) = tgKitScreen("Debug // WIP") {
        category("Misc") {
            switch {
                isAvailable = !CherrygramConfig.isStableBuild() && !CherrygramConfig.isPlayStoreBuild()
                title = "Toast all RPC errors *"
                description = "you'll see RPC errors from Telegram's backend as toast messages."

                contract({
                    return@contract CherrygramConfig.showRPCErrors
                }) {
                    CherrygramConfig.showRPCErrors = it
                    AppRestartHelper.createRestartBulletin(bf)
                }
            }
            switch {
                title = "Default time style in chats *"
                description = "unlike IOS and TDesktop"

                contract({
                    return@contract CherrygramConfig.oldTimeStyle
                }) {
                    CherrygramConfig.oldTimeStyle = it
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
                    ) { dialog: DialogInterface?, which: Int ->
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
                        LocaleController.getString("Cancel", R.string.Cancel),
                        null
                    )
                    builder.show()
                }
            }
            switch {
                title = "Bot tabs 3d effect"

                contract({
                    return@contract SharedConfig.botTabs3DEffect
                }) {
                    SharedConfig.setBotTabs3DEffect(!SharedConfig.botTabs3DEffect)
                }
            }
            switch {
                title = "Swipe inside a bot to close *"
                description = "When you swipe down (accidentally or intentionally), web-view just gets closed while you just want to scroll something in the mini-app"

                contract({
                    return@contract CherrygramConfig.swipeInsideBotToClose
                }) {
                    CherrygramConfig.swipeInsideBotToClose = it
                }
            }
        }
        category("Blur") {
            switch {
                title = "Force chat blur *"

                contract({
                    return@contract CherrygramConfig.forceChatBlurEffect
                }) {
                    CherrygramConfig.forceChatBlurEffect = it
                }
            }
            switch {
                isAvailable = Build.VERSION.SDK_INT >= 31
                title = "New blur (GPU)"

                contract({
                    return@contract SharedConfig.useNewBlur
                }) {
                    SharedConfig.toggleUseNewBlur()
                }
            }
        }
        category("Chats") {
            switch {
                isAvailable = Build.VERSION.SDK_INT >= 23
                title = "Ask passcode before open a chat"
                description = "Ask a system passcode (fingerprint, face id or pattern/password) before open a chat. To enable, tap on 3 dots inside a chat."

                contract({
                    return@contract CherrygramConfig.askForPasscodeBeforeOpenChat
                }) {
                    CherrygramConfig.askForPasscodeBeforeOpenChat = it
                }
            }
            list {
                isAvailable = Build.VERSION.SDK_INT >= 29
                title = "Microphone Audio Source *"

                contract({
                    return@contract listOf(
                        Pair(CherrygramConfig.AUDIO_SOURCE_DEFAULT, "DEFAULT"),
                        Pair(CherrygramConfig.AUDIO_SOURCE_CAMCORDER, "CAMCORDER"),
                        Pair(CherrygramConfig.AUDIO_SOURCE_MIC, "MIC"),
                        Pair(CherrygramConfig.AUDIO_SOURCE_REMOTE_SUBMIX, "REMOTE_SUBMIX"),
                        Pair(CherrygramConfig.AUDIO_SOURCE_UNPROCESSED, "UNPROCESSED"),
                        Pair(CherrygramConfig.AUDIO_SOURCE_VOICE_CALL, "VOICE_CALL"),
                        Pair(CherrygramConfig.AUDIO_SOURCE_VOICE_COMMUNICATION, "VOICE_COMMUNICATION"),
                        Pair(CherrygramConfig.AUDIO_SOURCE_VOICE_DOWNLINK, "VOICE_DOWNLINK"),
                        Pair(CherrygramConfig.AUDIO_SOURCE_VOICE_PERFORMANCE, "VOICE_PERFORMANCE"),
                        Pair(CherrygramConfig.AUDIO_SOURCE_VOICE_RECOGNITION, "VOICE_RECOGNITION"),
                        Pair(CherrygramConfig.AUDIO_SOURCE_VOICE_UPLINK, "VOICE_UPLINK")
                    )
                }, {
                    return@contract when (CherrygramConfig.audioSource) {
                        CherrygramConfig.AUDIO_SOURCE_CAMCORDER -> "CAMCORDER"
                        CherrygramConfig.AUDIO_SOURCE_MIC -> "MIC"
                        CherrygramConfig.AUDIO_SOURCE_REMOTE_SUBMIX -> "REMOTE_SUBMIX"
                        CherrygramConfig.AUDIO_SOURCE_UNPROCESSED -> "UNPROCESSED"
                        CherrygramConfig.AUDIO_SOURCE_VOICE_CALL -> "VOICE_CALL"
                        CherrygramConfig.AUDIO_SOURCE_VOICE_COMMUNICATION -> "VOICE_COMMUNICATION"
                        CherrygramConfig.AUDIO_SOURCE_VOICE_DOWNLINK -> "VOICE_DOWNLINK"
                        CherrygramConfig.AUDIO_SOURCE_VOICE_PERFORMANCE -> "VOICE_PERFORMANCE"
                        CherrygramConfig.AUDIO_SOURCE_VOICE_RECOGNITION -> "VOICE_RECOGNITION"
                        CherrygramConfig.AUDIO_SOURCE_VOICE_UPLINK -> "VOICE_UPLINK"
                        else -> "DEFAULT"
                    }
                }) {
                    CherrygramConfig.audioSource = it
                }
            }
            switch {
                title = "Send videos at max quality *"
                description = "Max quality will be automatically selected when you send a video"

                contract({
                    return@contract CherrygramConfig.sendVideosAtMaxQuality
                }) {
                    CherrygramConfig.sendVideosAtMaxQuality = it
                }
            }
            switch {
                title = "Play GIFs as Videos *"

                contract({
                    return@contract CherrygramConfig.playGIFsAsVideos
                }) {
                    CherrygramConfig.playGIFsAsVideos = it
                }
            }
            textIcon {
                title = LocaleController.getString("DebugMenuResetDialogs", R.string.DebugMenuResetDialogs)

                listener = TGKitTextIconRow.TGTIListener {
                    bf.messagesController.forceResetDialogs()
                    AppRestartHelper.createDebugSuccessBulletin(bf)
                }
            }
            textIcon {
                title = LocaleController.getString("DebugMenuClearMediaCache", R.string.DebugMenuClearMediaCache)

                listener = TGKitTextIconRow.TGTIListener {
                    bf.messagesStorage.clearSentMedia()

                    SharedConfig.setNoSoundHintShowed(false)

                    var editor = MessagesController.getGlobalMainSettings().edit()
                    editor.remove("archivehint").remove("proximityhint").remove("archivehint_l")
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
                        .remove("newppsms").remove("monetizationadshint").apply()

                    MessagesController.getEmojiSettings(UserConfig.selectedAccount).edit()
                        .remove("featured_hidden").remove("emoji_featured_hidden").apply()

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
                    editor = prefs.edit()
                    editor.remove("peerColors").remove("profilePeerColors")
                        .remove("boostingappearance").remove("bizbothint")
                    for (key in prefs.all.keys) {
                        if (key.contains("show_gift_for_") || key.contains("bdayhint_") || key.contains(
                                "bdayanim_"
                            )
                        ) {
                            editor.remove(key)
                        }
                    }
                    editor.apply()
                    AppRestartHelper.createDebugSuccessBulletin(bf)
                }
            }
            textIcon {
                title = LocaleController.getString("DebugMenuReadAllDialogs", R.string.DebugMenuReadAllDialogs)

                listener = TGKitTextIconRow.TGTIListener {
                    bf.messagesStorage.readAllDialogs(-1)
                    AppRestartHelper.createDebugSuccessBulletin(bf)
                }
            }
        }
        category("Contacts") {
            textIcon {
                title = LocaleController.getString("DebugMenuImportContacts", R.string.DebugMenuImportContacts)

                listener = TGKitTextIconRow.TGTIListener {
                    bf.userConfig.syncContacts = true
                    bf.userConfig.saveConfig(false)
                    bf.contactsController.forceImportContacts()
                    AppRestartHelper.createDebugSuccessBulletin(bf)
                }
            }
            textIcon {
                title = LocaleController.getString("DebugMenuReloadContacts", R.string.DebugMenuReloadContacts)

                listener = TGKitTextIconRow.TGTIListener {
                    bf.contactsController.loadContacts(false, 0)
                    AppRestartHelper.createDebugSuccessBulletin(bf)
                }
            }
            textIcon {
                title = LocaleController.getString("DebugMenuResetContacts", R.string.DebugMenuResetContacts)

                listener = TGKitTextIconRow.TGTIListener {
                    bf.contactsController.resetImportedContacts()
                    AppRestartHelper.createDebugSuccessBulletin(bf)
                }
            }
            hint("* Cherrygram's feature.")
        }
    }
}