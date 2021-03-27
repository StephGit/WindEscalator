package ch.stephgit.windescalator.alert.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import ch.stephgit.windescalator.TAG
import ch.stephgit.windescalator.alert.service.AlertJobIntentService

class AlarmBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Got AlarmBroadcast")

        val alertJobIntentService = AlertJobIntentService()
        alertJobIntentService.enqueueWork(context, intent)
    }
}