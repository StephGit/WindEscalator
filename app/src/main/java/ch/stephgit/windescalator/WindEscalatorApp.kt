package ch.stephgit.windescalator

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import ch.stephgit.windescalator.di.Injector

val Any.TAG: String
    get() {
        return "WindEscalator"
    }
class WindEscalatorApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Injector.init(this)

        // Create the Notification Channel (if needed)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val descriptionText = getString(R.string.channel_description)
            val channel = NotificationChannel(
                getString(R.string.default_notification_channel_id),
                getString(R.string.default_notification_channel_id),
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = descriptionText
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}