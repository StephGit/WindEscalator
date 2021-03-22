package windescalator.alert.service

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.StringRequest
import windescalator.TAG
import windescalator.alert.WindData
import windescalator.alert.detail.WindResource
import windescalator.data.entity.Alert
import windescalator.di.Injector
import javax.inject.Inject

class WindDataHandler @Inject constructor(val context: Context) {

    private lateinit var cache: DiskBasedCache
    private lateinit var network: BasicNetwork
    private lateinit var requestQueue: RequestQueue
    private lateinit var windData: WindData

    init {
        Injector.appComponent.inject(this)
        initNetworkQueue()
    }

    private fun initNetworkQueue() {
        cache = DiskBasedCache(context.cacheDir, 1024 * 1024) // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        network = BasicNetwork(HurlStack())
        // Instantiate the RequestQueue with the cache and network. Start the queue.
        requestQueue = RequestQueue(cache, network).apply {
            start()
        }
    }

    fun isFiring(alert: Alert): Boolean {
        getWindData(alert)
        return isAlert(alert, windData)
    }

    private fun isAlert(alert: Alert, windData: WindData): Boolean {
        return ((alert.windForceKts!! <= windData.windForce) &&
            (alert.startTime!! <= windData.messureTime) &&
            (alert.endTime!! > windData.messureTime) &&
            (alert.directions!!.contains(windData.windDirection)))
    }

    private fun getWindData(alert: Alert) {
        val windResource = WindResource.valueOf(alert.resource!!)
        val url = context.getString(windResource.url)
        Log.d(TAG, "getWinddata")
        // Formulate the request and handle the response.
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                // extract response depending on source
                windData = windResource.extractData(response)
            },
            { error ->
                // Handle error
                Log.e(TAG, "ERROR: %s".format(error.toString()))
            })
        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest)

    }
}