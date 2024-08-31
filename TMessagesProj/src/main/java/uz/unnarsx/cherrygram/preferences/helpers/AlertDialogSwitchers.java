package uz.unnarsx.cherrygram.preferences.helpers;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Components.LayoutHelper;

import java.util.Random;

import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig;
import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig;

public class AlertDialogSwitchers {

    public static void showChatActionsAlert(BaseFragment fragment) {
        if (fragment.getParentActivity() == null) {
            return;
        }
        Context context = fragment.getParentActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.CP_ChatMenuShortcuts));

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout linearLayoutInviteContainer = new LinearLayout(context);
        linearLayoutInviteContainer.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(linearLayoutInviteContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        int count = 6;
        for (int a = 0; a < count; a++) {
            TextCell textCell = new TextCell(context, 23, false, true, fragment.getResourceProvider());
            switch (a) {
                case 0: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.CG_JumpToBeginning), CherrygramChatsConfig.INSTANCE.getShortcut_JumpToBegin(), R.drawable.ic_upward, false);
                    break;
                }
                case 1: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.CG_DeleteAllFromSelf), CherrygramChatsConfig.INSTANCE.getShortcut_DeleteAll(), R.drawable.msg_delete, false);
                    break;
                }
                case 2: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.SavedMessages), CherrygramChatsConfig.INSTANCE.getShortcut_SavedMessages(), R.drawable.msg_saved, false);
                    break;
                }
                case 3: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.BlurInChat), CherrygramChatsConfig.INSTANCE.getShortcut_Blur(), R.drawable.msg_theme, true);
                    break;
                }
                case 4: {
                    textCell.setTextAndCheckAndIcon("Telegram Browser", CherrygramChatsConfig.INSTANCE.getShortcut_Browser(), R.drawable.msg_language, true);
                    break;
                }
                case 5: {
                    textCell.checkBox.setVisibility(View.INVISIBLE);
                    textCell.setTextAndIcon(getString(R.string.CP_AdminActions), R.drawable.msg_admins, false);
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
                        CherrygramChatsConfig.INSTANCE.toggleShortcutJumpToBegin();
                        textCell.setChecked(CherrygramChatsConfig.INSTANCE.getShortcut_JumpToBegin());
                        break;
                    }
                    case 1: {
                        CherrygramChatsConfig.INSTANCE.toggleShortcutDeleteAll();
                        textCell.setChecked(CherrygramChatsConfig.INSTANCE.getShortcut_DeleteAll());
                        break;
                    }
                    case 2: {
                        CherrygramChatsConfig.INSTANCE.toggleShortcutSavedMessages();
                        textCell.setChecked(CherrygramChatsConfig.INSTANCE.getShortcut_SavedMessages());
                        break;
                    }
                    case 3: {
                        CherrygramChatsConfig.INSTANCE.toggleShortcutBlur();
                        textCell.setChecked(CherrygramChatsConfig.INSTANCE.getShortcut_Blur());
                        break;
                    }
                    case 4: {
                        CherrygramChatsConfig.INSTANCE.toggleShortcutBrowser();
                        textCell.setChecked(CherrygramChatsConfig.INSTANCE.getShortcut_Browser());
                        break;
                    }
                    case 5: {
                        showAdminActionsAlert(fragment);
                        break;
                    }
                }
                fragment.getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
            });
        }
        builder.setPositiveButton(getString(R.string.OK), null);
        builder.setView(linearLayout);
        fragment.showDialog(builder.create());
    }

    public static void showAdminActionsAlert(BaseFragment fragment) {
        if (fragment.getParentActivity() == null) {
            return;
        }
        Context context = fragment.getParentActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.CP_AdminActions));

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout linearLayoutInviteContainer = new LinearLayout(context);
        linearLayoutInviteContainer.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(linearLayoutInviteContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        int count = 6;
        for (int a = 0; a < count; a++) {
            TextCell textCell = new TextCell(context, 23, false, true, fragment.getResourceProvider());
            switch (a) {
                case 0: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.Reactions), CherrygramChatsConfig.INSTANCE.getAdmins_Reactions(), R.drawable.msg_reactions2, true);
                    break;
                }
                case 1: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.ChannelPermissions), CherrygramChatsConfig.INSTANCE.getAdmins_Permissions(), R.drawable.msg_permissions, true);
                    break;
                }
                case 2: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.ChannelAdministrators), CherrygramChatsConfig.INSTANCE.getAdmins_Administrators(), R.drawable.msg_admins, true);
                    break;
                }
                case 3: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.ChannelMembers), CherrygramChatsConfig.INSTANCE.getAdmins_Members(), R.drawable.msg_groups, true);
                    break;
                }
                case 4: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.StatisticsAndBoosts), CherrygramChatsConfig.INSTANCE.getAdmins_Statistics(), R.drawable.msg_stats, true);
                    break;
                }
                case 5: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.EventLog), CherrygramChatsConfig.INSTANCE.getAdmins_RecentActions(), R.drawable.msg_log, true);
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
                        CherrygramChatsConfig.INSTANCE.toggleAdminsReactions();
                        textCell.setChecked(CherrygramChatsConfig.INSTANCE.getAdmins_Reactions());
                        break;
                    }
                    case 1: {
                        CherrygramChatsConfig.INSTANCE.toggleAdminsPermissions();
                        textCell.setChecked(CherrygramChatsConfig.INSTANCE.getAdmins_Permissions());
                        break;
                    }
                    case 2: {
                        CherrygramChatsConfig.INSTANCE.toggleAdminsAdministrators();
                        textCell.setChecked(CherrygramChatsConfig.INSTANCE.getAdmins_Administrators());
                        break;
                    }
                    case 3: {
                        CherrygramChatsConfig.INSTANCE.toggleAdminsMembers();
                        textCell.setChecked(CherrygramChatsConfig.INSTANCE.getAdmins_Members());
                        break;
                    }
                    case 4: {
                        CherrygramChatsConfig.INSTANCE.toggleAdminsStatistics();
                        textCell.setChecked(CherrygramChatsConfig.INSTANCE.getAdmins_Statistics());
                        break;
                    }
                    case 5: {
                        CherrygramChatsConfig.INSTANCE.toggleAdminsRecentActions();
                        textCell.setChecked(CherrygramChatsConfig.INSTANCE.getAdmins_RecentActions());
                        break;
                    }
                }
                fragment.getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
            });
        }
        builder.setPositiveButton(getString(R.string.OK), null);
        builder.setView(linearLayout);
        fragment.showDialog(builder.create());
    }

    public static void showDirectShareAlert(BaseFragment fragment) {
        if (fragment.getParentActivity() == null) {
            return;
        }
        Context context = fragment.getParentActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.DirectShare));

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout linearLayoutInviteContainer = new LinearLayout(context);
        linearLayoutInviteContainer.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(linearLayoutInviteContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        int count = 6;
        for (int a = 0; a < count; a++) {
            TextCell textCell = new TextCell(context, 23, false, true, fragment.getResourceProvider());
            switch (a) {
                case 0: {
                    int[] drawableIDs = {R.drawable.msg_replace_solar, R.drawable.large_repost_story, R.drawable.msg_forward_replace_solar, R.drawable.msg_menu_stories};
                    int storyIcon = drawableIDs[new Random().nextInt(4)];

                    textCell.setTextAndCheckAndIcon(getString(R.string.RepostToStory), CherrygramChatsConfig.INSTANCE.getShareDrawStoryButton(), storyIcon, true);
                    break;
                }
                case 1: {
                    textCell.setTextAndCheck(getString(R.string.FilterChats), CherrygramChatsConfig.INSTANCE.getUsersDrawShareButton(), false);
                    break;
                }
                case 2: {
                    textCell.setTextAndCheck(getString(R.string.FilterGroups), CherrygramChatsConfig.INSTANCE.getSupergroupsDrawShareButton(), false);
                    break;
                }
                case 3: {
                    textCell.setTextAndCheck(getString(R.string.FilterChannels), CherrygramChatsConfig.INSTANCE.getChannelsDrawShareButton(), false);
                    break;
                }
                case 4: {
                    textCell.setTextAndCheck(getString(R.string.FilterBots), CherrygramChatsConfig.INSTANCE.getBotsDrawShareButton(), false);
                    break;
                }
                case 5: {
                    textCell.setTextAndCheck(getString(R.string.StickersName), CherrygramChatsConfig.INSTANCE.getStickersDrawShareButton(), false);
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
                        CherrygramChatsConfig.INSTANCE.toggleShareDrawStoryButton();
                        textCell.setChecked(CherrygramChatsConfig.INSTANCE.getShareDrawStoryButton());
                        break;
                    }
                    case 1: {
                        CherrygramChatsConfig.INSTANCE.toggleUsersDrawShareButton();
                        textCell.setChecked(CherrygramChatsConfig.INSTANCE.getUsersDrawShareButton());
                        break;
                    }
                    case 2: {
                        CherrygramChatsConfig.INSTANCE.toggleSupergroupsDrawShareButton();
                        textCell.setChecked(CherrygramChatsConfig.INSTANCE.getSupergroupsDrawShareButton());
                        break;
                    }
                    case 3: {
                        CherrygramChatsConfig.INSTANCE.toggleChannelsDrawShareButton();
                        textCell.setChecked(CherrygramChatsConfig.INSTANCE.getChannelsDrawShareButton());
                        break;
                    }
                    case 4: {
                        CherrygramChatsConfig.INSTANCE.toggleBotsDrawShareButton();
                        textCell.setChecked(CherrygramChatsConfig.INSTANCE.getBotsDrawShareButton());
                        break;
                    }
                    case 5: {
                        CherrygramChatsConfig.INSTANCE.toggleStickersDrawShareButton();
                        textCell.setChecked(CherrygramChatsConfig.INSTANCE.getStickersDrawShareButton());
                        break;
                    }
                }
                fragment.getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
            });
        }
        builder.setPositiveButton(getString(R.string.OK), null);
        builder.setView(linearLayout);
        fragment.showDialog(builder.create());
    }

    public static void showDrawerIconsAlert(BaseFragment fragment) {
        if (fragment.getParentActivity() == null) {
            return;
        }
        Context context = fragment.getParentActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.AP_DrawerButtonsCategory));

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout linearLayoutInviteContainer = new LinearLayout(context);
        linearLayoutInviteContainer.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(linearLayoutInviteContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        int count = 12;
        for (int a = 0; a < count; a++) {
            TextCell textCell = new TextCell(context, 23, false, true, fragment.getResourceProvider());
            switch (a) {
                case 0: {
                    UserConfig me = UserConfig.getInstance(UserConfig.selectedAccount);
                    textCell.setTextAndCheckAndIcon(
                            me.getEmojiStatus() != null ? getString(R.string.ChangeEmojiStatus) : getString(R.string.SetEmojiStatus),
                            CherrygramAppearanceConfig.INSTANCE.getChangeStatusDrawerButton(),
                            me.getEmojiStatus() != null ? R.drawable.msg_status_edit : R.drawable.msg_status_set,
                            false
                    );
                    break;
                }
                /*case 1: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.ProfileStories), CherrygramAppearanceConfig.INSTANCE.getMyStoriesDrawerButton(), R.drawable.msg_menu_stories, true);
                    break;
                }*/
                case 1: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.MyProfile), CherrygramAppearanceConfig.INSTANCE.getMyProfileDrawerButton(), R.drawable.left_status_profile, true);
                    break;
                }
                case 2: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.NewGroup), CherrygramAppearanceConfig.INSTANCE.getCreateGroupDrawerButton(), R.drawable.msg_groups, false);
                    break;
                }
                case 3: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.NewSecretChat), CherrygramAppearanceConfig.INSTANCE.getSecretChatDrawerButton(), R.drawable.msg_secret, false);
                    break;
                }
                case 4: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.NewChannel), CherrygramAppearanceConfig.INSTANCE.getCreateChannelDrawerButton(), R.drawable.msg_channel, false);
                    break;
                }
                case 5: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.Contacts), CherrygramAppearanceConfig.INSTANCE.getContactsDrawerButton(), R.drawable.msg_contacts, false);
                    break;
                }
                case 6: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.Calls), CherrygramAppearanceConfig.INSTANCE.getCallsDrawerButton(), R.drawable.msg_calls, false);
                    break;
                }
                case 7: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.SavedMessages), CherrygramAppearanceConfig.INSTANCE.getSavedMessagesDrawerButton(), R.drawable.msg_saved, false);
                    break;
                }
                case 8: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.ArchivedChats), CherrygramAppearanceConfig.INSTANCE.getArchivedChatsDrawerButton(), R.drawable.msg_archive, false);
                    break;
                }
                case 9: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.PeopleNearby), CherrygramAppearanceConfig.INSTANCE.getPeopleNearbyDrawerButton(), R.drawable.msg_nearby, false);
                    break;
                }
                case 10: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.AuthAnotherClient), CherrygramAppearanceConfig.INSTANCE.getScanQRDrawerButton(), R.drawable.msg_qrcode, false);
                    break;
                }
                case 11: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.CGP_AdvancedSettings), CherrygramAppearanceConfig.INSTANCE.getCGPreferencesDrawerButton(), R.drawable.msg_settings, false);
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
                        CherrygramAppearanceConfig.INSTANCE.toggleChangeStatusDrawerButton();
                        textCell.setChecked(CherrygramAppearanceConfig.INSTANCE.getChangeStatusDrawerButton());
                        break;
                    }
                    /*case 1: {
                        CherrygramAppearanceConfig.INSTANCE.toggleMyStoriesDrawerButton();
                        textCell.setChecked(CherrygramAppearanceConfig.INSTANCE.getMyStoriesDrawerButton());
                        break;
                    }*/
                    case 1: {
                        CherrygramAppearanceConfig.INSTANCE.toggleMyProfileDrawerButton();
                        textCell.setChecked(CherrygramAppearanceConfig.INSTANCE.getMyProfileDrawerButton());
                        break;
                    }
                    case 2: {
                        CherrygramAppearanceConfig.INSTANCE.toggleCreateGroupDrawerButton();
                        textCell.setChecked(CherrygramAppearanceConfig.INSTANCE.getCreateGroupDrawerButton());
                        break;
                    }
                    case 3: {
                        CherrygramAppearanceConfig.INSTANCE.toggleSecretChatDrawerButton();
                        textCell.setChecked(CherrygramAppearanceConfig.INSTANCE.getSecretChatDrawerButton());
                        break;
                    }
                    case 4: {
                        CherrygramAppearanceConfig.INSTANCE.toggleCreateChannelDrawerButton();
                        textCell.setChecked(CherrygramAppearanceConfig.INSTANCE.getCreateChannelDrawerButton());
                        break;
                    }
                    case 5: {
                        CherrygramAppearanceConfig.INSTANCE.toggleContactsDrawerButton();
                        textCell.setChecked(CherrygramAppearanceConfig.INSTANCE.getContactsDrawerButton());
                        break;
                    }
                    case 6: {
                        CherrygramAppearanceConfig.INSTANCE.toggleCallsDrawerButton();
                        textCell.setChecked(CherrygramAppearanceConfig.INSTANCE.getCallsDrawerButton());
                        break;
                    }
                    case 7: {
                        CherrygramAppearanceConfig.INSTANCE.toggleSavedMessagesDrawerButton();
                        textCell.setChecked(CherrygramAppearanceConfig.INSTANCE.getSavedMessagesDrawerButton());
                        break;
                    }
                    case 8: {
                        CherrygramAppearanceConfig.INSTANCE.toggleArchivedChatsDrawerButton();
                        textCell.setChecked(CherrygramAppearanceConfig.INSTANCE.getArchivedChatsDrawerButton());
                        break;
                    }
                    case 9: {
                        CherrygramAppearanceConfig.INSTANCE.togglePeopleNearbyDrawerButton();
                        textCell.setChecked(CherrygramAppearanceConfig.INSTANCE.getPeopleNearbyDrawerButton());
                        break;
                    }
                    case 10: {
                        CherrygramAppearanceConfig.INSTANCE.toggleScanQRDrawerButton();
                        textCell.setChecked(CherrygramAppearanceConfig.INSTANCE.getScanQRDrawerButton());
                        break;
                    }
                    case 11: {
                        CherrygramAppearanceConfig.INSTANCE.toggleCGPreferencesDrawerButton();
                        textCell.setChecked(CherrygramAppearanceConfig.INSTANCE.getCGPreferencesDrawerButton());
                        break;
                    }
                }
                fragment.getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
            });
        }
        builder.setPositiveButton(getString(R.string.OK), null);
        builder.setView(linearLayout);
        fragment.showDialog(builder.create());
    }

    public static void showChatMenuIconsAlert(BaseFragment fragment) {
        if (fragment.getParentActivity() == null) {
            return;
        }
        Context context = fragment.getParentActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.CP_MessageMenu));

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout linearLayoutInviteContainer = new LinearLayout(context);
        linearLayoutInviteContainer.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(linearLayoutInviteContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        int count = 11;
        for (int a = 0; a < count; a++) {
            TextCell textCell = new TextCell(context, 23, false, true, fragment.getResourceProvider());
            switch (a) {
                case 0: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.SaveForNotifications), CherrygramChatsConfig.INSTANCE.getShowSaveForNotifications(), R.drawable.msg_tone_add, false);
                    break;
                }
                case 1: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.Reply), CherrygramChatsConfig.INSTANCE.getShowReply(), R.drawable.menu_reply, false);
                    break;
                }
                case 2: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.CG_CopyPhoto), CherrygramChatsConfig.INSTANCE.getShowCopyPhoto(), R.drawable.msg_copy, false);
                    break;
                }
                case 3: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.CG_CopyPhotoAsSticker), CherrygramChatsConfig.INSTANCE.getShowCopyPhotoAsSticker(), R.drawable.msg_sticker, false);
                    break;
                }
                case 4: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.CG_ClearFromCache), CherrygramChatsConfig.INSTANCE.getShowClearFromCache(), R.drawable.clear_cache, false);
                    break;
                }
                case 5: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.Forward), CherrygramChatsConfig.INSTANCE.getShowForward(), R.drawable.msg_forward, false);
                    break;
                }
                case 6: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.Forward) + getString(R.string.CG_Without_Authorship), CherrygramChatsConfig.INSTANCE.getShowForwardWoAuthorship(), R.drawable.msg_forward, false);
                    break;
                }
                case 7: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.CG_ViewUserHistory), CherrygramChatsConfig.INSTANCE.getShowViewHistory(), R.drawable.msg_recent, false);
                    break;
                }
                case 8: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.CG_ToSaved), CherrygramChatsConfig.INSTANCE.getShowSaveMessage(), R.drawable.msg_saved, false);
                    break;
                }
                case 9: {
                    textCell.setTextAndCheckAndIcon(getString(R.string.ReportChat), CherrygramChatsConfig.INSTANCE.getShowReport(), R.drawable.msg_report, false);
                    break;
                }
                case 10: {
                    textCell.setTextAndCheckAndIcon("JSON", CherrygramChatsConfig.INSTANCE.getShowJSON(), R.drawable.msg_info, false);
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
                        CherrygramChatsConfig.INSTANCE.toggleShowSaveForNotifications();
                        textCell.setChecked(CherrygramChatsConfig.INSTANCE.getShowSaveForNotifications());
                        break;
                    }
                    case 1: {
                        CherrygramChatsConfig.INSTANCE.toggleShowReply();
                        textCell.setChecked(CherrygramChatsConfig.INSTANCE.getShowReply());
                        break;
                    }
                    case 2: {
                        CherrygramChatsConfig.INSTANCE.toggleShowCopyPhoto();
                        textCell.setChecked(CherrygramChatsConfig.INSTANCE.getShowCopyPhoto());
                        break;
                    }
                    case 3: {
                        CherrygramChatsConfig.INSTANCE.toggleShowCopyPhotoAsSticker();
                        textCell.setChecked(CherrygramChatsConfig.INSTANCE.getShowCopyPhotoAsSticker());
                        break;
                    }
                    case 4: {
                        CherrygramChatsConfig.INSTANCE.toggleShowClearFromCache();
                        textCell.setChecked(CherrygramChatsConfig.INSTANCE.getShowClearFromCache());
                        break;
                    }
                    case 5: {
                        CherrygramChatsConfig.INSTANCE.toggleShowForward();
                        textCell.setChecked(CherrygramChatsConfig.INSTANCE.getShowForward());
                        break;
                    }
                    case 6: {
                        CherrygramChatsConfig.INSTANCE.toggleShowForwardWoAuthorship();
                        textCell.setChecked(CherrygramChatsConfig.INSTANCE.getShowForwardWoAuthorship());
                        break;
                    }
                    case 7: {
                        CherrygramChatsConfig.INSTANCE.toggleShowViewHistory();
                        textCell.setChecked(CherrygramChatsConfig.INSTANCE.getShowViewHistory());
                        break;
                    }
                    case 8: {
                        CherrygramChatsConfig.INSTANCE.toggleShowSaveMessage();
                        textCell.setChecked(CherrygramChatsConfig.INSTANCE.getShowSaveMessage());
                        break;
                    }
                    case 9: {
                        CherrygramChatsConfig.INSTANCE.toggleShowReport();
                        textCell.setChecked(CherrygramChatsConfig.INSTANCE.getShowReport());
                        break;
                    }
                    case 10: {
                        CherrygramChatsConfig.INSTANCE.toggleShowJSON();
                        textCell.setChecked(CherrygramChatsConfig.INSTANCE.getShowJSON());
                        break;
                    }
                }
                fragment.getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
            });
        }
        builder.setPositiveButton(getString(R.string.OK), null);
        builder.setView(linearLayout);
        fragment.showDialog(builder.create());
    }

}
