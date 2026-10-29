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

import static uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper.applyNewSpan;
import static uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper.applyProSpan;

import android.os.Build;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BotWebViewVibrationEffect;
import org.telegram.messenger.R;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.chats.CGMessageMenuInjector;
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.core.configs.CherrygramMessagesConfig;
import uz.unnarsx.cherrygram.core.firebase.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.core.helpers.DeeplinkHelper;
import uz.unnarsx.cherrygram.donates.DonatesManager;
import uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper;

public class MessageMenuPreferencesEntry extends BaseCGPreferencesEntry {

    private final int enableNewMessageMenuRow = 1;
    private final int useNativeBlurRow = 2;
    private final int fixAnimationDurationRow = 3;
    private final int showWholeAlbumRow = 4;
    private final int rememberKeyboardStateRow = 5;

    private final int messageMenuItemsRow = 6;
    private final int messageMenuItemsCompactViewRow = 7;

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("message_menu_preferences_screen");
        return getString(R.string.CP_MessageMenu);
    }

    @Override
    protected String getKey() {
        return DeeplinkHelper.DeepLinksRepo.CG_Message_Menu;
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        boolean requireDonate = !DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace();
        boolean requireDonate2 = !DonatesManager.INSTANCE.checkAllDonatedAccounts() && !DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            items.add(UItem.asHeader(getString(R.string.AP_Header_Appearance)));
            items.add(
                    SettingsHelper.asSwitchCG
                            (
                                    enableNewMessageMenuRow,
                                    applyProSpan(getString(R.string.CP_BlurMessageMenu), getResourceProvider()),
                                    getString(R.string.CP_BlurMessageMenu_Desc)
                            )
                    .setChecked(CherrygramMessagesConfig.INSTANCE.getBlurMessageMenuBackground())
                    .setLocked(requireDonate)
                    .slug("iosMsgMenu")
            );
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                items.add(SettingsHelper.asSwitchCG(useNativeBlurRow, getString(R.string.CP_MessageMenuNativeBlur), getString(R.string.CP_MessageMenuNativeBlur_Desc))
                        .setChecked(CherrygramMessagesConfig.INSTANCE.getMsgMenuNativeBlur())
                        .setEnabled(CherrygramMessagesConfig.INSTANCE.getBlurMessageMenuBackground())
                        .slug("nativeBlur")
                );
            }
            items.add(
                    SettingsHelper.asSwitchCG
                            (
                                    showWholeAlbumRow,
                                    applyNewSpan(getString(R.string.CG_MsgMenuShowWholeAlbum)),
                                    getString(R.string.CG_MsgMenuShowWholeAlbum_Desc)
                            )
                    .setChecked(CherrygramMessagesConfig.INSTANCE.getMsgMenuShowWholeAlbum())
                    .setEnabled(CherrygramMessagesConfig.INSTANCE.getBlurMessageMenuBackground())
                    .slug("showWholeAlbum")
            );
            items.add(SettingsHelper.asSwitchCG(rememberKeyboardStateRow, applyNewSpan(getString(R.string.CP_RememberKeyboardState)))
                    .setChecked(CherrygramMessagesConfig.INSTANCE.getRememberKeyboardState())
                    .setEnabled(CherrygramMessagesConfig.INSTANCE.getBlurMessageMenuBackground())
                    .slug("rememberKbdState")
            );
            if (CherrygramCoreConfig.isDevBuild()) {
                items.add(SettingsHelper.asSwitchCG(fixAnimationDurationRow, "Fix animation duration")
                        .setChecked(CherrygramMessagesConfig.INSTANCE.getFixMsgMenuAnimation())
                        .setEnabled(CherrygramMessagesConfig.INSTANCE.getBlurMessageMenuBackground())
                );
            }
            items.add(UItem.asShadow(null));
        }

        items.add(UItem.asHeader(getString(R.string.LocalMiscellaneousCache)));
        items.add(UItem.asButton(messageMenuItemsRow, R.drawable.msg_list, getString(R.string.CP_MessageMenuItems))
                .slug("menuItems")
        );
        items.add(
                SettingsHelper.asSwitchCG
                        (
                            messageMenuItemsCompactViewRow,
                            applyProSpan(getString(R.string.CP_MessageMenuCompactLayout), getResourceProvider()),
                            getString(R.string.CP_MessageMenuCompactLayout_Desc) + "\n\n" + getString(R.string.CP_MessageMenuCompactLayout_Dot)
                        )
                .setChecked(CherrygramMessagesConfig.INSTANCE.getMsgMenuItemsCompactView())
                .setLocked(requireDonate2)
                .slug("compactMsgMenu")
        );
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        boolean requireDonate;

        if (item.id == messageMenuItemsCompactViewRow) {
            requireDonate = !DonatesManager.INSTANCE.checkAllDonatedAccounts() && !DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace();
        } else {
            requireDonate = !DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace();
        }

        var holder = listView.findViewHolderForAdapterPosition(position);
        if (holder == null || !listView.adapter.isEnabled(holder)) {
            return;
        }
        if (requireDonate && item.id != messageMenuItemsRow) {
            AndroidUtilities.shakeViewSpring(view);
            BotWebViewVibrationEffect.APP_ERROR.vibrate();
            showDonateBulletin();
            return;
        }

        if (item.id == enableNewMessageMenuRow) {
            CherrygramMessagesConfig.INSTANCE.setBlurMessageMenuBackground(!CherrygramMessagesConfig.INSTANCE.getBlurMessageMenuBackground());
            SettingsHelper.updateCheckState(view, CherrygramMessagesConfig.INSTANCE.getBlurMessageMenuBackground());

            updateRows(true);
        } else if (item.id == useNativeBlurRow) {
            CherrygramMessagesConfig.INSTANCE.setMsgMenuNativeBlur(!CherrygramMessagesConfig.INSTANCE.getMsgMenuNativeBlur());
            SettingsHelper.updateCheckState(view, CherrygramMessagesConfig.INSTANCE.getMsgMenuNativeBlur());
        } else if (item.id == showWholeAlbumRow) {
            CherrygramMessagesConfig.INSTANCE.setMsgMenuShowWholeAlbum(!CherrygramMessagesConfig.INSTANCE.getMsgMenuShowWholeAlbum());
            SettingsHelper.updateCheckState(view, CherrygramMessagesConfig.INSTANCE.getMsgMenuShowWholeAlbum());
        } else if (item.id == rememberKeyboardStateRow) {
            CherrygramMessagesConfig.INSTANCE.setRememberKeyboardState(!CherrygramMessagesConfig.INSTANCE.getRememberKeyboardState());
            SettingsHelper.updateCheckState(view, CherrygramMessagesConfig.INSTANCE.getRememberKeyboardState());
        } else if (item.id == fixAnimationDurationRow) {
            CherrygramMessagesConfig.INSTANCE.setFixMsgMenuAnimation(!CherrygramMessagesConfig.INSTANCE.getFixMsgMenuAnimation());
            SettingsHelper.updateCheckState(view, CherrygramMessagesConfig.INSTANCE.getFixMsgMenuAnimation());
        } else if (item.id == messageMenuItemsRow) {
            CGMessageMenuInjector.INSTANCE.showMessageMenuItemsConfigurator(this);
        } else if (item.id == messageMenuItemsCompactViewRow) {
            CherrygramMessagesConfig.INSTANCE.setMsgMenuItemsCompactView(!CherrygramMessagesConfig.INSTANCE.getMsgMenuItemsCompactView());
            SettingsHelper.updateCheckState(view, CherrygramMessagesConfig.INSTANCE.getMsgMenuItemsCompactView());
        }
    }

}
