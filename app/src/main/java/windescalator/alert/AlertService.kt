package windescalator.alert

import android.content.Context
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import windescalator.data.entity.Alert
import windescalator.data.repo.AlertRepo
import javax.inject.Inject

class AlertService @Inject constructor(
        private val context: Context,
        private val alertRepo: AlertRepo
) : OnCompleteListener<Void> {


    private fun add(alert: Alert) {
        TODO("Not yet implemented")
    }

    fun addOrUpdate(alert: Alert) {
        TODO("Not yet implemented")
    }

    fun remove(alert: Alert) {
        TODO("Not yet implemented")
    }

    override fun onComplete(p0: Task<Void>) {
        TODO("Not yet implemented")
    }
}