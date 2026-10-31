package moe.rukamori.archivetune.innertube.models.body

import moe.rukamori.archivetune.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class BrowseBody(
    val context: Context,
    val browseId: String?,
    val params: String?,
    val continuation: String?,
    val query: String? = null,
    val canonicalBaseUrl: String? = null,
    val formData: FormData? = null,
) {
    @Serializable
    data class FormData(
        val selectedValues: List<String>,
    )
}
