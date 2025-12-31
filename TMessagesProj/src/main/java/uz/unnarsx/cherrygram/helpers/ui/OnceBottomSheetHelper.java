/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.helpers.ui;

import android.content.Context;

import org.telegram.ui.ActionBar.BottomSheet;

public class OnceBottomSheetHelper extends BottomSheet {
    private static boolean shown = false;

    public OnceBottomSheetHelper(Context context, boolean needFocus) {
        super(context, needFocus);
    }

    @Override
    public void show() {
        if (shown) {
            return;
        }
        shown = true;
        super.show();
    }
}
