package ch.stephgit.windescalator.alert.detail

import android.util.Log
import ch.stephgit.windescalator.R
import ch.stephgit.windescalator.alert.detail.TimePickerFragment.Companion.TAG
import ch.stephgit.windescalator.alert.detail.direction.Direction
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.json.JSONObject
import org.jsoup.Jsoup
import kotlin.math.roundToInt


enum class WindResource(
    val id: Int,
    val fullName: String,
    val icon: Int,
    val url: Int,
    val extractData: (data: String) -> WindData
) {
    WSCT(1, "WSCT Thun", R.drawable.ic_windbag_black_24, R.string.thun, ::extractWsctData),
    SCNI(2, "SCNI Interlaken", R.drawable.ic_windbag_black_24, R.string.scni, ::extractScniData),
    NEUC(3, "St. Blaise", R.drawable.ic_windbag_black_24, R.string.neuc, ::extractNeucData)
}

fun extractNeucData(data: String): WindData {
    Log.d(TAG, data)
    val json = JSONObject(data);
    val windData = WindData()
    val fmt: DateTimeFormatter = DateTimeFormat.forPattern("HH:mm")
    val localTime: LocalTime = fmt.parseLocalTime(json.getString("recordTimeIchtus"))

    return WindData(
        json.getDouble("windSpeedKnotsIchtus").roundToInt(),
        Direction.getByDegree(json.getInt("windDirectionDegreesIchtus")).toString(),
        localTime.toString()
    )
}


private fun extractScniData(data: String): WindData {
    val doc = Jsoup.parse(data)
    val windData = WindData()
    // throw away titles
    val dailyData = doc.text().split("0:00");
    val lines = dailyData[dailyData.lastIndex].replace(' ', '_').split('_');
    val pos = lines.size - 15;
    if (lines.isNotEmpty()) {
        if (isActualData(lines[pos])) {
            windData.time = lines[pos]
            windData.direction = Direction.getByDegree(lines[pos + 1].toInt()).toString()
            windData.force = calcKnotsByKmh(lines[pos + 2])
        }
    }
    return windData
}

private fun isActualData(time: String): Boolean {
    return LocalDateTime.now().hourOfDay.toString() == time.split(':')[0]

}

private fun extractWsctData(data: String): WindData {
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

private fun calcKnotsByKmh(windText: String) = (windText.toDouble() / 1.852).roundToInt()

