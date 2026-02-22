/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */
package uz.unnarsx.cherrygram.core.ui;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.StateSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.BaseCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.ProfileMusicView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.FiltersSetupActivity;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.ProfileActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig;

public abstract class MD3ListAdapter extends RecyclerListView.SelectionAdapter {

    public static final int ROLE_UNKNOWN = -1;
    public static final int ROLE_CONTENT = 0;
    public static final int ROLE_HEADER = 1;
    public static final int ROLE_DIVIDER = 2;
    public static final int ROLE_STICKER_CELL = 3;
    public static final int ROLE_IGNORE = 8;

    private final SparseIntArray roles = new SparseIntArray();
    private final SparseBooleanArray headerViewTypes = new SparseBooleanArray();
    private final SparseBooleanArray stickerCellViewTypes = new SparseBooleanArray();
    private final SparseBooleanArray mustIgnoreViewTypes = new SparseBooleanArray();
    private final SparseBooleanArray dividerViewTypes = new SparseBooleanArray();
    public static final Md3Config config = new Md3Config();
    private RecyclerView attachedRecyclerView;

    private static final ArrayList<WeakReference<MD3ListAdapter>> attachedAdapters = new ArrayList<>();
    //private static ValueAnimator md3MorphAnimator;

    private final RecyclerView.AdapterDataObserver md3DataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            reapplyVisible();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            reapplyVisible();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            reapplyVisible();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            reapplyVisible();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            reapplyVisible();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            reapplyVisible();
        }
    };

    private final RecyclerView.OnChildAttachStateChangeListener md3AttachListener =
            new RecyclerView.OnChildAttachStateChangeListener() {
                @Override
                public void onChildViewAttachedToWindow(@NonNull View view) {
                    RecyclerView rv = attachedRecyclerView;
                    if (rv == null) {
                        return;
                    }
                    RecyclerView.ViewHolder holder = rv.getChildViewHolder(view);
                    int position = resolveAdapterPosition(holder);
                    if (position != RecyclerView.NO_POSITION) {
                        applyMD3Background(holder, position);
                    }
                }

                @Override
                public void onChildViewDetachedFromWindow(@NonNull View view) {
                }
            };

    protected MD3ListAdapter() {
        this(null);
        Log.d("lolkek", "MD3ListAdapter()");
    }

    protected MD3ListAdapter(@Nullable Theme.ResourcesProvider resourcesProvider) {
        config.resourcesProvider = resourcesProvider;
        Log.d("lolkek", "MD3ListAdapter(resourcesProvider)");
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        attachedRecyclerView = recyclerView;
        recyclerView.addOnChildAttachStateChangeListener(md3AttachListener);
        registerAdapterDataObserver(md3DataObserver);
        registerAdapterInstance(this);
        recyclerView.post(this::reapplyVisible);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.removeOnChildAttachStateChangeListener(md3AttachListener);
        unregisterAdapterDataObserver(md3DataObserver);
        attachedRecyclerView = null;
        unregisterAdapterInstance(this);
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    @SuppressWarnings({"unchecked", "NullableProblems"})
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int position = resolveAdapterPosition(holder);
        if (position != RecyclerView.NO_POSITION) {
            applyMD3Background(holder, position);
        }
    }

    protected final void applyMD3Background(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position == RecyclerView.NO_POSITION || !isMd3ContainersEnabled() || mustIgnoreViewForPosition(position)) {
            return;
        }

        int itemCount = getItemCount();
        if (position < 0 || position >= itemCount) {
            return;
        }
        learnRole(holder);

        int viewType = holder.getItemViewType();

        if (mustIgnoreView(viewType)) {
            return;
        }

        if (isDividerView(viewType)) {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            return;
        }

        int previousItemViewType = position > 0 ? getSafeItemViewType(position - 1) : ROLE_UNKNOWN;
        int nextItemViewType = position < itemCount - 1 ? getSafeItemViewType(position + 1) : ROLE_UNKNOWN;
        boolean isPreviousItemBoundary = isBoundaryView(previousItemViewType);
        boolean isNextItemBoundary = isBoundaryView(nextItemViewType);

        boolean isHeaderView = isHeaderView(viewType);
        boolean isDividerView = isDividerView(viewType);
        boolean headerActsAsBoundary = isHeaderView && !config.headerBackgroundEnabled;

        if ((isDividerView || headerActsAsBoundary) || isOctoPreferencesHeader(viewType)) {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            applyMd3Margins(holder.itemView, position, position == getItemCount() - 1, isNextItemBoundary, true);
            return;
        }

        boolean isFirstInGroup = position == 0 || isPreviousItemBoundary;

        if (!isFirstInGroup) {
            boolean isEffectFirst = true;
            for (int i = 0; i < position; i++) {
                if (!mustIgnoreView(getItemViewType(i))) {
                    isEffectFirst = false;
                }
            }
            isFirstInGroup = isEffectFirst;
        }

        boolean isLastInGroup = position == getItemCount() - 1 || isNextItemBoundary;


        int cornerRadius = dp(config.cornerRadiusDp);
        int topLeft, topRight = 0, bottomLeft = 0, bottomRight = 0;

        if (isHeaderView) {
            topLeft = topRight = cornerRadius;
        } else if (isFirstInGroup && isLastInGroup) {
            topLeft = topRight = bottomLeft = bottomRight = cornerRadius;
        } else if (isFirstInGroup) {
            topLeft = topRight = cornerRadius;
        } else {
            topLeft = 0;
            if (isLastInGroup) {
                bottomLeft = bottomRight = cornerRadius;
            }
        }

        int backgroundColor = getBackgroundColor();
        Drawable backgroundDrawable = Theme.createRoundRectDrawable(topLeft, topRight, bottomRight, bottomLeft, backgroundColor);

        if (config.useRipple) {
            Drawable rippleMaskDrawable = Theme.createRoundRectDrawable(topLeft, topRight, bottomRight, bottomLeft, Color.WHITE);
            int rippleColor = Theme.getColor(Theme.key_listSelector, config.resourcesProvider);
            ColorStateList rippleColorStateList = new ColorStateList(new int[][]{StateSet.WILD_CARD}, new int[]{rippleColor});
            Drawable rippleDrawable = new BaseCell.RippleDrawableSafe(rippleColorStateList, backgroundDrawable, rippleMaskDrawable);
            holder.itemView.setBackground(rippleDrawable);
        } else {
            holder.itemView.setBackground(backgroundDrawable);
        }

        int finalBottomRight = bottomRight;
        int finalBottomLeft = bottomLeft;
        int finalTopRight = topRight;

        holder.itemView.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        holder.itemView.setClipToOutline(false);

        holder.itemView.setClipToOutline(true);
        holder.itemView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                final int width = view.getWidth();
                final int height = view.getHeight();

                float[] radii = new float[]{
                        topLeft, topLeft,
                        finalTopRight, finalTopRight,
                        finalBottomRight, finalBottomRight,
                        finalBottomLeft, finalBottomLeft
                };

                Path path = new Path();

                path.addRoundRect(
                        0f,
                        0f,
                        (float) width,
                        (float) height,
                        radii,
                        Path.Direction.CW
                );

                outline.setConvexPath(path);
            }
        });

        int selectorColor = Theme.getColor(Theme.key_listSelector, config.resourcesProvider);
        Drawable md3Selector = Theme.createRadSelectorDrawable(selectorColor, /*cornerRadius*/ 0, 0);

        holder.itemView.setForeground(md3Selector);
        md3Selector.setCallback(holder.itemView);

        applyMd3Margins(holder.itemView, position, position == getItemCount() - 1, isNextItemBoundary, true);
    }

    private int resolveAdapterPosition(@NonNull RecyclerView.ViewHolder holder) {
        int pos = holder.getAdapterPosition();
        if (pos == RecyclerView.NO_POSITION) {
            pos = holder.getLayoutPosition();
        }
        return pos;
    }

    public static void applyMd3Margins(View itemView, int position, boolean isLastChild, boolean isNextItemBoundary, boolean fromRecyclerView) {
        int bottomMargin = 0;
        int sideMargin = dp(config.sidePaddingDp);

        boolean isFirstChild = position == 0;

        if (isLastChild || isNextItemBoundary) {
            bottomMargin = dp(config.innerGapDp);
        }

        if (fromRecyclerView) {
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) itemView.getLayoutParams();
            if (layoutParams == null) {
                layoutParams = new RecyclerView.LayoutParams(
                        RecyclerView.LayoutParams.MATCH_PARENT,
                        RecyclerView.LayoutParams.WRAP_CONTENT
                );
            }
            layoutParams.topMargin = isFirstChild && !canTryToIgnoreTopBarBackground(LaunchActivity.getSafeLastFragment()) ? dp(config.firstTopPaddingDp) : 0;
            layoutParams.leftMargin = sideMargin;
            layoutParams.rightMargin = sideMargin;
            layoutParams.bottomMargin = bottomMargin;
            itemView.setLayoutParams(layoutParams);
        } else {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) itemView.getLayoutParams();
            if (layoutParams == null) {
                layoutParams = new LinearLayout.LayoutParams(
                        RecyclerView.LayoutParams.MATCH_PARENT,
                        RecyclerView.LayoutParams.WRAP_CONTENT
                );
            }
            layoutParams.topMargin = isFirstChild && !canTryToIgnoreTopBarBackground(LaunchActivity.getSafeLastFragment()) ? dp(config.firstTopPaddingDp) : 0;
            layoutParams.leftMargin = sideMargin;
            layoutParams.rightMargin = sideMargin;
            layoutParams.bottomMargin = bottomMargin;
            itemView.setLayoutParams(layoutParams);
        }
    }

    public void reapplyVisible() {
        RecyclerView rv = attachedRecyclerView;
        if (rv == null) {
            return;
        }

        for (int i = 0, count = rv.getChildCount(); i < count; i++) {
            View child = rv.getChildAt(i);
            RecyclerView.ViewHolder vh = rv.getChildViewHolder(child);
            int pos = resolveAdapterPosition(vh);
            if (pos != RecyclerView.NO_POSITION) {
                applyMD3Background(vh, pos);
            }
        }
    }

    private void learnRole(@NonNull RecyclerView.ViewHolder holder) {
        int viewType = holder.getItemViewType();
        if (roles.get(viewType, ROLE_UNKNOWN) != ROLE_UNKNOWN) {
            return;
        }
        View view = holder.itemView;
        if (isDividerView(view)) {
            roles.put(viewType, ROLE_DIVIDER);
            dividerViewTypes.put(viewType, true);
        } else if (isHeaderView(view)) {
            roles.put(viewType, ROLE_HEADER);
            headerViewTypes.put(viewType, true);
        } else if (isOctoPreferencesHeader(view)) {
            roles.put(viewType, ROLE_STICKER_CELL);
            stickerCellViewTypes.put(viewType, true);
        } else if (mustIgnoreView(view)) {
            roles.put(viewType, ROLE_IGNORE);
            mustIgnoreViewTypes.put(viewType, true);
        } else {
            int custom = resolveCustomRole();
            roles.put(viewType, custom == ROLE_UNKNOWN ? ROLE_CONTENT : custom);
        }
    }

    public void forceLearnRole(int viewType, int role) {
        roles.put(viewType, ROLE_CONTENT); // ROLE_DIVIDER
        switch (role) {
            case ROLE_DIVIDER -> dividerViewTypes.put(viewType, true);
            case ROLE_HEADER -> headerViewTypes.put(viewType, true);
            case ROLE_STICKER_CELL -> stickerCellViewTypes.put(viewType, true);
            case ROLE_IGNORE -> mustIgnoreViewTypes.put(viewType, true);
        }
    }

    protected static boolean isBoundaryView(@NonNull View view) {
        return !mustIgnoreView(view) && (isDividerView(view) || (isHeaderView(view) && !config.headerBackgroundEnabled));
    }

    private boolean isBoundaryView(int viewType) {
        return !mustIgnoreView(viewType) && (isDividerView(viewType) || (isHeaderView(viewType) && !config.headerBackgroundEnabled));
    }

    protected static boolean isDividerView(@NonNull View view) {
        return view instanceof ShadowSectionCell || view instanceof TextInfoPrivacyCell;
    }

    protected boolean isDividerView(int viewType) {
        return checkRole(viewType, dividerViewTypes, ROLE_DIVIDER);
    }

    protected static boolean isHeaderView(@NonNull View view) {
        return view instanceof HeaderCell /*c1 && ObjectUtils.notEqual(ListAdapter.HEADER_WITHOUT_STYLING_TAG, c1.getTag())*/;
    }

    protected boolean isHeaderView(int viewType) {
        return checkRole(viewType, headerViewTypes, ROLE_HEADER);
    }

    protected static boolean isOctoPreferencesHeader(@NonNull View view) {
        return view instanceof FiltersSetupActivity.HintInnerCell;
    }

    protected boolean isOctoPreferencesHeader(int viewType) {
        return checkRole(viewType, stickerCellViewTypes, ROLE_STICKER_CELL);
    }
    
    protected static boolean mustIgnoreView(@NonNull View view) {
        return view instanceof ProfileMusicView;
    }
    
    protected boolean mustIgnoreView(int viewType) {
        return checkRole(viewType, mustIgnoreViewTypes, ROLE_IGNORE);
    }

    protected boolean mustIgnoreViewForPosition(int position) {
        return false;
    }

    private boolean checkRole(int viewType, SparseBooleanArray array, int checknRole) {
        int role = roles.get(viewType, ROLE_UNKNOWN);
        if (role == checknRole) return true;
        return array.get(viewType, false);
    }

    protected int resolveCustomRole() {
        return ROLE_UNKNOWN;
    }

    protected boolean canUseTopMarginForFirstItem() {
        return true;
    }

    private int getSafeItemViewType(int position) {
        if (position < 0 || position >= getItemCount()) {
            return ROLE_UNKNOWN;
        }
        return getItemViewType(position);
    }

    public static class Md3Config {
        public int sidePaddingDp = 12; // 16
        public int firstTopPaddingDp = 12; // Use when actionBar background == listView's background
        public int innerGapDp = 0; // 2
        public int cornerRadiusDp = 16; // 14

        public boolean useRipple = false;
        public boolean headerBackgroundEnabled = true;

        @Nullable
        public Theme.ResourcesProvider resourcesProvider;
    }

    public static int getBackgroundColor() {
        if (shouldUseCustomColors()) {
            if (config.resourcesProvider == null) {
                return ColorUtils.blendARGB(Theme.getColor(Theme.key_actionBarDefaultSubmenuItemIcon), Theme.getColor(Theme.key_dialogBackground), 0.9f);
            } else {
                return Theme.getColor(Theme.key_windowBackgroundWhite, config.resourcesProvider);
            }
        } else {
            return Theme.getColor(Theme.key_windowBackgroundWhite, config.resourcesProvider);
        }
    }

    public static boolean shouldUseCustomColors() {
        return Theme.getActiveTheme().isMonetLight() || Theme.getActiveTheme().isMonetDark() || Theme.getCurrentTheme().isMonetAmoled() || Theme.getActiveTheme().isAmoled();
    }

    private static void registerAdapterInstance(MD3ListAdapter adapter) {
        for (int i = attachedAdapters.size() - 1; i >= 0; i--) {
            MD3ListAdapter existing = attachedAdapters.get(i).get();
            if (existing == null) {
                attachedAdapters.remove(i);
            }
        }
        attachedAdapters.add(new WeakReference<>(adapter));
    }

    private static void unregisterAdapterInstance(MD3ListAdapter adapter) {
        for (int i = attachedAdapters.size() - 1; i >= 0; i--) {
            MD3ListAdapter existing = attachedAdapters.get(i).get();
            if (existing == null || existing == adapter) {
                attachedAdapters.remove(i);
            }
        }
    }

    private static void notifyAdapters() {
        for (int i = attachedAdapters.size() - 1; i >= 0; i--) {
            MD3ListAdapter existing = attachedAdapters.get(i).get();
            if (existing == null) {
                attachedAdapters.remove(i);
                continue;
            }
            existing.reapplyVisible();
        }
    }

    public static boolean isMd3ContainersEnabled() {
        return true;
    }

    public static boolean canTryToIgnoreHeaderBackground(Object fragment) {
        return isMd3ContainersEnabled() && !(fragment instanceof ChatActivity || fragment instanceof ProfileActivity);
    }

    public static boolean canTryToIgnoreTopBarBackground() {
        return isMd3ContainersEnabled();
    }

    public static boolean canTryToIgnoreTopBarBackground(Object fragment) {
        if (!isMd3ContainersEnabled()) {
            return false;
        }

        return fragment instanceof ProfileActivity || fragment instanceof ProfileMusicView;
    }

    public static void forceAdaptContainerItems(View container) {
        if (!isMd3ContainersEnabled()) {
            return;
        }

        if (!(container instanceof LinearLayout c)) {
            return;
        }

        int childCount = c.getChildCount();

        boolean isFirstNonIgnoredItem = true;

        for (int i = 0; i < childCount; i++) {
            View child = c.getChildAt(i);

            if (mustIgnoreView(child)) {
                continue;
            }

            boolean isPreviousItemBoundary = i > 0 && isBoundaryView(c.getChildAt(i-1));
            boolean isNextItemBoundary = i != childCount-1 && isBoundaryView(c.getChildAt(i+1));

            boolean isHeaderView = isHeaderView(child);
            boolean isDividerView = isDividerView(child);
            boolean headerActsAsBoundary = isHeaderView && !config.headerBackgroundEnabled;

            if ((isDividerView || headerActsAsBoundary) || isOctoPreferencesHeader(child)) {
                child.setBackgroundColor(Color.TRANSPARENT);
                applyMd3Margins(child, i, i == childCount - 1, isNextItemBoundary, false);
                continue;
            }

            boolean isFirstInGroup = (i == 0 || isPreviousItemBoundary) || isFirstNonIgnoredItem;
            boolean isLastInGroup = i == childCount - 1 || isNextItemBoundary;

            int cornerRadius = dp(config.cornerRadiusDp);
            int topLeft = 0, topRight = 0, bottomLeft = 0, bottomRight = 0;

            if (isHeaderView) {
                topLeft = topRight = cornerRadius;
            } else if (isFirstInGroup && isLastInGroup) {
                topLeft = topRight = bottomLeft = bottomRight = cornerRadius;
            } else if (isFirstInGroup) {
                topLeft = topRight = cornerRadius;
            } else if (isLastInGroup) {
                bottomLeft = bottomRight = cornerRadius;
            }

            Log.e("a", "setting "+child);
            int backgroundColor = getBackgroundColor();
            Drawable backgroundDrawable = Theme.createRoundRectDrawable(topLeft, topRight, bottomRight, bottomLeft, backgroundColor);
            Drawable rippleMaskDrawable = Theme.createRoundRectDrawable(topLeft, topRight, bottomRight, bottomLeft, Color.WHITE);

            child.setBackgroundColor(Color.TRANSPARENT);
            child.setBackground(null);

            if (config.useRipple) {
                int rippleColor = Theme.getColor(Theme.key_listSelector, config.resourcesProvider);
                ColorStateList rippleColorStateList = new ColorStateList(new int[][]{StateSet.WILD_CARD}, new int[]{rippleColor});
                Drawable rippleDrawable = new BaseCell.RippleDrawableSafe(rippleColorStateList, backgroundDrawable, rippleMaskDrawable);
                child.setBackground(rippleDrawable);
            } else {
                child.setBackground(backgroundDrawable);
            }

            child.setClipToOutline(cornerRadius > 0);

            applyMd3Margins(child, i, i == childCount - 1, isNextItemBoundary, false);
            child.invalidate();
            child.requestLayout();

            if (!mustIgnoreView(child)) {
                isFirstNonIgnoredItem = false;
            }
        }
    }

}
