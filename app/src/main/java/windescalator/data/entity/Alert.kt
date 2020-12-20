package windescalator.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.joda.time.LocalDateTime
import windescalator.alert.detail.WindResource

@Entity
data class Alert(
    var name: String?,
    var active: Boolean = false,
    var resource: String?,
    var startTime: String?,
    var endTime: String?,
    var windForceKts: Int?,
    var directions: List<String>?,
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null
)