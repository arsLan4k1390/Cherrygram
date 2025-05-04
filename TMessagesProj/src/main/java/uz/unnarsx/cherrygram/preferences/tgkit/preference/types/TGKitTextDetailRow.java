/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.preferences.tgkit.preference.types;

import androidx.annotation.Nullable;

import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.TextDetailSettingsCell;

import uz.unnarsx.cherrygram.preferences.tgkit.preference.TGKitPreference;

public class TGKitTextDetailRow extends TGKitPreference {
    public int icon = -1;
    public String detail;
    public boolean divider;

    @Nullable
    public TGTDListener listener;

    public void bindCell(TextDetailSettingsCell textDetailCell) {
        if (icon != -1 && detail != null) {
            textDetailCell.setTextAndValueAndIcon(title.toString(), detail, icon, divider);
        } else if (detail != null) {
            textDetailCell.setTextAndValue(title, detail, divider);
        }
    }

    @Override
    public TGPType getType() {
        return TGPType.TEXT_DETAIL;
    }

    public interface TGTDListener {
        void onClick(BaseFragment bf);
    }
}
