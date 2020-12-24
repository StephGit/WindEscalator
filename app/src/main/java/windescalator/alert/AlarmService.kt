package windescalator.alert

import ch.stephgit.windescalator.R
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.os.Vibrator
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.joda.time.LocalDateTime
import windescalator.data.repo.AlertRepo
import windescalator.di.Injector
import windescalator.remote.NotificationHandler
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer


class AlarmService : Service() {

    @Inject
    lateinit var alertRepo: AlertRepo

    @Inject
    lateinit var notificationHandler: NotificationHandler

    private var timer: Timer? = null
    private var lastExecution: LocalDateTime? = LocalDateTime()
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    init {
        Injector.appComponent.inject(this)
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound)
        mediaPlayer!!.isLooping = true
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator?
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        timer?.cancel()
        val fiveMinutes = 1000L * 60 * 5

        timer = fixedRateTimer("AlarmService", true, initialDelay = 0, period = fiveMinutes) {
            // track last execution to handle service restarts
            if(lastExecution?.plusMinutes(5)?.isBefore(LocalDateTime()) == true) {
                GlobalScope.launch {
                    // get winddata for alert resources
                    // check if alert is needed
                    notificationHandler.createAlarmNotification()
                    mediaPlayer!!.start()
                    val pattern = longArrayOf(0, 100, 1000)
                    vibrator!!.vibrate(pattern, 0)
                }
                lastExecution = LocalDateTime()
            }

        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer!!.stop()
        vibrator!!.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}