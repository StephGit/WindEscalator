package ch.stephgit.windescalator

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import ch.stephgit.windescalator.alert.AlertFragment
import ch.stephgit.windescalator.di.Injector
import ch.stephgit.windescalator.log.LogCatViewModel
import ch.stephgit.windescalator.log.LogFragment
import ch.stephgit.windescalator.wind.WindFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import javax.inject.Inject


class WindEscalatorActivity : AppCompatActivity(), WindEscalatorNavigator {

    private lateinit var navigation: BottomNavigationView

    private lateinit var viewModel: LogCatViewModel

    private lateinit var menu: Menu

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    companion object {
        fun newIntent(ctx: Context) = Intent(ctx, WindEscalatorActivity::class.java)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wind_escalator)
        setSupportActionBar(findViewById(R.id.toolbar))
        navigation = findViewById(R.id.navigation)
        navigation.setOnItemSelectedListener { selectedNavItem -> selectNavItem(selectedNavItem) }

        Injector.appComponent.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[LogCatViewModel::class.java]

        Firebase.messaging.token.addOnCompleteListener(
            OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result

                // Log and toast

                Log.d(TAG, token)
                Toast.makeText(baseContext, token, Toast.LENGTH_SHORT).show()
            },
        )
        replaceFragment(AlertFragment())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        this.menu = menu
        inflater.inflate(R.menu.menu_settings, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection.
        return when (item.itemId) {
            R.id.menu_settings -> {
                replaceFragment(SettingsFragment())
                hideMainNavAndMenu(item)
                true
            }
            R.id.menu_back -> {
                replaceFragment(AlertFragment())
                showMainNavAndMenu(item)
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun hideMainNavAndMenu(item: MenuItem) {
        navigation.visibility = INVISIBLE
        item.isVisible = false
        menu.getItem(1).isVisible = true
    }

    private fun showMainNavAndMenu(item: MenuItem) {
        navigation.visibility = VISIBLE
        item.isVisible = false
        menu.getItem(0).isVisible = true
    }


    private fun selectNavItem(selectedNavItem: MenuItem): Boolean {
        Log.i(TAG, "selectMenuItem: ${selectedNavItem.title}")

        when (selectedNavItem.itemId) {
            R.id.bottom_navigation_alert -> replaceFragment(AlertFragment())
            R.id.bottom_navigation_messure -> replaceFragment(WindFragment())
            R.id.bottom_navigation_webcam -> replaceFragment(WebcamFragment())
            R.id.bottom_navigation_log -> replaceFragment(LogFragment())
            else -> throw IllegalArgumentException("Unknown clickedMenuItem.itemId: ${selectedNavItem.itemId}")
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