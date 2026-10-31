package moe.rukamori.archivetune.innertube.pages

import moe.rukamori.archivetune.innertube.models.YTItem

data class LibraryContinuationPage(
    val items: List<YTItem>,
    val continuation: String?,
)
