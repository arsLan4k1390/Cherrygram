package uz.unnarsx.cherrygram.preferences;

import android.content.Context;
import android.widget.LinearLayout;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Components.LayoutHelper;

import uz.unnarsx.cherrygram.CherrygramConfig;

public class AlertDialogSwitchHelper {

    public static void showDirectShareAlert(BaseFragment fragment) {
        if (fragment.getParentActivity() == null) {
            return;
        }
        Context context = fragment.getParentActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(LocaleController.getString("DirectShare", R.string.DirectShare));

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout linearLayoutInviteContainer = new LinearLayout(context);
        linearLayoutInviteContainer.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(linearLayoutInviteContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        int count = 5;
        for (int a = 0; a < count; a++) {
            TextCell textCell = new TextCell(context, 23, false, true, fragment.getResourceProvider());
            switch (a) {
                case 0: {
                    textCell.setTextAndCheck(LocaleController.getString("FilterChats", R.string.FilterChats), CherrygramConfig.INSTANCE.getUsersDrawShareButton(), false);
                    break;
                }
                case 1: {
                    textCell.setTextAndCheck(LocaleController.getString("FilterGroups", R.string.FilterGroups), CherrygramConfig.INSTANCE.getSupergroupsDrawShareButton(), false);
                    break;
                }
                case 2: {
                    textCell.setTextAndCheck(LocaleController.getString("FilterChannels", R.string.FilterChannels), CherrygramConfig.INSTANCE.getChannelsDrawShareButton(), false);
                    break;
                }
                case 3: {
                    textCell.setTextAndCheck(LocaleController.getString("FilterBots", R.string.FilterBots), CherrygramConfig.INSTANCE.getBotsDrawShareButton(), false);
                    break;
                }
                case 4: {
                    textCell.setTextAndCheck(LocaleController.getString("StickersName", R.string.StickersName), CherrygramConfig.INSTANCE.getStickersDrawShareButton(), false);
                    break;
                }
            }
            textCell.setTag(a);
            textCell.setBackground(Theme.getSelectorDrawable(false));
            linearLayoutInviteContainer.addView(textCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            textCell.setOnClickListener(v2 -> {
                Integer tag = (Integer) v2.getTag();
                switch (tag) {
                    case 0: {
                        CherrygramConfig.INSTANCE.toggleUsersDrawShareButton();
                        textCell.setChecked(CherrygramConfig.INSTANCE.getUsersDrawShareButton());
                        break;
                    }
                    case 1: {
                        CherrygramConfig.INSTANCE.toggleSupergroupsDrawShareButton();
                        textCell.setChecked(CherrygramConfig.INSTANCE.getSupergroupsDrawShareButton());
                        break;
                    }
                    case 2: {
                        CherrygramConfig.INSTANCE.toggleChannelsDrawShareButton();
                        textCell.setChecked(CherrygramConfig.INSTANCE.getChannelsDrawShareButton());
                        break;
                    }
                    case 3: {
                        CherrygramConfig.INSTANCE.toggleBotsDrawShareButton();
                        textCell.setChecked(CherrygramConfig.INSTANCE.getBotsDrawShareButton());
                        break;
                    }
                    case 4: {
                        CherrygramConfig.INSTANCE.toggleStickersDrawShareButton();
                        textCell.setChecked(CherrygramConfig.INSTANCE.getStickersDrawShareButton());
                        break;
                    }
                }
                fragment.getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
            });
        }
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
        builder.setView(linearLayout);
        fragment.showDialog(builder.create());
    }

    public static void showDrawerIconsAlert(BaseFragment fragment) {
        if (fragment.getParentActivity() == null) {
            return;
        }
        Context context = fragment.getParentActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(LocaleController.getString("AP_DrawerButtonsCategory", R.string.AP_DrawerButtonsCategory));

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout linearLayoutInviteContainer = new LinearLayout(context);
        linearLayoutInviteContainer.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(linearLayoutInviteContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        int count = 10;
        for (int a = 0; a < count; a++) {
            TextCell textCell = new TextCell(context, 23, false, true, fragment.getResourceProvider());
            switch (a) {
                case 0: {
                    textCell.setTextAndCheckAndIcon(LocaleController.getString("NewGroup", R.string.NewGroup), CherrygramConfig.INSTANCE.getCreateGroupDrawerButton(), R.drawable.msg_groups, false);
                    break;
                }
                case 1: {
                    textCell.setTextAndCheckAndIcon(LocaleController.getString("NewSecretChat", R.string.NewSecretChat), CherrygramConfig.INSTANCE.getSecretChatDrawerButton(), R.drawable.msg_secret, false);
                    break;
                }
                case 2: {
                    textCell.setTextAndCheckAndIcon(LocaleController.getString("NewChannel", R.string.NewChannel), CherrygramConfig.INSTANCE.getCreateChannelDrawerButton(), R.drawable.msg_channel, false);
                    break;
                }
                case 3: {
                    textCell.setTextAndCheckAndIcon(LocaleController.getString("Contacts", R.string.Contacts), CherrygramConfig.INSTANCE.getContactsDrawerButton(), R.drawable.msg_contacts, false);
                    break;
                }
                case 4: {
                    textCell.setTextAndCheckAndIcon(LocaleController.getString("Calls", R.string.Calls), CherrygramConfig.INSTANCE.getCallsDrawerButton(), R.drawable.msg_calls, false);
                    break;
                }
                case 5: {
                    textCell.setTextAndCheckAndIcon(LocaleController.getString("SavedMessages", R.string.SavedMessages), CherrygramConfig.INSTANCE.getSavedMessagesDrawerButton(), R.drawable.msg_saved, false);
                    break;
                }
                case 6: {
                    textCell.setTextAndCheckAndIcon(LocaleController.getString("ArchivedChats", R.string.ArchivedChats), CherrygramConfig.INSTANCE.getArchivedChatsDrawerButton(), R.drawable.msg_archive, false);
                    break;
                }
                case 7: {
                    textCell.setTextAndCheckAndIcon(LocaleController.getString("PeopleNearby", R.string.PeopleNearby), CherrygramConfig.INSTANCE.getPeopleNearbyDrawerButton(), R.drawable.msg_nearby, false);
                    break;
                }
                case 8: {
                    textCell.setTextAndCheckAndIcon(LocaleController.getString("AuthAnotherClient", R.string.AuthAnotherClient), CherrygramConfig.INSTANCE.getScanQRDrawerButton(), R.drawable.msg_qrcode, false);
                    break;
                }
                case 9: {
                    textCell.setTextAndCheckAndIcon(LocaleController.getString("CGP_AdvancedSettings", R.string.CGP_AdvancedSettings), CherrygramConfig.INSTANCE.getCGPreferencesDrawerButton(), R.drawable.msg_settings, false);
                    break;
                }
            }
            textCell.setTag(a);
            textCell.setBackground(Theme.getSelectorDrawable(false));
            linearLayoutInviteContainer.addView(textCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            textCell.setOnClickListener(v2 -> {
                Integer tag = (Integer) v2.getTag();
                switch (tag) {
                    case 0: {
                        CherrygramConfig.INSTANCE.toggleCreateGroupDrawerButton();
                        textCell.setChecked(CherrygramConfig.INSTANCE.getCreateGroupDrawerButton());
                        break;
                    }
                    case 1: {
                        CherrygramConfig.INSTANCE.toggleSecretChatDrawerButton();
                        textCell.setChecked(CherrygramConfig.INSTANCE.getSecretChatDrawerButton());
                        break;
                    }
                    case 2: {
                        CherrygramConfig.INSTANCE.toggleCreateChannelDrawerButton();
                        textCell.setChecked(CherrygramConfig.INSTANCE.getCreateChannelDrawerButton());
                        break;
                    }
                    case 3: {
                        CherrygramConfig.INSTANCE.toggleContactsDrawerButton();
                        textCell.setChecked(CherrygramConfig.INSTANCE.getContactsDrawerButton());
                        break;
                    }
                    case 4: {
                        CherrygramConfig.INSTANCE.toggleCallsDrawerButton();
                        textCell.setChecked(CherrygramConfig.INSTANCE.getCallsDrawerButton());
                        break;
                    }
                    case 5: {
                        CherrygramConfig.INSTANCE.toggleSavedMessagesDrawerButton();
                        textCell.setChecked(CherrygramConfig.INSTANCE.getSavedMessagesDrawerButton());
                        break;
                    }
                    case 6: {
                        CherrygramConfig.INSTANCE.toggleArchivedChatsDrawerButton();
                        textCell.setChecked(CherrygramConfig.INSTANCE.getArchivedChatsDrawerButton());
                        break;
                    }
                    case 7: {
                        CherrygramConfig.INSTANCE.togglePeopleNearbyDrawerButton();
                        textCell.setChecked(CherrygramConfig.INSTANCE.getPeopleNearbyDrawerButton());
                        break;
                    }
                    case 8: {
                        CherrygramConfig.INSTANCE.toggleScanQRDrawerButton();
                        textCell.setChecked(CherrygramConfig.INSTANCE.getScanQRDrawerButton());
                        break;
                    }
                    case 9: {
                        CherrygramConfig.INSTANCE.toggleCGPreferencesDrawerButton();
                        textCell.setChecked(CherrygramConfig.INSTANCE.getCGPreferencesDrawerButton());
                        break;
                    }
                }
                fragment.getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
            });
        }
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
        builder.setView(linearLayout);
        fragment.showDialog(builder.create());
    }

    public static void showChatMenuIconsAlert(BaseFragment fragment) {
        if (fragment.getParentActivity() == null) {
            return;
        }
        Context context = fragment.getParentActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(LocaleController.getString("CP_MessageMenu", R.string.CP_MessageMenu));

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout linearLayoutInviteContainer = new LinearLayout(context);
        linearLayoutInviteContainer.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(linearLayoutInviteContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        int count = 8;
        for (int a = 0; a < count; a++) {
            TextCell textCell = new TextCell(context, 23, false, true, fragment.getResourceProvider());
            switch (a) {
                case 0: {
                    textCell.setTextAndCheckAndIcon(LocaleController.getString("Reply", R.string.Reply), CherrygramConfig.INSTANCE.getShowReply(), R.drawable.msg_reply, false);
                    break;
                }
                case 1: {
                    textCell.setTextAndCheckAndIcon(LocaleController.getString("CG_ClearFromCache", R.string.CG_ClearFromCache), CherrygramConfig.INSTANCE.getShowClearFromCache(), R.drawable.clear_cache, false);
                    break;
                }
                case 2: {
                    textCell.setTextAndCheckAndIcon(LocaleController.getString("Forward", R.string.Forward), CherrygramConfig.INSTANCE.getShowForward(), R.drawable.msg_forward, false);
                    break;
                }
                case 3: {
                    textCell.setTextAndCheckAndIcon(LocaleController.getString("Forward", R.string.Forward) + " " + LocaleController.getString("CG_Without_Authorship", R.string.CG_Without_Authorship), CherrygramConfig.INSTANCE.getShowForwardWoAuthorship(), R.drawable.msg_forward, false);
                    break;
                }
                case 4: {
                    textCell.setTextAndCheckAndIcon(LocaleController.getString("CG_ViewUserHistory", R.string.CG_ViewUserHistory), CherrygramConfig.INSTANCE.getShowViewHistory(), R.drawable.msg_recent, false);
                    break;
                }
                case 5: {
                    textCell.setTextAndCheckAndIcon(LocaleController.getString("CG_ToSaved", R.string.CG_ToSaved), CherrygramConfig.INSTANCE.getShowSaveMessage(), R.drawable.msg_saved, false);
                    break;
                }
                case 6: {
                    textCell.setTextAndCheckAndIcon(LocaleController.getString("ReportChat", R.string.ReportChat), CherrygramConfig.INSTANCE.getShowReport(), R.drawable.msg_report, false);
                    break;
                }
                case 7: {
                    textCell.setTextAndCheckAndIcon("JSON", CherrygramConfig.INSTANCE.getShowJSON(), R.drawable.msg_info, false);
                    break;
                }
            }
            textCell.setTag(a);
            textCell.setBackground(Theme.getSelectorDrawable(false));
            linearLayoutInviteContainer.addView(textCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            textCell.setOnClickListener(v2 -> {
                Integer tag = (Integer) v2.getTag();
                switch (tag) {
                    case 0: {
                        CherrygramConfig.INSTANCE.toggleShowReply();
                        textCell.setChecked(CherrygramConfig.INSTANCE.getShowReply());
                        break;
                    }
                    case 1: {
                        CherrygramConfig.INSTANCE.toggleShowClearFromCache();
                        textCell.setChecked(CherrygramConfig.INSTANCE.getShowClearFromCache());
                        break;
                    }
                    case 2: {
                        CherrygramConfig.INSTANCE.toggleShowForward();
                        textCell.setChecked(CherrygramConfig.INSTANCE.getShowForward());
                        break;
                    }
                    case 3: {
                        CherrygramConfig.INSTANCE.toggleShowForwardWoAuthorship();
                        textCell.setChecked(CherrygramConfig.INSTANCE.getShowForwardWoAuthorship());
                        break;
                    }
                    case 4: {
                        CherrygramConfig.INSTANCE.toggleShowViewHistory();
                        textCell.setChecked(CherrygramConfig.INSTANCE.getShowViewHistory());
                        break;
                    }
                    case 5: {
                        CherrygramConfig.INSTANCE.toggleShowSaveMessage();
                        textCell.setChecked(CherrygramConfig.INSTANCE.getShowSaveMessage());
                        break;
                    }
                    case 6: {
                        CherrygramConfig.INSTANCE.toggleShowReport();
                        textCell.setChecked(CherrygramConfig.INSTANCE.getShowReport());
                        break;
                    }
                    case 7: {
                        CherrygramConfig.INSTANCE.toggleShowJSON();
                        textCell.setChecked(CherrygramConfig.INSTANCE.getShowJSON());
                        break;
                    }
                }
                fragment.getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
            });
        }
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
        builder.setView(linearLayout);
        fragment.showDialog(builder.create());
    }
}
