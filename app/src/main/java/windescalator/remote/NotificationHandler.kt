package windescalator.remote

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import ch.stephgit.windescalator.BuildConfig
import ch.stephgit.windescalator.R
import windescalator.WindEscalatorActivity

class NotificationHandler(val context: Context) {

    private val notificationChannelId = BuildConfig.APPLICATION_ID + ".channel"
    private val notificationId = 789556

    fun createAlarmNotification() {

        createNotificationChannel(notificationChannelId)
        val builder = NotificationCompat.Builder(context, notificationChannelId)
            .setSmallIcon(R.drawable.ic_windbag_black_24)
            .setContentTitle(context.getString(R.string.alert_notification_title))
            .setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(R.string.alarm_notification_text)))
            .setVibrate(longArrayOf(500, 1000, 500, 1000))
            .setAutoCancel(true)

        val targetIntent = Intent(context, WindEscalatorActivity::class.java)
        val contentIntent = PendingIntent.getActivity(context, 0, targetIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        builder.setContentIntent(contentIntent)
        val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nManager.notify(notificationId, builder.build())
    }

    private fun createNotificationChannel(name: String) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val descriptionText = context.getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(notificationChannelId, name, importance).apply {
                description = descriptionText
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            // Register the channel with the system
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}