package windescalator.alert.detail

import ch.stephgit.windescalator.R

enum class WindResource(val id: Int, val fullname: String, val icon: Int) {
    WSCT(1, "WSCT Thun", R.drawable.ic_windbag_black_24),
    SCNI(2, "SCNI Interlaken", R.drawable.ic_windbag_black_24)
}