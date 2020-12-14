package windescalator.alert

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.core.content.ContextCompat.getSystemService
import ch.stephgit.windescalator.R
import windescalator.alert.receiver.AlarmBroadcastReceiver
import windescalator.alert.receiver.WindDataJobIntentService
import windescalator.data.entity.Alert
import windescalator.data.repo.AlertRepo
import javax.inject.Inject

class AlertService @Inject constructor(
        private val context: Context,
        private val alertRepo: AlertRepo
) {

    private val alerts: MutableMap<Long, String> = HashMap()
    private lateinit var alertPendingIntent: PendingIntent
    private val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun add(alert: Alert) {
        if (isAlreadyAdded(alert)) return

        setAlarmReceiver(alert)
    }

    private fun setAlarmReceiver(alert: Alert) {
        alertPendingIntent = Intent(context, AlarmBroadcastReceiver::class.java).let {
            PendingIntent.getBroadcast(context, 0, it, 0)
        }
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000*60, 1000*60, alertPendingIntent)
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