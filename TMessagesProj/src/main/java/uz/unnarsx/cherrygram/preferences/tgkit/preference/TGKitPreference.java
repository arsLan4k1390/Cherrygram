package uz.unnarsx.cherrygram.preferences.tgkit.preference;

import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGPType;

abstract public class TGKitPreference {
    public String title;

    abstract public TGPType getType();
}
