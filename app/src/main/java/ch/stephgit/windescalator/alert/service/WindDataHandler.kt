package ch.stephgit.windescalator.alert.service

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import ch.stephgit.windescalator.R
import ch.stephgit.windescalator.TAG
import ch.stephgit.windescalator.alert.detail.WindData
import ch.stephgit.windescalator.alert.detail.WindResource
import ch.stephgit.windescalator.data.entity.Alert
import ch.stephgit.windescalator.di.Injector
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.StringRequest
import javax.inject.Inject

class WindDataHandler @Inject constructor(val context: Context) {

    private lateinit var cache: DiskBasedCache
    private lateinit var network: BasicNetwork
    private lateinit var requestQueue: RequestQueue

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

    fun isFiring(
        alert: Alert,
        sendAlertBroadcast: (Long, String) -> Unit,
        setNextInterval: (Alert) -> Unit
    ) {
        getWindData(alert, object : VolleyCallback {
            @SuppressLint("StringFormatMatches")
            override fun onSuccess(result: WindData) {
                val windData = context.getString(
                    R.string.winddata,
                    result.force.toString(),
                    result.direction,
                    result.time
                )
                Log.d(TAG, "WindDataHandler: $windData $alert")
                if (isAlert(alert, result)) {
                    sendAlertBroadcast(
                        alert.id!!,
                        windData
                    )
                } else {
                    setNextInterval(alert)
                }
            }
        });
    }

    private fun isAlert(alert: Alert, windData: WindData): Boolean {
        return ((alert.windForceKts!! <= windData.force) &&
                (alert.startTime!! <= windData.time) &&
                (alert.endTime!! > windData.time) &&
                (alert.directions!!.contains(windData.direction)))
    }

    private fun getWindData(alert: Alert, callback: VolleyCallback) {
        val windResource = WindResource.valueOf(alert.resource!!)
        val url = context.getString(windResource.url)
        Log.d(TAG, "WindDataHandler: getWinddata $url")
        // Formulate the request and handle the response.


        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                val windData = windResource.extractData(response)
                callback.onSuccess(windData);
            },
            { error ->
                // Handle error
                Log.e(TAG, "ERROR: %s".format(error.toString()))
            })
        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest)
    }

    public interface VolleyCallback {
        fun onSuccess(result: WindData);

    }
}