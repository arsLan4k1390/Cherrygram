package uz.unnarsx.cherrygram.stickers;

import org.telegram.messenger.MessageObject;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

public class StickersIDsLocal {
    private static final ArrayList<Long> SET_IDS = new ArrayList<>();

    static {
        SET_IDS.add(683462835916767409L);
        SET_IDS.add(1510769529645432834L);
        SET_IDS.add(8106175868352593928L);
        SET_IDS.add(5835129661968875533L);
    }

    public static ArrayList<Long> getIDs() {
        return SET_IDS;
    }

    public static boolean isLocalSetId(TLRPC.Document document) {
        return getIDs().stream().anyMatch(setID -> setID == MessageObject.getStickerSetId(document));
    }

}
