/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.core.helpers;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.misc.Constants;

public class LocalVerificationsHelper {
    private static final ArrayList<Long> DEFAULT_VERIFY_LIST = new ArrayList<>();

    static {
        DEFAULT_VERIFY_LIST.add(Constants.Cherrygram_Channel);
        DEFAULT_VERIFY_LIST.add(Constants.Cherrygram_Support);
        DEFAULT_VERIFY_LIST.add(Constants.Cherrygram_APKs);
        DEFAULT_VERIFY_LIST.add(Constants.Cherrygram_Beta);
        DEFAULT_VERIFY_LIST.add(Constants.Cherrygram_Archive);
    }

    public static ArrayList<Long> getVerify() {
        return DEFAULT_VERIFY_LIST;
    }

}
