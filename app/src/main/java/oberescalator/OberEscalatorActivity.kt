package oberescalator

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView


class OberEscalatorActivity : AppCompatActivity() {

    private lateinit var navigation: BottomNavigationView
    private lateinit var prefs: SharedPreferences


    companion object {
        fun newIntent(ctx: Context) = Intent(ctx, OberEscalatorActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ober_escalator)

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


    private fun replaceFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_content, fragment)
            .commit()
    }
}