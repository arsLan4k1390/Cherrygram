package uz.unnarsx.cherrygram.preferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
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
import org.telegram.ui.Cells.TextCheckbox2Cell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.UndoView;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.CherrygramConfig;
import uz.unnarsx.cherrygram.camera.CameraTypeSelector;
import uz.unnarsx.cherrygram.camera.CameraXUtilities;
import uz.unnarsx.cherrygram.helpers.EntitiesHelper;
import uz.unnarsx.cherrygram.helpers.PopupHelper;

public class ExperimentalPrefenrecesEntry extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private int rowCount;
    private ListAdapter listAdapter;
    private RecyclerListView listView;
    private int cameraTypeHeaderRow;
    private int cameraTypeSelectorRow;
    private int cameraXOptimizeRow;
    private int cameraXFpsRow;
    private int cameraAdviseRow;

    private UndoView restartTooltip;

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

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        if (CherrygramConfig.INSTANCE.getBackButton())
            actionBar.setBackButtonImage(R.drawable.arrow_back);
        actionBar.setTitle(LocaleController.getString("EP_Category_Experimental", R.string.EP_Category_Experimental));
        actionBar.setAllowOverlayTitle(false);

        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
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
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setAdapter(listAdapter);
        if (listView.getItemAnimator() != null) {
            ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
        }
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setOnItemClickListener((view, position, x, y) -> {
            if (position == cameraXOptimizeRow) {
                CherrygramConfig.INSTANCE.toggleCameraXOptimizedMode();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramConfig.INSTANCE.getUseCameraXOptimizedMode());
                }
                restartTooltip.showWithAction(0, UndoView.ACTION_NEED_RESTART, null, null);
            } else if (position == cameraXFpsRow) {
                ArrayList<String> arrayList = new ArrayList<>();
                ArrayList<Integer> types = new ArrayList<>();
                arrayList.add("60 Fps");
                types.add(60);
                arrayList.add("30 Fps");
                types.add(30);
                PopupHelper.show(arrayList, LocaleController.getString("EP_MotionSmoothness", R.string.EP_MotionSmoothness), types.indexOf(CherrygramConfig.INSTANCE.getCameraXFps()), context, i -> {
                    CherrygramConfig.INSTANCE.saveCameraXFps(types.get(i));
                    listAdapter.notifyItemChanged(cameraXFpsRow);
                    restartTooltip.showWithAction(0, UndoView.ACTION_NEED_RESTART, null, null);
                });
            }
        });

        restartTooltip = new UndoView(context);
        frameLayout.addView(restartTooltip, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.LEFT, 8, 0, 8, 8));

        return fragmentView;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateRowsId(boolean notify) {
        rowCount = 0;
        cameraTypeHeaderRow = -1;
        cameraTypeSelectorRow = -1;
        cameraXOptimizeRow = -1;
        cameraXFpsRow = -1;
        cameraAdviseRow = -1;

        if (CameraXUtilities.isCameraXSupported()) {
            cameraTypeHeaderRow = rowCount++;
            cameraTypeSelectorRow = rowCount++;
            if (CherrygramConfig.INSTANCE.getCameraType() == 1) {
                cameraXOptimizeRow = rowCount++;
                cameraXFpsRow = rowCount++;
            }
            cameraAdviseRow = rowCount++;
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
                        headerCell.setText(LocaleController.getString("EP_CameraType", R.string.EP_CameraType));
                    }
                    break;
                case 3:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    textCheckCell.setEnabled(true, null);
                    if (position == cameraXOptimizeRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("EP_PerformanceMode", R.string.EP_PerformanceMode), LocaleController.getString("EP_PerformanceModeDesc", R.string.EP_PerformanceModeDesc), CherrygramConfig.INSTANCE.getUseCameraXOptimizedMode(), true, true);
                    }
                    break;
                case 6:
                    TextInfoPrivacyCell textInfoPrivacyCell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == cameraAdviseRow) {
                        String advise;
                        switch (CherrygramConfig.INSTANCE.getCameraType()) {
                            case 0:
                                advise = LocaleController.getString("EP_DefaultCameraDesc", R.string.EP_DefaultCameraDesc);
                                break;
                            case 1:
                                advise = LocaleController.getString("EP_CameraXDesc", R.string.EP_CameraXDesc);
                                break;
                            case 2:
                            default:
                                advise = LocaleController.getString("EP_SystemCameraDesc", R.string.EP_SystemCameraDesc);
                                break;
                        }
                        Spannable htmlParsed;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            htmlParsed = new SpannableString(Html.fromHtml(advise, Html.FROM_HTML_MODE_LEGACY));
                        }else{
                            htmlParsed = new SpannableString(Html.fromHtml(advise));
                        }
                        textInfoPrivacyCell.setText(EntitiesHelper.getUrlNoUnderlineText(htmlParsed));
                    }
                    break;
                case 7:
                    TextSettingsCell textSettingsCell = (TextSettingsCell) holder.itemView;
                    textSettingsCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    if (position == cameraXFpsRow) {
                        textSettingsCell.setTextAndValue(LocaleController.getString("EP_MotionSmoothness", R.string.EP_MotionSmoothness), CherrygramConfig.INSTANCE.getCameraXFps() + " Fps", false);
                    }
                    break;
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == 3 || type == 7 || type == 8;
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
                            if (cameraSelected == 1) {
                                updateRowsId(false);
                                listAdapter.notifyItemInserted(cameraXOptimizeRow);
                                listAdapter.notifyItemInserted(cameraXFpsRow);
                                listAdapter.notifyItemChanged(cameraAdviseRow);
                            } else {
                                listAdapter.notifyItemRemoved(cameraXOptimizeRow);
                                listAdapter.notifyItemRemoved(cameraXFpsRow);
                                listAdapter.notifyItemChanged(cameraAdviseRow - 1);
                                updateRowsId(false);
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
                case 8:
                    view = new TextCheckbox2Cell(mContext);
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
            if (position == cameraTypeHeaderRow) {
                return 2;
            } else if (position == cameraXOptimizeRow) {
                return 3;
            } else if (position == cameraTypeSelectorRow) {
                return 5;
            } else if (position == cameraAdviseRow) {
                return 6;
            } else if (position == cameraXFpsRow) {
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
