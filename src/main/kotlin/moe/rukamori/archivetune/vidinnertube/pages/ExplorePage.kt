package moe.rukamori.archivetune.innertube.pages

import moe.rukamori.archivetune.innertube.models.AlbumItem

data class ExplorePage(
    val newReleaseAlbums: List<AlbumItem>,
    val moodAndGenres: List<MoodAndGenres.Item>,
)
