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
    var startTime: LocalDateTime?,
    var endTime: LocalDateTime?,
    var windForceKts: Int?,
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null
)