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
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.core.crashlytics.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper;
import uz.unnarsx.cherrygram.core.helpers.DeeplinkHelper;
import uz.unnarsx.cherrygram.core.helpers.backup.BackupHelper;

public class CGPreferencesEntry extends UniversalFragment {

    private final int generalRow = 1;
    private final int appearanceRow = 2;
    private final int chatsRow = 3;
    private final int cameraRow = 4;
    private final int experimentalRow = 5;
    private final int privacyRow = 6;

    private final int supportRow = 7;
    private final int exportRow = 8;
    private final int importRow = 9;
    private final int restartRow = 10;

    private final int aboutRow = 11;

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("main_preferences_screen");
        return getString(R.string.CGP_AdvancedSettings);
    }

    @Override
    public View createView(Context context) {
        setMD3(true);
        return super.createView(context);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(getString(R.string.AP_Header_General)));

        items.add(
                UItem.asButton(
                        generalRow,
                        R.drawable.msg_settings_solar,
                        getString(R.string.AP_Header_General)
                )
        );

        items.add(
                UItem.asButton(
                        appearanceRow,
                        R.drawable.msg_theme_solar,
                        getString(R.string.AP_Header_Appearance)
                )
        );

        items.add(
                UItem.asButton(
                        chatsRow,
                        R.drawable.msg_msgbubble3_solar,
                        getString(R.string.CP_Header_Chats)
                )
        );

        items.add(
                UItem.asButton(
                        cameraRow,
                        R.drawable.camera_solar,
                        getString(R.string.CP_Category_Camera)
                )
        );

        items.add(
                UItem.asButton(
                        experimentalRow,
                        R.drawable.msg_fave_solar,
                        getString(R.string.EP_Category_Experimental)
                )
        );

        items.add(
                UItem.asButton(
                        privacyRow,
                        R.drawable.msg_secret_solar,
                        getString(R.string.SP_Category_PrivacyAndSecurity)
                )
        );

        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.LocalOther)));

        items.add(
                UItem.asButton(
                        supportRow,
                        R.drawable.heart_angle_solar,
                        getString(R.string.DP_Support)
                )
        );

        items.add(
                UItem.asButton(
                        exportRow,
                        R.drawable.msg_instant_link_solar,
                        getString(R.string.CG_ExportSettings)
                )
        );

        items.add(
                UItem.asButton(
                        importRow,
                        R.drawable.msg_photo_settings_solar,
                        getString(R.string.CG_ImportSettings)
                )
        );

        items.add(
                UItem.asButton(
                        restartRow,
                        R.drawable.msg_retry_solar,
                        getString(R.string.CG_Restart)
                )
        );

        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.CGP_Header_About)));

        items.add(
                UItem.asButton(
                        aboutRow,
                        R.drawable.msg_info_solar,
                        getString(R.string.CGP_Header_About_Desc)
                )
        );

        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == generalRow) {
            presentFragment(CherrygramPreferencesNavigator.INSTANCE.createGeneral());
        } else if (item.id == appearanceRow) {
            presentFragment(CherrygramPreferencesNavigator.INSTANCE.createAppearance());
        } else if (item.id == chatsRow) {
            presentFragment(CherrygramPreferencesNavigator.INSTANCE.createChats());
        } else if (item.id == cameraRow) {
            CherrygramPreferencesNavigator.INSTANCE.createCamera(this);
        } else if (item.id == experimentalRow) {
            CherrygramPreferencesNavigator.INSTANCE.createExperimental(this);
        } else if (item.id == privacyRow) {
            presentFragment(CherrygramPreferencesNavigator.INSTANCE.createPrivacyAndSecurity());
        } else if (item.id == supportRow) {
            CherrygramPreferencesNavigator.INSTANCE.createDonate(this);
        } else if (item.id == exportRow) {
            BackupHelper.INSTANCE.backupSettings(this);
        } else if (item.id == importRow) {
            BackupHelper.INSTANCE.importSettings(this);
        } else if (item.id == restartRow) {
            AppRestartHelper.restartApp(getContext());
        } else if (item.id == aboutRow) {
            CherrygramPreferencesNavigator.INSTANCE.createAbout(this);
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
        } else if (item.id == restartRow) {
            AndroidUtilities.addToClipboard("tg://" + DeeplinkHelper.DeepLinksRepo.CG_Restart);
            return true;
        } else if (item.id == aboutRow) {
            AndroidUtilities.addToClipboard("tg://" + DeeplinkHelper.DeepLinksRepo.CG_About);
            return true;
        }
        return false;
    }

}
