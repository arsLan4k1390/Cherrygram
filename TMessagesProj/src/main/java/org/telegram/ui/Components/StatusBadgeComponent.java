package org.telegram.ui.Components;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.DialogObject;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.Premium.PremiumGradient;

import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig;
import uz.unnarsx.cherrygram.donates.BadgeHelper;

public class StatusBadgeComponent {

    private final AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable statusDrawable;
    private Drawable verifiedDrawable;

    public StatusBadgeComponent(View parentView) {
        this(parentView, 18);
    }

    public StatusBadgeComponent(View parentView, int sizeDp) {
        statusDrawable = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable(parentView, AndroidUtilities.dp(sizeDp));
        cherrygramStatusDrawable = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable(parentView, AndroidUtilities.dp(sizeDp));
        badgesDrawable = new BadgesDrawable(AndroidUtilities.dp(sizeDp));
    }

    public Drawable updateDrawable(TLObject object, int colorFilter, boolean animated) {
        if (object instanceof TLRPC.User) {
            return updateDrawable((TLRPC.User) object, null, colorFilter, animated);
        } else if (object instanceof TLRPC.Chat) {
            return updateDrawable(null, (TLRPC.Chat) object, colorFilter, animated);
        }
        return updateDrawable(null, null, colorFilter, animated);
    }

    public Drawable updateDrawable(TLRPC.User user, TLRPC.Chat chat, int colorFilter, boolean animated) {
        final boolean statusesDisabled = CherrygramAppearanceConfig.INSTANCE.getDisablePremiumStatuses();

        if (chat != null && chat.verified) {
            statusDrawable.set(verifiedDrawable = (verifiedDrawable == null ? new CombinedDrawable(Theme.dialogs_verifiedDrawable, Theme.dialogs_verifiedCheckDrawable) : verifiedDrawable), animated);
            statusDrawable.setColor(null);
        } else if (chat != null && DialogObject.getEmojiStatusDocumentId(chat.emoji_status) != 0 && !statusesDisabled) {
            statusDrawable.set(DialogObject.getEmojiStatusDocumentId(chat.emoji_status), animated);
            statusDrawable.setColor(colorFilter);
        } else if (user != null && user.verified) {
            statusDrawable.set(verifiedDrawable = (verifiedDrawable == null ? new CombinedDrawable(Theme.dialogs_verifiedDrawable, Theme.dialogs_verifiedCheckDrawable) : verifiedDrawable), animated);
            statusDrawable.setColor(null);
        } else if (user != null && DialogObject.getEmojiStatusDocumentId(user.emoji_status) != 0 && !statusesDisabled) {
            statusDrawable.set(DialogObject.getEmojiStatusDocumentId(user.emoji_status), animated);
            statusDrawable.setColor(colorFilter);
        } else if (user != null && user.premium && !statusesDisabled) {
            statusDrawable.set(PremiumGradient.getInstance().premiumStarDrawableMini, animated);
            statusDrawable.setColor(colorFilter);
        } else {
            statusDrawable.set((Drawable) null, animated);
            statusDrawable.setColor(null);
        }

        /** Cherrygram start */
        if (BadgeHelper.setCherrygramStatus(cherrygramStatusDrawable, user, animated)) {
            cherrygramStatusDrawable.setColor(BadgeHelper.getEmojiStatusColor(user.id, colorFilter, false));
        }
        return badgesDrawable.update();
        /** Cherrygram finish */
    }

    public Drawable getDrawable() {
        return badgesDrawable;
    }

    public void onAttachedToWindow() {
        statusDrawable.attach();
        cherrygramStatusDrawable.attach();
    }

    public void onDetachedFromWindow() {
        statusDrawable.detach();
        cherrygramStatusDrawable.detach();
    }

    /** Cherrygram start */
    private final AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable cherrygramStatusDrawable;
    private final BadgesDrawable badgesDrawable;

    private class BadgesDrawable extends Drawable {

        private final int size;
        private final int gap = AndroidUtilities.dp(3);
        private int width;

        BadgesDrawable(int size) {
            this.size = size;
        }

        private int calcWidth() {
            int w = 0;
            if (!statusDrawable.isEmpty()) {
                w += size;
            }
            if (!cherrygramStatusDrawable.isEmpty()) {
                w += (w > 0 ? gap : 0) + size;
            }
            return w;
        }

        BadgesDrawable update() {
            final int newWidth = calcWidth();
            if (newWidth != width) {
                width = newWidth;
                if (getCallback() instanceof View) {
                    ((View) getCallback()).requestLayout();
                }
            }
            invalidateSelf();
            return this;
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            final Rect b = getBounds();
            final float scale = b.height() / (float) size;
            float x = b.left;
            if (!statusDrawable.isEmpty()) {
                statusDrawable.setBounds((int) x, b.top, (int) (x + size * scale), b.bottom);
                statusDrawable.draw(canvas);
                x += (size + gap) * scale;
            }
            if (!cherrygramStatusDrawable.isEmpty()) {
                cherrygramStatusDrawable.setBounds((int) x, b.top, (int) (x + size * scale), b.bottom);
                cherrygramStatusDrawable.draw(canvas);
            }
        }

        @Override
        public int getIntrinsicWidth() {
            return width;
        }

        @Override
        public int getIntrinsicHeight() {
            return size;
        }

        @Override
        public void setAlpha(int alpha) {
            statusDrawable.setAlpha(alpha);
            cherrygramStatusDrawable.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {}

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSPARENT;
        }
    }
    /** Cherrygram finish */

}