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
import uz.unnarsx.cherrygram.CherrygramConfig
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
        val fetchInterval = if (CherrygramConfig.isDevBuild()) 10800 else 43200 // 12 hours

        activate(fetchInterval.toLong())
            .onSuccess {
                if (it.getLong(Constants.Videomessages_Resolution) != 0L) {
                    setRoundVideoResolution(it.getLong(Constants.Videomessages_Resolution))
                }
                if (CherrygramConfig.isDevBuild() || CherrygramConfig.showRPCErrors) {
                    AndroidUtilities.runOnUIThread {
                        Toast.makeText(ApplicationLoader.applicationContext, "Fetch and activate succeeded", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .onFailure {
                if (CherrygramConfig.isDevBuild() || CherrygramConfig.showRPCErrors) {
                    AndroidUtilities.runOnUIThread {
                        Toast.makeText(ApplicationLoader.applicationContext, "Fetch failed", Toast.LENGTH_SHORT).show()
                    }
                }
                FileLog.e(it, false)
            }
    }

    fun isFeatureEnabled(featureFlag: String?): Boolean {
        return FirebaseRemoteConfig.getInstance().getBoolean(featureFlag!!)
    }

    private fun setRoundVideoResolution(resolution: Long) {
        if (CherrygramConfig.isDevBuild() || BuildVars.LOGS_ENABLED) {
            FileLog.d("Old videomessages resolution:" + CherrygramConfig.videoMessagesResolution)
        }

        CherrygramConfig.videoMessagesResolution = resolution.toInt()

        if (CherrygramConfig.isDevBuild() || BuildVars.LOGS_ENABLED) {
            FileLog.d("New videomessages resolution:" + CherrygramConfig.videoMessagesResolution)
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