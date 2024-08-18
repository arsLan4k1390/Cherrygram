package uz.unnarsx.cherrygram.chats;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ChatActivity;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.CherrygramConfig;

// I've created this so CG features can be injected in a source file with 1 line only (maybe)
// Because manual editing of drklo's sources harms your mental health.
public class CherrygramMessageMenuInjector {

    public static void injectCopyPhoto(ArrayList<CharSequence> items, final ArrayList<Integer> options, ArrayList<Integer> icons) {
        if (CherrygramConfig.INSTANCE.getShowCopyPhoto()) {
            items.add(LocaleController.getString("CG_CopyPhoto", R.string.CG_CopyPhoto));
            options.add(ChatActivity.OPTION_COPY_PHOTO);
            icons.add(R.drawable.msg_copy);
        }
        if (CherrygramConfig.INSTANCE.getShowCopyPhotoAsSticker()) {
            items.add(LocaleController.getString("CG_CopyPhotoAsSticker", R.string.CG_CopyPhotoAsSticker));
            options.add(ChatActivity.OPTION_COPY_PHOTO_AS_STICKER);
            icons.add(R.drawable.msg_sticker);
        }
    }

    public static void injectClearFromCache(ArrayList<CharSequence> items, final ArrayList<Integer> options, ArrayList<Integer> icons) {
        if (CherrygramConfig.INSTANCE.getShowClearFromCache()) {
            items.add(LocaleController.getString("CG_ClearFromCache", R.string.CG_ClearFromCache));
            options.add(ChatActivity.OPTION_CLEAR_FROM_CACHE);
            icons.add(R.drawable.clear_cache);
        }
    }

    public static void injectForwardWoAuthorship(MessageObject selectedObject, int chatMode, ArrayList<CharSequence> items, final ArrayList<Integer> options, ArrayList<Integer> icons) {
        if (CherrygramConfig.INSTANCE.getShowForwardWoAuthorship() && !selectedObject.isSponsored() && chatMode != ChatActivity.MODE_SCHEDULED
                && (!selectedObject.needDrawBluredPreview() || selectedObject.hasExtendedMediaPreview())
                && !selectedObject.isLiveLocation() && selectedObject.type != MessageObject.TYPE_PHONE_CALL
                && selectedObject.type != MessageObject.TYPE_GIFT_PREMIUM && selectedObject.type != MessageObject.TYPE_SUGGEST_PHOTO
        ) {
            items.add(LocaleController.getString("Forward", R.string.Forward) + " " + LocaleController.getString("CG_Without_Authorship", R.string.CG_Without_Authorship));
            options.add(ChatActivity.OPTION_FORWARD_WO_AUTHOR);
            icons.add(R.drawable.msg_forward);
        }
    }

    public static void injectViewHistory(ArrayList<CharSequence> items, final ArrayList<Integer> options, ArrayList<Integer> icons) {
        if (CherrygramConfig.INSTANCE.getShowViewHistory()) {
            items.add(LocaleController.getString("CG_ViewUserHistory", R.string.CG_ViewUserHistory));
            options.add(ChatActivity.OPTION_VIEW_HISTORY);
            icons.add(R.drawable.msg_recent);
        }
    }

    public static void injectSaveMessage(MessageObject message, int chatMode, TLRPC.User currentUser, ArrayList<CharSequence> items, final ArrayList<Integer> options, ArrayList<Integer> icons) {
        if (CherrygramConfig.INSTANCE.getShowSaveMessage() && chatMode != ChatActivity.MODE_SCHEDULED
                && !UserObject.isUserSelf(currentUser) && !message.isSponsored()
        ) {
            items.add(LocaleController.getString("CG_ToSaved", R.string.CG_ToSaved));
            options.add(ChatActivity.OPTION_SAVE_MESSAGE_CHAT);
            icons.add(R.drawable.msg_saved);
        }
    }

    public static void injectDownloadSticker(MessageObject selectedObject, ArrayList<CharSequence> items, final ArrayList<Integer> options, ArrayList<Integer> icons) {
        if (!selectedObject.isAnimatedSticker()) {
            items.add(LocaleController.getString("CG_SaveSticker", R.string.CG_SaveSticker));
            options.add(ChatActivity.OPTION_DOWNLOAD_STICKER);
            icons.add(R.drawable.msg_download);
        }
    }

    public static void injectJSON(ArrayList<CharSequence> items, final ArrayList<Integer> options, ArrayList<Integer> icons) {
        if (CherrygramConfig.INSTANCE.getShowJSON()) {
            items.add("JSON");
            options.add(ChatActivity.OPTION_DETAILS);
            icons.add(R.drawable.msg_info);
        }
    }

}
