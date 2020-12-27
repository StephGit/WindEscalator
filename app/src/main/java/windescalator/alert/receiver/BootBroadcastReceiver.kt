package windescalator.alert.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import windescalator.alert.service.AlertService

class BootBroadcastReceiver : BroadcastReceiver() {

    /**
     * Unsafe action-filtering happens in async-task
     */
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        val alarmServiceIntent = Intent(context, AlertService::class.java)
        context.startService(alarmServiceIntent)
    }

}