package uz.unnarsx.cherrygram.preferences;

import android.annotation.SuppressLint;
import android.content.Context;
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
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.UndoView;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.CherrygramConfig;
import uz.unnarsx.cherrygram.helpers.PopupHelper;

public class ExperimentalPrefenrecesEntry extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private int rowCount;
    private ListAdapter listAdapter;
    private RecyclerListView listView;

    private int experimentalHeaderRow;
    private int downloadSpeedBoostRow;
    private int uploadSpeedBoostRow;
    private int slowNetworkMode;

    protected Theme.ResourcesProvider resourcesProvider;
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
            if (position == uploadSpeedBoostRow) {
                CherrygramConfig.INSTANCE.toggleUploadSpeedBoost();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramConfig.INSTANCE.getUploadSpeedBoost());
                }
                restartTooltip.showWithAction(0, UndoView.ACTION_NEED_RESTART, null, null);
            } if (position == slowNetworkMode) {
                CherrygramConfig.INSTANCE.toggleSlowNetworkMode();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramConfig.INSTANCE.getSlowNetworkMode());
                }
                restartTooltip.showWithAction(0, UndoView.ACTION_NEED_RESTART, null, null);
            } else if (position == downloadSpeedBoostRow) {
                ArrayList<String> arrayList = new ArrayList<>();
                ArrayList<Integer> types = new ArrayList<>();
                arrayList.add(LocaleController.getString("EP_DownloadSpeedBoostNone", R.string.EP_DownloadSpeedBoostNone));
                types.add(CherrygramConfig.BOOST_NONE);
                arrayList.add(LocaleController.getString("EP_DownloadSpeedBoostAverage", R.string.EP_DownloadSpeedBoostAverage));
                types.add(CherrygramConfig.BOOST_AVERAGE);
                arrayList.add(LocaleController.getString("EP_DownloadSpeedBoostExtreme", R.string.EP_DownloadSpeedBoostExtreme));
                types.add(CherrygramConfig.BOOST_EXTREME);
                PopupHelper.show(arrayList, LocaleController.getString("EP_DownloadSpeedBoost", R.string.EP_DownloadSpeedBoost), types.indexOf(CherrygramConfig.INSTANCE.getDownloadSpeedBoost()), context, i -> {
                    CherrygramConfig.INSTANCE.setDownloadSpeedBoost(types.get(i));
                    listAdapter.notifyItemChanged(downloadSpeedBoostRow);
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

        experimentalHeaderRow = rowCount++;
        downloadSpeedBoostRow = rowCount++;
        uploadSpeedBoostRow = rowCount++;
        slowNetworkMode = rowCount++;

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
                    if (position == experimentalHeaderRow) {
                        headerCell.setText(LocaleController.getString("EP_Category_Experimental", R.string.EP_Category_Experimental));
                    }
                    break;
                case 3: {
                    TextSettingsCell textCell = (TextSettingsCell) holder.itemView;
                    textCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    if (position == downloadSpeedBoostRow) {
                        String value;
                        switch (CherrygramConfig.INSTANCE.getDownloadSpeedBoost()) {
                            case CherrygramConfig.BOOST_NONE:
                                value = LocaleController.getString("EP_DownloadSpeedBoostNone", R.string.EP_DownloadSpeedBoostNone);
                                break;
                            case CherrygramConfig.BOOST_EXTREME:
                                value = LocaleController.getString("EP_DownloadSpeedBoostExtreme", R.string.EP_DownloadSpeedBoostExtreme);
                                break;
                            default:
                            case CherrygramConfig.BOOST_AVERAGE:
                                value = LocaleController.getString("EP_DownloadSpeedBoostAverage", R.string.EP_DownloadSpeedBoostAverage);
                                break;
                        }
                        textCell.setTextAndValue(LocaleController.getString("EP_DownloadSpeedBoost", R.string.EP_DownloadSpeedBoost), value, true);
                    }
                    break;
                }
                case 4: {
                    TextCheckCell textCell = (TextCheckCell) holder.itemView;
                    textCell.setEnabled(true, null);
                    if (position == uploadSpeedBoostRow) {
                        textCell.setTextAndCheck(LocaleController.getString("EP_UploadloadSpeedBoost", R.string.EP_UploadloadSpeedBoost), CherrygramConfig.INSTANCE.getUploadSpeedBoost(), true);
                    } else if (position == slowNetworkMode) {
                        textCell.setTextAndCheck(LocaleController.getString("EP_SlowNetworkMode", R.string.EP_SlowNetworkMode), CherrygramConfig.INSTANCE.getSlowNetworkMode(), true);
                    }
                    break;
                }
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
                    view = new TextSettingsCell(mContext, resourcesProvider);
                    view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
                    break;
                case 4:
                    view = new TextCheckCell(mContext, resourcesProvider);
                    view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
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
            if (position == experimentalHeaderRow) {
                return 2;
            } else if (position == downloadSpeedBoostRow) {
                return 3;
            } else if (position > downloadSpeedBoostRow) {
                return 4;
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
