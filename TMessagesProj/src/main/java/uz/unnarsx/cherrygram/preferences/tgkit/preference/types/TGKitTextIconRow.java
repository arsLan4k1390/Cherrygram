/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.preferences.tgkit.preference.types;

import androidx.annotation.Nullable;

import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.TextCell;

import uz.unnarsx.cherrygram.preferences.tgkit.preference.TGKitPreference;

public class TGKitTextIconRow extends TGKitPreference {
    public boolean divider = false;
    public int icon = -1;

    @Nullable
    public String value = null;

    @Nullable
    public String subtitle = null;

    @Nullable
    public TGTIListener listener;

    public void bindCell(TextCell cell) {
        if (icon != -1 && value != null) {
            cell.setTextAndValueAndIcon(title, value, icon, divider);
        } else if (icon != -1 && subtitle != null) {
            cell.setTextAndIcon(title, icon, divider);
            cell.setSubtitle(subtitle);
        } else if (value != null) {
            cell.setTextAndValue(title, value, divider);
        } else if (icon != -1) {
            cell.setTextAndIcon(title, icon, divider);
        } else {
            cell.setText(title, divider);
        }
    }

    @Override
    public TGPType getType() {
        return TGPType.TEXT_ICON;
    }

    public interface TGTIListener {
        void onClick(BaseFragment bf);

        default void onLongClick(BaseFragment bf) {
        }
    }

}
