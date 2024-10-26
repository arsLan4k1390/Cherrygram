package uz.unnarsx.cherrygram.misc

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.telegram.messenger.*
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.browser.Browser
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.TLObject
import org.telegram.tgnet.TLRPC
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.BaseFragment
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper
import kotlin.system.exitProcess

object CherrygramExtras : CoroutineScope by MainScope() {

    fun pause(seconds: Double) {
        try {
            Thread.sleep((seconds * 1000).toLong())
        } catch (ignored: InterruptedException) { }
    }

    private val channelUsername = Constants.CG_CHANNEL_USERNAME
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

            builder.setTitle(getString(R.string.CG_FollowChannelTitle))
            builder.setMessage(getString(R.string.CG_FollowChannelInfo))

            builder.setPositiveButton(getString(R.string.ProfileJoinChannel)) { _, _ ->
                messagesCollector.addUserToChat(channel.id, userConfig.currentUser, 0, null, null, null)
                Browser.openUrl(ctx, "https://t.me/$channelUsername")
            }

//            builder.setNegativeButton(getString(R.string.Cancel), null)

            builder.setNeutralButton(getString(R.string.CG_DoNotRemindAgain)) { _, _ ->
                MessagesController.getMainSettings(currentAccount).edit().putBoolean("update_channel_follow_skip", true).apply()
            }

            try {
                builder.show()
            } catch (ignored: Exception) {}

        }

    }

    fun requestReviewFlow(fragment: BaseFragment, context: Context, activity: Activity) {
        val reviewManager = ReviewManagerFactory.create(activity)

        val requestReviewFlow = reviewManager.requestReviewFlow()
        requestReviewFlow.addOnCompleteListener { request ->
            if (!MessagesController.getMainSettings(fragment.currentAccount).getBoolean("is_cherrygram_rated", false)) {
                if (request.isSuccessful) {
                    val reviewInfo = request.result

                    val flow = reviewManager.launchReviewFlow(activity, reviewInfo)
                    flow.addOnCompleteListener {
                        AppRestartHelper.createDebugSuccessBulletin(fragment)
                        MessagesController.getMainSettings(fragment.currentAccount).edit().putBoolean("is_cherrygram_rated", true).apply()
                    }
                } else {
                    reviewInGooglePlay(context)
                }
            } else {
                reviewInGooglePlay(context)
            }

        }
    }

    private fun reviewInGooglePlay(context: Context) {
        Browser.openUrl(context, "market://details?id=${context.packageName}")
    }

}