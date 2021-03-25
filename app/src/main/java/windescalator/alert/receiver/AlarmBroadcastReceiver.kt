package windescalator.alert.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import windescalator.TAG
import windescalator.alert.service.AlertService
import windescalator.di.Injector

class AlarmBroadcastReceiver: BroadcastReceiver() {

    init {
        Injector.appComponent.inject(this)
    }


    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Got AlarmBroadcast")

        val alertServiceIntent = Intent(context, AlertService::class.java)
        context.startForegroundService(alertServiceIntent)

    }
}