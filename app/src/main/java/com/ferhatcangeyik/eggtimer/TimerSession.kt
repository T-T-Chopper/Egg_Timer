package com.ferhatcangeyik.eggtimer

import android.content.Context

private const val PREFS_NAME = "EggTimerPrefs"
private const val KEY_LEVEL = "timer_level"
private const val KEY_METHOD = "timer_method"
private const val KEY_SIZE = "timer_size"
private const val KEY_START_TEMP = "timer_start_temp"
private const val KEY_END_AT = "timer_end_at"
private const val KEY_REMAINING = "timer_remaining"
private const val KEY_RUNNING = "timer_running"

private const val KEY_LAST_LEVEL = "last_level"
private const val KEY_LAST_METHOD = "last_method"
private const val KEY_LAST_SIZE = "last_size"
private const val KEY_LAST_START_TEMP = "last_start_temp"

// Süresi çoktan dolmuş bir oturumu geri yüklemek anlamsız: ertesi sabah
// uygulamayı açınca "Yumurta hazır!" ekranıyla karşılaşmayalım.
private const val STALE_AFTER_MS = 6 * 60 * 60 * 1000L

/** Pişirme seçimlerinin tamamı. */
data class CookingChoice(
    val level: EggLevel,
    val method: CookingMethod,
    val size: EggSize,
    val startTemp: EggStartTemp
) {
    val totalSeconds: Int get() = cookingSeconds(level, method, size, startTemp)
}

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
    val choice: CookingChoice,
    val endAt: Long,
    val remainingSeconds: Int,
    val isRunning: Boolean
)

/** [TimerSession] ve son kullanılan seçimler için SharedPreferences sarmalayıcısı. */
class TimerStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): TimerSession? {
        val level = enumOrNull<EggLevel>(prefs.getString(KEY_LEVEL, null)) ?: return null
        val method = enumOrNull<CookingMethod>(prefs.getString(KEY_METHOD, null)) ?: return null

        val endAt = prefs.getLong(KEY_END_AT, 0L)
        if (endAt > 0L && System.currentTimeMillis() - endAt > STALE_AFTER_MS) {
            clearSession()
            return null
        }

        return TimerSession(
            choice = CookingChoice(
                level = level,
                method = method,
                // Boy ve sıcaklık sonradan eklendi: eski kayıtlarda yoksa
                // varsayılana düşeriz.
                size = enumOrNull<EggSize>(prefs.getString(KEY_SIZE, null)) ?: EggSize.MEDIUM,
                startTemp = enumOrNull<EggStartTemp>(prefs.getString(KEY_START_TEMP, null))
                    ?: EggStartTemp.FRIDGE
            ),
            endAt = endAt,
            remainingSeconds = prefs.getInt(KEY_REMAINING, 0),
            isRunning = prefs.getBoolean(KEY_RUNNING, false)
        )
    }

    fun save(session: TimerSession) {
        prefs.edit()
            .putString(KEY_LEVEL, session.choice.level.name)
            .putString(KEY_METHOD, session.choice.method.name)
            .putString(KEY_SIZE, session.choice.size.name)
            .putString(KEY_START_TEMP, session.choice.startTemp.name)
            .putLong(KEY_END_AT, session.endAt)
            .putInt(KEY_REMAINING, session.remainingSeconds)
            .putBoolean(KEY_RUNNING, session.isRunning)
            .apply()
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_LEVEL)
            .remove(KEY_METHOD)
            .remove(KEY_SIZE)
            .remove(KEY_START_TEMP)
            .remove(KEY_END_AT)
            .remove(KEY_REMAINING)
            .remove(KEY_RUNNING)
            .apply()
    }

    /** Bir sonraki açılışta hazır gelsin diye son kullanılan seçimi saklar. */
    fun saveLastChoice(choice: CookingChoice) {
        prefs.edit()
            .putString(KEY_LAST_LEVEL, choice.level.name)
            .putString(KEY_LAST_METHOD, choice.method.name)
            .putString(KEY_LAST_SIZE, choice.size.name)
            .putString(KEY_LAST_START_TEMP, choice.startTemp.name)
            .apply()
    }

    fun lastChoice(): CookingChoice = CookingChoice(
        level = enumOrNull<EggLevel>(prefs.getString(KEY_LAST_LEVEL, null)) ?: EggLevel.SOFT,
        method = enumOrNull<CookingMethod>(prefs.getString(KEY_LAST_METHOD, null))
            ?: CookingMethod.BOILING_WATER,
        size = enumOrNull<EggSize>(prefs.getString(KEY_LAST_SIZE, null)) ?: EggSize.MEDIUM,
        startTemp = enumOrNull<EggStartTemp>(prefs.getString(KEY_LAST_START_TEMP, null))
            ?: EggStartTemp.FRIDGE
    )
}

private inline fun <reified T : Enum<T>> enumOrNull(name: String?): T? =
    name?.let { saved -> enumValues<T>().firstOrNull { it.name == saved } }

/** [endAt] anına kalan saniye; geçmişte kaldıysa 0. */
fun secondsUntil(endAt: Long): Int {
    val diff = endAt - System.currentTimeMillis()
    return if (diff <= 0L) 0 else ((diff + 999L) / 1000L).toInt()
}
