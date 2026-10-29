/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.configs.backup

/**
 * User-facing settings that "Reset settings" removes from mainconfig, so configs fall back to their defaults.
 *
 * Intentionally NOT here:
 * - CP_GeminiApiKey, CG_GitLab_Api_Key - secrets;
 * - CherrygramFirebaseConfig - remote config values;
 * - hint counters (CG_Round_Flash_Hint_Count, CG_Round_Zoom_Hint_Count), migration key AP_ShowID_DC,
 *   UI state (messagesSearchFilter).
 *
 * Keep in sync with configs when adding a new setting.
 */
object CherrygramResetKeys {

    @JvmField
    val KEYS: Set<String> = setOf(
        /** CherrygramCoreConfig */
        "CG_NavigationAnimation", "CG_ActionbarCrossfade", "CG_PredictiveBack",
        "CP_SilenceNonContacts", "CG_IgnoreMentions", "CG_IgnoreMentionsMarkAsRead", "AP_Old_Notification_Icon", "CG_ResidentNotification",
        "CP_HideStories", "CP_ArchiveStoriesFromUsers", "CP_ArchiveStoriesFromChannels",
        "CP_NoRounding1", "AP_SystemEmoji", "AP_SystemFonts", "CP_EdgeToEdge", "AP_Tablet_Mode",
        "EP_DownloadSpeedBoost", "EP_UploadSpeedBoost", "EP_SlowNetworkMode",
        "CG_Install_Beta_Ver", "CG_Check_Auto_OTA",
        "DP_BrandedScreenshots", "CG_Sleep_Timer",

        /** CherrygramAppearanceConfig */
        "AP_CenterTitle", "AP_HideSearchField", "AP_DrawSnowInActionBar",
        "AP_IconBasedSplash", "AP_Icon_Replacements1", "AP_OneUI_SwitchStyle", "AP_DisableDividers", "AP_GlareEffect",
        "CP_NewTabs_RemoveAllChats", "CP_NewTabs_NoCounter", "CP_TabsIncludeMutedInCounter",
        "AP_TabMode", "AP_TabStyleAddStroke", "AP_FolderNameInHeader", "AP_FoldersAtBottom",
        "AP_ShowMainTabs", "AP_OpenSettingsBySwipe", "AP_MainTabsPosition_New", "AP_ShowSearchInTabs_New",
        "AP_ShowMainTabsTitle", "AP_MainTabsForceOpenChats",
        "CP_ShowSeconds", "CP_DisablePremiumStatuses", "CP_ReplyBackground", "CP_ReplyCustomColors", "CP_ReplyBackgroundEmoji",
        "CP_ProfileHidePhoneNumber", "CP_ProfileChannelPreview", "AP_ShowID_DC_new",
        "CP_ProfileBirthDatePreview", "CP_ProfileBusinessPreview", "CP_ProfileBackgroundColor", "CP_ProfileBackgroundEmoji",
        "AP_ShowAccounts", "AP_MarketplaceDrawerButton",

        /** CherrygramChatsConfig */
        "AP_CenterChatTitle_v2", "AP_CenterChatTitle_AdaptiveWidth_v2", "CP_UnreadBadgeOnBackButton", "CP_UnreadBadgeOnBackButton_iOS",
        "CP_Shortcut_JumpToBegin", "CP_Shortcut_DeleteAll", "CP_Shortcut_SavedMessages", "CP_Shortcut_Browser",
        "CP_Admins_Reactions", "CP_Admins_Permissions", "CP_Admins_Administrators",
        "CP_Admins_Members", "CP_Admins_Statistics", "CP_Admins_RecentActions",
        "CP_CustomWallpapers", "AP_DrawSnowInChat", "CP_DiscussInsteadOfMute", "CP_iOSMessageInputField",
        "CP_HideSendAsChannel", "CP_HideMuteUnmuteButton", "CP_Slider_RecentEmojisAmplifier", "CP_Slider_RecentStickersAmplifier",
        "CP_CustomChatForSavedMessages", "CP_CustomChatIDSM", "CP_HideKeyboardOnScrollIntensity",
        "CP_AutoQuoteReplies", "CP_DisableSwipeToNext", "CP_DisableVibration", "CP_OpenLinksInIV",
        "CP_RecordMultiMic", "CP_RecordInStereo", "CP_MuteSystemSounds", "CP_VoiceMessagesAutoPlay",
        "CP_PlayVideo", "CP_AutoPauseVideo", "CP_VideoSeekDuration",
        "CP_Notification_Sound", "CP_VibrationInChats",
        "CG_ForwardAuthorship", "CG_ForwardCaptions", "CG_ForwardNotify", "CG_NoAuthorship", "CG_NoCaptions",
        "CG_UnarchiveOnSwipe", "CG_SortByUnread",

        /** CherrygramMessagesConfig */
        "CP_GeminiModelName", "CP_GeminiSystemPrompt", "CP_GeminiTemperature", "CP_VoiceTranscriptionProvider",
        "CP_BlurMessageMenuBackground", "CP_MsgMenuShowWholeAlbum", "CP_MsgMenuNativeBlur1", "CP_RememberKeyboardState", "CP_FixMsgMenuAnim",
        "CP_ShowSaveForNotifications", "CP_ShowGemini", "CP_ShowReply", "CP_ShowSaveToGallery", "CP_ShowCopyPhoto",
        "CP_ShowCopyPhotoAsSticker", "CP_ShowSaveToDownloads", "CP_ShowShare", "CP_ShowClearFromCache", "CP_ShowForward",
        "CP_ShowForward_WO_Authorship", "CP_ShowViewHistory", "CP_ShowSaveMessage", "CP_ShowReport", "CP_OpenInExternalApp",
        "CP_ShowJSON", "CP_JacksonJSON_Provider", "CP_MsgMenuItemsCompactView",
        "CP_LargerVoiceMessagesLayout", "CP_Slider_MediaAmplifier", "CP_Slider_StickerAmplifier", "CP_Slider_GifsAmplifier",
        "CP_ShareDrawStoryButton", "CP_UsersDrawShareButton", "CP_SupergroupsDrawShareButton",
        "CP_ChannelsDrawShareButton", "CP_BotsDrawShareButton", "CP_StickersDrawShareButton",
        "CP_WideMessagesLayout", "CP_TimeOnStick", "CP_ForwardMsgDate", "AP_PencilIcon",
        "CP_EnableMsgFilter", "CP_MsgFiltersElements", "CP_MsgFiltersDetectTranslit", "CP_MsgFiltersMatchExactWord",
        "CP_MsgFiltersExcludeUsers", "CP_MsgFiltersExcludeBots", "CP_MsgFiltersExcludeGroups", "CP_MsgFiltersExcludeChannels",
        "CP_MsgFiltersDetectEntities", "CP_MsgFiltersFullyHideMessages", "CP_MsgFiltersHideFromBlocked1",
        "CP_MsgFiltersHideAll", "CP_MsgFiltersCollapseAutomatically", "CP_MsgFilterTransparentMsg",
        "CP_LeftBottomButtonAction", "CP_DoubleTapAction", "CP_MessageSlideAction", "CP_DeleteForAll",
        "CP_DisableReactionsOverlay", "CP_DisableReactionAnim", "CP_DisablePremStickAnim", "CP_DisablePremStickAutoPlay",
        "CG_GifSpoiler", "CG_PhotoAsSticker", "CG_MotionPhotosEnabled",
        "translationKeyboardTarget", "translationTarget", "translationTargetGemini",

        /** CherrygramCameraConfig */
        "CP_CameraType1", "CP_DisableAttachCam", "CP_UseDualCamera", "CP_CameraAspectRatio",
        "CP_CameraNewEncoder", "CP_CameraResolution", "CP_StartFromUltraWideCam", "CP_CameraFpsRange",
        "CP_VideoStabilisation", "CP_OpticalStabilisation", "CP_ContinuousAutofocus", "CP_NoiceReduction",
        "CP_FaceDetection", "CP_BokehEffect", "CP_CenterCameraControlButtons", "CP_ExposureSlider", "CP_RearCam",
        "CG_Round_Video_Resolution", "CG_Round_Flash_Warmth_Intensity", "CG_Round_Flash_Intensity", "CG_Round_Rear_Flash_Intensity",

        /** CherrygramPrivacyConfig */
        "SP_NoProxySponsor", "SP_GoogleAnalytics1", "SP_HideArchiveFromChatsList",
        "SP_AskBiometricsToOpenArchive", "SP_AskBiometricsToOpenEncrypted", "SP_AskBiometricsToOpenChat",
        "SP_AskPinBeforeDelete", "SP_AllowSystemPasscode",
        "CP_HideArchivedStories",

        /** CherrygramExperimentalConfig */
        "CG_Record_OOM_AS_NF", "CG_Use_CG_OOMHandler", "CG_OOMHandlerPopup", "CG_OOMHandlerPopupThreshold",

        /** CherrygramDebugConfig */
        "EP_ShowRPCErrors", "CP_OldTimeStyle",
        "replacePunctuationMarks", "editTextSuggestionsFix", "fixChineseKeyboards", "audioSource",
        "sendVideosMaxQuality", "CP_PlayGIFsAsVideos1", "CP_HideVideoTimestamp", "fakeDelete",
    )

}