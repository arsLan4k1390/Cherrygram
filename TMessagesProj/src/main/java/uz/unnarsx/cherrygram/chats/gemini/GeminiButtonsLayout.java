/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.chats.gemini;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.PopupSwipeBackLayout;

import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig;

public class GeminiButtonsLayout {

    public final ActionBarPopupWindow.ActionBarPopupWindowLayout layout;
    public final LinearLayout buttonsLayout;
    private final Callback callback;

    public GeminiButtonsLayout(Context context, PopupSwipeBackLayout swipeBackLayout, Callback callback) {
        this.callback = callback;
        layout = new ActionBarPopupWindow.ActionBarPopupWindowLayout(context, 0, null);
        layout.setFitItems(true);

        ActionBarMenuSubItem backItem = ActionBarMenuItem.addItem(layout, R.drawable.msg_arrow_back, getString(R.string.Back), false, null);
        backItem.setOnClickListener(view -> swipeBackLayout.closeForeground());
        backItem.setColors(0xfffafafa, 0xfffafafa);
        backItem.setSelectorColor(0x0fffffff);

        FrameLayout gap = new FrameLayout(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        };
        gap.setMinimumWidth(dp(196));
        gap.setBackgroundColor(0xff181818);
        layout.addView(gap);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) gap.getLayoutParams();
        if (LocaleController.isRTL) {
            layoutParams.gravity = Gravity.RIGHT;
        }
        layoutParams.width = LayoutHelper.MATCH_PARENT;
        layoutParams.height = dp(8);
        gap.setLayoutParams(layoutParams);

        buttonsLayout = new LinearLayout(context);
        buttonsLayout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(buttonsLayout);
    }

    public boolean update(MessageObject messageObject) {
        if (messageObject == null || messageObject.messageOwner == null || messageObject.messageOwner.media == null) return false;

        buttonsLayout.removeAllViews();

        ActionBarMenuSubItem describeItem = ActionBarMenuItem.addItem(buttonsLayout, R.drawable.msg_info_solar, getString(R.string.AccDescrQuizExplanation), false, null);
        describeItem.setColors(0xfffafafa, 0xfffafafa);
        describeItem.setOnClickListener((view) -> callback.onGeminiClicked(messageObject, false));
        describeItem.setSelectorColor(0x0fffffff);

        ActionBarMenuSubItem ocrItem = ActionBarMenuItem.addItem(buttonsLayout, R.drawable.msg_edit_solar, getString(R.string.CP_GeminiAI_ExtractText), false, null);
        ocrItem.setColors(0xfffafafa, 0xfffafafa);
        ocrItem.setOnClickListener((view) -> callback.onGeminiClicked(messageObject, true));
        ocrItem.setSelectorColor(0x0fffffff);

        return true;
    }

    public interface Callback {
        void onGeminiClicked(MessageObject messageObject, boolean isOCR);
    }

    public static boolean geminiButtonsVisible() {
        return CherrygramChatsConfig.INSTANCE.getGeminiApiKey().length() > 10 && CherrygramChatsConfig.INSTANCE.getGeminiModelName().length() > 5;
    }

}
