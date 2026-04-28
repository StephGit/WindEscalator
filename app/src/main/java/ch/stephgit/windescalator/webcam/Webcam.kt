package ch.stephgit.windescalator.webcam

import com.google.firebase.firestore.Exclude
import ch.stephgit.windescalator.R

data class Webcam(
    @get:Exclude var id: String = "",
    var displayName: String = "",
    @get:Exclude var icon: Int = R.drawable.ic_windbag_black_24,
    var url: String = ""
)
