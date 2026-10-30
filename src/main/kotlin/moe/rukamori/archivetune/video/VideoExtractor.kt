package moe.rukamori.archivetune.video

import moe.rukamori.archivetune.player.stream.InnerTubeVideoStreamExtractor

object VideoExtractor {
    suspend fun getVideoUrl(videoId: String): String? {
        return try {
            val result = InnerTubeVideoStreamExtractor.extract(videoId, forceSabr = true)
            
            result?.videoFormats?.firstOrNull()?.url
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
