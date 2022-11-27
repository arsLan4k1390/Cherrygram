package uz.unnarsx.cherrygram.vkui.icon_replaces

import android.util.SparseIntArray
import org.telegram.messenger.R
import uz.unnarsx.cherrygram.vkui.newSparseInt

class SolarIconReplace : BaseIconReplace() {
    override val replaces: SparseIntArray = newSparseInt(

            // Dialogs Activity
            R.drawable.list_pin to R.drawable.pin_circle,
            R.drawable.msg_markread to R.drawable.chat_round_check,
            R.drawable.msg_markunread to R.drawable.chat_round_unread,
            R.drawable.chats_pin to R.drawable.pin_outline,
            /*R.drawable.chats_unpin to R.drawable.pin_outline,*/

            // Drawer
            R.drawable.msg_add to R.drawable.user_plus,
            R.drawable.msg_groups to R.drawable.users_group_rounded,
            /*R.drawable.msg_channel to R.drawable.advertising_outline_28,*/
            R.drawable.msg_contacts to R.drawable.user,
            R.drawable.msg_calls to R.drawable.phone_rounded,
            R.drawable.msg_archive to R.drawable.archive,
            R.drawable.msg_nearby to R.drawable.users_group_two_rounded,
            R.drawable.msg_qrcode to R.drawable.qr_code,

            // Settings
            R.drawable.msg_settings to R.drawable.settings,
            R.drawable.msg_settings_old to R.drawable.settings,
            R.drawable.msg_notifications to R.drawable.bell,
            R.drawable.msg_data to R.drawable.pie_chart_2,
            R.drawable.msg_msgbubble3 to R.drawable.chat_round,
            R.drawable.msg_sticker to R.drawable.sticker_smile_square,
            R.drawable.msg_folder to R.drawable.folder,
            R.drawable.msg_folders to R.drawable.folder,
            R.drawable.msg_devices to R.drawable.monitor_smartphone,
            R.drawable.menu_devices to R.drawable.monitor_smartphone,
            R.drawable.msg_language to R.drawable.global,
            R.drawable.msg_ask_question to R.drawable.chat_round_dots,
            R.drawable.msg_policy to R.drawable.shield_check,
            // Logout activity
            R.drawable.msg_contact_add to R.drawable.user_plus,
            R.drawable.msg_clearcache to R.drawable.trash_bin_2,
            R.drawable.msg_newphone to R.drawable.login,
            R.drawable.msg_help to R.drawable.question_circle,

            // Action Bar SubItems
            //Chats
            R.drawable.ic_ab_back to R.drawable.alt_arrow_left,
            R.drawable.msg_arrow_back to R.drawable.alt_arrow_left,
            R.drawable.calls_back to R.drawable.alt_arrow_left,
            R.drawable.ic_ab_other to R.drawable.menu_dots,
            R.drawable.msg_unmute to R.drawable.volume_loud,
            R.drawable.msg_mute to R.drawable.volume_cross,
            R.drawable.msg_mute_period to R.drawable.bell_off,
            R.drawable.msg_customize to R.drawable.tuning_2,
            R.drawable.msg_callback to R.drawable.phone_rounded,
            R.drawable.msg_videocall to R.drawable.videocamera,
            R.drawable.ic_upward to R.drawable.export,
            R.drawable.msg_clear to R.drawable.broom,
            /*R.drawable.msg_colors to R.drawable.broom,*/
            R.drawable.msg_delete to R.drawable.trash_bin_2,
            R.drawable.msg_pinnedlist to R.drawable.pin_list, // Pinend messages
            //Profile
            R.drawable.profile_video to R.drawable.videocamera,
            R.drawable.ic_call to R.drawable.phone_rounded,
            R.drawable.msg_info to R.drawable.info_circle,
            R.drawable.msg_share to R.drawable.forward,
            R.drawable.msg_block to R.drawable.forbidden_circle,
            R.drawable.group_edit_profile to R.drawable.pen,
            R.drawable.msg_addphoto to R.drawable.camera_add,
            R.drawable.msg_leave to R.drawable.leave,
            R.drawable.msg_retry to R.drawable.refresh, // Restart Cherrygram
            R.drawable.msg_gift_premium to R.drawable.gift,
            R.drawable.msg_secret to R.drawable.lock_keyhole,
            R.drawable.msg_discussion to R.drawable.chat_round,
            R.drawable.msg_home to R.drawable.home_add,
            //Topics
            R.drawable.msg_addcontact to R.drawable.user_plus,
            R.drawable.msg_topic_create to R.drawable.dialog_2,
            R.drawable.msg_topic_close to R.drawable.minus_circle,

            // Saved Messages activity
            R.drawable.msg_zoomin to R.drawable.magnifer_zoom_in,
            R.drawable.msg_zoomout to R.drawable.magnifer_zoom_out,
            R.drawable.msg_calendar2 to R.drawable.calendar_minimalistic,
            R.drawable.msg_message to R.drawable.eye,

            // Message menu (Chat)
            R.drawable.msg_tone_add to R.drawable.music_notes,
            R.drawable.msg_reply to R.drawable.reply,
            R.drawable.msg_theme to R.drawable.pallete_2,
            R.drawable.msg_copy to R.drawable.copy,
            R.drawable.msg_download to R.drawable.download_minimalistic,
            R.drawable.msg_shareout to R.drawable.share_outline,
            R.drawable.msg_viewreplies to R.drawable.chat_square_arrow,
            R.drawable.msg_link to R.drawable.link,
            R.drawable.msg_fave to R.drawable.star,
            /*R.drawable.msg_unfave to R.drawable.star,*/
            R.drawable.msg_gallery to R.drawable.gallery_download,
            R.drawable.clear_cache to R.drawable.broom,
            R.drawable.msg_forward to R.drawable.forward,
            R.drawable.msg_stats to R.drawable.course_up,
            R.drawable.msg_recent to R.drawable.clock_circle,
            R.drawable.msg_saved to R.drawable.bookmark,
            R.drawable.msg_pin to R.drawable.pin_outline,
            /*R.drawable.msg_unpin to R.drawable.unpin_outline_28,*/
            /*R.drawable.msg_translate to R.drawable.hieroglyph_character_outline_28,*/
            R.drawable.msg_report to R.drawable.danger_circle,
            R.drawable.msg_edit to R.drawable.pen,

            // Group/Channel edit
            R.drawable.msg_discuss to R.drawable.chat_round_line,
            /*R.drawable.msg_topics to R.drawable.list_outline_28,*/
            R.drawable.msg_reactions to R.drawable.heart,
            R.drawable.msg_reactions2 to R.drawable.heart,
            R.drawable.msg_permissions to R.drawable.key,
            R.drawable.msg_link2 to R.drawable.link,
            R.drawable.msg_admins to R.drawable.medal_ribbon_star,
            R.drawable.msg_admin_add to R.drawable.medal_ribbon_star,
            R.drawable.msg_log to R.drawable.clipboard,

            // Message panel
            R.drawable.msg_panel_reply to R.drawable.forward,
            R.drawable.input_bot1 to R.drawable.slash_square,
            R.drawable.input_bot2 to R.drawable.code_scan,
            R.drawable.input_attach to R.drawable.paperclip,
            R.drawable.input_mic to R.drawable.microphone,
            R.drawable.input_video to R.drawable.camera_square,
            R.drawable.msg_send to R.drawable.plain,
            R.drawable.attach_send to R.drawable.plain,
            R.drawable.ic_send to R.drawable.plain,
            R.drawable.input_schedule to R.drawable.calendar_mark,
            R.drawable.input_notify_off to R.drawable.bell_off,
            R.drawable.input_notify_on to R.drawable.bell,
            //Emoji panel
            R.drawable.msg_emoji_recent to R.drawable.clock_circle,
            R.drawable.smiles_tab_smiles to R.drawable.smile_circle,
            //R.drawable.smiles_tab_gif to R.drawable.picture_outline_28,
            R.drawable.smiles_tab_stickers to R.drawable.sticker_smile_square,
            R.drawable.input_keyboard to R.drawable.keyboard,
            R.drawable.smiles_tab_clear to R.drawable.backspace,

            // Photo/Video viewer
            R.drawable.share to R.drawable.share_outline,
            R.drawable.input_smile to R.drawable.smile_circle,
            R.drawable.msg_openin to R.drawable.square_top_down,
            R.drawable.msg_list to R.drawable.hamburger_menu,

            // Unsorted
            R.drawable.msg_location to R.drawable.map_point,
            R.drawable.group_edit to R.drawable.pen,
            R.drawable.msg_filled_shareout to R.drawable.share_outline,
            R.drawable.msg_contacts_time to R.drawable.sort_by_time, // contacts activity
            R.drawable.msg_contacts_name to R.drawable.sort_by_alphabet, // contacts activity
            R.drawable.burn to R.drawable.fire, // secret photos
            R.drawable.msg_viewchats to R.drawable.dialog, // maybe topics
            R.drawable.msg_sendfile to R.drawable.file, // updater bottom sheet
            R.drawable.msg_mini_qr to R.drawable.qr_code, // active sessions QR
            R.drawable.msg_qr_mini to R.drawable.qr_code, // ProfileActivity QR
            /*R.drawable.msg_gif to R.drawable.airplay_video_outline_28,
            R.drawable.msg_search to R.drawable.magnifer,
            R.drawable.ic_ab_search to R.drawable.magnifer,*/
    )
}
