package windescalator.data

import androidx.room.TypeConverter
import org.joda.time.LocalDateTime
import java.lang.reflect.Type


class DatabaseTypeConverters {

    @TypeConverter
    fun longToDateTime(v: Long?): LocalDateTime? {
        return if(v == null){
            null
        } else {
            LocalDateTime(v)
        }
    }

    @TypeConverter
    fun dateTimeToLong(v: LocalDateTime?): Long? {
        return v?.toDateTime()?.millis
    }

    @TypeConverter
    fun fromString(stringListString: String): List<String> {
        return stringListString.split(",").map { it }
    }

    @TypeConverter
    fun toString(stringList: List<String>): String {
        return stringList.joinToString(separator = ",")
    }
}