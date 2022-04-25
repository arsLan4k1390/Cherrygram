package uz.unnarsx.tgkit.preference.types;

import uz.unnarsx.tgkit.preference.TGKitPreference;

public class TGKitTextHintRow extends TGKitPreference {
    public boolean divider;

    @Override
    public TGPType getType() {
        return TGPType.HINT;
    }
}
