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
import android.os.Build;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BotWebViewVibrationEffect;
import org.telegram.messenger.R;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.chats.CGMessageMenuInjector;
import uz.unnarsx.cherrygram.core.configs.CherrygramMessagesConfig;
import uz.unnarsx.cherrygram.core.crashlytics.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.core.ui.CGBulletinCreator;
import uz.unnarsx.cherrygram.donates.DonatesManager;
import uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper;

public class MessageMenuPreferencesEntry extends UniversalFragment {

    private final int enableNewMessageMenuRow = 1;
    private final int unifiedScrollRow = 2;
    private final int autoScrollMessagesRow = 3;
    private final int fixedMessageHeightRow = 4;
    private final int blurMessageMenuItemsRow = 5;
    private final int useNativeBlurRow = 6;

    private final int messageMenuItemsRow = 7;
    private final int messageMenuItemsCompactViewRow = 8;

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("message_menu_preferences_screen");
        return getString(R.string.CP_MessageMenu);
    }

    @Override
    public View createView(Context context) {
        setMD3(true);
        setGilroy(true);
        return super.createView(context);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        boolean requireDonate = !DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace();
        boolean requireDonate2 = !DonatesManager.INSTANCE.checkAllDonatedAccounts() && !DonatesManager.INSTANCE.checkAllDonatedAccountsForMarketplace();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            items.add(UItem.asHeader(getString(R.string.AP_Header_Appearance)));
            items.add(SettingsHelper.asSwitchCG(enableNewMessageMenuRow, getString(R.string.CP_BlurMessageMenu), getString(R.string.CP_BlurMessageMenu_Desc))
                    .setChecked(CherrygramMessagesConfig.INSTANCE.getBlurMessageMenuBackground())
                    .setLocked(requireDonate)
            );
            items.add(SettingsHelper.asSwitchCG(unifiedScrollRow, getString(R.string.CP_MessageMenuUnifiedScroll), getString(R.string.CP_MessageMenuUnifiedScroll_Desc))
                    .setChecked(CherrygramMessagesConfig.INSTANCE.getMsgMenuUnifiedScroll())
                    .setEnabled(CherrygramMessagesConfig.INSTANCE.getBlurMessageMenuBackground())
            );
            items.add(SettingsHelper.asSwitchCG(autoScrollMessagesRow, getString(R.string.CP_MessageMenuAutoscroll), getString(R.string.CP_MessageMenuAutoscroll_Desc))
                    .setChecked(CherrygramMessagesConfig.INSTANCE.getMsgMenuAutoScroll())
                    .setEnabled(CherrygramMessagesConfig.INSTANCE.getBlurMessageMenuBackground() && !CherrygramMessagesConfig.INSTANCE.getMsgMenuUnifiedScroll())
            );
            items.add(SettingsHelper.asSwitchCG(fixedMessageHeightRow, getString(R.string.CP_MessageMenuFixedHeight), getString(R.string.CP_MessageMenuFixedHeight_Desc))
                    .setChecked(CherrygramMessagesConfig.INSTANCE.getMsgMenuFixedHeight())
                    .setEnabled(CherrygramMessagesConfig.INSTANCE.getBlurMessageMenuBackground() && !CherrygramMessagesConfig.INSTANCE.getMsgMenuUnifiedScroll())
            );
            /*items.add(SettingsHelper.asSwitchCG(blurMessageMenuItemsRow, getString(R.string.CP_BlurMessageMenuItems), getString(R.string.CP_BlurMessageMenuItems_Desc))
                    .setChecked(CherrygramMessagesConfig.INSTANCE.getBlurMessageMenuItems())
                    .setEnabled(CherrygramMessagesConfig.INSTANCE.getBlurMessageMenuBackground())
            );*/
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                items.add(SettingsHelper.asSwitchCG(useNativeBlurRow, getString(R.string.CP_MessageMenuNativeBlur), getString(R.string.CP_MessageMenuNativeBlur_Desc))
                        .setChecked(CherrygramMessagesConfig.INSTANCE.getMsgMenuNativeBlur())
                        .setEnabled(CherrygramMessagesConfig.INSTANCE.getBlurMessageMenuBackground())
                );
            }
            items.add(UItem.asShadow(null));
        }

        items.add(UItem.asHeader(getString(R.string.LocalMiscellaneousCache)));
        items.add(UItem.asButton(messageMenuItemsRow, R.drawable.msg_list, getString(R.string.CP_MessageMenuItems)));
        items.add(
                SettingsHelper.asSwitchCG
                        (
                            messageMenuItemsCompactViewRow,
                            getString(R.string.CP_MessageMenuCompactLayout),
                            getString(R.string.CP_MessageMenuCompactLayout_Desc) + "\n\n" + getString(R.string.CP_MessageMenuCompactLayout_Dot)
                        )
                .setChecked(CherrygramMessagesConfig.INSTANCE.getMsgMenuItemsCompactView())
                .setLocked(requireDonate2)
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
            CGBulletinCreator.INSTANCE.createRequireDonateBulletin(this);
            return;
        }

        if (item.id == enableNewMessageMenuRow) {
            CherrygramMessagesConfig.INSTANCE.setBlurMessageMenuBackground(!CherrygramMessagesConfig.INSTANCE.getBlurMessageMenuBackground());
            SettingsHelper.updateCheckState(view, CherrygramMessagesConfig.INSTANCE.getBlurMessageMenuBackground());

            listView.adapter.update(true);
        } else if (item.id == unifiedScrollRow) {
            CherrygramMessagesConfig.INSTANCE.setMsgMenuUnifiedScroll(!CherrygramMessagesConfig.INSTANCE.getMsgMenuUnifiedScroll());
            SettingsHelper.updateCheckState(view, CherrygramMessagesConfig.INSTANCE.getMsgMenuUnifiedScroll());
//            if (view instanceof TextCheckCell) {
//                ((TextCheckCell) view).setChecked(CherrygramMessagesConfig.INSTANCE.getMsgMenuUnifiedScroll());
//
//                /*if (CherrygramMessagesConfig.INSTANCE.getMsgMenuUnifiedScroll() && !CherrygramMessagesConfig.INSTANCE.getBlurMessageMenuBackground()) {
//                    CherrygramMessagesConfig.INSTANCE.setBlurMessageMenuBackground(true);
//                    listAdapter.notifyItemChanged(enableNewMessageMenuRow, false);
//                }
//
//                CherrygramMessagesConfig.INSTANCE.setMsgMenuFixedHeight(!CherrygramMessagesConfig.INSTANCE.getMsgMenuUnifiedScroll() || !CherrygramMessagesConfig.INSTANCE.getMsgMenuFixedHeight());*/
//
//                listAdapter.notifyItemChanged(autoScrollMessagesRow, false);
//                listAdapter.notifyItemChanged(fixedMessageHeightRow, false);
//            }
            listView.adapter.update(true);
        } else if (item.id == autoScrollMessagesRow) {
            CherrygramMessagesConfig.INSTANCE.setMsgMenuAutoScroll(!CherrygramMessagesConfig.INSTANCE.getMsgMenuAutoScroll());
            SettingsHelper.updateCheckState(view, CherrygramMessagesConfig.INSTANCE.getMsgMenuAutoScroll());
        } else if (item.id == fixedMessageHeightRow) {
            CherrygramMessagesConfig.INSTANCE.setMsgMenuFixedHeight(!CherrygramMessagesConfig.INSTANCE.getMsgMenuFixedHeight());
            SettingsHelper.updateCheckState(view, CherrygramMessagesConfig.INSTANCE.getMsgMenuFixedHeight());
        } else if (item.id == blurMessageMenuItemsRow) {
            CherrygramMessagesConfig.INSTANCE.setBlurMessageMenuItems(!CherrygramMessagesConfig.INSTANCE.getBlurMessageMenuItems());
            SettingsHelper.updateCheckState(view, CherrygramMessagesConfig.INSTANCE.getBlurMessageMenuItems());
        } else if (item.id == useNativeBlurRow) {
            CherrygramMessagesConfig.INSTANCE.setMsgMenuNativeBlur(!CherrygramMessagesConfig.INSTANCE.getMsgMenuNativeBlur());
            SettingsHelper.updateCheckState(view, CherrygramMessagesConfig.INSTANCE.getMsgMenuNativeBlur());
        } else if (item.id == messageMenuItemsRow) {
            CGMessageMenuInjector.INSTANCE.showMessageMenuItemsConfigurator(this);
        } else if (item.id == messageMenuItemsCompactViewRow) {
            CherrygramMessagesConfig.INSTANCE.setMsgMenuItemsCompactView(!CherrygramMessagesConfig.INSTANCE.getMsgMenuItemsCompactView());
            SettingsHelper.updateCheckState(view, CherrygramMessagesConfig.INSTANCE.getMsgMenuItemsCompactView());
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }

}
