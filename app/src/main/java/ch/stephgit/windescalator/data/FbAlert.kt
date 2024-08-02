package ch.stephgit.windescalator.data

data class FbAlert(
    var name: String = "",
    var active: Boolean = false,
    var resource: String = "",
    var nextRun: Long = 0L,
    var startTime: String = "",
    var endTime: String = "",
    var windForceKts: Int = 0,
    var directions: List<String> = emptyList(),
    var pending: Boolean = false,
    var userId: String = "",
    var id: String = ""
)