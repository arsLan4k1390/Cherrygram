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
