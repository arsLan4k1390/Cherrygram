/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core

import android.util.Log
import org.telegram.messenger.BuildVars
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig.isDevBuild

object CherrygramLogger {

    /**
     * Log levels:
     * 
     * D — Debug: detailed logs for development only.
     * I — Info: important runtime events.
     * W — Warn: non-fatal issues and unexpected states.
     * E — Error: failures, exceptions, critical problems.
     */
    private const val TAG: String = "cherrygramLogger"

    private fun isDebuggable(): Boolean {
        return BuildVars.LOGS_ENABLED && isDevBuild()
    }

    /**
     * Logging level:
     * 
     * D (Debug) — отладочная информация.
     * Используется для подробных технических логов, которые нужны только во время разработки:
     * состояния переменных, вызовы методов, промежуточные данные.
     * В релизной сборке обычно отключается.
     */
    @JvmStatic
    fun d(message: () -> String) {
        if (!isDebuggable()) return
        Log.d(TAG, message())
    }

    @JvmStatic
    fun d(t: Throwable) {
        Log.d(TAG, t.message, t)
    }

    @JvmStatic
    fun d(tag: String, message: () -> String) {
        if (!isDebuggable()) return
        Log.d(tag, message())
    }

    @JvmStatic
    fun d(message: () -> String, t: Throwable) {
        if (!isDebuggable()) return
        Log.d(TAG, message(), t)
    }

    @JvmStatic
    fun d(tag: String, message: () -> String, t: Throwable) {
        if (!isDebuggable()) return
        Log.d(tag, message(), t)
    }

    /**
     * Logging level:
     *
     * I (Info) — информационные сообщения.
     * Используется для важных событий приложения:
     * запуск компонентов, успешные операции, ключевые шаги логики.
     * Менее шумный, чем Debug.
     */
    @JvmStatic
    fun i(message: () -> String) {
        if (!isDebuggable()) return
        Log.i(TAG, message())
    }

    @JvmStatic
    fun i(t: Throwable) {
        Log.i(TAG, t.message, t)
    }

    @JvmStatic
    fun i(tag: String, message: () -> String) {
        if (!isDebuggable()) return
        Log.i(tag, message())
    }

    @JvmStatic
    fun i(message: () -> String, t: Throwable) {
        if (!isDebuggable()) return
        Log.i(TAG, message(), t)
    }

    @JvmStatic
    fun i(tag: String, message: () -> String, t: Throwable) {
        if (!isDebuggable()) return
        Log.i(tag, message(), t)
    }

    /**
     * Logging level:
     *
     * W (Warn) — предупреждения.
     * Используется, когда произошло что-то неожиданное или потенциально проблемное,
     * но приложение продолжает работать:
     * fallback-логика, некорректные данные, нестабильное состояние.
     */
    @JvmStatic
    fun w(message: () -> String) {
        if (!isDebuggable()) return
        Log.w(TAG, message())
    }

    @JvmStatic
    fun w(t: Throwable) {
        Log.w(TAG, t.message, t)
    }

    @JvmStatic
    fun w(tag: String, message: () -> String) {
        if (!isDebuggable()) return
        Log.w(tag, message())
    }

    @JvmStatic
    fun w(message: () -> String, t: Throwable) {
        if (!isDebuggable()) return
        Log.w(TAG, message(), t)
    }

    @JvmStatic
    fun w(tag: String, message: () -> String, t: Throwable) {
        if (!isDebuggable()) return
        Log.w(tag, message(), t)
    }

    /**
     * Logging level:
     *
     * E (Error) — ошибки.
     * Используется при сбоях, исключениях и критических проблемах:
     * падения, ошибки БД, сетевые ошибки и т.д.
     * Такие логи желательно сохранять даже в релизной сборке.
     */
    @JvmStatic
    @JvmOverloads
    fun e(message: () -> String, showOnlyInDev: Boolean = false) {
        if (showOnlyInDev && isDebuggable()) Log.e(TAG, message())
    }

    @JvmStatic
    @JvmOverloads
    fun e(t: Throwable, showOnlyInDev: Boolean = false) {
        if (showOnlyInDev && isDebuggable()) Log.e(TAG, t.message, t)
    }

    @JvmStatic
    @JvmOverloads
    fun e(tag: String, message: () -> String, showOnlyInDev: Boolean = false) {
        if (showOnlyInDev && isDebuggable()) Log.e(tag, message())
    }

    @JvmStatic
    @JvmOverloads
    fun e(message: () -> String, t: Throwable, showOnlyInDev: Boolean = false) {
        if (showOnlyInDev && isDebuggable()) Log.e(TAG, message(), t)
    }

    @JvmStatic
    @JvmOverloads
    fun e(tag: String, message: () -> String, t: Throwable, showOnlyInDev: Boolean = false) {
        if (showOnlyInDev && isDebuggable()) Log.e(tag, message(), t)
    }

}