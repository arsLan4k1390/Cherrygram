package uz.unnarsx.cherrygram.helpers;

import android.content.Context;

import org.telegram.ui.ActionBar.BottomSheet;

public class OnceBottomSheet extends BottomSheet {
    private static boolean shown = false;

    public OnceBottomSheet(Context context, boolean needFocus) {
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
