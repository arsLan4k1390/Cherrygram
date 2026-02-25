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
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BotWebViewVibrationEffect;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import uz.unnarsx.cherrygram.chats.CGMessageMenuInjector;
import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig;
import uz.unnarsx.cherrygram.core.crashlytics.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.core.ui.CGBulletinCreator;
import uz.unnarsx.cherrygram.donates.DonatesManager;

public class MessageMenuPreferencesEntry extends BaseFragment {

    private int rowCount;
    private ListAdapter listAdapter;
    private RecyclerListView listView;

    private int redesignHeaderRow;
    private int enableNewMessageMenuRow;
    private int unifiedScrollRow;
    private int autoScrollMessagesRow;
    private int fixedMessageHeightRow;
    private int blurMessageMenuItemsRow;
    private int useNativeBlurRow;
    private int redesignEndDivisorRow;

    private int miscellaneousHeaderRow;
    private int messageMenuItemsRow;
    private int messageMenuItemsCompactView;
    private int miscellaneousEndDivisor;

    protected Theme.ResourcesProvider resourcesProvider;

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

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonDrawable(new BackDrawable(false));

        actionBar.setTitle(getString(R.string.CP_MessageMenu));
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
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setAdapter(listAdapter);
        if (listView.getItemAnimator() != null) {
            ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
        }
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView.setOnItemClickListener((view, position, x, y) -> {
            boolean requireDonate;

            if (position == messageMenuItemsCompactView) {
                requireDonate = !DonatesManager.INSTANCE.checkAllDonatedAccounts() && !DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace();
            } else {
                requireDonate = !DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace();
            }

            var holder = listView.findViewHolderForAdapterPosition(position);
            if (holder == null || !listAdapter.isEnabled(holder)) {
                return;
            }
            if (requireDonate && position != messageMenuItemsRow) {
                AndroidUtilities.shakeViewSpring(view);
                BotWebViewVibrationEffect.APP_ERROR.vibrate();
                CGBulletinCreator.INSTANCE.createRequireDonateBulletin(this);
                return;
            }
            if (position == enableNewMessageMenuRow) {
                CherrygramChatsConfig.INSTANCE.setBlurMessageMenuBackground(!CherrygramChatsConfig.INSTANCE.getBlurMessageMenuBackground());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramChatsConfig.INSTANCE.getBlurMessageMenuBackground());
                }

                listAdapter.notifyItemChanged(unifiedScrollRow, false);
                listAdapter.notifyItemChanged(autoScrollMessagesRow, false);
                listAdapter.notifyItemChanged(fixedMessageHeightRow, false);
                listAdapter.notifyItemChanged(blurMessageMenuItemsRow, false);
                listAdapter.notifyItemChanged(useNativeBlurRow, false);
            } else if (position == unifiedScrollRow) {
                CherrygramChatsConfig.INSTANCE.setMsgMenuUnifiedScroll(!CherrygramChatsConfig.INSTANCE.getMsgMenuUnifiedScroll());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramChatsConfig.INSTANCE.getMsgMenuUnifiedScroll());

                    /*if (CherrygramChatsConfig.INSTANCE.getMsgMenuUnifiedScroll() && !CherrygramChatsConfig.INSTANCE.getBlurMessageMenuBackground()) {
                        CherrygramChatsConfig.INSTANCE.setBlurMessageMenuBackground(true);
                        listAdapter.notifyItemChanged(enableNewMessageMenuRow, false);
                    }

                    CherrygramChatsConfig.INSTANCE.setMsgMenuFixedHeight(!CherrygramChatsConfig.INSTANCE.getMsgMenuUnifiedScroll() || !CherrygramChatsConfig.INSTANCE.getMsgMenuFixedHeight());*/

                    listAdapter.notifyItemChanged(autoScrollMessagesRow, false);
                    listAdapter.notifyItemChanged(fixedMessageHeightRow, false);
                }
            } else if (position == autoScrollMessagesRow) {
                CherrygramChatsConfig.INSTANCE.setMsgMenuAutoScroll(!CherrygramChatsConfig.INSTANCE.getMsgMenuAutoScroll());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramChatsConfig.INSTANCE.getMsgMenuAutoScroll());

                    if (CherrygramChatsConfig.INSTANCE.getMsgMenuAutoScroll() && !CherrygramChatsConfig.INSTANCE.getBlurMessageMenuBackground()) {
                        CherrygramChatsConfig.INSTANCE.setBlurMessageMenuBackground(true);
                        listAdapter.notifyItemChanged(enableNewMessageMenuRow, false);
                    }
                }
            } else if (position == fixedMessageHeightRow) {
                CherrygramChatsConfig.INSTANCE.setMsgMenuFixedHeight(!CherrygramChatsConfig.INSTANCE.getMsgMenuFixedHeight());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramChatsConfig.INSTANCE.getMsgMenuFixedHeight());

                    if (CherrygramChatsConfig.INSTANCE.getMsgMenuFixedHeight() && !CherrygramChatsConfig.INSTANCE.getBlurMessageMenuBackground()) {
                        CherrygramChatsConfig.INSTANCE.setBlurMessageMenuBackground(true);
                        listAdapter.notifyItemChanged(enableNewMessageMenuRow, false);
                    }
                }
            } else if (position == blurMessageMenuItemsRow) {
                CherrygramChatsConfig.INSTANCE.setBlurMessageMenuItems(!CherrygramChatsConfig.INSTANCE.getBlurMessageMenuItems());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramChatsConfig.INSTANCE.getBlurMessageMenuItems());

                    if (CherrygramChatsConfig.INSTANCE.getBlurMessageMenuItems() && !CherrygramChatsConfig.INSTANCE.getBlurMessageMenuBackground()) {
                        CherrygramChatsConfig.INSTANCE.setBlurMessageMenuBackground(true);
                        listAdapter.notifyItemChanged(enableNewMessageMenuRow, false);
                    }
                }
            } else if (position == useNativeBlurRow) {
                CherrygramChatsConfig.INSTANCE.setMsgMenuNativeBlur(!CherrygramChatsConfig.INSTANCE.getMsgMenuNativeBlur());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramChatsConfig.INSTANCE.getMsgMenuNativeBlur());

                    if (CherrygramChatsConfig.INSTANCE.getMsgMenuNativeBlur() && !CherrygramChatsConfig.INSTANCE.getBlurMessageMenuBackground()) {
                        CherrygramChatsConfig.INSTANCE.setBlurMessageMenuBackground(true);
                        listAdapter.notifyItemChanged(enableNewMessageMenuRow, false);
                    }
                }
            } else if (position == messageMenuItemsRow) {
                CGMessageMenuInjector.INSTANCE.showMessageMenuItemsConfigurator(this);
            } else if (position == messageMenuItemsCompactView) {
                CherrygramChatsConfig.INSTANCE.setMsgMenuItemsCompactView(!CherrygramChatsConfig.INSTANCE.getMsgMenuItemsCompactView());
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramChatsConfig.INSTANCE.getMsgMenuItemsCompactView());
                }
            }
        });

        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("message_menu_preferences_screen");

        listView.setSections(true);
        actionBar.setAdaptiveBackground(listView);

        return fragmentView;
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private final Context mContext;

        private final int VIEW_TYPE_SHADOW = 0;
        private final int VIEW_TYPE_HEADER = 1;
        private final int VIEW_TYPE_TEXT_CELL = 2;
        private final int VIEW_TYPE_TEXT_CHECK = 3;
//        private final int VIEW_TYPE_TEXT_SETTINGS = 4;
//        private final int VIEW_TYPE_TEXT_INFO_PRIVACY = 5;
//        private final int VIEW_TYPE_TEXT_DETAIL_SETTINGS = 6;
//        private final int VIEW_TYPE_SLIDER = 7;

        ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            boolean requireDonate = !DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace();
            switch (holder.getItemViewType()) {
                case VIEW_TYPE_SHADOW:
                    ShadowSectionCell shadowSectionCell = (ShadowSectionCell) holder.itemView;
                    shadowSectionCell.setEnabled(false);
                    break;
                case VIEW_TYPE_HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    headerCell.setEnabled(false);

                    if (position == redesignHeaderRow) {
                        headerCell.setText(getString(R.string.AP_Header_Appearance));
                    } else if (position == miscellaneousHeaderRow) {
                        headerCell.setText(getString(R.string.LocalMiscellaneousCache));
                    }
                    break;
                case VIEW_TYPE_TEXT_CELL:
                    TextCell textCell = (TextCell) holder.itemView;

                    if (position == messageMenuItemsRow) {
                        textCell.setEnabled(true);
                        textCell.setTextAndIcon(getString(R.string.CP_MessageMenuItems), R.drawable.msg_list, true);
                    }
                    break;
                case VIEW_TYPE_TEXT_CHECK:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    textCheckCell.setEnabled(!requireDonate, null);

                    if (position == messageMenuItemsCompactView) {
                        requireDonate = !DonatesManager.INSTANCE.checkAllDonatedAccounts() && !DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace();
                    } else {
                        requireDonate = !DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace();
                    }
                    if (requireDonate) textCheckCell.setCheckBoxIcon(R.drawable.permission_locked);

                    if (position == enableNewMessageMenuRow) {
                        textCheckCell.setEnabled(true, null);
                        textCheckCell.setTextAndValueAndCheck(
                                getString(R.string.CP_BlurMessageMenu),
                                getString(R.string.CP_BlurMessageMenu_Desc),
                                CherrygramChatsConfig.INSTANCE.getBlurMessageMenuBackground(),
                                true,
                                true
                        );
                    } else if (position == unifiedScrollRow) {
                        textCheckCell.setEnabled(CherrygramChatsConfig.INSTANCE.getBlurMessageMenuBackground(), null);
                        textCheckCell.setTextAndValueAndCheck(
                                getString(R.string.CP_MessageMenuUnifiedScroll),
                                getString(R.string.CP_MessageMenuUnifiedScroll_Desc),
                                CherrygramChatsConfig.INSTANCE.getMsgMenuUnifiedScroll(),
                                true,
                                true
                        );
                    } else if (position == autoScrollMessagesRow) {
                        textCheckCell.setEnabled(CherrygramChatsConfig.INSTANCE.getBlurMessageMenuBackground() && !CherrygramChatsConfig.INSTANCE.getMsgMenuUnifiedScroll(), null);
                        textCheckCell.setTextAndValueAndCheck(
                                getString(R.string.CP_MessageMenuAutoscroll),
                                getString(R.string.CP_MessageMenuAutoscroll_Desc),
                                CherrygramChatsConfig.INSTANCE.getMsgMenuAutoScroll(),
                                true,
                                true
                        );
                    } else if (position == fixedMessageHeightRow) {
                        textCheckCell.setEnabled(CherrygramChatsConfig.INSTANCE.getBlurMessageMenuBackground() && !CherrygramChatsConfig.INSTANCE.getMsgMenuUnifiedScroll(), null);
                        textCheckCell.setTextAndValueAndCheck(
                                getString(R.string.CP_MessageMenuFixedHeight),
                                getString(R.string.CP_MessageMenuFixedHeight_Desc),
                                CherrygramChatsConfig.INSTANCE.getMsgMenuFixedHeight(),
                                true,
                                true
                        );
                    } else if (position == blurMessageMenuItemsRow) {
                        textCheckCell.setEnabled(CherrygramChatsConfig.INSTANCE.getBlurMessageMenuBackground(), null);
                        textCheckCell.setTextAndValueAndCheck(
                                getString(R.string.CP_BlurMessageMenuItems),
                                getString(R.string.CP_BlurMessageMenuItems_Desc),
                                CherrygramChatsConfig.INSTANCE.getBlurMessageMenuItems(),
                                true,
                                true
                        );
                    } else if (position == useNativeBlurRow) {
                        textCheckCell.setEnabled(CherrygramChatsConfig.INSTANCE.getBlurMessageMenuBackground(), null);
                        textCheckCell.setTextAndValueAndCheck(
                                getString(R.string.CP_MessageMenuNativeBlur),
                                getString(R.string.CP_MessageMenuNativeBlur_Desc),
                                CherrygramChatsConfig.INSTANCE.getMsgMenuNativeBlur(),
                                true,
                                false
                        );
                    } else if (position == messageMenuItemsCompactView) {
                        textCheckCell.setEnabled(true, null);
                        textCheckCell.setTextAndValueAndCheck(
                                getString(R.string.CP_MessageMenuCompactLayout),
                                getString(R.string.CP_MessageMenuCompactLayout_Desc) + "\n\n" + getString(R.string.CP_MessageMenuCompactLayout_Dot),
                                CherrygramChatsConfig.INSTANCE.getMsgMenuItemsCompactView(),
                                true,
                                false
                        );
                    }
                    break;
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return holder.itemView.isEnabled();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case VIEW_TYPE_SHADOW:
                    view = new ShadowSectionCell(mContext);
                    view.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case VIEW_TYPE_HEADER:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_TEXT_CELL:
                    view = new TextCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_TEXT_CHECK:
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + viewType);
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == redesignEndDivisorRow || position == miscellaneousEndDivisor) {
                return VIEW_TYPE_SHADOW;
            } else if (position == redesignHeaderRow || position == miscellaneousHeaderRow) {
                return VIEW_TYPE_HEADER;
            } else if (position == enableNewMessageMenuRow || position == unifiedScrollRow || position == autoScrollMessagesRow || position == fixedMessageHeightRow || position == blurMessageMenuItemsRow || position == useNativeBlurRow || position == messageMenuItemsCompactView) {
                return VIEW_TYPE_TEXT_CHECK;
            } else if (position == messageMenuItemsRow) {
                return VIEW_TYPE_TEXT_CELL;
            }
            return VIEW_TYPE_SHADOW;
        }
    }

    private void updateRowsId(boolean notify) {
        rowCount = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            redesignHeaderRow = rowCount++;
            enableNewMessageMenuRow = rowCount++;
            unifiedScrollRow = rowCount++;
            autoScrollMessagesRow = rowCount++;
            fixedMessageHeightRow = rowCount++;
            blurMessageMenuItemsRow = rowCount++;
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
                useNativeBlurRow = -1;
            } else {
                useNativeBlurRow = rowCount++;
            }
            redesignEndDivisorRow = rowCount++;
        } else {
            redesignHeaderRow = -1;
            enableNewMessageMenuRow = -1;
            unifiedScrollRow = -1;
            autoScrollMessagesRow = -1;
            fixedMessageHeightRow = -1;
            blurMessageMenuItemsRow = -1;
            useNativeBlurRow = -1;
            redesignEndDivisorRow = -1;
        }

        miscellaneousHeaderRow = rowCount++;
        messageMenuItemsRow = rowCount++;
        messageMenuItemsCompactView = rowCount++;
        miscellaneousEndDivisor = rowCount++;

        if (listAdapter != null && notify) {
            listAdapter.notifyDataSetChanged();
        }
    }

}
