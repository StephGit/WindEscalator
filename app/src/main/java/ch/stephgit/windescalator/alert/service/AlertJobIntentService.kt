package ch.stephgit.windescalator.alert.service

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import ch.stephgit.windescalator.alert.receiver.AlertBroadcastReceiver
import ch.stephgit.windescalator.data.entity.Alert
import ch.stephgit.windescalator.data.repo.AlertRepo
import ch.stephgit.windescalator.di.Injector
import javax.inject.Inject

class AlertJobIntentService : JobIntentService() {

    @Inject
    lateinit var alarmHandler: AlarmHandler

    @Inject
    lateinit var alertRepo: AlertRepo

    @Inject
    lateinit var alertReceiver: AlertBroadcastReceiver

    @Inject
    lateinit var windDataAdapter: WindDataHandler

    private val fmt: DateTimeFormatter = DateTimeFormat.forPattern("HH:mm")
    private val jobId = 111
    private val interval = 5 // TODO make interval customizable

    init {
        Injector.appComponent.inject(this)
    }

    fun enqueueWork(context: Context, intent: Intent) {
        enqueueWork(context, AlertJobIntentService::class.java, jobId, intent)
    }

    override fun onHandleWork(intent: Intent) {
        val currentTimeString = LocalDateTime.now().toString(fmt)
        val alerts = alertRepo.getActiveAndInTimeAlerts(currentTimeString)
        if (!alerts.isNullOrEmpty()) { // maybe some ghost alarm?!
            alerts.forEach { alert ->
                if (windDataAdapter.isFiring(alert)) sendAlertBroadcast(alert.id!!)
            }
            // Trigger AlertJobIntentService in interval-time again
            alarmHandler.addOrUpdate(Alert("", true, "",
                    LocalDateTime.now().plusMinutes(interval).toString(fmt), "",
                    0, null, 666))
        }


    }

    private fun sendAlertBroadcast(alertId: Long) {
        registerReceiver(alertReceiver, alertReceiver.getFilter())
        val intent = Intent(applicationContext, AlertBroadcastReceiver::class.java).apply {
            action = alertReceiver.getFilter().getAction(0)
            putExtra("ALERT_ID", alertId)
        }
        applicationContext.sendBroadcast(intent)
    }


}