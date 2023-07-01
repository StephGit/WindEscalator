package ch.stephgit.windescalator.alert.receiver

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
        var alertId = intent.getLongExtra("ALERT_ID", -1)
        var windData = intent.getStringExtra("WIND_DATA")

        if (action.equals(WIND_ALERT_ACTION)) {
            val activityIntent = Intent(context, AlertNotificationActivity::class.java)
            activityIntent.putExtra("ALERT_ID",  alertId)
            activityIntent.putExtra("WIND_DATA", windData)
            context.startActivity(activityIntent)
        }
    }

    fun getFilter(): IntentFilter {
        val filter = IntentFilter()
        filter.addAction(WIND_ALERT_ACTION)
        return filter
    }
}