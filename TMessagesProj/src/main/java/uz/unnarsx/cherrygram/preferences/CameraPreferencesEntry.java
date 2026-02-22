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

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Size;
import android.view.View;

import androidx.camera.video.Quality;

import org.telegram.messenger.R;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uz.unnarsx.cherrygram.camera.CameraTypeSelector;
import uz.unnarsx.cherrygram.camera.CameraXUtils;
import uz.unnarsx.cherrygram.core.configs.CherrygramCameraConfig;
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.core.crashlytics.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper;
import uz.unnarsx.cherrygram.core.ui.CGBulletinCreator;
import uz.unnarsx.cherrygram.donates.DonatesManager;
import uz.unnarsx.cherrygram.helpers.ui.PopupHelper;

public class CameraPreferencesEntry extends UniversalFragment {

    private final int cameraTypeSelectorRow = 0;

    private final int disableAttachCameraRow = 1;
    private final int cameraAspectRatioRow = 2;

    private final int cameraXQualityRow = 3;
    private final int cameraUseDualCameraRow = 4;
    private final int startFromUltraWideRow = 5;
    private final int cameraXFpsRangeRow = 6;
    private final int cameraStabilisationRow = 7;
    private final int exposureSliderRow = 8;
    private final int cameraControlButtonsRow = 9;
    private final int rearCamRow = 10;

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("camera_preferences_screen");
        return getString(R.string.CP_Category_Camera);
    }

    @Override
    public View createView(Context context) {
        setMD3(true);
        return super.createView(context);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        if (CameraXUtils.isCameraXSupported()) {
            items.add(UItem.asHeader(getString(R.string.CP_CameraType)));

            items.add(UItem.asCustomWithBackground(new CameraTypeSelector(getContext()) {
                @Override
                protected void onSelectedCamera(int cameraSelected) {
                    super.onSelectedCamera(cameraSelected);

                    CherrygramCameraConfig.INSTANCE.setCameraType(cameraSelected);

                    listView.adapter.update(true);
                }
            }));

            items.add(UItem.asShadow(getCameraAdvise()));
        }

        items.add(UItem.asHeader(getString(R.string.CP_Category_Camera)));

        if (CherrygramCoreConfig.isDevBuild()) {
            items.add(
                    UItem.asButtonCheck(
                            disableAttachCameraRow,
                            getString(R.string.CP_DisableCam),
                            getString(R.string.CP_DisableCam_Desc)
                    ).setChecked(CherrygramCameraConfig.INSTANCE.getDisableAttachCamera())
            );
        }

        if (CherrygramCameraConfig.INSTANCE.getCameraType() != CherrygramCameraConfig.CAMERA_2) {
            items.add(
                    UItem.asButton(
                        cameraAspectRatioRow,
                        getString(R.string.CP_CameraAspectRatio),
                        getCameraAspectRatio()
                    )
            );
        }
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.CP_Header_Videomessages)));

        if (CameraXUtils.isCurrentCameraCameraX()) {
            items.add(
                    UItem.asButton(
                        cameraXQualityRow,
                        getString(R.string.CP_CameraQuality),
                        CherrygramCameraConfig.INSTANCE.getCameraResolution() + "p"
                    )
            );
        }

        if (CherrygramCameraConfig.INSTANCE.getCameraType() == CherrygramCameraConfig.CAMERA_2) {
            items.add(
                    UItem.asButtonCheck(
                        cameraUseDualCameraRow,
                        getString(R.string.CP_CameraDualCamera),
                        getString(R.string.CP_CameraDualCamera_Desc)
                    ).setChecked(CherrygramCameraConfig.INSTANCE.getUseDualCamera())
            );
        }

        if (CameraXUtils.isCurrentCameraCameraX()) {
            items.add(
                    UItem.asButtonCheck(
                            startFromUltraWideRow,
                            getString(R.string.CP_CameraUW),
                            getString(R.string.CP_CameraUW_Desc)
                    ).setChecked(CherrygramCameraConfig.INSTANCE.getStartFromUltraWideCam())
            );

            items.add(
                    UItem.asButton(
                        cameraXFpsRangeRow,
                        "FPS",
                        getCameraXFpsRange()
                    )
            );

            items.add(
                    UItem.asCheck(
                        cameraStabilisationRow,
                        getString(R.string.CP_CameraStabilisation)
                    ).setChecked(CherrygramCameraConfig.INSTANCE.getCameraStabilisation())
            );

            items.add(
                    UItem.asButton(
                        exposureSliderRow,
                        getString(R.string.CP_ExposureSliderPosition),
                        getExposureSliderPosition()
                    )
            );
        }

        items.add(
                UItem.asButtonCheck(
                    cameraControlButtonsRow,
                    getString(R.string.CP_CenterCameraControlButtons),
                    getString(R.string.CP_CenterCameraControlButtons_Desc)
                ).setChecked(CherrygramCameraConfig.INSTANCE.getCenterCameraControlButtons())
        );

        if (!(CherrygramCameraConfig.INSTANCE.getCameraType() == CherrygramCameraConfig.CAMERA_2 && CherrygramCameraConfig.INSTANCE.getUseDualCamera())) {
            items.add(
                    UItem.asButtonCheck(
                        rearCamRow,
                        getString(R.string.CP_RearCam),
                        getString(R.string.CP_RearCam_Desc)
                    ).setChecked(CherrygramCameraConfig.INSTANCE.getRearCam())
            );
        }

        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == disableAttachCameraRow) {
            CherrygramCameraConfig.INSTANCE.setDisableAttachCamera(!CherrygramCameraConfig.INSTANCE.getDisableAttachCamera());
            ((NotificationsCheckCell) view).setChecked(CherrygramCameraConfig.INSTANCE.getDisableAttachCamera());

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        } else if (item.id == cameraAspectRatioRow) {
            showAspectRatioSelector(getContext(), () -> ((TextCell) view).setValue(getCameraAspectRatio(), true));
        } else if (item.id == cameraXQualityRow) {
            Map<Quality, Size> availableSizes = CameraXUtils.getAvailableVideoSizes();
            Stream<Integer> tmp = availableSizes.values().stream().sorted(Comparator.comparingInt(Size::getWidth).reversed()).map(Size::getHeight);
            ArrayList<Integer> types = tmp.collect(Collectors.toCollection(ArrayList::new));
            ArrayList<String> arrayList = types.stream().map(p -> p + "p").collect(Collectors.toCollection(ArrayList::new));

            PopupHelper.show(arrayList, getString(R.string.CP_CameraQuality), types.indexOf(CherrygramCameraConfig.INSTANCE.getCameraResolution()), getContext(), i -> {
                CherrygramCameraConfig.INSTANCE.setCameraResolution(types.get(i));
                ((TextCell) view).setValue(CherrygramCameraConfig.INSTANCE.getCameraResolution() + "p", true);
            });
        } else if (item.id == cameraUseDualCameraRow) {
            CherrygramCameraConfig.INSTANCE.setUseDualCamera(!CherrygramCameraConfig.INSTANCE.getUseDualCamera());
            ((NotificationsCheckCell) view).setChecked(CherrygramCameraConfig.INSTANCE.getUseDualCamera());

            listView.adapter.update(true);
        } else if (item.id == startFromUltraWideRow) {
            CherrygramCameraConfig.INSTANCE.setStartFromUltraWideCam(!CherrygramCameraConfig.INSTANCE.getStartFromUltraWideCam());
            ((NotificationsCheckCell) view).setChecked(CherrygramCameraConfig.INSTANCE.getStartFromUltraWideCam());
        } else if (item.id == cameraXFpsRangeRow) {
            ArrayList<String> configStringKeys = new ArrayList<>();
            ArrayList<Integer> configValues = new ArrayList<>();

            configStringKeys.add("25-30");
            configValues.add(CherrygramCameraConfig.CameraXFpsRange25to30);

            configStringKeys.add("30-30");
            configValues.add(CherrygramCameraConfig.CameraXFpsRange30to30);

            if (isExtendedFpsAvailable()) {
                configStringKeys.add("30-60");
                configValues.add(CherrygramCameraConfig.CameraXFpsRange30to60);

                configStringKeys.add("60-60");
                configValues.add(CherrygramCameraConfig.CameraXFpsRange60to60);
            }

            configStringKeys.add(getString(R.string.Default));
            configValues.add(CherrygramCameraConfig.CameraXFpsRangeDefault);

            PopupHelper.show(configStringKeys, "FPS", configValues.indexOf(CherrygramCameraConfig.INSTANCE.getCameraXFpsRange()), getContext(), i -> {
                CherrygramCameraConfig.INSTANCE.setCameraXFpsRange(configValues.get(i));
                ((TextCell) view).setValue(getCameraXFpsRange(), true);
            });
        } else if (item.id == cameraStabilisationRow) {
            CherrygramCameraConfig.INSTANCE.setCameraStabilisation(!CherrygramCameraConfig.INSTANCE.getCameraStabilisation());
            ((TextCheckCell) view).setChecked(CherrygramCameraConfig.INSTANCE.getCameraStabilisation());
        } else if (item.id == exposureSliderRow) {
            ArrayList<String> configStringKeys = new ArrayList<>();
            ArrayList<Integer> configValues = new ArrayList<>();

            /*configStringKeys.add(getString(R.string.CP_ZoomSliderPosition_Bottom));
            configValues.add(CherrygramCameraConfig.EXPOSURE_SLIDER_BOTTOM);*/

            configStringKeys.add(getString(R.string.CP_ZoomSliderPosition_Right));
            configValues.add(CherrygramCameraConfig.EXPOSURE_SLIDER_RIGHT);

            /*configStringKeys.add(getString(R.string.CP_ZoomSliderPosition_Left));
            configValues.add(CherrygramCameraConfig.EXPOSURE_SLIDER_LEFT);*/

            configStringKeys.add(getString(R.string.Disable));
            configValues.add(CherrygramCameraConfig.EXPOSURE_SLIDER_NONE);

            PopupHelper.show(configStringKeys, getString(R.string.CP_ExposureSliderPosition), configValues.indexOf(CherrygramCameraConfig.INSTANCE.getExposureSlider()), getContext(), i -> {
                CherrygramCameraConfig.INSTANCE.setExposureSlider(configValues.get(i));
                ((TextCell) view).setValue(getExposureSliderPosition(), true);
            });
        } else if (item.id == cameraControlButtonsRow) {
            CherrygramCameraConfig.INSTANCE.setCenterCameraControlButtons(!CherrygramCameraConfig.INSTANCE.getCenterCameraControlButtons());
            ((NotificationsCheckCell) view).setChecked(CherrygramCameraConfig.INSTANCE.getCenterCameraControlButtons());
        } else if (item.id == rearCamRow) {
            CherrygramCameraConfig.INSTANCE.setRearCam(!CherrygramCameraConfig.INSTANCE.getRearCam());
            ((NotificationsCheckCell) view).setChecked(CherrygramCameraConfig.INSTANCE.getRearCam());
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }

    public static void showAspectRatioSelector(Context context, Runnable runnable) {
        ArrayList<String> configStringKeys = new ArrayList<>();
        ArrayList<Integer> configValues = new ArrayList<>();

        configStringKeys.add("1:1");
        configValues.add(CherrygramCameraConfig.Camera1to1);

        configStringKeys.add("4:3");
        configValues.add(CherrygramCameraConfig.Camera4to3);

        configStringKeys.add("16:9");
        configValues.add(CherrygramCameraConfig.Camera16to9);

        configStringKeys.add(getString(R.string.Default));
        configValues.add(CherrygramCameraConfig.CameraAspectDefault);

        PopupHelper.show(configStringKeys, getString(R.string.CP_CameraAspectRatio), configValues.indexOf(CherrygramCameraConfig.INSTANCE.getCameraAspectRatio()), context, i -> {
            CherrygramCameraConfig.INSTANCE.setCameraAspectRatio(configValues.get(i));
            if (runnable != null) runnable.run();
        });
    }

    private boolean isExtendedFpsAvailable() {
        return CherrygramCoreConfig.isDevBuild() || CherrygramCoreConfig.isStandalonePremiumBuild()
                || DonatesManager.INSTANCE.checkAllDonatedAccounts() || DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace();
    }

    public static String getCameraName() {
        return switch (CherrygramCameraConfig.INSTANCE.getCameraType()) {
            case CherrygramCameraConfig.TELEGRAM_CAMERA -> "Telegram";
            case CherrygramCameraConfig.CAMERA_X -> "CameraX";
            case CherrygramCameraConfig.CAMERA_2 -> "Camera 2 (Telegram)";
            default -> getString(R.string.CP_CameraTypeSystem);
        };
    }

    private CharSequence getCameraAdvise() {
        String advise = switch (CherrygramCameraConfig.INSTANCE.getCameraType()) {
            case CherrygramCameraConfig.TELEGRAM_CAMERA -> getString(R.string.CP_DefaultCameraDesc);
            case CherrygramCameraConfig.CAMERA_X -> getString(R.string.CP_CameraXDesc);
            case CherrygramCameraConfig.CAMERA_2 -> getString(R.string.CP_Camera2Desc);
            default -> getString(R.string.CP_SystemCameraDesc);
        };

        Spannable htmlParsed;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            htmlParsed = new SpannableString(Html.fromHtml(advise, Html.FROM_HTML_MODE_LEGACY));
        } else {
            htmlParsed = new SpannableString(Html.fromHtml(advise));
        }

        return CGResourcesHelper.INSTANCE.getUrlNoUnderlineText(htmlParsed);
    }

    private static String getCameraAspectRatio()  {
        return switch (CherrygramCameraConfig.INSTANCE.getCameraAspectRatio()) {
            case CherrygramCameraConfig.Camera1to1 -> "1:1";
            case CherrygramCameraConfig.Camera4to3 -> "4:3";
            case CherrygramCameraConfig.Camera16to9 -> "16:9";
            default -> getString(R.string.Default);
        };
    }

    private String getCameraXFpsRange() {
        return switch (CherrygramCameraConfig.INSTANCE.getCameraXFpsRange()) {
            case CherrygramCameraConfig.CameraXFpsRange25to30 -> "25-30";
            case CherrygramCameraConfig.CameraXFpsRange30to30 -> "30-30";
            case CherrygramCameraConfig.CameraXFpsRange30to60 -> "30-60";
            case CherrygramCameraConfig.CameraXFpsRange60to60 -> "60-60";
            default -> getString(R.string.Default);
        };
    }

    private String getExposureSliderPosition() {
        return switch (CherrygramCameraConfig.INSTANCE.getExposureSlider()) {
            case CherrygramCameraConfig.EXPOSURE_SLIDER_BOTTOM -> getString(R.string.CP_ZoomSliderPosition_Bottom);
            case CherrygramCameraConfig.EXPOSURE_SLIDER_RIGHT -> getString(R.string.CP_ZoomSliderPosition_Right);
            case CherrygramCameraConfig.EXPOSURE_SLIDER_LEFT -> getString(R.string.CP_ZoomSliderPosition_Left);
            default -> getString(R.string.Disable);
        };
    }

}
