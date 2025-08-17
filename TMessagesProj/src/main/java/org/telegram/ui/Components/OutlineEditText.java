package org.telegram.ui.Components;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;

public class OutlineEditText extends OutlineTextContainerView {

    EditTextBoldCursor editText;

    public OutlineEditText(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context, resourcesProvider);

        editText = new EditTextBoldCursor(context) {
            @Override
            protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
                super.onFocusChanged(focused, direction, previouslyFocusedRect);
                animateSelection(focused || isFocused() ? 1f : 0f);
            }
        };
        editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        editText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        editText.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        editText.setBackground(null);
        editText.setSingleLine(true);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setTypeface(Typeface.DEFAULT);
        editText.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteInputFieldActivated));
        editText.setCursorWidth(1.5f);
        editText.setPadding(
                AndroidUtilities.dp(15), 0, AndroidUtilities.dp(15), 0
        );
        attachEditText(editText);

        addView(editText, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL));

    }

    public void setHint(String hint) {
        setText(hint);
    }

    public EditTextBoldCursor getEditText() {
        return editText;
    }

    public void setEnabled(boolean value, ArrayList<Animator> animators) {
        super.setEnabled(value);
        if (animators != null) {
            animators.add(ObjectAnimator.ofFloat(editText, View.ALPHA, value ? 1.0f : 0.5f));
        } else {
            editText.setAlpha(value ? 1.0f : 0.5f);
        }
    }
}
