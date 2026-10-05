package com.lucashollmann.incrementalperiodiccounter

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lucashollmann.incrementalperiodiccounter.data.Counter
import com.lucashollmann.incrementalperiodiccounter.data.CounterDatabase
import kotlinx.coroutines.flow.SharingStarted
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

    fun incrementCounter(counterId: Long) {
        viewModelScope.launch {
            counterDao.incrementCounter(counterId)
        }
    }

    fun decrementCounter(counterId: Long) {
        viewModelScope.launch {
            counterDao.decrementCounter(counterId)
        }
    }

    fun resetCounter(counterId: Long) {
        viewModelScope.launch {
            counterDao.resetCounter(counterId)
        }
    }
}
