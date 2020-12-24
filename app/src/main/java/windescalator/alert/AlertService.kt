package windescalator.alert

import android.content.Context
import windescalator.data.entity.Alert
import windescalator.data.repo.AlertRepo
import javax.inject.Inject

class AlertService @Inject constructor(
        private val context: Context,
        private val alertRepo: AlertRepo
) {

    private val alerts: MutableMap<Long, String> = HashMap()

    private fun add(alert: Alert) {
        if (isAlreadyAdded(alert)) return

    }

    private fun isAlreadyAdded(alert: Alert): Boolean {
        return (this.alerts.containsKey(alert.id))
    }

    fun addOrUpdate(alert: Alert) {
        add(alert)
    }

    fun remove(alert: Alert) {

    }

    fun initAlerts(): Boolean {
        val tmpList: MutableList<Alert> = ArrayList()
        tmpList.addAll(alertRepo.getActiveAlerts())
        tmpList.forEach {
            add(it)
        }
        return (alerts.isNotEmpty())
    }

    /**
     * Returns a PendingIntent with the request to trigger AlertBroadcastReceiver.
     * Location Service addresses the intent inside this PendingIntent.
     * With usage of FLAG_UPDATE_CURRENT we reuse the same pending intent when calling this function
     */
//    private val alertPendingIntent: PendingIntent? by lazy {
//        if (alertPendingIntent != null) return@lazy localAlertPendingIntent
//
//        val intent = Intent(context, AlarmBroadcastReceiver::class.java)
//        intent.action = context.getString(R.string.wind_alert_action)
//        localAlertPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
//        return@lazy localAlertPendingIntent
//    }
}