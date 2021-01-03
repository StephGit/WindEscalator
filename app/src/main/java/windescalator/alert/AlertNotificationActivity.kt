package windescalator.alert

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ch.stephgit.windescalator.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import windescalator.WindFragment
import windescalator.alert.service.NoiseControl
import windescalator.di.Injector
import javax.inject.Inject

class AlertNotificationActivity : AppCompatActivity() {

    @Inject
    lateinit var noiseControl: NoiseControl

    private lateinit var prefs: SharedPreferences
    init {
        Injector.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert_notification)

        // get settings for alerts from prefs
        prefs = getSharedPreferences("windescalator", Context.MODE_PRIVATE)

        val alertId = intent.getLongExtra("ALERT_ID", -1)
        if (alertId != -1L ) {
//            noiseControl.makeNoise()

        }

        findViewById<FloatingActionButton>(R.id.btn_showWindData).setOnClickListener{
            showWindData()
        }

        findViewById<FloatingActionButton>(R.id.btn_stopAlert).setOnClickListener{
            stopAlert()
        }
    }

    private fun stopAlert() {
        noiseControl.stopNoise()
    }

    private fun showWindData() {

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_content, WindFragment())
                .commit()
    }


}