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

import static uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper.applyNewSpan;
import static uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper.applyProSpan;

import android.content.Context;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.IconBackgroundColors;
import org.telegram.ui.Components.ItemOptions;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import org.telegram.ui.SettingsActivity;

import java.util.ArrayList;

import kotlin.Pair;
import uz.unnarsx.cherrygram.chats.CGChatMenuInjector;
import uz.unnarsx.cherrygram.core.configs.CherrygramExperimentalConfig;
import uz.unnarsx.cherrygram.core.configs.CherrygramFirebaseConfig;
import uz.unnarsx.cherrygram.core.firebase.crashlytics.Crashlytics;
import uz.unnarsx.cherrygram.core.firebase.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper;
import uz.unnarsx.cherrygram.core.helpers.DeeplinkHelper;
import uz.unnarsx.cherrygram.core.helpers.backup.BackupHelper;
import uz.unnarsx.cherrygram.preferences.helpers.TelegramSettingsHelper;

public class CGPreferencesEntry extends UniversalFragment {

    private final int generalRow = 1;
    private final int appearanceRow = 2;
    private final int chatsRow = 3;
    private final int cameraRow = 4;
    private final int experimentalRow = 5;
    private final int privacyRow = 6;
    private final int aboutRow = 7;

    private final int supportRow = 8;
    private final int proxyRow = 9;

    private final int alternativeSupportRow = 10;

    public ActionBarMenuItem otherItem;

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("main_preferences_screen");
        return getString(R.string.CGP_AdvancedSettings);
    }

    @Override
    public View createView(Context context) {
        setMD3(true);
        setGilroy(true);

        final ActionBarMenu menu = actionBar.createMenu();
        otherItem = menu.addItem(1, R.drawable.ic_ab_other);
        otherItem.setOnClickListener(view -> showItemOptions(otherItem));

        return super.createView(context);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(getString(R.string.Settings)));
        items.add(
                SettingsActivity.SettingCell.Factory.of(
                        generalRow,
                        IconBackgroundColors.ORANGE_DEEP.bottom, IconBackgroundColors.RED.bottom,
                        R.drawable.settings_filled_solar,
                        getString(R.string.AP_Header_General),
                        getString(R.string.CGP_General_Desc),
                        true
                )
        );
        Pair<Integer, Integer> colors = TelegramSettingsHelper.Helper.INSTANCE.getProfileButtonColor(getUserConfig().getCurrentUser(), true);
        items.add(
                SettingsActivity.SettingCell.Factory.of(
                        appearanceRow,
                        Theme.isCurrentThemeDay() ? colors.getSecond() : colors.getFirst(),
                        Theme.isCurrentThemeDay() ? colors.getFirst() : colors.getSecond(),
                        R.drawable.settings_palette_filled_solar,
                        getString(R.string.AP_Header_Appearance),
                        getString(R.string.CGP_Appearance_Desc),
                        true
                )
        );
        items.add(
                SettingsActivity.SettingCell.Factory.of(
                        chatsRow, IconBackgroundColors.ORANGE.top, IconBackgroundColors.ORANGE.bottom,
                        R.drawable.settings_chats_filled_solar,
                        getString(R.string.FilterChats),
                        getString(R.string.CGP_Chats_Desc),
                        true
                )
        );
        items.add(
                SettingsActivity.SettingCell.Factory.of(
                        cameraRow,
                        0xFFE54C7F, 0xFFA33156,
                        R.drawable.settings_camera_filled_solar,
                        getString(R.string.CP_Category_Camera),
                        getString(R.string.CGP_Camera_Desc),
                        true
                )
        );
        if (CherrygramExperimentalConfig.INSTANCE.getUse_CG_OOMHandler()) {
            items.add(
                    SettingsActivity.SettingCell.Factory.of(
                            experimentalRow,
                            IconBackgroundColors.PURPLE.top, IconBackgroundColors.PURPLE.bottom,
                            R.drawable.settings_tube_filled_solar,
                            applyNewSpan(getString(R.string.EP_Category_Experimental)),
                            getString(R.string.CGP_Experimental_Desc),
                            true
                    )
            );
        }
        items.add(
                SettingsActivity.SettingCell.Factory.of(
                        privacyRow,
                        IconBackgroundColors.CYAN.top, IconBackgroundColors.CYAN.bottom,
                        R.drawable.settings_privacy_solar_filled,
                        getString(R.string.SettingsPrivacySecurity),
                        getString(R.string.CGP_Privacy_Desc),
                        true
                )
        );
        items.add(
                SettingsActivity.SettingCell.Factory.of(
                        aboutRow,
                        0xFFE54C7F, 0xFFA33156,
                        R.drawable.settings_info_filled_solar,
                        getString(R.string.CGP_Header_About),
                        getString(R.string.CGP_Header_About_Desc),
                        true
                )
        );
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.DP_Support)));
        items.add(
                SettingsActivity.SettingCell.Factory.of(
                        supportRow,
                        0xFFB659FF, 0xFF617CFF,
                        R.drawable.settings_support_filled_solar,
                        applyProSpan(getString(R.string.DP_DonateBadge), getResourceProvider())
                )
        );
        if (!getConnectionsManager().isTestBackend()) {
            items.add(
                    SettingsActivity.SettingCell.Factory.of(
                            alternativeSupportRow,
                            0xFFF6538A, 0xFF581668,
                            R.drawable.settings_stars,
                            applyNewSpan(getString(R.string.AS_Header))
                    )
            );
        }
        if (CherrygramFirebaseConfig.INSTANCE.getShowProxyInSettings() && CGChatMenuInjector.INSTANCE.showProxyButton()) {
            items.add(UItem.asShadow(null));
            items.add(
                    SettingsActivity.SettingCell.Factory.of(
                            proxyRow,
                            IconBackgroundColors.BLUE_DEEP.top, IconBackgroundColors.BLUE_DEEP.bottom,
                            R.drawable.settings_language,
                            getString(R.string.Proxy)
                    )
            );
        }
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == generalRow) {
            CherrygramPreferencesNavigator.INSTANCE.createGeneral(this);
        } else if (item.id == appearanceRow) {
            CherrygramPreferencesNavigator.INSTANCE.createAppearance(this);
        } else if (item.id == chatsRow) {
            CherrygramPreferencesNavigator.INSTANCE.createChats(this);
        } else if (item.id == cameraRow) {
            CherrygramPreferencesNavigator.INSTANCE.createCamera(this);
        } else if (item.id == experimentalRow) {
            CherrygramPreferencesNavigator.INSTANCE.createExperimental(this);
        } else if (item.id == privacyRow) {
            CherrygramPreferencesNavigator.INSTANCE.createPrivacy(this);
        } else if (item.id == aboutRow) {
            CherrygramPreferencesNavigator.INSTANCE.createAbout(this);
        } else if (item.id == supportRow) {
            CherrygramPreferencesNavigator.INSTANCE.createDonate(this);
        } else if (item.id == proxyRow) {
            Browser.openUrl(getContext(), CherrygramFirebaseConfig.INSTANCE.getProxyURL());
        } else if (item.id == alternativeSupportRow) {
            CherrygramPreferencesNavigator.INSTANCE.createAlternativeSupport(this);
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        if (item.id == generalRow) {
            AndroidUtilities.addToClipboard("tg://" + DeeplinkHelper.DeepLinksRepo.CG_General);
            return true;
        } else if (item.id == appearanceRow) {
            AndroidUtilities.addToClipboard("tg://" + DeeplinkHelper.DeepLinksRepo.CG_Appearance);
            return true;
        } else if (item.id == chatsRow) {
            AndroidUtilities.addToClipboard("tg://" + DeeplinkHelper.DeepLinksRepo.CG_Chats);
            return true;
        } else if (item.id == cameraRow) {
            AndroidUtilities.addToClipboard("tg://" + DeeplinkHelper.DeepLinksRepo.CG_Camera);
            return true;
        } else if (item.id == experimentalRow) {
            AndroidUtilities.addToClipboard("tg://" + DeeplinkHelper.DeepLinksRepo.CG_Experimental);
            return true;
        } else if (item.id == privacyRow) {
            AndroidUtilities.addToClipboard("tg://" + DeeplinkHelper.DeepLinksRepo.CG_Privacy);
            return true;
        } else if (item.id == supportRow) {
            AndroidUtilities.addToClipboard("tg://" + DeeplinkHelper.DeepLinksRepo.CG_Support_Force);
            return true;
        } else if (item.id == aboutRow) {
            AndroidUtilities.addToClipboard("tg://" + DeeplinkHelper.DeepLinksRepo.CG_About);
            return true;
        } else if (item.id == alternativeSupportRow) {
            AndroidUtilities.addToClipboard("tg://" + DeeplinkHelper.DeepLinksRepo.CG_Alternative_Support);
            return true;
        }
        return false;
    }

    private void showItemOptions(View button) {
        ItemOptions o = ItemOptions.makeOptions(this, button);

        o.add(R.drawable.msg_instant_link_solar, getString(R.string.CG_ExportSettings), () -> BackupHelper.INSTANCE.backupSettings(this));
        o.add(R.drawable.msg_photo_settings_solar, getString(R.string.CG_ImportSettings), () -> BackupHelper.INSTANCE.importSettings(this));
        o.addGap();
        o.add(R.drawable.bug_solar, getString(R.string.CG_CopyReportDetails), () -> {
            AndroidUtilities.addToClipboard(Crashlytics.getReportMessage() + "\n\n#bug");
            BulletinFactory.of(this).createErrorBulletin(getString(R.string.CG_ReportDetailsCopied))
                    .setDuration(Bulletin.DURATION_SHORT)
                    .show();
        });

        o.addSpaceGap();

        o.add(R.drawable.msg_retry_solar, getString(R.string.CG_Restart), () -> AppRestartHelper.restartApp(getContext()));

        o.setBlur(false);
        o.setDrawScrim(false);
        o.translate(0F, -dp(48F));
        o.show();
    }

}
