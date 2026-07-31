package com.ferhatcangeyik.eggtimer

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

const val ALARM_CHANNEL_ID = "egg_timer_alarm"
private const val ALARM_NOTIFICATION_ID = 1001
private const val ALARM_REQUEST_CODE = 42

private val ALARM_VIBRATION_PATTERN = longArrayOf(0, 800, 300, 800, 300, 800)

/**
 * Zamanlayıcı arka plandayken süre dolduğunda telefonu uyandıran alarm.
 *
 * Uygulama ön plandayken alarm kurulmaz; oradaki uyarıyı ekranın kendi
 * ses/titreşim döngüsü üstlenir. Alarm yalnızca uygulama arka plana
 * alındığında kurulur ve geri dönüldüğünde iptal edilir, böylece iki
 * uyarı üst üste binmez.
 */
object EggAlarm {

    fun schedule(context: Context, triggerAtMillis: Long) {
        val manager = context.getSystemService(AlarmManager::class.java) ?: return
        val pending = pendingIntent(context)

        // Tam zamanlı alarm izni yoksa yaklaşık alarma düşeriz: uyku modunda
        // birkaç dakika sapabilir ama zamanlayıcı yine de çalar.
        val canBeExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            manager.canScheduleExactAlarms()

        try {
            if (canBeExact) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
            } else {
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
            }
        } catch (_: SecurityException) {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
        }
    }

    fun cancel(context: Context) {
        context.getSystemService(AlarmManager::class.java)?.cancel(pendingIntent(context))
        NotificationManagerCompat.from(context).cancel(ALARM_NOTIFICATION_ID)
    }

    private fun pendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        ALARM_REQUEST_CODE,
        Intent(context, AlarmReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

/** Bildirim kanalını oluşturur; zaten varsa dokunmaz. */
fun ensureAlarmChannel(context: Context, strings: LocalizedStrings) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val manager = context.getSystemService(NotificationManager::class.java) ?: return
    if (manager.getNotificationChannel(ALARM_CHANNEL_ID) != null) return

    val channel = NotificationChannel(
        ALARM_CHANNEL_ID,
        strings.notificationChannelName,
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        description = strings.notificationChannelDescription
        enableVibration(true)
        vibrationPattern = ALARM_VIBRATION_PATTERN
        setSound(
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
    }
    manager.createNotificationChannel(channel)
}

/** Süre dolduğunda tetiklenip bildirimi gösteren alıcı. */
class AlarmReceiver : BroadcastReceiver() {

    // İzin aşağıda açıkça kontrol ediliyor; lint koşulu tanımıyor.
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val strings = localizedStrings(storedLanguage(context))
        ensureAlarmChannel(context, strings)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            // Bildirim izni yok: sessizce vazgeç, uygulama açıldığında ekran
            // zaten "hazır" durumunu gösterecek.
            return
        }

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_egg)
            .setContentTitle(strings.timerReadyTitle)
            .setContentText(strings.timerReadySubtitle)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(openApp)
            .setVibrate(ALARM_VIBRATION_PATTERN)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .build()

        NotificationManagerCompat.from(context).notify(ALARM_NOTIFICATION_ID, notification)
    }
}
