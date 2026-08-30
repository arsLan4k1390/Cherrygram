/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.preferences;

import static org.telegram.messenger.LocaleController.getString;

import static uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper.applyNewSpan;

import android.content.Context;
import android.os.Build;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import org.telegram.ui.SettingsActivity;
import org.telegram.ui.Stars.StarsIntroActivity;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.core.configs.CherrygramFirebaseConfig;
import uz.unnarsx.cherrygram.core.firebase.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.core.helpers.DeeplinkHelper;
import uz.unnarsx.cherrygram.misc.CherrygramExtras;
import uz.unnarsx.cherrygram.misc.Constants;
import uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper;

public class AlternativeSupportScreen extends BaseCGPreferencesEntry {

    private final int safeStarsRow = 1;
    private final int safeSurfRow = 2;

    private final int safePayRow = 3;

    private final int watchADSRow = 4;
    private final int brandedScreenshotsRow = 5;
    private final int rateInGooglePlayRow = 6;

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("alternative_support_screen");
        return getString(R.string.AS_Header);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        if (CherrygramFirebaseConfig.INSTANCE.getAllowSafeStars() || CherrygramFirebaseConfig.INSTANCE.getAllowSafeSurf()) {
            items.add(UItem.asHeader(getString(R.string.AS_DigitalGoods)));
        }

        if (CherrygramFirebaseConfig.INSTANCE.getAllowSafeStars()) {
            items.add(
                    SettingsActivity.SettingCell.Factory.of(
                            safeStarsRow,
                            0xFF6779E3, 0xFF7451AA,
                            R.drawable.settings_stars,
                            "SafeStars",
                            getString(R.string.CG_SafeStars_Desc),
                            true
                    )
            );
        }

        if (CherrygramFirebaseConfig.INSTANCE.getAllowSafeSurf()) {
            items.add(
                    SettingsActivity.SettingCell.Factory.of(
                            safeSurfRow,
                            0xFF5352C3, 0xFF875DF5,
                            R.drawable.settings_safesurf_logo,
                            "SafeSurf VPN"
                    )
            );
        }

        if (CherrygramFirebaseConfig.INSTANCE.getAllowSafePay()) {
            items.add(UItem.asShadow(null));

            items.add(UItem.asHeader(getString(R.string.AS_UsefulServices)));
            items.add(
                    SettingsActivity.SettingCell.Factory.of(
                            safePayRow,
                            0xFF7451AA, 0xFF6779E3,
                            R.drawable.settings_wallet,
                            applyNewSpan("SafePay")
                    )
            );
        }

        if (CherrygramFirebaseConfig.INSTANCE.getAllowSafeStars() || CherrygramFirebaseConfig.INSTANCE.getAllowSafeSurf() || CherrygramFirebaseConfig.INSTANCE.getAllowSafePay()) {
            CharSequence tosInfoText = AndroidUtilities.replaceSingleTag(LocaleController.getString(R.string.CG_SafeSurf_TOS),
                    Theme.key_windowBackgroundWhiteLinkText,
                    AndroidUtilities.REPLACING_TAG_TYPE_LINKBOLD,
                    () -> Browser.openUrl(getContext(), Constants.CG_PRIVACY_URL)
            );
            items.add(UItem.asShadow(tosInfoText));
        }

        items.add(UItem.asHeader(getString(R.string.AS_ZeroCost)));
        boolean showAds = CherrygramFirebaseConfig.INSTANCE.getShowAdsScreenInSettings();
        if (CherrygramCoreConfig.isPlayStoreBuild()) {
            showAds = showAds && CherrygramFirebaseConfig.INSTANCE.getShowAdsInPlayStoreBuilds();
        }
        if (showAds) {
            items.add(
                    SettingsActivity.SettingCell.Factory.of(
                            watchADSRow,
                            0xFFF6538A, 0xFF581668,
                            R.drawable.settings_watch_ads_filled_solar,
                            applyNewSpan(getString(R.string.CGP_ADS))
                    )
            );
            items.add(UItem.asShadow(null));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            items.add(SettingsHelper.asSwitchCG(brandedScreenshotsRow, getString(R.string.DP_CameraCutoutHeader), AndroidUtilities.replaceTags(getString(R.string.DP_CameraCutoutDesc)))
                    .setChecked(CherrygramCoreConfig.INSTANCE.getCgBrandedScreenshots())
            );
        }

        items.add(UItem.asShadow(null));

        if (CherrygramCoreConfig.isPlayStoreBuild()) {
            items.add(
                    SettingsHelper.asTextDetail(
                            rateInGooglePlayRow,
                            0,
                            getString(R.string.DP_EnjoyingUs),
                            getString(R.string.DP_RateUs)
                    )
            );
        }

        items.add(UItem.asShadow(null));

    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == safeStarsRow) {
            StarsIntroActivity.createSafeStars(null, null, -1);
        } else if (item.id == safeSurfRow) {
            Browser.openAsInternalIntent(getContext(), CherrygramFirebaseConfig.INSTANCE.getSafe_surf_URL());
        } else if (item.id == safePayRow) {
            Browser.openAsInternalIntent(getContext(), CherrygramFirebaseConfig.INSTANCE.getSafe_pay_URL());
        } else if (item.id == watchADSRow) {
            CherrygramPreferencesNavigator.INSTANCE.createADS(this);
        } else if (item.id == brandedScreenshotsRow) {
            CherrygramCoreConfig.INSTANCE.setCgBrandedScreenshots(!CherrygramCoreConfig.INSTANCE.getCgBrandedScreenshots());
            SettingsHelper.updateCheckState(view, CherrygramCoreConfig.INSTANCE.getCgBrandedScreenshots());

            showRestartBulletin();
        } else if (item.id == rateInGooglePlayRow) {
            CherrygramExtras.INSTANCE.requestReviewFlow(this);
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        if (item.id == safeStarsRow) {
            AndroidUtilities.addToClipboard("tg://" + DeeplinkHelper.DeepLinksRepo.CG_Stars);
            return true;
        } else if (item.id == safeSurfRow) {
            AndroidUtilities.addToClipboard("tg://" + DeeplinkHelper.DeepLinksRepo.CG_Proxy);
            return true;
        } else if (item.id == watchADSRow) {
            AndroidUtilities.addToClipboard("tg://" + DeeplinkHelper.DeepLinksRepo.CG_ADS);
            return true;
        }
        return false;
    }

}
