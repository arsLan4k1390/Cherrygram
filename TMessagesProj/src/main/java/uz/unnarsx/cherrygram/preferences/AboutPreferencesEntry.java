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
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.core.crashlytics.Crashlytics;
import uz.unnarsx.cherrygram.core.crashlytics.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper;
import uz.unnarsx.cherrygram.core.helpers.DeeplinkHelper;
import uz.unnarsx.cherrygram.misc.Constants;

public class AboutPreferencesEntry extends UniversalFragment {

    private final int readmeRow = 1;
    private final int updatesRow = 2;
    private final int bugReportRow = 3;
    private final int debugPrefsRow = 4;

    private final int channelRow = 5;
    private final int chatRow = 6;
    private final int githubRow = 7;
    private final int crowdinRow = 8;
    private final int policyRow = 9;

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("about_preferences_screen");
        return getString(R.string.CGP_Header_About);
    }

    @Override
    public View createView(Context context) {
        setMD3(true);
        return super.createView(context);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(getString(R.string.Info)));

        items.add(
                UItem.asTextDetail(
                        readmeRow,
                        0,
                        CGResourcesHelper.getAppName() + " " + CGResourcesHelper.getCherryVersion() + " | " + "Telegram " + BuildVars.BUILD_VERSION_STRING,
                        getString(R.string.CGP_About_Desc)
                )
        );

        items.add(
                UItem.asTextDetail(
                        updatesRow,
                        R.drawable.msg_retry_solar,
                        getString(R.string.UP_Category_Updates),
                        getLastCheckUpdateTime()
                )
        );

        items.add(
                UItem.asButton(
                        bugReportRow,
                        R.drawable.bug_solar,
                        getString(R.string.CG_CopyReportDetails)
                )
        );

        items.add(
                UItem.asButton(
                        debugPrefsRow,
                        R.drawable.test_tube_solar,
                        "Debug // WIP"
                )
        );

        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.CGP_Links)));

        items.add(
                UItem.asButton(
                        channelRow,
                        R.drawable.msg_channel_solar,
                        getString(R.string.CGP_ToChannel)
                )
        );

        items.add(
                UItem.asButton(
                        chatRow,
                        R.drawable.msg_discuss_solar,
                        getString(R.string.CGP_ToChat)
                )
        );

        if (!CherrygramCoreConfig.isStandalonePremiumBuild()) {
            String value;
            if (CherrygramCoreConfig.isStandaloneBetaBuild() || CherrygramCoreConfig.isDevBuild()) {
                value = "GitHub";
            } else {
                value = "commit " + BuildConfig.GIT_COMMIT_HASH.substring(0, 8);
            }

            items.add(
                    UItem.asButton(
                            githubRow,
                            R.drawable.github_logo_white,
                            getString(R.string.CGP_Source),
                            value
                    )
            );
        }

        items.add(
                UItem.asButton(
                        crowdinRow,
                        R.drawable.msg_translate_solar,
                        getString(R.string.CGP_Crowdin),
                        "Crowdin"
                )
        );

        items.add(
                UItem.asButton(
                        policyRow,
                        R.drawable.msg_policy_solar,
                        getString(R.string.PrivacyPolicy)
                )
        );

        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == readmeRow) {
            Browser.openUrl(getContext(), Constants.CG_GITHUB_URL + "#readme");
        } else if (item.id == updatesRow) {
            if (CherrygramCoreConfig.isPlayStoreBuild()) {
                CherrygramCoreConfig.INSTANCE.setLastUpdateCheckTime(System.currentTimeMillis());
                ((TextDetailSettingsCell) view).setValue(getLastCheckUpdateTime());

                Browser.openUrl(getContext(), Constants.UPDATE_APP_URL);
            } else if (CherrygramCoreConfig.isStandalonePremiumBuild()) {
                // Fuckoff :)
            } else {
                LaunchActivity.instance.showCgUpdaterSettings(this);
            }
        } else if (item.id == bugReportRow) {
            AndroidUtilities.addToClipboard(Crashlytics.getReportMessage() + "\n\n#bug");
            BulletinFactory.of(this).createErrorBulletin(getString(R.string.CG_ReportDetailsCopied))
                    .setDuration(Bulletin.DURATION_SHORT)
                    .show();
        } else if (item.id == debugPrefsRow) {
            CherrygramPreferencesNavigator.INSTANCE.createDebug(this);
        } else if (item.id == channelRow) {
            getMessagesController().openByUserName(Constants.CG_CHANNEL_USERNAME, this, 1);
        } else if (item.id == chatRow) {
            getMessagesController().openByUserName(Constants.CG_CHAT_USERNAME, this, 1);
        } else if (item.id == githubRow) {
            if (CherrygramCoreConfig.isStandaloneBetaBuild() || CherrygramCoreConfig.isDevBuild()) {
                Browser.openUrl(getContext(), Constants.CG_GITHUB_URL);
            } else {
                Browser.openUrl(getContext(), Constants.CG_GITHUB_URL + "/commit/" + BuildConfig.GIT_COMMIT_HASH);
            }
        } else if (item.id == crowdinRow) {
            Browser.openUrl(getContext(), Constants.CG_CROWDIN_URL);
        } else if (item.id == policyRow) {
            Browser.openUrl(getContext(), Constants.CG_PRIVACY_URL);
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        if (item.id == readmeRow) {
            AndroidUtilities.addToClipboard(Constants.CG_GITHUB_URL + "#readme");
            return true;
        } else if (item.id == updatesRow) {
            AndroidUtilities.addToClipboard("tg://" + DeeplinkHelper.DeepLinksRepo.CG_Updater_Bottom_Sheet);
            return true;
        } else if (item.id == debugPrefsRow) {
            AndroidUtilities.addToClipboard("tg://" + DeeplinkHelper.DeepLinksRepo.CG_Debug);
            return true;
        } else if (item.id == channelRow) {
            AndroidUtilities.addToClipboard("@" + Constants.CG_CHANNEL_USERNAME);
            return true;
        } else if (item.id == chatRow) {
            AndroidUtilities.addToClipboard("@" + Constants.CG_CHAT_USERNAME);
            return true;
        } else if (item.id == crowdinRow) {
            AndroidUtilities.addToClipboard(Constants.CG_CROWDIN_URL);
            return true;
        } else if (item.id == policyRow) {
            AndroidUtilities.addToClipboard(Constants.CG_PRIVACY_URL);
            return true;
        }
        return false;
    }

    private String getLastCheckUpdateTime() {
        return getString(R.string.UP_LastCheck) + ": " + LocaleController.formatDateTime(CherrygramCoreConfig.INSTANCE.getLastUpdateCheckTime() / 1000, true);
    }

}
