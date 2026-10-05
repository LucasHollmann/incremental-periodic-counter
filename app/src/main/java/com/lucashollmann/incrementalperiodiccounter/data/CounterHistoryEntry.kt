package com.lucashollmann.incrementalperiodiccounter.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(
    tableName = "counter_history",
    foreignKeys = [
        ForeignKey(
            entity = Counter::class,
            parentColumns = ["id"],
            childColumns = ["counter_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["counter_id", "timestamp"])],
)
data class CounterHistoryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "counter_id")
    val counterId: Long,
    val timestamp: Long,
    @ColumnInfo(name = "new_value")
    val newValue: Int,
    val step: Long,
)
