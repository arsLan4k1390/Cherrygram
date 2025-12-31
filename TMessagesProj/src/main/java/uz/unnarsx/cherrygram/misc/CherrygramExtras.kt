/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.misc

import android.app.Activity
import android.content.Context
import android.graphics.Color
import androidx.core.graphics.ColorUtils
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.telegram.messenger.*
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.browser.Browser
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.TLObject
import org.telegram.tgnet.TLRPC
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.CallLogActivity
import org.telegram.ui.ChatActivity
import org.telegram.ui.Components.MediaActivity
import org.telegram.ui.ContactsActivity
import org.telegram.ui.GroupCreateActivity
import org.telegram.ui.ProfileActivity
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper
import uz.unnarsx.cherrygram.preferences.ExperimentalPreferencesEntry
import uz.unnarsx.cherrygram.preferences.tgkit.TGKitSettingsFragment
import androidx.core.content.edit

object CherrygramExtras : CoroutineScope by MainScope() {

    fun pause(seconds: Double) {
        try {
            Thread.sleep((seconds * 1000).toLong())
        } catch (ignored: InterruptedException) { }
    }

    private val channelUsername = Constants.CG_CHANNEL_USERNAME
    fun checkChannelFollow(activity: Activity, currentAccount: Int) = AndroidUtilities.runOnUIThread {

        if (MessagesController.getMainSettings(currentAccount).getBoolean("update_channel_follow_skip", false)) return@runOnUIThread

        val messagesCollector = MessagesController.getInstance(currentAccount)
        val connectionsManager = ConnectionsManager.getInstance(currentAccount)
        val messagesStorage = MessagesStorage.getInstance(currentAccount)
        val updateChannel = messagesCollector.getUserOrChat(channelUsername)

        if (updateChannel is TLRPC.Chat) {
            launch(Dispatchers.IO) {
                if (updateChannel.id != Constants.Cherrygram_Channel) {
                    KotlinFragmentsManager.nfweioufwehr117()
                }
                if (updateChannel.id == 1323680752L) {
                    KotlinFragmentsManager.nfweioufwehr117()
                }
            }
            checkChannelFollow(activity, currentAccount, updateChannel)
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
                            KotlinFragmentsManager.nfweioufwehr117()
                        }
                        if (chat.id == 1323680752L) {
                            KotlinFragmentsManager.nfweioufwehr117()
                        }
                    }
                    checkChannelFollow(activity, currentAccount, chat)
                }
            }
        }

    }

    private fun checkChannelFollow(activity: Activity, currentAccount: Int, channel: TLRPC.Chat) {

        launch(Dispatchers.IO) {
            if (channel.id != Constants.Cherrygram_Channel) {
                KotlinFragmentsManager.nfweioufwehr117()
            }
            if (channel.id == 1323680752L) {
                KotlinFragmentsManager.nfweioufwehr117()
            }
        }

        if (!channel.left || channel.kicked) {
//            MessagesController.getMainSettings(currentAccount).edit().putBoolean("update_channel_follow_skip", true).apply()
            return
        }

        AndroidUtilities.runOnUIThread {

            val messagesController = MessagesController.getInstance(currentAccount)
            val userConfig = UserConfig.getInstance(currentAccount)

            val builder = AlertDialog.Builder(activity)

            builder.setTitle(getString(R.string.CG_FollowChannelTitle))
            builder.setMessage(getString(R.string.CG_FollowChannelInfo))

            builder.setPositiveButton(getString(R.string.ProfileJoinChannel)) { _, _ ->
                messagesController.addUserToChat(channel.id, userConfig.currentUser, 0, null, null, null)
                Browser.openUrl(activity, "https://t.me/$channelUsername")
            }

//            builder.setNegativeButton(getString(R.string.Cancel), null)

            builder.setNeutralButton(getString(R.string.CG_DoNotRemindAgain)) { _, _ ->
                messagesController.mainSettings.edit {
                    putBoolean("update_channel_follow_skip", true)
                }
            }

            try {
                builder.show()
            } catch (ignored: Exception) {}

        }

    }

    fun requestReviewFlow(fragment: BaseFragment) {
        val reviewManager = ReviewManagerFactory.create(fragment.parentActivity)

        val requestReviewFlow = reviewManager.requestReviewFlow()
        requestReviewFlow.addOnCompleteListener { request ->
            if (!fragment.messagesController.mainSettings.getBoolean("is_cherrygram_rated", false)) {
                if (request.isSuccessful) {
                    val reviewInfo = request.result

                    val flow = reviewManager.launchReviewFlow(fragment.parentActivity, reviewInfo)
                    flow.addOnCompleteListener {
                        AppRestartHelper.createDebugSuccessBulletin(fragment)
                        fragment.messagesController.mainSettings.edit {
                            putBoolean("is_cherrygram_rated", true)
                        }
                    }
                } else {
                    reviewInGooglePlay(fragment.context)
                }
            } else {
                reviewInGooglePlay(fragment.context)
            }
        }
    }

    private fun reviewInGooglePlay(context: Context) {
        Browser.openUrl(context, "market://details?id=${context.packageName}")
    }

    fun needToAnimateFragment(fragment: BaseFragment) : Boolean {
        return fragment is CallLogActivity
                || fragment is ContactsActivity
                || fragment is ChatActivity
                || fragment is GroupCreateActivity
                || fragment is MediaActivity
                || fragment is ProfileActivity

                || fragment is TGKitSettingsFragment
                || fragment is ExperimentalPreferencesEntry
    }

    fun getTransparentColor(color: Int, opacity: Float): Int {
        var alpha = Color.alpha(color)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        // Set alpha based on your logic, here I'm making it 25% of it's initial value.
        alpha = (alpha * opacity).toInt()
        return Color.argb(alpha, red, green, blue)
    }

    fun isLight(color: Int): Boolean {
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        val hsl = FloatArray(3)
        ColorUtils.RGBToHSL(red, green, blue, hsl)
        return hsl[2] >= 0.5f
    }

}