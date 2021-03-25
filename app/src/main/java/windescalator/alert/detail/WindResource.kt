package windescalator.alert.detail

import ch.stephgit.windescalator.R
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.jsoup.Jsoup
import windescalator.alert.WindData
import windescalator.alert.detail.direction.Direction
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
    var elements = doc.select("td[width=120]")
    val windData = WindData()
    elements.forEach { element ->
        if (element.text().contains("km/h")) {
            val windText = element.text().filter { it.isDigit() || it == '.' }
            windData.windForce = calcKnotsByKmh(windText)
        } else if (!element.text().contains(":") && (!element.text().contains("Bft"))) {
            var tmp = element.text().replace("&nbsp;", "").also {
                it.replace("\n", "")
            }
            if (tmp.contains(' ')) {
                val dirs = tmp.split(" ")
                tmp = dirs[0]
            }

            Direction.getByFullName(tmp)?.name.also {
                if (it != null) {
                    windData.windDirection = it
                }
            }
        }
    }
    elements = doc.select("span")
    elements.forEach { element ->
        val times = element.text().split(" ")
        windData.messureTime = times[3].filter { it.isDigit() || it == ':' }
    }
    return windData
}

private fun calcKnotsByKmh(windText: String) = (windText.toDouble() / 1.852).roundToInt()

