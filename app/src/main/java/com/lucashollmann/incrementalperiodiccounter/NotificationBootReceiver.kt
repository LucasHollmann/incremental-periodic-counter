package com.lucashollmann.incrementalperiodiccounter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lucashollmann.incrementalperiodiccounter.data.CounterDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val counters = CounterDatabase.getInstance(context).counterDao().getAllCounters()
                counters.forEach { CounterNotificationScheduler.scheduleNext(context, it) }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
