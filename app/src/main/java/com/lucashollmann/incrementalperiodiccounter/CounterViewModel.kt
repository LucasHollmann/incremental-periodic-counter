package com.lucashollmann.incrementalperiodiccounter

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lucashollmann.incrementalperiodiccounter.data.Counter
import com.lucashollmann.incrementalperiodiccounter.data.CounterHistoryEntry
import com.lucashollmann.incrementalperiodiccounter.data.CounterDatabase
import com.lucashollmann.incrementalperiodiccounter.data.NotificationSchedule
import com.lucashollmann.incrementalperiodiccounter.data.NotificationScheduleCodec
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CounterViewModel(application: Application) : AndroidViewModel(application) {
    private val counterDao = CounterDatabase.getInstance(application).counterDao()

    val counters = counterDao.observeCounters().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    init {
        viewModelScope.launch {
            if (counterDao.getCounterCount() == 0) {
                counterDao.insertCounter(Counter(name = "Meu contador"))
            }
        }
    }

    fun addCounter(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return

        viewModelScope.launch {
            counterDao.insertCounter(Counter(name = trimmedName))
        }
    }

    fun deleteCounter(counterId: Long) {
        viewModelScope.launch {
            CounterNotificationScheduler.cancel(getApplication(), counterId)
            counterDao.deleteCounter(counterId)
        }
    }

    fun observeCounterHistory(counterId: Long): Flow<List<CounterHistoryEntry>> =
        counterDao.observeHistory(counterId)

    fun rescheduleNotifications() {
        viewModelScope.launch {
            counterDao.getAllCounters().forEach {
                CounterNotificationScheduler.scheduleNext(getApplication(), it)
            }
        }
    }

    fun incrementCounter(counterId: Long, increment: Int) {
        if (increment <= 0) return

        viewModelScope.launch {
            counterDao.changeCounter(counterId, increment.toLong())
        }
    }

    fun decrementCounter(counterId: Long, increment: Int) {
        if (increment <= 0) return

        viewModelScope.launch {
            counterDao.changeCounter(counterId, -increment.toLong())
        }
    }

    fun resetCounter(counterId: Long, clearHistory: Boolean) {
        viewModelScope.launch {
            counterDao.resetCounter(counterId, clearHistory)
        }
    }

    fun setCounterValue(counterId: Long, value: Int) {
        viewModelScope.launch {
            counterDao.setCounterValue(counterId, value)
        }
    }

    fun updateCounterSettings(
        counterId: Long,
        name: String,
        normalIncrement: Int,
        secondaryIncrement: Int?,
        allowNegative: Boolean,
        notificationSchedules: List<NotificationSchedule>,
    ) {
        val trimmedName = name.trim()
        if (
            trimmedName.isEmpty() ||
            normalIncrement <= 0 ||
            secondaryIncrement?.let { it <= 0 } == true
                || notificationSchedules.any {
                    !it.time.matches(Regex("""([01]\d|2[0-3]):[0-5]\d""")) ||
                        it.days.any { day -> day !in 1..7 }
                }
        ) {
            return
        }

        viewModelScope.launch {
            counterDao.updateCounterSettings(
                counterId,
                trimmedName,
                normalIncrement,
                secondaryIncrement,
                allowNegative,
                NotificationScheduleCodec.encode(notificationSchedules),
            )
            counterDao.getCounter(counterId)?.let {
                CounterNotificationScheduler.scheduleNext(getApplication(), it)
            }
        }
    }
}
