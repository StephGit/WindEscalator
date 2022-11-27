package ch.stephgit.windescalator.alert.detail

import ch.stephgit.windescalator.R
import ch.stephgit.windescalator.alert.WindData
import ch.stephgit.windescalator.alert.detail.direction.Direction
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
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
    SCNI(2, "SCNI Interlaken", R.drawable.ic_windbag_black_24, R.string.scni, ::extractScniData)
}



private fun extractScniData(data: String): WindData {
    val doc = Jsoup.parse(data)
    val windData = WindData()
    var lines = doc.text().split("87.6")
    if (lines.isNotEmpty()) {
        var values = lines[lines.lastIndex - 1].split(" ")
        if (isActualData(lines[0], values)) {
            windData.messureTime = values[1]
            windData.windDirection = Direction.getByDegree(values[2].toInt()).toString()
            windData.windForce = calcKnotsByKmh(values[3])
        }
    }
    return windData
}

private fun isActualData(s: String, values: List<String>): Boolean {
    val fmt: DateTimeFormatter = DateTimeFormat.forPattern("DD/MM/yy")
    var date = s.split(' ')[0]
    return LocalDate.now().toString(fmt)
        .equals(date.toString()) && LocalDateTime.now().hourOfDay.toString() == values[1].split(':')[0]

}

private fun extractWsctData(data: String): WindData {
    val doc = Jsoup.parse(data)
    val knots = doc.select("windkts").text();
    val degrees = doc.select("curval_winddir").text();
    val time = doc.select("time").text();

    return WindData(
        knots.toDouble().roundToInt(),
        Direction.getByDegree(Integer.parseInt(degrees)).name,
        time)

}

private fun calcKnotsByKmh(windText: String) = (windText.toDouble() / 1.852).roundToInt()

