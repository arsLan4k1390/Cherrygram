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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.core.configs.CherrygramDebugConfig;
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper;
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper;
import uz.unnarsx.cherrygram.core.helpers.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.helpers.ui.PopupHelper;

public class CameraPreferencesEntry extends BaseFragment {

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
    private int cameraStabilisationRow;
    private int exposureSliderRow;
    private int cameraControlButtonsRow;
    private int rearCamRow;
    private int rearCamRowDivisorRow;

    public LinearLayoutManager layoutManager;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        updateRowsId(true);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
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
                CherrygramCameraConfig.INSTANCE.setDisableAttachCamera(!CherrygramCameraConfig.INSTANCE.getDisableAttachCamera());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramCameraConfig.INSTANCE.getDisableAttachCamera());
                }
                AppRestartHelper.createRestartBulletin(this);
            } else if (position == cameraAspectRatioRow) {
                ArrayList<String> configStringKeys = new ArrayList<>();
                ArrayList<Integer> configValues = new ArrayList<>();

                if (CameraXUtils.isCurrentCameraNotCameraX()) {
                    configStringKeys.add("1:1");
                    configValues.add(CherrygramCameraConfig.Camera1to1);
                }

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
                });
            } else if (position == cameraUseDualCameraRow) {
                CherrygramCameraConfig.INSTANCE.setUseDualCamera(!CherrygramCameraConfig.INSTANCE.getUseDualCamera());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramCameraConfig.INSTANCE.getUseDualCamera());
                }

                listAdapter.notifyItemChanged(rearCamRow);
                updateRowsId(true);
                parentLayout.rebuildAllFragmentViews(false, false);
            } else if (position == startFromUltraWideRow) {
                CherrygramCameraConfig.INSTANCE.setStartFromUltraWideCam(!CherrygramCameraConfig.INSTANCE.getStartFromUltraWideCam());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramCameraConfig.INSTANCE.getStartFromUltraWideCam());
                }
            } else if (position == cameraXFpsRangeRow) {
//                if (!CherrygramCoreConfig.INSTANCE.isDevBuild()) return;
                ArrayList<String> configStringKeys = new ArrayList<>();
                ArrayList<Integer> configValues = new ArrayList<>();

                configStringKeys.add("25-30");
                configValues.add(CherrygramCameraConfig.CameraXFpsRange25to30);

                configStringKeys.add("30-30");
                configValues.add(CherrygramCameraConfig.CameraXFpsRange30to30);

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
            } else if (position == cameraStabilisationRow) {
                CherrygramCameraConfig.INSTANCE.setCameraStabilisation(!CherrygramCameraConfig.INSTANCE.getCameraStabilisation());
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
                CherrygramCameraConfig.INSTANCE.setCenterCameraControlButtons(!CherrygramCameraConfig.INSTANCE.getCenterCameraControlButtons());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramCameraConfig.INSTANCE.getCenterCameraControlButtons());
                }
            } else if (position == rearCamRow) {
                CherrygramCameraConfig.INSTANCE.setRearCam(!CherrygramCameraConfig.INSTANCE.getRearCam());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramCameraConfig.INSTANCE.getRearCam());
                }
            }
        });

        FirebaseAnalyticsHelper.trackEventWithEmptyBundle("camera_preferences_screen");

        return fragmentView;
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private final Context mContext;

        private final int VIEW_TYPE_SHADOW = 0;
        private final int VIEW_TYPE_HEADER = 1;
        //        private final int VIEW_TYPE_TEXT_CELL = 2;
        private final int VIEW_TYPE_TEXT_CHECK = 3;
        private final int VIEW_TYPE_TEXT_SETTINGS = 4;
        private final int VIEW_TYPE_TEXT_INFO_PRIVACY = 5;
        //        private final int VIEW_TYPE_TEXT_DETAIL_SETTINGS = 6;
        private final int VIEW_TYPE_CAMERA_SELECTOR = 7;

        ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case VIEW_TYPE_SHADOW:
                    holder.itemView.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    applyMD3Background(holder, position);
                    break;
                case VIEW_TYPE_HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == cameraTypeHeaderRow) {
                        headerCell.setText(getString(R.string.CP_CameraType));
                    } else if (position == audioVideoHeaderRow) {
                        headerCell.setText(getString(R.string.CP_Category_Camera));
                    } else if (position == videoMessagesHeaderRow) {
                        headerCell.setText(getString(R.string.CP_Header_Videomessages));
                    }
                    applyMD3Background(holder, position);
                    break;
                case VIEW_TYPE_TEXT_CHECK:
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
                    applyMD3Background(holder, position);
                    break;
                case VIEW_TYPE_TEXT_SETTINGS:
                    TextSettingsCell textSettingsCell = (TextSettingsCell) holder.itemView;
                    textSettingsCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));

                    if (position == cameraAspectRatioRow) {
                        textSettingsCell.setTextAndValue(getString(R.string.CP_CameraAspectRatio), CGResourcesHelper.INSTANCE.getCameraAspectRatio(), true);
                    } else if (position == cameraXQualityRow) {
                        textSettingsCell.setTextAndValue(getString(R.string.CP_CameraQuality), CherrygramCameraConfig.INSTANCE.getCameraResolution() + "p", true);
                    } else if (position == cameraXFpsRangeRow) {
                        textSettingsCell.setTextAndValue("FPS", CGResourcesHelper.INSTANCE.getCameraXFpsRange(), true);
                    } else if (position == exposureSliderRow) {
                        textSettingsCell.setTextAndValue(getString(R.string.CP_ExposureSliderPosition), CGResourcesHelper.INSTANCE.getExposureSliderPosition(), true);
                    }
                    applyMD3Background(holder, position);
                    break;
                case VIEW_TYPE_TEXT_INFO_PRIVACY:
                    TextInfoPrivacyCell textInfoPrivacyCell = (TextInfoPrivacyCell) holder.itemView;
                    textInfoPrivacyCell.setBottomPadding(16);
                    if (position == cameraAdviseRow) {
                        textInfoPrivacyCell.setText(CGResourcesHelper.INSTANCE.getCameraAdvise());
                    }
                    applyMD3Background(holder, position);
                    break;
                case VIEW_TYPE_CAMERA_SELECTOR:
                    /*CameraTypeSelector cameraTypeSelector = (CameraTypeSelector) holder.itemView;
                    if (position == cameraAdviseRow) {

                    }*/
                    applyMD3Background(holder, position);
                    break;
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == VIEW_TYPE_TEXT_CHECK || type == VIEW_TYPE_TEXT_SETTINGS;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case VIEW_TYPE_SHADOW:
                    view = new ShadowSectionCell(mContext);
                    break;
                case VIEW_TYPE_HEADER:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_TEXT_CHECK:
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_TEXT_SETTINGS:
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_TEXT_INFO_PRIVACY:
                    view = new TextInfoPrivacyCell(mContext);
                    break;
                case VIEW_TYPE_CAMERA_SELECTOR:
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
                            /*if (CherrygramCoreConfig.INSTANCE.isDevBuild())*/ listAdapter.notifyItemChanged(cameraXFpsRangeRow);
                            listAdapter.notifyItemChanged(cameraStabilisationRow);
                            listAdapter.notifyItemChanged(exposureSliderRow);
                            listAdapter.notifyItemChanged(cameraControlButtonsRow);

                            listAdapter.notifyItemChanged(rearCamRow);
                            listAdapter.notifyItemChanged(rearCamRowDivisorRow);
                        }
                    };
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + viewType);
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            if (!CherrygramDebugConfig.INSTANCE.getMdContainers()) return;

            int viewType = holder.getItemViewType();
            int position = holder.getAdapterPosition();

            if (viewType == VIEW_TYPE_SHADOW || viewType == VIEW_TYPE_TEXT_INFO_PRIVACY)
                return;

            int side = AndroidUtilities.dp(16);
            int top = 0;
            int bottom = 0;

            boolean prevIsHeader = position > 0 && getItemViewType(position - 1) == VIEW_TYPE_HEADER;
            boolean nextIsHeader = position < getItemCount() - 1 && getItemViewType(position + 1) == VIEW_TYPE_HEADER;

            if (position == 0 || getItemViewType(position - 1) == VIEW_TYPE_SHADOW || getItemViewType(position - 1) == VIEW_TYPE_TEXT_INFO_PRIVACY) {
                top = AndroidUtilities.dp(2);
            }

            if (position == 0 /*|| viewType == VIEW_TYPE_HEADER*/) {
                top = AndroidUtilities.dp(16);
            }

            if (prevIsHeader) {
                top = 0;
            }

            if (position == getItemCount() - 1
                    || nextIsHeader
                    || getItemViewType(position + 1) == VIEW_TYPE_SHADOW
                || getItemViewType(position + 1) == VIEW_TYPE_TEXT_INFO_PRIVACY
            ) {
                bottom = AndroidUtilities.dp(2);
            }

            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            lp.leftMargin = side;
            lp.rightMargin = side;
            lp.topMargin = top;
            lp.bottomMargin = bottom;
            holder.itemView.setLayoutParams(lp);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == audioVideoDivisorRow || position == rearCamRowDivisorRow) {
                return VIEW_TYPE_SHADOW;
            } else if (position == cameraTypeHeaderRow || position == audioVideoHeaderRow || position == videoMessagesHeaderRow) {
                return VIEW_TYPE_HEADER;
            } else if (position == disableAttachCameraRow || position == cameraUseDualCameraRow || position == startFromUltraWideRow || position == cameraStabilisationRow || position == cameraControlButtonsRow || position == rearCamRow) {
                return VIEW_TYPE_TEXT_CHECK;
            } else if (position == cameraAspectRatioRow || position == cameraXQualityRow || position == cameraXFpsRangeRow || position == exposureSliderRow) {
                return VIEW_TYPE_TEXT_SETTINGS;
            } else if (position == cameraAdviseRow) {
                return VIEW_TYPE_TEXT_INFO_PRIVACY;
            } else if (position == cameraTypeSelectorRow) {
                return VIEW_TYPE_CAMERA_SELECTOR;
            }
            return VIEW_TYPE_SHADOW;
        }

        private void applyMD3Background(RecyclerView.ViewHolder holder, int position) {
            if (!CherrygramDebugConfig.INSTANCE.getMdContainers()) return;

            int viewType = holder.getItemViewType();

            if (viewType == VIEW_TYPE_SHADOW || viewType == VIEW_TYPE_TEXT_INFO_PRIVACY) {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
                return;
            }

            int prevType = position > 0 ? getItemViewType(position - 1) : -1;
            int nextType = position < getItemCount() - 1 ? getItemViewType(position + 1) : -1;

            boolean isHeader = viewType == VIEW_TYPE_HEADER;

            boolean isGroupStart = position == 0
                    || prevType == VIEW_TYPE_SHADOW
                    || prevType == VIEW_TYPE_TEXT_INFO_PRIVACY;

            boolean isGroupEnd = position == getItemCount() - 1
                    || nextType == VIEW_TYPE_SHADOW
                    || nextType == VIEW_TYPE_TEXT_INFO_PRIVACY;

            int r = AndroidUtilities.dp(14);

            int topLeft = 0, topRight = 0, bottomLeft = 0, bottomRight = 0;

            if (isHeader) {
                topLeft = topRight = r;
            } else if (isGroupStart && isGroupEnd) {
                topLeft = topRight = bottomLeft = bottomRight = r;
            } else if (isGroupStart) {
                topLeft = topRight = r;
            } else if (isGroupEnd) {
                bottomLeft = bottomRight = r;
            }

            Drawable bg = Theme.createRoundRectDrawable(
                    topLeft, topRight, bottomRight, bottomLeft,
                    Theme.getColor(Theme.key_windowBackgroundWhite)
            );
            holder.itemView.setBackground(bg);

            final int side = 0;
            holder.itemView.setPadding(side, holder.itemView.getPaddingTop(), side, holder.itemView.getPaddingBottom());
        }

    }

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
            /*if (CherrygramCoreConfig.INSTANCE.isDevBuild())*/ cameraXFpsRangeRow = rowCount++;
            cameraStabilisationRow = rowCount++;
            exposureSliderRow = rowCount++;
        } else {
            startFromUltraWideRow = -1;
            /*if (CherrygramCoreConfig.INSTANCE.isDevBuild())*/ cameraXFpsRangeRow = -1;
            cameraStabilisationRow = -1;
            exposureSliderRow = -1;
        }

        cameraControlButtonsRow = rowCount++;

        if (CherrygramCameraConfig.INSTANCE.getCameraType() == CherrygramCameraConfig.CAMERA_2 && CherrygramCameraConfig.INSTANCE.getUseDualCamera()) {
            rearCamRow = -1;
        } else {
            rearCamRow = rowCount++;
        }
        rearCamRowDivisorRow = rowCount++;

        if (listAdapter != null && notify) {
            listAdapter.notifyDataSetChanged();
        }
    }

}
