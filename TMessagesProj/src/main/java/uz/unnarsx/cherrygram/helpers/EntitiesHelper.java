package uz.unnarsx.cherrygram.helpers;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.URLSpan;
import android.view.View;

import org.telegram.ui.Components.URLSpanNoUnderline;

public class EntitiesHelper {

    public static CharSequence getUrlNoUnderlineText(CharSequence charSequence) {
        Spannable spannable = new SpannableString(charSequence);
        URLSpan[] spans = spannable.getSpans(0, charSequence.length(), URLSpan.class);
        for (URLSpan urlSpan : spans) {
            URLSpan span = urlSpan;
            int start = spannable.getSpanStart(span);
            int end = spannable.getSpanEnd(span);
            spannable.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL()) {
                @Override
                public void onClick(View widget) {
                    super.onClick(widget);
                }
            };
            spannable.setSpan(span, start, end, 0);
        }
        return spannable;
    }

}
