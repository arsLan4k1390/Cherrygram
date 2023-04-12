package uz.unnarsx.cherrygram.preferences

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import org.telegram.messenger.LocaleController
import org.telegram.messenger.NotificationCenter
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Cells.TextCell
import org.telegram.ui.Components.LayoutHelper
import uz.unnarsx.cherrygram.CherrygramConfig

class AlertDialogSwitchHelper {
    companion object {
        fun showDirectShareAlert(bf: BaseFragment) {
            if (bf.parentActivity == null) {
                return
            }
            val context: Context = bf.parentActivity
            val builder = AlertDialog.Builder(context)
            builder.setTitle(LocaleController.getString("DirectShare", R.string.DirectShare))

            val linearLayout = LinearLayout(context)
            linearLayout.orientation = LinearLayout.VERTICAL
            val linearLayoutInviteContainer = LinearLayout(context)
            linearLayoutInviteContainer.orientation = LinearLayout.VERTICAL
            linearLayout.addView(linearLayoutInviteContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT))

            val count = 5
            for (a in 0 until count) {
                val textCell = TextCell(context, 23, false, true, bf.resourceProvider)
                textCell.isEnabled = true
                when (a) {
                    0 -> {
                        textCell.setTextAndCheck(LocaleController.getString("FilterChats", R.string.FilterChats), CherrygramConfig.usersDrawShareButton, false)
                    }
                    1 -> {
                        textCell.setTextAndCheck(LocaleController.getString("FilterGroups", R.string.FilterGroups), CherrygramConfig.supergroupsDrawShareButton, false)
                    }
                    2 -> {
                        textCell.setTextAndCheck(LocaleController.getString("FilterChannels", R.string.FilterChannels), CherrygramConfig.channelsDrawShareButton, false)
                    }
                    3 -> {
                        textCell.setTextAndCheck(LocaleController.getString("FilterBots", R.string.FilterBots), CherrygramConfig.botsDrawShareButton, false)
                    }
                    4 -> {
                        textCell.setTextAndCheck(LocaleController.getString("StickersName", R.string.StickersName), CherrygramConfig.stickersDrawShareButton, false)
                    }
                }
                textCell.tag = a
                textCell.background = Theme.getSelectorDrawable(false)
                linearLayoutInviteContainer.addView(textCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT))

                textCell.setOnClickListener { v2: View ->
                    when (v2.tag as Int) {
                        0 -> {
                            CherrygramConfig.toggleUsersDrawShareButton()
                            textCell.isChecked = CherrygramConfig.usersDrawShareButton
                        }
                        1 -> {
                            CherrygramConfig.toggleSupergroupsDrawShareButton()
                            textCell.isChecked = CherrygramConfig.supergroupsDrawShareButton
                        }
                        2 -> {
                            CherrygramConfig.toggleChannelsDrawShareButton()
                            textCell.isChecked = CherrygramConfig.channelsDrawShareButton
                        }
                        3 -> {
                            CherrygramConfig.toggleBotsDrawShareButton()
                            textCell.isChecked = CherrygramConfig.botsDrawShareButton
                        }
                        4 -> {
                            CherrygramConfig.toggleStickersDrawShareButton()
                            textCell.isChecked = CherrygramConfig.stickersDrawShareButton
                        }
                    }
                    bf.notificationCenter.postNotificationName(NotificationCenter.mainUserInfoChanged)
                }
            }
            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null)
            builder.setView(linearLayout)
            bf.showDialog(builder.create())
        }

        fun showDrawerIconsAlert(bf: BaseFragment) {
            if (bf.parentActivity == null) {
                return
            }
            val context: Context = bf.parentActivity
            val builder = AlertDialog.Builder(context)
            builder.setTitle(LocaleController.getString("AP_DrawerButtonsCategory", R.string.AP_DrawerButtonsCategory))

            val linearLayout = LinearLayout(context)
            linearLayout.orientation = LinearLayout.VERTICAL
            val linearLayoutInviteContainer = LinearLayout(context)
            linearLayoutInviteContainer.orientation = LinearLayout.VERTICAL
            linearLayout.addView(linearLayoutInviteContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT))

            val count = 10
            for (a in 0 until count) {
                val textCell = TextCell(context, 23, false, true, bf.resourceProvider)
                when (a) {
                    0 -> {
                        textCell.setTextAndCheckAndIcon(
                            LocaleController.getString("NewGroup", R.string.NewGroup), CherrygramConfig.createGroupDrawerButton, R.drawable.msg_groups, false
                        )
                    }
                    1 -> {
                        textCell.setTextAndCheckAndIcon(
                            LocaleController.getString("NewSecretChat", R.string.NewSecretChat), CherrygramConfig.secretChatDrawerButton, R.drawable.msg_secret, false
                        )
                    }
                    2 -> {
                        textCell.setTextAndCheckAndIcon(
                            LocaleController.getString("NewChannel", R.string.NewChannel), CherrygramConfig.createChannelDrawerButton, R.drawable.msg_channel, false
                        )
                    }
                    3 -> {
                        textCell.setTextAndCheckAndIcon(
                            LocaleController.getString("Contacts", R.string.Contacts), CherrygramConfig.contactsDrawerButton, R.drawable.msg_contacts, false
                        )
                    }
                    4 -> {
                        textCell.setTextAndCheckAndIcon(
                            LocaleController.getString("Calls", R.string.Calls), CherrygramConfig.callsDrawerButton, R.drawable.msg_calls, false
                        )
                    }
                    5 -> {
                        textCell.setTextAndCheckAndIcon(
                            LocaleController.getString("SavedMessages", R.string.SavedMessages), CherrygramConfig.savedMessagesDrawerButton, R.drawable.msg_saved, false
                        )
                    }
                    6 -> {
                        textCell.setTextAndCheckAndIcon(
                            LocaleController.getString("ArchivedChats", R.string.ArchivedChats), CherrygramConfig.archivedChatsDrawerButton, R.drawable.msg_archive, false
                        )
                    }
                    7 -> {
                        textCell.setTextAndCheckAndIcon(
                            LocaleController.getString("PeopleNearby", R.string.PeopleNearby), CherrygramConfig.peopleNearbyDrawerButton, R.drawable.msg_nearby, false
                        )
                    }
                    8 -> {
                        textCell.setTextAndCheckAndIcon(
                            LocaleController.getString("AuthAnotherClient", R.string.AuthAnotherClient), CherrygramConfig.scanQRDrawerButton, R.drawable.msg_qrcode, false
                        )
                    }
                    9 -> {
                        textCell.setTextAndCheckAndIcon(
                            LocaleController.getString("CGP_AdvancedSettings", R.string.CGP_AdvancedSettings), CherrygramConfig.cGPreferencesDrawerButton, R.drawable.msg_settings, false
                        )
                    }
                }
                textCell.tag = a
                textCell.background = Theme.getSelectorDrawable(false)
                linearLayoutInviteContainer.addView(textCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT))

                textCell.setOnClickListener { v2: View ->
                    when (v2.tag as Int) {
                        0 -> {
                            CherrygramConfig.toggleCreateGroupDrawerButton()
                            textCell.isChecked = CherrygramConfig.createGroupDrawerButton
                        }
                        1 -> {
                            CherrygramConfig.toggleSecretChatDrawerButton()
                            textCell.isChecked = CherrygramConfig.secretChatDrawerButton
                        }
                        2 -> {
                            CherrygramConfig.toggleCreateChannelDrawerButton()
                            textCell.isChecked = CherrygramConfig.createChannelDrawerButton
                        }
                        3 -> {
                            CherrygramConfig.toggleContactsDrawerButton()
                            textCell.isChecked = CherrygramConfig.contactsDrawerButton
                        }
                        4 -> {
                            CherrygramConfig.toggleCallsDrawerButton()
                            textCell.isChecked = CherrygramConfig.callsDrawerButton
                        }
                        5 -> {
                            CherrygramConfig.toggleSavedMessagesDrawerButton()
                            textCell.isChecked = CherrygramConfig.savedMessagesDrawerButton
                        }
                        6 -> {
                            CherrygramConfig.toggleArchivedChatsDrawerButton()
                            textCell.isChecked = CherrygramConfig.archivedChatsDrawerButton
                        }
                        7 -> {
                            CherrygramConfig.togglePeopleNearbyDrawerButton()
                            textCell.isChecked = CherrygramConfig.peopleNearbyDrawerButton
                        }
                        8 -> {
                            CherrygramConfig.toggleScanQRDrawerButton()
                            textCell.isChecked = CherrygramConfig.scanQRDrawerButton
                        }
                        9 -> {
                            CherrygramConfig.toggleCGPreferencesDrawerButton()
                            textCell.isChecked = CherrygramConfig.cGPreferencesDrawerButton
                        }
                    }
                    bf.notificationCenter.postNotificationName(NotificationCenter.mainUserInfoChanged)
                }
            }
            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null)
            builder.setView(linearLayout)
            bf.showDialog(builder.create())
        }

        fun showChatMenuIconsAlert(bf: BaseFragment) {
            if (bf.parentActivity == null) {
                return
            }
            val context: Context = bf.parentActivity
            val builder = AlertDialog.Builder(context)
            builder.setTitle(LocaleController.getString("CP_MessageMenu", R.string.CP_MessageMenu))

            val linearLayout = LinearLayout(context)
            linearLayout.orientation = LinearLayout.VERTICAL
            val linearLayoutInviteContainer = LinearLayout(context)
            linearLayoutInviteContainer.orientation = LinearLayout.VERTICAL
            linearLayout.addView(linearLayoutInviteContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT))

            val count = 7
            for (a in 0 until count) {
                val textCell = TextCell(context, 23, false, true, bf.resourceProvider)
                when (a) {
                    0 -> {
                        textCell.setTextAndCheckAndIcon(
                            LocaleController.getString("Reply", R.string.Reply), CherrygramConfig.showReply, R.drawable.msg_reply, false
                        )
                    }
                    1 -> {
                        textCell.setTextAndCheckAndIcon(
                            LocaleController.getString("CG_ClearFromCache", R.string.CG_ClearFromCache), CherrygramConfig.showClearFromCache, R.drawable.clear_cache, false
                        )
                    }
                    2 -> {
                        textCell.setTextAndCheckAndIcon(
                            LocaleController.getString("Forward", R.string.Forward), CherrygramConfig.showForward, R.drawable.msg_forward, false
                        )
                    }
                    3 -> {
                        textCell.setTextAndCheckAndIcon(
                            LocaleController.getString("Forward", R.string.Forward) + " " + LocaleController.getString("CG_Without_Authorship", R.string.CG_Without_Authorship),
                            CherrygramConfig.showForwardWoAuthorship, R.drawable.msg_forward, false
                        )
                    }
                    4 -> {
                        textCell.setTextAndCheckAndIcon(
                            LocaleController.getString("CG_ViewUserHistory", R.string.CG_ViewUserHistory), CherrygramConfig.showViewHistory, R.drawable.msg_recent, false
                        )
                    }
                    5 -> {
                        textCell.setTextAndCheckAndIcon(
                            LocaleController.getString("CG_ToSaved", R.string.CG_ToSaved), CherrygramConfig.showSaveMessage, R.drawable.msg_saved, false
                        )
                    }
                    6 -> {
                        textCell.setTextAndCheckAndIcon(
                            LocaleController.getString("ReportChat", R.string.ReportChat), CherrygramConfig.showReport, R.drawable.msg_report, false
                        )
                    }
                }
                textCell.tag = a
                textCell.background = Theme.getSelectorDrawable(false)
                linearLayoutInviteContainer.addView(textCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT))

                textCell.setOnClickListener { v2: View ->
                    when (v2.tag as Int) {
                        0 -> {
                            CherrygramConfig.toggleShowReply()
                            textCell.isChecked = CherrygramConfig.showReply
                        }
                        1 -> {
                            CherrygramConfig.toggleShowClearFromCache()
                            textCell.isChecked = CherrygramConfig.showClearFromCache
                        }
                        2 -> {
                            CherrygramConfig.toggleShowForward()
                            textCell.isChecked = CherrygramConfig.showForward
                        }
                        3 -> {
                            CherrygramConfig.toggleShowForwardWoAuthorship()
                            textCell.isChecked = CherrygramConfig.showForwardWoAuthorship
                        }
                        4 -> {
                            CherrygramConfig.toggleShowViewHistory()
                            textCell.isChecked = CherrygramConfig.showViewHistory
                        }
                        5 -> {
                            CherrygramConfig.toggleShowSaveMessage()
                            textCell.isChecked = CherrygramConfig.showSaveMessage
                        }
                        6 -> {
                            CherrygramConfig.toggleShowReport()
                            textCell.isChecked = CherrygramConfig.showReport
                        }
                    }
                    bf.notificationCenter.postNotificationName(NotificationCenter.mainUserInfoChanged)
                }
            }
            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null)
            builder.setView(linearLayout)
            bf.showDialog(builder.create())
        }
    }
}