package com.lucashollmann.incrementalperiodiccounter.data

data class NotificationSchedule(
    val time: String,
    val days: List<Int>,
)

object NotificationScheduleCodec {
    private val timePattern = Regex("""([01]\d|2[0-3]):[0-5]\d""")

    fun parse(value: String): List<NotificationSchedule> =
        value.split(';')
            .mapNotNull { entry ->
                val parts = entry.split('|', limit = 2)
                val time = parts.getOrNull(0)?.takeIf(timePattern::matches) ?: return@mapNotNull null
                val days = parts.getOrNull(1)
                    .orEmpty()
                    .split(',')
                    .mapNotNull(String::toIntOrNull)
                    .filter { it in 1..7 }
                    .distinct()
                    .sorted()
                NotificationSchedule(time, days)
            }
            .distinctBy(NotificationSchedule::time)
            .sortedBy(NotificationSchedule::time)

    fun encode(schedules: List<NotificationSchedule>): String =
        schedules.distinctBy(NotificationSchedule::time)
            .sortedBy(NotificationSchedule::time)
            .joinToString(";") { schedule ->
                "${schedule.time}|${schedule.days.filter { it in 1..7 }.distinct().sorted().joinToString(",")}"
            }

    fun fromLegacyFields(counter: Counter): List<NotificationSchedule> {
        val days = counter.notificationDays
            .split(',')
            .mapNotNull(String::toIntOrNull)
            .filter { it in 1..7 }
            .distinct()
            .sorted()
        return counter.notificationTimes
            .split(',')
            .filter(timePattern::matches)
            .distinct()
            .sorted()
            .map { time -> NotificationSchedule(time, days) }
    }

    fun forCounter(counter: Counter): List<NotificationSchedule> =
        if (counter.notificationSchedules.isNotBlank()) {
            parse(counter.notificationSchedules)
        } else {
            fromLegacyFields(counter)
        }
}
