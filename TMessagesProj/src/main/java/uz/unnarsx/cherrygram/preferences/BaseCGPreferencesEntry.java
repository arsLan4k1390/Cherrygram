/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.preferences;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.INavigationLayout;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.ItemOptions;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import org.telegram.ui.Components.UniversalRecyclerView;
import org.telegram.ui.Components.blur3.DownscaleScrollableNoiseSuppressor;
import org.telegram.ui.Components.blur3.RenderNodeWithHash;
import org.telegram.ui.Components.blur3.ViewGroupPartRenderer;
import org.telegram.ui.Components.blur3.capture.IBlur3Capture;
import org.telegram.ui.Components.blur3.capture.IBlur3Hash;
import org.telegram.ui.Components.blur3.source.BlurredBackgroundSourceRenderNode;

import java.util.ArrayList;
import java.util.Locale;

import uz.unnarsx.cherrygram.core.CherrygramLogger;
import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig;
import uz.unnarsx.cherrygram.core.helpers.DeeplinkHelper;
import uz.unnarsx.cherrygram.core.ui.CGBulletinCreator;

public class BaseCGPreferencesEntry extends UniversalFragment {

    protected FrameLayout contentView;
    protected UniversalRecyclerView listView;
    protected LinearLayoutManager layoutManager;
    protected Theme.ResourcesProvider resourcesProvider;
    protected View actionBarBackground;
    protected FrameLayout actionBarContainer;
    protected ActionBarMenuItem searchItem;

    public BaseCGPreferencesEntry() {
        super();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            scrollableViewNoiseSuppressor = new DownscaleScrollableNoiseSuppressor();
            iBlur3SourceGlassFrosted = new BlurredBackgroundSourceRenderNode(null);
            iBlur3SourceGlassFrosted.setupRenderer(new RenderNodeWithHash.Renderer() {
                @Override
                public void renderNodeCalculateHash(IBlur3Hash hash) {
                    hash.add(getThemedColor(Theme.key_windowBackgroundWhite));
                    hash.add(SharedConfig.chatBlurEnabled());
                }

                @Override
                public void renderNodeUpdateDisplayList(Canvas canvas) {
                    canvas.drawColor(getThemedColor(Theme.key_windowBackgroundWhite));
                    if (SharedConfig.chatBlurEnabled()) {
                        scrollableViewNoiseSuppressor.draw(canvas, DownscaleScrollableNoiseSuppressor.DRAW_FROSTED_GLASS);
                    }
                }
            });
        } else {
            scrollableViewNoiseSuppressor = null;
            iBlur3SourceGlassFrosted = null;
        }
    }

    @Override
    public View createView(Context context) {
        contentView = new FrameLayout(context) {
            @Override
            protected void dispatchDraw(@NonNull Canvas canvas) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    blur3_InvalidateBlur();
                    if (iBlur3SourceGlassFrosted != null) {
                        iBlur3SourceGlassFrosted.setSize(getMeasuredWidth(), getMeasuredHeight());
                        iBlur3SourceGlassFrosted.updateDisplayListIfNeeded();
                    }
                }
                super.dispatchDraw(canvas);
            }
        };
        contentView.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundGray));

        listView = new UniversalRecyclerView(this, this::fillItems, this::onClick, (item, view, position, x, y) -> {
            if (onLongClick(item, view, position, x, y)) {
                return true;
            }
            var slug = item.slug;
            var key = getKey();
            if (key != null && item.enabled && !TextUtils.isEmpty(slug)) {
                ItemOptions.makeOptions(this, view)
                        .setScrimViewBackground(listView.getClipBackground(view))
                        .add(R.drawable.msg_copy, getString(R.string.CopyLink), () -> {
                            if (isDeepLinkSectionSlug(slug)) {
                                AndroidUtilities.addToClipboard(String.format(Locale.getDefault(), "https://%s/cgSettings/%s", getMessagesController().linkPrefix, slug));
                            } else {
                                AndroidUtilities.addToClipboard(String.format(Locale.getDefault(), "https://%s/cgSettings/%s?r=%s", getMessagesController().linkPrefix, key, slug));
                            }
                            BulletinFactory.of(this).createCopyLinkBulletin().show();
                        })
                        .setMinWidth(190)
                        .show();
                return true;
            }
            return false;
        });
        listView.adapter.setApplyBackground(false);
        listView.setClipToPadding(false);
        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && scrollableViewNoiseSuppressor != null) {
                    scrollableViewNoiseSuppressor.onScrolled(dx, dy);
                    blur3_InvalidateBlur();
                }
            }
        });
        iBlur3Capture = new ViewGroupPartRenderer(listView, contentView, listView::drawChild);
        listView.addEdgeEffectListener(() -> listView.postOnAnimation(this::blur3_InvalidateBlur));
        listView.setSections();
        listView.setPadding(0, needActionBarPadding() ? ActionBar.getCurrentActionBarHeight() : dp(16), 0, 0);
        contentView.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.FILL));

        actionBarBackground = new View(context) {
            private final Paint blurScrimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final RectF rect = new RectF();

            @Override
            protected void onDraw(@NonNull Canvas canvas) {
                var actionBarHeight = actionBarContainer.getMeasuredHeight();
                rect.set(0, 0, getMeasuredWidth(), actionBarHeight);
                blurScrimPaint.setColor(Theme.getColor(Theme.key_actionBarDefault, resourcesProvider));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && iBlur3SourceGlassFrosted != null) {
                    iBlur3SourceGlassFrosted.draw(canvas, rect.left, rect.top, rect.right, rect.bottom);
                    canvas.saveLayerAlpha(rect, ChatActivity.ACTION_BAR_BLUR_ALPHA);
                    canvas.drawRect(rect, blurScrimPaint);
                    canvas.restore();
                } else {
                    canvas.drawRect(rect, blurScrimPaint);
                }
                if (parentLayout != null) {
                    parentLayout.drawHeaderShadow(canvas, actionBarHeight);
                }
            }
        };
        contentView.addView(actionBarBackground, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 200, Gravity.TOP));
        actionBarContainer = new FrameLayout(context);
        actionBarContainer.addView(actionBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.FILL_HORIZONTAL | Gravity.BOTTOM));
        contentView.addView(actionBarContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.FILL_HORIZONTAL | Gravity.TOP));

        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                updateActionBarVisible();
            }
        });

        updateActionBarVisible(true, false);

        return fragmentView = contentView;
    }

    protected boolean isSearchFieldVisible() {
        return searchItem != null && searchItem.isSearchFieldVisible2();
    }

    @Override
    public boolean onBackPressed(boolean invoked) {
        if (isSearchFieldVisible()) {
            if (invoked) actionBar.closeSearchField();
            return false;
        }
        return super.onBackPressed(invoked);
    }

    private boolean actionBarVisible;
    private ValueAnimator actionBarVisibleAnimator;

    protected void updateActionBarVisible() {
        updateActionBarVisible(false, true);
    }

    private void updateActionBarVisible(boolean force, boolean animated) {
        final boolean visible;
        if (isSearchFieldVisible()) {
            visible = true;
        } else if (listView.getChildCount() > 0) {
            var firstChild = listView.getChildAt(0);
            visible = needActionBarPadding() ? listView.canScrollVertically(-1) : (
                    listView.getChildAdapterPosition(firstChild) > 0 ||
                    firstChild.getY() + firstChild.getHeight() < actionBar.getHeight()
            );
        } else {
            visible = false;
        }
        if (actionBarVisible == visible && !force) return;

        actionBarVisible = visible;
        if (actionBarVisibleAnimator != null) {
            actionBarVisibleAnimator.cancel();
            actionBarVisibleAnimator = null;
        }
        if (!animated) {
            if (!needActionBarPadding())
                actionBar.getTitlesContainer().setAlpha(visible ? 1.0f : 0.0f);
            actionBarBackground.setAlpha(visible ? 1.0f : 0.0f);
        } else {
            actionBarVisibleAnimator = ValueAnimator.ofFloat(actionBarBackground.getAlpha(), visible ? 1.0f : 0.0f);
            actionBarVisibleAnimator.addUpdateListener(a -> {
                final float t = (float) a.getAnimatedValue();
                if (!needActionBarPadding()) actionBar.getTitlesContainer().setAlpha(t);
                actionBarBackground.setAlpha(t);
            });
            actionBarVisibleAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
            actionBarVisibleAnimator.setDuration(420);
            actionBarVisibleAnimator.start();
        }
    }

    protected CharSequence getTitle() {
        return null;
    }

    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {

    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {

    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }

    @Override
    public void setParentLayout(INavigationLayout layout) {
        if (layout != null && layout.getLastFragment() != null) {
            resourcesProvider = layout.getLastFragment().getResourceProvider();
        }
        super.setParentLayout(layout);
    }

    @Override
    public Theme.ResourcesProvider getResourceProvider() {
        return resourcesProvider;
    }

    @Override
    public ActionBar createActionBar(Context context) {
        var actionBar = super.createActionBar(context);
        actionBar.setBackgroundColor(Color.TRANSPARENT);
        actionBar.setAddToContainer(false);
        actionBar.setUseContainerForTitles();
        actionBar.setOccupyStatusBar(false);
        actionBar.setTitle(getTitle(), null, true);
        actionBar.setTitleColor(getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
        actionBar.setItemsColor(getThemedColor(Theme.key_windowBackgroundWhiteBlackText), false);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });
        BackDrawable backDrawable = new BackDrawable(false);
        backDrawable.setShowStick(!CherrygramAppearanceConfig.INSTANCE.getCenterTitle());
        actionBar.setBackButtonDrawable(backDrawable);
        return actionBar;
    }

    protected boolean needActionBarPadding() {
        return true;
    }

    /** Bulletins start */
    protected void showRestartBulletin() {
        CGBulletinCreator.INSTANCE.createRestartBulletin(this);
    }

    protected void showDonateBulletin() {
        CGBulletinCreator.INSTANCE.createRequireDonateBulletin(this);
    }

    protected void showSuccessBulletin() {
        CGBulletinCreator.INSTANCE.createDebugSuccessBulletin(this);
    }
    /** Bulletins finish */

    protected void updateRows(boolean animated) {
        if (listView != null && listView.adapter != null) listView.adapter.update(animated);
    }

    protected String getKey() {
        return null;
    }

    public void scrollToRow(String key, Runnable unknown) {
        if (listView == null) return;
        var position = listView.findPositionByItemSlug(key);
        if (position != -1) {
            listView.highlightRow(() -> {
                var layoutManager = (LinearLayoutManager) listView.getLayoutManager();
                var offset = AndroidUtilities.displaySize.y / 2;
                layoutManager.scrollToPositionWithOffset(position, offset);
                return position;
            });
        } else {
            if (unknown != null) unknown.run();
        }
    }

    private static boolean isDeepLinkSectionSlug(String slug) {
        try {
            for (var field : DeeplinkHelper.DeepLinksRepo.class.getFields()) {
                if (field.getType() == String.class && slug.equals(field.get(null))) {
                    return true;
                }
            }
        } catch (IllegalAccessException e) {
            CherrygramLogger.e(e);
        }
        return false;
    }

    @Override
    public boolean isSupportEdgeToEdge() {
        return true;
    }

    @Override
    public void onInsets(int left, int top, int right, int bottom) {
        var topPadding = needActionBarPadding() ? ActionBar.getCurrentActionBarHeight() : dp(16);
        listView.setPadding(0, top + topPadding, 0, bottom);
        actionBarContainer.setPadding(0, top, 0, 0);
        super.onInsets(left, top, right, bottom);
    }

    /* Blur */

    private final @Nullable DownscaleScrollableNoiseSuppressor scrollableViewNoiseSuppressor;
    private final @Nullable BlurredBackgroundSourceRenderNode iBlur3SourceGlassFrosted;

    private IBlur3Capture iBlur3Capture;

    private final ArrayList<RectF> iBlur3Positions = new ArrayList<>();
    private final RectF iBlur3PositionActionBar = new RectF();

    {
        iBlur3Positions.add(iBlur3PositionActionBar);
    }

    private void blur3_InvalidateBlur() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || scrollableViewNoiseSuppressor == null) {
            return;
        }

        final int additionalList = dp(48);
        iBlur3PositionActionBar.set(0, -additionalList, fragmentView.getMeasuredWidth(), actionBarContainer.getMeasuredHeight() + additionalList);

        scrollableViewNoiseSuppressor.setupRenderNodes(iBlur3Positions, 1);
        scrollableViewNoiseSuppressor.invalidateResultRenderNodes(iBlur3Capture, fragmentView.getMeasuredWidth(), fragmentView.getMeasuredHeight());
    }

}