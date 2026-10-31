package moe.rukamori.archivetune.innertube.models.body

import moe.rukamori.archivetune.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class ReelItemWatchBody(
    val context: Context,
    val playerRequest: PlayerRequest,
    val params: String? = null,
    val disablePlayerResponse: Boolean = true,
    val inputType: String? = null,
) {
    @Serializable
    data class PlayerRequest(
        val videoId: String,
        val params: String? = null,
        val contentCheckOk: Boolean = true,
        val racyCheckOk: Boolean = true,
    )
}
