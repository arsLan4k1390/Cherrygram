/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.helpers;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BaseController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_stars;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

import java.util.Set;

import uz.unnarsx.cherrygram.core.CherrygramLogger;
import uz.unnarsx.cherrygram.core.configs.CherrygramFirebaseConfig;
import uz.unnarsx.cherrygram.donates.SSLUtils;

public class DeletedGiftsManager extends BaseController {

    private static final DeletedGiftsManager[] Instance = new DeletedGiftsManager[UserConfig.MAX_ACCOUNT_COUNT];

    public DeletedGiftsManager(int num) {
        super(num);
    }

    public static DeletedGiftsManager getInstance(int num) {
        DeletedGiftsManager localInstance = Instance[num];
        if (localInstance == null) {
            synchronized (DeletedGiftsManager.class) {
                localInstance = Instance[num];
                if (localInstance == null) {
                    Instance[num] = localInstance = new DeletedGiftsManager(num);
                }
            }
        }
        return localInstance;
    }

//    private static final String GIFTS_URL = "https://raw.githubusercontent.com/binbash-0/DeletedGifts-Plugin/refs/heads/main/gift_list.json";
    private static final String GIFTS_URL = CherrygramFirebaseConfig.INSTANCE.getDeletedGiftsConfigURL();
    private static final String STICKER_PACK_NAME = CherrygramFirebaseConfig.INSTANCE.getDeletedGiftsStickerPackName();
    private static final int STICKER_OFFSET = CherrygramFirebaseConfig.INSTANCE.getDeletedGiftsStickerPackOffset(); // 0, 1, 3 и т.д.

    private static final long CACHE_TIME = 6L * 60L * 60L * 1000L;

    private final ArrayList<DeletedGift> deletedGifts = new ArrayList<>();

    private boolean loading;
    private boolean loaded;
    private long lastLoadTime;

    private static class DeletedGift {
        long id;
        long price;
        int stickerNumber;
        String debugName;
    }

    public void preload() {
        if (!CherrygramFirebaseConfig.INSTANCE.getShowDeletedGifts()) return;

        preloadStickerPack();
        loadGiftListIfNeeded();
    }

    public void injectDeletedGifts(ArrayList<TL_stars.StarGift> giftsList) {
        if (!CherrygramFirebaseConfig.INSTANCE.getShowDeletedGifts()) return;

        if (giftsList == null || giftsList.isEmpty()) {
            return;
        }

        loadGiftListIfNeeded();

        TL_stars.TL_starGift donor = null;
        Set<Long> existingIds = new HashSet<>();

        for (TL_stars.StarGift gift : giftsList) {
            if (gift instanceof TL_stars.TL_starGift) {
                if (donor == null) {
                    donor = (TL_stars.TL_starGift) gift;
                }
                existingIds.add(gift.id);
            }
        }

        if (donor == null) {
            return;
        }

        int insertPos = Math.min(11, giftsList.size());

        for (DeletedGift deleted : deletedGifts) {

            if (existingIds.contains(deleted.id)) {
                continue;
            }

            TL_stars.TL_starGift clone = cloneGift(donor);

            clone.id = deleted.id;
            clone.stars = deleted.price;

            TLRPC.Document sticker = getCustomGiftSticker(deleted.stickerNumber);

            clone.sticker = sticker != null ? sticker : donor.sticker;

            if (insertPos <= giftsList.size()) {
                giftsList.add(insertPos++, clone);
            } else {
                giftsList.add(clone);
            }
        }
    }

    public ArrayList<TL_stars.StarGift> getDeletedGifts(ArrayList<TL_stars.StarGift> source) {
        ArrayList<TL_stars.StarGift> result = new ArrayList<>();

        if (source == null || source.isEmpty()) {
            return result;
        }

        TL_stars.TL_starGift donor = null;
        for (TL_stars.StarGift gift : source) {
            if (gift instanceof TL_stars.TL_starGift) {
                donor = (TL_stars.TL_starGift) gift;
                break;
            }
        }

        if (donor == null) {
            return result;
        }

        for (DeletedGift deleted : deletedGifts) {
            TL_stars.TL_starGift clone = cloneGift(donor);
            clone.id = deleted.id;
            clone.stars = deleted.price;

            TLRPC.Document sticker = getCustomGiftSticker(deleted.stickerNumber);
            clone.sticker = sticker != null ? sticker : donor.sticker;

            result.add(clone);
        }

        return result;
    }

    private TL_stars.TL_starGift cloneGift(TL_stars.TL_starGift donor) {
        try {
            NativeByteBuffer buffer = new NativeByteBuffer(donor.getObjectSize());
            donor.serializeToStream(buffer);
            buffer.rewind();

            TL_stars.TL_starGift clone = (TL_stars.TL_starGift) TL_stars.StarGift.TLdeserialize(
                    buffer,
                    buffer.readInt32(true), true
            );

            buffer.reuse();

            clone.attributes = new ArrayList<>();

            return clone;
        } catch (Exception e) {
            CherrygramLogger.e(e);

            TL_stars.TL_starGift clone = new TL_stars.TL_starGift();

            clone.flags = donor.flags;
            clone.limited = donor.limited;
            clone.sold_out = donor.sold_out;
            clone.birthday = donor.birthday;
            clone.require_premium = donor.require_premium;
            clone.resale_ton_only = donor.resale_ton_only;
            clone.limited_per_user = donor.limited_per_user;
            clone.peer_color_available = donor.peer_color_available;
            clone.can_upgrade = donor.can_upgrade;
            clone.auction = donor.auction;
            clone.theme_available = donor.theme_available;
            clone.burned = donor.burned;
            clone.crafted = donor.crafted;
            clone.title = donor.title;
            clone.slug = donor.slug;
            clone.attributes = new ArrayList<>();

            return clone;
        }
    }

    private TLRPC.Document getCustomGiftSticker(int stickerNumber) {
        TLRPC.TL_messages_stickerSet set = getMediaDataController().getStickerSetByName(STICKER_PACK_NAME);

        if (set == null || set.documents == null || set.documents.isEmpty()) {
            return null;
        }

        int index = Math.max(0, Math.min(STICKER_OFFSET + stickerNumber - 1, set.documents.size() - 1));

        return set.documents.get(index);
    }

    private void preloadStickerPack() {
        TLRPC.TL_messages_stickerSet cached = getMediaDataController().getStickerSetByName(STICKER_PACK_NAME);

        if (cached != null) {
            return;
        }

        TLRPC.TL_inputStickerSetShortName input = new TLRPC.TL_inputStickerSetShortName();
        input.short_name = STICKER_PACK_NAME;

        getMediaDataController().getStickerSet(input, 0, false);
    }

    private void loadGiftListIfNeeded() {
        if (loaded && System.currentTimeMillis() - lastLoadTime < CACHE_TIME) {
            return;
        }

        if (loading) {
            return;
        }

        loading = true;

        new Thread(() -> {
            try {
                HttpURLConnection connection = SSLUtils.INSTANCE.openSecureConnection(new URL(GIFTS_URL));
                if (connection == null) return;

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                StringBuilder builder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                reader.close();

                JSONObject json = new JSONObject(builder.toString());
                JSONArray array = json.getJSONArray("gifts");

                ArrayList<DeletedGift> list = new ArrayList<>();

                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);

                    DeletedGift gift = new DeletedGift();

                    gift.id = obj.getLong("id");
                    gift.price = obj.optLong("price", 50);
                    gift.stickerNumber = obj.optInt("sticker_number", 1);
                    gift.debugName = obj.optString("debug_name", "");

                    list.add(gift);
                }

                AndroidUtilities.runOnUIThread(() -> {
                    deletedGifts.clear();
                    deletedGifts.addAll(list);

                    loaded = true;
                    loading = false;
                    lastLoadTime = System.currentTimeMillis();
                });
            } catch (Exception e) {
                CherrygramLogger.e(e);
                AndroidUtilities.runOnUIThread(() -> loading = false);
            }

        }).start();
    }

}