package uz.unnarsx.cherrygram

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import kotlinx.coroutines.*
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.MessagesController
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManagerImpl
import org.telegram.tgnet.TLRPC
import uz.unnarsx.cherrygram.helpers.LocalVerificationsHelper
import uz.unnarsx.cherrygram.helpers.CherrygramToasts
import uz.unnarsx.cherrygram.helpers.FirebaseAnalyticsHelper
import uz.unnarsx.cherrygram.preferences.boolean
import uz.unnarsx.cherrygram.preferences.int
import uz.unnarsx.cherrygram.preferences.long
import uz.unnarsx.cherrygram.preferences.string
import uz.unnarsx.cherrygram.helpers.StickersHelper
import uz.unnarsx.cherrygram.ui.icons.icon_replaces.BaseIconReplace
import uz.unnarsx.cherrygram.ui.icons.icon_replaces.NoIconReplace
import uz.unnarsx.cherrygram.ui.icons.icon_replaces.SolarIconReplace
import uz.unnarsx.cherrygram.ui.icons.icon_replaces.VkIconReplace

object CherrygramConfig: CoroutineScope by MainScope() {

    private val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)

    fun putBoolean(key: String, value: Boolean) {
        val preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun putStringForUserPrefs(key: String, value: String) {
        val preferences = MessagesController.getMainSettings(UserConfig.selectedAccount)
        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    // General Settings
    //General
    var noRounding by sharedPreferences.boolean("CP_NoRounding", false)
    var systemEmoji by sharedPreferences.boolean("AP_SystemEmoji", false)
    var systemFonts by sharedPreferences.boolean("AP_SystemFonts", true)
    var oldNotificationIcon by sharedPreferences.boolean("AP_Old_Notification_Icon", false)

    //Animations and Premium Features
    var hideStories by sharedPreferences.boolean("CP_HideStories", false)
    var customWallpapers by sharedPreferences.boolean("CP_CustomWallpapers", true)
    var disableAnimatedAvatars by sharedPreferences.boolean("CP_DisableAnimAvatars", false)
    var disableReactionsOverlay by sharedPreferences.boolean("CP_DisableReactionsOverlay", false)
    var disableReactionAnim by sharedPreferences.boolean("CP_DisableReactionAnim", false)
    var disablePremStickAnim by sharedPreferences.boolean("CP_DisablePremStickAnim", false)
    var disablePremStickAutoPlay by sharedPreferences.boolean("CP_DisablePremStickAutoPlay", false)
    var hideSendAsChannel by sharedPreferences.boolean("CP_HideSendAsChannel", false)

    // Appearance Settings
    //Redesign
    const val ICON_REPLACE_NONE = 0
    const val ICON_REPLACE_VKUI = 1
    const val ICON_REPLACE_SOLAR = 2

    var iconReplacement by sharedPreferences.int("AP_Icon_Replacements", ICON_REPLACE_SOLAR)
    fun getCurrentIconPack(): BaseIconReplace {
        return when (iconReplacement) {
            ICON_REPLACE_VKUI -> VkIconReplace()
            ICON_REPLACE_SOLAR -> SolarIconReplace()
            else -> NoIconReplace()
        }
    }

    var oneUI_SwitchStyle by sharedPreferences.boolean("AP_OneUI_SwitchStyle", true)
    var centerTitle by sharedPreferences.boolean("AP_CenterTitle", true)
    var disableToolBarShadow by sharedPreferences.boolean("AP_ToolBarShadow", true)
    var disableDividers by sharedPreferences.boolean("AP_DisableDividers", true)
    var overrideHeaderColor by sharedPreferences.boolean("AP_OverrideHeaderColor", true)
    var flatNavbar by sharedPreferences.boolean("AP_FlatNavBar", true)

    //Messages and profiles
    var showSeconds by sharedPreferences.boolean("CP_ShowSeconds", false)
    fun toggleShowSeconds() {
        showSeconds = !showSeconds
        putBoolean("CP_ShowSeconds", showSeconds)
    }

    var disablePremiumStatuses by sharedPreferences.boolean("CP_DisablePremiumStatuses", false)
    fun toggleDisablePremiumStatuses() {
        disablePremiumStatuses = !disablePremiumStatuses
        putBoolean("CP_DisablePremiumStatuses", disablePremiumStatuses)
    }

    var replyBackground by sharedPreferences.boolean("CP_ReplyBackground", true)
    fun toggleReplyBackground() {
        replyBackground = !replyBackground
        putBoolean("CP_ReplyBackground", replyBackground)
    }

    var replyCustomColors by sharedPreferences.boolean("CP_ReplyCustomColors", true)
    fun toggleReplyCustomColors() {
        replyCustomColors = !replyCustomColors
        putBoolean("CP_ReplyCustomColors", replyCustomColors)
    }

    var replyBackgroundEmoji by sharedPreferences.boolean("CP_ReplyBackgroundEmoji", true)
    fun toggleReplyBackgroundEmoji() {
        replyBackgroundEmoji = !replyBackgroundEmoji
        putBoolean("CP_ReplyBackgroundEmoji", replyBackgroundEmoji)
    }

    var profileChannelPreview by sharedPreferences.boolean("CP_ProfileChannelPreview", true)
    fun toggleProfileChannelPreview() {
        profileChannelPreview = !profileChannelPreview
        putBoolean("CP_ProfileChannelPreview", profileChannelPreview)
    }

    const val ID_DC_NONE = 0
    const val ID_ONLY = 1
    const val ID_DC = 2
    var showIDDC by sharedPreferences.int("AP_ShowID_DC", ID_DC_NONE)

    var profileBirthDatePreview by sharedPreferences.boolean("CP_ProfileBirthDatePreview", true)
    fun toggleProfileBirthDatePreview() {
        profileBirthDatePreview = !profileBirthDatePreview
        putBoolean("CP_ProfileBirthDatePreview", profileBirthDatePreview)
    }

    var profileBusinessPreview by sharedPreferences.boolean("CP_ProfileBusinessPreview", true)
    fun toggleProfileBusinessPreview() {
        profileBusinessPreview = !profileBusinessPreview
        putBoolean("CP_ProfileBusinessPreview", profileBusinessPreview)
    }

    var profileBackgroundColor by sharedPreferences.boolean("CP_ProfileBackgroundColor", true)
    fun toggleProfileBackgroundColor() {
        profileBackgroundColor = !profileBackgroundColor
        putBoolean("CP_ProfileBackgroundColor", profileBackgroundColor)
    }

    var profileBackgroundEmoji by sharedPreferences.boolean("CP_ProfileBackgroundEmoji", true)
    fun toggleProfileBackgroundEmoji() {
        profileBackgroundEmoji = !profileBackgroundEmoji
        putBoolean("CP_ProfileBackgroundEmoji", profileBackgroundEmoji)
    }

    //Folders
    var folderNameInHeader by sharedPreferences.boolean("AP_FolderNameInHeader", false)
    fun toggleFolderNameInHeader() {
        folderNameInHeader = !folderNameInHeader
        putBoolean("AP_FolderNameInHeader", folderNameInHeader)
    }

    var tabsHideAllChats by sharedPreferences.boolean("CP_NewTabs_RemoveAllChats", false)
    fun toggleTabsHideAllChats() {
        tabsHideAllChats = !tabsHideAllChats
        putBoolean("CP_NewTabs_RemoveAllChats", tabsHideAllChats)
    }

    var tabsNoUnread by sharedPreferences.boolean("CP_NewTabs_NoCounter", false)
    fun toggleTabsNoUnread() {
        tabsNoUnread = !tabsNoUnread
        putBoolean("CP_NewTabs_NoCounter", tabsNoUnread)
    }

    const val TAB_TYPE_MIX = 0
    const val TAB_TYPE_TEXT = 1
    const val TAB_TYPE_ICON = 2
    var tabMode by sharedPreferences.int("AP_TabMode", 1)

    const val TAB_STYLE_DEFAULT = 0
    const val TAB_STYLE_ROUNDED = 1
    const val TAB_STYLE_TEXT = 2
    const val TAB_STYLE_VKUI = 3
    const val TAB_STYLE_PILLS = 4
    var tabStyle by sharedPreferences.int("AP_TabStyle", TAB_STYLE_ROUNDED)

    var tabStyleStroke by sharedPreferences.boolean("AP_TabStyleAddStroke", false)
    fun toggleTabStyleStroke() {
        tabStyleStroke = !tabStyleStroke
        putBoolean("AP_TabStyleAddStroke", tabStyleStroke)
    }

    //Drawer
    var drawSnowInDrawer by sharedPreferences.boolean("AP_DrawSnowInDrawer", false)
    fun toggleDrawerSnow() {
        drawSnowInDrawer = !drawSnowInDrawer
        putBoolean("AP_DrawSnowInDrawer", drawSnowInDrawer)
    }

    var drawerAvatar by sharedPreferences.boolean("AP_DrawerAvatar", true)
    fun toggleDrawerAvatar() {
        drawerAvatar = !drawerAvatar
        putBoolean("AP_DrawerAvatar", drawerAvatar)
    }

    var drawerSmallAvatar by sharedPreferences.boolean("AP_DrawerSmallAvatar", false)
    fun toggleDrawerSmallAvatar() {
        drawerSmallAvatar = !drawerSmallAvatar
        putBoolean("AP_DrawerSmallAvatar", drawerSmallAvatar)
    }

    var drawerDarken by sharedPreferences.boolean("AP_DrawerDarken", true)
    fun toggleDrawerDarken() {
        drawerDarken = !drawerDarken
        putBoolean("AP_DrawerDarken", drawerDarken)
    }

    var drawerGradient by sharedPreferences.boolean("AP_DrawerGradient", false)
    fun toggleDrawerGradient() {
        drawerGradient = !drawerGradient
        putBoolean("AP_DrawerGradient", drawerGradient)
    }

    var drawerBlur by sharedPreferences.boolean("AP_DrawerBlur", true)
    fun toggleDrawerBlur() {
        drawerBlur = !drawerBlur
        putBoolean("AP_DrawerBlur", drawerBlur)
    }

    var drawerBlurIntensity by sharedPreferences.int("AP_DrawerBlur_Intensity", 50)
    var eventType by sharedPreferences.int("AP_DrawerEventType", 0)

    //Drawer buttons
    var changeStatusDrawerButton by sharedPreferences.boolean("AP_ChangeStatusDrawerButton", true)
    fun toggleChangeStatusDrawerButton() {
        changeStatusDrawerButton = !changeStatusDrawerButton
        putBoolean("AP_ChangeStatusDrawerButton", changeStatusDrawerButton)
    }

    /*var myStoriesDrawerButton by sharedPreferences.boolean("AP_MyStoriesDrawerButton", true)
    fun toggleMyStoriesDrawerButton() {
        myStoriesDrawerButton = !myStoriesDrawerButton
        putBoolean("AP_MyStoriesDrawerButton", myStoriesDrawerButton)
    }*/

    var myProfileDrawerButton by sharedPreferences.boolean("AP_MyProfileDrawerButton", true)
    fun toggleMyProfileDrawerButton() {
        myProfileDrawerButton = !myProfileDrawerButton
        putBoolean("AP_MyProfileDrawerButton", myProfileDrawerButton)
    }

    var createGroupDrawerButton by sharedPreferences.boolean("AP_CreateGroupDrawerButton", false)
    fun toggleCreateGroupDrawerButton() {
        createGroupDrawerButton = !createGroupDrawerButton
        putBoolean("AP_CreateGroupDrawerButton", createGroupDrawerButton)
    }

    var secretChatDrawerButton by sharedPreferences.boolean("AP_SecretChatDrawerButton", false)
    fun toggleSecretChatDrawerButton() {
        secretChatDrawerButton = !secretChatDrawerButton
        putBoolean("AP_SecretChatDrawerButton", secretChatDrawerButton)
    }

    var createChannelDrawerButton by sharedPreferences.boolean("AP_CreateChannelDrawerButton", false)
    fun toggleCreateChannelDrawerButton() {
        createChannelDrawerButton = !createChannelDrawerButton
        putBoolean("AP_CreateChannelDrawerButton", createChannelDrawerButton)
    }

    var contactsDrawerButton by sharedPreferences.boolean("AP_ContactsDrawerButton", false)
    fun toggleContactsDrawerButton() {
        contactsDrawerButton = !contactsDrawerButton
        putBoolean("AP_ContactsDrawerButton", contactsDrawerButton)
    }

    var callsDrawerButton by sharedPreferences.boolean("AP_CallsDrawerButton", true)
    fun toggleCallsDrawerButton() {
        callsDrawerButton = !callsDrawerButton
        putBoolean("AP_CallsDrawerButton", callsDrawerButton)
    }

    var savedMessagesDrawerButton by sharedPreferences.boolean("AP_SavedMessagesDrawerButton", true)
    fun toggleSavedMessagesDrawerButton() {
        savedMessagesDrawerButton = !savedMessagesDrawerButton
        putBoolean("AP_SavedMessagesDrawerButton", savedMessagesDrawerButton)
    }

    var archivedChatsDrawerButton by sharedPreferences.boolean("AP_ArchivedChatsDrawerButton", true)
    fun toggleArchivedChatsDrawerButton() {
        archivedChatsDrawerButton = !archivedChatsDrawerButton
        putBoolean("AP_ArchivedChatsDrawerButton", archivedChatsDrawerButton)
    }

    var peopleNearbyDrawerButton by sharedPreferences.boolean("AP_PeopleNearbyDrawerButton", false)
    fun togglePeopleNearbyDrawerButton() {
        peopleNearbyDrawerButton = !peopleNearbyDrawerButton
        putBoolean("AP_PeopleNearbyDrawerButton", peopleNearbyDrawerButton)
    }

    var scanQRDrawerButton by sharedPreferences.boolean("AP_ScanQRDrawerButton", true)
    fun toggleScanQRDrawerButton() {
        scanQRDrawerButton = !scanQRDrawerButton
        putBoolean("AP_ScanQRDrawerButton", scanQRDrawerButton)
    }

    var cGPreferencesDrawerButton by sharedPreferences.boolean("AP_CGPreferencesDrawerButton", true)
    fun toggleCGPreferencesDrawerButton() {
        cGPreferencesDrawerButton = !cGPreferencesDrawerButton
        putBoolean("AP_CGPreferencesDrawerButton", cGPreferencesDrawerButton)
    }

    //Snowflakes
    var drawSnowInActionBar by sharedPreferences.boolean("AP_DrawSnowInActionBar", false)
    var drawSnowInChat by sharedPreferences.boolean("AP_DrawSnowInChat", false)

    // Chats Settings
    //Stickers
    var hideStickerTime by sharedPreferences.boolean("CP_TimeOnStick", false)
    var slider_stickerAmplifier by sharedPreferences.int("CP_Slider_StickerAmplifier", 100)

    //Chats
    //Admin Shortcuts
    var admins_Reactions by sharedPreferences.boolean("CP_Admins_Reactions", false)
    fun toggleAdminsReactions() {
        admins_Reactions = !admins_Reactions
        putBoolean("CP_Admins_Reactions", admins_Reactions)
    }

    var admins_Permissions by sharedPreferences.boolean("CP_Admins_Permissions", false)
    fun toggleAdminsPermissions() {
        admins_Permissions = !admins_Permissions
        putBoolean("CP_Admins_Permissions", admins_Permissions)
    }

    var admins_Administrators by sharedPreferences.boolean("CP_Admins_Administrators", false)
    fun toggleAdminsAdministrators() {
        admins_Administrators = !admins_Administrators
        putBoolean("CP_Admins_Administrators", admins_Administrators)
    }

    var admins_Members by sharedPreferences.boolean("CP_Admins_Members", false)
    fun toggleAdminsMembers() {
        admins_Members = !admins_Members
        putBoolean("CP_Admins_Members", admins_Members)
    }

    var admins_Statistics by sharedPreferences.boolean("CP_Admins_Statistics", false)
    fun toggleAdminsStatistics() {
        admins_Statistics = !admins_Statistics
        putBoolean("CP_Admins_Statistics", admins_Statistics)
    }

    var admins_RecentActions by sharedPreferences.boolean("CP_Admins_RecentActions", false)
    fun toggleAdminsRecentActions() {
        admins_RecentActions = !admins_RecentActions
        putBoolean("CP_Admins_RecentActions", admins_RecentActions)
    }

    var centerChatTitle by sharedPreferences.boolean("AP_CenterChatTitle", true)
    var unreadBadgeOnBackButton by sharedPreferences.boolean("CP_UnreadBadgeOnBackButton", false)
    var confirmCalls by sharedPreferences.boolean("CP_ConfirmCalls", false)
    var hideKeyboardOnScroll by sharedPreferences.boolean("CP_HideKbdOnScroll", false)
    var disableSwipeToNext by sharedPreferences.boolean("CP_DisableSwipeToNext", false)
    var hideMuteUnmuteButton by sharedPreferences.boolean("CP_HideMuteUnmuteButton", false)
    var slider_RecentEmojisAmplifier by sharedPreferences.int("CP_Slider_RecentEmojisAmplifier", 45)
    var slider_RecentStickersAmplifier by sharedPreferences.int("CP_Slider_RecentStickersAmplifier", 20)

    //Messages
    //Direct Share
    var shareDrawStoryButton by sharedPreferences.boolean("CP_ShareDrawStoryButton", true)
    fun toggleShareDrawStoryButton() {
        shareDrawStoryButton = !shareDrawStoryButton
        putBoolean("CP_ShareDrawStoryButton", shareDrawStoryButton)
    }

    var usersDrawShareButton by sharedPreferences.boolean("CP_UsersDrawShareButton", false)
    fun toggleUsersDrawShareButton() {
        usersDrawShareButton = !usersDrawShareButton
        putBoolean("CP_UsersDrawShareButton", usersDrawShareButton)
    }

    var supergroupsDrawShareButton by sharedPreferences.boolean("CP_SupergroupsDrawShareButton", false)
    fun toggleSupergroupsDrawShareButton() {
        supergroupsDrawShareButton = !supergroupsDrawShareButton
        putBoolean("CP_SupergroupsDrawShareButton", supergroupsDrawShareButton)
    }

    var channelsDrawShareButton by sharedPreferences.boolean("CP_ChannelsDrawShareButton", true)
    fun toggleChannelsDrawShareButton() {
        channelsDrawShareButton = !channelsDrawShareButton
        putBoolean("CP_ChannelsDrawShareButton", channelsDrawShareButton)
    }

    var botsDrawShareButton by sharedPreferences.boolean("CP_BotsDrawShareButton", true)
    fun toggleBotsDrawShareButton() {
        botsDrawShareButton = !botsDrawShareButton
        putBoolean("CP_BotsDrawShareButton", botsDrawShareButton)
    }

    var stickersDrawShareButton by sharedPreferences.boolean("CP_StickersDrawShareButton", false)
    fun toggleStickersDrawShareButton() {
        stickersDrawShareButton = !stickersDrawShareButton
        putBoolean("CP_StickersDrawShareButton", stickersDrawShareButton)
    }

    //Message menu
    var showReply by sharedPreferences.boolean("CP_ShowReply", true)
    fun toggleShowReply() {
        showReply = !showReply
        putBoolean("CP_ShowReply", showReply)
    }

    var showCopyPhoto by sharedPreferences.boolean("CP_ShowCopyPhoto", true)
    fun toggleShowCopyPhoto() {
        showCopyPhoto = !showCopyPhoto
        putBoolean("CP_ShowCopyPhoto", showCopyPhoto)
    }

    var showCopyPhotoAsSticker by sharedPreferences.boolean("CP_ShowCopyPhotoAsSticker", true)
    fun toggleShowCopyPhotoAsSticker() {
        showCopyPhotoAsSticker = !showCopyPhotoAsSticker
        putBoolean("CP_ShowCopyPhotoAsSticker", showCopyPhotoAsSticker)
    }

    var showClearFromCache by sharedPreferences.boolean("CP_ShowClearFromCache", true)
    fun toggleShowClearFromCache() {
        showClearFromCache = !showClearFromCache
        putBoolean("CP_ShowClearFromCache", showClearFromCache)
    }

    var showForward by sharedPreferences.boolean("CP_ShowForward", false)
    fun toggleShowForward() {
        showForward = !showForward
        putBoolean("CP_ShowForward", showForward)
    }

    var showForwardWoAuthorship by sharedPreferences.boolean("CP_ShowForward_WO_Authorship", false)
    fun toggleShowForwardWoAuthorship() {
        showForwardWoAuthorship = !showForwardWoAuthorship
        putBoolean("CP_ShowForward_WO_Authorship", showForwardWoAuthorship)
    }

    var showViewHistory by sharedPreferences.boolean("CP_ShowViewHistory", true)
    fun toggleShowViewHistory() {
        showViewHistory = !showViewHistory
        putBoolean("CP_ShowViewHistory", showViewHistory)
    }

    var showSaveMessage by sharedPreferences.boolean("CP_ShowSaveMessage", false)
    fun toggleShowSaveMessage() {
        showSaveMessage = !showSaveMessage
        putBoolean("CP_ShowSaveMessage", showSaveMessage)
    }

    var showReport by sharedPreferences.boolean("CP_ShowReport", true)
    fun toggleShowReport() {
        showReport = !showReport
        putBoolean("CP_ShowReport", showReport)
    }

    var showGetReplyBackground by sharedPreferences.boolean("CP_ShowGetReplyBackground", true)
    fun toggleShowGetReplyBackground() {
        showGetReplyBackground = !showGetReplyBackground
        putBoolean("CP_ShowGetReplyBackground", showGetReplyBackground)
    }

    var showJSON by sharedPreferences.boolean("CP_ShowJSON", false)
    fun toggleShowJSON() {
        showJSON = !showJSON
        putBoolean("CP_ShowJSON", showJSON)
    }

    var deleteForAll by sharedPreferences.boolean("CP_DeleteForAll", false)
    var msgForwardDate by sharedPreferences.boolean("CP_ForwardMsgDate", false)
    var showPencilIcon by sharedPreferences.boolean("AP_PencilIcon", false)

    const val LEFT_BUTTON_FORWARD_WO_AUTHORSHIP = 0
    const val LEFT_BUTTON_REPLY = 1
    const val LEFT_BUTTON_SAVE_MESSAGE= 2
    const val LEFT_BUTTON_DIRECT_SHARE = 3
    var leftBottomButton by sharedPreferences.int("CP_LeftBottomButtonAction", LEFT_BUTTON_FORWARD_WO_AUTHORSHIP)

    const val DOUBLE_TAP_ACTION_NONE = 0
    const val DOUBLE_TAP_ACTION_REACTION = 1
    const val DOUBLE_TAP_ACTION_REPLY = 2
    const val DOUBLE_TAP_ACTION_SAVE = 3
    const val DOUBLE_TAP_ACTION_EDIT = 4
    const val DOUBLE_TAP_ACTION_TRANSLATE = 5
    var doubleTapAction by sharedPreferences.int("CP_DoubleTapAction", DOUBLE_TAP_ACTION_REACTION)

    const val MESSAGE_SLIDE_ACTION_REPLY = 0
    const val MESSAGE_SLIDE_ACTION_SAVE = 1
    const val MESSAGE_SLIDE_ACTION_TRANSLATE = 2
    const val MESSAGE_SLIDE_ACTION_DIRECT_SHARE = 3
    var messageSlideAction by sharedPreferences.int("CP_MessageSlideAction", MESSAGE_SLIDE_ACTION_REPLY)

    //Media
    var largePhotos by sharedPreferences.boolean("CP_LargePhotos", true)
    var spoilersOnMedia by sharedPreferences.boolean("CP_SpoilersOnMedia", true)
    var voicesAgc by sharedPreferences.boolean("CP_VoicesAGC", false)
    var playVideoOnVolume by sharedPreferences.boolean("CP_PlayVideo", false)
    var autoPauseVideo by sharedPreferences.boolean("CP_AutoPauseVideo", false)
    var disableVibration by sharedPreferences.boolean("CP_DisableVibration", false)
    var videoSeekDuration by sharedPreferences.int("CP_VideoSeekDuration", 10)

    //Notifications
    const val NOTIF_SOUND_DISABLE = 0
    const val NOTIF_SOUND_DEFAULT = 1
    const val NOTIF_SOUND_IOS = 2
    var notificationSound by sharedPreferences.int("CP_Notification_Sound", NOTIF_SOUND_DEFAULT)

    const val VIBRATION_DISABLE = 0
    const val VIBRATION_CLICK = 1
    const val VIBRATION_WAVE_FORM = 2
    const val VIBRATION_KEYBOARD_TAP = 3
    const val VIBRATION_LONG = 4
    var vibrateInChats by sharedPreferences.int("CP_VibrationInChats", VIBRATION_DISABLE)

    var silenceNonContacts by sharedPreferences.boolean("CP_SilenceNonContacts", false)

    // Camera Settings
    //Camera type
    const val TELEGRAM_CAMERA = 0
    const val CAMERA_X = 1
    const val CAMERA_2 = 2
    const val SYSTEM_CAMERA = 3
    var cameraType by sharedPreferences.int("CP_CameraType", TELEGRAM_CAMERA)

    var cameraResolution by sharedPreferences.int("CP_CameraResolution", -1)
    //Camera
    var disableAttachCamera by sharedPreferences.boolean("CP_DisableCam", false)
    fun toggleDisableAttachCamera() {
        disableAttachCamera = !disableAttachCamera
        putBoolean("CP_DisableCam", disableAttachCamera)
    }

    var useDualCamera by sharedPreferences.boolean("CP_UseDualCamera", false)
    fun toggleUseDualCamera() {
        useDualCamera = !useDualCamera
        putBoolean("CP_UseDualCamera", useDualCamera)
    }

    var rearCam by sharedPreferences.boolean("CP_RearCam", false)
    fun toggleRearCam() {
        rearCam = !rearCam
        putBoolean("CP_RearCam", rearCam)
    }

    var cameraStabilisation by sharedPreferences.boolean("CP_CameraStabilisation", false)
    fun toggleCameraStabilisation() {
        cameraStabilisation = !cameraStabilisation
        putBoolean("CP_CameraStabilisation", cameraStabilisation)
    }

    var startFromUltraWideCam by sharedPreferences.boolean("CP_StartFromUltraWideCam", true)
    fun toggleStartFromUltraWideCam() {
        startFromUltraWideCam = !startFromUltraWideCam
        putBoolean("CP_StartFromUltraWideCam", startFromUltraWideCam)
    }

    const val Camera16to9 = 0
    const val Camera4to3 = 1
    const val Camera1to1 = 2
    const val CameraAspectDefault = 3
    var cameraAspectRatio by sharedPreferences.int("CP_CameraAspectRatio", CameraAspectDefault)

    var whiteBackground by sharedPreferences.boolean("CG_WhiteBG", false)

    // Privacy
    var hideProxySponsor by sharedPreferences.boolean("SP_NoProxyPromo", true)
    var googleAnalytics by sharedPreferences.boolean("SP_GoogleAnalytics", ApplicationLoader.checkPlayServices())

    // Experimental
    //General
    const val ANIMATION_SPRING = 0
    const val ANIMATION_CLASSIC = 1
    var springAnimation by sharedPreferences.int("EP_SpringAnimation", ANIMATION_SPRING)

    var actionbarCrossfade by sharedPreferences.boolean("EP_ActionbarCrossfade", true)
    fun toggleActionbarCrossfade() {
        actionbarCrossfade = !actionbarCrossfade
        putBoolean("EP_ActionbarCrossfade", actionbarCrossfade)
    }

    var residentNotification by sharedPreferences.boolean("CG_ResidentNotification", !ApplicationLoader.checkPlayServices())
    fun toggleResidentNotification() {
        residentNotification = !residentNotification
        putBoolean("CG_ResidentNotification", residentNotification)
    }

    var showRPCErrors by sharedPreferences.boolean("EP_ShowRPCErrors", false)
    fun toggleShowRPCErrors() {
        showRPCErrors = !showRPCErrors
        putBoolean("EP_ShowRPCErrors", showRPCErrors)
    }

    var customChatForSavedMessages by sharedPreferences.boolean("CP_CustomChatForSavedMessages", false)
    fun toggleCustomChatForSavedMessages() {
        customChatForSavedMessages = !customChatForSavedMessages
        putBoolean("CP_CustomChatForSavedMessages", customChatForSavedMessages)
    }

    const val BOOST_NONE = 0
    const val BOOST_AVERAGE = 1
    const val BOOST_EXTREME = 2
    var downloadSpeedBoost by sharedPreferences.int("EP_DownloadSpeedBoost", BOOST_NONE)

    var uploadSpeedBoost by sharedPreferences.boolean("EP_UploadSpeedBoost", false)
    fun toggleUploadSpeedBoost() {
        uploadSpeedBoost = !uploadSpeedBoost
        putBoolean("EP_UploadSpeedBoost", uploadSpeedBoost)
    }

    var slowNetworkMode by sharedPreferences.boolean("EP_SlowNetworkMode", false)
    fun toggleSlowNetworkMode() {
        slowNetworkMode = !slowNetworkMode
        putBoolean("EP_SlowNetworkMode", slowNetworkMode)
    }

    // OTA
    var installBetas by sharedPreferences.boolean("CG_Install_Beta_Ver", isBeta())
    fun toggleInstallBetas() {
        installBetas = !installBetas
        putBoolean("CG_Install_Beta_Ver", installBetas)
    }

    var autoOTA by sharedPreferences.boolean("CG_Auto_OTA", !isPremium())
    fun toggleAutoOTA() {
        autoOTA = !autoOTA
        putBoolean("CG_Auto_OTA", autoOTA)
    }

    var lastUpdateCheckTime by sharedPreferences.long("CG_LastUpdateCheckTime", 0)
    var updateScheduleTimestamp by sharedPreferences.long("CG_UpdateScheduleTimestamp", 0)

    // Misc
    var forwardNoAuthorship by sharedPreferences.boolean("CG_ForwardNoAuthorship", false)
    var forwardWithoutCaptions by sharedPreferences.boolean("CG_ForwardWithoutCaptions", false)
    var forwardNotify by sharedPreferences.boolean("CG_ForwardNotify", true)
    var noAuthorship by sharedPreferences.boolean("CG_NoAuthorship", false)
    var gifSpoilers by sharedPreferences.boolean("CG_GifSpoiler", false)

    var filterLauncherIcon by sharedPreferences.boolean("AP_Filter_Launcher_Icon", false)
    fun toggleAppIconFilter() { // Telegram chats settings
        filterLauncherIcon = !filterLauncherIcon
        putBoolean("AP_Filter_Launcher_Icon", filterLauncherIcon)
    }

    var forceChatBlurEffect by sharedPreferences.boolean("AP_ForceBlur", false)
    fun toggleForceChatBlurEffect() {
        forceChatBlurEffect = !forceChatBlurEffect
        putBoolean("AP_ForceBlur", forceChatBlurEffect)
    }
    var forceChatBlurEffectIntensity by sharedPreferences.int("AP_ForceBlur_Intensity", 155)

    //Translator
    var translationKeyboardTarget by sharedPreferences.string("translationKeyboardTarget", "app")
    var translationTarget by sharedPreferences.string("translationTarget", "app")
    //Telegram Debug Menu
    var oldTimeStyle by sharedPreferences.boolean("CP_OldTimeStyle", false)
    fun toggleOldTimeStyle() {
        oldTimeStyle = !oldTimeStyle
        putBoolean("CP_OldTimeStyle", oldTimeStyle)
    }

    //Search Filter
    const val FILTER_NONE = 0
    const val FILTER_PHOTOS = 1
    const val FILTER_VIDEOS = 2
    const val FILTER_VOICE_MESSAGES = 3
    const val FILTER_VIDEO_MESSAGES = 4
    const val FILTER_FILES = 5
    const val FILTER_MUSIC = 6
    const val FILTER_GIFS = 7
    const val FILTER_GEO = 8
    const val FILTER_CONTACTS = 9
    const val FILTER_MENTIONS = 10
    var messagesSearchFilter by sharedPreferences.int("messagesSearchFilter", FILTER_NONE)

    init {
        CherrygramToasts.init(sharedPreferences)
        ConnectionsManagerImpl.launch {}
    }

    init {
        launch(Dispatchers.IO) {
            StickersHelper.getStickerSetIDs()
            StickersHelper.copyStickerFromAssets()
            delay(2000)
            if (googleAnalytics && ApplicationLoader.checkPlayServices()) {
                try {
                    FirebaseAnalyticsHelper.start(ApplicationLoader.applicationContext)
                    val bundle = Bundle()
                    FirebaseAnalyticsHelper.trackEvent("cg_start", bundle)
                    /*AndroidUtilities.runOnUIThread(Runnable {
                        Toast.makeText(ApplicationLoader.applicationContext, "cg_start", Toast.LENGTH_SHORT).show()
                    })*/
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    /*AndroidUtilities.runOnUIThread(Runnable {
                        Toast.makeText(ApplicationLoader.applicationContext, "error", Toast.LENGTH_SHORT).show()
                    })*/
                }
            }
        }
    }

    fun isStable(): Boolean {
        return true
    }

    fun isBeta(): Boolean {
        return false
    }

    fun isDev(): Boolean {
        return false
    }

    fun isPremium(): Boolean {
        return false
    }

    /*fun isCherryVerified(chat: TLRPC.Chat): Boolean {
        return LocalVerificationsHelper.getVerify().stream().anyMatch { id: Long -> id == chat.id }
    }*/

    fun isDeleteAllHidden(chat: TLRPC.Chat): Boolean {
        return LocalVerificationsHelper.hideDeleteAll().stream().anyMatch { id: Long -> id == chat.id }
    }

}
