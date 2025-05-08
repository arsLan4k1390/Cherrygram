/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.helpers

import org.telegram.messenger.AndroidUtilities
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
    private val actionBarContainer: PhotoViewerActionBarContainer,
    private val avatarsArr: ArrayList<TLRPC.Photo>,
    private val switchingToIndex: Int,
    private val userFull: TLRPC.UserFull?,
    private val emojiMarkup: TLRPC.VideoSize?,
    private val parentFragment: BaseFragment
) {

    fun getUserAvatarDate(): String {
        val avatar = avatarsArr.getOrNull(switchingToIndex)

        return when {
            avatar?.date != null && avatar.date != 0 -> formatDate(avatar.date, null)
            userFull?.fallback_photo?.date != null && userFull.fallback_photo.date != 0 -> formatDate(userFull.fallback_photo.date, getString(R.string.FallbackTooltip))
            userFull?.personal_photo?.date != null && userFull.personal_photo.date != 0 -> formatDate(userFull.personal_photo.date, getString(R.string.CustomAvatarTooltip))
            else -> ""
        }
    }

    private fun formatDate(date: Int, subtitle: String?): String {
        if (date == 0) return ""

        val formattedDate = CGResourcesHelper.createDateAndTime(date.toLong())
        return subtitle?.let { "$formattedDate | $subtitle" } ?: formattedDate
    }

    fun fetchSetId() {
        val subtitle = StringBuilder(getUserAvatarDate())

        if (emojiMarkup == null) return

        when (emojiMarkup) {
            is TLRPC.TL_videoSizeEmojiMarkup -> {
                val documentId = emojiMarkup.emoji_id
                AnimatedEmojiDrawable.getDocumentFetcher(parentFragment.currentAccount)
                    .fetchDocument(documentId) { document ->
                        AndroidUtilities.runOnUIThread {
                            actionBarContainer.subtitleTextView?.setOnClickListener {
                                val inputSets = ArrayList<TLRPC.InputStickerSet>(1)
                                inputSets.add(MessageObject.getInputStickerSet(document))
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

        actionBarContainer.setSubtitle(subtitle.toString(), true)
    }

}
