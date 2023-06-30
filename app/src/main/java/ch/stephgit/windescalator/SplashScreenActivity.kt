package ch.stephgit.windescalator

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity


@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        window.insetsController?.hide(WindowInsets.Type.statusBars());
        setContentView(R.layout.activity_splash_screen)
        startActivity(Intent(applicationContext,WindEscalatorActivity::class.java))
        finish()
    }
}