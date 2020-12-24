package windescalator.alert

import android.content.Context
import android.util.Log
import ch.stephgit.windescalator.R
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.StringRequest
import windescalator.TAG
import windescalator.di.Injector
import javax.inject.Inject

class WindDataService @Inject constructor(
        private val context: Context) {

    init {
        Injector.appComponent.inject(this)
    }

    public fun getData() {
        val cache = DiskBasedCache(context.cacheDir, 1024 * 1024) // 1MB cap

// Set up the network to use HttpURLConnection as the HTTP client.
        val network = BasicNetwork(HurlStack())

// Instantiate the RequestQueue with the cache and network. Start the queue.
        val requestQueue = RequestQueue(cache, network).apply {
            start()
        }

        val url = context.getString(R.string.thun)

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