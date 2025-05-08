/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.chats;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.dpf2;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.os.Build;
import android.os.CountDownTimer;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.math.MathUtils;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.CodeHighlighting;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedFloat;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LinkPath;
import org.telegram.ui.Components.LoadingDrawable;
import org.telegram.ui.Components.RecyclerListView;

import java.io.IOException;

import uz.unnarsx.cherrygram.chats.helpers.ChatsHelper2;
import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig;

public class JsonBottomSheet extends BottomSheet implements NotificationCenter.NotificationCenterDelegate {

    private HeaderView headerView;
    private LoadingTextView loadingTextView;
    private FrameLayout textViewContainer;
    private TextView textView;

    private boolean sheetTopNotAnimate;
    private RecyclerListView listView;
    private LinearLayoutManager layoutManager;
    private PaddedAdapter adapter;

    private View buttonShadowView;
    private FrameLayout buttonView;
    private TextView buttonTextView;
    private ImageView copyButton;

    public Theme.ResourcesProvider resourcesProvider;
    public BaseFragment fragment;

    private JsonBottomSheet(Context context, Theme.ResourcesProvider resourcesProvider, MessageObject messageObject, TLRPC.Chat currentChat) {
        super(context, false, resourcesProvider);
        this.resourcesProvider = resourcesProvider;

        backgroundPaddingLeft = 0;

        fixNavigationBar();

        boolean isNoForwards = currentChat != null && (currentChat.noforwards || messageObject.messageOwner.noforwards);

        containerView = new ContainerView(context);
        sheetTopAnimated = new AnimatedFloat(containerView, 320, CubicBezierInterpolator.EASE_OUT_QUINT);

        loadingTextView = new LoadingTextView(context);
        loadingTextView.setPadding(dp(22), dp(12), dp(22), dp(6));

        textViewContainer = new FrameLayout(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), heightMeasureSpec);
            }
        };

        textView = new TextView(context);
        textView.setPadding(dp(22), dp(12), dp(22), dp(6));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setTypeface(AndroidUtilities.bold());
        textView.setTextColor(getThemedColor(Theme.key_dialogTextBlack));
        textView.setTextIsSelectable(!isNoForwards);

        textViewContainer.addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView = new RecyclerListView(context, resourcesProvider) {
            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
                if (ev.getAction() == MotionEvent.ACTION_DOWN && ev.getY() < getSheetTop() - getTop()) {
                    dismiss();
                    return true;
                }
                return super.dispatchTouchEvent(ev);
            }

            @Override
            protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
                return true;
            }

            @Override
            public void requestChildFocus(View child, View focused) {}
        };
        listView.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        listView.setPadding(0, AndroidUtilities.statusBarHeight + dp(56), 0, dp(80));
        listView.setClipToPadding(true);
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(context));
        listView.setAdapter(adapter = new PaddedAdapter(context, loadingTextView));
        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                containerView.invalidate();
                updateButtonShadow(listView.canScrollVertically(1));
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    sheetTopNotAnimate = false;
                }
                if ((newState == RecyclerView.SCROLL_STATE_IDLE || newState == RecyclerView.SCROLL_STATE_SETTLING) && getSheetTop(false) > 0 && getSheetTop(false) < dp(64 + 32) && listView.canScrollVertically(1) && hasEnoughHeight()) {
                    sheetTopNotAnimate = true;
                    listView.smoothScrollBy(0, (int) getSheetTop(false));
                }
            }
        });
        DefaultItemAnimator itemAnimator = new DefaultItemAnimator() {
            @Override
            protected void onChangeAnimationUpdate(RecyclerView.ViewHolder holder) {
                containerView.invalidate();
            }

            @Override
            protected void onMoveAnimationUpdate(RecyclerView.ViewHolder holder) {
                containerView.invalidate();
            }
        };
        itemAnimator.setDurations(180);
        itemAnimator.setInterpolator(new LinearInterpolator());
        listView.setItemAnimator(itemAnimator);
        containerView.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM));

        headerView = new HeaderView(context, messageObject);
        containerView.addView(headerView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 78, Gravity.TOP | Gravity.FILL_HORIZONTAL));

        buttonView = new FrameLayout(context);
        buttonView.setBackgroundColor(getThemedColor(Theme.key_dialogBackground));

        buttonShadowView = new View(context);
        buttonShadowView.setBackgroundColor(getThemedColor(Theme.key_dialogShadowLine));
        buttonShadowView.setAlpha(0);
        buttonView.addView(buttonShadowView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, AndroidUtilities.getShadowHeight() / dpf2(1), Gravity.TOP | Gravity.FILL_HORIZONTAL));

        buttonTextView = new TextView(context);
        buttonTextView.setLines(1);
        buttonTextView.setSingleLine(true);
        buttonTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        buttonTextView.setEllipsize(TextUtils.TruncateAt.END);
        buttonTextView.setGravity(Gravity.CENTER);
        buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText, resourcesProvider));
        buttonTextView.setTypeface(AndroidUtilities.bold());
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        buttonTextView.setText(getString(R.string.Close));
        buttonTextView.setBackground(Theme.AdaptiveRipple.filledRect(Theme.getColor(Theme.key_featuredStickers_addButton, resourcesProvider), 6));
        buttonTextView.setOnClickListener(e -> dismiss());
        buttonView.addView(buttonTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 16, 16, 72, 16));

        copyButton = new ImageView(context);
        copyButton.setScaleType(ImageView.ScaleType.CENTER);
        copyButton.setImageResource(R.drawable.msg_copy);
        copyButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_featuredStickers_buttonText, resourcesProvider), PorterDuff.Mode.MULTIPLY));
        copyButton.setBackground(Theme.AdaptiveRipple.filledRect(Theme.getColor(Theme.key_featuredStickers_addButton, resourcesProvider), 6));
        copyButton.setOnClickListener(v -> {
            if (isNoForwards) {
                if (ChatObject.isChannel(currentChat) && !currentChat.megagroup) {
                    BulletinFactory.of(getContainer(), null).createErrorBulletin(getString(R.string.ForwardsRestrictedInfoChannel)).show();
                } else {
                    BulletinFactory.of(getContainer(), null).createErrorBulletin(getString(R.string.ForwardsRestrictedInfoGroup)).show();
                }
            } else {
                AndroidUtilities.addToClipboard(textView.getText());
                BulletinFactory.of(getContainer(), null).createCopyBulletin(getString(R.string.TextCopied)).show();
            }
        });
        buttonView.addView(copyButton, LayoutHelper.createFrame(48, 48, Gravity.BOTTOM | Gravity.RIGHT, 0, 16, 16, 16));

        containerView.addView(buttonView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL));

        colorizeJson(messageObject);
    }

    private boolean hasEnoughHeight() {
        float height = 0;
        for (int i = 0; i < listView.getChildCount(); ++i) {
            View child = listView.getChildAt(i);
            if (listView.getChildAdapterPosition(child) == 1)
                height += child.getHeight();
        }
        return height >= listView.getHeight() - listView.getPaddingTop() - listView.getPaddingBottom();
    }

    private static int messageId;
    public static int getMessageId(MessageObject messageObject) {
        return messageId = messageObject.messageOwner.id;
    }

    public void colorizeJson(MessageObject messageObject) {
        String jsonString = "";

        if (messageObject.messageOwner.action instanceof TLRPC.TL_messageActionSetChatWallPaper || !isJacksonSupportedAndEnabled()) {
            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                jsonString = gson.toJson(messageObject.messageOwner);
            } catch (Exception e) {
                CherrygramChatsConfig.INSTANCE.setJacksonJSON_Provider(true);
            }
        } else {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                jsonString = mapper.writeValueAsString(messageObject.messageOwner);
            } catch (IOException e) {
                FileLog.e(e);
            }
        }

        final SpannableString[] sb = new SpannableString[1];
        String finalJsonString = jsonString;
        new CountDownTimer(400, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                sb[0] = CodeHighlighting.getHighlighted(finalJsonString, "json");
            }

            @Override
            public void onFinish() {
                textView.setText(sb[0]);
            }
        }.start();
        adapter.updateMainView(textViewContainer);
    }

    @Override
    public void dismissInternal() {
        super.dismissInternal();
    }

    public void setFragment(BaseFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    protected boolean canDismissWithSwipe() {
        return false;
    }

    private class LoadingTextView extends TextView {

        private final LinkPath path = new LinkPath(true);
        private final LoadingDrawable loadingDrawable = new LoadingDrawable();

        public LoadingTextView(Context context) {
            super(context);
            loadingDrawable.usePath(path);
            loadingDrawable.setSpeed(.65f);
            loadingDrawable.setRadiiDp(4);
            setBackground(loadingDrawable);
        }

        @Override
        public void setTextColor(int color) {
            super.setTextColor(Theme.multAlpha(color, .2f));
            loadingDrawable.setColors(
                    Theme.multAlpha(color, 0.03f),
                    Theme.multAlpha(color, 0.175f),
                    Theme.multAlpha(color, 0.2f),
                    Theme.multAlpha(color, 0.45f)
            );
        }

        private void updateDrawable() {
            if (path == null || loadingDrawable == null) {
                return;
            }

            path.rewind();
            if (getLayout() != null && getLayout().getText() != null) {
                path.setCurrentLayout(getLayout(), 0, getPaddingLeft(), getPaddingTop());
                getLayout().getSelectionPath(0, getLayout().getText().length(), path);
            }
            loadingDrawable.updateBounds();
        }

        @Override
        public void setText(CharSequence text, BufferType type) {
            super.setText(text, type);
            updateDrawable();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            updateDrawable();
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            loadingDrawable.reset();
        }
    }

    private static class PaddedAdapter extends RecyclerListView.Adapter {

        private Context mContext;
        private View mMainView;

        public PaddedAdapter(Context context, View mainView) {
            mContext = context;
            mMainView = mainView;
        }

        private int mainViewType = 1;

        public void updateMainView(View newMainView) {
            if (mMainView == newMainView) {
                return;
            }
            mainViewType++;
            mMainView = newMainView;
            notifyItemChanged(1);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == 0) {
                return new RecyclerListView.Holder(new View(mContext) {
                    @Override
                    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                        super.onMeasure(
                                MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                                MeasureSpec.makeMeasureSpec((int) (AndroidUtilities.displaySize.y * .4f), MeasureSpec.EXACTLY)
                        );
                    }
                });
            } else {
                return new RecyclerListView.Holder(mMainView);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {}

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return 0;
            } else {
                return mainViewType;
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

    private AnimatedFloat sheetTopAnimated;
    private float getSheetTop() {
        return getSheetTop(true);
    }
    private float getSheetTop(boolean animated) {
        float top = listView.getTop();
        if (listView.getChildCount() >= 1) {
            top += Math.max(0, listView.getChildAt(listView.getChildCount() - 1).getTop());
        }
        top = Math.max(0, top - dp(78));
        if (animated && sheetTopAnimated != null) {
            if (!listView.scrollingByUser && !sheetTopNotAnimate) {
                top = sheetTopAnimated.set(top);
            } else {
                sheetTopAnimated.set(top, true);
            }
        }
        return top;
    }

    private class HeaderView extends FrameLayout {

        private ImageView backButton;
        private TextView titleTextView;
        private LinearLayout subtitleView;
        public TextView messageIdTextView;
        private ImageView menuIconImageView;

        private View shadow;

        public HeaderView(Context context, MessageObject messageObject) {
            super(context);

            backButton = new ImageView(context);
            backButton.setScaleType(ImageView.ScaleType.CENTER);
            backButton.setImageResource(R.drawable.ic_ab_back);
            backButton.setColorFilter(new PorterDuffColorFilter(getThemedColor(Theme.key_dialogTextBlack), PorterDuff.Mode.MULTIPLY));
            backButton.setBackground(Theme.createSelectorDrawable(getThemedColor(Theme.key_listSelector)));
            backButton.setAlpha(0f);
            backButton.setOnClickListener(e -> dismiss());
            addView(backButton, LayoutHelper.createFrame(54, 54, Gravity.TOP, 1, 1, 1, 1));

            menuIconImageView = new ImageView(context);
            menuIconImageView.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_actionBarActionModeDefaultSelector, resourcesProvider), 1));
            menuIconImageView.setScaleType(ImageView.ScaleType.CENTER);
            menuIconImageView.setImageResource(R.drawable.ic_ab_other);
            menuIconImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogSearchIcon, resourcesProvider), PorterDuff.Mode.MULTIPLY));
            menuIconImageView.setOnClickListener((v) -> ChatsHelper2.showJsonMenu(JsonBottomSheet.this, headerView, messageObject));
            addView(menuIconImageView, LayoutHelper.createFrame(36, 36, Gravity.RIGHT | Gravity.TOP, 0, 11, 16, 0));

            titleTextView = new TextView(context) {
                @Override
                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                    if (LocaleController.isRTL) {
                        titleTextView.setPivotX(getMeasuredWidth());
                    }
                }
            };
            titleTextView.setTextColor(getThemedColor(Theme.key_dialogTextBlack));
            titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            titleTextView.setTypeface(AndroidUtilities.bold());
            titleTextView.setText("JSON");
            titleTextView.setPivotX(0);
            titleTextView.setPivotY(0);
            addView(titleTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.FILL_HORIZONTAL, 22, 20, 22, 0));

            subtitleView = new LinearLayout(context) {
                @Override
                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                    if (LocaleController.isRTL) {
                        subtitleView.setPivotX(getMeasuredWidth());
                    }
                }
            };
            if (LocaleController.isRTL) {
                subtitleView.setGravity(Gravity.RIGHT);
            }
            subtitleView.setPivotX(0);
            subtitleView.setPivotY(0);

            messageIdTextView = new TextView(context);
            messageIdTextView.setLines(2);
            messageIdTextView.setTextColor(getThemedColor(Theme.key_player_actionBarSubtitle));
            messageIdTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            StringBuilder sb = new StringBuilder();
            sb.append("message ID: ");
            sb.append(messageId);
            sb.append("\nJSON Library: ");
            sb.append(messageObject.messageOwner.action instanceof TLRPC.TL_messageActionSetChatWallPaper || !isJacksonSupportedAndEnabled() ? "Google GSON" : "Jackson");
            messageIdTextView.setText(sb);
            messageIdTextView.setPadding(0, dp(2), 0, dp(2));
            messageIdTextView.setOnClickListener(v -> {
                AndroidUtilities.addToClipboard(String.valueOf(messageId));
                BulletinFactory.of(getContainer(), null).createCopyBulletin(getString(R.string.TextCopied)).show();
            });

            if (LocaleController.isRTL) {
                subtitleView.addView(messageIdTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 4, 5, 0, 0));
            } else {
                subtitleView.addView(messageIdTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 0, 5, 4, 0));
            }

            addView(subtitleView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.FILL_HORIZONTAL, 22, 34, 22, 0));
            shadow = new View(context);
            shadow.setBackgroundColor(getThemedColor(Theme.key_dialogShadowLine));
            shadow.setAlpha(0);
            addView(shadow, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, AndroidUtilities.getShadowHeight() / dpf2(1), Gravity.TOP | Gravity.FILL_HORIZONTAL, 0, 56, 0, 0));
        }

        @Override
        public void setTranslationY(float translationY) {
            super.setTranslationY(translationY);

            float t = MathUtils.clamp((translationY - AndroidUtilities.statusBarHeight) / dp(64), 0, 1);
            if (!hasEnoughHeight()) {
                t = 1;
            }
            t = CubicBezierInterpolator.EASE_OUT.getInterpolation(t);

            titleTextView.setScaleX(AndroidUtilities.lerp(.85f, 1f, t));
            titleTextView.setScaleY(AndroidUtilities.lerp(.85f, 1f, t));
            titleTextView.setTranslationY(AndroidUtilities.lerp(dpf2(-12), 0, t));
            if (!LocaleController.isRTL) {
                titleTextView.setTranslationX(AndroidUtilities.lerp(dpf2(50), 0, t));
                subtitleView.setTranslationX(AndroidUtilities.lerp(dpf2(50), 0, t));
            }

            subtitleView.setTranslationY(AndroidUtilities.lerp(dpf2(-22), 0, t));

            backButton.setTranslationX(AndroidUtilities.lerp(0, dpf2(-25), t));
            backButton.setAlpha(1f - t);

            shadow.setTranslationY(AndroidUtilities.lerp(0, dpf2(22), t));
            shadow.setAlpha(1f - t);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(dp(78), MeasureSpec.EXACTLY)
            );
        }
    }

    private class ContainerView extends FrameLayout {
        public ContainerView(Context context) {
            super(context);

            bgPaint.setColor(getThemedColor(Theme.key_dialogBackground));
            Theme.applyDefaultShadow(bgPaint);
        }

        private Path bgPath = new Path();
        private Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        @Override
        protected void dispatchDraw(Canvas canvas) {

            float top = getSheetTop();
            final float R = AndroidUtilities.lerp(0, dp(12), MathUtils.clamp(top / dpf2(24), 0, 1));
            headerView.setTranslationY(Math.max(AndroidUtilities.statusBarHeight, top));
            updateLightStatusBar(top <= AndroidUtilities.statusBarHeight / 2f);

            bgPath.rewind();
            AndroidUtilities.rectTmp.set(0, top, getWidth(), getHeight() + R);
            bgPath.addRoundRect(AndroidUtilities.rectTmp, R, R, Path.Direction.CW);
            canvas.drawPath(bgPath, bgPaint);

            super.dispatchDraw(canvas);
        }

        private Boolean lightStatusBarFull;
        private void updateLightStatusBar(boolean full) {
            if (lightStatusBarFull == null || lightStatusBarFull != full) {
                lightStatusBarFull = full;
                AndroidUtilities.setLightStatusBar(getWindow(), AndroidUtilities.computePerceivedBrightness(
                        full ?
                                getThemedColor(Theme.key_dialogBackground) :
                                Theme.blendOver(
                                        getThemedColor(Theme.key_actionBarDefault),
                                        0x33000000
                                )
                ) > .721f);
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY));
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            Bulletin.addDelegate(this, new Bulletin.Delegate() {
                @Override
                public int getBottomOffset(int tag) {
                    return dp(16 + 48 + 16);
                }
            });
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            Bulletin.removeDelegate(this);
        }
    }

    @Override
    public void show() {
        super.show();
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiLoaded);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiLoaded);
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.emojiLoaded) {
            loadingTextView.invalidate();
            textView.invalidate();
        }
    }

    private Boolean buttonShadowShown;
    private void updateButtonShadow(boolean show) {
        if (buttonShadowShown == null || buttonShadowShown != show) {
            buttonShadowShown = show;
            buttonShadowView.animate().cancel();
            buttonShadowView.animate().alpha(show ? 1f : 0f).setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT).setDuration(320).start();
        }
    }

    public boolean isJacksonSupportedAndEnabled() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && CherrygramChatsConfig.INSTANCE.getJacksonJSON_Provider()) {
            CherrygramChatsConfig.INSTANCE.setJacksonJSON_Provider(false);
        }
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && CherrygramChatsConfig.INSTANCE.getJacksonJSON_Provider();
    }

    public static JsonBottomSheet showAlert(Context context, Theme.ResourcesProvider resourcesProvider, BaseFragment fragment, MessageObject messageObject, TLRPC.Chat currentChat) {
        JsonBottomSheet alert = new JsonBottomSheet(context, resourcesProvider, messageObject, currentChat);
        if (fragment.getParentActivity() != null) {
            fragment.showDialog(alert);
        }
        alert.dimBehindAlpha = 140;
        alert.setFragment(fragment);
        return alert;
    }

}

