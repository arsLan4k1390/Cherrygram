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
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.chats.helpers.ChatsHelper2;
import uz.unnarsx.cherrygram.core.configs.CherrygramExperimentalConfig;
import uz.unnarsx.cherrygram.core.crashlytics.FirebaseAnalyticsHelper;
import uz.unnarsx.cherrygram.core.ui.CGBulletinCreator;
import uz.unnarsx.cherrygram.helpers.ui.PopupHelper;

public class ExperimentalPreferencesEntry extends UniversalFragment {

    private final int springAnimationRow = 1;
    private final int actionbarCrossfadeRow = 2;
    private final int predictiveBackRow = 3;

    private final int customChatRow = 4;

    private final int downloadSpeedBoostRow = 5;
    private final int uploadSpeedBoostRow = 6;
    private final int slowNetworkMode = 7;

    @Override
    protected CharSequence getTitle() {
        FirebaseAnalyticsHelper.INSTANCE.trackEventWithEmptyBundle("experimental_preferences_screen");
        return getString(R.string.EP_Category_Experimental);
    }

    @Override
    public View createView(Context context) {
        setMD3(true);
        return super.createView(context);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(getString(R.string.AP_Header_General)));

        items.add(
                UItem.asButton(
                        springAnimationRow,
                        getString(R.string.EP_NavigationAnimation),
                        getSpringValue()
                )
        );

        if (CherrygramExperimentalConfig.INSTANCE.getSpringAnimation() == CherrygramExperimentalConfig.ANIMATION_SPRING) {
            items.add(
                    UItem.asCheck(
                            actionbarCrossfadeRow,
                            getString(R.string.EP_NavigationAnimationCrossfading)
                    ).setChecked(CherrygramExperimentalConfig.INSTANCE.getActionbarCrossfade())
            );
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            items.add(
                    UItem.asCheck(
                            predictiveBackRow,
                            getString(R.string.CG_PredictiveBackAnimation)
                    ).setChecked(CherrygramExperimentalConfig.INSTANCE.getPredictiveBack())
            );
        }

        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.CP_Header_Chats)));

        items.add(
                UItem.asButtonCheck(
                        customChatRow,
                        getString(R.string.EP_CustomChat),
                        getString(R.string.EP_CustomChat_Desc)
                ).setChecked(CherrygramExperimentalConfig.INSTANCE.getCustomChatForSavedMessages())
        );

        if (CherrygramExperimentalConfig.INSTANCE.getCustomChatForSavedMessages()) {
            items.add(UItem.asCustomWithBackground(createUserCell()));
        }

        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.EP_Network)));

        items.add(
                UItem.asButton(
                        downloadSpeedBoostRow,
                        getString(R.string.EP_DownloadSpeedBoost),
                        getDownloadSpeedBoostText()
                )
        );

        items.add(
                UItem.asCheck(
                        uploadSpeedBoostRow,
                        getString(R.string.EP_UploadloadSpeedBoost)
                ).setChecked(CherrygramExperimentalConfig.INSTANCE.getUploadSpeedBoost())
        );

        items.add(
                UItem.asCheck(
                        slowNetworkMode,
                        getString(R.string.EP_SlowNetworkMode)
                ).setChecked(CherrygramExperimentalConfig.INSTANCE.getSlowNetworkMode())
        );

        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == springAnimationRow) {
            ArrayList<String> configStringKeys = new ArrayList<>();
            ArrayList<Integer> configValues = new ArrayList<>();

            configStringKeys.add(getString(R.string.EP_NavigationAnimationSpring));
            configValues.add(CherrygramExperimentalConfig.ANIMATION_SPRING);

            configStringKeys.add(getString(R.string.EP_NavigationAnimationBezier));
            configValues.add(CherrygramExperimentalConfig.ANIMATION_CLASSIC);

            PopupHelper.show(configStringKeys, getString(R.string.EP_NavigationAnimation), configValues.indexOf(CherrygramExperimentalConfig.INSTANCE.getSpringAnimation()), getContext(), i -> {
                CherrygramExperimentalConfig.INSTANCE.setSpringAnimation(configValues.get(i));
                ((TextCell) view).setValue(getSpringValue(), true);

                listView.adapter.update(true);

                CGBulletinCreator.INSTANCE.createRestartBulletin(this);
            });
        } else if (item.id == actionbarCrossfadeRow) {
            CherrygramExperimentalConfig.INSTANCE.setActionbarCrossfade(!CherrygramExperimentalConfig.INSTANCE.getActionbarCrossfade());
            ((TextCheckCell) view).setChecked(CherrygramExperimentalConfig.INSTANCE.getActionbarCrossfade());

            if (CherrygramExperimentalConfig.INSTANCE.getActionbarCrossfade() && CherrygramExperimentalConfig.INSTANCE.getPredictiveBack()) {
                CherrygramExperimentalConfig.INSTANCE.setPredictiveBack(false);
                listView.adapter.update(true);
            }

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        } else if (item.id == predictiveBackRow) {
            CherrygramExperimentalConfig.INSTANCE.setPredictiveBack(!CherrygramExperimentalConfig.INSTANCE.getPredictiveBack());
            ((TextCheckCell) view).setChecked(CherrygramExperimentalConfig.INSTANCE.getPredictiveBack());

            if (CherrygramExperimentalConfig.INSTANCE.getPredictiveBack() && CherrygramExperimentalConfig.INSTANCE.getActionbarCrossfade()) {
                CherrygramExperimentalConfig.INSTANCE.setActionbarCrossfade(false);
                listView.adapter.update(true);
            }

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        } else if (item.id == customChatRow) {
            CherrygramExperimentalConfig.INSTANCE.setCustomChatForSavedMessages(!CherrygramExperimentalConfig.INSTANCE.getCustomChatForSavedMessages());
            ((NotificationsCheckCell) view).setChecked(CherrygramExperimentalConfig.INSTANCE.getCustomChatForSavedMessages());

            listView.adapter.update(true);
        } else if (item.id == downloadSpeedBoostRow) {
            ArrayList<String> configStringKeys = new ArrayList<>();
            ArrayList<Integer> configValues = new ArrayList<>();

            configStringKeys.add(getString(R.string.EP_DownloadSpeedBoostNone));
            configValues.add(CherrygramExperimentalConfig.BOOST_NONE);

            configStringKeys.add(getString(R.string.EP_DownloadSpeedBoostAverage));
            configValues.add(CherrygramExperimentalConfig.BOOST_AVERAGE);

            configStringKeys.add(getString(R.string.EP_DownloadSpeedBoostExtreme));
            configValues.add(CherrygramExperimentalConfig.BOOST_EXTREME);

            PopupHelper.show(configStringKeys, getString(R.string.EP_DownloadSpeedBoost), configValues.indexOf(CherrygramExperimentalConfig.INSTANCE.getDownloadSpeedBoost()), getContext(), i -> {
                CherrygramExperimentalConfig.INSTANCE.setDownloadSpeedBoost(configValues.get(i));
                ((TextCell) view).setValue(getDownloadSpeedBoostText(), true);

                CGBulletinCreator.INSTANCE.createRestartBulletin(this);
            });
        } else if (item.id == uploadSpeedBoostRow) {
            CherrygramExperimentalConfig.INSTANCE.setUploadSpeedBoost(!CherrygramExperimentalConfig.INSTANCE.getUploadSpeedBoost());
            ((TextCheckCell) view).setChecked(CherrygramExperimentalConfig.INSTANCE.getUploadSpeedBoost());

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        } else if (item.id == slowNetworkMode) {
            CherrygramExperimentalConfig.INSTANCE.setSlowNetworkMode(!CherrygramExperimentalConfig.INSTANCE.getSlowNetworkMode());
            ((TextCheckCell) view).setChecked(CherrygramExperimentalConfig.INSTANCE.getSlowNetworkMode());

            CGBulletinCreator.INSTANCE.createRestartBulletin(this);
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }

    private UserCell createUserCell() {
        UserCell userCell = new UserCell(getContext(), 14, 0, false, true, getResourceProvider(), false, false);

        userCell.addButton.setText(getString(R.string.Edit));
        userCell.addButton.setOnClickListener(view1 -> {
            if (getUserConfig().getCurrentUser() == null) {
                return;
            }
            Bundle args = new Bundle();
            args.putBoolean("onlySelect", true);
            args.putBoolean("cgPrefs", true);
            args.putBoolean("allowGlobalSearch", false);
            args.putInt("dialogsType", DialogsActivity.DIALOGS_TYPE_FORWARD);
            args.putBoolean("resetDelegate", false);
            args.putBoolean("closeFragment", true);
            DialogsActivity fragment = new DialogsActivity(args);
            fragment.setDelegate((fragment1, dids, message, param, notify, scheduleDate, scheduleRepeatPeriod, topicsFragment) -> {
                long did = dids.get(0).dialogId;

                String selectedChatId = String.valueOf(did);

                SharedPreferences.Editor editor = MessagesController.getMainSettings(currentAccount).edit();
                editor.putString("CP_CustomChatIDSM", selectedChatId).apply();

                fragment.finishFragment(true);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    View avatar = userCell.avatarImageView;

                    int[] loc = new int[2];
                    avatar.getLocationOnScreen(loc);

                    float cx = loc[0] + avatar.getWidth() / 2f;
                    float cy = loc[1] + avatar.getHeight() / 2f;

                    LaunchActivity.makeRipple(cx, cy, 5f);
                }

                listView.adapter.update(false);
                return true;
            });
            presentFragment(fragment);
        });

        long chatId = ChatsHelper2.INSTANCE.getCustomChatID();

        TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(-chatId);
        TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(chatId);

        StringBuilder status = new StringBuilder();
        status.append(getString(R.string.EP_CustomChat_Selected_Title));
        status.append(' ');
        status.append("\"");
        status.append(getString(R.string.SavedMessages));
        status.append("\".");

        if (chatId == getUserConfig().clientUserId) {
            userCell.setData("saved_cg", getString(R.string.SavedMessages), "", 0);
        } else if (chat != null) {
            userCell.setData(chat, chat.title, status, 0);
        } else {
            userCell.setData(user, UserObject.getUserName(user), status, 0);
        }

        return userCell;
    }

    private String getSpringValue()  {
        return switch (CherrygramExperimentalConfig.INSTANCE.getSpringAnimation()) {
            case CherrygramExperimentalConfig.ANIMATION_CLASSIC -> getString(R.string.EP_NavigationAnimationBezier);
            default -> getString(R.string.EP_NavigationAnimationSpring);
        };
    }

    private String getDownloadSpeedBoostText()  {
        return switch (CherrygramExperimentalConfig.INSTANCE.getDownloadSpeedBoost()) {
            case CherrygramExperimentalConfig.BOOST_NONE -> getString(R.string.EP_DownloadSpeedBoostNone);
            case CherrygramExperimentalConfig.BOOST_AVERAGE -> getString(R.string.EP_DownloadSpeedBoostAverage);
            default -> getString(R.string.EP_DownloadSpeedBoostExtreme);
        };
    }

}
