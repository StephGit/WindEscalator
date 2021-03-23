package windescalator.alert.detail

import ch.stephgit.windescalator.R
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
    SCNI(2, "SCNI Interlaken", R.drawable.ic_windbag_black_24, R.string.thun, ::extractWsctData)
}

fun extractWsctData(data: String): WindData {
    val doc = Jsoup.parse(data)
    var elements = doc.select("td[width=120]")
    val windData = WindData()
    elements.forEach { element ->
        if (element.text().contains("km/h")) {
            val windText = element.text().filter { it.isDigit() || it == '.' }
            windData.windForce = (windText.toDouble() / 1.852).roundToInt()
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
