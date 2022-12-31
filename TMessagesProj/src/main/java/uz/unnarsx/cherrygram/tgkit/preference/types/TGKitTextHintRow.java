package uz.unnarsx.cherrygram.tgkit.preference.types;

import uz.unnarsx.cherrygram.tgkit.preference.TGKitPreference;

public class TGKitTextHintRow extends TGKitPreference {
    public boolean divider;

    @Override
    public TGPType getType() {
        return TGPType.HINT;
    }
}
