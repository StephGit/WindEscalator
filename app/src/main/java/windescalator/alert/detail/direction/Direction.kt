package windescalator.alert.detail.direction

enum class Direction(val fullName: String) {
    E("Ost"),
    SE("Südost"),
    S("Süd"),
    SW("Südwest"),
    W("West"),
    NW("Nordwest"),
    N("Nord"),
    NE("Nordost");

    companion object {
        fun getByFullName(value: String): Direction? = values().find {
            it.fullName == value }
    }
}