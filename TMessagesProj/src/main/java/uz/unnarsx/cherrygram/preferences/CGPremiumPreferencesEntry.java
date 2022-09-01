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
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.UndoView;

import uz.unnarsx.cherrygram.CherrygramConfig;
import uz.unnarsx.cherrygram.CherrygramPreferencesNavigator;


public class CGPremiumPreferencesEntry extends BaseFragment {
    private int rowCount;
    private ListAdapter listAdapter;

    private int categoryPrivacyHeaderRow;
    private int readStateInfoRow;
    private int typingStatusInfoRow;
    private int messagesInfoRow;
    private int secretChatsInfoRow;
    private int flagInfoRow;
    private int divisorInfoRow;

    private int infoHeaderRow;
    private int channelUpdatesRow;
    private int groupUpdatesRow;
    private int supportDonationRow;

    private long userId;
    private UndoView restartTooltip;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        updateRowsId();
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);

        actionBar.setTitle(LocaleController.getString("CP_Header_Premium", R.string.CP_Header_Premium));
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

        RecyclerListView listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(listAdapter);
        if(listView.getItemAnimator() != null){
            ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
        }
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setOnItemClickListener((view, position, x, y) -> {
            if (position == channelUpdatesRow) {
                MessagesController.getInstance(currentAccount).openByUserName(LocaleController.getString("CGP_ToChannelLink", R.string.CGP_ToChannelLink), this, 1);
            } else if (position == groupUpdatesRow) {
                MessagesController.getInstance(currentAccount).openByUserName(LocaleController.getString("CGP_ToChatLink", R.string.CGP_ToChatLink), this, 1);
            } else if (position == supportDonationRow) {
                presentFragment(CherrygramPreferencesNavigator.INSTANCE.createDonate());
            }
        });

        restartTooltip = new UndoView(context);
        frameLayout.addView(restartTooltip, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.LEFT, 8, 0, 8, 8));

        return fragmentView;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateRowsId() {
        TLRPC.User user = getMessagesController().getUser(userId);
        rowCount = 0;
        categoryPrivacyHeaderRow = -1;
        readStateInfoRow = -1;
        typingStatusInfoRow = -1;
        messagesInfoRow = -1;
        secretChatsInfoRow = -1;
        flagInfoRow = -1;
        divisorInfoRow = -1;

        categoryPrivacyHeaderRow = rowCount++;
        readStateInfoRow = rowCount++;
        typingStatusInfoRow = rowCount++;
        messagesInfoRow = rowCount++;
        secretChatsInfoRow = rowCount++;
        flagInfoRow = rowCount++;
        divisorInfoRow = rowCount++;

        infoHeaderRow = rowCount++;
        channelUpdatesRow = rowCount++;
        groupUpdatesRow = rowCount++;
        supportDonationRow = rowCount++;

        if (listAdapter != null) {
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
                    TextCell textCell = (TextCell) holder.itemView;
                    textCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    if (position == channelUpdatesRow) {
                        textCell.setTextAndValueAndIcon(LocaleController.getString("CGP_ToChannel", R.string.CGP_ToChannel), "@" + ("Cherry_gram"), R.drawable.advertising_outline_28, true);
                    } else if (position == groupUpdatesRow) {
                        textCell.setTextAndValueAndIcon(LocaleController.getString("CGP_ToChat", R.string.CGP_ToChat), "@" + ("CherrygramSupport"), R.drawable.chats_outline_28, true);
                    }
                    break;
                case 3:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == categoryPrivacyHeaderRow){
                        headerCell.setText(LocaleController.getString("SP_Header_Privacy", R.string.SP_Header_Privacy));
                    } else if (position == infoHeaderRow){
                        headerCell.setText(LocaleController.getString("Info", R.string.Info));
                    }
                    break;
                case 4:
                    TextDetailSettingsCell textDetailCell = (TextDetailSettingsCell) holder.itemView;
                    textDetailCell.setMultilineDetail(true);
                    if (position == readStateInfoRow) {
                        textDetailCell.setTextAndValueAndIcon(LocaleController.getString("CP_ReadState", R.string.CP_ReadState), LocaleController.getString("CP_ReadState_Desc", R.string.CP_ReadState_Desc), R.drawable.ghost_outline_28, true);
                    } else if (position == typingStatusInfoRow) {
                        textDetailCell.setTextAndValueAndIcon(LocaleController.getString("CP_TypingStatus", R.string.CP_TypingStatus), LocaleController.getString("CP_TypingStatus_Desc", R.string.CP_TypingStatus_Desc), R.drawable.msg_send, true);
                    } else if (position == messagesInfoRow) {
                        textDetailCell.setTextAndValueAndIcon(LocaleController.getString("CP_DoNotDeleteMessages", R.string.CP_DoNotDeleteMessages), LocaleController.getString("CP_DoNotDeleteMessages_Desc", R.string.CP_DoNotDeleteMessages_Desc), R.drawable.clear_data_outline_28, true);
                    } else if (position == secretChatsInfoRow) {
                        textDetailCell.setTextAndValueAndIcon(LocaleController.getString("CP_DoNotDeleteSecretChats", R.string.CP_DoNotDeleteSecretChats), LocaleController.getString("CP_DoNotDeleteSecretChats_Desc", R.string.CP_DoNotDeleteSecretChats_Desc), R.drawable.lock_outline_28, true);
                    } else if (position == flagInfoRow) {
                        textDetailCell.setTextAndValueAndIcon(LocaleController.getString("CP_SecureFlag", R.string.CP_SecureFlag), LocaleController.getString("CP_SecureFlag_Desc", R.string.CP_SecureFlag_Desc), R.drawable.msg_screencast, true);
                    } else if (position == supportDonationRow) {
                        textDetailCell.setTextAndValueAndIcon(LocaleController.getString("DP_Donate", R.string.DP_Donate), LocaleController.getString("DP_DonateInfo", R.string.DP_DonateInfo), R.drawable.money_send_outline_28, true);
                    }
                    break;
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == 2 || type == 4;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 2:
                    view = new TextCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 3:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 4:
                    view = new TextDetailSettingsCell(mContext);
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
            if (position == divisorInfoRow) {
                return 1;
            } else if (position == channelUpdatesRow || position == groupUpdatesRow){
                return 2;
            } else if (position == categoryPrivacyHeaderRow || position == infoHeaderRow) {
                return 3;
            } else if (position == readStateInfoRow || position == typingStatusInfoRow || position == messagesInfoRow || position == secretChatsInfoRow || position == flagInfoRow || position == supportDonationRow){
                return 4;
            }
            return 1;
        }
    }
}
