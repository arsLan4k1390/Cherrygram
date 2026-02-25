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
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig;
import uz.unnarsx.cherrygram.core.crashlytics.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.core.helpers.DeeplinkHelper;
import uz.unnarsx.cherrygram.core.ui.CGBulletinCreator;
import uz.unnarsx.cherrygram.helpers.ui.PopupHelper;

public class AppearancePreferencesEntry extends UniversalFragment {

    private final int centerTitleRow = 1;
    private final int snowflakesRow = 2;

    private final int iconPackRow = 3;
    private final int oneUISwitchesRow = 4;
    private final int disableDividersRow = 5;

    private final int foldersRow = 6;
    private final int bottomTabsRow = 7;
    private final int messagesAndProfilesRow = 8;

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("appearance_preferences_screen");
        return getString(R.string.AP_Header_Appearance);
    }

    @Override
    public View createView(Context context) {
        setMD3(true);
        return super.createView(context);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(getString(R.string.AP_Header)));
        items.add(
                UItem.asCheck(
                        centerTitleRow,
                        getString(R.string.AP_CenterTitle)
                ).setChecked(CherrygramAppearanceConfig.INSTANCE.getCenterTitle())
        );
        items.add(
                UItem.asCheck(
                        snowflakesRow,
                        getString(R.string.CP_Snowflakes_Header)
                ).setChecked(CherrygramAppearanceConfig.INSTANCE.getDrawSnowInActionBar())
        );
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.AP_Header_Appearance)));
        items.add(
                UItem.asButton(
                        iconPackRow,
                        getString(R.string.AP_IconReplacements),
                        getIconPackValueText()
                )
        );
        items.add(
                UItem.asCheck(
                        oneUISwitchesRow,
                        getString(R.string.AP_OneUI_Switch_Style)
                ).setChecked(CherrygramAppearanceConfig.INSTANCE.getOneUI_SwitchStyle())
        );
        items.add(
                UItem.asCheck(
                        disableDividersRow,
                        getString(R.string.AP_DisableDividers)
                ).setChecked(CherrygramAppearanceConfig.INSTANCE.getDisableDividers())
        );
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.LocalMiscellaneousCache)));
        items.add(
                UItem.asButton(
                        foldersRow,
                        R.drawable.msg_folders,
                        getString(R.string.CP_Filters_Header)
                )
        );
        items.add(
                UItem.asButton(
                        bottomTabsRow,
                        R.drawable.tabs_reorder,
                        getString(R.string.CP_MainTabs_Header)
                )
        );
        items.add(
                UItem.asButton(
                        messagesAndProfilesRow,
                        R.drawable.msg_customize,
                        getString(R.string.CP_ProfileReplyBackground)
                )
        );
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == centerTitleRow) {
            CherrygramAppearanceConfig.INSTANCE.setCenterTitle(!CherrygramAppearanceConfig.INSTANCE.getCenterTitle());
            ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getCenterTitle());

            getParentLayout().rebuildAllFragmentViews(true, true);
        } else if (item.id == snowflakesRow) {
            CherrygramAppearanceConfig.INSTANCE.setDrawSnowInActionBar(!CherrygramAppearanceConfig.INSTANCE.getDrawSnowInActionBar());
            ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getDrawSnowInActionBar());

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        } else if (item.id == iconPackRow) {
            ArrayList<String> configStringKeys = new ArrayList<>();
            ArrayList<Integer> configValues = new ArrayList<>();

            configStringKeys.add(getString(R.string.Default));
            configValues.add(CherrygramAppearanceConfig.ICON_REPLACE_NONE);

            configStringKeys.add(getString(R.string.AP_IconReplacement_Solar));
            configValues.add(CherrygramAppearanceConfig.ICON_REPLACE_SOLAR);

            PopupHelper.show(configStringKeys, getString(R.string.AP_IconReplacements), configValues.indexOf(CherrygramAppearanceConfig.INSTANCE.getIconReplacement()), getContext(), i -> {
                CherrygramAppearanceConfig.INSTANCE.setIconReplacement(configValues.get(i));
                ((TextCell) view).setValue(getIconPackValueText(), true);

                if (getParentActivity() instanceof LaunchActivity) {
                    ((LaunchActivity) getParentActivity()).reloadResources();
                }
                Theme.reloadAllResources(getParentActivity());

                getParentLayout().rebuildAllFragmentViews(false, false);
            });
        } else if (item.id == oneUISwitchesRow) {
            CherrygramAppearanceConfig.INSTANCE.setOneUI_SwitchStyle(!CherrygramAppearanceConfig.INSTANCE.getOneUI_SwitchStyle());
            ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getOneUI_SwitchStyle());

            listView.adapter.update(true);
        } else if (item.id == disableDividersRow) {
            CherrygramAppearanceConfig.INSTANCE.setDisableDividers(!CherrygramAppearanceConfig.INSTANCE.getDisableDividers());
            ((TextCheckCell) view).setChecked(CherrygramAppearanceConfig.INSTANCE.getDisableDividers());

            Theme.applyCommonTheme();
            listView.adapter.update(true);
        }  else if (item.id == foldersRow) {
            CherrygramPreferencesNavigator.INSTANCE.createFoldersPrefs(this);
        } else if (item.id == bottomTabsRow) {
            CherrygramPreferencesNavigator.INSTANCE.createTabs(this);
        } else if (item.id == messagesAndProfilesRow) {
            CherrygramPreferencesNavigator.INSTANCE.createMessagesAndProfiles(this);
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        if (item.id == foldersRow) {
            AndroidUtilities.addToClipboard("tg://" + DeeplinkHelper.DeepLinksRepo.CG_Folders);
            return true;
        } else if (item.id == bottomTabsRow) {
            AndroidUtilities.addToClipboard("tg://" + DeeplinkHelper.DeepLinksRepo.CG_Tabs);
            return true;
        } else if (item.id == messagesAndProfilesRow) {
            AndroidUtilities.addToClipboard("tg://" + DeeplinkHelper.DeepLinksRepo.CG_Messages_And_Profiles);
            return true;
        }
        return false;
    }

    private String getIconPackValueText()  {
        return switch (CherrygramAppearanceConfig.INSTANCE.getIconReplacement()) {
            case CherrygramAppearanceConfig.ICON_REPLACE_SOLAR -> getString(R.string.AP_IconReplacement_Solar);
            default -> getString(R.string.Default);
        };
    }

}
