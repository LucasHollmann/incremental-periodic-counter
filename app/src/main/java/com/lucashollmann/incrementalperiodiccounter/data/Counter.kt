package com.lucashollmann.incrementalperiodiccounter.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "counters")
data class Counter(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val value: Int = 0,
)
