package ch.stephgit.windescalator.alert

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.PowerManager
import android.view.Window
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
import androidx.appcompat.app.AppCompatActivity
import ch.stephgit.windescalator.R
import ch.stephgit.windescalator.TAG
import ch.stephgit.windescalator.WindEscalatorActivity
import ch.stephgit.windescalator.alert.service.AlarmHandler
import ch.stephgit.windescalator.alert.service.NoiseHandler
import ch.stephgit.windescalator.data.entity.Alert
import ch.stephgit.windescalator.data.repo.AlertRepo
import ch.stephgit.windescalator.di.Injector
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
    lateinit var alertRepo: AlertRepo

    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var prefs: SharedPreferences
    private lateinit var alert: Alert

    init {
        Injector.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
//        window.insetsController?.hide(WindowInsets.Type.statusBars());
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_alert_notification)

        // get settings for alerts from prefs
        prefs = getSharedPreferences("windescalator", Context.MODE_PRIVATE)

        val alertId = intent.getLongExtra("ALERT_ID", -1)
        if (alertId != -1L ) {

            findViewById<FloatingActionButton>(R.id.btn_showWindData).setOnClickListener{
                showWindData()
            }

            findViewById<FloatingActionButton>(R.id.btn_stopAlert).setOnClickListener{
                stopAlert()
            }

            alert = alertRepo.getAlert(alertId)!!
            noiseHandler.makeNoise()

            wakeUp()

        } else this.onDestroy()


    }

    private fun wakeUp() {
        this.window.addFlags(FLAG_SHOW_WHEN_LOCKED)
        val power = this.getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = power.newWakeLock(PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP
                or PowerManager.ON_AFTER_RELEASE, "$TAG:wakeup!"
        )
        wakeLock.acquire(10000)
    }

    override fun onPause() {
        super.onPause()
        if (wakeLock.isHeld) wakeLock.release()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun stopAlert() {
        noiseHandler.stopNoise()
        alarmHandler.removeAlarm(alert)
        finish()
    }

    private fun showWindData() {
        stopAlert()
        val activityIntent = Intent(applicationContext, WindEscalatorActivity::class.java)
        activityIntent.putExtra("ALERT_ID",  alert.id)
        applicationContext.startActivity(activityIntent)
    }
}