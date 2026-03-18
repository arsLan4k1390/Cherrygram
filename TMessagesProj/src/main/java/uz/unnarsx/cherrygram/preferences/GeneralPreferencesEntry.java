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
import android.content.Intent;
import android.os.Build;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.NotificationsService;
import org.telegram.messenger.R;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.core.crashlytics.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.core.ui.CGBulletinCreator;
import uz.unnarsx.cherrygram.helpers.ui.PopupHelper;
import uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper;

public class GeneralPreferencesEntry extends UniversalFragment {

    private final int springAnimationRow = 1;
    private final int actionbarCrossfadeRow = 2;
    private final int predictiveBackRow = 3;

    private final int silenceNonContactsRow = 4;
    private final int defaultNotificationIconRow = 5;
    private final int residentNotificationRow = 6;

    private final int hideStoriesRow = 7;
    private final int archiveStoriesRow = 8;

    private final int useSystemEmojiRow = 9;
    private final int useSystemFontsRow = 10;
    private final int tabledModeRow = 11;

    private final int downloadSpeedBoostRow = 12;
    private final int uploadSpeedBoostRow = 13;
    private final int slowNetworkMode = 14;

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("general_preferences_screen");
        return getString(R.string.AP_Header_General);
    }

    @Override
    public View createView(Context context) {
        setMD3(true);
        return super.createView(context);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(getString(R.string.LiteMode)));
        items.add(UItem.asButton(springAnimationRow, getString(R.string.EP_NavigationAnimation), getSpringValue()));
        if (CherrygramCoreConfig.INSTANCE.getSpringAnimation() == CherrygramCoreConfig.ANIMATION_SPRING) {
            items.add(SettingsHelper.asSwitchCG(actionbarCrossfadeRow, getString(R.string.EP_NavigationAnimationCrossfading))
                    .setChecked(CherrygramCoreConfig.INSTANCE.getActionbarCrossfade())
            );
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            items.add(SettingsHelper.asSwitchCG(predictiveBackRow, getString(R.string.CG_PredictiveBackAnimation))
                    .setChecked(CherrygramCoreConfig.INSTANCE.getPredictiveBack())
            );
        }
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.SettingsNotifications)));
        items.add(SettingsHelper.asSwitchCG(silenceNonContactsRow, getString(R.string.CP_SilenceNonContacts), getString(R.string.CP_SilenceNonContacts_Desc))
                .setChecked(CherrygramCoreConfig.INSTANCE.getSilenceNonContacts())
        );
        items.add(SettingsHelper.asSwitchCG(defaultNotificationIconRow, getString(R.string.AP_Old_Notification_Icon))
                .setChecked(CherrygramCoreConfig.INSTANCE.getOldNotificationIcon())
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            items.add(SettingsHelper.asSwitchCG(residentNotificationRow, getString(R.string.CG_ResidentNotification), getString(R.string.NotificationsService))
                    .setChecked(CherrygramCoreConfig.INSTANCE.getResidentNotification())
            );
        }
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.FilterStories)));
        items.add(SettingsHelper.asSwitchCG(hideStoriesRow, getString(R.string.CP_HideStories), getString(R.string.CP_HideStories_Desc))
                .setChecked(CherrygramCoreConfig.INSTANCE.getHideStories())
        );
        items.add(SettingsHelper.asTextDetail(archiveStoriesRow, R.drawable.msg_archive, getString(R.string.CP_ArchiveStories), getString(R.string.CP_ArchiveStories_Desc)));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.LocalMiscellaneousCache)));
        items.add(SettingsHelper.asSwitchCG(useSystemEmojiRow, getString(R.string.AP_SystemEmoji))
                .setChecked(CherrygramCoreConfig.INSTANCE.getSystemEmoji())
        );
        items.add(SettingsHelper.asSwitchCG(useSystemFontsRow, getString(R.string.AP_SystemFonts))
                .setChecked(CherrygramCoreConfig.INSTANCE.getSystemFonts())
        );
        items.add(UItem.asButton(tabledModeRow, getString(R.string.AP_Tablet_Mode), getTabletModeValue()));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.EP_Network)));
        items.add(UItem.asButton(downloadSpeedBoostRow, getString(R.string.EP_DownloadSpeedBoost), getDownloadSpeedBoostText()));
        items.add(SettingsHelper.asSwitchCG(uploadSpeedBoostRow, getString(R.string.EP_UploadloadSpeedBoost))
                .setChecked(CherrygramCoreConfig.INSTANCE.getUploadSpeedBoost())
        );
        items.add(SettingsHelper.asSwitchCG(slowNetworkMode, getString(R.string.EP_SlowNetworkMode))
                .setChecked(CherrygramCoreConfig.INSTANCE.getSlowNetworkMode())
        );
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == springAnimationRow) {
            ArrayList<String> configStringKeys = new ArrayList<>();
            ArrayList<Integer> configValues = new ArrayList<>();

            configStringKeys.add(getString(R.string.EP_NavigationAnimationSpring));
            configValues.add(CherrygramCoreConfig.ANIMATION_SPRING);

            configStringKeys.add(getString(R.string.EP_NavigationAnimationBezier));
            configValues.add(CherrygramCoreConfig.ANIMATION_CLASSIC);

            PopupHelper.show(configStringKeys, getString(R.string.EP_NavigationAnimation), configValues.indexOf(CherrygramCoreConfig.INSTANCE.getSpringAnimation()), getContext(), i -> {
                CherrygramCoreConfig.INSTANCE.setSpringAnimation(configValues.get(i));
                SettingsHelper.updateButtonValue(view, getSpringValue());

                listView.adapter.update(true);

                CGBulletinCreator.INSTANCE.createRestartBulletin(this);
            });
        } else if (item.id == actionbarCrossfadeRow) {
            CherrygramCoreConfig.INSTANCE.setActionbarCrossfade(!CherrygramCoreConfig.INSTANCE.getActionbarCrossfade());
            SettingsHelper.updateCheckState(view, CherrygramCoreConfig.INSTANCE.getActionbarCrossfade());

            if (CherrygramCoreConfig.INSTANCE.getActionbarCrossfade() && CherrygramCoreConfig.INSTANCE.getPredictiveBack()) {
                CherrygramCoreConfig.INSTANCE.setPredictiveBack(false);
                listView.adapter.update(true);
            }

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        } else if (item.id == predictiveBackRow) {
            CherrygramCoreConfig.INSTANCE.setPredictiveBack(!CherrygramCoreConfig.INSTANCE.getPredictiveBack());
            SettingsHelper.updateCheckState(view, CherrygramCoreConfig.INSTANCE.getPredictiveBack());

            if (CherrygramCoreConfig.INSTANCE.getPredictiveBack() && CherrygramCoreConfig.INSTANCE.getActionbarCrossfade()) {
                CherrygramCoreConfig.INSTANCE.setActionbarCrossfade(false);
                listView.adapter.update(true);
            }

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        } else if (item.id == silenceNonContactsRow) {
            CherrygramCoreConfig.INSTANCE.setSilenceNonContacts(!CherrygramCoreConfig.INSTANCE.getSilenceNonContacts());
            SettingsHelper.updateCheckState(view, CherrygramCoreConfig.INSTANCE.getSilenceNonContacts());
        } else if (item.id == defaultNotificationIconRow) {
            CherrygramCoreConfig.INSTANCE.setOldNotificationIcon(!CherrygramCoreConfig.INSTANCE.getOldNotificationIcon());
            SettingsHelper.updateCheckState(view, CherrygramCoreConfig.INSTANCE.getOldNotificationIcon());

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        } else if (item.id == residentNotificationRow) {
            CherrygramCoreConfig.INSTANCE.setResidentNotification(!CherrygramCoreConfig.INSTANCE.getResidentNotification());
            SettingsHelper.updateCheckState(view, CherrygramCoreConfig.INSTANCE.getResidentNotification());

            ApplicationLoader.applicationContext.stopService(new Intent(ApplicationLoader.applicationContext, NotificationsService.class));
            ApplicationLoader.startPushService();
            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        } else if (item.id == hideStoriesRow) {
            CherrygramCoreConfig.INSTANCE.setHideStories(!CherrygramCoreConfig.INSTANCE.getHideStories());
            SettingsHelper.updateCheckState(view, CherrygramCoreConfig.INSTANCE.getHideStories());

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        } else if (item.id == archiveStoriesRow) {
            showStoriesArchiveConfigurator();
        } else if (item.id == useSystemEmojiRow) {
            CherrygramCoreConfig.INSTANCE.setSystemEmoji(!CherrygramCoreConfig.INSTANCE.getSystemEmoji());
            SettingsHelper.updateCheckState(view, CherrygramCoreConfig.INSTANCE.getSystemEmoji());
        } else if (item.id == useSystemFontsRow) {
            CherrygramCoreConfig.INSTANCE.setSystemFonts(!CherrygramCoreConfig.INSTANCE.getSystemFonts());
            SettingsHelper.updateCheckState(view, CherrygramCoreConfig.INSTANCE.getSystemFonts());

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        } else if (item.id == tabledModeRow) {
            showTabletModeSelector(() -> {
                SettingsHelper.updateButtonValue(view, getTabletModeValue());

                AndroidUtilities.resetTabletFlag();
                if (getParentActivity() instanceof LaunchActivity launchActivity) {
                    launchActivity.invalidateTabletMode();
                }
            });
        } else if (item.id == downloadSpeedBoostRow) {
            ArrayList<String> configStringKeys = new ArrayList<>();
            ArrayList<Integer> configValues = new ArrayList<>();

            configStringKeys.add(getString(R.string.LiteBatteryDisabled));
            configValues.add(CherrygramCoreConfig.BOOST_NONE);

            configStringKeys.add(getString(R.string.LiteBatteryEnabled));
            configValues.add(CherrygramCoreConfig.BOOST_AVERAGE);

            configStringKeys.add(getString(R.string.EP_DownloadSpeedBoostExtreme));
            configValues.add(CherrygramCoreConfig.BOOST_EXTREME);

            PopupHelper.show(configStringKeys, getString(R.string.EP_DownloadSpeedBoost), configValues.indexOf(CherrygramCoreConfig.INSTANCE.getDownloadSpeedBoost()), getContext(), i -> {
                CherrygramCoreConfig.INSTANCE.setDownloadSpeedBoost(configValues.get(i));
                SettingsHelper.updateButtonValue(view, getDownloadSpeedBoostText());

                CGBulletinCreator.INSTANCE.createRestartBulletin(this);
            });
        } else if (item.id == uploadSpeedBoostRow) {
            CherrygramCoreConfig.INSTANCE.setUploadSpeedBoost(!CherrygramCoreConfig.INSTANCE.getUploadSpeedBoost());
            SettingsHelper.updateCheckState(view, CherrygramCoreConfig.INSTANCE.getUploadSpeedBoost());

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        } else if (item.id == slowNetworkMode) {
            CherrygramCoreConfig.INSTANCE.setSlowNetworkMode(!CherrygramCoreConfig.INSTANCE.getSlowNetworkMode());
            SettingsHelper.updateCheckState(view, CherrygramCoreConfig.INSTANCE.getSlowNetworkMode());

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }

    private String getSpringValue()  {
        return switch (CherrygramCoreConfig.INSTANCE.getSpringAnimation()) {
            case CherrygramCoreConfig.ANIMATION_CLASSIC -> getString(R.string.EP_NavigationAnimationBezier);
            default -> getString(R.string.EP_NavigationAnimationSpring);
        };
    }

    private String getDownloadSpeedBoostText()  {
        return switch (CherrygramCoreConfig.INSTANCE.getDownloadSpeedBoost()) {
            case CherrygramCoreConfig.BOOST_NONE -> getString(R.string.LiteBatteryDisabled);
            case CherrygramCoreConfig.BOOST_AVERAGE -> getString(R.string.LiteBatteryEnabled);
            default -> getString(R.string.EP_DownloadSpeedBoostExtreme);
        };
    }

    private void showTabletModeSelector(Runnable runnable) {
        ArrayList<String> configStringKeys = new ArrayList<>();
        ArrayList<Integer> configValues = new ArrayList<>();

        configStringKeys.add(getString(R.string.QualityAuto));
        configValues.add(CherrygramCoreConfig.TABLET_MODE_AUTO);

        configStringKeys.add(getString(R.string.LiteBatteryEnabled));
        configValues.add(CherrygramCoreConfig.TABLET_MODE_ENABLE);

        configStringKeys.add(getString(R.string.LiteBatteryDisabled));
        configValues.add(CherrygramCoreConfig.TABLET_MODE_DISABLE);

        PopupHelper.show(configStringKeys, getString(R.string.AP_Tablet_Mode), configValues.indexOf(CherrygramCoreConfig.INSTANCE.getTabletMode()), getContext(), i -> {
            CherrygramCoreConfig.INSTANCE.setTabletMode(configValues.get(i));
            if (runnable != null) runnable.run();
        });
    }

    private String getTabletModeValue()  {
        return switch (CherrygramCoreConfig.INSTANCE.getTabletMode()) {
            case CherrygramCoreConfig.TABLET_MODE_ENABLE -> getString(R.string.LiteBatteryEnabled);
            case CherrygramCoreConfig.TABLET_MODE_DISABLE -> getString(R.string.LiteBatteryDisabled);
            default -> getString(R.string.QualityAuto);
        };
    }

    private void showStoriesArchiveConfigurator() {
        List<MenuItemConfig> menuItems = Arrays.asList(
                new MenuItemConfig(
                        getString(R.string.FilterContacts),
                        R.drawable.msg_contacts,
                        CherrygramCoreConfig.INSTANCE::getArchiveStoriesFromUsers,
                        () -> CherrygramCoreConfig.INSTANCE.setArchiveStoriesFromUsers(!CherrygramCoreConfig.INSTANCE.getArchiveStoriesFromUsers()),
                        false
                ),
                new MenuItemConfig(
                        getString(R.string.FilterChannels),
                        R.drawable.msg_channel,
                        CherrygramCoreConfig.INSTANCE::getArchiveStoriesFromChannels,
                        () -> CherrygramCoreConfig.INSTANCE.setArchiveStoriesFromChannels(!CherrygramCoreConfig.INSTANCE.getArchiveStoriesFromChannels()),
                        false
                )
        );

        ArrayList<String> prefTitle = new ArrayList<>();
        ArrayList<Integer> prefIcon = new ArrayList<>();
        ArrayList<Boolean> prefCheck = new ArrayList<>();
        ArrayList<Boolean> prefDivider = new ArrayList<>();
        ArrayList<Runnable> clickListener = new ArrayList<>();

        for (MenuItemConfig item : menuItems) {
            prefTitle.add(item.titleRes());
            prefIcon.add(item.iconRes());
            prefCheck.add(item.isChecked().get());
            prefDivider.add(item.divider());
            clickListener.add(item.toggle());
        }

        PopupHelper.showSwitchAlert(
                getString(R.string.CP_ArchiveStories),
                GeneralPreferencesEntry.this,
                prefTitle,
                prefIcon,
                prefCheck,
                null,
                null,
                prefDivider,
                clickListener,
                null
        );
    }

    private record MenuItemConfig(
            String titleRes,
            int iconRes,
            Supplier<Boolean> isChecked,
            Runnable toggle,
            boolean divider
    ) {

    }

}
