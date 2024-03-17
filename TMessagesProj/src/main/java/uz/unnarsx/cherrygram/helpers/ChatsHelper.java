package uz.unnarsx.cherrygram.helpers;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BaseController;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.ColoredImageSpan;
import org.telegram.ui.Components.EmojiPacksAlert;
import org.telegram.ui.Components.Reactions.ReactionsLayoutInBubble;
import org.telegram.ui.PeerColorActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

import uz.unnarsx.cherrygram.CherrygramConfig;

public class ChatsHelper extends BaseController {

    private static final ChatsHelper[] Instance = new ChatsHelper[UserConfig.MAX_ACCOUNT_COUNT];

    public ChatsHelper(int num) {
        super(num);
    }

    public static ChatsHelper getInstance(int num) {
        ChatsHelper localInstance = Instance[num];
        if (localInstance == null) {
            synchronized (ChatsHelper.class) {
                localInstance = Instance[num];
                if (localInstance == null) {
                    Instance[num] = localInstance = new ChatsHelper(num);
                }
            }
        }
        return localInstance;
    }

    public ChatActivity.ThemeDelegate themeDelegate;

    public static SpannableStringBuilder forwardsSpan;
    public static Drawable forwardsDrawable;

    public static SpannableStringBuilder editedSpan;
    public static Drawable editedDrawable;

    public static CharSequence createForwardedString(MessageObject messageObject) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

        if (forwardsDrawable == null) {
            forwardsDrawable = Objects.requireNonNull(ContextCompat.getDrawable(ApplicationLoader.applicationContext, R.drawable.forwards_solar)).mutate();
        }
        if (forwardsSpan == null) {
            forwardsSpan = new SpannableStringBuilder("\u200B");
            forwardsSpan.setSpan(new ColoredImageSpan(forwardsDrawable), 0, 1, 0);
        }
        spannableStringBuilder
                .append(' ')
                .append(forwardsSpan)
                .append(' ')
                .append(String.format("%d", messageObject.messageOwner.forwards))
                .append( " • ")
                .append(LocaleController.getInstance().formatterDay.format((long) (messageObject.messageOwner.date) * 1000));
        return spannableStringBuilder;
    }

    public static CharSequence createEditedString(MessageObject messageObject) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        boolean hasForwards = messageObject.messageOwner.forwards > 0;

        if (editedDrawable == null) {
            editedDrawable = Objects.requireNonNull(ContextCompat.getDrawable(ApplicationLoader.applicationContext, R.drawable.msg_edited)).mutate();
        }
        if (editedSpan == null) {
            editedSpan = new SpannableStringBuilder("\u200B");
            editedSpan.setSpan(new ColoredImageSpan(editedDrawable), 0, 1, 0);
        }
        if (forwardsDrawable == null) {
            forwardsDrawable = Objects.requireNonNull(ContextCompat.getDrawable(ApplicationLoader.applicationContext, R.drawable.forwards_solar)).mutate();
        }
        if (forwardsSpan == null) {
            forwardsSpan = new SpannableStringBuilder("\u200B");
            forwardsSpan.setSpan(new ColoredImageSpan(forwardsDrawable), 0, 1, 0);
        }
        spannableStringBuilder
                .append(' ')
                .append(hasForwards ? forwardsSpan : "")
                .append(hasForwards ? " " : "")
                .append(hasForwards ? String.format("%d", messageObject.messageOwner.forwards) : "")
                .append(' ')
                .append(hasForwards ? "• " : "")
                .append(CherrygramConfig.INSTANCE.getShowPencilIcon() ? editedSpan : LocaleController.getString("EditedMessage", R.string.EditedMessage))
                .append(hasForwards ? " • " : " ")
                .append(LocaleController.getInstance().formatterDay.format((long) (messageObject.messageOwner.date) * 1000));
        return spannableStringBuilder;
    }

    public static void addMessageToClipboard(MessageObject selectedObject, Runnable callback) {
        String path = getPathToMessage(selectedObject);
        if (!TextUtils.isEmpty(path)) {
            addFileToClipboard(new File(path), callback);
        }
    }

    public static void addMessageToClipboardAsSticker(MessageObject selectedObject, Runnable callback) {
        String path = getPathToMessage(selectedObject);

        try {
            Bitmap image = BitmapFactory.decodeFile(path);
            if (image != null && !TextUtils.isEmpty(path)) {
                File file = new File(path.endsWith(".jpg") ? path.replace(".jpg", ".webp") : path + ".webp");
                FileOutputStream stream = new FileOutputStream(file);
                image.compress(Bitmap.CompressFormat.WEBP, 50, stream);
                stream.close();
                addFileToClipboard(file, callback);
            }
        } catch (Exception ignored) {}
    }

    public static String getPathToMessage(MessageObject messageObject) {
        String path = messageObject.messageOwner.attachPath;
        if (!TextUtils.isEmpty(path)) {
            File temp = new File(path);
            if (!temp.exists()) {
                path = null;
            }
        }
        if (TextUtils.isEmpty(path)) {
            path = FileLoader.getInstance(UserConfig.selectedAccount).getPathToMessage(messageObject.messageOwner).toString();
            File temp = new File(path);
            if (!temp.exists()) {
                path = null;
            }
        }
        if (TextUtils.isEmpty(path)) {
            path = FileLoader.getInstance(UserConfig.selectedAccount).getPathToAttach(messageObject.getDocument(), true).toString();
            File temp = new File(path);
            if (!temp.exists()) {
                return null;
            }
        }
        return path;
    }

    public static void addFileToClipboard(File file, Runnable callback) {
        try {
            Context context = ApplicationLoader.applicationContext;
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            Uri uri = FileProvider.getUriForFile(context, ApplicationLoader.getApplicationId() + ".provider", file);
            ClipData clip = ClipData.newUri(context.getContentResolver(), "label", uri);
            clipboard.setPrimaryClip(clip);
            callback.run();
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public void saveStickerToGallery(Activity activity, MessageObject messageObject, Utilities.Callback<Uri> callback) {
        saveStickerToGallery(activity, getPathToMessage(messageObject), messageObject.isVideoSticker(), callback);
    }

    public static void saveStickerToGallery(Activity activity, TLRPC.Document document, Utilities.Callback<Uri> callback) {
        String path = FileLoader.getInstance(UserConfig.selectedAccount).getPathToAttach(document, true).toString();
        File temp = new File(path);
        if (!temp.exists()) {
            return;
        }
        saveStickerToGallery(activity, path, MessageObject.isVideoSticker(document), callback);
    }

    private static void saveStickerToGallery(Activity activity, String path, boolean video, Utilities.Callback<Uri> callback) {
        Utilities.globalQueue.postRunnable(() -> {
            try {
                if (video) {
                    MediaController.saveFile(path, activity, 1, null, null, callback);
                } else {
                    Bitmap image = BitmapFactory.decodeFile(path);
                    if (image != null) {
                        File file = new File(path.endsWith(".webp") ? path.replace(".webp", ".png") : path + ".png");
                        FileOutputStream stream = new FileOutputStream(file);
                        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        stream.close();
                        MediaController.saveFile(file.toString(), activity, 0, null, null, callback);
                    }
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        });
    }

    public long getEmojiIdFromReply(MessageObject messageObject, TLRPC.User user) {
        if (messageObject == null || messageObject.messageOwner == null || messageObject.replyMessageObject == null || messageObject.messageOwner.from_id == null) {
            return 0;
        }
        if (messageObject.replyMessageObject.isFromUser() && user != null) {
            return UserObject.getEmojiId(user);
        } else {
            return ChatObject.getEmojiId(MessagesController.getInstance(messageObject.currentAccount).getChat(messageObject.replyMessageObject.messageOwner.from_id.channel_id));
        }
    }

    private int getEmojiBackgroundFromReply(MessageObject messageObject, TLRPC.User user) {
        if (messageObject == null || messageObject.messageOwner == null || messageObject.replyMessageObject == null || messageObject.messageOwner.from_id == null) {
            return 0;
        }
        if (messageObject.replyMessageObject.isFromUser() && user != null) {
            return UserObject.getColorId(user);
        } else {
            return ChatObject.getColorId(MessagesController.getInstance(messageObject.currentAccount).getChat(messageObject.replyMessageObject.messageOwner.from_id.channel_id));
        }
    }

    public void applyReplyBackground(MessageObject selectedObject, BaseFragment fragment) {
        long emojiDocumentId = getEmojiIdFromReply(selectedObject, MessagesController.getInstance(UserConfig.selectedAccount).getUser(selectedObject.replyMessageObject.messageOwner.from_id.user_id));
        int colorId = getEmojiBackgroundFromReply(selectedObject, MessagesController.getInstance(UserConfig.selectedAccount).getUser(selectedObject.replyMessageObject.messageOwner.from_id.user_id));
        TLRPC.User me = UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser();

        TLRPC.TL_account_updateColor req = new TLRPC.TL_account_updateColor();
        if (me.color == null) {
            me.color = new TLRPC.TL_peerColor();
            me.flags2 |= 256;
            me.color.flags |= 1;
        }
        req.flags |= 4;
        req.color = me.color.color = colorId;
        if (emojiDocumentId != 0) {
            req.flags |= 1;
            me.color.flags |= 2;
            req.background_emoji_id = me.color.background_emoji_id = emojiDocumentId;
        } else {
            me.color.flags &=~ 2;
            me.color.background_emoji_id = 0;
        }
        ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(req, null);

        fragment.presentFragment(new PeerColorActivity(0).setOnApplied(fragment));
    }

    public void openEmojiPack(MessageObject selectedObject, BaseFragment fragment) {
        long emojiDocumentId = getEmojiIdFromReply(selectedObject, MessagesController.getInstance(UserConfig.selectedAccount).getUser(selectedObject.replyMessageObject.messageOwner.from_id.user_id));

        AnimatedEmojiDrawable.getDocumentFetcher(UserConfig.selectedAccount).fetchDocument(emojiDocumentId, document -> AndroidUtilities.runOnUIThread(() -> {
            ArrayList<TLRPC.InputStickerSet> inputSets = new ArrayList<>(1);
            inputSets.add(MessageObject.getInputStickerSet(document));
            EmojiPacksAlert alert = new EmojiPacksAlert(fragment, fragment.getParentActivity(), themeDelegate, inputSets);
            alert.setDimBehindAlpha(100);
            alert.show();
        }));
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private Bitmap cropCenter(Bitmap bmp) {
        int dimension = Math.min(bmp.getWidth(), bmp.getHeight());
        return ThumbnailUtils.extractThumbnail(bmp, dimension, dimension);
    }

    public Drawable getBackgroundDrawable(Drawable drawable) {
        Drawable d = new BitmapDrawable(ApplicationLoader.applicationContext.getResources(), cropCenter(drawableToBitmap(drawable)));

        return d;
    }

    public int getCustomReactionsCount(MessageObject selectedObject) {
        ArrayList<ReactionsLayoutInBubble.VisibleReaction> visibleCustomReactions = new ArrayList<>(selectedObject.getCustomReactions());
        ArrayList<TLRPC.InputStickerSet> customEmojiStickerSets = new ArrayList<>();

        if (selectedObject.messageOwner != null && selectedObject.messageOwner.reactions != null) {
            for (int i = 0; i < visibleCustomReactions.size(); i++) {
                customEmojiStickerSets.clear();
                ArrayList<TLRPC.InputStickerSet> stickerSets = new ArrayList<>();
                HashSet<Long> setIds = new HashSet<>();
                for (int j = 0; j < visibleCustomReactions.size(); j++) {
                    TLRPC.InputStickerSet stickerSet = MessageObject.getInputStickerSet(AnimatedEmojiDrawable.findDocument(currentAccount, visibleCustomReactions.get(j).documentId));
                    if (stickerSet != null && !setIds.contains(stickerSet.id)) {
                        stickerSets.add(stickerSet);
                        setIds.add(stickerSet.id);
                    }
                }
                customEmojiStickerSets.addAll(stickerSets);
            }
        }

        return customEmojiStickerSets.size();
    }
}
