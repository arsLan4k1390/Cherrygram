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
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import org.telegram.messenger.LiteMode;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BackgroundGradientDrawable;
import org.telegram.ui.Components.ChatActivityEnterView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.MotionBackgroundDrawable;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.Components.blur3.BlurredBackgroundDrawableViewFactory;
import org.telegram.ui.Components.blur3.drawable.color.BlurredBackgroundColorProvider;
import org.telegram.ui.Components.blur3.drawable.color.impl.BlurredBackgroundProviderImpl;
import org.telegram.ui.Components.blur3.source.BlurredBackgroundSourceColor;
import org.telegram.ui.Components.blur3.source.BlurredBackgroundSourceRenderNode;
import org.telegram.ui.Components.blur3.source.BlurredBackgroundSourceWrapped;
import org.telegram.ui.Components.chat.ChatInputViewsContainer;
import org.telegram.ui.Components.chat.ViewPositionWatcher;
import org.telegram.ui.Components.inset.WindowInsetsStateHolder;
import org.telegram.ui.Stories.recorder.StoryEntry;

import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig;

@SuppressLint("ViewConstructor")
public class ChatInputPreviewView extends FrameLayout {

    private final ChatInputViewsContainer chatInputViewsContainer;
    private final ChatActivityEnterView chatActivityEnterView;

    private Drawable backgroundDrawable;
    private BackgroundGradientDrawable.Disposable backgroundGradientDisposable;

    public final WindowInsetsStateHolder windowInsetsStateHolder = new WindowInsetsStateHolder(this::checkInsets);

    private final int bubbleHeight = dp(44);

    public ChatInputPreviewView(Context context, BaseFragment fragment, Theme.ResourcesProvider resourcesProvider) {
        super(context);

        setWillNotDraw(false);
        setClipChildren(false);
        setClipToPadding(false);

        SizeNotifierFrameLayout contentView = new SizeNotifierFrameLayout(context);
        contentView.setClipChildren(false);
        contentView.setClipToPadding(false);

        addView(contentView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL));

        chatInputViewsContainer = new ChatInputViewsContainer(context);
        chatInputViewsContainer.setClipChildren(false);
        chatInputViewsContainer.setWindowInsetsProvider(windowInsetsStateHolder);

        contentView.addView(chatInputViewsContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL));

        setupGlass(resourcesProvider);

        FrameLayout chatInputBubbleContainer = new FrameLayout(context);
        chatInputBubbleContainer.setClipChildren(false);
        chatInputBubbleContainer.setClipToPadding(false);

        chatInputViewsContainer.addView(
                chatInputBubbleContainer,
                LayoutHelper.createFrame(
                        LayoutHelper.MATCH_PARENT,
                        LayoutHelper.WRAP_CONTENT,
                        Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL
                )
        );

        chatActivityEnterView = new ChatActivityEnterView(
                fragment.getParentActivity(),
                contentView,
                null,
                true,
                resourcesProvider,
                CherrygramChatsConfig.INSTANCE.getIOSMessageInputField()
        ) {
            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
                return false;
            }
        };
        chatActivityEnterView.setClickable(false);
        chatActivityEnterView.setChatInputViewsContainer(chatInputViewsContainer);
        chatActivityEnterView.shouldDrawBackground = false;

        chatActivityEnterView.setAllowStickersAndGifs(true, true, true);
        chatActivityEnterView.setDialogId(fragment.getUserConfig().clientUserId, UserConfig.selectedAccount);
        chatActivityEnterView.updateSendAsButtonPreview();
        chatActivityEnterView.updateAudioVideoButton();

        chatActivityEnterView.setDelegate(new ChatActivityEnterView.ChatActivityEnterViewDelegate() {
            @Override
            public void onMessageSend(CharSequence message, boolean notify, int scheduleDate, int scheduleRepeatPeriod, long payStars) {}

            @Override
            public void needSendTyping() {}

            @Override
            public void onTextChanged(CharSequence text, boolean bigChange, boolean fromDraft) {}

            @Override
            public void onTextSelectionChanged(int start, int end) {}

            @Override
            public void onTextSpansChanged(CharSequence text) {}

            @Override
            public void onAttachButtonHidden() {}

            @Override
            public void onAttachButtonShow() {}

            @Override
            public void onWindowSizeChanged(int size) {}

            @Override
            public void onStickersTab(boolean opened) {}

            @Override
            public void onMessageEditEnd(boolean loading) {}

            @Override
            public void didPressAttachButton() {}

            @Override
            public void needStartRecordVideo(int state, boolean notify, int scheduleDate, int scheduleRepeatPeriod, int ttl, long effectId, long stars) {}

            @Override
            public void toggleVideoRecordingPause() {}

            @Override
            public boolean isVideoRecordingPaused() {
                return false;
            }

            @Override
            public void needChangeVideoPreviewState(int state, float seekProgress) {}

            @Override
            public void onSwitchRecordMode(boolean video) {}

            @Override
            public void onPreAudioVideoRecord() {}

            @Override
            public void needStartRecordAudio(int state) {}

            @Override
            public void needShowMediaBanHint() {}

            @Override
            public void onStickersExpandedChange() {}

            @Override
            public void onUpdateSlowModeButton(View button, boolean show, CharSequence time) {}

            @Override
            public void onSendLongClick() {}

            @Override
            public void onAudioVideoInterfaceUpdated() {}
        });

        chatInputBubbleContainer.addView(
                chatActivityEnterView,
                LayoutHelper.createFrame(
                        LayoutHelper.MATCH_PARENT,
                        LayoutHelper.WRAP_CONTENT,
                        Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL,
                        7,
                        0,
                        7,
                        1
                )
        );

        if (chatActivityEnterView.messageEditText != null) {
            chatActivityEnterView.messageEditText.setTranslationX(CherrygramChatsConfig.INSTANCE.getIOSMessageInputField() ? 30 : 0);
            chatActivityEnterView.messageEditText.setText(null);

            chatActivityEnterView.messageEditText.setHintText(getString(R.string.TypeMessage));

            chatActivityEnterView.messageEditText.setFocusable(false);
            chatActivityEnterView.messageEditText.setClickable(false);
            chatActivityEnterView.messageEditText.setCursorVisible(false);
        }

        chatActivityEnterView.setFocusable(false);
        chatActivityEnterView.setClickable(false);

        updateInputBubbleOffsets();
    }

    private void setupGlass(Theme.ResourcesProvider resourcesProvider) {
        BlurredBackgroundDrawableViewFactory glassBackgroundDrawableFactory;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            final ViewPositionWatcher viewPositionWatcher = new ViewPositionWatcher(this);

            BlurredBackgroundSourceWrapped navbarContentSourceWallpaper = new BlurredBackgroundSourceWrapped();
            BlurredBackgroundSourceRenderNode glassBackgroundSourceRenderNode = new BlurredBackgroundSourceRenderNode(navbarContentSourceWallpaper);
            glassBackgroundSourceRenderNode.setUnderSource(navbarContentSourceWallpaper);

            glassBackgroundDrawableFactory = new BlurredBackgroundDrawableViewFactory(glassBackgroundSourceRenderNode);
            glassBackgroundDrawableFactory.setLiquidGlassEffectAllowed(LiteMode.isEnabled(LiteMode.FLAG_LIQUID_GLASS));
            glassBackgroundDrawableFactory.setSourceRootView(viewPositionWatcher, this);
        } else {
            BlurredBackgroundSourceColor sourceColor = new BlurredBackgroundSourceColor();
            sourceColor.setColor(Theme.getColor(Theme.key_windowBackgroundWhite, resourcesProvider));

            glassBackgroundDrawableFactory = new BlurredBackgroundDrawableViewFactory(sourceColor);
        }

        BlurredBackgroundColorProvider blurredBackgroundColorProvider = BlurredBackgroundProviderImpl.topPanelChatActivity(resourcesProvider);

        chatInputViewsContainer.setInputIslandBubbleDrawable(
                glassBackgroundDrawableFactory.create(chatInputViewsContainer, blurredBackgroundColorProvider)
        );

        chatInputViewsContainer.setUnderKeyboardBackgroundDrawable(
                glassBackgroundDrawableFactory.create(chatInputViewsContainer, blurredBackgroundColorProvider)
        );

        if (CherrygramChatsConfig.INSTANCE.getIOSMessageInputField()) {
            chatInputViewsContainer.setLeftBubbleDrawable(
                    glassBackgroundDrawableFactory.create(chatInputViewsContainer, blurredBackgroundColorProvider)
            );

            chatInputViewsContainer.setRightBubbleDrawable(
                    glassBackgroundDrawableFactory.create(chatInputViewsContainer, blurredBackgroundColorProvider)
            );
        }
    }

    private void updateInputBubbleOffsets() {
        if (chatInputViewsContainer != null) {
            chatInputViewsContainer.checkInsets();
            chatInputViewsContainer.setInputBubbleHeight(bubbleHeight);
        }

        if (chatActivityEnterView != null) {
            chatActivityEnterView.checkBubbles();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        updateInputBubbleOffsets();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateInputBubbleOffsets();
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

    @Override
    protected void onDetachedFromWindow() {
        if (backgroundGradientDisposable != null) {
            backgroundGradientDisposable.dispose();
            backgroundGradientDisposable = null;
        }
        super.onDetachedFromWindow();
    }

    private void checkInsets() {
        chatInputViewsContainer.checkInsets();
    }

}