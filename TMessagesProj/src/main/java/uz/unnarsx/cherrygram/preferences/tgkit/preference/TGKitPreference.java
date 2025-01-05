/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.preferences.tgkit.preference;

import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGPType;

abstract public class TGKitPreference {
    public String title;
    public boolean isAvailable = true;

    abstract public TGPType getType();
}
