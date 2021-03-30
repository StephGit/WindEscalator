package ch.stephgit.windescalator.alert.service


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import ch.stephgit.windescalator.alert.receiver.AlarmBroadcastReceiver
import ch.stephgit.windescalator.data.entity.Alert
import ch.stephgit.windescalator.data.repo.AlertRepo
import ch.stephgit.windescalator.di.Injector
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject


class AlarmHandler @Inject constructor(
        val context: Context,
        val alertRepo: AlertRepo
) {

    private val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
    private val fmt: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private lateinit var alarmIntent: PendingIntent
    private val alarms: MutableMap<Long, Alert> = HashMap()
    private val interval = 5L // TODO make interval customizable


    init {
        Injector.appComponent.inject(this)
    }


    fun addOrUpdate(alert: Alert) {
        if (isExisting(alert)) {
            if (alarms[alert.id] != alert) {
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
        alarms[alert.id!!] = alert
        createAlarm(alert)
    }

    fun removeAlarm(alert: Alert) {
        if (!isExisting(alert)) return
        this.alarms.remove(alert.id)
        cancelAlarm(alert)
    }

    private fun createAlarm(alert: Alert) {

        val alarmTimeInMillis: Long = if (alert.pending) {
            // is endtime arrived
            if (alert.endTime!! <= LocalDateTime.now().format(fmt).toString()) {
                // set the alarm to next day starttime
                getMillis(LocalDateTime.parse(alert.startTime, fmt).plusDays(1))
            } else {
                // set to interval
                getMillis(LocalDateTime.now().plusMinutes(interval))
            }
        } else {
            // set to starttime
            getMillis(LocalDateTime.parse(alert.startTime, fmt))
        }

        alarmIntent = getPendingIntent(alert.id!!.toInt())

        alarmManager?.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                alarmTimeInMillis,
                0,
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

    private fun getMillis(time: LocalDateTime): Long {
        return time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }


    fun initAlarms() {
        val tmpList: MutableList<Alert> = ArrayList()
        tmpList.addAll(alertRepo.getActiveAlerts())
        tmpList.forEach {
            this.addAlarm(it)
        }
    }
}