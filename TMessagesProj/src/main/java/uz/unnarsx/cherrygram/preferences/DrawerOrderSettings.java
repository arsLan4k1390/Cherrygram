//package uz.unnarsx.cherrygram.preferences;
//
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.graphics.Canvas;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.FrameLayout;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.DefaultItemAnimator;
//import androidx.recyclerview.widget.ItemTouchHelper;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import org.telegram.messenger.AndroidUtilities;
//import org.telegram.messenger.LocaleController;
//import org.telegram.messenger.NotificationCenter;
//import org.telegram.messenger.R;
//import org.telegram.ui.ActionBar.ActionBar;
//import org.telegram.ui.ActionBar.ActionBarMenu;
//import org.telegram.ui.ActionBar.ActionBarMenuItem;
//import org.telegram.ui.ActionBar.BaseFragment;
//import org.telegram.ui.ActionBar.Theme;
//import org.telegram.ui.Cells.HeaderCell;
//import org.telegram.ui.Cells.ShadowSectionCell;
//import org.telegram.ui.Components.LayoutHelper;
//import org.telegram.ui.Components.RecyclerListView;
//
//import it.owlgram.android.components.AddItemCell;
//import it.owlgram.android.components.HintHeaderCell;
//import it.owlgram.android.components.SwapOrderCell;
//import it.owlgram.android.helpers.MenuOrderManager;
//
//public class DrawerOrderSettings extends BaseFragment {
//    private int rowCount;
//    private ListAdapter listAdapter;
//    private ItemTouchHelper itemTouchHelper;
//    private RecyclerListView listView;
//    private ActionBarMenuItem menuItem;
//
//    private int headerHintRow;
//    private int headerSuggestedOptionsRow;
//    private int headerMenuRow;
//    private int menuHintsStartRow;
//    private int menuHintsEndRow;
//    private int hintsDividerRow;
//    private int menuItemsStartRow;
//    private int menuItemsEndRow;
//    private int menuItemsDividerRow;
//
//    @Override
//    public boolean onFragmentCreate() {
//        super.onFragmentCreate();
//        updateRowsId(true);
//        return true;
//    }
//
//
//    @Override
//    public View createView(Context context) {
//        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
//        actionBar.setTitle(LocaleController.getString("MenuItems", R.string.MenuItems));
//        actionBar.setAllowOverlayTitle(false);
//        if (AndroidUtilities.isTablet()) {
//            actionBar.setOccupyStatusBar(false);
//        }
//        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
//            @Override
//            public void onItemClick(int id) {
//                if (id == -1) {
//                    finishFragment();
//                } else if (id == 1) {
//                    MenuOrderManager.resetToDefaultPosition();
//                    updateRowsId(true);
//                    menuItem.setVisibility(View.GONE);
//                    getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
//                }
//            }
//        });
//
//        ActionBarMenu menu = actionBar.createMenu();
//        menuItem = menu.addItem(0, R.drawable.ic_ab_other);
//        menuItem.setContentDescription(LocaleController.getString("AccDescrMoreOptions", R.string.AccDescrMoreOptions));
//        menuItem.addSubItem(1, R.drawable.msg_reset, LocaleController.getString("ResetItemsOrder", R.string.ResetItemsOrder));
//        menuItem.setVisibility(MenuOrderManager.IsDefaultPosition() ? View.GONE : View.VISIBLE);
//
//        listAdapter = new ListAdapter(context);
//        fragmentView = new FrameLayout(context);
//        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
//        FrameLayout frameLayout = (FrameLayout) fragmentView;
//
//        listView = new RecyclerListView(context);
//        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
//        listView.setVerticalScrollBarEnabled(false);
//        itemTouchHelper = new ItemTouchHelper(new TouchHelperCallback());
//        itemTouchHelper.attachToRecyclerView(listView);
//        listView.setAdapter(listAdapter);
//        if(listView.getItemAnimator() != null){
//            ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
//        }
//        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
//        return fragmentView;
//    }
//
//    @SuppressLint("NotifyDataSetChanged")
//    private void updateRowsId(boolean notify) {
//        rowCount = 0;
//        headerSuggestedOptionsRow = -1;
//        hintsDividerRow = -1;
//
//        int size_hints = MenuOrderManager.sizeHints();
//        headerHintRow = rowCount++;
//
//        if(size_hints > 0) {
//            headerSuggestedOptionsRow = rowCount++;
//            menuHintsStartRow = rowCount;
//            rowCount += size_hints;
//            menuHintsEndRow = rowCount;
//            hintsDividerRow = rowCount++;
//        }
//
//        headerMenuRow = rowCount++;
//        menuItemsStartRow = rowCount;
//        rowCount += MenuOrderManager.sizeAvailable();
//        menuItemsEndRow = rowCount;
//        menuItemsDividerRow = rowCount++;
//
//        if (listAdapter != null && notify) {
//            listAdapter.notifyDataSetChanged();
//        }
//    }
//
//    @SuppressLint("NotifyDataSetChanged")
//    @Override
//    public void onResume() {
//        super.onResume();
//        if (listAdapter != null) {
//            listAdapter.notifyDataSetChanged();
//        }
//    }
//
//    private class ListAdapter extends RecyclerListView.SelectionAdapter {
//        private final Context mContext;
//
//        public ListAdapter(Context context) {
//            mContext = context;
//        }
//
//        @Override
//        public int getItemCount() {
//            return rowCount;
//        }
//
//        @Override
//        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//            switch (holder.getItemViewType()) {
//                case 1:
//                    holder.itemView.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
//                    break;
//                case 3:
//                    HeaderCell headerCell = (HeaderCell) holder.itemView;
//                    if (position == headerSuggestedOptionsRow) {
//                        headerCell.setText(LocaleController.getString("RecommendedItems", R.string.RecommendedItems));
//                    } else if (position == headerMenuRow) {
//                        headerCell.setText(LocaleController.getString("MenuItems", R.string.MenuItems));
//                    }
//                    break;
//                case 4:
//                    SwapOrderCell swapOrderCell = (SwapOrderCell) holder.itemView;
//                    MenuOrderManager.EditableMenuItem data = MenuOrderManager.getSingleAvailableMenuItem(position - menuItemsStartRow);
//                    if(data != null) {
//                        swapOrderCell.setData(data.text, data.isDefault, data.id, true);
//                    }
//                    break;
//                case 5:
//                    AddItemCell addItemCell = (AddItemCell) holder.itemView;
//                    MenuOrderManager.EditableMenuItem notData = MenuOrderManager.getSingleNotAvailableMenuItem(position - menuHintsStartRow);
//                    if(notData != null) {
//                        addItemCell.setData(notData.text, notData.id,true);
//                    }
//                    break;
//            }
//        }
//
//        @Override
//        public boolean isEnabled(RecyclerView.ViewHolder holder) {
//            int type = holder.getItemViewType();
//            return type == 4;
//        }
//
//        @SuppressLint("ClickableViewAccessibility")
//        @NonNull
//        @Override
//        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            View view;
//            switch (viewType) {
//                case 2:
//                    view = new HintHeaderCell(mContext, R.raw.filters, LocaleController.formatString("MenuItemsOrderDesc", R.string.MenuItemsOrderDesc));
//                    view.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider_top, Theme.key_windowBackgroundGrayShadow));
//                    break;
//                case 3:
//                    view = new HeaderCell(mContext);
//                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
//                    break;
//                case 4:
//                    SwapOrderCell swapOrderCell = new SwapOrderCell(mContext);
//                    swapOrderCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
//                    swapOrderCell.setOnReorderButtonTouchListener((v, event) -> {
//                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                            itemTouchHelper.startDrag(listView.getChildViewHolder(swapOrderCell));
//                        }
//                        return false;
//                    });
//                    swapOrderCell.setOnDeleteClick(v -> {
//                        if(MenuOrderManager.isAvailable(swapOrderCell.menuId)) {
//                            int index = MenuOrderManager.getPositionOf(swapOrderCell.menuId);
//                            if (index != -1) {
//                                index += menuItemsStartRow;
//                                MenuOrderManager.removeItem(swapOrderCell.menuId);
//                                int index2 = MenuOrderManager.getPositionOf(swapOrderCell.menuId);
//                                index2 += menuHintsStartRow;
//                                int prevRecommendedHeaderRow = headerSuggestedOptionsRow;
//                                updateRowsId(false);
//                                if (prevRecommendedHeaderRow == -1 && headerSuggestedOptionsRow != -1) {
//                                    int itemsCount = hintsDividerRow - headerSuggestedOptionsRow + 1;
//                                    index += itemsCount;
//                                    listAdapter.notifyItemRangeInserted(headerSuggestedOptionsRow, itemsCount);
//                                } else {
//                                    index += 1;
//                                    listAdapter.notifyItemInserted(index2);
//                                }
//                                listAdapter.notifyItemRemoved(index);
//                            } else {
//                                updateRowsId(true);
//                            }
//                            menuItem.setVisibility(MenuOrderManager.IsDefaultPosition() ? View.GONE : View.VISIBLE);
//                            getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
//                        }
//                    });
//                    view = swapOrderCell;
//                    break;
//                case 5:
//                    AddItemCell addItemCell = new AddItemCell(mContext);
//                    addItemCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
//                    addItemCell.setAddOnClickListener(v -> {
//                        if(!MenuOrderManager.isAvailable(addItemCell.menuId)) {
//                            int index = MenuOrderManager.getPositionOf(addItemCell.menuId);
//                            if (index != -1) {
//                                MenuOrderManager.addItem(addItemCell.menuId);
//                                int prevRecommendedHintHeaderRow = headerSuggestedOptionsRow;
//                                int prevRecommendedHintSectionRow = hintsDividerRow;
//                                index += menuHintsStartRow;
//                                updateRowsId(false);
//                                if (prevRecommendedHintHeaderRow != -1 && headerSuggestedOptionsRow == -1) {
//                                    listAdapter.notifyItemRangeRemoved(prevRecommendedHintHeaderRow, prevRecommendedHintSectionRow - prevRecommendedHintHeaderRow + 1);
//                                } else {
//                                    listAdapter.notifyItemRemoved(index);
//                                }
//                                listAdapter.notifyItemInserted(menuItemsStartRow);
//                            } else {
//                                updateRowsId(true);
//                            }
//                            menuItem.setVisibility(MenuOrderManager.IsDefaultPosition() ? View.GONE : View.VISIBLE);
//                            getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
//                        }
//                    });
//                    view = addItemCell;
//                    break;
//                default:
//                    view = new ShadowSectionCell(mContext);
//                    break;
//            }
//            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
//            return new RecyclerListView.Holder(view);
//        }
//
//        @Override
//        public int getItemViewType(int position) {
//            if (position == hintsDividerRow || position == menuItemsDividerRow) {
//                return 1;
//            } else if (position == headerHintRow) {
//                return 2;
//            } else if (position == headerSuggestedOptionsRow || position == headerMenuRow) {
//                return 3;
//            } else if (position >= menuItemsStartRow && position < menuItemsEndRow) {
//                return 4;
//            } else if (position >= menuHintsStartRow && position < menuHintsEndRow) {
//                return 5;
//            }
//            return 1;
//        }
//
//        public void swapElements(int fromIndex, int toIndex) {
//            int idx1 = fromIndex - menuItemsStartRow;
//            int idx2 = toIndex - menuItemsStartRow;
//            int count = menuItemsEndRow - menuItemsStartRow;
//            if (idx1 < 0 || idx2 < 0 || idx1 >= count || idx2 >= count) {
//                return;
//            }
//            MenuOrderManager.changePosition(idx1, idx2);
//            notifyItemMoved(fromIndex, toIndex);
//            menuItem.setVisibility(MenuOrderManager.IsDefaultPosition() ? View.GONE : View.VISIBLE);
//            getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
//        }
//    }
//
//    public class TouchHelperCallback extends ItemTouchHelper.Callback {
//
//        @Override
//        public boolean isLongPressDragEnabled() {
//            return true;
//        }
//
//        @Override
//        public int getMovementFlags(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
//            if (viewHolder.getItemViewType() != 4) {
//                return makeMovementFlags(0, 0);
//            }
//            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
//        }
//
//        @Override
//        public boolean onMove(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
//            if (source.getItemViewType() != target.getItemViewType()) {
//                return false;
//            }
//            listAdapter.swapElements(source.getAdapterPosition(), target.getAdapterPosition());
//            return true;
//        }
//
//        @Override
//        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
//            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
//        }
//
//        @Override
//        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
//            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
//                listView.cancelClickRunnables(false);
//                viewHolder.itemView.setPressed(true);
//            }
//            super.onSelectedChanged(viewHolder, actionState);
//        }
//
//        @Override
//        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}
//
//        @Override
//        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
//            super.clearView(recyclerView, viewHolder);
//            viewHolder.itemView.setPressed(false);
//        }
//    }
//}
