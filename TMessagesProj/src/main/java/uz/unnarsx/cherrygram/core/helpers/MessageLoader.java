/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.helpers;

import android.net.Uri;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.List;

public class MessageLoader {

    public interface Callback {
        void onLoaded(MessageObject message);
        void onError(String error);
    }

    public static void loadMessageByLink(int currentAccount, String link, Callback callback) {
        if (link == null || link.isEmpty()) {
            callback.onError("Empty link");
            return;
        }

        try {
            Uri uri = Uri.parse(link);
            List<String> segments = uri.getPathSegments();

            if (segments.size() < 2) {
                callback.onError("Invalid link format");
                return;
            }

            if (!"c".equals(segments.get(0))) {
                String username = segments.get(0);
                int messageId = Integer.parseInt(segments.get(1));

                TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
                req.username = username;

                ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> {
                    if (error != null || response == null) {
                        callback.onError("Failed to resolve username");
                        return;
                    }

                    TLRPC.TL_contacts_resolvedPeer res = (TLRPC.TL_contacts_resolvedPeer) response;

                    if (res.chats.isEmpty()) {
                        callback.onError("Chat not found");
                        return;
                    }

                    TLRPC.Chat chat = res.chats.get(0);
                    MessagesController.getInstance(currentAccount).putChat(chat, false);

                    loadMessageInternal(currentAccount, chat, messageId, callback);
                });

            } else {
                long channelId = Long.parseLong(segments.get(1));
                int messageId = Integer.parseInt(segments.get(2));

                long realChannelId = -1000000000000L + channelId;

                TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(realChannelId);

                if (chat == null) {
                    callback.onError("Chat not in cache (open it once)");
                    return;
                }

                loadMessageInternal(currentAccount, chat, messageId, callback);
            }

        } catch (Exception e) {
            callback.onError("Parse error: " + e.getMessage());
        }
    }

    private static void loadMessageInternal(int currentAccount, TLRPC.Chat chat, int messageId, Callback callback) {
        TLRPC.InputChannel inputChannel = MessagesController.getInputChannel(chat);

        if (inputChannel == null) {
            callback.onError("Invalid channel");
            return;
        }

        TLRPC.TL_channels_getMessages req = new TLRPC.TL_channels_getMessages();
        req.channel = inputChannel;
        req.id = new ArrayList<>();
        req.id.add(messageId);

        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> {
            if (error != null || response == null) {
                callback.onError("Failed to load message");
                return;
            }

            TLRPC.messages_Messages res = (TLRPC.messages_Messages) response;

            if (res.messages.isEmpty()) {
                callback.onError("Message not found");
                return;
            }

            TLRPC.Message message = res.messages.get(0);
            MessageObject messageObject = new MessageObject(currentAccount, message, false, false);

            AndroidUtilities.runOnUIThread(() -> callback.onLoaded(messageObject));
        });
    }

}