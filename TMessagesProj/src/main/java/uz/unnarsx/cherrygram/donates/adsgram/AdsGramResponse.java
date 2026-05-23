/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.donates.adsgram;

import java.util.List;

public class AdsGramResponse {

    public BannerData banner;

    public static class BannerData {
        public String vast;

        public List<BannerAsset> bannerAssets;
        public List<Tracking> trackings;
        public List<String> flags;

        public String getAsset(String key) {
            if (bannerAssets == null) {
                return null;
            }

            for (BannerAsset asset : bannerAssets) {
                if (key.equals(asset.name)) {
                    return asset.value;
                }
            }

            return null;
        }

        public String getTracking(String key) {
            if (trackings == null) {
                return null;
            }

            for (Tracking tracking : trackings) {
                if (key.equals(tracking.name)) {
                    return tracking.value;
                }
            }

            return null;
        }
    }

    public static class BannerAsset {
        public String name;
        public String value;
    }

    public static class Tracking {
        public String name;
        public String value;
    }

}
