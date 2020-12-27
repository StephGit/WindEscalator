package windescalator

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import ch.stephgit.windescalator.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import windescalator.di.Injector
import windescalator.alert.AlertFragment
import windescalator.alert.service.AlertService
import windescalator.alert.service.NoiseControl
import javax.inject.Inject


class WindEscalatorActivity : AppCompatActivity(), WindEscalatorNavigator {

    @Inject
    lateinit var noiseControl: NoiseControl

    private lateinit var navigation: BottomNavigationView
    private lateinit var prefs: SharedPreferences


    companion object {
        fun newIntent(ctx: Context) = Intent(ctx, WindEscalatorActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wind_escalator)
        navigation = findViewById(R.id.wind_escalator_navigation)
        navigation.setOnNavigationItemSelectedListener { clickedMenuItem -> selectMenuItem(clickedMenuItem) }
        Injector.appComponent.inject(this)
        prefs = getSharedPreferences("windescalator", Context.MODE_PRIVATE)

        val extras = intent.extras
        val alertResource = extras?.get("STOP_ALARM") as String?
        if (!alertResource.isNullOrEmpty()) {
            noiseControl.stopNoise()
            replaceFragment(WindFragment())
        } else {
            replaceFragment(AlertFragment())

            val alertServiceIntent = Intent(this, AlertService::class.java)
            startService(alertServiceIntent)
        }
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onStop() {
        super.onStop()
    }

    private fun selectMenuItem(clickedMenuItem: MenuItem): Boolean {
        Log.i(TAG, "selectMenuItem: ${clickedMenuItem.title}")

        when (clickedMenuItem.itemId) {
            R.id.bottom_navigation_alert -> replaceFragment(AlertFragment())
            R.id.bottom_navigation_messure -> replaceFragment(WindFragment())
            R.id.bottom_navigation_webcam -> replaceFragment(WebcamFragment())
            else -> throw IllegalArgumentException("Unknown clickedMenuItem.itemId: ${clickedMenuItem.itemId}")
        }
        return true
    }

    private fun replaceFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_content, fragment)
            .commit()
    }
}