package windescalator.alert.detail

import ch.stephgit.windescalator.R

enum class WindResource(val code: String, val fullname: String, val icon: Int) {
    WSCT("WSCT", "WSCT Thun", R.drawable.ic_windbag_black_24),
    SCNI("SCNI", "SCNI Interlaken",  R.drawable.ic_windbag_black_24)
}