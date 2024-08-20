package ch.stephgit.windescalator.alert.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import ch.stephgit.windescalator.TAG
import ch.stephgit.windescalator.alert.AlertNotificationActivity
import ch.stephgit.windescalator.di.Injector

class AlertBroadcastReceiver: BroadcastReceiver() {

    private val WIND_ALERT_ACTION = "WIND_ALERT_ACTION"

    init {
        Injector.appComponent.inject(this)
    }


    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Got Broadcast")
        val action = intent.action
        var alertId = intent.getStringExtra("ALERT_ID")
        var windData = intent.getStringExtra("WIND_DATA")

        if (action.equals(WIND_ALERT_ACTION)) {
            val activityIntent = Intent(context, AlertNotificationActivity::class.java)
            activityIntent.putExtra("ALERT_ID", alertId)
            activityIntent.putExtra("WIND_DATA", windData)
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            val pendingIntent = PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE)
            pendingIntent.send()
        }
    }

    fun getFilter(): IntentFilter {
        val filter = IntentFilter()
        filter.addAction(WIND_ALERT_ACTION)
        return filter
    }
}