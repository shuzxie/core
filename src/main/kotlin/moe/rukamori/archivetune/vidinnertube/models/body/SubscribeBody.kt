package moe.rukamori.archivetune.innertube.models.body

import moe.rukamori.archivetune.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class SubscribeBody(
    val channelIds: List<String>,
    val context: Context,
)
