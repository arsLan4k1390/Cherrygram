package uz.unnarsx.cherrygram.ui.icons.icon_replaces

import android.util.SparseIntArray
import org.telegram.messenger.R
import uz.unnarsx.cherrygram.ui.icons.newSparseInt

class SolarIconReplace : BaseIconReplace() {
    override val replaces: SparseIntArray = newSparseInt(

            // Dialogs Activity
            R.drawable.list_pin to R.drawable.list_pin_solar,
            R.drawable.msg_markread to R.drawable.msg_markread_solar,
            R.drawable.msg_markunread to R.drawable.msg_markunread_solar,
            R.drawable.chats_pin to R.drawable.msg_pin_solar,
            R.drawable.chats_unpin to R.drawable.unpin_outline_28,
            R.drawable.chats_saved to R.drawable.msg_saved_solar,

            // Drawer
            R.drawable.msg_groups to R.drawable.msg_groups_solar,
            R.drawable.msg_channel to R.drawable.msg_channel_solar,
            R.drawable.msg_contacts to R.drawable.msg_contacts_solar,
            R.drawable.msg_calls to R.drawable.msg_calls_solar,
            R.drawable.msg_archive to R.drawable.msg_archive_solar,
            R.drawable.msg_nearby to R.drawable.msg_nearby_solar,
            R.drawable.msg_qrcode to R.drawable.msg_qrcode_solar,

            // Settings
            R.drawable.msg_settings to R.drawable.msg_settings_solar,
            R.drawable.msg_settings_old to R.drawable.msg_settings_solar,
            R.drawable.msg_notifications to R.drawable.msg_notifications_solar,
            R.drawable.msg2_notifications to R.drawable.msg_notifications_solar,
            R.drawable.msg2_data to R.drawable.msg_data_solar,
            R.drawable.msg2_battery to R.drawable.msg2_battery_solar,
            R.drawable.msg_msgbubble3 to R.drawable.msg_discussion_solar,
            R.drawable.msg2_discussion to R.drawable.msg_discussion_solar,
            R.drawable.msg_sticker to R.drawable.msg_sticker_solar,
            R.drawable.msg2_folder to R.drawable.msg_folder_solar,
            R.drawable.msg_folders to R.drawable.msg_folder_solar,
            R.drawable.msg2_devices to R.drawable.msg_devices_solar,
            R.drawable.menu_devices to R.drawable.msg_devices_solar,
            R.drawable.msg_language to R.drawable.msg_language_solar,
            R.drawable.msg2_language to R.drawable.msg_language_solar,
            R.drawable.msg2_ask_question to R.drawable.msg_ask_question_solar,
            R.drawable.msg_policy to R.drawable.msg_policy_solar,
            R.drawable.msg2_policy to R.drawable.msg_policy_solar,

            // Power Saving
            R.drawable.msg2_sticker to R.drawable.msg_sticker_solar,
            R.drawable.msg2_smile_status to R.drawable.input_smile_solar,
            R.drawable.msg2_call_earpiece to R.drawable.msg_calls_regular_solar,
            R.drawable.msg2_videocall to R.drawable.msg_videocall_solar,
            // Logout activity
            R.drawable.msg_contact_add to R.drawable.msg_contact_add_solar,
            R.drawable.msg_clearcache to R.drawable.msg_delete_solar,
            R.drawable.msg_newphone to R.drawable.msg_newphone_solar,
            R.drawable.msg_help to R.drawable.msg_help_solar,

            // Action Bar SubItems
            //Chats
            R.drawable.ic_ab_other to R.drawable.ic_ab_other_solar, //need to replace with new
            R.drawable.msg_unmute to R.drawable.msg_unmute_solar, //need to replace with new
            R.drawable.msg_mute to R.drawable.msg_mute_solar,
            R.drawable.msg_mute_period to R.drawable.msg_mute_period_solar,
            R.drawable.msg_customize to R.drawable.msg_customize_solar,
            R.drawable.msg_callback to R.drawable.msg_callback_solar,
            R.drawable.msg_videocall to R.drawable.msg_videocall_solar,
            R.drawable.ic_upward to R.drawable.ic_upward_solar,
            R.drawable.msg_clear to R.drawable.msg_clear_solar,
            /*R.drawable.msg_colors to R.drawable.msg_colors_solar,*/
            R.drawable.msg_delete to R.drawable.msg_delete_solar,
            R.drawable.msg_pinnedlist to R.drawable.msg_pinnedlist_solar, // Pinned messages
            //Profile
            R.drawable.profile_newmsg to R.drawable.profile_newmsg_solar,
            R.drawable.profile_video to R.drawable.profile_video_solar,
            R.drawable.ic_call to R.drawable.ic_call_solar,
            R.drawable.msg_calls_regular to R.drawable.msg_calls_regular_solar, //need to replace with new
            R.drawable.msg_info to R.drawable.msg_info_solar,
            R.drawable.msg_share to R.drawable.msg_share_solar,
            R.drawable.msg_block to R.drawable.msg_block_solar,
            R.drawable.group_edit_profile to R.drawable.group_edit_profile_solar,
            R.drawable.msg_addphoto to R.drawable.msg_addphoto_solar,
            R.drawable.msg_leave to R.drawable.msg_leave_solar,
            R.drawable.msg_retry to R.drawable.msg_retry_solar, // Restart Cherrygram
            R.drawable.msg_gift_premium to R.drawable.msg_gift_premium_solar,
            R.drawable.msg_secret to R.drawable.msg_secret_solar,
            R.drawable.msg2_secret to R.drawable.msg_secret_solar,
            R.drawable.msg_discussion to R.drawable.msg_discussion_solar,
            R.drawable.msg_home to R.drawable.msg_home_solar,
            //Topics
            R.drawable.msg_addcontact to R.drawable.msg_contact_add_solar, //need to replace with new
            /*R.drawable.msg_topic_create to R.drawable.msg_topic_create_solar,*/ //need to replace with new
            R.drawable.msg_topic_close to R.drawable.msg_topic_close_solar, //need to replace with new

            // Saved Messages activity
            R.drawable.msg_zoomin to R.drawable.msg_zoomin_solar,
            R.drawable.msg_zoomout to R.drawable.msg_zoomout_solar,
            R.drawable.msg_calendar2 to R.drawable.msg_calendar2_solar,
            R.drawable.msg_message to R.drawable.msg_message_solar,

            // Message menu (Chat)
            R.drawable.msg_tone_add to R.drawable.msg_tone_add_solar,
            R.drawable.msg_reply to R.drawable.msg_reply_solar,
            R.drawable.msg_theme to R.drawable.msg_theme_solar,
            R.drawable.msg_copy to R.drawable.msg_copy_solar,
            R.drawable.msg_download to R.drawable.msg_download_solar,
            R.drawable.msg_shareout to R.drawable.msg_shareout_solar,
            R.drawable.msg_viewreplies to R.drawable.msg_viewreplies_solar,
            R.drawable.msg_link to R.drawable.msg_link_solar,
            R.drawable.msg_fave to R.drawable.msg_fave_solar,
            R.drawable.msg_unfave to R.drawable.msg_unfave_solar,
            R.drawable.msg_gallery to R.drawable.msg_gallery_solar,
            R.drawable.clear_cache to R.drawable.msg_clear_solar,
            R.drawable.msg_forward to R.drawable.msg_forward_solar,
            R.drawable.msg_stats to R.drawable.msg_stats_solar,
            R.drawable.msg_recent to R.drawable.msg_recent_solar,
            R.drawable.msg_saved to R.drawable.msg_saved_solar,
            R.drawable.msg_pin to R.drawable.msg_pin_solar,
            R.drawable.msg_unpin to R.drawable.unpin_outline_28,
            R.drawable.msg_translate to R.drawable.msg_translate_solar,
            R.drawable.msg_report to R.drawable.msg_report_solar,
            R.drawable.msg_edit to R.drawable.msg_edit_solar,

            // Group/Channel edit
            R.drawable.msg_discuss to R.drawable.msg_discuss_solar,
            R.drawable.msg_topics to R.drawable.msg_topics_solar,
            R.drawable.msg_reactions to R.drawable.msg_reactions_solar,
            R.drawable.msg_reactions2 to R.drawable.msg_reactions_solar,
            R.drawable.msg_permissions to R.drawable.msg_permissions_solar,
            R.drawable.msg_link2 to R.drawable.msg_link_1_solar,
            R.drawable.msg_admins to R.drawable.msg_admins_solar,
            R.drawable.msg_admin_add to R.drawable.msg_admin_add_solar,
            R.drawable.msg_log to R.drawable.msg_log_solar,
            R.drawable.msg_notspam to R.drawable.msg_notspam_solar,

            // Message panel
            R.drawable.msg_panel_reply to R.drawable.msg_forward_solar,
            R.drawable.input_bot1 to R.drawable.input_bot1_solar,
            R.drawable.input_bot2 to R.drawable.input_bot2_solar,
            R.drawable.input_attach to R.drawable.input_attach_solar,
            R.drawable.msg_send to R.drawable.msg_send_solar,
            R.drawable.attach_send to R.drawable.attach_send_solar,
            R.drawable.ic_send to R.drawable.attach_send_solar,
            R.drawable.input_schedule to R.drawable.input_schedule_solar,
//            R.drawable.input_notify_off to R.drawable.input_notify_off_solar, //need to replace with new
            R.drawable.input_notify_on to R.drawable.input_notify_on_solar, //need to replace with new
            //Emoji panel
            R.drawable.msg_emoji_recent to R.drawable.msg_emoji_recent_solar,
//            R.drawable.smiles_tab_gif to R.drawable.picture_outline_28,
            R.drawable.input_keyboard to R.drawable.input_keyboard_solar,
            R.drawable.smiles_tab_clear to R.drawable.smiles_tab_clear_solar,

            // Photo/Video viewer
            R.drawable.share to R.drawable.msg_filled_shareout_solar, //need to replace with new
            R.drawable.input_smile to R.drawable.input_smile_solar,
            R.drawable.msg_openin to R.drawable.msg_openin_solar,
            R.drawable.msg_list to R.drawable.msg_list_solar, //need to replace with new

            // Unsorted
            R.drawable.msg_location to R.drawable.msg_location_solar,
            R.drawable.group_edit to R.drawable.group_edit_profile_solar, //need to replace with new
            R.drawable.msg_filled_shareout to R.drawable.msg_filled_shareout_solar,
            R.drawable.msg_contacts_time to R.drawable.msg_contacts_time_solar, // contacts activity
            R.drawable.msg_contacts_name to R.drawable.msg_contacts_name_solar, // contacts activity
            R.drawable.burn to R.drawable.burn_solar, // secret photos
            R.drawable.msg_viewchats to R.drawable.msg_discuss_solar, // maybe topics
            R.drawable.msg_sendfile to R.drawable.msg_sendfile_solar, // updater bottom sheet
            R.drawable.msg_mini_qr to R.drawable.msg_mini_qr_solar, // active sessions QR
            R.drawable.msg_qr_mini to R.drawable.msg_qr_mini_solar, // ProfileActivity QR
            R.drawable.msg_view_file to R.drawable.msg_message_solar, // Cache settings

            /*R.drawable.msg_gif to R.drawable.airplay_video_outline_28,
            R.drawable.msg_search to R.drawable.magnifier,
            R.drawable.ic_ab_search to R.drawable.magnifier,*/
    )
}
