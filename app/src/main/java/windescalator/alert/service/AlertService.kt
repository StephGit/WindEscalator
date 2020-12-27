package windescalator.alert.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.os.Vibrator
import android.util.Log
import ch.stephgit.windescalator.R
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.StringRequest
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.joda.time.LocalDateTime
import windescalator.TAG
import windescalator.alert.detail.WindResource
import windescalator.data.entity.Alert
import windescalator.data.repo.AlertRepo
import windescalator.di.Injector
import windescalator.remote.NotificationHandler
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer

class AlertService : Service() {

    @Inject
    lateinit var alertRepo: AlertRepo

    @Inject
    lateinit var notificationHandler: NotificationHandler

    @Inject
    lateinit var noiseControl: NoiseControl

    private var timer: Timer? = null
    private var lastExecution: LocalDateTime? = LocalDateTime()

    private lateinit var cache: DiskBasedCache
    private lateinit var network: BasicNetwork
    private lateinit var requestQueue: RequestQueue
    var STARTED: Boolean = false

    init {
        Injector.appComponent.inject(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        timer?.cancel()
        STARTED = true
        var alerts = alertRepo.getActiveAlerts()
        if (alerts.isNullOrEmpty()) {
            Log.d(TAG, "Service stopped")
            stopService(intent)
            return START_NOT_STICKY
        } else {
            val fiveMinutes = 1000L * 60 * 1
            initNetworkQueue()
            timer = fixedRateTimer("AlarmService", true, initialDelay = 0, period = fiveMinutes) {
                // track last execution to handle service restarts
                if (lastExecution?.plusMinutes(1)?.isBefore(LocalDateTime()) == true) {
                    Log.d(TAG, "LastExecution: " + lastExecution)
                    alerts = alertRepo.getActiveAlerts()
                    if (alerts.isNullOrEmpty()) {
                        this.cancel()
                    }
                    GlobalScope.launch {
                        alerts.forEach { alert ->
                            getWindData(alert)
                            notificationHandler.createAlarmNotification(alert.resource!!)
                            noiseControl.makeNoise()
                        }
                    }
                    lastExecution = LocalDateTime()
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        STARTED = false
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun initNetworkQueue() {
        cache = DiskBasedCache(cacheDir, 1024 * 1024) // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        network = BasicNetwork(HurlStack())
        // Instantiate the RequestQueue with the cache and network. Start the queue.
        requestQueue = RequestQueue(cache, network).apply {
            start()
        }
    }

    private fun getWindData(alert: Alert) {
        val url = getString(WindResource.valueOf(alert.resource!!).url)
        Log.d(TAG, "getWinddata")
        // Formulate the request and handle the response.
        val stringRequest = StringRequest(
                Request.Method.GET, url,
                { response ->
                    Log.d(TAG, "response: " + response)
                },
                { error ->
                    // Handle error
                    Log.e(TAG, "ERROR: %s".format(error.toString()))
                })

        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest)
        // get event
        // check event for errors
        // handle Event

    }
}