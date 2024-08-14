package ch.stephgit.windescalator.alert.service

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import ch.stephgit.windescalator.TAG
import ch.stephgit.windescalator.alert.receiver.AlertBroadcastReceiver
import ch.stephgit.windescalator.di.Injector
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class FirebaseForgroundMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var alertReceiver: AlertBroadcastReceiver

    private val MAX_RETRIES = 3
    private val RETRY_DELAY_MS: Long = 2000 // 2 seconds


    init {
        Injector.appComponent.inject(this)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Get the current user's UID
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        storeTokenWithRetry(token, uid);
    }


    private fun storeTokenWithRetry(token: String, uid: String) {
        var retryCount = 0
        while (retryCount < MAX_RETRIES) {
            try {
                // Store the token in Firestore
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(uid)
                    .set(mapOf("fcmToken" to token), SetOptions.merge())
                    .addOnSuccessListener { Log.d("FCM", "Token stored successfully") }
                    .addOnFailureListener { e ->
                        retryCount++

                        // Wait before retrying
                        if (retryCount < MAX_RETRIES) {
                            Handler(Looper.getMainLooper()).postDelayed({
                                Log.d(
                                    "FCM",
                                    "Retrying in " + RETRY_DELAY_MS / 1000 + " seconds..."
                                )
                            }, RETRY_DELAY_MS)
                            try {
                                TimeUnit.MILLISECONDS.sleep(RETRY_DELAY_MS)
                            } catch (ex: InterruptedException) {
                                Thread.currentThread().interrupt()
                            }
                        } else {
                            // Handle the case where all retries failed
                            Log.e(
                                "FCM",
                                "Token storage failed after $MAX_RETRIES retries."
                            )
                            Toast.makeText(baseContext, "Token storage failed", Toast.LENGTH_SHORT).show()
                        }
                    }

                // Exit the loop if the token was stored successfully
                return
            } catch (e: Exception) {
                retryCount++

                // Wait before retrying
                if (retryCount < MAX_RETRIES) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        Log.d(
                            "FCM",
                            "Retrying in " + RETRY_DELAY_MS / 1000 + " seconds..."
                        )
                    }, RETRY_DELAY_MS)
                    try {
                        TimeUnit.MILLISECONDS.sleep(RETRY_DELAY_MS)
                    } catch (ex: InterruptedException) {
                        Thread.currentThread().interrupt()
                    }
                } else {
                    // Handle the case where all retries failed
                    Log.e("FCM", "Token storage failed after $MAX_RETRIES retries.")
                    Toast.makeText(baseContext, "Token storage failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification ID: ${it.title} - Winddata: ${it.body}")

            val data = Data.Builder()
                .putString("ALERT_ID", it.title)
                .putString("WIND_DATA", it.body)
                .build()

            val workRequest =
                OneTimeWorkRequest.Builder(WakeUpWorker::class.java).setInputData(data).build()
            WorkManager.getInstance(this).enqueue(workRequest)
        }
    }



}