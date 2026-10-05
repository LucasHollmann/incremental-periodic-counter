package com.lucashollmann.incrementalperiodiccounter

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import com.lucashollmann.incrementalperiodiccounter.data.Counter
import com.lucashollmann.incrementalperiodiccounter.data.NotificationScheduleCodec
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal object CounterNotificationScheduler {
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT)

    fun scheduleNext(context: Context, counter: Counter) {
        val schedules = NotificationScheduleCodec.forCounter(counter)
            .filter { it.days.isNotEmpty() }
        if (schedules.isEmpty()) {
            cancel(context, counter.id)
            return
        }

        val now = LocalDateTime.now()
        val nextTime = schedules
            .flatMap { schedule ->
                (0L..7L).mapNotNull { dayOffset ->
                    val candidate = now.toLocalDate()
                        .plusDays(dayOffset)
                        .atTime(LocalTime.parse(schedule.time, timeFormatter))
                    candidate.takeIf {
                        it.isAfter(now) &&
                            it.dayOfWeek.value in schedule.days
                    }
                }
            }
            .minOrNull() ?: return
        val triggerAtMillis = nextTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = alarmPendingIntent(context, counter.id)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent,
                )
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent,
                )
            }
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    fun cancel(context: Context, counterId: Long) {
        val pendingIntent = alarmPendingIntent(context, counterId)
        context.getSystemService(AlarmManager::class.java).cancel(pendingIntent)
        pendingIntent.cancel()
        context.getSystemService(NotificationManager::class.java).cancel(counterId.toInt())
    }

    private fun alarmPendingIntent(context: Context, counterId: Long): PendingIntent {
        val intent = Intent(context, CounterNotificationReceiver::class.java)
            .setAction(CounterNotificationReceiver.ACTION_NOTIFY)
            .putExtra(CounterNotificationReceiver.EXTRA_COUNTER_ID, counterId)
        return PendingIntent.getBroadcast(
            context,
            counterId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
