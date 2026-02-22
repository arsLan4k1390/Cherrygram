/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.chats

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.BuildVars
import org.telegram.messenger.ChatObject
import org.telegram.messenger.FileLog
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.R
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.TLRPC
import org.telegram.ui.ActionBar.ActionBarMenuItem
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ActionBar.INavigationLayout
import org.telegram.ui.ActionIntroActivity
import org.telegram.ui.CameraScanActivity
import org.telegram.ui.ChannelCreateActivity
import org.telegram.ui.ChatActivity
import org.telegram.ui.Components.AlertsCreator
import org.telegram.ui.Components.ItemOptions
import org.telegram.ui.DialogsActivity
import org.telegram.ui.Gifts.GiftSheet
import org.telegram.ui.LaunchActivity
import uz.unnarsx.cherrygram.chats.helpers.ChatActivityHelper
import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramPrivacyConfig
import uz.unnarsx.cherrygram.donates.DonatesManager
import uz.unnarsx.cherrygram.misc.Constants
import kotlin.math.abs
import androidx.core.content.edit
import org.telegram.messenger.AccountInstance
import org.telegram.ui.ActionBar.ActionBarMenu
import org.telegram.ui.ActionBar.ActionBarMenuSubItem
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.CallLogActivity
import org.telegram.ui.Components.ChatActivityEnterView
import org.telegram.ui.Components.ChatAttachAlert
import org.telegram.ui.ProxyListActivity
import uz.unnarsx.cherrygram.chats.helpers.ChatsHelper2
import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig
import uz.unnarsx.cherrygram.core.ui.mainTabs.MainTabsManager

// I've created this so CG features can be injected in a source file with 1 line only (maybe)
// Because manual editing of drklo's sources harms your mental health.
object CGChatMenuInjector {

    fun injectAttachItem(
        headerItem: ActionBarMenuItem?,
        attachItem: ActionBarMenu.LazyItem?,
        chatActivityEnterView: ChatActivityEnterView?,
        chatAttachAlert: ChatAttachAlert?,
        context: Context,
        resourcesProvider: Theme.ResourcesProvider
    ) {
        if (headerItem == null) return
        if (chatActivityEnterView != null && chatActivityEnterView.hasText() && TextUtils.isEmpty(chatActivityEnterView.slowModeTimer)) {
            val attach = ActionBarMenuSubItem(context, false, true, true, resourcesProvider)
            attach.setTextAndIcon(getString(R.string.AttachMenu), R.drawable.input_attach)
            attach.setOnClickListener {
                headerItem.closeSubMenu()
                chatAttachAlert?.setEditingMessageObject(0, null)
                chatActivityEnterView.attachButton.performClick()
            }
            headerItem.setOnClickListener {
                headerItem.toggleSubMenu(attach, attachItem?.createView())
            }
        } else {
            headerItem.setOnClickListener {
                headerItem.toggleSubMenu(null, null)
            }
        }
    }

    fun injectCallShortcuts(headerItem: ActionBarMenuItem, userFull: TLRPC.UserFull?) {
        if (userFull != null && userFull.phone_calls_available) {
            headerItem.lazilyAddSubItem(
                ChatActivity.call,
                R.drawable.msg_callback,
                getString(R.string.Call)
            )
            if (userFull.video_calls_available) headerItem.lazilyAddSubItem(
                    ChatActivity.video_call,
                    R.drawable.msg_videocall,
                    getString(R.string.VideoCall)
            )
        }
    }

    fun injectCherrygramShortcuts(
        chatActivity: ChatActivity,
        headerItem: ActionBarMenuItem,
        currentChat: TLRPC.Chat?,
        currentUser: TLRPC.User?,
        secretChat: Boolean
    ) {
        val requireBiometrics = CherrygramPrivacyConfig.askBiometricsToOpenChat && !secretChat
        val isAnyButtonEnabled = requireBiometrics || CherrygramChatsConfig.shortcut_JumpToBegin
                    || CherrygramChatsConfig.shortcut_DeleteAll || CherrygramChatsConfig.shortcut_SavedMessages
                    || CherrygramChatsConfig.shortcut_Browser

        if (isAnyButtonEnabled) headerItem.lazilyAddColoredGap()

        if (requireBiometrics) {
            if (chatActivity.chatsPasswordHelper.shouldRequireBiometrics(currentUser?.id ?: 0, currentChat?.id ?: 0, 0)) {
                headerItem.lazilyAddSubItem(
                    ChatActivityHelper.OPTION_DO_NOT_ASK_PASSCODE,
                    R.drawable.msg_secret,
                    getString(R.string.SP_DoNotAskPin)
                )
            } else {
                headerItem.lazilyAddSubItem(
                    ChatActivityHelper.OPTION_ASK_PASSCODE,
                    R.drawable.msg_secret,
                    getString(R.string.SP_AskPin)
                )
            }
        }

        if (CherrygramChatsConfig.shortcut_JumpToBegin) headerItem.lazilyAddSubItem(
            ChatActivityHelper.OPTION_JUMP_TO_BEGINNING,
            R.drawable.ic_upward,
            getString(R.string.CG_JumpToBeginning)
        )

        if (currentChat != null && !isDeleteAllHidden(currentChat) && (ChatObject.isMegagroup(currentChat) || !ChatObject.isChannel(currentChat))) {
            if (CherrygramChatsConfig.shortcut_DeleteAll) headerItem.lazilyAddSubItem(
                ChatActivityHelper.OPTION_DELETE_ALL_FROM_SELF,
                R.drawable.msg_delete,
                getString(R.string.CG_DeleteAllFromSelf)
            )
        }

        if (currentChat != null && !ChatObject.isChannel(currentChat) && currentChat.creator) headerItem.lazilyAddSubItem(
            ChatActivityHelper.OPTION_UPGRADE_GROUP,
                R.drawable.ic_upward,
                getString(R.string.UpgradeGroup)
        )

        if (currentChat != null && currentChat.id != abs(ChatsHelper2.getCustomChatID())
            || currentUser != null && currentUser.id != abs(ChatsHelper2.getCustomChatID())
        ) {
            if (CherrygramChatsConfig.shortcut_SavedMessages) headerItem.lazilyAddSubItem(
                ChatActivityHelper.OPTION_GO_TO_SAVED,
                R.drawable.msg_saved,
                getString(R.string.SavedMessages)
            )
        }

        if (CherrygramChatsConfig.shortcut_Browser) headerItem.lazilyAddSubItem(
            ChatActivityHelper.OPTION_OPEN_TELEGRAM_BROWSER,
            R.drawable.msg_language,
            "Telegram Browser"
        )

    }

    fun injectAdminShortcuts(headerItem: ActionBarMenuItem, currentChat: TLRPC.Chat) {
        val isAnyButtonEnabled = CherrygramChatsConfig.admins_Reactions || CherrygramChatsConfig.admins_Permissions || CherrygramChatsConfig.admins_Administrators
                || CherrygramChatsConfig.admins_Members || CherrygramChatsConfig.admins_Statistics || CherrygramChatsConfig.admins_RecentActions

        if (isAnyButtonEnabled) headerItem.lazilyAddColoredGap()

        if (CherrygramChatsConfig.admins_Reactions && ChatObject.canChangeChatInfo(currentChat)) headerItem.lazilyAddSubItem(
                ChatActivityHelper.OPTION_FOR_ADMINS_REACTIONS,
                R.drawable.msg_reactions2,
                getString(R.string.Reactions)
        )

        if (CherrygramChatsConfig.admins_Permissions && !(ChatObject.isChannel(currentChat) && !currentChat.megagroup) && !currentChat.gigagroup) headerItem.lazilyAddSubItem(
                ChatActivityHelper.OPTION_FOR_ADMINS_PERMISSIONS,
                R.drawable.msg_permissions,
                getString(R.string.ChannelPermissions)
        )

        if (CherrygramChatsConfig.admins_Administrators) headerItem.lazilyAddSubItem(
                ChatActivityHelper.OPTION_FOR_ADMINS_ADMINISTRATORS,
                R.drawable.msg_admins,
                getString(R.string.ChannelAdministrators)
        )

        if (CherrygramChatsConfig.admins_Members) headerItem.lazilyAddSubItem(
                ChatActivityHelper.OPTION_FOR_ADMINS_MEMBERS,
                R.drawable.msg_groups,
                getString(R.string.ChannelMembers)
        )

        if (CherrygramChatsConfig.admins_Permissions && (ChatObject.isChannel(currentChat) && !currentChat.megagroup || currentChat.gigagroup)) headerItem.lazilyAddSubItem(
                ChatActivityHelper.OPTION_FOR_ADMINS_PERMISSIONS,
                R.drawable.msg_user_remove,
                getString(R.string.ChannelBlacklist)
        )

        if (CherrygramChatsConfig.admins_Statistics && ChatObject.isBoostSupported(currentChat)) headerItem.lazilyAddSubItem(
                ChatActivityHelper.OPTION_FOR_ADMINS_STATISTICS,
                R.drawable.msg_stats,
                getString(R.string.StatisticsAndBoosts)
        )

        if (CherrygramChatsConfig.admins_RecentActions) headerItem.lazilyAddSubItem(
                ChatActivityHelper.OPTION_FOR_ADMINS_RECENT_ACTIONS,
                R.drawable.msg_log,
                getString(R.string.EventLog)
        )

    }

    private fun isDeleteAllHidden(chat: TLRPC.Chat): Boolean {
        return Constants.Cherrygram_Support == chat.id
    }

    /** Dialogs Activity start */
    fun injectCreateChannel(io: ItemOptions, fragment: BaseFragment) {
        io.add(
            R.drawable.msg_channel,
            getString(R.string.NewChannel)
        ) {
            val preferences = fragment.messagesController.mainSettings

            if (!BuildVars.DEBUG_VERSION && preferences.getBoolean("channel_intro", false)) {
                val args = Bundle().apply {
                    putInt("step", 0)
                }
                fragment.presentFragment(ChannelCreateActivity(args))
            } else {
                fragment.presentFragment(ActionIntroActivity(ActionIntroActivity.ACTION_TYPE_CHANNEL_CREATE))
                preferences.edit { putBoolean("channel_intro", true) }
            }
        }
    }

    fun injectArchived(io: ItemOptions, fragment: BaseFragment) {
        io.addIf(
            !CherrygramPrivacyConfig.hideArchiveFromChatsList && (CherrygramAppearanceConfig.tabsHideAllChats || !MainTabsManager.hasTab(MainTabsManager.TabType.SETTINGS) || !CherrygramAppearanceConfig.showMainTabs),
            R.drawable.msg_archive,
            getString(R.string.ArchivedChats)
        ) {
            openArchivedChats(fragment)
        }
    }

    fun injectSaved(io: ItemOptions, fragment: BaseFragment) {
        io.addIf(
            !MainTabsManager.hasTab(MainTabsManager.TabType.SETTINGS) || !CherrygramAppearanceConfig.showMainTabs,
            R.drawable.msg_saved,
            getString(R.string.SavedMessages)
        ) {
            fragment.presentFragment(ChatActivity.of(ChatsHelper2.getCustomChatID()))
        }
    }

    fun injectCalls(io: ItemOptions, fragment: BaseFragment) {
        io.addIf(
            !CherrygramAppearanceConfig.showMainTabs || !MainTabsManager.hasTab(MainTabsManager.TabType.CALLS),
            R.drawable.msg_calls,
            getString(R.string.Calls)
        ) {
            val args = Bundle().apply {
                putBoolean("needFinishFragment", false)
                putBoolean("hasMainTabs", false)
            }
            fragment.presentFragment(CallLogActivity(args))
        }
    }

    fun injectGifts(io: ItemOptions, currentAccount: Int, context: Context) {
        val available: Boolean = CherrygramAppearanceConfig.marketPlaceDrawerButton && DonatesManager.checkAllDonatedAccountsForMarketplace()

        io.addGapIf(available)

        io.addIf(
            available,
            R.drawable.menu_gift,
            getString(R.string.Gift2TitleSelf1)
        ) {
            AndroidUtilities.runOnUIThread {
                val alert = GiftSheet(context, currentAccount, UserConfig.getInstance(currentAccount).clientUserId, null, null)
                alert.show()
            }
        }
    }

    fun injectScanQR(io: ItemOptions, fragment: BaseFragment) {
        io.add(
            R.drawable.msg_qrcode,
            getString(R.string.AuthAnotherClient)
        ) {
            val activity = fragment.parentActivity

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    ActionIntroActivity.CAMERA_PERMISSION_REQUEST_CODE
                )
                return@add
            }

            if (activity is LaunchActivity) {
                openCameraScanActivity(fragment, activity.actionBarLayout)

                if (AndroidUtilities.isTablet()) {
                    activity.actionBarLayout.rebuildFragments(INavigationLayout.REBUILD_FLAG_REBUILD_LAST)
                    activity.rightActionBarLayout.rebuildFragments(INavigationLayout.REBUILD_FLAG_REBUILD_LAST)
                }
            }
        }
    }

    fun injectProxySettings(io: ItemOptions, fragment: BaseFragment) {
        var available = false

        for (i in 0 until UserConfig.MAX_ACCOUNT_COUNT) {
            val userConfig = AccountInstance.getInstance(i).userConfig
            val phone = userConfig?.currentUser?.phone ?: continue

            if (
                phone.startsWith("7") || // RU, KZ
                phone.startsWith("98") || // Iran
                phone.startsWith("964") // Iraq
            ) {
                available = true
                break
            }
        }

        io.addGapIf(available)
        io.addIf(
            available,
            R.drawable.shield_network_filled_solar,
            getString(R.string.ProxySettings)
        ) {
            fragment.presentFragment(ProxyListActivity())
        }
    }

    private fun openCameraScanActivity(fragment: BaseFragment, actionBarLayout: INavigationLayout) {
        CameraScanActivity.showAsSheet(fragment, false, CameraScanActivity.TYPE_QR_LOGIN, object : CameraScanActivity.CameraScanActivityDelegate {
            override fun processQr(link: String, onLoadEnd: Runnable): Boolean {
                AndroidUtilities.runOnUIThread({
                    try {
                        val code = link.removePrefix("tg://login?token=")
                            .replace("/", "_")
                            .replace("+", "-")

                        val token = Base64.decode(code, Base64.URL_SAFE)

                        val req = TLRPC.TL_auth_acceptLoginToken().apply {
                            this.token = token
                        }

                        ConnectionsManager.getInstance(UserConfig.selectedAccount)
                            .sendRequest(req) { _, _ ->
                                AndroidUtilities.runOnUIThread(onLoadEnd)
                            }
                    } catch (e: Exception) {
                        FileLog.e("Failed to pass qr code auth", e)

                        val fragmentStack = actionBarLayout.fragmentStack
                        if (fragmentStack.isNotEmpty()) {
                            val fragment = fragmentStack[0]
                            AndroidUtilities.runOnUIThread {
                                AlertsCreator.showSimpleAlert(
                                    fragment,
                                    getString(R.string.AuthAnotherClient),
                                    getString(R.string.ErrorOccurred)
                                )
                            }
                        }
                        onLoadEnd.run()
                    }
                }, 750)
                return true
            }
        })
    }

    fun openArchivedChats(fragment: BaseFragment) {
        val args = Bundle().apply {
            putInt("folderId", 1)
        }
        fragment.presentFragment(DialogsActivity(args))
    }
    /** Dialogs Activity finish */

}