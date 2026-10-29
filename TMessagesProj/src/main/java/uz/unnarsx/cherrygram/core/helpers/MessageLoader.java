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
import android.text.TextUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageLoader {

    public interface Callback {
        void onLoaded(MessageObject message);
        void onError(String error);
    }

    private static class ParsedLink {
        String username;
        long channelId;
        int messageId;
    }

    private static final Object lock = new Object();
    private static final HashMap<String, ArrayList<Callback>> pending = new HashMap<>();
    private static final HashMap<String, MessageObject> loaded = new HashMap<>();

    public static void loadMessageByLink(int currentAccount, String link, Callback callback) {
        String key = currentAccount + ":" + link;

        synchronized (lock) {
            MessageObject cached = loaded.get(key);
            if (cached != null) {
                AndroidUtilities.runOnUIThread(() -> callback.onLoaded(cached));
                return;
            }
            ArrayList<Callback> waiting = pending.get(key);
            if (waiting != null) {
                waiting.add(callback);
                return;
            }
            waiting = new ArrayList<>();
            waiting.add(callback);
            pending.put(key, waiting);
        }

        loadMessageByLinkInternal(currentAccount, link, new Callback() {
            @Override
            public void onLoaded(MessageObject message) {
                ArrayList<Callback> callbacks;
                synchronized (lock) {
                    loaded.put(key, message);
                    callbacks = pending.remove(key);
                }
                if (callbacks != null) {
                    for (Callback c : callbacks) c.onLoaded(message);
                }
            }

            @Override
            public void onError(String error) {
                ArrayList<Callback> callbacks;
                synchronized (lock) {
                    callbacks = pending.remove(key);
                }
                if (callbacks != null) {
                    for (Callback c : callbacks) c.onError(error);
                }
            }
        });
    }

    private static void loadMessageByLinkInternal(int currentAccount, String link, Callback callback) {
        ParsedLink parsed;
        try {
            parsed = parse(link);
        } catch (Exception e) {
            parsed = null;
        }
        if (parsed == null) {
            error(callback, "Invalid link format");
            return;
        }

        MessagesController controller = MessagesController.getInstance(currentAccount);
        int messageId = parsed.messageId;

        if (parsed.username != null) {
            TLObject cached = controller.getUserOrChat(parsed.username);
            if (cached instanceof TLRPC.Chat) {
                loadMessageInternal(currentAccount, (TLRPC.Chat) cached, messageId, callback);
                return;
            }

            TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
            req.username = parsed.username;
            ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, err) -> {
                if (err != null || !(response instanceof TLRPC.TL_contacts_resolvedPeer)) {
                    error(callback, "Failed to resolve username" + (err != null ? ": " + err.text : ""));
                    return;
                }
                TLRPC.TL_contacts_resolvedPeer res = (TLRPC.TL_contacts_resolvedPeer) response;
                controller.putUsers(res.users, false);
                controller.putChats(res.chats, false);

                TLRPC.Chat chat = null;
                if (res.peer != null && res.peer.channel_id != 0) {
                    chat = controller.getChat(res.peer.channel_id);
                }
                if (chat == null) {
                    error(callback, "Not a channel or supergroup");
                    return;
                }
                loadMessageInternal(currentAccount, chat, messageId, callback);
            });
        } else {
            TLRPC.Chat chat = controller.getChat(parsed.channelId);
            if (chat == null) {
                error(callback, "Chat not in cache (open it once)");
                return;
            }
            loadMessageInternal(currentAccount, chat, messageId, callback);
        }
    }

    private static void loadMessageInternal(int currentAccount, TLRPC.Chat chat, int messageId, Callback callback) {
        TLRPC.InputChannel inputChannel = MessagesController.getInputChannel(chat);
        if (inputChannel == null) {
            error(callback, "Invalid channel");
            return;
        }

        TLRPC.TL_channels_getMessages req = new TLRPC.TL_channels_getMessages();
        req.channel = inputChannel;
        req.id = new ArrayList<>();
        req.id.add(messageId);

        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, err) -> {
            if (err != null || !(response instanceof TLRPC.messages_Messages)) {
                error(callback, "Failed to load message" + (err != null ? ": " + err.text : ""));
                return;
            }
            TLRPC.messages_Messages res = (TLRPC.messages_Messages) response;
            MessagesController controller = MessagesController.getInstance(currentAccount);
            controller.putUsers(res.users, false);
            controller.putChats(res.chats, false);

            TLRPC.Message message = res.messages.isEmpty() ? null : res.messages.get(0);
            if (message == null || message instanceof TLRPC.TL_messageEmpty) {
                error(callback, "Message not found");
                return;
            }
            MessageObject messageObject = new MessageObject(currentAccount, message, false, false);
            AndroidUtilities.runOnUIThread(() -> callback.onLoaded(messageObject));
        });
    }

    private static void error(Callback callback, String text) {
        AndroidUtilities.runOnUIThread(() -> callback.onError(text));
    }

    private static ParsedLink parse(String link) {
        if (TextUtils.isEmpty(link)) return null;
        link = link.trim();
        if (!link.contains("://")) {
            link = "https://" + link;
        }

        Uri uri = Uri.parse(link);
        ParsedLink result = new ParsedLink();

        if ("tg".equalsIgnoreCase(uri.getScheme())) {
            String host = uri.getHost();
            String post = uri.getQueryParameter("post");
            if (post == null) return null;
            result.messageId = Integer.parseInt(post);
            if ("resolve".equals(host)) {
                result.username = uri.getQueryParameter("domain");
            } else if ("privatepost".equals(host)) {
                String channel = uri.getQueryParameter("channel");
                if (channel == null) return null;
                result.channelId = Long.parseLong(channel);
            } else {
                return null;
            }
        } else {
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase();
            if (host.startsWith("www.")) host = host.substring(4);
            if (!host.equals("t.me") && !host.equals("telegram.me") && !host.equals("telegram.dog")) {
                return null;
            }

            List<String> segments = new ArrayList<>(uri.getPathSegments());
            if (!segments.isEmpty() && "s".equals(segments.get(0))) {
                segments.remove(0);
            }
            if (segments.size() < 2) return null;

            result.messageId = Integer.parseInt(segments.get(segments.size() - 1));
            if ("c".equals(segments.get(0))) {
                if (segments.size() < 3) return null;
                result.channelId = Long.parseLong(segments.get(1));
            } else {
                result.username = segments.get(0);
            }
        }

        if (result.messageId <= 0) return null;
        if (TextUtils.isEmpty(result.username) && result.channelId <= 0) return null;
        return result;
    }

}