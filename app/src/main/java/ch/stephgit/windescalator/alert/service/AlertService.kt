//package ch.stephgit.windescalator.alert.service
//
//import android.app.Service
//import android.content.Intent
//import android.os.IBinder
//import android.util.Log
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//import org.joda.time.LocalDateTime
//import org.joda.time.format.DateTimeFormat
//import org.joda.time.format.DateTimeFormatter
//import ch.stephgit.windescalator.TAG
//import ch.stephgit.windescalator.alert.receiver.AlertBroadcastReceiver
//import ch.stephgit.windescalator.data.repo.AlertRepo
//import ch.stephgit.windescalator.di.Injector
//import ch.stephgit.windescalator.remote.NotificationHandler
//import java.util.*
//import javax.inject.Inject
//import kotlin.concurrent.fixedRateTimer
//
//
//class AlertService : Service() {
//
//    @Inject
//    lateinit var alertRepo: AlertRepo
//
//    @Inject
//    lateinit var notificationHandler: NotificationHandler
//
//    @Inject
//    lateinit var alertReceiver: AlertBroadcastReceiver
//
//    @Inject
//    lateinit var windDataAdapter: WindDataHandler
//
//    private var timer: Timer? = null
//    private var lastExecution: LocalDateTime? = LocalDateTime()
//    private val fmt: DateTimeFormatter = DateTimeFormat.forPattern("HH:mm")
//
//    init {
//        Injector.appComponent.inject(this)
//    }
//
//    override fun onCreate() {
//        super.onCreate()
//        startForeground(1, notificationHandler.getServiceNotification())
//    }
//
//    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
//        Log.d(TAG, "AlertService started $this")
//
//        registerReceiver(alertReceiver, alertReceiver.getFilter())
//        timer?.cancel()
//
//        var alerts = alertRepo.getActiveAlerts()
//        if (alerts.isNullOrEmpty()) {
//            Log.d(TAG, "AlertService stopped")
//            stopService(intent)
//            return START_NOT_STICKY
//        } else {
//            Log.d(TAG, "AlertService starting timer")
//            val fiveMinutes = 1000L * 10//60 * 5  // TODO set to 5 min
//
//            timer = fixedRateTimer("AlertService", true, initialDelay = 0, period = fiveMinutes) {
//                // track last execution to handle windescalator.alert.service restarts
//                if (lastExecution?.plusMinutes(1)?.isBefore(LocalDateTime()) == true) {
//                    Log.d(TAG, "AlertService: LastExecution: $lastExecution")
//                    alerts = alertRepo.getActiveAndInTimeAlerts(LocalDateTime.now().toString(fmt))
//                    if (alerts.isNullOrEmpty()) {
//                        stopService(intent)
//                    }
//                    GlobalScope.launch {
//                        alerts.forEach { alert ->
//                            if (windDataAdapter.isFiring(alert)) sendAlertBroadcast(alert.id!!)
////                            notificationHandler.createAlarmNotification(alert.resource!!)
//                        }
//                    }
//                    lastExecution = LocalDateTime()
//                }
//            }
//            Log.d(TAG, "AlertService timer $timer")
//        }
//        return START_STICKY
//    }
//
//    private fun sendAlertBroadcast(alertId: Long) {
//        val intent = Intent(applicationContext, AlertBroadcastReceiver::class.java).apply {
//            action = alertReceiver.getFilter().getAction(0)
//            putExtra("ALERT_ID", alertId)
//        }
//        applicationContext.sendBroadcast(intent)
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        unregisterReceiver(alertReceiver)
//    }
//
//    override fun onBind(intent: Intent?): IBinder? {
//        return null
//    }
//
//}