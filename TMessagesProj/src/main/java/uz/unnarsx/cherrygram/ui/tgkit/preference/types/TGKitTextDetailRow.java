package uz.unnarsx.cherrygram.ui.tgkit.preference.types;

import androidx.annotation.Nullable;

import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;

import uz.unnarsx.cherrygram.ui.tgkit.preference.TGKitPreference;

public class TGKitTextDetailRow extends TGKitPreference {
    public int icon = -1;
    public String detail;
    public boolean divider;

    @Nullable
    public TGTDListener listener;

    public void bindCell(TextDetailSettingsCell textDetailCell) {
        if (icon != -1 && detail != null) {
            textDetailCell.setTextAndValueAndIcon(title, detail, icon, divider);
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
