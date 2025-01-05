/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.preferences.tgkit.preference.types;

public enum TGPType {
    SECTION(0, false),
    SETTINGS_CELL(2, true),
    HEADER(2, false),
    SWITCH(3, true),
    TEXT_DETAIL(4, true),
    TEXT_ICON(5, true),
    SLIDER(6, true),
    LIST(7, true),
    HINT(8, true);

    public final int adapterType;
    public final boolean enabled;

    TGPType(int adapterType, boolean enabled) {
        this.adapterType = adapterType;
        this.enabled = enabled;
    }
}