/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.preferences.tabs;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig;
import uz.unnarsx.cherrygram.core.crashlytics.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.core.ui.CGBulletinCreator;
import uz.unnarsx.cherrygram.core.ui.mainTabs.MainTabsManager;

public class MainTabsPreferencesEntry extends UniversalFragment {

    private final int enableTabsRow = 1;
    private final int tabsPreviewRow = 2;
    private final int openSettingsBySwipeRow = 3;
    private final int showTabTitleRow = 4;

    private MainTabsPreviewCell tabsView;
    private ArrayList<MainTabsManager.Tab> tabs;
    private ArrayList<MainTabsManager.Tab> initialTabs;

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("tabs_preferences_screen");
        return getString(R.string.CP_MainTabs_Header);
    }

    @Override
    public View createView(Context context) {
        setMD3(true);
        return super.createView(context);
    }

    @Override
    public boolean onFragmentCreate() {
        initialTabs = new ArrayList<>();
        for (MainTabsManager.Tab t : MainTabsManager.INSTANCE.getAllTabs()) {
            initialTabs.add(new MainTabsManager.Tab(t.getType(), t.enabled));
        }

        tabs = new ArrayList<>(MainTabsManager.INSTANCE.getAllTabs());

        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        checkSaveTabs();
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(getString(R.string.CP_MainTabs_Layout)));

        if (CherrygramAppearanceConfig.INSTANCE.getShowMainTabs()) {
            UItem enableTabs = UItem.asButtonCheck(
                    enableTabsRow,
                    getString(R.string.CP_MainTabs_ShowTabs),
                    getString(R.string.CP_MainTabs_DoubleTap_Desc)
            ).setChecked(CherrygramAppearanceConfig.INSTANCE.getShowMainTabs());
            enableTabs.hideDivider = true;
            items.add(enableTabs);
        } else {
            items.add(
                    UItem.asCheck(
                            enableTabsRow,
                            getString(R.string.CP_MainTabs_ShowTabs)
                    ).setChecked(CherrygramAppearanceConfig.INSTANCE.getShowMainTabs())
            );

            items.add(
                    UItem.asButtonCheck(
                            openSettingsBySwipeRow,
                            getString(R.string.CP_MainTabs_OpenSettings),
                            getString(R.string.CP_MainTabs_OpenSettings_Desc)
                    ).setChecked(CherrygramAppearanceConfig.INSTANCE.getOpenSettingsBySwipe())
            );
        }

        PreviewCell previewContainer = new PreviewCell(getContext());

        tabsView = new MainTabsPreviewCell(getContext());
        tabsView.setEditMode(true);
        tabsView.setTabs(tabs, getContext(), getResourceProvider(), currentAccount, true);

        previewContainer.addView(tabsView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.CENTER, dp(5), 0, dp(5), 0));

        if (CherrygramAppearanceConfig.INSTANCE.getShowMainTabs()) {
            items.add(UItem.asCustomWithBackground(tabsPreviewRow, previewContainer, 58));
            UItem space = UItem.asSpaceCG(dp(12));
            space.id = -1;
            space.transparent = true;
            items.add(space);
            items.add(UItem.asShadow(getString(R.string.CP_MainTabs_Layout_Desc)));

            items.add(
                    UItem.asCheck(
                            showTabTitleRow,
                            getString(R.string.CP_MainTabs_ShowTabsTitle)
                    ).setChecked(CherrygramAppearanceConfig.INSTANCE.getShowMainTabsTitle())
            );
        }
    }

    private static class PreviewCell extends FrameLayout {
        private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();

        public PreviewCell(Context context) {
            super(context);
            setWillNotDraw(false);

            int color = Theme.getColor(Theme.key_switchTrack);
            backgroundPaint.setColor(ColorUtils.setAlphaComponent(color, 20));

            outlinePaint.setStyle(Paint.Style.STROKE);
            outlinePaint.setStrokeWidth(Math.max(2, dp(1f)));
            outlinePaint.setColor(ColorUtils.setAlphaComponent(color, 0x3F));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float w = getMeasuredWidth();
            float h = getMeasuredHeight();

            float radius = dp(50);

            float stroke = outlinePaint.getStrokeWidth() / 2;
            rect.set(stroke + dp(8), stroke, w - stroke - dp(8), h - stroke);

            canvas.drawRoundRect(rect, radius, radius, backgroundPaint);
            canvas.drawRoundRect(rect, radius, radius, outlinePaint);
        }
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == enableTabsRow) {
            CherrygramAppearanceConfig.INSTANCE.setShowMainTabs(!CherrygramAppearanceConfig.INSTANCE.getShowMainTabs());
            if (view instanceof NotificationsCheckCell) ((NotificationsCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getShowMainTabs());
            if (view instanceof TextCheckCell) ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getShowMainTabs());

            if (!CherrygramAppearanceConfig.INSTANCE.getShowMainTabs()) {
                CherrygramAppearanceConfig.INSTANCE.setMainTabsOrder("SETTINGS,CHATS,CONTACTS,!CALLS,!PROFILE,SEARCH");
            }
            listView.adapter.update(true);

            if (CherrygramAppearanceConfig.INSTANCE.getFoldersAtBottom()) CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        } else if (item.id == tabsPreviewRow) {
            /*if (MainTabsManager.getEnabledTabs().size() > 5) {
                CherrygramAppearanceConfig.INSTANCE.setShowMainTabsTitle(false);
                listView.adapter.update(true);
            }*/
        } else if (item.id == openSettingsBySwipeRow) {
            CherrygramAppearanceConfig.INSTANCE.setOpenSettingsBySwipe(!CherrygramAppearanceConfig.INSTANCE.getOpenSettingsBySwipe());
            ((NotificationsCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getOpenSettingsBySwipe());

            CherrygramAppearanceConfig.INSTANCE.setMainTabsOrder("SETTINGS,CHATS,CONTACTS,!CALLS,!PROFILE,SEARCH");
        } else if (item.id == showTabTitleRow) {
            CherrygramAppearanceConfig.INSTANCE.setShowMainTabsTitle(!CherrygramAppearanceConfig.INSTANCE.getShowMainTabsTitle());
            ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getShowMainTabsTitle());

            tabsView.removeAllViews();
            tabsView.setTabs(tabs, getContext(), getResourceProvider(), currentAccount, true);

            postUpdateTabsNotification();
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }


    @Override
    public boolean onBackPressed(boolean invoked) {
        checkSaveTabs();
        return super.onBackPressed(invoked);
    }

    private void checkSaveTabs() {
        MainTabsManager.INSTANCE.saveTabs(tabs);
//        if (MainTabsManager.getEnabledTabs().size() > 5) CherrygramAppearanceConfig.INSTANCE.setShowMainTabsTitle(false);

        if (!tabs.equals(initialTabs)) {
            postUpdateTabsNotification();
        }
    }

    private void postUpdateTabsNotification() {
        new Handler(Looper.getMainLooper()).postDelayed(() ->
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.cgTabsUpdated),
                200
        );
    }

}

