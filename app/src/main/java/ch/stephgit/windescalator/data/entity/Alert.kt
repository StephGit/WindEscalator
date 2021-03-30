package ch.stephgit.windescalator.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Alert(
    var name: String?,
    var active: Boolean = false,
    var resource: String?,
    var startTime: String?,
    var endTime: String?,
    var windForceKts: Int?,
    var directions: List<String>?,
    var pending: Boolean = false,
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null
)