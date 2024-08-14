package ch.stephgit.windescalator.alert.service

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.Worker
import androidx.work.WorkerParameters
import ch.stephgit.windescalator.alert.receiver.AlertBroadcastReceiver


class WakeUpWorker(context: Context, params: WorkerParameters) : Worker(context, params) {


    @SuppressLint("ServiceCast")
    override fun doWork(): Result {
        val alertId = inputData.getString("ALERT_ID")
        val windData = inputData.getString("WIND_DATA")


        // Get the AlarmManager instance
        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // Create an intent to trigger the wake-up action
        val intent = Intent(applicationContext, AlertBroadcastReceiver::class.java).apply {
            action =  "WIND_ALERT_ACTION"
            putExtra("ALERT_ID", alertId)
            putExtra("WIND_DATA", windData)

        }

        val pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Set the alarm to trigger immediately
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent)
        }

        // Return success
        return Result.success()
    }
}