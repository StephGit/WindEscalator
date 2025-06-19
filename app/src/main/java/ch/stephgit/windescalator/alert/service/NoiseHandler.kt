package ch.stephgit.windescalator.alert.service

import android.content.Context
import android.media.MediaPlayer
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import androidx.preference.PreferenceManager
import ch.stephgit.windescalator.R
import ch.stephgit.windescalator.TAG
import ch.stephgit.windescalator.di.Injector
import javax.inject.Inject


class NoiseHandler @Inject constructor(
    val context: Context
) {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context)
    private val playAlertSound = sharedPreferences.getBoolean("activate_alert_sound", false)
    private val vibrateOnAlert = sharedPreferences.getBoolean("activate_vibration", false)
    private val soundVolume = sharedPreferences.getInt("alert_sound_level", 5)
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var vibrator: Vibrator

    init {
        Injector.appComponent.inject(this)
    }

    fun makeNoise() {
        mediaPlayer = MediaPlayer.create(context, R.raw.alarm_sound)
        vibrator = getSystemService(context, Vibrator::class.java) as Vibrator

        val vibe =
            VibrationEffect.createWaveform(longArrayOf(500, 1000, 500, 1000, 500, 1000, 500), 4)

        if (vibrateOnAlert) {
            vibrator.vibrate(vibe)
        }

        if (playAlertSound) {
            mediaPlayer.setOnPreparedListener {
                mediaPlayer.setVolume(soundVolume.toFloat(), soundVolume.toFloat())
                mediaPlayer.isLooping = false
                mediaPlayer.start()
            }
        }

    }

    fun stopNoise() {
        Log.d(TAG, "NoiseHandler: stopNoise called")
        if (vibrateOnAlert) {
            vibrator.cancel()
        }
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            vibrator.cancel()
        }
    }
}