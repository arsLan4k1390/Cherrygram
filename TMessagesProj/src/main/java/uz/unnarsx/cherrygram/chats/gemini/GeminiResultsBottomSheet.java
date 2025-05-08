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
import static org.telegram.messenger.AndroidUtilities.dpf2;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
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
import androidx.core.content.ContextCompat;
import androidx.core.math.MathUtils;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.TranslateController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.AnimatedFloat;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LinkPath;
import org.telegram.ui.Components.LinkSpanDrawable;
import org.telegram.ui.Components.LoadingDrawable;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig;
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper;

public class GeminiResultsBottomSheet extends BottomSheet implements NotificationCenter.NotificationCenterDelegate {

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
    private TextView summarizeButton;
    private ImageView copyButton;

    private static MessageObject selectedObject; // TODO set to null when closing bottom sheet
    private static TLRPC.Chat currentChat; // TODO set to null when closing bottom sheet

    public static int GEMINI_TYPE_TRANSLATE = 1;
    public static int GEMINI_TYPE_TRANSCRIBE = 2;
    public static int GEMINI_TYPE_EXPLANATION = 3;
    public static int GEMINI_TYPE_OCR = 4;
    public static int GEMINI_TYPE_SUMMARIZE = 5;

    private GeminiResultsBottomSheet(BaseFragment fragment, ChatActivity chatActivity, String geminiResult, int subtitle) {
        super(fragment.getContext(), false, fragment.getResourceProvider());

        backgroundPaddingLeft = 0;

        fixNavigationBar();

        MessagesController mc = MessagesController.getInstance(UserConfig.selectedAccount);
        boolean noforwards = mc.isChatNoForwards(selectedObject.getChatId()) || selectedObject.messageOwner.noforwards || selectedObject.getDialogId() == UserObject.VERIFY;
        boolean noforwardsOrPaidMedia = noforwards || selectedObject.type == MessageObject.TYPE_PAID_MEDIA;

        containerView = new ContainerView(fragment.getContext());
        sheetTopAnimated = new AnimatedFloat(containerView, 320, CubicBezierInterpolator.EASE_OUT_QUINT);

        loadingTextView = new LoadingTextView(fragment.getContext());
        loadingTextView.setPadding(dp(22), dp(12), dp(22), dp(6));

        textViewContainer = new FrameLayout(fragment.getContext()) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), heightMeasureSpec);
            }
        };

        textView = new TextView(fragment.getContext());
        textView.setPadding(dp(22), dp(0), dp(22), dp(6));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, SharedConfig.fontSize);
//        textView.setTypeface(AndroidUtilities.bold());
        textView.setTextColor(getThemedColor(Theme.key_dialogTextBlack));
        textView.setTextIsSelectable(!noforwardsOrPaidMedia);

        textViewContainer.addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView = new RecyclerListView(fragment.getContext(), getResourcesProvider()) {
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
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(fragment.getContext()));
        listView.setAdapter(adapter = new PaddedAdapter(fragment.getContext(), loadingTextView));
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

        headerView = new HeaderView(fragment.getContext(), chatActivity, subtitle);
        containerView.addView(headerView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 5, Gravity.TOP | Gravity.FILL_HORIZONTAL));

        buttonView = new FrameLayout(fragment.getContext());
        buttonView.setBackgroundColor(getThemedColor(Theme.key_dialogBackground));

        buttonShadowView = new View(fragment.getContext());
        buttonShadowView.setBackgroundColor(getThemedColor(Theme.key_dialogShadowLine));
        buttonShadowView.setAlpha(0);
        buttonView.addView(buttonShadowView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, AndroidUtilities.getShadowHeight() / dpf2(1), Gravity.TOP | Gravity.FILL_HORIZONTAL));

        summarizeButton = new TextView(fragment.getContext());
        summarizeButton.setLines(1);
        summarizeButton.setSingleLine(true);
        summarizeButton.setGravity(Gravity.CENTER_HORIZONTAL);
        summarizeButton.setEllipsize(TextUtils.TruncateAt.END);
        summarizeButton.setGravity(Gravity.CENTER);
        summarizeButton.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText, getResourcesProvider()));
        summarizeButton.setTypeface(AndroidUtilities.bold());
        summarizeButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        summarizeButton.setText(subtitle == 5 ? getString(R.string.Close) : getString(R.string.CP_GeminiAI_Summarize));
        summarizeButton.setBackground(Theme.AdaptiveRipple.filledRect(Theme.getColor(Theme.key_featuredStickers_addButton, getResourcesProvider()), 6));
        summarizeButton.setOnClickListener(e -> {
            if (subtitle == 5) {
                dismiss();
            } else {
                dismiss();
                GeminiSDKImplementation.initGeminiConfig(
                        fragment,
                        null,
                        geminiResult, false, true,
                        null,
                        false, false
                );
            }
        });
        buttonView.addView(summarizeButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 16, 16, 72, 16));

        copyButton = new ImageView(fragment.getContext());
        copyButton.setScaleType(ImageView.ScaleType.CENTER);
        copyButton.setImageResource(R.drawable.msg_copy);
        copyButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_featuredStickers_buttonText, getResourcesProvider()), PorterDuff.Mode.MULTIPLY));
        copyButton.setBackground(Theme.AdaptiveRipple.filledRect(Theme.getColor(Theme.key_featuredStickers_addButton, getResourcesProvider()), 6));
        copyButton.setOnClickListener(v -> {
            if (noforwardsOrPaidMedia) {
                if (ChatObject.isChannel(currentChat) && !currentChat.megagroup) {
                    BulletinFactory.of(getContainer(), getResourcesProvider()).createErrorBulletin(getString(R.string.ForwardsRestrictedInfoChannel)).show();
                } else {
                    BulletinFactory.of(getContainer(), getResourcesProvider()).createErrorBulletin(getString(R.string.ForwardsRestrictedInfoGroup)).show();
                }
            } else {
                AndroidUtilities.addToClipboard(textView.getText());
                BulletinFactory.of(getContainer(), getResourcesProvider()).createCopyBulletin(getString(R.string.TextCopied)).show();
            }
        });
        buttonView.addView(copyButton, LayoutHelper.createFrame(48, 48, Gravity.BOTTOM | Gravity.RIGHT, 0, 16, 16, 16));

        containerView.addView(buttonView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL));

        textView.setText(geminiResult);
        adapter.updateMainView(textViewContainer);
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

    @Override
    public void dismissInternal() {
        super.dismissInternal();
    }

    public static void setMessageObject(MessageObject messageObject) {
        selectedObject = messageObject;
    }

    public static void setCurrentChat(TLRPC.Chat chat) {
        currentChat = chat;
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
        private AnimatedTextView betaHeader;
        private LinearLayout subtitleView;
        private AnimatedTextView toLanguageTextView;

        private View backgroundView;

        private ImageView menuIconImageView;

        private View shadow;

        private HeaderView(Context context, ChatActivity chatActivity, int subtitle) {
            super(context);

            backgroundView = new View(context);
            backgroundView.setBackgroundColor(getThemedColor(Theme.key_dialogBackground));
            addView(backgroundView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 44, Gravity.TOP | Gravity.FILL_HORIZONTAL, 0,  12, 0, 0));

            backButton = new ImageView(context);
            backButton.setScaleType(ImageView.ScaleType.CENTER);
            backButton.setImageResource(R.drawable.ic_ab_back);
            backButton.setColorFilter(new PorterDuffColorFilter(getThemedColor(Theme.key_dialogTextBlack), PorterDuff.Mode.MULTIPLY));
            backButton.setBackground(Theme.createSelectorDrawable(getThemedColor(Theme.key_listSelector)));
            backButton.setAlpha(0f);
            backButton.setOnClickListener(e -> dismiss());
            addView(backButton, LayoutHelper.createFrame(54, 54, Gravity.TOP, 1, 1, 1, 1));

            menuIconImageView = new ImageView(context);
            menuIconImageView.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_actionBarActionModeDefaultSelector, getResourcesProvider()), 1));
            menuIconImageView.setScaleType(ImageView.ScaleType.CENTER);
            menuIconImageView.setImageResource(R.drawable.arrow_more);
            menuIconImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogSearchIcon, getResourcesProvider()), PorterDuff.Mode.MULTIPLY));
            menuIconImageView.setOnClickListener(v -> dismiss());
            if (subtitle != 5) addView(menuIconImageView, LayoutHelper.createFrame(36, 36, Gravity.RIGHT | Gravity.TOP, 0, 11, 16, 0));

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
            titleTextView.setText(getString(R.string.CP_GeminiAI_Header));
            titleTextView.setPivotX(0);
            titleTextView.setPivotY(0);
            addView(titleTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.FILL_HORIZONTAL, 22, 20, 22, 0));

            betaHeader = new AnimatedTextView(context, true, false, false) {
                Drawable backgroundDrawable = Theme.createRoundRectDrawable(AndroidUtilities.dp(4), Theme.multAlpha(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader, getResourcesProvider()), 0.15f));

                @Override
                protected void onDraw(Canvas canvas) {
                    backgroundDrawable.setBounds(0, 0, (int) (getPaddingLeft() + getDrawable().getCurrentWidth() + getPaddingRight()), getMeasuredHeight());
                    backgroundDrawable.draw(canvas);

                    super.onDraw(canvas);
                }

                @Override
                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                    if (LocaleController.isRTL) {
                        betaHeader.setPivotX(getMeasuredWidth());
                    }
                }
            };
            betaHeader.setText("BETA");
            betaHeader.setTypeface(AndroidUtilities.bold());
            betaHeader.setPadding(AndroidUtilities.dp(5), AndroidUtilities.dp(2), AndroidUtilities.dp(5), AndroidUtilities.dp(2));
            betaHeader.setTextSize(AndroidUtilities.dp(10));
            betaHeader.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader, getResourcesProvider()));
            addView(betaHeader, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 17, Gravity.CENTER_VERTICAL, 115, 25, 0, 0));

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

            toLanguageTextView = new AnimatedTextView(context) {
                private Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                private LinkSpanDrawable.LinkCollector links = new LinkSpanDrawable.LinkCollector();

                @Override
                protected void onDraw(Canvas canvas) {
                    if (LocaleController.isRTL) {
                        AndroidUtilities.rectTmp.set(getWidth() - width(), (getHeight() - dp(18)) / 2f, getWidth(), (getHeight() + dp(18)) / 2f);
                    } else {
                        AndroidUtilities.rectTmp.set(0, (getHeight() - dp(18)) / 2f, width(), (getHeight() + dp(18)) / 2f);
                    }
                    bgPaint.setColor(Theme.multAlpha(getThemedColor(Theme.key_player_actionBarSubtitle), .1175f));
                    canvas.drawRoundRect(AndroidUtilities.rectTmp, dp(4), dp(4), bgPaint);
                    if (links.draw(canvas)) {
                        invalidate();
                    }

                    super.onDraw(canvas);
                }

                @Override
                public boolean onTouchEvent(MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        LinkSpanDrawable link = new LinkSpanDrawable(null, getResourcesProvider(), event.getX(), event.getY());
                        link.setColor(Theme.multAlpha(getThemedColor(Theme.key_player_actionBarSubtitle), .1175f));
                        LinkPath path = link.obtainNewPath();
                        if (LocaleController.isRTL) {
                            AndroidUtilities.rectTmp.set(getWidth() - width(), (getHeight() - dp(18)) / 2f, getWidth(), (getHeight() + dp(18)) / 2f);
                        } else {
                            AndroidUtilities.rectTmp.set(0, (getHeight() - dp(18)) / 2f, width(), (getHeight() + dp(18)) / 2f);
                        }
                        path.addRect(AndroidUtilities.rectTmp, Path.Direction.CW);
                        links.addLink(link);
                        invalidate();
                        return true;
                    } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            performClick();
                        }
                        links.clear();
                        invalidate();
                    }
                    return super.onTouchEvent(event);
                }
            };
            if (LocaleController.isRTL) {
                toLanguageTextView.setGravity(Gravity.RIGHT);
            }
            toLanguageTextView.setAnimationProperties(.25f, 0, 350, CubicBezierInterpolator.EASE_OUT_QUINT);
            toLanguageTextView.setTextColor(getThemedColor(Theme.key_player_actionBarSubtitle));
            toLanguageTextView.setTextSize(dp(14));

            String subtitleText = "";
            if (subtitle == GEMINI_TYPE_TRANSLATE) {
                subtitleText = capitalFirst(languageName(CherrygramChatsConfig.INSTANCE.getTranslationTargetGemini()));
            } else if (subtitle == GEMINI_TYPE_TRANSCRIBE) {
                subtitleText = getString(R.string.PremiumPreviewVoiceToText);
            } else if (subtitle == GEMINI_TYPE_EXPLANATION) {
                subtitleText = getString(R.string.AccDescrQuizExplanation);
            } else if (subtitle == GEMINI_TYPE_OCR) {
                subtitleText = getString(R.string.CP_GeminiAI_ExtractText);
            } if (subtitle == GEMINI_TYPE_SUMMARIZE) {
                subtitleText = getString(R.string.CP_GeminiAI_Summarize);
            }

            toLanguageTextView.setText(subtitleText);
            toLanguageTextView.setPadding(dp(4), dp(2), dp(4), dp(2));

            if (subtitle == GEMINI_TYPE_TRANSLATE) {
                toLanguageTextView.setOnClickListener(e -> {
                    openLanguagesSelect(() -> {
                        dismiss();
                        chatActivity.processGeminiWithText(selectedObject, null, true, false);
                    });
                });
            }

            subtitleView.addView(toLanguageTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 0, 0, 0, 0));
            addView(subtitleView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.FILL_HORIZONTAL, 20, 43, 22, 0));

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

            betaHeader.setScaleX(AndroidUtilities.lerp(.85f, 1f, t));
            betaHeader.setScaleY(AndroidUtilities.lerp(.85f, 1f, t));
            betaHeader.setTranslationY(AndroidUtilities.lerp(dpf2(-14), 0, t));
            if (!LocaleController.isRTL) {
                titleTextView.setTranslationX(AndroidUtilities.lerp(dpf2(50), 0, t));
                betaHeader.setTranslationX(AndroidUtilities.lerp(dpf2(35), 0, t));
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

        private void openLanguagesSelect(Runnable callback) {
            ArrayList<String> targetLanguages = new ArrayList<>(targetLanguagesForGemini);
            ArrayList<CharSequence> names = new ArrayList<>();
            for (String language : targetLanguages) {
                Locale locale = Locale.forLanguageTag(language);
                if (!TextUtils.isEmpty(locale.getScript())) {
                    names.add(HtmlCompat.fromHtml(String.format("%s - %s", CGResourcesHelper.INSTANCE.capitalize(locale.getDisplayScript()), CGResourcesHelper.INSTANCE.capitalize(locale.getDisplayScript(locale))), HtmlCompat.FROM_HTML_MODE_LEGACY).toString());
                } else {
                    names.add(String.format("%s - %s", CGResourcesHelper.INSTANCE.capitalize(locale.getDisplayName()), CGResourcesHelper.INSTANCE.capitalize(locale.getDisplayName(locale))));
                }
            }
            AndroidUtilities.selectionSort(names, targetLanguages);

            ActionBarPopupWindow.ActionBarPopupWindowLayout layout = new ActionBarPopupWindow.ActionBarPopupWindowLayout(getContext()) {
                @Override
                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    super.onMeasure(widthMeasureSpec,
                            MeasureSpec.makeMeasureSpec(Math.min((int) (AndroidUtilities.displaySize.y * .33f), MeasureSpec.getSize(heightMeasureSpec)), MeasureSpec.EXACTLY)
                    );
                }
            };

            Drawable shadowDrawable2 = ContextCompat.getDrawable(getContext(), R.drawable.popup_fixed_alert).mutate();
            shadowDrawable2.setColorFilter(new PorterDuffColorFilter(getThemedColor(Theme.key_actionBarDefaultSubmenuBackground), PorterDuff.Mode.MULTIPLY));
            layout.setBackground(shadowDrawable2);

            boolean first = true;
            for (int i = 0; i < targetLanguages.size(); ++i) {
                ActionBarMenuSubItem button = new ActionBarMenuSubItem(getContext(), 2, first, i == targetLanguages.size() - 1, resourcesProvider);
                button.setText(names.get(i));
                button.setChecked(TextUtils.equals(
                        targetLanguages.get(i),
                        CherrygramChatsConfig.INSTANCE.getTranslationTargetGemini()
                ));
                int finalI = i;
                button.setOnClickListener(e -> {
                    adapter.updateMainView(loadingTextView);
                    CherrygramChatsConfig.INSTANCE.setTranslationTargetGemini(targetLanguages.get(finalI));
                    callback.run();
                });
                layout.addView(button);

                first = false;
            }

            ActionBarPopupWindow window = new ActionBarPopupWindow(layout, LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
            window.setPauseNotifications(true);
            window.setDismissAnimationDuration(220);
            window.setOutsideTouchable(true);
            window.setClippingEnabled(true);
            window.setAnimationStyle(R.style.PopupContextAnimation);
            window.setFocusable(true);
            int[] location = new int[2];
            toLanguageTextView.getLocationInWindow(location);
            layout.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.displaySize.x, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(AndroidUtilities.displaySize.y, MeasureSpec.AT_MOST));
            int height = layout.getMeasuredHeight();
            int y = location[1] > AndroidUtilities.displaySize.y * .9f - height ? location[1] - height + dp(8) : location[1] + toLanguageTextView.getMeasuredHeight() - dp(8);
            window.showAtLocation(containerView, Gravity.TOP | Gravity.LEFT, location[0] - dp(8), y);
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

    public static GeminiResultsBottomSheet showAlert(BaseFragment fragment, ChatActivity chatActivity, String geminiResult, int subtitle) {
        GeminiResultsBottomSheet alert = new GeminiResultsBottomSheet(fragment, chatActivity, geminiResult, subtitle);
        if (fragment.getParentActivity() != null) {
            fragment.showDialog(alert);
        }
        alert.dimBehindAlpha = 140;
        return alert;
    }

    public static String capitalFirst(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    public static String languageName(String locale) {
        return languageName(locale, null);
    }

    private static String languageName(String locale, boolean[] accusative) {
        if (locale == null || locale.equals(TranslateController.UNKNOWN_LANGUAGE) || locale.equals("auto")) {
            return null;
        }

        String simplifiedLocale = locale.split("_")[0];
        if ("nb".equals(simplifiedLocale)) {
            simplifiedLocale = "no";
        }

        // getting localized language name in accusative case
        if (accusative != null) {
            String localed = LocaleController.getString("TranslateLanguage" + simplifiedLocale.toUpperCase());
            if (accusative[0] = (localed != null && !localed.startsWith("LOC_ERR"))) {
                return localed;
            }
        }

        // getting language name from system
        String systemLangName = systemLanguageName(locale);
        if (systemLangName == null) {
            systemLangName = systemLanguageName(simplifiedLocale);
        }
        if (systemLangName != null) {
            return systemLangName;
        }

        // getting language name from lang packs
        if ("no".equals(locale)) {
            locale = "nb";
        }
        final LocaleController.LocaleInfo currentLanguageInfo = LocaleController.getInstance().getCurrentLocaleInfo();
        final LocaleController.LocaleInfo thisLanguageInfo = LocaleController.getInstance().getBuiltinLanguageByPlural(locale);
        if (thisLanguageInfo == null) {
            return null;
        }
        boolean isCurrentLanguageEnglish = currentLanguageInfo != null && "en".equals(currentLanguageInfo.pluralLangCode);
        if (isCurrentLanguageEnglish) {
            return thisLanguageInfo.nameEnglish;
        } else {
            return thisLanguageInfo.name;
        }
    }

    private static String systemLanguageName(String langCode) {
        return systemLanguageName(langCode, false);
    }

    private static HashMap<String, Locale> localesByCode;
    private static String systemLanguageName(String langCode, boolean inItsOwnLocale) {
        if (langCode == null) {
            return null;
        }
        if (localesByCode == null) {
            localesByCode = new HashMap<>();
            try {
                Locale[] allLocales = Locale.getAvailableLocales();
                for (Locale allLocale : allLocales) {
                    localesByCode.put(allLocale.getLanguage(), allLocale);
                    String region = allLocale.getCountry();
                    if (region != null && !region.isEmpty()) {
                        localesByCode.put(allLocale.getLanguage() + "-" + region.toLowerCase(), allLocale);
                    }
                }
            } catch (Exception ignore) {}
        }
        langCode = langCode.replace("_", "-").toLowerCase();
        try {
            Locale locale = localesByCode.get(langCode);
            if (locale != null) {
                String name = locale.getDisplayLanguage(inItsOwnLocale ? locale : Locale.getDefault());
                if (langCode.contains("-")) {
                    String region = locale.getDisplayCountry(inItsOwnLocale ? locale : Locale.getDefault());
                    if (!TextUtils.isEmpty(region)) {
                        name += " (" + region + ")";
                    }
                }
                return name;
            }
        } catch (Exception ignore) {}
        return null;
    }

    private final List<String> targetLanguagesForGemini = Arrays.asList(
            "ar", "bn", "bg", "zh", "zh-CN", "zh-TW", "hr", "cs", "da", "nl",
            "en", "et", "fi", "fr", "de", "el", "iw", "hi", "hu", "id",
            "it", "ja", "ko", "lv", "lt", "no", "pl", "pt", "ro", "ru",
            "sr", "sk", "sl", "es", "sw", "sv", "th", "tr", "uk", "vi"
    ); // 40 Languages

}

