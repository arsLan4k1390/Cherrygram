/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.chats.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.LinearLayout;

import org.telegram.messenger.BaseController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.ChatScrimPopupContainerLayout;
import org.telegram.ui.Components.ReactionsContainerLayout;

// Dear Nagram / Nagram X / Octogram and related fork developers:
// Please respect this work and do not copy or reuse this feature in your forks.
// It required a significant amount of time and effort to implement,
// and it is provided exclusively for my users, who also support this project financially.

public class MessageMenuHelper extends BaseController {

    private static final MessageMenuHelper[] Instance = new MessageMenuHelper[UserConfig.MAX_ACCOUNT_COUNT];

    public MessageMenuHelper(int num) {
        super(num);
    }

    public static MessageMenuHelper getInstance(int num) {
        MessageMenuHelper localInstance = Instance[num];
        if (localInstance == null) {
            synchronized (MessageMenuHelper.class) {
                localInstance = Instance[num];
                if (localInstance == null) {
                    Instance[num] = localInstance = new MessageMenuHelper(num);
                }
            }
        }
        return localInstance;
    }

    public View hiddenMessageView;
    public void createMenu(
            ChatActivity chatActivity,
            View view,
            ChatActivity.ChatActivityFragmentView contentView,
            ChatScrimPopupContainerLayout scrimPopupContainerLayout,
            ReactionsContainerLayout reactionsLayout,
            MessageObject messageObject,
            ActionBarPopupWindow.ActionBarPopupWindowLayout popupWindowLayout
    ) {

    }

    public void checkBlur(Activity activity, boolean enable, boolean hideStatusBar, float windowBlurRadius) {
        checkBlur(activity, enable, hideStatusBar, windowBlurRadius, 0);
    }

    public void checkBlur(Activity activity, boolean enable, boolean hideStatusBar, float windowBlurRadius, float windowDimAlpha) {
        if (activity == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return;

        WindowBlurHelper blurHelper = new WindowBlurHelper();
        blurHelper.setWindowBlur(activity, enable, hideStatusBar, windowBlurRadius, windowDimAlpha);
    }

    public void hideMessageView(
            View originalCell,
            ChatActivity chatActivity,
            ChatActivity.ChatActivityAdapter chatAdapter,
            MessageObject messageObjectToReset,
            Activity parentActivity,
            boolean hide
    ) {

    }

    public static class MaxHeightLinearLayout extends LinearLayout {

        public MaxHeightLinearLayout(Context context) {
            super(context);
        }

        public void setMaxHeight(int maxHeight) {
            requestLayout();
        }

        public void setMaxWidth(int maxWidth) {
            requestLayout();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public int getMessageMenuAlpha(boolean divider) {
        return 255;
    }

    public boolean showDivider() {
        return true;
    }

    public boolean showCustomDivider(boolean verifyDonates) {
        return true;
    }

    public boolean allowUnifiedScroll(boolean verifyDonates) {
        return false;
    }

    public boolean allowNewMessageMenu() {
        return false;
    }

    public boolean allowNewMessageMenu(MessageObject messageObject) {
        return false;
    }

    public boolean allowNewMessageMenu(boolean verifyDonates) {
        return false;
    }

}
