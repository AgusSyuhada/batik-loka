package com.bangkit.batikloka.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import com.bangkit.batikloka.R
import com.bangkit.batikloka.ui.main.MainActivity

class NewsNotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private const val CHANNEL_ID = "batik_news_channel"
        private const val CHANNEL_NAME = "Batik News Updates"
        private const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.news_and_updates)
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 200, 300, 400)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showDummyNotifications() {
        val dummyNotifications = listOf(
            createDummyNewsNotification(
                context.getString(R.string.latest_news_notification),
                context.getString(R.string.latest_news_notification_description),
                R.drawable.card
            )
        )

        dummyNotifications.forEachIndexed { index, notification ->
            Handler(Looper.getMainLooper()).postDelayed({
                notificationManager.notify(NOTIFICATION_ID + index, notification)
            }, (index * 2000).toLong())
        }
    }

    private fun createDummyNewsNotification(
        title: String,
        content: String,
        @DrawableRes imageResId: Int
    ): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("OPEN_FRAGMENT", "NEWS_FRAGMENT")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = BitmapFactory.decodeResource(context.resources, imageResId)

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_batik_loka)
            .setLargeIcon(largeIcon)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(content)
                    .setBigContentTitle(title)
            )
            .build()
    }

    fun clearAllNotifications() {
        notificationManager.cancelAll()
    }
}