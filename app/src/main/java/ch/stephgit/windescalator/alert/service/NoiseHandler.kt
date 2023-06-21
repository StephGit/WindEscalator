package ch.stephgit.windescalator.alert.service

import android.content.Context
import android.media.MediaPlayer
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import ch.stephgit.windescalator.R
import ch.stephgit.windescalator.TAG
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

        val vibe = VibrationEffect.createWaveform(longArrayOf(500, 1000, 500, 1000, 500, 1000, 500), 4)

        vibrator.vibrate(vibe)

        mediaPlayer.setOnPreparedListener {
            mediaPlayer.isLooping = true
//            mediaPlayer.start()
        }

    }

    fun stopNoise() {
        Log.d(TAG, "NoiseHandler: stopNoise called")
        vibrator.cancel()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            vibrator.cancel()
        }
    }
}