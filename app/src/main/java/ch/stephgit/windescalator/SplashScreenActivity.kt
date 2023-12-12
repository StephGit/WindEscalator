package ch.stephgit.windescalator

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity


@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen)
        this.window.insetsController?.hide(WindowInsets.Type.statusBars());
        startActivity(Intent(applicationContext,WindEscalatorActivity::class.java))
        finish()
    }
}