package ch.stephgit.windescalator.data

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
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
    @get:Exclude var id: String = ""
) : Parcelable
