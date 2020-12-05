package windescalator.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Alert(
    var name: String?,
    var active: Boolean = false,
    var requestId: String?,
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null
)