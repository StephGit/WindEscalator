package ch.stephgit.windescalator.alert.detail.direction

enum class Direction(val fullName: String, val degree: Int) {
    E("Ost", 90),
    SE("Südost", 135),
    S("Süd", 180),
    SW("Südwest", 225),
    W("West", 270),
    NW("Nordwest", 315),
    N("Nord", 0),
    NE("Nordost", 45);

    companion object {
        fun getByFullName(value: String): Direction? = values().find {
            it.fullName == value }

        fun getByDegree(value: Int): Direction {
            return values().find {
            it.degree - 23 < value && it.degree + 23 > value } ?: N
        }
    }
}