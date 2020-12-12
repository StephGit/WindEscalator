package windescalator.alert.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import windescalator.TAG
import windescalator.alert.AlertService
import windescalator.di.Injector
import javax.inject.Inject

class BootBroadcastReceiver : BroadcastReceiver() {

    /**
     * Unsafe action-filtering happens in async-task
     */
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "handling BOOT_COMPLETED")
        val pendingResult: PendingResult = goAsync()
        val asyncTask = BootReceiverTask(pendingResult, intent)
        asyncTask.execute()
    }

    // FIXME https://www.techyourchance.com/asynctask-deprecated/
    class BootReceiverTask internal constructor(private val pendingResult: PendingResult, private val intent: Intent) :
            AsyncTask<Void, Void, Void>() {

        @Inject
        lateinit var alertService: AlertService

        init {
            Injector.appComponent.inject(this)
        }

        override fun doInBackground(vararg params: Void?): Void? {
            if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
                // alerts are removed on reboot
                alertService.initAlerts()
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            // Must call finish() so the BroadcastReceiver can be recycled.
            pendingResult.finish()
        }

    }
}