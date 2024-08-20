package ch.stephgit.windescalator.alert

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.SharedPreferences
import android.os.Bundle
import android.os.PowerManager
import android.view.Window
import android.view.WindowInsets
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ContentInfoCompat.Flags
import androidx.preference.PreferenceManager
import ch.stephgit.windescalator.R
import ch.stephgit.windescalator.TAG
import ch.stephgit.windescalator.WindEscalatorActivity
import ch.stephgit.windescalator.alert.service.AlarmHandler
import ch.stephgit.windescalator.alert.service.NoiseHandler
import ch.stephgit.windescalator.data.FbAlert
import ch.stephgit.windescalator.data.entity.Alert
import ch.stephgit.windescalator.data.repo.AlertRepo
import ch.stephgit.windescalator.data.repo.AlertRepository
import ch.stephgit.windescalator.di.Injector
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
 Activity to show ongoing alert
 */

class AlertNotificationActivity : AppCompatActivity() {

    @Inject
    lateinit var noiseHandler: NoiseHandler

    @Inject
    lateinit var alarmHandler: AlarmHandler

    @Inject
    lateinit var alertRepo: AlertRepository

    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var alert: FbAlert
    private var nextInterval: Boolean = false

    init {
        Injector.appComponent.inject(this)
    }

    @SuppressLint("StringFormatMatches")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_alert_notification)
        this.window.insetsController?.hide(WindowInsets.Type.statusBars());

        // get settings for alerts from prefs
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.applicationContext)
        nextInterval =  sharedPreferences.getBoolean("cancel_firing_alert_behavior", false)

        val alertId = intent.getStringExtra("ALERT_ID")
        val windData = intent.getStringExtra("WIND_DATA")
        if (!alertId.isNullOrBlank()) {

            findViewById<FloatingActionButton>(R.id.btn_showWindData).setOnClickListener{
                showWindData()
            }

            findViewById<FloatingActionButton>(R.id.btn_stopAlert).setOnClickListener{
                stopAlert()
            }

            CoroutineScope(Dispatchers.IO).launch {
                alertRepo.get(alertId).collect { alert = it}
            }

            noiseHandler.makeNoise()

            findViewById<TextView>(R.id.tv_alertDetailText).text = applicationContext.getString(R.string.winddata_alertnotification, alert.resource, windData);

            wakeUp()

        } else this.onDestroy()


    }

    private fun wakeUp() {
        this.setShowWhenLocked(true)
        this.setTurnScreenOn(true)
        val power = this.getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = power.newWakeLock(PowerManager.FULL_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE, "$TAG:wakeup!")
        wakeLock.acquire(10000)
    }

    override fun onPause() {
        super.onPause()
        if (wakeLock.isHeld) wakeLock.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (wakeLock.isHeld) wakeLock.release()
    }

    private fun stopAlert() {
        noiseHandler.stopNoise()
        //FIXME
//        if (nextInterval) {
//            alarmHandler.setNextInterval(alert.id!!)
//        } else {
//            alarmHandler.removeAlarm(alert.id!!, true)
//        }
        finish()
    }

    private fun showWindData() {
        stopAlert()

        val activityIntent = Intent(applicationContext, WindEscalatorActivity::class.java)
        activityIntent.putExtra("ALERT_ID",  alert.id)
        activityIntent.flags = FLAG_ACTIVITY_NEW_TASK
        applicationContext.startActivity(activityIntent)
    }
}