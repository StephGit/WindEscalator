package ch.stephgit.windescalator.alert.service

import android.content.Intent
import android.util.Log
import ch.stephgit.windescalator.R
import ch.stephgit.windescalator.TAG
import ch.stephgit.windescalator.alert.AlertNotificationActivity
import ch.stephgit.windescalator.alert.receiver.AlertBroadcastReceiver
import ch.stephgit.windescalator.di.Injector
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import javax.inject.Inject

class FirebaseForgroundMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var alertReceiver: AlertBroadcastReceiver

    init {
        Injector.appComponent.inject(this)
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")


        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")


        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")

            registerReceiver(alertReceiver, alertReceiver.getFilter(), R.string.broadcast_permission.toString(), null )
            val intent = Intent(applicationContext, AlertBroadcastReceiver::class.java).apply {
                action = alertReceiver.getFilter().getAction(0)
                putExtra("ALERT_ID", 25L)
                putExtra("WIND_DATA", "windData")

            }
            applicationContext.sendBroadcast(intent)
            unregisterReceiver(alertReceiver);
        }
    }



}