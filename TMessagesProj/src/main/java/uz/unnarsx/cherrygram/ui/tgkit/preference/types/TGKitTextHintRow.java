package uz.unnarsx.cherrygram.ui.tgkit.preference.types;

import uz.unnarsx.cherrygram.ui.tgkit.preference.TGKitPreference;

public class TGKitTextHintRow extends TGKitPreference {
    public boolean divider;

    @Override
    public TGPType getType() {
        return TGPType.HINT;
    }
}
