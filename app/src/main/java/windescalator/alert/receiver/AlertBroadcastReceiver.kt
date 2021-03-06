package windescalator.alert.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import android.os.PowerManager.*
import android.util.Log
import windescalator.TAG
import windescalator.alert.AlertNotificationActivity
import windescalator.di.Injector

class AlertBroadcastReceiver: BroadcastReceiver() {

    private val WIND_ALERT_ACTION = "WIND_ALERT_ACTION"

    init {
        Injector.appComponent.inject(this)
    }


    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Got Broadcast")
        val action = intent.action
        val alertId = intent.getLongExtra("ALERT_ID", -1)

        if (action.equals(WIND_ALERT_ACTION)) {
            val activityIntent = Intent(context, AlertNotificationActivity::class.java)
            intent.putExtra("ALERT_ID", alertId)
            context.startActivity(activityIntent)
        }
    }

    fun getFilter(): IntentFilter {
        val filter = IntentFilter()
        filter.addAction(WIND_ALERT_ACTION)
        return filter
    }
}