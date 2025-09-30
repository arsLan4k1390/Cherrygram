/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.chats

import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import androidx.core.graphics.ColorUtils
import org.telegram.messenger.AndroidUtilities.dp
import org.telegram.messenger.ChatObject
import org.telegram.messenger.LocaleController
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.MessageObject
import org.telegram.messenger.R
import org.telegram.messenger.UserObject
import org.telegram.tgnet.TLRPC
import org.telegram.ui.ActionBar.ActionBarMenuSubItem
import org.telegram.ui.ActionBar.ActionBarPopupWindow
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.ChatActivity
import org.telegram.ui.Components.LayoutHelper
import uz.unnarsx.cherrygram.chats.gemini.GeminiResultsBottomSheet
import uz.unnarsx.cherrygram.chats.gemini.GeminiSDKImplementation
import uz.unnarsx.cherrygram.chats.helpers.ChatActivityHelper
import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig
import uz.unnarsx.cherrygram.helpers.ui.PopupHelper
import uz.unnarsx.cherrygram.preferences.CherrygramPreferencesNavigator
import androidx.core.graphics.drawable.toDrawable

// I've created this so CG features can be injected in a source file with 1 line only (maybe)
// Because manual editing of drklo's sources harms your mental health.
object CGMessageMenuInjector {

    fun showGeminiItems(
        chatActivity: ChatActivity,
        popupLayout: ActionBarPopupWindow.ActionBarPopupWindowLayout,
        selectedObject: MessageObject,
    ) {
        val linearLayout = LinearLayout(chatActivity.parentActivity)
        linearLayout.orientation = LinearLayout.VERTICAL

        val isVoiceOrVideoMessage = selectedObject.type == MessageObject.TYPE_VOICE || selectedObject.type == MessageObject.TYPE_ROUND_VIDEO
        val isVoiceMessage = selectedObject.type == MessageObject.TYPE_VOICE
        val isPhoto = selectedObject.type == MessageObject.TYPE_PHOTO
        val showDivider = !Theme.isCurrentThemeDay() && !Theme.getActiveTheme().isMonet && !Theme.getActiveTheme().isAmoled

        val backCell = ActionBarMenuSubItem(chatActivity.parentActivity, true, false, chatActivity.resourceProvider)
        backCell.setItemHeight(46)
        backCell.setTextAndIcon(getString(R.string.Back), R.drawable.msg_arrow_back)
        backCell.textView.setPadding(
            if (LocaleController.isRTL) 0 else dp(40f),
            0,
            if (LocaleController.isRTL) dp(40f) else 0,
            0
        )
        backCell.setOnClickListener {
            popupLayout.swipeBack?.closeForeground()
        }
        linearLayout.addView(
            backCell,
            LayoutHelper.createLinear(
                if (isVoiceOrVideoMessage && !chatActivity.messageMenuHelper.allowNewMessageMenu()) dp(100f) else LayoutHelper.MATCH_PARENT,
                LayoutHelper.WRAP_CONTENT
            )
        )

        if (chatActivity.messageMenuHelper.allowNewMessageMenu()) {
            if (showDivider /* && getMessageMenuHelper().showCustomDivider() */) {
                linearLayout.addView(
                    ActionBarPopupWindow.GapView(
                        chatActivity.context,
                        ColorUtils.setAlphaComponent(chatActivity.getThemedColor(Theme.key_windowBackgroundGray), chatActivity.messageMenuHelper.getMessageMenuAlpha(true)),
                        Theme.getColor(Theme.key_windowBackgroundGrayShadow, chatActivity.resourceProvider)
                    ),
                    LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 8)
                )
            }
        } else {
            if (showDivider) {
                linearLayout.addView(
                    ActionBarPopupWindow.GapView(chatActivity.context, chatActivity.resourceProvider),
                    LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 8)
                )
            }
        }

        val sections = ArrayList<View>()

        if (!TextUtils.isEmpty(selectedObject.messageOwner.message) &&
            (chatActivity.currentChat != null && ChatObject.canSendMessages(chatActivity.currentChat) || chatActivity.currentUser != null)
        ) {
            val cell = ActionBarMenuSubItem(chatActivity.parentActivity, true, false, chatActivity.resourceProvider)
            cell.setTextAndIcon(getString(R.string.Reply), R.drawable.menu_reply)
            cell.setOnClickListener {
                chatActivity.processSelectedOption(ChatActivityHelper.OPTION_REPLY_GEMINI)
            }
            sections.add(cell)
        }

        if (!TextUtils.isEmpty(selectedObject.messageOwner.message)) {
            val cell = ActionBarMenuSubItem(chatActivity.parentActivity, true, false, chatActivity.resourceProvider)
            cell.setTextAndIcon(getString(R.string.TranslateMessage), R.drawable.msg_translate)
            cell.setOnClickListener {
                chatActivity.processSelectedOption(ChatActivityHelper.OPTION_TRANSLATE_GEMINI)
            }
            sections.add(cell)
        }

        if (!TextUtils.isEmpty(selectedObject.messageOwner.message) || isVoiceMessage) {
            val cell = ActionBarMenuSubItem(chatActivity.parentActivity, true, false, chatActivity.resourceProvider)
            cell.setTextAndIcon(getString(R.string.CP_GeminiAI_Summarize), R.drawable.magic_stick_solar)
            cell.setOnClickListener {
                GeminiResultsBottomSheet.setMessageObject(selectedObject)
                GeminiResultsBottomSheet.setCurrentChat(chatActivity.currentChat)
                if (isVoiceMessage) {
                    GeminiSDKImplementation.injectGeminiForMedia(
                        chatActivity,
                        chatActivity,
                        selectedObject,
                        false,
                        true,
                        true
                    )
                } else {
                    chatActivity.processSelectedOption(ChatActivityHelper.OPTION_SUMMARIZE_GEMINI)
                }
            }
            sections.add(cell)
        }

        if (isVoiceOrVideoMessage) {
            val cell = ActionBarMenuSubItem(chatActivity.parentActivity, true, false, chatActivity.resourceProvider)
            cell.setTextAndIcon(getString(R.string.PremiumPreviewVoiceToText), R.drawable.msg_photo_text_solar)
            cell.setOnClickListener {
                chatActivity.closeMenu()
                chatActivity.processSelectedOption(ChatActivityHelper.OPTION_TRANSCRIBE_GEMINI)
            }
            sections.add(cell)
        }

        if (isPhoto) {
            val cell = ActionBarMenuSubItem(chatActivity.parentActivity, true, false, chatActivity.resourceProvider)
            cell.setTextAndIcon(getString(R.string.AccDescrQuizExplanation), R.drawable.msg_info_solar)
            cell.setOnClickListener {
                chatActivity.closeMenu()
                GeminiResultsBottomSheet.setMessageObject(selectedObject)
                GeminiResultsBottomSheet.setCurrentChat(chatActivity.currentChat)
                GeminiSDKImplementation.injectGeminiForMedia(
                    chatActivity,
                    chatActivity,
                    selectedObject,
                    false,
                    false,
                    false
                )
            }
            sections.add(cell)
        }

        if (isPhoto) {
            val cell = ActionBarMenuSubItem(chatActivity.parentActivity, true, false, chatActivity.resourceProvider)
            cell.setTextAndIcon(getString(R.string.CP_GeminiAI_ExtractText), R.drawable.msg_edit_solar)
            cell.setOnClickListener {
                chatActivity.closeMenu()
                GeminiResultsBottomSheet.setMessageObject(selectedObject)
                GeminiResultsBottomSheet.setCurrentChat(chatActivity.currentChat)
                GeminiSDKImplementation.injectGeminiForMedia(
                    chatActivity,
                    chatActivity,
                    selectedObject,
                    true,
                    false,
                    false
                )
            }
            sections.add(cell)
        }

        if (showDivider) {
            val gap = if (chatActivity.messageMenuHelper.allowNewMessageMenu()) {
                ActionBarPopupWindow.GapView(
                    chatActivity.context,
                    ColorUtils.setAlphaComponent(chatActivity.getThemedColor(Theme.key_windowBackgroundGray), chatActivity.messageMenuHelper.getMessageMenuAlpha(true)),
                    Theme.getColor(Theme.key_windowBackgroundGrayShadow, chatActivity.resourceProvider)
                )
            } else {
                ActionBarPopupWindow.GapView(chatActivity.context, chatActivity.resourceProvider)
            }
            gap.layoutParams = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 8)
            sections.add(gap)
        }

        val settingsCell = ActionBarMenuSubItem(chatActivity.parentActivity, true, false, chatActivity.resourceProvider)
        settingsCell.setTextAndIcon(getString(R.string.Settings), R.drawable.msg_settings)
        settingsCell.setOnClickListener {
            chatActivity.closeMenu()
            CherrygramPreferencesNavigator.createGemini(chatActivity)
        }
        sections.add(settingsCell)

        for (section in sections) {
            if (section.layoutParams != null) {
                linearLayout.addView(section)
            } else {
                linearLayout.addView(section, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT))
            }
        }

        val foregroundIndex = popupLayout.addViewToSwipeBack(linearLayout)

        val cell = ActionBarMenuSubItem(chatActivity.parentActivity, true, true, chatActivity.resourceProvider)
        cell.setTextAndIcon(getString(R.string.CP_GeminiAI_Header), R.drawable.magic_stick_solar)
        popupLayout.addView(cell)
        cell.setOnClickListener {
            if (chatActivity.contentView == null || chatActivity.parentActivity == null) {
                return@setOnClickListener
            }
            popupLayout.swipeBack?.openForeground(foregroundIndex)
        }

        if (chatActivity.messageMenuHelper.allowNewMessageMenu()) {
            if (showDivider /* && getMessageMenuHelper().showCustomDivider() */) {
                popupLayout.addView(
                    ActionBarPopupWindow.GapView(
                        chatActivity.context,
                        ColorUtils.setAlphaComponent(chatActivity.getThemedColor(Theme.key_windowBackgroundGray), chatActivity.messageMenuHelper.getMessageMenuAlpha(true)),
                        Theme.getColor(Theme.key_windowBackgroundGrayShadow, chatActivity.resourceProvider)
                    ),
                    LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 8)
                )
            }
        } else {
            if (showDivider) {
                popupLayout.addView(
                    ActionBarPopupWindow.GapView(chatActivity.context, chatActivity.resourceProvider),
                    LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 8)
                )
            }
        }
    }

    fun injectCopyPhoto(
        items: ArrayList<CharSequence?>,
        options: ArrayList<Int?>,
        icons: ArrayList<Int?>
    ) {
        if (CherrygramChatsConfig.showCopyPhoto) {
            items.add(getString(R.string.CG_CopyPhoto))
            options.add(ChatActivityHelper.OPTION_COPY_PHOTO)
            icons.add(R.drawable.msg_copy)
        }
        if (CherrygramChatsConfig.showCopyPhotoAsSticker) {
            items.add(getString(R.string.CG_CopyPhotoAsSticker))
            options.add(ChatActivityHelper.OPTION_COPY_PHOTO_AS_STICKER)
            icons.add(R.drawable.msg_sticker)
        }
    }

    fun injectClearFromCache(
        items: ArrayList<CharSequence?>,
        options: ArrayList<Int?>,
        icons: ArrayList<Int?>
    ) {
        if (CherrygramChatsConfig.showClearFromCache) {
            items.add(getString(R.string.CG_ClearFromCache))
            options.add(ChatActivityHelper.OPTION_CLEAR_FROM_CACHE)
            icons.add(R.drawable.msg_clear)
        }
    }

    fun injectForwardWoAuthorship(
        selectedObject: MessageObject,
        chatMode: Int,
        items: ArrayList<CharSequence?>,
        options: ArrayList<Int?>,
        icons: ArrayList<Int?>
    ) {
        if (CherrygramChatsConfig.showForwardWoAuthorship && !selectedObject.isSponsored && chatMode != ChatActivity.MODE_SCHEDULED && (!selectedObject.needDrawBluredPreview() || selectedObject.hasExtendedMediaPreview())
            && !selectedObject.isLiveLocation && selectedObject.type != MessageObject.TYPE_PHONE_CALL && selectedObject.type != MessageObject.TYPE_GIFT_PREMIUM && selectedObject.type != MessageObject.TYPE_SUGGEST_PHOTO
        ) {
            items.add(
                getString(R.string.Forward) + " " + getString(
                    R.string.CG_Without_Authorship
                )
            )
            options.add(ChatActivityHelper.OPTION_FORWARD_WO_AUTHOR)
            icons.add(R.drawable.msg_forward)
        }
    }

    fun injectViewHistory(
        items: ArrayList<CharSequence?>,
        options: ArrayList<Int?>,
        icons: ArrayList<Int?>
    ) {
        if (CherrygramChatsConfig.showViewHistory) {
            items.add(getString(R.string.CG_ViewUserHistory))
            options.add(ChatActivityHelper.OPTION_VIEW_HISTORY)
            icons.add(R.drawable.msg_recent)
        }
    }

    fun injectSaveMessage(
        message: MessageObject,
        chatMode: Int,
        currentUser: TLRPC.User?,
        items: ArrayList<CharSequence?>,
        options: ArrayList<Int?>,
        icons: ArrayList<Int?>
    ) {
        if (CherrygramChatsConfig.showSaveMessage && chatMode != ChatActivity.MODE_SCHEDULED && !UserObject.isUserSelf(
                currentUser
            ) && !message.isSponsored
        ) {
            items.add(getString(R.string.CG_ToSaved))
            options.add(ChatActivityHelper.OPTION_SAVE_MESSAGE_CHAT)
            icons.add(R.drawable.msg_saved)
        }
    }

    fun injectViewStatistics(
        chatActivity: ChatActivity,
        message: MessageObject,
        items: ArrayList<CharSequence?>,
        options: ArrayList<Int?>,
        icons: ArrayList<Int?>
    ) {
        if (message.messageOwner.forwards > 0 && ChatObject.hasAdminRights(chatActivity.currentChat) && !message.isForwarded) {
            items.add(getString(R.string.ViewStatistics))
            options.add(ChatActivity.OPTION_STATISTICS)
            icons.add(R.drawable.msg_stats)
        }
    }

    fun injectDownloadSticker(
        selectedObject: MessageObject?,
        items: ArrayList<CharSequence?>,
        options: ArrayList<Int?>,
        icons: ArrayList<Int?>
    ) {
        if (selectedObject?.isAnimatedSticker == false) {
            items.add(getString(R.string.CG_SaveSticker))
            options.add(ChatActivityHelper.OPTION_DOWNLOAD_STICKER)
            icons.add(R.drawable.msg_download)
        }
    }

    fun injectImportSettings(
        items: ArrayList<CharSequence?>,
        options: ArrayList<Int?>,
        icons: ArrayList<Int?>
    ) {
        items.add(getString(R.string.SaveToDownloads))
        options.add(ChatActivity.OPTION_SAVE_TO_DOWNLOADS_OR_MUSIC)
        icons.add(R.drawable.msg_download)

        options.add(ChatActivityHelper.OPTION_IMPORT_SETTINGS)
        items.add(getString(R.string.CG_ImportSettings))
        icons.add(R.drawable.msg_customize)
    }

    fun injectJSON(
        items: ArrayList<CharSequence?>,
        options: ArrayList<Int?>,
        icons: ArrayList<Int?>
    ) {
        if (CherrygramChatsConfig.showJSON) {
            items.add("JSON")
            options.add(ChatActivityHelper.OPTION_DETAILS)
            icons.add(R.drawable.msg_info)
        }
    }

    fun showMessageMenuItemsConfigurator(fragment: BaseFragment) {
        val menuItems = listOf(
            MenuItemConfig(
                getString(R.string.SaveForNotifications),
                R.drawable.msg_tone_add,
                { CherrygramChatsConfig.showSaveForNotifications },
                { CherrygramChatsConfig.showSaveForNotifications = !CherrygramChatsConfig.showSaveForNotifications },
                true
            ),
            MenuItemConfig(
                getString(R.string.CP_GeminiAI_Header),
                R.drawable.magic_stick_solar,
                { CherrygramChatsConfig.showGemini },
                { CherrygramChatsConfig.showGemini = !CherrygramChatsConfig.showGemini },
                true
            ),
            MenuItemConfig(
                getString(R.string.Reply),
                R.drawable.menu_reply,
                { CherrygramChatsConfig.showReply },
                { CherrygramChatsConfig.showReply = !CherrygramChatsConfig.showReply }
            ),
            MenuItemConfig(
                getString(R.string.CG_CopyPhoto),
                R.drawable.msg_copy,
                { CherrygramChatsConfig.showCopyPhoto },
                { CherrygramChatsConfig.showCopyPhoto = !CherrygramChatsConfig.showCopyPhoto }
            ),
            MenuItemConfig(
                getString(R.string.CG_CopyPhotoAsSticker),
                R.drawable.msg_copy,
                { CherrygramChatsConfig.showCopyPhotoAsSticker },
                { CherrygramChatsConfig.showCopyPhotoAsSticker = !CherrygramChatsConfig.showCopyPhotoAsSticker }
            ),
            MenuItemConfig(
                getString(R.string.CG_ClearFromCache),
                R.drawable.msg_clear,
                { CherrygramChatsConfig.showClearFromCache },
                { CherrygramChatsConfig.showClearFromCache = !CherrygramChatsConfig.showClearFromCache }
            ),
            MenuItemConfig(
                getString(R.string.Forward),
                R.drawable.msg_forward,
                { CherrygramChatsConfig.showForward },
                { CherrygramChatsConfig.showForward = !CherrygramChatsConfig.showForward }
            ),
            MenuItemConfig(
                getString(R.string.Forward) + " " + getString(R.string.CG_Without_Authorship),
                R.drawable.msg_forward,
                { CherrygramChatsConfig.showForwardWoAuthorship },
                { CherrygramChatsConfig.showForwardWoAuthorship = !CherrygramChatsConfig.showForwardWoAuthorship }
            ),
            MenuItemConfig(
                getString(R.string.CG_ViewUserHistory),
                R.drawable.msg_recent,
                { CherrygramChatsConfig.showViewHistory },
                { CherrygramChatsConfig.showViewHistory = !CherrygramChatsConfig.showViewHistory }
            ),
            MenuItemConfig(
                getString(R.string.CG_ToSaved),
                R.drawable.msg_saved,
                { CherrygramChatsConfig.showSaveMessage },
                { CherrygramChatsConfig.showSaveMessage = !CherrygramChatsConfig.showSaveMessage }
            ),
            MenuItemConfig(
                getString(R.string.ReportChat),
                R.drawable.msg_report,
                { CherrygramChatsConfig.showReport },
                { CherrygramChatsConfig.showReport = !CherrygramChatsConfig.showReport }
            ),
            MenuItemConfig(
                "JSON",
                R.drawable.msg_info,
                { CherrygramChatsConfig.showJSON },
                { CherrygramChatsConfig.showJSON = !CherrygramChatsConfig.showJSON }
            )
        )

        val prefTitle = ArrayList<String>()
        val prefIcon = ArrayList<Int>()
        val prefCheck = ArrayList<Boolean>()
        val prefDivider = ArrayList<Boolean>()
        val clickListener = ArrayList<Runnable>()

        for (item in menuItems) {
            prefTitle.add(item.titleRes)
            prefIcon.add(item.iconRes)
            prefCheck.add(item.isChecked())
            prefDivider.add(item.divider)
            clickListener.add(Runnable { item.toggle() })
        }

        PopupHelper.showSwitchAlert(
            getString(R.string.CP_MessageMenu),
            fragment,
            prefTitle,
            prefIcon,
            prefCheck,
            null,
            null,
            prefDivider,
            clickListener,
            null
        )

    }

    data class MenuItemConfig(
        val titleRes: String,
        val iconRes: Int,
        val isChecked: () -> Boolean,
        val toggle: () -> Unit,
        val divider: Boolean = false
    )

}
