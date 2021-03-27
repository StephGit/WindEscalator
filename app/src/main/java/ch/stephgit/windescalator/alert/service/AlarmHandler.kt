package ch.stephgit.windescalator.alert.service


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import ch.stephgit.windescalator.alert.receiver.AlarmBroadcastReceiver
import ch.stephgit.windescalator.data.entity.Alert
import ch.stephgit.windescalator.data.repo.AlertRepo
import ch.stephgit.windescalator.di.Injector
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject


// Alarms to trigger AlertService
class AlarmHandler @Inject constructor(
        val context: Context,
        val alertRepo: AlertRepo
        ) {

    private val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
    private lateinit var alarmIntent: PendingIntent
    private val alarms: MutableMap<Long, String> = HashMap()


    init {
        Injector.appComponent.inject(this)
    }

    fun addOrUpdate(alert: Alert) {
        if (isExisting(alert)) {
            if (alarms[alert.id] != alert.startTime) {
                removeAlarm(alert)
                addAlarm(alert)
            }
        } else {
            addAlarm(alert)
        }
    }

    private fun isExisting(alert: Alert): Boolean {
        return (this.alarms.containsKey(alert.id))
    }

    private fun addAlarm(alert: Alert) {
        alarms[alert.id!!] = alert.startTime!!
        createAlarm(alert)
    }

    fun removeAlarm(alert: Alert) {
        if (!isExisting(alert)) return
        this.alarms.remove(alert.id)
        cancelAlarm(alert)
    }

    private fun createAlarm(alert: Alert) {

        val fmt: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        // Set the alarm to start at alert-time-window
        val timeInMillis = LocalTime.parse(alert.startTime, fmt).atDate(LocalDate.now())
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        alarmIntent = getPendingIntent(alert.id!!.toInt())

        alarmManager?.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                AlarmManager.INTERVAL_DAY,
                alarmIntent
        )
    }

    private fun cancelAlarm(alert: Alert) {
        alarmManager?.cancel(getPendingIntent(alert.id!!.toInt()))
    }

    private fun getPendingIntent(alertId: Int): PendingIntent {
        return Intent(context, AlarmBroadcastReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, alertId, intent, 0)
        }
    }


    fun initAlarms() {
        val tmpList: MutableList<Alert> = ArrayList()
        tmpList.addAll(alertRepo.getActiveAlerts())
        tmpList.forEach {
            this.addAlarm(it)
        }
    }
}