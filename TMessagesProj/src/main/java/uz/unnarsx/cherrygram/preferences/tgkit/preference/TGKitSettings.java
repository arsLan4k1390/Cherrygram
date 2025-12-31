/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.preferences.tgkit.preference;

import java.util.List;

public class TGKitSettings {
    public String name;
    public List<TGKitCategory> categories;

    public TGKitSettings(String name, List<TGKitCategory> categories) {
        this.name = name;
        this.categories = categories;
    }
}
