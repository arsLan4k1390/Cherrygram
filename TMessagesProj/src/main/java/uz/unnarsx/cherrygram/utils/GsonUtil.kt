package uz.unnarsx.cherrygram.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.internal.Streams
import com.google.gson.stream.JsonWriter
import java.io.StringWriter

object GsonUtil {

    private val gson = Gson()

    @JvmStatic
    fun toJsonObject(json: String): JsonObject {
        return gson.fromJson(json, JsonObject::class.java)
    }

}