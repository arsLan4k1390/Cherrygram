/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.preferences.cells;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import org.telegram.messenger.ContactsController;
import org.telegram.messenger.LiteMode;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.BackgroundGradientDrawable;
import org.telegram.ui.Components.ChatAvatarContainer;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.MotionBackgroundDrawable;
import org.telegram.ui.Components.blur3.BlurredBackgroundDrawableViewFactory;
import org.telegram.ui.Components.blur3.drawable.color.impl.BlurredBackgroundProviderImpl;
import org.telegram.ui.Components.blur3.source.BlurredBackgroundSourceColor;
import org.telegram.ui.Components.blur3.source.BlurredBackgroundSourceRenderNode;
import org.telegram.ui.Components.blur3.source.BlurredBackgroundSourceWrapped;
import org.telegram.ui.Components.chat.ViewPositionWatcher;
import org.telegram.ui.Stories.recorder.StoryEntry;

import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig;

@SuppressLint("ViewConstructor")
public class ChatHeaderPreviewView extends FrameLayout {

    private final ActionBar actionBar;

    private Drawable backgroundDrawable;
    private BackgroundGradientDrawable.Disposable backgroundGradientDisposable;

    private boolean isTitleCentered = CherrygramChatsConfig.INSTANCE.getCenterChatTitle();

    public ChatHeaderPreviewView(Context context, BaseFragment fragment, Theme.ResourcesProvider resourcesProvider) {
        super(context);

        setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite, resourcesProvider));

        actionBar = new ActionBar(context);
        actionBar.setOccupyStatusBar(false);
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault, resourcesProvider));
        actionBar.setItemsColor(Theme.getColor(Theme.key_actionBarDefaultIcon, resourcesProvider), false);
        actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_actionBarDefaultSelector, resourcesProvider), false);
        actionBar.setCastShadows(false);

        BackDrawable backDrawable = new BackDrawable(false);
        backDrawable.setShowStick(!isTitleCentered);
        actionBar.setBackButtonDrawable(backDrawable);

        ActionBarMenu menu = actionBar.createMenu();
        ActionBarMenuItem menuItem = menu.addItem(0, R.drawable.ic_ab_other);
        menuItem.setContentDescription(getString(R.string.AccDescrMoreOptions));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            final ViewPositionWatcher viewPositionWatcher = new ViewPositionWatcher(this);

            BlurredBackgroundSourceWrapped navbarContentSourceWallpaper = new BlurredBackgroundSourceWrapped();

            BlurredBackgroundSourceRenderNode glassBackgroundSourceRenderNode = new BlurredBackgroundSourceRenderNode(navbarContentSourceWallpaper);
            glassBackgroundSourceRenderNode.setUnderSource(navbarContentSourceWallpaper);

            BlurredBackgroundDrawableViewFactory glassBackgroundDrawableFactory = new BlurredBackgroundDrawableViewFactory(glassBackgroundSourceRenderNode);
            glassBackgroundDrawableFactory.setLiquidGlassEffectAllowed(LiteMode.isEnabled(LiteMode.FLAG_LIQUID_GLASS));
            glassBackgroundDrawableFactory.setSourceRootView(viewPositionWatcher, this);

            actionBar.setupGlass(glassBackgroundDrawableFactory, BlurredBackgroundProviderImpl.topPanelChatActivity(resourcesProvider));
        } else {
            BlurredBackgroundSourceColor sourceColor = new BlurredBackgroundSourceColor();
            sourceColor.setColor(fragment.getThemedColor(Theme.key_windowBackgroundWhite));
            BlurredBackgroundDrawableViewFactory factory = new BlurredBackgroundDrawableViewFactory(sourceColor);
            actionBar.setupGlass(factory, BlurredBackgroundProviderImpl.topPanelChatActivity(resourcesProvider));
        }

        addView(actionBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 70, Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0, 0, 0));

        ChatAvatarContainer avatarContainer = new ChatAvatarContainer(context, fragment, true, resourcesProvider) {
            @Override
            public boolean isCentered() {
                return isTitleCentered;
            }

            @Override
            protected boolean useAnimatedSubtitle() {
                return true;
            }
        };
        avatarContainer.setActionBar(actionBar);
        avatarContainer.setOccupyStatusBar(false);

        avatarContainer.setUserAvatar(getUser());
        avatarContainer.setGlassMode();
        avatarContainer.allowShorterStatus = true;
        avatarContainer.premiumIconHiddable = false;
        avatarContainer.allowDrawStories = false;
        avatarContainer.setClipChildren(false);

        avatarContainer.setTitle(getChatTitle(getUser()));
        avatarContainer.setSubtitle(getString(R.string.Online));

        if (isTitleCentered) {
            avatarContainer.setMoveText(true);
            avatarContainer.getTitleTextView().setGravity(Gravity.CENTER_HORIZONTAL);
            if (avatarContainer.getSubtitleTextView() instanceof SimpleTextView simpleTextView) {
                simpleTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                simpleTextView.setTranslationX(-dp(CherrygramChatsConfig.INSTANCE.getUnreadBadgeOnBackButton_iOS() ? 46 : 54));
            } else if (avatarContainer.getSubtitleTextView() instanceof AnimatedTextView animatedTextView) {
                animatedTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            }
        } else {
            avatarContainer.setMoveText(false);
        }

        actionBar.addView(avatarContainer, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.START | Gravity.TOP, 54, 0, isTitleCentered ? 0 : 54, 0));
        actionBar.setChatAvatarContainer2(avatarContainer);
        actionBar.setForceAdaptiveWidth(CherrygramChatsConfig.INSTANCE.getCenterChatTitle() && CherrygramChatsConfig.INSTANCE.getCenterChatTitle_AdaptiveWidth());
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        Drawable drawable = Theme.getCachedWallpaperNonBlocking();

        if (Theme.wallpaperLoadTask != null) {
            invalidate();
        }

        if (drawable != backgroundDrawable) {
            if (backgroundGradientDisposable != null) {
                backgroundGradientDisposable.dispose();
                backgroundGradientDisposable = null;
            }
            backgroundDrawable = drawable;
        }

        if (drawable != null) {
            drawable.setAlpha(255);
            if (drawable instanceof ColorDrawable || drawable instanceof GradientDrawable || drawable instanceof MotionBackgroundDrawable) {
                drawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());

                if (drawable instanceof BackgroundGradientDrawable bg) {
                    backgroundGradientDisposable = bg.drawExactBoundsSize(canvas, this);
                } else {
                    drawable.draw(canvas);
                }
            } else if (drawable instanceof BitmapDrawable bitmapDrawable) {
                bitmapDrawable.setFilterBitmap(true);

                float scaleX = (float) getMeasuredWidth() / drawable.getIntrinsicWidth();
                float scaleY = (float) getMeasuredHeight() / drawable.getIntrinsicHeight();
                float scale = Math.max(scaleX, scaleY);

                int width = (int) Math.ceil(drawable.getIntrinsicWidth() * scale);
                int height = (int) Math.ceil(drawable.getIntrinsicHeight() * scale);

                int x = (getMeasuredWidth() - width) / 2;
                int y = (getMeasuredHeight() - height) / 2;

                drawable.setBounds(x, y, x + width, y + height);
                drawable.draw(canvas);

            } else {
                StoryEntry.drawBackgroundDrawable(canvas, drawable, getWidth(), getHeight());
            }
        } else {
            canvas.drawColor(Theme.getColor(Theme.key_chat_wallpaper));
        }
        super.onDraw(canvas);
    }

    public void setTitleCentered(boolean titleCentered) {
        this.isTitleCentered = titleCentered;
    }

    public void updateIOSUnreadBadge() {
        if (actionBar != null && actionBar.backButtonImageView != null) {
            actionBar.setIOSUnreadBadgeAvailable(CherrygramChatsConfig.INSTANCE.getUnreadBadgeOnBackButton_iOS() && isTitleCentered);
            actionBar.backButtonImageView.checkUnreadView(10);

            actionBar.updateBackPillWidth(true);
        }
    }

    private TLRPC.User getUser() {
        TLRPC.User user = UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser();
        user.self = false;
        return user;
    }

    private String getChatTitle(TLRPC.User user) {
        String title;

        if (user != null && (!TextUtils.isEmpty(user.first_name) || !TextUtils.isEmpty(user.last_name))) {
            title = ContactsController.formatName(user.first_name, user.last_name);
        } else {
            title = getString(R.string.CG_AppName);
        }

        return title;
    }

}