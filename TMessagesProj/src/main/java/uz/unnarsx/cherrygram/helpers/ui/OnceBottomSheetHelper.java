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
