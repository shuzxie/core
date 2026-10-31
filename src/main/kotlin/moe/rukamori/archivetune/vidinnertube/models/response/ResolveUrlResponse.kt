package moe.rukamori.archivetune.innertube.models.response

import moe.rukamori.archivetune.innertube.models.NavigationEndpoint
import kotlinx.serialization.Serializable

@Serializable
data class ResolveUrlResponse(
    val endpoint: NavigationEndpoint? = null,
)
