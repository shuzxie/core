package moe.rukamori.archivetune.innertube.pages

import moe.rukamori.archivetune.innertube.models.SongItem

data class PlaylistContinuationPage(
    val songs: List<SongItem>,
    val continuation: String?,
)
