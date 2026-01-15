/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.chats.helpers;

import static org.telegram.messenger.LocaleController.getString;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Base64;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.collection.LongSparseArray;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BaseController;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.Emoji;
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
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_account;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.ColoredImageSpan;
import org.telegram.ui.Components.EmojiPacksAlert;
import org.telegram.ui.Components.ItemOptions;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.Reactions.ReactionsLayoutInBubble;
import org.telegram.ui.Components.ScrimOptions;
import org.telegram.ui.Components.ShareAlert;
import org.telegram.ui.Components.TranscribeButton;
import org.telegram.ui.Components.UndoView;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.PeerColorActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;

import uz.unnarsx.cherrygram.core.CGFeatureHooks;
import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig;
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper;
import uz.unnarsx.cherrygram.helpers.ui.PopupHelper;

// I've created this so CG features can be injected in a source file with 1 line only (maybe)
// Because manual editing of drklo's sources harms your mental health.
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
            forwardsSpan.setSpan(new ColoredImageSpan(forwardsDrawable, true), 0, 1, 0);
        }
        spannableStringBuilder
                .append(' ')
                .append(forwardsSpan)
                .append(' ')
                .append(String.format("%d", messageObject.messageOwner.forwards))
                .append( " • ")
                .append(LocaleController.getInstance().getFormatterDay().format((long) (messageObject.messageOwner.date) * 1000));
        return spannableStringBuilder;
    }

    public static CharSequence createEditedString(MessageObject messageObject) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        boolean hasForwards = messageObject.messageOwner.forwards > 0;
        boolean isMusic = messageObject.isMusic();

        if (editedDrawable == null) {
            editedDrawable = Objects.requireNonNull(ContextCompat.getDrawable(ApplicationLoader.applicationContext, R.drawable.msg_edited)).mutate();
        }
        if (editedSpan == null) {
            editedSpan = new SpannableStringBuilder("\u200B");
            editedSpan.setSpan(new ColoredImageSpan(editedDrawable, true), 0, 1, 0);
        }
        if (forwardsDrawable == null) {
            forwardsDrawable = Objects.requireNonNull(ContextCompat.getDrawable(ApplicationLoader.applicationContext, R.drawable.forwards_solar)).mutate();
        }
        if (forwardsSpan == null) {
            forwardsSpan = new SpannableStringBuilder("\u200B");
            forwardsSpan.setSpan(new ColoredImageSpan(forwardsDrawable, true), 0, 1, 0);
        }
        spannableStringBuilder
                .append(isMusic ? "" : " ")
                .append(hasForwards && !isMusic ? forwardsSpan : "")
                .append(hasForwards && !isMusic ? " " : "")
                .append(hasForwards && !isMusic ? String.format("%d", messageObject.messageOwner.forwards) : "")
                .append(isMusic ? "" : " ")
                .append(hasForwards && !isMusic ? "• " : "")
                .append(CherrygramChatsConfig.INSTANCE.getShowPencilIcon() ? editedSpan : getString(R.string.EditedMessage))
                .append(hasForwards && !isMusic ? " • " : " ")
                .append(LocaleController.getInstance().getFormatterDay().format((long) (messageObject.messageOwner.date) * 1000));
        return spannableStringBuilder;
    }

    public void addMessageToClipboard(MessageObject selectedObject, Runnable callback) {
        String path = getPathToMessage(selectedObject);
        if (!TextUtils.isEmpty(path)) {
            addFileToClipboard(new File(path), callback);
        }
    }

    public String getPathToMessage(MessageObject messageObject) {
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

    public void addFileToClipboard(File file, Runnable callback) {
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

//    public void saveStickerToGallery(Activity activity, MessageObject messageObject, Utilities.Callback<Uri> callback) {
//        saveStickerToGallery(activity, getPathToMessage(messageObject), messageObject.isVideoSticker(), callback);
//    }

    public void saveStickerToGallery(Activity activity, TLRPC.Document document, Utilities.Callback<Uri> callback) {
        String path = FileLoader.getInstance(UserConfig.selectedAccount).getPathToAttach(document, true).toString();
        File temp = new File(path);
        if (!temp.exists()) {
            return;
        }
        saveStickerToGallery(activity, path, MessageObject.isVideoSticker(document), callback);
    }

    private void saveStickerToGallery(Activity activity, String path, boolean video, Utilities.Callback<Uri> callback) {
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

    public long getEmojiIdFromReply(MessageObject messageObject, TLRPC.User currentUser) {
        if (messageObject != null && messageObject.messageOwner != null && messageObject.replyMessageObject != null && messageObject.replyMessageObject.messageOwner != null && messageObject.replyMessageObject.messageOwner.from_id != null) {
            if (DialogObject.isEncryptedDialog(messageObject.replyMessageObject.getDialogId())) {
                TLRPC.User user = messageObject.replyMessageObject.isOutOwner() ? UserConfig.getInstance(messageObject.replyMessageObject.currentAccount).getCurrentUser() : currentUser;
                if (user != null) {
                    return UserObject.getEmojiId(user);
                }
            } else if (messageObject.replyMessageObject.isFromUser()) {
                TLRPC.User user = MessagesController.getInstance(messageObject.currentAccount).getUser(messageObject.replyMessageObject.messageOwner.from_id.user_id);
                if (user != null) {
                    return UserObject.getEmojiId(user);
                }
            } else if (messageObject.replyMessageObject.isFromChannel()) {
                TLRPC.Chat chat = MessagesController.getInstance(messageObject.currentAccount).getChat(messageObject.replyMessageObject.messageOwner.from_id.channel_id);
                if (chat != null) {
                    return ChatObject.getEmojiId(chat);
                }
            }
        }
        return 0;
    }

    private int getEmojiBackgroundFromReply(MessageObject messageObject, TLRPC.User currentUser) {
        if (messageObject != null && messageObject.messageOwner != null && messageObject.replyMessageObject != null && messageObject.replyMessageObject.messageOwner != null && messageObject.replyMessageObject.messageOwner.from_id != null) {
            if (DialogObject.isEncryptedDialog(messageObject.replyMessageObject.getDialogId())) {
                TLRPC.User user = messageObject.replyMessageObject.isOutOwner() ? UserConfig.getInstance(messageObject.replyMessageObject.currentAccount).getCurrentUser() : currentUser;
                if (user != null) {
                    return UserObject.getColorId(user);
                }
            } else if (messageObject.replyMessageObject.isFromUser()) {
                TLRPC.User user = MessagesController.getInstance(messageObject.currentAccount).getUser(messageObject.replyMessageObject.messageOwner.from_id.user_id);
                if (user != null) {
                    return UserObject.getColorId(user);
                }
            } else if (messageObject.replyMessageObject.isFromChannel()) {
                TLRPC.Chat chat = MessagesController.getInstance(messageObject.currentAccount).getChat(messageObject.replyMessageObject.messageOwner.from_id.channel_id);
                if (chat != null) {
                    return ChatObject.getColorId(chat);
                }
            }
        }
        return 0;
    }

    public void applyReplyBackground(MessageObject selectedObject, BaseFragment fragment) {
        long emojiDocumentId = getEmojiIdFromReply(selectedObject, MessagesController.getInstance(UserConfig.selectedAccount).getUser(selectedObject.replyMessageObject.messageOwner.from_id.user_id));
        int colorId = getEmojiBackgroundFromReply(selectedObject, MessagesController.getInstance(UserConfig.selectedAccount).getUser(selectedObject.replyMessageObject.messageOwner.from_id.user_id));
        TLRPC.User me = UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser();

        final TL_account.updateColor req = new TL_account.updateColor();
        if (me.color == null) {
            me.color = new TLRPC.PeerColor();
            me.flags2 |= 256;
            me.color.flags |= 1;
        }
        req.flags |= 4;
        req.color = new TLRPC.TL_peerColor();
        req.color.flags |= 1;
        req.color.color = me.color.color = colorId;

        if (emojiDocumentId != 0) {
            me.color.flags |= 2;
            req.color.flags |= 2;
            req.color.background_emoji_id = me.color.background_emoji_id = emojiDocumentId;
        } else {
            me.color.flags &= ~2;
            me.color.background_emoji_id = 0;
            req.color.flags &= ~2;
            req.color.background_emoji_id = 0;
        }

        getConnectionsManager().sendRequest(req, (res, err) -> {
            if (res != null) {
                AndroidUtilities.runOnUIThread(() -> {
                    BulletinFactory.of(fragment).createSimpleBulletin(
                            PeerColorActivity.PeerColorDrawable.from(currentAccount, colorId),
                            getString(R.string.UserColorApplied)
                    ).setDuration(Bulletin.DURATION_PROLONG).show();
                });
            }
        });
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

    public void showCustomReactionsInfo(ChatActivity chatActivity, ActionBarPopupWindow.ActionBarPopupWindowLayout popupLayout, MessageObject message, MessageObject selectedObject, TLRPC.Chat currentChat, boolean isTopic) {
        boolean buttonAvailable = (message != null && message.messageOwner != null && message.messageOwner.reactions != null
                && ChatObject.isChannel(currentChat) && !ChatObject.canSendMessages(currentChat)
                && (!currentChat.megagroup || !currentChat.gigagroup || !isTopic)/* && getUserConfig().isPremium()*/
        );

        if (buttonAvailable && getCustomReactionsCount(selectedObject) > 0) {
            if (chatActivity.getMessageMenuHelper().allowNewMessageMenu() && chatActivity.getMessageMenuHelper().showCustomDivider(false)) {
                // Don't remove the divider here cause of broken layout
                View gap = new FrameLayout(chatActivity.contentView.getContext());
                gap.setBackgroundColor(ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_windowBackgroundGray, themeDelegate), chatActivity.getMessageMenuHelper().getMessageMenuAlpha(true)));
                popupLayout.addView(gap, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 8));
            } else {
                View gap = new FrameLayout(chatActivity.getContext());
                gap.setBackgroundColor(chatActivity.getThemedColor(Theme.key_actionBarDefaultSubmenuSeparator));
                popupLayout.addView(gap, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 8));
            }

            FrameLayout button = new FrameLayout(chatActivity.getParentActivity());

            TextView buttonText = new TextView(chatActivity.getParentActivity());
            buttonText.setPadding(AndroidUtilities.dp(18), AndroidUtilities.dp(10), AndroidUtilities.dp(18), AndroidUtilities.dp(10));
            buttonText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            buttonText.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
            buttonText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText));
            buttonText.setText(LocaleController.formatPluralString("CG_MessageContainsCustomReactions", getCustomReactionsCount(selectedObject)));

            button.addView(buttonText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            button.setOnClickListener(e -> chatActivity.processSelectedOption(ChatActivityHelper.OPTION_GET_CUSTOM_REACTIONS));

            popupLayout.addView(button, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            popupLayout.precalculateHeight();
        }
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

    public void showPlasticCardMenu(ChatActivity chatActivity, ItemOptions options, ScrimOptions dialog, String card) {
        if (getUserConfig() != null
                && getUserConfig().getCurrentUser() != null
                && getUserConfig().getCurrentUser().phone != null
                && getUserConfig().getCurrentUser().phone.startsWith("998")
                /*CherrygramChatsConfig.INSTANCE.isDev() && (card.startsWith("9860") || card.startsWith("555536")
                    || card.startsWith("429434") || card.startsWith("418783") || card.startsWith("400847") || card.startsWith("472887") || card.startsWith("406228") || card.startsWith("419813") || card.startsWith("407342")
                    || card.startsWith("8600") || card.startsWith("561468") || card.startsWith("5440") || card.startsWith("6262") || card.startsWith("6264"))*/
        ) {
            options.add(R.drawable.msg_payment_card, "Anorbank P2P", () -> {
                dialog.dismiss();
                AndroidUtilities.addToClipboard(card);
                Browser.openUrl(chatActivity.getParentActivity(), "https://anorbank.uz/deeplink/p2p");
                Toast.makeText(ApplicationLoader.applicationContext, getString(R.string.CardNumberCopied), Toast.LENGTH_SHORT).show();
            });
            options.add(R.drawable.msg_payment_card, "Click P2P", () -> {
                dialog.dismiss();
                AndroidUtilities.addToClipboard(card);
                Browser.openUrl(chatActivity.getParentActivity(), "https://my.click.uz/app/clickp2p/");
                Toast.makeText(ApplicationLoader.applicationContext, getString(R.string.CardNumberCopied), Toast.LENGTH_SHORT).show();
            });
            options.add(R.drawable.msg_payment_card, "Humans P2P", () -> {
                dialog.dismiss();
                AndroidUtilities.addToClipboard(card);
                Browser.openUrl(chatActivity.getParentActivity(), "https://apps.humans.uz/auth/send");
                Toast.makeText(ApplicationLoader.applicationContext, getString(R.string.CardNumberCopied), Toast.LENGTH_SHORT).show();
            });
            options.add(R.drawable.msg_payment_card, "Uzum Bank P2P", () -> {
                dialog.dismiss();
                AndroidUtilities.addToClipboard(card);
                Browser.openUrl(chatActivity.getParentActivity(), "https://uzumbank.uz/goto?action=transfer_to_card");
                Toast.makeText(ApplicationLoader.applicationContext, getString(R.string.CardNumberCopied), Toast.LENGTH_SHORT).show();
            });
            options.addGap();
        }
    }

    public void updateMultipleSelection(ActionBarMenu actionMode, ChatActivity chatActivity) {
        if (actionMode == null) {
            return;
        }
        View item = actionMode.getItem(ChatActivityHelper.OPTION_SELECT_BETWEEN);
        if (item == null) {
            return;
        }
        final boolean t = chatActivity.selectedMessagesIds[0].size() > 1;
        item.setVisibility(t ? View.VISIBLE : View.GONE);
    }

    public void makeReplyButtonClick(ChatActivity chatActivity, MessageObject selectedObject, boolean noForwards) {
        if (noForwards || chatActivity.isInScheduleMode()) createReplyAction(chatActivity, selectedObject);

        switch (CherrygramChatsConfig.INSTANCE.getLeftBottomButton()) {
            case CherrygramChatsConfig.LEFT_BUTTON_REPLY:
                createReplyAction(chatActivity, selectedObject);
                break;
            case CherrygramChatsConfig.LEFT_BUTTON_SAVE_MESSAGE:
                createCGSaveMessagesSelected(chatActivity);
                break;
            case CherrygramChatsConfig.LEFT_BUTTON_DIRECT_SHARE:
                createCGShareAlertSelected(chatActivity);
                break;
            case CherrygramChatsConfig.LEFT_BUTTON_FORWARD_WO_AUTHORSHIP:
                CGFeatureHooks.INSTANCE.switchNoAuthor(true);
                CGFeatureHooks.INSTANCE.switchNoCaptions(false);
                chatActivity.openForward(false);
                break;
            case CherrygramChatsConfig.LEFT_BUTTON_FORWARD_WO_CAPTION:
                CGFeatureHooks.INSTANCE.switchNoAuthor(true);
                CGFeatureHooks.INSTANCE.switchNoCaptions(true);
                chatActivity.openForward(false);
                break;
        }
    }

    public void makeReplyButtonLongClick(ChatActivity chatActivity, boolean noForwards, Theme.ResourcesProvider resourcesProvider) {
        ArrayList<String> configStringKeys = new ArrayList<>();
        ArrayList<Integer> configValues = new ArrayList<>();

        configStringKeys.add(getString(R.string.Forward) + " " + getString(R.string.CG_Without_Authorship));
        configValues.add(CherrygramChatsConfig.LEFT_BUTTON_FORWARD_WO_AUTHORSHIP);

        configStringKeys.add(getString(R.string.Forward) + " " + getString(R.string.CG_Without_Caption));
        configValues.add(CherrygramChatsConfig.LEFT_BUTTON_FORWARD_WO_CAPTION);

        configStringKeys.add(getString(R.string.Reply));
        configValues.add(CherrygramChatsConfig.LEFT_BUTTON_REPLY);

        configStringKeys.add(getString(R.string.CG_ToSaved));
        configValues.add(CherrygramChatsConfig.LEFT_BUTTON_SAVE_MESSAGE);

        configStringKeys.add(getString(R.string.DirectShare));
        configValues.add(CherrygramChatsConfig.LEFT_BUTTON_DIRECT_SHARE);

        PopupHelper.show(configStringKeys, getString(R.string.CP_LeftBottomButtonAction), configValues.indexOf(CherrygramChatsConfig.INSTANCE.getLeftBottomButton()), chatActivity.getContext(), i -> {
            CherrygramChatsConfig.INSTANCE.setLeftBottomButton(configValues.get(i));

            if (chatActivity.actionsButtonsLayout.getReplyButton() == null) return;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                int[] location = new int[2];
                chatActivity.actionsButtonsLayout.getReplyButton().getLocationOnScreen(location);

                float centerX = location[0] + chatActivity.actionsButtonsLayout.getReplyButton().getWidth() / 2f;
                float centerY = location[1] + chatActivity.actionsButtonsLayout.getReplyButton().getHeight() / 2f;

                LaunchActivity.makeRipple(centerX, centerY, 5);
            }

            chatActivity.actionsButtonsLayout.updateReplyButtonUI(CGResourcesHelper.INSTANCE.getLeftButtonText(noForwards), CGResourcesHelper.INSTANCE.getLeftButtonDrawable(noForwards), CherrygramChatsConfig.INSTANCE.getLeftBottomButton() != CherrygramChatsConfig.LEFT_BUTTON_REPLY);
            chatActivity.actionsButtonsLayout.updateForwardButtonUI(getString(R.string.Forward), R.drawable.input_forward, CherrygramChatsConfig.INSTANCE.getLeftBottomButton() == CherrygramChatsConfig.LEFT_BUTTON_REPLY);

        }, resourcesProvider);
    }

    private void createReplyAction(ChatActivity chatActivity, MessageObject selectedObject) {
        if (selectedObject != null && selectedObject.messageOwner != null && selectedObject.messageOwner.noforwards) {
            return;
        }
        if (selectedObject != null && chatActivity.getCurrentChat() != null && (ChatObject.isNotInChat(chatActivity.getCurrentChat()) && !ChatObject.isMonoForum(chatActivity.getCurrentChat()) && !chatActivity.isThreadChat() || ChatObject.isChannel(chatActivity.getCurrentChat()) && !ChatObject.canPost(chatActivity.getCurrentChat()) && !chatActivity.getCurrentChat().megagroup || !ChatObject.canSendMessages(chatActivity.getCurrentChat()))) {
            MessageObject messageObject = selectedObject;
            if (messageObject.getGroupId() != 0) {
                MessageObject.GroupedMessages group = chatActivity.getGroup(messageObject.getGroupId());
                if (group != null) {
                    messageObject = group.captionMessage;
                }
            }
            chatActivity.replyingMessageObject = messageObject;
            Bundle args = new Bundle();
            args.putBoolean("onlySelect", true);
            args.putInt("dialogsType", DialogsActivity.DIALOGS_TYPE_FORWARD);
            args.putBoolean("quote", true);
            args.putBoolean("reply_to", true);
            final long author = DialogObject.getPeerDialogId(selectedObject.getFromPeer());
            if (author != 0 && author != chatActivity.getDialogId() && author != getUserConfig().getClientUserId() && author > 0) {
                args.putLong("reply_to_author", author);
            }
            args.putInt("messagesCount", 1);
            args.putBoolean("canSelectTopics", true);
            DialogsActivity fragment = new DialogsActivity(args);
            fragment.setDelegate(chatActivity);
            chatActivity.presentFragment(fragment);
        } else {
            chatActivity.showFieldPanelForReply(selectedObject);
        }
    }

    private void createCGSaveMessagesSelected(ChatActivity chatActivity) {
        try {
            long chatID = ChatsHelper2.INSTANCE.getCustomChatID();

            ArrayList<MessageObject> messages = getSelectedMessages(chatActivity);
            forwardMessages(chatActivity, messages, false, true, 0, chatID);
            chatActivity.createUndoView();
            if (chatActivity.getUndoView() == null) {
                return;
            }
            if (!BulletinFactory.of(chatActivity).showForwardedBulletinWithTag(chatID, messages.size())) {
                chatActivity.getUndoView().showWithAction(chatID, UndoView.ACTION_FWD_MESSAGES, messages.size());
            }
        } catch (Exception ignore) {
            chatActivity.clearSelectionMode();
            Toast.makeText(chatActivity.getParentActivity(), getString(R.string.ErrorOccurred), Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<MessageObject> getSelectedMessages(ChatActivity chatActivity) {
        ArrayList<MessageObject> fmessages = new ArrayList<>();

        for (int a = 1; a >= 0; a--) {
            ArrayList<Integer> ids = new ArrayList<>();
            for (int b = 0; b < chatActivity.selectedMessagesIds[a].size(); b++) {
                ids.add(chatActivity.selectedMessagesIds[a].keyAt(b));
            }
            Collections.sort(ids);
            for (int b = 0; b < ids.size(); b++) {
                Integer id = ids.get(b);
                MessageObject messageObject = chatActivity.selectedMessagesIds[a].get(id);
                if (messageObject != null) {
                    fmessages.add(messageObject);
                }
            }
            chatActivity.selectedMessagesCanCopyIds[a].clear();
            chatActivity.selectedMessagesCanStarIds[a].clear();
            chatActivity.selectedMessagesIds[a].clear();
        }

        chatActivity.hideActionMode();
        chatActivity.updatePinnedMessageView(true);
        chatActivity.updateVisibleRows();

        return fmessages;
    }

    // This method is used to forward messages to Saved Messages, or to multi Dialogs
    public void forwardMessages(ChatActivity chatActivity, ArrayList<MessageObject> arrayList, boolean fromMyName, boolean notify, int scheduleDate, long did) {
        if (arrayList == null || arrayList.isEmpty()) {
            return;
        }
        if ((scheduleDate != 0) == (chatActivity.getChatMode() == ChatActivity.MODE_SCHEDULED)) {
            chatActivity.waitingForSendingMessageLoad = true;
        }
        AlertsCreator.showSendMediaAlert(getSendMessagesHelper().sendMessage(arrayList, did == 0 ? chatActivity.getDialogId(): did, fromMyName, false, notify, scheduleDate, 0), chatActivity, chatActivity.getResourceProvider());
    }

    private void createCGShareAlertSelected(ChatActivity chatActivity) {
        if (chatActivity.forwardingMessage == null && chatActivity.selectedMessagesIds[0].size() == 0 && chatActivity.selectedMessagesIds[1].size() == 0) {
            return;
        }
        ArrayList<MessageObject> fmessages = new ArrayList<>();
        if (chatActivity.forwardingMessage != null) {
            if (chatActivity.forwardingMessageGroup != null) {
                fmessages.addAll(chatActivity.forwardingMessageGroup.messages);
            } else {
                fmessages.add(chatActivity.forwardingMessage);
            }
            chatActivity.forwardingMessage = null;
            chatActivity.forwardingMessageGroup = null;
        } else {
            for (int a = 1; a >= 0; a--) {
                ArrayList<Integer> ids = new ArrayList<>();
                for (int b = 0; b < chatActivity.selectedMessagesIds[a].size(); b++) {
                    ids.add(chatActivity.selectedMessagesIds[a].keyAt(b));
                }
                Collections.sort(ids);
                for (int b = 0; b < ids.size(); b++) {
                    MessageObject messageObject = chatActivity.selectedMessagesIds[a].get(ids.get(b));
                    if (messageObject != null) {
                        fmessages.add(messageObject);
                    }
                }
                chatActivity.selectedMessagesCanCopyIds[a].clear();
                chatActivity.selectedMessagesCanStarIds[a].clear();
                chatActivity.selectedMessagesIds[a].clear();
            }
        }
        chatActivity.hideActionMode();
        chatActivity.updatePinnedMessageView(true);
        chatActivity.updateVisibleRows();

        chatActivity.showDialog(new ShareAlert(chatActivity.getContext(), chatActivity, fmessages, null, null, ChatObject.isChannel(chatActivity.getCurrentChat()), null, null, false, false, false, null, chatActivity.getResourceProvider()) {
            @Override
            public void dismissInternal() {
                super.dismissInternal();
                AndroidUtilities.requestAdjustResize(chatActivity.getParentActivity(), chatActivity.getClassGuid());
                if (chatActivity.getChatActivityEnterView().getVisibility() == View.VISIBLE) {
                    chatActivity.fragmentView.requestLayout();
                }
            }

            @Override
            protected void onSend(LongSparseArray<TLRPC.Dialog> dids, int count, TLRPC.TL_forumTopic topic, boolean showToast) {
                chatActivity.createUndoView();
                if (chatActivity.getUndoView() == null || !showToast) {
                    return;
                }
                if (dids.size() == 1) {
                    chatActivity.getUndoView().showWithAction(dids.valueAt(0).id, UndoView.ACTION_FWD_MESSAGES, count, topic, null, null);
                } else {
                    chatActivity.getUndoView().showWithAction(0, UndoView.ACTION_FWD_MESSAGES, count, dids.size(), null, null);
                }
            }
        });
        AndroidUtilities.setAdjustResizeToNothing(chatActivity.getParentActivity(), chatActivity.getClassGuid());
        chatActivity.fragmentView.requestLayout();
    }

    public void showSearchMessageFilterSelector(ChatActivity chatActivity) {
        ArrayList<String> configStringKeys = new ArrayList<>();
        ArrayList<Integer> configValues = new ArrayList<>();

        configStringKeys.add(getString( R.string.CG_SearchFilter_None));
        configValues.add(CherrygramChatsConfig.FILTER_NONE);

        configStringKeys.add(getString(R.string.CG_SearchFilter_Photos));
        configValues.add(CherrygramChatsConfig.FILTER_PHOTOS);

        configStringKeys.add(getString(R.string.CG_SearchFilter_Videos));
        configValues.add(CherrygramChatsConfig.FILTER_VIDEOS);

        configStringKeys.add(getString(R.string.CG_SearchFilter_VoiceMessages));
        configValues.add(CherrygramChatsConfig.FILTER_VOICE_MESSAGES);

        configStringKeys.add(getString(R.string.CG_SearchFilter_VideoMessages));
        configValues.add(CherrygramChatsConfig.FILTER_VIDEO_MESSAGES);

        configStringKeys.add(getString(R.string.CG_SearchFilter_Files));
        configValues.add(CherrygramChatsConfig.FILTER_FILES);

        configStringKeys.add(getString(R.string.CG_SearchFilter_Music));
        configValues.add(CherrygramChatsConfig.FILTER_MUSIC);

        configStringKeys.add(getString(R.string.CG_SearchFilter_GIFs));
        configValues.add(CherrygramChatsConfig.FILTER_GIFS);

        configStringKeys.add(getString(R.string.CG_SearchFilter_Geolocation));
        configValues.add(CherrygramChatsConfig.FILTER_GEO);

        configStringKeys.add(getString(R.string.CG_SearchFilter_Contacts));
        configValues.add(CherrygramChatsConfig.FILTER_CONTACTS);

        configStringKeys.add(getString(R.string.CG_SearchFilter_MyMentions));
        configValues.add(CherrygramChatsConfig.FILTER_MENTIONS);

        PopupHelper.show(configStringKeys, getString(R.string.CG_SearchFilter), configValues.indexOf(CherrygramChatsConfig.INSTANCE.getMessagesSearchFilter()), chatActivity.getContext(), i -> {
            CherrygramChatsConfig.INSTANCE.setMessagesSearchFilter(configValues.get(i));

            chatActivity.openSearchWithText(null);
            chatActivity.showMessagesSearchListView(true);
        }, chatActivity.getResourceProvider());
    }

    private static final CharsetDecoder textDecoder = StandardCharsets.UTF_8.newDecoder();
    public static String getTextFromCallback(byte[] data) {
        try {
            return textDecoder.decode(ByteBuffer.wrap(data)).toString();
        } catch (CharacterCodingException e) {
            return Base64.encodeToString(data, Base64.NO_PADDING | Base64.NO_WRAP);
        }
    }

    public CharSequence getMessageText(MessageObject selectedObject, MessageObject.GroupedMessages selectedObjectGroup) {
        CharSequence messageTextToTranslate = null;
        if (selectedObject.type != MessageObject.TYPE_EMOJIS && selectedObject.type != MessageObject.TYPE_ANIMATED_STICKER && selectedObject.type != MessageObject.TYPE_STICKER) {
            messageTextToTranslate = getMessageCaption(selectedObject, selectedObjectGroup);
            if (messageTextToTranslate == null && selectedObject.isPoll()) {
                try {
                    TLRPC.Poll poll = ((TLRPC.TL_messageMediaPoll) selectedObject.messageOwner.media).poll;
                    StringBuilder pollText = new StringBuilder(poll.question.text).append("\n");
                    for (TLRPC.PollAnswer answer : poll.answers)
                        pollText.append("\n\uD83D\uDD18 ").append(answer.text == null ? "" : answer.text.text);
                    messageTextToTranslate = pollText.toString();
                } catch (Exception e) {
                }
            }
            if (messageTextToTranslate == null && MessageObject.isMediaEmpty(selectedObject.messageOwner)) {
                messageTextToTranslate = getMessageContent(selectedObject);
            }
            if (messageTextToTranslate != null && Emoji.fullyConsistsOfEmojis(messageTextToTranslate)) {
                messageTextToTranslate = null;
            }
        }
        return messageTextToTranslate;
    }

    public CharSequence getMessageCaption(MessageObject messageObject, MessageObject.GroupedMessages group) {
        String restrictionReason = getMessagesController().getRestrictionReason(messageObject.messageOwner.restriction_reason);
        if (!TextUtils.isEmpty(restrictionReason)) {
            return restrictionReason;
        }
        if (messageObject.isVoiceTranscriptionOpen() && !TranscribeButton.isTranscribing(messageObject)) {
            return messageObject.getVoiceTranscription();
        }
        if (messageObject.caption != null) {
            return messageObject.caption;
        }
        if (group == null) {
            return null;
        }
        CharSequence caption = null;
        for (int a = 0, N = group.messages.size(); a < N; a++) {
            MessageObject message = group.messages.get(a);
            if (message.caption != null) {
                if (caption != null) {
                    return null;
                }
                caption = message.caption;
            }
        }
        return caption;
    }

    public CharSequence getMessageContent(MessageObject messageObject) {
        SpannableStringBuilder str = new SpannableStringBuilder();
        String restrictionReason = getMessagesController().getRestrictionReason(messageObject.messageOwner.restriction_reason);
        if (!TextUtils.isEmpty(restrictionReason)) {
            str.append(restrictionReason);
        } else if (messageObject.caption != null) {
            str.append(messageObject.caption);
        } else {
            str.append(messageObject.messageText);
        }
        return str;
    }

    public boolean isTopic(MessageObject messageObject) {
        TLRPC.TL_forumTopic topic = MessagesController.getInstance(currentAccount).getTopicsController().findTopic(
                -messageObject.getDialogId(), MessageObject.getTopicId(currentAccount, messageObject.messageOwner, true)
        );
        return topic != null;
    }

}
