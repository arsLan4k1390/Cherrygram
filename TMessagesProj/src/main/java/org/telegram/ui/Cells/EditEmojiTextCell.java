package org.telegram.ui.Cells;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;
import static org.telegram.ui.Components.EditTextEmoji.STYLE_GIFT;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedColor;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.EditTextCaption;
import org.telegram.ui.Components.EditTextEmoji;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.Components.TextStyleSpan;
import org.telegram.ui.Components.TypefaceSpan;

public class EditEmojiTextCell extends FrameLayout {

    private boolean ignoreEditText;
    public EditTextEmoji editTextEmoji;
    private int maxLength;

    private ImageView[] iconImageView = new ImageView[2];
    private View codeDividerView;

    private boolean showLimitWhenEmpty;
    private int showLimitWhenNear = -1;
    private boolean showLimitWhenFocused;

    public boolean autofocused;
    private boolean focused;

    private boolean allowEntities = true;

    AnimatedColor limitColor;
    private int limitCount;
    final AnimatedTextView.AnimatedTextDrawable limit = new AnimatedTextView.AnimatedTextDrawable(false, true, true); {
        limit.setAnimationProperties(.2f, 0, 160, CubicBezierInterpolator.EASE_OUT_QUINT);
        limit.setTextSize(dp(15.33f));
        limit.setGravity(Gravity.RIGHT);
    }

    public void setShowLimitWhenEmpty(boolean show) {
        showLimitWhenEmpty = show;
        if (showLimitWhenEmpty) {
            updateLimitText();
        }
    }

    public void setShowLimitWhenNear(int near) {
        showLimitWhenNear = near;
        updateLimitText();
    }

    private void updateLimitText() {
        if (editTextEmoji == null || editTextEmoji.getEditText() == null) return;
        limitCount = maxLength - getText().length();
        limit.setText(TextUtils.isEmpty(getText()) && !showLimitWhenEmpty || showLimitWhenFocused && (!focused || autofocused) || (showLimitWhenNear != -1 && limitCount > showLimitWhenNear) ? "" : "" + limitCount);
    }

    public void whenHitEnter(Runnable whenEnter) {
        editTextEmoji.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
        editTextEmoji.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    whenEnter.run();
                    return true;
                }
                return false;
            }
        });
    }

    public void hideKeyboardOnEnter() {
        whenHitEnter(() -> AndroidUtilities.hideKeyboard(editTextEmoji.getEditText()));
    }


    public void setShowLimitOnFocus(boolean show) {
        showLimitWhenFocused = show;
    }

    public EditEmojiTextCell(Context context, SizeNotifierFrameLayout parent, String hint, int style, boolean multiline) {
        this(context, parent, hint, multiline, -1, style, null);
    }

    public int emojiCacheType() {
        return AnimatedEmojiDrawable.getCacheTypeForEnterView();
    }

    public void setEmojiViewCacheType(int cacheType) {
        editTextEmoji.setEmojiViewCacheType(cacheType);
    }

    public EditEmojiTextCell setAllowEntities(boolean allow) {
        allowEntities = allow;
        return this;
    }

    public EditEmojiTextCell(Context context, SizeNotifierFrameLayout parent, String hint, boolean multiline, int maxLength, int style, Theme.ResourcesProvider resourceProvider) {
        this(context, parent, hint, multiline, maxLength, style, resourceProvider, null);
    }

    public EditEmojiTextCell(
        Context context,
        SizeNotifierFrameLayout parent,
        String hint,
        boolean multiline,
        int maxLength,
        int style,
        Theme.ResourcesProvider resourceProvider,
        OnClickListener onIconChangeListener
    ) {
        super(context);
        this.maxLength = maxLength;

        editTextEmoji = new EditTextEmoji(context, parent, null, style, true) {
            @Override
            protected boolean verifyDrawable(@NonNull Drawable who) {
                return who == limit || super.verifyDrawable(who);
            }

            @Override
            protected void onDraw(Canvas canvas) {
                canvas.save();
                canvas.clipRect(getScrollX() + getPaddingLeft(), 0, getScrollX() + getWidth() - getPaddingRight(), getHeight());
                super.onDraw(canvas);
                canvas.restore();

                if (limitColor != null) {
                    limit.setTextColor(limitColor.set(Theme.getColor(limitCount <= 0 ? Theme.key_text_RedRegular : Theme.key_dialogSearchHint, resourceProvider)));
                }
                int h = Math.min(dp(48), getHeight());
                final float ty = multiline ? 0 : -dp(1);
                limit.setBounds(getScrollX(), ty + getHeight() - h, getScrollX() + getWidth() - dp(12 + (!multiline ? 44 : 0)), ty + getHeight());
                limit.draw(canvas);
            }

            @Override
            protected void extendActionMode(ActionMode actionMode, Menu menu) {
                if (menu.findItem(R.id.menu_bold) != null) {
                    return;
                }
                if (Build.VERSION.SDK_INT >= 23) {
                    menu.removeItem(android.R.id.shareText);
                }
                int order = 6;
                menu.add(R.id.menu_groupbolditalic, R.id.menu_spoiler, order++, LocaleController.getString(R.string.Spoiler));
                SpannableStringBuilder stringBuilder = new SpannableStringBuilder(getString(R.string.Bold));
                stringBuilder.setSpan(new TypefaceSpan(AndroidUtilities.bold()), 0, stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                menu.add(R.id.menu_groupbolditalic, R.id.menu_bold, order++, stringBuilder);
                stringBuilder = new SpannableStringBuilder(getString(R.string.Italic));
                stringBuilder.setSpan(new TypefaceSpan(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM_ITALIC)), 0, stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                menu.add(R.id.menu_groupbolditalic, R.id.menu_italic, order++, stringBuilder);
                stringBuilder = new SpannableStringBuilder(LocaleController.getString(R.string.Strike));
                TextStyleSpan.TextStyleRun run = new TextStyleSpan.TextStyleRun();
                run.flags |= TextStyleSpan.FLAG_STYLE_STRIKE;
                stringBuilder.setSpan(new TextStyleSpan(run), 0, stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                menu.add(R.id.menu_groupbolditalic, R.id.menu_strike, order++, stringBuilder);
                menu.add(R.id.menu_groupbolditalic, R.id.menu_regular, order++, getString(R.string.Regular));
            }

            @Override
            protected boolean allowEntities() {
                return allowEntities && super.allowEntities();
            }

            @Override
            public int emojiCacheType() {
                return EditEmojiTextCell.this.emojiCacheType();
            }
        };
        final EditTextCaption editText = editTextEmoji.getEditText();
        editText.setDelegate(new EditTextCaption.EditTextCaptionDelegate() {
            @Override
            public void onSpansChanged() {
                onTextChanged(editText.getText());
            }
        });
        editTextEmoji.setWillNotDraw(false);
        limitColor = new AnimatedColor(editTextEmoji);
        limit.setCallback(editTextEmoji);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        editText.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText, resourceProvider));
        editText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourceProvider));
        editText.setBackground(null);
        if (multiline) {
            editText.setMaxLines(5);
            editText.setSingleLine(false);
        } else {
            editText.setMaxLines(1);
            editText.setSingleLine(true);
        }
        editText.setPadding(editText.getPaddingLeft(), editText.getPaddingTop(), dp(style == STYLE_GIFT ? 0 : (maxLength > 0 ? 42 : 0) + 21), editText.getPaddingBottom());
        editText.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_CLASS_TEXT | (multiline ? InputType.TYPE_TEXT_FLAG_MULTI_LINE : 0) | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editText.setRawInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editText.setHint(hint);
        editText.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourceProvider));
        editText.setCursorSize(dp(19));
        editText.setCursorWidth(1.5f);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (!ignoreEditText) {
                    autofocused = false;
                }
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            @Override
            public void afterTextChanged(Editable editable) {
                if (!ignoreEditText) {
                    if (maxLength > 0 && editable != null && editable.length() > maxLength) {
                        ignoreEditText = true;
                        editText.setText(editable.subSequence(0, maxLength));
                        editText.setSelection(editText.length());
                        ignoreEditText = false;
                    }
                    EditEmojiTextCell.this.onTextChanged(editable);
                }

                if (multiline) {
                    int pos;
                    while ((pos = editable.toString().indexOf("\n")) >= 0) {
                        editable.delete(pos, pos + 1);
                    }
                }

                if (limit != null && maxLength > 0) {
                    limit.cancelAnimation();
                    updateLimitText();
                }
            }
        });
        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                focused = hasFocus;
                if (showLimitWhenFocused) {
                    updateLimitText();
                }
                onFocusChanged(hasFocus);
            }
        });
        addView(editTextEmoji, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP));

        if (onIconChangeListener != null) {
            setOnChangeIconListener(onIconChangeListener);
        }

        updateLimitText();
    }

    public void setOnChangeIconListener(OnClickListener listener) {
        editTextEmoji.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.RIGHT | Gravity.TOP, 72 - 7 - 21, 0, 0, 0));

        codeDividerView = new View(getContext());
        codeDividerView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhiteInputField));
        LinearLayout.LayoutParams params = LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 54, 10, 8, 10);
        params.width = Math.max(4, AndroidUtilities.dp(0.5f));
        params.height = Math.max(90, AndroidUtilities.dp(0.5f));
        addView(codeDividerView, params);

        for (int i = 0; i < iconImageView.length; i++) {
            iconImageView[i] = new ImageView(getContext());
            iconImageView[i].setFocusable(true);
            iconImageView[i].setVisibility(i == 0 ? VISIBLE : GONE);
            iconImageView[i].setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_stickers_menuSelector)));
            iconImageView[i].setScaleType(ImageView.ScaleType.CENTER);
            iconImageView[i].setOnClickListener(listener);
            iconImageView[i].setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon), PorterDuff.Mode.MULTIPLY));
            iconImageView[i].setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
            addView(iconImageView[i], LayoutHelper.createFrame(48, 48, Gravity.LEFT | Gravity.CENTER_VERTICAL, 12, 0, 8, 0));
        }
    }

    public void setIcon(int icon, boolean animated) {
        iconImageView[animated ? 1 : 0].setImageResource(icon);
        if (animated) {
            ImageView tmp = iconImageView[0];
            iconImageView[0] = iconImageView[1];
            iconImageView[1] = tmp;
        }
        AndroidUtilities.updateViewVisibilityAnimated(iconImageView[0], true, 0.5f, animated);
        AndroidUtilities.updateViewVisibilityAnimated(iconImageView[1], false, 0.5f, animated);
    }

    public void setText(CharSequence text) {
        ignoreEditText = true;
        editTextEmoji.setText(text);
        editTextEmoji.setSelection(editTextEmoji.getText().length());
        ignoreEditText = false;
    }

    public CharSequence getText() {
        return editTextEmoji.getText();
    }

    public boolean validate() {
        return maxLength < 0 || editTextEmoji.getText().length() <= maxLength;
    }

    protected void onTextChanged(CharSequence newText) {

    }

    protected void onFocusChanged(boolean focused) {

    }

    private boolean needDivider;
    public void setDivider(boolean divider) {
        setWillNotDraw(!(needDivider = divider));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (needDivider) {
            canvas.drawLine(
                    LocaleController.isRTL ? 0 : dp(22),
                    getMeasuredHeight() - 1,
                    getMeasuredWidth() - (LocaleController.isRTL ? dp(22) : 0),
                    getMeasuredHeight() - 1,
                    Theme.dividerPaint
            );
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), heightMeasureSpec);
    }
}