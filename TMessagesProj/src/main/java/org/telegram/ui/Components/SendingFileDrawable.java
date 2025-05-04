/*
 * This is the source code of Telegram for Android v. 5.x.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui.Components;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatActivity;

import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig;

public class SendingFileDrawable extends StatusDrawable {

    private boolean isChat = false;
    private long lastUpdateTime = 0;
    private boolean started = false;
    private float progress;

    Paint currentPaint;

    public SendingFileDrawable(boolean createPaint) {
        if (createPaint) {
            currentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            currentPaint.setStyle(Paint.Style.STROKE);
            currentPaint.setStrokeCap(Paint.Cap.ROUND);
            currentPaint.setStrokeWidth(AndroidUtilities.dp(2));
        }
    }

    public SendingFileDrawable(boolean createPaint, ChatActivity parentFragment) {
        if (createPaint) {
            currentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            currentPaint.setStyle(Paint.Style.STROKE);
            currentPaint.setStrokeCap(Paint.Cap.ROUND);
            currentPaint.setStrokeWidth(AndroidUtilities.dp(2));
        }
        this.chatActivity = parentFragment;
    }

    @Override
    public void setColor(int color) {
        if (currentPaint != null) {
            currentPaint.setColor(color);
        }
    }

    public void setIsChat(boolean value) {
        isChat = value;
    }

    private void update() {
        long newTime = System.currentTimeMillis();
        long dt = newTime - lastUpdateTime;
        lastUpdateTime = newTime;
        if (dt > 50) {
            dt = 50;
        }
        progress += dt / 500.0f;
        while (progress > 1.0f) {
            progress -= 1.0f;
        }
        invalidateSelf();
    }

    public void start() {
        lastUpdateTime = System.currentTimeMillis();
        started = true;
        invalidateSelf();
    }

    public void stop() {
        started = false;
    }

    @Override
    public void draw(Canvas canvas) {
        Paint paint = currentPaint == null ? Theme.chat_statusRecordPaint : currentPaint;
        for (int a = 0; a < 3; a++) {
            if (a == 0) {
                paint.setAlpha((int) (255 * progress));
            } else if (a == 2) {
                paint.setAlpha((int) (255 * (1.0f - progress)));
            } else {
                paint.setAlpha(255);
            }
            if (centerChatTitle && chatActivity != null) {
                float totalWidth = AndroidUtilities.dp(22);

                float centerX = getBounds().centerX() - AndroidUtilities.dp(2);
                float startX = centerX - totalWidth / 2f;

                float offset = AndroidUtilities.dp(5) * a + AndroidUtilities.dp(5) * progress;
                float side = startX + offset;

                float topY = AndroidUtilities.dp(isChat ? 3 : 4);
                float midY = AndroidUtilities.dp(isChat ? 7 : 8);
                float bottomY = AndroidUtilities.dp(isChat ? 11 : 12);

                canvas.drawLine(side, topY, side + AndroidUtilities.dp(4), midY, paint);
                canvas.drawLine(side, bottomY, side + AndroidUtilities.dp(4), midY, paint);
            } else {
                float side = AndroidUtilities.dp(5) * a + AndroidUtilities.dp(5) * progress;
                canvas.drawLine(side, AndroidUtilities.dp(isChat ? 3 : 4), side + AndroidUtilities.dp(4), AndroidUtilities.dp(isChat ? 7 : 8), paint);
                canvas.drawLine(side, AndroidUtilities.dp(isChat ? 11 : 12), side + AndroidUtilities.dp(4), AndroidUtilities.dp(isChat ? 7 : 8), paint);
            }
        }

        if (started) {
            update();
        }
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }

    @Override
    public int getIntrinsicWidth() {
        return centerChatTitle && chatActivity != null ? AndroidUtilities.dp(14) : AndroidUtilities.dp(18);
    }

    @Override
    public int getIntrinsicHeight() {
        return AndroidUtilities.dp(14);
    }

    /** Cherrygram start */
    private boolean centerChatTitle = CherrygramChatsConfig.INSTANCE.getCenterChatTitle();
    private ChatActivity chatActivity;
    /** Cherrygram finish */

}
