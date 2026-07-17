package ch.stephgit.windescalator.alert.detail

import android.util.Log
import ch.stephgit.windescalator.R
import ch.stephgit.windescalator.alert.detail.TimePickerFragment.Companion.TAG
import ch.stephgit.windescalator.alert.detail.direction.Direction
import com.google.firebase.firestore.Exclude
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt


data class WindResource (
    @get:Exclude var id: String = "",
    var displayName: String = "",
    var name: String = "",
    @get:Exclude var icon: Int = R.drawable.ic_windbag_black_24,
    var localId: Int = -1,
    var online: Boolean = false,
    var lastChecked: Long = 0,
    var url: String = "",
    var latestForce: Int = 0,
    var latestGust: Int = 0,
    var latestDirection: String = "",
    var latestTime: String = ""
) {
    @get:Exclude
    val type: WindResourceType?
        get() = WindResourceType.fromId(localId)
}

enum class WindResourceType(val localId: Int) {
    SCNI(1) {
        override fun extract(data: String) = extractScniData(data)
    },
    NEUC(2) {
        override fun extract(data: String) = extractNeucData(data)
    },
    WSCT_LAKE(3) {
        override fun extract(data: String) = extractWsctData(data)
    },
    WSCT_PROG(4) {
        override fun extract(data: String) = extractWsctData(data)
    },
    BRIE(5) {
        override fun extract(data: String) = extractBrieData(data)
    },
    GRUYERE(6) {
        override fun extract(data: String) = extractGruyData(data)
    };

    abstract fun extract(data: String): WindData

    companion object {
        fun fromId(id: Int) = entries.find { it.localId == id }
    }
}

private val jsonConfig = Json { ignoreUnknownKeys = true }

@InternalSerializationApi @Serializable
internal data class NeucResponse(
    val recordTimeIchtus: String,
    val windSpeedKnotsIchtus: Double,
    val windSpeedHigh1KnotsIchtus: Double? = null,
    val windDirectionDegreesIchtus: Int
)

@InternalSerializationApi @Serializable
internal data class ScniResponse(
    val data: ScniDataContainer
)

@InternalSerializationApi @Serializable
internal data class ScniDataContainer(
    val data: List<ScniPoint>,
    val hourlyDir: String? = null
)

@InternalSerializationApi @Serializable
internal data class ScniPoint(
    val x: String,
    val y: Double
)

@InternalSerializationApi @Serializable
internal data class GruyResponse(
    val measures: List<GruyMeasure> = emptyList()
)

@InternalSerializationApi @Serializable
internal data class GruyMeasure(
    val updatedAt: String,
    val windSpeed: Double? = null,
    val windBurst: Double? = null,
    val windDir: Int? = null
)

@OptIn(InternalSerializationApi::class)
fun extractNeucData(data: String): WindData {
    val response = jsonConfig.decodeFromString<NeucResponse>(data)
    val time = LocalDateTime.parse(response.recordTimeIchtus)
        .format(DateTimeFormatter.ofPattern("HH:mm"))
    return WindData(
        force = response.windSpeedKnotsIchtus.roundToInt(),
        gust = response.windSpeedHigh1KnotsIchtus?.roundToInt() ?: 0,
        direction = Direction.getByDegree(response.windDirectionDegreesIchtus).name,
        time = time
    )
}


@OptIn(InternalSerializationApi::class)
fun extractScniData(windJson: String): WindData {
    val response = jsonConfig.decodeFromString<ScniResponse>(windJson)
    val dataArray = response.data.data

    if (dataArray.isEmpty()) return WindData()

    val lastWind = dataArray.last()
    val force = calcKnotsByKmh(lastWind.y.toString())

    val time = LocalDateTime.parse(lastWind.x.replace(' ', 'T'))
        .atZone(ZoneOffset.UTC)
        .format(DateTimeFormatter.ofPattern("HH:mm"))

    // Extract direction from hourlyDir HTML (title="27.04.2026 09:00 : SSE")
    val hourlyDir = response.data.hourlyDir ?: ""
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
    val knots = doc.select("windkts").text()
    val gustKts = doc.select("windgustkts").text()
    val degrees = doc.select("curval_winddir").text()
    val timeRaw = doc.select("time").text()

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
    var time: String

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

@OptIn(InternalSerializationApi::class)
fun extractGruyData(data: String): WindData {
    val response = jsonConfig.decodeFromString<GruyResponse>(data)
    if (response.measures.isEmpty()) return WindData()

    val latestMeasure = response.measures.maxByOrNull { ZonedDateTime.parse(it.updatedAt).toInstant() } ?: return WindData()
    val latestZdt = ZonedDateTime.parse(latestMeasure.updatedAt)

    val zurichTime = latestZdt.withZoneSameInstant(ZoneId.of("Europe/Zurich"))
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    return WindData(
        force = calcKnotsByKmh(latestMeasure.windSpeed.toString()) ?: 0,
        gust = calcKnotsByKmh(latestMeasure.windBurst.toString()) ?: 0,
        direction = Direction.getByDegree(latestMeasure.windDir ?: 0).name,
        time = zurichTime.format(timeFormatter)
    )
}

fun extractWindData(data: String, localId: Int): WindData {
    return WindResourceType.fromId(localId)?.extract(data) ?: WindData()
}

fun isWindDataFresh(timeStr: String, maxMinutes: Long = 15): Boolean {
    if (timeStr.isBlank()) return false
    return try {
        val dataTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"))
        val now = LocalTime.now()
        val diff = Duration.between(dataTime, now).toMinutes()
        diff in 0..maxMinutes
    } catch (e: Exception) {
        Log.e(TAG, e.toString())
        false
    }
}

private fun calcKnotsByKmh(windText: String) = (windText.toDouble() / 1.852).roundToInt()
