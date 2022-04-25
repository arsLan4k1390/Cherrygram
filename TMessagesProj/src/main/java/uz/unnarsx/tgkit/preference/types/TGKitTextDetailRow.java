package uz.unnarsx.tgkit.preference.types;

import uz.unnarsx.tgkit.preference.TGKitPreference;

public class TGKitTextDetailRow extends TGKitPreference {
    public String detail;
    public boolean divider;

    @Override
    public TGPType getType() {
        return TGPType.TEXT_DETAIL;
    }
}
