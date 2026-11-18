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
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.INavigationLayout;
import org.telegram.ui.ActionBar.SimpleActionBar;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.ItemOptions;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import org.telegram.ui.Components.UniversalRecyclerView;

import java.lang.reflect.Modifier;
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
    protected View actionBarFadeView;
    protected FrameLayout actionBarContainer;
    protected ActionBarMenuItem searchItem;

    @Override
    public View createView(Context context) {
        contentView = new FrameLayout(context) {
            @Override
            protected void dispatchDraw(@NonNull Canvas canvas) {
                super.dispatchDraw(canvas);
                glassEngine.invalidate();
            }
        };
        contentView.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundGray));

        listView = new UniversalRecyclerView(this, this::fillItems, this::onClick, (item, view, position, x, y) -> handleItemLongClick(listView, item, view, position, x, y));
        listView.setCaptureSectionsDecoratorAllowed(true);
        listView.adapter.setApplyBackground(false);
        listView.setSections();
        listView.setSectionsDrawBackground(true);
        listView.setClipToPadding(false);
        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                updateActionBarVisible();
            }
        });
        contentView.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.FILL));

        actionBarFadeView = new View(context);
        actionBarFadeView.setBackground(getBaseSimpleGlass().createActionBarFadeDrawable(actionBarFadeView));
        contentView.addView(actionBarFadeView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 0, Gravity.TOP));

        actionBarContainer = new FrameLayout(context);
        actionBarContainer.addView(getHeader(), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.FILL_HORIZONTAL | Gravity.BOTTOM));
        contentView.addView(actionBarContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.FILL_HORIZONTAL | Gravity.TOP));

        updateActionBarFadeHeight(Math.max(mSystemInsets.top, AndroidUtilities.statusBarHeight));
        updateActionBarVisible(true, false);

        fragmentView = contentView;
        ViewCompat.setOnApplyWindowInsetsListener(fragmentView, this::onInsetsInternal);
        return contentView;
    }

    protected boolean handleItemLongClick(UniversalRecyclerView list, UItem item, View view, int position, float x, float y) {
        if (onLongClick(item, view, position, x, y)) {
            return true;
        }
        var slug = item.slug;
        var key = getKey();
        if (key != null && item.enabled && !TextUtils.isEmpty(slug)) {
            ItemOptions.makeOptions(this, view)
                    .setScrimViewBackground(list.getClipBackground(view))
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
    }

    protected boolean isSearchFieldVisible() {
        return searchItem != null && searchItem.isSearchFieldVisible2();
    }

    @Override
    public boolean onBackPressed(boolean invoked) {
        if (isSearchFieldVisible()) {
            if (invoked && actionBar != null) actionBar.closeSearchField();
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
                    firstChild.getY() + firstChild.getHeight() < getHeader().getHeight()
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
            setActionBarVisibilityFactor(visible ? 1.0f : 0.0f);
        } else {
            actionBarVisibleAnimator = ValueAnimator.ofFloat(actionBarFadeView.getAlpha(), visible ? 1.0f : 0.0f);
            actionBarVisibleAnimator.addUpdateListener(a -> setActionBarVisibilityFactor((float) a.getAnimatedValue()));
            actionBarVisibleAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
            actionBarVisibleAnimator.setDuration(420);
            actionBarVisibleAnimator.start();
        }
    }

    private void setActionBarVisibilityFactor(float factor) {
        if (!needActionBarPadding()) {
            if (simpleActionBar != null) {
                simpleActionBar.setTitlesAlpha(factor);
            } else {
                actionBar.getTitlesContainer().setAlpha(factor);
            }
        }
        actionBarFadeView.setAlpha(factor);
    }

    private void updateActionBarFadeHeight(int safeTop) {
        if (actionBarFadeView == null) return;
        final int height = safeTop + ActionBar.getCurrentActionBarHeight();
        ViewGroup.LayoutParams params = actionBarFadeView.getLayoutParams();
        if (params.height != height) {
            params.height = height;
            actionBarFadeView.setLayoutParams(params);
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
        actionBar.setOccupyStatusBar(true);
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

    protected View getHeader() {
        return simpleActionBar != null ? simpleActionBar : actionBar;
    }

    @Override
    public SimpleActionBar createSimpleActionBar(Context context) {
        final SimpleActionBar simpleActionBar = super.createSimpleActionBar(context);
        simpleActionBar.setTitle(getTitle(), null, true);
        simpleActionBar.setBackButtonImage(R.drawable.ic_ab_back);
        simpleActionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });
        return simpleActionBar;
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
        if (TextUtils.isEmpty(slug)) {
            return false;
        }

        try {
            for (var field : DeeplinkHelper.DeepLinksRepo.class.getDeclaredFields()) {
                if (field.getType() != String.class) {
                    continue;
                }

                if (!Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                Object value = field.get(null);

                if (slug.equals(value)) {
                    CherrygramLogger.d(() ->
                            "DeepLink match: slug=" + slug + ", field=" + field.getName()
                    );
                    return true;
                }
            }
        } catch (IllegalAccessException e) {
            CherrygramLogger.e(e);
        }

        CherrygramLogger.d(() -> "DeepLink NO match: slug=" + slug);
        return false;
    }

    @Override
    public boolean isSupportEdgeToEdge() {
        return true;
    }

    @Override
    public void onInsets(int left, int top, int right, int bottom) {
        final int safeTop = Math.max(top, AndroidUtilities.statusBarHeight);

        if (listView != null && !needActionBarPadding()) {
            listView.setPadding(listView.getPaddingLeft(), safeTop + dp(16), listView.getPaddingRight(), listView.getPaddingBottom());
        }
        updateActionBarFadeHeight(safeTop);
        super.onInsets(left, safeTop, right, bottom);
    }

    @Override
    protected boolean useFadeDrawableForActionBar() {
        return false;
    }

    @Nullable
    @Override
    protected RecyclerListView getListViewForSimpleGlass() {
        return listView;
    }

}