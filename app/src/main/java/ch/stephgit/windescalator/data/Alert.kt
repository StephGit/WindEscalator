package ch.stephgit.windescalator.data

import java.io.Serializable

data class Alert(
    var name: String = "",
    var active: Boolean = false,
    var resource: Int = -1,
    var nextRun: Long = 0L,
    var startTime: String = "",
    var endTime: String = "",
    var windForceKts: Int = 0,
    var directions: List<String> = emptyList(),
    var pending: Boolean = false,
    var userId: String = "",
    var id: String = ""
) : Serializable