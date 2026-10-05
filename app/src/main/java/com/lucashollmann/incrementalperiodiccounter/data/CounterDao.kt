package com.lucashollmann.incrementalperiodiccounter.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CounterDao {
    @Query("SELECT * FROM counters ORDER BY id")
    fun observeCounters(): Flow<List<Counter>>

    @Query("SELECT COUNT(*) FROM counters")
    suspend fun getCounterCount(): Int

    @Query("SELECT * FROM counters WHERE id = :counterId")
    suspend fun getCounter(counterId: Long): Counter?

    @Query("SELECT * FROM counters")
    suspend fun getAllCounters(): List<Counter>

    @Query("SELECT * FROM counter_history WHERE counter_id = :counterId ORDER BY timestamp DESC, id DESC")
    fun observeHistory(counterId: Long): Flow<List<CounterHistoryEntry>>

    @Insert
    suspend fun insertCounter(counter: Counter)

    @Query("DELETE FROM counters WHERE id = :counterId")
    suspend fun deleteCounter(counterId: Long)

    @Insert
    suspend fun insertHistoryEntry(entry: CounterHistoryEntry)

    @Query("UPDATE counters SET value = :value WHERE id = :counterId")
    suspend fun updateCounterValue(counterId: Long, value: Int)

    @Query("DELETE FROM counter_history WHERE counter_id = :counterId")
    suspend fun deleteCounterHistory(counterId: Long)

    @Transaction
    suspend fun changeCounter(counterId: Long, step: Long) {
        val counter = getCounter(counterId) ?: return
        val newValue = (counter.value.toLong() + step)
            .coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong())
            .let { if (counter.allowNegative) it else it.coerceAtLeast(0) }
            .toInt()
        updateCounterValue(counterId, newValue)
        insertHistoryEntry(
            CounterHistoryEntry(
                counterId = counterId,
                timestamp = System.currentTimeMillis(),
                newValue = newValue,
                step = step,
            ),
        )
    }

    @Transaction
    suspend fun setCounterValue(counterId: Long, value: Int) {
        val counter = getCounter(counterId) ?: return
        if (!counter.allowNegative && value < 0) return
        updateCounterValue(counterId, value)
        insertHistoryEntry(
            CounterHistoryEntry(
                counterId = counterId,
                timestamp = System.currentTimeMillis(),
                newValue = value,
                step = value.toLong() - counter.value,
            ),
        )
    }

    @Transaction
    suspend fun resetCounter(counterId: Long, clearHistory: Boolean) {
        val counter = getCounter(counterId) ?: return
        updateCounterValue(counterId, 0)
        if (clearHistory) {
            deleteCounterHistory(counterId)
        } else {
            insertHistoryEntry(
                CounterHistoryEntry(
                    counterId = counterId,
                    timestamp = System.currentTimeMillis(),
                    newValue = 0,
                    step = -counter.value.toLong(),
                ),
            )
        }
    }

    @Query(
        """
        UPDATE counters
        SET name = :name,
            normal_increment = :normalIncrement,
            secondary_increment = :secondaryIncrement,
            notification_times = '',
            notification_days = '1,2,3,4,5,6,7',
            notification_schedules = :notificationSchedules,
            value = CASE
                WHEN :allowNegative = 0 AND value < 0 THEN 0
                ELSE value
            END,
            allow_negative = :allowNegative
        WHERE id = :counterId
        """,
    )
    suspend fun updateCounterSettings(
        counterId: Long,
        name: String,
        normalIncrement: Int,
        secondaryIncrement: Int?,
        allowNegative: Boolean,
        notificationSchedules: String,
    )
}
