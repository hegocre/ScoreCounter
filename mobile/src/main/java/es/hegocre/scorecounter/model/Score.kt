package es.hegocre.scorecounter.model

import kotlinx.serialization.Serializable

@Serializable
data class Score(
    val score: Int = 0,
    val playerName: String = ""
)
