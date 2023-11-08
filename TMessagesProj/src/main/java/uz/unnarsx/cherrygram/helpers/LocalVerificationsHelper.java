package uz.unnarsx.cherrygram.helpers;

import java.util.ArrayList;

public class LocalVerificationsHelper {
    private static final ArrayList<Long> DEFAULT_VERIFY_LIST = new ArrayList<>();
    private static final ArrayList<Long> HIDE_DELETE_ALL_BUTTON = new ArrayList<>();

    static {
        DEFAULT_VERIFY_LIST.add(1776033848L); // Cherrygram Channel
        DEFAULT_VERIFY_LIST.add(1554776538L); // Cherrygram Support Group
        DEFAULT_VERIFY_LIST.add(1557718915L); // Cherrygram APKs
        DEFAULT_VERIFY_LIST.add(1544768810L); // Cherrygram Beta APKs
        DEFAULT_VERIFY_LIST.add(1633574643L); // Cherrygram Premium APKs
        DEFAULT_VERIFY_LIST.add(1719103382L); // Cherrygram Archive

        HIDE_DELETE_ALL_BUTTON.add(1554776538L); // Cherrygram Support Group
        HIDE_DELETE_ALL_BUTTON.add(1201287079L); // Abitur
    }

    public static ArrayList<Long> getVerify() {
        return DEFAULT_VERIFY_LIST;
    }

    public static ArrayList<Long> hideDeleteAll() {
        return HIDE_DELETE_ALL_BUTTON;
    }

}
