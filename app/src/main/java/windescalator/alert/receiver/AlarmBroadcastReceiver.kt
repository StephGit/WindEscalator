package windescalator.alert.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ch.stephgit.windescalator.R

/**
 * Receives alarm broadcast with an Intent and the transition type
 * Creates a JobIntentService to handle the intent in the background
 */
class AlarmBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == context.getString(R.string.wind_alert_action)) {
            val service = WindDataJobIntentService()
            service.enqueueWork(context, intent)
        }
    }
}