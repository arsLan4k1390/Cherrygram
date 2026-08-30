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
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.IconBackgroundColors;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.SettingsActivity;

import java.util.ArrayList;

import kotlin.Pair;
import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig;
import uz.unnarsx.cherrygram.core.firebase.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.core.helpers.DeeplinkHelper;
import uz.unnarsx.cherrygram.helpers.ui.PopupHelper;
import uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper;
import uz.unnarsx.cherrygram.preferences.helpers.TelegramSettingsHelper;

public class AppearancePreferencesEntry extends BaseCGPreferencesEntry {

    private final int centerTitleRow = 1;
    private final int hideSearchBar = 2;
    private final int snowflakesRow = 3;

    private final int iconPackRow = 4;
    private final int oneUISwitchesRow = 5;
    private final int disableDividersRow = 6;

    private final int foldersRow = 7;
    private final int bottomTabsRow = 8;
    private final int messagesAndProfilesRow = 9;

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("appearance_preferences_screen");
        return getString(R.string.AP_Header_Appearance);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(getString(R.string.AP_Header)));
        items.add(SettingsHelper.asSwitchCG(centerTitleRow, getString(R.string.AP_CenterTitle))
                .setChecked(CherrygramAppearanceConfig.INSTANCE.getCenterTitle())
        );
        items.add(SettingsHelper.asSwitchCG(hideSearchBar, getString(R.string.AP_HideSearchBar))
                .setChecked(CherrygramAppearanceConfig.INSTANCE.getHideSearchFiled())
        );
        items.add(SettingsHelper.asSwitchCG(snowflakesRow, getString(R.string.CP_Snowflakes_Header))
                .setChecked(CherrygramAppearanceConfig.INSTANCE.getDrawSnowInActionBar())
        );
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.AP_Header_Appearance)));
        items.add(UItem.asButton(iconPackRow, getString(R.string.AP_IconReplacements), getIconPackValueText()));
        items.add(SettingsHelper.asSwitchCG(oneUISwitchesRow, getString(R.string.AP_OneUI_Switch_Style))
                .setChecked(CherrygramAppearanceConfig.INSTANCE.getOneUI_SwitchStyle())
        );
        items.add(SettingsHelper.asSwitchCG(disableDividersRow, getString(R.string.AP_DisableDividers))
                .setChecked(CherrygramAppearanceConfig.INSTANCE.getDisableDividers())
        );
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.LocalMiscellaneousCache)));
        items.add(
                SettingsActivity.SettingCell.Factory.of(
                        foldersRow,
                        IconBackgroundColors.BLUE_ALT.top, IconBackgroundColors.BLUE_ALT.bottom,
                        R.drawable.settings_folders_filled_solar,
                        getString(R.string.CP_Filters_Header),
                        getString(R.string.CGP_Folders_Desc),
                        true
                )
        );
        items.add(
                SettingsActivity.SettingCell.Factory.of(
                        bottomTabsRow,
                        IconBackgroundColors.ORANGE_DEEP.top, IconBackgroundColors.ORANGE_DEEP.bottom,
                        R.drawable.settings_reorder_filled_solar,
                        getString(R.string.CP_MainTabs_Header),
                        getString(R.string.CGP_BottomTabs_Desc),
                        true
                )
        );
        Pair<Integer, Integer> colors = TelegramSettingsHelper.Helper.INSTANCE.getProfileButtonColor(getUserConfig().getCurrentUser(), true);
        items.add(
                SettingsActivity.SettingCell.Factory.of(
                        messagesAndProfilesRow,
                        Theme.isCurrentThemeDay() ? colors.getSecond() : colors.getFirst(),
                        Theme.isCurrentThemeDay() ? colors.getFirst() : colors.getSecond(),
                        R.drawable.settings_customize_filled_solar,
                        getString(R.string.CP_ProfileReplyBackground),
                        getString(R.string.CGP_MessagesProfiles_Desc),
                        true
                )
        );
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == centerTitleRow) {
            CherrygramAppearanceConfig.INSTANCE.setCenterTitle(!CherrygramAppearanceConfig.INSTANCE.getCenterTitle());
            SettingsHelper.updateCheckState(view, CherrygramAppearanceConfig.INSTANCE.getCenterTitle());

            getParentLayout().rebuildAllFragmentViews(true, true);
        } else  if (item.id == hideSearchBar) {
            CherrygramAppearanceConfig.INSTANCE.setHideSearchFiled(!CherrygramAppearanceConfig.INSTANCE.getHideSearchFiled());
            SettingsHelper.updateCheckState(view, CherrygramAppearanceConfig.INSTANCE.getHideSearchFiled());

            getNotificationCenter().postNotificationName(NotificationCenter.cgUpdateSearchFiledVisibility);
        } else if (item.id == snowflakesRow) {
            CherrygramAppearanceConfig.INSTANCE.setDrawSnowInActionBar(!CherrygramAppearanceConfig.INSTANCE.getDrawSnowInActionBar());
            SettingsHelper.updateCheckState(view, CherrygramAppearanceConfig.INSTANCE.getDrawSnowInActionBar());

            showRestartBulletin();
        } else if (item.id == iconPackRow) {
            ArrayList<String> configStringKeys = new ArrayList<>();
            ArrayList<Integer> configValues = new ArrayList<>();

            configStringKeys.add(getString(R.string.Default));
            configValues.add(CherrygramAppearanceConfig.ICON_REPLACE_NONE);

            configStringKeys.add(getString(R.string.AP_IconReplacement_Solar));
            configValues.add(CherrygramAppearanceConfig.ICON_REPLACE_SOLAR);

            PopupHelper.show(configStringKeys, getString(R.string.AP_IconReplacements), configValues.indexOf(CherrygramAppearanceConfig.INSTANCE.getIconReplacement()), getContext(), i -> {
                CherrygramAppearanceConfig.INSTANCE.setIconReplacement(configValues.get(i));
                SettingsHelper.updateButtonValue(view, getIconPackValueText());

                if (getParentActivity() instanceof LaunchActivity) {
                    ((LaunchActivity) getParentActivity()).reloadResources();
                }
                Theme.reloadAllResources(getContext() != null ? getContext() : ApplicationLoader.applicationContext);

                getParentLayout().rebuildAllFragmentViews(false, false);
            });
        } else if (item.id == oneUISwitchesRow) {
            CherrygramAppearanceConfig.INSTANCE.setOneUI_SwitchStyle(!CherrygramAppearanceConfig.INSTANCE.getOneUI_SwitchStyle());
            SettingsHelper.updateCheckState(view, CherrygramAppearanceConfig.INSTANCE.getOneUI_SwitchStyle());

            updateRows(true);
        } else if (item.id == disableDividersRow) {
            CherrygramAppearanceConfig.INSTANCE.setDisableDividers(!CherrygramAppearanceConfig.INSTANCE.getDisableDividers());
            SettingsHelper.updateCheckState(view, CherrygramAppearanceConfig.INSTANCE.getDisableDividers());

            Theme.applyCommonTheme();
            updateRows(true);
        } else if (item.id == foldersRow) {
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
