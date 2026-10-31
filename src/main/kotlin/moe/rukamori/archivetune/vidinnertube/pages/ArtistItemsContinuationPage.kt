package moe.rukamori.archivetune.innertube.pages

import moe.rukamori.archivetune.innertube.models.YTItem

data class ArtistItemsContinuationPage(
    val items: List<YTItem>,
    val continuation: String?,
)
