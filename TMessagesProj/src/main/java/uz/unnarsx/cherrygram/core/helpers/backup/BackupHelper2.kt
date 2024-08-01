package uz.unnarsx.cherrygram.core.helpers.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.telegram.messenger.ApplicationLoader
import org.telegram.ui.LaunchActivity
import java.io.File

object BackupHelper2 {

    @JvmStatic
    fun initDir(dir: File) {
        var parentDir: File? = dir
        while (parentDir != null) {
            if (parentDir.isDirectory) break
            if (parentDir.isFile) parentDir.deleteRecursively()
            parentDir = parentDir.parentFile
        }
        dir.mkdirs()
        // ignored
    }

    @JvmStatic
    fun initFile(file: File) {
        file.parentFile?.also { initDir(it) }
        if (!file.isFile) {
            if (file.isDirectory) file.deleteRecursively()
            if (!file.isFile) {
                if (!file.createNewFile() && !file.isFile) {
                    error("unable to create file ${file.path}")
                }
            }
        }
    }

    @JvmStatic
    fun readUtf8String(file: File) = file.readText()

    @JvmStatic
    fun writeUtf8String(text: String, save: File) {
        initFile(save)
        save.writeText(text)
    }


    // Gson
    private val gson = Gson()

    @JvmStatic
    fun toJsonObject(json: String): JsonObject {
        return gson.fromJson(json, JsonObject::class.java)
    }


    //Sharing the file
    @JvmOverloads
    @JvmStatic
    fun shareFile(ctx: Context, fileToShare: File, caption: String = "") {
        val uri = if (Build.VERSION.SDK_INT >= 24) {
            FileProvider.getUriForFile(ctx, ApplicationLoader.getApplicationId() + ".provider", fileToShare)
        } else {
            Uri.fromFile(fileToShare)
        }

        val i = Intent(Intent.ACTION_SEND)

        if (Build.VERSION.SDK_INT >= 24) {
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        i.type = "message/rfc822"
        i.putExtra(Intent.EXTRA_EMAIL, "")

        if (caption.isNotBlank()) i.putExtra(Intent.EXTRA_SUBJECT, caption)

        i.putExtra(Intent.EXTRA_STREAM, uri)
        i.setClass(ctx, LaunchActivity::class.java)

        ctx.startActivity(i)

    }

}