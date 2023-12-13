package ch.stephgit.windescalator

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import ch.stephgit.windescalator.alert.AlertFragment
import ch.stephgit.windescalator.di.Injector
import ch.stephgit.windescalator.log.LogCatViewModel
import ch.stephgit.windescalator.log.LogFragment
import ch.stephgit.windescalator.wind.WindFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import javax.inject.Inject


class WindEscalatorActivity : AppCompatActivity(), WindEscalatorNavigator {

    private lateinit var navigation: BottomNavigationView

    private lateinit var viewModel: LogCatViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    companion object {
        fun newIntent(ctx: Context) = Intent(ctx, WindEscalatorActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wind_escalator)
        navigation = findViewById(R.id.wind_escalator_navigation)
        navigation.setOnItemSelectedListener { clickedMenuItem -> selectMenuItem(clickedMenuItem) }
        Injector.appComponent.inject(this)

        viewModel = ViewModelProvider(this, viewModelFactory)[LogCatViewModel::class.java]

        replaceFragment(AlertFragment())
    }

    private fun selectMenuItem(clickedMenuItem: MenuItem): Boolean {
        Log.i(TAG, "selectMenuItem: ${clickedMenuItem.title}")

        when (clickedMenuItem.itemId) {
            R.id.bottom_navigation_alert -> replaceFragment(AlertFragment())
            R.id.bottom_navigation_messure -> replaceFragment(WindFragment())
            R.id.bottom_navigation_webcam -> replaceFragment(WebcamFragment())
            R.id.bottom_navigation_log -> replaceFragment(LogFragment())
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