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
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.OutlineEditText;
import org.telegram.ui.Stories.recorder.HintView2;

import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig;
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper;

public class GeminiPreferencesBottomSheet extends BottomSheet {

    private BaseFragment fragment;
    private LinearLayout linearLayout;

    private OutlineEditText geminiApiKeyField;
    private OutlineEditText geminiModelNameField;

    public GeminiPreferencesBottomSheet(Context context) {
        super(context, true);
        setOpenNoDelay(true);
        fixNavigationBar();
        smoothKeyboardAnimationEnabled = true;

        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        FrameLayout header = new FrameLayout(context);
        linearLayout.addView(header, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 21, 6, 0, 3));

        SimpleTextView nameView = new SimpleTextView(context);
        nameView.setTextSize(20);
        nameView.setTypeface(AndroidUtilities.bold());
        nameView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        nameView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        nameView.setText(getString(R.string.CP_GeminiAI_Header));
        header.addView(nameView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 30, Gravity.LEFT, 0, 6, 0, 0));

        AnimatedTextView betaHeader = new AnimatedTextView(context, true, false, false) {
            Drawable backgroundDrawable = Theme.createRoundRectDrawable(AndroidUtilities.dp(4), Theme.multAlpha(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader), 0.15f));

            @Override
            protected void onDraw(Canvas canvas) {
                backgroundDrawable.setBounds(0, 0, (int) (getPaddingLeft() + getDrawable().getCurrentWidth() + getPaddingRight()), getMeasuredHeight());
                backgroundDrawable.draw(canvas);

                super.onDraw(canvas);
            }
        };
        betaHeader.setText("BETA");
        betaHeader.setTypeface(AndroidUtilities.bold());
        betaHeader.setPadding(AndroidUtilities.dp(5), AndroidUtilities.dp(2), AndroidUtilities.dp(5), AndroidUtilities.dp(2));
        betaHeader.setTextSize(AndroidUtilities.dp(10));
        betaHeader.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
        header.addView(betaHeader, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 17, Gravity.CENTER_VERTICAL, 95, 12, 0, 0));

        geminiApiKeyField = new OutlineEditText(context, resourcesProvider);
        geminiApiKeyField.getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        geminiApiKeyField.getEditText().setImeOptions(EditorInfo.IME_ACTION_NEXT);
        geminiApiKeyField.setHint(getString(R.string.CP_GeminiAI_API_Key));
        geminiApiKeyField.getEditText().setSingleLine(false);
        geminiApiKeyField.getEditText().setFilters(getInputFilter());
        geminiApiKeyField.getEditText().setText(CherrygramChatsConfig.INSTANCE.getGeminiApiKey());
        linearLayout.addView(geminiApiKeyField, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 80, 0, 16, 15, 16, 3));

        TextInfoPrivacyCell geminiApiKeyAdviceCell = new TextInfoPrivacyCell(context, dp(1), resourcesProvider);
        geminiApiKeyAdviceCell.setText(CGResourcesHelper.getGeminiApiKeyAdvice());
        linearLayout.addView(geminiApiKeyAdviceCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 58, 16, 0, 16, 0));

        View geminiApiKeyAdviceCellDivider = new View(context) {
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                canvas.drawLine(0, AndroidUtilities.dp(1), getMeasuredWidth(), AndroidUtilities.dp(1), Theme.dividerPaint);
            }
        };
        linearLayout.addView(geminiApiKeyAdviceCellDivider, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(1), 0, 16, 0, 16, 0));

        geminiModelNameField = new OutlineEditText(context, resourcesProvider);
        geminiModelNameField.getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        geminiModelNameField.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
        geminiModelNameField.setHint(getString(R.string.CP_GeminiAI_Model));
        geminiModelNameField.getEditText().setSingleLine(true);
        geminiModelNameField.getEditText().setFilters(getInputFilter());
        geminiModelNameField.getEditText().setHint(getString(R.string.CP_GeminiAI_Sample) + " gemini-1.5-flash");
        geminiModelNameField.getEditText().setText(CherrygramChatsConfig.INSTANCE.getGeminiModelName());
        linearLayout.addView(geminiModelNameField, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 58, 0, 16, 15, 16, 3));

        TextInfoPrivacyCell geminiModelAdviceCell = new TextInfoPrivacyCell(context, dp(1), resourcesProvider);
        geminiModelAdviceCell.setText(CGResourcesHelper.getGeminiModelNameAdvice());
        linearLayout.addView(geminiModelAdviceCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 58, 16, 0, 16, 0));

        View geminiModelAdviceCellDivider = new View(context) {
            @Override
            protected void onDraw(@NonNull Canvas canvas) {
                super.onDraw(canvas);
                canvas.drawLine(0, AndroidUtilities.dp(1), getMeasuredWidth(), AndroidUtilities.dp(1), Theme.dividerPaint);
            }
        };
        linearLayout.addView(geminiModelAdviceCellDivider, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(1), 0, 16, 0, 16, 0));

        FrameLayout buttonView = new FrameLayout(context);
        buttonView.setBackgroundColor(getThemedColor(Theme.key_dialogBackground));

        TextView doneButton = new TextView(context);
        doneButton.setEllipsize(TextUtils.TruncateAt.END);
        doneButton.setGravity(Gravity.CENTER_HORIZONTAL);
        doneButton.setGravity(Gravity.CENTER);
        doneButton.setText(getString(R.string.Done));
        doneButton.setTypeface(AndroidUtilities.bold());
        doneButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        doneButton.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText, resourcesProvider));
        doneButton.setBackground(Theme.AdaptiveRipple.filledRect(Theme.getColor(Theme.key_featuredStickers_addButton, resourcesProvider), 6));
        doneButton.setOnClickListener(v -> doOnDone());
        buttonView.addView(doneButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 16, 16, 72, 16));

        HintView2 hintView = new HintView2(context, HintView2.DIRECTION_BOTTOM)
                .setRounding(10)
                .setDuration(5000)
                .setCloseButton(true)
                .setMaxWidth(180)
                .setMultilineText(true)
                .setText(getString(R.string.CP_GeminiAI_Instruction))
                .setJoint(1, 40)
                .setBgColor(getThemedColor(Theme.key_undo_background));
        container.addView(hintView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.RIGHT, dp(20), 0, dp(8), dp(20)));

        ImageView infoButton = new ImageView(context);
        infoButton.setScaleType(ImageView.ScaleType.CENTER);
        infoButton.setImageResource(R.drawable.msg_info);
        infoButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_featuredStickers_buttonText, resourcesProvider), PorterDuff.Mode.MULTIPLY));
        infoButton.setBackground(Theme.AdaptiveRipple.filledRect(Theme.getColor(Theme.key_featuredStickers_addButton, resourcesProvider), 6));
        infoButton.setOnClickListener(v -> hintView.show());
        buttonView.addView(infoButton, LayoutHelper.createFrame(48, 48, Gravity.BOTTOM | Gravity.RIGHT, 0, 16, 16, 16));

        linearLayout.addView(buttonView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL));

        ScrollView scrollView = new ScrollView(context);
        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.addView(linearLayout);
        setCustomView(scrollView);
    }

    public void setFragment(BaseFragment fragment) {
        this.fragment = fragment;
    }

    private InputFilter[] getInputFilter() {
        InputFilter filter = (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                if (Character.isWhitespace(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        };
        return new InputFilter[] { filter };
    }

    private void doOnDone() {
        if (fragment == null || fragment.getParentActivity() == null) {
            return;
        }

        if (geminiApiKeyField.getEditText().length() == 0) {
            Vibrator v = (Vibrator) fragment.getParentActivity().getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                v.vibrate(200);
            }
            AndroidUtilities.shakeView(geminiApiKeyField);
            return;
        }
        CherrygramChatsConfig.INSTANCE.setGeminiApiKey(
                geminiApiKeyField.getEditText().getText().toString()
        );

        if (geminiModelNameField.getEditText().length() == 0) {
            Vibrator v = (Vibrator) fragment.getParentActivity().getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                v.vibrate(200);
            }
            AndroidUtilities.shakeView(geminiModelNameField);
            return;
        }
        CherrygramChatsConfig.INSTANCE.setGeminiModelName(
                geminiModelNameField.getEditText().getText().toString()
        );

        dismiss();
    }

    @Override
    public void show() {
        super.show();
        geminiApiKeyField.getEditText().requestFocus();
        geminiApiKeyField.getEditText().setSelection(geminiApiKeyField.getEditText().length());
    }

    @Override
    public void dismiss() {
        super.dismiss();
        AndroidUtilities.runOnUIThread(() -> AndroidUtilities.hideKeyboard(linearLayout), 50);
    }

    public static GeminiPreferencesBottomSheet showAlert(BaseFragment fragment) {
        GeminiPreferencesBottomSheet alert = new GeminiPreferencesBottomSheet(fragment.getContext());
        if (fragment != null) {
            if (fragment.getParentActivity() != null) {
                fragment.showDialog(alert);
            }
            alert.setFragment(fragment);
        } else {
            alert.show();
        }
        return alert;
    }
}
