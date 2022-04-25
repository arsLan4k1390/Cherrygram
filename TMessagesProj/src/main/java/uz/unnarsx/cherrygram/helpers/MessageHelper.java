package uz.unnarsx.cherrygram.helpers;

import android.content.Context;
import android.util.SparseArray;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BaseController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;

import java.util.ArrayList;
import java.util.HashSet;

import uz.unnarsx.cherrygram.utils.AlertUtil;
import static uz.unnarsx.cherrygram.utils.UIUtilKt.uDismiss;
import static uz.unnarsx.cherrygram.utils.UIUtilKt.uUpdate;

public class MessageHelper extends BaseController {

    private static SparseArray<MessageHelper> Instance = new SparseArray<>();
    private int lastReqId;

    public MessageHelper(int num) {
        super(num);
    }

    public void resetMessageContent(long dialog_id, MessageObject messageObject) {
        TLRPC.Message message = messageObject.messageOwner;

        MessageObject obj = new MessageObject(currentAccount, message, true, true);

        ArrayList<MessageObject> arrayList = new ArrayList<>();
        arrayList.add(obj);
        getNotificationCenter().postNotificationName(NotificationCenter.replaceMessagesObjects, dialog_id, arrayList, false);
    }

    public void resetMessageContent(long dialog_id, ArrayList<MessageObject> messageObjects) {
        ArrayList<MessageObject> arrayList = new ArrayList<>();
        for (MessageObject messageObject : messageObjects) {
            MessageObject obj = new MessageObject(currentAccount, messageObject.messageOwner, true, true);
            arrayList.add(obj);
        }
        getNotificationCenter().postNotificationName(NotificationCenter.replaceMessagesObjects, dialog_id, arrayList, false);
    }

    public static MessageHelper getInstance(int num) {
        MessageHelper localInstance = Instance.get(num);
        if (localInstance == null) {
            synchronized (MessageHelper.class) {
                localInstance = Instance.get(num);
                if (localInstance == null) {
                    Instance.put(num, localInstance = new MessageHelper(num));

                }
            }
        }
        return localInstance;
    }

    public void deleteUserChannelHistoryWithSearch(Context ctx, final long dialog_id, final TLRPC.User user) {
        AlertDialog progress = null;
        if (ctx != null) {
            progress = AlertUtil.showProgress(ctx);
            progress.show();
        }
        deleteUserChannelHistoryWithSearch(progress, dialog_id, user, 0, 0);
    }

    public void deleteUserChannelHistoryWithSearch(AlertDialog progress, final long dialog_id, final TLRPC.User user, final int offset_id, int index) {
        final TLRPC.TL_messages_search req = new TLRPC.TL_messages_search();
        req.peer = getMessagesController().getInputPeer((int) dialog_id);
        if (req.peer == null) {
            if (progress != null) uDismiss(progress);
            return;
        }
        req.limit = 100;
        req.q = "";
        req.offset_id = offset_id;
        if (user != null) {
            req.from_id = MessagesController.getInputPeer(user);
            req.flags |= 1;
        }
        req.filter = new TLRPC.TL_inputMessagesFilterEmpty();
        getConnectionsManager().sendRequest(req, (response, error) -> {
            if (error == null) {
                int lastMessageId = offset_id;
                TLRPC.messages_Messages res = (TLRPC.messages_Messages) response;
                ArrayList<Integer> ids = new ArrayList<>();
                ArrayList<Long> random_ids = new ArrayList<>();
                long channelId = 0;
                int indey = index;
                for (int a = 0; a < res.messages.size(); a++) {
                    TLRPC.Message message = res.messages.get(a);
                    if (!message.out || message instanceof TLRPC.TL_messageService) {
                        continue;
                    }
                    ids.add(message.id);
                    if (message.random_id != 0) {
                        random_ids.add(message.random_id);
                    }
                    if (message.peer_id.channel_id != 0) {
                        channelId = message.peer_id.channel_id;
                    }
                    if (message.id > lastMessageId) {
                        lastMessageId = message.id;
                    }
                    indey++;
                }
                if (ids.size() == 0) {
                    if (progress != null) uDismiss(progress);
                    return;
                }
                AndroidUtilities.runOnUIThread(() -> getMessagesController().deleteMessages(ids, random_ids, null, dialog_id, true, false));
                if (progress != null) uUpdate(progress, ">> " + indey);
                deleteUserChannelHistoryWithSearch(progress, dialog_id, user, lastMessageId, indey);
            } else {
                if (progress != null) uDismiss(progress);
                AlertUtil.showToast(error);
            }
        }, ConnectionsManager.RequestFlagFailOnServerErrors);
    }

    public void deleteChannelHistory(final long dialog_id, TLRPC.Chat chat, final int offset_id) {

        final TLRPC.TL_messages_getHistory req = new TLRPC.TL_messages_getHistory();
        req.peer = getMessagesController().getInputPeer((int) dialog_id);
        if (req.peer == null) {
            return;
        }
        req.limit = 100;
        req.offset_id = offset_id;
        final int currentReqId = ++lastReqId;
        getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (error == null) {
                int lastMessageId = offset_id;
                if (currentReqId == lastReqId) {
                    if (response != null) {
                        TLRPC.messages_Messages res = (TLRPC.messages_Messages) response;
                        int size = res.messages.size();
                        if (size == 0) {
                            return;
                        }
                        /*
                        ArrayList<Integer> ids = new ArrayList<>();
                        ArrayList<Long> random_ids = new ArrayList<>();
                        int channelId = 0;
                        for (int a = 0; a < res.messages.size(); a++) {
                            TLRPC.Message message = res.messages.get(a);
                            ids.add(message.id);
                            if (message.random_id != 0) {
                                random_ids.add(message.random_id);
                            }
                            if (message.to_id.channel_id != 0) {
                                channelId = message.to_id.channel_id;
                            }
                            if (message.id > lastMessageId) {
                                lastMessageId = message.id;
                            }
                        }
                        getMessagesController().deleteMessages(ids, random_ids, null, dialog_id, channelId, true, false);
                         */
                        HashSet<Long> ids = new HashSet<>();
                        ArrayList<Integer> msgIds = new ArrayList<>();
                        ArrayList<Long> random_ids = new ArrayList<>();
                        for (int a = 0; a < res.messages.size(); a++) {
                            TLRPC.Message message = res.messages.get(a);
//                            ids.add(message.id);
                            msgIds.add(message.id);
                            if (message.from_id.user_id > 0) {
                                ids.add(message.peer_id.user_id);
                            } else {
                                msgIds.add(message.id);
                                if (message.random_id != 0) {
                                    random_ids.add(message.random_id);
                                }
                            }
                            if (message.id > lastMessageId) {
                                lastMessageId = message.id;
                            }
                        }
                        for (long userId : ids) {
                            deleteUserChannelHistory(chat, userId, 0);
                        }
                        if (!msgIds.isEmpty()) {
                            getMessagesController().deleteMessages(msgIds, random_ids, null, dialog_id, true, false);
                        }
                        deleteChannelHistory(dialog_id, chat, lastMessageId);

                    }
                }
            } else {
                AlertUtil.showToast(error.code + ": " + error.text);
            }
        }), ConnectionsManager.RequestFlagFailOnServerErrors);
    }

    public void deleteUserChannelHistory(final TLRPC.Chat chat, long userId, int offset) {
        if (offset == 0) {
            getMessagesStorage().deleteUserChatHistory(chat.id, userId);
        }
        TLRPC.TL_channels_deleteParticipantHistory req = new TLRPC.TL_channels_deleteParticipantHistory();
        req.channel = getMessagesController().getInputChannel(chat.id);
        req.participant = getMessagesController().getInputPeer(userId);
        getConnectionsManager().sendRequest(req, (response, error) -> {
            if (error == null) {
                TLRPC.TL_messages_affectedHistory res = (TLRPC.TL_messages_affectedHistory) response;
                if (res.offset > 0) {
                    deleteUserChannelHistory(chat, userId, res.offset);
                }
                getMessagesController().processNewChannelDifferenceParams(res.pts, res.pts_count, chat.id);
            }
        });
    }

}
