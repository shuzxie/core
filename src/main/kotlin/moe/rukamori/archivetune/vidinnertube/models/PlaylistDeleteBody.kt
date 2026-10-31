package moe.rukamori.archivetune.innertube.models.body

import moe.rukamori.archivetune.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistDeleteBody(
    val context: Context,
    val playlistId: String
)
