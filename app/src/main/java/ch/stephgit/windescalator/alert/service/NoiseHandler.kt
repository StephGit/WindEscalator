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
import javax.inject.Inject


class NoiseHandler @Inject constructor(
    val context: Context
) {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context)
    private val playAlertSound = sharedPreferences.getBoolean("activate_alert_sound", false)
    private val vibrateOnAlert = sharedPreferences.getBoolean("activate_vibration", false)
    private val soundVolume = sharedPreferences.getInt("alert_sound_level", 5)
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var vibrator: Vibrator

    fun makeNoise() {
        releaseMediaPlayer()
        mediaPlayer = MediaPlayer.create(context, R.raw.alarm_sound)
        vibrator = getSystemService(context, Vibrator::class.java) as Vibrator

        val vibe =
            VibrationEffect.createWaveform(longArrayOf(500, 1000, 500, 1000, 500, 1000, 500), 4)

        if (vibrateOnAlert) {
            vibrator.vibrate(vibe)
        }

        if (playAlertSound) {
            mediaPlayer?.setOnPreparedListener {
                it.setVolume(soundVolume.toFloat(), soundVolume.toFloat())
                it.isLooping = false
                it.start()
            }
        }
    }

    fun stopNoise() {
        Log.d(TAG, "NoiseHandler: stopNoise called")
        if (::vibrator.isInitialized && vibrateOnAlert) {
            vibrator.cancel()
        }
        releaseMediaPlayer()
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
    }
}
