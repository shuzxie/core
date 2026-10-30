package moe.rukamori.archivetune.video

import moe.rukamori.archivetune.innertube.YouTube
import moe.rukamori.archivetune.innertube.models.YouTubeClient

object VideoExtractor {

    suspend fun getVideoUrl(videoId: String): String? {
        return try {
            // Kita "pinjam" fungsi player() dari ArchiveTune
            val response = YouTube.player(
                videoId = videoId,
                client = YouTubeClient.WEB,
                setLogin = true
            ).getOrNull() ?: return null

            // Ambil semua format video & audio dari response
            val formats = response.streamingData?.adaptiveFormats ?: return null

            // Cari video muxed (yang ada audio & videonya jadi satu)
            // Atau ambil video resolution tertinggi + audio terpisah
            val videoFormat = formats
                .filter { !it.isAudio && it.url != null }
                .maxByOrNull { it.height ?: 0 }

            val audioFormat = formats
                .filter { it.isAudio && it.url != null }
                .maxByOrNull { it.bitrate }

            // Untuk eksperimen ini, kita coba ambil URL video yang udah ada audionya
            // (kadang YouTube ngasih format muxed di progressiveStreams)
            val progressive = response.streamingData?.formats
                ?.firstOrNull { it.url != null && !it.mimeType.contains("audio") }

            if (progressive?.url != null) {
                return progressive.url
            }

            // Kalau nggak ada yang muxed, kita ambil video aja (nanti audionya nggak muncul, tapi buat tes dulu)
            videoFormat?.url
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
