package uz.unnarsx.cherrygram.tgkit.preference.types;

import androidx.annotation.Nullable;

import org.telegram.ui.ActionBar.BaseFragment;

import uz.unnarsx.cherrygram.tgkit.preference.TGKitPreference;

public class TGKitTextDetailRow extends TGKitPreference {
    public String detail;
    public boolean divider;

    @Nullable
    public TGTDListener listener;

    @Override
    public TGPType getType() {
        return TGPType.TEXT_DETAIL;
    }

    public interface TGTDListener {
        void onClick(BaseFragment bf);
    }
}
