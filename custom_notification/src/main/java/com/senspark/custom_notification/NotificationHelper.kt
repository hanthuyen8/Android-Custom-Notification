package com.senspark.custom_notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory

class NotificationHelper(context: Context) : ContextWrapper(context) {
    companion object {
        const val CHANNEL_GENERAL_NOTIFICATIONS = "general_notifications"

        @JvmStatic
        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create Channel if SDK is Oreo (API 26 - Android 8.0) or higher
                val channel = NotificationChannel(
                    CHANNEL_GENERAL_NOTIFICATIONS,
                    "General Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                val notificationManager = context.getSystemService(NotificationManager::class.java);
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    fun showNotification(notificationId: Int, body: String) {
        val notification =
            createCustomLayoutNotification(
                CHANNEL_GENERAL_NOTIFICATIONS,
                getString(R.string.app_name),
                body
            )
        notificationManager.notify(notificationId, notification);
    }

    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun createNotification(channelId: String, title: String, body: String): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(applicationContext, channelId)
        } else {
            Notification.Builder(applicationContext)
        }

        return builder
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_notification)
            .build()
    }

    private fun createCustomLayoutNotification(
        channelId: String,
        title: String,
        body: String
    ): Notification {
        val customLayout = RemoteViews(packageName, R.layout.custom_notification_layout)
        val parentView = customLayout.apply(applicationContext, null) as ViewGroup

        // Make rounded background image
        val imgView = parentView.findViewById<ImageView>(R.id.notification_background)
        val res = resources
        val imgSrc = BitmapFactory.decodeResource(res, R.drawable.notification_background)
        val dr = RoundedBitmapDrawableFactory.create(res, imgSrc)
        val cornerRadius = resources.getDimensionPixelSize(R.dimen.corner_radius).toFloat()
        dr.cornerRadius = cornerRadius
        imgView.setImageDrawable(dr)

        // Set text
        customLayout.setTextViewText(R.id.notification_title, title)
        customLayout.setTextViewText(R.id.notification_body, body)
//        customLayout.setImageViewResource(R.id.notification_banner, R.drawable.ic_notification_banner)

        return NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setCustomContentView(customLayout)
            .setAutoCancel(true)
            .build()
    }
}