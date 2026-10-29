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
import android.text.TextUtils;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.SettingsActivity;
import org.telegram.ui.Stars.StarsIntroActivity;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.core.configs.CherrygramFirebaseConfig;
import uz.unnarsx.cherrygram.core.firebase.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.core.helpers.DeeplinkHelper;
import uz.unnarsx.cherrygram.misc.CherrygramExtras;
import uz.unnarsx.cherrygram.misc.Constants;
import uz.unnarsx.cherrygram.misc.LogoOverlayView;
import uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper;

public class AlternativeSupportScreen extends BaseCGPreferencesEntry {

    private final int safeStarsRow = 1;
    private final int safeSurfRow = 2;

    private final int safePayRow = 3;
    private final int edifyRow = 4;

    private final int watchADSRow = 5;
    private final int brandedScreenshotsRow = 6;
    private final int rateInGooglePlayRow = 7;

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("alternative_support_screen");
        return getString(R.string.AS_Header);
    }

    @Override
    protected String getKey() {
        return DeeplinkHelper.DeepLinksRepo.CG_Alternative_Support;
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
                            true,
                            false
                    )
                    .slug(DeeplinkHelper.DeepLinksRepo.CG_Stars)
            );
        }

        if (CherrygramFirebaseConfig.INSTANCE.getAllowSafeSurf()) {
            items.add(
                    SettingsActivity.SettingCell.noMonet(SettingsActivity.SettingCell.Factory.of(
                            safeSurfRow,
                            0xFF5352C3, 0xFF875DF5,
                            R.drawable.settings_safesurf_logo,
                            "SafeSurf VPN"
                    ))
                    .slug(DeeplinkHelper.DeepLinksRepo.CG_Proxy)
            );
        }

        if (CherrygramFirebaseConfig.INSTANCE.getAllowSafeStars() || CherrygramFirebaseConfig.INSTANCE.getAllowSafeSurf()) {
            items.add(UItem.asShadow(null));
        }

        if (CherrygramFirebaseConfig.INSTANCE.getAllowSafePay() || CherrygramFirebaseConfig.INSTANCE.getAllowEdify()) {
            items.add(UItem.asHeader(getString(R.string.AS_UsefulServices)));
        }

        if (CherrygramFirebaseConfig.INSTANCE.getAllowSafePay()) {
            items.add(
                    SettingsActivity.SettingCell.noMonet(SettingsActivity.SettingCell.Factory.of(
                            safePayRow,
                            0xFF7451AA, 0xFF6779E3,
                            R.drawable.settings_safepay_logo,
                            applyNewSpan("SafePay")
                    ))
                    .slug(DeeplinkHelper.DeepLinksRepo.CG_SafePay)
            );
        }

        if (CherrygramFirebaseConfig.INSTANCE.getAllowEdify()) {
            items.add(
                    SettingsActivity.SettingCell.Factory.of(
                            edifyRow,
                            0xFF0B0B0F, 0xFF0B0B0F,
                            R.drawable.settings_edify_logo,
                            applyNewSpan("Edify"),
                            getString(R.string.CG_Edify_Desc),
                            true,
                            false
                    )
//                    .slug(DeeplinkHelper.DeepLinksRepo.CG_Edify)
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
                    SettingsActivity.SettingCell.noMonet(SettingsActivity.SettingCell.Factory.of(
                            watchADSRow,
                            0xFFF6538A, 0xFF581668,
                            R.drawable.settings_watch_ads_filled_solar,
                            getString(R.string.CGP_ADS)
                    ))
                    .slug(DeeplinkHelper.DeepLinksRepo.CG_ADS)
            );
            items.add(UItem.asShadow(null));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            items.add(SettingsHelper.asSwitchCG(brandedScreenshotsRow, getString(R.string.DP_CameraCutoutHeader), AndroidUtilities.replaceTags(getString(R.string.DP_CameraCutoutDesc)))
                    .setChecked(CherrygramCoreConfig.INSTANCE.getCgBrandedScreenshots())
                    .slug("logoUnderCamera")
            );
        }

        items.add(UItem.asShadow(null));

        if (CherrygramCoreConfig.isPlayStoreBuild() || CherrygramCoreConfig.isDevBuild()) {
            items.add(
                    SettingsActivity.SettingCell.Factory.of(
                            rateInGooglePlayRow,
                            0xFFE54C7F, 0xFFA33156,
                            R.drawable.settings_google_play_logo,
                            getString(R.string.DP_EnjoyingUs),
                            getString(R.string.DP_RateUs),
                            true
                    )
                    .slug("rateUs")
            );
        }

        items.add(UItem.asShadow(null));

    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == safeStarsRow) {
            StarsIntroActivity.createSafeStars(null, null, -1);
        } else if (item.id == safeSurfRow) {
            openBotWithStart(getContext(), CherrygramFirebaseConfig.INSTANCE.getSafe_surf_URL());
        } else if (item.id == safePayRow) {
            openBotWithStart(getContext(), CherrygramFirebaseConfig.INSTANCE.getSafe_pay_URL());
        } else if (item.id == edifyRow) {
            openBotWithStart(getContext(), CherrygramFirebaseConfig.INSTANCE.getEdify_URL());
        } else if (item.id == watchADSRow) {
            CherrygramPreferencesNavigator.INSTANCE.createADS(this);
        } else if (item.id == brandedScreenshotsRow) {
            CherrygramCoreConfig.INSTANCE.setCgBrandedScreenshots(!CherrygramCoreConfig.INSTANCE.getCgBrandedScreenshots());
            SettingsHelper.updateCheckState(view, CherrygramCoreConfig.INSTANCE.getCgBrandedScreenshots());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                LogoOverlayView.apply(getParentActivity(), CherrygramCoreConfig.INSTANCE.getCgBrandedScreenshots());
            }
        } else if (item.id == rateInGooglePlayRow) {
            CherrygramExtras.INSTANCE.requestReviewFlow(this);
        }
    }

    private static void openBotWithStart(Context context, String url) {
        if (TextUtils.isEmpty(url)) return;
        LaunchActivity activity = AndroidUtilities.findActivity(context) instanceof LaunchActivity
                ? (LaunchActivity) AndroidUtilities.findActivity(context) : LaunchActivity.instance;
        if (activity != null) activity.setNavigateToPremiumBot(true);
        Browser.openAsInternalIntent(context, url);
    }

}
