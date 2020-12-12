package windescalator.alert

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.getSystemService
import windescalator.R
import windescalator.alert.receiver.AlarmBroadcastReceiver
import windescalator.data.entity.Alert
import windescalator.data.repo.AlertRepo
import javax.inject.Inject

class AlertService @Inject constructor(
        private val context: Context,
        private val alertRepo: AlertRepo
) {

    private val alerts: MutableMap<Long, String> = HashMap()
    private val alarmManager: AlarmManager = getSystemService(context, AlarmManager::class.java) as AlarmManager
    private var localAlertPendingIntent: PendingIntent? = null

    private fun add(alert: Alert) {
        TODO("Not yet implemented")
    }

    fun addOrUpdate(alert: Alert) {
//        val ALARM_DELAY_IN_SECONDS = 10
//        val alarmTimeUTC = System.currentTimeMillis() + ALARM_DELAY_IN_SECONDS * 1_000L
//        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTimeUTC, alertPendingIntent)
    }

    fun remove(alert: Alert) {
        TODO("Not yet implemented")
    }

    fun initAlerts() {
        TODO("Not yet implemented")
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
//        localAlertPendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//        return@lazy localAlertPendingIntent
//    }
}