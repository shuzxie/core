package moe.rukamori.archivetune.innertube.pages

import moe.rukamori.archivetune.innertube.models.Album
import moe.rukamori.archivetune.innertube.models.Artist
import moe.rukamori.archivetune.innertube.models.MusicResponsiveListItemRenderer
import moe.rukamori.archivetune.innertube.models.PlaylistItem
import moe.rukamori.archivetune.innertube.models.Runs
import moe.rukamori.archivetune.innertube.models.SongItem
import moe.rukamori.archivetune.innertube.models.oddElements
import moe.rukamori.archivetune.innertube.models.splitBySeparator
import moe.rukamori.archivetune.innertube.utils.parseTime

data class PlaylistPage(
    val playlist: PlaylistItem,
    val songs: List<SongItem>,
    val songsContinuation: String?,
    val continuation: String?,
    /** The playlist's own song count, which can exceed the rows served when some are unavailable. */
    val trackCount: Int? = null,
) {
    companion object {
        /**
         * The song count from the header's second subtitle, read by position because its words are
         * localized: "[views •] 49 tracks • 5+ hours", so the count is the group before the length.
         */
        fun trackCountFrom(secondSubtitle: Runs?): Int? {
            val groups = secondSubtitle?.runs?.splitBySeparator().orEmpty()
            if (groups.size < 2) return null
            return groups[groups.size - 2]
                .joinToString("") { it.text }
                .filter(Char::isDigit)
                .toIntOrNull()
        }

        fun fromMusicResponsiveListItemRenderer(renderer: MusicResponsiveListItemRenderer): SongItem? {
            return SongItem(
                id = renderer.videoId ?: return null,
                title =
                    renderer.flexColumns
                        .firstOrNull()
                        ?.musicResponsiveListItemFlexColumnRenderer
                        ?.text
                        ?.runs
                        ?.firstOrNull()
                        ?.text ?: return null,
                artists =
                    renderer.flexColumns
                        .getOrNull(1)
                        ?.musicResponsiveListItemFlexColumnRenderer
                        ?.text
                        ?.runs
                        ?.oddElements()
                        ?.map {
                            Artist(
                                name = it.text,
                                id = it.navigationEndpoint?.browseEndpoint?.browseId,
                            )
                        }.orEmpty(),
                album =
                    renderer.flexColumns.getOrNull(2)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.let {
                        Album(
                            name = it.text,
                            id = it.navigationEndpoint?.browseEndpoint?.browseId ?: return@let null,
                        )
                    },
                duration =
                    renderer.fixedColumns
                        ?.firstOrNull()
                        ?.musicResponsiveListItemFlexColumnRenderer
                        ?.text
                        ?.runs
                        ?.firstOrNull()
                        ?.text
                        ?.parseTime(),
                musicVideoType = renderer.musicVideoType,
                thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.getThumbnailUrl() ?: return null,
                explicit =
                    renderer.badges?.find {
                        it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                    } != null,
                endpoint =
                    renderer.overlay
                        ?.musicItemThumbnailOverlayRenderer
                        ?.content
                        ?.musicPlayButtonRenderer
                        ?.playNavigationEndpoint
                        ?.watchEndpoint,
                setVideoId = renderer.playlistItemData?.playlistSetVideoId ?: return null,
            )
        }
    }
}
