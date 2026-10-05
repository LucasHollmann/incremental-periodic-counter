package com.lucashollmann.incrementalperiodiccounter.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "counters")
data class Counter(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val value: Int = 0,
    @ColumnInfo(name = "normal_increment")
    val normalIncrement: Int = 1,
    @ColumnInfo(name = "secondary_increment")
    val secondaryIncrement: Int? = null,
    @ColumnInfo(name = "allow_negative", defaultValue = "0")
    val allowNegative: Boolean = false,
    @ColumnInfo(name = "notification_times", defaultValue = "''")
    val notificationTimes: String = "",
    @ColumnInfo(name = "notification_days", defaultValue = "'1,2,3,4,5,6,7'")
    val notificationDays: String = "1,2,3,4,5,6,7",
    @ColumnInfo(name = "notification_schedules", defaultValue = "''")
    val notificationSchedules: String = "",
)
