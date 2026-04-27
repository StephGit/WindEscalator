package ch.stephgit.windescalator.alert.detail

import android.util.Log
import com.google.firebase.firestore.Exclude
import ch.stephgit.windescalator.R
import ch.stephgit.windescalator.alert.detail.TimePickerFragment.Companion.TAG
import ch.stephgit.windescalator.alert.detail.direction.Direction
import org.joda.time.LocalDateTime
import org.json.JSONObject
import org.jsoup.Jsoup
import kotlin.math.roundToInt


data class WindResource (@get:Exclude var id: String = "", var displayName: String = "", var name: String = "", @get:Exclude var icon: Int = R.drawable.ic_windbag_black_24, var localId: Int = -1, var online: Boolean = false, var lastChecked: Long = 0, var webcamUrl: String = "", var url: String = "", var latestForce: Int = 0, var latestDirection: String = "", var latestTime: String = "")

fun extractNeucData(data: String): WindData {
    Log.d(TAG, data)
    val json = JSONObject(data);
    return WindData(
        json.getDouble("windSpeedKnotsIchtus").roundToInt(),
        Direction.getByDegree(json.getInt("windDirectionDegreesIchtus")).toString(),
        LocalDateTime.parse(json.getString("recordTimeIchtus")).toLocalTime().toString()
    )
}


fun extractScniData(windJson: String): WindData {
    val json = JSONObject(windJson)
    val dataObj = json.optJSONObject("data") ?: return WindData()
    val dataArray = dataObj.optJSONArray("data") ?: return WindData()

    if (dataArray.length() == 0) return WindData()

    val lastWind = dataArray.getJSONObject(dataArray.length() - 1)
    val force = calcKnotsByKmh(lastWind.getDouble("y").toString())
    val timestamp = lastWind.getString("x")
    val time = LocalDateTime.parse(timestamp.replace(' ', 'T')).plusHours(2).toLocalTime().toString()

    // Extract direction from hourlyDir HTML (title="27.04.2026 09:00 : SSE")
    val hourlyDir = dataObj.optString("hourlyDir", "")
    val dirPattern = Regex("""title="[^"]*"""")
    val matches = dirPattern.findAll(hourlyDir).toList()
    val direction = if (matches.isNotEmpty()) {
        val lastMatch = matches.last().value
        lastMatch.split(" : ").lastOrNull()?.replace("\"", "") ?: ""
    } else ""

    return WindData(force, direction, time)
}

fun extractWsctData(data: String): WindData {
    val doc = Jsoup.parse(data)
    val knots = doc.select("windkts").text();
    val degrees = doc.select("curval_winddir").text();
    val time = doc.select("time").text();

    return WindData(
        knots.toDouble().roundToInt(),
        Direction.getByDegree(Integer.parseInt(degrees)).name,
        time
    )

}

fun extractWindData(data: String, localId: Int): WindData {
    return when (localId) {
        1 -> extractScniData(data)
        2 -> extractNeucData(data)
        else -> extractWsctData(data)
    }
}

private fun calcKnotsByKmh(windText: String) = (windText.toDouble() / 1.852).roundToInt()
