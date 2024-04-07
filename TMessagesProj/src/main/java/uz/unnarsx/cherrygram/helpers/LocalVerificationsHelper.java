package uz.unnarsx.cherrygram.helpers;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.extras.Constants;

public class LocalVerificationsHelper {
    private static final ArrayList<Long> DEFAULT_VERIFY_LIST = new ArrayList<>();
    private static final ArrayList<Long> HIDE_DELETE_ALL_BUTTON = new ArrayList<>();

    static {
        DEFAULT_VERIFY_LIST.add(Constants.Cherrygram_Channel);
        DEFAULT_VERIFY_LIST.add(Constants.Cherrygram_Support);
        DEFAULT_VERIFY_LIST.add(Constants.Cherrygram_APKs);
        DEFAULT_VERIFY_LIST.add(Constants.Cherrygram_Beta);
        DEFAULT_VERIFY_LIST.add(Constants.Cherrygram_Archive);

        HIDE_DELETE_ALL_BUTTON.add(Constants.Cherrygram_Support);
        HIDE_DELETE_ALL_BUTTON.add(1201287079L); // Abitur
    }

    public static ArrayList<Long> getVerify() {
        return DEFAULT_VERIFY_LIST;
    }

    public static ArrayList<Long> hideDeleteAll() {
        return HIDE_DELETE_ALL_BUTTON;
    }

}
