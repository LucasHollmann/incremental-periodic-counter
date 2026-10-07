package com.lucashollmann.incrementalperiodiccounter

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.lucashollmann.incrementalperiodiccounter.data.Counter
import com.lucashollmann.incrementalperiodiccounter.data.CounterDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CounterNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val counterId = intent.getLongExtra(EXTRA_COUNTER_ID, -1L)
        if (counterId < 0) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val counterDao = CounterDatabase.getInstance(context).counterDao()
                when (intent.getStringExtra(EXTRA_ACTION) ?: intent.action) {
                    ACTION_NOTIFY -> {
                        val counter = counterDao.getCounter(counterId)
                        if (counter != null) {
                            postCounterNotification(context, counter)
                            CounterNotificationScheduler.scheduleNext(context, counter)
                        }
                    }
                    ACTION_INCREMENT -> {
                        val increment = intent.getIntExtra(EXTRA_INCREMENT, 0)
                        if (increment > 0) counterDao.changeCounter(counterId, increment.toLong())
                        NotificationManagerCompat.from(context).cancel(counterId.toInt())
                    }
                    ACTION_DECREMENT -> {
                        val increment = intent.getIntExtra(EXTRA_INCREMENT, 0)
                        if (increment > 0) counterDao.changeCounter(counterId, -increment.toLong())
                        NotificationManagerCompat.from(context).cancel(counterId.toInt())
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun postCounterNotification(context: Context, counter: Counter) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ),
            )
        }

        val openAppIntent = Intent(context, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
            context,
            counter.id.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context)
        }
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(counter.name)
            .setContentText("Valor atual: ${counter.value}")
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setCategory(Notification.CATEGORY_REMINDER)

        val secondary = counter.secondaryIncrement
            ?.takeIf { it != counter.normalIncrement }
        val actions = buildList {
            add(ACTION_DECREMENT to counter.normalIncrement)
            add(ACTION_INCREMENT to counter.normalIncrement)
            if (secondary != null) add(ACTION_INCREMENT to secondary)
        }
        actions.forEachIndexed { index, (action, increment) ->
            val actionIntent = Intent(context, CounterNotificationReceiver::class.java)
                .setAction("$action.${counter.id}.$increment")
                .putExtra(EXTRA_COUNTER_ID, counter.id)
                .putExtra(EXTRA_INCREMENT, increment)
                .putExtra(EXTRA_ACTION, action)
            val actionPendingIntent = PendingIntent.getBroadcast(
                context,
                counter.id.toInt() * 10 + index + 1,
                actionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val label = if (action == ACTION_INCREMENT) "+$increment" else "-$increment"
            builder.addAction(Notification.Action.Builder(null, label, actionPendingIntent).build())
        }

        notificationManager.notify(counter.id.toInt(), builder.build())
    }

    companion object {
        const val ACTION_NOTIFY = "com.lucashollmann.incrementalperiodiccounter.NOTIFY"
        const val ACTION_INCREMENT = "com.lucashollmann.incrementalperiodiccounter.INCREMENT"
        const val ACTION_DECREMENT = "com.lucashollmann.incrementalperiodiccounter.DECREMENT"
        const val EXTRA_COUNTER_ID = "counter_id"
        const val EXTRA_INCREMENT = "increment"
        const val EXTRA_ACTION = "notification_action"
        private const val CHANNEL_ID = "counter_reminders"
    }
}
