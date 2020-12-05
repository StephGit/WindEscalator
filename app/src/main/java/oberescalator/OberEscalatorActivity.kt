package oberescalator

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import oberescalator.di.Injector
import oberescalator.alert.AlertFragment


class OberEscalatorActivity : AppCompatActivity() {

    private lateinit var navigation: BottomNavigationView
    private lateinit var prefs: SharedPreferences


    companion object {
        fun newIntent(ctx: Context) = Intent(ctx, OberEscalatorActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ober_escalator)
        navigation = findViewById(R.id.ober_escalator_navigation)
        navigation.setOnNavigationItemSelectedListener { clickedMenuItem -> selectMenuItem(clickedMenuItem) }
        Injector.appComponent.inject(this)
        prefs = getSharedPreferences("oberescalator", Context.MODE_PRIVATE)

        if (savedInstanceState == null) {
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
            R.id.bottom_navigation_messure -> replaceFragment(MessureFragment())
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