package uz.unnarsx.cherrygram.utils

import java.io.File

object FileUtil {

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

}