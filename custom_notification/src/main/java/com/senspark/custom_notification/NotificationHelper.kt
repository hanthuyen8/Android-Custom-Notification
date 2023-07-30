package com.senspark.custom_notification

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import org.json.JSONObject

class NotificationHelper(context: Context) : ContextWrapper(context) {
    companion object {
        const val kCHANNEL_GENERAL_NOTIFICATIONS = "general_notifications"
        const val kNOTIFICATION_EXTRA_DATA = "notificationData"
        const val kNOTIFICATION_ID = "notificationId"
        const val TAG_UNITY = "Unity"

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
        val notification = createNotification(notificationId, body, extraData)
        if (delaySeconds > 0) {
            AlarmReceiver.schedule(
                notificationId,
                notification,
                delaySeconds,
                repeatSeconds,
                applicationContext
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
        val notification = createNotification(notificationId, body, extraData)
        AlarmReceiver.schedule(
            notificationId,
            notification,
            atHour,
            atMinute,
            repeatDays,
            applicationContext
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
        builder.setContentIntent(createClickIntent(notificationId, extraData, _unityActivity!!))

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

        notificationManager.notify(notificationId, builder.build());
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
        notificationId: Int,
        body: String,
        extraData: String?
    ): Notification {
        val customLayout = createCustomLayoutNotification(getString(R.string.app_name), body)
        customLayout.setImageViewResource(R.id.notification_launcher_icon, R.mipmap.app_icon)

        val builder = createNotificationBuilder(kCHANNEL_GENERAL_NOTIFICATIONS, customLayout)
        builder.setContentIntent(createClickIntent(notificationId, extraData, _unityActivity!!))

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

        val parentView = customLayout.apply(applicationContext, null) as ViewGroup
        val imgView = parentView.findViewById<ImageView>(R.id.notification_background)
        val res = resources
        val imgSrc = BitmapFactory.decodeResource(res, R.drawable.notification_background)

        // Make rounded background image
        val dr = RoundedBitmapDrawableFactory.create(res, imgSrc)
        val cornerRadius = res.getDimensionPixelSize(R.dimen.corner_radius).toFloat()
        dr.cornerRadius = cornerRadius
        imgView.setImageDrawable(dr)

        // Set text
        customLayout.setTextViewText(R.id.notification_title, title)
        customLayout.setTextViewText(R.id.notification_body, body)

        return customLayout;
    }

    private fun createClickIntent(
        notificationId: Int,
        extraData: String?,
        activity: Activity
    ): PendingIntent {
        val it = Intent(applicationContext, getOpenAppActivity(applicationContext))
        it.putExtra(kNOTIFICATION_ID, notificationId)
        if (extraData != null) {
            it.putExtra(kNOTIFICATION_EXTRA_DATA, extraData)
        }
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        return PendingIntent.getActivity(
            applicationContext,
            0,
            it,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        );
    }

    // Returns Activity class to be opened when notification is tapped
    // Search is done in this order:
    //   * class specified in meta-data key custom_notification_android_activity
    //   * the only enabled activity with name ending in either .UnityPlayerActivity or .UnityPlayerGameActivity
    //   * the only enabled activity in the package
    private fun getOpenAppActivity(context: Context): Class<*>? {
        try {
            val pm = context.packageManager
            val ai = pm.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            val bundle = ai.metaData
            if (bundle.containsKey("custom_notification_android_activity")) {
                try {
                    return Class.forName(bundle.getString("custom_notification_android_activity"))
                } catch (e: ClassNotFoundException) {
                    Log.e(
                        TAG_UNITY,
                        "Specified activity class for notifications not found: " + e.message
                    )
                }
            }
            Log.w(
                TAG_UNITY,
                "No custom_notification_android_activity found, attempting to find app activity class"
            )
            val aInfo =
                pm.getPackageInfo(context.packageName, PackageManager.GET_ACTIVITIES).activities
            if (aInfo == null) {
                Log.e(TAG_UNITY, "Could not get package activities")
                return null
            }
            var activityClassName: String? = null
            var activityIsUnity = false
            var activityConflict = false
            for (info in aInfo) {
                // activity alias not supported
                if (!info.enabled || info.targetActivity != null) continue
                val candidateIsUnity: Boolean = isUnityActivity(info.name)
                if (activityClassName == null) {
                    activityClassName = info.name
                    activityIsUnity = candidateIsUnity
                    continue
                }

                // two Unity activities is a hard conflict
                // two non-Unity activities is a conflict unless we find a Unity activity later on
                if (activityIsUnity == candidateIsUnity) {
                    activityConflict = true
                    if (activityIsUnity && candidateIsUnity) break
                    continue
                }
                if (candidateIsUnity) {
                    activityClassName = info.name
                    activityIsUnity = candidateIsUnity
                    activityConflict = false
                }
            }
            if (activityConflict) {
                Log.e(
                    TAG_UNITY,
                    "Multiple choices for activity for notifications, set activity explicitly in Notification Settings"
                )
                return null
            }
            if (activityClassName == null) {
                Log.e(TAG_UNITY, "Activity class for notifications not found")
                return null
            }
            return Class.forName(activityClassName)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            Log.e(TAG_UNITY, "Failed to find activity class: " + e.message)
        }
        return null
    }

    private fun isUnityActivity(name: String): Boolean {
        return name.endsWith(".UnityPlayerActivity") || name.endsWith(".UnityPlayerGameActivity")
    }
}