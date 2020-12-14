package windescalator.alert.receiver

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import ch.stephgit.windescalator.R
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.StringRequest
import windescalator.TAG
import windescalator.di.Injector

class WindDataJobIntentService : JobIntentService() {


    private val jobId = 654

    init {
        Injector.appComponent.inject(this)
    }

    fun enqueueWork(context: Context, intent: Intent) {
        enqueueWork(context, WindDataJobIntentService::class.java, jobId, intent)
    }

    override fun onHandleWork(intent: Intent) {
        val cache = DiskBasedCache(cacheDir, 1024 * 1024) // 1MB cap

// Set up the network to use HttpURLConnection as the HTTP client.
        val network = BasicNetwork(HurlStack())

// Instantiate the RequestQueue with the cache and network. Start the queue.
        val requestQueue = RequestQueue(cache, network).apply {
            start()
        }

        val url = getString(R.string.thun)

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