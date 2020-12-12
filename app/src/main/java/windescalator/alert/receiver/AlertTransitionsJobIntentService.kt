package windescalator.alert.receiver

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import windescalator.di.Injector

class AlertTransitionsJobIntentService : JobIntentService() {

    private val jobId = 654

    init {
        Injector.appComponent.inject(this)
    }

    fun enqueueWork(context: Context, intent: Intent) {
        enqueueWork(context, AlertTransitionsJobIntentService::class.java, jobId, intent)
    }

    override fun onHandleWork(intent: Intent) {
        TODO("Not yet implemented")
        // get event
        // check event for errors
        // handle Event
    }
}