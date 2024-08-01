package uz.unnarsx.cherrygram.preferences.tgkit.preference.types;

import androidx.annotation.Nullable;

import uz.unnarsx.cherrygram.preferences.tgkit.preference.TGKitPreference;

public class TGKitSliderPreference extends TGKitPreference {
    public TGSLContract contract;

    @Nullable
    public String description;

    @Override
    public TGPType getType() {
        return TGPType.SLIDER;
    }

    public interface TGSLContract {
        void setValue(int value);

        int getPreferenceValue();

        int getMin();

        int getMax();
    }
}
