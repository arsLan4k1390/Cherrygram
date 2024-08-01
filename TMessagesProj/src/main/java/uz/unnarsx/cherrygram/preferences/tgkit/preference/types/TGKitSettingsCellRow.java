package uz.unnarsx.cherrygram.preferences.tgkit.preference.types;

import androidx.annotation.Nullable;

import org.telegram.ui.ActionBar.Theme;

import uz.unnarsx.cherrygram.preferences.tgkit.preference.TGKitPreference;

public class TGKitSettingsCellRow extends TGKitPreference {
    public boolean divider = false;
    public int textColor = Theme.getColor(Theme.key_windowBackgroundWhiteBlackText);

    @Nullable
    public TGKitTextIconRow.TGTIListener listener;

    @Override
    public TGPType getType() {
        return TGPType.TEXT_ICON;
    }
}
