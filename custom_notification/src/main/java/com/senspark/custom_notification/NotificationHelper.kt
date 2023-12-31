package com.senspark.custom_notification

import android.app.Activity
import android.app.Notification
import android.app.Notification.DEFAULT_SOUND
import android.app.Notification.DEFAULT_VIBRATE
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.emoji2.text.EmojiCompat
import org.json.JSONObject

/// Expanded View min height 252 dp
/// Collapsed View max height 48 dp
/// Heads-up View min height 88 dp
class NotificationHelper(context: Context) : ContextWrapper(context) {
    companion object {
        const val kCHANNEL_GENERAL_NOTIFICATIONS = "general_notifications"
        const val kNOTIFICATION_EXTRA_DATA = "notificationData"
        const val kNOTIFICATION_ID = "notificationId"

        // Android 12 new behaviour
        // https://developer.android.com/about/versions/12/behavior-changes-12#custom-notifications
        private val isAndroid13OrHigher: Boolean
            get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU // API 33

        private val isAndroid8OrHigher: Boolean
            get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O // API 26

        private val isAndroid6OrHigher: Boolean
            get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M // API 21

        private val isAndroid5OrHigher: Boolean
            get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP // API 21

        @JvmStatic
        fun createNotificationChannel(context: Context) {
            if (isAndroid8OrHigher) {
                // Create Channel if SDK is Oreo (API 26 - Android 8.0) or higher
                val channel = NotificationChannel(
                    kCHANNEL_GENERAL_NOTIFICATIONS,
                    "General Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                )
                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private var _unityActivity: Activity? = null

    fun init(enableLog: Boolean, unityActivity: Activity) {
        _unityActivity = unityActivity
        Logger(enableLog)
        createNotificationChannel(unityActivity)
        EmojiCompat.init(unityActivity)
    }

    fun unityScheduleTime(jsonString: String) {
        Logger.getInstance().log("unityScheduleTime: $jsonString")
        try {
            val json = JSONObject(jsonString)
            var notificationId = json.getInt("notificationId")
            val title = json.getString("title")
            val body = json.getString("body")
            val titleColor = if (json.has("titleColor")) json.getString("titleColor") else null
            val bodyColor = if (json.has("bodyColor")) json.getString("bodyColor") else null
            val backgroundIndex = json.getInt("backgroundIndex")
            val extraData = json.getString("extraData")
            val delaySeconds = json.getLong("delaySeconds")
            val repeatSeconds = json.getLong("repeatSeconds")
            unitySchedule(
                notificationId,
                title,
                body,
                titleColor,
                bodyColor,
                backgroundIndex,
                extraData,
                delaySeconds,
                repeatSeconds
            )
        } catch (e: Exception) {
            Logger.getInstance().error("Error parsing json: $jsonString")
        }
    }

    fun unityScheduleDate(jsonString: String) {
        Logger.getInstance().log("unityScheduleDate: $jsonString")
        try {
            val json = JSONObject(jsonString)
            var notificationId = json.getInt("notificationId")
            val title = json.getString("title")
            val body = json.getString("body")
            val titleColor = if (json.has("titleColor")) json.getString("titleColor") else null
            val bodyColor = if (json.has("bodyColor")) json.getString("bodyColor") else null
            val backgroundIndex = json.getInt("backgroundIndex")
            val extraData = json.getString("extraData")
            val atHour = json.getInt("atHour")
            val atMinute = json.getInt("atMinute")
            val repeatDays = json.getInt("repeatDays")
            unitySchedule(
                notificationId,
                title,
                body,
                titleColor,
                bodyColor,
                backgroundIndex,
                extraData,
                atHour,
                atMinute,
                repeatDays
            )
        } catch (e: Exception) {
            Logger.getInstance().error("Error parsing json: $jsonString")
        }
    }

    fun unitySchedule(
        notificationId: Int,
        title: String,
        body: String,
        titleColor: String?,
        bodyColor: String?,
        backgroundIndex: Int,
        extraData: String?,
        delaySeconds: Long,
        repeatSeconds: Long
    ) {
        val notification = createNotification(
            _unityActivity!!,
            notificationId,
            title,
            body,
            titleColor,
            bodyColor,
            backgroundIndex,
            extraData
        )
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
        title: String,
        body: String,
        titleColor: String?,
        bodyColor: String?,
        backgroundIndex: Int,
        extraData: String?,
        atHour: Int,
        atMinute: Int,
        repeatDays: Int
    ) {
        val notification = createNotification(
            _unityActivity!!,
            notificationId,
            title,
            body,
            titleColor,
            bodyColor,
            backgroundIndex,
            extraData
        )
        AlarmReceiver.schedule(
            notificationId,
            notification,
            atHour,
            atMinute,
            repeatDays,
            _unityActivity!!
        )
    }

    fun cocosCreateNotificationBuilder(body: String): NotificationCompat.Builder {
        val newTitle = getString(R.string.app_name)
        val newBody = processEmojiText(body)

        val views = createMultipleViews(newTitle, newBody, null, null, 0)

        return createNotificationBuilder(
            kCHANNEL_GENERAL_NOTIFICATIONS,
            newTitle,
            newBody,
            views
        )
    }

    fun cancel(
        notificationId: Int
    ) {
        AlarmReceiver.cancel(notificationId, _unityActivity!!)
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

    private fun createNotification(
        activity: Activity,
        notificationId: Int,
        title: String,
        body: String,
        titleColor: String?,
        bodyColor: String?,
        backgroundIndex: Int,
        extraData: String?
    ): Notification {
        val newTitle = processEmojiText(title)
        val newBody = processEmojiText(body)

        val views = createMultipleViews(newTitle, newBody, titleColor, bodyColor, backgroundIndex)

        val builder = createNotificationBuilder(
            kCHANNEL_GENERAL_NOTIFICATIONS,
            newTitle,
            newBody,
            views
        )
        val clickIntent = createExtrasClickIntent(activity, notificationId, extraData)
        builder.setContentIntent(clickIntent)

        return builder.build()
    }

    // Tạo 3 view: expanded, collapsed, headsUp
    private fun createMultipleViews(
        title: String,
        body: String,
        titleColor: String?,
        bodyColor: String?,
        backgroundIndex: Int,
    ): Array<RemoteViews?> {
        val newTitle = processEmojiText(title)
        val newBody = processEmojiText(body)

        var collapsedLayout: RemoteViews? = null
        val expandedLayout: RemoteViews

        if (isAndroid13OrHigher) {
            collapsedLayout = createCustomLayoutNotification(
                newTitle,
                newBody,
                titleColor,
                bodyColor,
                R.layout.custom_notification_layout_collapsed,
                backgroundIndex
            )
            expandedLayout = createCustomLayoutNotification(
                newTitle,
                newBody,
                titleColor,
                bodyColor,
                R.layout.custom_notification_layout_12,
                backgroundIndex
            )
        } else {
            expandedLayout = createCustomLayoutNotification(
                newTitle,
                newBody,
                titleColor,
                bodyColor,
                R.layout.custom_notification_layout,
                backgroundIndex
            )
            expandedLayout.setImageViewResource(R.id.notification_launcher_icon, R.mipmap.app_icon)
        }
        val headsUpLayout = collapsedLayout

        return arrayOf(expandedLayout, collapsedLayout, headsUpLayout)
    }

    // Views must be: [expandedLayout, collapsedLayout, headsUpLayout]
    private fun createNotificationBuilder(
        channelId: String,
        title: String,
        body: String,
        views: Array<RemoteViews?>
    ): NotificationCompat.Builder {
        val value = TypedValue()
        applicationContext.theme.resolveAttribute(
            androidx.appcompat.R.attr.colorPrimary,
            value,
            true
        )

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(value.data)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        if (isAndroid8OrHigher) {
            builder.setDefaults(DEFAULT_SOUND or DEFAULT_VIBRATE)
            builder.setVibrate(longArrayOf(0L))
        }

        val viewsCount = views.count()
        val expandedLayout = views[0]
        val collapsedLayout: RemoteViews? = if (viewsCount >= 2) views[1] else null
        val headsUpLayout: RemoteViews? = if (viewsCount >= 3) views[2] else null

        if (isAndroid13OrHigher) {
            builder
                .setCustomBigContentView(expandedLayout)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setContentTitle(title)
                .setContentText(body)
//            if (collapsedLayout != null) {
//                builder.setCustomContentView(collapsedLayout)
//            }
//            if (headsUpLayout != null) {
//                builder.setCustomHeadsUpContentView(headsUpLayout)
//            }
        } else {
            builder.setCustomContentView(expandedLayout)
        }
        return builder
    }

    private fun createCustomLayoutNotification(
        title: String,
        body: String,
        titleColor: String?,
        bodyColor: String?,
        customLayoutId: Int,
        backgroundIndex: Int,
    ): RemoteViews {
        val customLayout = RemoteViews(packageName, customLayoutId)
        // Set text
        customLayout.setTextViewText(
            R.id.notification_title,
            title
        )
        if (titleColor != null) {
            try {
                val color = Color.parseColor(titleColor)
                customLayout.setTextColor(R.id.notification_title, color)
            } catch (e: Exception) {
                Logger.getInstance().error("Error parsing color: $titleColor")
            }
        }

        customLayout.setTextViewText(
            R.id.notification_body,
            body
        )
        if (bodyColor != null) {
            try {
                val color = Color.parseColor(bodyColor)
                customLayout.setTextColor(R.id.notification_body, color)
            } catch (e: Exception) {
                Logger.getInstance().error("Error parsing color: $bodyColor")
            }
        }

        val backgroundUri = "@drawable/notification_background_$backgroundIndex"
        var backgroundId = resources.getIdentifier(backgroundUri, null, packageName)
        if (backgroundId <= 0) {
            backgroundId = R.drawable.notification_background_blank
        }

        // Set Image
        customLayout.setImageViewResource(
            R.id.notification_background,
            backgroundId
        )

        return customLayout;
    }

    private fun processEmojiText(text: String): String {
        val emoji = EmojiCompat.get()
        val emojiState = emoji.loadState
        return if (emojiState == EmojiCompat.LOAD_STATE_SUCCEEDED) {
            emoji.process(text).toString()
        } else {
            Logger.getInstance().error("EmojiCompat failed to load")
            text
        }
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

        val ptFlags =
            if (isAndroid6OrHigher) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
            } else {
                PendingIntent.FLAG_CANCEL_CURRENT
            }

        return PendingIntent.getActivity(
            activity,
            notificationId,
            it,
            ptFlags
        )
    }
}