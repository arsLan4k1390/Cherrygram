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
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.WindowInsets
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.core.content.edit
import androidx.core.graphics.ColorUtils
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.KotlinFragmentsManager
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.MessagesController
import org.telegram.messenger.MessagesStorage
import org.telegram.messenger.R
import org.telegram.messenger.UserConfig
import org.telegram.messenger.browser.Browser
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.TLObject
import org.telegram.tgnet.TLRPC
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.LaunchActivity
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig
import uz.unnarsx.cherrygram.core.ui.CGBulletinCreator
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object CherrygramExtras : CoroutineScope by MainScope() {

    fun pause(seconds: Double) {
        try {
            Thread.sleep((seconds * 1000).toLong())
        } catch (_: InterruptedException) { }
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

    private suspend fun getChat(fragment: BaseFragment): TLRPC.Chat = withContext(Dispatchers.IO) {
        val cached = fragment.messagesController.getUserOrChat(channelUsername)
        if (cached is TLRPC.Chat) return@withContext cached

        suspendCancellableCoroutine { cont ->
            fragment.connectionsManager.sendRequest(
                TLRPC.TL_contacts_resolveUsername().apply { username = channelUsername }
            ) { response, error ->

                if (error != null || response !is TLRPC.TL_contacts_resolvedPeer) {
                    cont.resumeWithException(Exception("Failed to resolve channel: $channelUsername"))
                    return@sendRequest
                }

                val chat = response.chats.firstOrNull { it.username == channelUsername }

                if (chat != null) {
                    fragment.messagesController.putChats(response.chats, false)
                    fragment.messagesStorage.putUsersAndChats(response.users, response.chats, false, true)
                    cont.resume(chat)
                } else {
                    cont.resumeWithException(Exception("Channel not found: $channelUsername"))
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getChatJava(fragment: BaseFragment): CompletableFuture<TLRPC.Chat> {
        val future = CompletableFuture<TLRPC.Chat>()
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        scope.launch {
            try {
                val chat = getChat(fragment)
                future.complete(chat)
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }

        return future
    }

    fun shouldCheckFollow(fragment: BaseFragment): Boolean {
        val lastShownEpoch = fragment.messagesController.mainSettings.getLong("last_follow_suggestion", 0)
        val today = LocalDate.now()

        if (lastShownEpoch == 0L) return true

        val lastShownDate = Instant.ofEpochMilli(lastShownEpoch)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val timeout = ChronoUnit.WEEKS.between(lastShownDate, today) >= 30

        return timeout
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
            } catch (_: Exception) {}

        }

    }

    fun requestReviewFlow(fragment: BaseFragment?) {
        val activity = fragment?.parentActivity ?: return

        val reviewManager = ReviewManagerFactory.create(activity)
        val requestTask = reviewManager.requestReviewFlow()

        requestTask.addOnCompleteListener { request ->
            if (fragment.parentActivity == null) return@addOnCompleteListener

            if (!fragment.messagesController.mainSettings.getBoolean("is_cherrygram_rated", false)) {
                if (request.isSuccessful) {
                    val reviewInfo = request.result

                    val flow = reviewManager.launchReviewFlow(fragment.parentActivity, reviewInfo)
                    flow.addOnCompleteListener {
                        CGBulletinCreator.createDebugSuccessBulletin(fragment)
                        fragment.messagesController.mainSettings.edit {
                            putBoolean("is_cherrygram_rated", true)
                        }
                    }
                } else {
                    reviewInGooglePlay(fragment)
                }
            } else {
                reviewInGooglePlay(fragment)
            }
        }
    }

    private fun reviewInGooglePlay(fragment: BaseFragment) {
        val context = fragment.context ?: return
        Browser.openUrl(context, "market://details?id=${context.packageName}")
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

    fun isWhiteOrNearWhite(@ColorInt color: Int): Boolean {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)

        if (r < 240 || g < 240 || b < 240) {
            return false
        }

        val hsl = FloatArray(3)
        ColorUtils.RGBToHSL(r, g, b, hsl)

        val saturation = hsl[1]
        val lightness = hsl[2]

        return saturation <= 0.1f && lightness >= 0.9f
    }

    private var isEdgeToEdgeCached: Boolean? = null

    @JvmStatic
    fun isEdgeToEdgeSupported(): Boolean {
        if (CherrygramCoreConfig.edgeToEdgeMode == CherrygramCoreConfig.EDGE_MODE_ENABLE) {
            return true
        } else {
            isEdgeToEdgeCached?.let { return it }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || CherrygramCoreConfig.edgeToEdgeMode == CherrygramCoreConfig.EDGE_MODE_DISABLE) {
                isEdgeToEdgeCached = false
                return false
            }

            val activity = LaunchActivity.getSafeLastFragment()?.parentActivity ?: return false

            val supported = isGestureNavigation(activity) || isGestureNavigationFallback(activity.window?.decorView)

            isEdgeToEdgeCached = supported

            return supported
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun isGestureNavigation(context: Context): Boolean {
        return try {
            val mode = Settings.Secure.getInt(
                context.contentResolver,
                "navigation_mode"
            )
            mode == 2 // GESTURES (pill)
        } catch (_: Exception) {
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun isGestureNavigationFallback(view: View?): Boolean {
        val insets = view?.rootWindowInsets ?: return false
        val navInsets = insets.getInsets(WindowInsets.Type.navigationBars())

        return navInsets.bottom == 0
    }

}