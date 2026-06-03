package ch.stephgit.windescalator.alert.detail

import android.util.Log
import com.google.firebase.firestore.Exclude
import ch.stephgit.windescalator.R
import ch.stephgit.windescalator.alert.detail.TimePickerFragment.Companion.TAG
import ch.stephgit.windescalator.alert.detail.direction.Direction
import org.joda.time.LocalDateTime
import org.json.JSONObject
import org.jsoup.Jsoup
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt


data class WindResource (@get:Exclude var id: String = "", var displayName: String = "", var name: String = "", @get:Exclude var icon: Int = R.drawable.ic_windbag_black_24, var localId: Int = -1, var online: Boolean = false, var lastChecked: Long = 0, var url: String = "", var latestForce: Int = 0, var latestGust: Int = 0, var latestDirection: String = "", var latestTime: String = "")

fun extractNeucData(data: String): WindData {
    Log.d(TAG, data)
    val json = JSONObject(data);
    val time = LocalDateTime.parse(json.getString("recordTimeIchtus")).toLocalTime().toString("HH:mm")
    return WindData(
        force = json.getDouble("windSpeedKnotsIchtus").roundToInt(),
        gust = json.optDouble("windSpeedHigh1KnotsIchtus", 0.0).roundToInt(),
        direction = Direction.getByDegree(json.getInt("windDirectionDegreesIchtus")).toString(),
        time = time
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
    val time = LocalDateTime.parse(timestamp.replace(' ', 'T')).plusHours(2).toLocalTime().toString("HH:mm")

    // Extract direction from hourlyDir HTML (title="27.04.2026 09:00 : SSE")
    val hourlyDir = dataObj.optString("hourlyDir", "")
    val dirPattern = Regex("""title="[^"]*"""")
    val matches = dirPattern.findAll(hourlyDir).toList()
    val direction = if (matches.isNotEmpty()) {
        val lastMatch = matches.last().value
        lastMatch.split(" : ").lastOrNull()?.replace("\"", "") ?: ""
    } else ""

    return WindData(force = force, direction = direction, time = time)
}

fun extractWsctData(data: String): WindData {
    val doc = Jsoup.parse(data)
    val knots = doc.select("windkts").text();
    val gustKts = doc.select("windgustkts").text();
    val degrees = doc.select("curval_winddir").text();
    val timeRaw = doc.select("time").text();

    val time = if (timeRaw.contains(":")) {
        val parts = timeRaw.split(":")
        if (parts.size >= 2) "${parts[0]}:${parts[1]}" else timeRaw
    } else timeRaw

    return WindData(
        force = knots.toDouble().roundToInt(),
        gust = if (gustKts.isNotBlank()) gustKts.toDouble().roundToInt() else 0,
        direction = Direction.getByDegree(Integer.parseInt(degrees)).name,
        time = time
    )
}

fun extractBrieData(data: String): WindData {
    val doc = Jsoup.parse(data)
    val table = doc.select("table#table-2").first() ?: return WindData()
    val rows = table.select("tr")

    var force = 0
    var gust = 0
    var direction = ""
    var time = ""

    for (row in rows) {
        val cells = row.select("td")
        if (cells.size < 2) continue

        val sensorName = cells[0].text().trim()
        val currentValue = cells[1].text().trim()

        when (sensorName) {
            "Wind aktuell" -> {
                force = calcKnotsByKmh(currentValue.replace("km/h", "").trim())
            }
            "Wind-B�e" -> {
                gust = calcKnotsByKmh(currentValue.replace("km/h", "").trim())
            }
            "Wind-Richtung" -> {
                direction = currentValue
            }
        }
    }

    val headerText = table.select("thead th[colspan=4]").first()?.text() ?: ""
    val timeMatch = Regex("""(\d{2}:\d{2})""").find(headerText)
    time = timeMatch?.value ?: ""

    return WindData(force = force, gust = gust, direction = direction, time = time)
}

fun extractWindData(data: String, localId: Int): WindData {
    return when (localId) {
        1 -> extractScniData(data)
        2 -> extractNeucData(data)
        3 -> extractWsctData(data)
        4 -> extractWsctData(data)
        5 -> extractBrieData(data)
        else -> return WindData()
    }
}



fun isWindDataFresh(timeStr: String, maxMinutes: Long = 15): Boolean {
    if (timeStr.isBlank()) return false
    return try {
        val dataTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"))
        val now = LocalTime.now()
        val diff = Duration.between(dataTime, now).toMinutes()
        diff in 0..maxMinutes
    } catch (e: Exception) {
        false
    }
}

private fun calcKnotsByKmh(windText: String) = (windText.toDouble() / 1.852).roundToInt()
