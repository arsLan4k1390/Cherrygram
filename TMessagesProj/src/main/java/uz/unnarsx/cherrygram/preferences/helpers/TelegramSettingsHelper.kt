/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.preferences.helpers

import android.os.Build
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.edit
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.AndroidUtilities.dp
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.MessageObject
import org.telegram.messenger.MessagesController
import org.telegram.messenger.R
import org.telegram.messenger.UserConfig
import org.telegram.messenger.UserObject
import org.telegram.messenger.browser.Browser
import org.telegram.tgnet.TLRPC
import org.telegram.tgnet.TLRPC.TL_peerColorCollectible
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.ChatActivity
import org.telegram.ui.Components.BackupImageView
import org.telegram.ui.Components.ImageUpdater
import org.telegram.ui.Components.ItemOptions
import org.telegram.ui.Components.Premium.LimitReachedBottomSheet
import org.telegram.ui.Components.Premium.LimitReachedBottomSheet.TYPE_ACCOUNTS
import org.telegram.ui.Components.UItem
import org.telegram.ui.Components.UniversalRecyclerView
import org.telegram.ui.Gifts.GiftSheet
import org.telegram.ui.LoginActivity
import org.telegram.ui.LogoutActivity
import org.telegram.ui.PhotoViewer
import org.telegram.ui.SettingsActivity
import uz.unnarsx.cherrygram.chats.CGChatMenuInjector
import uz.unnarsx.cherrygram.chats.helpers.ChatsHelper2
import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramPrivacyConfig
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper
import uz.unnarsx.cherrygram.core.helpers.DeeplinkHelper
import uz.unnarsx.cherrygram.core.ui.mainTabs.MainTabsManager
import uz.unnarsx.cherrygram.donates.DonatesManager
import uz.unnarsx.cherrygram.misc.CherrygramExtras
import uz.unnarsx.cherrygram.misc.Constants
import uz.unnarsx.cherrygram.preferences.CherrygramPreferencesNavigator

class TelegramSettingsHelper(
    private var fragment: BaseFragment
) {

    private lateinit var avatarContainer: View
    private lateinit var avatarView: BackupImageView
    private lateinit var cameraButton: View
    private lateinit var imageUpdater: ImageUpdater

    private lateinit var listView: UniversalRecyclerView

    fun bindViews(
        avatarContainer: FrameLayout,
        avatarView: BackupImageView,
        cameraButton: FrameLayout,
        imageUpdater: ImageUpdater,

        listView: UniversalRecyclerView
    ) {
        this.avatarContainer = avatarContainer
        this.avatarView = avatarView
        this.cameraButton = cameraButton
        this.imageUpdater = imageUpdater

        this.listView = listView
    }

    fun showMyProfile(): Boolean {
        return !CherrygramAppearanceConfig.showMainTabs || !MainTabsManager.hasTab(MainTabsManager.TabType.PROFILE)
    }

    fun showItemOptions(button: View) {
        val o = ItemOptions.makeOptions(fragment, button)

        o.add(
            R.drawable.msg_leave,
            getString(R.string.LogOut)
        ) {
            fragment.presentFragment(LogoutActivity())
        }

        o.add(
            R.drawable.msg_retry,
            getString(R.string.CG_Restart)
        ) {
            AppRestartHelper.restartApp(fragment.context)
        }

        o.addGapIf(
            DonatesManager.checkAllDonatedAccountsForMarketplace() && fragment.messageMenuHelper.showDivider()
        )

        o.addIf(
            DonatesManager.checkAllDonatedAccountsForMarketplace(),
            R.drawable.menu_gift,
            getString(
                if (CherrygramAppearanceConfig.marketPlaceDrawerButton)
                    R.string.Gift2HideGift
                else
                    R.string.Gift2ShowGift
            )
        ) {
            CherrygramAppearanceConfig.marketPlaceDrawerButton = !CherrygramAppearanceConfig.marketPlaceDrawerButton
            listView.adapter.update(true)
        }

        o.setBlur(false)
        o.setDrawScrim(false)
        o.translate(0F, -dp(48F).toFloat())
        o.show()
    }

    /** Avatar start */
    private val provider = object : PhotoViewer.EmptyPhotoViewerProvider() {

        override fun getPlaceForPhoto(
            messageObject: MessageObject?,
            fileLocation: TLRPC.FileLocation?,
            index: Int,
            needPreview: Boolean,
            closing: Boolean
        ): PhotoViewer.PlaceProviderObject? {

            if (fileLocation == null) return null
            if (avatarContainer.scaleX > 0.96f && closing) return null

            val user = fragment.userConfig.currentUser
            val photoBig = user?.photo?.photo_big

            if (photoBig != null &&
                photoBig.local_id == fileLocation.local_id &&
                photoBig.volume_id == fileLocation.volume_id &&
                photoBig.dc_id == fileLocation.dc_id
            ) {
                val coords = IntArray(2)
                avatarView.getLocationInWindow(coords)

                val obj = PhotoViewer.PlaceProviderObject()
                obj.viewX = coords[0]
                obj.viewY = coords[1]
                obj.parentView = avatarView
                obj.imageReceiver = avatarView.imageReceiver
                obj.dialogId = fragment.userConfig.clientUserId
                obj.thumb = obj.imageReceiver.bitmapSafe ?: return null
                obj.size = -1
                obj.radius = avatarView.imageReceiver.getRoundRadius(true)
                obj.scale = avatarContainer.scaleX
                obj.canEdit = true
                obj.fadeIn = avatarContainer.scaleX > 0.96f

                return obj
            }

            return null
        }

        override fun willHidePhotoViewer() {
            avatarView.imageReceiver.setVisible(true, true)
        }

        override fun openPhotoForEdit(file: String?, thumb: String?, isVideo: Boolean) {
            imageUpdater.openPhotoForEdit(file, thumb, 0, isVideo)
        }

    }

    fun openAvatar() {
        val user = fragment.userConfig.currentUser
        val photo = user?.photo?.photo_big ?: return

        PhotoViewer.getInstance().parentActivity = fragment.parentActivity

        if (user.photo.dc_id != 0) {
            photo.dc_id = user.photo.dc_id
        }

        PhotoViewer.getInstance().setParentActivity(fragment)
        PhotoViewer.getInstance().openPhoto(photo, provider)
    }

    fun checkAvatarActions() {
        avatarContainer.setOnClickListener {
            openAvatar()
        }

        cameraButton.setOnClickListener {
            val user = fragment.userConfig.currentUser
            imageUpdater.openMenu(
                user != null && user.photo?.photo_big != null && user.photo !is TLRPC.TL_userProfilePhotoEmpty,
                { fragment.messagesController.deleteUserPhoto(null) },
                {},
                0
            )
        }
    }
    /** Avatar finish */

    /** Cherrygram channel start */
    var cachedCherryChannel: TLRPC.Chat? = null
    var isCheckingChannel = false

    fun checkChannelSubscription() {
        if (cachedCherryChannel != null || isCheckingChannel) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !CherrygramCoreConfig.isStandalonePremiumBuild()) {
            isCheckingChannel = true
            CherrygramExtras.getChatJava(fragment).thenAccept { channel ->
                AndroidUtilities.runOnUIThread {
                    cachedCherryChannel = channel
                    isCheckingChannel = false
                    listView.adapter.update(true)
                }
            }
        }
    }

    fun checkChannelSubscription(follow: Boolean) {
        fragment.messagesController.mainSettings
            .edit {
                putLong("last_follow_suggestion", System.currentTimeMillis())
            }

        isCheckingChannel = false
        cachedCherryChannel = null

        listView.adapter.update(true)

        if (follow) {
            fragment.messagesController.addUserToChat(
                Constants.Cherrygram_Channel,
                fragment.userConfig.currentUser,
                0,
                null,
                null,
                null
            )
            Browser.openUrl(fragment.context, "https://t.me/" + Constants.CG_CHANNEL_USERNAME)
        }
    }
    /** Cherrygram channel finish */

    /** Inject options start */
    fun injectChannelAdvice(items: ArrayList<UItem>) {
        if (CherrygramExtras.shouldCheckFollow(fragment) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val channel = cachedCherryChannel
            if (channel != null && (channel.left || channel.kicked)) {

                items.add(
                    SettingsActivity.SuggestionCell.Factory.of(
                        getString(R.string.CG_FollowChannelTitle),
                        getString(R.string.CG_FollowChannelInfo),
                        getString(R.string.AppUpdateRemindMeLater),
                        { checkChannelSubscription(false) },
                        getString(R.string.ProfileJoinChannel),
                        { checkChannelSubscription(true) }
                    )
                )

                items.add(UItem.asShadow(null))
            }
        }

    }

    fun injectAccounts(items: MutableList<UItem>, accountNumbers: ArrayList<Int>, user: TLRPC.User) {
        items.add(UItem.asHeader(getString(R.string.SettingsAccounts)))

        val addAccountItem = SettingsActivity.SettingCell.Factory.of(
            1392,
            0xFF1CA5ED.toInt(),
            0xFF1488E1.toInt(),
            R.drawable.filled_add_album,
            getString(R.string.AddAccount)
        )

        if (accountNumbers.size >= 1) {
            addAccountItem.`object` = Runnable {
                CherrygramAppearanceConfig.showAccounts = !CherrygramAppearanceConfig.showAccounts
            }
        }

        items.add(addAccountItem)

        for (i in accountNumbers.indices) {
            if (CherrygramAppearanceConfig.showAccounts) {
                items.add(SettingsActivity.AccountCell.Factory.of(i, accountNumbers[i]))
            }
        }

        var colorTop = 0xFF1CA5ED.toInt()
        var colorBottom = 0xFF1488E1.toInt()

        if (showMyProfile()) {
            if (user.color is TL_peerColorCollectible) {
                val p = user.color as TL_peerColorCollectible
                val dark = Theme.isCurrentThemeDark()
                val colors = if (dark && p.dark_colors != null) p.dark_colors else p.colors

                val color1 = colors[0]!! or -0x1000000
                val color2 = if (colors.size >= 2) colors[1]!! or -0x1000000 else color1
                val color3 = if (colors.size >= 3) colors[2]!! or -0x1000000 else color1

                colorTop = color1
                colorBottom = color2
            } else {
                val colorId = UserObject.getColorId(user)
                if (colorId < 7) {
                    colorTop = Theme.getColor(Theme.keys_avatar_nameInMessage[colorId])
                    colorBottom = Theme.getColor(Theme.keys_avatar_nameInMessage[colorId])
                } else {
                    val peerColors = MessagesController.getInstance(UserConfig.selectedAccount).peerColors
                    val peerColor = if (peerColors == null) null else peerColors.getColor(colorId)
                    if (peerColor != null) {
                        colorTop = peerColor.color1
                        colorBottom = peerColor.color2
                    }
                }
            }
        }

        items.add(
            SettingsActivity.SettingCell.Factory.of(
                1,
                colorTop,
                colorBottom,
                R.drawable.settings_account,
                if (showMyProfile()) getString(R.string.MyProfile) else getString(R.string.SettingsAccount),
                getString(R.string.SettingsAccountInfo)
            )
        )

        items.add(UItem.asShadow(null))
    }

    fun injectCherryItems(items: MutableList<UItem>) {
        if (!CherrygramPrivacyConfig.hideArchiveFromChatsList && fragment.messagesController.getDialogs(1).isNotEmpty()) {
            val archiveItem = SettingsActivity.SettingCell.Factory.of(
                1395,
                0xFFF45255.toInt(),
                0xFFDF3955.toInt(),
                R.drawable.cg_settings_archive_solar,
                getString(R.string.ArchivedChats)
            )
            archiveItem.`object` = "archive"
            items.add(archiveItem)
        }

        items.add(
            SettingsActivity.SettingCell.Factory.of(
                1393,
                0xFF4F85F6.toInt(),
                0xFF3568E8.toInt(),
                R.drawable.cg_settings_saved_solar,
                getString(R.string.SavedMessages)
            )
        )

        items.add(UItem.asShadow(null))

        items.add(
            SettingsActivity.SettingCell.Factory.of(
                1390,
                0xFFE54C7F.toInt(),
                0xFFA33156.toInt(),
                if (CGResourcesHelper.isAnyOfBraIconsEnabled()) R.drawable.cg_settings_bra else R.drawable.cg_settings,
                getString(R.string.CGP_AdvancedSettings)
            )
        )

        if (CherrygramAppearanceConfig.marketPlaceDrawerButton && DonatesManager.checkAllDonatedAccountsForMarketplace()) {
            val giftsItem = SettingsActivity.SettingCell.Factory.of(
                1394,
                0xFFF38B31.toInt(),
                0xFFE26314.toInt(),
                R.drawable.settings_gift,
                getString(R.string.Gift2TitleSelf1)
            )
            giftsItem.`object` = "gifts"
            items.add(giftsItem)
        }

        items.add(UItem.asShadow(null))
    }

    fun handleOnClick(item: UItem) {
        when (item.id) {
            1390 -> CherrygramPreferencesNavigator.createCherrySettings(fragment)
            1392 -> {
                var freeAccounts = 0
                var availableAccount: Int? = null
                for (a in UserConfig.MAX_ACCOUNT_COUNT - 1 downTo 0) {
                    val uc = UserConfig.getInstance(a)
                    if (!uc.isClientActivated) {
                        freeAccounts++
                        if (availableAccount == null) availableAccount = a
                    }
                }

                if (!UserConfig.hasPremiumOnAccounts()) {
                    freeAccounts -= (UserConfig.MAX_ACCOUNT_COUNT - UserConfig.MAX_ACCOUNT_DEFAULT_COUNT)
                }

                if (freeAccounts > 0 && availableAccount != null) {
                    fragment.presentFragment(LoginActivity(availableAccount))
                } else if (!UserConfig.hasPremiumOnAccounts()) {
                    fragment.showDialog(
                        LimitReachedBottomSheet(
                            fragment,
                            fragment.context,
                            TYPE_ACCOUNTS,
                            fragment.currentAccount,
                            fragment.resourceProvider
                        )
                    )
                }
            }
            1393 -> fragment.presentFragment(ChatActivity.of(ChatsHelper2.getCustomChatID()))
            1394 -> {
                AndroidUtilities.runOnUIThread {
                    val alert = GiftSheet(
                        fragment.context,
                        fragment.currentAccount,
                        fragment.userConfig.clientUserId,
                        null,
                        null
                    )
                    alert.show()
                }
            }
            1395 -> CGChatMenuInjector.openArchivedChats(fragment)
        }
    }

    fun handleOnLongClick(item: UItem): Boolean {
        when (item.id) {
            1390 -> AndroidUtilities.addToClipboard("tg://${DeeplinkHelper.DeepLinksRepo.CG_Settings}")
            12 -> AndroidUtilities.addToClipboard("tg://${DeeplinkHelper.DeepLinksRepo.CG_Stars}")
        }
        return true
    }
    /** Inject options finish */

}
