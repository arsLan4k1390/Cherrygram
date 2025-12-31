/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.chats;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.lerp;
import static org.telegram.messenger.LocaleController.getString;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Insets;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ChatMessageCell;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.EarListener;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ScaleStateListAnimator;
import org.telegram.ui.Components.SeekBar;
import org.telegram.ui.Components.VideoPlayer;

import java.io.File;
import java.net.URLEncoder;

// Dear Nagram / Nagram X / Octogram and related fork developers:
// Please respect this work and do not copy or reuse this feature in your forks.
// It required a significant amount of time and effort to implement,
// and it is provided exclusively for my users, who also support this project financially.

public class RoundVideoPlayer extends Dialog implements NotificationCenter.NotificationCenterDelegate {

    public final Context context;

    private FrameLayout windowView;
    private FrameLayout containerView;

    private final Rect insets = new Rect();
    private Bitmap blurBitmap;
    private BitmapShader blurBitmapShader;
    private Paint blurBitmapPaint;
    private Matrix blurMatrix;

    private boolean open;
    private float openProgress;
    private float openProgress2;

    private VideoPlayer player;

    private TextView closeButton;

    private EarListener earListener;

    public RoundVideoPlayer(Context context) {
        super(context, R.style.TransparentDialog);
        this.context = context;

        windowView = new FrameLayout(context) {
            @Override
            protected void dispatchDraw(@NonNull Canvas canvas) {
                if (openProgress > 0 && blurBitmapPaint != null) {
                    blurMatrix.reset();
                    final float s = (float) getWidth() / blurBitmap.getWidth();
                    blurMatrix.postScale(s, s);
                    blurBitmapShader.setLocalMatrix(blurMatrix);

                    blurBitmapPaint.setAlpha((int) (0xFF * openProgress));
                    canvas.drawRect(0, 0, getWidth(), getHeight(), blurBitmapPaint);
                }
                super.dispatchDraw(canvas);
            }

            @Override
            public boolean dispatchKeyEventPreIme(KeyEvent event) {
                if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    dismiss();
                    return true;
                }
                return super.dispatchKeyEventPreIme(event);
            }

            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);
                setupTranslation();
            }

            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                if (cell != null) {
                    int[] loc = new int[2];
                    cell.getLocationOnScreen(loc);
                    float x = ev.getRawX();
                    float y = ev.getRawY();

                    if (x >= loc[0] && x <= loc[0] + cell.getWidth()
                            && y >= loc[1] && y <= loc[1] + cell.getHeight()) {
                        return false;
                    }
                }
                return super.onInterceptTouchEvent(ev);
            }
        };

        containerView = new FrameLayout(context) {
            private final Path clipPath = new Path();
            @Override
            protected boolean drawChild(@NonNull Canvas canvas, View child, long drawingTime) {
                if (child == myCell) {
                    canvas.save();
                    canvas.clipRect(0, AndroidUtilities.lerp(clipTop, 0, openProgress), getWidth(), AndroidUtilities.lerp(clipBottom, getHeight(), openProgress));
                    boolean r = super.drawChild(canvas, child, drawingTime);
                    canvas.restore();
                    return r;
                }
                if (child == textureView) {
                    canvas.save();

                    clipPath.rewind();
                    clipPath.addCircle(myCell.getX() + rect.centerX(), myCell.getY() + rect.centerY(), rect.width() / 2f, Path.Direction.CW);
                    canvas.clipPath(clipPath);
                    canvas.clipRect(0, AndroidUtilities.lerp(clipTop, 0, openProgress), getWidth(), AndroidUtilities.lerp(clipBottom, getHeight(), openProgress));
                    canvas.translate(
                        - textureView.getX(),
                        - textureView.getY()
                    );
                    canvas.translate(
                        myCell.getX() + rect.left,
                        myCell.getY() + rect.top
                    );
                    canvas.scale(
                        rect.width() / textureView.getMeasuredWidth(),
                        rect.height() / textureView.getMeasuredHeight(),
                        textureView.getX(),
                        textureView.getY()
                    );
                    boolean r = super.drawChild(canvas, child, drawingTime);
                    canvas.restore();
                    return r;
                }
                return super.drawChild(canvas, child, drawingTime);
            }
        };
        containerView.setClipToPadding(false);
        windowView.addView(containerView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.FILL));

        windowView.setFitsSystemWindows(true);
        windowView.setOnApplyWindowInsetsListener((v, insets) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Insets r = insets.getInsets(WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.systemBars());
                RoundVideoPlayer.this.insets.set(r.left, r.top, r.right, r.bottom);
            } else {
                RoundVideoPlayer.this.insets.set(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            }
            containerView.setPadding(RoundVideoPlayer.this.insets.left, RoundVideoPlayer.this.insets.top, RoundVideoPlayer.this.insets.right, RoundVideoPlayer.this.insets.bottom);
            windowView.requestLayout();
            if (Build.VERSION.SDK_INT >= 30) {
                return WindowInsets.CONSUMED;
            } else {
                return insets.consumeSystemWindowInsets();
            }
        });

        if (SharedConfig.raiseToListen) {
            earListener = new EarListener(context);
        }
    }

    private void prepareBlur(View withoutView) {
        if (withoutView != null) {
            withoutView.setVisibility(View.INVISIBLE);
        }
        AndroidUtilities.makeGlobalBlurBitmap(bitmap -> {
            if (withoutView != null) {
                withoutView.setVisibility(View.VISIBLE);
            }
            blurBitmap = bitmap;

            blurBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            blurBitmapPaint.setShader(blurBitmapShader = new BitmapShader(blurBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
            ColorMatrix colorMatrix = new ColorMatrix();
            AndroidUtilities.adjustSaturationColorMatrix(colorMatrix, Theme.isCurrentThemeDark() ? .05f : +.25f);
            AndroidUtilities.adjustBrightnessColorMatrix(colorMatrix, Theme.isCurrentThemeDark() ? -.02f : -.04f);
            blurBitmapPaint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
            blurMatrix = new Matrix();
        }, 14);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setWindowAnimations(R.style.DialogNoAnimation);
        setContentView(windowView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        WindowManager.LayoutParams params = window.getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.FILL;
        params.dimAmount = 0;
        params.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING;
        params.flags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
            WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
            WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS |
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
        params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        if (Build.VERSION.SDK_INT >= 28) {
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        window.setAttributes(params);

        windowView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN);
        AndroidUtilities.setLightNavigationBar(windowView, !Theme.isCurrentThemeDark());
    }

    private float tx, ty;
    private boolean hasTranslation;
    private float dtx, dty;
    private boolean hasDestTranslation;
    private float heightdiff;

    private void setupTranslation() {
        if (hasTranslation || windowView.getWidth() <= 0) return;
        if (cell != null) {
            int[] loc = new int[2];
            cell.getLocationOnScreen(loc);
            tx = loc[0] - insets.left - (windowView.getWidth() - insets.left - insets.right - cell.getWidth()) / 2f;
            ty = loc[1] - insets.top - (windowView.getHeight() - insets.top - insets.bottom - cell.getHeight() - heightdiff) / 2f;
            if (!hasDestTranslation) {
                hasDestTranslation = true;
                dtx = 0;
                dty = 0;
            }
            updateTranslation();
        } else {
            tx = ty = 0;
        }
        hasTranslation = true;
    }

    private void updateTranslation() {
        myCell.setTranslationX(AndroidUtilities.lerp(tx, dtx, openProgress));
        myCell.setTranslationY(AndroidUtilities.lerp(ty, dty, openProgress));
    }

    private ChatMessageCell myCell;
    private ChatMessageCell cell;
    private TextureView textureView;
    private boolean renderedFirstFrame;
    private final RectF rect = new RectF();

    private float clipTop = 0, clipBottom = 0;

    private Runnable openAction, closeAction;
    private boolean allowRunCloseAction = false;

    public void setCell(ChatMessageCell messageCell, MessageObject messageObject, Runnable openAction, Runnable closeAction) {
        this.openAction = openAction;
        this.closeAction = closeAction;

        if (myCell != null) {
            containerView.removeView(myCell);
            myCell = null;
        }
        int videoDp = 360;

        cell = messageCell;
        if (cell != null) {
            clipTop = messageCell.parentBoundsTop;
            clipBottom = messageCell.parentBoundsBottom;
            if (messageCell.getParent() instanceof View parent) {
                clipTop += parent.getY();
                clipBottom += parent.getY();
            }

            int width = cell.getWidth();
            int height = Math.min(dp(360), Math.min(width, AndroidUtilities.displaySize.y));

            heightdiff = height - cell.getHeight();
            final int finalWidth = width;
            final int finalHeight = height;
            videoDp = (int) Math.ceil(Math.min(width, height) * .92f / AndroidUtilities.density);

            myCell = new ChatMessageCell(getContext(), UserConfig.selectedAccount, false, null, cell.getResourcesProvider()) {

                @Override
                public boolean onTouchEvent(MotionEvent event) {
                    return super.onTouchEvent(event);
                }

                @Override
                public int getBoundsLeft() {
                    return 0;
                }

                @Override
                public int getBoundsRight() {
                    return getWidth();
                }

                private boolean setRect = false;
                final RectF fromRect = new RectF();
                final RectF toRect = new RectF();

                @Override
                protected void onDraw(Canvas canvas) {
                    if (!setRect) {
                        fromRect.set(getPhotoImage().getImageX(), getPhotoImage().getImageY(), getPhotoImage().getImageX2(), getPhotoImage().getImageY2());
                        final float sz = Math.min(getMeasuredWidth(), getMeasuredHeight()) * .92f;
                        toRect.set((getMeasuredWidth() - sz) / 2f, (getMeasuredHeight() - sz) / 2f, (getMeasuredWidth() + sz) / 2f, (getMeasuredHeight() + sz) / 2f);
                        setRect = true;
                    }

                    AndroidUtilities.lerp(fromRect, toRect, openProgress, rect);
                    setImageCoords(rect.left, rect.top, rect.width(), rect.height());
                    getPhotoImage().setRoundRadius((int) rect.width());

                    if (openProgress > 0 && renderedFirstFrame) {
                        canvas.saveLayerAlpha(0, 0, getWidth(), getHeight(), 0xFF, Canvas.ALL_SAVE_FLAG);
                    }

                    radialProgressAlpha = 1f - openProgress;
                    super.onDraw(canvas);
                    if (openProgress > 0 && renderedFirstFrame) {
                        canvas.restore();
                    }
                }

                @Override
                public void drawReactionsLayout(Canvas canvas, float alpha, Integer only) {
                    canvas.save();
                    canvas.translate(lerp(0, -reactionsLayoutInBubble.x, openProgress), lerp(cell.getBackgroundDrawableBottom() - getBackgroundDrawableBottom(), reactionsLayoutInBubble.totalHeight, openProgress));
                    super.drawReactionsLayout(canvas, (1.0f - openProgress) * alpha, only);
                    canvas.restore();
                }

                @Override
                public void drawTime(Canvas canvas, float alpha, boolean fromParent) {
                    canvas.save();
                    final float timeWidth = this.timeWidth + AndroidUtilities.dp(8 + (messageObject != null && messageObject.isOutOwner() ? 20 + (messageObject.type == MessageObject.TYPE_EMOJIS ? 4 : 0) : 0));
                    canvas.translate((toRect.right - timeWidth - timeX) * openProgress, 0);
                    super.drawTime(canvas, alpha, fromParent);
                    canvas.restore();
                }

                @Override
                protected void drawRadialProgress(Canvas canvas) {
                    super.drawRadialProgress(canvas);
                }

                @Override
                public void setVisibility(int visibility) {
                    super.setVisibility(visibility);
                    if (textureView != null && visibility == View.GONE) {
                        textureView.setVisibility(visibility);
                    }
                }

                @Override
                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    setMeasuredDimension(finalWidth, finalHeight);
                }

            };
            cell.copyVisiblePartTo(myCell);

            myCell.setDelegate(new ChatMessageCell.ChatMessageCellDelegate() {
                @Override
                public boolean canPerformActions() {
                    return true;
                }

                @Override
                public boolean needPlayMessage(ChatMessageCell cell, MessageObject messageObject, boolean muted) {
                    if (messageObject.isRoundVideo() && player != null) {
                        boolean result = MediaController.getInstance().playMessage(messageObject, muted);
                        MediaController.getInstance().setVoiceMessagesPlaylist(null, false);
                        return result;
                    }
                    return false;
                }

            });

            myCell.setMessageObject(messageObject, cell.getCurrentMessagesGroup(), cell.pinnedBottom, cell.pinnedTop, false);
            hasTranslation = false;
            containerView.addView(myCell, new FrameLayout.LayoutParams(cell.getWidth(), height, Gravity.CENTER));
        }

        if (textureView != null) {
            containerView.removeView(textureView);
            textureView = null;
        }
        renderedFirstFrame = false;
        textureView = new TextureView(context);
        containerView.addView(textureView, 0, LayoutHelper.createFrame(videoDp, videoDp));

        MediaController.getInstance().pauseByRewind();

        if (player != null) {
            player.pause();
            player.releasePlayer(true);
            player = null;
            MediaController.getInstance().cleanup();
        }
        if (messageObject != null) {
            File file = null;
            boolean exists = false;
            if (messageObject.messageOwner.attachPath != null && !messageObject.messageOwner.attachPath.isEmpty()) {
                file = new File(messageObject.messageOwner.attachPath);
                exists = file.exists();
                if (!exists) {
                    file = null;
                }
            }
            final File cacheFile = file != null ? file : FileLoader.getInstance(messageObject.currentAccount).getPathToMessage(messageObject.messageOwner);
            boolean canStream = SharedConfig.streamMedia && (messageObject.isMusic() || messageObject.isRoundVideo() || messageObject.isVideo() && messageObject.canStreamVideo()) && !messageObject.shouldEncryptPhotoOrVideo() && !DialogObject.isEncryptedDialog(messageObject.getDialogId());
            if (cacheFile != file && !(exists = cacheFile.exists()) && !canStream) {
                FileLoader.getInstance(messageObject.currentAccount).loadFile(messageObject.getDocument(), messageObject, FileLoader.PRIORITY_LOW, messageObject.shouldEncryptPhotoOrVideo() ? 2 : 0);
                NotificationCenter.getInstance(messageObject.currentAccount).postNotificationName(NotificationCenter.messagePlayingPlayStateChanged, messageObject.getId());
            }

            player = new VideoPlayer();
            player.setDelegate(new VideoPlayer.VideoPlayerDelegate() {
                @Override
                public void onStateChanged(boolean playWhenReady, int playbackState) {
                    if (playbackState == ExoPlayer.STATE_ENDED) {
                        dismiss(false);
                    } else {
                        AndroidUtilities.cancelRunOnUIThread(checkTimeRunnable);
                        AndroidUtilities.runOnUIThread(checkTimeRunnable, 16);
                    }
                }

                @Override
                public void onError(VideoPlayer player, Exception e) {
                    FileLog.e(e);
                }

                @Override
                public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

                }

                @Override
                public void onRenderedFirstFrame() {
                    AndroidUtilities.runOnUIThread(() -> {
                        renderedFirstFrame = true;
                        myCell.invalidate();
                    });
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
                    AndroidUtilities.runOnUIThread(() -> myCell.invalidate());
                }

                @Override
                public boolean onSurfaceDestroyed(SurfaceTexture surfaceTexture) {
                    return false;
                }
            });

            player.setTextureView(textureView);

            if (exists) {
                if (!messageObject.mediaExists && cacheFile != file) {
                    AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(messageObject.currentAccount).postNotificationName(NotificationCenter.fileLoaded, FileLoader.getAttachFileName(messageObject.getDocument()), cacheFile));
                }
                player.preparePlayer(Uri.fromFile(cacheFile), "other");
            } else {
                try {
                    int reference = FileLoader.getInstance(messageObject.currentAccount).getFileReference(messageObject);
                    TLRPC.Document document = messageObject.getDocument();
                    String params = "?account=" + messageObject.currentAccount +
                            "&id=" + document.id +
                            "&hash=" + document.access_hash +
                            "&dc=" + document.dc_id +
                            "&size=" + document.size +
                            "&mime=" + URLEncoder.encode(document.mime_type, "UTF-8") +
                            "&rid=" + reference +
                            "&name=" + URLEncoder.encode(FileLoader.getDocumentFileName(document), "UTF-8") +
                            "&reference=" + Utilities.bytesToHex(document.file_reference != null ? document.file_reference : new byte[0]);
                    Uri uri = Uri.parse("tg://" + messageObject.getFileName() + params);
                    player.preparePlayer(uri, "other");
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
            player.play();
            if (earListener != null) {
                earListener.attachPlayer(player);
            }
            MediaController.getInstance().injectVideoPlayer(player, messageObject);
            MediaController.getInstance().setTextureView(textureView, new AspectRatioFrameLayout(getContext()), containerView, true);
        }

        if (closeButton != null) {
            containerView.removeView(closeButton);
            closeButton = null;
        }
        closeButton = new TextView(context);
        closeButton.setTextColor(0xFFFFFFFF);
        closeButton.setTypeface(AndroidUtilities.bold());
        if (Theme.isCurrentThemeDark()) {
            closeButton.setBackground(Theme.createSimpleSelectorRoundRectDrawable(64, 0x20ffffff, 0x33ffffff));
        } else {
            closeButton.setBackground(Theme.createSimpleSelectorRoundRectDrawable(64, 0x2e000000, 0x44000000));
        }
        closeButton.setPadding(dp(12), dp(6), dp(12), dp(6));
        ScaleStateListAnimator.apply(closeButton);
        closeButton.setText(getString(R.string.VoiceOnceClose));
        closeButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        closeButton.setOnClickListener(v -> dismiss(true));
        containerView.addView(closeButton, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.CENTER, 0, 0, 0, dp(40)));
    }

    @Override
    public void show() {
        if (!AndroidUtilities.isSafeToShow(getContext())) return;
        super.show();

        prepareBlur(cell);

        animateOpenTo(open = true, null);

        if (this.openAction != null) {
            AndroidUtilities.runOnUIThread(this.openAction);
            this.openAction = null;
        }

        if (earListener != null) {
            earListener.attach();
        }

        NotificationCenter.getInstance(UserConfig.selectedAccount).addObserver(this, NotificationCenter.messagePlayingDidStart);
        NotificationCenter.getInstance(UserConfig.selectedAccount).addObserver(this, NotificationCenter.messagePlayingPlayStateChanged);
        NotificationCenter.getInstance(UserConfig.selectedAccount).addObserver(this, NotificationCenter.messagePlayingDidReset);
        NotificationCenter.getInstance(UserConfig.selectedAccount).addObserver(this, NotificationCenter.messagePlayingProgressDidChanged);
    }

    private final Runnable checkTimeRunnable = this::checkTime;

    private void checkTime() {
        if (player == null) {
            return;
        }

        float progress = player.getCurrentPosition() / (float) player.getDuration();
        if (myCell != null) {
            myCell.overrideDuration((player.getDuration() - player.getCurrentPosition()) / 1000L);

            SeekBar seekBar = myCell.getSeekBar();
            if (seekBar != null) {
                seekBar.setProgress((int)(progress * 100));
            }
        }

        if (player.isPlaying()) {
            AndroidUtilities.cancelRunOnUIThread(checkTimeRunnable);
            AndroidUtilities.runOnUIThread(checkTimeRunnable, 16);
        }
    }

    public boolean isShown() {
        return !dismissing;
    }

    private boolean dismissing = false;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void dismiss(boolean byClose) {
        if (byClose) {
            allowRunCloseAction = true;
            dismiss();
        } else {
            dismiss();
        }
    }

    @Override
    public void dismiss() {
        if (dismissing) return;

        dismissing = true;

        if (player != null) {
            player.pause();
            player.releasePlayer(true);
            player = null;
            MediaController.getInstance().cleanup();
        }

        hasTranslation = false;
        setupTranslation();
        animateOpenTo(open = false, () -> {
            AndroidUtilities.runOnUIThread(super::dismiss);

            MediaController.getInstance().tryResumePausedAudio();
        });
        windowView.invalidate();

        if (this.closeAction != null && allowRunCloseAction) {
            AndroidUtilities.runOnUIThread(this.closeAction);
            this.closeAction = null;

            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            getWindow().setAttributes(params);
        }

        if (earListener != null) {
            earListener.detach();
        }

        NotificationCenter.getInstance(UserConfig.selectedAccount).removeObserver(this, NotificationCenter.messagePlayingDidStart);
        NotificationCenter.getInstance(UserConfig.selectedAccount).removeObserver(this, NotificationCenter.messagePlayingPlayStateChanged);
        NotificationCenter.getInstance(UserConfig.selectedAccount).removeObserver(this, NotificationCenter.messagePlayingDidReset);
        NotificationCenter.getInstance(UserConfig.selectedAccount).removeObserver(this, NotificationCenter.messagePlayingProgressDidChanged);
    }

    private ValueAnimator openAnimator;
    private ValueAnimator open2Animator;
    private void animateOpenTo(boolean open, Runnable after) {
        if (openAnimator != null) {
            openAnimator.cancel();
        }
        if (open2Animator != null) {
            open2Animator.cancel();
        }
        setupTranslation();
        openAnimator = ValueAnimator.ofFloat(openProgress, open ? 1 : 0);
        openAnimator.addUpdateListener(anm -> {
            openProgress = (float) anm.getAnimatedValue();
            windowView.invalidate();
            containerView.invalidate();
            myCell.invalidate();

            updateTranslation();
            if (closeButton != null) {
                closeButton.setAlpha(openProgress);
            }
        });
        openAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                openProgress = open ? 1 : 0;
                windowView.invalidate();
                containerView.invalidate();
                updateTranslation();
                if (closeButton != null) {
                    closeButton.setAlpha(openProgress);
                }
                myCell.invalidate();

                if (after != null) {
                    after.run();
                }
            }
        });
        final long duration = open ? 400 : 650;
        openAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        openAnimator.setDuration(duration);
        openAnimator.start();

        open2Animator = ValueAnimator.ofFloat(openProgress2, open ? 1 : 0);
        open2Animator.addUpdateListener(anm -> {
            openProgress2 = (float) anm.getAnimatedValue();
            myCell.invalidate();
        });
        open2Animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                openProgress2 = open ? 1 : 0;
                myCell.invalidate();
            }
        });
        open2Animator.setDuration((long) (1.5f * duration));
        open2Animator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        open2Animator.start();
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.messagePlayingDidStart) {
            if (myCell != null) {
                MessageObject messageObject1 = myCell.getMessageObject();
                if (messageObject1 != null) {
                    if (messageObject1.isRoundVideo()) {
                        myCell.checkVideoPlayback(false, null);
                        if (!MediaController.getInstance().isPlayingMessage(messageObject1)) {
                            if (messageObject1.audioProgress != 0) {
                                messageObject1.resetPlayingProgress();
                                myCell.invalidate();
                            }
                        }
                    }
                }
            }
        } else if (id == NotificationCenter.messagePlayingDidReset || id == NotificationCenter.messagePlayingPlayStateChanged) {
            if (myCell != null) {
                if (id == NotificationCenter.messagePlayingDidReset) {
                    dismiss(false);
                }
                MessageObject messageObject = myCell.getMessageObject();
                if (messageObject != null) {
                    if (messageObject.isRoundVideo()) {
                        if (!MediaController.getInstance().isPlayingMessage(messageObject)) {
                            myCell.checkVideoPlayback(true, null);
                        }
                    }
                }
            }
        } else if (id == NotificationCenter.messagePlayingProgressDidChanged) {
            Integer mid = (Integer) args[0];
            if (myCell != null) {
                MessageObject playing = myCell.getMessageObject();
                if (playing != null && playing.getId() == mid) {
                    MessageObject player = MediaController.getInstance().getPlayingMessageObject();
                    if (player != null) {
                        playing.audioProgress = player.audioProgress;
                        playing.audioProgressSec = player.audioProgressSec;
                        playing.audioPlayerDuration = player.audioPlayerDuration;
                        myCell.updatePlayingMessageProgress();
                    }
                }
            }
        }
    }

}
