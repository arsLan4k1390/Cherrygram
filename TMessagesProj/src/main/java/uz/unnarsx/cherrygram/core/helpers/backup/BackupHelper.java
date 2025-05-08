/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.core.helpers.backup;

import static org.telegram.messenger.LocaleController.getString;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.LaunchActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;

import kotlin.text.StringsKt;
import uz.unnarsx.cherrygram.core.PermissionsUtils;
import uz.unnarsx.cherrygram.core.helpers.AppRestartHelper;

public class BackupHelper {

    public static void backupSettings(BaseFragment fragment, Context context) {
        if (Build.VERSION.SDK_INT >= 23 && !PermissionsUtils.isStoragePermissionGranted()) {
            PermissionsUtils.requestStoragePermission(fragment.getParentActivity());
            return;
        }
        try {
            String formattedDate = String.format(LocaleController.getInstance().getFormatterYear().format(System.currentTimeMillis()), LocaleController.getInstance().getFormatterDay().format(System.currentTimeMillis()));

            File cacheFile = new File(ApplicationLoader.applicationContext.getExternalFilesDir(null), formattedDate + "-settings.cherry");
            BackupHelper2.writeUtf8String(backupSettingsJson(), cacheFile);
            BackupHelper2.shareFile(context, cacheFile);
        } catch (JSONException e) {
            AndroidUtilities.addToClipboard(e.toString());
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public static void importSettings(BaseFragment fragment) {
        if (Build.VERSION.SDK_INT >= 23 && !PermissionsUtils.isStoragePermissionGranted()) {
            PermissionsUtils.requestStoragePermission(fragment.getParentActivity());
            return;
        }
        BackupFileImportActivity importActivity = new BackupFileImportActivity();
        importActivity.setMaxSelectedFiles(1);
        importActivity.setDelegate(new BackupFileImportActivity.DocumentSelectActivityDelegate() {
            @Override
            public void didSelectFiles(BackupFileImportActivity activity, ArrayList<String> files, String caption, boolean notify, int scheduleDate) {
                activity.finishFragment();
                BackupHelper.importSettings(fragment.getContext(), new File(files.get(0)));
            }

            @Override
            public void didSelectPhotos(ArrayList<SendMessagesHelper.SendingMediaInfo> photos, boolean notify, int scheduleDate) {
            }

            @Override
            public void startDocumentSelectActivity() {
            }
        });
        fragment.presentFragment(importActivity);
    }


    private static String backupSettingsJson() throws JSONException {
        JSONObject configJson = new JSONObject();

        /*ArrayList<String> userconfig = new ArrayList<>();
        userconfig.add("saveIncomingPhotos");
        userconfig.add("passcodeHash");
        userconfig.add("passcodeType");
        userconfig.add("passcodeHash");
        userconfig.add("autoLockIn");
        userconfig.add("useFingerprint");
        spToJSON("userconfing", configJson, userconfig::contains);*/

        ArrayList<String> mainconfig = new ArrayList<>();
        /*mainconfig.add("saveToGallery");
        mainconfig.add("autoplayGifs");
        mainconfig.add("autoplayVideo");
        mainconfig.add("mapPreviewType");
        mainconfig.add("raiseToSpeak");
        mainconfig.add("customTabs");
        mainconfig.add("directShare");
        mainconfig.add("shuffleMusic");
        mainconfig.add("playOrderReversed");
        mainconfig.add("repeatMode");
        mainconfig.add("fontSize");
        mainconfig.add("bubbleRadius");
        mainconfig.add("ivFontSize");
        mainconfig.add("allowBigEmoji");
        mainconfig.add("streamMedia");
        mainconfig.add("saveStreamMedia");
        mainconfig.add("smoothKeyboard");
        mainconfig.add("pauseMusicOnRecord");
        mainconfig.add("streamAllVideo");
        mainconfig.add("streamMkv");
        mainconfig.add("suggestStickers");
        mainconfig.add("sortContactsByName");
        mainconfig.add("sortFilesByName");
        mainconfig.add("noSoundHintShowed");
        mainconfig.add("directShareHash");
        mainconfig.add("useThreeLinesLayout");
        mainconfig.add("archiveHidden");
        mainconfig.add("distanceSystemType");
        mainconfig.add("loopStickers");
        mainconfig.add("keepMedia");
        mainconfig.add("noStatusBar");
        mainconfig.add("lastKeepMediaCheckTime");
        mainconfig.add("searchMessagesAsListHintShows");
        mainconfig.add("searchMessagesAsListUsed");
        mainconfig.add("stickersReorderingHintUsed");
        mainconfig.add("textSelectionHintShows");
        mainconfig.add("scheduledOrNoSoundHintShows");
        mainconfig.add("lockRecordAudioVideoHint");
        mainconfig.add("disableVoiceAudioEffects");
        mainconfig.add("chatSwipeAction");

        mainconfig.add("theme");
        mainconfig.add("selectedAutoNightType");
        mainconfig.add("autoNightScheduleByLocation");
        mainconfig.add("autoNightBrighnessThreshold");
        mainconfig.add("autoNightDayStartTime");
        mainconfig.add("autoNightDayEndTime");
        mainconfig.add("autoNightSunriseTime");
        mainconfig.add("autoNightCityName");
        mainconfig.add("autoNightSunsetTime");
        mainconfig.add("autoNightLocationLatitude3");
        mainconfig.add("autoNightLocationLongitude3");
        mainconfig.add("autoNightLastSunCheckDay");

        mainconfig.add("lang_code");*/

        //cherry
        /** General start **/
        mainconfig.add("CP_NoRounding");
        mainconfig.add("AP_SystemEmoji");
        mainconfig.add("AP_SystemFonts");
        mainconfig.add("AP_Old_Notification_Icon");
        mainconfig.add("AP_Tablet_Mode");

        mainconfig.add("CP_HideStories");
        mainconfig.add("CP_ArchiveStoriesFromUsers");
        mainconfig.add("CP_ArchiveStoriesFromChannels");
        mainconfig.add("CP_CustomWallpapers");
        mainconfig.add("CP_DisableAnimAvatars");
        mainconfig.add("CP_DisableReactionsOverlay");
        mainconfig.add("CP_DisableReactionAnim");
        mainconfig.add("CP_DisablePremStickAnim");
        mainconfig.add("CP_DisablePremStickAutoPlay");
        mainconfig.add("CP_HideSendAsChannel");
        /** General finish **/

        /** Appearance start **/
        mainconfig.add("AP_Icon_Replacements");
        mainconfig.add("AP_OneUI_SwitchStyle");
        mainconfig.add("AP_DisableDividers");
        mainconfig.add("AP_CenterTitle");
        mainconfig.add("AP_ToolBarShadow");
        mainconfig.add("AP_OverrideHeaderColor");
        mainconfig.add("AP_FlatNavBar");

        mainconfig.add("CP_ShowSeconds");
        mainconfig.add("CP_DisablePremiumStatuses");
        mainconfig.add("CP_ReplyBackground");
        mainconfig.add("CP_ReplyCustomColors");
        mainconfig.add("CP_ReplyBackgroundEmoji");
        mainconfig.add("CP_ProfileChannelPreview");
        mainconfig.add("AP_ShowID_DC");
        mainconfig.add("CP_ProfileBirthDatePreview");
        mainconfig.add("CP_ProfileBusinessPreview");
        mainconfig.add("CP_ProfileBackgroundColor");
        mainconfig.add("CP_ProfileBackgroundEmoji");

        mainconfig.add("AP_FolderNameInHeader");
        mainconfig.add("CP_NewTabs_RemoveAllChats");
        mainconfig.add("CP_NewTabs_NoCounter");
        mainconfig.add("AP_TabMode");
        mainconfig.add("AP_TabStyle");
        mainconfig.add("AP_TabStyleAddStroke");

        mainconfig.add("AP_DrawSnowInDrawer");
        mainconfig.add("AP_DrawerAvatar");
        mainconfig.add("AP_DrawerSmallAvatar");
        mainconfig.add("AP_DrawerDarken");
        mainconfig.add("AP_DrawerGradient");
        mainconfig.add("AP_DrawerBlur");
        mainconfig.add("AP_DrawerBlur_Intensity");

        mainconfig.add("AP_ChangeStatusDrawerButton");
//        mainconfig.add("AP_MyStoriesDrawerButton");
        mainconfig.add("AP_MyProfileDrawerButton");
        mainconfig.add("AP_CreateGroupDrawerButton");
        mainconfig.add("AP_CreateChannelDrawerButton");
        mainconfig.add("AP_ContactsDrawerButton");
        mainconfig.add("AP_CallsDrawerButton");
        mainconfig.add("AP_SavedMessagesDrawerButton");
        mainconfig.add("AP_ArchivedChatsDrawerButton");
        mainconfig.add("AP_ScanQRDrawerButton");
        mainconfig.add("AP_CGPreferencesDrawerButton");
        mainconfig.add("AP_DrawerEventType");

        mainconfig.add("AP_DrawSnowInActionBar");
        mainconfig.add("AP_DrawSnowInChat");
        /** Appearance finish **/

        /** Chats start **/
        mainconfig.add("CP_Shortcut_JumpToBegin");
        mainconfig.add("CP_Shortcut_DeleteAll");
        mainconfig.add("CP_Shortcut_SavedMessages");
        mainconfig.add("CP_Shortcut_Blur");
        mainconfig.add("CP_Shortcut_Browser");

        mainconfig.add("CP_Admins_Reactions");
        mainconfig.add("CP_Admins_Permissions");
        mainconfig.add("CP_Admins_Administrators");
        mainconfig.add("CP_Admins_Members");
        mainconfig.add("CP_Admins_Statistics");
        mainconfig.add("CP_Admins_RecentActions");

        mainconfig.add("AP_CenterChatTitle");
        mainconfig.add("CP_UnreadBadgeOnBackButton");
        mainconfig.add("CP_DisableSwipeToNext");
        mainconfig.add("CP_HideMuteUnmuteButton");
        mainconfig.add("CP_HideKeyboardOnScrollIntensity");

        mainconfig.add("CP_GeminiApiKey");
        mainconfig.add("CP_GeminiModelName");

        mainconfig.add("CP_ShareDrawStoryButton");
        mainconfig.add("CP_UsersDrawShareButton");
        mainconfig.add("CP_SupergroupsDrawShareButton");
        mainconfig.add("CP_ChannelsDrawShareButton");
        mainconfig.add("CP_BotsDrawShareButton");
        mainconfig.add("CP_StickersDrawShareButton");

        mainconfig.add("CP_ShowSaveForNotifications");
        mainconfig.add("CP_ShowGemini");
        mainconfig.add("CP_ShowReply");
        mainconfig.add("CP_ShowCopyPhoto");
        mainconfig.add("CP_ShowCopyPhotoAsSticker");
        mainconfig.add("CP_ShowClearFromCache");
        mainconfig.add("CP_ShowForward");
        mainconfig.add("CP_ShowForward_WO_Authorship");
        mainconfig.add("CP_ShowViewHistory");
        mainconfig.add("CP_ShowSaveMessage");
        mainconfig.add("CP_ShowReport");
        mainconfig.add("CP_ShowJSON");
        mainconfig.add("CP_JacksonJSON_Provider");

        mainconfig.add("CP_Slider_MediaAmplifier");
        mainconfig.add("CP_Slider_StickerAmplifier");
        mainconfig.add("CP_Slider_GifsAmplifier");

        mainconfig.add("CP_TimeOnStick");
        mainconfig.add("CP_DeleteForAll");
        mainconfig.add("CP_ForwardMsgDate");
        mainconfig.add("AP_PencilIcon");
        mainconfig.add("CP_LeftBottomButtonAction");
        mainconfig.add("CP_DoubleTapAction");
        mainconfig.add("CP_MessageSlideAction");

        mainconfig.add("CP_LargePhotos");
        mainconfig.add("CP_SpoilersOnMedia");
        mainconfig.add("CP_VoicesAGC");
        mainconfig.add("CP_PlayVideo");
        mainconfig.add("CP_AutoPauseVideo");
        mainconfig.add("CP_DisableVibration");
        mainconfig.add("CP_VideoSeekDuration");

        mainconfig.add("CP_Notification_Sound");
        mainconfig.add("CP_VibrationInChats");
        mainconfig.add("CP_SilenceNonContacts");
        mainconfig.add("CG_UnarchiveOnSwipe");
        /** Chats finish **/

        /** Camera start **/
        mainconfig.add("CP_CameraType");
        mainconfig.add("CP_DisableCam");
        mainconfig.add("CP_UseDualCamera");
        mainconfig.add("CP_CameraAspectRatio");
        mainconfig.add("CP_StartFromUltraWideCam");
//        mainconfig.add("CP_CameraXFpsRange");
        mainconfig.add("CP_CameraStabilisation");
        mainconfig.add("CP_CenterCameraControlButtons");
        mainconfig.add("CP_ExposureSlider");
        mainconfig.add("CP_RearCam");
        /** Camera finish **/

        /** Privacy start **/
        mainconfig.add("SP_NoProxyPromo");
//        mainconfig.add("SP_GoogleAnalytics");
        mainconfig.add("SP_HideArchiveFromChatsList");
//        mainconfig.add("SP_AskBiometricsToOpenArchive");
//        mainconfig.add("SP_AskBiometricsToOpenChat");
//        mainconfig.add("SP_AskPinBeforeDelete");
//        mainconfig.add("SP_AllowSystemPasscode");
        /** Privacy finish **/

        /** Experimental start **/
        mainconfig.add("EP_SpringAnimation");
        mainconfig.add("EP_ActionbarCrossfade");
        mainconfig.add("CG_ResidentNotification");
        mainconfig.add("CP_CustomChatForSavedMessages");
        mainconfig.add("CP_CustomChatIDSM");
        mainconfig.add("EP_DownloadSpeedBoost");
        mainconfig.add("EP_UploadSpeedBoost");
        mainconfig.add("EP_SlowNetworkMode");
        /** Experimental finish **/
        //cherry

        spToJSON("mainconfig", configJson, mainconfig::contains);
//        spToJSON("themeconfig", configJson, null);

//        spToJSON("cherrycfg", configJson, null);

        return configJson.toString(4);
    }

    private static void spToJSON(String sp, JSONObject object, Function<String, Boolean> filter) throws JSONException {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(sp, Activity.MODE_PRIVATE);
        JSONObject jsonConfig = new JSONObject();
        for (Map.Entry<String, ?> entry : preferences.getAll().entrySet()) {
            String key = entry.getKey();
            if (filter != null && !filter.apply(key)) continue;
            if (entry.getValue() instanceof Long) {
                key = key + "_long";
            } else if (entry.getValue() instanceof Float) {
                key = key + "_float";
            }
            jsonConfig.put(key, entry.getValue());
        }
        object.put(sp, jsonConfig);
    }

    public static void importSettings(Context context, File settingsFile) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setBlurredBackground(false);
        builder.setTitle(getString(R.string.CG_ImportSettings));
        builder.setMessage(getString(R.string.CG_ImportSettingsAlert));
        builder.setNegativeButton(getString(R.string.Cancel), null);
        builder.setPositiveButton(getString(R.string.OK), (dialog2, which2) -> {
            importSettingsConfirmed(context, settingsFile);
        });
        builder.show();

        AlertDialog dialog = builder.create();
        TextView button = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (button != null) {
            button.setTextColor(Theme.getColor(Theme.key_text_RedBold));
        }
    }

    public static void importSettingsConfirmed(Context context, File settingsFile) {
        try {
            JsonObject configJson = BackupHelper2.toJsonObject(BackupHelper2.readUtf8String(settingsFile));
            importSettings(configJson);

            AlertDialog restart = new AlertDialog(context, 0);
            restart.setTitle(getString(R.string.CG_AppName));
            restart.setMessage(getString(R.string.CG_RestartToApply));
            restart.setPositiveButton(getString(R.string.BotUnblock), (__, ___) -> {
                AppRestartHelper.triggerRebirth(context, new Intent(context, LaunchActivity.class));
            });
            restart.show();
        } catch (Exception e) {
            AndroidUtilities.addToClipboard(e.toString());
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        }

    }

    @SuppressLint("ApplySharedPref")
    public static void importSettings(JsonObject configJson) throws JSONException {
        for (Map.Entry<String, JsonElement> element : configJson.entrySet()) {
            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(element.getKey(), Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            for (Map.Entry<String, JsonElement> config : ((JsonObject) element.getValue()).entrySet()) {
                String key = config.getKey();
                JsonPrimitive value = (JsonPrimitive) config.getValue();
                if (value.isBoolean()) {
                    editor.putBoolean(key, value.getAsBoolean());
                } else if (value.isNumber()) {
                    boolean isLong = false;
                    boolean isFloat = false;
                    if (key.endsWith("_long")) {
                        key = StringsKt.substringBeforeLast(key, "_long", key);
                        isLong = true;
                    } else if (key.endsWith("_float")) {
                        key = StringsKt.substringBeforeLast(key, "_float", key);
                        isFloat = true;
                    }
                    if (isLong) {
                        editor.putLong(key, value.getAsLong());
                    } else if (isFloat) {
                        editor.putFloat(key, value.getAsFloat());
                    } else {
                        editor.putInt(key, value.getAsInt());
                    }
                } else {
                    editor.putString(key, value.getAsString());
                }
            }
            editor.apply();
        }
    }

}
