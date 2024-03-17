package uz.unnarsx.cherrygram.preferences;

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
import org.telegram.messenger.LocaleController;
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

import uz.unnarsx.cherrygram.CGFeatureHooks;
import uz.unnarsx.cherrygram.CherrygramConfig;
import uz.unnarsx.cherrygram.camera.CameraTypeSelector;
import uz.unnarsx.cherrygram.camera.CameraXUtils;
import uz.unnarsx.cherrygram.helpers.AppRestartHelper;
import uz.unnarsx.cherrygram.helpers.ui.PopupHelper;

public class CameraPreferencesEntry extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private int rowCount;
    private ListAdapter listAdapter;
    private RecyclerListView listView;
    private int cameraUseDualCameraRow;
    private int cameraTypeHeaderRow;
    private int cameraTypeSelectorRow;
    private int cameraXOptimizeRow;
    private int cameraXQualityRow;
    private int cameraAdviseRow;

    private int audioVideoHeaderRow;
    private int disableAttachCameraRow;
    private int rearCamRow;
    private int cameraAspectRatioRow;
    private int cameraAspectRatioAdviseRow;

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

        actionBar.setTitle(LocaleController.getString("CP_Category_Camera", R.string.CP_Category_Camera));
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
            if (position == cameraUseDualCameraRow) {
                CherrygramConfig.INSTANCE.toggleUseDualCamera();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramConfig.INSTANCE.getUseDualCamera());
                }

                listAdapter.notifyItemChanged(rearCamRow);
                updateRowsId(false);
                parentLayout.rebuildAllFragmentViews(false, false);
            } else if (position == cameraXOptimizeRow) {
                CherrygramConfig.INSTANCE.toggleCameraXOptimizedMode();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramConfig.INSTANCE.getUseCameraXOptimizedMode());
                }
                AppRestartHelper.createRestartBulletin(this);
            } else if (position == cameraXQualityRow) {
                Map<Quality, Size> availableSizes = CameraXUtils.getAvailableVideoSizes();
                Stream<Integer> tmp = availableSizes.values().stream().sorted(Comparator.comparingInt(Size::getWidth).reversed()).map(Size::getHeight);
                ArrayList<Integer> types = tmp.collect(Collectors.toCollection(ArrayList::new));
                ArrayList<String> arrayList = types.stream().map(p -> p + "p").collect(Collectors.toCollection(ArrayList::new));
                PopupHelper.show(arrayList, LocaleController.getString("CP_CameraQuality", R.string.CP_CameraQuality), types.indexOf(CherrygramConfig.INSTANCE.getCameraResolution()), context, i -> {
                    CherrygramConfig.INSTANCE.setCameraResolution(types.get(i));
                    listAdapter.notifyItemChanged(cameraXQualityRow);
                    AppRestartHelper.createRestartBulletin(this);
                });
            } else if (position == disableAttachCameraRow) {
                CherrygramConfig.INSTANCE.toggleDisableAttachCamera();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramConfig.INSTANCE.getDisableAttachCamera());
                }
                AppRestartHelper.createRestartBulletin(this);
            } else if (position == rearCamRow) {
                CherrygramConfig.INSTANCE.toggleRearCam();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramConfig.INSTANCE.getRearCam());
                }
            } else if (position == cameraAspectRatioRow) {
                ArrayList<String> arrayList = new ArrayList<>();
                ArrayList<Integer> types = new ArrayList<>();
                arrayList.add("1:1");
                types.add(CherrygramConfig.Camera1to1);
                arrayList.add("4:3");
                types.add(CherrygramConfig.Camera4to3);
                arrayList.add("16:9");
                types.add(CherrygramConfig.Camera16to9);
                arrayList.add(LocaleController.getString("Default", R.string.Default));
                types.add(CherrygramConfig.CameraAspectDefault);
                PopupHelper.show(arrayList, (LocaleController.getString("CP_CameraAspectRatio", R.string.CP_CameraAspectRatio)), types.indexOf(CherrygramConfig.INSTANCE.getCameraAspectRatio()), context, i -> {
                    CherrygramConfig.INSTANCE.setCameraAspectRatio(types.get(i));
                    listAdapter.notifyItemChanged(cameraAspectRatioRow);
                });
            }
        });

        return fragmentView;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateRowsId(boolean notify) {
        rowCount = 0;

        cameraTypeHeaderRow = -1;
        cameraTypeSelectorRow = -1;
        cameraXOptimizeRow = -1;
        cameraXQualityRow = -1;
        cameraAdviseRow = -1;

        if (CameraXUtils.isCameraXSupported()) {
            cameraTypeHeaderRow = rowCount++;
            cameraTypeSelectorRow = rowCount++;
            if (CherrygramConfig.INSTANCE.getCameraType() == CherrygramConfig.CAMERA_X) {
                cameraXOptimizeRow = rowCount++;
                cameraXQualityRow = rowCount++;
            }
            cameraAdviseRow = rowCount++;
        }

        audioVideoHeaderRow = rowCount++;
        disableAttachCameraRow = rowCount++;

        if (CherrygramConfig.INSTANCE.getCameraType() != CherrygramConfig.CAMERA_2) {
            cameraUseDualCameraRow = -1;
            rearCamRow = rowCount++;
            cameraAspectRatioRow = rowCount++;
            cameraAspectRatioAdviseRow = rowCount++;
        } else if (CherrygramConfig.INSTANCE.getCameraType() == CherrygramConfig.CAMERA_2) {
            cameraUseDualCameraRow = rowCount++;
            if (!CherrygramConfig.INSTANCE.getUseDualCamera()) {
                rearCamRow = rowCount++;
            } else {
                rearCamRow = -1;
            }

            cameraAspectRatioRow = -1;
            cameraAspectRatioAdviseRow = -1;
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
                        headerCell.setText(LocaleController.getString("CP_CameraType", R.string.CP_CameraType));
                    } else if (position == audioVideoHeaderRow) {
                        headerCell.setText(LocaleController.getString("CP_Category_Camera", R.string.CP_Category_Camera));
                    }
                    break;
                case 3:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    textCheckCell.setEnabled(true, null);
                    if (position == cameraUseDualCameraRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("CP_CameraDualCamera", R.string.CP_CameraDualCamera), LocaleController.getString("CP_CameraDualCamera_Desc", R.string.CP_CameraDualCamera_Desc), CherrygramConfig.INSTANCE.getUseDualCamera(), true, true);
                    } else if (position == cameraXOptimizeRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("CP_PerformanceMode", R.string.CP_PerformanceMode), LocaleController.getString("CP_PerformanceModeDesc", R.string.CP_PerformanceModeDesc), CherrygramConfig.INSTANCE.getUseCameraXOptimizedMode(), true, true);
                    } else if (position == disableAttachCameraRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("CP_DisableCam", R.string.CP_DisableCam), LocaleController.getString("CP_DisableCam_Desc", R.string.CP_DisableCam_Desc), CherrygramConfig.INSTANCE.getDisableAttachCamera(), true, true);
                    } else if (position == rearCamRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("CP_RearCam", R.string.CP_RearCam), LocaleController.getString("CP_RearCam_Desc", R.string.CP_RearCam_Desc), CherrygramConfig.INSTANCE.getRearCam(), true, true);
                    }
                    break;
                case 6:
                    TextInfoPrivacyCell textInfoPrivacyCell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == cameraAdviseRow) {
                        textInfoPrivacyCell.setText(CGFeatureHooks.getCameraAdvise());
                    } else if (position == cameraAspectRatioAdviseRow) {
                        TextInfoPrivacyCell textCell = (TextInfoPrivacyCell) holder.itemView;
                        textCell.setText(LocaleController.getString("CP_CameraAspectRatio_Desc", R.string.CP_CameraAspectRatio_Desc));
                    }
                    break;
                case 7:
                    TextSettingsCell textSettingsCell = (TextSettingsCell) holder.itemView;
                    textSettingsCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    TextSettingsCell textCell = (TextSettingsCell) holder.itemView;
                    textCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    if (position == cameraXQualityRow) {
                        textSettingsCell.setTextAndValue(LocaleController.getString("CP_CameraQuality", R.string.CP_CameraQuality), CherrygramConfig.INSTANCE.getCameraResolution() + "p", false);
                    } else if (position == cameraAspectRatioRow) {
                        textCell.setTextAndValue(LocaleController.getString("CP_CameraAspectRatio", R.string.CP_CameraAspectRatio), CGFeatureHooks.getCameraAspectRatio(), true);
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
                            int oldValue = CherrygramConfig.INSTANCE.getCameraType();
                            CherrygramConfig.INSTANCE.setCameraType(cameraSelected);
                            if (cameraSelected == CherrygramConfig.CAMERA_X) {
                                updateRowsId(false);
                                listAdapter.notifyItemInserted(cameraXOptimizeRow);
                                listAdapter.notifyItemInserted(cameraXQualityRow);
                                listAdapter.notifyItemChanged(cameraAdviseRow);
                                listAdapter.notifyItemChanged(cameraUseDualCameraRow);
                                listAdapter.notifyItemChanged(rearCamRow);
                            } else if (oldValue == CherrygramConfig.CAMERA_X){
                                listAdapter.notifyItemRemoved(cameraXOptimizeRow);
                                listAdapter.notifyItemRemoved(cameraXQualityRow);
                                listAdapter.notifyItemChanged(cameraAdviseRow - 1);
                                updateRowsId(false);
                                listAdapter.notifyItemChanged(cameraUseDualCameraRow);
                                listAdapter.notifyItemChanged(rearCamRow);
                            } else {
                                updateRowsId(false);
                                listAdapter.notifyItemChanged(cameraUseDualCameraRow);
                                listAdapter.notifyItemChanged(rearCamRow);
                                listAdapter.notifyItemChanged(cameraAdviseRow);
                            }
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
            if (position == audioVideoHeaderRow || position == cameraTypeHeaderRow) {
                return 2;
            } else if (position == cameraUseDualCameraRow || position == cameraXOptimizeRow || position == disableAttachCameraRow || position == rearCamRow) {
                return 3;
            } else if (position == cameraTypeSelectorRow) {
                return 5;
            } else if (position == cameraAdviseRow || position == cameraAspectRatioAdviseRow) {
                return 6;
            } else if (position == cameraXQualityRow || position == cameraAspectRatioRow) {
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
