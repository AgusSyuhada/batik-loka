package com.bangkit.batikloka.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class NewsWorkerScheduler {
    fun scheduleNewsCheck(context: Context) {
        val newsCheckRequest = PeriodicWorkRequestBuilder<NewsWorker>(
            15,
            TimeUnit.MINUTES
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "news_check_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            newsCheckRequest
        )
    }
}