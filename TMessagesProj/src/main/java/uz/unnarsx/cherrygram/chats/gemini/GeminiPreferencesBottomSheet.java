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
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.OutlineEditText;
import org.telegram.ui.Stories.recorder.HintView2;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig;
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper;

public class GeminiPreferencesBottomSheet extends BottomSheet {

    private LinearLayout contentLayout;

    private OutlineEditText geminiApiKeyField;
    private OutlineEditText geminiModelNameField;

    private final BaseFragment parentFragment;

    public GeminiPreferencesBottomSheet(BaseFragment parentFragment, Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context, true, resourcesProvider);
        fixNavigationBar();
        waitingKeyboard = true;
        smoothKeyboardAnimationEnabled = true;
        this.parentFragment = parentFragment;
        setCustomView(createView(getContext(), resourcesProvider));
        setTitle(getString(R.string.CP_GeminiAI_Header), true);
    }

    public View createView(Context context, Theme.ResourcesProvider resourcesProvider) {
        contentLayout = new LinearLayout(context);
        contentLayout.setPadding(dp(20), dp(5), dp(20), 0);
        contentLayout.setOrientation(LinearLayout.VERTICAL);

        InputFilter filter = (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                if (Character.isWhitespace(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        };

        geminiApiKeyField = new OutlineEditText(context, resourcesProvider);
        geminiApiKeyField.getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        geminiApiKeyField.getEditText().setImeOptions(EditorInfo.IME_ACTION_NEXT);
        geminiApiKeyField.setHint(getString(R.string.CP_GeminiAI_API_Key));
        geminiApiKeyField.getEditText().setSingleLine(false);
        geminiApiKeyField.getEditText().setFilters(new InputFilter[] { filter });
        geminiApiKeyField.getEditText().setText(CherrygramChatsConfig.INSTANCE.getGeminiApiKey());
        contentLayout.addView(geminiApiKeyField, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 80, Gravity.LEFT | Gravity.TOP, 0, 0, 0, 0));

        TextInfoPrivacyCell geminiApiKeyAdviceCell = new TextInfoPrivacyCell(context, dp(1), resourcesProvider);
        geminiApiKeyAdviceCell.setText(CGResourcesHelper.getGeminiApiKeyAdvice());
        contentLayout.addView(geminiApiKeyAdviceCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 58, 0, 3, 0, 0));

        View geminiApiKeyAdviceCellDivider = new View(context) {
            @Override
            protected void onDraw(@NonNull Canvas canvas) {
                super.onDraw(canvas);
                canvas.drawLine(0, AndroidUtilities.dp(1), getMeasuredWidth(), AndroidUtilities.dp(1), Theme.dividerPaint);
            }
        };
        contentLayout.addView(geminiApiKeyAdviceCellDivider, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(1)));

        geminiModelNameField = new OutlineEditText(context, resourcesProvider);
        geminiModelNameField.getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        geminiModelNameField.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
        geminiModelNameField.setHint(getString(R.string.CP_GeminiAI_Model));
        geminiModelNameField.getEditText().setSingleLine(true);
        geminiModelNameField.getEditText().setFilters(new InputFilter[] { filter });
        geminiModelNameField.getEditText().setHint(getString(R.string.CP_GeminiAI_Sample) + " gemini-1.5-flash");
        geminiModelNameField.getEditText().setText(CherrygramChatsConfig.INSTANCE.getGeminiModelName());
        contentLayout.addView(geminiModelNameField, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 58, Gravity.LEFT | Gravity.TOP, 0, 10, 0, 0));

        TextInfoPrivacyCell geminiModelAdviceCell = new TextInfoPrivacyCell(context, dp(1), resourcesProvider);
        geminiModelAdviceCell.setText(CGResourcesHelper.getGeminiModelNameAdvice());
        contentLayout.addView(geminiModelAdviceCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 58, 0, 3, 0, 0));

        View geminiModelAdviceCellDivider = new View(context) {
            @Override
            protected void onDraw(@NonNull Canvas canvas) {
                super.onDraw(canvas);
                canvas.drawLine(0, AndroidUtilities.dp(1), getMeasuredWidth(), AndroidUtilities.dp(1), Theme.dividerPaint);
            }
        };
        contentLayout.addView(geminiModelAdviceCellDivider, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(1)));

        FrameLayout buttonView = new FrameLayout(context);
        buttonView.setBackgroundColor(Theme.getColor(Theme.key_dialogBackground, resourcesProvider));

        TextView doneButton = new TextView(context);
        doneButton.setEllipsize(TextUtils.TruncateAt.END);
        doneButton.setGravity(Gravity.CENTER);
        doneButton.setBackground(Theme.AdaptiveRipple.filledRect(Theme.getColor(Theme.key_featuredStickers_addButton, resourcesProvider), 6));
        doneButton.setText(getString(R.string.Done));
        doneButton.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText, resourcesProvider));
        doneButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        doneButton.setTypeface(AndroidUtilities.bold());
        doneButton.setOnClickListener(v -> doOnDone());
        buttonView.addView(doneButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM | Gravity.LEFT, 0, 0, dp(16), 0));

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
        buttonView.addView(infoButton, LayoutHelper.createFrame(48, 48, Gravity.BOTTOM | Gravity.RIGHT, 0, 0, 0, 0));

        contentLayout.addView(buttonView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, 0, 0, 16, 0, 16));

        ScrollView fragmentView = new ScrollView(context);
        fragmentView.setVerticalScrollBarEnabled(false);
        fragmentView.addView(contentLayout, LayoutHelper.createScroll(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP));

        return fragmentView;
    }

    private void doOnDone() {
        if (parentFragment == null || parentFragment.getParentActivity() == null) {
            return;
        }

        if (geminiApiKeyField.getEditText().length() == 0) {
            Vibrator v = (Vibrator) parentFragment.getParentActivity().getSystemService(Context.VIBRATOR_SERVICE);
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
            Vibrator v = (Vibrator) parentFragment.getParentActivity().getSystemService(Context.VIBRATOR_SERVICE);
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
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();

        themeDescriptions.add(new ThemeDescription(geminiApiKeyField, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(geminiApiKeyField, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteHintText));
        themeDescriptions.add(new ThemeDescription(geminiApiKeyField, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_windowBackgroundWhiteInputField));
        themeDescriptions.add(new ThemeDescription(geminiApiKeyField, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, Theme.key_windowBackgroundWhiteInputFieldActivated));

        themeDescriptions.add(new ThemeDescription(geminiModelNameField, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(geminiModelNameField, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteHintText));
        themeDescriptions.add(new ThemeDescription(geminiModelNameField, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_windowBackgroundWhiteInputField));
        themeDescriptions.add(new ThemeDescription(geminiModelNameField, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, Theme.key_windowBackgroundWhiteInputFieldActivated));

        return themeDescriptions;
    }

    @Override
    public void dismiss() {
        super.dismiss();
        AndroidUtilities.runOnUIThread(() -> AndroidUtilities.hideKeyboard(contentLayout), 50);
    }

}
