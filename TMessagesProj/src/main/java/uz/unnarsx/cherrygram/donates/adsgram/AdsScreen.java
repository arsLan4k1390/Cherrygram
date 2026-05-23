/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.donates.adsgram;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import com.google.gson.Gson;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import uz.unnarsx.cherrygram.core.CherrygramLogger;
import uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper;

public class AdsScreen extends UniversalFragment {

    private final int adsRow = 1;

    private AlertDialog progressDialog;
    private int loadedAds;
    private int expectedAds;

    private AdsGramCell compactBannerCell;
    private AdsGramResponse adsGramResponseForCompactBanner;
    private static final String compact_banner_id = "30776";

    private AdsGramCell largeBannerCell;
    private AdsGramResponse adsGramResponseForLargeBanner;
    private static final String large_banner_id = "30775";

    @Override
    protected CharSequence getTitle() {
        return "ADS";
    }

    @Override
    public void onResume() {
        super.onResume();

        if (loadedAds >= expectedAds) {
            if (adsGramResponseForLargeBanner != null && adsGramResponseForLargeBanner.banner != null && adsGramResponseForLargeBanner.banner.getTracking("return") != null) {
                String returnTracking = adsGramResponseForLargeBanner.banner.getTracking("return");

                if (returnTracking != null) {
                    AdsGramCell.openTracking(returnTracking);
                    CherrygramLogger.d("ADSgram", () -> "return request sent");
                }
            }

            if (adsGramResponseForCompactBanner != null && adsGramResponseForCompactBanner.banner != null && adsGramResponseForCompactBanner.banner.getTracking("return") != null) {
                String returnTracking = adsGramResponseForCompactBanner.banner.getTracking("return");

                if (returnTracking != null) {
                    AdsGramCell.openTracking(returnTracking);
                    CherrygramLogger.d("ADSgram", () -> "return request sent");
                }
            }
        }
    }

    @Override
    public View createView(Context context) {
        setMD3(true);
        setGilroy(true);

        loadADS();
        return super.createView(context);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        /*items.add(
                SettingsActivity.SettingCell.Factory.of(
                        adsRow,
                        0xFFB659FF, 0xFF617CFF,
                        R.drawable.settings_support_filled_solar,
                        "watch ads"
                )
        );*/
        items.add(UItem.asShadow(null));
        if (adsGramResponseForLargeBanner == null) items.add(UItem.asSpace(dp(50)));

        if (adsGramResponseForLargeBanner != null) {
            items.add(SettingsHelper.asCustomWithBackground(largeBannerCell));

            if (adsGramResponseForLargeBanner.banner != null && adsGramResponseForLargeBanner.banner.getAsset("advertiserName") != null) {
                String advertiser = adsGramResponseForLargeBanner.banner.getAsset("advertiserName");

                items.add(
                        UItem.asShadow(
                                getString(R.string.CGP_ADS_Advertiser) + " " + advertiser
                        )
                );
            }
            items.add(UItem.asSpace(dp(20)));
        }

        if (adsGramResponseForCompactBanner != null) {
            items.add(SettingsHelper.asCustomWithBackground(compactBannerCell));
            if (adsGramResponseForCompactBanner.banner != null && adsGramResponseForCompactBanner.banner.getAsset("advertiserName") != null) {
                String advertiser = adsGramResponseForCompactBanner.banner.getAsset("advertiserName");

                items.add(
                        UItem.asShadow(
                                getString(R.string.CGP_ADS_Advertiser) + " " + advertiser
                        )
                );
            }
            items.add(UItem.asShadow(getString(R.string.CGP_ADS_Click)));
            items.add(UItem.asShadow(null));
        }
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == adsRow) {
            loadADS();
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }

    private void loadADS() {
        progressDialog = new AlertDialog(
                getParentActivity(),
                AlertDialog.ALERT_TYPE_SPINNER,
                getResourceProvider()
        );

        AndroidUtilities.runOnUIThread(() -> {
            try {
                if (!getParentActivity().isFinishing()) progressDialog.show();
            } catch (Exception e) {
                CherrygramLogger.e(e);
            }
        });

        loadedAds = 0;
        adsGramResponseForCompactBanner = null;
        adsGramResponseForLargeBanner = null;
        compactBannerCell = null;
        largeBannerCell = null;

        int randomType = new Random().nextInt(3);

        if (randomType == 0) {
            expectedAds = 1;
            loadCompactAd();
        } else if (randomType == 1) {
            expectedAds = 1;
            loadLargeAd();
        } else {
            expectedAds = 2;
            loadCompactAd();
            loadLargeAd();
        }
    }

    private void loadCompactAd() {
        loadSingleAd(
                compact_banner_id,
                dp(100),
                response -> {
                    adsGramResponseForCompactBanner = response;

                    compactBannerCell = new AdsGramCell(getContext(), this, AdsGramCell.ViewType.BANNER_COMPACT);
                    compactBannerCell.setAd(adsGramResponseForCompactBanner);
                    onAdLoaded();
                }
        );
    }

    private void loadLargeAd() {
        loadSingleAd(
                large_banner_id,
                dp(250),
                response -> {
                    adsGramResponseForLargeBanner = response;

                    largeBannerCell = new AdsGramCell(getContext(), this, AdsGramCell.ViewType.BANNER_LARGE);
                    largeBannerCell.setAd(adsGramResponseForLargeBanner);
                    onAdLoaded();
                }
        );
    }

    private void loadSingleAd(String blockId, int adHeight, Consumer<AdsGramResponse> callback) {
        AdsGramApi adsGramApi = new AdsGramApi(getContext());

        AdsGramApi.fetchRealIp(ip -> {
            adsGramApi.loadAd(
                    blockId,
                    getUserConfig().getClientUserId(),
                    getUserConfig().isPremium(),
                    AndroidUtilities.displaySize.x - dp(24),
                    adHeight,
                    ip,
                    new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            CherrygramLogger.e(e, true);

                            AndroidUtilities.runOnUIThread(() -> {
                                try {
                                    if (!getParentActivity().isFinishing() && progressDialog.isShowing()) progressDialog.dismiss();
                                } catch (Exception e2) {
                                    CherrygramLogger.e(e2);
                                }
                            });
                            callback.accept(null);
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            AndroidUtilities.runOnUIThread(() -> {
                                try {
                                    if (!getParentActivity().isFinishing() && progressDialog.isShowing()) progressDialog.dismiss();
                                } catch (Exception e2) {
                                    CherrygramLogger.e(e2);
                                }
                            });

                            try (response) {
                                if (!response.isSuccessful()) {
                                    CherrygramLogger.e(new IOException("Сервер вернул ошибку: " + response.code()), true);
                                    callback.accept(null);
                                    return;
                                }

                                String body = response.body().string();

                                try {
                                    AdsGramResponse parsed = new Gson().fromJson(body, AdsGramResponse.class);
                                    callback.accept(parsed);
                                } catch (Exception e) {
                                    CherrygramLogger.e(new Exception("Ошибка парсинга JSON. Тело ответа: " + body, e), true);
                                    callback.accept(null);
                                }
                            }
                        }
                    }
            );
        });
    }

    private void onAdLoaded() {
        loadedAds++;

        if (loadedAds >= expectedAds) {
            AndroidUtilities.runOnUIThread(() -> {
                if (listView != null && listView.adapter != null) {
                    listView.adapter.update(true);
                }
            });
        }
    }

}
