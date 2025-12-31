/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */
package uz.unnarsx.cherrygram.core.ui;

import android.content.Context;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;

public class FakeProfileHeaderEmpty extends View {

    public FakeProfileHeaderEmpty(Context context, int color) {
        super(context);
        setBackgroundColor(color);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(MD3ListAdapter.canTryToIgnoreTopBarBackground() ? 0.01f : 7.33f), MeasureSpec.EXACTLY));
    }

}
