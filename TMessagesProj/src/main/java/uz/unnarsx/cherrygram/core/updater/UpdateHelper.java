package uz.unnarsx.cherrygram.core.updater;

import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper;

public class UpdateHelper extends BaseRemoteHelper {

    private boolean updateAlways = false;
    public static final long updateCheckInterval = 3600000L; // 1 hour

    public static UpdateHelper getInstance() {
        return InstanceHolder.instance;
    }

    @Override
    protected void onError(String text, Delegate delegate) {
        delegate.onTLResponse(null, text);
    }

    @Override
    protected String getTag() {
        return CherrygramCoreConfig.INSTANCE.getInstallBetas() ? "updateBeta" : "updateRelease";
    }

    @SuppressWarnings("ConstantConditions")
    private int getPreferredAbiFile(Map<String, Integer> files) {
        for (String abi : Build.SUPPORTED_ABIS) {
            if (files.containsKey(abi)) {
                return files.get(abi);
            }
        }
        return files.get("arm64-v8a");
    }

    private Map<String, Integer> jsonToMap(JSONObject obj) {
        Map<String, Integer> map = new HashMap<>();
        List<String> abis = new ArrayList<>();
        abis.add("universal");
        abis.add("arm64-v8a");
        abis.add("armeabi-v7a");
        try {
            for (var abi : abis) {
                map.put(abi, obj.getInt(abi));
            }
        } catch (JSONException ignored) {
        }
        return map;
    }

    private Update getShouldUpdateVersion(List<JSONObject> responses) {
        Update ref = null;
        for (var string : responses) {
            try {
                String remoteVersion = string.getString("version");
                int remoteVersionCode = string.getInt("version_code");

                boolean shouldUpdate = isNew(CGResourcesHelper.getCherryVersion(), remoteVersion)
                        || isNew(CGResourcesHelper.getCodeVersion(), String.valueOf(remoteVersionCode));

                if (shouldUpdate || updateAlways) {
                    if (updateAlways) {
                        updateAlways = false;
                    }
                    ref = new Update(
                            string.getString("build_flavor"),
                            string.getString("release_date"),
                            string.getBoolean("can_not_skip"),
                            remoteVersion,
                            remoteVersionCode,
                            string.getInt("sticker"),
                            string.getInt("message"),
                            jsonToMap(string.getJSONObject("document")),
                            string.getString("url")
                    );
                    break;
                }
            } catch (JSONException ignored) {
            }
        }
        return ref;
    }

    public boolean isNew(String currentVersion, String newVersion) {
        String[] current = currentVersion.split("\\.");
        String[] latest = newVersion.split("\\.");
        int cmp = compareVersions(current, latest);
        return cmp < 0;
    }

    private static int compareVersions(String[] a, String[] b) {
        int length = Math.max(a.length, b.length);
        for (int i = 0; i < length; i++) {
            int v1 = i < a.length ? Utilities.parseInt(a[i]) : 0;
            int v2 = i < b.length ? Utilities.parseInt(b[i]) : 0;
            if (v1 != v2) {
                return Integer.compare(v1, v2);
            }
        }
        return 0;
    }

    private void getNewVersionMessagesCallback(Delegate delegate, Update json, HashMap<String, Integer> metadataIds, TLObject metadataResponse, HashMap<String, Integer> updateIds, TLObject updateResponse) {
        var update = new TLRPC.TL_help_appUpdate();
        update.build_flavor = json.build_flavor;
        update.release_date = json.release_date;
        update.version = json.version;
        update.can_not_skip = json.canNotSkip;
        if (json.url != null) {
            update.url = json.url;
            update.flags |= 4;
        }

        if (metadataResponse instanceof TLRPC.messages_Messages res) {
            getMessagesController().removeDeletedMessagesFromArray(BaseRemoteHelper.getMetadataChannelID(), res.messages);

            var messages = new HashMap<Integer, TLRPC.Message>();
            for (var message : res.messages) {
                messages.put(message.id, message);
            }

            if (metadataIds != null) {
                if (metadataIds.containsKey("sticker")) {
                    var sticker = messages.get(metadataIds.get("sticker"));
                    if (sticker != null && sticker.media != null) {
                        update.sticker = sticker.media.document;
                        update.flags |= 8;
                    }
                }

                if (metadataIds.containsKey("message")) {
                    var message = messages.get(metadataIds.get("message"));
                    if (message != null) {
                        update.text = message.message;
                        update.entities = message.entities;
                    }
                }
            }
        }

        if (updateResponse instanceof TLRPC.messages_Messages res) {
            getMessagesController().removeDeletedMessagesFromArray(BaseRemoteHelper.getUpdateChannelID(), res.messages);

            var messages = new HashMap<Integer, TLRPC.Message>();
            for (var message : res.messages) {
                messages.put(message.id, message);
            }

            if (updateIds != null && updateIds.containsKey("document")) {
                var file = messages.get(updateIds.get("document"));
                if (file != null && file.media != null) {
                    update.document = file.media.document;
                    update.flags |= 2;
                }
            }
        }
        delegate.onTLResponse(update, null);
    }

    @Override
    protected void onLoadSuccess(ArrayList<JSONObject> responses, Delegate delegate) {
        var update = getShouldUpdateVersion(responses);
        if (update == null) {
            delegate.onTLResponse(null, null);
            return;
        }

        var updateChannelIds = new HashMap<String, Integer>();
        var metadataChannelIds = new HashMap<String, Integer>();

        if (update.document != null) {
            updateChannelIds.put("document", getPreferredAbiFile(update.document));
        }
        if (update.sticker != null) {
            metadataChannelIds.put("sticker", update.sticker);
        }
        if (update.message != null) {
            metadataChannelIds.put("message", update.message);
        }

        if (updateChannelIds.isEmpty() && metadataChannelIds.isEmpty()) {
            getNewVersionMessagesCallback(delegate, update, null, null, null, null);
            return;
        }

        final AtomicReference<TLObject> metadataResponse = new AtomicReference<>(null);
        final AtomicReference<TLObject> updateResponse = new AtomicReference<>(null);
        final AtomicInteger pending = new AtomicInteger(0);

        Runnable tryFinish = () -> {
            if (pending.decrementAndGet() == 0) {
                getNewVersionMessagesCallback(
                        delegate,
                        update,
                        metadataChannelIds,
                        metadataResponse.get(),
                        updateChannelIds,
                        updateResponse.get()
                );
            }
        };

        if (!metadataChannelIds.isEmpty()) {
            pending.incrementAndGet();

            getInputChannelWithFallback(
                    BaseRemoteHelper.getMetadataChannelID(),
                    getMetadataChannel(),
                    inputChannel -> {
                        if (inputChannel == null) {
                            tryFinish.run();
                            return;
                        }

                        TLRPC.TL_channels_getMessages req = new TLRPC.TL_channels_getMessages();
                        req.channel = inputChannel;
                        req.id = new ArrayList<>(metadataChannelIds.values());

                        getConnectionsManager().sendRequest(req, (res, err) -> {
                            metadataResponse.set(res);
                            tryFinish.run();
                        });
                    }
            );
        }

        if (!updateChannelIds.isEmpty()) {
            pending.incrementAndGet();

            getInputChannelWithFallback(
                    BaseRemoteHelper.getUpdateChannelID(),
                    getUpdateChannelUsername(),
                    inputChannel -> {
                        if (inputChannel == null) {
                            tryFinish.run();
                            return;
                        }

                        TLRPC.TL_channels_getMessages req = new TLRPC.TL_channels_getMessages();
                        req.channel = inputChannel;
                        req.id = new ArrayList<>(updateChannelIds.values());

                        getConnectionsManager().sendRequest(req, (res, err) -> {
                            updateResponse.set(res);
                            tryFinish.run();
                        });
                    }
            );
        }
    }

    protected void getInputChannelWithFallback(
            long channelId,
            String username,
            Utilities.Callback<TLRPC.InputChannel> callback
    ) {
        TLRPC.InputChannel channel = getMessagesController().getInputChannel(channelId);

        if (channel != null && channel.access_hash != 0) {
            callback.run(channel);
            return;
        }

        TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
        req.username = username;

        getConnectionsManager().sendRequest(req, (res, err) -> {
            if (err == null && res instanceof TLRPC.TL_contacts_resolvedPeer resolvedPeer && !resolvedPeer.chats.isEmpty()) {
                getMessagesController().putUsers(resolvedPeer.users, false);
                getMessagesController().putChats(resolvedPeer.chats, false);
                getMessagesStorage().putUsersAndChats(resolvedPeer.users, resolvedPeer.chats, false, true);

                TLRPC.Chat chat = resolvedPeer.chats.get(0);

                TLRPC.TL_inputChannel inputChannel = new TLRPC.TL_inputChannel();
                inputChannel.channel_id = chat.id;
                inputChannel.access_hash = chat.access_hash;

                callback.run(inputChannel);
            } else {
                callback.run(null);
            }
        });
    }

    public void checkNewVersionAvailable(Delegate delegate) {
        checkNewVersionAvailable(delegate, false);
    }

    public void checkNewVersionAvailable(Delegate delegate, boolean updateAlways) {
        this.updateAlways = updateAlways;
        load(delegate);
    }

    private static final class InstanceHolder {
        private static final UpdateHelper instance = new UpdateHelper();
    }

    public static class Update {
        public String build_flavor;
        public String release_date;
        public Boolean canNotSkip;
        public String version;
        public Integer versionCode;
        public Integer sticker;
        public Integer message;
        public Map<String, Integer> document;
        public String url;

        public Update(String build_flavor, String release_date, Boolean canNotSkip, String version, int versionCode, int sticker, int message, Map<String, Integer> document, String url) {
            this.build_flavor = build_flavor;
            this.release_date = release_date;
            this.canNotSkip = canNotSkip;
            this.version = version;
            this.versionCode = versionCode;
            this.sticker = sticker;
            this.message = message;
            this.document = document;
            this.url = url;
        }
    }

}
