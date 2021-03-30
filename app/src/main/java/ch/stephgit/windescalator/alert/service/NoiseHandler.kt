package ch.stephgit.windescalator.alert.service

import android.content.Context
import android.media.MediaPlayer
import android.os.Vibrator
import androidx.core.content.ContextCompat.getSystemService
import ch.stephgit.windescalator.R
import ch.stephgit.windescalator.di.Injector
import javax.inject.Inject


class NoiseHandler @Inject constructor(
        val context: Context) {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var vibrator: Vibrator

    init {
        Injector.appComponent.inject(this)
    }

    // TODO
    // Ask for permission to start media on silent modes!
    // Add preferences for alarm settings
    fun makeNoise() {
        mediaPlayer = MediaPlayer.create(context, R.raw.alarm_sound)
        vibrator = getSystemService(context, Vibrator::class.java) as Vibrator

        val pattern = longArrayOf(500, 1000, 500, 1000, 500, 1000, 500)
        vibrator.vibrate(pattern, 4)

        mediaPlayer.setOnPreparedListener {
            mediaPlayer.isLooping = true
            mediaPlayer.start()
        }

    }

    fun stopNoise() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            vibrator.cancel()
        }
    }
}