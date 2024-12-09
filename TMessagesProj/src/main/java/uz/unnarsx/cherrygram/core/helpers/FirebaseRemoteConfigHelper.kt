package uz.unnarsx.cherrygram.core.helpers

import android.widget.Toast
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.BuildVars
import org.telegram.messenger.FileLog
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramCameraConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramDebugConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramPrivacyConfig
import uz.unnarsx.cherrygram.misc.Constants
import kotlin.coroutines.resume

object FirebaseRemoteConfigHelper {

    private suspend fun activate(
        fetchInterval: Long
    ): Result<FirebaseRemoteConfig> = suspendCancellableCoroutine { continuation ->
        FirebaseRemoteConfig.getInstance().fetch(fetchInterval)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    FirebaseRemoteConfig
                        .getInstance()
                        .activate()
                        .addOnCompleteListener {
                            continuation.resume(Result.success(FirebaseRemoteConfig.getInstance()))
                        }
                } else {
                    continuation.resume(Result.failure(task.exception ?: Exception()))
                }
            }
            /*.addOnFailureListener { continuation.resume(Result.failure(it.cause ?: Exception())) }
            .addOnCanceledListener { continuation.resume(Result.failure(Exception())) }*/
    }

    suspend fun initRemoteConfig() = withContext(Dispatchers.IO) {
        val fetchInterval = if (CherrygramCoreConfig.isDevBuild()) 10800 else 43200 // 12 hours

        activate(fetchInterval.toLong())
            .onSuccess {
                if (it.getLong(Constants.Videomessages_Resolution) != 0L) {
                    setRoundVideoResolution(it.getLong(Constants.Videomessages_Resolution))
                }
                toggleReTgCheck(it.getBoolean(Constants.Re_Tg_Check))

                if (CherrygramCoreConfig.isDevBuild() || CherrygramDebugConfig.showRPCErrors) {
                    AndroidUtilities.runOnUIThread {
                        Toast.makeText(ApplicationLoader.applicationContext, "Fetch and activate succeeded", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .onFailure {
                if (CherrygramCoreConfig.isDevBuild() || CherrygramDebugConfig.showRPCErrors) {
                    AndroidUtilities.runOnUIThread {
                        Toast.makeText(ApplicationLoader.applicationContext, "Fetch failed", Toast.LENGTH_SHORT).show()
                    }
                }
                FileLog.e(it, false)
            }
    }

    fun isFeatureEnabled(featureFlag: String?): Boolean {
        return if (ApplicationLoader.checkPlayServices()) {
            FirebaseRemoteConfig.getInstance().getBoolean(featureFlag!!)
        } else {
            false
        }
    }

    private fun setRoundVideoResolution(resolution: Long) {
        if (CherrygramCoreConfig.isDevBuild() || BuildVars.LOGS_ENABLED) {
            FileLog.d("Old videomessages resolution:" + CherrygramCameraConfig.videoMessagesResolution)
        }

        CherrygramCameraConfig.videoMessagesResolution = resolution.toInt()

        if (CherrygramCoreConfig.isDevBuild() || BuildVars.LOGS_ENABLED) {
            FileLog.d("New videomessages resolution:" + CherrygramCameraConfig.videoMessagesResolution)
        }
    }

    private fun toggleReTgCheck(enable: Boolean) {
        if (CherrygramCoreConfig.isDevBuild() || BuildVars.LOGS_ENABLED) {
            FileLog.d("Old reTg value:" + CherrygramPrivacyConfig.reTgCheck)
        }

        CherrygramPrivacyConfig.reTgCheck = enable

        if (CherrygramCoreConfig.isDevBuild() || BuildVars.LOGS_ENABLED) {
            FileLog.d("New reTg value:" + CherrygramPrivacyConfig.reTgCheck)
        }
    }

    /*fun getVideoMessageResolution(): Int {
        val res = if (FirebaseRemoteConfig.getInstance().getLong(Constants.Videomessages_Resolution) != 0L) {
            FirebaseRemoteConfig.getInstance().getLong(Constants.Videomessages_Resolution).toInt()
        } else {
            512
        }

        if (CherrygramConfig.isDevBuild()) {
            FileLog.d("VideoMessages resolution: $res")
        }

        return res
    }*/
}