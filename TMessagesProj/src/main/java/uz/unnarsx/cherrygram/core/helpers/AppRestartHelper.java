/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.core.helpers;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;

import java.util.ArrayList;
import java.util.Arrays;

public final class AppRestartHelper extends Activity {
    private static final String KEY_RESTART_INTENTS = "cherrygram_restart_intents";
    private static final String KEY_MAIN_PROCESS_PID = "cherrygram_main_process_pid";

    public static void triggerRebirth(Context context, Intent... nextIntents) {
        nextIntents[0].addFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK);
        Intent intent = new Intent(context, AppRestartHelper.class);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.putParcelableArrayListExtra(KEY_RESTART_INTENTS, new ArrayList<>(Arrays.asList(nextIntents)));
        intent.putExtra(KEY_MAIN_PROCESS_PID, Process.myPid());
        context.startActivity(intent);
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Process.killProcess(getIntent().getIntExtra(KEY_MAIN_PROCESS_PID, -1));
        ArrayList<Intent> intents = getIntent().getParcelableArrayListExtra(KEY_RESTART_INTENTS);
        startActivities(intents.toArray(new Intent[0]));
        finish();
        Runtime.getRuntime().exit(0);
    }

    public static void createRestartBulletin(BaseFragment fragment) {
        BulletinFactory.of(fragment).createRestartBulletin(
                R.raw.chats_infotip,
                LocaleController.getString(R.string.CG_RestartToApply),
                LocaleController.getString(R.string.BotUnblock),
                () -> {
                }).show();
    }

    public static void createDebugSuccessBulletin(BaseFragment fragment) {
        BulletinFactory.of(fragment)
                .createSuccessBulletin(LocaleController.getString(R.string.OK))
                .setDuration(Bulletin.DURATION_LONG)
                .show();
    }
}

