/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.preferences

import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class StringPreference(
        private val sharedPreferences: SharedPreferences,
        private val key: String,
        private val defaultValue: String,
) : ReadWriteProperty<Any, String> {
    override fun getValue(thisRef: Any, property: KProperty<*>): String = sharedPreferences.getString(key, defaultValue)!!
    override fun setValue(thisRef: Any, property: KProperty<*>, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
}

class IntPreference(
        private val sharedPreferences: SharedPreferences,
        private val key: String,
        private val defaultValue: Int,
) : ReadWriteProperty<Any, Int> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Int = sharedPreferences.getInt(key, defaultValue)
    override fun setValue(thisRef: Any, property: KProperty<*>, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }
}

class FloatPreference(
    private val sharedPreferences: SharedPreferences,
    private val key: String,
    private val defaultValue: Float,
) : ReadWriteProperty<Any, Float> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Float = sharedPreferences.getFloat(key, defaultValue)
    override fun setValue(thisRef: Any, property: KProperty<*>, value: Float) {
        sharedPreferences.edit().putFloat(key, value).apply()
    }
}

class BooleanPreference(
        private val sharedPreferences: SharedPreferences,
        private val key: String,
        private val defaultValue: Boolean,
) : ReadWriteProperty<Any, Boolean> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Boolean = sharedPreferences.getBoolean(key, defaultValue)
    override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }
}

class LongPreference(
        private val sharedPreferences: SharedPreferences,
        private val key: String,
        private val defaultValue: Long,
) : ReadWriteProperty<Any, Long> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Long = sharedPreferences.getLong(key, defaultValue)
    override fun setValue(thisRef: Any, property: KProperty<*>, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
    }
}

fun SharedPreferences.int(key: String, defaultValue: Int): ReadWriteProperty<Any, Int> = IntPreference(this, key, defaultValue)
fun SharedPreferences.float(key: String, defaultValue: Float): ReadWriteProperty<Any, Float> = FloatPreference(this, key, defaultValue)
fun SharedPreferences.boolean(key: String, defaultValue: Boolean): ReadWriteProperty<Any, Boolean> = BooleanPreference(this, key, defaultValue)
fun SharedPreferences.string(key: String, defaultValue: String): ReadWriteProperty<Any, String> = StringPreference(this, key, defaultValue)
fun SharedPreferences.long(key: String, defaultValue: Long): ReadWriteProperty<Any, Long> = LongPreference(this, key, defaultValue)