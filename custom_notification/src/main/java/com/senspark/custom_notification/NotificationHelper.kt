package com.senspark.custom_notification

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import org.json.JSONObject

class NotificationHelper(context: Context) : ContextWrapper(context) {
    companion object {
        const val kCHANNEL_GENERAL_NOTIFICATIONS = "general_notifications"
        const val kNOTIFICATION_EXTRA_DATA = "notificationData"
        const val kNOTIFICATION_ID = "notificationId"

        @JvmStatic
        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create Channel if SDK is Oreo (API 26 - Android 8.0) or higher
                val channel = NotificationChannel(
                    kCHANNEL_GENERAL_NOTIFICATIONS,
                    "General Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    private var _unityActivity: Activity? = null

    fun init(enableLog: Boolean, unityActivity: Activity) {
        _unityActivity = unityActivity
        Logger(enableLog)
        createNotificationChannel(unityActivity)
    }

    fun unitySchedule(
        notificationId: Int,
        body: String,
        extraData: String?,
        delaySeconds: Long,
        repeatSeconds: Long
    ) {
        val notification = createNotification(_unityActivity!!, notificationId, body, extraData)
        if (delaySeconds > 0) {
            AlarmReceiver.schedule(
                notificationId,
                notification,
                delaySeconds,
                repeatSeconds,
                _unityActivity!!
            )
        } else {
            notificationManager.notify(notificationId, notification)
        }
    }

    fun unitySchedule(
        notificationId: Int,
        body: String,
        extraData: String?,
        atHour: Int,
        atMinute: Int,
        repeatDays: Int
    ) {
        val notification = createNotification(_unityActivity!!, notificationId, body, extraData)
        AlarmReceiver.schedule(
            notificationId,
            notification,
            atHour,
            atMinute,
            repeatDays,
            _unityActivity!!
        )
    }

    // For Unity
    fun showNotification(
        notificationId: Int,
        body: String,
        extraData: String?
    ) {
        val customLayout = createCustomLayoutNotification(getString(R.string.app_name), body)
        customLayout.setImageViewResource(R.id.notification_launcher_icon, R.mipmap.app_icon)

        val builder = createNotificationBuilder(kCHANNEL_GENERAL_NOTIFICATIONS, customLayout)
        builder.setContentIntent(
            createExtrasClickIntent(
                _unityActivity!!,
                notificationId,
                extraData
            )
        )

        notificationManager.notify(notificationId, builder.build());
    }

    // For Cocos2dx
    fun showNotification(
        notificationId: Int,
        body: String,
        clickIntent: PendingIntent? = null
    ) {
        val customLayout = createCustomLayoutNotification(getString(R.string.app_name), body)

        val builder = createNotificationBuilder(kCHANNEL_GENERAL_NOTIFICATIONS, customLayout)
        if (clickIntent != null) {
            builder.setContentIntent(clickIntent)
        }

        builder.setAutoCancel(true)
        notificationManager.notify(notificationId, builder.build());
    }

    fun createCocosNotification(
        activity: Activity,
        notificationId: Int,
        body: String
    ) {
        val customLayout = createCustomLayoutNotification(getString(R.string.app_name), body)
        var clickIntent = createClickIntent(activity)

        val builder = createNotificationBuilder(kCHANNEL_GENERAL_NOTIFICATIONS, customLayout)
        builder
            .setContentIntent(clickIntent)
            .setAutoCancel(true)
        notificationManager.notify(notificationId, builder.build());
    }

    fun showCocosNotification(
        activity: Activity,
        notificationId: Int,
        title: String,
        body: String
    ) {
        val clickIntent = createClickIntent(activity)

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "ee_x_channel_id_01"
        val channelName = "ee_x_channel_name"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.setShowBadge(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)
        notificationBuilder.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.mipmap.app_icon)
            .setContentIntent(clickIntent)
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    // Lấy ra extraData của notification gần nhất
    fun getIntentExtraData(): String {
        if (_unityActivity == null) return ""
        val intent = _unityActivity!!.intent
        val id = intent.getIntExtra(kNOTIFICATION_ID, -1)
        if (id >= 0) {
            notificationManager.cancel(id)
        }
        val jsonObject = JSONObject();
        jsonObject.put("id", id)
        jsonObject.put("extraData", intent.getStringExtra(kNOTIFICATION_EXTRA_DATA))
        return jsonObject.toString()
    }

    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun createNotification(
        Activity: Activity,
        notificationId: Int,
        body: String,
        extraData: String?
    ): Notification {
        val customLayout = createCustomLayoutNotification(getString(R.string.app_name), body)
        customLayout.setImageViewResource(R.id.notification_launcher_icon, R.mipmap.app_icon)

        val builder = createNotificationBuilder(kCHANNEL_GENERAL_NOTIFICATIONS, customLayout)
        val clickIntent = createExtrasClickIntent(Activity, notificationId, extraData)
        builder.setContentIntent(clickIntent)

        return builder.build()
    }

    private fun createNotificationBuilder(
        channelId: String,
        layout: RemoteViews
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(Color.MAGENTA)
            .setCustomContentView(layout)
    }

    private fun createCustomLayoutNotification(
        title: String,
        body: String
    ): RemoteViews {
        val customLayoutId = R.layout.custom_notification_layout
        val customLayout = RemoteViews(packageName, customLayoutId)

        // Set text
        customLayout.setTextViewText(R.id.notification_title, title)
        customLayout.setTextViewText(R.id.notification_body, body)

        return customLayout;
    }

    private fun createClickIntent(activity: Activity): PendingIntent? {
        val intent = Intent(activity, activity.javaClass)
            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        return PendingIntent.getActivity(
            activity,
            0,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createExtrasClickIntent(
        activity: Activity,
        notificationId: Int,
        extraData: String?
    ): PendingIntent {
        val it = Intent(activity, activity.javaClass)
        it.putExtra(kNOTIFICATION_ID, notificationId)
        if (extraData != null) {
            it.putExtra(kNOTIFICATION_EXTRA_DATA, extraData)
        }
        it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

        return PendingIntent.getActivity(
            activity,
            0,
            it,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}