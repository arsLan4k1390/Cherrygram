package uz.unnarsx.extras;

import java.util.ArrayList;

public class LocalVerifications {
    private static final ArrayList<Long> DEFAULT_VERIFY_LIST = new ArrayList<>();
    /*private static final ArrayList<Long> DEFAULT_SCAM_LIST = new ArrayList<>();
    private static final ArrayList<Long> DEFAULT_FAKE_LIST = new ArrayList<>();*/

    static {
        DEFAULT_VERIFY_LIST.add(1776033848L); // Cherrygram Channel
        DEFAULT_VERIFY_LIST.add(1554776538L); // Cherrygram Support Group
        DEFAULT_VERIFY_LIST.add(1557718915L); // Cherrygram APKs
        DEFAULT_VERIFY_LIST.add(1544768810L); // Cherrygram Beta APKs
        DEFAULT_VERIFY_LIST.add(1633574643L); // Cherrygram Premium APKs
        DEFAULT_VERIFY_LIST.add(1719103382L); // Cherrygram Archive

        /*DEFAULT_SCAM_LIST.add("ID+L");
        DEFAULT_SCAM_LIST.add("ID+L");

        DEFAULT_FAKE_LIST.add("ID+L");
        DEFAULT_FAKE_LIST.add("ID+L");*/
    }

    public static ArrayList<Long> getVerify() {
        return DEFAULT_VERIFY_LIST;
    }

    /*public static ArrayList<Long> getScam() {
        return DEFAULT_SCAM_LIST;
    }

    public static ArrayList<Long> getFake() {
        return DEFAULT_FAKE_LIST;
    }*/

}
