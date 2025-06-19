package ch.stephgit.windescalator.alert.detail

import kotlinx.serialization.Serializable

@Serializable
data class WindData (var force: Int = 0, var direction: String = "", var time: String = "")