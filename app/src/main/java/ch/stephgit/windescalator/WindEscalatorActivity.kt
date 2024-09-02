package ch.stephgit.windescalator

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import ch.stephgit.windescalator.alert.AlertFragment
import ch.stephgit.windescalator.alert.service.AlertMessagingService
import ch.stephgit.windescalator.di.Injector
import ch.stephgit.windescalator.log.LogCatViewModel
import ch.stephgit.windescalator.log.LogFragment
import ch.stephgit.windescalator.wind.WindFragment
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.initialize
import com.google.firebase.messaging.messaging
import javax.inject.Inject


class WindEscalatorActivity : AppCompatActivity(), WindEscalatorNavigator {


    private lateinit var navigation: BottomNavigationView

    private lateinit var viewModel: LogCatViewModel

    private lateinit var menu: Menu

    @Inject
    lateinit var db: FirebaseFirestore

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var firebaseForgroundMessagingService: AlertMessagingService

    companion object {
        fun newIntent(ctx: Context) = Intent(ctx, WindEscalatorActivity::class.java)
        private const val REQUEST_CODE_POST_NOTIFICATION = 666
    }

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }

    private fun createSignInIntent() {

        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            Log.d(TAG, user.toString())
        } else {
            Log.d(TAG, response?.error.toString())
            Toast.makeText(baseContext, getString(R.string.signInError), Toast.LENGTH_LONG).show()
            createSignInIntent()
        }
    }
     private fun signOut() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                // ...
            }
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

        requestAppPermissions()

        //Firebase Stuff
        initFirebase()
        replaceFragment(AlertFragment())
    }

    private fun requestAppPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this.baseContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            when {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) -> {
                    showPermissionDeniedDialog(
                        Manifest.permission.POST_NOTIFICATIONS,
                        REQUEST_CODE_POST_NOTIFICATION
                    )
                }

            }
        }
    }

    private fun showPermissionDeniedDialog(permissions: String, permissionRequestCode: Int) {
        AlertDialog.Builder(this).apply {
            setCancelable(true)
            setMessage(getString(R.string.permission_post_notification_required))
            setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                ActivityCompat.requestPermissions(this@WindEscalatorActivity, arrayOf(permissions), permissionRequestCode)
            }
        }.show()
    }


    private fun initFirebase() {
        Firebase.initialize(context = this)
        // TODO link app after release with firebase
        // https://firebase.google.com/docs/app-check/android/play-integrity-provider#project-setup
        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        createSignInIntent()

        Firebase.messaging.token.addOnCompleteListener(
            OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Store new FCM registration token
               firebaseForgroundMessagingService.onNewToken(task.result)
            },
        )
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
