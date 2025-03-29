/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.helpers.ui;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.RadioColorCell;
import org.telegram.ui.Cells.TextCell;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.preferences.ChatsPreferencesEntry;

public class PopupHelper {

    public static void show(ArrayList<? extends CharSequence> entries, String title, int checkedIndex, Context context, OnItemClickListener listener) {
        show(entries, title, checkedIndex, context, listener, null);
    }

    public static void show(ArrayList<? extends CharSequence> entries, String title, int checkedIndex, Context context, OnItemClickListener listener, Theme.ResourcesProvider resourcesProvider) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, resourcesProvider);
        builder.setTitle(title);
        final LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        builder.setView(linearLayout);

        for (int a = 0; a < entries.size(); a++) {
            RadioColorCell cell = new RadioColorCell(context);
            cell.setPadding(AndroidUtilities.dp(4), 0, AndroidUtilities.dp(4), 0);
            cell.setTag(a);
            cell.setCheckColor(Theme.getColor(Theme.key_radioBackground, resourcesProvider), Theme.getColor(Theme.key_dialogRadioBackgroundChecked, resourcesProvider));
            cell.setTextAndValue(entries.get(a), checkedIndex == a);
            linearLayout.addView(cell);
            cell.setOnClickListener(v -> {
                Integer which = (Integer) v.getTag();
                builder.getDismissRunnable().run();
                listener.onClick(which);
            });
        }
        builder.setNegativeButton(getString(R.string.Cancel), null);
        builder.show();
    }

    public static void show(
            String title,
            ArrayList<String> prefTitle,
            ArrayList<String> prefDesc,
            int checkedIndex,
            Context context,
            OnItemClickListener listener,
            Theme.ResourcesProvider resourcesProvider
    ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, resourcesProvider);
        builder.setTitle(title);
        final LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        builder.setView(linearLayout);

        for (int a = 0; a < prefTitle.size(); a++) {
            RadioColorCell cell = new RadioColorCell(context);
            cell.setPadding(AndroidUtilities.dp(4), 0, AndroidUtilities.dp(4), 0);
            cell.setTag(a);
            cell.setCheckColor(Theme.getColor(Theme.key_radioBackground, resourcesProvider), Theme.getColor(Theme.key_dialogRadioBackgroundChecked, resourcesProvider));
            cell.setTextAndText2AndValue(prefTitle.get(a), prefDesc.get(a), checkedIndex == a);
            linearLayout.addView(cell);
            cell.setOnClickListener(v -> {
                Integer which = (Integer) v.getTag();
                builder.getDismissRunnable().run();
                listener.onClick(which);
            });
        }
        builder.setNegativeButton(getString(R.string.Cancel), null);
        builder.show();
    }

    public interface OnItemClickListener {
        void onClick(int i);
    }

    public static void showSwitchAlert(
            String title,
            BaseFragment fragment,
            ArrayList<String> prefTitle,
            ArrayList<Integer> prefIcon,
            ArrayList<Boolean> prefCheck,
            ArrayList<Boolean> prefCheckInvisible,
            ArrayList<Boolean> prefDivider,
            ArrayList<Runnable> clickListener,
            Runnable dismissRunnable
    ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getContext(), fragment.getResourceProvider());
        builder.setTitle(title);
        final LinearLayout linearLayout = new LinearLayout(fragment.getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        for (int a = 0; a < prefTitle.size(); a++) {
            TextCell textCell = new TextCell(fragment.getContext(), 23, false, true, fragment.getResourceProvider());
            textCell.setTextAndCheckAndIcon(prefTitle.get(a), prefCheck.get(a), prefIcon.get(a), prefDivider.get(a));

            textCell.setTag(a);
            textCell.setBackground(Theme.getSelectorDrawable(false));

            if (prefCheckInvisible != null && prefCheckInvisible.get(a)) {
                textCell.getCheckBox().setVisibility(View.INVISIBLE);
            }

            linearLayout.addView(textCell);
            int finalA = a;
            textCell.setOnClickListener(v -> {
                boolean newValue = !prefCheck.get(finalA);
                prefCheck.set(finalA, newValue);
                textCell.setChecked(newValue);
                clickListener.get(finalA).run();
            });
        }

        builder.setView(linearLayout);

        if (title.equals(getString(R.string.CP_AdminActions))) {
            builder.setNegativeButton(getString(R.string.Back),
                    ((dialog, which) -> ChatsPreferencesEntry.INSTANCE.showChatMenuItemsConfigurator(fragment))
            );
        }

        builder.setPositiveButton(getString(R.string.Close), dismissRunnable != null ? (dialogInterface, i) -> dismissRunnable.run() : null);
        if (dismissRunnable != null) builder.setOnDismissListener(v -> dismissRunnable.run());
        fragment.showDialog(builder.create());
    }

}

