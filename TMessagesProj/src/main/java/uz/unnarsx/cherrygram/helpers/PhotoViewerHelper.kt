/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.helpers

import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.FileLoader
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.MessageObject
import org.telegram.messenger.R
import org.telegram.tgnet.TLRPC
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.Components.AnimatedEmojiDrawable
import org.telegram.ui.Components.EmojiPacksAlert
import org.telegram.ui.Components.StickersAlert
import org.telegram.ui.PhotoViewer.PhotoViewerActionBarContainer
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper

class PhotoViewerHelper(
    private val avatarsArr: ArrayList<TLRPC.Photo>,
    private var switchingToIndex: Int,
    private var avatarsDialogId: Long,
    private var parentFragment: BaseFragment,
    private var actionBarContainer: PhotoViewerActionBarContainer
) {

    fun getUserFull(): TLRPC.UserFull? {
        return parentFragment.messagesController.getUserFull(avatarsDialogId)
    }

    fun getUserAvatarDate(): String {
        val avatar = avatarsArr.getOrNull(switchingToIndex)
        val userFull = getUserFull()

        return when {
            avatar?.date?.takeIf { it != 0 } != null -> formatDate(avatar.date, null)
            userFull?.fallback_photo?.date?.takeIf { it != 0 } != null -> formatDate(userFull.fallback_photo.date, getString(R.string.FallbackTooltip))
            userFull?.personal_photo?.date?.takeIf { it != 0 } != null -> formatDate(userFull.personal_photo.date, getString(R.string.CustomAvatarTooltip))
            else -> ""
        }
    }

    private fun formatDate(date: Int, subtitle: String?): String {
        if (date == 0) return ""

        val formattedDate = CGResourcesHelper.createDateAndTime(date.toLong())
        return subtitle?.let { "$formattedDate | $subtitle" } ?: formattedDate
    }

    fun getEmojiMarkup(): TLRPC.VideoSize? {
        val avatar = avatarsArr.getOrNull(switchingToIndex)
        val userFull = getUserFull()

        val hasVideoAvatar = when {
            avatar?.video_sizes?.isNotEmpty() == true -> true
            userFull?.profile_photo?.video_sizes?.isNotEmpty() == true -> true
            else -> false
        }

        val hasPublicVideoAvatar = userFull?.fallback_photo?.video_sizes?.isNotEmpty() == true

        return when {
            hasVideoAvatar -> FileLoader.getEmojiMarkup(avatar?.video_sizes ?: userFull?.profile_photo?.video_sizes)
            hasPublicVideoAvatar -> FileLoader.getEmojiMarkup(userFull.fallback_photo?.video_sizes)
            else -> null
        }
    }

    fun fetchSetId(animated: Boolean) {
        val subtitle = StringBuilder(getUserAvatarDate())

        val emojiMarkup = getEmojiMarkup() ?: return

        when (emojiMarkup) {
            is TLRPC.TL_videoSizeEmojiMarkup -> {
                val documentId = emojiMarkup.emoji_id
                AnimatedEmojiDrawable.getDocumentFetcher(parentFragment.currentAccount)
                    .fetchDocument(documentId) { document ->
                        AndroidUtilities.runOnUIThread {
                            actionBarContainer.subtitleTextView?.setOnClickListener {
                                val inputSets = arrayListOf(MessageObject.getInputStickerSet(document))
                                EmojiPacksAlert(
                                    parentFragment,
                                    parentFragment.context,
                                    parentFragment.resourceProvider,
                                    inputSets
                                ).show()
                            }
                        }
                    }
                subtitle.append(" | ").append(getString(R.string.CG_OpenEmojiPack))
            }

            is TLRPC.TL_videoSizeStickerMarkup -> {
                val inputStickerSet = TLRPC.TL_inputStickerSetID().apply {
                    access_hash = emojiMarkup.stickerset.access_hash
                    id = emojiMarkup.stickerset.id
                }
                actionBarContainer.subtitleTextView?.setOnClickListener {
                    StickersAlert(
                        parentFragment.context,
                        parentFragment,
                        inputStickerSet,
                        null,
                        null,
                        parentFragment.resourceProvider,
                        true
                    ).show()
                }
                subtitle.append(" | ").append(getString(R.string.CG_OpenStickerPack))
            }
        }

        actionBarContainer.setSubtitle(subtitle.toString(), animated)
    }

    fun getSubtitle(): CharSequence {
        val avatar = avatarsArr.getOrNull(switchingToIndex)
        val chat = parentFragment.messagesController.getChat(-avatarsDialogId)

        return if (avatarsDialogId > 0) {
            getUserAvatarDate()
        } else if (chat != null) {
            val chatFull = parentFragment.messagesController.getChatFull(-avatarsDialogId)
            when {
                avatar?.date?.takeIf { it != 0 } != null -> CGResourcesHelper.createDateAndTime(avatar.date.toLong())
                chatFull?.chat_photo?.date?.takeIf { it != 0 } != null -> CGResourcesHelper.createDateAndTime(chatFull.chat_photo.date.toLong())
                else -> ""
            }
        } else {
            ""
        }
    }

    fun release() {
        avatarsArr.clear()
        switchingToIndex = 0
        avatarsDialogId = 0
    }

}
