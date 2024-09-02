package ch.stephgit.windescalator.alert

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import ch.stephgit.windescalator.R
import ch.stephgit.windescalator.TAG
import ch.stephgit.windescalator.WindEscalatorActivity
import ch.stephgit.windescalator.alert.service.NoiseHandler
import ch.stephgit.windescalator.data.FbAlert
import ch.stephgit.windescalator.data.repo.AlertRepository
import ch.stephgit.windescalator.di.Injector
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
 Activity to show ongoing alert
 */

class AlertNotificationActivity : AppCompatActivity() {

    @Inject
    lateinit var noiseHandler: NoiseHandler

    @Inject
    lateinit var alertRepo: AlertRepository

    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var alert: FbAlert
    private var alertId: String? = null
    private var windData: String? = null
    private var nextInterval: Boolean = false

    init {
        Injector.appComponent.inject(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "new intent log")

        alertId = intent!!.getStringExtra("ALERT_ID")
        windData = intent!!.getStringExtra("WIND_DATA")


    }

    @SuppressLint("StringFormatMatches")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wakeUp()

        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_alert_notification)
        this.window.insetsController?.hide(WindowInsets.Type.statusBars());

        // get settings for alerts from prefs
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.applicationContext)
        nextInterval =  sharedPreferences.getBoolean("cancel_firing_alert_behavior", false)
        alertId = intent!!.getStringExtra("ALERT_ID")
        windData = intent!!.getStringExtra("WIND_DATA")

        if (!alertId.isNullOrBlank()) {

            findViewById<FloatingActionButton>(R.id.btn_showWindData).setOnClickListener{
                showWindData()
            }

            findViewById<FloatingActionButton>(R.id.btn_stopAlert).setOnClickListener{
                stopAlert()
            }

            CoroutineScope(Dispatchers.IO).launch {
                alertRepo.get(alertId!!).collect {

                    alert = it
                    runOnUiThread {
                        findViewById<TextView>(R.id.tv_alertDetailText).text = applicationContext.getString(R.string.winddata_alertnotification, alert.resource, windData);
                    }
                }
//                noiseHandler.makeNoise()
            }

        } else super.onDestroy()


    }

    private fun wakeUp() {
        this.setShowWhenLocked(true)
        this.setTurnScreenOn(true)
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "$TAG:wakeup!")
        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        wakeLock.acquire(10000)
    }

    override fun onPause() {
        super.onPause()
//        if (wakeLock.isHeld) wakeLock.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (wakeLock.isHeld) wakeLock.release()
    }

    private fun stopAlert() {
//        noiseHandler.stopNoise()
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