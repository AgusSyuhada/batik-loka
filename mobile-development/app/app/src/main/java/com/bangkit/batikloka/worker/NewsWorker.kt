package com.bangkit.batikloka.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.BigPictureStyle
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.local.database.NewsDatabase
import com.bangkit.batikloka.data.remote.api.ApiConfig
import com.bangkit.batikloka.data.repository.NewsRepository
import com.bangkit.batikloka.ui.main.MainActivity
import com.bangkit.batikloka.utils.PreferencesManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NewsWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private val TAG = NewsWorker::class.java.simpleName
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "news_channel"
        const val CHANNEL_NAME = "News Updates"
    }

    private val repository: NewsRepository by lazy {
        val preferencesManager = PreferencesManager(context)
        val apiService = ApiConfig.getNewsApiService(context, preferencesManager)
        val newsDao = NewsDatabase.getDatabase(context).newsDao()
        NewsRepository(apiService, newsDao)
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val newsResult = repository.getNews()
            newsResult.onSuccess { newsList ->
                if (newsList.isNotEmpty()) {
                    val latestNews = newsList.first()
                    val title =
                        context.getString(R.string.latest_news_notification_title, latestNews.judul)
                    val description = latestNews.body?.take(100) + "..."

                    if (latestNews.gambar != null && latestNews.link != null) {
                        showNotification(title, description, latestNews.gambar)
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching news: ${e.message}")
            Result.failure()
        }
    }

    private fun showNotification(
        title: String,
        description: String,
        imageUrl: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("OPEN_FRAGMENT", "NEWS_FRAGMENT")
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        Glide.with(applicationContext)
            .asBitmap()
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_batik_loka)
                        .setContentTitle(title)
                        .setContentText(description)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setStyle(BigPictureStyle().bigPicture(resource))
                        .setDefaults(NotificationCompat.DEFAULT_ALL)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val channel = NotificationChannel(
                            CHANNEL_ID,
                            CHANNEL_NAME,
                            NotificationManager.IMPORTANCE_HIGH
                        )
                        notification.setChannelId(CHANNEL_ID)
                        notificationManager.createNotificationChannel(channel)
                    }

                    notificationManager.notify(NOTIFICATION_ID, notification.build())
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    //
                }
            })
    }
}