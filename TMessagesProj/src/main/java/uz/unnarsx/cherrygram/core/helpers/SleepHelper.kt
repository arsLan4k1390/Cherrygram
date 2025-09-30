package uz.unnarsx.cherrygram.core.helpers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.telegram.messenger.MediaController
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig

class SleepHelper : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (!MediaController.getInstance().isMessagePaused) {
            MediaController.getInstance().pauseMessage(MediaController.getInstance().playingMessageObject)
        }
        CherrygramCoreConfig.sleepTimer = false
    }

}