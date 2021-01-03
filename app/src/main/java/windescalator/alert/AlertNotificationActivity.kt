package windescalator.alert

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.PowerManager
import android.view.Window
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
import ch.stephgit.windescalator.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import windescalator.TAG
import windescalator.alert.service.NoiseControl
import windescalator.di.Injector
import javax.inject.Inject

class AlertNotificationActivity : Activity() {

    @Inject
    lateinit var noiseControl: NoiseControl

    private lateinit var lock: PowerManager.WakeLock
    private lateinit var prefs: SharedPreferences
    init {
        Injector.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_alert_notification)

        // get settings for alerts from prefs
        prefs = getSharedPreferences("windescalator", Context.MODE_PRIVATE)

        val alertId = intent.getLongExtra("ALERT_ID", -1)
        if (alertId != -1L ) {
            noiseControl.makeNoise()

        }

        findViewById<FloatingActionButton>(R.id.btn_showWindData).setOnClickListener{
            showWindData()
        }

        findViewById<FloatingActionButton>(R.id.btn_stopAlert).setOnClickListener{
            stopAlert()
        }
        wakeUp()
    }

    private fun wakeUp() {

        this.window.addFlags(FLAG_SHOW_WHEN_LOCKED)
        val power = this.getSystemService(POWER_SERVICE) as PowerManager
        lock = power.newWakeLock(PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP
                or PowerManager.ON_AFTER_RELEASE, "$TAG:wakeup!"
        )
        lock.acquire(1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        lock.release()
    }

    private fun stopAlert() {
        noiseControl.stopNoise()
    }

    private fun showWindData() {
        stopAlert()
        // start MainActivity with extras for windfragment
    }


}