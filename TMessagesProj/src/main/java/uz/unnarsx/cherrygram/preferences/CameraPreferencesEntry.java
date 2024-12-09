package uz.unnarsx.cherrygram.preferences;

import static org.telegram.messenger.LocaleController.getString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Size;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.camera.video.Quality;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uz.unnarsx.cherrygram.camera.CameraTypeSelector;
import uz.unnarsx.cherrygram.camera.CameraXUtils;
import uz.unnarsx.cherrygram.core.configs.CherrygramCameraConfig;
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper;
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper;
import uz.unnarsx.cherrygram.core.helpers.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.helpers.ui.PopupHelper;

public class CameraPreferencesEntry extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private int rowCount;
    private ListAdapter listAdapter;
    private RecyclerListView listView;
    private int cameraTypeHeaderRow;
    private int cameraTypeSelectorRow;
    private int cameraAdviseRow;

    private int audioVideoHeaderRow;
    private int disableAttachCameraRow;
    private int cameraAspectRatioRow;
    private int audioVideoDivisorRow;

    private int videoMessagesHeaderRow;
    private int cameraXQualityRow;
    private int cameraUseDualCameraRow;
    private int startFromUltraWideRow;
    private int cameraXFpsRangeRow;
    private int cameraXCameraEffectsRow;
    private int cameraStabilisationRow;
    private int exposureSliderRow;
    private int cameraControlButtonsRow;
    private int rearCamRow;
    private int captureTypeFrontRow;
    private int captureTypeBackRow;
    private int captureTypeAdviseRow;

    public LinearLayoutManager layoutManager;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiLoaded);
        updateRowsId(true);
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiLoaded);
    }

    protected boolean hasWhiteActionBar() {
        return true;
    }

    @Override
    public boolean isLightStatusBar() {
        if (!hasWhiteActionBar()) return super.isLightStatusBar();
        int color = getThemedColor(Theme.key_windowBackgroundWhite);
        return ColorUtils.calculateLuminance(color) > 0.7f;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);

        actionBar.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
        actionBar.setItemsColor(getThemedColor(Theme.key_windowBackgroundWhiteBlackText), false);
        actionBar.setItemsBackgroundColor(getThemedColor(Theme.key_actionBarActionModeDefaultSelector), true);
        actionBar.setItemsBackgroundColor(getThemedColor(Theme.key_actionBarWhiteSelector), false);
        actionBar.setItemsColor(getThemedColor(Theme.key_actionBarActionModeDefaultIcon), true);
        actionBar.setTitleColor(getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
        actionBar.setCastShadows(false);

        actionBar.setTitle(getString(R.string.CP_Category_Camera));
        actionBar.setAllowOverlayTitle(false);

        actionBar.setOccupyStatusBar(!AndroidUtilities.isTablet());
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        listAdapter = new ListAdapter(context);

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(false);
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false) {
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (IndexOutOfBoundsException e) {
//                    Log.e("TAG", "meet a IOOBE in RecyclerView");
                }
            }
        });
        listView.setAdapter(listAdapter);
        if (listView.getItemAnimator() != null) {
            ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
        }
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setOnItemClickListener((view, position, x, y) -> {
            if (position == disableAttachCameraRow) {
                CherrygramCameraConfig.INSTANCE.toggleDisableAttachCamera();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramCameraConfig.INSTANCE.getDisableAttachCamera());
                }
                AppRestartHelper.createRestartBulletin(this);
            } else if (position == cameraAspectRatioRow) {
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

                    listAdapter.notifyItemChanged(cameraAspectRatioRow);
                });
            } else if (position == cameraXQualityRow) {
                Map<Quality, Size> availableSizes = CameraXUtils.getAvailableVideoSizes();
                Stream<Integer> tmp = availableSizes.values().stream().sorted(Comparator.comparingInt(Size::getWidth).reversed()).map(Size::getHeight);
                ArrayList<Integer> types = tmp.collect(Collectors.toCollection(ArrayList::new));
                ArrayList<String> arrayList = types.stream().map(p -> p + "p").collect(Collectors.toCollection(ArrayList::new));
                PopupHelper.show(arrayList, getString(R.string.CP_CameraQuality), types.indexOf(CherrygramCameraConfig.INSTANCE.getCameraResolution()), context, i -> {
                    CherrygramCameraConfig.INSTANCE.setCameraResolution(types.get(i));

                    listAdapter.notifyItemChanged(cameraXQualityRow);
                    AppRestartHelper.createRestartBulletin(this);
                });
            } else if (position == cameraUseDualCameraRow) {
                CherrygramCameraConfig.INSTANCE.toggleUseDualCamera();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramCameraConfig.INSTANCE.getUseDualCamera());
                }

                listAdapter.notifyItemChanged(rearCamRow);
                updateRowsId(false);
                parentLayout.rebuildAllFragmentViews(false, false);
            } else if (position == startFromUltraWideRow) {
                CherrygramCameraConfig.INSTANCE.toggleStartFromUltraWideCam();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramCameraConfig.INSTANCE.getStartFromUltraWideCam());
                }
            } else if (position == cameraXFpsRangeRow) {
                ArrayList<String> configStringKeys = new ArrayList<>();
                ArrayList<Integer> configValues = new ArrayList<>();

                configStringKeys.add("25-30");
                configValues.add(CherrygramCameraConfig.CameraXFpsRange25to30);

                configStringKeys.add("30-60");
                configValues.add(CherrygramCameraConfig.CameraXFpsRange30to60);

                configStringKeys.add("60-60");
                configValues.add(CherrygramCameraConfig.CameraXFpsRange60to60);

                configStringKeys.add(getString(R.string.Default));
                configValues.add(CherrygramCameraConfig.CameraXFpsRangeDefault);

                PopupHelper.show(configStringKeys, "FPS", configValues.indexOf(CherrygramCameraConfig.INSTANCE.getCameraXFpsRange()), context, i -> {
                    CherrygramCameraConfig.INSTANCE.setCameraXFpsRange(configValues.get(i));

                    listAdapter.notifyItemChanged(cameraXFpsRangeRow);
                });
            } else if (position == cameraXCameraEffectsRow) {
                ArrayList<String> configStringKeys = new ArrayList<>();
                ArrayList<Integer> configValues = new ArrayList<>();

                configStringKeys.add("MONO");
                configValues.add(CherrygramCameraConfig.CONTROL_EFFECT_MODE_MONO);

                configStringKeys.add("NEGATIVE");
                configValues.add(CherrygramCameraConfig.CONTROL_EFFECT_MODE_NEGATIVE);

                configStringKeys.add("SOLARIZE");
                configValues.add(CherrygramCameraConfig.CONTROL_EFFECT_MODE_SOLARIZE);

                configStringKeys.add("SEPIA");
                configValues.add(CherrygramCameraConfig.CONTROL_EFFECT_MODE_SEPIA);

                configStringKeys.add("POSTERIZE");
                configValues.add(CherrygramCameraConfig.CONTROL_EFFECT_MODE_POSTERIZE);

                configStringKeys.add("WHITEBOARD");
                configValues.add(CherrygramCameraConfig.CONTROL_EFFECT_MODE_WHITEBOARD);

                configStringKeys.add("BLACKBOARD");
                configValues.add(CherrygramCameraConfig.CONTROL_EFFECT_MODE_BLACKBOARD);

                configStringKeys.add("AQUA");
                configValues.add(CherrygramCameraConfig.CONTROL_EFFECT_MODE_AQUA);

                configStringKeys.add(getString(R.string.Default));
                configValues.add(CherrygramCameraConfig.CONTROL_EFFECT_MODE_OFF);

                PopupHelper.show(configStringKeys, "Effect", configValues.indexOf(CherrygramCameraConfig.INSTANCE.getCameraXCameraEffect()), context, i -> {
                    CherrygramCameraConfig.INSTANCE.setCameraXCameraEffect(configValues.get(i));

                    listAdapter.notifyItemChanged(cameraXCameraEffectsRow);
                });
            } else if (position == cameraStabilisationRow) {
                CherrygramCameraConfig.INSTANCE.toggleCameraStabilisation();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramCameraConfig.INSTANCE.getCameraStabilisation());
                }
            } else if (position == exposureSliderRow) {
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

                PopupHelper.show(configStringKeys, getString(R.string.CP_ExposureSliderPosition), configValues.indexOf(CherrygramCameraConfig.INSTANCE.getExposureSlider()), context, i -> {
                    CherrygramCameraConfig.INSTANCE.setExposureSlider(configValues.get(i));

                    listAdapter.notifyItemChanged(exposureSliderRow);
                });
            } else if (position == cameraControlButtonsRow) {
                CherrygramCameraConfig.INSTANCE.toggleCenterCameraControlButtons();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramCameraConfig.INSTANCE.getCenterCameraControlButtons());
                }
            } else if (position == rearCamRow) {
                CherrygramCameraConfig.INSTANCE.toggleRearCam();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramCameraConfig.INSTANCE.getRearCam());
                }
            } else if (position == captureTypeFrontRow) {
                ArrayList<String> configStringKeys = new ArrayList<>();
                ArrayList<Integer> configValues = new ArrayList<>();

                configStringKeys.add("ImageCapture");
                configValues.add(CherrygramCameraConfig.CaptureType_ImageCapture);

                configStringKeys.add("VideoCapture");
                configValues.add(CherrygramCameraConfig.CaptureType_VideoCapture);

                PopupHelper.show(configStringKeys, "Capture type (Front camera)", configValues.indexOf(CherrygramCameraConfig.INSTANCE.getCaptureTypeFront()), context, i -> {
                    CherrygramCameraConfig.INSTANCE.setCaptureTypeFront(configValues.get(i));

                    listAdapter.notifyItemChanged(captureTypeFrontRow);
                    listAdapter.notifyItemChanged(cameraXCameraEffectsRow);
                    updateRowsId(true);
                });
            } else if (position == captureTypeBackRow) {
                ArrayList<String> configStringKeys = new ArrayList<>();
                ArrayList<Integer> configValues = new ArrayList<>();

                configStringKeys.add("ImageCapture");
                configValues.add(CherrygramCameraConfig.CaptureType_ImageCapture);

                configStringKeys.add("VideoCapture");
                configValues.add(CherrygramCameraConfig.CaptureType_VideoCapture);

                PopupHelper.show(configStringKeys, "Capture type (Back camera)", configValues.indexOf(CherrygramCameraConfig.INSTANCE.getCaptureTypeBack()), context, i -> {
                    CherrygramCameraConfig.INSTANCE.setCaptureTypeBack(configValues.get(i));

                    listAdapter.notifyItemChanged(captureTypeBackRow);
                    listAdapter.notifyItemChanged(cameraXCameraEffectsRow);
                    updateRowsId(true);
                });
            }
        });

        FirebaseAnalyticsHelper.trackEventWithEmptyBundle("camera_preferences_screen");

        return fragmentView;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateRowsId(boolean notify) {
        rowCount = 0;

        if (CameraXUtils.isCameraXSupported()) {
            cameraTypeHeaderRow = rowCount++;
            cameraTypeSelectorRow = rowCount++;
            cameraAdviseRow = rowCount++;
        } else {
            cameraTypeHeaderRow = -1;
            cameraTypeSelectorRow = -1;
            cameraAdviseRow = -1;
        }

        audioVideoHeaderRow = rowCount++;
        disableAttachCameraRow = rowCount++;

        if (CherrygramCameraConfig.INSTANCE.getCameraType() != CherrygramCameraConfig.CAMERA_2) {
            cameraAspectRatioRow = rowCount++;
        } else {
            cameraAspectRatioRow = -1;
        }
        audioVideoDivisorRow = rowCount++;

        videoMessagesHeaderRow = rowCount++;
        if (CameraXUtils.isCurrentCameraCameraX()) {
            cameraXQualityRow = rowCount++;
        } else {
            cameraXQualityRow = -1;
        }

        if (CherrygramCameraConfig.INSTANCE.getCameraType() == CherrygramCameraConfig.CAMERA_2) {
            cameraUseDualCameraRow = rowCount++;
        } else {
            cameraUseDualCameraRow = -1;
        }

        if (CameraXUtils.isCurrentCameraCameraX()) {
            startFromUltraWideRow = rowCount++;
            cameraXFpsRangeRow = rowCount++;
            if (CherrygramCameraConfig.INSTANCE.getCaptureTypeFront() == CherrygramCameraConfig.CaptureType_ImageCapture
                    || CherrygramCameraConfig.INSTANCE.getCaptureTypeBack() == CherrygramCameraConfig.CaptureType_ImageCapture) {
                cameraXCameraEffectsRow = rowCount++;
            } else {
                cameraXCameraEffectsRow = -1;
            }
            cameraStabilisationRow = rowCount++;
            exposureSliderRow = rowCount++;
        } else {
            startFromUltraWideRow = -1;
            cameraXFpsRangeRow = -1;
            cameraXCameraEffectsRow = -1;
            cameraStabilisationRow = -1;
            exposureSliderRow = -1;
        }

        cameraControlButtonsRow = rowCount++;

        if (CherrygramCameraConfig.INSTANCE.getCameraType() == CherrygramCameraConfig.CAMERA_2 && CherrygramCameraConfig.INSTANCE.getUseDualCamera()) {
            rearCamRow = -1;
        } else {
            rearCamRow = rowCount++;;
        }

        if (CameraXUtils.isCurrentCameraCameraX()) {
            captureTypeFrontRow = rowCount++;
            captureTypeBackRow = rowCount++;
            captureTypeAdviseRow = rowCount++;
        } else {
            captureTypeFrontRow = -1;
            captureTypeBackRow = -1;
            captureTypeAdviseRow = -1;
        }

        if (listAdapter != null && notify) {
            listAdapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {
        private final Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 1:
                    holder.itemView.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case 2:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == cameraTypeHeaderRow) {
                        headerCell.setText(getString(R.string.CP_CameraType));
                    } else if (position == audioVideoHeaderRow) {
                        headerCell.setText(getString(R.string.CP_Category_Camera));
                    } else if (position == videoMessagesHeaderRow) {
                        headerCell.setText(getString(R.string.CP_Header_Videomessages));
                    }
                    break;
                case 3:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    textCheckCell.setEnabled(true, null);
                    if (position == disableAttachCameraRow) {
                        textCheckCell.setTextAndValueAndCheck(getString(R.string.CP_DisableCam), getString(R.string.CP_DisableCam_Desc), CherrygramCameraConfig.INSTANCE.getDisableAttachCamera(), true, true);
                    } else if (position == cameraUseDualCameraRow) {
                        textCheckCell.setTextAndValueAndCheck(getString(R.string.CP_CameraDualCamera), getString(R.string.CP_CameraDualCamera_Desc), CherrygramCameraConfig.INSTANCE.getUseDualCamera(), true, true);
                    } else if (position == startFromUltraWideRow) {
                        textCheckCell.setTextAndValueAndCheck(getString(R.string.CP_CameraUW), getString(R.string.CP_CameraUW_Desc), CherrygramCameraConfig.INSTANCE.getStartFromUltraWideCam(), true, true);
                    } else if (position == cameraStabilisationRow) {
                        textCheckCell.setTextAndCheck(getString(R.string.CP_CameraStabilisation), CherrygramCameraConfig.INSTANCE.getCameraStabilisation(), true);
                    } else if (position == cameraControlButtonsRow) {
                        textCheckCell.setTextAndValueAndCheck(getString(R.string.CP_CenterCameraControlButtons), getString(R.string.CP_CenterCameraControlButtons_Desc), CherrygramCameraConfig.INSTANCE.getCenterCameraControlButtons(), true, true);
                    } else if (position == rearCamRow) {
                        textCheckCell.setTextAndValueAndCheck(getString(R.string.CP_RearCam), getString(R.string.CP_RearCam_Desc), CherrygramCameraConfig.INSTANCE.getRearCam(), true, true);
                    }
                    break;
                case 6:
                    TextInfoPrivacyCell textInfoPrivacyCell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == cameraAdviseRow) {
                        textInfoPrivacyCell.setText(CGResourcesHelper.getCameraAdvise());
                    } else if (position == captureTypeAdviseRow) {
                        textInfoPrivacyCell.setText(getString(R.string.CP_CaptureType_Desc));
                    }
                    break;
                case 7:
                    TextSettingsCell textSettingsCell = (TextSettingsCell) holder.itemView;
                    textSettingsCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    TextSettingsCell textCell = (TextSettingsCell) holder.itemView;
                    textCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    if (position == cameraAspectRatioRow) {
                        textCell.setTextAndValue(getString(R.string.CP_CameraAspectRatio), CGResourcesHelper.getCameraAspectRatio(), true);
                    } else if (position == cameraXQualityRow) {
                        textSettingsCell.setTextAndValue(getString(R.string.CP_CameraQuality), CherrygramCameraConfig.INSTANCE.getCameraResolution() + "p", true);
                    } else if (position == cameraXFpsRangeRow) {
                        textCell.setTextAndValue("FPS", CGResourcesHelper.getCameraXFpsRange(), true);
                    } else if (position == cameraXCameraEffectsRow) {
                        textCell.setTextAndValue("Effect", CGResourcesHelper.getCameraXCameraEffect(), true);
                    } else if (position == exposureSliderRow) {
                        textCell.setTextAndValue(getString(R.string.CP_ExposureSliderPosition), CGResourcesHelper.getExposureSliderPosition(), true);
                    } else if (position == captureTypeFrontRow) {
                        textCell.setTextAndValue("Capture type (Front camera)", CGResourcesHelper.getCameraCaptureTypeFront(), false);
                    } else if (position == captureTypeBackRow) {
                        textCell.setTextAndValue("Capture type (Back camera)", CGResourcesHelper.getCameraCaptureTypeBack(), false);
                    }
                    break;
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == 3 || type == 7;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 2:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 3:
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 5:
                    view = new CameraTypeSelector(mContext) {
                        @Override
                        protected void onSelectedCamera(int cameraSelected) {
                            super.onSelectedCamera(cameraSelected);

                            CherrygramCameraConfig.INSTANCE.setCameraType(cameraSelected);

                            updateRowsId(false);

                            listAdapter.notifyItemChanged(cameraAdviseRow);

                            listAdapter.notifyItemChanged(disableAttachCameraRow);
                            listAdapter.notifyItemChanged(cameraAspectRatioRow);
                            listAdapter.notifyItemChanged(audioVideoDivisorRow);

                            listAdapter.notifyItemChanged(cameraXQualityRow);
                            listAdapter.notifyItemChanged(cameraUseDualCameraRow);
                            listAdapter.notifyItemChanged(startFromUltraWideRow);
                            listAdapter.notifyItemChanged(cameraXFpsRangeRow);
                            listAdapter.notifyItemChanged(cameraXCameraEffectsRow);
                            listAdapter.notifyItemChanged(cameraStabilisationRow);
                            listAdapter.notifyItemChanged(exposureSliderRow);
                            listAdapter.notifyItemChanged(cameraControlButtonsRow);
                            listAdapter.notifyItemChanged(rearCamRow);
                            listAdapter.notifyItemChanged(captureTypeFrontRow);
                            listAdapter.notifyItemChanged(captureTypeBackRow);
                            listAdapter.notifyItemChanged(captureTypeAdviseRow);
                        }
                    };
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 6:
                    TextInfoPrivacyCell textInfoPrivacyCell = new TextInfoPrivacyCell(mContext);
                    textInfoPrivacyCell.setBottomPadding(16);
                    view = textInfoPrivacyCell;
                    break;
                case 7:
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                default:
                    view = new ShadowSectionCell(mContext);
                    break;
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == audioVideoDivisorRow) {
                return 1;
            } else if (position == cameraTypeHeaderRow || position == audioVideoHeaderRow || position == videoMessagesHeaderRow) {
                return 2;
            } else if (position == disableAttachCameraRow || position == cameraUseDualCameraRow || position == startFromUltraWideRow || position == cameraStabilisationRow || position == cameraControlButtonsRow || position == rearCamRow) {
                return 3;
            } else if (position == cameraTypeSelectorRow) {
                return 5;
            } else if (position == cameraAdviseRow || position == captureTypeAdviseRow) {
                return 6;
            } else if (position == cameraAspectRatioRow || position == cameraXQualityRow || position == cameraXFpsRangeRow || position == cameraXCameraEffectsRow || position == exposureSliderRow || position == captureTypeFrontRow || position == captureTypeBackRow) {
                return 7;
            }
            return 1;
        }
    }

    @Override
    public void didReceivedNotification(int id, int account, final Object... args) {
        if (id == NotificationCenter.emojiLoaded) {
            if (listView != null) {
                listView.invalidateViews();
            }
        }
    }
}
