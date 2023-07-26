package com.senspark.custom_notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.ViewGroup
import android.view.WindowManager
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
                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    enum class FrameWork {
        Unknown, Unity, Cocos2dx
    }

    private var _framework: FrameWork = FrameWork.Unknown

    fun init(frameWork: Int, context: Context) {
        _framework = FrameWork.values()[frameWork]
        createNotificationChannel(context)
    }

    fun showNotification(
        notificationId: Int,
        body: String,
        clickIntent: PendingIntent? = null
    ) {
        val notification =
            createCustomLayoutNotification(
                CHANNEL_GENERAL_NOTIFICATIONS,
                getString(R.string.app_name),
                body,
                clickIntent
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
        body: String,
        clickIntent: PendingIntent? = null
    ): Notification {
        // Get portrait or landscape layout
        val customLayoutId = R.layout.custom_notification_layout
        val customLayout = RemoteViews(packageName, customLayoutId)

        val parentView = customLayout.apply(applicationContext, null) as ViewGroup
        val imgView = parentView.findViewById<ImageView>(R.id.notification_background)
        val res = resources
        var imgSrc = BitmapFactory.decodeResource(res, R.drawable.notification_background)

        // Make rounded background image
        val dr = RoundedBitmapDrawableFactory.create(res, imgSrc)
        val cornerRadius = res.getDimensionPixelSize(R.dimen.corner_radius).toFloat()
        dr.cornerRadius = cornerRadius
        imgView.setImageDrawable(dr)

        // Set text
        customLayout.setTextViewText(R.id.notification_title, title)
        customLayout.setTextViewText(R.id.notification_body, body)

        // Set Icons
        if (_framework == FrameWork.Unity) {
            // Vì Unity đặt tên như vậy
            customLayout.setImageViewResource(R.id.notification_launcher_icon, R.mipmap.app_icon)
        }

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(Color.MAGENTA)
            .setCustomContentView(customLayout)
            .setAutoCancel(true)

        if (clickIntent != null) {
            builder.setContentIntent(clickIntent)
        }
        return builder.build()
    }
}