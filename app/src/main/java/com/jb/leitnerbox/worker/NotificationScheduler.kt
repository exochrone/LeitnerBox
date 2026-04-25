package com.jb.leitnerbox.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    private const val WORK_NAME = "session_reminder"

    fun schedule(context: Context, time: LocalTime) {
        val now = LocalDateTime.now()
        var nextTrigger = LocalDateTime.of(LocalDate.now(), time)
        if (nextTrigger.isBefore(now)) {
            nextTrigger = nextTrigger.plusDays(1)
        }
        val initialDelay = Duration.between(now, nextTrigger).toMillis()

        val request = PeriodicWorkRequestBuilder<SessionReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
