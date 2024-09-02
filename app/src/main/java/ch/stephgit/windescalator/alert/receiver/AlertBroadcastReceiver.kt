package ch.stephgit.windescalator.alert.receiver

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ch.stephgit.windescalator.R
import ch.stephgit.windescalator.TAG
import ch.stephgit.windescalator.alert.AlertNotificationActivity
import ch.stephgit.windescalator.di.Injector

class AlertBroadcastReceiver : BroadcastReceiver() {

    private val WIND_ALERT_ACTION = "WIND_ALERT_ACTION"

    init {
        Injector.appComponent.inject(this)
    }


    @SuppressLint("ObsoleteSdkInt")
    @RequiresApi(VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Got Broadcast")
        val action = intent.action
        var alertId = intent.getStringExtra("ALERT_ID")
        var windData = intent.getStringExtra("WIND_DATA")

        if (action.equals(WIND_ALERT_ACTION)) {
            val activityIntent = Intent(context, AlertNotificationActivity::class.java)
            activityIntent.putExtra("ALERT_ID", alertId)
            activityIntent.putExtra("WIND_DATA", windData)
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_FROM_BACKGROUND or Intent.FLAG_ACTIVITY_CLEAR_TASK)

            if (VERSION.SDK_INT > VERSION_CODES.TIRAMISU) {
                val activityOptions = ActivityOptions.makeBasic()
                    .setPendingIntentCreatorBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    activityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT,
                    activityOptions.toBundle()
                )
                pendingIntent.send()
            } else {
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    activityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )


                val notificationBuilder = NotificationCompat.Builder(
                    context,
                    context.getString(R.string.default_notification_channel_id)
                )
                    .setSmallIcon(R.drawable.ic_windbag_24)
                    .setContentTitle("Wind Alert")
                    .setContentText("Wind conditions have changed.")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)

                // Send the notification
                with(NotificationManagerCompat.from(context)) {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.d(TAG, "Missing Permission")
                        return
                    }
                    notify(1, notificationBuilder.build())
                }
            }
        }
    }

    fun getFilter(): IntentFilter {
        val filter = IntentFilter()
        filter.addAction(WIND_ALERT_ACTION)
        return filter
    }
}