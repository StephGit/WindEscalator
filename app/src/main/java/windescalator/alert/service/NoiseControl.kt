package windescalator.alert.service

import android.content.Context
import android.media.MediaPlayer
import android.os.Vibrator
import androidx.core.content.ContextCompat.getSystemService
import ch.stephgit.windescalator.R
import windescalator.di.Injector
import javax.inject.Inject

class NoiseControl @Inject constructor(
        val context: Context) {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var vibrator: Vibrator

    init {
        Injector.appComponent.inject(this)
    }

    public fun makeNoise() {
        mediaPlayer = MediaPlayer.create(context, R.raw.alarm_sound)
        mediaPlayer.isLooping = true
        vibrator = getSystemService(context, Vibrator::class.java) as Vibrator

        val pattern = longArrayOf(500, 1000, 500, 1000)
        vibrator.vibrate(pattern, 0)
        mediaPlayer.start()
    }

    fun stopNoise() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            vibrator.cancel()
        }
    }
}