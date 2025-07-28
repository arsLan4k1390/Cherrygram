/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.preferences.tgkit.preference;

import java.util.List;

public class TGKitCategory {
    public String name;
    public List<TGKitPreference> preferences;

    public boolean isAvailable = true;

    public TGKitCategory(String name, boolean isAvailable, List<TGKitPreference> preferences) {
        this.name = name;
        this.preferences = preferences;
        this.isAvailable = isAvailable;
    }

    public TGKitCategory(String name, List<TGKitPreference> preferences) {
        this.name = name;
        this.preferences = preferences;
    }

}
