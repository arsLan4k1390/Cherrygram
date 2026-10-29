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

import static uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper.applySpan;

import android.content.Context;
import android.util.Size;
import android.view.View;

import androidx.camera.video.Quality;

import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.RoundVideoSettingsActivity;
import org.telegram.ui.SettingsActivity;
import org.telegram.utils.settings.SharedSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uz.unnarsx.cherrygram.camera.CameraTypeSelector;
import uz.unnarsx.cherrygram.camera.CameraXUtils;
import uz.unnarsx.cherrygram.camera.roundvideo.VideoMessagesHelper;
import uz.unnarsx.cherrygram.core.configs.CherrygramCameraConfig;
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.core.firebase.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.core.helpers.DeeplinkHelper;
import uz.unnarsx.cherrygram.helpers.ui.PopupHelper;
import uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper;

public class CameraPreferencesEntry extends BaseCGPreferencesEntry {

    private final int cameraTypeSelectorRow = 0;

    private final int disableAttachCameraRow = 1;
    private final int cameraAspectRatioRow = 2;

    private final int cameraUseDualCameraRow = 3;
    private final int rearCamRow = 4;
    private final int startFromUltraWideRow = 5;

    private final int cameraXQualityRow = 6;
    private final int cameraXFpsRangeRow = 7;

    private final int cameraEnhancementsRow = 8;
    private final int opticalStabilisationRow = 9;
    private final int videoStabilisationRow = 10;
    private final int continuousAutofocusRow = 11;
    private final int noiceReductionRow = 12;
    private final int faceDetectionRow = 13;
    private final int bokehEffectRow = 14;

    private final int exposureSliderRow = 15;
    private final int cameraControlButtonsRow = 16;

    private final int newEncoderRow = 17;
    private final int newEncoderBitrateRow = 19;
    private final int newEncoderSettingsRow = 20;

    private static final int[] NEW_ENCODER_BITRATES = {
            750_000,
            1_000_000,
            1_200_000
    };

    private boolean expandedCameraEnhancementsSection = false;

    private static final Set<String> ENHANCEMENTS_SECTION_SLUGS = Set.of(
            ""
    );

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("camera_preferences_screen");
        return getString(R.string.CP_Category_Camera);
    }

    @Override
    protected String getKey() {
        return DeeplinkHelper.DeepLinksRepo.CG_Camera;
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        if (CameraXUtils.isCameraXSupported()) {
            items.add(UItem.asHeader(getString(R.string.CP_CameraType)));
            items.add(SettingsHelper.asCustomWithBackground(new CameraTypeSelector(getContext()) {
                @Override
                protected void onSelectedCamera(int cameraSelected) {
                    super.onSelectedCamera(cameraSelected);

                    CherrygramCameraConfig.INSTANCE.setCameraType(cameraSelected);

                    updateRows(true);
                }
            })
                .slug("cameraType")
            );
            items.add(UItem.asShadow(null));
        }

        if (CherrygramCameraConfig.INSTANCE.getCameraType() != CherrygramCameraConfig.CAMERA_2 || CherrygramCoreConfig.isDevBuild()) {
            items.add(UItem.asHeader(getString(R.string.CP_Category_Camera)));
        }
        items.add(SettingsHelper.asSwitchCG(disableAttachCameraRow, getString(R.string.CP_DisableCam), getString(R.string.CP_DisableCam_Desc))
                .setChecked(CherrygramCameraConfig.INSTANCE.getDisableAttachCamera())
        );
        if (CherrygramCameraConfig.INSTANCE.getCameraType() != CherrygramCameraConfig.CAMERA_2) {
            items.add(UItem.asButton(cameraAspectRatioRow, getString(R.string.CP_CameraAspectRatio), getCameraAspectRatio())
                    .slug("aspectRatio")
            );
        }
        if (CherrygramCameraConfig.INSTANCE.getCameraType() != CherrygramCameraConfig.CAMERA_2 || CherrygramCoreConfig.isDevBuild()) {
            items.add(UItem.asShadow(null));
        }

        items.add(UItem.asHeader(getString(R.string.CP_Header_Videomessages)));
        if (CameraXUtils.isCurrentCameraCameraX() || CameraXUtils.isCurrentCameraCamera2()) {
            items.add(
                    SettingsHelper.asSwitchCG(
                            newEncoderRow,
                            applySpan(
                                    getString(R.string.CP_NewEncoder),
                                    "BETA",
                                    Theme.key_cgGradient2,
                                    resourcesProvider
                            )
                    )
                    .setChecked(CherrygramCameraConfig.INSTANCE.getUseNewEncoder())
                    .slug("newCameraEncoder")
            );

            if (CherrygramCameraConfig.INSTANCE.getUseNewEncoder() && CameraXUtils.isCurrentCameraCamera2()) {
                items.add(
                        SettingsActivity.SettingCell.Factory.of(
                                newEncoderSettingsRow,
                                0xFFF45255, 0xFFDF3955,
                                R.drawable.settings_camera_filled_solar,
                                getString(R.string.RoundVideoSettings),
                                getString(R.string.Settings),
                                true
                        )
                );
                items.add(UItem.asShadow(null));
            }
        }

        if (CameraXUtils.isCurrentCameraCameraX() || CameraXUtils.isCurrentCameraCamera2()) {
            items.add(SettingsHelper.asSwitchCG(cameraUseDualCameraRow, getString(R.string.CP_CameraDualCamera), getString(R.string.CP_CameraDualCamera_Desc))
                    .setChecked(CherrygramCameraConfig.INSTANCE.getUseDualCamera())
                    .slug("dualCamera")
            );
        }
        if (!(CameraXUtils.isCurrentCameraCamera2() && CherrygramCameraConfig.INSTANCE.getUseDualCamera())) {
            items.add(SettingsHelper.asSwitchCG(rearCamRow, getString(R.string.CP_RearCam), getString(R.string.CP_RearCam_Desc))
                    .setChecked(CherrygramCameraConfig.INSTANCE.getRearCam())
                    .slug("rearCamera")
            );
        }
        if (CameraXUtils.isCurrentCameraCameraX()) {
            items.add(SettingsHelper.asSwitchCG(startFromUltraWideRow, getString(R.string.CP_CameraUW), getString(R.string.CP_CameraUW_Desc))
                    .setChecked(CherrygramCameraConfig.INSTANCE.getStartFromUltraWideCam())
                    .slug("startFromUW")
            );
        }

        if (CameraXUtils.isCurrentCameraCameraX() || CameraXUtils.isCurrentCameraCamera2()) {
            items.add(UItem.asShadow(null));
            if (CameraXUtils.isCurrentCameraCameraX()) {
                if (CherrygramCameraConfig.INSTANCE.getUseNewEncoder()) {
                    items.add(UItem.asButton(newEncoderBitrateRow, getString(R.string.RoundVideoBitrate), formatBitrate(SharedSettings.roundVideoVideoBitrate.get())));
                }
            }
            if (CherrygramCameraConfig.INSTANCE.getUseNewEncoder()) {
                items.add(UItem.asButton(cameraXFpsRangeRow, "FPS", getCameraXFpsRange())
                        .slug("fps")
                );
                items.add(UItem.asShadow(getString(R.string.RoundVideoGeneralInfo)));
            }
            items.add(
                    SettingsHelper.asExpandableSwitch(
                            cameraEnhancementsRow,
                            R.drawable.magic_stick_solar,
                            getString(R.string.CP_CategoryEnhancements),
                            getArchiveStoriesCountText()
                    )
                    .setChecked(useCaptureRequestOptions())
                    .setCollapsed(!expandedCameraEnhancementsSection)
                    .setClickCallback(v -> {
                        boolean newValue = !(useCaptureRequestOptions());

                        CherrygramCameraConfig.INSTANCE.setOpticalStabilisation(newValue);
                        CherrygramCameraConfig.INSTANCE.setVideoStabilisation(newValue);
                        CherrygramCameraConfig.INSTANCE.setContinuousAutofocus(newValue);
                        CherrygramCameraConfig.INSTANCE.setNoiceReduction(newValue);
                        CherrygramCameraConfig.INSTANCE.setFaceDetection(newValue);
                        CherrygramCameraConfig.INSTANCE.setBokehEffect(newValue);

                        expandedCameraEnhancementsSection = !expandedCameraEnhancementsSection;
                        updateRows(true);
                    })
            );
            if (expandedCameraEnhancementsSection) {
                items.add(UItem.asRoundCheckbox(opticalStabilisationRow, getString(R.string.CP_OpticalStabilisation))
                        .setChecked(CherrygramCameraConfig.INSTANCE.getOpticalStabilisation())
                        .setPad(1)
                );

                items.add(UItem.asRoundCheckbox(videoStabilisationRow, getString(R.string.CP_VideoStabilisation))
                        .setChecked(CherrygramCameraConfig.INSTANCE.getVideoStabilisation())
                        .setPad(1)
                );

                items.add(UItem.asRoundCheckbox(continuousAutofocusRow, getString(R.string.CP_ContinuousAutofocus))
                        .setChecked(CherrygramCameraConfig.INSTANCE.getContinuousAutofocus())
                        .setPad(1)
                );

                items.add(UItem.asRoundCheckbox(noiceReductionRow, getString(R.string.CP_NoiseReduction))
                        .setChecked(CherrygramCameraConfig.INSTANCE.getNoiceReduction())
                        .setPad(1)
                );

                items.add(UItem.asRoundCheckbox(faceDetectionRow, getString(R.string.CP_FaceDetection))
                        .setChecked(CherrygramCameraConfig.INSTANCE.getFaceDetection())
                        .setPad(1)
                );

                if (isBokehAvailable()) {
                    items.add(UItem.asRoundCheckbox(bokehEffectRow, applySpan(getString(R.string.CP_BokehEffect), "BETA", Theme.key_cgGradient2, resourcesProvider))
                            .setChecked(CherrygramCameraConfig.INSTANCE.getBokehEffect())
                            .setPad(1)
                    );
                }
            }
            items.add(UItem.asShadow(getString(R.string.CP_EnhancementsNote)));
        } else {
            items.add(UItem.asShadow(null));
        }

        if (CameraXUtils.isCurrentCameraCameraX()) {
            items.add(UItem.asButton(exposureSliderRow, getString(R.string.CP_ExposureSliderPosition), getExposureSliderPosition())
                    .slug("exposureSlider")
            );
        }
        items.add(SettingsHelper.asSwitchCG(cameraControlButtonsRow, getString(R.string.CP_CenterCameraControlButtons), getString(R.string.CP_CenterCameraControlButtons_Desc))
                .setChecked(CherrygramCameraConfig.INSTANCE.getCenterCameraControlButtons())
                .slug("centerControlButtons")
        );
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == disableAttachCameraRow) {
            CherrygramCameraConfig.INSTANCE.setDisableAttachCamera(!CherrygramCameraConfig.INSTANCE.getDisableAttachCamera());
            SettingsHelper.updateCheckState(view, CherrygramCameraConfig.INSTANCE.getDisableAttachCamera());
        } else if (item.id == cameraAspectRatioRow) {
            showAspectRatioSelector(getContext(), () -> SettingsHelper.updateButtonValue(view, getCameraAspectRatio()));
        } else if (item.id == cameraUseDualCameraRow) {
            CherrygramCameraConfig.INSTANCE.setUseDualCamera(!CherrygramCameraConfig.INSTANCE.getUseDualCamera());
            SettingsHelper.updateCheckState(view, CherrygramCameraConfig.INSTANCE.getUseDualCamera());

            if (CameraXUtils.isCurrentCameraCameraX()) {
                CherrygramCameraConfig.INSTANCE.setCameraAspectRatio(CherrygramCameraConfig.Camera4to3);
            }

            updateRows(true);
        } else if (item.id == rearCamRow) {
            CherrygramCameraConfig.INSTANCE.setRearCam(!CherrygramCameraConfig.INSTANCE.getRearCam());
            SettingsHelper.updateCheckState(view, CherrygramCameraConfig.INSTANCE.getRearCam());
        } else if (item.id == startFromUltraWideRow) {
            CherrygramCameraConfig.INSTANCE.setStartFromUltraWideCam(!CherrygramCameraConfig.INSTANCE.getStartFromUltraWideCam());
            SettingsHelper.updateCheckState(view, CherrygramCameraConfig.INSTANCE.getStartFromUltraWideCam());
        } else if (item.id == cameraXFpsRangeRow) {
            ArrayList<String> configStringKeys = new ArrayList<>();
            ArrayList<Integer> configValues = new ArrayList<>();

            configStringKeys.add("30");
            configValues.add(CherrygramCameraConfig.CameraXFpsRange30);

            configStringKeys.add("60");
            configValues.add(CherrygramCameraConfig.CameraXFpsRange60);

            PopupHelper.show(configStringKeys, "FPS", configValues.indexOf(CherrygramCameraConfig.INSTANCE.getCameraXFpsRange()), getContext(), i -> {
                CherrygramCameraConfig.INSTANCE.setCameraXFpsRange(configValues.get(i));
                SettingsHelper.updateButtonValue(view, getCameraXFpsRange());
            });
        } else if (item.id == cameraEnhancementsRow) {
            expandedCameraEnhancementsSection = !expandedCameraEnhancementsSection;
            item.collapsed = !item.collapsed;

            updateRows(true);
        } else if (item.id == opticalStabilisationRow) {
            CherrygramCameraConfig.INSTANCE.setOpticalStabilisation(!CherrygramCameraConfig.INSTANCE.getOpticalStabilisation());
            updateRows(true);
        } else if (item.id == videoStabilisationRow) {
            CherrygramCameraConfig.INSTANCE.setVideoStabilisation(!CherrygramCameraConfig.INSTANCE.getVideoStabilisation());
            updateRows(true);
        } else if (item.id == continuousAutofocusRow) {
            CherrygramCameraConfig.INSTANCE.setContinuousAutofocus(!CherrygramCameraConfig.INSTANCE.getContinuousAutofocus());
            updateRows(true);
        } else if (item.id == noiceReductionRow) {
            CherrygramCameraConfig.INSTANCE.setNoiceReduction(!CherrygramCameraConfig.INSTANCE.getNoiceReduction());
            updateRows(true);
        } else if (item.id == faceDetectionRow) {
            CherrygramCameraConfig.INSTANCE.setFaceDetection(!CherrygramCameraConfig.INSTANCE.getFaceDetection());
            updateRows(true);
        } else if (item.id == bokehEffectRow) {
            CherrygramCameraConfig.INSTANCE.setBokehEffect(!CherrygramCameraConfig.INSTANCE.getBokehEffect());
            updateRows(true);
        } else if (item.id == exposureSliderRow) {
            ArrayList<String> configStringKeys = new ArrayList<>();
            ArrayList<Integer> configValues = new ArrayList<>();

            /*configStringKeys.add(getString(R.string.CP_ZoomSliderPosition_Bottom));
            configValues.add(CherrygramCameraConfig.EXPOSURE_SLIDER_BOTTOM);*/

            configStringKeys.add(getString(R.string.CP_ZoomSliderPosition_Right));
            configValues.add(CherrygramCameraConfig.EXPOSURE_SLIDER_RIGHT);

            configStringKeys.add(getString(R.string.CP_ZoomSliderPosition_Left));
            configValues.add(CherrygramCameraConfig.EXPOSURE_SLIDER_LEFT);

            configStringKeys.add(getString(R.string.Disable));
            configValues.add(CherrygramCameraConfig.EXPOSURE_SLIDER_NONE);

            PopupHelper.show(configStringKeys, getString(R.string.CP_ExposureSliderPosition), configValues.indexOf(CherrygramCameraConfig.INSTANCE.getExposureSlider()), getContext(), i -> {
                CherrygramCameraConfig.INSTANCE.setExposureSlider(configValues.get(i));
                SettingsHelper.updateButtonValue(view, getExposureSliderPosition());
            });
        } else if (item.id == cameraControlButtonsRow) {
            CherrygramCameraConfig.INSTANCE.setCenterCameraControlButtons(!CherrygramCameraConfig.INSTANCE.getCenterCameraControlButtons());
            SettingsHelper.updateCheckState(view, CherrygramCameraConfig.INSTANCE.getCenterCameraControlButtons());
        } else if (item.id == newEncoderRow) {
            CherrygramCameraConfig.INSTANCE.setUseNewEncoder(!CherrygramCameraConfig.INSTANCE.getUseNewEncoder());
            SettingsHelper.updateCheckState(view, CherrygramCameraConfig.INSTANCE.getUseNewEncoder());

            updateRows(true);
        } else if (item.id == newEncoderSettingsRow) {
            presentFragment(new RoundVideoSettingsActivity());
        } else if (item.id == newEncoderBitrateRow) {
            ArrayList<String> configStringKeys = new ArrayList<>();
            ArrayList<Integer> configValues = new ArrayList<>();
            for (int bitrate : NEW_ENCODER_BITRATES) {
                configStringKeys.add(formatBitrate(bitrate));
                configValues.add(bitrate);
            }

            PopupHelper.show(configStringKeys, getString(R.string.RoundVideoBitrate), configValues.indexOf(SharedSettings.roundVideoVideoBitrate.get()), getContext(), i -> {
                SharedSettings.roundVideoVideoBitrate.set(configValues.get(i));
                SettingsHelper.updateButtonValue(view, formatBitrate(SharedSettings.roundVideoVideoBitrate.get()));
            });
        }
    }

    @Override
    public void scrollToRow(String slug, Runnable unknown) {
        boolean needsExpand = false;
        if (ENHANCEMENTS_SECTION_SLUGS.contains(slug) && !expandedCameraEnhancementsSection) {
            expandedCameraEnhancementsSection = true;
            needsExpand = true;
        }
        if (needsExpand) {
            updateRows(true);
        }
        super.scrollToRow(slug, unknown);
    }

    private String getArchiveStoriesCountText() {
        int count = 0;

        if (CherrygramCameraConfig.INSTANCE.getOpticalStabilisation()) count++;
        if (CherrygramCameraConfig.INSTANCE.getVideoStabilisation()) count++;
        if (CherrygramCameraConfig.INSTANCE.getContinuousAutofocus()) count++;
        if (CherrygramCameraConfig.INSTANCE.getNoiceReduction()) count++;
        if (CherrygramCameraConfig.INSTANCE.getFaceDetection()) count++;
        if (CherrygramCameraConfig.INSTANCE.getBokehEffect() && isBokehAvailable()) count++;

        return count + (isBokehAvailable() ? "/6" : "/5");
    }

    private boolean useCaptureRequestOptions() {
        return CherrygramCameraConfig.INSTANCE.getOpticalStabilisation() || CherrygramCameraConfig.INSTANCE.getVideoStabilisation()
                || CherrygramCameraConfig.INSTANCE.getContinuousAutofocus()
                || CherrygramCameraConfig.INSTANCE.getNoiceReduction()
                || CherrygramCameraConfig.INSTANCE.getFaceDetection()
                || (CherrygramCameraConfig.INSTANCE.getBokehEffect() && isBokehAvailable());
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

        if (!CherrygramCameraConfig.INSTANCE.getUseDualCamera() && CameraXUtils.isCurrentCameraCameraX()) {
            configStringKeys.add(getString(R.string.Default));
            configValues.add(CherrygramCameraConfig.CameraAspectDefault);
        }

        PopupHelper.show(configStringKeys, getString(R.string.CP_CameraAspectRatio), configValues.indexOf(CherrygramCameraConfig.INSTANCE.getCameraAspectRatio()), context, i -> {
            CherrygramCameraConfig.INSTANCE.setCameraAspectRatio(configValues.get(i));
            if (runnable != null) runnable.run();
        });
    }

    private boolean isBokehAvailable() {
        return CameraXUtils.isCurrentCameraCameraX()
//                && !CherrygramCameraConfig.INSTANCE.getUseDualCamera()
                && SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_AVERAGE; // PERFORMANCE_CLASS_HIGH
    }

    public static String getCameraName() {
        return switch (CherrygramCameraConfig.INSTANCE.getCameraType()) {
            case CherrygramCameraConfig.TELEGRAM_CAMERA -> "Telegram";
            case CherrygramCameraConfig.CAMERA_X -> "CameraX";
            case CherrygramCameraConfig.CAMERA_2 -> "Camera 2 (Telegram)";
            default -> getString(R.string.CP_CameraTypeSystem);
        };
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
        return VideoMessagesHelper.isCamera60Fps() ? "60" : "30";
    }

    private String getExposureSliderPosition() {
        return switch (CherrygramCameraConfig.INSTANCE.getExposureSlider()) {
            case CherrygramCameraConfig.EXPOSURE_SLIDER_BOTTOM -> getString(R.string.CP_ZoomSliderPosition_Bottom);
            case CherrygramCameraConfig.EXPOSURE_SLIDER_RIGHT -> getString(R.string.CP_ZoomSliderPosition_Right);
            case CherrygramCameraConfig.EXPOSURE_SLIDER_LEFT -> getString(R.string.CP_ZoomSliderPosition_Left);
            default -> getString(R.string.Disable);
        };
    }

    private String formatBitrate(int bitrate) {
        if (bitrate % 1_000_000 == 0) {
            return (bitrate / 1_000_000) + " Mbps";
        }
        if (bitrate > 1_000_000) {
            return String.format(Locale.US, "%.1f Mbps", bitrate / 1_000_000f);
        }
        return (bitrate / 1_000) + " kbps";
    }

}
