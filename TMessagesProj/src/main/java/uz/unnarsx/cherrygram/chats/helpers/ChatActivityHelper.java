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

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.Toast;

import org.telegram.messenger.BaseController;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.AccountFrozenAlert;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.Cells.ChatMessageCell;
import org.telegram.ui.ChannelAdminLogActivity;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.ChatReactionsEditActivity;
import org.telegram.ui.ChatUsersActivity;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.EmojiPacksAlert;
import org.telegram.ui.Components.Reactions.ChatCustomReactionsEditActivity;
import org.telegram.ui.Components.Reactions.ReactionsLayoutInBubble;
import org.telegram.ui.Components.TranslateAlert2;
import org.telegram.ui.Components.UndoView;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.StatisticActivity;
import org.telegram.ui.web.SearchEngine;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import uz.unnarsx.cherrygram.chats.JsonBottomSheet;
import uz.unnarsx.cherrygram.chats.gemini.GeminiResultsBottomSheet;
import uz.unnarsx.cherrygram.chats.gemini.GeminiSDKImplementation;
import uz.unnarsx.cherrygram.core.CGBiometricPrompt;
import uz.unnarsx.cherrygram.core.CGFeatureHooks;
import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig;
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.core.helpers.backup.BackupHelper;
import uz.unnarsx.cherrygram.helpers.network.StickersManager;
import uz.unnarsx.cherrygram.preferences.CherrygramPreferencesNavigator;

public class ChatActivityHelper extends BaseController {

    private static final ChatActivityHelper[] Instance = new ChatActivityHelper[UserConfig.MAX_ACCOUNT_COUNT];

    public ChatActivityHelper(int num) {
        super(num);
    }

    public static ChatActivityHelper getInstance(int num) {
        ChatActivityHelper localInstance = Instance[num];
        if (localInstance == null) {
            synchronized (ChatActivityHelper.class) {
                localInstance = Instance[num];
                if (localInstance == null) {
                    Instance[num] = localInstance = new ChatActivityHelper(num);
                }
            }
        }
        return localInstance;
    }

    /** Cherrygram chat options constant id's start */
    public final static int OPTION_JUMP_TO_BEGINNING = 2000;
    public final static int OPTION_DELETE_ALL_FROM_SELF = 2001;
    public final static int OPTION_UPGRADE_GROUP = 2002;
    public final static int OPTION_TEXT_MENTION = 2003;
    public final static int OPTION_SELECT_BETWEEN = 2004;
    public final static int OPTION_SAVE_MESSAGE_CHAT = 2005;
    public final static int OPTION_FOR_ADMINS_REACTIONS = 2006;
    public final static int OPTION_FOR_ADMINS_PERMISSIONS = 2007;
    public final static int OPTION_FOR_ADMINS_ADMINISTRATORS = 2008;
    public final static int OPTION_FOR_ADMINS_MEMBERS = 2009;
    public final static int OPTION_FOR_ADMINS_STATISTICS = 2010;
    public final static int OPTION_FOR_ADMINS_RECENT_ACTIONS = 2011;
    public final static int OPTION_COPY_PHOTO = 2012;
    public final static int OPTION_COPY_PHOTO_AS_STICKER = 2013;
    public final static int OPTION_CLEAR_FROM_CACHE = 2014;
    public final static int OPTION_VIEW_HISTORY = 2015;
    public final static int OPTION_DOWNLOAD_STICKER = 2016;
    public final static int OPTION_FORWARD_WO_AUTHOR = 2017;
    public final static int OPTION_FORWARD_WO_CAPTION = 2018;
    public final static int OPTION_GET_CUSTOM_REACTIONS = 2019;
    public final static int OPTION_IMPORT_SETTINGS = 2020;
    public final static int OPTION_DETAILS = 2021;
    public final static int OPTION_TRANSLATE_DOUBLE_TAP = 2022;
    public final static int OPTION_TEXT_CODE = 2023;
    public final static int OPTION_GO_TO_SAVED = 2024;
    public final static int OPTION_ASK_PASSCODE = 2025;
    public final static int OPTION_DO_NOT_ASK_PASSCODE = 2026;
    public final static int OPTION_OPEN_TELEGRAM_BROWSER = 2027;
    public final static int OPTION_REPLY_GEMINI = 2028;
    public final static int OPTION_TRANSLATE_GEMINI = 2029;
    public final static int OPTION_TRANSCRIBE_GEMINI = 2030;
    public final static int OPTION_EXPLANATION_GEMINI = 2031;
    public final static int OPTION_SUMMARIZE_GEMINI = 2032;
    public final static int OPTION_ADVANCED_SEARCH = 2033;

    public final static int OPTION_VIEW_EDITED_MESSAGE_HISTORY = 2100;
    public final static int OPTION_MARK_TTL_AS_READ = 2101;
    public final static int OPTION_MARK_MESSAGE_AS_READ = 2102;
    /** Cherrygram chat options constant id's finish */

    /** ActionBar options start*/
    public void checkActionBarOptions(
            int id,
            ChatActivity chatActivity, ActionBarMenuItem headerItem,
            ArrayList<MessageObject> messages,
            SparseArray<MessageObject>[] selectedMessagesIds,
            long mergeDialogId, int editTextStart, int editTextEnd,
            TLRPC.TL_forumTopic forumTopic, TLRPC.Chat currentChat
    ) {
        if (id == OPTION_ADVANCED_SEARCH) {
            chatActivity.createSearchWithIDAlert();
        } else if (id == OPTION_JUMP_TO_BEGINNING) {
            chatActivity.jumpToDate(2);
        } else if (id == OPTION_DELETE_ALL_FROM_SELF) {
            chatActivity.getMessageHelper().createDeleteHistoryAlert(chatActivity, currentChat, forumTopic, mergeDialogId, chatActivity.getResourceProvider());
        } else if (id == OPTION_UPGRADE_GROUP) {
            AlertDialog.Builder builder = new AlertDialog.Builder(chatActivity.getParentActivity());
            builder.setMessage(getString(R.string.ConvertGroupAlert));
            builder.setTitle(getString(R.string.Warning));
            builder.setPositiveButton(getString(R.string.OK), (dialogInterface, i) -> getMessagesController().convertToMegaGroup(chatActivity.getParentActivity(), currentChat.id, chatActivity, chatNew -> {
                if (chatNew != 0) {
                    getMessagesController().toggleChannelInvitesHistory(chatNew, false);
                }
            }));
            builder.setNegativeButton(getString(R.string.Cancel), null);
            chatActivity.showDialog(builder.create());
        } else if (id == OPTION_TEXT_MENTION) {
            if (chatActivity.getChatActivityEnterView() != null && chatActivity.getChatActivityEnterView().getEditField() != null) {
                chatActivity.getChatActivityEnterView().getEditField().setSelectionOverride(editTextStart, editTextEnd);
                chatActivity.getChatActivityEnterView().getEditField().makeSelectedMention();
            }
        } else if (id == OPTION_SELECT_BETWEEN) {
            // For selecting messages between the first and the last.
            ArrayList<Integer> ids = new ArrayList<>();
            for (int a = 1; a >= 0; a--) {
                for (int b = 0; b < selectedMessagesIds[a].size(); b++) {
                    ids.add(selectedMessagesIds[a].keyAt(b));
                }
            }
            Collections.sort(ids);
            Integer begin = ids.get(0);
            Integer end = ids.get(ids.size() - 1);
            for (int i = 0; i < messages.size(); i++) {
                int msgId = messages.get(i).getId();
                if (msgId > begin && msgId < end && !(selectedMessagesIds[0].indexOfKey(msgId) >= 0)) {
                    chatActivity.addToSelectedMessages(messages.get(i), false);
                    chatActivity.updateActionModeTitle();
                    chatActivity.updateVisibleRows();
                }
            }
        } else if (id == OPTION_FOR_ADMINS_REACTIONS) {
            TLRPC.ChatFull info = MessagesStorage.getInstance(currentAccount).loadChatInfo(currentChat.id, ChatObject.isChannel(currentChat), new CountDownLatch(1), false, false);

            if (info == null) return;
            if (ChatObject.isChannelAndNotMegaGroup(currentChat)) {
                chatActivity.presentFragment(new ChatCustomReactionsEditActivity(currentChat.id, info));
            } else {
                Bundle args = new Bundle();
                args.putLong(ChatReactionsEditActivity.KEY_CHAT_ID, currentChat.id);
                ChatReactionsEditActivity reactionsEditActivity = new ChatReactionsEditActivity(args);
                reactionsEditActivity.setInfo(info);
                chatActivity.presentFragment(reactionsEditActivity);
            }
        } else if (id == OPTION_FOR_ADMINS_PERMISSIONS) {
            Bundle args = new Bundle();
            args.putLong("chat_id", currentChat.id);
            args.putInt("type", !(ChatObject.isChannel(currentChat) && !currentChat.megagroup) && !currentChat.gigagroup ? ChatUsersActivity.TYPE_KICKED : ChatUsersActivity.TYPE_BANNED);
            ChatUsersActivity fragment = new ChatUsersActivity(args);
            fragment.setInfo(getMessagesController().getChatFull(currentChat.id));
            chatActivity.presentFragment(fragment);
        } else if (id == OPTION_FOR_ADMINS_ADMINISTRATORS) {
            Bundle args = new Bundle();
            args.putLong("chat_id", currentChat.id);
            args.putInt("type", ChatUsersActivity.TYPE_ADMIN);
            ChatUsersActivity fragment = new ChatUsersActivity(args);
            fragment.setInfo(getMessagesController().getChatFull(currentChat.id));
            chatActivity.presentFragment(fragment);
        } else if (id == OPTION_FOR_ADMINS_MEMBERS) {
            Bundle args = new Bundle();
            args.putLong("chat_id", currentChat.id);
            args.putInt("type", ChatUsersActivity.TYPE_USERS);
            ChatUsersActivity fragment = new ChatUsersActivity(args);
            fragment.setInfo(getMessagesController().getChatFull(currentChat.id));
            chatActivity.presentFragment(fragment);
        } else if (id == OPTION_FOR_ADMINS_STATISTICS) {
            chatActivity.presentFragment(StatisticActivity.create(currentChat, false));
        } else if (id == OPTION_FOR_ADMINS_RECENT_ACTIONS) {
            chatActivity.presentFragment(new ChannelAdminLogActivity(currentChat));
        } else if (id == OPTION_TEXT_CODE) {
            if (chatActivity.getChatActivityEnterView() != null && chatActivity.getChatActivityEnterView().getEditField() != null) {
                chatActivity.getChatActivityEnterView().getEditField().setSelectionOverride(editTextStart, editTextEnd);
                chatActivity.getChatActivityEnterView().getEditField().makeSelectedCode();
            }
        } else if (id == OPTION_GO_TO_SAVED) {
            chatActivity.presentFragment(ChatActivity.of(ChatsHelper2.INSTANCE.getCustomChatID()));
        } else if (id == OPTION_ASK_PASSCODE) {
            CGBiometricPrompt.prompt(chatActivity.getParentActivity(), () -> {
                List<String> arr = chatActivity.getChatsPasswordHelper().getArrayList(chatActivity.getChatsPasswordHelper().getPasscodeArray());
                String dialogIdStr = String.valueOf(chatActivity.getDialogId());

                if (
                        (DialogObject.isUserDialog(chatActivity.getDialogId()) || DialogObject.isChatDialog(chatActivity.getDialogId()))
                                && !arr.contains(dialogIdStr)
                ) {
                    arr.add(dialogIdStr);
                    headerItem.hideSubItem(OPTION_ASK_PASSCODE);

                    if (CherrygramCoreConfig.INSTANCE.isDevBuild()) {
                        FileLog.d("new locked chats array: " + arr);
                    }

                    chatActivity.getChatsPasswordHelper().saveArrayList(new ArrayList<>(arr), chatActivity.getChatsPasswordHelper().getPasscodeArray());
                }
            });
        } else if (id == OPTION_DO_NOT_ASK_PASSCODE) {
            CGBiometricPrompt.prompt(chatActivity.getParentActivity(), () -> {
                List<String> arr = chatActivity.getChatsPasswordHelper().getArrayList(chatActivity.getChatsPasswordHelper().getPasscodeArray());
                if (DialogObject.isUserDialog(chatActivity.getDialogId()) || DialogObject.isChatDialog(chatActivity.getDialogId())) {
                    if (arr.remove(String.valueOf(chatActivity.getDialogId()))) { // Удаляем и проверяем, изменился ли список
                        headerItem.hideSubItem(OPTION_DO_NOT_ASK_PASSCODE);

                        if (CherrygramCoreConfig.INSTANCE.isDevBuild()) {
                            FileLog.d("new locked chats array: " + arr);
                        }

                        chatActivity.getChatsPasswordHelper().saveArrayList(new ArrayList<>(arr), chatActivity.getChatsPasswordHelper().getPasscodeArray());
                    }
                }
            });
        } else if (id == OPTION_OPEN_TELEGRAM_BROWSER) {
            Browser.openInTelegramBrowser(chatActivity.getParentActivity(), SearchEngine.getCurrent().getSearchURL(""), null);
        }
    }
    /** ActionBar options finish*/

    /** Message menu options start*/
    public void checkProcessSelectedOption(
            int option,
            ChatActivity chatActivity,
            MessageObject selectedObject, MessageObject.GroupedMessages selectedObjectGroup,
            long threadMessageId,
            TLRPC.Chat currentChat
    ) {
        switch (option) {
            case OPTION_FORWARD_WO_AUTHOR: {
                if (getMessagesController().isFrozen()) {
                    AccountFrozenAlert.show(currentAccount);
                    /*selectedObject = null;
                    selectedObjectToEditCaption = null;
                    selectedObjectGroup = null;*/
                    return;
                }
                CGFeatureHooks.INSTANCE.switchNoAuthor(true);
                CGFeatureHooks.INSTANCE.switchNoCaptions(false);
                chatActivity.forwardingMessage = selectedObject;
                chatActivity.forwardingMessageGroup = selectedObjectGroup;
                Bundle args = new Bundle();
                args.putBoolean("onlySelect", true);
                args.putInt("dialogsType", DialogsActivity.DIALOGS_TYPE_FORWARD);
                args.putInt("messagesCount", 1);
                args.putInt("hasPoll", chatActivity.forwardingMessage.isPoll() ? (chatActivity.forwardingMessage.isPublicPoll() ? 2 : 1) : 0);
                args.putBoolean("hasInvoice", chatActivity.forwardingMessage.isInvoice());
                args.putBoolean("canSelectTopics", true);
                DialogsActivity fragment = new DialogsActivity(args);
                fragment.setDelegate(chatActivity);
                chatActivity.presentFragment(fragment);
                break;
            }
            case OPTION_FORWARD_WO_CAPTION: {
                if (getMessagesController().isFrozen()) {
                    AccountFrozenAlert.show(currentAccount);
                    /*selectedObject = null;
                    selectedObjectToEditCaption = null;
                    selectedObjectGroup = null;*/
                    return;
                }
                CGFeatureHooks.INSTANCE.switchNoAuthor(true);
                CGFeatureHooks.INSTANCE.switchNoCaptions(true);
                chatActivity.forwardingMessage = selectedObject;
                chatActivity.forwardingMessageGroup = selectedObjectGroup;
                Bundle args = new Bundle();
                args.putBoolean("onlySelect", true);
                args.putInt("dialogsType", DialogsActivity.DIALOGS_TYPE_FORWARD);
                args.putInt("messagesCount", 1);
                args.putInt("hasPoll", chatActivity.forwardingMessage.isPoll() ? (chatActivity.forwardingMessage.isPublicPoll() ? 2 : 1) : 0);
                args.putBoolean("hasInvoice", chatActivity.forwardingMessage.isInvoice());
                args.putBoolean("canSelectTopics", true);
                DialogsActivity fragment = new DialogsActivity(args);
                fragment.setDelegate(chatActivity);
                chatActivity.presentFragment(fragment);
                break;
            }
            case OPTION_COPY_PHOTO: {
                chatActivity.getChatsHelper().addMessageToClipboard(selectedObject, () -> BulletinFactory.global()
                        .createSuccessBulletin(getString(R.string.CG_PhotoCopied), chatActivity.getResourceProvider())
                        .setDuration(Bulletin.DURATION_SHORT)
                        .show());
                break;
            }
            case OPTION_COPY_PHOTO_AS_STICKER: {
                StickersManager.INSTANCE.addMessageToClipboardAsSticker(selectedObject, () -> BulletinFactory.global()
                        .createSuccessBulletin(getString(R.string.CG_PhotoCopied), chatActivity.getResourceProvider())
                        .setDuration(Bulletin.DURATION_SHORT)
                        .show());
                break;
            }
            case OPTION_CLEAR_FROM_CACHE: {
                if (Build.VERSION.SDK_INT >= 23 && (Build.VERSION.SDK_INT <= 28 || BuildVars.NO_SCOPED_STORAGE) && chatActivity.getParentActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    chatActivity.getParentActivity().requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 4);
                    /*selectedObject = null;
                    selectedObjectGroup = null;
                    selectedObjectToEditCaption = null;*/
                    return;
                }
                ChatMessageCell messageCell = null;
                int count = chatActivity.getChatListView().getChildCount();
                for (int a = 0; a < count; a++) {
                    View child = chatActivity.getChatListView().getChildAt(a);
                    if (child instanceof ChatMessageCell cell) {
                        if (cell.getMessageObject() == selectedObject) {
                            messageCell = cell;
                            break;
                        }
                    }
                }
                String path = selectedObject.messageOwner.attachPath;
                if (path != null && !path.isEmpty()) {
                    File temp = new File(path);
                    if (!temp.exists()) {
                        path = null;
                    }
                }
                if (path == null || path.isEmpty()) {
                    path = getFileLoader().getPathToMessage(selectedObject.messageOwner).toString();
                }
                File file = new File(path);
                try {
                    file.delete();
                    selectedObject.mediaExists = false;
                    if (messageCell != null) {
                        messageCell.updateButtonState(false, true, false);
                    }
                    chatActivity.createUndoView();
                    if (chatActivity.getUndoView() == null) {
                        return;
                    }
                    chatActivity.getUndoView().setInfoText(LocaleController.formatString(R.string.CG_ClearedFromCache));
                    chatActivity.getUndoView().showWithAction(0, UndoView.ACTION_CACHE_WAS_CLEARED, null, null);
                } catch (Exception ignore) {
                    Toast.makeText(chatActivity.getParentActivity(), "Looks like something went wrong.", Toast.LENGTH_SHORT).show();
                    file.deleteOnExit();
                }
                if (messageCell != null) {
                    messageCell.updateButtonState(false, true, false);
                }
                break;
            }
            case OPTION_REPLY_GEMINI: {
                if (selectedObject == null && selectedObject.messageOwner == null && selectedObject.messageOwner.message == null) {
                    return;
                }

                chatActivity.showFieldPanelForReply(selectedObject);

                GeminiResultsBottomSheet.setMessageObject(selectedObject);
                GeminiResultsBottomSheet.setCurrentChat(currentChat);
                chatActivity.processGeminiWithText(selectedObject, null, false, false);
                break;
            }
            case OPTION_TRANSLATE_GEMINI: {
                if (selectedObject == null && selectedObject.messageOwner == null && selectedObject.messageOwner.message == null) {
                    return;
                }

                GeminiResultsBottomSheet.setMessageObject(selectedObject);
                GeminiResultsBottomSheet.setCurrentChat(currentChat);
                chatActivity.processGeminiWithText(selectedObject, null, true, false);

                break;
            }
            case OPTION_SUMMARIZE_GEMINI: {
                if (selectedObject == null && selectedObject.messageOwner == null && selectedObject.messageOwner.message == null) {
                    return;
                }

                GeminiResultsBottomSheet.setMessageObject(selectedObject);
                GeminiResultsBottomSheet.setCurrentChat(currentChat);
                chatActivity.processGeminiWithText(selectedObject, null, false, true);

                break;
            }
            case OPTION_TRANSCRIBE_GEMINI: {
                if (selectedObject == null && selectedObject.messageOwner == null) {
                    return;
                }

                GeminiResultsBottomSheet.setMessageObject(selectedObject);
                GeminiResultsBottomSheet.setCurrentChat(currentChat);
                GeminiSDKImplementation.injectGeminiForMedia(
                        chatActivity,
                        chatActivity,
                        selectedObject,
                        false,
                        true,
                        false
                );

                break;
            }
            case OPTION_EXPLANATION_GEMINI: {
                if (selectedObject == null && selectedObject.messageOwner == null) {
                    return;
                }

                GeminiResultsBottomSheet.setMessageObject(selectedObject);
                GeminiResultsBottomSheet.setCurrentChat(currentChat);
                GeminiSDKImplementation.injectGeminiForMedia(
                        chatActivity,
                        chatActivity,
                        selectedObject,
                        false,
                        false,
                        false
                );

                break;
            }
            case OPTION_IMPORT_SETTINGS: {
                File locFile = null;
                if (!TextUtils.isEmpty(selectedObject.messageOwner.attachPath)) {
                    File f = new File(selectedObject.messageOwner.attachPath);
                    if (f.exists()) {
                        locFile = f;
                    }
                }
                if (locFile == null) {
                    File f = getFileLoader().getPathToMessage(selectedObject.messageOwner);
                    if (f.exists()) {
                        locFile = f;
                    }
                }
                if (locFile != null) {
                    BackupHelper.INSTANCE.importSettings(locFile, chatActivity.getContext());
                }
                break;
            }
            case OPTION_DETAILS: {
                if (selectedObject != null) {
                    JsonBottomSheet.getMessageId(selectedObject);
                    JsonBottomSheet.showAlert(chatActivity.getContext(), chatActivity.getResourceProvider(), chatActivity, selectedObject, currentChat);
                }
                break;
            }
            case OPTION_GET_CUSTOM_REACTIONS: {
                ArrayList<ReactionsLayoutInBubble.VisibleReaction> visibleCustomReactions = new ArrayList<>(selectedObject.getCustomReactions());
                ArrayList<TLRPC.InputStickerSet> customEmojiStickerSets = new ArrayList<>();

                if (selectedObject != null && selectedObject.messageOwner != null && selectedObject.messageOwner.reactions != null) {
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
                        EmojiPacksAlert alert = new EmojiPacksAlert(chatActivity, chatActivity.getParentActivity(), chatActivity.getResourceProvider(), stickerSets) {
                            @Override
                            public void dismiss() {
                                super.dismiss();
                                chatActivity.dimBehindView(false);
                            }
                        };
                        alert.setCalcMandatoryInsets(chatActivity.isKeyboardVisible());
                        alert.setDimBehind(false);
                        chatActivity.closeMenu(false);
                        chatActivity.showDialog(alert);
                    }
                }
                break;
            }
            case OPTION_VIEW_HISTORY: {
                TLRPC.Peer peer = selectedObject.messageOwner.from_id;
                if ((threadMessageId == 0 || chatActivity.isTopic) && !UserObject.isReplyUser(chatActivity.getCurrentUser())) {
                    chatActivity.openSearchWithText("");
                } else {
                    chatActivity.searchItem.openSearch(false);
                }
                if (peer.user_id != 0) {
                    TLRPC.User user = getMessagesController().getUser(peer.user_id);
                    chatActivity.searchUserMessages(user, null);
                } else if (peer.chat_id != 0) {
                    TLRPC.Chat chat = getMessagesController().getChat(peer.chat_id);
                    chatActivity.searchUserMessages(null, chat);
                } else if (peer.channel_id != 0) {
                    TLRPC.Chat chat = getMessagesController().getChat(peer.channel_id);
                    chatActivity.searchUserMessages(null, chat);
                }
                if (chatActivity.searchFilterButton != null && chatActivity.searchFilterButton.getVisibility() == View.VISIBLE) chatActivity.searchFilterButton.setVisibility(View.INVISIBLE);
                chatActivity.showMessagesSearchListView(true);
                break;
            }
            case OPTION_DOWNLOAD_STICKER: {
                if (Build.VERSION.SDK_INT >= 23 && (Build.VERSION.SDK_INT <= 28 || BuildVars.NO_SCOPED_STORAGE) && chatActivity.getParentActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    chatActivity.getParentActivity().requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 4);
                    /*selectedObject = null;
                    selectedObjectGroup = null;
                    selectedObjectToEditCaption = null;*/
                    return;
                }
                chatActivity.getChatsHelper().saveStickerToGallery(chatActivity.getParentActivity(), selectedObject.getDocument(), (uri) -> {
                    if (BulletinFactory.canShowBulletin(chatActivity)) {
                        BulletinFactory.of(chatActivity).createDownloadBulletin(BulletinFactory.FileType.STICKER, chatActivity.getResourceProvider()).show();
                    }
                });
                break;
            }
            case OPTION_SAVE_MESSAGE_CHAT: {
                try {
                    long chatID = ChatsHelper2.INSTANCE.getCustomChatID();

                    ArrayList<MessageObject> messages = new ArrayList<>();
                    if (selectedObjectGroup != null) {
                        messages.addAll(selectedObjectGroup.messages);
                    } else {
                        messages.add(selectedObject);
                    }
                    chatActivity.getChatsHelper().forwardMessages(chatActivity, messages, false, true, 0, chatID);
                    chatActivity.createUndoView();
                    if (chatActivity.getUndoView() == null) {
                        return;
                    }
                    if (!BulletinFactory.of(chatActivity).showForwardedBulletinWithTag(chatID, messages.size())) {
                        chatActivity.getUndoView().showWithAction(chatID, UndoView.ACTION_FWD_MESSAGES, messages.size());
                    }
                } catch (Exception ignore) {
                    Toast.makeText(chatActivity.getParentActivity(), getString(R.string.ErrorOccurred), Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case OPTION_TRANSLATE_DOUBLE_TAP: {
                String fromLang = selectedObject.messageOwner.originalLanguage;
                String toLang = TranslateAlert2.getToLanguage();

                boolean noforwards = getMessagesController().isChatNoForwards(currentChat) || selectedObject.messageOwner.noforwards || chatActivity.getDialogId() == UserObject.VERIFY;
                boolean noforwardsOrPaidMedia = noforwards || selectedObject.type == MessageObject.TYPE_PAID_MEDIA;

                ArrayList<TLRPC.MessageEntity> entities = selectedObject != null && selectedObject.messageOwner != null ? selectedObject.messageOwner.entities : null;
                TranslateAlert2.showAlert(
                        chatActivity.getContext(),
                        chatActivity,
                        currentAccount,
                        fromLang,
                        toLang,
                        ChatsHelper.getInstance(currentAccount).getMessageText(selectedObject, selectedObjectGroup),
                        entities,
                        noforwardsOrPaidMedia,
                        null,
                        () -> chatActivity.dimBehindView(false)
                );
                chatActivity.dimBehindView(true);
                break;
            }
        }

    }
    /** Message menu options finish*/

    /** Cherrygram chat functions start */
    public void checkDoubleTapOptions(ChatActivity chatActivity) {
        switch (CherrygramChatsConfig.INSTANCE.getDoubleTapAction()) {
            case CherrygramChatsConfig.DOUBLE_TAP_ACTION_TRANSLATE:
                chatActivity.processSelectedOption(OPTION_TRANSLATE_DOUBLE_TAP);
                break;
            case CherrygramChatsConfig.DOUBLE_TAP_ACTION_TRANSLATE_GEMINI:
                chatActivity.processSelectedOption(OPTION_TRANSLATE_GEMINI);
                break;
            case CherrygramChatsConfig.DOUBLE_TAP_ACTION_REPLY:
                chatActivity.processSelectedOption(ChatActivity.OPTION_REPLY);
                break;
            case CherrygramChatsConfig.DOUBLE_TAP_ACTION_SAVE:
                chatActivity.processSelectedOption(OPTION_SAVE_MESSAGE_CHAT);
                break;
            case CherrygramChatsConfig.DOUBLE_TAP_ACTION_EDIT:
                chatActivity.processSelectedOption(ChatActivity.OPTION_EDIT);
                break;
        }
    }
    /** Cherrygram chat functions finish */

}