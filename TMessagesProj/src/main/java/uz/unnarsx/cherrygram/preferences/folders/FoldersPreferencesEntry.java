/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.preferences.folders;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BotWebViewVibrationEffect;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig;
import uz.unnarsx.cherrygram.core.crashlytics.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.core.ui.CGBulletinCreator;
import uz.unnarsx.cherrygram.donates.DonatesManager;
import uz.unnarsx.cherrygram.helpers.ui.PopupHelper;
import uz.unnarsx.cherrygram.preferences.folders.cells.FoldersPreviewCell;

public class FoldersPreferencesEntry extends UniversalFragment {

    protected FoldersPreviewCell foldersPreviewCell;

    private final int foldersAtBottomRow = 1;
    private final int folderNameAppHeaderRow = 2;
    private final int hideAllChatsTabRow = 3;

    private final int hideCounterRow = 4;
    private final int tabIconTypeRow = 5;
    private final int addStrokeRow = 6;


    @Override
    public View createView(Context context) {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("folders_preferences_screen");
        setMD3(true);
        return super.createView(context);
    }

    @Override
    protected CharSequence getTitle() {
        return getString(R.string.CP_Filters_Header);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        foldersPreviewCell = new FoldersPreviewCell(getContext());
        foldersPreviewCell.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        items.add(UItem.asCustomWithBackground(foldersPreviewCell));
        items.add(UItem.asShadow(null));

        items.add(
                UItem.asCheck(
                        hideAllChatsTabRow,
                        getString(R.string.CP_NewTabs_RemoveAllChats)
                ).setChecked(CherrygramAppearanceConfig.INSTANCE.getTabsHideAllChats())
        );

        items.add(
                UItem.asCheck(
                        hideCounterRow,
                        getString(R.string.CP_NewTabs_NoCounter)
                ).setChecked(CherrygramAppearanceConfig.INSTANCE.getTabsNoUnread())
        );

        items.add(
                UItem.asButton(
                        tabIconTypeRow,
                        getString(R.string.AP_Tab_Style),
                        getTabModeValue()
                )
        );

        items.add(
                UItem.asCheck(
                        addStrokeRow,
                        getString(R.string.AP_Tab_Style_Stroke)
                ).setChecked(CherrygramAppearanceConfig.INSTANCE.getTabStyleStroke())
        );

        items.add(UItem.asShadow(null));

        items.add(
                UItem.asButtonCheck(
                        folderNameAppHeaderRow,
                        getString(R.string.AP_FolderNameInHeader),
                        getString(R.string.AP_FolderNameInHeader_Desc)
                ).setChecked(CherrygramAppearanceConfig.INSTANCE.getFolderNameInHeader())
        );

        items.add(
                UItem.asCheck(
                        foldersAtBottomRow,
                        getString(R.string.AP_FoldersAtBottom)
                ).setChecked(CherrygramAppearanceConfig.INSTANCE.getFoldersAtBottom()).setLocked(!DonatesManager.INSTANCE.didUserDonateForFeature())
        );

    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == foldersAtBottomRow) {
            if (!DonatesManager.INSTANCE.didUserDonateForFeature()) {
                AndroidUtilities.shakeViewSpring(view);
                BotWebViewVibrationEffect.APP_ERROR.vibrate();
                CGBulletinCreator.INSTANCE.createRequireDonateBulletin(this);
                return;
            }

            CherrygramAppearanceConfig.INSTANCE.setFoldersAtBottom(!CherrygramAppearanceConfig.INSTANCE.getFoldersAtBottom());
            ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getFoldersAtBottom());

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        } else if (item.id == folderNameAppHeaderRow) {
            CherrygramAppearanceConfig.INSTANCE.setFolderNameInHeader(!CherrygramAppearanceConfig.INSTANCE.getFolderNameInHeader());
            ((NotificationsCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getFolderNameInHeader());

            parentLayout.rebuildAllFragmentViews(false, false);

            getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
        } else if (item.id == hideAllChatsTabRow) {
            CherrygramAppearanceConfig.INSTANCE.setTabsHideAllChats(!CherrygramAppearanceConfig.INSTANCE.getTabsHideAllChats());
            ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getTabsHideAllChats());

            foldersPreviewCell.updateAllChatsTabName(true);

            parentLayout.rebuildAllFragmentViews(false, false);

            getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
            getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
        } else if (item.id == hideCounterRow) {
            CherrygramAppearanceConfig.INSTANCE.setTabsNoUnread(!CherrygramAppearanceConfig.INSTANCE.getTabsNoUnread());
            ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getTabsNoUnread());

            foldersPreviewCell.updateTabCounter(true);

            parentLayout.rebuildAllFragmentViews(false, false);

            getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
        } else if (item.id == tabIconTypeRow) {
            ArrayList<String> configStringKeys = new ArrayList<>();
            ArrayList<Integer> configValues = new ArrayList<>();

            configStringKeys.add(getString(R.string.CG_FoldersTypeIconsTitles));
            configValues.add(CherrygramAppearanceConfig.TAB_TYPE_MIX);

            configStringKeys.add(getString(R.string.CG_FoldersTypeTitles));
            configValues.add(CherrygramAppearanceConfig.TAB_TYPE_TEXT);

            configStringKeys.add(getString(R.string.CG_FoldersTypeIcons));
            configValues.add(CherrygramAppearanceConfig.TAB_TYPE_ICON);

            PopupHelper.show(configStringKeys, getString(R.string.AP_Tab_Style), configValues.indexOf(CherrygramAppearanceConfig.INSTANCE.getTabMode()), getContext(), i -> {
                CherrygramAppearanceConfig.INSTANCE.setTabMode(configValues.get(i));
                ((TextCell) view).setValue(getTabModeValue(), true);

                foldersPreviewCell.updateTabIcons(true);
                foldersPreviewCell.updateTabTitle(true);

                parentLayout.rebuildAllFragmentViews(false, false);

                getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
            });
        } else if (item.id == addStrokeRow) {
            CherrygramAppearanceConfig.INSTANCE.setTabStyleStroke(!CherrygramAppearanceConfig.INSTANCE.getTabStyleStroke());
            ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getTabStyleStroke());

            foldersPreviewCell.invalidate();
            parentLayout.rebuildAllFragmentViews(false, false);
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }

    private String getTabModeValue() {
        return switch (CherrygramAppearanceConfig.INSTANCE.getTabMode()) {
            case CherrygramAppearanceConfig.TAB_TYPE_MIX -> getString(R.string.CG_FoldersTypeIconsTitles);
            case CherrygramAppearanceConfig.TAB_TYPE_ICON -> getString(R.string.CG_FoldersTypeIcons);
            default -> getString(R.string.CG_FoldersTypeTitles);
        };
    }

}
