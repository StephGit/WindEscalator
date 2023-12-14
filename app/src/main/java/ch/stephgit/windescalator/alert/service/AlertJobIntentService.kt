package ch.stephgit.windescalator.alert.service

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import ch.stephgit.windescalator.R
import ch.stephgit.windescalator.TAG
import ch.stephgit.windescalator.alert.receiver.AlertBroadcastReceiver
import ch.stephgit.windescalator.data.entity.Alert
import ch.stephgit.windescalator.data.repo.AlertRepo
import ch.stephgit.windescalator.di.Injector
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import javax.inject.Inject

class AlertJobIntentService : JobIntentService() {

    @Inject
    lateinit var alarmHandler: AlarmHandler

    @Inject
    lateinit var alertRepo: AlertRepo

    @Inject
    lateinit var alertReceiver: AlertBroadcastReceiver

    @Inject
    lateinit var windDataHandler: WindDataHandler

    private val fmt: DateTimeFormatter = DateTimeFormat.forPattern("HH:mm")
    private val jobId = 111


    init {
        Injector.appComponent.inject(this)

    }

    fun enqueueWork(context: Context, intent: Intent) {
        enqueueWork(context, AlertJobIntentService::class.java, jobId, intent)
    }

    override fun onHandleWork(intent: Intent) {
        val alertId = intent.getLongExtra("ALERT_ID", -1)
        if (alertId > -1L) {
            alertRepo.getAlert(alertId)?.let { handleAlert(it) }
        } else { Log.e(TAG, "UNEXPECTED ALARM WITHOUT ID") }
    }

    private fun handleAlert(alert: Alert) {
        // check if there is wind
        windDataHandler.isFiring(alert, ::sendAlertBroadcast, ::setNextInterval)
    }

    private fun setNextInterval(alert: Alert) {
        alarmHandler.setNextInterval(alert.id!!)
    }

    private fun sendAlertBroadcast(alertId: Long, windData: String) {

        registerReceiver(alertReceiver, alertReceiver.getFilter(), R.string.broadcast_permission.toString(), null )
        val intent = Intent(applicationContext, AlertBroadcastReceiver::class.java).apply {
            action = alertReceiver.getFilter().getAction(0)
            putExtra("ALERT_ID", alertId)
            putExtra("WIND_DATA", windData)

        }
        applicationContext.sendBroadcast(intent)
        unregisterReceiver(alertReceiver);
    }


}