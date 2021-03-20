package windescalator.alert.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.joda.time.LocalDateTime
import org.jsoup.Jsoup
import windescalator.TAG
import windescalator.alert.detail.WindResource
import windescalator.alert.receiver.AlertBroadcastReceiver
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
    lateinit var alertReceiver: AlertBroadcastReceiver

    private var timer: Timer? = null
    private var lastExecution: LocalDateTime? = LocalDateTime()

    private lateinit var cache: DiskBasedCache
    private lateinit var network: BasicNetwork
    private lateinit var requestQueue: RequestQueue

    init {
        Injector.appComponent.inject(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        registerReceiver(alertReceiver, alertReceiver.getFilter())
        timer?.cancel()

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
                            if (isFiring(alert)) sendAlertBroadcast(alert.id!!)
//                            notificationHandler.createAlarmNotification(alert.resource!!)
                        }
                    }
                    lastExecution = LocalDateTime()
                }
            }
        }
        return START_STICKY
    }

    private fun isFiring(alert: Alert): Boolean {
        getWindData(alert)
        return false

    }

    private fun sendAlertBroadcast(alertId: Long) {
        Log.d(TAG, alertId.toString())
        val intent = Intent(applicationContext, AlertBroadcastReceiver::class.java).apply {
            action = alertReceiver.getFilter().getAction(0)
            putExtra("ALERT_ID", alertId)
        }
        applicationContext.sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(alertReceiver)
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
                    var doc = Jsoup.parse(response)

                    // extract response depending on source

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