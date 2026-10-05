package com.lucashollmann.incrementalperiodiccounter.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CounterDao {
    @Query("SELECT * FROM counters ORDER BY id")
    fun observeCounters(): Flow<List<Counter>>

    @Query("SELECT COUNT(*) FROM counters")
    suspend fun getCounterCount(): Int

    @Insert
    suspend fun insertCounter(counter: Counter)

    @Query("UPDATE counters SET value = value + 1 WHERE id = :counterId")
    suspend fun incrementCounter(counterId: Long)

    @Query("UPDATE counters SET value = value - 1 WHERE id = :counterId")
    suspend fun decrementCounter(counterId: Long)

    @Query("UPDATE counters SET value = 0 WHERE id = :counterId")
    suspend fun resetCounter(counterId: Long)
}
