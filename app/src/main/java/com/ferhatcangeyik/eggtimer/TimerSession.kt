package com.ferhatcangeyik.eggtimer

import android.content.Context

private const val PREFS_NAME = "EggTimerPrefs"
private const val KEY_LEVEL = "timer_level"
private const val KEY_METHOD = "timer_method"
private const val KEY_END_AT = "timer_end_at"
private const val KEY_REMAINING = "timer_remaining"
private const val KEY_RUNNING = "timer_running"

// Süresi çoktan dolmuş bir oturumu geri yüklemek anlamsız: ertesi sabah
// uygulamayı açınca "Yumurta hazır!" ekranıyla karşılaşmayalım.
private const val STALE_AFTER_MS = 6 * 60 * 60 * 1000L

/**
 * Çalışan zamanlayıcının diskteki hali.
 *
 * Zamanlayıcı geri sayan bir sayaç değil, bir **bitiş anıdır**: [endAt] epoch
 * milisaniye olarak saklanır. Böylece uygulama arka plana alınsa, süreci
 * öldürülse ya da telefon yeniden başlatılsa bile kalan süre saatten yeniden
 * hesaplanabilir.
 *
 * Duraklatılmış zamanlayıcıda [endAt] sıfırdır ve kalan süre
 * [remainingSeconds] içinde tutulur.
 */
data class TimerSession(
    val level: EggLevel,
    val method: CookingMethod,
    val endAt: Long,
    val remainingSeconds: Int,
    val isRunning: Boolean
)

/** [TimerSession] için SharedPreferences sarmalayıcısı. */
class TimerStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): TimerSession? {
        val level = EggLevel.entries.firstOrNull { it.name == prefs.getString(KEY_LEVEL, null) }
            ?: return null
        val method = CookingMethod.entries.firstOrNull { it.name == prefs.getString(KEY_METHOD, null) }
            ?: return null

        val endAt = prefs.getLong(KEY_END_AT, 0L)
        if (endAt > 0L && System.currentTimeMillis() - endAt > STALE_AFTER_MS) {
            clear()
            return null
        }

        return TimerSession(
            level = level,
            method = method,
            endAt = endAt,
            remainingSeconds = prefs.getInt(KEY_REMAINING, 0),
            isRunning = prefs.getBoolean(KEY_RUNNING, false)
        )
    }

    fun save(session: TimerSession) {
        prefs.edit()
            .putString(KEY_LEVEL, session.level.name)
            .putString(KEY_METHOD, session.method.name)
            .putLong(KEY_END_AT, session.endAt)
            .putInt(KEY_REMAINING, session.remainingSeconds)
            .putBoolean(KEY_RUNNING, session.isRunning)
            .apply()
    }

    fun clear() {
        prefs.edit()
            .remove(KEY_LEVEL)
            .remove(KEY_METHOD)
            .remove(KEY_END_AT)
            .remove(KEY_REMAINING)
            .remove(KEY_RUNNING)
            .apply()
    }
}

/** [endAt] anına kalan saniye; geçmişte kaldıysa 0. */
fun secondsUntil(endAt: Long): Int {
    val diff = endAt - System.currentTimeMillis()
    return if (diff <= 0L) 0 else ((diff + 999L) / 1000L).toInt()
}
