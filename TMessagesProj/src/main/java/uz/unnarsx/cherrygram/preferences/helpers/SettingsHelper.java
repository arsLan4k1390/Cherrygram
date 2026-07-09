/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.preferences.helpers;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.View;

import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.FilterCreateActivity;
import org.telegram.ui.SettingsActivity;

import uz.unnarsx.cherrygram.core.CherrygramLogger;

public class SettingsHelper {

    public static UItem asCustomWithBackground(int id, View view) {
        UItem i = new UItem(UniversalAdapter.VIEW_TYPE_CUSTOM_WITH_BACKGROUND, false);
        i.id = id;
        i.view = view;
        i.intValue = LayoutHelper.MATCH_PARENT;
        return i;
    }

    public static UItem asCustomWithBackground(View view) {
        UItem i = new UItem(UniversalAdapter.VIEW_TYPE_CUSTOM_WITH_BACKGROUND, false);
        i.view = view;
        i.intValue = LayoutHelper.MATCH_PARENT;
        return i;
    }

    public static UItem asCustomWithBackground(int id, View view, int heightDp) {
        UItem i = new UItem(UniversalAdapter.VIEW_TYPE_CUSTOM_WITH_BACKGROUND, false);
        i.id = id;
        i.view = view;
        i.intValue = heightDp;
        return i;
    }

    public static UItem asCustomWithBackground(View view, int heightDp) {
        UItem i = new UItem(UniversalAdapter.VIEW_TYPE_CUSTOM_WITH_BACKGROUND, false);
        i.view = view;
        i.intValue = heightDp;
        return i;
    }

    public static UItem asTextDetail(int id, int iconResId, CharSequence text, CharSequence value) {
        UItem i = new UItem(UniversalAdapter.VIEW_TYPE_TEXT_DETAIL_SETTINGS, false);
        i.id = id;
        i.iconResId = iconResId;
        i.text = text;
        i.textValue = value;
        return i;
    }

    public static UItem asSpaceCG(int height) {
        UItem item = new UItem(UniversalAdapter.VIEW_TYPE_SPACE_CG, false);
        item.intValue = height;
        return item;
    }

    public static UItem asSwitchCG(int id, CharSequence text) {
        UItem i = new UItem(UniversalAdapter.VIEW_TYPE_CHECK, false);
        i.id = id;
        i.text = text;
        return i;
    }

    public static UItem asSwitchCG(int id, CharSequence text, CharSequence subtext) {
        UItem i = new UItem(UniversalAdapter.VIEW_TYPE_TEXT_CHECK, false);
        i.id = id;
        i.text = text;
        i.subtext = subtext;
        return i;
    }

    public static UItem asExpandableSwitch(int id, CharSequence text, CharSequence subText) {
        UItem item = new UItem(UniversalAdapter.VIEW_TYPE_EXPANDABLE_SWITCH, false);
        item.id = id;
        item.text = text;
        item.animatedText = subText;
        return item;
    }

    public static UItem asExpandableSwitch(int id, int iconResId, CharSequence text, CharSequence subText) {
        UItem item = new UItem(UniversalAdapter.VIEW_TYPE_EXPANDABLE_SWITCH, false);
        item.id = id;
        item.red = false;
        item.iconResId = iconResId;
        item.text = text;
        item.animatedText = subText;
        return item;
    }

    public static UItem asRoundGroupCheckbox(int id, CharSequence text, CharSequence subtext) {
        UItem item = new UItem(UniversalAdapter.VIEW_TYPE_ROUND_GROUP_CHECKBOX, false);
        item.id = id;
        item.text = text;
        item.subtext = subtext;
        return item;
    }

    public static void updateCheckState(View view, boolean isChecked) {
        if (view instanceof NotificationsCheckCell notificationsCheckCell) {
            notificationsCheckCell.setChecked(isChecked);
        } else if (view instanceof TextCheckCell textCheckCell) {
            textCheckCell.setChecked(isChecked);
        } else {
            if (view != null) {
                CherrygramLogger.e(() -> "Unknown view type for setChecked: " + view.getClass().getName());
            } else {
                CherrygramLogger.e(() -> "Attempted to update check state on a NULL view");
            }
        }
    }

    public static void updateButtonValue(View view, String value) {
        if (view instanceof TextCell textCell) {
            textCell.setValue(value, true);
        } else if (view instanceof TextSettingsCell textSettingsCell) {
            textSettingsCell.getValueTextView().setText(value);
        } else if (view instanceof TextDetailSettingsCell textDetailSettingsCell) {
            textDetailSettingsCell.getValueTextView().setText(value);
        } else if (view instanceof SettingsActivity.SettingCell settingCell) {
            settingCell.setValueCG(value);
        } else {
            if (view != null) {
                CherrygramLogger.e(() -> "Unknown view type for setChecked: " + view.getClass().getName());
            } else {
                CherrygramLogger.e(() -> "Attempted to update check state on a NULL view");
            }
        }
    }

    public static CharSequence applyNewSpan(CharSequence str) {
        return applyNewSpan(str, false);
    }
    public static CharSequence applyNewSpan(CharSequence str, boolean outline) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(str);
        spannableStringBuilder.append("  d");
        FilterCreateActivity.NewSpan span = new FilterCreateActivity.NewSpan(outline, 10);
        span.usePaintAlpha = true;
        span.setColor(Theme.getColor(Theme.key_premiumGradient1));
        spannableStringBuilder.setSpan(span, spannableStringBuilder.length() - 1, spannableStringBuilder.length(), 0);
        return spannableStringBuilder;
    }

    public static CharSequence applyProSpan(CharSequence str, Theme.ResourcesProvider resourcesProvider) {
        return applySpan(str, "PRO", Theme.key_cgGradient2, resourcesProvider);
    }

    public static CharSequence applySpan(CharSequence str, String badgeText, int colorKey, Theme.ResourcesProvider resourcesProvider) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(str);
        spannableStringBuilder.append("  d");

        FilterCreateActivity.TextSpan span = new FilterCreateActivity.TextSpan(badgeText, 10, colorKey, resourcesProvider);
        span.backgroundHeight = dp(16.66f);

        spannableStringBuilder.setSpan(span, spannableStringBuilder.length() - 1, spannableStringBuilder.length(), 0);
        return spannableStringBuilder;
    }

}
