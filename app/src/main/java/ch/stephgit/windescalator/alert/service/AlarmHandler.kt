package ch.stephgit.windescalator.alert.service


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import ch.stephgit.windescalator.TAG
import ch.stephgit.windescalator.alert.receiver.AlarmBroadcastReceiver
import ch.stephgit.windescalator.data.entity.Alert
import ch.stephgit.windescalator.data.repo.AlertRepo
import ch.stephgit.windescalator.di.Injector
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/*
 Handles continues trigger to alertJobIntentService
 Naming is really confusing *rofl*, but in android scheduling work handler is called AlarmManager
 */
class AlarmHandler @Inject constructor(
        val context: Context,
        val alertRepo: AlertRepo
) {

    private val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
    private val fmt: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private lateinit var alarmIntent: PendingIntent
    private val alarms: MutableMap<Long, Alert> = HashMap() // Contains Map of active alarms
    private val interval = 10L // TODO make interval customizable
    private val alarmRequestCode = 42;


    init {
        Injector.appComponent.inject(this)
    }


    fun addOrUpdate(alert: Alert) {
        if (isExisting(alert)) {
            if (alarms[alert.id] != alert) {
                removeAlarm(alert, false)
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
        if (alert.active) {
            alarms[alert.id!!] = alert
            setAlarm(alert, false)
        }
    }

    fun removeAlarm(alert: Alert, forToday: Boolean) {
        if (forToday) {
            setAlarm(alert, true)
        } else {
            if (!isExisting(alert)) return
            this.alarms.remove(alert.id)
            cancelAlarm(alert)
        }
    }

    /**
     * Sets alarm in AlarmManager
     * With `nextDay`=true the calculation for the next possible alarm is skipped and the alarm is
     * set to it's start time on the next day.
     */
    private fun setAlarm(alert: Alert, nextDay: Boolean) {
        val alarmTimeInMillis: Long =
                // is endtime arrived
                if (nextDay) setNextDayAlarm(alert) else calculateNextAlarm(alert, alert.pending)


        alarmIntent = getPendingIntent(alert)
        Log.i(TAG, "Set Alarm for alert: $alert.name" )
        alarmManager?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTimeInMillis, alarmIntent)

    }

    private fun calculateNextAlarm(alert: Alert, pendingAlert: Boolean): Long {
        return when {
            alert.endTime!! <= LocalDateTime.now().format(fmt).toString() -> {
                setNextDayAlarm(alert)
            }
            pendingAlert -> {
                // set to interval
                getMillis(LocalDateTime.now().plusMinutes(interval))
            }
            else -> {
                // set to starttime
                alert.pending = true
                alertRepo.update(alert)
                getMillis(LocalDateTime.of(LocalDate.now(), LocalTime.parse(alert.startTime, fmt)));
            }
        }
    }

    private fun setNextDayAlarm(alert: Alert) =
        // set the alarm to next day starttime
        getMillis(
            LocalDateTime.of(LocalDate.now(), LocalTime.parse(alert.startTime, fmt)).plusDays(1)
        )

    private fun cancelAlarm(alert: Alert) {
        Log.i(TAG, "Cancel alarm: $alert")
        alarmManager?.cancel(getPendingIntent(alert))
        alert.pending = false;
        alertRepo.update(alert)
    }

    private fun getPendingIntent(alert: Alert): PendingIntent {
        return Intent(context, AlarmBroadcastReceiver::class.java).let { intent ->
            intent.putExtra("ALERT_ID", alert.id)
            PendingIntent.getBroadcast(context, this.alarmRequestCode + alert.id!!.toInt(), intent, PendingIntent.FLAG_IMMUTABLE)
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

    fun setNextInterval(alert: Alert) {
        setAlarm(alert, false);
    }
}
