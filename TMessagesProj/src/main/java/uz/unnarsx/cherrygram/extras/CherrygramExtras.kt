package uz.unnarsx.cherrygram.extras

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.URLSpan
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.telegram.messenger.*
import org.telegram.messenger.browser.Browser
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.TLObject
import org.telegram.tgnet.TLRPC
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.Components.URLSpanNoUnderline
import uz.unnarsx.cherrygram.CherrygramConfig
import java.util.*
import kotlin.system.exitProcess

object CherrygramExtras : CoroutineScope by MainScope() {

    fun getDCGeo(dcId: Int): String {
        return when (dcId) {
            1, 3 -> "USA (Miami)"
            2, 4 -> "NLD (Amsterdam)"
            5 -> "SGP (Singapore)"
            else -> "UNK (Unknown)"
        }
    }

    fun getDCName(dc: Int): String {
        return when (dc) {
            1 -> "Pluto"
            2 -> "Venus"
            3 -> "Aurora"
            4 -> "Vesta"
            5 -> "Flora"
            else -> LocaleController.getString("NumberUnknown", R.string.NumberUnknown)
        }
    }

    @Suppress("DEPRECATION")
    fun getAbiCode(): String {
        var abi = ""
        try {
            when (ApplicationLoader.applicationContext.packageManager.getPackageInfo(ApplicationLoader.applicationContext.packageName, 0).versionCode % 10) {
                1, 3 -> abi = "armeabi-v7a"
                2, 4 -> abi = "x86"
                5, 7 -> abi = "arm64-v8a"
                6, 8 -> abi = "x86_64"
                0, 9 -> abi = "universal"
            }
        } catch (e: java.lang.Exception) {
            FileLog.e(e)
        }
        return abi
    }

    fun createDateAndTime(date: Long): String {
        var dateAndTime = date
        try {
            dateAndTime *= 1000
            val rightNow = Calendar.getInstance()
            rightNow.timeInMillis = dateAndTime
            return String.format("%1\$s | %2\$s", LocaleController.getInstance().formatterYear.format(Date(dateAndTime)),
                LocaleController.getInstance().formatterDay.format(Date(dateAndTime))
            )
        } catch (ignore: Exception) { }
        return "LOC_ERR"
    }

    fun createDateAndTimeForJSON(date: Long): String {
        var dateAndTime = date
        try {
            dateAndTime *= 1000
            val rightNow = Calendar.getInstance()
            rightNow.timeInMillis = dateAndTime
            return String.format("%1\$s | %2\$s", LocaleController.getInstance().formatterYear.format(Date(dateAndTime)),
                LocaleController.getInstance().formatterDayWithSeconds.format(Date(dateAndTime))
            )
        } catch (ignore: Exception) { }
        return "LOC_ERR"
    }

    fun getUrlNoUnderlineText(charSequence: CharSequence): CharSequence {
        val spannable: Spannable = SpannableString(charSequence)
        val spans = spannable.getSpans(0, charSequence.length, URLSpan::class.java)
        for (urlSpan in spans) {
            var span = urlSpan
            val start = spannable.getSpanStart(span)
            val end = spannable.getSpanEnd(span)
            spannable.removeSpan(span)
            span = object : URLSpanNoUnderline(span.url) {
            }
            spannable.setSpan(span, start, end, 0)
        }
        return spannable
    }

    private const val channelUsername = "Cherry_gram"
    @JvmStatic
    fun postCheckFollowChannel(ctx: Context, currentAccount: Int) = AndroidUtilities.runOnUIThread {

        if (MessagesController.getMainSettings(currentAccount).getBoolean("update_channel_follow_skip", false)) return@runOnUIThread

        val messagesCollector = MessagesController.getInstance(currentAccount)
        val connectionsManager = ConnectionsManager.getInstance(currentAccount)
        val messagesStorage = MessagesStorage.getInstance(currentAccount)
        val updateChannel = messagesCollector.getUserOrChat(channelUsername)

        if (updateChannel is TLRPC.Chat) {
            launch(Dispatchers.IO) {
                if (updateChannel.id != Constants.Cherrygram_Channel) {
                    AndroidUtilities.runOnUIThread {
                        Toast.makeText(ApplicationLoader.applicationContext, "Моднииий хуесос", Toast.LENGTH_LONG).show()
                        Toast.makeText(ApplicationLoader.applicationContext, "Старайся лучше!", Toast.LENGTH_LONG).show()
                    }
                    delay(1500)
                    exitProcess(0)
                }
            }
            checkFollowChannel(ctx, currentAccount, updateChannel)
        } else {
            connectionsManager.sendRequest(TLRPC.TL_contacts_resolveUsername().apply {
                username = channelUsername
            }) { response: TLObject?, error: TLRPC.TL_error? ->
                if (error == null) {
                    val res = response as TLRPC.TL_contacts_resolvedPeer
                    val chat = res.chats.find { it.username == channelUsername } ?: return@sendRequest
                    messagesCollector.putChats(res.chats, false)
                    messagesStorage.putUsersAndChats(res.users, res.chats, false, true)
                    launch(Dispatchers.IO) {
                        if (chat.id != Constants.Cherrygram_Channel) {
                            AndroidUtilities.runOnUIThread {
                                Toast.makeText(ApplicationLoader.applicationContext, "Моднииий хуесос", Toast.LENGTH_LONG).show()
                                Toast.makeText(ApplicationLoader.applicationContext, "Старайся лучше!", Toast.LENGTH_LONG).show()
                            }
                            delay(1500)
                            exitProcess(0)
                        }
                    }
                    checkFollowChannel(ctx, currentAccount, chat)
                }
            }
        }

    }

    private fun checkFollowChannel(ctx: Context, currentAccount: Int, channel: TLRPC.Chat) {

        launch(Dispatchers.IO) {
            if (channel.id != Constants.Cherrygram_Channel) {
                AndroidUtilities.runOnUIThread {
                    Toast.makeText(ApplicationLoader.applicationContext, "Моднииий хуесос", Toast.LENGTH_LONG).show()
                    Toast.makeText(ApplicationLoader.applicationContext, "Старайся лучше!", Toast.LENGTH_LONG).show()
                }
                delay(1500)
                exitProcess(0)
            }
        }

        if (!channel.left || channel.kicked) {
//            MessagesController.getMainSettings(currentAccount).edit().putBoolean("update_channel_follow_skip", true).apply()
            return
        }

        AndroidUtilities.runOnUIThread {

            val messagesCollector = MessagesController.getInstance(currentAccount)
            val userConfig = UserConfig.getInstance(currentAccount)

            val builder = AlertDialog.Builder(ctx)

            builder.setTitle(LocaleController.getString("CG_FollowChannelTitle", R.string.CG_FollowChannelTitle))
            builder.setMessage(LocaleController.getString("CG_FollowChannelInfo", R.string.CG_FollowChannelInfo))

            builder.setPositiveButton(LocaleController.getString("ProfileJoinChannel", R.string.ProfileJoinChannel)) { _, _ ->
                messagesCollector.addUserToChat(channel.id, userConfig.currentUser, 0, null, null, null)
                Browser.openUrl(ctx, "https://t.me/$channelUsername")
            }

//            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null)

            builder.setNeutralButton(LocaleController.getString("CG_DoNotRemindAgain", R.string.CG_DoNotRemindAgain)) { _, _ ->
                MessagesController.getMainSettings(currentAccount).edit().putBoolean("update_channel_follow_skip", true).apply()
            }

            try {
                builder.show()
            } catch (ignored: Exception) {}

        }

    }

    @JvmStatic
    fun checkCustomChatID(currentAccount: Int) {
        val preferences = MessagesController.getMainSettings(currentAccount)
        val empty = preferences.getString("CP_CustomChatIDSM", "CP_CustomChatIDSM").equals("")
        if (empty) {
            CherrygramConfig.putStringForUserPrefs("CP_CustomChatIDSM",
                UserConfig.getInstance(currentAccount).getClientUserId().toString()
            )
        }
    }

    @JvmStatic
    fun getCustomChatID(): Long {
        val id: Long
        val preferences = MessagesController.getMainSettings(UserConfig.selectedAccount)
        val savedMessagesChatID =
            preferences.getString("CP_CustomChatIDSM", UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId().toString())
        val chatID = savedMessagesChatID!!.replace("-100", "-").toLong()

        id = if (CherrygramConfig.customChatForSavedMessages) {
            chatID
        } else {
            UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId()
        }
        return id
    }

    @JvmStatic
    fun getSearchFilterType(): TLRPC.MessagesFilter {
        val filter: TLRPC.MessagesFilter = when (CherrygramConfig.messagesSearchFilter) {
            CherrygramConfig.FILTER_PHOTOS -> {
                TLRPC.TL_inputMessagesFilterPhotos()
            }
            CherrygramConfig.FILTER_VIDEOS -> {
                TLRPC.TL_inputMessagesFilterVideo()
            }
            CherrygramConfig.FILTER_VOICE_MESSAGES -> {
                TLRPC.TL_inputMessagesFilterVoice()
            }
            CherrygramConfig.FILTER_VIDEO_MESSAGES -> {
                TLRPC.TL_inputMessagesFilterRoundVideo()
            }
            CherrygramConfig.FILTER_FILES -> {
                TLRPC.TL_inputMessagesFilterDocument()
            }
            CherrygramConfig.FILTER_MUSIC -> {
                TLRPC.TL_inputMessagesFilterMusic()
            }
            CherrygramConfig.FILTER_GIFS -> {
                TLRPC.TL_inputMessagesFilterGif()
            }
            CherrygramConfig.FILTER_GEO -> {
                TLRPC.TL_inputMessagesFilterGeo()
            }
            CherrygramConfig.FILTER_CONTACTS -> {
                TLRPC.TL_inputMessagesFilterContacts()
            }
            CherrygramConfig.FILTER_MENTIONS -> {
                TLRPC.TL_inputMessagesFilterMyMentions()
            }
            else -> {
                TLRPC.TL_inputMessagesFilterEmpty()
            }
        }
        return filter
    }

}